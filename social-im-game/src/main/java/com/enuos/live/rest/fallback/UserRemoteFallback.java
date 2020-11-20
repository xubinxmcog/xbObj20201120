package com.enuos.live.rest.fallback;

import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * TODO 熔断处理.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/5/21 13:37
 */

@Component
public class UserRemoteFallback implements UserRemote {

  /**
   * TODO 新增经验&金币.
   *
   * @param params [userId、experience、gold]
   * @return [添加结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/8 21:08
   * @update 2020/7/9 21:15
   */
  @Override
  public Result gameHandler(Map<String, Object> params) {
    return null;
  }

  /**
   * TODO 任务处理.
   *
   * @param params [userId、code、progress、isReset]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/29 16:07
   * @update 2020/7/29 16:07
   */
  @Override
  public void taskHandler(Map<String, Object> params) {

  }

  /**
   * TODO 成就处理.
   *
   * @param params [userId、code、progress、isReset]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/29 16:07
   * @update 2020/7/29 16:07
   */
  @Override
  public void achievementHandlers(Map<String, Object> params) {

  }

  /**
   * TODO 装饰信息.
   *
   * @param userId [用户ID]
   * @return [装饰信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/30 21:28
   * @update 2020/7/30 21:28
   */
  @Override
  public Map<String, Object> getUserFrame(Long userId) {
    return null;
  }

  /**
   * TODO 钻石金币.
   *
   * @param userId [用户ID]
   * @return [钻石金币]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/17 18:05
   * @update 2020/8/17 18:05
   */
  @Override
  public Result getCurrency(Long userId) {
    return null;
  }

  /**
   * TODO 用户信息.
   *
   * @param userId [用户ID]
   * @return [用户信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/14 12:33
   * @update 2020/8/14 12:33
   */
  @Override
  public Result getBase(Long userId) {
    return null;
  }
}
