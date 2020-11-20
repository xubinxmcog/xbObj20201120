package com.enuos.live.mapper;

import com.enuos.live.pojo.*;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Description 邀请
 * @Author wangyingjie
 * @Date 2020/8/4
 * @Modified
 */
public interface InviteMapper {

    /**
     * @Description: 获取邀请奖励
     * @Param: []
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/8/7
     */
    List<Map<String, Object>> getReward();

    /**
     * @Description: 邀请者是否存在
     * @Param: [userId]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/8/4
     */
    Integer isExistsInviter(Long userId);
    
    /** 
     * @Description: 受邀者是否注册过
     * @Param: [account] 
     * @Return: java.lang.Integer 
     * @Author: wangyingjie
     * @Date: 2020/8/4 
     */ 
    Integer isExistsAccount(String account);

    /**
     * @Description: 受邀者是否存在被他人邀请
     * @Param: [account]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/8/4
     */
    Integer isExistsInviterUser(String account);
    
    /**
     * @Description: 获取受邀用户信息
     * @Param: [userId, account]
     * @Return: com.enuos.live.pojo.InviteUser
     * @Author: wangyingjie
     * @Date: 2020/8/7
     */
    InviteUser getInviterUser(Long toUserId);

    /**
     * @Description: 创建邀请者信息
     * @Param: [userId]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/8/4
     */
    int saveInviter(Long userId);

    /**
     * @Description: 创建邀请者受邀者关系
     * @Param: [invitePO]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/8/4
     */
    int saveInviterUser(InviteUser inviteUser);

    /** 
     * @Description: 更新受邀用户ID及登陆时间
     * @Param: [inviteUser] 
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/8/7 
     */ 
    int updateInviterUser(InviteUser inviteUser);

    /**
     * @Description: 获取邀请相关信息
     * @Param: [userId]
     * @Return: com.enuos.live.pojo.Invite
     * @Author: wangyingjie
     * @Date: 2020/8/5
     */
    Invite getInviteInfo(Long userId);

    /**
     * @Description: 新增审核单
     * @Param: [sourceCode, userId, toUserId, auditMoney]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/8/7
     */
    int saveAuditMoney(@Param("sourceCode") String sourceCode, @Param("userId") Long userId, @Param("toUserId") Long toUserId, @Param("auditMoney") BigDecimal auditMoney);

    /** 
     * @Description: 待审核&审核通过金额
     * @Param: [userId] 
     * @Return: java.util.List<com.enuos.live.pojo.InviteAuditVO>
     * @Author: wangyingjie
     * @Date: 2020/8/5 
     */ 
    List<InviteMoney> getAuditMoney(Long userId);

    /**
     * @Description: 今日是否存在审核单
     * @Param: [sourceCode, userId, toUserId, createTime]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/8/7
     */
    Integer isExistsAuditMoneyByDay(@Param("sourceCode") String sourceCode, @Param("userId") Long userId, @Param("toUserId") Long toUserId, @Param("createTime") LocalDateTime createTime);

    /**
     * @Description: 获取可用金额
     * @Param: [userId]
     * @Return: com.enuos.live.pojo.Invite
     * @Author: wangyingjie
     * @Date: 2020/8/5
     */
    Invite getAbleMoney(Long userId);

    /**
     * @Description: 获取今日已提现
     * @Param: [userId, localDate]
     * @Return: java.math.BigDecimal
     * @Author: wangyingjie
     * @Date: 2020/8/6
     */
    BigDecimal getTodayGetMoney(@Param("userId") Long userId, @Param("localDate") LocalDate localDate);

    /**
     * @Description: 保存提现
     * @Param: [inviteMoney]
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/8/6 
     */ 
    int saveMoneyGet(InviteMoney inviteMoney);

    /**
     * @Description: 提现记录
     * @Param: [userId]
     * @Return: java.util.List<com.enuos.live.pojo.InviteMoneyRecordVO>
     * @Author: wangyingjie
     * @Date: 2020/8/6
     */
    List<InviteMoney> getRecordList(Long userId);
    
    /** 
     * @Description: 更新支付结果
     * @Param: [params] 
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/8/6 
     */ 
    int updateOrderStatus(Map<String, Object> params);

    /**
     * @Description: 获取邀请者已提现
     * @Param: [orderId]
     * @Return: com.enuos.live.pojo.Invite
     * @Author: wangyingjie
     * @Date: 2020/8/6
     */
    Invite getHaveMoney(String orderId);

    /** 
     * @Description: 获取用户金额
     * @Param: [userId] 
     * @Return: com.enuos.live.pojo.Invite 
     * @Author: wangyingjie
     * @Date: 2020/8/10 
     */ 
    Invite getInvite(Long userId);
    
    /**
     * @Description: 更新
     * @Param: [invite]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/8/6
     */
    int updateInvite(Invite invite);

    /**
     * @Description: 查询记录
     * @Param: [limit]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/8/10
     */
    List<Map<String, Object>> getRecordListByLimit(Integer limit);
}
