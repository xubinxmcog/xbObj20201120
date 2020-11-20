package com.enuos.live.mapper;

import com.enuos.live.pojo.AccountAttach;

/**
 * @Description 账户附属信息
 * @Author wangyingjie
 * @Date 10:40 2020/4/7
 * @Modified
 */
public interface AccountAttachMapper {

    /**
     * @Description: 附属信息初始化
     * @Param: [userId]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/9/28
     */
    int initAccountAttach(Long userId);

    /**
     * @Description: 获取账号信息
     * @Param: [userId]
     * @Return: com.enuos.live.pojo.AccountAttach
     * @Author: wangyingjie
     * @Date: 2020/9/28
     */
    AccountAttach getByUserId(Long userId);

    /** 
     * @Description: 更新 
     * @Param: [accountAttach] 
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/9/28 
     */ 
    int update(AccountAttach accountAttach);

}
