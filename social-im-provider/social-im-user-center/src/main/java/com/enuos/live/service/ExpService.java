package com.enuos.live.service;

import com.enuos.live.pojo.AccountAttach;
import com.enuos.live.result.Result;

import java.util.Map;

/**
 * @Description 等级
 * @Author wangyingjie
 * @Date 2020/5/18
 * @Modified
 */
public interface ExpService {

    /**
     * @Description: 等级经验
     * @Param: [accountAttach]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/5/21
     */
    Result gameHandler(AccountAttach accountAttach);

    /** 
     * @Description: 游戏今日已得经验
     * @Param: [accountAttach]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/5/21 
     */ 
    Result gameToday(AccountAttach accountAttach);
    
    /** 
     * @Description: 计算经验
     * @Param: [userId, exp] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/12 
     */ 
    Result countExp(Long userId, Long experience);
    
    /** 
     * @Description: 计算经验
     * @Param: [level, experience, addExperience]
     * @Return: java.util.Map<java.lang.String,java.lang.Object> 
     * @Author: wangyingjie
     * @Date: 2020/9/28 
     */ 
    Map<String, Object> level(Integer level, Long experience, Long addExperience);
}
