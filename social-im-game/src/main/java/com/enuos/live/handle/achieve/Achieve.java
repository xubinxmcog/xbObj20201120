package com.enuos.live.handle.achieve;

/**
 * TODO 成就接口.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/11/4 14:47
 */
public interface Achieve {

  /**
   * TODO 每日任务.
   *
   * @param params [玩家ID, 游戏ID, 触发CODE, 累积数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:23
   * @update 2020/11/4 16:23
   */
  void everyDay(Object... params);

  /**
   * TODO 一次成就.
   *
   * @param params [玩家ID, 游戏ID, 触发CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:25
   * @update 2020/11/4 16:25
   */
  void complete(Object... params);

  /**
   * TODO 累计任务.
   *
   * @param params [玩家ID, 游戏ID, 触发CODE, 累积数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:24
   * @update 2020/11/4 16:24
   */
  void grandTotal(Object... params);

}
