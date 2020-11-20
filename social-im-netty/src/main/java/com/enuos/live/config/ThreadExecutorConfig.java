package com.enuos.live.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName ThreadExecutorConfig
 * @Description: TODO  线程池配置
 * @Author xubin
 * @Date 2020/7/31
 * @Version V2.0
 **/
@Slf4j
@Configuration
@EnableAsync//开启异步调用
public class ThreadExecutorConfig {

    /**
     * 核心线程数
     */
    private int corePoolSize = 10;
    /**
     * 最大线程数
     */
    private int maxPoolSize = 100;
    /**
     * 队列数
     */
    private int queueCapacity = 200;

    /**
     * @return
     * @Configuration = <beans></beans>
     * @Bean = <bean></bean>
     * 返回值类型为<bean></bean>中的属性"class"对应的value
     * 方法名为<bean></bean>中的属性"id"对应的value
     */
    @Bean
    public ExecutorService taskFxbDrawExecutor() {
        log.info("开始加载 taskFxbDrawExecutor... ");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("test-fxb-draw-service-");

        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 执行初始化
        executor.initialize();
        return executor.getThreadPoolExecutor();
    }
}
