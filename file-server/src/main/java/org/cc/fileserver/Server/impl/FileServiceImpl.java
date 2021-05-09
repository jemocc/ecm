package org.cc.fileserver.Server.impl;

import org.apache.logging.log4j.core.util.Assert;
import org.cc.common.component.DistributeSynchronized;
import org.cc.common.model.Page;
import org.cc.common.utils.PublicUtil;
import org.cc.common.config.ThreadPool;
import org.cc.common.model.Pageable;
import org.cc.common.utils.JsonUtil;
import org.cc.fileserver.Server.FileService;
import org.cc.fileserver.dao.CacheFileDao;
import org.cc.fileserver.dao.VideoDao;
import org.cc.fileserver.entity.CacheFile;
import org.cc.fileserver.entity.Video;
import org.cc.fileserver.entity.enums.FileFormType;
import org.cc.fileserver.thread.DownCacheFilesTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {
    private final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    private final CacheFileDao cacheFileDao;
    private final VideoDao videoDao;

    public FileServiceImpl(CacheFileDao cacheFileDao, VideoDao videoDao) {
        this.cacheFileDao = cacheFileDao;
        this.videoDao = videoDao;
    }

    @Override
    public int saveRemoteVideo(List<Video> videos) {
        log.info("请求保存远程文件[{}]个", videos.size());
        if (videos.size() == 0)
            return 0;
        LocalDateTime now = LocalDateTime.now();
        videos.forEach(i -> {
            i.setFormType(FileFormType.REMOTE);
            i.setCreateAt(now);
        });
        return videoDao.save(videos).size();
    }

    @Override
    public int cacheVideo(Integer id) {
        Video v = videoDao.queryOne(id);
        Assert.requireNonEmpty(v, "video can not be found");
        List<CacheFile> r = Collections.singletonList(v);
        log.info("begin to down video，with data:\n{}", JsonUtil.bean2Json_FN(r));
        DownCacheFilesTask tasks = new DownCacheFilesTask(r, "update video set type=?,uri=?,remark2=uri where id=?",
                f -> new Object[]{f.getType(), f.getUri(), f.getId()});
        ThreadPool.submit(tasks);
        return 1;
    }

    @Override
    public void cacheCover(int p) {
        Pageable pageable = Pageable.ofPage(p++, 100);
        List<CacheFile> r = videoDao.queryAllWithoutCacheCover(pageable).stream().map(i -> {
//            i.setCoverUri(i.getCoverUri().replaceAll("\\.com/?", ".com/"));
            i.setUri(i.getCoverUri());
            return (CacheFile) i;
        }).collect(Collectors.toList());
//        log.info("begin to down cover, with data:\n{}", JsonUtil.bean2Json_FN(r));
        if (r.size() > 0) {
            DownCacheFilesTask tasks = new DownCacheFilesTask(r, "update video set remark2 = cover_uri,cover_uri = ? where id = ?",
                    f -> new Object[]{f.getUri(), f.getId()});
            ThreadPool.submit(tasks);
            cacheCover(p);
        }
    }

    @Override
    @DistributeSynchronized
    @Async
    public void testLock(Integer time) {
        log.info("exec test lock start.");
        PublicUtil.sleep(time == null ? 5000 : time);
        log.info("exec test lock end.");
    }

    @Override
    public Page<Video> queryAllVideo(Pageable pageable) {
        return videoDao.queryAll(pageable);
    }
}
