package org.cc.fileserver.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.cc.common.exception.GlobalException;
import org.cc.fileserver.entity.M3U8Video;
import org.cc.fileserver.entity.Video;
import org.cc.fileserver.entity.enums.FileFormType;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.Security;
import java.util.List;
import java.util.Map;

public class HttpFileUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static void printHeaders(HttpURLConnection conn) {
        Map<String, List<String>> headers = conn.getHeaderFields();
        headers.forEach((k, v) -> System.out.println(k + ": " + v.toString()));
    }

    public static String getDomain(HttpURLConnection conn) {
        return conn.getURL().getProtocol() + "://" + conn.getURL().getHost() + ":" + conn.getURL().getPort();
    }

    public static byte[] doGet(String url, int r, int m) {
        try {
            URL uri = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.connect();
            return readData(conn);
        } catch (SSLException | SocketTimeoutException e) {
            if (r < m)
                return doGet(url, r+1, m);
            else
                throw new GlobalException(501, "开启远程连接[" + url + "]失败");
        } catch (IOException e) {
            e.printStackTrace();
            throw new GlobalException(501, "开启远程连接[" + url + "]失败");
        }
    }

    public static HttpURLConnection doGetForConn(String url, int r, int m) {
        try {
            URL uri = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.connect();
            return conn;
        } catch (SSLException | SocketTimeoutException e) {
            if (r<m)
                return doGetForConn(url, ++r, m);
            else {
                e.printStackTrace();
                throw new GlobalException(501, "开启远程连接[" + url + "]失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new GlobalException(501, "开启远程连接[" + url + "]失败");
        }
    }

    public static byte[] readData(HttpURLConnection conn) throws IOException {
        try (
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                ByteArrayOutputStream aos = new ByteArrayOutputStream(bis.available())
        ) {
            byte[] b = new byte[20480];
            int len;
            while ((len = bis.read(b)) != -1)
                aos.write(b, 0, len);
            return aos.toByteArray();
        } catch (IOException e) {
            throw e;
        }
    }

    public static String downloadFile(String remoteUri, String localUri) {
        System.out.println(remoteUri);
        HttpURLConnection conn = doGetForConn(remoteUri, 0, 20);
        String contentType = conn.getHeaderField("Content-Type");
        if ("application/vnd.apple.mpegURL".equals(contentType)) {
            Video video = M3U8Video.ofNew("测试", ".mp4", remoteUri, null, FileFormType.REMOTE, localUri);
            video.beginDown();
        } else {
            File localFile = new File(localUri);
            if (!localFile.exists())
                createFile(localFile);
            try (
                    BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(localFile));
            ){
                byte[] b = new byte[20 * 1024];
                int len;
                while ((len = bis.read(b)) != -1)
                    bos.write(b, 0, len);
            } catch (IOException e) {
                e.printStackTrace();
                throw new GlobalException(501, "下载远程文件[" + remoteUri + "]失败");
            }
        }
        return null;
    }

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
            System.out.println("删除文件" + file.getName() + "成功");
        else
            System.out.println("删除文件" + file.getName() + "失败");
    }

    public static void main(String[] args) {
        downloadFile("https://www.dgzhuorui.com:65/20200820/uxynyBPa/1200kb/hls/index.m3u8", "D:/file_local/t.mp4");
    }

}
