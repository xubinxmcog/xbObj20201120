package com.enuos.live.handle.achieve;

/**
 * TODO 成就工厂.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/11/4 15:45
 */

@SuppressWarnings("WeakerAccess")
public class AchieveFactory {

  public static Achieve getAchieve(String achievePath) {
    Achieve achieve = null;
    try {
      achieve = (Achieve) Class.forName(achievePath).newInstance();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return achieve;
  }

}
