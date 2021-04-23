package org.cc.fileserver.thread;

import org.cc.common.config.ThreadPool;
import org.cc.common.exception.GlobalException;
import org.cc.fileserver.entity.CacheFile;
import org.cc.fileserver.entity.enums.FileFormType;
import org.cc.fileserver.model.HttpFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class DownFileTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DownFileTask.class);
    final CacheFile file;
    final CountDownLatch cdl;

    public DownFileTask(CacheFile file, CountDownLatch cdl) {
        this.file = file;
        this.cdl = cdl;
    }

    @Override
    public void run() {
        String remoteUri = file.getUri();
        try {
            HttpFileHelper helper = HttpFileHelper.uri(remoteUri).toFile().down();
            file.setFormType(FileFormType.LOCAL);
            file.setUri(helper.getLocalUri());
        } catch (GlobalException e) {
            log.error(e.getMessage());
        } finally {
            cdl.countDown();
        }
    }
}
