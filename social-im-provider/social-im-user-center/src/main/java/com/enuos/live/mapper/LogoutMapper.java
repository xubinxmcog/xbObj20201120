package com.enuos.live.mapper;

import com.enuos.live.pojo.Logout;

/**
 * @Description
 * @Author wangyingjie
 * @Date 14:08 2020/5/9
 * @Modified
 */
public interface LogoutMapper {

    /**
     * 保存账户注销信息
     * @param logout
     * @return
     */
    int save(Logout logout);

}
