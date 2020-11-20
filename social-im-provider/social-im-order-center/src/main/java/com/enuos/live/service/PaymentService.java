package com.enuos.live.service;

import com.enuos.live.pojo.OrderMsg;
import com.enuos.live.result.Result;

import java.util.Map;

public interface PaymentService {

    Result payment(Long userId, Long productId, Integer amount, Integer payType, Integer sriceId);

    Result cashPayment(Long userId, Long productId, Integer amount, Integer payType, Integer priceId);

    // 会员预订单
    Result paymentVIP(Long userId, String productId);

    Result paymentLog(Long userId, Integer pageNum, Integer pageSize);

    void diamondRecharge(OrderMsg order);

    Result diamondExGold(Long userId, Long exGold);
}
