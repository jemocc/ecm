package org.cc.fileserver.Server;

import org.cc.fileserver.entity.Video;

import java.util.List;

public interface FileService {

    int saveRemoteVideo(List<Video> videos);
}
