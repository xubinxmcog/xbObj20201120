package com.enuos.live.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * order
 * @author 
 */
@Data
public class OrderMsg implements Serializable {
    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private Long orderSn;

    /**
     * 下单人ID
     */
    private Long userId;

    /**
     * 商品类型 1: 钻石 2:会员 3:其他
     */
    private Integer productType;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品编码
     */
    private String productCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品数量
     */
    private Integer productCnt;

    /**
     * 额度
     */
    private Integer productQuota;

    /**
     * 支付方式：1：人民币，2：钻石，3：金币
     */
    private Integer paymentMethod;

    /**
     * 现金支付方式：1：支付宝 2：微信 3：银联 4：ApplePay 5:其他
     */
    private Integer rmbMethod;

    /**
     * 现金支付方式类型
     */
    private String tradeType;

    /**
     * 订单金额
     */
    private String orderMoney;

    /**
     * 优惠金额
     */
    private String districtMoney;

    /**
     * 支付金额
     */
    private String paymentMoney;

    /**
     * 下单时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date createTime;

    /**
     * 支付时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date payTime;

    /**
     * 订单状态：1：交易成功  2：待支付 3：交易失败
     */
    private Integer orderStatus;

    /**
     * 处理状态 0: 未处理 1:成功 2:失败
     */
    private Integer isHandle;

    private static final long serialVersionUID = 1L;
}