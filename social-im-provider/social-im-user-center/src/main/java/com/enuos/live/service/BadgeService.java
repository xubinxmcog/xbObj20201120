package com.enuos.live.service;

import com.enuos.live.pojo.Badge;
import com.enuos.live.pojo.User;
import com.enuos.live.result.Result;

import java.util.List;
import java.util.Map;

/**
 * @Description 用户徽章
 * @Author wangyingjie
 * @Date 2020/7/16
 * @Modified
 */
public interface BadgeService {

    /**
     * @Description: 获取主页徽章
     * @Param: [user]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/7
     */
    Result wearBadgeList(User user);

    /**
     * @Description: 获得的徽章
     * @Param: [badge]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/18
     */
    Result num(Badge badge);

    /**
     * @Description: 用户徽章墙
     * @Param: [badge]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    Result list(Badge badge);
    
    /** 
     * @Description: 佩戴
     * @Param: [badge] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/9/17 
     */ 
    Result wear(Badge badge);

    /**
     * @Description: 批量保存
     * @Param: [userId, badgeList]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Result batchSave(Long userId, List<Map<String, Object>> badgeList);

}
