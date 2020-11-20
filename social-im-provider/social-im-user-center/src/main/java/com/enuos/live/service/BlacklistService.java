package com.enuos.live.service;

import com.enuos.live.pojo.Blacklist;
import com.enuos.live.result.Result;

/**
 * @Description 黑名单
 * @Author wangyingjie
 * @Date 2020/7/8
 * @Modified
 */
public interface BlacklistService {

    /** 
     * @Description: 黑名单/屏蔽单（不看某人动态）
     * @Param: [blacklist]
     * @Return: Result 
     * @Author: wangyingjie
     * @Date: 2020/7/8 
     */ 
    Result list(Blacklist blacklist);
    
    /** 
     * @Description: 拉黑/屏蔽某人动态
     * @Param: [blacklist]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/7/8 
     */ 
    Result pullBlack(Blacklist blacklist);

    /**
     * @Description: 解除黑名单/屏蔽
     * @Param: [blacklist]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    Result unBlack(Blacklist blacklist);

}
