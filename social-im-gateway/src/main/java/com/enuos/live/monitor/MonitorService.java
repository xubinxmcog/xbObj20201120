package com.enuos.live.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * @ClassName MonitorJob
 * @Description: TODO 监测任务
 * @Author xubin
 * @Date 2020/9/15
 * @Version V2.0
 **/
@Slf4j
@Component
public class MonitorService {

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private MonitorFeign monitorFeign;

    /**
     * @MethodName: codeException
     * @Description: TODO 服务异常监控
     * @Param: []
     * @Return: void
     * @Author: xubin
     * @Date: 16:29 2020/9/15
     **/
    @Async
    @Scheduled(cron = "0 0/3 * * * ?") // 10分钟一次
    public void monitorException() {
        try {
            monitorFeign.monitorService(InetAddress.getLocalHost().getHostAddress(), applicationName);
        } catch (Exception e) {
            log.error("心跳发送失败");
        }

    }

}
