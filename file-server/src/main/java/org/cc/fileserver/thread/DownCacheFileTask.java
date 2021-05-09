package org.cc.fileserver.thread;

import org.cc.common.config.ThreadPool;
import org.cc.common.exception.GlobalException;
import org.cc.common.utils.PublicUtil;
import org.cc.fileserver.entity.CacheFile;
import org.cc.fileserver.entity.enums.FileFormType;
import org.cc.fileserver.model.FileDownProcess;
import org.cc.fileserver.model.HttpDownFileHelper;
import org.cc.fileserver.model.Profile;
import org.cc.fileserver.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
    protected final CacheFile file;
    protected FileDownProcess process;
    protected OutputStream os;
    protected int contentSize;

    public DownCacheFileTask(CacheFile file) {
        this.file = file;
    }

    @Override
    public void run() {
        HttpDownFileHelper helper = HttpDownFileHelper.init(file.getUri()).request();
        String localUri = FileUtil.getLocalUri(helper.getFileType());
        File localFile = new File(FileUtil.getFullLocalUri(localUri));
        if (!localFile.exists())
            FileUtil.createFile(localFile);
        os = FileUtil.openOS(localFile);

        contentSize = helper.getContentSize();
        process = new FileDownProcess(file.getId(), file.getName(), contentSize);
        FileDownloadWatch.addProcess(process);
        try {
            if (contentSize > Profile.getDownFilePartMaxSize() * 2) {  //超过两倍则进行分片下载
                helper.close();
                partDownFile();
            } else if (helper.isM3U8()){
                helper.close();
            } else {    //直接下载
                helper.localFile(os).watch(process).down();
            }
            file.setRemark2(file.getUri());
            file.setUri(localUri);
            file.setFormType(FileFormType.LOCAL);
            file.setType(helper.getFileType());
        } catch (Exception e) {
            log.error("download file [{}] failure. ex: ", file.getId(), e);
            process.failure();
            PublicUtil.close(os);
            FileUtil.deleteFile(localFile);
        }
    }

    protected void partDownFile() {
        int filePartMaxNum = Profile.getDownFilePartMaxNum();
        int filePartMaxSize = Profile.getDownFilePartMaxSize();
        int partCount = (int) Math.ceil((float) contentSize / filePartMaxSize);
        int rangStart = 0;
        for (int i = 0; i < partCount; i += filePartMaxNum) {
            List<HttpDownFileHelper> helpers = new ArrayList<>(filePartMaxNum);
            List<CompletableFuture<?>> futures = new ArrayList<>(filePartMaxNum);
            for (int j = 0; j < filePartMaxNum; j++) {
                int randEnd = Math.min(rangStart + filePartMaxSize, contentSize);
                HttpDownFileHelper th = HttpDownFileHelper.init(file.getUri()).range(j, rangStart, randEnd).watch(process);
                rangStart = randEnd + 1;
                helpers.add(th);
                futures.add(CompletableFuture.runAsync(() -> th.request().down(), ThreadPool.getExecutor()));
            }
            final boolean isEnd = rangStart > contentSize;
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
                writeDataToFile(helpers, isEnd);
            });
        }

    }

    protected synchronized void writeDataToFile(List<HttpDownFileHelper> helpers, boolean end) {
        helpers.forEach(i -> {
            try {
                os.write(i.getData());
                i.close();
            } catch (IOException e) {
                log.error("down ex: ", e);
                PublicUtil.close(os);
                throw new GlobalException(501, "open local file stream failure.");
            }
        });
        if (end)
            PublicUtil.close(os);
    }
}
