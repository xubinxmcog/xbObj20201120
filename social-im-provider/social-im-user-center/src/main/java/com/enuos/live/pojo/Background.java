package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/7/3
 * @Modified
 */
@Data
public class Background implements Serializable {

    private static final long serialVersionUID = -1731977475464261757L;

    /** 背景 */
    private String backgroundUrl;

}
