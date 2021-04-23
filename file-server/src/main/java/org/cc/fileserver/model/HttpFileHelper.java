package org.cc.fileserver.model;

import org.cc.common.exception.GlobalException;
import org.cc.fileserver.utils.HttpFileUtil;
import org.cc.fileserver.utils.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @ClassName: HttpFileHelper
 * @Description: TODO
 * @Author: CC
 * @Date 2021/4/23 9:00
 * @ModifyRecords: v1.0 new
 */
public class HttpFileHelper {
    private static final Logger log = LoggerFactory.getLogger(HttpFileHelper.class);

    private String uri;

    private boolean toFile = false;
    private int retry = 0;
    private int maxRetry = 10;
    private int connectionTimeOut = 3000;
    private int readTimeOut = 3000;

    private String domain;
    private byte[] data;
    private String localUri;

    public HttpFileHelper doGet() {
        URL url = new URL(uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(connectionTimeOut);
        conn.setReadTimeout(readTimeOut);
        conn.connect();
        domain = HttpUtil.getDomain(conn);
        String ct = conn.getHeaderField("Content-Type");
        if (ct == null)
            HttpUtil.printHeaders(conn);

        if (toFile) {
            localUri = HttpUtil.getLocalUri(HttpUtil.getFileType(ct));
        }

        String localPath = getLocalUri(getFileType(ct));
        if (localPath.endsWith("."))
            printHeaders(conn);
        File localFile = new File(getFullLocalUri(localPath));
        if (!localFile.exists())
            HttpFileUtil.createFile(localFile);
        try (
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(localFile));
        ){
            byte[] b = new byte[20 * 1024];
            int len;
            while ((len = bis.read(b)) != -1)
                bos.write(b, 0, len);
            System.out.println("下载远程文件[" + remoteUri + "]成功");
            return localPath;
        } catch (Exception e) {
            if (n < m) {
                System.out.println("下载远程文件[" + remoteUri + "]失败重试[" + n + "]：" + e.getMessage());
                sleep(200);
                return down(remoteUri, n + 1, m);
            } else {
                e.printStackTrace();
                throw new GlobalException(501, "下载远程文件[" + remoteUri + "]失败");
            }
        }
    }

}
