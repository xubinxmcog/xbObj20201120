package com.enuos.live.pay;

import com.enuos.live.feign.UserFeign;
import com.enuos.live.manager.TaskEnum;
import com.enuos.live.pojo.OrderMsg;
import com.enuos.live.service.PaymentService;
import com.enuos.live.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName PayService
 * @Description: TODO 支付处理
 * @Author xubin
 * @Date 2020/7/15
 * @Version V2.0
 **/
@Slf4j
@Service
public class PayService {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserFeign userFeign;

    public void handle(OrderMsg order) {
        Integer productType = order.getProductType(); // 商品类型 1: 钻石 2:会员 3:其他
        switch (productType) {
            case 1:
                log.info("钻石充值");
                paymentService.diamondRecharge(order);
                break;
            case 2:
                log.info("会员充值");
                userFeign.rechargeResult(new HashMap<String, Object>() {
                    {
                        put("userId", order.getUserId());
                        put("productId", order.getProductCode());
                    }
                });
                // productService.rechargeResult(order);
                break;
            case 3:
                log.info("其他商品类型3");
                break;
            default:
                log.error("支付后验证失败, 订单号:[{}]", order.getOrderSn());
        }
        Map<String, Object> map = new HashMap<String, Object>() {
            {
                put("userId", order.getUserId());
                put("code", TaskEnum.PMT0001.getCode());
                put("progress", 1);
                put("isReset", 0);
            }
        };
        new Thread(() -> userFeign.taskHandler(map)).start();
    }
}
