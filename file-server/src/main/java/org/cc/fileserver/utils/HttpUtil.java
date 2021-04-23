package org.cc.fileserver.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.cc.common.exception.GlobalException;
import org.cc.common.utils.DateTimeUtil;
import org.cc.common.utils.JsonUtil;
import org.cc.fileserver.model.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.Security;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HttpUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void printHeaders(HttpURLConnection conn) {
        Map<String, List<String>> headers = conn.getHeaderFields();
        log.info("header:{}", JsonUtil.bean2Json_FN(headers));
    }

    public static String getDomain(HttpURLConnection conn) {
        return conn.getURL().getProtocol() + "://" + conn.getURL().getHost() + ":" + conn.getURL().getPort();
    }


    public static String getLocalUri(String type) {
        return DateTimeUtil.getCurrentDate() + File.separator + UUID.randomUUID().toString().replace("-", "") + "." + type;
    }

    public static String getFullLocalUri(String localPath) {
        return Profile.getConfigPath() + File.separator + localPath;
    }

    public static String getFileType(String contentType) {
        if (contentType == null)
            return "";
        if (contentType.contains("image/jpeg") || contentType.contains("image/pjpeg"))
            return "jpg";
        else if (contentType.contains("image/png") || contentType.contains("image/x-png"))
            return "png";
        return "";
    }

}
