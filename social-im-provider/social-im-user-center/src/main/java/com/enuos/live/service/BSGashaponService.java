package com.enuos.live.service;

import com.enuos.live.pojo.BSGashapon;
import com.enuos.live.result.Result;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/28
 * @Modified
 */
public interface BSGashaponService {
    
    /** 
     * @Description: 保存
     * @Param: [bsGashapon] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/28 
     */ 
    Result save(BSGashapon bsGashapon);
}
