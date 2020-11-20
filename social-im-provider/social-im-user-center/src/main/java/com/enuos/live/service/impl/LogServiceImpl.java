package com.enuos.live.service.impl;

import com.enuos.live.feign.ProducerFeign;
import com.enuos.live.pojo.Account;
import com.enuos.live.service.LogService;
import com.enuos.live.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @Description 日志
 * @Author wangyingjie
 * @Date 2020/11/4
 * @Modified
 */
@Slf4j
@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private ProducerFeign producerFeign;

    /**
     * @Description: 登陆日志
     * @Param: [account]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/11/4
     */
    @Async
    @Override
    public void sendLogin(Account account) {
        Map<String, Object> message = new HashMap<String, Object>() {
            {
                put("signature", 1);

                put("userId", account.userId);
                put("account", account.getAccount());
                put("level", Optional.ofNullable(account.getLevel()).orElse(1));
                put("type", account.getRegistType());
                put("deviceNumber", account.getDeviceNumber());
                put("model", account.getModel());
                put("address", account.getAddress());
                put("loginDevice", account.getLoginDevice());
                put("download", account.getDownload());
                put("logTime", DateUtils.getCurrentDateTime());
            }
        };

        producerFeign.sendLog(message);
    }
}
