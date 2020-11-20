package com.enuos.live.constant;

import com.enuos.live.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.*;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/5/20
 * @Modified
 */
@Slf4j
public class Constellation {

    /** 星座 */
    private static final String[] CONSTELLATION = {"水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座"};

    /** code */
    private static final String[] TASK_CODE = {"CST0001", "CST0002", "CST0003", "CST0004", "CST0005", "CST0006", "CST0007", "CST0008", "CST0009", "CST0010", "CST0011", "CST0012"};

    /** 星座日期 */
    private static final String[] MONTH_DATE = {"01-20,02-18", "02-19,03-20", "03-21,04-19", "04-20,05-20", "05-21,06-21", "06-22,07-22", "07-23,08-22", "08-23,09-22", "09-23,10-23", "10-24,11-22", "11-23,12-21", "12-22,01-19"};

    /** 截至日期 */
    private static final int[] BEGIN_DAY = {20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22};

    private static Map<String, List<String>> CONSTELLATION_DATE;

    /**
     * @Description: 获取星座签到code
     * @Param: [localDate]
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/8/18
     */
    public static String getCode(LocalDate localDate) {
        int month = localDate.getMonthValue();
        int index = month - 1;
        int day = localDate.getDayOfMonth();

        if (day < BEGIN_DAY[index]) {
            // 星座轮询
            if(index == 0) {
                index = 11;
            } else {
                index = index - 1;
            }
        }

        return TASK_CODE[index];
    }

    /**
     * @Description: 获取星座及日历
     * @Param: []
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/6/2
     */
    public static Map<String, Object> getConstellation() {
        LocalDate localDate = LocalDate.now();

        log.info("LocalDate now is [{}]", localDate.toString());

        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();

        int index = month - 1;
        if (day < BEGIN_DAY[index]) {
            // 星座轮询
            if(index == 0) {
                index = 11;
            } else {
                index = index - 1;
            }
        }

        if (Objects.isNull(CONSTELLATION_DATE)) {
            CONSTELLATION_DATE = new HashMap<>();
        }

        String constellation = CONSTELLATION[index];
        String taskCode = TASK_CODE[index];
        String key = String.valueOf(year).concat("_").concat(constellation);

        if (!CONSTELLATION_DATE.containsKey(key)) {
            String[] date = MONTH_DATE[index].split(",");
            CONSTELLATION_DATE.put(key, DateUtils.getDateIntervalList(year + "-" + date[0], year + "-" + date[1]));
        }

        return new HashMap<String, Object>() {
            {
                put("constellation", constellation);
                put("calendar", CONSTELLATION_DATE.get(key));
                put("taskCode", taskCode);
            }
        };
    }

}
