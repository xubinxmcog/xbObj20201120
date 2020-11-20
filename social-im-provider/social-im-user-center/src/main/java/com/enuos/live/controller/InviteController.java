package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.Base;
import com.enuos.live.pojo.InviteMoney;
import com.enuos.live.pojo.InviteUser;
import com.enuos.live.result.Result;
import com.enuos.live.service.InviteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 邀请
 * @Author wangyingjie
 * @Date 2020/8/4
 * @Modified
 */
@Slf4j
@Api("邀请")
@RestController
@RequestMapping("/invite")
public class InviteController {

    @Autowired
    private InviteService inviteService;

    /**
     * @Description: 接受邀请
     * @Param: [inviteUser]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/4
     */
    @ApiOperation(value = "接受邀请", notes = "接受邀请")
    @Cipher
    @PostMapping("/accept")
    public Result accept(@RequestBody InviteUser inviteUser) {
        return inviteService.accept(inviteUser);
    }

    /**
     * @Description: 我的奖励
     * @Param: [base]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/4
     */
    @ApiOperation(value = "我的奖励", notes = "我的奖励")
    @Cipher
    @PostMapping("/myReward")
    public Result myReward(@RequestBody Base base) {
        return inviteService.myReward(base.userId);
    }

    /**
     * @Description: 提现
     * @Param: [inviteMoney]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/5
     */
    @ApiOperation(value = "提现", notes = "提现")
    @Cipher
    @PostMapping("/toGet")
    public Result toGet(@RequestBody InviteMoney inviteMoney) {
        return inviteService.toGet(inviteMoney);
    }

    /**
     * @Description: 提现记录
     * @Param: [base]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/6
     */
    @ApiOperation(value = "提现记录", notes = "提现记录")
    @Cipher
    @PostMapping("/record")
    public Result record(@RequestBody Base base) {
        return inviteService.record(base.userId);
    }

}
