package com.enuos.live.pojo;

import lombok.Data;

/**
 * @Description 阈值
 * @Author wangyingjie
 * @Date 2020/5/18
 * @Modified
 */
@Data
public class Threshold {

    /** 码 */
    private Integer code;

    /** 类型 */
    private Integer codeType;

    /** 阈值 */
    private Long threshold;

}
