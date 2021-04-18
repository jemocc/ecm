package org.cc.fileserver.dao;

import org.cc.fileserver.entity.CacheFile;

import java.util.List;

public interface CacheFileDao {

    int save(CacheFile cacheFile);

    int save(List<CacheFile> cacheFiles);


}
