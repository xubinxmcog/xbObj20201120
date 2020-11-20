package com.enuos.live;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author WangCaiWen
 * Created on 2020/4/24 9:29
 */
//@RefreshScope
@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.enuos.live.mapper")
@ComponentScan(basePackages = {"com.enuos"})
public class SocialVoiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialVoiceApplication.class, args);
    }
}
