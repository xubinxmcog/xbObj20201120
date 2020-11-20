package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.Account;
import com.enuos.live.pojo.Base;
import com.enuos.live.pojo.User;
import com.enuos.live.result.Result;
import com.enuos.live.service.CommonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Description 通用
 * @Author wangyingjie
 * @Date 12:55 2020/4/14
 * @Modified
 */
@Slf4j
@Api("通用接口")
@RestController
@RequestMapping("/common")
public class CommonController {

    @Autowired
    private CommonService commonService;

    /**
     * @Description: label
     * @Param: [base]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "label", notes = "label")
    @Cipher
    @PostMapping("/label")
    public Result label(@RequestBody Base base) {
        return commonService.label();
    }

    /**
     * @Description: 刷新登陆
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/28
     */
    @ApiOperation(value = "刷新登陆", notes = "刷新登陆")
    @Cipher
    @PostMapping("/refreshLogin")
    public Result refreshLogin(@RequestBody Account account) {
        return commonService.refreshLogin(account);
    }

    /**
     * @Description: 刷新位置
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "刷新位置", notes = "刷新位置")
    @Cipher
    @PostMapping("/refreshPoint")
    public Result refreshPoint(@RequestBody Account account) {
        return commonService.refreshPoint(account);
    }

    /**
     * @Description: 刷新用户状态
     * @Param: [user]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "刷新用户状态", notes = "刷新用户状态")
    @Cipher
    @PostMapping("/refreshOnLineStatus")
    public Result refreshOnLineStatus(@RequestBody User user) {
        return Result.success(commonService.refreshOnLineStatus(user));
    }

    /**
     * ==========[内部服务]==========
     */

    /**
     * @Description: 获取用户信息
     * @Param: [userId, keys]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "获取用户信息", notes = "获取用户信息")
    @GetMapping("/getUserBase")
    public Map<String, Object> getUserBase(@RequestParam("userId") Long userId, @RequestParam("keys") String...keys) {
        return commonService.getUserBase(userId, keys);
    }

    /**
     * @Description: 获取二维码
     * @Param: [request, response]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @ApiOperation(value = "刷新用户状态", notes = "刷新用户状态")
    @GetMapping("/getUserQRCode")
    public void getUserQRCode(HttpServletRequest request, HttpServletResponse response) {
        commonService.getUserQRCode(request, response);
    }

}
