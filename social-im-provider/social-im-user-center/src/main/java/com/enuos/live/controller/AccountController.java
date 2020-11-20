package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.Account;
import com.enuos.live.pojo.BindAccount;
import com.enuos.live.pojo.Logout;
import com.enuos.live.result.Result;
import com.enuos.live.service.AccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @Description 账户接口
 * @Author wangyingjie
 * @Date 2020/9/10
 * @Modified
 */
@Slf4j
@Api("账户接口")
@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    /**
     * @Description: 账户注册
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/10
     */
    @ApiOperation(value = "账户注册", notes = "账户注册")
    @Cipher
    @PostMapping("/regist")
    public Result regist(@Valid @RequestBody Account account, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(201, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return accountService.regist(account);
    }

    /**
     * @Description: 账户绑定列表
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/10
     */
    @ApiOperation(value = "账户绑定列表", notes = "账户绑定列表")
    @Cipher
    @PostMapping("/bindList")
    public Result bindList(@RequestBody Account account) {
        return accountService.bindList(account);
    }

    /**
     * @Description: 账户绑定
     * @Param: [bindAccount]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/10
     */
    @ApiOperation(value = "账户绑定", notes = "账户绑定")
    @Cipher
    @PostMapping("/bind")
    public Result bind(@RequestBody BindAccount bindAccount) {
        return accountService.bind(bindAccount);
    }

    /**
     * @Description: 账户注销
     * @Param: [logout]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/10
     */
    @ApiOperation(value = "账户注销", notes = "账户注销")
    @Cipher
    @PostMapping("/logout")
    public Result logout(@RequestBody Logout logout) {
        return accountService.logout(logout);
    }

}