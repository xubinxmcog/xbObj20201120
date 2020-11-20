package com.enuos.live.mapper;

import com.enuos.live.pojo.*;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Description 乐享令状
 * @Author wangyingjie
 * @Date 2020/10/10
 * @Modified
 */
public interface WritMapper {
    
    /**
     * @Description: 获取任务编码
     * @Param: [templateCode, dateTime]
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/10/27
     */
    String getTaskCode(@Param("templateCode") String templateCode, @Param("dateTime") LocalDateTime dateTime);

    /**
     * @Description: 获取基础等级奖励
     * @Param: [taskCode, templateCode, category, suffix]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/10/27
     */
    List<Map<String, Object>> getLevelRewardOfList(@Param("taskCode") String taskCode, @Param("templateCode") String templateCode, @Param("category") Integer category, @Param("suffix") Integer suffix);
    
    /** 
     * @Description: 获取基础等级奖励 
     * @Param: [taskCode, templateCode, category, suffix] 
     * @Return: java.util.Map<java.lang.String,java.lang.Object> 
     * @Author: wangyingjie
     * @Date: 2020/10/28 
     */ 
    Map<String, Object> getLevelRewardOfMap(@Param("taskCode") String taskCode, @Param("templateCode") String templateCode, @Param("category") Integer category, @Param("suffix") Integer suffix);
    
    /**
     * @Description: 获取奖励[等级]
     * @Param: [taskCode, templateCode, levelList]
     * @Return: java.util.List<com.enuos.live.pojo.Reward>
     * @Author: wangyingjie
     * @Date: 2020/10/27
     */
    List<Reward> getRewardByLevel(@Param("taskCode") String taskCode, @Param("templateCode") String templateCode, @Param("levelList") List<Integer> levelList);

    /**
     * @Description: 获取奖励
     * @Param: [taskCode, templateCode]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/10/28
     */
    Map<String, Object> getReward(@Param("taskCode") String taskCode, @Param("templateCode") String templateCode);
    
    /**
     * @Description: 获取奖励
     * @Param: [taskCode, templateCode]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/11/2
     */
    List<Map<String, Object>> getRewardSimple(@Param("taskCode") String taskCode, @Param("templateCode") String templateCode);

    /**
     * @Description: 用户令状信息
     * @Param: [taskCode, templateCode, userId]
     * @Return: com.enuos.live.pojo.WritUser
     * @Author: wangyingjie
     * @Date: 2020/10/26
     */
    WritUser getWritUser(@Param("taskCode") String taskCode, @Param("templateCode") String templateCode, @Param("userId") Long userId);

    /** 
     * @Description: 修改用户令状信息
     * @Param: [writUser] 
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/10/12 
     */ 
    void updateWritUser(WritUser writUser);
    
    /**
     * @Description: 初始化用户令状信息
     * @Param: [taskCode, userId]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/10/10
     */
    void initWritUser(@Param("taskCode") String taskCode, @Param("userId") Long userId);

    /** 
     * @Description: 保存记录
     * @Param: [writRecord] 
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/10/12 
     */ 
    void saveRecord(WritRecord writRecord);
    
    /** 
     * @Description: 是否存在记录
     * @Param: [userId, taskCode, templateCode, category, suffix] 
     * @Return: java.lang.Integer 
     * @Author: wangyingjie
     * @Date: 2020/10/28 
     */ 
    Integer isExistsRecord(WritRecord writRecord);

    /**
     * @Description: 获取记录
     * @Param: [taskCode, templateCode, userId]
     * @Return: java.util.List<java.lang.String>
     * @Author: wangyingjie
     * @Date: 2020/10/27
     */
    List<String> getRecord(@Param("taskCode") String taskCode, @Param("templateCode") String templateCode, @Param("userId") Long userId);
    
    /**
     * @Description: 获取等级
     * @Param: [taskCode, templateCode]
     * @Return: java.util.List<java.lang.Integer>
     * @Author: wangyingjie
     * @Date: 2020/10/27
     */
    List<Integer> getLevelOfPage(@Param("taskCode") String taskCode, @Param("templateCode") String templateCode);

    /**
     * @Description: 任务列表
     * @Param: [taskCode, groupId]
     * @Return: java.util.List<com.enuos.live.pojo.Task>
     * @Author: wangyingjie
     * @Date: 2020/10/27
     */
    List<Task> getTask(@Param("taskCode") String taskCode, @Param("groupId") Integer groupId);
    
    /**
     * @Description: 领奖记录
     * @Param: [taskCode, groupId, userId, currentDate]
     * @Return: java.util.List<java.lang.String>
     * @Author: wangyingjie
     * @Date: 2020/10/27
     */
    List<String> getRecordTemplateCodeList(@Param("userId") Long userId, @Param("taskCode") String taskCode, @Param("groupId") Integer groupId, @Param("date") LocalDate date);

    /**
     * @Description: 令状任务
     * @Param: [taskCode, userId]
     * @Return: com.enuos.live.pojo.WritTask
     * @Author: wangyingjie
     * @Date: 2020/10/27
     */
    WritTask getWritTask(@Param("taskCode") String taskCode, @Param("userId") Long userId);

    /**
     * @Description: 兑换
     * @Param: [taskCode, groupId, userId]
     * @Return: com.enuos.live.pojo.WritExchange
     * @Author: wangyingjie
     * @Date: 2020/10/27
     */
    WritExchange getWritExchange(@Param("taskCode") String taskCode, @Param("groupId") Integer groupId, @Param("userId") Long userId);

    /** 
     * @Description: 获取礼盒内奖品
     * @Param: [boxCodeList] 
     * @Return: java.util.List<com.enuos.live.pojo.Reward> 
     * @Author: wangyingjie
     * @Date: 2020/10/14 
     */ 
    List<Reward> getRewardOfBoxByBoxCodeList(List<String> boxCodeList);

    /**
     * @Description: 获取礼盒内奖品
     * @Param: [boxCode]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/10/14
     */
    List<Map<String, Object>> getRewardOfBoxByBoxCode(String boxCode);

    /**
     * @Description: 排行榜
     * @Param: [writCode, limit]
     * @Return: java.util.List<com.enuos.live.pojo.WritUser>
     * @Author: wangyingjie
     * @Date: 2020/10/13
     */
    List<WritUser> getRankList(@Param("taskCode") String taskCode, @Param("limit") Integer limit);

    /**
     * @Description: 获取任务次数
     * @Param: [taskCode, templateCode]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/10/28
     */
    Task getTaskInfo(@Param("taskCode") String taskCode, @Param("templateCode") String templateCode);
    
    /** 
     * @Description: 获取记录次数
     * @Param: [userId, taskCode, templateCode, date] 
     * @Return: java.lang.Integer 
     * @Author: wangyingjie
     * @Date: 2020/10/28 
     */ 
    Integer getRecordCount(@Param("userId") Long userId, @Param("taskCode") String taskCode, @Param("templateCode") String templateCode, @Param("date") LocalDate date);

    /** 
     * @Description: 获取日常奖励积分
     * @Param: [taskCode, templateCode] 
     * @Return: java.lang.Integer 
     * @Author: wangyingjie
     * @Date: 2020/10/27 
     */ 
    Integer getIntegralOfDayTask(@Param("taskCode") String taskCode, @Param("templateCode") String templateCode);
}
