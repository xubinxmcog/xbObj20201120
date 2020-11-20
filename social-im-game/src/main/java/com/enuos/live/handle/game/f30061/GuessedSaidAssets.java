package com.enuos.live.handle.game.f30061;

import com.enuos.live.utils.StringUtils;
import com.google.common.collect.Maps;
import java.util.HashMap;

/**
 * TODO 静态参数.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/8/4 10:19
 */

public enum GuessedSaidAssets {

  /** 静态参数名称. */
  TEST_ID("测试ID"),
  MIN_PEOPLE("最低开始人数"),
  MAX_CANCEL("最大取消次数"),
  SPECIAL_TIME("回合时间变化"),
  SENSITIVE_WORD("敏感词长度"),
  USER_ID_LENGTH("用户ID长度"),
  TIME_CHECK("断线时间校验点");

  private String function;

  GuessedSaidAssets(String function) {
    this.function = function;
  }

  private static HashMap<GuessedSaidAssets, Object> STATIC_PARAM = Maps.newHashMap();
  static {
    STATIC_PARAM.put(TEST_ID, 2020L);
    STATIC_PARAM.put(MIN_PEOPLE, 4);
    STATIC_PARAM.put(MAX_CANCEL, 3);
    STATIC_PARAM.put(SPECIAL_TIME, 10);
    STATIC_PARAM.put(SENSITIVE_WORD, 10);
    STATIC_PARAM.put(USER_ID_LENGTH, 9);
    STATIC_PARAM.put(TIME_CHECK, 85);
  }

  /**
   * TODO int静态参数.
   *
   * @param param [参数名称]
   * @return [静态参数]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 10:43
   * @update 2020/8/4 10:43
   */
  public static int getInt(GuessedSaidAssets param) {
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
   * @create 2020/8/4 10:43
   * @update 2020/8/4 10:43
   */
  public static long getLong(GuessedSaidAssets param) {
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
   * @create 2020/8/4 10:43
   * @update 2020/8/4 10:43
   */
  public static String getString(GuessedSaidAssets param) {
    if (StringUtils.isEmpty(param.function)) {
      return null;
    }
    Object object = STATIC_PARAM.get(param);
    return StringUtils.nvl(object);
  }
}
