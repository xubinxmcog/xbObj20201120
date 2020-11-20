package com.enuos.live.service;

import com.enuos.live.pojo.Account;

/**
 * @Description 日志
 * @Author wangyingjie
 * @Date 2020/11/4
 * @Modified
 */
public interface LogService {
    
    /**
     * @Description: 登陆日志
     * @Param: [account]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/11/4
     */
    void sendLogin(Account account);
}
