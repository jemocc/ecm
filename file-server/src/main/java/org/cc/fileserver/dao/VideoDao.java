package org.cc.fileserver.dao;

import org.cc.common.model.Page;
import org.cc.common.model.Pageable;
import org.cc.fileserver.entity.Video;

import java.util.List;

public interface VideoDao {
    int save(Video video);

    List<Video> save(List<Video> videos);

    Video queryOne(Integer id);

    Page<Video> queryAll(Pageable pageable);

    List<Video> queryAllWithoutCacheCover(Pageable pageable);

    Integer delRepeat();
}
