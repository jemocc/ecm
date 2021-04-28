package org.cc.fileserver.thread;

import org.cc.common.config.ThreadPool;
import org.cc.common.utils.PublicUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class DownloadWatch implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DownloadWatch.class);

    @Override
    public void run() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) ThreadPool.getExecutor();
        while (true) {
            log.info("thread pool active {}/{}", executor.getActiveCount(), executor.getMaxPoolSize());
            PublicUtil.sleep(1000);
        }
    }
}
