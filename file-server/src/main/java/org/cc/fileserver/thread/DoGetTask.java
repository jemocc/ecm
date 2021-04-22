package org.cc.fileserver.thread;

import org.cc.fileserver.utils.HttpFileUtil;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class DoGetTask implements Runnable {
    private final int no;
    private final String remoteUri;
    private final CountDownLatch cdl;
    private final Map<Integer, byte[]> dataMap;

    public DoGetTask(int no, String remoteUri, CountDownLatch cdl, Map<Integer, byte[]> dataMap) {
        this.no = no;
        this.remoteUri = remoteUri;
        this.cdl = cdl;
        this.dataMap = dataMap;
    }

    @Override
    public void run() {
        try {
            byte[] bs = HttpFileUtil.down2(remoteUri);
            dataMap.put(no, bs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cdl.countDown();
        }
    }

}
