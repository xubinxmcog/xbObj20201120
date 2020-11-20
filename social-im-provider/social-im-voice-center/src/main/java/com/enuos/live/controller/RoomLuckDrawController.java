package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.result.Result;
import com.enuos.live.service.RoomLuckDrawService;
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
 * @ClassName RoomLuckDrawController
 * @Description: TODO 幸运抽奖
 * @Author xubin
 * @Date 2020/6/16
 * @Version V1.0
 **/
@Api("幸运抽奖")
@Slf4j
@RestController
@RequestMapping("/luckDraw")
public class RoomLuckDrawController {

    @Autowired
    private RoomLuckDrawService roomLuckDrawService;

    @ApiOperation("开始抽奖")
    @Cipher
    @PostMapping("/start")
    public Result start(@RequestBody Map<String, Object> params) {
        log.info("参数=[{}]", params);
        return roomLuckDrawService.start(params);

    }




}
