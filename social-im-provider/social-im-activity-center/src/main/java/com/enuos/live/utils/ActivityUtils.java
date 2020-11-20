package com.enuos.live.utils;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.result.Result;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * @Description 活动相关工具类
 * @Author wangyingjie
 * @Date 2020/9/17
 * @Modified
 */
@Component
public class ActivityUtils {

    /**
     * @Description: 是否活动期内
     * @Param: [startTime, endTime]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/14
     */
    public Result isBetween(String startTime, String endTime) {
        LocalDate currentDate = DateUtils.getCurrentDate();
        LocalDate startDate = LocalDate.parse(startTime);
        LocalDate endDate = LocalDate.parse(endTime);

        if (currentDate.isBefore(startDate)) {
            return Result.error(ErrorCode.ACTIVITY_NOT_START);
        } else if (currentDate.isAfter(endDate)) {
            return Result.error(ErrorCode.ACTIVITY_YET_END);
        } else {
            return null;
        }
    }
}
