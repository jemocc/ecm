package org.cc.fileserver.Server.impl;

import org.cc.common.utils.DateTimeUtil;
import org.cc.fileserver.Server.FileService;
import org.cc.fileserver.dao.CacheFileDao;
import org.cc.fileserver.dao.VideoDao;
import org.cc.fileserver.entity.CacheFile;
import org.cc.fileserver.entity.Video;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class FileServiceImpl implements FileService {
    private final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);
    @Value("self-config.local-file-path")
    private String localFilePath;

    private final CacheFileDao cacheFileDao;
    private final VideoDao videoDao;

    public FileServiceImpl(CacheFileDao cacheFileDao, VideoDao videoDao) {
        this.cacheFileDao = cacheFileDao;
        this.videoDao = videoDao;
    }

    public CacheFile saveNewCacheFile(String fileName, String fileType, String remoteUri, boolean isCacheToLocal) {
        if (fileName.contains(".")) {
            String[] ss = fileName.split("[.]");
            fileName = ss[0];
            if (ss.length > 1)
                fileType = ss[1];
        }
//        CacheFile cacheFile;
//        if (isCacheToLocal) {
//            String cachePath = getCachePath(fileName, null);
//            HttpUtil.downloadFile(remoteUri, localFilePath + cachePath);
//            cacheFile = CacheFile.ofNew(fileName, fileType, cachePath, FileFormType.LOCAL);
//            cacheFile.setRemark1(remoteUri);
//        } else
//            cacheFile = CacheFile.ofNew(fileName, fileType, remoteUri, FileFormType.REMOTE);
//        int fid = cacheFileDao.save(cacheFile);
//        cacheFile.setFid(fid);
//        return cacheFile;
        return null;
    }

    public Video saveNewVideo(String videoName, String coverUri, String videoUri, boolean isCacheToLocal) {

//        CacheFile cacheFile;
//        if (isCacheToLocal) {
//            String cachePath = getCachePath(fileName, null);
//            HttpUtil.downloadFile(remoteUri, localFilePath + cachePath);
//            cacheFile = CacheFile.of(fileName, fileType, cachePath, FileFormType.LOCAL);
//            cacheFile.setRemark1(remoteUri);
//        } else
//            cacheFile = CacheFile.of(fileName, fileType, remoteUri, FileFormType.REMOTE);
//        int fid = cacheFileDao.save(cacheFile);
//        cacheFile.setFid(fid);
//        return cacheFile;
        return null;
    }

    @Override
    public int saveRemoteVideo(String fileName, String remoteUri, boolean isCacheToLocal) {
        String fileType = null;
        if (fileName.contains(".")) {
            String[] ss = fileName.split("[.]");
            fileName = ss[0];
            if (ss.length > 1)
                fileType = ss[1];
        }


        CacheFile cacheFile = saveNewCacheFile(fileName, fileType, remoteUri, isCacheToLocal);

        Video video = new Video();
        video.setName(fileName);

        return 0;
    }

    private String getCachePath() {
        return File.separator + DateTimeUtil.getCurrentDate() + File.separator;
    }
}
