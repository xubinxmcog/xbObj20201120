package com.enuos.live.service;

import com.enuos.live.pojo.Tip;
import com.enuos.live.result.Result;

/**
 * @Description 红点提示
 * @Author wangyingjie
 * @Date 2020/8/18
 * @Modified
 */
public interface TipService {
    
    /** 
     * @Description: 是否提示
     * @Param: [tip] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/8/18 
     */ 
    Result isTip(Tip tip);
}
