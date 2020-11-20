package com.enuos.live.service;

import com.enuos.live.result.Result;

import java.util.Map;

public interface PetsTaskService {

    /**
     * @MethodName: getTaskList
     * @Description: TODO 任务列表
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:45 2020/10/22
     **/
    Result getTaskList(Map<String, Object> params);

    /**
     * @MethodName: receiveReward
     * @Description: TODO 领取任务奖励
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 13:19 2020/10/23
    **/
    Result receiveReward(Map<String, Object> params);


}
