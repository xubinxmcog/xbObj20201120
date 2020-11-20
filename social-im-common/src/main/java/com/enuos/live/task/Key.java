package com.enuos.live.task;

import com.enuos.live.constants.RedisKey;
import com.enuos.live.utils.DateUtils;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/10/27
 * @Modified
 */
public class Key {

    public static String getTaskDay(Long userId) {
        return new StringBuilder(RedisKey.KEY_TASK_DAY).append(userId).append(":").append(DateUtils.getLocalDateOfPattern()).toString();
    }

    public static String getTaskWeek(Long userId) {
        return new StringBuilder(RedisKey.KEY_TASK_WEEK).append(userId).append(":").append(DateUtils.getLocalDateOfPattern(DateUtils.getThisWeekBegin())).toString();
    }

    public static String getTaskActive(Long userId) {
        return new StringBuilder(RedisKey.KEY_TASK_ACTIVE).append(userId).append(":").append(DateUtils.getLocalDateOfPattern(DateUtils.getThisWeekBegin())).toString();
    }
}
