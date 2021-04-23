package org.cc.fileserver.thread;

import org.cc.common.config.ThreadPool;
import org.cc.common.utils.PublicUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

public class DownloadWatch implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DownloadWatch.class);

    @Override
    public void run() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) ThreadPool.getExecutor();
        while (true) {
            log.info("thread pool active {}/{}", executor.getActiveCount(), executor.getMaximumPoolSize());
            PublicUtil.sleep(1000);
        }
    }
}
