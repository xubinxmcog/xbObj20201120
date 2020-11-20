package com.enuos.live.mapper;

import com.enuos.live.pojo.GashaponUser;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/23
 * @Modified
 */
public interface GashaponMapper {

    /**
     * @Description: 获取cron
     * @Param: []
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/6/23
     */
    List<Map<String, Object>> getGashaponCrontab();
    
    /** 
     * @Description: 获取suffix
     * @Param: [taskCode, time] 
     * @Return: java.lang.String 
     * @Author: wangyingjie
     * @Date: 2020/6/23 
     */ 
    String getSuffix(@Param("taskCode") String taskCode, @Param("time") int time);

    /** 
     * @Description: 获取user
     * @Param: [code] 
     * @Return: java.util.List<com.enuos.live.pojo.GashaponUser> 
     * @Author: wangyingjie
     * @Date: 2020/6/23 
     */ 
    List<GashaponUser> getJoinUser(String code);

    /** 
     * @Description: 设置中奖用户 
     * @Param: [gashaponUserList]
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/6/23 
     */ 
    int batchUpdate(List<GashaponUser> gashaponUserList);

    /**
     * @Description: 奖励
     * @Param: [code]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/6/24
     */
    List<Map<String, Object>> getReward(String code);

}
