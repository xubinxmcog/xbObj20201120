package com.enuos.live.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @ClassName TimeDateUtils
 * @Description: TODO
 * @Author xubin
 * @Date 2020/9/2
 * @Version V2.0
 **/
public class TimeDateUtils {

    public final static String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取一个简单的日期格式化对象
     *
     * @return 一个简单的日期格式化对象
     */
    private static SimpleDateFormat getFormatter(String parttern) {
        return new SimpleDateFormat(parttern);
    }

    /**
     * 日期格式化－将 Date 类型的日期格式化为 String 型
     *
     * @param date    待格式化的日期
     * @param pattern 时间样式
     * @return 一个被格式化了的 String 日期
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return "";
        } else {
            return getFormatter(pattern).format(date);
        }
    }

    /**
     * 获取指定毫秒数所表示的日期
     *
     * @param millis millis the new time in UTC milliseconds from the epoch.
     * @return Date 日期
     */
    public static Date getDate(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.getTime();
    }

    /**
     * 获取指定毫秒数所表示的日期时间字符串，样式: yyyy-MM-dd HH:mm:ss
     *
     * @param millis millis the new time in UTC milliseconds from the epoch.
     * @return 一个包含年月日时分秒的<code>String</code>型日期时间字符串，格式：yyyy-MM-dd HH:mm:ss
     */
    public static String getDateTimeStr(long millis) {
        return format(getDate(millis), yyyy_MM_dd_HH_mm_ss);
    }

    /**
     * @MethodName: getesterdayY
     * @Description: TODO 获取昨天0点的时间字符串，样式: yyyy-MM-dd HH:mm:ss
     * @Param: []
     * @Return: java.lang.String
     * @Author: xubin
     * @Date: 17:27 2020/9/2
     **/
    public static String getYesterdayZeroPoint() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) - 1, 0, 0, 0);
        return getDateTimeStr(calendar.getTime().getTime());
    }

    /**
     * @MethodName: getYesterdayLastSecond
     * @Description: TODO 获取昨天23:59:59 秒的时间字符串，样式: yyyy-MM-dd HH:mm:ss
     * @Param: []
     * @Return: java.lang.String
     * @Author: xubin
     * @Date: 17:32 2020/9/2
     **/
    public static String getYesterdayLastSecond() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) - 1, 23, 59, 59);
        return getDateTimeStr(calendar.getTime().getTime());
    }

    /**
     * @MethodName: getTodayZeroPoint
     * @Description: TODO 获取今天0点的时间字符串，样式: yyyy-MM-dd HH:mm:ss
     * @Param: []
     * @Return: java.lang.String
     * @Author: xubin
     * @Date: 17:35 2020/9/2
     **/
    public static String getTodayZeroPoint() {
        Long time = System.currentTimeMillis();  //当前时间的时间戳
        long zero = time / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();
        return getDateTimeStr(zero);
    }

    /**
     * @MethodName: getDaySurplusTime
     * @Description: TODO 获取当天剩余时间
     * @Param: []
     * @Return: long 返回秒值
     * @Author: xubin
     * @Date: 19:01 2020/7/30
     **/
    public static long getDaySurplusTime() {
        LocalDateTime midnight = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
//        long millSeconds = ChronoUnit.MILLIS.between(LocalDateTime.now(), midnight);
        long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), midnight);
        return seconds == 0 ? 86400L : seconds;
    }

    /**
     * @Description: 转date
     * @Param: [str]
     * @Return: java.util.Date
     * @Author: wangyingjie
     * @Date: 2020/5/20
     */
    public static Date strToDate(String str) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;

        try {
            date = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
