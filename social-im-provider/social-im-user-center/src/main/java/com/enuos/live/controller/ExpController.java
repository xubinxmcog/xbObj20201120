package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.AccountAttach;
import com.enuos.live.result.Result;
import com.enuos.live.service.ExpService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/5/18
 * @Modified
 */
@Slf4j
@Api("经验接口")
@RestController
@RequestMapping("/exp")
public class ExpController {

    @Autowired
    private ExpService expService;

    /**
     * @Description: 游戏今日已得经验
     * @Param: [accountAttach]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "游戏今日已得经验", notes = "游戏今日已得经验")
    @Cipher
    @PostMapping("/game/today")
    public Result gameToday(@RequestBody AccountAttach accountAttach) {
        return expService.gameToday(accountAttach);
    }

    /**
     * ==========[内部服务]==========
     */

    /**
     * @Description: 计算经验值
     * @Param: [accountAttach]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "计算经验值", notes = "计算经验值")
    @PostMapping("/game/handler")
    public Result gameHandler(@RequestBody AccountAttach accountAttach) {
        return expService.gameHandler(accountAttach);
    }

    /**
     * @Description: 计算经验值
     * @Param: [accountAttach]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "计算经验值", notes = "计算经验值")
    @PostMapping("/countExp")
    public Result countExp(@RequestBody AccountAttach accountAttach) {
        return expService.countExp(accountAttach.userId, accountAttach.getExperience());
    }

}
