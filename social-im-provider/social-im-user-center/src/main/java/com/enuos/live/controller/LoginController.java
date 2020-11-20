package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.Account;
import com.enuos.live.result.Result;
import com.enuos.live.service.LoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 登陆&注册
 * @Author wangyingjie
 * @Date 16:04 2020/3/31
 * @Modified
 */
@Slf4j
@Api("用户登陆&注册")
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    /**
     * @Description: 发送短信
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @ApiOperation(value = "发送短信", notes = "发送短信")
    @Cipher
    @PostMapping("/sendSMS")
    public Result sendSMS(@RequestBody Account account) {
        return loginService.sendSMS(account);
    }

    /**
     * @Description: 短信验证登陆
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @ApiOperation(value = "短信验证登陆", notes = "短信验证登陆")
    @Cipher
    @PostMapping("/loginWithSMS")
    public Result loginWithSMS(@RequestBody Account account) {
        return loginService.loginWithSMS(account);
    }

    /**
     * @Description: 微信登陆
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @ApiOperation(value = "微信登陆", notes = "微信登陆")
    @Cipher
    @PostMapping("/loginWithWeChat")
    public Result loginWithWeChat(@RequestBody Account account) {
        return loginService.loginWithWeChat(account);
    }

    /**
     * @Description: QQ登陆
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @ApiOperation(value = "QQ登陆", notes = "QQ登陆")
    @Cipher
    @PostMapping("/loginWithQQ")
    public Result loginWithQQ(@RequestBody Account account) {
        return loginService.loginWithQQ(account);
    }

    /**
     * @Description: apple登陆
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/1
     */
    @ApiOperation(value = "apple登陆", notes = "apple登陆")
    @Cipher
    @PostMapping("/loginWithApple")
    public Result loginWithApple(@RequestBody Account account) {
        return loginService.loginWithApple(account);
    }

    /**
     * @Description: web登陆
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/1
     */
    @ApiOperation(value = "web登陆", notes = "web登陆")
    @Cipher
    @PostMapping("/loginForWebWithSMS")
    public Result loginForWebWithSMS(@RequestBody Account account) {
        return loginService.loginForWebWithSMS(account);
    }

}