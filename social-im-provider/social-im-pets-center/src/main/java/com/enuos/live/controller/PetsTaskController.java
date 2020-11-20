package com.enuos.live.controller;

import com.enuos.live.service.PetsTaskService;
import com.enuos.live.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName PetsTaskController
 * @Description: TODO 任务中心
 * @Author xubin
 * @Date 2020/10/22
 * @Version V2.0
 **/
@RestController
@RequestMapping("/petsTask")
public class PetsTaskController {

    @Autowired
    private PetsTaskService petsTaskService;

    /**
     * @MethodName: getTaskList
     * @Description: TODO 任务列表
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:44 2020/10/22
     **/
    @PostMapping("/list")
    public Result getTaskList(@RequestBody Map<String, Object> params) {
        return petsTaskService.getTaskList(params);
    }

    /**
     * @MethodName: receiveReward
     * @Description: TODO 领取任务奖励
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 13:20 2020/10/23
    **/
    @PostMapping("/receiveReward")
    public Result receiveReward(@RequestBody Map<String, Object> params) {
        return petsTaskService.receiveReward(params);
    }
}
