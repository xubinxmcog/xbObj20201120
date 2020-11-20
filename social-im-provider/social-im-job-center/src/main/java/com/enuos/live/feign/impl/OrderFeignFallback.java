package com.enuos.live.feign.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.OrderFeign;
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
public class OrderFeignFallback implements OrderFeign {

    /**
     * @Description: 入用户背包
     * @Param: [param]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/15
     */
    @Override
    public Result addBackpack(Map<String, Object> params) {
        log.error("==========[SOCIAL-IM-JOB OrderFeign addBackpack error, params={}]", params);
        return Result.error(ErrorCode.NETWORK_ERROR);
    }
}
