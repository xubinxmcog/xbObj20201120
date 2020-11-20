package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 附近的人入参
 * @Author wangyingjie
 * @Date 15:53 2020/4/13
 * @Modified
 */
@Data
public class Nearby extends Base implements Serializable {

    private static final long serialVersionUID = 3787716159112813597L;

    /** 经度 */
    private Double Longitude;

    /** 纬度 */
    private Double latitude;

    /** 距离 */
    private Double distance;

    /** 单位 */
    private String unit;

    /** 附近的几个 */
    private Integer limit;

}
