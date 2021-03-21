package org.cc.common.model;

import java.io.Serializable;

public class RspResult<T> implements Serializable {
    private int code;
    private String message;
    private T data;

    public static <T> RspResult<T> ok(T data) {
        RspResult<T> r = new RspResult<>();
        r.code = 0;
        r.message = "操作成功";
        r.data = data;
        return r;
    }

    public static <T> RspResult<T> failure(int code, String message) {
        RspResult<T> r = new RspResult<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public RspResult<T> msg(String message) {
        this.message = message;
        return this;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
