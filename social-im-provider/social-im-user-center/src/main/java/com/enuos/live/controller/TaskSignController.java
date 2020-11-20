package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.pojo.TaskSign;
import com.enuos.live.result.Result;
import com.enuos.live.service.TaskSignService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 签到任务
 * @Author wangyingjie
 * @Date 9:30 2020/4/9
 * @Modified
 */
@Slf4j
@Api("签到任务")
@RestController
@RequestMapping("/task/sign")
public class TaskSignController {

    @Autowired
    private TaskSignService taskSignService;

    /**
     * @Description: 签到详情
     * @Param: [taskSign]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/5/21
     */
    @ApiOperation(value = "签到详情", notes = "签到详情")
    @Cipher
    @PostMapping("/detail")
    public Result detail(@RequestBody TaskSign taskSign) {
        return taskSignService.detail(taskSign);
    }

    /**
     * @Description: 每日签到/补签
     * @Param: [taskSign]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    @ApiOperation(value = "每日签到/补签", notes = "每日签到/补签")
    @Cipher
    @PostMapping("/daySign")
    public Result daySign(@RequestBody TaskSign taskSign) {
        try {
            return taskSignService.daySign(taskSign);
        } catch (Exception e) {
            return Result.error(ErrorCode.EXCEPTION_CODE, e.getMessage());
        }
    }

    /**
     * @Description: 去补签
     * @Param: [taskSign]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    @ApiOperation(value = "去补签", notes = "去补签")
    @Cipher
    @PostMapping("/toBackSign")
    public Result toBackSign(@RequestBody TaskSign taskSign) {
        return taskSignService.toBackSign(taskSign);
    }

    /**
     * @Description: 领取奖励
     * @Param: [taskSign]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    @ApiOperation(value = "领取奖励", notes = "领取奖励")
    @Cipher
    @PostMapping("/toGet")
    public Result toGet(@RequestBody TaskSign taskSign) {
        try {
            return taskSignService.toGet(taskSign);
        } catch (Exception e) {
            return Result.error(ErrorCode.EXCEPTION_CODE, e.getMessage());
        }
    }
}
