package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * product_info
 * @author 
 */
//@Document(indexName = "testproduct", type = "product")
@Data
@ToString
@JsonIgnoreProperties(value = {"handler","hibernateLazyInitializer","fieldHandler"})
public class ProductInfo implements Serializable {
    /**
     * 商品ID
     */
//    @Id
    private Long productId;

    /**
     * 商品编码
     */
    @ApiModelProperty(value = "商品编码", required = true)
    private String productCode;

    /**
     * 商品名称
     */
    @ApiModelProperty(value = "商品名称", required = true)
    private String productName;

    /**
     * 分类ID
     */
//    @NotBlank(message = "分类ID不可为空")
    @ApiModelProperty(value = "分类ID", required = true)
    private Integer oneCategoryId;

    /**
     * 基础价格
     */
    private String priceBasic;

    /**
     * 价格列表
     */
    private List<ProductPrice> prices;

    /**
     * 支付方式ID
     */
    private Integer payType;

    /**
     * 支付方式名称
     */
    private String payTypeName;

    /**
     * 上下架状态：0下架1上架
     */
    private Integer publishStatus;

    /**
     * 审核状态：0未审核，1已审核
     */
    private Integer auditStatus;

    /**
     * 排序 : 数字越小,排名越靠前
     */
    @ApiModelProperty(value = "排序")
    private String sort;


    /**
     * 展示区域: 0: app端 其他web端
     */
    @ApiModelProperty(value = "展示区域")
    private String market;

    /**
     * 游戏标签
     */
    @ApiModelProperty(value = "游戏标签")
    private String gameLabelId;

    /**
     * 商品长度
     */
    @ApiModelProperty(value = "属性4")
    private String attribute4;

    /**
     * 颜色
     */
    @ApiModelProperty(value = "属性5")
    private String attribute5;

    /**
     * 商品描述
     */
    @ApiModelProperty(value = "商品描述")
    private String descript;

    /**
     * 图片URL
     */
    private String picUrl;

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

    private static final long serialVersionUID = 1L;
}