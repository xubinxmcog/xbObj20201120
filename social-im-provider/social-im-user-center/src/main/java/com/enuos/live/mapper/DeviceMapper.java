package com.enuos.live.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * @Description 设备
 * @Author wangyingjie
 * @Date 2020/8/7
 * @Modified
 */
public interface DeviceMapper {
    /**
     * @Description: 该用户的设备号绑定了几个用户ID
     * @Param: [userId]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/11/6
     */
    Integer getCount(@Param("userId") Long userId);

    /**
     * @Description: 设备号是否存在
     * @Param: [userId, number, type]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/8/7
     */
    Integer isExists(@Param("userId") Long userId, @Param("number") String number, @Param("type") Integer type);

    /** 
     * @Description: 保存
     * @Param: [userId, number, type]
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/8/7 
     */ 
    int save(@Param("userId") Long userId, @Param("number") String number, @Param("type") Integer type);
}
