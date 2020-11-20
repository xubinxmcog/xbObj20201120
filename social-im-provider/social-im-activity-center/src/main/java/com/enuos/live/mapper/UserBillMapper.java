package com.enuos.live.mapper;

import com.enuos.live.pojo.Bill;

/**
 * @Description 账单
 * @Author wangyingjie
 * @Date 2020/10/13
 * @Modified
 */
public interface UserBillMapper {

    /** 
     * @Description: 保存
     * @Param: [bill] 
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/10/13 
     */ 
    void save(Bill bill);
}
