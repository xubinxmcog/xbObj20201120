package com.enuos.live.feign;

import com.enuos.live.feign.impl.NettyFeignFallback;
import com.enuos.live.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
     * @MethodName: vipGradeNotice
     * @Description: TODO 会员升级通知
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 15:23 2020/8/25
     **/
    @PostMapping("/notice/vipGradeNotice")
    Result vipGradeNotice(@RequestBody Map<String, Object> params);
}
