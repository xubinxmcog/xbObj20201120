package com.enuos.live.utils;

import cn.hutool.core.util.StrUtil;
import com.enuos.live.manager.PatternEnum;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 日期工具类
 *
 * @author WangCaiWen
 * Created on 2019/10/21 13:42
 */

public class DateUtils {

    public final static String yyyy = "yyyy";

    public final static String MM = "MM";

    public final static String dd = "dd";

    public final static String yyyy_MM_dd = "yyyy-MM-dd";

    public final static String yyyy_MM_dd_CH = "yyyy年MM月dd日";

    public final static String yyyyMMdd = "yyyyMMdd";

    public final static String yyyy_MM = "yyyy-MM";

    public final static String yyyyMM = "yyyyMM";

    public final static String HH_mm_ss = "HH:mm:ss";

    public final static String HH_mm = "HH:mm";

    public final static String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";

    public final static String yyyyMMdd_HH_mm_ss = "yyyyMMdd HH:mm:ss";

    public final static String yyyyMMddHHmmss = "yyyyMMddHHmmss";

    public final static String yyyy_MM_dd_HH_mm = "yyyy-MM-dd HH:mm";

    public final static String yyyyMMddHHmm = "yyyyMMddHHmm";

    public final static String yyyy_MM_dd_HH_mm_ss_SSS = "yyyy-MM-dd HH:mm:ss.SSS";

    public final static String yyyyMMddHHmmssSSS = "yyyyMMddHHmmssSSS";

    /**
     * 默认为yyyy-MM-dd的格式化
     *
     * @param date 待格式化的日期
     * @return String
     */
    public static String format(Date date) {
        return format(date, yyyy_MM_dd);
    }

    /**
     * 得到yyyy年MM月dd日的格式化字符串
     *
     * @param date 待格式化的日期
     * @return String
     */
    public static String formatCH(Date date) {
        return format(date, yyyy_MM_dd_CH);
    }

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
     * 默认为yyyy-MM-dd格式的解析
     *
     * @param strDate 待转换的字符串
     * @return 日期
     */
    public static Date parse(String strDate) {
        return parse(strDate, yyyy_MM_dd);
    }

    /**
     * 日期解析－将 String 类型的日期解析为 Date 型
     *
     * @param strDate 待解析的日期字符串
     * @param pattern 日期格式
     *                ParseException 如果所给的字符串不能被解析成一个日期
     * @return 一个被格式化了的 Date 日期
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
     * 获取日期(java.util.Date)
     *
     * @return Date 日期
     */
    public static Date getCurrDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    /**
     * 获取当天，从0点起算
     *
     * @return Date 日期
     */
    public static Date getCurrDay() {
        Date currentDate = getCurrDate();
        return parse(format(currentDate));
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
     * 获取间隔日期
     *
     * @param date      基准日期
     * @param field     指定日期字段
     * @param intervals 间隔数
     * @return Date 日期
     */
    public static Date getDate(Date date, int field, int intervals) {
        try {
            Calendar calendar = Calendar.getInstance();
            if (date != null) {
                calendar.setTime(date);
            }
            calendar.set(field, calendar.get(field) + intervals);
            return calendar.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取当前日期字符串
     *
     * @return 一个包含年月日 String 型日期，yyyyMMdd
     */
    public static String getCurrDateStr() {
        return format(getCurrDate(), yyyy_MM_dd);
    }

    /**
     * 获取当前日期时间字符串，格式: yyyy-MM-dd HH:mm:ss
     *
     * @return 一个包含年月日时分秒的 String 型日期时间字符串，格式：yyyy-MM-dd HH:mm:ss
     */
    public static String getCurrDateTimeStr() {
        return format(getCurrDate(), yyyy_MM_dd_HH_mm_ss);
    }

    public static String getCurrDateTimeMillsStr() {
        return format(getCurrDate(), yyyyMMddHHmmssSSS);
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
     * 获取当前年分 样式：yyyy
     *
     * @return 当前年分
     */
    public static String getYear() {
        return format(getCurrDate(), yyyy);
    }

    /**
     * 获取当前月分 样式：MM
     *
     * @return 当前月分
     */
    public static String getMonth() {
        return format(getCurrDate(), MM);
    }

    /**
     * 获取月份
     *
     * @param date      基准日期
     * @param intervals 间隔月数
     * @return Date 日期
     */
    public static Date getMonth(Date date, int intervals) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + intervals);
            return calendar.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取当前日期号 样式：dd
     *
     * @return 当前日期号
     */
    public static String getDay() {
        return format(getCurrDate(), dd);
    }

    /**
     * 获取日期
     *
     * @param date      基准日期
     * @param intervals 相隔天数
     * @return Date 日期
     */
    public static Date getDay(Date date, int intervals) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + intervals);
            return calendar.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 判断当前时间是一年中的第几天
     *
     * @return int
     */
    public static int getDayOfYear() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            return calendar.get(Calendar.DAY_OF_YEAR);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 判断当前时间是一天中的第几个小时
     *
     * @return int
     */
    public static int getHourOfday() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            return calendar.get(Calendar.HOUR_OF_DAY);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取两个日期相差天数
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return long
     */
    public static long getIntevalDays(String startDate, String endDate) {
        return getIntevalDays(parse(startDate, yyyy_MM_dd), parse(endDate, yyyy_MM_dd));
    }

    /**
     * 得到两个时间相差的天数
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return long
     */
    public static long getIntevalDays(Date startDate, Date endDate) {
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        endCalendar.setTime(endDate);
        long diff = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
        return (diff / (1000 * 60 * 60 * 24));
    }

    /**
     * 得到两个时间之间时间数组包括起始时间
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return string
     */
    public static String[] getBetweenDays(Date startDate, Date endDate) {
        String x[] = new String[(int) getIntevalDays(startDate, endDate) + 1];
        if (x.length > 0) {
            Date tem = startDate;
            x[0] = format(startDate);
            for (int i = 1; i < x.length; i++) {
                tem = getNextDay(tem);
                x[i] = format(tem);
            }
        }
        return x;
    }

    /**
     * 得到两个时间相差的年数
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return int
     */
    public static int getIntevalYears(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return -1;
        }
        Date start = startDate;
        Date end = endDate;
        if (start.after(end)) {
            start = endDate;
            end = startDate;
        }
        int intevals = 0;
        while (start.before(end)) {
            intevals++;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            calendar.add(Calendar.YEAR, 1);
            start = calendar.getTime();
        }
        return intevals;
    }

    /**
     * 得到两个时间相差的小时数
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return double
     */
    public static double getIntevalHours(Date startDate, Date endDate) {
        try {
            Calendar startCalendar = Calendar.getInstance();
            Calendar endCalendar = Calendar.getInstance();
            startCalendar.setTime(startDate);
            endCalendar.setTime(endDate);
            long diff = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
            return ((double) diff / (1000 * 60 * 60));
        } catch (Exception ee) {
            return 0.0;
        }
    }

    /**
     * 得到两个时间相差的分钟数
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return long
     */
    public static long getIntevalMinutes(Date startDate, Date endDate) {
        try {
            Calendar startCalendar = Calendar.getInstance();
            Calendar endCalendar = Calendar.getInstance();
            startCalendar.setTime(startDate);
            endCalendar.setTime(endDate);
            long diff = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
            return diff / (1000 * 60);
        } catch (Exception ee) {
            return 0;
        }
    }

    /**
     * 得到毫秒
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return long
     */
    public static long getMilliseconds(Date startDate, Date endDate) {
        try {
            Calendar startCalendar = Calendar.getInstance();
            Calendar endCalendar = Calendar.getInstance();
            startCalendar.setTime(startDate);
            endCalendar.setTime(endDate);
            long diff = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
            return diff;
        } catch (Exception ee) {
            return 0;
        }
    }

    /**
     * 获取当前日期所在月的第一天
     *
     * @return date
     */
    public static Date getFirstDayOfMonth() {
        return getFirstDayOfMonth(getCurrDate());
    }

    /**
     * 获取指定日期所在月的第一天
     *
     * @param date
     * @return
     */
    public static Date getFirstDayOfMonth(Date date) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.set(Calendar.DATE, 1); // 设为当前月的第一天
            return c.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取当前日期所在月的最后一天
     *
     * @return
     */
    public static Date getLastDayOfMonth() {
        return getLastDayOfMonth(getCurrDate());
    }

    /**
     * 获取指定日期所在月的最后一天
     *
     * @param date 日期
     * @return
     */
    public static Date getLastDayOfMonth(Date date) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.set(Calendar.MONTH, c.get(Calendar.MONTH) + 1); // 当前月加1变为下一个月
            c.set(Calendar.DATE, 1); // 设为下一个月的第一天
            c.add(Calendar.DATE, -1); // 减一天，变为本月最后一天
            return c.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 判断指定时间是否过期
     *
     * @param datetime 日期时间字符串
     * @param parttern 日期时间格式
     * @return
     */
    public static boolean isOverdue(String datetime, String parttern) {
        return DateUtils.parse(datetime, parttern)
                .before(DateUtils.getCurrDate());
    }

    /**
     * 以友好的方式显示过去的时间
     *
     * @param date 日期
     * @return
     */
    public static String friendlyTime(Date date) {
        // 计算时间差，单位：秒
        int ct = (int) ((System.currentTimeMillis() - date.getTime()) / 1000);
        if (ct < 3600) {
            return String.format("%d 分钟之前", ct / 60);
        }
        if (ct >= 3600 && ct < 86400) {
            return String.format("%d 小时之前", ct / 3600);
        }
        if (ct >= 86400 && ct < 2592000) { // 86400 * 30
            int day = ct / 86400;
            if (day > 1) {
                return String.format("%d 天之前", day);
            } else {
                return "昨天";
            }
        }
        if (ct >= 2592000 && ct < 31104000) { // 86400 * 30
            return String.format("%d 月之前", ct / 2592000);
        }
        return String.format("%d 年之前", ct / 31104000);
    }

    /**
     * 获取当前时间戳 <br>
     * 〈功能详细描述〉
     *
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static Timestamp getCurrTimeStamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * 返回日期对应的Timestamp <br>
     * 〈功能详细描述〉
     *
     * @param dateTime
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static Timestamp getTimeStamp(Date dateTime) {
        return new Timestamp(dateTime.getTime());
    }

    /**
     * 日期调整 <br>
     * 〈功能详细描述〉
     *
     * @param date
     * @param field
     * @param amount
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static Date add(Date date, int field, int amount) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(field, amount);
            return c.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取指定日期下一天
     */
    public static Date getNextDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        date = calendar.getTime();
        return date;
    }

    /**
     * 获取来个月份之间相差月份
     *
     * @param startDate 2014-02
     * @param endDate   2016-08
     * @return
     */
    public static int getIntevalMonth(String startDate, String endDate) {
        String s[] = startDate.split("-");
        String e[] = endDate.split("-");
        int xc = 0;
        if (!s[0].trim()
                .equals(e[0].trim())) {
            xc = Integer.parseInt(e[0].trim()) - Integer.parseInt(s[0].trim());
        }
        int size = xc * 12 + (Integer.parseInt(e[1].trim()) - Integer.parseInt(s[1].trim()));
        return size;
    }

    /**
     * 获取来个月份之间相所有月份（包括起始）
     *
     * @param startDate 2014-02
     * @param endDate   2016-08
     * @return
     */
    public static String[] getBetweenMonth(String startDate, String endDate) {
        String s[] = startDate.split("-");
        String e[] = endDate.split("-");
        int xc = 0;
        if (!s[0].trim()
                .equals(e[0].trim())) {
            xc = Integer.parseInt(e[0].trim()) - Integer.parseInt(s[0].trim());
        }
        int size = xc * 12 + (Integer.parseInt(e[1].trim()) - Integer.parseInt(s[1].trim())) + 1;
        String x[] = new String[size];
        int tem_y = Integer.parseInt(s[0].trim());
        int tem_m = Integer.parseInt(s[1].trim());
        x[0] = startDate;
        for (int i = 1; i < x.length; i++) {
            tem_m = tem_m + 1;
            if (tem_m > 12) {
                tem_y = tem_y + 1;
                tem_m = 1;
            }
            x[i] = tem_y + "-" + tem_m;
        }
        return x;
    }

    /**
     * 功能描述: <br>
     * 将两个日期段转化为job cron 表达式(为了适配当前竞拍需求，将凌晨12点以后时间移除)
     *
     * @param startDate
     * @param endDate
     * @param interval  间隔频率
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static String parseCronExpression(Date startDate, Date endDate, int interval) {
        String pattern = "interval minutes hour day month ? year";
        Map<String, Integer> startDateMap = convertDateToMap(startDate);
        Map<String, Integer> endDateMap = convertDateToMap(endDate);
        int sy = startDateMap.get("yyyy");
        int sM = startDateMap.get("MM");
        int sd = startDateMap.get("dd");
        int sh = startDateMap.get("HH");
        int sm = startDateMap.get("mm");
        int ed = endDateMap.get("dd");
        int eh = endDateMap.get("HH");
        int em = endDateMap.get("mm");
        pattern = pattern.replace("year", String.valueOf(sy));
        pattern = pattern.replace("month", String.valueOf(sM));
        pattern = pattern.replace("day", String.valueOf(sd));
        if (sd != ed) {
            pattern = pattern.replace("hour", buildPlaceholder(sh, 23));
            pattern = pattern.replace("minutes", buildPlaceholder(sm, 59));
        } else {
            pattern = pattern.replace("hour", buildPlaceholder(sh, eh));
            pattern = pattern.replace("minutes", buildPlaceholder(sm, em));
        }
        pattern = pattern.replace("interval", "0/" + interval);
        return pattern;
    }

    private static Map<String, Integer> convertDateToMap(Date date) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        String _startDate = DateUtils.format(date, "yyyy-MM-dd HH:mm:ss");
        String[] array = _startDate.split("\\s");
        String[] ymd = array[0].split("-");
        String[] hms = array[1].split(":");
        map.put("yyyy", Integer.parseInt(ymd[0]));
        map.put("MM", Integer.parseInt(ymd[1]));
        map.put("dd", Integer.parseInt(ymd[2]));
        map.put("HH", Integer.parseInt(hms[0]));
        map.put("mm", Integer.parseInt(hms[1]));
        map.put("ss", Integer.parseInt(hms[2]));
        return map;
    }

    private static String buildPlaceholder(int sd, int ed) {
        if (ed != sd) {
            return sd + "-" + ed;
        }
        return String.valueOf(sd);
    }

    /**
     * 功能描述: <br>
     * 暂时特定用于预约检测短信，其他模块慎用，后期再考虑通用实现
     *
     * @param baseDate
     * @param seconds
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static Date setupDelayByHour(Date baseDate, int seconds) {
        Date triggerStartTime = DateUtils.add(baseDate, Calendar.SECOND, -seconds);
        Date currentDate = new Date();
        // 短信平台少于5分钟的预约短信被认为是即时短信，立即发送，为保证可以预约发送，改为6分钟
        // 如果当前时间大于触发时间点,则延迟6分钟秒时间
        if (currentDate.after(triggerStartTime)) {
            triggerStartTime = DateUtils.add(currentDate, Calendar.SECOND, 6 * 60);
        }
        return triggerStartTime;
    }
//FIXME NEW 2019-11-08 15:15:33
    /**
     * 年月日时分秒(无下划线) yyyyMMddHHmmss
     */
    public static final String dtLong = "yyyyMMddHHmmss";

    /**
     * 获取当时系统日期
     *
     * @return yyyy-MM-dd
     */
    public static String getCurDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }

    /**
     * 获取当前系统日期时间（中文格式）
     *
     * @return yyyy年MM月dd日 HH:mm
     */
    public static String getCurTimeStrZh() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        return sdf.format(new Date());
    }

    public static String getCurTimeStrZhs() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月");
        return sdf.format(new Date());
    }

    /**
     * 获取当前系统日期时间
     *
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getCurTimeStr() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    /**
     * 获取当前系统日期时间秒数
     *
     * @return
     */
    public static String getCurTimeStrMsec() {
        long systiem = System.currentTimeMillis();
        return String.valueOf(systiem / 1000);
    }

    /**
     * 获取指定日期时间转换秒数
     *
     * @return
     */
    public static Long getAppointTimeSec(String dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = null;
        try {
            d = sdf.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d.getTime() / 1000;
    }

    /**
     * 将字符串类型的日期时间转换成Date类型
     *
     * @param souce
     * @return yyyy-MM-dd HH:mm:ss
     * @throws ParseException
     */
    public static Date parseDate(String souce) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(souce.substring(0, 20));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 获取一个月前的日期
     *
     * @return yyyy-MM-dd
     */
    public static String getBeforeOneMonthDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd"); // 制定日期格式
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1); // 得到前一天
        c.add(Calendar.MONTH, -1); // 得到前一个月
        return df.format(c.getTime()); // 返回String型的时间
    }

    /**
     * 获取三个月前的日期
     *
     * @return yyyy-MM-dd
     */
    public static String getBeforeThreeMonthDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd"); // 制定日期格式
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1); // 得到前一天
        c.add(Calendar.MONTH, -4); // 得到前三个月
        return df.format(c.getTime()); // 返回String型的时间
    }

    /**
     * 获取两个日期相隔的天数
     *
     * @param first 起始日期
     * @param end   结止日期
     * @return long
     * @throws ParseException
     */
    public static long betweenDate(String first, String end) {
        long day = 24L * 60L * 60L * 1000L;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        long days = 0;
        try {
            Date d1 = df.parse(first);
            Date d2 = df.parse(end);
            days = ((d2.getTime() - d1.getTime()) / day);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return days;
    }

    /**
     * 返回系统当前时间(精确到毫秒),作为一个唯一的订单编号
     *
     * @return 以yyyyMMddHHmmss为格式的当前系统时间
     */
    public static String getOrderNum() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat(dtLong);
        return df.format(date);
    }

    /**
     * 返回当前周的周一
     *
     * @return 以yyyyMMddHHmmss为格式的当前系统时间
     */
    public static String getMonday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return sdf.format(c.getTime());
    }

    /**
     * 返回当前周的周六
     *
     * @return 以yyyyMMddHHmmss为格式的当前系统时间
     */
    public static String getSaturday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        return sdf.format(c.getTime());
    }

    /**
     * 获取当前日期是星期几<br>
     *
     * @param dt
     * @return 当前日期是星期几
     */
    public static String getWeekOfDate(Date dt) {
        String[] weekDays = {
                "星期日"
                , "星期一"
                , "星期二"
                , "星期三"
                , "星期四"
                , "星期五"
                , "星期六"
        };
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return weekDays[w];
    }

    /**
     * 获取当前日期是星期几<br>
     *
     * @param dt
     * @return 当前日期是星期几
     */
    public static int getWeekOfDateIndex(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return w;
    }

    /**
     * 获取当前日期是一星期的第几天<br>
     *
     * @param dt
     * @return 当前日期是星期几
     */
    public static int getXuhaoOfDate(Date dt) {
        //String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return w;
    }

    /**
     * @Description: 是否同一天
     * @Param: [a, b]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    public static boolean isThisDay(Date a, Date b) {
        SimpleDateFormat sdf = new SimpleDateFormat(yyyy_MM_dd);
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
        SimpleDateFormat sdf = new SimpleDateFormat(yyyy_MM_dd);
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
        SimpleDateFormat sdf = new SimpleDateFormat(yyyy_MM_dd_HH_mm_ss);
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
        SimpleDateFormat sdf = new SimpleDateFormat(yyyy_MM_dd);
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

        String start = localDate.plusDays(1 - day).toString();
        String end = localDate.plusDays(7 - day).toString();
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
        LocalDate localDate = LocalDate.now();
        int day = localDate.getDayOfWeek().getValue();
        return localDate.plusDays(1 - day);
    }

    /**
     * @Description: 本周第一天
     * @Param: []
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/6/12
     */
    public static String getWeekBegin(LocalDate... localDates) {
        LocalDate localDate;
        if (localDates.length == 0) {
            localDate = LocalDate.now();
        } else {
            localDate = localDates[0];
        }
        int day = localDate.getDayOfWeek().getValue();
        return localDate.plusDays(1 - day).toString();
    }

    /**
     * @Description: 本周最后一天
     * @Param: []
     * @Return: java.util.Date
     * @Author: wangyingjie
     * @Date: 2020/6/12
     */
    public static LocalDate getThisWeekEnd() {
        LocalDate localDate = LocalDate.now();
        int day = localDate.getDayOfWeek().getValue();
        return localDate.plusDays(7 - day);
    }

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

    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now(ZoneOffset.of("+8"));
    }

    public static LocalDate getCurrentDate() {
        return LocalDate.now(ZoneOffset.of("+8"));
    }

    /**
     * @Description: 后缀
     * @Param: []
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/10/27
     */
    public static String getLocalDateOfPattern() {
        return getCurrentDate().format(DateTimeFormatter.ofPattern(PatternEnum.YYYYMMDD.getPattern()));
    }

    /**
     * @Description: 后缀
     * @Param: [localDate]
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/10/27
     */
    public static String getLocalDateOfPattern(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern(PatternEnum.YYYYMMDD.getPattern()));
    }
}