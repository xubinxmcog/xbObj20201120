package com.enuos.live.mapper;

import java.time.LocalDate;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/23
 * @Modified
 */
public interface TaskCenterMapper {

    /**
     * @Description: 删除本周一以前的活跃记录
     * @Param: [localDate]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/6/15
     */
    void deleteTaskActive(LocalDate localDate);

    /**
     * @Description: 删除本周一以前的日常任务记录
     * @Param: [localDate]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/6/15
     */
    void deleteTaskFollow(LocalDate localDate);

    /**
     * @Description: 删除本周一以前的领奖记录
     * @Param: [localDate]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/6/15
     */
    void deleteTaskRewardRecord(LocalDate localDate);

}
