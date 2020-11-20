package com.enuos.live.feign.impl;

import com.enuos.live.feign.UserFeign;
import com.enuos.live.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Description 异常处理
 * @Author wangyingjie
 * @Date 2020/9/3
 * @Modified
 */
@Slf4j
@Component
public class UserFeignFallback implements UserFeign {

    /**
     * @Description: 获取用户
     * @Param: [userId, keys]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/9/3
     */
    @Override
    public Map<String, Object> getUserBase(Long userId, String... keys) {
        log.error("==========[SOCIAL-IM-POST UserFeign getUserBase error, params=[userId={}, keys={}]]", userId, keys);
        return null;
    }

    /**
     * @Description: 成就进度统一处理
     * @Param: [params]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/13
     */
    @Override
    public void achievementHandlers(Map<String, Object> params) {
        log.error("==========[SOCIAL-IM-POST UserFeign achievementHandlers error, params={}]", params);
    }

    /**
     * @Description: 匹配敏感词，全匹配
     * @Param: [txt]
     * @Return: java.lang.Boolean
     * @Author: wangyingjie
     * @Date: 2020/7/24
     */
    @Override
    public Boolean matchWords(String words) {
        log.error("==========[SOCIAL-IM-POST UserFeign matchWords error, words={}]", words);
        return false;
    }

    /**
     * @Description: 日常任务处理
     * @Param: [userId, value, template]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/10/29
     */
    @Override
    public void handlerOfDailyTask(Task task) {
        log.error("==========[SOCIAL-IM-POST UserFeign handlerOfDailyTask error, params:[userId:{}, value:{}, template:{}]]", task.getUserId(), task.getValue(), task.getTemplate());
    }

}
