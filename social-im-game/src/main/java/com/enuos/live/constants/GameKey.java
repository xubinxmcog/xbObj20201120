package com.enuos.live.constants;

/**
 * TODO 游戏缓存Key.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version V2.2.0
 * @since 2020/9/10 10:27
 */

public enum GameKey {

  /** 玩家信息. */
  KEY_GAME_USER_LOGIN("KEY_GAME_USER_LOGIN:", "玩家信息"),
  /** 测试玩家. */
  KEY_GAME_TEST_LOGIN("KEY_GAME_TEST_LOGIN:", "测试玩家"),
  /** 玩家匹配. */
  KEY_GAME_USER_MATCH("KEY_GAME_USER_MATCH:", "玩家匹配"),
  /** 加入记录. */
  KEY_GAME_JOIN_RECORD("KEY_GAME_JOIN_RECORD:", "加入记录"),
  /** 房间记录. */
  KEY_GAME_ROOM_RECORD("KEY_GAME_ROOM_RECORD:", "房间记录");

  private String name;
  private String desc;

  GameKey(String name, String desc) {
    this.name = name;
    this.desc = desc;
  }

  public String getName() {
    return name;
  }

  public String getDesc() {
    return desc;
  }
}
