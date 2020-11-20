package com.enuos.live.rest;

import com.enuos.live.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @ClassName GiftService
 * @Description: TODO 调用订单中心
 * @Author xubin
 * @Date 2020/6/18
 * @Version V2.0
 **/
@Component
@FeignClient(contextId = "orderFeign", name = "SOCIAL-IM-ORDER"/*, fallback = OrderFeignFallback.class*/)
public interface OrderFeign {

    /**
     * @MethodName: entryBill
     * @Description: TODO 入账
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 11:26 2020/6/18
    **/
    @PostMapping("/payment/entryBill")
    Result entryBill(@RequestBody Map<String, Object> params);

}
