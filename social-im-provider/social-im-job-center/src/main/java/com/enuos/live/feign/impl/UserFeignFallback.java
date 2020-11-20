package com.enuos.live.feign.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.UserFeign;
import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/9/30
 * @Modified
 */
@Slf4j
@Component
public class UserFeignFallback implements UserFeign {

    /**
     * @Description: 奖励
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/15
     */
    @Override
    public Result rewardHandler(Map<String, Object> params) {
        log.error("==========[SOCIAL-IM-JOB UserFeign rewardHandler error, params={}]", params);
        return Result.error(ErrorCode.NETWORK_ERROR);
    }

    /**
     * @Description: 成就进度统一处理
     * @Param: [params]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/13
     */
    @Override
    public void achievementHandlers(Map<String, Object> params) {
        log.error("==========[SOCIAL-IM-JOB UserFeign achievementHandlers error, params={}]", params);
    }

    /**
     * @Description: 处理会员装饰
     * @Param: [userId, vip]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/31
     */
    @Override
    public void decorationHandler(Long userId, Integer vip) {
        log.error("==========[SOCIAL-IM-JOB UserFeign decorationHandler error, params=[userId={}, vip={}]]", userId, vip);
    }
}
