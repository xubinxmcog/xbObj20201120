package com.enuos.live.dto;

import lombok.Data;

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
    Long userId;

    /**
     * 商品ID
     */
    Long productId;

    /**
     * 购买数量
     */
    Integer amount;

    /**
     * 支付方式 1：人民币 2：钻石  3：金币 4：积分 5：经验值
     */
    Integer payType;

    /**
     * 商品价格ID
     */
    Integer priceId;
}
