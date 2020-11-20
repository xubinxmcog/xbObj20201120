package com.enuos.live.rest.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.rest.RoomRemote;
import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @ClassName RoomRemoteFallback
 * @Description: TODO
 * @Author xubin
 * @Date 2020/5/14
 * @Version V1.0
 **/
@Slf4j
@Component
public class RoomRemoteFallback implements RoomRemote {
    @Override
    public Result getRoomInfoResult(Long roomId) {
        return Result.error(ErrorCode.NETWORK_ERROR);
    }

    @Override
    public List<Long> getRoomUserIdList(Long roomId) {
        return null;
    }

    @Override
    public Map<String, Object> isBanned(Long roomId, Long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("msg", "");
        map.put("isBanned", 0);
        return map;
    }

    @Override
    public Result out(Long roomId, Long userId) {
        return null;
    }

    @Override
    public Result endBroadcast(Long roomId) {
        return null;
    }

    @Override
    public int roomTaskHandler(String code, Long userId, Long roomId) {
        log.error("语音房任务达成异常code=[{}], userId=[{}], roomId=[{}]", code, userId, roomId);
        return 0;
    }

    @Override
    public Integer getOnlineNum(Long roomId) {
        log.error("获取房间人数异常,roomId=[{}]", roomId);
        return 0;
    }

    @Override
    public Integer getVoiceRoomUserRole(Long userId, Long roomId) {
        log.error("语音房获取房间用户角色异常,userId=[{}],roomId=[{}]", userId, roomId);
        return 0;
    }

    @Override
    public String enterEffects(Long userId) {
        log.error("获取用户语音房进场特效异常,userId=[{}]", userId);
        return "";
    }
}
