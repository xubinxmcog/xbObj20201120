package com.enuos.live.controller;

import cn.hutool.core.map.MapUtil;
import com.enuos.live.annotations.PetsTask;
import com.enuos.live.server.handler.PetsHandler;
import com.enuos.live.service.PetsService;
import com.enuos.live.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName PetsController
 * @Description: TODO 宠物
 * @Author xubin
 * @Date 2020/8/24
 * @Version V2.0
 **/
@RestController
@RequestMapping("/pets")
public class PetsController {

    @Autowired
    private PetsService petsService;

    /**
     * @MethodName: getInfo
     * @Description: TODO 获取用户宠物基础信息
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 14:58 2020/8/24
     **/
    @GetMapping("/getInfo")
    public Result getInfo(@RequestParam("userId") Long userId) {
        return petsService.getInfo(userId);
    }

    /**
     * @MethodName: getOperation
     * @Description: TODO 获取操作
     * @Param: [userId, operation]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:44 2020/8/26
     **/
    @GetMapping("/getOperation")
    public Result getOperation(@RequestParam("userId") Long userId, @RequestParam("operation") Integer operation) {
        return petsService.getOperation(userId, operation);
    }

    /**
     * @MethodName: food
     * @Description: TODO 喂食或玩具
     * @Param: [userId, petCode宠物ID, id物品id, operation操作类型 1:喂食 2:互动]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:28 2020/8/27
     **/
    @PetsTask(task = 1, describe = "喂食")
    @PostMapping("/food")
    public Result foodOrToys(@RequestBody Map<String, Object> params) {
        Long userId = MapUtil.getLong(params, "userId");
        String petCode = MapUtil.getStr(params, "petCode");
        String id = MapUtil.getStr(params, "id");
        return petsService.foodOrToys(userId, null, petCode, id, 1);
    }

    /**
     * @MethodName: toys
     * @Description: TODO 互动
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:45 2020/10/23
    **/
    @PetsTask(task = 2, describe = "互动")
    @PostMapping("/toys")
    public Result toys(@RequestBody Map<String, Object> params) {
        Long userId = MapUtil.getLong(params, "userId");
        String petCode = MapUtil.getStr(params, "petCode");
        String id = MapUtil.getStr(params, "id");
        return petsService.foodOrToys(userId, null, petCode, id, 2);
    }

    @Autowired
    PetsHandler petsHandler;
    @PostMapping("/getPetsInfoAndDressUp")
    public Object getPetsInfoAndDressUp(@RequestBody Map<String, Object> params){
//        return petsService.getPetsInfoAndDressUp(params);
       return petsHandler.upPetsInfo(params);
    }


}
