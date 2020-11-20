package com.enuos.live.feign;

import com.enuos.live.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/15
 * @Modified
 */
@Component
@FeignClient(name = "SOCIAL-IM-ORDER")
public interface OrderFeign {

    /** 
     * @Description: 入用户背包 
     * @Param: [param]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/15 
     */ 
    @PostMapping(value = "/productBackpack/addBackpack")
    Result addBackpack(@RequestBody Map<String, Object> params);

    /**
     * @Description: 提现
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/11/3
     */
    @PostMapping("/payment/weChatPay/business/zfifus")
    Result businessPay(@RequestBody Map<String, String> params);
}
