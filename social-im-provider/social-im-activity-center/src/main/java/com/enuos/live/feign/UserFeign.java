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
 * @Description 用户中心FEIGN
 * @Author wangyingjie
 * @Date 2020/9/17
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
     * @Description: 是否会员
     * @Param: [userId]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    @GetMapping(value = "member/isMember")
    Integer isMember(@RequestParam("userId") Long userId);

    /**
     * @Description: 计算金币
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    @PostMapping(value = "currency/countGold")
    Result countGold(@RequestBody Map<String, Long> params);

    /**
     * @Description: 计算钻石
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    @PostMapping(value = "currency/countDiamond")
    Result countDiamond(@RequestBody Map<String, Long> params);
}
