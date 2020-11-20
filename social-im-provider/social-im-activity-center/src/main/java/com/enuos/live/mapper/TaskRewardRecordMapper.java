package com.enuos.live.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/9/17
 * @Modified
 */
public interface TaskRewardRecordMapper {
    /**
     * @Description: 获取领奖记录
     * @Param: [userId, code]
     * @Return: java.util.List<java.lang.String>
     * @Author: wangyingjie
     * @Date: 2020/6/11
     */
    List<String> getRecordByCode(@Param("userId") Long userId, @Param("code") String code);

    /**
     * @Description: 获取领奖记录
     * @Param: [userId, codeList]
     * @Return: java.util.List<java.lang.String>
     * @Author: wangyingjie
     * @Date: 2020/6/11
     */
    List<String> getRecordByCodeList(@Param("userId") Long userId, @Param("codeList") List<String> codeList);

    /**
     * @Description: 获取领奖记录
     * @Param: [userId, prefix]
     * @Return: java.util.List<java.lang.String>
     * @Author: wangyingjie
     * @Date: 2020/6/12
     */
    List<String> getRecordByPrefix(@Param("userId") Long userId, @Param("prefix") String prefix);

    /**
     * @Description: 获取数量
     * @Param: [userId, prefix]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/6/17
     */
    Map<String, Integer> getRecordNumByPrefix(@Param("userId") Long userId, @Param("prefix") String prefix);

    /**
     * @Description: 保存领奖记录
     * @Param: [userId, code]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/6/9
     */
    int save(@Param("userId") Long userId, @Param("code") String code);

    /**
     * @Description: 是否领奖
     * @Param: [userId, code]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/7/21
     */
    Integer isExists(@Param("userId") Long userId, @Param("code") String code);

}
