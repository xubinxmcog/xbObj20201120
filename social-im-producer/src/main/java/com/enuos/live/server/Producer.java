package com.enuos.live.server;

import com.enuos.live.channel.KafkaSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Description 生产者
 * @Author wangyingjie
 * @Date 2020/9/2
 * @Modified
 */
@Slf4j
@Component
public class Producer {

    @Autowired
    private KafkaSource kafkaSource;

    /**
     * @Description: 发送日志
     * @Param: [message]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/3
     */
    public void sendLogMessage(Map<String, Object> message) {
        Object signature = message.get("signature");
        if (signature != null) {
            boolean result = kafkaSource.sendLogMessage().send(MessageBuilder.withPayload(message).setHeader("signature", signature).build());
            log.info("==========[MQ send log, message:{}, result:{}]", message, result);
        } else {
            log.info("==========[MQ do not send log, message:{}]", message);
        }
    }
}
