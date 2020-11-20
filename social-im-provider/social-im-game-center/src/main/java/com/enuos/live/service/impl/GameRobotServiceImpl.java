package com.enuos.live.service.impl;

import com.enuos.live.mapper.GameRobotMapper;
import com.enuos.live.result.Result;
import com.enuos.live.service.GameRobotService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * TODO 机器人信息实现.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V2.0.0
 * @since 2020-09-11 10:32:31
 */

@Service("gameRobotService")
public class GameRobotServiceImpl implements GameRobotService {

  @Resource
  private GameRobotMapper gameRobotMapper;

  /**
   * TODO 获得机器人信息.
   *
   * @return 机器人信息
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/11 10:39
   * @update 2020/9/11 10:39
   */
  @Override
  public Result getRandomGameRobot(Integer number) {
    return Result.success(this.gameRobotMapper.getRandomGameRobot(number));
  }

}