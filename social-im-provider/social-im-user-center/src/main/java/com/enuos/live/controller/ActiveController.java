package com.enuos.live.controller;

import com.enuos.live.result.Result;
import com.enuos.live.service.ActiveService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Description 活跃度
 * @Author wangyingjie
 * @Date 2020/6/12
 * @Modified
 */
@Slf4j
@Api("活跃度相关接口")
@RestController
@RequestMapping("/active")
public class ActiveController {

    @Autowired
    private ActiveService activeService;

    /**
     * ==========[内部服务]==========
     */

    /**
     * @Description: 计算活跃度
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "计算活跃度", notes = "计算活跃度")
    @PostMapping("/countActive")
    public Result countActive(@RequestBody Map<String, Object> params) {
        if (MapUtils.isEmpty(params)) {
            return Result.empty();
        }

        return activeService.countActive(MapUtils.getLong(params, "userId"), MapUtils.getInteger(params, "active"));
    }

}
