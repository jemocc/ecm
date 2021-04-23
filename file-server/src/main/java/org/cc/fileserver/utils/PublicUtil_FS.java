package org.cc.fileserver.utils;

import org.cc.common.exception.GlobalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @ClassName: PublicUtil_FS
 * @Description: TODO
 * @Author: CC
 * @Date 2021/4/23 9:34
 * @ModifyRecords: v1.0 new
 */
public class PublicUtil_FS {
    private static final Logger log = LoggerFactory.getLogger(PublicUtil_FS.class);

    public static void createFile(File f) {
        try {
            if(!f.createNewFile())
                throw new GlobalException(501, "创建本地文件[" + f.getAbsolutePath() + "]失败");
        } catch (IOException e) {
            File d = new File(f.getAbsolutePath().replaceAll("[/\\\\][^/\\\\]*$", ""));
            if (d.exists()) {
                e.printStackTrace();
                throw new GlobalException(501, "创建本地文件[" + f.getAbsolutePath() + "]异常");
            } else {
                if (d.mkdirs())
                    createFile(f);
                else
                    throw new GlobalException(501, "创建本地文件[" + f.getAbsolutePath() + "]失败");
            }
        }
    }
    public static void deleteFile(File file) {
        if (file.delete())
            log.info("删除文件[{}]成功", file.getName());
        else
            log.info("删除文件[{}]失败", file.getName());
    }
}
