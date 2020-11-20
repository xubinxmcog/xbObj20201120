package com.enuos.live.mapper;

import com.enuos.live.pojo.Badge;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description 用户徽章
 * @Author wangyingjie
 * @Date 2020/7/16
 * @Modified
 */
public interface BadgeMapper {
    
    /** 
     * @Description: 获取主页徽章 
     * @Param: [userId] 
     * @Return: java.util.List<com.enuos.live.pojo.Badge> 
     * @Author: wangyingjie
     * @Date: 2020/9/24
     */ 
    List<Badge> getWearBadgeList(Long userId);
    
    /**
     * @Description: 获得的徽章
     * @Param: [userId]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/9/18
     */
    Map<String, Object> getNum(Long userId);

    /**
     * @Description: 类别分页
     * @Param: []
     * @Return: java.util.List<java.lang.Integer>
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    List<Integer> getType();

    /**
     * @Description: 获取徽章
     * @Param: [userId, typeList]
     * @Return: java.util.List<com.enuos.live.pojo.Badge>
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    List<Badge> getBadgeList(@Param("userId") Long userId, @Param("typeList") List<Integer> typeList);

    /**
     * @Description: 获取佩戴状态
     * @Param: [userId, code]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/6/17
     */
    Integer getWear(@Param("userId") Long userId, @Param("code") String code);

    /**
     * @Description: 获取已经佩戴的徽章数
     * @Param: [userId]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/7/16
     */
    Integer getWearNum(Long userId);

    /**
     * @Description: 佩戴/卸下
     * @Param: [userId, code, wear]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/6/17
     */
    int updateWear(@Param("userId") Long userId, @Param("code") String code, @Param("wear") Integer wear);

    /**
     * ==========[内部调用]==========
     */

    /**
     * @Description: 获取已经存在的用户徽章
     * @Param: [userId, badgeLit]
     * @Return: java.util.List<java.lang.String>
     * @Author: wangyingjie
     * @Date: 2020/7/16
     */
    List<String> getBadgeCode(@Param("userId") Long userId, @Param("badgeList") List<Map<String, Object>> badgeList);
    
    /** 
     * @Description: 批量新增
     * @Param: [userId, badgeLit]
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/7/16 
     */ 
    int batchSave(@Param("userId") Long userId, @Param("badgeList") List<Map<String, Object>> badgeList);

}
