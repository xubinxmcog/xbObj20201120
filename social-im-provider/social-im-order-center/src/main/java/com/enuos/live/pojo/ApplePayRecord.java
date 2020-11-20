package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * apple_pay_record
 * @author 
 */
@Data
public class ApplePayRecord implements Serializable {
    private Integer id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 本地订单编码
     */
    private Long orderSn;

    /**
     * 苹果订单号
     */
    private String transactionId;

    /**
     * 凭证
     */
    private String payload;

    /**
     * 凭证
     */
    private Integer verification;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}