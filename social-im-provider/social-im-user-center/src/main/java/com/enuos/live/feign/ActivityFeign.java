package com.enuos.live.feign;

import com.enuos.live.feign.impl.ActivityFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description 活动
 * @Author wangyingjie
 * @Date 2020/10/29
 * @Modified
 */
@Component
@FeignClient(name = "SOCIAL-IM-ACTIVITY", fallback = ActivityFeignFallback.class)
public interface ActivityFeign {
    
    /** 
     * @Description: 乐享令状日常任务领奖
     * @Param: [userId, templateCode]
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/10/29 
     */
    @GetMapping("/writ/dailyTask")
    void dailyTaskOfWrit(@RequestParam("userId") Long userId, @RequestParam("templateCode") String templateCode);
}
