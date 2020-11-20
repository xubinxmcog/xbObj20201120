package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.Tip;
import com.enuos.live.result.Result;
import com.enuos.live.service.TipService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 红点提示
 * @Author wangyingjie
 * @Date 2020/8/18
 * @Modified
 */
@Slf4j
@Api("红点提示")
@RestController
@RequestMapping("/tip")
public class TipController {

    @Autowired
    private TipService tipService;

    /**
     * @Description: 是否提示
     * @Param: [tip]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/18
     */
    @ApiOperation(value = "是否提示", notes = "是否提示")
    @Cipher
    @PostMapping("/isTip")
    public Result isTip(@RequestBody Tip tip) {
        return tipService.isTip(tip);
    }

}
