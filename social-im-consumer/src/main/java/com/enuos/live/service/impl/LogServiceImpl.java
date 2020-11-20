package com.enuos.live.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.enuos.live.mapper.LogMapper;
import com.enuos.live.service.LogService;
import com.enuos.live.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description 日志
 * @Author wangyingjie
 * @Date 2020/8/31
 * @Modified
 */
@Slf4j
@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LogMapper logMapper;

    /**
     * @Description: 记录登陆日志
     * @Param: [message]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/8/31
     */
    @Override
    @Transactional
    public void logLogin(Object message) {
        if (message == null) {
            return;
        }

        Map<String, Object> logLogin = JSONObject.parseObject(message.toString(), HashMap.class);

        logMapper.saveLogLogin(logLogin);
    }

    /**
     * @Description: 记录动态日志
     * @Param: [message]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/2
     */
    @Override
    @Transactional
    public void logPost(Object message) {
        if (message == null) {
            return;
        }

        Map<String, Object> logPost = JSONObject.parseObject(message.toString(), HashMap.class);

        logMapper.saveLogPost(logPost);
    }

    /**
     * @Description: 记录账单日志
     * @Param: [message]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/4
     */
    @Override
    public void logBill(Object message) {
        if (message == null) {
            return;
        }

        Map<String, Object> logBill = JSONObject.parseObject(message.toString(), HashMap.class);
        logBill.put("logTime", DateUtils.getCurrentDateTime());

        logMapper.saveLogBill(logBill);
    }

}
