package org.cc.common.utils;

import com.sun.istack.NotNull;
import org.apache.http.util.Asserts;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PublicUtil {

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException("线程中断");
        }
    }

    public static void sleep(long start, long waitTotalTime) {
        long sleep = waitTotalTime - (System.currentTimeMillis() - start);
        if (sleep > 0) {
            sleep(sleep);
        }
    }

    public static void wait(Object obj, long start, long waitTotalTime) {
        try {
            long wait = waitTotalTime - (System.currentTimeMillis() - start);
            obj.wait(wait);
        } catch (InterruptedException e) {
            throw new RuntimeException("线程中断");
        }
    }

    public static void close(OutputStream os) {
        try {
            if (os != null)
                os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Number> List<T> splitStr(String s, @NotNull String sp, @NotNull Class<T> c){
        if (s == null || s.length() == 0)
            return new ArrayList<>(0);
        String[] ss = s.split(sp);
        if (Integer.class.isAssignableFrom(c)) {
            return Arrays.stream(ss).map(i -> (T)Integer.valueOf(i)).collect(Collectors.toList());
        } else if (Long.class.isAssignableFrom(c)) {
            return Arrays.stream(ss).map(i -> (T)Long.valueOf(i)).collect(Collectors.toList());
        } else {
            throw new RuntimeException("The number parser of " + c.getName() + " can not be found.");
        }

    }
}
