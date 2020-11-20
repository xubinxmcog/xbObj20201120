package com.enuos.live.mapper;

import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Description 访客业务
 * @Author wangyingjie
 * @Date 2020/7/14
 * @Modified
 */
public interface VisitorMapper {
    
    /** 
     * @Description: 获取当天访问记录
     * @Param: [userId, visitorId, visitTime] 
     * @Return: java.util.Map<java.lang.String,java.lang.Object> 
     * @Author: wangyingjie
     * @Date: 2020/7/15 
     */ 
    Map<String, Object> getVisitor(@Param("userId") Long userId, @Param("visitorId") Long visitorId);
    
    /** 
     * @Description: 保存访客记录
     * @Param: [userId, visitorId]
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/7/14 
     */ 
    int save(@Param("userId") Long userId, @Param("visitorId") Long visitorId);

    /**
     * @Description: 更新访问时间
     * @Param: [id]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/7/15
     */
    int updateVisitTime(Integer id);
    
    /**
     * @Description: 会员信息
     * @Param: [userId]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/15
     */
    Map<String, Object> getMember(Long userId);
    
    /** 
     * @Description: vip专享访客记录
     * @Param: [userId] 
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>> 
     * @Author: wangyingjie
     * @Date: 2020/7/14 
     */ 
    List<Map<String, Object>> list(Long userId);
}
