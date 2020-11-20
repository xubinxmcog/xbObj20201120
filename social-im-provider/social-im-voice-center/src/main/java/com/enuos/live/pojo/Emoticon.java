package com.enuos.live.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

/**
 * tb_emoticon
 *
 * @author
 */
@Data
public class Emoticon implements Serializable {
    private Long emId;

    /**
     * 表情名称
     */
    private String emName;

    /**
     * 价格
     */
    private Long emPrice;

    /**
     * 价格类型
     */
    private Integer priceType;

    /**
     * 表情链接
     */
    private String emUrl;

    /**
     * 特效
     */
    private String moveAnimToLeft; // 向左动画
    private String endAnimToLeft; // 左结束动画
    private String moveAnimToRight; // 向右动画
    private String endAnimToRight; // 右结束动画

    private String staticToLeft; // 左图(安卓使用)
    private String staticToRight; // 右图(安卓使用)

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后修改时间
     */
    private Date modifiedTime;

    private static final long serialVersionUID = 1L;
}