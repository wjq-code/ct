package com.uek.producer;

import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 类名称: Model
 * 类描述: 电信客服模拟数据，主叫手机号，主叫姓名，被叫手机号，被叫姓名，通话建立的时间，通话时长
 *
 * @author 武建谦
 * @Time 2019/4/26 14:29
 */
public class Model {

    static Random random = new Random();
    //设置时间的区间范围
    String startTime = "2018-01-01 00:00:00";
    String endTime = "2018-12-31 23:59:59";

    /**
     * 模拟手机号码
     *
     * @return
     */
    public static String phoneNumber(String fileName) {
        String phoneNumber = null;
        String phoneName = null;
        List<String> phoneList = new ArrayList<>();
        // 封装手机号 以及姓名
        HashMap<String, String> phoneNameMap = new HashMap<>();

        String caller = null;
        String callerName = null;
        String callee = null;
        String calleeName = null;
        File file = new File(fileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null) {
                phoneNumber = line.split("\t")[0];
                phoneName = line.split("\t")[1];
                phoneList.add(phoneNumber);
                phoneNameMap.put(phoneNumber, phoneName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 随机索引 获取随机手机号
        int index = random.nextInt(phoneList.size());
        caller = phoneList.get(index);
        callerName = phoneNameMap.get(caller);
        while (true) {
            int calleeIndex = random.nextInt(phoneList.size());
            callee = phoneList.get(calleeIndex);
            calleeName = phoneNameMap.get(callee);
            if (!caller.equals(callee)) {
                break;
            }
        }
        StringBuffer sb = new StringBuffer();
        sb.append(caller).append(",").append(callerName).append(",").append(callee).append(",").append(calleeName);
        return sb.toString();
    }

    /**
     * 根据传入的时间区间，在范围内随机建立通话时间
     * 随机时间: startTimeTS + (endTimeTS - startTimeTS) * Math.random() [0-1)
     *
     * @param startTime
     * @param endTime
     */
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

    /**
     * 随机生成通话时长
     *
     * @return
     */
    public static String randomDuration() {
        int num = random.nextInt(1999);
        // 300
        DecimalFormat df = new DecimalFormat("0000");
        // 0300
        String duration = df.format(num);
        return duration;
    }

    /**
     * 拼接数据
     *
     * @return
     */
    public String product(String filePath) {
        String phoneNumber = phoneNumber(filePath);
        String buildTime = randomBuildTime(startTime, endTime);
        String duration = randomDuration();
        return phoneNumber + "," + buildTime + "," + duration;
    }

    public void writeLog(String filePath1, String filePath2) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filePath2));
            while (true) {
                Thread.sleep(100);
                String log = product(filePath1);
                System.out.println(log);
                osw.write(log);
                osw.write("\n");
                //把缓冲中的数据刷入到本地
                osw.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Model model = new Model();
//        String product = model.product("File/input.csv");
//        System.out.println(product);
        model.writeLog(args[0], args[1]);
    }
}
