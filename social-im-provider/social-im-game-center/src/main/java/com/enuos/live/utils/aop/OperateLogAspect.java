package com.enuos.live.utils.aop;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import lombok.Data;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

/**
 * TODO 切面处理类.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/11/3 10:52
 */

@Aspect
@Component
public class OperateLogAspect {

  private static Logger logger = LoggerFactory.getLogger(OperateLogAspect.class);

  /**
   * TODO 操作日志切入点.
   * 设置操作日志切入点 记录操作日志 在注解的位置切入代码
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/3 10:58
   * @update 2020/11/3 10:58
   */
  @Pointcut("@annotation(com.enuos.live.utils.annotation.OperateLog)")
  public void operateLogPointCut() { }

  /**
   * TODO 异常日志切入点.
   * 设置操作异常切入点记录异常日志 扫描所有controller包下操作
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/3 11:03
   * @update 2020/11/3 11:03
   */
  @Pointcut("execution(* com.enuos.live.controller..*.*(..))")
  public void operateExceptionLogPointCut() { }

  /**
   * TODO 正常通知.
   *
   * @param joinPoint 切入点
   * @param keys 返回结果
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/3 11:13
   * @update 2020/11/3 11:13
   */
  @AfterReturning(value = "operateLogPointCut()", returning = "keys")
  public void startOperateLog(JoinPoint joinPoint, Object keys) {

  }

  /**
   * TODO 前置通知.
   *
   * @param proceedingJoinPoint [连接点]
   * @return [参数信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/3 13:20
   * @update 2020/11/3 13:20
   */
  @Around("operateExceptionLogPointCut()")
  public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes();
    Object result = proceedingJoinPoint.proceed();
    if (Objects.nonNull(attributes)) {
      HttpServletRequest request = attributes.getRequest();
      RequestInfo requestInfo = new RequestInfo();
      requestInfo.setIp(request.getRemoteAddr());
      requestInfo.setUrl(request.getRequestURL().toString());
      requestInfo.setHttpMethod(request.getMethod());
      requestInfo.setClassMethod(String.format("%s.%s", proceedingJoinPoint.getSignature()
          .getDeclaringTypeName(), proceedingJoinPoint.getSignature().getName()));
      requestInfo.setRequestParams(getRequestParamsByProceedingJoinPoint(proceedingJoinPoint));
      requestInfo.setTimeCost(System.currentTimeMillis() - start);
      logger.info("[REQUEST INFOS]: {}", JSON.toJSONString(requestInfo));
    }
    return result;
  }

  /**
   * TODO 异常通知.
   *
   * @param joinPoint [切入点]
   * @param e [异常信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/3 13:19
   * @update 2020/11/3 13:19
   */
  @AfterThrowing(pointcut = "operateExceptionLogPointCut()", throwing = "e")
  public void doAfterThrowing(JoinPoint joinPoint, RuntimeException e) {
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes();
    if (Objects.nonNull(attributes)) {
      HttpServletRequest request = attributes.getRequest();
      RequestErrorInfo requestErrorInfo = new RequestErrorInfo();
      requestErrorInfo.setIp(request.getRemoteAddr());
      requestErrorInfo.setUrl(request.getRequestURL().toString());
      requestErrorInfo.setHttpMethod(request.getMethod());
      requestErrorInfo.setClassMethod(String.format("%s.%s", joinPoint.getSignature()
          .getDeclaringTypeName(), joinPoint.getSignature().getName()));
      requestErrorInfo.setRequestParams(getRequestParamsByJoinPoint(joinPoint));
      requestErrorInfo.setException(e);
      logger.info("[ERROR REQUEST]:  {}", JSON.toJSONString(requestErrorInfo));
    }
  }

  /**
   * TODO 获得参数.
   *
   * @param proceedingJoinPoint [切入点]
   * @return [参数信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/3 13:01
   * @update 2020/11/3 13:01
   */
  private Map<String, Object> getRequestParamsByProceedingJoinPoint(ProceedingJoinPoint proceedingJoinPoint) {
    // 参数名
    String[] paramNames = ((MethodSignature) proceedingJoinPoint.getSignature()).getParameterNames();
    // 参数值
    Object[] paramValues = proceedingJoinPoint.getArgs();
    return buildRequestParam(paramNames, paramValues);
  }

  /**
   * TODO 获得参数.
   *
   * @param joinPoint [切入点]
   * @return [参数信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/3 13:07
   * @update 2020/11/3 13:07
   */
  private Map<String, Object> getRequestParamsByJoinPoint(JoinPoint joinPoint) {
    // 参数名
    String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
    // 参数值
    Object[] paramValues = joinPoint.getArgs();
    return buildRequestParam(paramNames, paramValues);
  }

  /**
   * TODO 建立参数.
   *
   * @param paramNames [参数名]
   * @param paramValues [参数值]
   * @return [参数信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/3 13:04
   * @update 2020/11/3 13:04
   */
  private Map<String, Object> buildRequestParam(String[] paramNames, Object[] paramValues) {
    Map<String, Object> requestParams = Maps.newHashMap();
    for (int i = 0; i < paramNames.length; i++) {
      if (!Objects.equals(paramNames[i], "request")) {
        Object value = paramValues[i];
        // 是否文件对象
        if (value instanceof MultipartFile) {
          MultipartFile file = (MultipartFile) value;
          // 获取文件名
          value = file.getOriginalFilename();
        }
        requestParams.put(paramNames[i], value);
      }
    }
    return requestParams;
  }

  @Data
  private class RequestInfo {
    private String ip;
    private String url;
    private String httpMethod;
    private String classMethod;
    private Object requestParams;
    private Long timeCost;
  }

  @Data
  private class RequestErrorInfo {
    private String ip;
    private String url;
    private String httpMethod;
    private String classMethod;
    private Object requestParams;
    private RuntimeException exception;
  }

}
