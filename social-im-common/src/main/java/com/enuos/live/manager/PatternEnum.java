package com.enuos.live.manager;

/**
 * @Description 日期格式
 * @Author wangyingjie
 * @Date 2020/7/28
 * @Modified
 */
public enum PatternEnum {

    YYYYMMDD("yyyyMMdd", "年月日"),
    YYYY_MM_DD("yyyy-MM-dd", "年月日"),
    YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd HH:mm:ss", "年月日时分秒");

    private String pattern;

    private String description;

    PatternEnum(String pattern, String description) {
        this.pattern = pattern;
        this.description = description;
    }

    public String getPattern() {
        return pattern;
    }

    public String getDescription() {
        return description;
    }
}
