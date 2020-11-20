package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.Activity;
import com.enuos.live.result.Result;
import com.enuos.live.service.ActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 活动中心
 * @Author wangyingjie
 * @Date 2020/8/12
 * @Modified
 */
@Slf4j
@Api("活动中心")
@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    /**
     * @Description: 活动列表
     * @Param: [activity]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "活动列表", notes = "活动列表")
    @Cipher
    @PostMapping("/list")
    public Result list(@RequestBody Activity activity) {
        return activityService.list();
    }

}
