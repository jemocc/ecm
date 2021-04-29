package org.cc.common.component;

import org.cc.common.utils.PublicUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RedisOperator {
    private static final Logger log = LoggerFactory.getLogger(RedisOperator.class);
    private static RedissonClient redissonClient;

    public RedisOperator(RedissonClient redissonClient) {
        RedisOperator.redissonClient = redissonClient;
    }

    public static RLock getLock(String key) {
        while (redissonClient == null) {
            PublicUtil.sleep(100);
        }
        key = "locks:" + key;
        log.info("get lock： {}", key);
        return redissonClient.getLock(key);
    }

}
