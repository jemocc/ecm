package org.cc.common.config;

import org.cc.common.exception.GlobalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.*;

@Configuration
@EnableAsync
public class ThreadPool implements AsyncConfigurer {
    private static final Logger log = LoggerFactory.getLogger(ThreadPool.class);
    private static final Executor executor;

    static {
        ThreadPoolTaskExecutor es = new ThreadPoolTaskExecutor();
        es.setCorePoolSize(4);
        es.setMaxPoolSize(50);
        es.setQueueCapacity(1000);
        es.setThreadNamePrefix("Async-");
        es.setAllowCoreThreadTimeOut(true);
        es.setRejectedExecutionHandler(new BlockingRejectedExecutionHandler());
        es.setTaskDecorator(new ContextCopyingDecorator());
        es.initialize();
        executor = es;
    }

    @Override
    public Executor getAsyncExecutor() {
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            if (throwable instanceof GlobalException) {
                log.error("Async exec error, method: [{}], ex: {}", method, throwable.toString());
            } else {
                log.error("Async exec error, method: [{}], ex:", method, throwable);
            }
        };
    }

    static class ContextCopyingDecorator implements TaskDecorator {

        @NonNull
        @Override
        public Runnable decorate(@NonNull Runnable runnable) {
            Map<String, String> mdcContext = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    if (mdcContext != null) {
                        MDC.setContextMap(mdcContext);
                    }
                    runnable.run();
                } finally {
                    MDC.clear();
                }
            };
        }
    }

    static class BlockingRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (!executor.isShutdown()) {
                try {
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    executor.execute(r);
                }
            }
        }
    }

    public static Executor getExecutor () {
        return executor;
    }

    public static Future<?> submit(Runnable runnable) {
        return ((ThreadPoolTaskExecutor)executor).submit(runnable);
    }

}

