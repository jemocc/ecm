package org.cc.fileserver.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.cc.common.exception.GlobalException;
import org.cc.common.utils.DateTimeUtil;
import org.cc.fileserver.model.Profile;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.Security;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HttpFileUtil {

    private static String configPath;

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

    public static HttpURLConnection doGetForConn(String url, int r, int m) {
        try {
            URL uri = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
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

    public static String getLocalUri(String type) {
        return DateTimeUtil.getCurrentDate() + File.separator + UUID.randomUUID().toString().replace("-", "") + "." + type;
    }

    public static String getFullLocalUri(String localPath) {
        return Profile.getConfigPath() + File.separator + localPath;
    }

    public static String down(String remoteUri) {
        return down(remoteUri, 0, 20);
    }

    public static String down(String remoteUri, int n, int m) {
        HttpURLConnection conn = HttpFileUtil.doGetForConn(remoteUri, 0, 20);
        String ct = conn.getHeaderField("Content-Type");
        if (ct == null)
            printHeaders(conn);
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

    public static byte[] down2(String remoteUri) {
        return down2(remoteUri, 0, 20);
    }
    public static byte[] down2(String remoteUri, int n, int m) {
        HttpURLConnection conn = HttpFileUtil.doGetForConn(remoteUri, 0, 20);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (BufferedInputStream bis = new BufferedInputStream(conn.getInputStream())){
            byte[] b = new byte[20 * 1024];
            int len;
            while ((len = bis.read(b)) != -1)
                bos.write(b, 0, len);
            return bos.toByteArray();
        } catch (Exception e) {
            if (n < m) {
                System.out.println("下载远程文件[" + remoteUri + "]失败重试[" + n + "]：" + e.getMessage());
                sleep(200);
                return down2(remoteUri, n + 1, m);
            } else {
                e.printStackTrace();
                throw new GlobalException(501, "下载远程文件[" + remoteUri + "]失败");
            }
        }
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

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
