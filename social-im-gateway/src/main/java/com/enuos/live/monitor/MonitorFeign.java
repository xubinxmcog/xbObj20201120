package com.enuos.live.monitor;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "EXCEPTION-MONITOR")
public interface MonitorFeign {

    /**
     * @MethodName: monitorService
     * @Description: TODO 服务异常监控
     * @Param: [ip, serviceName]
     * @Return: void
     * @Author: xubin
     * @Date: 9:20 2020/9/16
     **/
    @PostMapping("/exception/monitorService")
    void monitorService(@RequestParam("ip") String ip, @RequestParam("serviceName") String serviceName);
}
