package org.cc.fileserver.utils;

import org.cc.common.exception.GlobalException;
import org.cc.common.utils.DateTimeUtil;
import org.cc.fileserver.model.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.UUID;

/**
 * @ClassName: FileUtil
 * @Description: TODO
 * @Author: CC
 * @Date 2021/5/8 18:15
 * @ModifyRecords: v1.0 new
 */
public class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    public static String getLocalUri(String type) {
        if (type == null)
            return "tmp" + File.separator + DateTimeUtil.getCurrentDate() + File.separator + UUID.randomUUID().toString().replace("-", "");
        else
            return type + File.separator + DateTimeUtil.getCurrentDate() + File.separator + UUID.randomUUID().toString().replace("-", "") + "." + type;
    }

    public static String getFullLocalUri(String localPath) {
        return Profile.getConfigPath() + File.separator + localPath;
    }

    public static String getFileType(String contentType) {
        if (contentType == null)
            return null;
        if (contentType.contains("jpeg"))
            return "jpg";
        else if (contentType.contains("png"))
            return "png";
        else if (contentType.contains("mp4"))
            return "mp4";
        log.info("no matched file type, with contentType: {}", contentType);
        return null;
    }

    public static void createFile(File f) {
        try {
            if(!f.createNewFile())
                throw new GlobalException(501, "创建本地文件[" + f.getAbsolutePath() + "]失败");
            else
                log.info("创建文件[{}]成功", f.getName());
        } catch (IOException e) {
            String dirPath = f.getAbsolutePath().replaceAll("[/\\\\][^/\\\\]*$", "");
            createDir(dirPath);
            try {
                if(!f.createNewFile())
                    throw new GlobalException(501, "创建本地文件[" + f.getAbsolutePath() + "]失败");
            } catch (IOException e2) {
                e2.printStackTrace();
                throw new GlobalException(501, "创建本地文件[" + f.getAbsolutePath() + "]异常");
            }
        }
    }

    public static synchronized void createDir(String path) {
        File dir = new File(path);
        if (dir.exists())
            return;
        log.info("创建文件目录[{}], r:[{}]",dir.getAbsolutePath(), dir.mkdirs());
    }

    public static void deleteFile(File file) {
        if (file.delete())
            log.info("删除文件[{}]成功", file.getName());
        else
            log.info("删除文件[{}]失败", file.getName());
    }

    public static OutputStream openOS(File file) {
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new GlobalException(501, "文件[" + file.getAbsolutePath() + "]不存在");
        }
    }
}
