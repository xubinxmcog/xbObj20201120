package com.enuos.live.mapper;

import java.util.List;
import java.util.Map;

/**
 * @Description 等级中心
 * @Author wangyingjie
 * @Date 2020/7/21
 * @Modified
 */
public interface LevelMapper {

    /** 
     * @Description: 获取当前等级
     * @Param: [userId] 
     * @Return: java.lang.Integer 
     * @Author: wangyingjie
     * @Date: 2020/7/21 
     */ 
    Integer getLevel(Long userId);

    /** 
     * @Description: 获取等级阈值
     * @Param: [userId] 
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>> 
     * @Author: wangyingjie
     * @Date: 2020/7/21 
     */ 
    List<Map<String, Object>> getLevelThreshold(Long userId);


}
