package com.enuos.live.feign.impl;

import com.enuos.live.feign.UserFeign;
import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/9/22
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
        log.error("==========[SOCIAL-IM-ACTIVITY UserFeign rewardHandler error, params={}]", params);
        return Result.error();
    }

    /**
     * @Description: 是否会员
     * @Param: [userId]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    @Override
    public Integer isMember(Long userId) {
        log.error("==========[SOCIAL-IM-ACTIVITY UserFeign isMember error, userId={}]", userId);
        return null;
    }

    /**
     * @Description: 计算金币
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    @Override
    public Result countGold(Map<String, Long> params) {
        log.error("==========[SOCIAL-IM-ACTIVITY UserFeign countGold error, params={}]", params);
        return null;
    }

    /**
     * @Description: 计算钻石
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    @Override
    public Result countDiamond(Map<String, Long> params) {
        log.error("==========[SOCIAL-IM-ACTIVITY UserFeign countDiamond error, params={}]", params);
        return null;
    }
}
