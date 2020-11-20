package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.pojo.Task;
import com.enuos.live.result.Result;
import com.enuos.live.service.AchievementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Description 成就管理
 * @Author wangyingjie
 * @Date 2020/6/16
 * @Modified
 */
@Slf4j
@Api("成就管理")
@RestController
@RequestMapping("/achievement")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    /**
     * @Description: 已达成成就数
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/10
     */
    @ApiOperation(value = "已达成成就数", notes = "已达成成就数")
    @Cipher
    @PostMapping("/num")
    public Result num(@RequestBody Task task) {
        return achievementService.num(task);
    }

    /**
     * @Description: 成就列表
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/10
     */
    @ApiOperation(value = "成就列表", notes = "成就列表")
    @Cipher
    @PostMapping("/list")
    public Result list(@RequestBody Task task) {
        return achievementService.list(task);
    }

    /**
     * @Description: 领取成就奖励
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/10
     */
    @ApiOperation(value = "领取成就奖励", notes = "领取成就奖励")
    @Cipher
    @PostMapping("/toGet")
    public Result toGet(@RequestBody Task task) {
        try {
            return achievementService.toGet(task);
        } catch (Exception e) {
            return Result.error(ErrorCode.EXCEPTION_CODE, e.getMessage());
        }
    }

    /**
     * ==========[内部服务]==========
     */

    /** 
     * @Description: 成就进度统一处理
     * @Param: [params]
     * {
     * userId:用户ID,
     * code:成就CODE,
     * progress:进度,
     * isReset:是否重置 0 否 1 是
     * }
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/13 
     */ 
    @ApiOperation(value = "成就进度统一处理", notes = "成就进度统一处理")
    @PostMapping("/handler")
    public void handler(@RequestBody Map<String, Object> params) {
        achievementService.handler(params);
    }

    /**
     * @Description: 成就进度统一处理
     * @Param: [params]
     * {
     * userId:用户ID,
     * list:{
     *     code:成就CODE,
     *     progress:进度,
     *     isReset:是否重置 0 否 1 是,
     * },
     * progress:进度
     * }
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/13
     */
    @ApiOperation(value = "成就进度统一处理", notes = "成就进度统一处理")
    @PostMapping("/handlers")
    public void handlers(@RequestBody Map<String, Object> params) {
        achievementService.handlers(params);
    }
}
