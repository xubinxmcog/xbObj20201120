package com.enuos.live.constant;

/**
 * @Description code枚举
 * @Author wangyingjie
 * @Date 2020/7/24
 * @Modified
 */
public enum CodeEnum {

    CODE4(4,"同一身份证号绑定限制[5次]"),
    CODE7(7, "登陆token过期时间[7天];每日任务过期时间[7天];好友邀请受邀者登陆任务期限"),
    CODE60(60, "手机短信验证码过期时间[60秒]");

    private Integer code;

    private String description;

    CodeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

}
