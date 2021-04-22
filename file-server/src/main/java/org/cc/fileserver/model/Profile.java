package org.cc.fileserver.model;

import org.cc.fileserver.config.SelfConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

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

}
