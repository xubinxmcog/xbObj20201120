package com.enuos.live.mapper;

import com.enuos.live.pojo.BSGashapon;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/28
 * @Modified
 */
public interface BSGashaponMapper {

    /** 
     * @Description: code是否可用
     * @Param: [code] 
     * @Return: java.lang.Integer 
     * @Author: wangyingjie
     * @Date: 2020/6/28 
     */ 
    Integer isExists(String code);

    /** 
     * @Description: 保存扭蛋任务
     * @Param: [bsGashapon] 
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/6/28 
     */ 
    int save(BSGashapon bsGashapon);

    /**
     * @Description: 任务拆分
     * @Param: [list]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/6/19
     */
    int initGashapon(List<Map<String, Object>> list);
    
    /** 
     * @Description: 设置定时
     * @Param: [bsGashapon] 
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/6/28 
     */ 
    int initJob(BSGashapon bsGashapon);
    
    /** 
     * @Description: 设置奖励
     * @Param: [bsGashapon] 
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/6/29 
     */ 
    int initReward(BSGashapon bsGashapon);
}
