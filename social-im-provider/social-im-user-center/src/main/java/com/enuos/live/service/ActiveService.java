package com.enuos.live.service;

import com.enuos.live.result.Result;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/12
 * @Modified
 */
public interface ActiveService {
    
    /** 
     * @Description: 计算活跃度
     * @Param: [userId, active] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/12 
     */ 
    Result countActive(Long userId, Integer active);
}
