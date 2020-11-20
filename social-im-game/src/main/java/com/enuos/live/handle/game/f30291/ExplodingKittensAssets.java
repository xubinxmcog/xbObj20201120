package com.enuos.live.handle.game.f30291;

import com.enuos.live.utils.StringUtils;
import java.util.HashMap;

/**
 * TODO 静态参数.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V2.0.0
 * @since 2020/8/31 12:35
 */

public enum ExplodingKittensAssets {

  /**
   * 静态参数名称.
   */
  TEST_ID("测试ID"),
  USER_ID("用户ID长度"),
  PLAYER_NOT_READY("玩家未准备"),
  PLAYER_IS_GAME("玩家已准备"),
  PLAYER_IN_GAME("玩家游戏中"),
  PLAYER_IS_OUT("玩家已出局"),
  BOMB_DISPOSAL("拆弹卡牌"),
  BOMB_SEAT_NUM("炸弹位置数量"),
  ROOM_START_NUM("游戏开始人数"),
  RANKING_INDEX_NUM("排名数量标记"),
  CARD_FUNC_09("诅咒卡牌1"),
  CARD_FUNC_10("诅咒卡牌2"),
  CARD_FUNC_11("祈求卡牌"),
  CARD_FUNC_12("束缚卡牌"),
  CARD_FUNC_13("交换卡牌");
  private String function;

  ExplodingKittensAssets(String function) {
    this.function = function;
  }

  private static HashMap<ExplodingKittensAssets, Object> STATIC_PARAM = new HashMap<ExplodingKittensAssets, Object>() {
    private static final long serialVersionUID = 7390718632635025456L;
    {
      put(TEST_ID, 2020L);
      put(USER_ID, 9);
      put(PLAYER_NOT_READY, 0);
      put(PLAYER_IS_GAME, 1);
      put(PLAYER_IN_GAME, 2);
      put(PLAYER_IS_OUT, 3);
      put(BOMB_DISPOSAL, 2);
      put(BOMB_SEAT_NUM, 5);
      put(ROOM_START_NUM, 5);
      put(RANKING_INDEX_NUM, 4);
      put(CARD_FUNC_09, 9);
      put(CARD_FUNC_10, 10);
      put(CARD_FUNC_11, 11);
      put(CARD_FUNC_12, 12);
      put(CARD_FUNC_13, 13);
    }
  };

  /**
   * TODO 获得int静态参数.
   *
   * @param param 参数名称
   * @return int 静态参数
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 10:24
   * @update 2020/8/17 10:24
   */
  public static int getInt(ExplodingKittensAssets param) {
    if (StringUtils.isEmpty(param.function)) {
      return 0;
    }
    Object object = STATIC_PARAM.get(param);
    return (int) object;
  }

  /**
   * TODO 获得long静态参数.
   *
   * @param param 参数名称
   * @return long 静态参数
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 10:27
   * @update 2020/8/17 10:27
   */
  public static long getLong(ExplodingKittensAssets param) {
    if (StringUtils.isEmpty(param.function)) {
      return 0L;
    }
    Object object = STATIC_PARAM.get(param);
    return (long) object;
  }

  /**
   * TODO 获得String静态参数.
   *
   * @param param 参数名称
   * @return String 静态参数
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 10:27
   * @update 2020/8/17 10:27
   */
  @SuppressWarnings("unused")
  public static String getString(ExplodingKittensAssets param) {
    if (StringUtils.isEmpty(param.function)) {
      return null;
    }
    Object object = STATIC_PARAM.get(param);
    return StringUtils.nvl(object);
  }
}
