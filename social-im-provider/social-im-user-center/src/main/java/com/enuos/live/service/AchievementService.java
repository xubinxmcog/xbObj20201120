package com.enuos.live.service;

import com.enuos.live.pojo.Task;
import com.enuos.live.result.Result;

import java.util.Map;

/**
 * @Description 成就管理
 * @Author wangyingjie
 * @Date 2020/6/16
 * @Modified
 */
public interface AchievementService {

    /**
     * @Description: 达成的成就数
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/17
     */
    Result num(Task task);

    /** 
     * @Description: 成就列表
     * @Param: [task]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/16 
     */ 
    Result list(Task task);
    
    /** 
     * @Description: 获取成就奖励
     * @Param: [task]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/17 
     */ 
    Result toGet(Task task);

    /**
     * @Description: 成就进度统一处理
     * @Param: [params]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/13
     */
    void handler(Map<String, Object> params);

    /**
     * @Description: 成就进度统一处理
     * @Param: [params]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/13
     */
    void handlers(Map<String, Object> params);

}
