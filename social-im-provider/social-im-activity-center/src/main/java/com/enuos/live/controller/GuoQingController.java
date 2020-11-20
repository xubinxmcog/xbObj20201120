package com.enuos.live.controller;

import com.enuos.live.pojo.GuoQing;
import com.enuos.live.result.Result;
import com.enuos.live.service.GuoQingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 欢度国庆[ACT0002]
 * @Author wangyingjie
 * @Date 2020/9/17
 * @Modified
 */
@Slf4j
@Api("欢度国庆")
@RestController
@RequestMapping("/guoqing")
public class GuoQingController {

    @Autowired
    private GuoQingService guoQingService;

    /**
     * @Description: 详情
     * @Param: [guoQing]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    @ApiOperation(value = "详情", notes = "详情")
    @PostMapping("/detail")
    public Result detail(@RequestBody GuoQing guoQing) {
        return guoQingService.detail(guoQing);
    }
}
