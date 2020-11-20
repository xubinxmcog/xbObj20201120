package com.enuos.live.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName ExecutorConfig
 * @Description: TODO 线程池配置
 * @Author xubin
 * @Date 2020/4/21
 * @Version V1.0
 **/
@Slf4j
@EnableAsync
@Configuration
public class ExecutorConfig {

    @Bean("eAsyncServiceExecutor")
    public Executor asyncServiceExecutor() {
        log.info("开始加载asyncServiceExecutor...");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(10);
        ///配置最大线程数
        executor.setMaxPoolSize(100);
        // 队列大小
        executor.setQueueCapacity(200);

        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("async-service-");

        // 设置拒绝策略：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //执行初始化
        executor.initialize();
        return executor;

    }
}
