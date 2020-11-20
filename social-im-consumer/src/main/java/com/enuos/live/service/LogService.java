package com.enuos.live.service;

/**
 * @Description 日志
 * @Author wangyingjie
 * @Date 2020/9/3
 * @Modified
 */
public interface LogService {

    /**
     * @Description: 记录登陆日志
     * @Param: [message]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/8/31
     */
    void logLogin(Object message);

    /**
     * @Description: 记录动态日志
     * @Param: [message]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/2
     */
    void logPost(Object message);

    /**
     * @Description: 记录账单日志
     * @Param: [message]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/4
     */
    void logBill(Object message);

}
