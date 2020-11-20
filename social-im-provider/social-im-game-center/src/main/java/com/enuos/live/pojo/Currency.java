package com.enuos.live.pojo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/5/19
 * @Modified
 */
@Data
@ToString
public class Currency extends Base implements Serializable {

    private static final long serialVersionUID = -4248912237033510940L;

    private Long id;

    /**
     * 原钻石
     */
    private Long originalDiamond;

    /**
     * 原金币
     */
    private Long originalGold;

    /**
     * 原扭蛋
     */
    private Long originalGashapon;

    /** 钻石 */
    private Long diamond;

    /** 金币 */
    private Long gold;

    /** 扭蛋 */
    private Integer gashapon;

    /** 签名 */
    private Long signature;

}
