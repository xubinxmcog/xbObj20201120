package com.enuos.live.handle.game.f30021;

import com.enuos.live.utils.StringUtils;
import com.google.common.collect.Maps;
import java.util.HashMap;

/**
 * TODO 静态参数.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/9/25 16:27
 */

public enum AirplaneAssets {

  /** 静态参数名称. */
  PLAYER_TIMEOUT("等待玩家(s)"),
  START_TIMEOUT("开始游戏(s)"),
  ACTION_TIMEOUT("操作定时(s)"),
  SHOW_TIMEOUT("展示定时(s)"),
  SETTLEMENT_TIME("结算时间(s)"),
  CHECKERBOARD("棋盘标记"),
  RED_PLAYER("红色标记"),
  YELLOW_PLAYER("黄色标记"),
  BLUE_PLAYER("蓝色标记"),
  GREEN_PLAYER("绿色标记"),
  TEST_ID("测试ID"),
  NUMBER_PEOPLE_0("房间人数"),
  NUMBER_PEOPLE_1("房间人数");

  private String function;

  AirplaneAssets(String function) {
    this.function = function;
  }

  private static HashMap<AirplaneAssets, Object> STATIC_PARAM = Maps.newHashMap();
  static {
    STATIC_PARAM.put(PLAYER_TIMEOUT, 30L);
    STATIC_PARAM.put(START_TIMEOUT, 1L);
    STATIC_PARAM.put(ACTION_TIMEOUT, 20L);
    STATIC_PARAM.put(SHOW_TIMEOUT, 20);
    STATIC_PARAM.put(SETTLEMENT_TIME, 20);
    STATIC_PARAM.put(CHECKERBOARD, 52);
    STATIC_PARAM.put(RED_PLAYER, 52);
    STATIC_PARAM.put(YELLOW_PLAYER, 13);
    STATIC_PARAM.put(BLUE_PLAYER, 26);
    STATIC_PARAM.put(GREEN_PLAYER, 39);
    STATIC_PARAM.put(TEST_ID, 2020L);
    STATIC_PARAM.put(NUMBER_PEOPLE_0, 2);
    STATIC_PARAM.put(NUMBER_PEOPLE_1, 4);
  }

  /**
   * TODO int静态参数.
   *
   * @param param [参数名称]
   * @return [静态参数]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:29
   * @update 2020/9/25 16:29
   */
  public static int getInt(AirplaneAssets param) {
    if (StringUtils.isEmpty(param.function)) {
      return 0;
    }
    Object object = STATIC_PARAM.get(param);
    return (int) object;
  }

  /**
   * TODO long静态参数.
   *
   * @param param [参数名称]
   * @return [静态参数]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:29
   * @update 2020/9/25 16:29
   */
  public static long getLong(AirplaneAssets param) {
    if (StringUtils.isEmpty(param.function)) {
      return 0L;
    }
    Object object = STATIC_PARAM.get(param);
    return (long) object;
  }

  /**
   * TODO String静态参数.
   *
   * @param param [参数名称]
   * @return [静态参数]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:29
   * @update 2020/9/25 16:29
   */
  public static String getString(AirplaneAssets param) {
    if (StringUtils.isEmpty(param.function)) {
      return null;
    }
    Object object = STATIC_PARAM.get(param);
    return StringUtils.nvl(object);
  }

}
