package com.enuos.live.rest;

import com.enuos.live.rest.fallback.UserRemoteFallback;
import com.enuos.live.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * TODO 用户中心.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/5/21 13:36
 */

@Component
@FeignClient(contextId = "userTelnet", name = "SOCIAL-IM-USER", fallback = UserRemoteFallback.class)
public interface UserRemote {

  /**
   * TODO 新增经验&金币.
   *
   * @param params [userId、experience、gold]
   * @return [添加结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/8 21:08
   * @update 2020/7/9 21:15
   */
  @RequestMapping(value = "/exp/game/handler", method = RequestMethod.POST)
  Result gameHandler(@RequestBody Map<String, Object> params);

  /**
   * TODO 任务处理.
   *
   * @param params [userId、code、progress、isReset]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/29 16:07
   * @update 2020/7/29 16:07
   */
  @PostMapping(value = "/task/handler")
  void taskHandler(@RequestBody Map<String, Object> params);

  /**
   * TODO 成就处理.
   *
   * @param params [userId、code、progress、isReset]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/29 16:07
   * @update 2020/7/29 16:07
   */
  @PostMapping(value = "/achievement/handler")
  void achievementHandlers(@RequestBody Map<String, Object> params);

  /**
   * TODO 装饰信息.
   *
   * @param userId [用户ID]
   * @return [装饰信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/30 21:28
   * @update 2020/7/30 21:28
   */
  @GetMapping(value = "/user/open/getUserFrame")
  Map<String, Object> getUserFrame(@RequestParam("userId") Long userId);

  /**
   * TODO 钻石金币.
   *
   * @param userId [用户ID]
   * @return [钻石金币]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/17 18:05
   * @update 2020/8/17 18:05
   */
  @GetMapping(value = "/user/open/getCurrencyForServer")
  Result getCurrency(@RequestParam("userId") Long userId);

  /**
   * TODO 用户信息.
   *
   * @param userId [用户ID]
   * @return [用户信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/14 12:33
   * @update 2020/8/14 12:33
   */
  @GetMapping(value = "/user/open/getBaseForServer")
  Result getBase(@RequestParam("userId") Long userId);
}
