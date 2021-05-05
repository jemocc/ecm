package org.cc.common.model;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class PageQuery {
    private Pageable pageable;
    private String sql;

    public static PageQuery of(Pageable pageable, String sql) {
        PageQuery pq = new PageQuery();
        pq.pageable = pageable;
        pq.sql = sql;
        return pq;
    }

    public <T> Page<T> exec(JdbcTemplate jdbcTemplate, Class<T> t) {
        String querySql = Pageable.warp(pageable, sql);
        String countSql = sql.replaceAll("select (.*?(?=from))from", "select count(*) from");
        Integer c = jdbcTemplate.queryForObject(countSql, Integer.class);
        assert c != null;
        List<T> data = jdbcTemplate.query(querySql, new BeanPropertyRowMapper<>(t));
        return Page.of(c, data);
    }
}
