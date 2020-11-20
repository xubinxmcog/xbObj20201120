package com.enuos.live.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description 微信第三方
 * @Author wangyingjie
 * @Date 14:21 2020/4/29
 * @Modified
 */
@Component
@FeignClient(name = "WECHAT-FEIGN", url = "https://api.weixin.qq.com")
public interface WeChatFeign {

    /**
     * 获取access_token
     * @param appid
     * @param secret
     * @param code
     * @param grantType
     * @return
     */
    @GetMapping(value = "/sns/oauth2/access_token")
    String getAccessToken(@RequestParam("appid") String appid, @RequestParam("secret") String secret, @RequestParam("code") String code, @RequestParam("grant_type") String grantType);

    /**
     * 获取userinfo
     * @param accessToken
     * @param openid
     * @return
     */
    @GetMapping(value = "/sns/userinfo")
    String getUserinfo(@RequestParam("access_token") String accessToken, @RequestParam("openid") String openid);

}
