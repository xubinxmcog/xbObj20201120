package com.enuos.live;

import com.enuos.live.server.WebSocketServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

/**
 * TODO Socket服务.
 * TODO 声明CommandLineRunner接口，实现run方法，就能给启动项目同时启动Socket服务
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/3/17 10:11
 */

@SpringBootApplication
@EnableDiscoveryClient
//@EnableHystrix
//@EnableHystrixDashboard
@EnableFeignClients
@ComponentScan(basePackages = {"com.enuos"})
public class SocketServerApplication implements CommandLineRunner {

  @Resource
  private WebSocketServer webSocketServer;

  public static void main(String[] args) {
    SpringApplication.run(SocketServerApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    CompletableFuture.runAsync(() -> this.webSocketServer.startServer());
  }
}