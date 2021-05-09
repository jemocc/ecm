package org.cc.common.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PublicUtil {

    public static PreparedStatement prepare(Connection conn, String sql, Object[] args) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i+1, args[i]);
            }
        }
        return ps;
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException("线程中断");
        }
    }

    public static void wait(long start, long waitTotalTime) {
        long sleep = waitTotalTime - (System.currentTimeMillis() - start);
        if (sleep > 0) {
            sleep(sleep);
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
}
