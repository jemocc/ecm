package org.cc.common.utils;

import com.alibaba.nacos.api.config.filter.IFilterConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

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

    public static int update(JdbcTemplate jdbcTemplate, String sql, Map<String, Object> updateCols, boolean skipNull, Object[] whereCond) {
        StringBuilder sb = new StringBuilder();
        List<Object> args = new ArrayList<>(updateCols.size());
        updateCols.forEach((k, v) -> {
            if (v != null || !skipNull) {
                sb.append(k).append("=?,");
                args.add(v);
            }
        });
        if (sb.length() == 0)
            return 0;
        sql = sql.replaceAll("set.*?where", "set " + sb.deleteCharAt(sb.length() - 1).toString() + " where");
        args.addAll(Arrays.asList(whereCond));
        return jdbcTemplate.update(sql, args);
    }
}
