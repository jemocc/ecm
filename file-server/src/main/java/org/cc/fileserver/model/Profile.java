package org.cc.fileserver.model;

import org.cc.fileserver.config.SelfConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class Profile implements ApplicationContextAware {
    private static ApplicationContext context;
    private static SelfConfig config;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
        config = getBean(SelfConfig.class);
    }

    public static <T> T getBean(Class<T> c) {return context.getBean(c); }

    public static String getConfigPath() {
        return config.getLocalFilePath();
    }

    public static Integer getDownFilePartMaxSize() { return Objects.requireNonNullElse(config.getDownFilePartMaxSize(), 2097152); }

    public static Integer getDownFilePartMaxNum() { return Objects.requireNonNullElse(config.getDownFilePartMaxNum(), 10); }

    public static Integer getDownFileConnectTimeout() {
        return Objects.requireNonNullElse(config.getDownFileConnectTimeout(), 3000);
    }

    public static Integer getDownFileReadTimeout() {
        return Objects.requireNonNullElse(config.getDownFileReadTimeout(), 3000);
    }

    public static Integer getDownFileMaxRetry() {
        return Objects.requireNonNullElse(config.getDownFileMaxRetry(), 10);
    }
}
