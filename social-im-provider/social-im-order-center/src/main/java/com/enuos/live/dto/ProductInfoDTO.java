package com.enuos.live.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * product_info
 *
 * @author
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductInfoDTO {
    /**
     * 商品ID
     */
    private Long id;

    private Integer pageNum;

    private Integer pageSize;

    /**
     * 商品编码
     */
    @ApiModelProperty(value = "商品编码")
    private String productCode;

    /**
     * 商品名称
     */
    @ApiModelProperty(value = "商品名称")
    private String productName;

    /**
     * 分类ID
     */
    @ApiModelProperty(value = "分类ID")
    private Integer oneCategoryId;

    /**
     * 商品销售价格
     */
    @ApiModelProperty(value = "商品销售价格")
    private Integer price;

    @ApiModelProperty(value = "商品销售价格")
    private Integer beginPrice;

    @ApiModelProperty(value = "商品销售价格")
    private Integer endPrice;

    /**
     * 商品加权平均成本
     */
    private Integer averageCost;

    /**
     * 上下架状态：0下架1上架
     */
    private Integer publishStatus;

    /**
     * 审核状态：0未审核，1已审核
     */
    private Integer auditStatus;

    /**
     * 排序: 数字越小,排名越靠前
     */
    @ApiModelProperty(value = "属性1")
    private String sort;


    /**
     * 展示区域: 0: app端 其他web端
     */
    @ApiModelProperty(value = "属性3")
    private String market;

    /**
     * 游戏标签
     */
    @ApiModelProperty(value = "游戏标签")
    private String gameLabelId;

    /**
     * 属性4
     */
    @ApiModelProperty(value = "属性4")
    private String attribute4;

    /**
     * 属性5
     */
    @ApiModelProperty(value = "属性5")
    private String attribute5;

    /**
     * 商品描述
     */
    @ApiModelProperty(value = "商品描述")
    private String descript;

    /**
     * 商品录入时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date indateTime;

    /**
     * 最后修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date modifiedTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date beginTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date endTime;

    private static final long serialVersionUID = 1L;
}