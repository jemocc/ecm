package org.cc.common.component;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface FlowTrack {
    String value();

    boolean isLogInput() default false;

    boolean isLogOutput() default false;
}
