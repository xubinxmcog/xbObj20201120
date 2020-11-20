package com.enuos.live.service.impl;

import com.enuos.live.constant.SuccessCode;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.ChatFeign;
import com.enuos.live.mapper.FriendMapper;
import com.enuos.live.pojo.Friend;
import com.enuos.live.result.Result;
import com.enuos.live.service.CurrencyService;
import com.enuos.live.service.FriendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;

/**
 * @Description 用户好友
 * @Author wangyingjie
 * @Date 2020/7/7
 * @Modified
 */
@Slf4j
@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private ChatFeign chatFeign;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private FriendMapper friendMapper;

    /**
     * @Description: 判定加好友是否需要支付身价
     * @Param: [friend]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    @Override
    public Result worth(Friend friend) {
        Map<String, Object> map = friendMapper.relation(friend);
        if (MapUtils.isEmpty(map)) {
            return Result.error(ErrorCode.NO_DATA);
        }

        Integer abIsExists = MapUtils.getInteger(map, "abIsExists");
        Integer abIsDel = MapUtils.getInteger(map, "abIsDel");

        Integer baIsExists = MapUtils.getInteger(map, "baIsExists");
        Integer baIsDel = MapUtils.getInteger(map, "baIsDel");

        Long bWorth = MapUtils.getLong(map, "bWorth");

        // b已经为a的好友
        if (abIsExists == 1 && abIsDel == 0) {
            return Result.error(ErrorCode.FRIEND_IS_EXISTS);
        }

        // 需要支付的场景：a不在b的好友列表中，即bWorth > 0 && ((baIsExists = 0)||(baIsExists = 1 && baIsDel = 1))
        return bWorth > 0 && (baIsExists == 0 || (baIsExists == 1 && baIsDel == 1)) ? Result.success(SuccessCode.SUCCESS_CODE, MessageFormat.format("添加好友需{0}金币。", bWorth)) : Result.success();
    }

    /**
     * @Description: 交朋友
     * 1.AB互加好友，一方付费，则另一方不需要付费；
     * 2.单向好友关系；
     * @Param: [friend]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/7/7 
     */ 
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result makeFriend(Friend friend) {
        if (Objects.isNull(friend)
                || Objects.isNull(friend.getFriendId())
                || Objects.isNull(friend.getType())
                || Objects.equals(friend.userId, friend.getFriendId())) {
            return Result.empty();
        }

        if (Optional.ofNullable(friend.getRemark()).orElse("").length() > 10) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "好友备注不超过10个字");
        }

        // 0 不需要支付身价 1 需要支付身价
        int type = friend.getType();

        // 判断好友位是否已满
        Map<String, Object> friendMap = friendMapper.getFriendNum(friend.userId);
        int vip = MapUtils.getIntValue(friendMap, "vip");
        int friendNum = MapUtils.getIntValue(friendMap, "friendNum");

        if (friendNum >= vip * 50 + 200) {
            return Result.error(ErrorCode.FRIEND_LIMIT);
        }

        // 判断是否存在好友记录
        Map<String, Object> map = friendMapper.relation(friend);
        if (MapUtils.isEmpty(map)) {
            return Result.error(ErrorCode.NO_DATA);
        }

        Integer abIsExists = MapUtils.getInteger(map, "abIsExists");
        Integer abIsDel = MapUtils.getInteger(map, "abIsDel");

        Integer baIsExists = MapUtils.getInteger(map, "baIsExists");
        Integer baIsDel = MapUtils.getInteger(map, "baIsDel");

        Long bWorth = MapUtils.getLong(map, "bWorth");

        // b已经为a的好友
        int result;
        if (abIsExists == 1) {
            if (abIsDel == 0) {
                return Result.error(ErrorCode.FRIEND_IS_EXISTS);
            } else {
                friend.setIsDel(0);
                result = friendMapper.updateRelation(friend);
            }
        } else {
            result = friendMapper.saveRelation(friend);
        }

        // 支付b身价(系统收费)
        if (type == 1 && result > 0 && bWorth > 0 && (baIsExists == 0 || (baIsExists == 1 && baIsDel == 1))) {
            Result payResult = currencyService.countGold(friend.userId, Math.negateExact(bWorth));
            if (payResult.getCode() != 0) {
                throw new RuntimeException(payResult.getMsg());
            }
        }

        // 若b已将a拉黑则不需要发送通知
        buildRelationships(friend.userId, friend.getFriendId(), 0, friendMapper.isBlack(friend) > 0 ? 0 : 1);

        return Result.success();

    }

    /**
     * @Description: 花名册[好友&聊天]
     * @Param: [friend]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    @Override
    public Result roster(Friend friend) {
        if (friend == null || friend.userId == null) {
            return Result.empty();
        }

        // 获取会员等级，依据会员等级，显示好友人数
        Map<String, Object> resultMap = friendMapper.getFriendNum(friend.userId);
        if (MapUtils.isEmpty(resultMap)) {
            return Result.empty();
        }

        int vip = MapUtils.getIntValue(resultMap, "vip");

        // 好友列表/搜索列表
        List<Map<String, Object>> friendList = friendMapper.getFriendList(friend);

        // 群组列表
        if (StringUtils.isNotBlank(friend.getNickName())) {
            resultMap.put("groupList", chatFeign.searchGroup(new HashMap<String, Object>() {
                {
                    put("userId", friend.userId);
                    put("groupName", friend.getNickName());
                }
            }).getData());
        }

        resultMap.put("friendLimit", vip * 50 + 200);
        resultMap.put("friendList", friendList);
        resultMap.remove("vip");

        return Result.success(resultMap);
    }

    /**
     * @Description: 修改备注
     * @Param: [friend]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    @Override
    @Transactional
    public Result updateFriend(Friend friend) {
        if (Optional.ofNullable(friend.getRemark()).orElse("").length() > 10) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "好友备注不超过10个字");
        }
        int result = friendMapper.updateFriend(friend);

        return result > 0 ? Result.success() : Result.error();
    }

    /**
     * @Description: 解除好友关系
     * @Param: [friend]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    @Override
    @Transactional
    public Result unFriend(Friend friend) {
        friend.setIsDel(1);
        int result = friendMapper.updateRelation(friend);

        buildRelationships(friend.userId, friend.getFriendId(), 1, friendMapper.isBlack(friend) > 0 ? 0 : 1);

        return result > 0 ? Result.success() : Result.error();
    }

    /** [PRIVATE] */

    /**
     * @Description: [PRIVATE]建立取消聊天关系[添加好友删除好友调用]
     * @Param: [userId, targetId, action]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    private void buildRelationships(Long userId, Long targetId, Integer action, Integer isNotice) {
        chatFeign.buildRelationships(new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("targetId", targetId);
                put("action", action);
                put("isNotice", isNotice);
            }
        });
    }

}
