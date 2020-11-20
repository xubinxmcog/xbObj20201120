package com.enuos.live.result;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.manager.ZoneEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @Description 出参
 * @Author wangyingjie
 * @Date 2020/9/10
 * @Modified
 */
@Data
@JsonInclude(Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 5001442597209582355L;

    /**
     * 返回code
     */
    private Integer code;

    /**
     * code说明
     */
    private String msg;

    /**
     * 签名
     */
    private Long signature;

    /**
     * 数据体（加密的数据体要求为kv结构）
     */
    private T data;

    public Result() {
        super();
    }

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(BaseCodeMsg baseCodeMsg) {
        if (baseCodeMsg != null) {
            this.code = baseCodeMsg.getCode();
            this.msg = baseCodeMsg.getMsg();
        }
    }

    public static <T> Result<T> success() {
        return new Result(BaseCodeMsg.SUCCESS);
    }

    public static <T> Result<T> success(Integer code, String message) {
        return new Result(code, message);
    }

    public static <T> Result<T> success(BaseCodeMsg baseCodeMsg) {
        return new Result(baseCodeMsg);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = success();
        result.signature = LocalDateTime.now().toEpochSecond(ZoneOffset.of(ZoneEnum.E8.getZone()));
        result.data = data;
        return result;
    }

    public static <T> Result<T> error() {
        return new Result(BaseCodeMsg.ERROR_INIT);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result(code, message);
    }

    public static <T> Result<T> error(BaseCodeMsg baseCodeMsg) {
        return new Result(baseCodeMsg);
    }

    public static <T> Result<T> empty() {
        return new Result(ErrorCode.DATA_ERROR);
    }

}
