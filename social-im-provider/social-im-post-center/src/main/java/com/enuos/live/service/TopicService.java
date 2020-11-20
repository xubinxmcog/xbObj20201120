package com.enuos.live.service;

import com.enuos.live.pojo.Topic;
import com.enuos.live.result.Result;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/15
 * @Modified
 */
public interface TopicService {

    /**
     * @Description: 话题列表
     * @Param: [topic]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/15
     */
    Result listWithPage(Topic topic);
    
    /** 
     * @Description: 话题列表
     * @Param: [topic]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/16 
     */ 
    Result list(Topic topic);

    /** 
     * @Description: 话题信息
     * @Param: [topic]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/16 
     */ 
    Result info(Topic topic);
}
