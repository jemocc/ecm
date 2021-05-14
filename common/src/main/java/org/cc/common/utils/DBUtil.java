package org.cc.common.utils;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

/**
 * @ClassName: DBUtil
 * @Description: TODO
 * @Author: CC
 * @Date 2021/5/14 17:58
 * @ModifyRecords: v1.0 new
 */
public class DBUtil {

    public static PreparedStatement prepare(Connection conn, String sql, Object[] args) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i+1, args[i]);
            }
        }
        return ps;
    }

    public static int insertRId(JdbcTemplate jdbcTemplate, String sql, Object[] args) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> prepare(conn, sql, args), keyHolder);
        return Objects.requireNonNullElse(keyHolder.getKey(), -1).intValue();
    }
}
