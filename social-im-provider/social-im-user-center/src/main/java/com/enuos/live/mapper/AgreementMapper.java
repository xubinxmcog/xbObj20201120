package com.enuos.live.mapper;

import java.util.List;
import java.util.Map;

/**
 * @Description 协议
 * @Author wangyingjie
 * @Date 2020/8/10
 * @Modified
 */
public interface AgreementMapper {

    /**
     * @Description: 获取url
     * @Param: [types]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/8/10
     */
    List<Map<String, Object>> getUrl(Integer[] types);

}
