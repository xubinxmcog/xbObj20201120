package com.enuos.live.assets;

import com.google.common.collect.Maps;
import java.util.HashMap;

/**
 * TODO 游戏邀请.
 *
 * @author WangCaiWen - 1443710411@qq.com
 * @version 1.0
 * @since 2020/7/27 11:12
 */

public class GameInviteAssets {

  private static HashMap<Long, String> GAME_INVITE = Maps.newHashMap();
  static {
    GAME_INVITE.put(30041L, "你画我猜");
    GAME_INVITE.put(30051L, "谁是卧底");
    GAME_INVITE.put(30061L, "你说我猜");
    GAME_INVITE.put(30081L, "狼人杀");
    GAME_INVITE.put(30251L, "UNO");
    GAME_INVITE.put(30291L, "炸弹猫");
  }

  /**
   * TODO 错误邀请.
   *
   * @param inviteId 邀请ID
   * @return 错误信息
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  public static String getErrorInvite(Long inviteId) {
    String quest = null;
    if (GAME_INVITE.containsKey(inviteId)) {
      quest = GAME_INVITE.get(inviteId);
    }
    return quest;
  }
}
