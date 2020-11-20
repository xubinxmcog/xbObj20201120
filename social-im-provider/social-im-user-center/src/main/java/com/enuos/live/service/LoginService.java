package com.enuos.live.service;

import com.enuos.live.pojo.Account;
import com.enuos.live.result.Result;

/**
 * @Description 用户登陆注册相关业务类
 * @Author wangyingjie
 * @Date 16:08 2020/3/31
 * @Modified
 */
public interface LoginService {

    /**
     * @Description: 发送短信
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Result sendSMS(Account account);

    /**
     * @Description: 手机短信验证登陆
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Result loginWithSMS(Account account);

    /**
     * @Description: 微信登陆
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Result loginWithWeChat(Account account);

    /**
     * @Description: QQ登陆
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Result loginWithQQ(Account account);

    /**
     * @Description: apple登陆
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/1
     */
    Result loginWithApple(Account account);

    /**
     * @Description: web登陆
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/1
     */
    Result loginForWebWithSMS(Account account);

}
