package com.enuos.live.feign;

import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Description apple
 * @Author wangyingjie
 * @Date 2020/6/1
 * @Modified
 */
@Component
@FeignClient(name = "APPLE-FEIGN", url = "https://appleid.apple.com")
public interface AppleFeign {

    /**
     * @Description:
     * @Param: []
     * @Return: com.alibaba.fastjson.JSONObject
     * @Author: wangyingjie
     * @Date: 2020/6/15
     */
    @GetMapping(value = "/auth/keys")
    JSONObject authKeys();

}
