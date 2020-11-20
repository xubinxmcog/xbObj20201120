package com.enuos.live.service;

import com.enuos.live.result.Result;
import java.util.Map;

/**
 * TODO 游玩时间接口
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020-11-05 15:16:14
 */

public interface GamePlayTimeService {

  /**
   * TODO 更新时间.
   *
   * @param params [userId, playTime]
   * @return [更新结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 15:28
   * @update 2020/11/5 15:28
   */
  Result update(Map<String, Object> params);

  /**
   * TODO 获得时间.
   *
   * @param userId [用户ID]
   * @return [查询结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 15:26
   * @update 2020/11/5 15:26
   */
  Result queryTimeByUserId(Long userId);

}