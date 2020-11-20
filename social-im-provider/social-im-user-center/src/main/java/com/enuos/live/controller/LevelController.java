package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.Level;
import com.enuos.live.result.Result;
import com.enuos.live.service.LevelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description 等级中心
 * @Author wangyingjie
 * @Date 2020/7/21
 * @Modified
 */
@Slf4j
@Api("等级中心")
@RestController
@RequestMapping("/level")
public class LevelController {

    @Autowired
    private LevelService levelService;

    /**
     * @Description: 等级条
     * @Param: [level]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/21
     */
    @ApiOperation(value = "等级条", notes = "等级条")
    @Cipher
    @PostMapping("/bar")
    public Result bar(@RequestBody Level level) {
        return levelService.bar(level);
    }

    /**
     * @Description: 等级奖励
     * @Param: [level]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/21
     */
    @ApiOperation(value = "等级奖励", notes = "等级奖励")
    @Cipher
    @PostMapping("/reward")
    public Result reward(@RequestBody Level level) {
        return levelService.reward(level);
    }

    /**
     * @Description: 领奖
     * @Param: [level]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/21
     */
    @ApiOperation(value = "领奖", notes = "领奖")
    @Cipher
    @PostMapping("/toGet")
    public Result toGet(@RequestBody Level level) {
        return levelService.toGet(level);
    }

}
