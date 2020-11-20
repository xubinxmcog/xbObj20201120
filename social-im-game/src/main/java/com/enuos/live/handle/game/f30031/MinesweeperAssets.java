package com.enuos.live.handle.game.f30031;

import com.enuos.live.utils.StringUtils;
import com.google.common.collect.Maps;
import java.util.HashMap;

/**
 * TODO 静态参数.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/6/24 10:06
 */

public enum MinesweeperAssets {

  /** 静态参数名称. */
  START_TIMEOUT("开始游戏(s)"),
  ACTION_TIMEOUT("操作定时(s)"),
  SHOW_TIMEOUT("展示定时(s)"),
  SETTLEMENT_TIME("结算时间(s)"),
  NUMBER_PEOPLE("房间人数"),
  FLAG_NUM("标记值"),
  TEST_ID("测试ID");

  private String function;

  MinesweeperAssets(String function) {
    this.function = function;
  }

  private static HashMap<MinesweeperAssets, Object> STATIC_PARAM = Maps.newHashMap();
  static {
    STATIC_PARAM.put(START_TIMEOUT, 1L);
    STATIC_PARAM.put(ACTION_TIMEOUT, 24L);
    STATIC_PARAM.put(SHOW_TIMEOUT, 25);
    STATIC_PARAM.put(SETTLEMENT_TIME, 20);
    STATIC_PARAM.put(NUMBER_PEOPLE, 2);
    STATIC_PARAM.put(FLAG_NUM, 5);
    STATIC_PARAM.put(TEST_ID, 2020L);
  }

  /**
   * TODO int静态参数.
   *
   * @param param [参数名称]
   * @return [静态参数]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/24 18:06
   * @update 2020/6/24 18:06
   */
  public static int getInt(MinesweeperAssets param) {
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
   * @create 2020/6/24 18:06
   * @update 2020/6/24 18:06
   */
  public static long getLong(MinesweeperAssets param) {
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
   * @create 2020/6/24 18:06
   * @update 2020/6/24 18:06
   */
  public static String getString(MinesweeperAssets param) {
    if (StringUtils.isEmpty(param.function)) {
      return null;
    }
    Object object = STATIC_PARAM.get(param);
    return StringUtils.nvl(object);
  }
}
