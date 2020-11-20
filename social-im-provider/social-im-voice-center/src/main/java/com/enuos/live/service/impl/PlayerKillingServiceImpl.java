package com.enuos.live.service.impl;

import com.enuos.live.constants.RedisKey;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.UserFeign;
import com.enuos.live.mapper.RoomMapper;
import com.enuos.live.pojo.PKPO;
import com.enuos.live.result.Result;
import com.enuos.live.service.PlayerKillingService;
import com.enuos.live.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ClassName PlayerKillingServiceImpl
 * @Description: TODO
 * @Author xubin
 * @Date 2020/7/6
 * @Version V2.0
 **/
@Service
public class PlayerKillingServiceImpl implements PlayerKillingService {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private RoomMapper roomMapper;

    /**
     * 用户
     */
    private static final String[] USER_COLUMN = {"nickName", "thumbIconUrl", "sex"};


    /**
     * @MethodName: createPK
     * @Description: TODO 创建PK
     * @Param: [po]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 18:19 2020/7/6
     **/
    @Override
    public Result createPK(PKPO po) {
        Long roomId = po.getRoomId();
        Long userId = po.getUserId();

        // 校验是否房主
        if (roomMapper.isOwner(roomId, userId) == 0) {
            return Result.error(ErrorCode.NO_PERMISSION);
        }

        Long time = po.getTime() + 10;
        Integer type = po.getType();
        Long targetUserId1 = po.getTargetUserId1();
        Long targetUserId2 = po.getTargetUserId2();
        boolean b = redisUtils.hasKey(RedisKey.KEY_PK + roomId);
        if (b) {
            return Result.error(2023, "当前房间有PK没有结束");
        }

        redisUtils.setHash(RedisKey.KEY_PK + roomId, targetUserId1 + "_" + type, 0);
        redisUtils.setHash(RedisKey.KEY_PK + roomId, targetUserId2 + "_" + type, 0);
        redisUtils.expire(RedisKey.KEY_PK + roomId, time);
        return Result.success();
    }

    /**
     * @MethodName: getPK
     * @Description: TODO 查询PK
     * @Param: [roomId, type]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 9:24 2020/7/7
     **/
    @Override
    public Result getPK(Long roomId) {
        Map hash = redisUtils.getHash(RedisKey.KEY_PK + roomId);
        Iterator<Map.Entry<String, Object>> entries = hash.entrySet().iterator();
        List<Map<String, Object>> list = new ArrayList<>();
        Integer type = 0;
        while (entries.hasNext()) {
            Map.Entry<String, Object> entry = entries.next();
            String key = entry.getKey();
            String[] strArr = key.split("_");
            Long userId = Long.valueOf(strArr[0]);
            type = Integer.valueOf(strArr[1]);
            Integer poll = (Integer) entry.getValue();
            Map<String, Object> userMap = userFeign.getUserBase(userId, USER_COLUMN);
            userMap.put("userId", userId);
            userMap.put("poll", poll);
            list.add(userMap);
        }
        long time = 0;
        if (list.size() > 0) {
            time = redisUtils.getExpire(RedisKey.KEY_PK + roomId);
        }
        time = (time - 10) > 0 ? (time - 10) : 0;
        Map<String, Object> map = new HashMap();
        map.put("roomId", roomId);
        map.put("type", type);
        map.put("time", time);
        map.put("list", list);
        return Result.success(map);
    }

    /**
     * @MethodName: poll
     * @Description: TODO 投票
     * @Param: [po]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 10:45 2020/7/7
     **/
    @Override
    public Result poll(PKPO po) {
        Long roomId = po.getRoomId();
        Long userId = po.getUserId();
        Long targetUserId = po.getTargetUserId();
        Integer type = po.getType(); // 1:魅力 2:人气
        Integer poll = po.getPoll();
        if (Objects.isNull(roomId) || Objects.isNull(userId) || Objects.isNull(targetUserId) || Objects.isNull(type) || Objects.isNull(poll)) {
            return Result.error(ErrorCode.CHAT_PARAM_NULL);
        }
        boolean b = redisUtils.hasKey(RedisKey.KEY_PK + roomId);
        if (!b) {
            return Result.error(2023, "PK已结束");
        }
        long expire = redisUtils.getExpire(RedisKey.KEY_PK + roomId);
        if (expire < 10) {
            return Result.error(2023, "PK投票已结束");
        }
        String pollUserKey = RedisKey.KEY_PK + roomId + "_" + type; // 已经投票的Key

        if (type == 2) {
            boolean b1 = redisUtils.sHasKey(pollUserKey, userId + "_" + type);
            if (b1) {
                return Result.error(2023, "你已经投过票");
            }
            redisUtils.sSetAndTime(pollUserKey, expire, userId + "_" + type);
        }
        Integer pollValue = (Integer) redisUtils.getHash(RedisKey.KEY_PK + roomId, targetUserId + "_" + type);
        if (Objects.isNull(pollValue)) {
            return Result.error(2023, "PK已结束!");
        }
        poll = poll + pollValue;
        redisUtils.setHash(RedisKey.KEY_PK + roomId, targetUserId + "_" + type, poll);
        return Result.success();
    }
}
