package org.cc.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class PlatformUtil {
    private static final Logger log = LoggerFactory.getLogger(PlatformUtil.class);

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

}
