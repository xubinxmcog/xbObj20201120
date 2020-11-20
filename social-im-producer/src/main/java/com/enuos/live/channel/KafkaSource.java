package com.enuos.live.channel;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

/**
 * @Description 发送通道
 * @Author wangyingjie
 * @Date 2020/9/3
 * @Modified
 */
public interface KafkaSource {

    /** 发送消息通道[日志] */
    String OUTPUT_LOG = "output_log";

    @Output(OUTPUT_LOG)
    MessageChannel sendLogMessage();
}
