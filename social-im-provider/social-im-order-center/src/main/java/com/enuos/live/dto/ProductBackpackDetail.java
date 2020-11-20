package com.enuos.live.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @ClassName ProductBackpackDetail
 * @Description: TODO 饰品详情
 * @Author xubin
 * @Date 2020/7/27
 * @Version V2.0
 **/
@Data
public class ProductBackpackDetail {

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
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date createTime;

    /**
     * 到期时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date useTime;

    /**
     * 物品有效期
     */
    private Long timeLimit;

    /**
     * 商品名称
     */
    private String productName;

    /**
     *
     */
    private String descript;

    /**
     * 图片
     */
    private String picUrl;

    /**
     * 游戏标签
     */
    private Integer gameLabelId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类编码
     */
    private String categoryCode;

    /**
     * 字体
     */
    private Object attribute4;
}
