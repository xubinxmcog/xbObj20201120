package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 账户
 * @Author wangyingjie
 * @Date 14:16 2020/4/1
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Account extends Base implements Serializable {

    private static final long serialVersionUID = 2191726825302586317L;

    /** 注册类型 [0：手机号注册；1：微信注册；2：QQ注册； 3：苹果注册] */
    private Integer registType;

    /** 账号 */
    private String account;

    /** 手机号 */
    private String phone;

    /** 短信验证码 */
    private String code;

    /** 微信唯一标识 */
    private String unionId;

    /** 微信[openid] */
    private String wOpenId;

    /** QQ[openid] */
    private String qOpenId;

    /** 微信或者QQ[openid] */
    private String openId;

    /** AppleUserId */
    private String appleUserId;

    /** Apple */
    private String identityToken;

    /** 账号来源 [1 IOS 2 Android 3 平台] */
    private Integer platform;

    /** 账号状态 [0 正常 1 封禁] */
    private Integer status;

    /** 等级 */
    private Integer level;

    /** 登陆设备[1：IOS；2：Android；3：平台] */
    private Integer loginDevice;

    /** 经度 */
    private Double longitude;

    /** 纬度 */
    private Double latitude;

    /** 设备号 */
    private String deviceNumber;

    /** 机型 */
    private String model;

    /** IP/MAC地址 */
    private String address;

    /** 下载渠道[1：AppleStore；2：其他] */
    private Integer download;

    /** 用户信息 */
    private User user;

}
