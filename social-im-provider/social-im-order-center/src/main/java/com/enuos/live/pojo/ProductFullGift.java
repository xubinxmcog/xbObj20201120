package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * product_full_gift
 * @author 
 */
@Data
public class ProductFullGift implements Serializable {
    private Long id;

    /**
     * 满多少
     */
    private Integer full;

    /**
     * 赠多少
     */
    private Integer gift;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 描述
     */
    private String descript;

    /**
     * 开始时间
     */
    private Date beginTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}