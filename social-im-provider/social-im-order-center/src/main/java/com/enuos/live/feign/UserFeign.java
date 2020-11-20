package com.enuos.live.feign;

import com.enuos.live.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Component
@FeignClient("SOCIAL-IM-USER")
public interface UserFeign {

    /**
     * 是否实名认证
     * @param userId
     * @return
     */
    @GetMapping(value = "/card/open/isAuthentication")
    Integer isAuthentication(@RequestParam("userId") Long userId);

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
     * TODO 任务达成.
     *
     * @param params 任务信息
     * @author WangCaiWen
     * @date 2020/7/29
     */
    @PostMapping(value = "/task/handler")
    void taskHandler(@RequestBody Map<String, Object> params);

    /**
     * @Description: 添加成长值
     * @Param: [userId, growth]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/17
     */
    @GetMapping("/member/addGrowth")
    Result addGrowth(@RequestParam("userId") Long userId, @RequestParam("growth") Integer growth);

    /** 
     * @Description: 会员充值
     * @Param: [params] 
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/9/15 
     */ 
    @PostMapping(value = "/member/rechargeResult")
    void rechargeResult(@RequestBody Map<String, Object> params);

}
