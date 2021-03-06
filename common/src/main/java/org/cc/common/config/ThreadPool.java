package org.cc.common.config;

import java.util.concurrent.*;

public class ThreadPool {

    static final class Inner {
        private static final ExecutorService es = new ThreadPoolExecutor(5, 200, 10, TimeUnit.SECONDS,
                new SynchronousQueue<>(), new BlockingRejectedExecutionHandler());
    }

    public static ExecutorService getExecutor () {
        return Inner.es;
    }

    public static void submit(Runnable runnable) {
        getExecutor().submit(runnable);
    }

    public static ExecutorService getNewExecutor () {
        return new ThreadPoolExecutor(5, 200, 5, TimeUnit.SECONDS,
                new SynchronousQueue<>(), new BlockingRejectedExecutionHandler());
    }
}

class BlockingRejectedExecutionHandler implements RejectedExecutionHandler {
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