package com.enuos.live.mapper;

import com.enuos.live.pojo.AccountAttach;
import com.enuos.live.pojo.Threshold;

import java.util.List;

/**
 * @Description 等级经验
 * @Author wangyingjie
 * @Date 2020/5/18
 * @Modified
 */
public interface ExpMapper {

    /**
     * @Description: 获取阈值
     * @Param: []
     * @Return: java.util.List<com.enuos.live.pojo.Threshold>
     * @Author: wangyingjie
     * @Date: 2020/5/29
     */
    List<Threshold> getThreshold();

    /**
     * @Description: 获取当前用户的等级经验
     * @Param: [userId]
     * @Return: com.enuos.live.pojo.AccountAttach
     * @Author: wangyingjie
     * @Date: 2020/5/29
     */
    AccountAttach getUserLevelExp(Long userId);

    /**
     * @Description: 更新
     * @Param: [accountAttach]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/5/29
     */
    int update(AccountAttach accountAttach);

    /**
     * @Description: 游戏今日已得经验
     * @Param: [userId]
     * @Return: com.enuos.live.pojo.AccountAttach
     * @Author: wangyingjie
     * @Date: 2020/5/29
     */
    AccountAttach getTodayGameExp(Long userId);

    /**
     * @Description: 获取等级经验
     * @Param: [userId]
     * @Return: com.enuos.live.pojo.AccountAttach
     * @Author: wangyingjie
     * @Date: 2020/6/12
     */
    AccountAttach getLevelExp(Long userId);
}
