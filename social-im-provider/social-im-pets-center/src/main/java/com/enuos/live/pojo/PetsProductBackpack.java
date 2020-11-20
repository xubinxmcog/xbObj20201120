package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * pets_product_backpack
 * @author 
 */
@Data
@ToString
public class PetsProductBackpack implements Serializable {
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 物品ID
     */
    private Long productId;

    /**
     * 物品编码值
     */
    private String productCode;

    /**
     * 物品数量
     */
    private Integer productNum;

    /**
     * 商品有效期限：-1：永久 0：过期的 其他为秒值
     */
    private Long timeLimit;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 物品使用状态：0：无使用状态 1：未使用 2：使用中
     */
    private Integer productStatus;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 最后修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifiedTime;

    /**
     * 到期时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date useTime;

    private static final long serialVersionUID = 1L;
}