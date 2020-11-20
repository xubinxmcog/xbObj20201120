package com.enuos.live.constants;

public enum BaseResponseEnum {
    SUCCESS(200,"操作成功"),
    USER_ID_NOT_NULL(10001,"用户id不能为空"),
    GET_USER_INFO(10002,"获取用户信息失败"),
    APPROVE_TYPE(10003,"审核类型错误"),
    STATUS_ERROR(10004,"业务状态错误"),
    USER_TYPE_ERROR(10005,"用户类型错误")
    ;
    BaseResponseEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private  int code;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
