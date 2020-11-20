package com.enuos.live.service;

import com.enuos.live.result.Result;

/**
 * @Description 货币加减
 * @Author wangyingjie
 * @Date 2020/10/13
 * @Modified
 */
public interface CurrencyService {

    /** 
     * @Description: 计算钻石
     * @Param: [userId, diamond, productName]
     * @Return: java.lang.Integer 
     * @Author: wangyingjie
     * @Date: 2020/10/13 
     */ 
    Result countDiamond(Long userId, Long diamond, String productName);

    /**
     * @Description: 计算金币
     * @Param: [userId, diamond, productName]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/10/13
     */
    Result countGold(Long userId, Long gold, String productName);
    
}
