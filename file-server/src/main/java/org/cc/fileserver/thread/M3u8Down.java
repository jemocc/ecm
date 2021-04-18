package org.cc.fileserver.thread;

import org.cc.fileserver.utils.HttpFileUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class M3u8Down implements Runnable{
    private final int seqNo;
    private final Map<Integer, byte[]> isMap;
    private final CountDownLatch countDownLatch;
    private final String url;

    public M3u8Down(int seqNo, Map<Integer, byte[]> isMap, CountDownLatch countDownLatch, String url) {
        this.seqNo = seqNo;
        this.isMap = isMap;
        this.countDownLatch = countDownLatch;
        this.url = url;
    }

    @Override
    public void run() {
        try {
            HttpURLConnection conn = HttpFileUtil.doGet(url, 0);
            isMap.put(seqNo, HttpFileUtil.getData(conn.getInputStream()));
            if (countDownLatch.getCount() % 200 == 0)
                System.out.println(".");
            else
                System.out.print(".");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(seqNo + " 异常");
        } finally {
            countDownLatch.countDown();
        }
    }
}
