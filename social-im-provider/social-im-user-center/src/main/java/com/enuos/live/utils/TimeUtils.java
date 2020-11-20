package com.enuos.live.utils;

import com.enuos.live.manager.PatternEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description 时间工具类
 * @Author wangyingjie
 * @Date 2020/6/19
 * @Modified
 */
@Slf4j
public class TimeUtils {

    private static String[] HMS = {"H", "m", "s"};

    /**
     * @Description: 返回时间区间
     * @Param: [startTime, endTime, space, time]
     * @Return: java.util.List<java.lang.String>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    public static List<String> getBetween(LocalDateTime startTime, LocalDateTime endTime, long space, String time) {
        List<String> result = new ArrayList<>();

        if (!ArrayUtils.contains(HMS, time)) {
            log.error("[TimeUtils-getBetween] the [{}] validators fail", time);
            return null;
        }

        while (startTime.isBefore(endTime)) {
            result.add(startTime.format(DateTimeFormatter.ofPattern(PatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())));
            switch (time) {
                case "H":
                    startTime = startTime.plusHours(space);
                    break;
                case "m":
                    startTime = startTime.plusMinutes(space);
                    break;
                case "s":
                    startTime = startTime.plusSeconds(space);
                    break;
                default:
                    break;
            }
        }

        return result;
    }

}

