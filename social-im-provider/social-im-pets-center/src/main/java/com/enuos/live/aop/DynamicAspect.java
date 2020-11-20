package com.enuos.live.aop;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.enuos.live.annotations.UserDynamic;
import com.enuos.live.mapper.PetsUserDynamicMsgMapper;
import com.enuos.live.pojo.PetsUserDynamicMsg;
import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName DynamicAspect
 * @Description: TODO 动态处理
 * @Author xubin
 * @Date 2020/10/15
 * @Version V2.0
 **/
@Slf4j
@Aspect
@Component
public class DynamicAspect {

    @Autowired
    private PetsUserDynamicMsgMapper dynamicMsgMapper;

    @Resource(name = "eAsyncServiceExecutor")
    private ThreadPoolTaskExecutor executor;

    @Pointcut("@annotation(userDynamic)")
    public void pointCut(UserDynamic userDynamic) {
    }

    @AfterReturning(value = "pointCut(userDynamic)", returning = "keys")
    public void around(JoinPoint point, UserDynamic userDynamic, Object keys) throws Throwable {

        Result result = (Result) keys;
        System.out.println(result);
        Integer code = result.getCode();

        if (0 == code) {
            String describe = userDynamic.describe(); // 描述
            System.out.println(describe);
            int dynamicType = userDynamic.dynamicType(); // 动态类型

            List<Object> args = Arrays.asList(point.getArgs());

            // 获取方法
            String name = point.getSignature().getName();

            System.out.println("入参数为:" + args);

            String paramStr = JSON.toJSONString(args.get(0));
            System.out.println(paramStr);
            JSONObject params = JSONObject.parseObject(paramStr);
            Long userId = params.getLong("userId");
            Long targetUserId = params.getLong("targetUserId");
            String petCode = params.getString("petCode"); // 宠物code

            String petsNick = "";
            Integer intNum = 0;

            if (ObjectUtil.isNotEmpty(result.getData())) {
                String resultStr = JSON.toJSONString(result.getData());
                JSONObject resultJson = JSONObject.parseObject(resultStr);
                petsNick = resultJson.getString("petsNick");
                intNum = resultJson.getInteger("intNum");
            }

            StringBuffer message = new StringBuffer();
            StringBuffer msgType = new StringBuffer();
            switch (dynamicType) {
                case 1: //喂食
                    message.append(describe).append(petsNick).append("喂食");
                    msgType.append("饱食度+").append(intNum);
                    break;
                case 2: // 互动
                    message.append(describe).append(petsNick).append("互动");
                    msgType.append("心情+").append(intNum);
                    break;
                case 3: // 打招呼
                    message.append(describe);
                    break;
                case 4: // 其他

                    break;
                default:
                    log.info("暂不支持的动态类型...");
            }
            PetsUserDynamicMsg dynamicMsg = new PetsUserDynamicMsg();

            dynamicMsg.setUserId(targetUserId);
            dynamicMsg.setGfUserId(userId);
            dynamicMsg.setMessage(message.toString());
            dynamicMsg.setMsgType(msgType.toString());
            dynamicMsg.setIsRead(0);

            executor.submit(() -> dynamicMsgMapper.insert(dynamicMsg));
        }
    }
}
