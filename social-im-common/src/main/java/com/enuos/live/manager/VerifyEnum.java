package com.enuos.live.manager;

/**
 * @ClassName VerifyEnum
 * @Description: TODO 校验类
 * @Author xubin
 * @Date 2020/4/17
 * @Version V1.0
 **/
public enum VerifyEnum {

    ERROR_CODE_201(201, "参数校验不正确。"),
    ERROR_CODE_202(202, "实例化不正确。"),
    ERROR_DOCE_203(203, "查询类别不存");

    private Integer code;

    private String msg;

    VerifyEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
