package com.enuos.live.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.enuos.live.exception.CustomBlockHandler;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @ClassName CustomBlockHandlerConfig
 * @Description: TODO
 * @Author xubin
 * @Date 2020/8/21
 * @Version V2.0
 **/
@Configuration
public class CustomBlockHandlerConfig {
    /**
     * @MethodName: init
     * @Description: TODO 初始化Sentinel 自定义统一异常处理
     * @Param: []
     * @Return: void
     * @Author: xubin
     * @Date: 10:46 2020/8/21
    **/
    @PostConstruct
    public void init() {
        WebCallbackManager.setUrlBlockHandler(new CustomBlockHandler());
    }
}
