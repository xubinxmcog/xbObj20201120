package com.enuos.live.service;

import com.enuos.live.pojo.Account;
import com.enuos.live.pojo.BindAccount;
import com.enuos.live.pojo.Logout;
import com.enuos.live.result.Result;

/**
 * @Description 用户业务层
 * @Author wangyingjie
 * @Date 17:19 2020/4/1
 * @Modified
 */
public interface AccountService {

    /**
     * @Description: 账号注册
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Result regist(Account account);

    /**
     * @Description: 账号绑定列表
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Result bindList(Account account);

    /**
     * @Description: 绑定
     * @Param: [bindAccount]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Result bind(BindAccount bindAccount);

    /**
     * @Description: 注销账户
     * @Param: [logout]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Result logout(Logout logout);

}
