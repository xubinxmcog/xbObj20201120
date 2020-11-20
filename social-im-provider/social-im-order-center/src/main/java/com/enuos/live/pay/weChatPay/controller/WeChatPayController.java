package com.enuos.live.pay.weChatPay.controller;

import cn.hutool.core.util.StrUtil;
import com.enuos.live.annotations.Cipher;
import com.enuos.live.pay.weChatPay.WeChatPayService;
import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName WapSignSignatureAction
 * @Description: TODO 微信支付
 * @Author xubin
 * @Date 2020/4/30
 * @Version V1.0
 **/
@Slf4j
@RestController
@RequestMapping("/payment/weChatPay")
public class WeChatPayController {

    @Autowired
    private WeChatPayService weChatPayService;

    /**
     * @MethodName: WapSignSignatureAction
     * @Description: TODO 微信APP支付
     * @Param: [prams, request, response]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 11:38 2020/8/10
     **/
    @Cipher
    @RequestMapping("/appSignSignatureAction")
    public Result appSignSignatureAction(@RequestBody Map<String, Object> prams,
                                         HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("微信APP支付入参=[{}]", prams);
        return weChatPayService.paySignSignatureAction(prams, "APP", request, response);
    }

    /**
     * @MethodName: WapSignSignatureAction
     * @Description: TODO 微信JSAPI支付
     * @Param: [prams, request, response]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 11:38 2020/8/10
     **/
    @Cipher
    @RequestMapping("/wapSignSignatureAction")
    public Result WapSignSignatureAction(@RequestBody Map<String, Object> prams,
                                         HttpServletRequest request, HttpServletResponse response) throws Exception {
        return weChatPayService.wapSignSignatureAction(prams, request, response);
    }

    /**
     * @MethodName: NativeSignSignatureAction
     * @Description: TODO 微信NATIVE支付
     * @Param: [prams, request, response]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 11:37 2020/8/7
     **/
    @Cipher
    @RequestMapping("/native")
    public Result NativeSignSignatureAction(@RequestBody Map<String, Object> prams,
                                            HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("微信NATIVE支付入参=[{}]", prams);
        if (Objects.isNull(prams.get("productName"))
                || Objects.isNull(prams.get("orderNo"))
                || Objects.isNull(prams.get("totalAmount"))
                || Objects.isNull(prams.get("ipAddress"))) {
            log.warn("----微信支付参数为空----");
            return Result.error(201, "参数为空");
        }
        return weChatPayService.NativeSignSignatureAction(prams, request, response);
    }

    /**
     * @MethodName: NativeSignSignatureAction
     * @Description: TODO 微信H5支付
     * @Param: [prams, request, response]
     * @Return: void
     * @Author: xubin
     * @Date: 17:33 2020/8/6
     **/
    @Cipher
    @RequestMapping(value = "/webSignSignatureAction", method = RequestMethod.POST)
    public Result webSignSignatureAction(@RequestBody Map<String, Object> prams,
                                         HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("微信H5支付入参=[{}]", prams);
        if (Objects.isNull(prams.get("productName"))
                || Objects.isNull(prams.get("orderNo"))
                || Objects.isNull(prams.get("totalAmount"))
                || Objects.isNull(prams.get("ipAddress"))) {
            log.warn("----微信H5支付参数为空----");
            return Result.error(201, "参数为空");
        }
        return weChatPayService.webSignSignatureAction(prams, request, response);
    }

    /**
     * 微信支付订单查询
     * 1.如果由于网络通信问题 导致微信没有通知到商户支付结果
     * 2.商户主动去查询支付结果 而后执行其他业务操作
     */
    @Cipher
    @RequestMapping(value = {"/orderQuery"})
    public Result orderQuery(@RequestParam(value = "orderNo") String orderNo) throws Exception {
        return weChatPayService.wxOrderQuery(orderNo);
    }

    // 支付回调地址
    @RequestMapping("/callback")
    public void callbackNative(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("微信支付回调地址");
        weChatPayService.callbackNative(request, response);
    }


    /**
     * @MethodName: businessPay
     * @Description: TODO 企业向用户支付接口
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 9:41 2020/10/26
     **/
    @PostMapping("/business/zfifus")
    public Result businessPay(@RequestBody Map<String, Object> params) {
        log.info("企业支付参数:[{}]", params);
        return weChatPayService.businessPay(params);
    }

    /**
     * 企业向个人转账查询
     *
     * @param tradeno 商户转账订单号
     */
    @PostMapping(value = "/pay/query")
    public Result orderPayQuery(String tradeno) {
        if (StrUtil.isEmpty(tradeno)) {
            return Result.error(201, "订单号不能为空");
        }
        return weChatPayService.orderPayQuery(tradeno);
    }
}
