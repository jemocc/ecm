package org.cc.common.config;

import com.google.gson.JsonObject;
import org.cc.common.component.WSService;
import org.cc.common.exception.GlobalException;
import org.cc.common.model.EventMessage;
import org.cc.common.pojo.EventMessageType;
import org.cc.common.utils.DateTimeUtil;
import org.cc.common.utils.PublicUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.*;

@Configuration
@EnableAsync
public class ThreadPool implements AsyncConfigurer {
    private static final Logger log = LoggerFactory.getLogger(ThreadPool.class);
    private static final Executor executor;
    private static Future<?> watchFuture;

    static {
        ThreadPoolTaskExecutor es = new ThreadPoolTaskExecutor();
        es.setCorePoolSize(4);
        es.setMaxPoolSize(300);
        es.setQueueCapacity(1);
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

    public static synchronized void openWatch() {
        if (watchFuture == null) {
            watchFuture = submit(new ExecutorWatch());
            log.info("开启线程池监控");
        }
    }

    public static synchronized void closeWatch() {
        if (watchFuture != null) {
            watchFuture.cancel(true);
            log.info("停止线程池监控");
            watchFuture = null;
        }
    }

    static class ExecutorWatch implements Runnable {
        @Override
        public void run() {
            long start;
            ThreadPoolTaskExecutor executor1 = (ThreadPoolTaskExecutor) executor;
            while (true) {
                start = System.currentTimeMillis();
                JsonObject object = new JsonObject();
                object.addProperty("core", executor1.getCorePoolSize());
                object.addProperty("time", DateTimeUtil.DEFAULT_TIME_FORMATTER.format(LocalTime.now()));
                object.addProperty("active", executor1.getActiveCount());
                EventMessage<JsonObject> msg = new EventMessage<>(EventMessageType.THREAD_POOL_WATCH, object);
                WSService.sendMessageToWatcher(msg);
                long sleep = 1000 - (System.currentTimeMillis() - start);
                if (sleep > 0) {
                    try {
                        PublicUtil.sleep(Long.valueOf(sleep).intValue());
                    } catch (RuntimeException e) {
                        break;
                    }
                }
            }
        }
    }
}

