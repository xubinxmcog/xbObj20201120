package com.enuos.live.manager;

import com.enuos.live.pojo.GameRobot;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * TODO 机器人管理.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.0.0
 * @since 2020/9/15 14:52
 */

@Component
public class RobotManager {

  /** 机器人信息. */
  private static ConcurrentHashMap<Long, GameRobot> GAME_ROBOT = new ConcurrentHashMap<>();

  /**
   * TODO 添加机器人.
   *
   * @param gameRobot [机器人信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/11 13:32
   * @update 2020/9/11 13:32
   */
  public static void addGameRobot(GameRobot gameRobot) {
    GAME_ROBOT.put(gameRobot.getRobotId(), gameRobot);
  }

  /**
   * TODO 删除机器人.
   *
   * @param robotId [机器人ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/11 13:32
   * @update 2020/9/11 13:32
   */
  public static void deleteGameRobot(Long robotId) {
    GAME_ROBOT.remove(robotId);
  }

  /**
   * TODO 机器人信息.
   *
   * @param robotId [机器人ID]
   * @return [机器人信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/11 13:36
   * @update 2020/9/11 13:36
   */
  public static GameRobot getGameRobot(Long robotId) {
    return GAME_ROBOT.get(robotId);
  }

}
