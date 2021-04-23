package org.cc.fileserver.thread;

import org.cc.common.config.ThreadPool;
import org.cc.fileserver.entity.CacheFile;
import org.cc.fileserver.entity.Video;
import org.cc.fileserver.entity.enums.FileFormType;

import java.util.ArrayList;
import java.util.List;

public class DownVideosTask extends DownFilesTask {
    public DownVideosTask(List<CacheFile> files, String callback) {
        super(files, callback);
    }

    @Override
    public void run() {
        files.forEach(i -> {
            if (i.getUri().endsWith(".m3u8"))
                ThreadPool.submit(new DownM3u8Task(i, cdl));
            else
                ThreadPool.submit(new DownFileTask(i, cdl));
        });
        try {
            cdl.await();
            if (callback != null)
                execCallback();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    List<Object[]> getArgs() {
        List<Object[]> args = new ArrayList<>();
        files.forEach(i -> {
            Video v = (Video)i;
            if (i.getFormType() == FileFormType.LOCAL)
                args.add(new Object[]{v.getUri(), v.getTotalTime(), v.getId()});
        });
        return args;
    }
}
