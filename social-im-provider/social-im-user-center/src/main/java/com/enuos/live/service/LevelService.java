package com.enuos.live.service;

import com.enuos.live.pojo.Level;
import com.enuos.live.result.Result;

/**
 * @Description 等级中心
 * @Author wangyingjie
 * @Date 2020/7/21
 * @Modified
 */
public interface LevelService {

    /** 
     * @Description: 等级条
     * @Param: [level]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/7/21 
     */ 
    Result bar(Level level);
    
    /** 
     * @Description: 等级奖励
     * @Param: [level]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/7/21 
     */ 
    Result reward(Level level);

    /** 
     * @Description: 领奖
     * @Param: [level]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/7/21 
     */ 
    Result toGet(Level level);

}
