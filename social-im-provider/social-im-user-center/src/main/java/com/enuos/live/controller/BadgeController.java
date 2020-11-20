package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.Badge;
import com.enuos.live.pojo.User;
import com.enuos.live.result.Result;
import com.enuos.live.service.BadgeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 徽章
 * @Author wangyingjie
 * @Date 2020/9/17
 * @Modified
 */
@Slf4j
@Api("徽章接口")
@RestController
@RequestMapping("/badge")
public class BadgeController {

    @Autowired
    private BadgeService badgeService;

    /**
     * @Description: 获取主页徽章
     * @Param: [user]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @ApiOperation(value = "获取主页徽章", notes = "获取主页徽章")
    @Cipher
    @PostMapping("/wearBadgeList")
    public Result wearBadgeList(@RequestBody User user) {
        return badgeService.wearBadgeList(user);
    }

    /** 
     * @Description: 获得的徽章
     * @Param: [badge] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/9/18 
     */ 
    @ApiOperation(value = "获得的徽章", notes = "获得的徽章")
    @Cipher
    @PostMapping("/num")
    public Result num(@RequestBody Badge badge) {
        return badgeService.num(badge);
    }
    
    /**
     * @Description: 列表
     * @Param: [badge]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    @ApiOperation(value = "徽章列表", notes = "列表")
    @Cipher
    @PostMapping("/list")
    public Result list(@RequestBody Badge badge) {
        return badgeService.list(badge);
    }

    /**
     * @Description: 佩戴
     * @Param: [badge]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    @ApiOperation(value = "佩戴", notes = "佩戴")
    @Cipher
    @PostMapping("/wear")
    public Result wear(@RequestBody Badge badge) {
        return badgeService.wear(badge);
    }

}
