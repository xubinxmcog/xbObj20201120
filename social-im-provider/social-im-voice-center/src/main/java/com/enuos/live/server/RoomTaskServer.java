package com.enuos.live.server;

import com.enuos.live.constants.RedisKey;
import com.enuos.live.feign.ActivityFeign;
import com.enuos.live.feign.UserFeign;
import com.enuos.live.manager.ActivityEnum;
import com.enuos.live.mapper.VoiceRoomUserTimeMapper;
import com.enuos.live.task.TemplateEnum;
import com.enuos.live.utils.RedisUtils;
import com.enuos.live.utils.TimeDateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @ClassName RoomTaskServer
 * @Description: TODO
 * @Author xubin
 * @Date 2020/7/31
 * @Version V2.0
 **/
@Slf4j
@Component
public class RoomTaskServer {

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ActivityFeign activityFeign;

    @Resource(name = "taskFxbDrawExecutor")
    ExecutorService executorService;

    @Autowired
    private VoiceRoomUserTimeMapper voiceRoomUserTimeMapper;

    /**
     * @MethodName: taskHandler
     * @Description: TODO 语音房任务达成.
     * @Param: [code, userId, roomId]
     * @Return: void
     * @Author: xubin
     * @Date: 9:49 2020/7/31
     **/
    public void taskHandler(String code, Long userId, Long roomId) {

        String key = RedisKey.KEY_ROOM_TASK + code + "_" + userId;
        if (!redisUtils.hasKey(key)) {
            log.info("语音房任务达成code=[{}],userId=[{}],roomId=[{}]", code, userId, roomId);
            Map<String, Object> map = new HashMap<String, Object>() {
                {
                    put("userId", userId);
                    put("code", code);
                    put("progress", 1);
                    put("isReset", 0);
                }
            };
            executorService.submit(() -> {
                userFeign.taskHandler(map);
            });
            redisUtils.set(key, roomId, TimeDateUtils.getDaySurplusTime());
        }

    }

    /**
     * @MethodName: roomActivity
     * @Description: TODO 活动
     * @Param: []
     * @Return: void
     * @Author: xubin
     * @Date: 13:17 2020/9/25
     **/
    public void roomActivity(String code, Long userId) {
        Map<String, Object> map = new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("code", code);
                put("progress", 1);
            }
        };
        activityFeign.openHandler(map);
    }

    /**
     * @MethodName: roomActivity
     * @Description: TODO 玩语音房20 分钟
     * @Param: [userId]
     * @Return: void
     * @Author: xubin
     * @Date: 15:03 2020/9/25
     **/
    public void room20MinActivity(Long userId) {
        String code = ActivityEnum.ACT000104.getCode();
        String key = RedisKey.KEY_ROOM_TASK + code + "_" + userId;
        if (!redisUtils.hasKey(key)) {

            Double roomAddUpMin = voiceRoomUserTimeMapper.getRoomAddUpMin(userId);

            if (!Objects.isNull(roomAddUpMin)) {
                log.info("用户当天累计在语音房时间=[{}]分钟", roomAddUpMin);
                if (roomAddUpMin >= 20) {
                    roomActivity(code, userId);
                    redisUtils.set(key, userId, TimeDateUtils.getDaySurplusTime());
                }
                userFeign.handlerOfDailyTask(userId, roomAddUpMin.intValue(), TemplateEnum.V06);
            }
        }

    }

    // 倒计时
    private static void time() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                midTime--;
                long hh = midTime / 60 / 60 % 60;
                long mm = midTime / 60 % 60;
                long ss = midTime % 60;
                System.out.println("还剩" + hh + "小时" + mm + "分钟" + ss + "秒");
            }
        }, 0, 1000);

    }

    public static long midTime;

    public static void main(String[] args) {
        midTime = 5; // 倒计时多少秒
        time();
    }
}
