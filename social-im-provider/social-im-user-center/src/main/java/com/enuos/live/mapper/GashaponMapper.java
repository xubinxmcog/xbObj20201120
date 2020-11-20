package com.enuos.live.mapper;

import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/19
 * @Modified
 */
public interface GashaponMapper {

    /**
     * @Description: 扭蛋数
     * @Param: [userId]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/6/22
     */
    Map<String, Object> getNum(Long userId);

    /**
     * @Description: 列表
     * @Param: [userId, prefix, time]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/6/22
     */
    List<Map<String, Object>> getLotteryList(@Param("userId") Long userId, @Param("prefix") String prefix, @Param("time") Integer time);

    /** 
     * @Description: 获取扭蛋活动相关设置信息
     * @Param: [userId, code, suffix]
     * @Return: java.util.Map<java.lang.String,java.lang.Object> 
     * @Author: wangyingjie
     * @Date: 2020/6/22 
     */ 
    Map<String, Object> getSettingsByCode(@Param("userId") Long userId, @Param("code") String code, @Param("suffix") Integer suffix);

    /** 
     * @Description: 是否参加
     * @Param: [userId, code] 
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/6/22 
     */ 
    Map<String, Object> isJoin(@Param("userId") Long userId, @Param("code") String code);

    /** 
     * @Description: 保存记录
     * @Param: [userId, code, joinCount] 
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/6/22 
     */ 
    int saveGashaponRecord(@Param("userId") Long userId, @Param("code") String code, @Param("joinCount") Integer joinCount, @Param("result") Integer result, @Param("createTime") LocalDateTime createTime);
    
    /** 
     * @Description: 更新记录
     * @Param: [userId, code, joinCount] 
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/6/22 
     */ 
    int updateGashaponRecord(@Param("userId") Long userId, @Param("code") String code, @Param("joinCount") Integer joinCount);

    /**
     * @Description: 获取开奖结果
     * @Param: [userId, code]
     * @Return: Integer
     * @Author: wangyingjie
     * @Date: 2020/6/24
     */
    Integer getResult(@Param("userId") Long userId, @Param("code") String code);
    
    /** 
     * @Description: 兑换列表
     * @Param: [userId, prefix]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>> 
     * @Author: wangyingjie
     * @Date: 2020/6/24 
     */ 
    List<Map<String, Object>> getExchangeList(@Param("userId") Long userId, @Param("prefix") String prefix);

    /**
     * @Description: 获取参与次数上限
     * @Param: [code]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/6/24
     */
    Map<String, Object> getSettingsForExchange(String code);

    /**
     * @Description: 奖励
     * @Param: [code]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/6/24
     */
    List<Map<String, Object>> getReward(String code);

    /**
     * @Description: 中奖记录
     * @Param: [userId]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/6/24
     */
    List<Map<String, Object>> lotteryRecordPage(Long userId);

    /**
     * @Description: 兑换记录
     * @Param: [userId]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/6/24
     */
    List<Map<String, Object>> exchangeRecordPage(Long userId);
}
