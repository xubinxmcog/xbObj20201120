package com.enuos.live.service;

import com.enuos.live.result.Result;

import java.util.Map;

public interface PetsService {

    /**
     * @MethodName: getInfo
     * @Description: TODO 获取宠物信息
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 14:55 2020/8/24
     **/
    Result getInfo(Long userId);

    /**
     * @MethodName: getOperation
     * @Description: TODO 操作
     * @Param: [userId, operation]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:46 2020/8/26
    **/
    Result getOperation(Long userId, Integer operation);

    /**
     * @MethodName: foodOrToys
     * @Description: TODO 喂食或玩具
     * @Param: [userId,petCode宠物ID, id物品编码, operation操作类型 1:喂食 2:玩具]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 14:35 2020/8/28
    **/
    Result foodOrToys(Long userId, Long targetUserId, String petCode, String id, Integer operation);

    Object getPetsInfoAndDressUp(Map<String, Object> params);
}
