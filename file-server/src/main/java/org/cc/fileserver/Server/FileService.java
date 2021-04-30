package org.cc.fileserver.Server;

import org.cc.fileserver.entity.Video;

import java.util.List;

public interface FileService {

    int saveRemoteVideo(List<Video> videos);

    int cacheVideo(Integer id);

    void cacheCover();

    void testLock(Integer time);
}
