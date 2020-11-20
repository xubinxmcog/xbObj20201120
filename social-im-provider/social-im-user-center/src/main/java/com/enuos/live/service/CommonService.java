package com.enuos.live.service;

import com.enuos.live.pojo.Account;
import com.enuos.live.pojo.User;
import com.enuos.live.result.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Description 通用业务
 * @Author wangyingjie
 * @Date 12:56 2020/4/14
 * @Modified
 */
public interface CommonService {

    /** 
     * @Description: 标签 
     * @Param: [] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/7/6 
     */ 
    Result label();
    
    /** 
     * @Description: 刷新登陆
     * @Param: [account] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/10/28 
     */ 
    Result refreshLogin(Account account);
    
    /** 
     * @Description: 刷新位置
     * @Param: [account]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/7/6 
     */ 
    Result refreshPoint(Account account);

    /** 
     * @Description: 获取用户基本信息 
     * @Param: [userId, keys] 
     * @Return: java.util.Map<java.lang.String,java.lang.Object> 
     * @Author: wangyingjie
     * @Date: 2020/7/6 
     */ 
    Map<String, Object> getUserBase(Long userId, String... keys);

    /**
     * @Description: 刷新在线状态
     * @Param: [user]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Map<String, Object> refreshOnLineStatus(User user);

    /**
     * @Description: 获取在线状态
     * @Param: [userId]
     * @Return: java.lang.Object
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Object getOnLineStatus(Long userId);

    /**
     * @Description: 获取二维码
     * @Param: [request, response]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    void getUserQRCode(HttpServletRequest request, HttpServletResponse response);

}
