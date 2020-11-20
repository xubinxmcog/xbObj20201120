package com.enuos.live.feign.impl;

import com.enuos.live.feign.ProducerFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/9/30
 * @Modified
 */
@Slf4j
@Component
public class ProducerFeignFallback implements ProducerFeign {

    /**
     * @Description: 发送日志消息
     * @Param: [message]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/3
     */
    @Override
    public void sendLog(Map<String, Object> message) {
        log.error("==========[SOCIAL-IM-USER ProducerFeign sendLog error, message={}]", message);
        return;
    }
}
