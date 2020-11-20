package com.enuos.live.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @ClassName paymentDTO
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/8
 * @Version V1.0
 **/
@Data
public class PaymentDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不可为空")
    Long userId;

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不可为空")
    Long productId;

    /**
     * 购买数量
     */
    @NotNull(message = "购买数量不可为空")
    Integer amount;

    /**
     * 支付方式 1：人民币 2：钻石  3：金币 4：积分 5：经验值
     */
    @NotNull(message = "支付方式不可为空")
    Integer payType;

    /**
     * 商品价格ID
     */
    @NotNull(message = "商品价格ID不可为空")
    Integer priceId;
}
