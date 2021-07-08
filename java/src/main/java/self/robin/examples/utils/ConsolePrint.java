package self.robin.examples.utils;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2021/5/17 11:50
 */
public class ConsolePrint {

    public static void main(String[] args) throws Exception {

        char[] list = new char[]{'\\','|','/','-'};

//        for (int i = 0; i < 100; i++) {
//            System.out.print( "\b"+list[i%4]);
//            Thread.sleep(1000L);
//        }

        System.out.println("abc");
        System.out.println("def");



        ProcessBuilder pb = new ProcessBuilder("cmd");
        Process p = pb.start();

        OutputStream out = p.getOutputStream();
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(out));
        w.write("sadfasdfsd");
        w.flush();
        w.write("cls");
        w.flush();
        w.write("fffffff");
        w.flush();
        Thread.sleep(10000);
        w.close();
        out.close();
        //System.out.print( "\112\2\3\4\5\6\7\0");


    }
}
