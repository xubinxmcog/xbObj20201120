package com.enuos.live.service;

import com.enuos.live.pojo.Task;
import com.enuos.live.result.Result;

/**
 * @Description 幸运扭蛋
 * @Author wangyingjie
 * @Date 2020/6/19
 * @Modified
 */
public interface GashaponService {

    /**
     * @Description: 扭蛋数
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/22
     */
    Result num(Task task);

    /**
     * @Description: 列表
     * @Param: [task]
     * @Return: Result
     * @Author: wangyingjie
     * @Date: 2020/6/19
     */
    Result lotteryList(Task task);

    /** 
     * @Description: 参与 
     * @Param: [task]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/22 
     */ 
    Result join(Task task);

    /**
     * @Description: 获取开奖结果
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/24
     */
    Result result(Task task);
    
    /** 
     * @Description: 兑换列表
     * @Param: [task]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/24 
     */ 
    Result exchangeList(Task task);
    
    /** 
     * @Description: 兑换 
     * @Param: [task]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/24 
     */ 
    Result exchange(Task task);
    
    /** 
     * @Description: 中奖记录 
     * @Param: [task]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/28 
     */ 
    Result lotteryRecordPage(Task task);

    /** 
     * @Description: 兑换记录
     * @Param: [task]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/28 
     */ 
    Result exchangeRecordPage(Task task);
}
