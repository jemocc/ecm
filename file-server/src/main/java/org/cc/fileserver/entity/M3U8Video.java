package org.cc.fileserver.entity;

import io.swagger.annotations.ApiModel;
import org.cc.common.config.ExecutorConfig;
import org.cc.common.exception.GlobalException;
import org.cc.common.utils.DateTimeUtil;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiModel(value = "M3U8视频", description = "视频映射实体")
public class M3U8Video extends Video {

    String remoteAddr;

    String localAddr;

    Map<Integer, byte[]> dataMap = new ConcurrentHashMap<>();

    CountDownLatch cdl;

    List<String> tss = new ArrayList<>();

    Cipher cipher;

    ExecutorService es = ExecutorConfig.getNewExecutor();

    boolean cancelFlag = false;
    CountDownLatch over = new CountDownLatch(1);

    public static Video ofNew(String name, String type, String vfUri, String vCover, FileFormType formType, String localAddr) {
        M3U8Video v = new M3U8Video();
        v.setCreateAt(LocalDateTime.now());
        v.setName(name);
        v.setType(type);
        v.setVfUri(vfUri);
        v.setvCover(vCover);
        v.setFormType(formType);
        v.localAddr = localAddr;
        v.remoteAddr = vfUri;
        return v;
    }

    public void beginDown() {
        M3u8DownTask task = new M3u8DownTask(remoteAddr, this);
        es.submit(task);
        try {
            over.await();
            es.shutdown();
            System.out.println("线程池关闭");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
class M3u8DownTask implements Runnable {
    private final String url;
    private final M3U8Video video;
    
    public M3u8DownTask(String url, M3U8Video video) {
        this.url = url;
        this.video = video;
    }

    @Override
    public void run() {
        HttpURLConnection conn = HttpFileUtil.doGetForConn(url, 0, 20);
        String domain = HttpFileUtil.getDomain(conn);
        List<String> data = M3U8Util.readM3U8FileData(conn);
        conn.disconnect();
        video.tss.clear();
        BigDecimal totalTime = BigDecimal.ZERO;
        for (String line : data) {
            if (!line.startsWith("#"))
                video.tss.add(line);
            else if (line.startsWith("#EXTINF:"))
                totalTime = totalTime.add(BigDecimal.valueOf(Double.parseDouble(line.replaceAll("#EXTINF:([^,]*),", "$1"))));
            else if (line.startsWith("#EXT-X-KEY:")) {
                Pattern r = Pattern.compile("METHOD=([^,]*),URI=\"([^\"]*)\"");
                Matcher m = r.matcher(line);
                if (m.find( )) {
                    Cipher cipher = M3U8Util.getCipher(m.group(1), m.group(2));
                    if (cipher == null)
                        throw new GlobalException(501, "密钥获取异常：" + line);
                    video.cipher = cipher;
                } else
                    throw new GlobalException(501, "密钥获取失败：" + line);
            }
        }
        if (video.tss.get(0).endsWith(".m3u8"))
            video.es.submit(new M3u8DownTask(domain + video.tss.get(0), video));
        else {
            System.out.println("total part: " + video.tss.size());
            video.setTotalTime(DateTimeUtil.parseTime(totalTime.intValue()));
            video.cdl = new CountDownLatch(video.tss.size());
            Watch watch = new Watch(video);
            video.es.submit(watch);

            for (int i = 0; i < video.tss.size(); i++) {
                M3u8DownPartTask task = new M3u8DownPartTask(i, domain + video.tss.get(i), video);
                video.es.submit(task);
            }
            System.out.println("任务提交完成");
            File f = new File(video.localAddr);
            if (!f.exists())
                HttpFileUtil.createFile(f);
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f))) {
                video.cdl.await();
                System.out.println("\n下载任务执行完成");
                if (video.cancelFlag) {
                    System.out.println("文件[" + video.localAddr + "]下载取消");
                    return;
                }
                video.dataMap.forEach((k, v) -> {
                    try {
                        if (video.cipher != null)
                            v=video.cipher.doFinal(v);
                        bos.write(v);
                    } catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
                        e.printStackTrace();
                    }
                });
                System.out.println("文件[" + video.localAddr + "]下载完成, " + video.toString());
                video.over.countDown();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
                HttpFileUtil.deleteFile(f);
            }
        }
    }
}
class M3u8DownPartTask implements Runnable {
    private final int seqNo;
    private final String url;
    private final M3U8Video video;

    public M3u8DownPartTask(int seqNo, String url, M3U8Video video) {
        this.seqNo = seqNo;
        this.url = url;
        this.video = video;
    }

    @Override
    public void run() {
        try {
            if (!video.cancelFlag) {
                byte[] data = HttpFileUtil.doGet(url, 0, 20);
                video.dataMap.put(seqNo, data);
            }
        } catch (Exception e) {
            video.cancelFlag = true;
            System.out.println("\n片段[" + seqNo + "]读取失败 [" + e.getMessage() + "], remain:" + video.cdl.getCount());
        } finally {
            video.cdl.countDown();
        }
    }
}
class Watch implements Runnable {
    private final M3U8Video video;

    Watch(M3U8Video video) {
        this.video = video;
    }

    @Override
    public void run() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) video.es;
        while (true) {
            try {
                System.out.println("\t\tactive:"+executor.getActiveCount()+"\t\tcompleted:"+executor.getCompletedTaskCount()+"\t\tcdl:"+video.cdl.getCount());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (video.cdl.getCount() == 0)
                return;
        }
    }
}
