package org.cc.fileserver.model;

import io.netty.handler.timeout.ReadTimeoutException;
import org.cc.common.exception.GlobalException;
import org.cc.common.utils.PublicUtil;
import org.cc.fileserver.utils.FileUtil;
import org.cc.fileserver.utils.HttpUtil;
import org.cc.fileserver.utils.M3U8Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.net.ssl.SSLException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName: HttpDownFileHelper
 * @Description: TODO
 * @Author: CC
 * @Date 2021/5/8 9:59
 * @ModifyRecords: v1.0 new
 */
public class HttpDownFileHelper {
    private final Logger log = LoggerFactory.getLogger(HttpDownFileHelper.class);
    private String uri;
    private FileDownProcess process;
    private OutputStream localFileOS;
    //down file part
    private int rangeNo = 0;
    private Integer rangeStart;
    private Integer rangeEnd;
    //down file setting
    private int retry = 0;
    private int maxRetry;
    private int connectionTimeOut;
    private int readTimeOut;
    private HttpURLConnection conn;
    //down file result
    private int contentSize;
    private String fileType;
    private int dbc = 0;
    private byte[] data;
    //m3u8
    private boolean isM3U8 = false;
    private boolean isM3U8Part = false;
    private String uriDomain;
    private int totalTime;
    private List<String> partUri;
    private Cipher cipher;

    public static HttpDownFileHelper init(String uri) {
        HttpDownFileHelper helper = new HttpDownFileHelper();
        helper.uri = uri;
        helper.maxRetry = Profile.getDownFileMaxRetry();
        helper.connectionTimeOut = Profile.getDownFileConnectTimeout();
        helper.readTimeOut = Profile.getDownFileReadTimeout();
        if (uri.endsWith(".m3u8"))
            helper.isM3U8 = true;
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

    public HttpDownFileHelper localFile(OutputStream localFileOS) {
        this.localFileOS = localFileOS;
        return this;
    }

    public HttpDownFileHelper m3u8Part() {
        isM3U8Part = true;
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
            process.failure();
            throw new GlobalException(501, "request remote file [" + uri + "] failure.");
        }
        if (retry < maxRetry) {
            retry++;
            return request();
        } else {
            process.failure();
            throw new GlobalException(501, "request remote file [" + uri + "] failure.");
        }
    }

    public void down() {
        try {
            if (localFileOS != null) {
                transToFile();
            } else {
                transToData();
            }
            return;
        } catch (SSLException | SocketTimeoutException e) {
            log.info("connect [{}] failure, ec: {}", uri, retry);
        } catch (ReadTimeoutException e) {
            log.info("read [{}] time out, ec: {}", uri, retry);
        } catch (GlobalException e) {
          throw e;
        } catch (Exception e) {
            log.error("down ex, ec: {}", retry, e);
            process.failure();
            throw new GlobalException(501, "request remote file [" + uri + "] failure.");
        }
        if (retry < maxRetry) {
            retry++;
            process.dbcAdd(isM3U8 ? -1 : -dbc);
            down();
        } else {
            process.failure();
            throw new GlobalException(501, "request remote file [" + uri + "] failure.");
        }
    }

    public void close() {
        this.conn.disconnect();
        this.data = null;
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
        if (ct == null && !isM3U8 && !isM3U8Part)
            HttpUtil.printHeaders(conn);
        fileType = FileUtil.getFileType(ct);
        if (isM3U8) {
            down();
            M3U8Util.readM3U8FileData(this);
            contentSize = partUri.size();
            fileType = "mp4";
        }
        return this;
    }

    private void transToFile() throws Exception {
        try (
                BufferedOutputStream bos = new BufferedOutputStream(localFileOS)
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
            if (contentSize == -1 && !isM3U8 && !isM3U8Part) {
                contentSize = bis.available();
                if (process != null)
                    process.setTbc(contentSize);
            }
            byte[] b = new byte[20 * 1024];
            int len;
            while ((len = read(bis, b, 0)) != -1) {
                os.write(b, 0, len);
                dbc += len;
                if (process != null && !isM3U8 && !isM3U8Part)
                    process.dbcAdd(len);
            }
        }
        if (isM3U8Part)
            process.dbcAdd(1);
        log.info("download part [{}_{}] success.", uri, rangeNo);
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

    public int getTotalTime() {
        return totalTime;
    }

    public int getRangeNo() {
        return rangeNo;
    }

    public boolean isM3U8() {
        return isM3U8;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public void setCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    public Cipher getCipher() {
        return cipher;
    }

    public void setPartUri(List<String> partUri) {
        this.partUri = partUri;
    }

    public List<String> getPartUri() {
        return partUri;
    }
}
