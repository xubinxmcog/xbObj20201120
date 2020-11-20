package com.enuos.live.handle.game.f30051;

import com.enuos.live.utils.StringUtils;
import com.google.common.collect.Maps;
import java.util.HashMap;

/**
 * TODO 静态参数.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/7/1 9:01
 */

public enum FindSpyAssets {

  /** 静态参数名称. */
  TEST_ID("测试ID"),
  PEOPLE_MIN_1("4-6最低人数"),
  PEOPLE_MIN_2("7-8最低人数"),
  PEOPLE_MAX_1("4-6最大人数"),
  PEOPLE_MAX_2("7-8最大人数");

  private String function;

  FindSpyAssets(String function) {
    this.function = function;
  }

  private static HashMap<FindSpyAssets, Object> STATIC_PARAM = Maps.newHashMap();
  static {
    STATIC_PARAM.put(TEST_ID, 2020L);
    STATIC_PARAM.put(PEOPLE_MIN_1, 3);
    STATIC_PARAM.put(PEOPLE_MIN_2, 7);
    STATIC_PARAM.put(PEOPLE_MAX_1, 6);
    STATIC_PARAM.put(PEOPLE_MAX_2, 8);
  }

  /**
   * TODO int静态参数.
   *
   * @param param [参数名称]
   * @return [静态参数]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/1 21:30
   * @update 2020/7/1 21:30
   */
  public static int getInt(FindSpyAssets param) {
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
   * @create 2020/7/1 21:30
   * @update 2020/7/1 21:30
   */
  public static long getLong(FindSpyAssets param) {
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
   * @create 2020/7/1 21:30
   * @update 2020/7/1 21:30
   */
  public static String getString(FindSpyAssets param) {
    if (StringUtils.isEmpty(param.function)) {
      return null;
    }
    Object object = STATIC_PARAM.get(param);
    return StringUtils.nvl(object);
  }
}
