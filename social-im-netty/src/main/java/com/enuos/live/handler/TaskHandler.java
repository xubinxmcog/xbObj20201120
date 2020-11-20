package com.enuos.live.handler;

/**
 * TODO 每日任务处理.
 *
 * @author WangCaiWen - 1443710411@qq.com
 * @version 1.0
 * @since 2020/7/27 14:06
 */

public interface TaskHandler {

  /**
   * TODO 好友PK.
   *
   * @param userId 用户ID
   * @param gameId 游戏ID
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  void handle(Long userId, Long gameId);

}
