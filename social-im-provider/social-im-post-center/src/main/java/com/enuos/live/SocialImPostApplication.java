package com.enuos.live;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @Author wangyingjie
 * @Description 
 * @Date Created in 9:53 2020/3/27
 * @Modified by 
 */
@EnableDiscoveryClient
@SpringBootApplication
//@EnableEurekaClient
@EnableFeignClients
@EnableAsync
@MapperScan("com.enuos.live.mapper")
@ComponentScan(basePackages = {"com.enuos"})
public class SocialImPostApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialImPostApplication.class, args);
    }
}
