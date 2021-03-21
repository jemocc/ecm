package org.cc.common.component;

import org.cc.common.exception.GlobalException;
import org.cc.common.model.RspResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ResponseBody
@ControllerAdvice
public class ExceptionHandlerAdvice {
    private final Logger log = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler(Exception.class)
    public RspResult<Void> handlerException(Exception e) {
        log.error("服务异常: ", e);
        return RspResult.failure(500, e.getMessage());
    }

    @ExceptionHandler(GlobalException.class)
    public RspResult<Void> handlerGlobalException(GlobalException e) {
        return RspResult.failure(e.getCode(), e.getMessage());
    }
}
