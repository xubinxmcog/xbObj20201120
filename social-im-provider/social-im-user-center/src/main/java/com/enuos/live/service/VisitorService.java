package com.enuos.live.service;

import com.enuos.live.result.Result;

import java.util.Map;

/**
 * @Description 访客业务
 * @Author wangyingjie
 * @Date 2020/7/14
 * @Modified
 */
public interface VisitorService {

    /** 
     * @Description: 保存访客记录 
     * @Param: [params] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/7/15 
     */ 
    Result save(Map<String, Object> params);
    
    /**
     * @Description: vip专享访客记录
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/14
     */
    Result list(Map<String, Object> params);

}
