package com.enuos.live.handle.activity;

import java.util.Objects;

/**
 * TODO 活动触发.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/11/4 13:53
 */

public class ActivityTrigger {

  /**
   * TODO 完成活动.
   *
   * @param code [活动CODE]
   * @param params [玩家Id, 触发CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:09
   * @update 2020/11/4 16:09
   */
  public static void finish(Integer code, Object... params) {
    ActivityEnum activityEnum = getActivityEnum(code);
    if (Objects.nonNull(activityEnum)) {
      Activity activity = ActivityFactory.getActivity(activityEnum);
      activity.playGame(params);
    }
  }

  /**
   * TODO 获取活动
   *
   * @param code [活动CODE]
   * @return [活动位置]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:22
   * @update 2020/11/4 16:22
   */
  private static ActivityEnum getActivityEnum(Integer code) {
    ActivityEnum activityEnum = null;
    switch (code) {
      // 丹枫迎秋
      case 1:
        activityEnum = ActivityEnum.MidAutumn2020;
        break;
      case 2:
        break;
      default:
        break;
    }
    return activityEnum;
  }

}
