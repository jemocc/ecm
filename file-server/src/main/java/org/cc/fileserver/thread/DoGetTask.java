package org.cc.fileserver.thread;

import org.cc.common.exception.GlobalException;
import org.cc.fileserver.model.HttpFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class DoGetTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DoGetTask.class);
    private final int no;
    private final String remoteUri;
    private final CountDownLatch cdl;
    private final Map<Integer, byte[]> dataMap;
    private final AtomicBoolean cancel;

    public DoGetTask(int no, String remoteUri, CountDownLatch cdl, Map<Integer, byte[]> dataMap, AtomicBoolean cancel) {
        this.no = no;
        this.remoteUri = remoteUri;
        this.cdl = cdl;
        this.dataMap = dataMap;
        this.cancel = cancel;
    }

    @Override
    public void run() {
        try {
            if (cancel.get())
                return;
            HttpFileHelper helper = HttpFileHelper.uri(remoteUri).down();
            dataMap.put(no, helper.getData());
        } catch (GlobalException e) {
            log.error(e.getMessage());
            cancel.set(true);
        } finally {
            cdl.countDown();
        }
    }

}
