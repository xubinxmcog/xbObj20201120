package com.enuos.live;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @ClassName SocialImOrderApplication
 * @Description: TODO 订单中心
 * @Author xubin
 * @Date 2020/4/3
 * @Version V1.0
 **/
@EnableDiscoveryClient
@EnableScheduling
@SpringBootApplication
@EnableFeignClients
@MapperScan("com.enuos.live.mapper")
@ComponentScan(basePackages = {"com.enuos"})
public class SocialImOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialImOrderApplication.class, args);
    }
}
