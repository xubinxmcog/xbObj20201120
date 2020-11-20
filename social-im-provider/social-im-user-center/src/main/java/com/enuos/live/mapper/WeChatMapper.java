package com.enuos.live.mapper;

import com.enuos.live.pojo.WeChat;

/**
 * @Description 微信平台信息
 * @Author wangyingjie
 * @Date 2020/11/6
 * @Modified
 */
public interface WeChatMapper {
    /**
     * @Description:
     * @Param: [userId]
     * @Return: com.enuos.live.pojo.WeChat
     * @Author: wangyingjie
     * @Date: 2020/11/6
     */
    WeChat getWeChat(Long userId);

    /**
     * @Description: 授权微信支付
     * @Param: [weChat]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/11/6
     */
    Integer save(WeChat weChat);
}
