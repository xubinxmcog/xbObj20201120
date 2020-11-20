package com.enuos.live.server;

import com.enuos.live.channel.KafkaSink;
import com.enuos.live.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * @Description 订阅
 * @Author wangyingjie
 * @Date 2020/9/2
 * @Modified
 */
@Slf4j
@Component
public class Consumer {

    @Autowired
    private LogService logService;

    /**
     * @Description: 日志订阅
     * @Param: [message]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/3
     */
    @StreamListener(KafkaSink.INPUT_LOG)
    public void receiveLogMessage(Message<String> message) {
        int signature = (int) message.getHeaders().get("signature");
        Object payload = message.getPayload();
        log.info("==========[MQ receive log, message:{}]", payload);
        switch (signature) {
            case 1:
                logService.logLogin(payload);
                break;
            case 2:
                logService.logPost(payload);
                break;
            case 3:
                logService.logBill(payload);
                break;
            default:
                break;
        }
    }
}
