package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.Activity;
import com.enuos.live.result.Result;
import com.enuos.live.service.ShuangJieService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 双节豪礼[ACT0004]
 * @Author wangyingjie
 * @Date 2020/9/17
 * @Modified
 */
@Slf4j
@Api("双节豪礼")
@RestController
@RequestMapping("/shuangjie")
public class ShuangJieController {

    @Autowired
    private ShuangJieService shuangJieService;

    /**
     * @Description: 详情
     * @Param: [activity]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/22
     */
    @ApiOperation(value = "详情", notes = "详情")
    @Cipher
    @PostMapping("/detail")
    public Result detail(@RequestBody Activity activity) {
        return null;
    }
}
