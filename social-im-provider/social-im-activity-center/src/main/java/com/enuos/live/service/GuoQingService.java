package com.enuos.live.service;

import com.enuos.live.pojo.GuoQing;
import com.enuos.live.result.Result;

/**
 * @Description 欢度国庆[ACT0002]
 * @Author wangyingjie
 * @Date 2020/9/17
 * @Modified
 */
public interface GuoQingService {
    
    /** 
     * @Description: 详情
     * @Param: [guoQing] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/9/17 
     */ 
    Result detail(GuoQing guoQing);
}
