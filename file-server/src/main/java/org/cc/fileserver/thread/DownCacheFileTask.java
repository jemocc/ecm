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
import java.util.concurrent.ExecutionException;

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
    protected File localFile;
    protected CacheFile tempFile = new CacheFile();
    protected OutputStream os;
    protected int contentSize;

    public DownCacheFileTask(CacheFile file) {
        this.file = file;
    }

    @Override
    public void run() {
        HttpDownFileHelper helper = HttpDownFileHelper.init(file.getUri()).request();
        openLocalFile(helper.getFileType());
        contentSize = helper.getContentSize();
        process = new FileDownProcess(file.getId(), file.getName(), contentSize);
        FileDownloadWatch.addProcess(process);
        try {
            if (helper.isM3U8()) {
                helper.close();
                partDownFile(helper);
            } else if (contentSize > Profile.getDownFilePartMaxSize() * 2) {  //超过两倍则进行分片下载
                helper.close();
                partDownFile();
            } else {    //直接下载
                helper.localFile(os).watch(process).down();
            }
            if (process.isFailure())
                failure(null);
            else
                success();
        } catch (Exception e) {
            failure(e);
        }
    }

    protected void openLocalFile(String fileType) {
        String localUri = FileUtil.getLocalUri(fileType);
        localFile = new File(FileUtil.getFullLocalUri(localUri));
        if (!localFile.exists())
            FileUtil.createFile(localFile);
        os = FileUtil.openOS(localFile);
        tempFile.setType(fileType);
        tempFile.setUri(localUri);
    }

    protected void success() {
        log.info("download file [{}] success.", file.getId());
        PublicUtil.close(os);
        file.setFormType(FileFormType.LOCAL);
        file.setType(tempFile.getType());
        file.setUri(tempFile.getUri());
    }

    protected void failure(Exception e) {
        log.error("download file [{}] failure. ex: ", file.getId(), e);
        process.failure();
        PublicUtil.close(os);
        FileUtil.deleteFile(localFile);
    }

    protected void partDownFile() throws ExecutionException, InterruptedException {
        int filePartMaxNum = Profile.getDownFilePartMaxNum();
        int filePartMaxSize = Profile.getDownFilePartMaxSize();
        int partCount = (int) Math.ceil((float) contentSize / filePartMaxSize);
        int rangStart = 0;
        for (int i = 0; i < partCount; i += filePartMaxNum) {
            List<HttpDownFileHelper> helpers = new ArrayList<>(filePartMaxNum);
            List<CompletableFuture<?>> futures = new ArrayList<>(filePartMaxNum);
            for (int j = 0; j < filePartMaxNum; j++) {
                int randEnd = Math.min(rangStart + filePartMaxSize, contentSize);
                HttpDownFileHelper th = HttpDownFileHelper.init(file.getUri()).range(i*filePartMaxNum + j, rangStart, randEnd).watch(process);
                rangStart = randEnd + 1;
                helpers.add(th);
                futures.add(CompletableFuture.runAsync(() -> th.request().down(), ThreadPool.getExecutor()));
                if (rangStart >= contentSize)
                    break;
            }
            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
                    if (process.isFailure())
                        throw new GlobalException(501, "part down failure");
                    writeDataToFile(helpers);
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                throw e;
            }
        }

    }

    protected void partDownFile(HttpDownFileHelper helper) {
        List<String> tss = helper.getPartUri();
        int filePartMaxNum = Profile.getDownFilePartMaxNum();
        for (int i = 0; i < tss.size(); i += filePartMaxNum) {
            List<HttpDownFileHelper> helpers = new ArrayList<>(filePartMaxNum);
            List<CompletableFuture<?>> futures = new ArrayList<>(filePartMaxNum);
            for (int j = 0; j < filePartMaxNum; j++) {
                HttpDownFileHelper th = HttpDownFileHelper.init(tss.get(i*filePartMaxNum + j)).watch(process).m3u8Part();
                helpers.add(th);
                futures.add(CompletableFuture.runAsync(() -> th.request().down(), ThreadPool.getExecutor()));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
                if (process.isFailure())
                    throw new GlobalException(501, "part down failure");
                writeDataToFile(helpers);
            });
        }
    }

    protected synchronized void writeDataToFile(List<HttpDownFileHelper> helpers) {
        for (HttpDownFileHelper helper : helpers) {
            try {
                os.write(helper.getData());
                helper.close();
            } catch (IOException e) {
                throw new GlobalException(501, "write data to file failure, " + e.getMessage() + ":" + e.getStackTrace()[0]);
            }
        }
    }
}
