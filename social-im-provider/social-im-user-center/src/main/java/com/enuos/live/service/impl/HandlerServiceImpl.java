package com.enuos.live.service.impl;

import com.enuos.live.feign.ActivityFeign;
import com.enuos.live.service.HandlerService;
import com.enuos.live.service.InviteService;
import com.enuos.live.task.Key;
import com.enuos.live.task.Task;
import com.enuos.live.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/10/28
 * @Modified
 */
@Service
@Slf4j
public class HandlerServiceImpl implements HandlerService {

    @Autowired
    private ActivityFeign activityFeign;

    @Autowired
    private InviteService inviteService;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * @Description: 日常
     * @Param: [task]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/10/28
     */
    @Async
    @Override
    public void dailyTask(Task task) {
        log.info("TASK[TD] Handler begin");
        if (task == null) {
            return;
        }

        Long userId = task.getUserId();
        Integer value = task.getValue();
        String name = task.getTemplate().NAME;
        String code = task.getTemplate().CODE;
        String target = task.getTarget();
        if (StringUtils.isNotBlank(target)) {
            code = code.concat(".").concat(target);
        }

        log.info("TASK[TD] Handler begin, params:[userId:{}, value:{}, code:{}, name:{}]", userId, value, code, name);

        String dayKey = Key.getTaskDay(userId);
        String weekKey = Key.getTaskWeek(userId);

        // 记录日次数
        if (redisUtils.hasHashKey(dayKey, code)) {
            redisUtils.incrHash(dayKey, code, value);
        } else {
            redisUtils.setHash(dayKey, code, value, 7, TimeUnit.DAYS);
        }

        // 记录周次数
        if (redisUtils.hasHashKey(weekKey, code)) {
            redisUtils.incrHash(weekKey, code, value);
        } else {
            redisUtils.setHash(weekKey, code, value, 7, TimeUnit.DAYS);
        }

        // 自动领奖
        autoTask(userId, code);

        log.info("TASK[TD] Handler end");
    }

    /**
     * @Description: 自动领奖[乐享令状，新人邀请]
     * @Param: [userId, templateCode]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/11/5
     */
    private void autoTask(Long userId, String templateCode) {
        // 乐享令状自动领奖
        activityFeign.dailyTaskOfWrit(userId, templateCode);
        // 新人邀请自动领奖
        inviteService.newbieTask(userId, templateCode);
    }
}
