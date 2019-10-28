import com.sun.tools.hat.internal.parser.ReadBuffer;
import com.sun.xml.internal.xsom.impl.scd.Iterators;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 类名称: Test
 * 类描述:
 *
 * @author 武建谦
 * @Time 2019/7/31 12:53
 */
public class Test {
    public static void main(String[] args) throws IOException {
        File file = new File("test.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = null;
        String kemu;
        Map<String, String> map1 = new HashMap<String, String>();
        Map<String, String> map2 = new HashMap<String, String>();
        ArrayList<String> list = new ArrayList();
        ArrayList<String> list2 = new ArrayList();
        String name = null;
        while ((line = br.readLine()) != null) {
            String[] s = line.split(" ");
            name = s[0];
            kemu = s[1];
            String source = s[2];
            list.add(kemu);
            list2.add(source);
        }
        System.out.println(name + " " + list.toString());
        System.out.println(name + " " + list2.toString());
    }
}
