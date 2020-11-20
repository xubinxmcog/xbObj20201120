package com.enuos.live.service;

import com.enuos.live.pojo.Recommend;
import com.enuos.live.result.Result;

/**
 * @Description 推荐业务处理
 * @Author wangyingjie
 * @Date 17:31 2020/4/16
 * @Modified
 */
public interface RecommendService {

    /**
     * @Description: 根据评分推荐用户
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Result getUserByScore(Long userId);

    /**
     * @Description: 根据等级推荐用户
     * @Param: [recommend]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Result getUserByLevel(Recommend recommend);

}
