package com.enuos.live.pay.applePay;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.enuos.live.mapper.ApplePayRecordMapper;
import com.enuos.live.mapper.OrderMapper;
import com.enuos.live.pay.PayService;
import com.enuos.live.pay.applePay.util.IosVerifyUtil;
import com.enuos.live.pojo.ApplePayRecord;
import com.enuos.live.pojo.OrderMsg;
import com.enuos.live.result.Result;
import com.enuos.live.service.PaymentService;
import com.enuos.live.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName ApplePayServer
 * @Description: TODO 苹果支付服务
 * @Author xubin
 * @Date 2020/7/10
 * @Version V2.0
 **/
@Slf4j
@Service
public class ApplePayService {

    @Value("${spring.profiles.active}")
    private String typeEnvironment;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ApplePayRecordMapper applePayRecordMapper;

    @Autowired
    private PayService payService;

    /**
     * @MethodName: applepayOrderNotifyCallBack
     * @Description: TODO 苹果支付验证
     * @Param: [orderNo: 本地生成的订单, payload: 苹果验证参数, userId: 用户ID, transactionId: 苹果生成的订单号]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:04 2020/7/10
     **/
    /**
     * status: 支付状态
     * 0 正常
     * 21000 App Store无法读取你提供的JSON数据
     * 21002 receipt-data数据不符合格式
     * 21003 receipt无法通过验证
     * 21004 你提供的共享密钥和账户的共享密钥不一致
     * 21005 receipt服务器当前不可用
     * 21006 receipt合法，但是订阅已过期。服务器接收到这个状态码时，receipt数据仍然会解码并一起发送
     * 21007 receipt信息是测试用（sandbox），但却被发送到产品环境中验证 【请求sandbox校验支付凭证】
     * 21008 receipt是生产receipt，但却发送至Sandbox环境的验证服务
     */
    public Result applePayOrderNotifyCallBack(String orderNo, String payload, Long userId, String transactionId) {
        Result result = null;
        try {
            log.info("苹果支付新订单");
            ApplePayRecord record = new ApplePayRecord();
            record.setOrderSn(Long.valueOf(orderNo));
            record.setPayload(payload);
            record.setUserId(userId);
            record.setTransactionId(transactionId);
            record.setVerification(0);
            applePayRecordMapper.insert(record);
//            long startTime = System.currentTimeMillis();
//            String verifyResult = IosVerifyUtil.buyAppVerify(payload, typeEnvironment);
//            long endTime = System.currentTimeMillis();
//            long expTime = endTime - startTime;
//            log.info("请求苹果服务器返回验证结果耗时: [{}]毫秒", expTime);
//            if (verifyResult == null) {                                            // 苹果服务器没有返回验证结果
//                log.info("applePay/notifyCallBack无订单信息, orderNo=[{}], transactionId=[{}]", orderNo, transactionId);
//                result = Result.error(2, "无订单信息");
//            } else {
//                log.info("苹果平台返回数据:[{}]", verifyResult);
//                JSONObject job = JSONObject.parseObject(verifyResult);
//                String status = job.getString("status");
//                log.info("ApplePay支付状态:[{}]", status);
////                if ("21007".equals(states)) {                                            //是沙盒环境，应沙盒测试，否则执行下面
////                    verifyResult = IosVerifyUtil.buyAppVerify(payload, typeEnvironment);            //2.再沙盒测试  发送平台验证
////                    log.info("沙盒环境，苹果平台返回JSON:" + verifyResult);
////                    job = JSONObject.parseObject(verifyResult);
////                    states = job.getString("status");
////                }
//
//                if (status.equals("0")) { // 验证成功
//
//                    JSONObject returnJson = JSONObject.parseObject(job.getString("receipt"));
//                    JSONArray array = JSONArray.parseArray(returnJson.getString("in_app"));
//
//                    JSONObject json = (JSONObject) array.stream().filter(jsonObject ->
//                            ((JSONObject) jsonObject).getString("transaction_id").equals(transactionId)).findFirst().get();
//                    String transaction_id = json.getString("transaction_id");
//                    String product_id = json.getString("product_id");
//
//                    log.info("验证成功, 订单编号:[{}], 商品编码=[{}]", transaction_id, product_id);
//
//                    //如果单号一致  则开始处理逻辑
//                    int exist = applePayRecordMapper.isExist(transaction_id);
//                    if (exist == 0) {
//                        applePaySuccessVerification(orderNo, userId, transaction_id, payload);
//                        result = Result.success();
//
//                    } else {
//                        result = Result.error(3, "已验证过订单");
//                    }
//                } else {
//                    result = Result.error(1, "receipt数据有问题");
//                }
//            }
            result = applePayOrderNotifyCallBack1(orderNo, payload, userId, transactionId);
        } catch (Exception e) {
            e.printStackTrace();
            result = Result.error(4, "异常");
        }
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Result applePayOrderNotifyCallBack1(String orderNo, String payload, Long userId, String transactionId) {
        Result result = null;
        log.info("苹果支付客户端传过来的值:transactionId:[{}], orderNo=[{}],userId=[{}], payload:[{}]", transactionId, orderNo, userId, payload);
        try {
            long startTime = System.currentTimeMillis();
            String verifyResult = IosVerifyUtil.buyAppVerify(payload, typeEnvironment);
            long endTime = System.currentTimeMillis();
            long expTime = endTime - startTime;
            log.info("请求苹果服务器返回验证结果耗时: [{}]毫秒", expTime);
            if (verifyResult == null) {                                            // 苹果服务器没有返回验证结果
                log.info("applePay/notifyCallBack无订单信息, orderNo=[{}], transactionId=[{}]", orderNo, transactionId);
                result = Result.error(2, "无订单信息");
            } else {
                log.info("苹果平台返回数据:[{}]", verifyResult);
                JSONObject job = JSONObject.parseObject(verifyResult);
                String status = job.getString("status");
                log.info("ApplePay支付状态:[{}]", status);
//                if ("21007".equals(states)) {                                            //是沙盒环境，应沙盒测试，否则执行下面
//                    verifyResult = IosVerifyUtil.buyAppVerify(payload, typeEnvironment);            //2.再沙盒测试  发送平台验证
//                    log.info("沙盒环境，苹果平台返回JSON:" + verifyResult);
//                    job = JSONObject.parseObject(verifyResult);
//                    states = job.getString("status");
//                }
                if (status.equals("0")) { // 验证成功
                    JSONObject returnJson = JSONObject.parseObject(job.getString("receipt"));
                    JSONArray array = JSONArray.parseArray(returnJson.getString("in_app"));

                    JSONObject json = (JSONObject) array.stream().filter(jsonObject ->
                            ((JSONObject) jsonObject).getString("transaction_id").equals(transactionId)).findFirst().get();
                    String transaction_id = json.getString("transaction_id");
                    String product_id = json.getString("product_id");

                    log.info("验证成功, 订单编号:[{}], 商品编码=[{}]", transaction_id, product_id);

                    //如果单号一致  则开始处理逻辑
                    int exist = applePayRecordMapper.isExist(transaction_id);
                    if (exist == 0) {
                        applePaySuccessVerification(orderNo, userId, transaction_id, payload);
                        result = Result.success();

                    } else {
                        log.warn("已验证过订单transaction_id =[{}]", transaction_id);
                        result = Result.error(3, "已验证过订单");
                    }
                } else {
                    result = Result.error(1, "receipt数据有问题");
                    int update = applePayRecordMapper.update(Long.valueOf(orderNo), 2);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = Result.error(4, "异常");
        }
        return result;
    }

    /**
     * @MethodName: payVerification
     * @Description: TODO 支付成功后验证
     * @Param: [orderSn:本地订单编号, userId用户ID, transaction_id:苹果支付订单号, payload]
     * @Return: void
     * @Author: xubin
     * @Date: 15:39 2020/7/10
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    public void applePaySuccessVerification(String orderSn, Long userId, String transaction_id, String payload) {
        List<OrderMsg> orders = orderMapper.selectByPrimaryKey(Long.valueOf(orderSn), null, 2, null, null);
        OrderMsg order = orders.get(0);
        order.setOrderStatus(1); // 订单状态：1：交易成功  2：待支付 3：交易失败
        order.setRmbMethod(4);// 现金支付方式：1：支付宝 2：微信 3：银联 4：ApplePay 5:其他
        order.setPaymentMoney(order.getOrderMoney());
        order.setPayTime(new Date());
        order.setTradeType("APP");
        int i = orderMapper.update(order);
        if (i > 0) {
            int update = applePayRecordMapper.update(Long.valueOf(orderSn), 1);
            log.info("applePaySuccessVerification.applePayRecordMapper.update=[{}]", update);
            payService.handle(order);
        }
    }
}
