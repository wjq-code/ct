package com.uek.test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 类名称: Test
 * 类描述:
 *
 * @author 武建谦
 * @Time 2019/4/26 14:33
 */
public class Test {
    String phoneNumber;
    List<String> list = new ArrayList<>();
    @org.junit.Test
    public void phoneNumber() {
        File file = new File("File/test.csv");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((phoneNumber = br.readLine()) != null){
                list.add(phoneNumber);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int i = 0;
        while (i < 50){
            i++;
            int index = new Random().nextInt(list.size());
            String s = list.get(index);
            System.out.println(s);
        }
    }
}
