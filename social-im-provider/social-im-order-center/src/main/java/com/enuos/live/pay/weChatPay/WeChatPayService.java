package com.enuos.live.pay.weChatPay;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.BusinessPayMsgMapper;
import com.enuos.live.mapper.OrderMapper;
import com.enuos.live.pay.PayService;
import com.enuos.live.pay.weChatPay.util.WeChatPayUtil;
import com.enuos.live.pay.weChatPay.util.WxUtil;
import com.enuos.live.pojo.BusinessPayMsg;
import com.enuos.live.pojo.OrderMsg;
import com.enuos.live.result.Result;
import com.enuos.live.utils.RandomUtil;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.enuos.live.pay.weChatPay.constants.WeChatPayConstants.*;
import static com.enuos.live.pay.weChatPay.util.WeChatPayUtil.*;
import static com.enuos.live.pay.weChatPay.util.WxUtil.createNonceStr;
import static com.enuos.live.pay.weChatPay.util.WxUtil.createTimestamp;

/**
 * @ClassName WeChatPayService
 * @Description: TODO 微信支付处理
 * @Author xubin
 * @Date 2020/8/6
 * @Version V2.0
 **/
@Slf4j
@Service
public class WeChatPayService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PayService payService;

    @Autowired
    private BusinessPayMsgMapper businessPayMsgMapper;

    /**
     * @MethodName: paySignSignatureAction
     * @Description: TODO  APP、JSAPI 微信支付处理
     * @Param: [prams(订单信息), tradeType(交易类型 JSAPI--JSAPI支付（或小程序支付）、NATIVE--Native支付、APP--app支付，MWEB--H5支付), request, response]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 15:26 2020/8/13
     **/
    public Result paySignSignatureAction(Map<String, Object> prams, String tradeType,
                                         HttpServletRequest request, HttpServletResponse response) throws Exception {
        Result result1 = null;
        SortedMap<String, String> parameters = new TreeMap<>();
        if (Objects.isNull(prams.get("productName"))
                || Objects.isNull(prams.get("orderNo"))
                || Objects.isNull(prams.get("totalAmount"))
                || Objects.isNull(prams.get("ipAddress"))) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "参数不可为空");
        }
        String productName = prams.get("productName").toString();//名称
        String orderNo = prams.get("orderNo").toString(); // 订单号
        String totalAmount = prams.get("totalAmount").toString(); // 金额
        String ipAddress = prams.get("ipAddress").toString(); // IP地址
        String openId = prams.get("openId") == null ? null : prams.get("openId").toString(); //
        if ("JSAPI".equals(tradeType) && StrUtil.isEmpty(openId)) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "openId不可为空");
        }
        String nonce_str = createNonceStr();
        String timestamp = createTimestamp();
        // 获取prepayId
        try {

            // 修改支付方式
            OrderMsg order = new OrderMsg();
            order.setRmbMethod(2);// 现金支付方式：1：支付宝 2：微信 3：银联 4：其他
            order.setOrderSn(Long.valueOf(orderNo));
            order.setOrderMoney(totalAmount);
            order.setTradeType(tradeType);
            int update = orderMapper.update(order);
            if (update > 0) {
                result1 = Result.success(parameters);
//                    createImage(codeUrl, null, response);
            } else {
                log.warn("金额校验不正确");
                return Result.error(ErrorCode.EXCEPTION_CODE, "订单金额校验不正确");
            }

            double amount = Double.parseDouble(totalAmount) * 100;
            String result = getPrepayId(request, productName, orderNo, replace(String.valueOf(amount)), tradeType, openId, ipAddress);
            Map<String, String> map = WxUtil.xmlToMap(result);
            log.info("微信统一下单返回结果解析=[{}]", map);
            switch (tradeType) {
                case "APP":
                    return app(map, nonce_str, timestamp);
//                    break;
                case "JSAPI":
                    break;
                case "NATIVE":
                    break;
                case "MWEB":
                    break;
                default:
                    log.warn("不支持的支付方式,[{}]", tradeType);
            }
            if (StrUtil.isNotEmpty(map.get("prepay_id"))) {
                // 重新生成签名
                parameters.put("appId", APP_ID);
                parameters.put("nonceStr", nonce_str);
                if ("JSAPI".equals(tradeType)) {
                    parameters.put("package", "prepay_id=" + map.get("prepay_id"));
                }
                parameters.put("signType", SING_MD5);
                parameters.put("timeStamp", timestamp);
                parameters.put("paySign", WxUtil.getSignature(parameters, API_KEY, SING_MD5));
                parameters.put("prepay_id", map.get("prepay_id"));
            } else {
                log.warn("----支付数据错误----");
//                WxUtil.responsePrint(response, Result.error(201, "生成二维码失败"));
                result1 = Result.error(ErrorCode.EXCEPTION_CODE, "data error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result1;
    }

    /**
     * @MethodName: app
     * @Description: TODO APP 支付处理
     * @Param: [map, nonce_str, timestamp]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 15:29 2020/8/17
     **/
    public Result app(Map<String, String> map, String nonce_str, String timestamp) {
        Result result1 = null;
        SortedMap<String, String> parameters = new TreeMap<>();
        try {
            if (StrUtil.isNotEmpty(map.get("prepay_id"))) {
                log.info("APP支付处理");
                // 重新生成签名
                parameters.put("appid", AAPP_ID);
                parameters.put("noncestr", nonce_str);
//                parameters.put("partnerId", map.get("partner_id"));
                parameters.put("partnerid", MCH_ID);
                parameters.put("package", "Sign=WXPay");
                parameters.put("timestamp", timestamp);
                parameters.put("prepayid", map.get("prepay_id"));
                parameters.put("paySign", WxUtil.getSignature(parameters, API_KEY, SING_MD5));
                parameters.put("signType", SING_MD5);
                result1 = Result.success(parameters);
            } else {
                log.warn("----APP支付数据错误----");
                result1 = Result.error(ErrorCode.EXCEPTION_CODE, "data error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result1;
    }


    public Result wapSignSignatureAction(Map<String, Object> prams,
                                         HttpServletRequest request, HttpServletResponse response) throws Exception {
        Result result1 = null;
        SortedMap<String, String> parameters = new TreeMap<>();
        log.info("微信JSAPI支付入参=[{}]", prams);
        if (Objects.isNull(prams.get("productName"))
                || Objects.isNull(prams.get("orderNo"))
                || Objects.isNull(prams.get("totalAmount"))
                || Objects.isNull(prams.get("ipAddress"))) {
            result1 = Result.error(ErrorCode.EXCEPTION_CODE, "参数不可为空");
        }
        String productName = prams.get("productName").toString();//名称
        String orderNo = prams.get("orderNo").toString(); // 订单号
        String totalAmount = prams.get("totalAmount").toString(); // 金额
        String ipAddress = prams.get("ipAddress").toString(); // IP地址
        String openId = prams.get("openId").toString(); //
//      code = request.getParameter("code");
        log.info("openId=[{}]", openId);
        // code作为换取access_token的票据，每次用户授权带上的code将不一样，code只能使用一次，5分钟未被使用自动过期。
        // 通过code换取网页授权access_token
//        Map<String, String> data = getAccess_tokenByCode(code, response);
//        String openid = data.get("openid");
        String nonce_str = createNonceStr();
        String timestamp = createTimestamp();
        // 获取prepayId
        try {
            double amount = Double.parseDouble(totalAmount) * 100;
            String result = getPrepayId(request, productName, orderNo, replace(String.valueOf(amount)), "JSAPI", openId, ipAddress);
            Map<String, String> map = WxUtil.xmlToMap(result);
            log.info("微信JSAPI返回信息=[{}]", map);
            if (StrUtil.isNotEmpty(map.get("prepay_id"))) {
                // 重新生成签名
                parameters.put("appId", APP_ID);
                parameters.put("nonceStr", nonce_str);
                parameters.put("package", "prepay_id=" + map.get("prepay_id"));
                parameters.put("signType", SING_MD5);
                parameters.put("timeStamp", timestamp);
                parameters.put("paySign", WxUtil.getSignature(parameters, API_KEY, SING_MD5));
                parameters.put("prepay_id", map.get("prepay_id"));

                // 修改支付方式
                OrderMsg order = new OrderMsg();
                order.setRmbMethod(2);// 现金支付方式：1：支付宝 2：微信 3：银联 4：其他
                order.setOrderSn(Long.valueOf(orderNo));
                order.setOrderMoney(totalAmount);
                int update = orderMapper.update(order);
                if (update > 0) {
                    result1 = Result.success(parameters);
//                    createImage(codeUrl, null, response);
                } else {
                    log.warn("金额校验不正确");
                    result1 = Result.error(ErrorCode.EXCEPTION_CODE, "订单金额校验不正确");
                }
            } else {
                log.warn("----JSAPI支付数据错误----");
//                WxUtil.responsePrint(response, Result.error(201, "生成二维码失败"));
                result1 = Result.error(ErrorCode.EXCEPTION_CODE, "data error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result1;
    }

    /**
     * @MethodName: NativeSignSignatureAction
     * @Description: TODO 微信二维码支付
     * @Param: [prams, request, response]
     * @Return: void
     * @Author: xubin
     * @Date: 17:33 2020/8/6
     **/
    public Result NativeSignSignatureAction(Map<String, Object> prams,
                                            HttpServletRequest request, HttpServletResponse response) throws Exception {
        Result result1 = null;
        String productName = MapUtil.getStr(prams, "productName");//名称
        String orderNo = MapUtil.getStr(prams, "orderNo"); // 订单号
        String totalAmount = MapUtil.getStr(prams, "totalAmount"); // 金额
        String ipAddress = MapUtil.getStr(prams, "ipAddress"); // IP地址
        String nonce_str = WxUtil.createNonceStr();
        String timestamp = WxUtil.createTimestamp();
        double amount = Double.parseDouble(totalAmount) * 100;

        try {
            String result = getPrepayId(request, productName, orderNo, replace(String.valueOf(amount)), "NATIVE", null, ipAddress);
            Map<String, String> map = WxUtil.xmlToMap(result);
            log.info("map=[{}]", map);
            String codeUrl = map.get("code_url");
            if (StrUtil.isNotEmpty(codeUrl)) {
                // 修改支付方式
                OrderMsg order = new OrderMsg();
                order.setRmbMethod(2);// 现金支付方式：1：支付宝 2：微信 3：银联 4：其他
                order.setOrderSn(Long.valueOf(orderNo));
                order.setOrderMoney(totalAmount);
                order.setTradeType("NATIVE");
                int update = orderMapper.update(order);
                if (update > 0) {
                    result1 = Result.success(codeUrl);
//                    createImage(codeUrl, null, response);
                } else {
                    log.warn("金额校验不正确");
//                    WxUtil.responsePrint(response, Result.error(201, "金额校验不正确"));
                    result1 = Result.error(201, "金额校验不正确");
                }
            } else {
                log.warn("----生成二维码失败----");
//                WxUtil.responsePrint(response, Result.error(201, "生成二维码失败"));
                result1 = Result.error(201, "生成二维码失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result1;
    }

    /**
     * @MethodName: NativeSignSignatureAction
     * @Description: TODO 微信H5支付
     * @Param: [prams, request, response]
     * @Return: void
     * @Author: xubin
     * @Date: 17:33 2020/8/6
     **/
    public Result webSignSignatureAction(Map<String, Object> prams,
                                         HttpServletRequest request, HttpServletResponse response) throws Exception {
        Result result1 = null;
        SortedMap<Object, Object> parameters = new TreeMap<>();
        String productName = prams.get("productName").toString();//名称
        String orderNo = prams.get("orderNo").toString(); // 订单号
        String totalAmount = prams.get("totalAmount").toString(); // 金额
        String ipAddress = prams.get("ipAddress").toString(); // IP地址
        String nonce_str = WxUtil.createNonceStr();
        String timestamp = WxUtil.createTimestamp();
        double amount = Double.parseDouble(totalAmount) * 100;

        try {
            String result = getPrepayId(request, productName, orderNo, replace(String.valueOf(amount)), "MWEB", null, ipAddress);
            Map<String, String> map = WxUtil.xmlToMap(result);
            log.info("map=[{}]", map);
            String mwebUrl = map.get("mweb_url");
            if (StrUtil.isNotEmpty(mwebUrl)) {
                // 修改支付方式
                OrderMsg order = new OrderMsg();
                order.setRmbMethod(2);// 现金支付方式：1：支付宝 2：微信 3：银联 4：其他
                order.setOrderSn(Long.valueOf(orderNo));
                order.setOrderMoney(totalAmount);
                order.setTradeType("MWEB");
                int update = orderMapper.update(order);
                if (update > 0) {
                    //支付完返回浏览器跳转的地址，如跳到查看订单页面
                    String redirect_url = REDIRECT_URL;
                    String redirect_urlEncode = URLEncoder.encode(redirect_url, "utf-8");//对上面地址urlencode
                    mwebUrl = mwebUrl + "&redirect_url=" + redirect_urlEncode;//拼接返回地址
                    result1 = Result.success(mwebUrl);
//                    createImage(codeUrl, null, response);
                } else {
                    log.warn("金额校验不正确");
//                    WxUtil.responsePrint(response, Result.error(201, "金额校验不正确"));
                    result1 = Result.error(201, "金额校验不正确");
                }
            } else {
                log.warn("----H5支付失败----");
//                WxUtil.responsePrint(response, Result.error(201, "生成二维码失败"));
                result1 = Result.error(201, "支付失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result1;
    }

    /**
     * @MethodName: wxOrderQuery
     * @Description: TODO 订单查询
     * @Param: [orderNo]
     * @Return: java.lang.String
     * @Author: xubin
     * @Date: 12:46 2020/8/10
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    public Result wxOrderQuery(String orderNo) throws Exception {
        SortedMap<String, String> data = new TreeMap<>();
        //公众账号ID
        data.put("appid", APP_ID);
        //商户号
        data.put("mch_id", MCH_ID);
        //随机字符串
        data.put("nonce_str", createNoncestr());
        //商户订单号
        data.put("out_trade_no", orderNo);
        //签名类型
        data.put("sign_type", SING_MD5);
        //签名 签名中加入key
        data.put("sign", WxUtil.getSignature(data, API_KEY, SING_MD5));
        String requestXML = WeChatPayUtil.getRequestXml(data);
        //调用统一下单接口
        String result = httpsRequest(PAY_ORDERQUERY, String.valueOf(HttpMethod.POST), requestXML);
        //解析返回的xml
        Map<String, String> resultMap = WxUtil.xmlToMap(result);
        log.info("微信订单查询结果=[{}]", resultMap);
        if ("SUCCESS".equals(resultMap.get("return_code")) && "SUCCESS".equals(resultMap.get("trade_state"))) {
            List<OrderMsg> orders = orderMapper.selectByPrimaryKey(Long.valueOf(orderNo), null, 2, null, null);
            if (ObjectUtil.isNotEmpty(orders)) {
                OrderMsg order = orders.get(0);
                log.info("交易成功，修改订单状态");
                double total_fee = Double.parseDouble(resultMap.get("total_fee")) / 100;
                order.setPaymentMoney(new BigDecimal(total_fee).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                order.setOrderStatus(1); // 订单状态：1：交易成功  2：待支付 3：交易失败
                order.setRmbMethod(2);// 现金支付方式：1：支付宝 2：微信 3：银联 4：其他
                order.setPayTime(new Date());
                int update = orderMapper.update(order);
                if (update > 0) {
                    payService.handle(order);
                } else {
                    log.warn("订单更新结果:[{}],orderNo=[{}]", update, orderNo);
                }
            } else {
                log.warn("该订单已确认,orderNo=[{}]", orderNo);
            }

            return Result.success(resultMap.get("trade_state"));
        } else {
            log.warn("订单支付失败orderNo=[{}], trade_state_desc=[{}]", orderNo, resultMap.get("trade_state_desc"));
            OrderMsg order = new OrderMsg();
            order.setOrderSn(Long.valueOf(orderNo));
            order.setOrderStatus(3); // 订单状态：1：交易成功  2：待支付 3：交易失败
            order.setRmbMethod(2);// 现金支付方式：1：支付宝 2：微信 3：银联 4：其他
            order.setPayTime(new Date());
            int update = orderMapper.update(order);
            Map msg = new HashMap();
            msg.put("code", RETURN_CODE_MAP.get(resultMap.get("trade_state")));
            msg.put("msg", resultMap.get("trade_state_desc"));
            return Result.error(ErrorCode.EXCEPTION_CODE, msg.toString());
        }
    }

    public static void main(String[] args) {
        Map<String, String> resultMap = new HashMap();
        resultMap.put("total_fee", "100");
        double total_fee = Double.parseDouble(resultMap.get("total_fee")) / 100;
        System.out.println(new BigDecimal(total_fee).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        System.out.println(RandomUtil.getRandom());

        double getMoney = 1.2;
        int vv = (int) (getMoney * 100); // 查询金额 转为分
        System.out.println(vv);
    }

    /**
     * @MethodName: callbackNative
     * @Description: TODO 支付回调地址
     * @Param: [request, response]
     * @Return: void
     * @Author: xubin
     * @Date: 17:44 2020/8/6
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    public void callbackNative(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //商户订单号
        String outTradeNo = null;
        String xmlContent = "<xml>" +
                "<return_code><![CDATA[FAIL]]></return_code>" +
                "<return_msg><![CDATA[签名失败]]></return_msg>" +
                "</xml>";

        try {
            String requestXml = WxUtil.getStreamString(request.getInputStream());
            log.info("requestXml : [{}]", requestXml);
            Map<String, String> map = WxUtil.xmlToMap(requestXml);
            log.info("解析后参数:[{}]", map);
            String returnCode = map.get("return_code");
            log.info("returnCode=[{}]", returnCode);
            //校验一下 ，判断是否已经支付成功
            if (StrUtil.isNotBlank(returnCode) && StrUtil.equals(returnCode, "SUCCESS") && WxUtil.isSignatureValid(map, API_KEY, SING_MD5)) {
                //商户订单号
                outTradeNo = map.get("out_trade_no");
                log.info("商户订单号,outTradeNo : " + outTradeNo);
                //微信支付订单号
                String transactionId = map.get("transaction_id");
                log.info("微信支付订单号, transactionId : " + transactionId);
                //支付完成时间
                SimpleDateFormat payFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date payDate = payFormat.parse(map.get("time_end"));

                SimpleDateFormat systemFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                log.info("支付时间：" + systemFormat.format(payDate));
                //临时缓存
//                WeChatPayUtil.setPayMap(outTradeNo, "SUCCESS");

                //根据支付结果修改数据库订单状态
                List<OrderMsg> orders = orderMapper.selectByPrimaryKey(Long.valueOf(outTradeNo), null, 2, null, null);
                log.info("交易成功，修改订单状态");
                if (ObjectUtil.isNotEmpty(orders)) {
                    OrderMsg order = orders.get(0);
                    order.setPaymentMoney(order.getOrderMoney());
                    order.setOrderStatus(1); // 订单状态：1：交易成功  2：待支付 3：交易失败
                    order.setRmbMethod(2);// 现金支付方式：1：支付宝 2：微信 3：银联 4：其他
                    order.setPayTime(new Date());
                    int update = orderMapper.update(order);
                    if (update > 0) {
                        payService.handle(order);
                    } else {
                        log.warn("订单更新结果:[{}],outTradeNo=[{}]", update, outTradeNo);
                    }
                } else {
                    log.warn("该订单已确认,outTradeNo=[{}]", outTradeNo);
                }

                //给微信的应答 xml, 通过 response 回写
                xmlContent = "<xml>" +
                        "<return_code><![CDATA[SUCCESS]]></return_code>" +
                        "<return_msg><![CDATA[OK]]></return_msg>" +
                        "</xml>";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        WxUtil.responsePrint(response, xmlContent);
    }

    /**
     * 企业向用户支付
     *
     * @param params
     * @return
     */

    private final ReentrantLock lock = new ReentrantLock();

    public Result businessPay(Map<String, Object> params) {

        String reUserName = MapUtil.getStr(params, "reUserName");
        Long userId = MapUtil.getLong(params, "userId");
        String openId = MapUtil.getStr(params, "openid");
        String partnerTradeNo = MapUtil.getStr(params, "tradeno");
        String amount = MapUtil.getStr(params, "amount");
        String desc = MapUtil.getStr(params, "desc");
        if (null == userId || null == openId || null == amount || null == desc || null == partnerTradeNo) {
            return Result.error(201, "必填参数不能为空");
        }
        final ReentrantLock lock = this.lock;
        lock.lock();

        Result result = null;
        try {
            SortedMap<String, String> data = new TreeMap<>();

            Map<String, Object> byOrderIdMoney = businessPayMsgMapper.getByOrderIdMoney(partnerTradeNo);
            double aDouble = Double.valueOf(amount);
            int xx = (int) (aDouble * 100); // 入参金额 转为分
            if (ObjectUtil.isNotEmpty(byOrderIdMoney) && StrUtil.isNotEmpty(MapUtil.getStr(byOrderIdMoney, "getMoney"))) {
                double getMoney = MapUtil.getDouble(byOrderIdMoney, "getMoney");
                int vv = (int) (getMoney * 100); // 查询金额 转为分
                long userId1 = MapUtil.getLong(byOrderIdMoney, "userId");
                if (vv != xx && userId1 != userId) {
                    log.info("订单校验失败,订单号=[{}], userId=[{}], getMoney=[{}],amount=[{}]", userId, getMoney, amount);
                    return Result.error(400, "订单校验失败");
                }
            }

            BusinessPayMsg selectBusinessPayMsg = businessPayMsgMapper.selectByOpenId(partnerTradeNo);
            if (ObjectUtil.isNotEmpty(selectBusinessPayMsg)) {
                String resultCode = selectBusinessPayMsg.getResultCode();
                if (!"SUCCESS".equals(resultCode)) {
                    String errMsg = selectBusinessPayMsg.getErrCode() + "：" + selectBusinessPayMsg.getErrCodeDes();
                    log.info("存在异常订单,订单号=[{}], 用户openid=[{}], 异常信息=[{}]", selectBusinessPayMsg.getPartnerTradeNo(), selectBusinessPayMsg.getOpenId(), errMsg);
//                return Result.error(400, errMsg);
                } else {
                    return Result.error(400, "已支付订单");
                }
            }

            data.put("mch_appid", AAPP_ID); // 商户账号appid
            data.put("mchid", MCH_ID); // 商户号
            data.put("nonce_str", createNoncestr()); // 随机字符串，不长于32位
            data.put("partner_trade_no", partnerTradeNo); // 商户订单号，需保持唯一性(只能是字母或者数字，不能包含有其它字符)
            data.put("openid", openId); // 用户openid
            data.put("check_name", "NO_CHECK"); // NO_CHECK：不校验真实姓名  FORCE_CHECK：强校验真实姓名
//        data.put("re_user_name", reUserName); // 收款用户真实姓名。如果check_name设置为FORCE_CHECK，则必填用户真实姓名
            data.put("amount", xx + ""); // 企业付款金额，单位为分
            data.put("desc", desc); // 企业付款备注

            data.put("sign", WxUtil.getSignature(data, API_KEY, SING_MD5)); // 签名
            String requestXML = WeChatPayUtil.getRequestXml(data);
            String resultMsg = posts(BUSINESS_PAY_URL, requestXML); // 发起请求
            log.info("向用户支付结果=[{}]", resultMsg);
            Map<String, String> map = WxUtil.xmlToMap(resultMsg);
            log.info("解析后结果=[{}]", map);

            BusinessPayMsg businessPayMsg = new BusinessPayMsg();
            businessPayMsg.setUserId(userId);
            businessPayMsg.setReUserName(reUserName);
            businessPayMsg.setOpenId(openId);
            businessPayMsg.setPartnerTradeNo(partnerTradeNo);
            businessPayMsg.setAmount(xx + "");
            businessPayMsg.setDesc(desc);


            if (ObjectUtil.isNotEmpty(map)) {
                String resultCode = MapUtil.getStr(map, "result_code");
                businessPayMsg.setResultCode(resultCode); // 支付结果
                if ("SUCCESS".equals(resultCode)) {
                    // 支付成功
                    String paymentNo = MapUtil.getStr(map, "payment_no");// 业务结果
                    String paymentTime = MapUtil.getStr(map, "payment_time");// 付款成功时间
                    businessPayMsg.setPaymentNo(paymentNo);
                    businessPayMsg.setPaymentTime(paymentTime);
                    result = Result.success(map);
                    businessPayMsgMapper.upMoneyGet(2, partnerTradeNo);

                } else {
                    int orderStatus = -1;
                    String errCode = MapUtil.getStr(map, "err_code");// 错误代码
                    String errCodeDes = MapUtil.getStr(map, "err_code_des");// 错误信息
                    businessPayMsg.setErrCode(errCode);
                    businessPayMsg.setErrCodeDes(errCodeDes);
                    result = Result.error(400, map.toString());
                    if (StrUtil.isNotEmpty(errorCodeMap.get(errCode))) {
                        orderStatus = 1;
                    }
                    businessPayMsgMapper.upMoneyGet(orderStatus, partnerTradeNo);
                }

                if (ObjectUtil.isNotEmpty(selectBusinessPayMsg)) {
                    businessPayMsgMapper.updateByPrimaryKeySelective(businessPayMsg);
                } else {
                    businessPayMsgMapper.insert(businessPayMsg);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return result;
    }

    /**
     * 企业向个人转账查询
     *
     * @param tradeno
     * @return
     */
    public Result orderPayQuery(String tradeno) {

        Map<String, String> restmap = null;
        try {
            SortedMap<String, String> parm = new TreeMap<>();
            parm.put("appid", AAPP_ID);
            parm.put("mch_id", MCH_ID);
            parm.put("partner_trade_no", tradeno);
            parm.put("nonce_str", createNoncestr());
            parm.put("sign", WxUtil.getSignature(parm, API_KEY, SING_MD5));
            String requestXML = WeChatPayUtil.getRequestXml(parm);
            String restxml = posts(BUSINESS_SELECT_URL, requestXML);
            restmap = WxUtil.xmlToMap(restxml);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if (ObjectUtil.isNotEmpty(restmap) && "SUCCESS".equals(restmap.get("result_code"))) {
            // 订单查询成功 处理业务逻辑
            log.info("订单查询：订单" + restmap.get("partner_trade_no") + "支付成功");
            Map<String, String> transferMap = new HashMap<>();
            transferMap.put("partnerTradeNo", restmap.get("partner_trade_no"));//商户转账订单号
            transferMap.put("openid", restmap.get("openid")); //收款微信号
            transferMap.put("paymentAmount", restmap.get("payment_amount")); //转账金额
            transferMap.put("transferTime", restmap.get("transfer_time")); //转账时间
            transferMap.put("desc", restmap.get("desc")); //转账描述

            BusinessPayMsg businessPayMsg = new BusinessPayMsg();
            businessPayMsg.setPartnerTradeNo(restmap.get("partner_trade_no"));
            businessPayMsg.setPaymentTime(restmap.get("transfer_time"));
            businessPayMsg.setPaymentNo(restmap.get("payment_no"));
            businessPayMsgMapper.upMoneyGet(2, tradeno);
            businessPayMsgMapper.updateByPrimaryKeySelective(businessPayMsg);
            return Result.success(transferMap);
        } else {
            if (ObjectUtil.isNotEmpty(restmap)) {
                businessPayMsgMapper.upMoneyGet(-1, tradeno);
                log.info("订单转账失败：" + restmap.get("err_code") + ":" + restmap.get("err_code_des"));
            }
            return Result.error(00000001, "订单转账失败");
        }
    }

    /**
     * @MethodName: getPrepayId
     * @Description: TODO 微信统一下单
     * @Param: [request, productName, orderNo, totalAmount, tradeType, openid]
     * @Return: java.lang.String
     * @Author: xubin
     * @Date: 17:36 2020/8/6
     **/
    public static String getPrepayId(HttpServletRequest request,
                                     String productName, String orderNo,
                                     String totalAmount, String tradeType,
                                     String openid, String ipAddress) throws Exception {
        SortedMap<String, String> parameters = new TreeMap<>();
        if ("APP".equals(tradeType)) {
            parameters.put("appid", AAPP_ID);
        } else {
            parameters.put("appid", APP_ID);
        }
        parameters.put("mch_id", MCH_ID);
        parameters.put("nonce_str", createNoncestr());//随机字符串
        parameters.put("body", "7乐-" + productName); //商品描述
        parameters.put("out_trade_no", orderNo);// 订单号
        parameters.put("fee_type", "CNY");//标价币种
        parameters.put("total_fee", totalAmount);// 交易金额 >默认为人民币交易，接口中参数支付金额单位为【分】，参数值不能带小数。对账单中的交易金额单位为【元】。
        parameters.put("spbill_create_ip", ipAddress/*getIp2(request)*/);
        parameters.put("notify_url", WECHAT_PAY_NOTIFY);
        parameters.put("trade_type", tradeType); // 交易类型 JSAPI--JSAPI支付（或小程序支付）、NATIVE--Native支付、APP--app支付，MWEB--H5支付
        if (StrUtil.isNotEmpty(openid)) {
            parameters.put("openid", openid);//openid :选填 trade_type=JSAPI时（即JSAPI支付），此参数必传，此参数为微信用户在商户对应appid下的唯一标识。
        }
        parameters.put("sign", WxUtil.getSignature(parameters, API_KEY, SING_MD5));
        String requestXML = WeChatPayUtil.getRequestXml(parameters);
        //调用统一下单接口
        String result = httpsRequest(WECHAT_PAY_URL, String.valueOf(HttpMethod.POST), requestXML);
        log.info("调用统一下单接口返回结果=[{}]", result);
        return result;
    }

    /**
     * 根据用户授权code获取access_token
     */
    public Map<String, String> getAccess_tokenByCode(String code, HttpServletResponse response) {
        Map<String, String> data = new HashMap<String, String>();
        String requestUrlMessageFormat = "https://api.weixin.qq.com/sns/oauth2/access_token?appid={0}&secret={1}&code={2}&grant_type=authorization_code";
        String requestUrl = MessageFormat.format(requestUrlMessageFormat, APP_ID, APP_SECRET, code);
        String requestMethod = "GET";
        String outputStr = "";
        JSONObject json = httpRequest(requestUrl, requestMethod, outputStr);
        String access_token = (String) json.get("access_token");
        String openid = (String) json.get("openid");
        data.put("access_token", access_token);
        data.put("openid", openid);
        return data;
    }

    /**
     * 使用java正则表达式去掉多余的.与0
     *
     * @param s
     * @return string
     */
    public static String replace(String s) {
        if (null != s && s.indexOf(".") > 0) {
            s = s.replaceAll("0+?$", "");//去掉多余的0
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
        }
        return s;
    }

    private static final Map<String, String> errorCodeMap = new HashMap<String, String>() {
        {
            put("NOTENOUGH", "付款帐号余额不足或资金未到账, 如果要继续付款必须使用原商户订单号重试。");
            put("SYSTEMERROR", "微信内部接口调用发生错误, 请先调用查询接口，查看此次付款结果，如结果为不明确状态（如订单号不存在），请务必使用原商户订单号进行重试。");
            put("NAME_MISMATCH", "收款人身份校验不通过, 如果要继续付款必须使用原商户订单号重试。");
            put("SIGN_ERROR", "校验签名错误, 请检查您的请求参数和签名密钥KEY是否正确，如果要继续付款必须使用原商户订单号重试。");
            put("FREQ_LIMIT", "接口请求频率超时接口限制, 调用接口过于频繁，请稍后再试，如果要继续付款必须使用原商户订单号重试。");
            put("MONEY_LIMIT", "已经达到今日付款总额上限/已达到付款给此用户额度上限，如果要继续付款必须使用原商户订单号重试。");
            put("CA_ERROR", "商户API证书校验出错, 请确认是否使用了正确的证书，可以前往商户平台重新下载，证书需与商户号对应，如果要继续付款必须使用原商户订单号重试。");
            put("V2_ACCOUNT_SIMPLE_BAN", "无法给未实名用户付款, 不支持给未实名用户付款，如果要继续付款必须使用原商户订单号重试。");
            put("PARAM_IS_NOT_UTF8", "请求参数中包含非utf8编码字符, 微信接口使用编码是UTF-8，请确认，如果要继续付款必须使用原商户订单号重试。");
            put("SENDNUM_LIMIT", "向用户付款的次数超限了，请参考接口使用条件，如果要继续付款必须使用原商户订单号重试。");
        }
    };
}
