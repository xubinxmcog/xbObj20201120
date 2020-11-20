package com.enuos;

import com.enuos.live.channel.KafkaSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;

/**
 * @Description 生产者启动类
 * @Author wangyingjie
 * @Date 2020/9/3
 * @Modified
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients
@EnableBinding(KafkaSource.class)
public class SocialImProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialImProducerApplication.class, args);
    }
}
