package org.cc.common.component.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.cc.common.component.FlowTrack;
import org.cc.common.utils.JsonUtil;
import org.cc.common.utils.SequenceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


/**
 * @ClassName: FlowTrackAop
 * @Description: TODO
 * @Author: chencheng
 * @Date 2021/4/30 14:30
 * @ModifyRecords: v1.0 new
 */
public class FlowTrackAop {
    private final Logger log = LoggerFactory.getLogger(FlowTrackAop.class);

    @Around("@annotation(ann)")
    public Object interceptor(ProceedingJoinPoint joinPoint, FlowTrack ann) throws Throwable {
        MDC.put("traceId", "" + SequenceGenerator.newSeq());
        if (ann.isLogInput() && joinPoint.getArgs() != null) {
            log.info("input: {}", JsonUtil.bean2Json(joinPoint.getArgs()));
        }
        Object result = joinPoint.proceed();
        if (ann.isLogOutput()) {
            log.info("output: {}", result == null ? null : JsonUtil.bean2Json_FN(result));
        }
        return result;
    }

}
