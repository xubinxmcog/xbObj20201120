package com.enuos.live.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description 任务跟踪
 * @Author wangyingjie
 * @Date 2020/6/11
 * @Modified
 */
public interface TaskFollowMapper {
    
    /** 
     * @Description: 获取任务进度
     * @Param: [userId, prefix] 
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>> 
     * @Author: wangyingjie
     * @Date: 2020/6/11 
     */ 
    List<Map<String, Object>> getProgress(@Param("userId") Long userId, @Param("prefix") String prefix);

    /**
     * @Description: 任务追踪
     * @Param: [params]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/13
     */
    Map<String, Object> getTaskFollow(Map<String, Object> params);

    /**
     * @Description: 保存记录
     * @Param: [params]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/7/13
     */
    int save(Map<String, Object> params);

    /**
     * @Description: 更新进度
     * @Param: [params]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/7/13
     */
    int updateProgress(Map<String, Object> params);
    
    /**
     * @Description: 成就进度记录
     * @Param: [userId, list]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/7/14
     */
    List<Map<String, Object>> getAchievementFollowList(@Param("userId") Long userId, @Param("list") List<Map<String, Object>> list);

    /**
     * @Description: 批量保存记录
     * @Param: [userId, list]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/7/14
     */
    int batchSave(@Param("userId") Long userId, @Param("list") List<Map<String, Object>> list);
    
    /** 
     * @Description: 批量更新进度
     * @Param: [userId, list]
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/7/14 
     */ 
    int batchUpdateProgress(@Param("userId") Long userId, @Param("list") List<Map<String, Object>> list);
}
