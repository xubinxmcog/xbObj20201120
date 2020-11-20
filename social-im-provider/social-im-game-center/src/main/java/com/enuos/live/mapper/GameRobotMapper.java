package com.enuos.live.mapper;

import java.util.List;
import java.util.Map;

/**
 * TODO 机器人信息访问层.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V2.0.0
 * @since 2020-09-11 10:32:29
 */

public interface GameRobotMapper {

  /**
   * TODO 获得机器人.
   *
   * @param number 机器人数
   * @return 机器人信息
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/11 10:39
   * @update 2020/9/11 10:39
   */
  List<Map<String, Object>> getRandomGameRobot(Integer number);

}