package org.cc.common.model;

import org.cc.common.exception.MyOauthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.DefaultThrowableAnalyzer;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.web.util.ThrowableAnalyzer;

public class MyWebResponseExceptionTranslator implements WebResponseExceptionTranslator<OAuth2Exception> {
    private final ThrowableAnalyzer throwableAnalyzer = new DefaultThrowableAnalyzer();
    private final Logger log = LoggerFactory.getLogger(MyWebResponseExceptionTranslator.class);
    @Override
    public ResponseEntity<OAuth2Exception> translate(Exception e) {
        Throwable[] causeChain = this.throwableAnalyzer.determineCauseChain(e);
        Exception ase = (OAuth2Exception)this.throwableAnalyzer.getFirstThrowableOfType(OAuth2Exception.class, causeChain);
        MyOauthException exception;
        String errorMsg;
        if (ase == null)
            errorMsg = "服务异常";
        else
            errorMsg = ase.getMessage() == null ? "" : ase.getMessage();
        if (errorMsg.contains("密码") || errorMsg.contains("用户"))
            exception = MyOauthException.failure(401, errorMsg);
        else if (errorMsg.startsWith("Invalid access token")) {
            exception = MyOauthException.failure(403, "登陆凭证过期");
        } else {
            log.error("Authenticate failure: ", ase);
            exception = MyOauthException.failure(501, "鉴权失败");
        }
        return handleOAuth2Exception(exception);
    }

    private ResponseEntity<OAuth2Exception> handleOAuth2Exception(MyOauthException e) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cache-Control", "no-store");
        headers.set("Pragma", "no-cache");

        return new ResponseEntity<>(e, headers, HttpStatus.OK);
    }

}
