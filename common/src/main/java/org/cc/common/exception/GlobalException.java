package org.cc.common.exception;

public class GlobalException extends RuntimeException{
    private final int code;
    private final String message;

    public GlobalException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "{\"code\":" + code + ",\"message\":\"" + message + "\"}";
    }
}
