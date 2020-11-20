package com.enuos.live.service;

import com.enuos.live.task.Task;

/**
 * @Description 处理器
 * @Author wangyingjie
 * @Date 2020/10/28
 * @Modified
 */
public interface HandlerService {
    
    /** 
     * @Description: 日常
     * @Param: [task]
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/10/28 
     */ 
    void dailyTask(Task task);
}
