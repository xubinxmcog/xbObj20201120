package com.enuos.live.aop;

import cn.hutool.core.util.IdUtil;
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
        String path = request.getServletPath();
        String key = getKey(userId, path);

        if (redisUtils.hasKey(key)) {
            return Result.error(200, "点的太快了, 等会再点吧");
        }

        String clientId = IdUtil.simpleUUID();
        redisUtils.set(key, clientId, lockSeconds);
        Object result = point.proceed();
        return result;

    }

    private String getKey(String token, String path) {
        return "KEY_NO_REPEAT_SUBMIT:" + token + "_" + path;
    }

}
