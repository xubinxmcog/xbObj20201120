package com.enuos.live.service;

import com.enuos.live.pojo.JinQiu;
import com.enuos.live.result.Result;

import java.util.Map;

/**
 * @Description 金秋送福[ACT0005]
 * @Author wangyingjie
 * @Date 2020/9/23
 * @Modified
 */
public interface JinQiuService {
    
    /** 
     * @Description: 详情 
     * @Param: [jinQiu] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/9/24 
     */ 
    Result detail(JinQiu jinQiu);

    /** 
     * @Description: 领奖
     * @Param: [params]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/9/25 
     */ 
    Result toGet(Map<String, Object> params);
}
