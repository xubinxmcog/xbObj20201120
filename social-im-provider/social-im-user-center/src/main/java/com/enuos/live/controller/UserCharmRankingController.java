package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.result.Result;
import com.enuos.live.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName UserCharmRankingController
 * @Description: TODO 用户魅力,贡献排行榜
 * @Author xubin
 * @Date 2020/9/2
 * @Version V2.0
 **/
@RestController
@RequestMapping("/user")
public class UserCharmRankingController {

    @Autowired
    private UserService userService;

    @ApiOperation(value = " 魅力: 礼物榜 or 真爱榜")
    @Cipher
    @PostMapping("/charmRanking")
    public Result charmRanking(@RequestBody Map<String, Object> params) {
        Long targetUserId = MapUtils.getLong(params, "targetUserId");
        Integer type = MapUtils.getInteger(params, "type");
        Integer pageSize = MapUtils.getInteger(params, "pageSize");

        return userService.charmRanking(targetUserId, type, pageSize);
    }

    /**
     * @MethodName: charmDedicate
     * @Description: TODO 魅力,守护排行榜
     * @Param: [type 1:魅力 2:守护, charmType 1:昨天 2:今天 3:荣誉, pageSize:多少条]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:43 2020/9/2
     **/
    @ApiOperation(value = "魅力,守护排行榜")
    @Cipher
    @PostMapping("/charmDedicate")
    public Result charmDedicate(@RequestBody Map<String, Object> params) {
        Integer type = MapUtils.getInteger(params, "type");
        Integer charmType = MapUtils.getInteger(params, "charmType");
        Integer pageSize = MapUtils.getInteger(params, "pageSize");

        return userService.charmDedicate(type, charmType, pageSize);
    }
}
