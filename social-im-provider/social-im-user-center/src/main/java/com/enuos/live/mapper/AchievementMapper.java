package com.enuos.live.mapper;

import com.enuos.live.pojo.Achievement;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/16
 * @Modified
 */
public interface AchievementMapper {

    /** 
     * @Description: 获取成就类型
     * @Param: [] 
     * @Return: java.util.List<java.lang.Integer> 
     * @Author: wangyingjie
     * @Date: 2020/6/16 
     */ 
    List<Integer> getType();

    /** 
     * @Description: 获取成就列表
     * @Param: [userId, typeList]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>> 
     * @Author: wangyingjie
     * @Date: 2020/6/16 
     */ 
    List<Achievement> getAchievementList(@Param("userId") Long userId, @Param("typeList") List<Integer> typeList);

}
