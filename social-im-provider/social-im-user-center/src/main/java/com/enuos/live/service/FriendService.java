package com.enuos.live.service;

import com.enuos.live.pojo.Friend;
import com.enuos.live.result.Result;

/**
 * @Description 用户好友
 * @Author wangyingjie
 * @Date 2020/7/7
 * @Modified
 */
public interface FriendService {

    /**
     * @Description: 判定加好友是否需要支付身价
     * @Param: [friend]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    Result worth(Friend friend);

    /** 
     * @Description: 交朋友
     * @Param: [friend]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/7/7 
     */ 
    Result makeFriend(Friend friend);

    /**
     * @Description: 花名册[好友&聊天]
     * @Param: [friend]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    Result roster(Friend friend);

    /**
     * @Description: 修改备注
     * @Param: [friend]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    Result updateFriend(Friend friend);

    /**
     * @Description: 解除好友关系
     * @Param: [friend]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    Result unFriend(Friend friend);

}
