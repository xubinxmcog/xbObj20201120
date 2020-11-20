package com.enuos.live.channel;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

/**
 * @Description 订阅通道
 * @Author wangyingjie
 * @Date 2020/9/3
 * @Modified
 */
public interface KafkaSink {

    /** 日志接收消息通道 */
    String INPUT_LOG = "input_log";

    @Input(INPUT_LOG)
    SubscribableChannel receiveLogMessage();
}
