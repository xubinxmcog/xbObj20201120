package com.enuos.live.bean;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Description 阿里云
 * @Author wangyingjie
 * @Date 15:00 2020/4/29
 * @Modified
 */
@Component
@Data
public class AliyunBean {

    @Value("${aliyun.sms.region-id}")
    public String regionId;

    @Value("${aliyun.sms.access-key-id}")
    public String accessKeyId;

    @Value("${aliyun.sms.secret}")
    public String secret;

    @Value("${aliyun.sms.domain}")
    public String domain;

    @Value("${aliyun.sms.action}")
    public String action;

    @Value("${aliyun.sms.version}")
    public String version;

    @Value("${aliyun.sms.sign-name}")
    public String signName;

    @Value("${aliyun.sms.regist-template-code}")
    public String registTemplateCode;

    @Value("${aliyun.sms.login-template-code}")
    public String loginTemplateCode;

    @Value("${aliyun.sms.on-off}")
    public boolean onOff;

}
