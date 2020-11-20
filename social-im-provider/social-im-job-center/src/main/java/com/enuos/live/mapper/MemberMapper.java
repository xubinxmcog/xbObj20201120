package com.enuos.live.mapper;

import com.enuos.live.pojo.Member;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/7/2
 * @Modified
 */
public interface MemberMapper {

    /** 
     * @Description: 充值会员列表
     * @Param: [] 
     * @Return: java.util.List<com.enuos.live.pojo.Member> 
     * @Author: wangyingjie
     * @Date: 2020/7/2 
     */ 
    List<Member> getMemberList();

    /**
     * @Description: 会员的扭蛋权益
     * @Param: []
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/2
     */
    List<Map<String, Object>> getGashaponInterest();

    /**
     * @Description: vip配置
     * @Param: []
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/7/2
     */
    List<Map<String, Object>> getVipConfig();

    /**
     * @Description: 更新会员
     * @Param: [list]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/7/2
     */
    int batchUpdateMember(List<Member> list);

}
