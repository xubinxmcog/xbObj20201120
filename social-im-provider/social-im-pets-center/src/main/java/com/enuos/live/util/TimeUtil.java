package com.enuos.live.util;

import cn.hutool.core.util.StrUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName TimeUtil
 * @Description: TODO 时间处理
 * @Author xubin
 * @Date 2020/6/5
 * @Version V1.0
 **/
public class TimeUtil {

    /**
     * 获取过期时间
     *
     * @param time 秒值
     * @return
     */
    public static String getExpire(long time) {

        long s = 60;
        long m = 60;
        long h = s * m;
        long d = h * 24;

//        StringBuffer sb = new StringBuffer();
        long days = time / d;


        String dayStr = days == 0 ? "" : days + "天";
        if (StrUtil.isNotEmpty(dayStr)) {
            return dayStr;
        }
        long hours = (time % d) / h;
        String hourStr = hours == 0 ? "" : hours + "小时";
        if (StrUtil.isNotEmpty(hourStr)) {
            return hourStr;
        }
        long minutes = (time % h) / m;
        String minuteStr = minutes == 0 ? "" : minutes + "分钟";
        if (StrUtil.isNotEmpty(minuteStr)) {
            return minuteStr;
        }
        long seconds = time % s;
        String secondStr = seconds == 0 ? "" : seconds + "秒";
        if (StrUtil.isNotEmpty(secondStr)) {
            return secondStr;
        }
//        sb.append(days == 0 ? "" : days + "天").append(hours == 0 ? "" : hours + "小时").append(minutes == 0 ? "" : minutes + "分").append(seconds == 0 ? "" : seconds + "秒");

        return "1秒";
    }

    /**
     * @MethodName: millisDateStr
     * @Description: TODO 毫秒转换为字符串格式日期
     * @Param: []       
     * @Return: java.lang.String
     * @Author: xubin
     * @Date: 14:12 2020/8/11
    **/
    public static String millisDateStr(long time){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date = new Date(time);
        return format.format(date);
    }

}
