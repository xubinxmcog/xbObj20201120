package com.enuos.live.task;

/**
 * @Description 前缀枚举
 * @Author wangyingjie
 * @Date 2020/10/26
 * @Modified
 */
public enum TaskEnum {

    A04("001", "乐享令状"),

    I01("002", "分享邀请"),

    T003("003", "分享邀请");

    public final String CODE;

    public final String NAME;

    TaskEnum(String CODE, String NAME) {
        this.CODE = CODE;
        this.NAME = NAME;
    }
}
