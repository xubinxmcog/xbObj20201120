package com.enuos;

import com.enuos.live.channel.KafkaSink;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;

/**
 * @Description 消费者启动类
 * @Author wangyingjie
 * @Date 2020/9/3
 * @Modified
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableBinding(KafkaSink.class)
@MapperScan("com.enuos.live.mapper")
public class SocialImConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialImConsumerApplication.class, args);
    }
}
