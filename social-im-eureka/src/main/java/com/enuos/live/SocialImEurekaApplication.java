package com.enuos.live;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author WangCaiWen
 * Created on 2020/3/17 9:48
 */
@EnableEurekaServer
@SpringBootApplication
public class SocialImEurekaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialImEurekaApplication.class, args);
    }
}
