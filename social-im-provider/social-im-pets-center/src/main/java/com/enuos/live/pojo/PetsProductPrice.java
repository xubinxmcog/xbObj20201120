package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @ClassName PetsProductPrice
 * @Description: TODO 商品价格
 * @Author xubin
 * @Date 2020/9/1
 * @Version V2.0
 **/
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PetsProductPrice implements Serializable {

    /**
     * 价格类别ID
     */
    private Integer priceId;

    /**
     * 商品id
     */
    private Long productId;

    /**
     * 有效期限
     */
    private Integer timeLimit;

    /**
     * 价格类型
     */
    private Integer payType1;

    /**
     * 价格
     */
    private Integer price1;

    private Integer payType2;

    private Integer price2;

    /**
     * 价格列
     */
    private List priceList;

    /**
     * 是否有效 ：0：无效 1：有效
     */
    private Integer status;

    /**
     * 商品有效期限描述
     */
    private String describe;

    /**
     * 最后修改时间
     */
    private Date modifyTime;

    private static final long serialVersionUID = 1L;


}
