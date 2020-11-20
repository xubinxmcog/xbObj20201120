package com.enuos.live.service;

import com.enuos.live.result.Result;

/**
 * TODO 机器人信息接口.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V2.0.0
 * @since 2020-09-11 10:32:31
 */

public interface GameRobotService {

  /**
   * TODO 获得机器人信息.
   *
   * @param number 机器数量
   * @return 机器人信息
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/11 10:39
   * @update 2020/9/11 10:39
   */
  Result getRandomGameRobot(Integer number);

}