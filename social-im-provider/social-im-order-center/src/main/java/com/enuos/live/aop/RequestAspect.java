package com.enuos.live.aop;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.enuos.live.annotations.NoRepeatSubmit;
import com.enuos.live.result.Result;
import com.enuos.live.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @ClassName RequestAspect
 * @Description: TODO 切面编程
 * @Author xubin
 * @Date 2020/7/10
 * @Version V2.0
 **/
@Aspect
@Component
@Slf4j
public class RequestAspect {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private RedisUtils redisUtils;

    @Pointcut("@annotation(noRepeatSubmit)")
    public void pointCut(NoRepeatSubmit noRepeatSubmit) {
    }

    @Around("pointCut(noRepeatSubmit)")
    public Object around(ProceedingJoinPoint point, NoRepeatSubmit noRepeatSubmit) throws Throwable {
        int lockSeconds = noRepeatSubmit.lockTime();

        String userId = request.getHeader("userId");
        if (StrUtil.isEmpty(userId)) {
            userId = getParmsUserId(point);
        }
        String path = request.getServletPath();
        String key = getKey(userId, path);
        log.info("不重复提交请求接口=[{}], userId=[{}]", path, userId);

        if (redisUtils.hasKey(key)) {
            return Result.error(200, "重复请求, 请稍后再试");
        }

        String clientId = IdUtil.simpleUUID();
        redisUtils.set(key, clientId, lockSeconds);
        Object result = point.proceed();
        return result;

    }

    private String getKey(String token, String path) {
        return "KEY_NO_REPEAT_SUBMIT:" + token + "_" + path;
    }


    private String getParmsUserId(ProceedingJoinPoint point) {
        String userId = "";
        //参数值
        Object[] args = point.getArgs();
        JSONArray jsonArray = (JSONArray) JSONObject.toJSON(args);
        for (Object o : jsonArray) {
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(o);
            userId = jsonObject.getString("userId");
        }
        return userId;
    }

    /**
     * 描述: TODO 获取用户IP
     * 参数: [request]
     *
     * @Return: java.lang.String
     * 创建人: xubin
     * 时间: 2019/12/19
     **/
    public String getIpAddr(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (ipAddress.equals("127.0.0.1")) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    ipAddress = inet.getHostAddress();
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) {
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "";
        }
        return ipAddress;
    }

}
