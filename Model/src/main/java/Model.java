import com.alibaba.fastjson.JSONObject;

import javax.crypto.Cipher;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 类名称: Model
 * 类描述: 用于模拟生成数据：
 * 城市(city)	岗位名称(career)	岗位数量(careerCount)	薪资(salary)
 *
 * @author 武建谦
 * @Time 2019/4/29 16:57
 */
public class Model {

    private static Random random = new Random();
    private static DataDao data = new DataDao();
    //设置时间的区间范围
    private static String startTime = "2018-01-01 00:00:00";
    private static String endTime = "2018-12-31 23:59:59";
    private static String[] citys = new String[]{"北京", "北京", "北京", "广州", "广州", "广州", "天津", "天津", "深圳", "深圳", "深圳", "上海", "上海", "上海", "成都", "成都", "杭州", "杭州", "杭州", "合肥", "武汉", "西安", "郑州", "哈尔滨", "长春", "长沙", "大连", "福州", "济南", "兰州", "南京", "宁波", "青岛", "沈阳", "石家庄", "苏州", "重庆", "太原"};
    private static String[] careers = new String[]{"大数据开发工程师(etl),9k", "大数据开发工程师(etl),12k", "大数据开发工程师(etl),10k", "大数据开发工程师(spark),12k", "大数据开发工程师(spark),15k", "大数据开发工程师(spark),8k", "大数据项目经理,25k", "大数据讲师,13k", "大数据分析工程师,15k", "大数据地图工程师,11k", "大数据专家/架构师,40k", "大数据研发专家,22k", "大数据开发工程师,8k", "大数据运维工程师,16k", "大数据产品经理,25k", "大数据应用架构师,18k", "大数据讲师,15k", "资深大数据工程师,24k", "高级大数据开发,28k", "模特,8k", "大数据测试,10k", "设计师,10k", "java工程师,12k","前端工程师,13k","微信小程序工程师,10k","工程师,10k","PHP工程师,12k", "大数据开发工程师,11k","大数据开发工程师,15k", "大数据开发工程师,12k"};

    //模拟城市信息
    private static String getCity() {
        int index = random.nextInt(citys.length);
        List<String> cityList = Arrays.asList(citys);
        String city = cityList.get(index);
        return city;
    }

    //模拟岗位名称
    private static String getCareerAndSalary() {
        int index = random.nextInt(careers.length);
        List<String> careersList = Arrays.asList(careers);
        String career = careersList.get(index);
        return career;
    }
    //生成随机时间
    public static String randomBuildTime(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long startTimeTS = 0L;
        long endTimeTS = 0L;
        long randomTimeTS = 0L;
        try {
            Date start_TimeTS = sdf.parse(startTime);
            startTimeTS = start_TimeTS.getTime();
            Date end_TimeTS = sdf.parse(endTime);
            endTimeTS = end_TimeTS.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        randomTimeTS = (long) (startTimeTS + (endTimeTS - startTimeTS) * Math.random());
        return sdf.format(new Date(randomTimeTS));
    }
    //对数据进行拼接
    public static DataDao product(){
        String city = getCity();
        String careerAndSalary = getCareerAndSalary();
        String dataTime = randomBuildTime(startTime, endTime);

        data.setCity(city);
        data.setCareerAndSalary(careerAndSalary);
        data.setDataTime(dataTime);
        return data;
    }

    //写入到文件中
    public static void writeLog(DataDao data,String filePath){
        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filePath,true));
            String line = data.toString();
            osw.write(line);
            osw.write("\n");
            osw.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args) throws InterruptedException {
        System.out.println("数据正在写入  " + args[0] + "   中");
        while (true){
            Thread.sleep(10);
            DataDao product = product();
//            writeLog(product, "File/output.json");
            writeLog(product, args[0]);
        }
    }
}
