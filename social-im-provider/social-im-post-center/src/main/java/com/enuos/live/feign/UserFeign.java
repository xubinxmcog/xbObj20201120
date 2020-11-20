package com.enuos.live.feign;

import com.enuos.live.feign.impl.UserFeignFallback;
import com.enuos.live.task.Task;
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
 * @Date 13:18 2020/4/30
 * @Modified
 */
@Component
@FeignClient(name = "SOCIAL-IM-USER", fallback = UserFeignFallback.class)
public interface UserFeign {

    /**
     * @Description: 获取用户
     * @Param: [userId, keys]
     * @Return: java.util.Map<java.lang.String   ,   java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @GetMapping(value = "/common/getUserBase")
    Map<String, Object> getUserBase(@RequestParam("userId") Long userId, @RequestParam("keys") String... keys);

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
     * @Description: 匹配敏感词，全匹配
     * @Param: [words]
     * @Return: java.lang.Boolean
     * @Author: wangyingjie
     * @Date: 2020/7/24
     */
    @GetMapping(value = "/word/matchWords")
    Boolean matchWords(@RequestParam("words") String words);

    /** 
     * @Description: 日常任务处理
     * @Param: [userId, value, template] 
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/10/29 
     */
    @PostMapping("/handler/dailyTask")
    void handlerOfDailyTask(@RequestBody Task task);

}
