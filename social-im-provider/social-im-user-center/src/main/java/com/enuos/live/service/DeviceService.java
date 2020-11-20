package com.enuos.live.service;

/**
 * @Description 设备
 * @Author wangyingjie
 * @Date 2020/8/10
 * @Modified
 */
public interface DeviceService {

    /** 
     * @Description: 保存设备号
     * @Param: [userId, number, type]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/8/10 
     */ 
    int save(Long userId, String number, Integer type);

}
