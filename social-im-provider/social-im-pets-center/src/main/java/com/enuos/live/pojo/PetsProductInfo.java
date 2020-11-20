package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * pets_product_info
 * @author 
 */
@Data
public class PetsProductInfo implements Serializable {
    private Long id;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 物品编码
     */
    private String productCode;

    /**
     * 物品名称
     */
    private String productName;

    /**
     * 物品图片
     */
    private String picUrl;

    /**
     * 上下架状态：0下架1上架
     */
    private Byte publishStatus;

    /**
     * 审核状态：0未审核，1已审核
     */
    private Byte auditStatus;

    /**
     * 排序 : 数字越小,排名越靠前
     */
    private Long sort;

    /**
     * 属性
     */
    private Object attribute;

    /**
     * 商品价格列表
     */
    private List<PetsProductPrice> priceList;

    /**
     * 描述
     */
    private String descript;

    /**
     * 录入时间
     */
    private Date indateTime;

    /**
     * 最后修改时间
     */
    private Date modifiedTime;

    private static final long serialVersionUID = 1L;
}