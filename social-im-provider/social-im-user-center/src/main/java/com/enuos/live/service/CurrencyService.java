package com.enuos.live.service;

import com.enuos.live.pojo.Currency;
import com.enuos.live.result.Result;

/**
 * @Description 货币相关业务接口
 * @Author wangyingjie
 * @Date 2020/5/19
 * @Modified
 */
public interface CurrencyService {

    /**
     * @Description: 计算金币
     * @Param: [userId, gold]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/9
     */
    Result countGold(Long userId, Long gold);

    /**
     * @Description: 更新用户钻石或金币
     * @Param: [currency]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Result upUserCurrency(Currency currency);

    /** 
     * @Description: 计算钻石 
     * @Param: [userId, diamond] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/12 
     */ 
    Result countDiamond(Long userId, Long diamond);

    /** 
     * @Description: 计算扭蛋
     * @Param: [userId, gashapon] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/22 
     */ 
    Result countGashapon(Long userId, Integer gashapon);

}
