package com.enuos.live;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author WangCaiWen
 * Created on 2020/3/17 11:04
 */
@EnableScheduling
@EnableFeignClients
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.enuos"})
public class SocialImGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialImGatewayApplication.class, args);
    }
}
