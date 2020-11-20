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
 * @Description 启动类
 * @Author wangyingjie
 * @Date 2020/6/23
 * @Modified
 */
@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@MapperScan("com.enuos.live.mapper")
@ComponentScan(basePackages = {"com.enuos"})
public class SocialImJobApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialImJobApplication.class, args);
    }
}
