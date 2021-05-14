package org.cc.common.component.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.cc.common.component.DistributeSynchronized;
import org.cc.common.component.RedisOperator;
import org.cc.common.exception.GlobalException;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName: DistributeSynchronizedAop
 * @Description: TODO
 * @Author: CC
 * @Date 2021/4/29 11:13
 * @ModifyRecords: v1.0 new
 */
@Aspect
@Component
public class DistributeSynchronizedAop {
    private final Logger log = LoggerFactory.getLogger(DistributeSynchronizedAop.class);

    @Around("@annotation(ann)")
    public Object interceptor(ProceedingJoinPoint joinPoint, DistributeSynchronized ann) throws Throwable {
        String key = getLockKey(joinPoint, ann);
        RLock lock = RedisOperator.getLock(key);
        if (lock.tryLock(ann.waitTime(), ann.leaseTime(), TimeUnit.SECONDS)) {
            log.info("get lock: {}", key);
            Object result = joinPoint.proceed();
            lock.unlock();
            return result;
        } else {
            throw new GlobalException(50101, "获取执行资源失败，请稍后再试");
        }
    }

    private String getLockKey(ProceedingJoinPoint joinPoint, DistributeSynchronized ann) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && (args[0] instanceof String || args[0] instanceof Number)) {
            return ann.lockKey() + ":" + args[0].toString();
        } else {
            return ann.lockKey();
        }
    }
}
