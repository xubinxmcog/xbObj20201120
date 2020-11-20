package com.enuos.live.controller;

import com.enuos.live.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TestController
 * @Description: TODO 这是一个测试
 * @Author xubin
 * @Date 2020/7/16
 * @Version V2.0
 **/
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private RedisUtils redisUtils;

    @RequestMapping("/run")
    public Object run(String key, String item, String value) {
        return redisUtils.hset(key, item, value);

    }

//    @Value("${useLocal.cache}")
    private String useLocalCache = "1";

    @RequestMapping("/get")
    public String get() {
        return useLocalCache;
    }
}
