package com.enuos.live.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.enuos.live.constants.RedisKey;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.ChatFeign;
import com.enuos.live.mapper.UserMapper;
import com.enuos.live.pojo.*;
import com.enuos.live.result.Result;
import com.enuos.live.service.CommonService;
import com.enuos.live.service.CurrencyService;
import com.enuos.live.service.MemberService;
import com.enuos.live.service.UserService;
import com.enuos.live.utils.DateUtils;
import com.enuos.live.utils.RedisUtils;
import com.enuos.live.utils.SortUtils;
import com.enuos.live.utils.TimeDateUtils;
import com.enuos.live.utils.sensitive.DFAWordUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 个人中心
 * @Author wangyingjie
 * @Date 17:21 2020/4/1
 * @Modified
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private ChatFeign chatFeign;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private DFAWordUtils dfaWordUtils;

    /**
     * @Description: 用户A获取用户B，是否好友[单向好友关系]
     * @Param: [userId, toUserId]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/3
     */
    @Override
    public Result getStranger(Long userId, Long toUserId) {
        if (Objects.isNull(userId) || Objects.isNull(toUserId)) {
            return Result.empty();
        }

        Map<String, Object> result = userMapper.getStranger(userId, toUserId);
        if (MapUtils.isEmpty(result)) {
            return Result.error(ErrorCode.NO_DATA);
        }

        return Result.success(result);
    }

    /**
     * @Description: 用户A获取用户B详情
     * @Param: [userId, toUserId]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/3
     */
    @Override
    public Result getStrangerDetail(Long userId, Long toUserId) {
        if (Objects.isNull(userId) || Objects.isNull(toUserId)) {
            return Result.empty();
        }

        Stranger stranger = userMapper.getStrangerDetail(userId, toUserId);
        if (Objects.isNull(stranger)) {
            return Result.error(ErrorCode.NO_DATA);
        }

        // 判定该账号是否已注销
        if (stranger.getIsDel() == 1) {
            return Result.error(ErrorCode.ACCOUNT_LOGINOUT);
        }

        stranger.setOnLineStatus(Integer.parseInt(commonService.getOnLineStatus(toUserId).toString()));
        stranger.setIsMember(memberService.isMember(toUserId));

        return Result.success(stranger);
    }

    /**
     * @Description: 获取个人基础信息
     * @Param: [user]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    public Result getBase(User user) {
        if (Objects.isNull(user)) {
            return Result.empty();
        }

        Map<String, Object> userBaseMap = userMapper.getUserBaseByUserId(user.userId);
        if (MapUtils.isEmpty(userBaseMap)) {
            return Result.error(ErrorCode.NO_DATA);
        }

        putMemberAndBackground(user.userId, userBaseMap);

        return Result.success(userBaseMap);
    }

    /**
     * @Description: 获取身价
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/7
     */
    @Override
    public Result worthList() {
        List<Map<String, Object>> list = userMapper.getWorthThreshold();
        if (CollectionUtils.isEmpty(list)) {
            return Result.error(ErrorCode.NO_DATA);
        }

        return Result.success(list);
    }

    /**
     * @Description: 获取修改性别需要的金币
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/7
     */
    @Override
    public Result toUpdateSex() {
        Long gold = userMapper.getSexThreshold();
        return Result.success(new HashMap<String, Object>() {
            {
                put("gold", gold == null ? 0 : gold);
            }
        });
    }

    /**
     * @Description: 更新
     * @Param: [userPO]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateBase(User user) {
        if (Objects.isNull(user)) {
            return Result.empty();
        }

        if (Optional.ofNullable(user.getNickName()).orElse("").length() > 10) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "昵称不超过10个字");
        }

        if (Optional.ofNullable(user.getSignature()).orElse("").length() > 100) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "个性签名不超过100个字");
        }

        Long userId = user.getUserId();

        // 昵称修改验证
        if (dfaWordUtils.matchWords(user.getNickName())) {
            return Result.error(ErrorCode.CONTENT_SENSITIVE);
        }

        // 个性签名校验
        if (dfaWordUtils.matchWords(user.getSignature())) {
            return Result.error(ErrorCode.CONTENT_SENSITIVE);
        }

        // 性别修改需要金币，金币不足则不改
        if (!Objects.isNull(user.getSex()) && !user.getSex().equals(userMapper.getSex(userId))) {
            Long needGold = userMapper.getSexThreshold();
            if (needGold == null || needGold == 0) {
                return Result.error(ErrorCode.NO_DATA);
            }
            Result result = currencyService.countGold(userId, Math.negateExact(needGold));
            if (result.getCode() != 0) {
                return result;
            }
        }

        // 更新用户
        userMapper.updateUserBase(user);

        // 更新背景
        List<Background> backgroundList = user.getBackgroundList();
        if (backgroundList != null) {
            userMapper.deleteBackgroundByUserId(userId);
            if (backgroundList.size() > 0) {
                userMapper.batchSaveBackground(user);
            }
        }

        // 更新身价
        if (!Objects.isNull(user.getWorth())) {
            userMapper.updateUserWorth(user);
        }

        return getBase(user);
    }

    /**
     * @Description: 获取主页综合信息
     * @Param: [userPO]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    public Result getDetail(User user) {
        if (Objects.isNull(user)) {
            return Result.empty();
        }

        Map<String, Object> userMap = userMapper.getUser(user.userId);
        if (MapUtils.isEmpty(userMap)) {
            return Result.error(ErrorCode.NO_DATA);
        }

        putMemberAndBackground(user.userId, userMap);

        return Result.success(userMap);
    }

    /**
     * @Description: 获取主页称号
     * @Param: [userPO]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/4
     */
    @Override
    public Result title(User user) {
        if (Objects.isNull(user) || Objects.isNull(user.getToUserId())) {
            return Result.empty();
        }

        return Result.success(userMapper.getTitle(user.userId, user.getToUserId()));
    }

    /**
     * @Description: 获取附近的人 5km以内最近的10个
     * @Param: [nearby]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    public Result nearbyList(Nearby nearby) {
        if (Objects.isNull(nearby) || Objects.isNull(nearby.getUserId())) {
            return Result.empty();
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        String userIdKey = String.valueOf(nearby.getUserId());

        // 默认5km以内
        Integer limit = Optional.ofNullable(nearby.getLimit()).orElse(10);
        Distance distance = new Distance(Optional.ofNullable(nearby.getDistance()).orElse(5d), Metrics.KILOMETERS);

        // 最近的所有
        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = redisUtils.getGeo(RedisKey.KEY_GEO, userIdKey, distance);
        if (Objects.isNull(geoResults)) {
            return Result.success(resultList);
        }

        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> geoList = geoResults.getContent();
        if (CollectionUtils.isEmpty(geoList)) {
            return Result.success(resultList);
        }

        // 过滤掉自己
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> filterSelfGeoList = geoList.stream().filter(geoResult -> !StringUtils.equals(geoResult.getContent().getName(), userIdKey)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filterSelfGeoList)) {
            return Result.success(resultList);
        }
        // 查询其他信息，排除好友
        resultList = userMapper.nearbyList(nearby.getUserId(), filterSelfGeoList);
        if (CollectionUtils.isEmpty(resultList)) {
            return Result.success(resultList);
        }

        resultList.forEach(map -> {
            filterSelfGeoList.forEach(geo -> {
                if (StringUtils.equals(geo.getContent().getName(), MapUtils.getString(map, "userId"))) {
                    map.put("distance", geo.getDistance().getValue());
                }
            });
        });

        SortUtils.sort(resultList, true, "distance");

        return Result.success(resultList.size() > limit ? resultList.subList(0, limit) : resultList);
    }

    /**
     * @Description: 屏蔽列表
     * @Param: [shield]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/1
     */
    @Override
    public Result shieldList(Shield shield) {
        if (Objects.isNull(shield) || Objects.isNull(shield.userId)) {
            return Result.empty();
        }

        return Result.success(userMapper.blacklist(shield.userId, 1));
    }

    /**
     * @Description: 取消屏蔽
     * @Param: [shield]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/1
     */
    @Override
    @Transactional
    public Result unShield(Shield shield) {
        if (Objects.isNull(shield) || Objects.isNull(shield.userId)) {
            return Result.empty();
        }

        int result = userMapper.deleteBlacklist(shield.userId, shield.getToUserId(), 1);

        return result > 0 ? Result.success() : Result.error();
    }

    /**
     * @Description: 获取钻石金币
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/28
     */
    @Override
    public Result getCurrency(Long userId) {
        if (userId == null) {
            return Result.empty();
        }
        return Result.success(userMapper.getCurrency(userId));
    }


    /** 对外接口 */

    /**
     * @Description: [OPEN]官网后台充值，输入userId，获取用户信息以校验
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/15
     */
    @Override
    public Result getUserForRecharge(Long userId) {
        if (userId == null) {
            return Result.empty();
        }

        Map<String, Object> map = userMapper.getUserForRecharge(userId);
        if (MapUtils.isEmpty(map)) {
            return Result.error(ErrorCode.NO_DATA);
        }

        return Result.success(map);
    }

    /**
     * @Description: [OPEN]获取用户基本信息
     * @Param: [userId, friendId]
     * @Return: java.util.Map<java.lang.String>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    public Map<String, Object> getUserBase(Long userId, Long friendId) {
        if (Objects.isNull(userId) || Objects.isNull(friendId)) {
            return null;
        }

        return userMapper.getUserBase(userId, friendId);
    }

    /**
     * @Description: [OPEN]获取关系
     * @Param: [userId, toUserId, flag]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    public Integer getRelation(Long userId, Long toUserId, Integer flag) {
        if (Objects.isNull(userId) || Objects.isNull(toUserId) || Objects.isNull(flag)) {
            return null;
        }

        int result;
        switch (flag) {
            case 0:
                result = userMapper.isRelation(userId, toUserId, "tb_user_friend");
                break;
            case 1:
                result = userMapper.isRelation(userId, toUserId, "tb_user_blacklist");
                break;
            default:
                result = 0;
                break;
        }

        return result == 0 ? 0 : 1;
    }

    /** 
     * @Description: [OPEN]获取用户昵称，性别，头像，账号等级 
     * @Param: [userIdList] 
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>> 
     * @Author: wangyingjie
     * @Date: 2020/9/11 
     */ 
    @Override
    public List<Map<String, Object>> getUserList(List<Long> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return null;
        }

        return userMapper.getUserList(userIdList);
    }

    /**
     * @Description: 获取头像框，聊天框
     * @Param: [userId]
     * @Return: java.util.Map<java.lang.String>
     * @Author: wangyingjie
     * @Date: 2020/7/27
     */
    @Override
    public Map<String, Object> getUserFrame(Long userId) {
        Map<String, Object> userFrame = userMapper.getUserFrame(userId);
//        Object chatFrameAttribute = userFrame.get("chatFrameAttribute");
//        if (!Objects.isNull(chatFrameAttribute)) {
//            JSONObject jsonObject = JSONObject.parseObject(chatFrameAttribute.toString());
//            userFrame.put("chatFrameAttribute",jsonObject);
//        }
        return userFrame;
    }
    
    /** 
     * @Description:  
     * @Param: [userId] 
     * @Return: java.util.Map<java.lang.String,java.lang.Object> 
     * @Author: wangyingjie
     * @Date: 2020/9/11 
     */ 
    @Override
    public Map<String, Object> getUserMsg(Long userId) {
        return userMapper.getUserMsg(userId);
    }

    /**
     * @Description: [OPEN]获取昵称的字符串以','拼接
     * @Param: [userIdList]
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    public String getNickName(List<Long> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return null;
        }

        return userMapper.getNickName(userIdList);
    }

    /**
     * @Description: [OPEN]获取用户id
     * @Param: [isMember]
     * @Return: java.util.List<java.lang.Long>
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    public List<Long> getUserIdList(Integer isMember) {
        return userMapper.getUserIdList(isMember);
    }

    /**
     * @MethodName: charmRanking
     * @Description: TODO  魅力: 礼物榜 or 真爱榜
     * @Param: [userId 用户ID, type 1:礼物榜 2:真爱榜, pageSize 查询条数]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:42 2020/7/7
     **/
    @Override
    public Result charmRanking(Long userId, Integer type, Integer pageSize) {

        List<Map<String, Object>> maps = null;
        switch (type) {
            case 1:
                if (Objects.isNull(pageSize)) {
                    pageSize = 100;
                }
                maps = userMapper.getGiftRanking(userId, pageSize);
                break;
            case 2:
                if (Objects.isNull(pageSize)) {
                    pageSize = 10;
                }
                maps = userMapper.getCharmRanking(userId, pageSize);
                break;
            default:
                log.error("UserServiceImpl.charmRanking type类型错误");
                return Result.error(ErrorCode.ERROR_OPERATION);
        }
        return Result.success(maps);
    }

    /**
     * @MethodName: charmDedicate
     * @Description: TODO 魅力,守护排行榜
     * @Param: [type 1:魅力 2:守护, charmType 1:昨天 2:今天, pageSize:多少条]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:46 2020/9/2
     **/
    @Override
    public Result charmDedicate(Integer type, Integer charmType, Integer pageSize) {
        log.info("查询魅力,守护排行榜入参,type=[{}], charmType=[{}], pageSize=[{}]", type, charmType, pageSize);

        String key = RedisKey.KEY_CHARM_DEDICATE + type + "_" + charmType;

        List<Map<String, Object>> charmDedicate = (List<Map<String, Object>>) redisUtils.get(key);
        if (ObjectUtil.isNotEmpty(charmDedicate)) {
            return Result.success(charmDedicate);
        }
        String column = "";
        String startTime = ""; // 开始时间
        String endTime = ""; // 结束时间
        long timeout = 600; // 默认10分钟
        switch (type) {
            case 1:
                column = "user_id";
                break;
            case 2:
                column = "give_user_id";
                break;
            default:
                log.error("UserServiceImpl.charmDedicate type=[{}]类型错误", type);
                return Result.error(ErrorCode.ERROR_OPERATION);
        }
        switch (charmType) {
            case 1: // 昨天
                pageSize = 20;
                startTime = TimeDateUtils.getYesterdayZeroPoint();
                endTime = TimeDateUtils.getYesterdayLastSecond();
                timeout = TimeDateUtils.getDaySurplusTime() + 200; // 当天剩余时间加200秒
                break;
            case 2: // 今天
                pageSize = 30;
                startTime = TimeDateUtils.getTodayZeroPoint();
                endTime = TimeDateUtils.getDateTimeStr(System.currentTimeMillis());
                timeout = 600; // 10分钟
                break;
            case 3: // 荣誉室
                pageSize = 100;
                timeout = TimeDateUtils.getDaySurplusTime() + 200; // 当天剩余时间加200秒
                charmDedicate = userMapper.getCharmHonor(type, pageSize);
                if (ObjectUtil.isNotEmpty(charmDedicate)) {
                    redisUtils.set(key, charmDedicate, timeout);
                    return Result.success(charmDedicate);
                } else {
                    log.warn("未查询到数据");
                    return Result.success("暂无数据");
                }
            default:
                log.error("UserServiceImpl.charmDedicate charmType=[{}]类型错误", charmType);
                return Result.error(ErrorCode.ERROR_OPERATION);
        }
        if (StrUtil.isNotEmpty(column) && StrUtil.isNotEmpty(startTime) && StrUtil.isNotEmpty(endTime) && !Objects.isNull(pageSize)) {

            charmDedicate = userMapper.getCharmDedicate(column, startTime, endTime, pageSize);

            if (ObjectUtil.isNotEmpty(charmDedicate)) {
                redisUtils.set(key, charmDedicate, timeout);
                return Result.success(charmDedicate);
            } else {
                log.warn("未查询到数据");
                return Result.success("暂无数据");
            }
        } else {
            log.error("必要参数为空, column=[{}], startTime=[{}], endTime=[{}], pageSize=[{}]", column, startTime, endTime, pageSize);
            return Result.error(201, "暂无数据");
        }
    }

    /**
     * @Description: 放入会员和背景
     * @Param: [userId, userMap]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/21
     */
    private void putMemberAndBackground(Long userId, Map<String, Object> userMap) {
        Integer vip = MapUtils.getInteger(userMap, "vip");
        LocalDateTime expirationTime = DateUtils.getLocalDateTime(MapUtils.getString(userMap, "expirationTime"));

        // -1 过期会员 0 非会员 1 会员
        userMap.put("isMember", vip > 0 ? expirationTime.isAfter(DateUtils.getCurrentDateTime()) ? 1 : -1 : 0);
        userMap.put("backgroundList", userMapper.getUserBackgroundByUserId(userId));
    }
}