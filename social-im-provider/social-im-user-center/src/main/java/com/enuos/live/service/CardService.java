package com.enuos.live.service;

import com.enuos.live.pojo.Card;
import com.enuos.live.result.Result;

/**
 * @Description
 * @Author wangyingjie
 * @Date 15:18 2020/5/8
 * @Modified
 */
public interface CardService {

    /**
     * @Description: 身份证二要素核验
     * @Param: [card]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Result idCard(Card card);

    /**
     * @Description: 是否认证
     * @Param: [userId]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Integer isAuthentication(Long userId);

}
