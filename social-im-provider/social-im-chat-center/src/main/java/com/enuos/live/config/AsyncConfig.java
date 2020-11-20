package com.enuos.live.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Do not modify ！！！
 *
 * @author WangCaiWen Created on 2020/4/29 15:44
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig {

  @Bean
  public Executor asyncServiceExecutor() {
    log.info("ChatAsyncConfig--asyncServiceExecutor: start asyncServiceExecutor");
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    //核心线程数
    executor.setCorePoolSize(5);
    //最大线程数
    executor.setMaxPoolSize(20);
    //缓冲队列大小
    executor.setQueueCapacity(50);
    //set thread name prefix
    executor.setThreadNamePrefix("chat-async-service-");
    // rejection-policy：当pool已经达到max size的时候，如何处理新任务
    // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    //执行初始化
    executor.initialize();
    return executor;
  }

}
