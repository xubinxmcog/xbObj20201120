package com.enuos.live;

import com.enuos.live.server.GameServer;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * TODO 游戏服务启动类.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version v1.0.0
 * @since 2020/5/15 12:39
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@ComponentScan(basePackages = {"com.enuos"})
public class GameServerApplication implements CommandLineRunner {

  @Resource
  private GameServer gameServer;

  public static void main(String[] args) {
    SpringApplication.run(GameServerApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    CompletableFuture.runAsync(() -> this.gameServer.startServer());
  }
}
