package org.cc.fileserver.thread;

import org.cc.common.config.ThreadPool;
import org.cc.common.utils.JsonUtil;
import org.cc.common.utils.SequenceGenerator;
import org.cc.fileserver.entity.CacheFile;
import org.cc.fileserver.model.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @ClassName: DownCacheFilesTask
 * @Description: TODO
 * @Author: CC
 * @Date 2021/5/8 9:33
 * @ModifyRecords: v1.0 new
 */
public abstract class DownCacheFilesTask implements Runnable{
    private final Logger log = LoggerFactory.getLogger(DownCacheFilesTask.class);
    protected Long taskSeq;
    protected List<CacheFile> files;
    protected String callbackSql;

    public DownCacheFilesTask(List<CacheFile> files, String callbackSql) {
        this.taskSeq = SequenceGenerator.newSeq();
        this.files = files;
        this.callbackSql = callbackSql;
    }

    @Override
    public void run() {
        CompletableFuture<?>[] futures = new CompletableFuture[files.size()];
        for (int i = 0; i < files.size(); i++) {
            CompletableFuture<?> future = CompletableFuture.runAsync(new DownCacheFileTask(files.get(i)), ThreadPool.getExecutor());
            futures[i] = future;
        }
        log.info("commit all down file task complete, total size [{}]", files.size());
        if (callbackSql != null) {
            CompletableFuture.allOf(futures).thenRun(() -> {
                JdbcTemplate jdbcTemplate = Profile.getBean(JdbcTemplate.class);
                List<Object[]> args = getArgs(files);
                jdbcTemplate.batchUpdate(callbackSql, args);
                log.info("exec callback sql complete, with args：{}", JsonUtil.bean2Json(args));
            });
        }
    }

    abstract List<Object[]> getArgs(List<CacheFile> files);
}
