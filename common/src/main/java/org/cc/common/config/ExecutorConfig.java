package org.cc.common.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: ExecutorConfig
 * @Description: TODO
 * @Author: CC
 * @Date 2021/4/18 11:05
 * @ModifyRecords: v1.0 new
 */
public class ExecutorConfig {

//    static final class Inner {
//        private static final ExecutorService es = new ThreadPoolExecutor(2, 100, 2, TimeUnit.SECONDS,
//                new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
//    }
//
//    public static ExecutorService getExecutor () {
//        return Inner.es;
//    }

    public static ExecutorService getNewExecutor () {
        return new ThreadPoolExecutor(5, 200, 2, TimeUnit.SECONDS,
                new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
