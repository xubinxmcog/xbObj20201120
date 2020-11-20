package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.BSGashapon;
import com.enuos.live.result.Result;
import com.enuos.live.service.BSGashaponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 后台扭蛋服务
 * @Author wangyingjie
 * @Date 2020/6/28
 * @Modified
 */
@Slf4j
@Api("后台扭蛋服务")
@RestController
@RequestMapping("/bs/gashapon")
public class BSGashaponController {

    @Autowired
    private BSGashaponService bsGashaponService;

    /**
     * @Description: 保存扭蛋
     * @Param: [bsGashapon]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "保存扭蛋", notes = "保存扭蛋")
    @Cipher
    @PostMapping("/save")
    public Result save(@RequestBody BSGashapon bsGashapon) {
        return bsGashaponService.save(bsGashapon);
    }

}