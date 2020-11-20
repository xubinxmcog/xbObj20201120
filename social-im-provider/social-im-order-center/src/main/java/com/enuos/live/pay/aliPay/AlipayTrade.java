package com.enuos.live.pay.aliPay;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.OrderMapper;
import com.enuos.live.pay.PayService;
import com.enuos.live.pay.aliPay.config.AliAppPayConfig;
import com.enuos.live.pay.aliPay.config.AlipayConfig;
import com.enuos.live.pay.aliPay.constants.AliPayConstants;
import com.enuos.live.pay.aliPay.util.SignUtil;
import com.enuos.live.pojo.OrderMsg;
import com.enuos.live.result.Result;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @ClassName AlipayTrade
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/27
 * @Version V1.0
 **/
@Service
@Slf4j
public class AlipayTrade {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PayService payService;

    /**
     * @MethodName: alipayWeb
     * @Description: TODO 支付宝手机网站支付
     * @Param: [sParaTemp, httpResponse]
     * @Return: void
     * @Author: xubin
     * @Date: 14:30 2020/8/13
     **/
    public void aliPayWapRequest(Map<String, String> sParaTemp, HttpServletResponse httpResponse) throws IOException {
        if (StrUtil.isEmpty(sParaTemp.get("orderNo"))
                || StrUtil.isEmpty(sParaTemp.get("totalAmount"))
                || StrUtil.isEmpty(sParaTemp.get("productName"))) {
            getHttpResponseWriter(Result.error(ErrorCode.EXCEPTION_CODE, "缺少参数").toString(), httpResponse);
            return;
        }
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        alipayRequest.setReturnUrl(AliPayConstants.RETURN_URL);//创建API对应的request
        alipayRequest.setNotifyUrl(AliPayConstants.NOTIFY_URL); //在公共参数中设置回跳和通知地址
        // 待请求参数数组
//        sParaTemp.put("seller_id",AlipayConfig.SELLER_ID);

        // 修改支付方式
        OrderMsg order = new OrderMsg();
        order.setRmbMethod(1);// 现金支付方式：1：支付宝 2：微信 3：银联 4：其他
        order.setOrderSn(Long.valueOf(sParaTemp.get("orderNo")));
        order.setOrderMoney(sParaTemp.get("totalAmount"));
        order.setTradeType("WAP");
        int update = orderMapper.update(order);
        if (update < 1) {
            getHttpResponseWriter(Result.error(ErrorCode.EXCEPTION_CODE, "订单金额不正确").toString(), httpResponse);
            return;
        }

        // 组装支付数据
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put("out_trade_no", sParaTemp.get("orderNo"));// 唯一订单号
        tempMap.put("total_amount", sParaTemp.get("totalAmount"));// 订单总金额，单位为元
        tempMap.put("subject", "7乐-" + sParaTemp.get("productName"));// 商品名称
        tempMap.put("product_code", sParaTemp.get("productCode"));// 销售产品码 固定值：QUICK_WAP_WAY
        tempMap.put("timeout_express", "30m");// 超时时间，逾期将关闭交易 30分钟

        String result = JSON.toJSONString(tempMap);
        log.info("支付宝支付数据:[{}]", result);
        alipayRequest.setBizContent(result);
        String form = "";
        try {
            form = AlipayConfig.getCertificateInstance().pageExecute(alipayRequest).getBody();// 证书方式
        } catch (AlipayApiException e) {
            log.error("支付宝构造表单失败", e);
        }
        log.info("支付宝支付表单构造:" + form);
        getHttpResponseWriter(form, httpResponse);
    }

    /**
     * @MethodName: alipayWeb
     * @Description: TODO 支付宝电脑支付
     * @Param: [sParaTemp, httpResponse]
     * @Return: void
     * @Author: xubin
     * @Date: 14:11 2020/8/13
     **/
    public void alipayWeb(Map<String, String> sParaTemp, HttpServletResponse httpResponse) throws IOException {
        if (StrUtil.isEmpty(sParaTemp.get("orderNo"))
                || StrUtil.isEmpty(sParaTemp.get("totalAmount"))
                || StrUtil.isEmpty(sParaTemp.get("productName"))) {
            getHttpResponseWriter(Result.error(ErrorCode.EXCEPTION_CODE, "缺少参数").toString(), httpResponse);
            return;
        }
        String form = "";
        try {

            // 修改支付方式
            OrderMsg order = new OrderMsg();
            order.setRmbMethod(1);// 现金支付方式：1：支付宝 2：微信 3：银联 4：其他
            order.setOrderSn(Long.valueOf(sParaTemp.get("orderNo")));
            order.setOrderMoney(sParaTemp.get("totalAmount"));
            order.setTradeType("WEB");
            int update = orderMapper.update(order);
            if (update < 1) {
                getHttpResponseWriter(Result.error(ErrorCode.EXCEPTION_CODE, "订单金额不正确").toString(), httpResponse);
                return;
            }
            // 1. 创建AlipayClient实例
            AlipayClient alipayClient = AlipayConfig.getCertificateInstance();
            // 组装支付数据
            Map<String, String> tempMap = new HashMap<>();
            tempMap.put("out_trade_no", sParaTemp.get("orderNo"));// 唯一订单号
            tempMap.put("total_amount", sParaTemp.get("totalAmount"));// 订单总金额，单位为元
            tempMap.put("subject", "7乐-" + sParaTemp.get("productName"));// 商品名称
            tempMap.put("product_code", sParaTemp.get("productCode"));// 销售产品码 固定值：FAST_INSTANT_TRADE_PAY
            tempMap.put("timeout_express", "30m");// 超时时间，逾期将关闭交易 30分钟
            String result = JSON.toJSONString(tempMap);
            log.info("支付宝支付数据:[{}]", result);

            // 2. 创建API对应的request
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            alipayRequest.setReturnUrl(AliPayConstants.RETURN_URL);
            alipayRequest.setNotifyUrl(AliPayConstants.NOTIFY_URL); //在公共参数中设置回跳和通知地址
            alipayRequest.setBizContent(result);
            // 3. 发起请求并处理响应
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (Exception e) {
            log.error("调用遭遇异常，原因：" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
        log.info("支付宝支付表单构造:" + form);
        getHttpResponseWriter(form, httpResponse);
    }

    /**
     * @MethodName: tradeAppPayRequest
     * @Description: TODO APP支付调用
     * @Param: []
     * @Return: java.lang.String
     * @Author: xubin
     * @Date: 2020/4/28
     **/
    public Result tradeAppPayRequest(Map<String, String> sParaTemp) {

        if (StrUtil.isEmpty(sParaTemp.get("orderNo"))
                || StrUtil.isEmpty(sParaTemp.get("totalAmount"))
                || StrUtil.isEmpty(sParaTemp.get("productName"))) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "缺少参数");
        }

        // 修改支付方式
        OrderMsg order = new OrderMsg();
        order.setRmbMethod(1);// 现金支付方式：1：支付宝 2：微信 3：银联 4：其他
        order.setOrderSn(Long.valueOf(sParaTemp.get("orderNo")));
        order.setOrderMoney(sParaTemp.get("totalAmount"));
        order.setTradeType("APP");
        int update = orderMapper.update(order);
        if (update < 1) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "订单金额不正确");
        }
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setBody("7乐APP充值-" + sParaTemp.get("productName")); // 描述
        model.setSubject("7乐-" + sParaTemp.get("productName"));
        model.setOutTradeNo(sParaTemp.get("orderNo")); // 交易编码
        model.setTimeoutExpress("30m"); //超时时间
        model.setTotalAmount(sParaTemp.get("totalAmount"));
        model.setProductCode("QUICK_MSECURITY_PAY");// 商品编码
        request.setBizModel(model);
        request.setNotifyUrl(AliPayConstants.NOTIFY_URL);//回跳和通知地址
        try {
            //这里和普通的接口调用不同，使用的是sdkExecute
            AlipayTradeAppPayResponse response = AliAppPayConfig.getAppCertificateInstance().sdkExecute(request);
            if(response.isSuccess()){
                log.info("调用成功");
            }
            log.info(response.getBody());//就是orderString 可以直接给客户端请求，无需再做处理。
            String body = response.getBody();
            return Result.success(body);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return Result.error();
    }

    /**
     * @MethodName: alipayWeb
     * @Description: TODO 支付查询
     * @Param: [sParaTemp, httpResponse]
     * @Return: void
     * @Author: xubin
     * @Date: 14:11 2020/8/14
     **/
    public Map<String, String> aliPayTradeQuery(String orderNo) {
        Map<String, String> map = new HashMap();
        if (StrUtil.isEmpty(orderNo)) {
            map.put("msg", "缺少参数");
            map.put("code", "201");
            return map;
        }
        String form = "";
        try {

            // 1. 创建AlipayClient实例
            AlipayClient alipayClient = AlipayConfig.getCertificateInstance();
            // 组装支付数据
            Map<String, String> tempMap = new HashMap<>();
            tempMap.put("out_trade_no", orderNo);// 唯一订单号
            String result = JSON.toJSONString(tempMap);
            log.info("支付宝支付数据:[{}]", result);

            // 2. 创建API对应的request
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent(result);
            // 3. 发起请求并处理响应
            AlipayTradeQueryResponse response = alipayClient.certificateExecute(request);
            form = response.getBody();
            if (response.isSuccess()) {
                log.info("调用成功");
            } else {
                log.info("调用失败");
            }
        } catch (Exception e) {
            log.error("调用遭遇异常，原因：" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
        log.info("支付宝支付表单构造:" + form);
        JSONObject obj = JSONObject.parseObject(form);
        String alipay_trade_query_response = obj.getString("alipay_trade_query_response");
        Gson gson = new Gson();
        map = gson.fromJson(alipay_trade_query_response, map.getClass());
        String code = map.get("code"); // 交易状态 10000成功
        if ("10000".equals(code)) {
            String outTradeNo = map.get("out_trade_no"); // 订单号
            String totalAmount = map.get("total_amount"); // 金额 6.00
            String tradeStatus = map.get("trade_status"); // 支付状态 成功:TRADE_SUCCESS  WAIT_BUYER_PAY:创建 为null订单不存在
            log.info("支付状态校验：失败, tradeStatus:[{}]", tradeStatus);
            switch (tradeStatus) {
                case "TRADE_SUCCESS":
                    List<OrderMsg> orders = orderMapper.selectByPrimaryKey(Long.valueOf(outTradeNo), null, 2, null, null);
                    if (ObjectUtil.isNotEmpty(orders)) {
                        OrderMsg order = orders.get(0);
                        if (new BigDecimal(order.getOrderMoney()).setScale(2, BigDecimal.ROUND_HALF_UP).toString().equals(totalAmount)) {
                            log.info("交易成功，修改订单状态");
                            order.setPaymentMoney(order.getOrderMoney());
                            order.setOrderStatus(1); // 订单状态：1：交易成功  2：待支付 3：交易失败
                            order.setRmbMethod(1);// 现金支付方式：1：支付宝 2：微信 3：银联 4：其他
                            order.setPayTime(new Date());
                            int i = orderMapper.update(order);
                            log.info("更新订单状态=[{}]", i);
                            if (i > 0) {
                                payService.handle(order);
                            }
                        } else {
                            log.info("订单金额核验失败,orderNo=[{}], totalAmount=[{}]", orderNo, totalAmount);
                        }
                    } else {
                        log.info("订单已核验,orderNo=[{}]", orderNo);
                    }
                    break;
                case "TRADE_CLOSED":
                    log.info("订单关闭,orderNo=[{}]", orderNo);
                    upOrderMsg(orderNo);
                    break;
                default:
                    log.info("支付待处理订单,tradeStatus=[{}]", tradeStatus);
            }
        } else {
            upOrderMsg(orderNo);
        }
        return map;
    }

    public String gainAliReturnMessage(HttpServletRequest request) throws AlipayApiException {
        //获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        //切记alipaypublickey是支付宝的公钥，请去open.alipay.com对应应用下查看。
        //boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
        boolean flag = AlipaySignature.rsaCheckV1(params, AliPayConstants.ALIPAY_PUBLIC_KEY, AliPayConstants.CHARSET, "RSA2");

        return flag + "";
    }

    /**
     * 申请退款
     *
     * @param sParaTemp 退款参数
     * @return true成功, 回调中处理
     * 备注:https://doc.open.alipay.com/docs/api.htm?spm=a219a.7629065.0.0.3RjsEZ&apiId=759&docType=4
     */
    public boolean tradeRefundRequest(Map<String, ?> sParaTemp) throws AlipayApiException {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
//        request.setReturnUrl(AlipayConfig.RETURN_URL);
//        request.setNotifyUrl(AlipayConfig.REFUND_NOTIFY);
        // 待请求参数数组
        request.setBizContent(JSON.toJSONString(sParaTemp));
        AlipayTradeRefundResponse response = AlipayConfig.getInstance().execute(request);
        log.debug("支付宝退货结果:" + response.isSuccess());
        return response.isSuccess();
    }

    /**
     * 支付宝回调验签
     *
     * @param request 回调请求
     * @return true成功
     * 备注:验签成功后，按照支付结果异步通知中的描述(二次验签接口,貌似称为历史接口了)
     */
    public boolean verifyNotify(HttpServletRequest request) throws AlipayApiException {
        Map<String, String> paranMap = SignUtil.request2Map(request);
        log.debug("支付宝回调参数:" + paranMap.toString());
        boolean isVerify = false;
        if (AliPayConstants.SUCCESS_REQUEST.equals(paranMap.get("trade_status")) || AliPayConstants.TRADE_CLOSED.equals(paranMap.get("trade_status"))) {
            isVerify = AlipaySignature.rsaCheckV1(paranMap, AliPayConstants.ALIPAY_PUBLIC_KEY, AliPayConstants.CHARSET); //调用SDK验证签名
        }
        log.debug("支付宝验签结果" + isVerify);
        return isVerify;
    }


    /**
     * @MethodName: synCnoticeVerification
     * @Description: TODO 支付宝支付验签
     * @Param: [param]
     * @Return: java.lang.String
     * @Author: xubin
     * @Date: 2020/4/30
     * <p>
     * Map参数如下
     * gmt_create：该笔交易创建的时间
     * charset： 编码格式
     * seller_email：卖家支付宝账号
     * notify_time：通知的发送时间。格式为yyyy-MM-dd HH:mm:ss
     * subject：商品的名称
     * sign：签名
     * buyer_id：买家支付宝账号对应的支付宝唯一用户号。以2088开头的纯16位数字
     * version：调用的接口版本，固定为：1.0
     * notify_id：通知校验ID
     * notify_type：通知的类型
     * out_trade_no：原支付请求的商户订单号
     * total_amount：订单金额
     * trade_status： 交易状态 TRADE_CLOSED交易关闭true  TRADE_FINISHED交易完结true	  TRADE_SUCCESS支付成功true	 WAIT_BUYER_PAY交易创建false
     * trade_no：// 支付宝28位交易号
     * auth_app_id：
     * buyer_logon_id：买家支付宝账号
     * app_id：支付宝分配给开发者的应用Id
     * sign_type：签名算法类型
     * seller_id
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    public Boolean synCnoticeVerification(Map<String, String> paramsMap) {
        log.info("开始支付宝支付验签,paramsMap:[{}]", paramsMap);
        String outTradeNo = paramsMap.get("out_trade_no"); // 订单号
        try {
            //调用SDK验证签名
            boolean signVerified = AlipaySignature.rsaCertCheckV1(paramsMap, AliPayConstants.CERT_PATH, AliPayConstants.CHARSET, AliPayConstants.SIGN_TYPE);
            log.info("验证签名结果=[{}]", signVerified);
            if (!signVerified) {
                log.warn("支付宝支付签名验证失败, outTradeNo=[{}]", outTradeNo);
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        // APPID 校验
        if (!AliPayConstants.APP_ID.equals(paramsMap.get("app_id"))) {
            return false;
        }
        String tradeStatus = paramsMap.get("trade_status"); // 交易状态
//        String subject = paramsMap.get("subject");
        String totalAmount = paramsMap.get("total_amount");

        log.info("支付状态=" + tradeStatus);
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            List<OrderMsg> orders = orderMapper.selectByPrimaryKey(Long.valueOf(outTradeNo), null, 2, null, null);
            OrderMsg order = orders.get(0);

            if (!new BigDecimal(order.getOrderMoney()).setScale(2, BigDecimal.ROUND_HALF_UP).toString().equals(totalAmount)) {
                log.info("商品金额核对：失败, totalAmount:[{}], paymentMoney:[{}]", totalAmount, order.getOrderMoney());
                return false;
            }
            log.info("交易成功，修改订单状态");
            order.setPaymentMoney(order.getOrderMoney());
            order.setOrderStatus(1); // 订单状态：1：交易成功  2：待支付 3：交易失败
            order.setRmbMethod(1);// 现金支付方式：1：支付宝 2：微信 3：银联 4：其他
            order.setPayTime(new Date());
            int i = orderMapper.update(order);
            if (i < 1)
                return false;
            payService.handle(order);
            return true;
        } else {
            log.info("支付状态校验：失败, tradeStatus:[{}]", tradeStatus);
            upOrderMsg(outTradeNo);
            return true;
        }
    }

    /**
     * @MethodName: getHttpResponseWriter
     * @Description: TODO 返回页面
     * @Param: [form, httpResponse]
     * @Return: void
     * @Author: xubin
     * @Date: 14:24 2020/8/13
     **/
    public void getHttpResponseWriter(String form, HttpServletResponse httpResponse) throws IOException {
        httpResponse.setContentType("text/html;charset=" + AliPayConstants.CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }

    private void upOrderMsg(String orderNo) {
        OrderMsg order = new OrderMsg();
        order.setOrderSn(Long.valueOf(orderNo));
        order.setOrderStatus(3); // 订单状态：1：交易成功  2：待支付 3：交易失败
        order.setRmbMethod(1);// 现金支付方式：1：支付宝 2：微信 3：银联 4：其他
        orderMapper.update(order);
    }

    public static void main(String[] args) throws AlipayApiException {
//        String publicKey = AlipaySignature.getAlipayPublicKey("C:/Users/enuosfive/Downloads/appCertPublicKey_2021001182602691.crt");
//        //输出应用公钥的值
//        System.out.println("应用公钥的值:" + publicKey);
    }
}
