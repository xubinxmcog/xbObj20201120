package com.enuos.live.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Description 身份认证
 * @Author wangyingjie
 * @Date 14:11 2020/5/8
 * @Modified
 */
@Component
@FeignClient(name = "IDCARD-FEIGN", url = "https://idenauthen.market.alicloudapi.com")
public interface IdCardFeign {

    /**
     * @Description: 校验身份证
     * @Param: [params]
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/9/15
     */
    @RequestMapping(value = "/idenAuthentication", method = RequestMethod.POST, headers = {"Authorization=APPCODE c9ea28dabaa447bc9755f6c92aac74e8"}, produces = {"application/x-www-form-urlencoded;charset=UTF-8"})
    String idenAuthentication(@RequestParam Map<String, String> params);

}
