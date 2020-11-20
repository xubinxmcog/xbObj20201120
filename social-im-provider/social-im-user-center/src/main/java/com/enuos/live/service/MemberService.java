package com.enuos.live.service;

import com.enuos.live.result.Result;

import java.util.Map;

/**
 * @Description 会员中心
 * @Author wangyingjie
 * @Date 2020/6/29
 * @Modified
 */
public interface MemberService {

    /**
     * @Description: 处理会员装饰
     * @Param: [userId, vip]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/31
     */
    void decorationHandler(Long userId, Integer vip);

    /**
 * @Description: 添加成长值
     * @Param: [userId, growth]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/20
     */
    Result addGrowth(Long userId, Integer growth);

    /** 
     * @Description: 会员中心
     * @Param: [userId] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/30 
     */ 
    Result center(Long userId);

    /**
     * @Description: 充值套餐
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/30
     */
    Result rechargePackage();

    /**
     * @Description: 充值结果
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/30
     */
    Result rechargeResult(Map<String, Object> params);

    /**
     * @Description: 是否会员
     * @Param: [userId]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/8/18
     */
    Integer isMember(Long userId);
}
