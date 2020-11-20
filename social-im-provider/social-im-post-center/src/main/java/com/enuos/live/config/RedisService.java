package com.enuos.live.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author WangCaiWen
 * Created on 2020/3/17 14:59
 */
@Component
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;
}
