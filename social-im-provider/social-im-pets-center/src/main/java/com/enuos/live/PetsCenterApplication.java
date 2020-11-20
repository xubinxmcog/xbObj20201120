package com.enuos.live;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * TODO 宠物服务.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/4/1 12:46
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.enuos.live.mapper")
@ComponentScan(basePackages = {"com.enuos"})
public class PetsCenterApplication {

  public static void main(String[] args) {
    SpringApplication.run(PetsCenterApplication.class, args);
  }
}
