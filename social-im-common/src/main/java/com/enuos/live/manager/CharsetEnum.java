package com.enuos.live.manager;

/**
 * @Description 编码枚举
 * @Author wangyingjie
 * @Date 2020/9/11
 * @Modified
 */
public enum CharsetEnum {

    UTF8("UTF-8", "utf-8");

    private String uppercase;

    private String lowercase;

    CharsetEnum(String uppercase, String lowercase) {
        this.uppercase = uppercase;
        this.lowercase = lowercase;
    }

    public String getUppercase() {
        return uppercase;
    }

    public String getLowercase() {
        return lowercase;
    }
}
