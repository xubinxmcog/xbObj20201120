package com.enuos.live.mapper;

import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @Description 红点提示
 * @Author wangyingjie
 * @Date 2020/8/18
 * @Modified
 */
public interface TipMapper {
    
    /** 
     * @Description: 是否签到
     * @Param: [userId, signTime] 
     * @Return: java.lang.Integer 
     * @Author: wangyingjie
     * @Date: 2020/8/18 
     */ 
    Integer isSign(@Param("userId") Long userId, @Param("signTime") LocalDate signTime);

    /**
     * @Description: 获取阈值
     * @Param: [code]
     * @Return: java.util.List<java.lang.Integer>
     * @Author: wangyingjie
     * @Date: 2020/8/18
     */
    List<Integer> getSuffix(String code);

    /** 
     * @Description: 累计签到次数
     * @Param: [userId, code] 
     * @Return: java.lang.Integer 
     * @Author: wangyingjie
     * @Date: 2020/8/18 
     */ 
    Integer getSignCount(@Param("userId") Long userId, @Param("code") String code);

    /**
     * @Description: 是否已经领奖
     * @Param: [userId, codeList]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/8/18
     */
    Integer isGotReward(@Param("userId") Long userId, @Param("codeList") List<String> codeList);

    /**
     * @Description: 获取每日活跃
     * @Param: [userId, createTime]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/8/18
     */
    Integer getActiveOfDay(@Param("userId") Long userId, @Param("createTime") LocalDate createTime);

    /**
     * @Description: 获取周活跃
     * @Param: [userId, begin, end]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/8/18
     */
    Integer getActiveOfWeek(@Param("userId") Long userId,  @Param("begin") LocalDate begin, @Param("end") LocalDate end);
    
    /** 
     * @Description: 获取进度
     * @Param: [userId, codeList] 
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>> 
     * @Author: wangyingjie
     * @Date: 2020/8/19 
     */ 
    List<Map<String, Object>> getTaskFollow(@Param("userId") Long userId, @Param("codeList") List<String> codeList);

    /**
     * @Description: 获取记录
     * @Param: [userId, codeList]
     * @Return: java.util.List<java.lang.String>
     * @Author: wangyingjie
     * @Date: 2020/8/28
     */
    List<String> getTaskRecord(@Param("userId") Long userId, @Param("codeList") List<String> codeList);

    /**
     * @Description: 获取code
     * @Param: [category]
     * @Return: java.util.List<java.lang.String>
     * @Author: wangyingjie
     * @Date: 2020/8/19
     */
    List<String> getTaskCode(Integer category);

    /**
     * @Description: 获取当前用户等级
     * @Param: [userId]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/8/19
     */
    Integer getLevel(Long userId);
}
