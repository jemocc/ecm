package org.cc.fileserver.thread;

import org.cc.common.config.ThreadPool;
import org.cc.common.exception.GlobalException;
import org.cc.common.utils.DateTimeUtil;
import org.cc.fileserver.entity.CacheFile;
import org.cc.fileserver.entity.Video;
import org.cc.fileserver.entity.enums.FileFormType;
import org.cc.fileserver.utils.HttpFileUtil;
import org.cc.fileserver.utils.M3U8Util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M3u8DownTask implements Runnable {
    private final Video video;
    private final CountDownLatch cdl;

    private final List<String> tss = new ArrayList<>();
    private LocalTime totalTime;
    private Cipher cipher;
    private final Map<Integer, byte[]> dataMap = new ConcurrentHashMap<>();
    private AtomicInteger flag;

    public M3u8DownTask(CacheFile file, CountDownLatch cdl) {
        this.video = (Video) file;
        this.cdl = cdl;
    }

    @Override
    public void run() {
        try {
            HttpURLConnection conn = HttpFileUtil.doGetForConn(video.getUri(), 0, 20);
            String domain = HttpFileUtil.getDomain(conn);
            List<String> data = M3U8Util.readM3U8FileData(conn);
            conn.disconnect();

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
            video.setTotalTime(DateTimeUtil.parseTime(totalTime.intValue()));
            if (tss.get(0).endsWith(".m3u8")) {
                video.setUri(domain + tss.get(0));
                run();
            } else {
                System.out.println("total part: " + tss.size());
                CountDownLatch tsCdl = new CountDownLatch(tss.size());
                for (int i = 0; i < tss.size(); i++) {
                    DoGetTask task = new DoGetTask(i, domain + tss.get(i), tsCdl, dataMap);
                    ThreadPool.submit(task);
                }
                System.out.println("任务提交完成");
                waitWriteFile(tsCdl);
            }
        } finally {
            cdl.countDown();
        }
    }

    private void waitWriteFile(CountDownLatch tsCdl) {
        String localPath = HttpFileUtil.getLocalUri("mp4");
        File localFile = new File(HttpFileUtil.getFullLocalUri(localPath));
        if (!localFile.exists())
            HttpFileUtil.createFile(localFile);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(localFile))) {
            tsCdl.await();
            System.out.println("\n下载任务执行完成");
            if (flag.get() == 1) {
                System.out.println("文件[" + video.getName() + "]下载取消");
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
            video.setFormType(FileFormType.LOCAL);
            video.setUri(localPath);
            System.out.println("文件[" + video.getName() + "]下载完成");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            HttpFileUtil.deleteFile(localFile);
        }
    }
}