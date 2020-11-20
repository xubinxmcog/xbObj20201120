package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.RedPacketsSend;
import com.enuos.live.pojo.RobVO;
import com.enuos.live.result.Result;
import com.enuos.live.service.RedPacketsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName RedPacketsController
 * @Description: TODO 红包
 * @Author xubin
 * @Date 2020/6/10
 * @Version V1.0
 **/
@Api("红包")
@RestController
@RequestMapping("/redPackets")
public class RedPacketsController {

    @Autowired
    private RedPacketsService redPacketsService;

    /**
     * @MethodName: startBroadcast
     * @Description: TODO  发红包
     * @Param: [roomPO, bindingResult]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/6/10
     **/
    @ApiOperation(value = "发红包")
    @Cipher
    @PostMapping("/send")
    public Result send(@Validated @RequestBody RedPacketsSend redPacketsSend, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(201, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return redPacketsService.send(redPacketsSend);
    }
    @ApiOperation(value = "抢红包")
    @Cipher
    @PostMapping("/rob")
    public Result rob(@Validated @RequestBody RobVO robVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(201, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return redPacketsService.rob(robVO);
    }

}
