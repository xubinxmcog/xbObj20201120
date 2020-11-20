package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.Recommend;
import com.enuos.live.result.Result;
import com.enuos.live.service.RecommendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 推荐
 * @Author wangyingjie
 * @Date 17:27 2020/4/16
 * @Modified
 */
@Slf4j
@Api("推荐")
@RestController
@RequestMapping("/recommend")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    /**
     * @Description: 获取推荐的用户
     * @Param: [recommend]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "获取推荐的用户", notes = "获取推荐的用户")
    @Cipher
    @PostMapping("/getUser")
    public Result getUser(@RequestBody Recommend recommend) {
        return recommendService.getUserByLevel(recommend);
    }

}

