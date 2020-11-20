package com.enuos.live.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * product_backpack 用户背包
 *
 * @author
 */
@Data
@ToString
public class BackpackDTO implements Serializable {
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
     * 物品图片链接
     */
    private String picUrl;

    /**
     * 有效期
     */
    private Long timeLimit;

    /**
     * 物品描述
     */
    private String descript;

    /**
     * 属性集
     */
    private String attribute4;

    /**
     * 状态
     */
    private Integer productStatus;

    /**
     * 有效期描述
     */
    private String termDescribe;

    /**
     * 最后修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date modifiedTime;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date createTime;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date useTime;

    private static final long serialVersionUID = 1L;
}