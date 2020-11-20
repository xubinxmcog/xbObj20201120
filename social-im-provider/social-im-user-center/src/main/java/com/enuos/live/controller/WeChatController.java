package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.pojo.WeChat;
import com.enuos.live.result.Result;
import com.enuos.live.service.WeChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 微信相关
 * @Author wangyingjie
 * @Date 2020/11/6
 * @Modified
 */
@Slf4j
@Api("微信相关")
@RestController
@RequestMapping("/weChat")
public class WeChatController {

    @Autowired
    private WeChatService weChatService;

    /** 
     * @Description: 绑定微信支付
     * @Param: [weChat, bindingResult] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/11/6 
     */ 
    @ApiOperation(value = "绑定微信支付", notes = "绑定微信支付")
    @Cipher
    @PostMapping("/bindPay")
    public Result bindPay(@Validated @RequestBody WeChat weChat, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(ErrorCode.EXCEPTION_CODE, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return weChatService.bindPay(weChat);
    }

}
