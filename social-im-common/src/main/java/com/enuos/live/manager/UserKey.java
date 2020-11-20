package com.enuos.live.manager;

/**
 * @author WangCaiWen
 * Created on 2020/3/19 13:10
 */
public enum UserKey {

    /** 用户信息*/
    USER("USER",100),
    /** 验证码*/
    CODE("CODE",200);

    private String name;
    private Integer code;

    UserKey(String name, Integer code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
