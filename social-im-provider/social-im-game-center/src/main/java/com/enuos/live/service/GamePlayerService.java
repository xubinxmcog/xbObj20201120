package com.enuos.live.service;

import com.enuos.live.result.Result;
import java.util.Map;

/**
 * TODO 玩家服务接口.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V1.0.0
 * @since 2020-05-19 10:37
 */

public interface GamePlayerService {

  /**
   * TODO 加入房间.
   *
   * @param params [userId,gameId,roomId]
   * @return 加入结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/21 21:34
   * @update 2020/8/21 12:39
   */
  Result enterRoom(Map<String, Object> params);

  /**
   * TODO 离开房间.
   *
   * @param userId 用户ID
   * @return 离开结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/21 21:34
   * @update 2020/8/21 12:37
   */
  Result leaveRoom(Long userId);

  /**
   * TODO 删除房间.
   *
   * @param roomId 房间ID
   * @return 删除结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/21 21:34
   * @update 2020/8/21 12:36
   */
  Result deleteRoom(Long roomId);

}