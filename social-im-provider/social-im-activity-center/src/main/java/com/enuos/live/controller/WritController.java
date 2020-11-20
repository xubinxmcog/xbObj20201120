package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.Writ;
import com.enuos.live.result.Result;
import com.enuos.live.service.WritService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description 乐享令状
 * @Author wangyingjie
 * @Date 2020/10/10
 * @Modified
 */
@Slf4j
@Api("乐享令状")
@RestController
@RequestMapping("/writ")
public class WritController {

    @Autowired
    private WritService writService;

    /**
     * @Description: 等级
     * @Param: [writ]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/26
     */
    @ApiOperation(value = "等级", notes = "等级")
    @Cipher
    @PostMapping("/level")
    public Result level(@RequestBody Writ writ) {
        return writService.level(writ);
    }

    /**
     * @Description: 列表
     * @Param: [writ:[type 1 奖励 2 任务 3 兑换 4 排行]]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/13
     */
    @ApiOperation(value = "列表", notes = "列表")
    @Cipher
    @PostMapping("/list")
    public Result list(@RequestBody Writ writ) {
        return writService.list(writ);
    }

    /**
     * @Description: 价格
     * @Param: [writ:[type 1 等级价格 2 进阶价格]]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/26
     */
    @ApiOperation(value = "价格", notes = "价格")
    @Cipher
    @PostMapping("/price")
    public Result price(@RequestBody Writ writ) {
        return writService.price(writ);
    }

    /**
     * @Description: 购买
     * @Param: [writ:[type 1 购买等级 2 解锁进阶]]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/26
     */
    @ApiOperation(value = "购买", notes = "购买")
    @Cipher
    @PostMapping("/buy")
    public Result buy(@RequestBody Writ writ) {
        return writService.buy(writ);
    }

    /**
     * @Description: 兑换
     * @Param: [writ]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/14
     */
    @ApiOperation(value = "兑换", notes = "兑换")
    @Cipher
    @PostMapping("/exchange")
    public Result exchange(@RequestBody Writ writ) {
        return writService.exchange(writ);
    }

    /** 
     * @Description: 领取等级奖励
     * @Param: [writ] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/10/27 
     */ 
    @ApiOperation(value = "领取等级奖励", notes = "领取等级奖励")
    @Cipher
    @PostMapping("/toGet")
    public Result toGet(@RequestBody Writ writ) {
        return writService.toGet(writ);
    }


    /**
     * ==========[内部调用]==========
     */


    /**
     * @Description: 日常任务领奖
     * @Param: [userId, templateCode]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/10/29
     */
    @ApiOperation(value = "日常任务领奖", notes = "日常任务领奖")
    @GetMapping("/dailyTask")
    public void dailyTask(@RequestParam("userId") Long userId, @RequestParam("templateCode") String templateCode) {
        writService.dailyTask(userId, templateCode);
    }
}
