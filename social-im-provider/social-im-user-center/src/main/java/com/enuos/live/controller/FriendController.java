package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.pojo.Friend;
import com.enuos.live.result.Result;
import com.enuos.live.service.FriendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 用户好友
 * @Author wangyingjie
 * @Date 2020/7/7
 * @Modified
 */
@Slf4j
@Api("用户花名册")
@RestController
@RequestMapping("/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    /**
     * @Description: 判定加好友是否需要支付身价
     * @Param: [friend]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "判定加好友是否需要支付身价", notes = "判定加好友是否需要支付身价")
    @Cipher
    @PostMapping("/worth")
    public Result worth(@RequestBody Friend friend) {
        return friendService.worth(friend);
    }

    /**
     * @Description: 交朋友
     * @Param: [friend]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/7
     */
    @ApiOperation(value = "交朋友", notes = "交朋友")
    @Cipher
    @PostMapping("/makeFriend")
    public Result makeFriend(@RequestBody Friend friend) {
        try {
            return friendService.makeFriend(friend);
        } catch (Exception e) {
            return Result.error(ErrorCode.EXCEPTION_CODE, e.getMessage());
        }
    }

    /**
     * @Description: 花名册[好友&聊天]
     * @Param: [friend]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    @ApiOperation(value = "花名册[好友&聊天]", notes = "花名册[好友&聊天]")
    @Cipher
    @PostMapping("/roster")
    public Result roster(@RequestBody Friend friend) {
        return friendService.roster(friend);
    }

    /**
     * @Description: 修改备注
     * @Param: [friend]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    @ApiOperation(value = "修改备注", notes = "修改备注")
    @Cipher
    @PostMapping("/updateFriend")
    public Result updateFriend(@RequestBody Friend friend) {
        return friendService.updateFriend(friend);
    }

    /**
     * @Description: 解除好友关系
     * @Param: [friend]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    @ApiOperation(value = "解除好友关系", notes = "解除好友关系")
    @Cipher
    @PostMapping("/unFriend")
    public Result unFriend(@RequestBody Friend friend) {
        return friendService.unFriend(friend);
    }

}
