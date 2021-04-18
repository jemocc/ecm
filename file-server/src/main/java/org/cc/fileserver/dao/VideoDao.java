package org.cc.fileserver.dao;

import org.cc.fileserver.entity.Video;

import java.util.List;

public interface VideoDao {
    int save(Video video);

    int save(List<Video> videos);
}
