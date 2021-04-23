package org.cc.fileserver.utils;

import org.cc.common.exception.GlobalException;
import org.cc.fileserver.model.HttpFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: M3U8Util
 * @Description: TODO
 * @Author: CC
 * @Date 2021/4/18 10:14
 * @ModifyRecords: v1.0 new
 */
public class M3U8Util {
    private static final Logger log = LoggerFactory.getLogger(M3U8Util.class);

    public static Cipher getCipher(String key) {
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
            log.error("获取解码器失败", ex);
            throw new GlobalException(501, "获取解码器失败");
        }
    }

    public static Cipher getCipher(String keyMethod, String keyUrl) {
        if (keyUrl == null)
            return null;
        HttpFileHelper helper = HttpFileHelper.uri(keyUrl).down();
        String key = readM3U8FileData(helper.getData()).get(0);
        log.info("获取解密密钥:{}", key);
        return getCipher(key);
    }

    public static List<String> readM3U8FileData(byte[] data) {
        String d = new String(data);
        return Arrays.asList(d.split("[\r\n]+"));
    }

}
