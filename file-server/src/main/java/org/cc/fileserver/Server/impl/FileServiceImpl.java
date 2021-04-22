package org.cc.fileserver.Server.impl;

import org.cc.common.config.ThreadPool;
import org.cc.common.utils.JsonUtil;
import org.cc.fileserver.Server.FileService;
import org.cc.fileserver.dao.CacheFileDao;
import org.cc.fileserver.dao.VideoDao;
import org.cc.fileserver.entity.CacheFile;
import org.cc.fileserver.entity.Video;
import org.cc.fileserver.entity.enums.FileFormType;
import org.cc.fileserver.thread.DownFilesTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        LocalDateTime now = LocalDateTime.now();
        videos.forEach(i -> {
            i.setFormType(FileFormType.REMOTE);
            i.setCreateAt(now);
        });
        List<CacheFile> r = videoDao.save(videos).stream().map(i -> {
            i.setUri(i.getCoverUri());
            return (CacheFile) i;
        }).collect(Collectors.toList());
        log.info("开始下载封面，with data: {}", JsonUtil.bean2Json_FN(r));
        DownFilesTask task = new DownFilesTask(r, "update video set cover_uri = ? where id = ?");
        ThreadPool.submit(task);
        return r.size();
    }
}
