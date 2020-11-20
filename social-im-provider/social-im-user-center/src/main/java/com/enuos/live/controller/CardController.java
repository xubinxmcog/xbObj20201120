package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.Card;
import com.enuos.live.result.Result;
import com.enuos.live.service.CardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description 证件认证
 * @Author wangyingjie
 * @Date 14:58 2020/5/8
 * @Modified
 */
@Slf4j
@Api("身份验证")
@RestController
@RequestMapping("/card")
public class CardController {

    @Autowired
    private CardService cardService;

    /**
     * @Description: 二要素核验
     * @Param: [card]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "二要素核验", notes = "二要素核验")
    @Cipher
    @PostMapping("/idCard")
    public Result idCard(@RequestBody Card card) {
        return cardService.idCard(card);
    }

    /**
     * ==========[内部服务]==========
     */

    /**
     * @Description: 是否认证
     * @Param: [userId]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "是否认证", notes = "是否认证")
    @GetMapping("/open/isAuthentication")
    public Integer isAuthentication(Long userId) {
        return cardService.isAuthentication(userId);
    }

}
