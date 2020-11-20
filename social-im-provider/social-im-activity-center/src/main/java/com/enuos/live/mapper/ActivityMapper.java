package com.enuos.live.mapper;

import com.enuos.live.pojo.Activity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description 活动中心
 * @Author wangyingjie
 * @Date 2020/8/12
 * @Modified
 */
public interface ActivityMapper {

    /** 
     * @Description: 活动列表
     * @Param: [] 
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>> 
     * @Author: wangyingjie
     * @Date: 2020/8/12 
     */ 
    List<Map<String, Object>> getList();

    /**
     * @Description: 活动主体
     * @Param: [code]
     * @Return: com.enuos.live.pojo.Activity
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    Activity getActivityByCode(String code);

    /**
     * @Description: 活动领奖记录
     * @Param: [userId, codeList]
     * @Return: java.util.List<java.lang.String>
     * @Author: wangyingjie
     * @Date: 2020/9/25
     */
    List<String> getRecordList(@Param("userId") Long userId, @Param("codeList") List<String> codeList);

    /**
     * @Description: 活动进度
     * @Param: [userId, codeList]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Integer>>
     * @Author: wangyingjie
     * @Date: 2020/9/25
     */
    List<Map<String, Integer>> getProgressList(@Param("userId") Long userId, @Param("codeList") List<String> codeList);

    /**
     * @Description: 获取奖励
     * @Param: [code]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/8/14
     */
    Map<String, Object> getRewardByCode(String code);
    
    /** 
     * @Description: 获取奖励
     * @Param: [code, suffix] 
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>> 
     * @Author: wangyingjie
     * @Date: 2020/9/27 
     */ 
    List<Map<String, Object>> getRewardListByCodeAndSuffix(@Param("code") String code, @Param("suffix") Integer suffix);

}
