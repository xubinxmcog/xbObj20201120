package com.enuos.live.feign;

import com.enuos.live.feign.impl.ProducerFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @Description 生产者
 * @Author wangyingjie
 * @Date 2020/9/3
 * @Modified
 */
@Component
@FeignClient(name = "SOCIAL-IM-PRODUCER", fallback = ProducerFeignFallback.class)
public interface ProducerFeign {

    /**
     * @Description: 发送动态日志消息
     * @Param: [message]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/3
     */
    @PostMapping("/log/sendPost")
    void sendPost(@RequestBody Map<String, Object> message);

}
