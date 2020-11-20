package com.enuos.live.result;

/**
 * @author WangCaiWen
 * Created on 2020/3/17 14:22
 */
public class BaseCodeMsg {

    private Integer code;
    private String msg;
    public static final BaseCodeMsg SUCCESS = app(0, "Success");
    public static final BaseCodeMsg ERROR_INIT = app(-1, "Error");

    private BaseCodeMsg() {
    }

    private BaseCodeMsg(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static BaseCodeMsg app(Integer code, String msg) {
        return new BaseCodeMsg(code, msg);
    }

    public BaseCodeMsg fillArgs(Object... args) {
        this.msg = String.format(this.msg, args);
        return this;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }
}
