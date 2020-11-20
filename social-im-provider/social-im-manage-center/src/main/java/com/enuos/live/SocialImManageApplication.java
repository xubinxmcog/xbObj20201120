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
 * @ClassName SocialImManageApplication
 * @Description: TODO 管理中心
 * @Author xubin
 * @Date 2020/4/14
 * @Version V1.0
 **/
@EnableFeignClients
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.enuos.live.mapper")
@ComponentScan(basePackages = {"com.enuos"})
public class SocialImManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialImManageApplication.class, args);
    }
}
