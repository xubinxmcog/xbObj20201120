package com.enuos.live.mapper;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 17:25 2020/4/20
 * @Modified
 */
public interface CommonMapper {

    /** 
     * @Description: 获取标签
     * @Param: [] 
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>> 
     * @Author: wangyingjie
     * @Date: 2020/7/20 
     */ 
    List<Map<String, Object>> getLabel();

    /** 
     * @Description: 获取用户信息
     * @Param: [userId] 
     * @Return: java.util.Map<java.lang.String,java.lang.Object> 
     * @Author: wangyingjie
     * @Date: 2020/7/6 
     */ 
    Map<String, Object> getUserBaseByUserId(Long userId);
    
    /**
     * @Description: 获取用户背景
     * @Param: [userId]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    List<Map<String, Object>> getUserBackgroundByUserId(Long userId);

}
