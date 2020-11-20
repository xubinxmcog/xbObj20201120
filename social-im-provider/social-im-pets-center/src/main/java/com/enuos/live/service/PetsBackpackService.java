package com.enuos.live.service;

import com.enuos.live.pojo.PetsProductBackpack;
import com.enuos.live.result.Result;

import java.util.Map;

public interface PetsBackpackService {

    Result upProductBackpack(PetsProductBackpack backpack, Integer usingPro);

    /**
     * @MethodName: queryBackpack
     * @Description: TODO 获取用户宠物背包所有物品
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 10:58 2020/9/2
    **/
    Result queryBackpack(Long userId, Integer categoryId);

    /**
     * @MethodName: dressUp
     * @Description: TODO 修改宠物装扮
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 13:45 2020/10/26
    **/
    Result dressUp(Map<String, Object> params);

    /**
     * @MethodName: getDressUp
     * @Description: TODO 获取宠物装扮
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:42 2020/10/27
    **/
    Result getDressUp(Map<String, Object> params);
}
