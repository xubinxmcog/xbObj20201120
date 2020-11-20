package com.enuos.live.mapper;

import com.enuos.live.pojo.TaskSign;

import java.util.List;
import java.util.Map;

/**
 * @Description 签到
 * @Author wangyingjie
 * @Date 9:34 2020/4/9
 * @Modified 2020/6/5
 */
public interface TaskSignMapper {

    /**
     * @Description: 获取签到描述
     * @Param: []
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/7/14
     */
    List<Map<String, Object>> getSignDescription();

    /**
     * @Description: 获取签到次数
     * @Param: [userId, code]
     * @Return: Map<String,Object>
     * @Author: wangyingjie
     * @Date: 2020/6/5
     */
    Map<String, Object> getSignCount(Long userId, String code);

    /**
     * @Description: 获取签到信息
     * @Param: [taskSign]
     * @Return: com.enuos.live.pojo.TaskSignVO
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    TaskSign getTaskSign(TaskSign taskSign);

    /**
     * @Description: 签到
     * @Param: [taskSign]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    int saveTaskSign(TaskSign taskSign);

    /**
     * @Description: 更新签到
     * @Param: [taskSign]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    int updateTaskSign(TaskSign taskSign);

}
