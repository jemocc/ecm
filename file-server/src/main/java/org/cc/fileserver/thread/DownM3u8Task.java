package org.cc.fileserver.thread;

import org.cc.common.config.ThreadPool;
import org.cc.common.exception.GlobalException;
import org.cc.common.utils.DateTimeUtil;
import org.cc.fileserver.entity.CacheFile;
import org.cc.fileserver.entity.Video;
import org.cc.fileserver.entity.enums.FileFormType;
import org.cc.fileserver.model.HttpFileHelper;
import org.cc.fileserver.utils.HttpUtil;
import org.cc.fileserver.utils.M3U8Util;
import org.cc.fileserver.utils.PublicUtil_FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownM3u8Task extends DownFileTask {
    private static final Logger log = LoggerFactory.getLogger(DownM3u8Task.class);

    private final List<String> tss = new ArrayList<>();
    private Cipher cipher;
    private final Map<Integer, byte[]> dataMap = new ConcurrentHashMap<>();
    private final AtomicBoolean cancel = new AtomicBoolean(false);

    public DownM3u8Task(CacheFile file, CountDownLatch cdl) {
        super(file, cdl);
    }

    @Override
    public void run() {
        try {
            HttpFileHelper helper = HttpFileHelper.uri(file.getUri()).down();
            String domain = helper.getDomain();
            List<String> data = M3U8Util.readM3U8FileData(helper.getData());

            BigDecimal totalTime = BigDecimal.ZERO;
            for (String line : data) {
                if (!line.startsWith("#"))
                    tss.add(line);
                else if (line.startsWith("#EXTINF:"))
                    totalTime = totalTime.add(BigDecimal.valueOf(Double.parseDouble(line.replaceAll("#EXTINF:([^,]*),", "$1"))));
                else if (line.startsWith("#EXT-X-KEY:")) {
                    Pattern r = Pattern.compile("METHOD=([^,]*),URI=\"([^\"]*)\"");
                    Matcher m = r.matcher(line);
                    if (m.find()) {
                        cipher = M3U8Util.getCipher(m.group(1), m.group(2));
                        if (cipher == null)
                            throw new GlobalException(501, "密钥获取异常：" + line);
                    } else
                        throw new GlobalException(501, "密钥获取失败：" + line);
                }
            }
            ((Video)file).setTotalTime(DateTimeUtil.parseTime(totalTime.intValue()));
            if (tss.get(0).endsWith(".m3u8")) {
                file.setUri(domain + tss.get(0));
                run();
            } else {
                log.info("total part: {}", tss.size());
                CountDownLatch tsCdl = new CountDownLatch(tss.size());
                for (int i = 0; i < tss.size(); i++) {
                    DoGetTask task = new DoGetTask(i, domain + tss.get(i), tsCdl, dataMap, cancel);
                    ThreadPool.submit(task);
                }
                log.info("文件[{}-{}]下载任务提交完成", file.getId(), file.getName());
                waitWriteFile(tsCdl);
            }
        } finally {
            cdl.countDown();
        }
    }

    private void waitWriteFile(CountDownLatch tsCdl) {
        String localPath = HttpUtil.getLocalUri("mp4");
        File localFile = new File(HttpUtil.getFullLocalUri(localPath));
        if (!localFile.exists())
            PublicUtil_FS.createFile(localFile);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(localFile))) {
            tsCdl.await();
            log.info("文件[{}-{}]下载任务执行完成", file.getId(), file.getName());
            if (cancel.get()) {
                log.warn("文件[{}-{}]下载取消", file.getId(), file.getName());
                return;
            }
            dataMap.forEach((k, v) -> {
                try {
                    if (cipher != null)
                        v = cipher.doFinal(v);
                    bos.write(v);
                } catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
                    e.printStackTrace();
                }
            });
            file.setFormType(FileFormType.LOCAL);
            file.setUri(localPath);
            log.info("文件[{}-{}]下载成功", file.getId(), file.getName());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            PublicUtil_FS.deleteFile(localFile);
        }
    }
}