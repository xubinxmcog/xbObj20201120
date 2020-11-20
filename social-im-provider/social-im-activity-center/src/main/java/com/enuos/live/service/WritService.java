package com.enuos.live.service;

import com.enuos.live.pojo.Writ;
import com.enuos.live.result.Result;

/**
 * @Description 乐享令状
 * @Author wangyingjie
 * @Date 2020/10/10
 * @Modified
 */
public interface WritService {

    /**
     * @Description: 等级
     * @Param: [writ]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/26
     */
    Result level(Writ writ);

    /** 
     * @Description: 列表
     * @Param: [writ:[type 1 奖励 2 任务 3 兑换 4 排行]]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/10/13 
     */ 
    Result list(Writ writ);

    /**
     * @Description: 价格
     * @Param: [writ:[type 1 等级价格 2 进阶价格]]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/26
     */
    Result price(Writ writ);

    /**
     * @Description: 购买
     * @Param: [writ:[type 1 购买等级 2 解锁进阶]]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/26
     */
    Result buy(Writ writ);

    /** 
     * @Description: 兑换
     * @Param: [writ] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/10/14 
     */ 
    Result exchange(Writ writ);
    
    /** 
     * @Description: 领取等级奖励
     * @Param: [writ] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/10/27 
     */ 
    Result toGet(Writ writ);


    /**
     * ==========[内部调用]==========
     */
    
    
    /** 
     * @Description: 日常任务领奖 
     * @Param: [userId, templateCode]
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/10/29 
     */ 
    void dailyTask(Long userId, String templateCode);
}
