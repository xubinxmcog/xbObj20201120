package com.enuos.live.mapper;

import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/12
 * @Modified
 */
public interface ActiveMapper {
    
    /** 
     * @Description: 获取天活跃
     * @Param: [userId, createTime] 
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/6/12 
     */ 
    Map<String, Object> getActive(@Param("userId") Long userId, @Param("createTime") LocalDate createTime);
    
    /** 
     * @Description: 保存活跃 
     * @Param: [userId, createTime] 
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/6/12 
     */ 
    int save(@Param("userId") Long userId, @Param("createTime") LocalDate createTime);
    
    /** 
     * @Description: 更新
     * @Param: [userId, active, createTime] 
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/6/12 
     */ 
    int update(@Param("userId") Long userId, @Param("active") Integer active, @Param("createTime") LocalDate createTime);
}
