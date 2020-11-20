package com.enuos.live.pay.applePay.controller;

import cn.hutool.core.util.StrUtil;
import com.enuos.live.annotations.Cipher;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.pay.applePay.ApplePayService;
import com.enuos.live.result.Result;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

/**
 * @ClassName applePay
 * @Description: TODO 苹果支付
 * @Author xubin
 * @Date 2020/6/2
 * @Version V1.0
 **/
@Api("苹果支付")
@Slf4j
@RestController
@RequestMapping("/applePay")
public class ApplePayController {

    @Autowired
    private ApplePayService applePayService;

    @Cipher
    @RequestMapping(value = "/notifyCallBack")
    public Result applepayOrderNotifyCallBack(@RequestBody Map<String, Object> prams) {
        String orderNo = prams.get("orderNo").toString();
        String payload = prams.get("payload").toString();
        Long userId = Long.valueOf(prams.get("userId").toString());
        String transactionId = prams.get("transactionId").toString();
        log.info("苹果支付客户端传过来的值:transactionId:[{}], orderNo=[{}],userId=[{}], payload:[{}]", transactionId, orderNo, userId, payload);
        if (StrUtil.isEmpty(orderNo) || StrUtil.isEmpty(payload) || StrUtil.isEmpty(transactionId) || Objects.isNull(userId)) {
            Result.error(ErrorCode.CONTENT_EMPTY);
        }
        return applePayService.applePayOrderNotifyCallBack(orderNo, payload, userId, transactionId);
    }

}
