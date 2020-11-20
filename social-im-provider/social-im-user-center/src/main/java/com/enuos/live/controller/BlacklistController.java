package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.Blacklist;
import com.enuos.live.result.Result;
import com.enuos.live.service.BlacklistService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 黑名单
 * @Author wangyingjie
 * @Date 2020/7/8
 * @Modified
 */
@Slf4j
@Api("黑名单")
@RestController
@RequestMapping("/blacklist")
public class BlacklistController {

    @Autowired
    private BlacklistService blacklistService;

    /**
     * @Description: 黑名单/屏蔽单（不看某人动态）
     * @Param: [blacklist]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "黑名单/屏蔽单（不看某人动态）", notes = "黑名单/屏蔽单（不看某人动态）")
    @Cipher
    @PostMapping("/list")
    public Result list(@RequestBody Blacklist blacklist) {
        return blacklistService.list(blacklist);
    }

    /**
     * @Description: 拉黑/屏蔽某人动态
     * @Param: [blacklist]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "拉黑/屏蔽某人动态", notes = "拉黑/屏蔽某人动态")
    @Cipher
    @PostMapping("/pullBlack")
    public Result pullBlack(@RequestBody Blacklist blacklist) {
        return blacklistService.pullBlack(blacklist);
    }

    /**
     * @Description: 解除黑名单/屏蔽
     * @Param: [blacklist]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "解除黑名单/屏蔽", notes = "解除黑名单/屏蔽")
    @Cipher
    @PostMapping("/unBlack")
    public Result unBlack(@RequestBody Blacklist blacklist) {
        return blacklistService.unBlack(blacklist);
    }

}
