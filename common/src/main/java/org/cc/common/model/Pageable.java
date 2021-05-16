package org.cc.common.model;

import java.io.Serializable;

public class Pageable implements Serializable {
    private Integer sid;
    private Integer page = 0;
    private Integer size = 20;

    public static Pageable of(int sid, int size){
        Pageable pageable = new Pageable();
        pageable.sid = sid;
        pageable.size = size;
        return pageable;
    }

    public static Pageable ofPage(int page, int size){
        Pageable pageable = new Pageable();
        pageable.page = page;
        pageable.size = size;
        return pageable;
    }

    public static String warp(Pageable pageable, String sql) {
        if (pageable == null)
            return sql;
        if (pageable.sid != null) {
            return pageable.appendWhere(sql) + " limit " + pageable.size;
        } else {
            return sql + " limit " + (pageable.page * pageable.size) + "," +pageable.size;
        }
    }

    private String appendWhere(String sql) {
        if (sql.contains("where")) {
            return sql.replace("where", "where id > " + sid + " and");
        } else if (sql.contains("order by")) {
            return sql.replace("order by", "where id > " + sid + " order by");
        } else {
            return sql + " where id > " + sid;
        }
    }

    public Integer getPage() {
        return page;
    }

    public Integer getSize() {
        return size;
    }
}
