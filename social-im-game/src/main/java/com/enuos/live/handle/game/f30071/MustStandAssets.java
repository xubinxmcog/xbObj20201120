package com.enuos.live.handle.game.f30071;

import com.enuos.live.utils.StringUtils;
import java.util.HashMap;

/**
 * TODO 静态参数.
 *
 * @author WangCaiWen - missiw@163.com
 * @since 2020/7/8 14:34
 */

public enum MustStandAssets {

  /**
   * 静态参数名称.
   */
  TEST_ID("测试ID"),
  FINISH_TIME("结算时间(s)"),
  NUMBER_PEOPLE("房间人数"),
  TOTAL_ROUND("总回合数");

  private String function;

  MustStandAssets(String function) {
    this.function = function;
  }

  private static HashMap<MustStandAssets, Object> STATIC = new HashMap<MustStandAssets, Object>() {
    private static final long serialVersionUID = -8711266132463028832L;

    {
      put(TEST_ID, 2020L);
      put(FINISH_TIME, 20);
      put(NUMBER_PEOPLE, 2);
      put(TOTAL_ROUND, 10);
    }
  };

  /**
   * TODO 获得int静态参数.
   *
   * @param param 参数名称
   * @return 静态参数
   * @author WangCaiWen
   * @since 2020/7/8 - 2020/7/8
   */
  @SuppressWarnings("unused")
  public static int getInt(MustStandAssets param) {
    if (StringUtils.isEmpty(param.function)) {
      return 0;
    }
    Object object = STATIC.get(param);
    return (int) object;
  }

  /**
   * TODO 获得long静态参数.
   *
   * @param param 参数名称
   * @return 静态参数
   * @author WangCaiWen
   * @since 2020/7/8 - 2020/7/8
   */
  @SuppressWarnings("unused")
  public static long getLong(MustStandAssets param) {
    if (StringUtils.isEmpty(param.function)) {
      return 0L;
    }
    Object object = STATIC.get(param);
    return (long) object;
  }

  /**
   * TODO 获得String静态参数.
   *
   * @param param 参数名称
   * @return 静态参数
   * @author WangCaiWen
   * @since 2020/7/8 - 2020/7/8
   */
  @SuppressWarnings("unused")
  public static String getString(MustStandAssets param) {
    if (StringUtils.isEmpty(param.function)) {
      return null;
    }
    Object object = STATIC.get(param);
    return StringUtils.nvl(object);
  }
}
