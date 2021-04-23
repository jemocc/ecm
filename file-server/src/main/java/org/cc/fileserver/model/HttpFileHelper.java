package org.cc.fileserver.model;

import io.netty.handler.timeout.ReadTimeoutException;
import org.cc.common.exception.GlobalException;
import org.cc.fileserver.utils.HttpUtil;
import org.cc.fileserver.utils.PublicUtil_FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
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

    public static HttpFileHelper uri(String uri) {
        HttpFileHelper helper = new HttpFileHelper();
        helper.uri = uri;
        return helper;
    }
    public HttpFileHelper toFile() {
        toFile = true;
        return this;
    }
    public HttpFileHelper maxRetry(int m) {
        maxRetry = m;
        return this;
    }
    public HttpFileHelper connectionTimeOut(int t) {
        connectionTimeOut = t;
        return this;
    }
    public HttpFileHelper readTimeOut(int t) {
        readTimeOut = t;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public byte[] getData() {
        return data;
    }

    public String getLocalUri() {
        return localUri;
    }

    public HttpFileHelper down() {
        try {
            doGet();
            return this;
        } catch (SSLException | SocketTimeoutException e) {
            log.info("connect [{}] failure, ec: {}", uri, retry);
        } catch (ReadTimeoutException e) {
            log.info("read [{}] time out, ec: {}", uri, retry);
        } catch (Exception e) {
            log.error("down ex, ec: {}", retry, e);
            throw new GlobalException(501, "远程文件[" + uri + "]下载失败");
        }
        retry++;
        if (retry < maxRetry)
            return down();
        else
            throw new GlobalException(501, "远程文件[" + uri + "]下载失败");
    }

    private void doGet() throws IOException {
        URL url = new URL(uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(connectionTimeOut);
        conn.setReadTimeout(readTimeOut);
        conn.connect();
        domain = HttpUtil.getDomain(conn);

        if (toFile) {
            transDataToFile(conn);
        } else {
            transDataToData(conn);
        }
    }

    private void transDataToFile(HttpURLConnection conn) throws IOException {
        String ct = conn.getHeaderField("Content-Type");
        if (ct == null)
            HttpUtil.printHeaders(conn);
        localUri = HttpUtil.getLocalUri(HttpUtil.getFileType(ct));
        File file = new File(HttpUtil.getFullLocalUri(localUri));
        if (!file.exists())
            PublicUtil_FS.createFile(file);
        try (
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))
        ){
            byte[] b = new byte[20 * 1024];
            int len;
            while ((len = bis.read(b)) != -1)
                bos.write(b, 0, len);
            log.info("下载远程文件[{}]成功", uri);
        } catch (Exception e) {
            log.error("下载文件失败", e);
            throw e;
        }
    }

    private void transDataToData(HttpURLConnection conn) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream())
        ){
            byte[] b = new byte[20 * 1024];
            int len;
            while ((len = bis.read(b)) != -1)
                bos.write(b, 0, len);
            data = bos.toByteArray();
            log.info("下载远程文件[{}]成功", uri);
        } catch (Exception e) {
            log.error("下载文件失败", e);
            throw e;
        }
    }
}
