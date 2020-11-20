package com.enuos.live.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.enuos.live.bean.WeChatBean;
import com.enuos.live.feign.WeChatFeign;
import com.enuos.live.mapper.WeChatMapper;
import com.enuos.live.pojo.WeChat;
import com.enuos.live.result.Result;
import com.enuos.live.service.WeChatService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @Description
 * @Author wangyingjie
 * @Date 11:09 2020/5/8
 * @Modified
 */
@Slf4j
@Service
public class WeChatServiceImpl implements WeChatService {

    @Autowired
    private WeChatMapper weChatMapper;

    @Autowired
    private WeChatBean weChatBean;

    @Autowired
    private WeChatFeign weChatFeign;

    /**
     * @Description: 通过code获取unionid
     * @Param: [code]
     * @Return: com.alibaba.fastjson.JSONObject
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    public JSONObject getAccessToken(String code) {
        if (StringUtils.isBlank(code)) {
            log.error("WeChat code is null");
            return null;
        }

        String atJson = weChatFeign.getAccessToken(weChatBean.appid, weChatBean.secret, code, weChatBean.grantType);
        JSONObject atJsonObject = JSONObject.parseObject(atJson);
        if (atJsonObject.containsKey("errcode")) {
            log.error("WeChat get access_token has error [response : {}]", atJson);
            return null;
        }

        return atJsonObject;
    }

    /**
     * @Description: 通过access_token 和 open_id 获取用户信息
     * @Param: [jsonObject]
     * @Return: com.alibaba.fastjson.JSONObject
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    public JSONObject getUserinfo(JSONObject jsonObject) {
        if (Objects.isNull(jsonObject)) {
            log.error("WeChat access_token is null");
            return null;
        }

        // 调用凭证和普通用户的标识
        String accessToken = jsonObject.getString("access_token");
        String openid = jsonObject.getString("openid");

        String userJson = weChatFeign.getUserinfo(accessToken, openid);
        JSONObject userJsonObject = JSONObject.parseObject(userJson);
        if (userJsonObject.containsKey("errcode")) {
            log.error("WeChat get userinfo has error [response : {}]", userJson);
            return null;
        }

        return userJsonObject;
    }

    /**
     * @Description: 绑定微信支付
     * @Param: [weChat]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/11/6
     */
    @Override
    public Result bindPay(WeChat weChat) {
        WeChat target = weChatMapper.getWeChat(weChat.getUserId());
        if (target != null) {
            return Result.error(201, "该用户已经绑定微信支付");
        }
        Integer result = weChatMapper.save(weChat);
        return result > 0 ? Result.success() : Result.error();
    }

}
