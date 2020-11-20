package com.enuos.live.mapper;

import com.enuos.live.pojo.Task;
import com.enuos.live.pojo.TaskParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description 任务dao
 * @Author wangyingjie
 * @Date 16:50 2020/4/10
 * @Modified
 */
public interface TaskMapper {

    /** 
     * @Description: 获取任务列表
     * @Param: [userId] 
     * @Return: java.util.List<com.enuos.live.pojo.Task>
     * @Author: wangyingjie
     * @Date: 2020/6/11 
     */ 
    List<Task> getList();
    
    /** 
     * @Description: 任务[阈值]
     * @Param: [taskParam] 
     * @Return: java.lang.Integer 
     * @Author: wangyingjie
     * @Date: 2020/11/12 
     */ 
    Integer getSuffix(TaskParam taskParam);
    
    /** 
     * @Description: 任务[活跃]
     * @Param: [taskCode, templateCodes]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>> 
     * @Author: wangyingjie
     * @Date: 2020/11/10 
     */ 
    List<Map<String, Object>> getTaskActive(@Param("taskCode") String taskCode, @Param("templateCodes") String... templateCodes);

    /**
     * @Description: 任务记录[列表]
     * @Param: [taskParam]
     * @Return: java.util.List<java.lang.String>
     * @Author: wangyingjie
     * @Date: 2020/11/11
     */
    List<String> getRecord(TaskParam taskParam);

    /** 
     * @Description: 任务记录[是否完成]
     * @Param: [taskParam]
     * @Return: java.lang.Integer 
     * @Author: wangyingjie
     * @Date: 2020/11/5 
     */ 
    Integer isExistsRecord(TaskParam taskParam);
    
    /** 
     * @Description: 任务记录[保存]
     * @Param: [taskParam]
     * @Return: java.lang.Integer 
     * @Author: wangyingjie
     * @Date: 2020/11/5 
     */ 
    Integer saveRecord(TaskParam taskParam);

    /** 
     * @Description: 任务奖励[获取]
     * @Param: [taskParam]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>> 
     * @Author: wangyingjie
     * @Date: 2020/11/5 
     */ 
    List<Map<String, Object>> getReward(TaskParam taskParam);
    
    /** 
     * @Description: 任务进度[获取]
     * @Param: [taskParam]
     * @Return: java.util.Map<java.lang.String,java.lang.Object> 
     * @Author: wangyingjie
     * @Date: 2020/11/6 
     */ 
    Map<String, Object> getFollow(TaskParam taskParam);
    
    /** 
     * @Description: 任务进度[保存] 
     * @Param: [taskParam]
     * @Return: java.lang.Integer 
     * @Author: wangyingjie
     * @Date: 2020/11/6 
     */  
    Integer saveFollow(TaskParam taskParam);
    
    /** 
     * @Description: 任务进度[更新] 
     * @Param: [id, byValue] 
     * @Return: java.lang.Integer 
     * @Author: wangyingjie
     * @Date: 2020/11/6 
     */ 
    Integer updateFollow(@Param("id") Integer id, @Param("byValue") Integer byValue);
}
