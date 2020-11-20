package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.JinQiu;
import com.enuos.live.result.Result;
import com.enuos.live.service.JinQiuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Description 金秋送福[ACT0005]
 * @Author wangyingjie
 * @Date 2020/9/23
 * @Modified
 */
@Slf4j
@Api("金秋送福")
@RestController
@RequestMapping("/jinqiu")
public class JinQiuController {

    @Autowired
    private JinQiuService jinQiuService;

    /**
     * @Description: 详情
     * @Param: [jinQiu]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/25
     */
    @ApiOperation(value = "详情", notes = "详情")
    @Cipher
    @PostMapping("/detail")
    public Result detail(@RequestBody JinQiu jinQiu) {
        return jinQiuService.detail(jinQiu);
    }

    /**
     * @Description: 领奖
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/25
     */
    @ApiOperation(value = "领奖", notes = "领奖")
    @Cipher
    @PostMapping("/toGet")
    public Result toGet(@RequestBody Map<String, Object> params) {
        try {
            return jinQiuService.toGet(params);
        } catch (Exception e) {
            return Result.error(201, e.getMessage());
        }
    }

}
