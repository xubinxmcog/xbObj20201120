package com.enuos.live.service;

import com.enuos.live.pojo.GashaponUser;

import java.util.List;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/24
 * @Modified
 */
public interface GashaponService {

    /**
     * @Description: 幸运用户
     * @Param: [code, list]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/6/24
     */
    void luckyUser(String code, List<GashaponUser> list);
}
