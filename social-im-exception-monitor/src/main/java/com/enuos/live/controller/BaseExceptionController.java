package com.enuos.live.controller;

import com.enuos.live.service.ExceptionMonitorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName BaseExceptionController
 * @Description: TODO
 * @Author xubin
 * @Date 2020/9/15
 * @Version V2.0
 **/
@Slf4j
@RestController
@RequestMapping("/exception")
public class BaseExceptionController {

    @Autowired
    private ExceptionMonitorHandler exceptionMonitorHandler;

    /**
     * @MethodName: saveException
     * @Description: TODO 保存异常信息
     * @Param: [url, code, describe, contnet]
     * @Return: void
     * @Author: xubin
     * @Date: 12:59 2020/9/15
     **/
    @PostMapping("/save")
    public void saveException(String url, String code, String describe, String contnet) {
        exceptionMonitorHandler.saveException(url, code, describe, contnet);
    }

    /**
     * @MethodName: monitorService
     * @Description: TODO 服务异常监控
     * @Param: []
     * @Return: void
     * @Author: xubin
     * @Date: 18:05 2020/9/15
     **/
    @PostMapping("/monitorService")
    public void monitorService(String ip, String serviceName) {
        log.info("服务ip=[{}], 服务名称=[{}]", ip, serviceName);
        exceptionMonitorHandler.monitorService(ip, serviceName);
    }
}
