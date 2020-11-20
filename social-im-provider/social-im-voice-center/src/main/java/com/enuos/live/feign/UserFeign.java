package com.enuos.live.feign;

import com.enuos.live.result.Result;
import com.enuos.live.task.Task;
import com.enuos.live.task.TemplateEnum;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @Description 用户
 * @Author wangyingjie
 * @Date 2020/5/12
 * @Modified
 */
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
     * 获取用户
     * @param userId
     * @param keys
     * @return
     */
    @GetMapping(value = "/common/getUserBase")
    Map<String, Object> getUserBase(@RequestParam("userId") Long userId, @RequestParam("keys") String...keys);

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
     * @Description: 计算经验值
     * @Param: [level, bindingResult]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/5/21
     */
    @ApiOperation(value = "计算经验值", notes = "计算经验值")
    @PostMapping("/exp/countExp")
    Result countExp(@RequestBody Map<String, Object> params);

    /**
     * @Description: 日常任务处理
     * @Param: [userId, value, template]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/10/29
     */
    @GetMapping("/handler/dailyTask")
    void handlerOfDailyTask(@RequestParam Long userId, @RequestParam Integer value, @RequestParam TemplateEnum template);

    @PostMapping("/handler/dailyTask")
    void dailyTask(@Validated @RequestBody Task task);

    /**
     * @Description: 奖励
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/15
     */
    @PostMapping(value = "/reward/handler")
    Result rewardHandler(@RequestBody Map<String, Object> params);

}
