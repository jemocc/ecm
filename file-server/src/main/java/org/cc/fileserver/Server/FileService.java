package org.cc.fileserver.Server;

import org.cc.common.model.Page;
import org.cc.common.model.Pageable;
import org.cc.fileserver.entity.Video;

import java.util.List;

public interface FileService {

    int saveRemoteVideo(List<Video> videos);

    int cacheVideo(Integer id);

    void cacheCover(int p);

    void testLock(Integer time);

    Page<Video> queryAllVideo(Pageable pageable);
}
