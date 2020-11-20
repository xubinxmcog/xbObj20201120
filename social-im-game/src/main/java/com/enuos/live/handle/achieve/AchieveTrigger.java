package com.enuos.live.handle.achieve;

/**
 * TODO 成就触发.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/11/4 15:24
 */

public class AchieveTrigger {

  private static final String ACHIEVE_PATH = "com.enuos.live.handle.achieve.Achievement";

  /**
   * TODO 完成成就.
   *
   * @param code [操作CODE]
   * @param params [玩家Id, 触发CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:09
   * @update 2020/11/4 16:09
   */
  public static void finish(Integer code, Object...params) {
    Achieve achieve = AchieveFactory.getAchieve(ACHIEVE_PATH);
    switch (code) {
      case 1:
        achieve.everyDay(params);
        break;
      case 2:
        achieve.complete(params);
        break;
      default:
        achieve.grandTotal(params);
        break;
    }
  }
}
