package com.enuos.live.schedule;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.enuos.live.constants.RedisKey;
import com.enuos.live.mapper.ApplePayRecordMapper;
import com.enuos.live.mapper.OrderMapper;
import com.enuos.live.pay.aliPay.AlipayTrade;
import com.enuos.live.pay.applePay.ApplePayService;
import com.enuos.live.pay.weChatPay.WeChatPayService;
import com.enuos.live.pojo.ApplePayRecord;
import com.enuos.live.pojo.OrderMsg;
import com.enuos.live.result.Result;
import com.enuos.live.utils.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName OrderJobTask
 * @Description: TODO 定时任务处理工作
 * @Author xubin
 * @Date 2020/8/7
 * @Version V2.0
 **/
@Slf4j
@Component
public class OrderJobTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ApplePayRecordMapper applePayRecordMapper;

    @Autowired
    private ApplePayService applePayService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private WeChatPayService weChatPayService;

    @Autowired
    private AlipayTrade alipayTrade;

    /**
     * 支付异常订单处理
     */
    @Scheduled(cron = "${scheduledCron.applePay}") // 每 * 分钟执行一次
    public void roomOwnerExceptionEndBroadcast() {
        String uuid = IdUtil.simpleUUID();
        String lockKey = "KEY_LOCK:PAY";
        try {
            // 使用Redis加锁
            Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, 90, TimeUnit.SECONDS);
            if (!ifAbsent) {
                log.info("Redis加锁状态, lockKey=[{}]", lockKey);
                return;
            }
            log.info("开始支付异常订单处理");
            List<ApplePayRecord> list = applePayRecordMapper.getApplePayRecords();
            if (ObjectUtil.isNotEmpty(list)) {
                log.info("苹果支付异常订单处理");
                for (ApplePayRecord applePayRecord : list) {
                    Result result = applePayService.applePayOrderNotifyCallBack1(String.valueOf(applePayRecord.getOrderSn()), applePayRecord.getPayload(),
                            applePayRecord.getUserId(), applePayRecord.getTransactionId());
                    log.info("苹果支付异常订单处理结果=[{}]", result);
                }
            }
            String dateStr = TimeUtil.millisDateStr(System.currentTimeMillis() - 1800000);
            List<OrderMsg> orders = orderMapper.selectOrderStatus2(dateStr);
            if (ObjectUtil.isNotEmpty(orders)) {
                log.info("微信支付异常订单处理");
                for (OrderMsg order : orders) {
                    int rmbMethod = order.getRmbMethod();
                    switch (rmbMethod) {
                        case 1:
                            log.info("支付宝支付异常订单处理, 订单号=[{}]", order.getOrderSn());
                            alipayTrade.aliPayTradeQuery(String.valueOf(order.getOrderSn()));
                            break;
                        case 2:
                            log.info("微信支付异常订单处理, 订单号=[{}]", order.getOrderSn());
                            weChatPayService.wxOrderQuery(String.valueOf(order.getOrderSn()));
                            break;
                        default:
                            log.info("支付异常订单支付方式=[{}]", rmbMethod);
                            break;
                    }
                }
            }

        } catch (Exception e) {
            log.error("支付异常订单处理任务异常!!! ");
            e.printStackTrace();
        } finally {
            if (uuid.equals(redisTemplate.opsForValue().get(lockKey))) {
                redisTemplate.delete(lockKey);
            }
        }
    }

}
