package org.cc.fileserver.utils;

import org.cc.common.utils.DateTimeUtil;
import org.cc.fileserver.model.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
}
