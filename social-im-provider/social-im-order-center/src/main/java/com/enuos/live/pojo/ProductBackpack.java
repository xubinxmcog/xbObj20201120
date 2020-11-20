package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * product_backpack 用户背包
 * @author 
 */
@Data
@ToString
public class ProductBackpack implements Serializable {
    private Integer id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 物品ID
     */
    private Long productId;

    /**
     * 商品编码值
     */
    private String productCode;

    /**
     * 物品数量
     */
    private Integer productNum;

    /**
     * 物品有效期
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
     * 游戏标签ID
      */
    private Integer gameLabelId;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date createTime;

    /**
     * 使用时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date useTime;

    /**
     * 最后修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date modifiedTime;

    private static final long serialVersionUID = 1L;
}