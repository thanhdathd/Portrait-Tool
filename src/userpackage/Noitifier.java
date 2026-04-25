package userpackage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Date;
import java.sql.Time;

public class Noitifier {
    Object ob;
    private static RandomAccessFile f;

    public Noitifier() {
        this.ob = "Not set";

        try {
            f = new RandomAccessFile("App-log.txt", "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public Noitifier(Object ob) {
        this.ob = ob;
    }

    public static void printConsole(Object ob) {
        System.out.println(ob);

//        try {
//            byte[] b = new byte[]{13, 10};
//            f = new RandomAccessFile("log.txt", "rw");
//            if (f.length() == 0L) {
//                Time t = new Time(System.currentTimeMillis());
//                Date d = new Date(System.currentTimeMillis());
//                f.writeBytes("[Portrait Tool - log] - " + t + " - " + d);
//                f.write(b);
//            }
//
//            f.seek(f.length());
//            f.write(ob.toString().getBytes());
//            f.write(b);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }
}
