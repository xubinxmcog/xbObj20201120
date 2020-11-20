package com.enuos.live.service;

import com.enuos.live.result.Result;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/12
 * @Modified
 */
public interface RewardService {

    /**
     * @Description: 奖励处理
     * @Param: [userId, list]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/12
     */
    Result handler(Long userId, List<Map<String, Object>> list);

}
