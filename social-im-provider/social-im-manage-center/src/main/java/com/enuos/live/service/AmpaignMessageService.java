package com.enuos.live.service;

import com.enuos.live.result.Result;

public interface AmpaignMessageService {

    /**
     * @MethodName: selectByList
     * @Description: TODO 获取活动
     * @Param: [pageNum, pageSize]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 14:38 2020/9/2
    **/
    Result selectByList(Integer pageNum, Integer pageSize);

    /**
     * @MethodName: getBusinessEntry
     * @Description: TODO 获取首页功能入口
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 14:38 2020/9/2
    **/
    Result getBusinessEntry();
}
