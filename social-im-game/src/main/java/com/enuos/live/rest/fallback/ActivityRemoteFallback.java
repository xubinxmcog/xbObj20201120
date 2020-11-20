package com.enuos.live.rest.fallback;

import com.enuos.live.rest.ActivityRemote;
import com.enuos.live.result.Result;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * TODO 熔断处理.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/10/10 12:48
 */

@Component
public class ActivityRemoteFallback implements ActivityRemote {

  /**
   * TODO [丹枫迎秋].活动处理.
   *
   * @param params [userId,userId,userId]
   * @return [处理结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/28 15:50
   * @update 2020/9/28 15:50
   */
  @Override
  public Result openHandler(Map<String, Object> params) {
    return null;
  }
}
