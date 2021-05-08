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
import java.util.Objects;

/**
 * @ClassName: HttpDownFileHelper
 * @Description: TODO
 * @Author: CC
 * @Date 2021/5/8 9:59
 * @ModifyRecords: v1.0 new
 */
public class HttpDownFileHelper {
    private final Logger log = LoggerFactory.getLogger(HttpFileHelper.class);
    private String uri;
    private FileDownProcess process;
    private File localFile;
    //down file part
    private int rangeNo = 0;
    private Integer rangeStart;
    private Integer rangeEnd;
    //down file setting
    private int retry = 0;
    private int maxRetry;
    private int connectionTimeOut;
    private int readTimeOut;
    HttpURLConnection conn;
    //down file result
    private String uriDomain;
    private int contentSize;
    private String fileType;
    private int dbc = 0;
    private byte[] data;

    public static HttpDownFileHelper init(String uri) {
        HttpDownFileHelper helper = new HttpDownFileHelper();
        helper.uri = uri;
        helper.maxRetry = Profile.getDownFileMaxRetry();
        helper.connectionTimeOut = Profile.getDownFileConnectTimeout();
        helper.readTimeOut = Profile.getDownFileReadTimeout();
        return helper;
    }

    public HttpDownFileHelper watch(FileDownProcess process) {
        this.process = process;
        return this;
    }

    public HttpDownFileHelper range(int rangeNo, int rangeStart, int rangeEnd) {
        this.rangeNo = rangeNo;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        return this;
    }

    public HttpDownFileHelper localUri(String localUri) {
        this.localFile = new File(localUri);
        if (!this.localFile.exists())
            PublicUtil_FS.createFile(this.localFile);
        return this;
    }

    public HttpDownFileHelper request() {
        try {
            return request_0();
        } catch (SSLException | SocketTimeoutException e) {
            log.info("connect [{}] failure, ec: {}", uri, retry);
        } catch (ReadTimeoutException e) {
            log.info("read [{}] time out, ec: {}", uri, retry);
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            log.error("down ex, ec: {}", retry, e);
            throw new GlobalException(501, "request remote file [" + uri + "] failure.");
        }
        if (retry < maxRetry) {
            retry++;
            return request();
        } else {
            throw new GlobalException(501, "request remote file [" + uri + "] failure.");
        }
    }

    public void down() {
        try {
            if (localFile != null) {
                transToFile();
            } else {
                transToData();
            }
        } catch (SSLException | SocketTimeoutException e) {
            log.info("connect [{}] failure, ec: {}", uri, retry);
        } catch (ReadTimeoutException e) {
            log.info("read [{}] time out, ec: {}", uri, retry);
        } catch (GlobalException e) {
          throw e;
        } catch (Exception e) {
            log.error("down ex, ec: {}", retry, e);
            if (localFile != null)
                PublicUtil_FS.deleteFile(localFile);
            throw new GlobalException(501, "request remote file [" + uri + "] failure.");
        }
        if (retry < maxRetry) {
            retry++;
            process.dbcAdd(-dbc);
            down();
        } else {
            if (localFile != null)
                PublicUtil_FS.deleteFile(localFile);
            throw new GlobalException(501, "request remote file [" + uri + "] failure.");
        }
    }

    public void close() {
        this.conn.disconnect();
    }

    private HttpDownFileHelper request_0() throws IOException {
        URL url = new URL(uri);
        conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(connectionTimeOut);
        conn.setReadTimeout(readTimeOut);
        if (rangeStart != null && rangeEnd != null) {
            conn.setRequestProperty("Range", "bytes=" + rangeStart + "-" + rangeEnd);
        }
        conn.connect();
        uriDomain = HttpUtil.getDomain(conn);
        contentSize = Integer.parseInt(Objects.requireNonNullElse(conn.getHeaderField("Content-Length"), "-1"));
        String ct = conn.getHeaderField("Content-Type");
        if (ct == null)
            HttpUtil.printHeaders(conn);
        fileType = HttpUtil.getFileType(ct);
        return this;
    }

    private void transToFile() throws Exception {
        try (
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(this.localFile))
        ){
            readData(bos);
        }
    }

    private void transToData() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        readData(bos);
        data = bos.toByteArray();
    }

    private void readData(OutputStream os) throws IOException {
        try (
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream())
        ) {
            if (contentSize == -1) {
                contentSize = bis.available();
                if (process != null)
                    process.setTbc(contentSize);
            }
            byte[] b = new byte[1024 * 1024];
            int len;
            while ((len = read(bis, b, 0)) != -1) {
                os.write(b, 0, len);
                dbc += len;
                if (process != null)
                    process.dbcAdd(len);
            }
        }
        log.info("down remote file [{}_{}] success.", process == null ? uri : process.getName(), rangeNo);
    }

    private int read(InputStream is, byte[] tbs, int t) throws IOException {
        try {
            return is.read(tbs);
        } catch (IOException e) {
            if (t > 10) {
                throw e;
            }
            return read(is, tbs, ++t);
        }
    }

    public String getUriDomain() {
        return uriDomain;
    }

    public int getContentSize() {
        return contentSize;
    }

    public String getFileType() {
        return fileType;
    }

    public byte[] getData() {
        return data;
    }
}
