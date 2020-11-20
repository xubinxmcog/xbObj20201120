package com.enuos.live.mapper;

import java.util.Map;

/**
 * @Description 日志保存
 * @Author wangyingjie
 * @Date 2020/9/3
 * @Modified
 */
public interface LogMapper {

    /**
     * @Description: 保存登陆日志
     * @Param: [logLogin]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/8/31
     */
    void saveLogLogin(Map<String, Object> logLogin);

    /**
     * @Description: 保存动态日志
     * @Param: [logPost]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/2
     */
    void saveLogPost(Map<String, Object> logPost);

    /**
     * @Description: 保存账单日志
     * @Param: [logBill]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/4
     */
    void saveLogBill(Map<String, Object> logBill);
}
