package com.enuos.live.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * @Description 货币加减
 * @Author wangyingjie
 * @Date 2020/10/13
 * @Modified
 */
public interface CurrencyMapper {
    
    /** 
     * @Description: 获取钻石 
     * @Param: [userId] 
     * @Return: java.lang.Long 
     * @Author: wangyingjie
     * @Date: 2020/10/13 
     */ 
    Long getDiamond(Long userId);
    
    /**
     * @Description: 更新钻石
     * @Param: [userId, diamond]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/10/13
     */
    int updateDiamond(@Param("userId") Long userId, @Param("diamond") Long diamond);

    /** 
     * @Description: 获取金币 
     * @Param: [userId] 
     * @Return: java.lang.Long 
     * @Author: wangyingjie
     * @Date: 2020/10/28 
     */ 
    Long getGold(Long userId);

    /** 
     * @Description: 更新金币
     * @Param: [userId, gold] 
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/10/28 
     */ 
    int updateGold(@Param("userId") Long userId, @Param("gold") Long gold);

}
