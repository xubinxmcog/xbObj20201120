package com.enuos.live.config;

import feign.Feign;
import feign.Request;
import feign.Retryer;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TODO Feign配置.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/3/16 10:50
 */

@Configuration
@ConditionalOnClass(Feign.class)
@AutoConfigureBefore(FeignAutoConfiguration.class)
public class FeignConfig {

  /** 超时时间. */
  private static final int CONNECT_TIMEOUT_MILLIS = 7000;
  private static final int READ_TIMEOUT_MILLIS = 7000;

  @Bean
  public Request.Options options() {
    return new Request.Options(CONNECT_TIMEOUT_MILLIS, READ_TIMEOUT_MILLIS);
  }

  @Bean
  public Retryer feignRetryer() {
    return new Retryer.Default();
  }

  @Bean
  public okhttp3.OkHttpClient okHttpClient() {
    return new okhttp3.OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .connectionPool(new ConnectionPool())
        .build();
  }
}
