package org.cc.fileserver.thread;

import org.cc.common.config.ThreadPool;
import org.cc.fileserver.entity.CacheFile;
import org.cc.fileserver.entity.enums.FileFormType;
import org.cc.fileserver.utils.HttpFileUtil;

import java.util.concurrent.CountDownLatch;

public class DownFileTask implements Runnable {
    private final CacheFile file;
    private final CountDownLatch cdl;

    public DownFileTask(CacheFile file, CountDownLatch cdl) {
        this.file = file;
        this.cdl = cdl;
    }

    @Override
    public void run() {
        String remoteUri = file.getUri();
        if (remoteUri.endsWith(".m3u8")) {
            M3u8DownTask task = new M3u8DownTask(file, cdl);
            ThreadPool.submit(task);
        } else {
            try {
                String localUri = HttpFileUtil.down(remoteUri);
                file.setUri(localUri);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            } finally {
                cdl.countDown();
            }
        }
    }
}
