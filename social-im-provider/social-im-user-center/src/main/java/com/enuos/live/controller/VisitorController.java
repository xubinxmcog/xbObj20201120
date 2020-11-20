package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.result.Result;
import com.enuos.live.service.VisitorService;
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
 * @Description 访客
 * @Author wangyingjie
 * @Date 2020/7/14
 * @Modified
 */
@Slf4j
@Api("访客记录")
@RestController
@RequestMapping("/visitor")
public class VisitorController {

    @Autowired
    private VisitorService visitorService;

    /**
     * @Description: 保存访客记录
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/15
     */
    @ApiOperation(value = "保存访客记录", notes = "保存访客记录")
    @Cipher
    @PostMapping("/save")
    public Result save(@RequestBody Map<String, Object> params) {
        return visitorService.save(params);
    }

    /**
     * @Description: vip专享访客记录
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/14
     */
    @ApiOperation(value = "vip专享访客记录", notes = "vip专享访客记录")
    @Cipher
    @PostMapping("/list")
    public Result list(@RequestBody Map<String, Object> params) {
        return visitorService.list(params);
    }

}