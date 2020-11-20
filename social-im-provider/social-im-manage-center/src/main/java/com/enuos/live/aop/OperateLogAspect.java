package com.enuos.live.aop;

import com.alibaba.fastjson.JSON;
import com.enuos.live.annotations.OperateLog;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @ClassName OperateLogAspect
 * @Description: TODO 注解织入点
 * @Author xubin
 * @Date 2020/4/16
 * @Version V1.0
 **/
@Slf4j
@Aspect
@Component()
public class OperateLogAspect {

    @Autowired
    private HttpServletRequest request;

    private final String accessTokenKey = "accessToken";

    @Pointcut("@annotation(com.enuos.live.annotations.OperateLog)")
    public void logPointCut() {
    }

    /**
     * 前置通知 用于拦截操作，在方法返回后执行
     *
     * @param point 切点
     */
    @Before("logPointCut()")
    public void doBefore(JoinPoint point) {

        MethodSignature signature = (MethodSignature) point.getSignature();

        Method method = signature.getMethod();

        OperateLog operateLog = method.getAnnotation(OperateLog.class);
        //获取url
        StringBuffer url = request.getRequestURL();

        // 请求类型
        String requestMethod = request.getMethod();

        //获取用户ip
        String userIp;

        // 获取用户信息
        String accessToken = request.getHeader(accessTokenKey);

        // 操作信息
        String operateMsg = operateLog.operateMsg();

        // 操作类型
        String logType = operateLog.logType();

        //获取参数名称
        String[] params = signature.getParameterNames();

        //参数值
        Object[] args = point.getArgs();
        //参数拼接成Map
        Map<String, Object> map = Maps.newHashMap();
        for (int i = 0; i < params.length; i++) {
            if (args == null) {
                map.put(params[i], "");
            } else {
                try {
                    map.put(params[i], JSON.toJSONString(args[i]));
                } catch (Exception e) {
                    log.info("参数" + params[i] + "序列化错误");
                }
            }
        }

        // 线程保存操作日志
        new Thread(()->{

        }).start();


    }

}
