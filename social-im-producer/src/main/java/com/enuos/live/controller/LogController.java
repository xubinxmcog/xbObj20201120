package com.enuos.live.controller;

import com.enuos.live.server.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Description 日志
 * @Author wangyingjie
 * @Date 2020/9/3
 * @Modified
 */
@Slf4j
@RestController
@RequestMapping("/log")
public class LogController {

    @Autowired
    private Producer producer;

    /**
     * @Description: 发送日志
     * @Param: [message]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/3
     */
    @PostMapping("/sendLog")
    public void sendLog(@RequestBody Map<String, Object> message) {
        producer.sendLogMessage(message);
    }

}
