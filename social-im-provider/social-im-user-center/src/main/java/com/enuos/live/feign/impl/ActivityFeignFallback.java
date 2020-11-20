package com.enuos.live.feign.impl;

import com.enuos.live.feign.ActivityFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/10/29
 * @Modified
 */
@Slf4j
@Component
public class ActivityFeignFallback implements ActivityFeign {

    /**
     * @Description: 乐享令状日常任务领奖
     * @Param: [userId, templateCode]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/10/29
     */
    @Override
    public void dailyTaskOfWrit(@RequestParam("userId") Long userId, @RequestParam("templateCode") String templateCode) {
        log.error("==========[SOCIAL-IM-ACTIVITY ActivityFeign dailyTaskOfWrit error, params:[userId:{}, templateCode:{}]]", userId, templateCode);
    }
}
