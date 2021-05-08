package org.cc.fileserver.thread;

import org.cc.common.config.ThreadPool;
import org.cc.common.exception.GlobalException;
import org.cc.fileserver.entity.CacheFile;
import org.cc.fileserver.model.FileDownProcess;
import org.cc.fileserver.model.HttpDownFileHelper;
import org.cc.fileserver.model.Profile;
import org.cc.fileserver.utils.FileUtil;
import org.cc.fileserver.utils.HttpUtil;
import org.cc.fileserver.utils.PublicUtil_FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @ClassName: DownCacheFileTask
 * @Description: TODO
 * @Author: CC
 * @Date 2021/5/8 9:46
 * @ModifyRecords: v1.0 new
 */
public class DownCacheFileTask implements Runnable{
    private final Logger log = LoggerFactory.getLogger(DownCacheFileTask.class);
    private final CacheFile file;
    private OutputStream os;

    public DownCacheFileTask(CacheFile file) {
        this.file = file;
    }

    @Override
    public void run() {
        HttpDownFileHelper helper = HttpDownFileHelper.init(file.getUri()).request();
        String localUri = FileUtil.getLocalUri(helper.getFileType());
        int contentSize = helper.getContentSize();
        FileDownProcess process = new FileDownProcess(file.getId(), file.getName(), contentSize);
        int filePartMaxSize = Profile.getDownFilePartMaxSize();
        int filePartMaxNum = Profile.getDownFilePartMaxNum();
        if (contentSize > filePartMaxSize * 2) {  //超过两倍则进行分片下载
            File localFile = new File(FileUtil.getFullLocalUri(localUri));
            if (!localFile.exists())
                PublicUtil_FS.createFile(localFile);
            try {
                os = new FileOutputStream(localFile);
            } catch (IOException e) {
                log.error("down ex: ", e);
                throw new GlobalException(501, "open local file stream [" + localFile.getAbsolutePath() + "] failure.");
            }

            helper.close();
            int partCount = (int) Math.ceil((float) helper.getContentSize() / filePartMaxSize);
            int rangStart = 0;
            for (int i = 0; i < partCount; i += filePartMaxNum) {
                List<HttpDownFileHelper> helpers = new ArrayList<>(filePartMaxNum);
                List<CompletableFuture<?>> futures = new ArrayList<>(filePartMaxNum);
                for (int j = 0; j < filePartMaxNum; j++) {
                    int randEnd = (rangStart + filePartMaxSize) > contentSize ? (contentSize - rangStart) : (rangStart + filePartMaxSize);
                    HttpDownFileHelper th = HttpDownFileHelper.init(file.getUri()).range(j, rangStart, randEnd).watch(process);
                    helpers.add(th);
                    futures.add(CompletableFuture.runAsync(() -> helper.request().down(), ThreadPool.getExecutor()));
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
                    writeDataToFile(helpers);
                });
            }
            os.close();
        } else {    //直接下载
            helper.localUri(localUri).down();
        }
    }

    private synchronized void writeDataToFile(List<HttpDownFileHelper> helpers) {

        helpers.forEach(i -> {
            try {
                os.write(i.getData());
            } catch (IOException e) {
                log.error("down ex: ", e);
                throw new GlobalException(501, "open local file stream [" + localFile.getAbsolutePath() + "] failure.");
            }
        });
    }
}
