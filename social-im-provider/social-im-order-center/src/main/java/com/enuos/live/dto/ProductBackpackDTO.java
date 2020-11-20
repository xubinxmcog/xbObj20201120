package com.enuos.live.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * product_backpack 用户背包
 * @author 
 */
@Data
@ToString
public class ProductBackpackDTO implements Serializable {
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
     * 物品名称
     */
    private String productName;

    /**
     * 物品数量
     */
    private Integer productNum;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类编码
     */
    private String categoryCode;

    /**
     * 物品图片链接
     */
    private String picUrl;

    /**
     * 有效期
     */
    private String timeLimit;

    /**
     * 物品描述
     */
    private String descript;

    /**
     * 状态
     */
    private Integer productStatus;

    private Integer gameDecorationId;

    /**
     * 最后修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date modifiedTime;

    private static final long serialVersionUID = 1L;
}