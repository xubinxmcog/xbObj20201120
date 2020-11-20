package com.enuos.live.pojo;

import lombok.Data;

/**
 * @ClassName productPayInfo
 * @Description: TODO 商品支付信息
 * @Author xubin
 * @Date 2020/7/9
 * @Version V2.0
 **/
@Data
public class PetsProductPayInfo {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品编码
     */
    private String  productCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 商品有效期限：-1：永久 7：7天
     */
    private Integer timeLimit;

    /**
     * 价格类型Id1：钻石 参考pay_type
     */
    private Integer payType1;

    /**
     * 价格 1
     */
    private Long price1;

    /**
     * 价格类型Id2 参考pay_type
     */
    private Integer payType2;

    /**
     * 价格 2
     */
    private Long price2;

    /**
     * 类别使用属性 1:消耗品 2:装饰品
     */
    private Integer usingPro;

}
