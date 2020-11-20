package com.enuos.live.handle.game.f30021;

import lombok.Data;

/**
 * TODO 飞机实体.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/9/24 21:00
 */

@Data
@SuppressWarnings("WeakerAccess")
public class AirplaneEntity {
  /** 飞机ID. */
  private Integer airplaneId;
  /** 当前位置. */
  private Integer positional = 0;
  /** 完成飞行 [0-飞行 1-结束]. */
  private Integer isFinish = 0;

  /**
   * TODO 初始飞机.
   *
   * @param airplaneId [飞机ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/24 21:04
   * @update 2020/9/24 21:04
   */
  AirplaneEntity(Integer airplaneId) {
    this.airplaneId = airplaneId;
  }
}
