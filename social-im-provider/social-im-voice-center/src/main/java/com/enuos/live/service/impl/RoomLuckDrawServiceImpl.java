package com.enuos.live.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.UserFeign;
import com.enuos.live.mapper.RoomMapper;
import com.enuos.live.mapper.RoomSeatMapper;
import com.enuos.live.mapper.RoomUserMapper;
import com.enuos.live.result.BaseCodeMsg;
import com.enuos.live.result.Result;
import com.enuos.live.service.RoomLuckDrawService;
import com.enuos.live.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ClassName RoomLuckDrawServiceImpl
 * @Description: TODO 幸运抽奖
 * @Author xubin
 * @Date 2020/6/16
 * @Version V1.0
 **/
@Slf4j
@Service
public class RoomLuckDrawServiceImpl implements RoomLuckDrawService {

    @Autowired
    private RoomUserMapper roomUserMapper;

    @Autowired
    private RoomSeatMapper roomSeatMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private UserFeign userFeign;

    /**
     * @MethodName: start
     * @Description: TODO 开始抽奖
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date:
     **/
    @Override
    public Result start(Map<String, Object> params) {
        Integer target = (Integer) params.get("target"); // 选择目标  1:麦上全员, 2:房间所有关注用户, 3:房间所有用户
        Integer numOfWinners = (Integer) params.get("numOfWinners"); //  中奖人数
        Long roomId = Long.valueOf(params.get("roomId").toString()); //  房间ID
        Long userId = Long.valueOf(params.get("userId").toString()); //  发起人ID(房主)

        List<Map<String, Object>> list = null;
        switch (target) {
            case 1:
                // 获取麦上全员
                list = roomSeatMapper.getRoomSeatUsers(roomId);
                break;
            case 2:
                // 获取房间内所有关注用户
                list = roomMapper.getRelationUsers(roomId);
                Map<String, Object> userMap = userFeign.getUserBase(userId, "nickName", "thumbIconUrl", "sex");
                userMap.put("userId", userId); // 添加房主
                list.add(userMap);
                break;
            case 3:
                // 获取房间所有用户id
                list = roomUserMapper.getRoomUserList(roomId);
                break;
            default:
                log.error("抽奖目标为空, roomId=[{}]", roomId);
                return Result.error(ErrorCode.DATA_ERROR);
        }
        if (ObjectUtil.isNotEmpty(list)) {
            if (list.size() < numOfWinners) {
                return Result.error(2012, "参与用户不足");
            }
            Map<String, Object> result = new HashMap<>();
            result.put("roomId", roomId);
            result.put("userId", userId);
            result.put("target", list);
            result.put("targetNum", list.size());

            if (list.size() == numOfWinners) {
                result.put("winning", list);
                result.put("winningNum", list.size());
            } else {
                Set set = randomUsers(list, numOfWinners);
                result.put("winning", set);
                result.put("winningNum", set.size());
            }
            return Result.success(result); // 返回抽奖结果信息
        }
        return Result.error(ErrorCode.DATA_ERROR);
    }

    /**
     * @MethodName: randomUsers
     * @Description: TODO 抽奖方法
     * @Param: [list: 目标数组, num: 中奖个数]
     * @Return: java.util.Set 返回结果
     * @Author: xubin
     * @Date: 2020年6月16日 17:16:39
     **/
    public static Set randomUsers(List list, Integer num) {
        Set numSet = new HashSet();
        while (numSet.size() < num) {
            int index = (int) Math.floor(Math.random() * list.size());
            if (!numSet.contains(list.get(index))) {
                numSet.add(list.get(index));
            }
        }
        return numSet;
    }

}
