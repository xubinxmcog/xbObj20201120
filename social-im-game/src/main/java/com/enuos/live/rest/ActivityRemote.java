package com.enuos.live.rest;

import com.enuos.live.rest.fallback.ActivityRemoteFallback;
import com.enuos.live.result.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * TODO 活动中心.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/10/10 12:47
 */

@Component
@FeignClient(contextId = "activityTelnet", name = "SOCIAL-IM-ACTIVITY", fallback = ActivityRemoteFallback.class)
public interface ActivityRemote {

  /**
   * TODO [丹枫迎秋].活动处理.
   *
   * @param params [userId,userId,userId]
   * @return [处理结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/28 15:50
   * @update 2020/9/28 15:50
   */
  @PostMapping(value = "/qiuri/open/handler")
  Result openHandler(@RequestBody Map<String, Object> params);
}
