package org.cc.common.model;

import java.util.List;

public class Page<T> {
    private Integer tc;
    private List<T> data;

    public static <T> Page<T> of(int tc, List<T> data) {
        Page<T> page = new Page<>();
        page.tc = tc;
        page.data = data;
        return page;
    }

    public Integer getTc() {
        return tc;
    }

    public List<T> getData() {
        return data;
    }
}
