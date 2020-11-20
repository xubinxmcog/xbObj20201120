package com.enuos.live.config;

import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author WangCaiWen
 * Created on 2020/3/16 10:50
 */
@Configuration
public class FeignConfig {

    /** 超时时间 */
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
}
