package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * business_pay_msg
 * @author 
 */
@Data
public class BusinessPayMsg implements Serializable {
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户真实姓名
     */
    private String reUserName;

    /**
     * 用户openid
     */
    private String openId;

    /**
     * 商户订单号
     */
    private String partnerTradeNo;

    /**
     * 企业付款金额，单位为分
     */
    private String amount;

    /**
     * 企业付款备注
     */
    private String desc;

    /**
     * 支付结果
     */
    private String resultCode;

    /**
     * 错误码信息
     */
    private String errCode;

    /**
     * 错误代码描述
     */
    private String errCodeDes;

    /**
     * 微信付款单号(企业付款成功，返回的微信付款单号)
     */
    private String paymentNo;

    /**
     * 企业付款成功时间
     */
    private String paymentTime;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 最后更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}