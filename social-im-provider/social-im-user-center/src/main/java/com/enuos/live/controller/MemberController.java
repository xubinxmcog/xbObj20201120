package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.pojo.Base;
import com.enuos.live.result.Result;
import com.enuos.live.service.MemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Description 会员中心
 * @Author wangyingjie
 * @Date 2020/6/29
 * @Modified
 */
@Slf4j
@Api("会员中心")
@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    /**
     * @Description: 会员中心
     * @Param: [base]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/30
     */
    @ApiOperation(value = "会员中心", notes = "会员中心")
    @Cipher
    @PostMapping("/center")
    public Result center(@RequestBody Base base) {
        return memberService.center(base.userId);
    }

    /**
     * @Description: 充值套餐列表
     * @Param: [base]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/30
     */
    @ApiOperation(value = "充值套餐列表", notes = "充值套餐列表")
    @Cipher
    @PostMapping("/rechargePackage")
    public Result rechargePackage(@RequestBody Base base) {
        return memberService.rechargePackage();
    }

    /**
     * ==========[内部服务]==========
     */

    /**
     * @Description: 处理会员装饰
     * @Param: [userId, vip]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/31
     */
    @ApiOperation(value = "处理会员装饰", notes = "处理会员装饰")
    @GetMapping("/decorationHandler")
    public void decorationHandler(@RequestParam("userId") Long userId, @RequestParam("vip") Integer vip) {
        memberService.decorationHandler(userId, vip);
    }

    /**
     * @Description: 添加成长值
     * @Param: [userId, growth]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/17
     */
    @ApiOperation(value = "添加成长值", notes = "添加成长值")
    @GetMapping("/addGrowth")
    public Result addGrowth(@RequestParam("userId") Long userId, @RequestParam("growth") Integer growth) {
        return memberService.addGrowth(userId, growth);
    }

    /**
     * @Description: 充值结果
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/30
     */
    @ApiOperation(value = "充值结果", notes = "充值结果")
    @PostMapping("/rechargeResult")
    public Result rechargeResult(@RequestBody Map<String, Object> params) {
        return memberService.rechargeResult(params);
    }
    
    /**
     * @Description: 是否会员
     * @Param: [userId]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    @ApiOperation(value = "是否会员", notes = "是否会员")
    @GetMapping("/isMember")
    public Integer isMember(@RequestParam("userId") Long userId) {
        return memberService.isMember(userId);
    }
}
