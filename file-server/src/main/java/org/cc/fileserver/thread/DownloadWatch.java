package org.cc.fileserver.thread;

import org.cc.common.config.ThreadPool;

import java.util.concurrent.ThreadPoolExecutor;

public class DownloadWatch implements Runnable {

    @Override
    public void run() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) ThreadPool.getExecutor();
        while (true) {
            try {
                System.out.println("\t\tactive:"+executor.getActiveCount());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
