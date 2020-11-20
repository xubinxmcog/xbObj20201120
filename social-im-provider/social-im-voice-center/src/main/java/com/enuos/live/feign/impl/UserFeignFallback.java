package com.enuos.live.feign.impl;

import com.enuos.live.feign.UserFeign;
import com.enuos.live.result.Result;
import com.enuos.live.task.Task;
import com.enuos.live.task.TemplateEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @ClassName UserFeignFallback
 * @Description: TODO
 * @Author xubin
 * @Date 2020/7/14
 * @Version V2.0
 **/
@Slf4j
@Component
public class UserFeignFallback implements UserFeign {
    @Override
    public Integer isAuthentication(Long userId) {
        return null;
    }

    @Override
    public Map<String, Object> getUserBase(Long userId, String... keys) {
        return null;
    }

    @Override
    public void achievementHandlers(Map<String, Object> params) {
        log.error("语音房调用成就异常！！！params：【{}】", params);
    }

    @Override
    public void taskHandler(Map<String, Object> params) {
        log.error("任务达成异常");
    }

    @Override
    public Result addGrowth(Long userId, Integer growth) {
        log.error("会员消费钻石加成长值");
        return null;
    }

    @Override
    public Result countExp(Map<String, Object> params) {
        log.error("添加经验值失败");
        return null;
    }

    @Override
    public void handlerOfDailyTask(Long userId, Integer value, TemplateEnum template) {
        log.error("日常任务处理失败");
    }

    @Override
    public void dailyTask(Task task) {
        log.error("任务处理失败-dailyTask");
    }

    @Override
    public Result rewardHandler(Map<String, Object> params) {
        return null;
    }
}
