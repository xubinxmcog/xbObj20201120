package com.enuos.live.mapper;

import com.enuos.live.pojo.Currency;
import org.apache.ibatis.annotations.Param;

/**
 * @Description 货币
 * @Author wangyingjie
 * @Date 2020/5/19
 * @Modified
 */
public interface CurrencyMapper {

    /**
     * @Description: 获取金币
     * @Param: [userId]
     * @Return: com.enuos.live.pojo.Currency
     * @Author: wangyingjie
     * @Date: 2020/5/20
     */
    Currency getCurrency(Long userId);

    /**
     * @Description: 更新金币
     * @Param: [userId, gold]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/5/20
     */
    int updateGold(@Param("userId") Long userId, @Param("gold") Long gold);

    /**
     * @Description: 更新钻石
     * @Param: [userId, diamond]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/6/12
     */
    int updateDiamond(@Param("userId") Long userId, @Param("diamond") Long diamond);

    /**
     * @MethodName: upUserCurrency
     * @Description: TODO 更新用户钻石或金币或扭蛋
     * @Param: [userId, diamond]
     * @Return: int
     * @Author: xubin
     * @Date: 16:54 2020/9/1
    **/
    int upUserCurrency(Currency currency);

    /** 
     * @Description: 更修扭蛋 
     * @Param: [userId, gashapon] 
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/6/22 
     */ 
    int updateGashapon(@Param("userId") Long userId, @Param("gashapon") Integer gashapon);
}
