package com.enuos.live;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author WangCaiWen
 * Created on 2020/3/17 11:35
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.enuos.live.mapper")
@ComponentScan(basePackages = {"com.enuos"})
public class SocialImUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialImUserApplication.class, args);
    }
}
