package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.QiuRi;
import com.enuos.live.pojo.Jackpot;
import com.enuos.live.result.Result;
import com.enuos.live.service.QiuRiService;
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
 * @Description 丹枫迎秋[ACT0001]
 * @Author wangyingjie
 * @Date 2020/9/17
 * @Modified
 */
@Slf4j
@Api("丹枫迎秋")
@RestController
@RequestMapping("/qiuri")
public class QiuRiController {

    @Autowired
    private QiuRiService qiuRiService;

    /**
     * @Description: 详情
     * @Param: [qiuRi]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    @ApiOperation(value = "详情", notes = "详情")
    @Cipher
    @PostMapping("/detail")
    public Result detail(@RequestBody QiuRi qiuRi) {
        return qiuRiService.detail(qiuRi);
    }

    /**
     * @Description: 领取任务奖励
     * @Param: [qiuRi]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "领取任务奖励", notes = "领取任务奖励")
    @Cipher
    @PostMapping("/toGet")
    public Result toGet(@RequestBody QiuRi qiuRi) {
        return qiuRiService.toGet(qiuRi);
    }

    /**
     * @Description: 任务处理
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/14
     */
    @ApiOperation(value = "任务处理", notes = "任务处理")
    @Cipher
    @PostMapping("/handler")
    public Result handler(@RequestBody Map<String, Object> params) {
        return qiuRiService.handler(params);
    }

    /**
     * @Description: 丹枫迎秋选牌
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "丹枫迎秋选牌", notes = "丹枫迎秋选牌")
    @Cipher
    @PostMapping("/choose")
    public Result choose(@RequestBody Map<String, Object> params) {
        return qiuRiService.choose(params);
    }

    /**
     * ==========[后台服务]==========
     */

    /**
     * @Description: 初始化丹枫迎秋奖池
     * @Param: [jackpot]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "初始化丹枫迎秋奖池", notes = "初始化丹枫迎秋奖池")
    @Cipher
    @PostMapping("/initJackpot")
    public Result initJackpot(@RequestBody Jackpot jackpot) {
        return qiuRiService.initJackpot(jackpot);
    }

    /**
     * @Description: 获取奖池配置
     * @Param: [jackpot]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "获取奖池配置", notes = "获取奖池配置")
    @Cipher
    @PostMapping("/getJackpot")
    public Result getJackpot(@RequestBody Jackpot jackpot) {
        return qiuRiService.getJackpot(jackpot);
    }

    /**
     * @Description: 任务处理
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/14
     */
    @ApiOperation(value = "任务处理", notes = "任务处理")
    @PostMapping("/open/handler")
    public Result openHandler(@RequestBody Map<String, Object> params) {
        return qiuRiService.handler(params);
    }

}
