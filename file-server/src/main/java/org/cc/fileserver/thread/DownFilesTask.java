package org.cc.fileserver.thread;

import org.cc.common.config.ThreadPool;
import org.cc.fileserver.entity.CacheFile;
import org.cc.fileserver.entity.enums.FileFormType;
import org.cc.fileserver.model.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DownFilesTask implements Runnable {
    private final List<CacheFile> files;
    private final String callback;
    private final CountDownLatch cdl;

    public DownFilesTask(List<CacheFile> files, String callback) {
        this.files = files;
        this.callback = callback;
        this.cdl = new CountDownLatch(files.size());
    }

    @Override
    public void run() {
        files.forEach(i -> ThreadPool.submit(new DownFileTask(i, cdl)));
        try {
            cdl.await();
            execCallback();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void execCallback() {
        if (callback == null)
            return;
        JdbcTemplate jdbcTemplate = Profile.getBean(JdbcTemplate.class);
        List<Object[]> args = new ArrayList<>();
        files.forEach(i -> {
            if (i.getFormType() == FileFormType.LOCAL)
                args.add(new Object[]{i.getUri(), i.getId()});
        });
        jdbcTemplate.batchUpdate(callback, args);
    }
}
