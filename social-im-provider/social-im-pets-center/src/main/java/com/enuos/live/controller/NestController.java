package com.enuos.live.controller;

import com.enuos.live.pojo.PetsInfo;
import com.enuos.live.service.NestService;
import com.enuos.live.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName CaveolaeController
 * @Description: TODO 小窝
 * @Author xubin
 * @Date 2020/10/9
 * @Version V2.0
 **/
@RestController
@RequestMapping("/nest")
public class NestController {

    @Autowired
    private NestService nestService;

    /**
     * @MethodName: me
     * @Description: TODO 我的小窝
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:53 2020/10/9
     **/
    @GetMapping("/me")
    public Result me(@RequestParam("userId") Long userId) {
        return nestService.me(userId);
    }

    /**
     * @MethodName: me
     * @Description: TODO 我的宠物列表
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:53 2020/10/9
     **/
    @GetMapping("/list")
    public Result list(@RequestParam("userId") Long userId) {
        return nestService.list(userId);
    }


    /**
     * @MethodName: upPetsInfo
     * @Description: TODO 修改宠物信息
     * @Param: [petsInfo]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 10:04 2020/10/12
     **/
    @PostMapping("/upPetsInfo")
    public Result upPetsInfo(@RequestBody PetsInfo petsInfo) {
        return nestService.upPetsInfo(petsInfo);

    }

    /**
     * @MethodName: unlock
     * @Description: TODO 解锁小窝
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 11:20 2020/10/13
     **/
    @PostMapping("/unlock")
    public Result unlock(@RequestBody Map<String, Object> params) {
        return nestService.unlock(params);
    }

    /**
     * @MethodName: upDown
     * @Description: TODO 宠物上下窝
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:08 2020/10/13
     **/
    @PostMapping("/upDown")
    public Result upDown(@RequestBody Map<String, Object> params) {
        return nestService.upDown(params);
    }

    /**
     * @MethodName: pieces
     * @Description: TODO 获取用户宠物碎片列表
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 11:05 2020/10/14
     **/
    @GetMapping("/pieces")
    public Result pieces(@RequestParam("userId") Long userId) {
        return nestService.pieces(userId);
    }

    /**
     * @MethodName: petsExchange
     * @Description: TODO 宠物兑换
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 11:21 2020/10/14
     **/
    @PostMapping("/petsExchange")
    public Result petsExchange(@RequestBody Map<String, Object> params) {
        return nestService.petsExchange(params);
    }
}
