package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import io.lettuce.core.protocol.CommandHandler;
import lombok.Data;

/**
 * product_price
 * @author 
 */
@Data
public class ProductPrice implements Serializable {
    private Integer priceId;

    /**
     * 商品Id
     */
    private Long productId;

    /**
     * 有效期限：-1：永久 7：7天
     */
    private Integer timeLimit;

    /**
     * 描述
     */
    private String describe;

    /**
     * 价格列
     */
    private List priceList;

    /**
     * 价格类型Id1：参考pay_type
     */
    private Integer payType1;

    /**
     * 价格1
     */
    private String price1;

    /**
     * 价格类型Id2：参考pay_type
     */
    private Integer payType2;

    /**
     * 价格2
     */
    private String price2;

    /**
     * 是否有效 ：0：无效 1：有效
     */
    private Byte status;

    /**
     * 最后修改时间
     */
    private Date modifyTime;

    private static final long serialVersionUID = 1L;
}