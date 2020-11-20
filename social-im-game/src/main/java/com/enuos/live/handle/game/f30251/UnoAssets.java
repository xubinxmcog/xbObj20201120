package com.enuos.live.handle.game.f30251;

import com.enuos.live.utils.StringUtils;
import java.util.HashMap;

/**
 * TODO 静态参数.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V2.0.0
 * @since 2020/8/13 11:09
 */

public enum UnoAssets {

  /**
   * 静态参数名称.
   */
  TEST_ID("测试ID"),
  USER_ID("用户ID长度"),
  ROOM_NUM("开始人数");

  private String function;

  UnoAssets(String function) {
    this.function = function;
  }

  private static HashMap<UnoAssets, Object> STATIC_PARAM = new HashMap<UnoAssets, Object>() {
    private static final long serialVersionUID = 2367599987508822773L;
    {
      put(TEST_ID, 2020L);
      put(USER_ID, 9);
      put(ROOM_NUM, 4);
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
  public static int getInt(UnoAssets param) {
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
  public static long getLong(UnoAssets param) {
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
  public static String getString(UnoAssets param) {
    if (StringUtils.isEmpty(param.function)) {
      return null;
    }
    Object object = STATIC_PARAM.get(param);
    return StringUtils.nvl(object);
  }

  /**
   * 扑克功能.
   */
  private static HashMap<Integer, Integer> funcMap = new HashMap<Integer, Integer>() {
    private static final long serialVersionUID = 1133894571753028204L;
    {
      put(0, 0);
      put(1, 0);
      put(2, 0);
      put(3, 0);
      put(4, 0);
      put(5, 0);
      put(6, 0);
      put(7, 0);
      put(8, 0);
      put(9, 0);
      put(10, 1);
      put(11, 2);
      put(12, 3);
      put(13, 4);
      put(14, 5);
    }
  };

  /**
   * TODO 扑克功能.
   *
   * @param pokerId 扑克ID
   * @return int 扑克功能
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 14:05
   * @update 2020/8/17 14:05
   */
  public static int getPokerFunc(Integer pokerId) {
    return funcMap.get(pokerId);
  }
}
