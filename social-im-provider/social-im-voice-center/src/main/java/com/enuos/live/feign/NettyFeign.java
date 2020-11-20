package com.enuos.live.feign;

import com.enuos.live.feign.impl.NettyFeignFallback;
import com.enuos.live.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @MethodName:
 * @Description: TODO socket 消息推送
 * @Param:
 * @Return:
 * @Author: xubin
 * @Date: 12:55 2020/8/25
 **/
@Component
@FeignClient(contextId = "nettyFeign", name = "SOCKET-SERVER", fallback = NettyFeignFallback.class)
public interface NettyFeign {

    /**
     * @MethodName: giveGiftNotice
     * @Description: TODO 赠送礼物通知
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 13:00 2020/8/25
     **/
    @PostMapping("/notice/giveGiftNotice")
    Result giveGiftNotice(@RequestBody Map<String, Object> params);

    /**
     * @MethodName: exceptionEndBroadcast
     * @Description: TODO 异常下播
     * @Param: [roomId, userId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 15:19 2020/9/24
     **/
    @GetMapping("/notice/exceptionEndBroadcast")
    Result exceptionEndBroadcast(@RequestParam("roomId") Long roomId, @RequestParam("userId")Long userId);
}
