package com.enuos.live.mapper;

import com.enuos.live.pojo.JinQiu;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description 金秋送福[ACT0005]
 * @Author wangyingjie
 * @Date 2020/9/23
 * @Modified
 */
public interface JinQiuMapper {

    /**
     * @Description: 详情
     * @Param: [code]
     * @Return: com.enuos.live.pojo.JinQiu
     * @Author: wangyingjie
     * @Date: 2020/9/24
     */
    JinQiu getDetail(String code);

    /**
     * @Description: 获取领奖记录
     * @Param: [userId, codeList]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/9/24
     */
    List<Map<String, Object>> getRewardRecord(@Param("userId") Long userId, @Param("codeList") List<String> codeList);

}
