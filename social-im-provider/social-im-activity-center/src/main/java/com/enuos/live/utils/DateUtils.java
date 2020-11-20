package com.enuos.live.utils;

import com.enuos.live.manager.PatternEnum;
import com.enuos.live.manager.ZoneEnum;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/19
 * @Modified
 */
public class DateUtils {

    private static final String YYYY_MM_DD = "yyyy-MM-dd";

    private static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    
    /** 
     * @Description: 格式化日期 
     * @Param: [strDate] 
     * @Return: java.util.Date 
     * @Author: wangyingjie
     * @Date: 2020/6/19 
     */ 
    public static Date parse(String strDate) {
        return parse(strDate, YYYY_MM_DD);
    }

    /** 
     * @Description: 格式化日期 
     * @Param: [strDate, pattern] 
     * @Return: java.util.Date 
     * @Author: wangyingjie
     * @Date: 2020/6/19 
     */ 
    public static Date parse(String strDate, String pattern) {
        try {
            return getFormatter(pattern).parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /** 
     * @Description: 格式化日期
     * @Param: [parttern] 
     * @Return: java.text.SimpleDateFormat 
     * @Author: wangyingjie
     * @Date: 2020/6/19 
     */ 
    private static SimpleDateFormat getFormatter(String parttern) {
        return new SimpleDateFormat(parttern);
    }
    
    /**
     * @Description: 是否同一天
     * @Param: [a, b]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    public static boolean isThisDay(Date a, Date b) {
        SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD);
        if (Objects.equals(sdf.format(a), sdf.format(b))) {
            return true;
        }
        return false;
    }

    /**
     * @Description: 判断是否小于等于当前时间
     * @Param: [date]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    public static boolean isPastDate(Date date) {
        return isPastDate(date, new Date());
    }

    /**
     * @Description: 判断是否小于等于目标时间
     * @Param: [date, target]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    public static boolean isPastDate(Date date, Date target) {
        SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD);
        if ((!sdf.format(target).equals(sdf.format(date))) && date.before(target)) {
            return true;
        }
        return false;
    }

    /**
     * @Description: 获取相差天数
     * @Param: [first, end]
     * @Return: java.lang.Long
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    public static Long getBetweenDays(Long first, Long end) {
        return (end - first) / (24 * 60 * 60 * 1000);
    }

    /**
     * @Description: 获取相差天数
     * @Param: [object]
     * @Return: java.util.Date
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    public static Date objectToDate(Object object) {
        SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD);
        Date date = null;
        try {
            date = sdf.parse(object.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * @Description: 获取两个日期中间的日期列表
     * @Param: [startDate, endDate]
     * @Return: java.util.List<java.lang.String>
     * @Author: wangyingjie
     * @Date: 2020/5/20
     */
    public static List<String> getDateIntervalList(Date startDate, Date endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<String> dateList = new ArrayList<>();

        Calendar calBegin = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calBegin.setTime(startDate);

        // 测试此日期是否在指定日期之后
        while (endDate.compareTo(calBegin.getTime()) >= 0) {
            dateList.add(sdf.format(calBegin.getTime()));
            calBegin.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dateList;
    }

    /**
     * @Description: 获取两个日期中间的日期列表
     * @Param: [startDate, endDate]
     * @Return: java.util.List<java.lang.String>
     * @Author: wangyingjie
     * @Date: 2020/5/20
     */
    public static List<String> getDateIntervalList(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD);
        List<String> dateList = new ArrayList<>();

        Calendar calBegin = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calBegin.setTime(parse(startDate));

        // 测试此日期是否在指定日期之后
        while (parse(endDate).compareTo(calBegin.getTime()) >= 0) {
            dateList.add(sdf.format(calBegin.getTime()));
            calBegin.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dateList;
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

    /**
     * @Description: 本周日期
     * @Param: []
     * @Return: java.util.List<java.lang.String> 
     * @Author: wangyingjie
     * @Date: 2020/6/9 
     */
    public static List<String> getThisWeekDays() {
        LocalDate localDate = LocalDate.now();
        int day = localDate.getDayOfWeek().getValue();

        String start = localDate.plusDays(1-day).toString();
        String end = localDate.plusDays(7-day).toString();
        return getDateIntervalList(start, end);
    }

    /**
     * @Description: 本周第一天
     * @Param: []
     * @Return: java.util.Date
     * @Author: wangyingjie
     * @Date: 2020/6/12 
     */
    public static LocalDate getThisWeekBegin() {
        LocalDate localDate = getCurrentDate();
        int day = localDate.getDayOfWeek().getValue();
        return localDate.plusDays(1-day);
    }

    /**
     * @Description: 周第一天
     * @Param: [localDate]
     * @Return: java.time.LocalDate
     * @Author: wangyingjie
     * @Date: 2020/8/18
     */
    public static LocalDate getThisWeekBegin(LocalDate localDate) {
        int day = localDate.getDayOfWeek().getValue();
        return localDate.plusDays(1-day);
    }

    /**
     * @Description: 本周第一天
     * @Param: []
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/6/12
     */
    public static LocalDate getWeekBegin(LocalDate... localDates) {
        LocalDate localDate;
        if (localDates.length == 0) {
            localDate = getCurrentDate();
        } else {
            localDate = localDates[0];
        }
        int day = localDate.getDayOfWeek().getValue();
        return localDate.plusDays(1-day);
    }

    /**
     * @Description: 本周最后一天
     * @Param: []
     * @Return: java.util.Date
     * @Author: wangyingjie
     * @Date: 2020/6/12 
     */
    public static LocalDate getThisWeekEnd() {
        LocalDate localDate = getCurrentDate();
        int day = localDate.getDayOfWeek().getValue();
        return localDate.plusDays(7-day);
    }

    /**
     * @Description: 周最后一天
     * @Param: [localDate]
     * @Return: java.time.LocalDate
     * @Author: wangyingjie
     * @Date: 2020/8/18
     */
    public static LocalDate getThisWeekEnd(LocalDate localDate) {
        int day = localDate.getDayOfWeek().getValue();
        return localDate.plusDays(7-day);
    }

    /** 
     * @Description: 日期 
     * @Param: [date] 
     * @Return: java.time.LocalDateTime 
     * @Author: wangyingjie
     * @Date: 2020/7/15 
     */ 
    public static LocalDateTime getLocalDateTime(String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS));
    }
    
    /** 
     * @Description: 日期 
     * @Param: [date] 
     * @Return: java.time.LocalDate 
     * @Author: wangyingjie
     * @Date: 2020/7/23
     */ 
    public static LocalDate getLocalDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(YYYY_MM_DD));
    }

    /**
     * @Description: 当前日期
     * @Param: []
     * @Return: java.time.LocalDateTime
     * @Author: wangyingjie
     * @Date: 2020/8/7
     */
    public static LocalDate getCurrentDate() {
        return LocalDate.now(ZoneOffset.of(ZoneEnum.E8.getZone()));
    }

    /** 
     * @Description: 当前日期
     * @Param: [] 
     * @Return: java.time.LocalDateTime 
     * @Author: wangyingjie
     * @Date: 2020/8/7 
     */ 
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now(ZoneOffset.of(ZoneEnum.E8.getZone()));
    }

    /**
     * @Description: 当前日期
     * @Param: []
     * @Return: java.time.LocalDateTime
     * @Author: wangyingjie
     * @Date: 2020/8/7
     */
    public static LocalDateTime getCurrentDateTimeOfPattern() {
        return LocalDateTime.parse(LocalDateTime.now(ZoneOffset.of(ZoneEnum.E8.getZone())).format(DateTimeFormatter.ofPattern(PatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())), DateTimeFormatter.ofPattern(PatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
    }

    /** 
     * @Description: 日期前缀
     * @Param: [] 
     * @Return: java.lang.String 
     * @Author: wangyingjie
     * @Date: 2020/8/12 
     */ 
    public static String getPrefixOfCurrentDate() {
        return getCurrentDate().format(DateTimeFormatter.ofPattern(PatternEnum.YYYYMMDD.getPattern())).concat("_");
    }

    /**
     * @Description: 日期前缀
     * @Param: []
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/8/12
     */
    public static String getPrefixOfLocalDate(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern(PatternEnum.YYYYMMDD.getPattern())).concat("_");
    }

    /** 
     * @Description: 后缀 
     * @Param: [] 
     * @Return: java.lang.String 
     * @Author: wangyingjie
     * @Date: 2020/10/27 
     */ 
    public static String getSuffixOfCurrentDate() {
        return getCurrentDate().format(DateTimeFormatter.ofPattern(PatternEnum.YYYYMMDD.getPattern()));
    }

    /** 
     * @Description: 后缀
     * @Param: [localDate] 
     * @Return: java.lang.String 
     * @Author: wangyingjie
     * @Date: 2020/10/27 
     */ 
    public static String getSuffixOfLocalDate(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern(PatternEnum.YYYYMMDD.getPattern()));
    }
}
