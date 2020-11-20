package com.enuos.live.mapper;

import com.enuos.live.pojo.QiuRi;

/**
 * @Description 丹枫迎秋
 * @Author wangyingjie
 * @Date 2020/9/17
 * @Modified
 */
public interface QiuRiMapper {

    /**
     * @Description: 丹枫迎秋活动日常
     * @Param: [code]
     * @Return: java.util.List<com.enuos.live.pojo.QiuRiTask>
     * @Author: wangyingjie
     * @Date: 2020/8/12
     */
    QiuRi getQiuRiByCode(String code);
}
