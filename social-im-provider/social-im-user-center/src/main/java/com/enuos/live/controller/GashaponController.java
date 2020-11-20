package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.pojo.Task;
import com.enuos.live.result.Result;
import com.enuos.live.service.GashaponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 幸运扭蛋
 * @Author wangyingjie
 * @Date 2020/6/19
 * @Modified
 */
@Slf4j
@Api("幸运扭蛋")
@RestController
@RequestMapping("/gashapon")
public class GashaponController {

    @Autowired
    private GashaponService gashaponService;

    /**
     * @Description: 扭蛋数
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "扭蛋数", notes = "扭蛋数")
    @Cipher
    @PostMapping("/num")
    public Result num(@RequestBody Task task) {
        return gashaponService.num(task);
    }

    /**
     * @Description: 抽奖列表
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/19
     */
    @ApiOperation(value = "抽奖列表", notes = "抽奖列表")
    @Cipher
    @PostMapping("/lotteryList")
    public Result lotteryList(@RequestBody Task task) {
        return gashaponService.lotteryList(task);
    }

    /**
     * @Description: 参与
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/22
     */
    @ApiOperation(value = "参与", notes = "参与")
    @Cipher
    @PostMapping("/join")
    public Result join(@RequestBody Task task) {
        try {
            return gashaponService.join(task);
        } catch (Exception e) {
            return Result.error(201, e.getMessage());
        }
    }

    /**
     * @Description: 获取开奖结果
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/24
     */
    @ApiOperation(value = "开奖结果", notes = "开奖结果")
    @Cipher
    @PostMapping("/result")
    public Result result(@RequestBody Task task) {
        return gashaponService.result(task);
    }

    /**
     * @Description: 兑换列表
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/19
     */
    @ApiOperation(value = "兑换列表", notes = "兑换列表")
    @Cipher
    @PostMapping("/exchangeList")
    public Result exchangeList(@RequestBody Task task) {
        return gashaponService.exchangeList(task);
    }

    /**
     * @Description: 兑换
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/24
     */
    @ApiOperation(value = "兑换", notes = "兑换")
    @Cipher
    @PostMapping("/exchange")
    public Result exchange(@RequestBody Task task) {
        try {
            return gashaponService.exchange(task);
        } catch (Exception e) {
            return Result.error(ErrorCode.EXCEPTION_CODE, e.getMessage());
        }
    }

    /**
     * @Description: 中奖记录
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/28
     */
    @ApiOperation(value = "中奖记录", notes = "中奖记录")
    @Cipher
    @PostMapping("/lotteryRecordPage")
    public Result lotteryRecordPage(@RequestBody Task task) {
        return gashaponService.lotteryRecordPage(task);
    }

    /**
     * @Description: 兑换记录
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/28
     */
    @ApiOperation(value = "兑换记录", notes = "兑换记录")
    @Cipher
    @PostMapping("/exchangeRecordPage")
    public Result exchangeRecordPage(@RequestBody Task task) {
        return gashaponService.exchangeRecordPage(task);
    }
}
