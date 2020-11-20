package com.enuos.live.mapper;

import com.enuos.live.pojo.Stranger;
import com.enuos.live.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.redis.connection.RedisGeoCommands;

import java.util.List;
import java.util.Map;

/**
 * @Description 用户
 * @Author wangyingjie
 * @Date 15:18 2020/4/1
 * @Modified
 */
public interface UserMapper {

    /**
     * @Description: 用户A获取用户B，是否好友[单向好友关系]
     * @Param: [userId, toUserId]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/3
     */
    Map<String, Object> getStranger(@Param("userId") Long userId, @Param("toUserId") Long toUserId);

    /**
     * @Description: 用户A获取用户B详情
     * @Param: [userId, toUserId]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/3
     */
    Stranger getStrangerDetail(@Param("userId") Long userId, @Param("toUserId") Long toUserId);
    
    /** 
     * @Description: 保存用户信息
     * @Param: [user]
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/7/6 
     */ 
    int saveUser(User user);

    /**
     * @Description: 获取用户信息
     * @Param: [userId]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Map<String, Object> getUserBaseByUserId(Long userId);

    /**
     * @Description: 获取主页综合信息
     * @Param: [userId]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    Map<String, Object> getUser(Long userId);

    /**
     * @Description: 更新
     * @Param: [user]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    int updateUserBase(User user);

    /**
     * @Description: 修改身价
     * @Param: [user]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/7/7
     */
    int updateUserWorth(User user);

    /** 
     * @Description: 批量保存用户背景
     * @Param: [user]
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/7/6 
     */ 
    int batchSaveBackground(User user);

    /**
     * @Description: 获取用户背景
     * @Param: [userId]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    List<Map<String, Object>> getUserBackgroundByUserId(Long userId);

    /**
     * @Description: 删除背景
     * @Param: [userId]
     * @Return:
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    int deleteBackgroundByUserId(Long userId);

    /**
     * @Description: 获取主页称号
     * @Param: [userId, toUserId]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/9/4
     */
    List<Map<String, Object>> getTitle(@Param("userId") Long userId, @Param("toUserId") Long toUserId);

    /**
     * @Description: 获取附近的用户列表
     * @Param: [userId, geoResultList]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    List<Map<String, Object>> nearbyList(@Param("userId") Long userId, @Param("geoResultList") List<GeoResult<RedisGeoCommands.GeoLocation<String>>> geoResultList);

    /**
     * @Description: 黑名单/屏蔽列表
     * @Param: [userId, rating]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/6/1
     */
    List<Map<String, Object>> blacklist(@Param("userId") Long userId, @Param("rating") Integer rating);

    /**
     * @Description: 黑名单/屏蔽取消
     * @Param: [userId, toUserId, rating]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/6/1
     */
    int deleteBlacklist(@Param("userId") Long userId, @Param("toUserId") Long toUserId, @Param("rating") Integer rating);

    /**
     * @Description: 获取身价阈值
     * @Param: []
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/7/7
     */
    List<Map<String, Object>> getWorthThreshold();

    /**
     * @Description: 获取修改性别的设置阈值
     * @Param: []
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/7/7
     */
    Long getSexThreshold();

    /** 
     * @Description: [OPEN]官网后台充值，输入userId，获取用户信息以校验
     * @Param: [userId] 
     * @Return: java.util.Map<java.lang.String,java.lang.Object> 
     * @Author: wangyingjie
     * @Date: 2020/7/15 
     */ 
    Map<String, Object> getUserForRecharge(Long userId);
    
    /** 
     * @Description: [OPEN]获取用户基本信息 
     * @Param: [userId, friendId] 
     * @Return: java.util.Map<java.lang.String,java.lang.Object> 
     * @Author: wangyingjie
     * @Date: 2020/7/6 
     */ 
    Map<String, Object> getUserBase(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * @Description: [OPEN]关系
     * @Param: [userId, toUserId, table] 
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/7/6 
     */ 
    int isRelation(@Param("userId") Long userId, @Param("toUserId") Long toUserId, @Param("table") String table);

    /**
     * @Description: [OPEN]获取用户昵称，性别，头像，账号等级
     * @Param: [userIdList]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    List<Map<String, Object>> getUserList(List<Long> userIdList);

    /**
     * @Description: 获取头像框，聊天框
     * @Param: [userId]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/27
     */
    Map<String, Object> getUserFrame(@Param("userId") Long userId);

    Map<String, Object> getUserMsg(@Param("userId") Long userId);

    /**
     * @Description: [OPEN]获取昵称的字符串以','拼接
     * @Param: [userIdList]
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    String getNickName(List<Long> userIdList);

    /**
     * @Description: [OPEN]获取用户ID
     * @Param: [isMember]
     * @Return: java.util.List<java.lang.Long>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    List<Long> getUserIdList(@Param("isMember") Integer isMember);

    List<Map<String, Object>> getCharmRanking(@Param("userId") Long userId, @Param("limit") Integer limit);


    List<Map<String, Object>> getCharmDedicate(@Param("column") String column,
                                               @Param("startTime") String startTime,
                                               @Param("endTime") String endTime,
                                               @Param("limit") Integer limit);

    List<Map<String, Object>> getCharmHonor(@Param("type") Integer type, @Param("limit") Integer limit);

    List<Map<String, Object>> getGiftRanking(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * @Description: 获取钻石金币
     * @Param: [userId]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/28
     */
    Map<String, Object> getCurrency(@Param("userId") Long userId);
    
    /** 
     * @Description: 获取性别
     * @Param: [userId] 
     * @Return: java.lang.Integer 
     * @Author: wangyingjie
     * @Date: 2020/7/30 
     */ 
    Integer getSex(Long userId);
}
