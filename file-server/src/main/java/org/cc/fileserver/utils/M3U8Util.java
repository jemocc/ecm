package org.cc.fileserver.utils;

import org.cc.common.exception.GlobalException;
import org.cc.fileserver.model.HttpDownFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        HttpDownFileHelper helper = HttpDownFileHelper.init(keyUrl).request();
        helper.down();
        String key = readM3U8FileData(helper.getData()).get(0);
        log.info("获取解密密钥:{}", key);
        return getCipher(key);
    }

    public static void readM3U8FileData(HttpDownFileHelper helper) {
        List<String> data = readM3U8FileData(helper.getData());
        BigDecimal totalTime = BigDecimal.ZERO;
        List<String> tss = new ArrayList<>();
        for (String line : data) {
            if (!line.startsWith("#"))
                tss.add(line);
            else if (line.startsWith("#EXTINF:"))
                totalTime = totalTime.add(BigDecimal.valueOf(Double.parseDouble(line.replaceAll("#EXTINF:([^,]*),", "$1"))));
            else if (line.startsWith("#EXT-X-KEY:")) {
                Pattern r = Pattern.compile("METHOD=([^,]*),URI=\"([^\"]*)\"");
                Matcher m = r.matcher(line);
                if (m.find()) {
                    Cipher cipher = getCipher(m.group(1), m.group(2));
                    if (cipher == null)
                        throw new GlobalException(501, "密钥获取异常：" + line);
                    else
                        helper.setCipher(cipher);
                } else
                    throw new GlobalException(501, "密钥获取失败：" + line);
            }
        }
        helper.setPartUri(tss);
        helper.setTotalTime((int) Math.ceil(totalTime.doubleValue()));
        if (tss.get(0).endsWith(".m3u8")) {
            HttpDownFileHelper h = HttpDownFileHelper.init(helper.getUriDomain() + tss.get(0)).request();
            helper.setTotalTime(h.getTotalTime());
            helper.setPartUri(h.getPartUri());
            helper.setCipher(Objects.requireNonNullElse(h.getCipher(), helper.getCipher()));
        }
    }

    public static List<String> readM3U8FileData(byte[] data) {
        String d = new String(data);
        return Arrays.asList(d.split("[\r\n]+"));
    }



}
