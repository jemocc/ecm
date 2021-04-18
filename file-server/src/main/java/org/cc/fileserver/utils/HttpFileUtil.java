package org.cc.fileserver.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.cc.common.exception.GlobalException;
import org.cc.fileserver.thread.M3u8Down;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLException;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpFileUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static void printHeaders(HttpURLConnection conn) {
        Map<String, List<String>> headers = conn.getHeaderFields();
        headers.forEach((k, v) -> System.out.println(k + ": " + v.toString()));
    }

    private static String getDomain(HttpURLConnection conn) {
        return conn.getURL().getProtocol() + "://" + conn.getURL().getHost();
    }

    public static HttpURLConnection doGet(String url, int c) {
        try {
            if (c > 10)
                throw new GlobalException(501, "最大尝试超过10次，直接失败");
            URL uri = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.connect();
            return conn;
        } catch (SSLException | SocketTimeoutException e) {
            System.out.println(e.getMessage());
            return doGet(url, ++c);
        } catch (IOException e) {
            e.printStackTrace();
            throw new GlobalException(501, "开启远程连接[" + url + "]失败");
        }
    }

    public static byte[] getData(InputStream is) throws IOException {
        try (
                ByteArrayOutputStream aos = new ByteArrayOutputStream(is.available());
                BufferedInputStream bis = new BufferedInputStream(is)
        ) {
            byte[] b = new byte[20480];
            int len;
            while ((len = bis.read(b)) != -1)
                aos.write(b, 0, len);
            return aos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String downloadFile(String remoteUri, String localUri) {
        System.out.println(remoteUri);
        HttpURLConnection conn = doGet(remoteUri, 0);
        String contentType = conn.getHeaderField("Content-Type");
        if ("application/vnd.apple.mpegURL".equals(contentType)) {
            String fileName = m3u8Down(conn, localUri);
            System.out.println(fileName + "下载完成");
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

    private static String m3u8Down(HttpURLConnection conn, String localUri) {
        List<String> tsFiles = new ArrayList<>();
        BigDecimal totalTime = BigDecimal.ZERO;
        String keyMethod = null;
        String keyUrl = null;
        try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#"))
                    tsFiles.add(line);
                else if (line.startsWith("#EXTINF:"))
                    totalTime = totalTime.add(BigDecimal.valueOf(Double.parseDouble(line.replaceAll("#EXTINF:([^,]*),", "$1"))));
                else if (line.startsWith("#EXT-X-KEY:")) {
                    Pattern r = Pattern.compile("METHOD=([^,]*),URI=\"([^\"]*)\"");
                    Matcher m = r.matcher(line);
                    if (m.find( )) {
                        keyMethod = m.group(1);
                        keyUrl = m.group(2);
                        if (keyMethod == null || keyUrl == null)
                            throw new GlobalException(501, "密钥获取异常：" + line);
                    } else
                        throw new GlobalException(501, "密钥获取失败：" + line);
                }
            }
            System.out.println(tsFiles.toString());
        } catch (IOException e) {
            e.printStackTrace();
            throw new GlobalException(501, "下载远程文件[" + conn.getURL().getPath() + "]失败");
        }
        String domain = getDomain(conn);
        if (tsFiles.get(0).endsWith(".m3u8")) {
            return downloadFile(domain + tsFiles.get(0), localUri);
        } else {
            System.out.println("总时长：" + totalTime.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP) + "min. total task num:" + tsFiles.size());
            String fileName = UUID.randomUUID() + ".mp4";
            File f = new File(localUri + File.separator + fileName);
            if (!f.exists())
                createFile(f);
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f))){
                CountDownLatch t = new CountDownLatch(tsFiles.size());
                Map<Integer, byte[]> isMap = new ConcurrentHashMap<>(tsFiles.size());
                ExecutorService es = getExecutor();
                for (int i = 0; i < tsFiles.size(); i++) {
                    M3u8Down task = new M3u8Down(i, isMap, t, domain + tsFiles.get(i));
                    es.submit(task);
                }
                String key = null;
                if (keyMethod != null) {
                    HttpURLConnection tc = doGet(domain + keyUrl, 0);
                    byte[] keys = getData(tc.getInputStream());
                    key = new String(keys);
                    System.out.println(key);
                }
                Cipher cipher = getCipher(key);
                t.await();
                es.shutdown();
                System.out.println();

                isMap.forEach((k, v) -> {
                    try {
                        if (cipher != null)
                            v=cipher.doFinal(v);
                        bos.write(v);
                        v=null;
                    } catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
                        e.printStackTrace();
                    }
                });
                return fileName;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                deleteFile(f);
                throw new GlobalException(501, "下载远程文件[" + conn.getURL().getPath() + "]失败");
            }
        }
    }

    private static Cipher getCipher(String key) {
        if (key == null)
            return null;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            //如果m3u8有IV标签，那么IvParameterSpec构造函数就把IV标签后的内容转成字节数组传进去
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(new byte[16]);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec);
            return cipher;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new GlobalException(501, "获取解码器失败");
        }
    }

    private static byte[] decrypt(byte[] sSrc, String sKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            SecretKeySpec keySpec = new SecretKeySpec(sKey.getBytes(StandardCharsets.UTF_8), "AES");
            //如果m3u8有IV标签，那么IvParameterSpec构造函数就把IV标签后的内容转成字节数组传进去
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(new byte[16]);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec);
            return cipher.doFinal(sSrc);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new GlobalException(501, "解码失败");
        }
    }

    @SuppressWarnings("")
    private static void createFile(File f) {
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

    private static void deleteFile(File file) {
        if (file.delete())
            System.out.println("删除文件" + file.getName() + "成功");
        else
            System.out.println("删除文件" + file.getName() + "失败");
    }

    public static void main(String[] args) {
        downloadFile("*", "D:/file_local");
    }

    public static ExecutorService getExecutor() {
        return new ThreadPoolExecutor(2, 100, 2, TimeUnit.SECONDS,
                new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
