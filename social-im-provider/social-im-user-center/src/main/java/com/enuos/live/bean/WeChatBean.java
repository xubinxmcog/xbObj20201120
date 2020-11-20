package com.enuos.live.bean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author wangyingjie
 * @Date 15:38 2020/4/29
 * @Modified
 */
@Component
public class WeChatBean {

    @Value("${wechat.appid}")
    public String appid;

    @Value("${wechat.secret}")
    public String secret;

    @Value("${wechat.grant-type}")
    public String grantType;

}
