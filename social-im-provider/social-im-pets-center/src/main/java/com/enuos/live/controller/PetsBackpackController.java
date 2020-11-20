package com.enuos.live.controller;

import com.enuos.live.service.PetsBackpackService;
import com.enuos.live.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName PetsBackpackController
 * @Description: TODO 背包
 * @Author xubin
 * @Date 2020/9/2
 * @Version V2.0
 **/
@RestController
@RequestMapping("/pets/backpack")
public class PetsBackpackController {

    @Autowired
    private PetsBackpackService petsBackpackService;

    /**
     * @MethodName: queryBackpack
     * @Description: TODO 查看背包
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 11:00 2020/9/2
     **/
    @GetMapping("/queryBackpack")
    public Result queryBackpack(@RequestParam("userId") Long userId,
                                @RequestParam(name = "categoryId", required = false) Integer categoryId) {
        return petsBackpackService.queryBackpack(userId, categoryId);
    }

    /**
     * @MethodName: dressUp
     * @Description: TODO 修改宠物装扮
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 13:46 2020/10/26
    **/
    @PostMapping("/dressUp")
    public Result dressUp(@RequestBody Map<String, Object> params) {
        return petsBackpackService.dressUp(params);
    }

    /**
     * @MethodName: dressUp
     * @Description: TODO 获取宠物装扮
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 13:46 2020/10/26
     **/
    @PostMapping("/getDressUp")
    public Result getDressUp(@RequestBody Map<String, Object> params) {
        return petsBackpackService.getDressUp(params);
    }
}
