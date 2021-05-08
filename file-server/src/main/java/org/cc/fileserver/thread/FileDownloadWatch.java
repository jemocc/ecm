package org.cc.fileserver.thread;

import org.cc.common.component.WSService;
import org.cc.common.config.ThreadPool;
import org.cc.common.model.EventMessage;
import org.cc.common.pojo.EventMessageType;
import org.cc.common.utils.PublicUtil;
import org.cc.fileserver.model.FileDownProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileDownloadWatch implements Runnable {
    private final Logger log = LoggerFactory.getLogger(FileDownloadWatch.class);
    private List<FileDownProcess> processes = new ArrayList<>();

    private FileDownloadWatch() {
        ThreadPool.submit(this);
    }

    static class Inner {
        private static final FileDownloadWatch fileDownloadWatch = new FileDownloadWatch();
    }

    public static void addProcess(FileDownProcess process) {
        synchronized (FileDownloadWatch.class) {
            Inner.fileDownloadWatch.processes.add(process);
            FileDownloadWatch.class.notify();
        }
    }

    @Override
    public void run() {
        log.info("file down watch started.");
        synchronized (FileDownloadWatch.class) {
            while (true) {
                try {
                    if (processes.size() == 0)
                        wait();
                    long start = System.currentTimeMillis();
                    List<FileDownProcess> notFinished = processes.stream().filter(FileDownProcess::isNotFinished).collect(Collectors.toList());
                    EventMessage<List<FileDownProcess>> msg = new EventMessage<>(EventMessageType.FILE_DOWN_WATCH, processes);
                    WSService.sendMessageToWatcher(msg);
                    processes = notFinished;
                    PublicUtil.wait(start, 1000L);
                } catch (Exception e) {
                    log.info("file down watch stop, with ex: {}", e.getMessage());
                    break;
                }
            }
        }
    }
}
