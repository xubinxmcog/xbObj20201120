package com.enuos.live.controller;

import com.enuos.live.result.Result;
import com.enuos.live.service.RewardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/24
 * @Modified
 */
@Slf4j
@Api("奖励")
@RestController
@RequestMapping("/reward")
public class RewardController {

    @Autowired
    private RewardService rewardService;

    /**
     * ==========[内部服务]==========
     */

    /**
     * @Description: 奖励
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/24
     */
    @ApiOperation(value = "奖励", notes = "奖励")
    @PostMapping("/handler")
    public Result handler(@RequestBody Map<String, Object> params) {
        try {
            return rewardService.handler(MapUtils.getLong(params, "userId"), (List<Map<String, Object>>) params.get("list"));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(201, e.getMessage());
        }
    }
}
