package com.enuos.live.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description 奖励
 * @Author wangyingjie
 * @Date 2020/6/3
 * @Modified
 */
public interface RewardMapper {

    /**
     * @Description: 签到奖励
     * @Param: []
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/6/3
     */
    List<Map<String, Object>> getSignReward();

    /**
     * @Description: 获取奖励
     * @Param: [taskCode, suffix]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/6/24
     */
    List<Map<String, Object>> getRewardByCode(@Param("taskCode") String taskCode, @Param("suffix") Integer suffix);
    
    /** 
     * @Description: 获取奖励
     * @Param: [taskCode, suffix]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/6/15 
     */ 
    List<Map<String, Object>> getRewardByCodeForMap(@Param("taskCode") String taskCode, @Param("suffix") Integer suffix);



}
