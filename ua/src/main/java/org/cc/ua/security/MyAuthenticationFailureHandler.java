package org.cc.ua.security;

import com.alibaba.fastjson.JSON;
import org.cc.common.model.RspResult;
import org.cc.common.utils.PlatformUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getOutputStream().write(PlatformUtil.toJSONStr(RspResult.failure(401, exception.getMessage())).getBytes());
    }
}
