package com.enuos.live.aop;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.enuos.live.annotations.RoomUserActivityNote;
import com.enuos.live.mapper.RoomMapper;
import com.enuos.live.pojo.SeatPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @ClassName RoomUserActivityNoteAspect
 * @Description: TODO 语音房用户活跃度
 * @Author xubin
 * @Date 2020/11/5
 * @Version V2.0
 **/
@Slf4j
@Aspect
@Component
public class RoomUserActivityNoteAspect {

    @Resource(name = "taskFxbDrawExecutor")
    ExecutorService executorService;

    @Autowired
    private RoomMapper roomMapper;

    @Pointcut("@annotation(roomUserActivityNote)")
    public void pointCut(RoomUserActivityNote roomUserActivityNote) {
    }

    @AfterReturning(value = "pointCut(roomUserActivityNote)", returning = "keys")
    public void around(JoinPoint point, RoomUserActivityNote roomUserActivityNote, Object keys) {
        int activityType = roomUserActivityNote.activityType();
        try {
            Object o = keys;
            List<Object> args = Arrays.asList(point.getArgs());
//            log.info("语音房用户活跃度,args=[{}]", args);
            executorService.submit(() -> {
                SeatPO seatPO = null;
                if (1 == activityType) {
                    seatPO = new SeatPO();
                    seatPO.setUserId((Long) args.get(1));
                    seatPO.setRoomId((Long) args.get(2));
                } else {
                    seatPO = (SeatPO) args.get(0);
                }
                if (ObjectUtil.isNotEmpty(seatPO)) {
                    Map<String, Object> map = new HashMap<>();
                    // 语音房用户活跃度
                    if (1 == activityType) {
                        map.put("userId", seatPO.userId);
                        map.put("roomId", seatPO.getRoomId());
                        map.put("activityType", activityType); // 活跃类型: 1:聊天 2:上麦
                    } else if (null == seatPO.getIsJoin() || 0 != seatPO.getIsJoin()) {
                        map.put("userId", seatPO.userId);
                        map.put("roomId", seatPO.getRoomId());
                        map.put("activityType", activityType); // 活跃类型: 1:聊天 2:上麦
                    }
                    if (MapUtil.isNotEmpty(map)) {
                        log.info("保存语音房用户活跃度,userId=[{}],roomId=[{}], activityType=[{}]", seatPO.userId, seatPO.getRoomId(), activityType);
                        int i = roomMapper.saveVoiceRoomUserActivity(map);
                        if (0 == i) {
                            roomMapper.updateVoiceRoomUserActivity(map);
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
