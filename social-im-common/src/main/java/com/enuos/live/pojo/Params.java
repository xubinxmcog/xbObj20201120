package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 统一入参
 * @Author wangyingjie
 * @Date 2020/9/10
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Params<T> implements Serializable {

    /**
     * 签名
     */
    private Long signature;

    /**
     * 加密数据体
     */
    private String data;

    /**
     * 解密数据体
     */
    private T body;

}
