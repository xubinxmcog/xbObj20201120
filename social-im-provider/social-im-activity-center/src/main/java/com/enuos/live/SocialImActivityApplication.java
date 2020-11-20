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
 * @Description 活动中心启动类
 * @Author wangyingjie
 * @Date 2020/9/16
 * @Modified
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.enuos.live.mapper")
@ComponentScan(basePackages = {"com.enuos"})
public class SocialImActivityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialImActivityApplication.class, args);
    }

}
