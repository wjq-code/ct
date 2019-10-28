package outputformat;

import converter.DimensionConverterImpl;
import kv.key.ComDimension;
import kv.value.CountDurationValue;
import utils.JDBCInstance;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import utils.JDBCUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MysqlOutputFormat extends OutputFormat<ComDimension, CountDurationValue>{
    private OutputCommitter committer = null;
    //用于返回一个RecordWriter的实例，Reduce任务在执行的时候就是利用这个实例来输出Key/Value的。
    @Override
    public RecordWriter<ComDimension, CountDurationValue> getRecordWriter(TaskAttemptContext context)
            throws IOException, InterruptedException {
        //初始化JDBC连接器对象
        Connection conn = null;
        conn = JDBCInstance.getInstance();
        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        return new MysqlRecordWriter(conn);
    }

    /**
     * checkOutputSpecs是 在JobClient提交Job之前被调用的（在使用InputFomat进行输入数据划分之前），
     * 用于检测Job的输出路径。比如，FileOutputFormat通过这个方法来确认在Job开始之前，
     * Job的Output路径并不存在，然后该方法又会重新创建这个Output 路径。
     * 这样一来，就能确保Job结束后，Output路径下的东西就是且仅是该Job输出的。
     */
    @Override
    public void checkOutputSpecs(JobContext context) throws IOException, InterruptedException {
        //输出校检
    }
    //用于返回一个OutputCommitter的实例
    @Override
    public OutputCommitter getOutputCommitter(TaskAttemptContext context) throws IOException, InterruptedException {
        if(committer == null){
            String name = context.getConfiguration().get(FileOutputFormat.OUTDIR);
            Path outputPath = name == null ? null : new Path(name);
            committer = new FileOutputCommitter(outputPath, context);
        }
        return committer;
    }

    static class MysqlRecordWriter extends RecordWriter<ComDimension, CountDurationValue> {
        private DimensionConverterImpl dci = new DimensionConverterImpl();
        private Connection conn = null;
        private PreparedStatement preparedStatement = null;
        private String insertSQL = null;
        private int count = 0;
        private final int BATCH_SIZE = 500;
        public MysqlRecordWriter(Connection conn) {
            this.conn = conn;
        }

        @Override
        public void write(ComDimension key, CountDurationValue value) throws IOException, InterruptedException {
            try {
                //tb_call
                //id_date_contact, id_date_dimension, id_contact, call_sum, call_duration_sum

                //year month day
                int idDateDimension = dci.getDimensionID(key.getDateDimension());
                //telephone name
                int idContactDimension = dci.getDimensionID(key.getContactDimension());

                String idDateContact = idDateDimension + "_" + idContactDimension;

                int callSum = Integer.valueOf(value.getCallSum());
                int callDurationSum = Integer.valueOf(value.getCallDurationSum());

                if(insertSQL == null){
                    insertSQL = "INSERT INTO `tb_call` (`id_date_contact`, `id_date_dimension`, `id_contact`, `call_sum`, `call_duration_sum`) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `id_date_contact` = ?;";
                }

                if(preparedStatement == null){
                    preparedStatement = conn.prepareStatement(insertSQL);
                }
                //本次SQL
                int i = 0;
                preparedStatement.setString(++i, idDateContact);
                preparedStatement.setInt(++i, idDateDimension);
                preparedStatement.setInt(++i, idContactDimension);
                preparedStatement.setInt(++i, callSum);
                preparedStatement.setInt(++i, callDurationSum);
                //无则插入，有则更新的判断依据
                preparedStatement.setString(++i, idDateContact);
                preparedStatement.addBatch();
                count++;
                if(count >= BATCH_SIZE){
                    preparedStatement.executeBatch();
                    conn.commit();
                    count = 0;
                    preparedStatement.clearBatch();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void close(TaskAttemptContext context) throws IOException, InterruptedException {
            try {
                if(preparedStatement != null){
                    preparedStatement.executeBatch();
                    this.conn.commit();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }finally {
                JDBCUtil.close(conn, preparedStatement, null);
            }
        }
    }
}
