package com.enuos.live.service;

import com.enuos.live.pojo.Nearby;
import com.enuos.live.pojo.Shield;
import com.enuos.live.pojo.User;
import com.enuos.live.result.Result;

import java.util.List;
import java.util.Map;

/**
 * @Description 个人中心
 * @Author wangyingjie
 * @Date 17:19 2020/4/1
 * @Modified
 */
public interface UserService {

    /**
     * @Description: 用户A获取用户B，是否好友[单向好友关系]
     * @Param: [userId, toUserId]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/3
     */
    Result getStranger(Long userId, Long toUserId);

    /**
     * @Description: 用户A获取用户B详情
     * @Param: [userId, toUserId]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/3
     */
    Result getStrangerDetail(Long userId, Long toUserId);

    /**
     * @Description: 获取个人基础信息
     * @Param: [user]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Result getBase(User user);

    /**
     * @Description: 获取身价
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/7
     */
    Result worthList();

    /**
     * @Description: 获取修改性别需要的金币
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/7
     */
    Result toUpdateSex();

    /**
     * @Description: 更新
     * @Param: [user]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Result updateBase(User user);

    /**
     * @Description: 获取主页综合信息
     * @Param: [user]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Result getDetail(User user);

    /**
     * @Description: 获取主页称号
     * @Param: [user]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/4
     */
    Result title(User user);

    /**
     * @Description: 获取附近的人
     * @Param: [nearby]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Result nearbyList(Nearby nearby);

    /**
     * @Description: 屏蔽列表
     * @Param: [shield]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/1
     */
    Result shieldList(Shield shield);

    /**
     * @Description: 取消屏蔽
     * @Param: [shield]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/1
     */
    Result unShield(Shield shield);

    /**
     * @Description: 获取钻石金币
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/28
     */
    Result getCurrency(Long userId);

    /**
     * @Description: [OPEN]官网后台充值，输入userId，获取用户信息以校验
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/15
     */
    Result getUserForRecharge(Long userId);

    /**
     * @Description: [OPEN]获取用户基本信息
     * @Param: [userId, friendId]
     * @Return: java.util.Map<java.lang.String       ,       java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Map<String, Object> getUserBase(Long userId, Long friendId);

    /**
     * @Description: [OPEN]获取关系
     * @Param: [userId, toUserId, flag]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Integer getRelation(Long userId, Long toUserId, Integer flag);

    /**
     * @Description: [OPEN]获取用户昵称，性别，头像，账号等级
     * @Param: [userIdList]
     * @Return: java.util.List<java.util.Map       <       java.lang.String       ,       java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    List<Map<String, Object>> getUserList(List<Long> userIdList);

    /**
     * @Description: 获取头像框，聊天框
     * @Param: [userId]
     * @Return: java.util.Map<java.lang.String   ,   java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/27
     */
    Map<String, Object> getUserFrame(Long userId);

    /**
     * @Description:
     * @Param: [userId]
     * @Return: java.util.Map<java.lang.String   ,   java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Map<String, Object> getUserMsg(Long userId);

    /**
     * @Description: [OPEN]获取昵称的字符串以','拼接
     * @Param: [userIdList]
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    String getNickName(List<Long> userIdList);

    /**
     * @Description: [OPEN]获取用户id
     * @Param: [isMember]
     * @Return: java.util.List<java.lang.Long>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    List<Long> getUserIdList(Integer isMember);

    /**
     * @MethodName: charmRanking
     * @Description: TODO 魅力: 礼物榜 or 真爱榜
     * @Param: [userId, type]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:34 2020/7/7
     **/
    Result charmRanking(Long userId, Integer type, Integer pageSize);

    /**
     * @MethodName: charmDedicate
     * @Description: TODO 魅力,守护排行榜
     * @Param: [type 1:魅力 2:守护, charmType 1:昨天 2:今天, pageSize:多少条]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:46 2020/9/2
    **/
    Result charmDedicate(Integer type, Integer charmType, Integer pageSize);

}
