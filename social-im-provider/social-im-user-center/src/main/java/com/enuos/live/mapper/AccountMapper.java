package com.enuos.live.mapper;

import com.enuos.live.pojo.Account;
import com.enuos.live.pojo.BindAccount;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description 账户
 * @Author wangyingjie
 * @Date 14:33 2020/4/1
 * @Modified
 */
public interface AccountMapper {

    /**
     * @Description: 白名单账号
     * @Param: []
     * @Return: java.util.List<java.lang.String>
     * @Author: wangyingjie
     * @Date: 2020/7/17
     */
    List<String> getWhiteList();

    /**
     * 获取账户
     * @param account
     * @return
     */
    Account getAccount(Account account);

    /**
     * 获取账户
     * @param userId
     * @return
     */
    Account getAccountByUserId(Long userId);

    /**
     * 获取账户
     * @param userId
     * @return
     */
    BindAccount getAccountBindInfo(Long userId);

    /**
     * 保存账户
     * @param account
     * @return
     */
    int saveAccount(Account account);

    /**
     * @Description: 用户ID是否可用
     * @Param: [userId]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/8/11
     */
    Integer isExistsUserId(Long userId);

    /**
     * @Description: 账号是否可用
     * @Param: [account, registType]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/8/11
     */
    Integer isExistsAccount(@Param("account") String account, @Param("registType") Integer registType);

    /**
     * 绑定账号
     * @param bindAccount
     * @return
     */
    int update(BindAccount bindAccount);

    /**
     * 注销账号和用户[逻辑]
     * @param userId
     * @param table
     * @return
     */
    int logout(@Param("userId") Long userId, @Param("table") String table);

    /**
     * 注销
     * @param userId
     * @param tables
     * @return
     */
    int logoutToDelete(@Param("userId") Long userId, @Param("tables") String[] tables);

    /**
     * @Description: 手机账号获取用户信息
     * @Param: [phone]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/1
     */
    Map<String, Object> getUserBaseForWebByPhone(String phone);
}
