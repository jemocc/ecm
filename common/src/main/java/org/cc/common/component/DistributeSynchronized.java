package org.cc.common.component;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


/**
 * @ClassName: DistributeSynchronized
 * @Description: TODO
 * @Author: CC
 * @Date 2021/4/29 11:11
 * @ModifyRecords: v1.0 new
 */
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributeSynchronized {

    @AliasFor("lockKey")
    String value() default "DEFAULT";

    @AliasFor("value")
    String lockKey() default "DEFAULT";

    long waitTime() default  3;     //s

    long leaseTime() default  60;   //s
}
