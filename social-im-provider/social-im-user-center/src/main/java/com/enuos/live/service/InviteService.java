package com.enuos.live.service;

import com.enuos.live.pojo.InviteMoney;
import com.enuos.live.pojo.InviteUser;
import com.enuos.live.result.Result;

/**
 * @Description 邀请
 * @Author wangyingjie
 * @Date 2020/8/4
 * @Modified
 */
public interface InviteService {
    
    /** 
     * @Description: 受邀者完成分享任务处理
     * @Param: [params] 
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/8/6 
     */ 
    void newbieTask(Long userId, String templateCode);
    
    /** 
     * @Description: 接受邀请
     * @Param: [inviteUser]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/8/4 
     */ 
    Result accept(InviteUser inviteUser);
    
    /** 
     * @Description: 我的奖励
     * @Param: [userId] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/8/5 
     */ 
    Result myReward(Long userId);

    /** 
     * @Description: 提现
     * @Param: [inviteMoney]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/8/5 
     */ 
    Result toGet(InviteMoney inviteMoney);
    
    /** 
     * @Description: 提现记录
     * @Param: [userId] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/8/6 
     */ 
    Result record(Long userId);

}
