package org.cc.common.model;

import java.io.Serializable;
import java.util.List;

public class Page<T> implements Serializable {
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
