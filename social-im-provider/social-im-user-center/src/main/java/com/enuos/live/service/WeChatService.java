package com.enuos.live.service;

import com.alibaba.fastjson.JSONObject;
import com.enuos.live.pojo.WeChat;
import com.enuos.live.result.Result;

/**
 * @Description 微信业务
 * @Author wangyingjie
 * @Date 11:07 2020/5/8
 * @Modified
 */
public interface WeChatService {

    /**
     * @Description: 通过code获取unionid
     * @Param: [code]
     * @Return: com.alibaba.fastjson.JSONObject
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    JSONObject getAccessToken(String code);

    /**
     * @Description: 通过access_token 和 open_id 获取用户信息
     * @Param: [jsonObject]
     * @Return: com.alibaba.fastjson.JSONObject
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    JSONObject getUserinfo(JSONObject jsonObject);

    /** 
     * @Description: 绑定微信支付
     * @Param: [weChat] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/11/6 
     */ 
    Result bindPay(WeChat weChat);
}
