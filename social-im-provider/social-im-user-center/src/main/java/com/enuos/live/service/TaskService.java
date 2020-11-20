package com.enuos.live.service;

import com.enuos.live.pojo.Task;
import com.enuos.live.result.Result;

/**
 * @Description 任务中心
 * @Author wangyingjie
 * @Date 2020/6/10
 * @Modified
 */
public interface TaskService {
    
    /** 
     * @Description: 活跃详情
     * @Param: [userId] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/10 
     */ 
    Result active(Long userId);

    /** 
     * @Description: 任务列表 
     * @Param: [task]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/11 
     */ 
    Result list(Task task);
    
    /** 
     * @Description: 活跃奖励 
     * @Param: [task]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/15 
     */ 
    Result activeReward(Task task);
    
    /** 
     * @Description: 领奖
     * @Param: [task]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/12 
     */
    Result toGet(Task task);

}
