package org.cc.common.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.*;

public class ThreadPool {

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
        executor = es;
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

