package org.cc.common.exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

@JsonSerialize(using = UserOAuth2ExceptionSerializer.class)
public class MyOauthException extends OAuth2Exception {
    private int code;

    private String message;

    public MyOauthException(String msg, Throwable t) {
        super(msg, t);
    }

    public MyOauthException(String msg) {
        super(msg);
    }

    public static MyOauthException failure(Integer code, String message) {
        MyOauthException exception = new MyOauthException(message);
        exception.code = code;
        exception.message = message;
        return exception;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
