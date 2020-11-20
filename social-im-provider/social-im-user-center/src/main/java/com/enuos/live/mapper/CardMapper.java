package com.enuos.live.mapper;

import com.enuos.live.pojo.Card;
import org.apache.ibatis.annotations.Param;

/**
 * @Description
 * @Author wangyingjie
 * @Date 16:50 2020/5/8
 * @Modified
 */
public interface CardMapper {

    /**
     * @Description: 用户是否绑定或该证件号已使用
     * @Param: [card]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Integer isExists(Card card);
    
    /**
     * @Description: 获取绑定次数
     * @Param: [card]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Integer getBandCount(Card card);

    /**
     * @Description: 保存
     * @Param: [card]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    int save(Card card);

    /**
     * @Description: 是否认证
     * @Param: [userId]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Integer isAuthentication(@Param("userId") Long userId);

}
