package com.enuos.live.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description 日志
 * @Author wangyingjie
 * @Date 2020/6/4
 * @Modified
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    @Autowired
    private HttpServletRequest request;

    @Pointcut("execution(public * com.enuos.live.controller..*.*(..))")
    public void log(){}
    
    /** 
     * @Description: 入参日志 
     * @Param: [joinPoint] 
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/6/4 
     */ 
    @Before("log()")
    public void doBefore(JoinPoint joinPoint) {
        String url = request.getRequestURI();
        String method = request.getMethod();
        log.debug("request:[uri={},method={},params={}]", url, method, joinPoint.getArgs());
    }
    
    /** 
     * @Description: 反参日志
     * @Param: [joinPoint] 
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/6/4 
     */ 
    @AfterReturning(pointcut = "log()",returning = "result")
    public void doAfterReturning(Object result) {
        log.debug("response:[response={}]", result == null ? "" : result.toString());
    }
}
