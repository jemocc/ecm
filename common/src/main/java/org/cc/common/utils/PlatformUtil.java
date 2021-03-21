package org.cc.common.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class PlatformUtil {

    public static void log(Environment env) {
        try {
            log.info("\n" +
                            "----------------------------------------------------------\n" +
                            "\tApplication '{}' is running! Access URLs:\n" +
                            "\t\t     Local: http://localhost:{}\n" +
                            "\t\t  External: http://{}:{}\n" +
                            "\t\tProfile(s): {}\n" +
                            "----------------------------------------------------------",
                    env.getProperty("spring.application.name"),
                    env.getProperty("server.port"),
                    InetAddress.getLocalHost().getHostAddress(),
                    env.getProperty("server.port"),
                    env.getActiveProfiles());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static String toJSONStr (Object obj) {
        return JSON.toJSONString(obj);
    }
}
