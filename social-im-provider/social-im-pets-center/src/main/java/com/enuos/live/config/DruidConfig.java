package com.enuos.live.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * TODO 连接池配置.
 *
 * @author WangCaiWen
 * @version 1.0
 * @since 2020/3/17 14:18
 */
@Configuration
public class DruidConfig {

  @Bean
  @ConfigurationProperties(prefix = "spring.datasource")
  public DataSource druidDataSource() {
    return new DruidDataSource();
  }
}
