package com.enuos.live.mapper;

import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * @Description 每日经验
 * @Author wangyingjie
 * @Date 2020/5/19
 * @Modified
 */
public interface DayExpMapper {

    /**
     * 初始化
     * @param userId
     * @return
     */
    int initDayExp(Long userId);

    /**
     * 更新每日经验
     * @param userId
     * @param experience
     * @param updateTime
     * @return
     */
    int update(@Param("userId") Long userId, @Param("experience") Long experience, @Param("updateTime") LocalDateTime updateTime);

}
