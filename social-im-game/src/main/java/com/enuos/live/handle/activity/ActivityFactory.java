package com.enuos.live.handle.activity;

/**
 * TODO 活动工厂.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/11/4 13:50
 */

@SuppressWarnings("WeakerAccess")
public class ActivityFactory {

  public static Activity getActivity(ActivityEnum activityEnum) {
    Activity activity = null;
    try {
      activity = (Activity) Class.forName(activityEnum.getPath()).newInstance();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return activity;
  }

}
