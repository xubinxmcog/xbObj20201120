package com.enuos.live.service;

import com.enuos.live.pojo.TaskSign;
import com.enuos.live.result.Result;

/**
 * @Description 签到任务
 * @Author wangyingjie
 * @Date 9:33 2020/4/9
 * @Modified
 */
public interface TaskSignService {
    
    /** 
     * @Description: 签到详情
     * @Param: [taskSign]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/5/21 
     */ 
    Result detail(TaskSign taskSign);

    /**
     * @Description: 每日签到/补签
     * @Param: [taskSign]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    Result daySign(TaskSign taskSign);
    
    /** 
     * @Description: 去补签 
     * @Param: [taskSign]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/8 
     */ 
    Result toBackSign(TaskSign taskSign);
    
    /** 
     * @Description: 领取奖励
     * @Param: [taskSign]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/8 
     */ 
    Result toGet(TaskSign taskSign);

}
