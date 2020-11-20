package com.enuos.live.feign;

import com.enuos.live.feign.impl.OrderFeignFallback;
import com.enuos.live.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @Description 订单FEIGN
 * @Author wangyingjie
 * @Date 2020/10/10
 * @Modified
 */
@Component
@FeignClient(name = "SOCIAL-IM-ORDER", fallback = OrderFeignFallback.class)
public interface OrderFeign {

    /**
     * @Description: 入用户背包
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/10
     */
    @PostMapping(value = "/productBackpack/addBackpack")
    Result addBackpack(@RequestBody Map<String, Object> params);

}
