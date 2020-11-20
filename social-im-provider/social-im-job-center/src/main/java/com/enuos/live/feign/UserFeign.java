package com.enuos.live.feign;

import com.enuos.live.feign.impl.UserFeignFallback;
import com.enuos.live.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/15
 * @Modified
 */
@Component
@FeignClient(name = "SOCIAL-IM-USER", fallback = UserFeignFallback.class)
public interface UserFeign {

    /**
     * @Description: 奖励
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/15
     */
    @PostMapping(value = "/reward/handler")
    Result rewardHandler(@RequestBody Map<String, Object> params);

    /**
     * @Description: 成就进度统一处理
     * @Param: [params]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/13
     */
    @PostMapping(value = "/achievement/handlers")
    void achievementHandlers(@RequestBody Map<String, Object> params);
    
    /** 
     * @Description: 处理会员装饰
     * @Param: [userId, vip]
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/7/31 
     */ 
    @GetMapping(value = "/member/decorationHandler")
    void decorationHandler(@RequestParam("userId") Long userId, @RequestParam("vip") Integer vip);

}
