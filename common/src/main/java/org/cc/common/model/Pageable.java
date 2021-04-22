package org.cc.common.model;

public class Pageable {

    public Integer sid;
    public Integer page;
    public Integer size;

    public static String warp(Pageable pageable, String sql) {
        if (pageable != null && pageable.size != null && pageable.sid != null) {
            return sql.replace("where", "where id > " + pageable.sid + " ") + " limit " + pageable.size;
        } else if (pageable != null && pageable.size != null && pageable.page != null) {
            return sql + " limit " + (pageable.page * pageable.size) + "," +pageable.size;
        }
        return sql;
    }
}
