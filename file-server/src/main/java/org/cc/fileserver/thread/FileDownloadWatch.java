package org.cc.fileserver.thread;

import org.apache.dubbo.common.utils.ConcurrentHashSet;
import org.cc.common.component.WSService;
import org.cc.common.config.ThreadPool;
import org.cc.common.model.EventMessage;
import org.cc.common.pojo.EventMessageType;
import org.cc.common.utils.PublicUtil;
import org.cc.fileserver.model.FileDownProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

public class FileDownloadWatch implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(FileDownloadWatch.class);
    private static Set<FileDownProcess> processes = new ConcurrentHashSet<>();

    static {
        FileDownloadWatch watch = new FileDownloadWatch();
        ThreadPool.submit(watch);
    }

    private FileDownloadWatch() {}

    public static void load() {}

    public synchronized static void addProcess(FileDownProcess process) {
        processes.add(process);
        FileDownloadWatch.class.notify();
    }

    @Override
    public void run() {
        log.info("file down watch started.");
        while (true) {
            synchronized (FileDownloadWatch.class) {
                try {
                    long start = System.currentTimeMillis();
                    EventMessage<Set<FileDownProcess>> msg = new EventMessage<>(EventMessageType.FILE_DOWN_WATCH, processes);
                    WSService.sendMessageToWatcher(msg);
                    processes = processes.stream().filter(FileDownProcess::isNotFinished).collect(Collectors.toSet());
                    if (processes.size() == 0)
                        FileDownloadWatch.class.wait();
                    else
                        PublicUtil.wait(FileDownloadWatch.class, start, 1000L);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("file down watch stop, with ex: {}", e.getMessage());
                    break;
                }
            }
        }
    }
}
