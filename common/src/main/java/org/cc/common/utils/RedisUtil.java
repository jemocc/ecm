package org.cc.common.utils;

import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {
    private static RedissonClient redissonClient;

    public RedisUtil(RedissonClient redissonClient) {
        RedisUtil.redissonClient = redissonClient;
    }


}
