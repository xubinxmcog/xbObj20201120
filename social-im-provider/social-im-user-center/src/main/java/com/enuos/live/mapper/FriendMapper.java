package com.enuos.live.mapper;

import com.enuos.live.pojo.Friend;

import java.util.List;
import java.util.Map;

/**
 * @Description 用户好友
 * @Author wangyingjie
 * @Date 2020/7/7
 * @Modified
 */
public interface FriendMapper {
    
    /** 
     * @Description: 两个用户的关系
     * @Param: [friend]
     * @Return: java.util.Map<java.lang.String,java.lang.Object> 
     * @Author: wangyingjie
     * @Date: 2020/7/7 
     */ 
    Map<String, Object> relation(Friend friend);

    /**
     * @Description: 保存好友关系
     * @Param: [friend]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    int saveRelation(Friend friend);

    /**
     * @Description: 是否拉黑
     * @Param: [friend]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/10
     */
    Integer isBlack(Friend friend);

    /**
     * @Description: 更新好友关系
     * @Param: [userId, friendId, isDel]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    int updateRelation(Friend friend);

    /**
     * @Description: 获取好友列表
     * @Param: [friend]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    List<Map<String, Object>> getFriendList(Friend friend);

    /**
     * @Description: 修改备注
     * @Param: [friend]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    int updateFriend(Friend friend);

    /**
     * @Description: 获取好友人数
     * @Param: [userId]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/9/15
     */
    Map<String, Object> getFriendNum(Long userId);

}
