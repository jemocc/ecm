package org.cc.fileserver.Server;

public interface FileService {

    int saveRemoteVideo(String fileName, String remoteUri, boolean isCacheToLocal);
}
