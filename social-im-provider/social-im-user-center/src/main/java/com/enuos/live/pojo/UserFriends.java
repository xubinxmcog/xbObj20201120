package com.enuos.live.pojo;

import lombok.Data;

import java.util.List;

/**
 * @Description 好友
 * @Author wangyingjie
 * @Date 10:56 2020/4/17
 * @Modified
 */
@Data
public class UserFriends {

    /** 好友ID */
    private Long friendId;

    /** 好友的好友 */
    private List<UserFriends> friendList;

}
