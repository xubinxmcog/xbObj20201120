package com.enuos.live.service;

import com.enuos.live.containers.ServiceContainer;
import com.enuos.live.mapper.ExceptionInfoMapper;
import com.enuos.live.pojo.ExceptionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName ExceptionMonitorAdvice
 * @Description: TODO 异常处理
 * @Author xubin
 * @Date 2020/9/14
 * @Version V2.0
 **/
@Slf4j
@Service
public class ExceptionMonitorHandler {

    @Autowired
    private ExceptionInfoMapper exceptionInfoMapper;

    public void saveException(String url, String code, String describe, String contnet) {
        if (null == contnet) {
            contnet = "null";
        }
        contnet.replaceAll("\n", "");
        contnet.replaceAll("\t", "");
        contnet.replaceAll("\r", "");
        log.info("开始保存异常信息, 异常接口=[{}],错误码=[{}], 描述=[{}], 异常信息=[{}]", url, code, describe, contnet);
        ExceptionInfo record = new ExceptionInfo();
        record.setUrl(url);
        record.setExceptionCode(code);
        record.setDescribe(describe);
        record.setContnet(contnet);
        exceptionInfoMapper.insert(record);
    }


    /**
     * @MethodName: monitorService
     * @Description: TODO 接收服务异常监控通知
     * @Param: [ip服务地址, serviceName服务名称]
     * @Return: void
     * @Author: xubin
     * @Date: 15:27 2020/9/17
     **/
    public void monitorService(String ip, String serviceName) {
        long overTime = System.currentTimeMillis() + (10 * 60 * 1000); // 当前系统时间加10分钟
        ServiceContainer.serviceMap.put(serviceName + "_" + ip, overTime);
    }

}
