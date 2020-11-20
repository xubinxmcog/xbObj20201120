package com.enuos.live.controller;

import com.enuos.live.service.PetsUfoCatcherService;
import com.enuos.live.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName PetsUFOCatcherController
 * @Description: TODO 娃娃机
 * @Author xubin
 * @Date 2020/10/9
 * @Version V2.0
 **/
@RestController
@RequestMapping("/UFOCatcher")
public class PetsUFOCatcherController {

    @Autowired
    private PetsUfoCatcherService petsUfoCatcherService;

    /**
     * @MethodName: catcherPrice
     * @Description: TODO 获取抽奖价格
     * @Param: [catcherId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 14:40 2020/10/9
    **/
    @GetMapping("/catcherPrice")
    public Result catcherPrice(@RequestParam("catcherId") Integer catcherId){

        return petsUfoCatcherService.catcherPrice(catcherId);

    }

    /**
     * @MethodName: getPetsUfoCatcher
     * @Description: TODO 抽奖
     * @Param: [catcherId: 娃娃机编号]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:07 2020/9/29
     **/
    @GetMapping("/getPetsUfoCatcher")
    public Result getPetsUfoCatcher(@RequestParam("catcherId") Integer catcherId,
                                    @RequestParam("drawNum") Integer drawNum,
                                    @RequestParam("userId") Long userId) {

        return Result.success(petsUfoCatcherService.getPetsUfoCatcher(catcherId, drawNum, userId));

    }

    /**
     * @MethodName: previewPrize
     * @Description: TODO 奖品预览
     * @Param: [catcherId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:10 2020/9/30
     **/
    @GetMapping("/previewPrize")
    public Result previewPrize(@RequestParam("catcherId") Integer catcherId) {

        return petsUfoCatcherService.previewPrize(catcherId);

    }

}
