package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.pojo.Base;
import com.enuos.live.pojo.Task;
import com.enuos.live.result.Result;
import com.enuos.live.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 任务管理
 * @Author wangyingjie
 * @Date 2020/6/10
 * @Modified
 */
@Slf4j
@Api("任务管理")
@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * @Description: 活跃详情
     * @Param: [base]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/10
     */
    @ApiOperation(value = "活跃详情", notes = "活跃详情")
    @Cipher
    @PostMapping("/active")
    public Result active(@RequestBody Base base) {
        return taskService.active(base.userId);
    }

    /**
     * @Description: 任务列表
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/11
     */
    @ApiOperation(value = "任务列表", notes = "任务列表")
    @Cipher
    @PostMapping("/list")
    public Result list(@RequestBody Task task) {
        return taskService.list(task);
    }

    /**
     * @Description: 活跃奖励
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/15
     */
    @ApiOperation(value = "领取活跃度奖励", notes = "领取活跃度奖励")
    @Cipher
    @PostMapping("/activeReward")
    public Result activeReward(@RequestBody Task task) {
        return taskService.activeReward(task);
    }

    /**
     * @Description: 领奖
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/12
     */
    @ApiOperation(value = "领奖", notes = "领奖")
    @Cipher
    @PostMapping("/toGet")
    public Result toGet(@RequestBody Task task) {
        try {
            return taskService.toGet(task);
        } catch (Exception e) {
            return Result.error(ErrorCode.EXCEPTION_CODE, e.getMessage());
        }
    }
}
