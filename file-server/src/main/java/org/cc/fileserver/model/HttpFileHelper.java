package org.cc.fileserver.model;

import com.google.gson.JsonObject;
import io.netty.handler.timeout.ReadTimeoutException;
import org.cc.common.component.WSService;
import org.cc.common.config.ThreadPool;
import org.cc.common.exception.GlobalException;
import org.cc.common.model.EventMessage;
import org.cc.common.pojo.EventMessageType;
import org.cc.common.utils.PublicUtil;
import org.cc.fileserver.utils.HttpUtil;
import org.cc.fileserver.utils.PublicUtil_FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.Future;

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

    private Integer rangeNo;
    private Integer rangeStart;
    private Integer rangeEnd;

    private String domain;
    private byte[] data;
    private String localUri;
    private String type;
    private Future<?> watchFuture = null;

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

    public String getType() {
        return type;
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
            if (watchFuture != null)
                watchFuture.cancel(true);
            throw new GlobalException(501, "远程文件[" + uri + "]下载失败");
        }
        retry++;
        if (retry < maxRetry) {
            return down();
        } else {
            if (watchFuture != null)
                watchFuture.cancel(true);
            throw new GlobalException(501, "远程文件[" + uri + "]下载失败");
        }
    }

    private void doGet() throws Exception {
        if (watchFuture != null) {
            watchFuture.cancel(true);
            watchFuture = null;
        }
        URL url = new URL(uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(connectionTimeOut);
        conn.setReadTimeout(readTimeOut);
        if (rangeStart != null && rangeEnd != null) {
            conn.setRequestProperty("Range", "bytes=" + rangeStart + "-" + rangeEnd);
        }
        conn.connect();
        domain = HttpUtil.getDomain(conn);

        if (toFile) {
            transDataToFile(conn);
        } else {
            transDataToData(conn);
        }
    }

    private int getContentLen() {
        try {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(connectionTimeOut);
            conn.setReadTimeout(readTimeOut);
            conn.connect();
            String cl = conn.getHeaderField("Content-Length");
            retry = 0;
            return cl == null ? -1 : Integer.parseInt(cl);
        } catch (Exception e) {
            retry++;
            if (retry < maxRetry) {
                return getContentLen();
            } else {
                throw new GlobalException(501, "远程文件[" + uri + "]文件长度获取失败");
            }
        }
    }

    private void transDataToFile(HttpURLConnection conn) throws Exception {
        String ct = conn.getHeaderField("Content-Type");
        String cl = conn.getHeaderField("Content-Length");
        if (ct == null)
            HttpUtil.printHeaders(conn);
        type = HttpUtil.getFileType(ct);
        localUri = HttpUtil.getLocalUri(type);
        File file = new File(HttpUtil.getFullLocalUri(localUri));
        if (!file.exists())
            PublicUtil_FS.createFile(file);
        try (
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))
        ){
            int tcl = cl == null ? bis.available() : Integer.parseInt(cl);
            SendDownStateToWatch watch = new SendDownStateToWatch(this);
            watch.setTcl(tcl);
            watchFuture = ThreadPool.submit(watch);
            int hcl = 0;
            byte[] b = new byte[1024 * 1024];
            int len;
            while ((len = read(bis, b, 0)) != -1) {
                bos.write(b, 0, len);
                hcl += len;
                watch.setHcl(hcl);
            }
            if (watchFuture != null)
                watchFuture.cancel(true);
            log.info("下载远程文件[{}]成功", uri);
        }
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
            if (watchFuture != null)
                watchFuture.cancel(true);
            log.info("下载远程文件[{}]成功", uri);
        } catch (Exception e) {
            log.error("下载文件失败", e);
            throw e;
        }
    }


    private static class SendDownStateToWatch implements Runnable{
        private final HttpFileHelper helper;
        private int tcl;
        private int hcl;

        public SendDownStateToWatch(HttpFileHelper helper) {
            this.helper = helper;
        }

        public void setTcl(int tcl) {
            this.tcl = tcl;
        }

        public void setHcl(int hcl) {
            this.hcl = hcl;
        }

        @Override
        public void run() {
            while (true) {
                long start = System.currentTimeMillis();
                sendMessage();
                long sleep = 1000 - (System.currentTimeMillis() - start);
                if (sleep > 0) {
                    try {
                        PublicUtil.sleep(Long.valueOf(sleep).intValue());
                    } catch (RuntimeException e) {
                        break;
                    }
                }
            }
            sendMessage();
            log.info("文件下载监控停止");
        }

        private void sendMessage() {
            JsonObject object = new JsonObject();
            object.addProperty("fn", helper.uri.replaceAll("^.*/([^/]*)$", "$1"));
            object.addProperty("tcl", tcl);
            object.addProperty("hcl", hcl);
            EventMessage<JsonObject> msg = new EventMessage<>(EventMessageType.FILE_DOWN_WATCH, object);
            WSService.sendMessageToWatcher(msg);
        }
    }
}
