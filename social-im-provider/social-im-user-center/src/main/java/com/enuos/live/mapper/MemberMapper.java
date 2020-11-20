package com.enuos.live.mapper;

import com.enuos.live.pojo.Member;
import com.enuos.live.pojo.MemberInterest;

import java.util.List;
import java.util.Map;

/**
 * @Description 会员中心
 * @Author wangyingjie
 * @Date 2020/6/29
 * @Modified
 */
public interface MemberMapper {

    /**
     * @Description: 会员说明
     * @Param: []
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/7/30
     */
    String ship();

    /**
     * @Description: vip配置
     * @Param: []
     * @Return: List<Map<String,Object>>
     * @Author: wangyingjie
     * @Date: 2020/6/30
     */
    List<Map<String, Object>> getVipConfig();

    /** 
     * @Description: 会员信息
     * @Param: [userId] 
     * @Return: com.enuos.live.pojo.Member 
     * @Author: wangyingjie
     * @Date: 2020/6/30 
     */ 
    Member getMember(Long userId);

    /** 
     * @Description: 会员信息 
     * @Param: [userId] 
     * @Return: com.enuos.live.pojo.Member 
     * @Author: wangyingjie
     * @Date: 2020/7/30 
     */ 
    Member getSimpleMember(Long userId);
    
    /** 
     * @Description: 更新会员成长值及等级
     * @Param: [member] 
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/7/20 
     */ 
    int updateMember(Member member);

    /** 
     * @Description: 更新会员到期时间
     * @Param: [member] 
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/7/1 
     */ 
    int updateExpirationTime(Member member);
    
    /** 
     * @Description: 权益列表
     * @Param: [] 
     * @Return: java.util.List<com.enuos.live.pojo.MemberInterest> 
     * @Author: wangyingjie
     * @Date: 2020/6/30 
     */ 
    List<MemberInterest> getMemberInterestList();
    
    /** 
     * @Description: 充值套餐
     * @Param: [] 
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>> 
     * @Author: wangyingjie
     * @Date: 2020/6/30 
     */ 
    List<Map<String, Object>> getRechargePackage();

    /** 
     * @Description: 充值套餐
     * @Param: [productId]
     * @Return: java.util.Map<java.lang.String,java.lang.Object> 
     * @Author: wangyingjie
     * @Date: 2020/7/1 
     */ 
    Map<String, Object> getRechargePackageByCode(String productId);

    /**
     * @Description: 获取会员装饰
     * @Param: []
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/7/31
     */
    List<Map<String, Object>> getMemberDecoration();

}
