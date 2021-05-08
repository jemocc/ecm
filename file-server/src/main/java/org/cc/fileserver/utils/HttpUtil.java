package org.cc.fileserver.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.cc.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.security.Security;
import java.util.List;
import java.util.Map;

public class HttpUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void printHeaders(HttpURLConnection conn) {
        Map<String, List<String>> headers = conn.getHeaderFields();
        log.info("headers:\n{}", JsonUtil.bean2Json_FN(headers));
    }

    public static String getDomain(HttpURLConnection conn) {
        return conn.getURL().getProtocol() + "://" + conn.getURL().getHost() + ":" + conn.getURL().getPort();
    }

}
