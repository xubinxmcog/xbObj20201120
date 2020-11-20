package com.enuos.live.handle.game.f30021;

import java.util.List;
import lombok.Data;

/**
 * TODO 飞行过程.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/10/14 10:16
 */

@Data
@SuppressWarnings("WeakerAccess")
public class FlightProcess {
  /** 房间ID. */
  private Long roomId;
  /** 玩家ID. */
  private Long playerId;
  /** 飞机ID. */
  private Integer airplaneId;
  /** 初始位置. */
  private Integer initValue;
  /** 结束位置. */
  private Integer endValue;
  /** 终点区最大值. */
  private Integer maxValue;
  /** 骰子值. */
  private Integer diceValue;
  /** 显示效果[0.正常 1.跳越 2.飞行]. */
  private Integer isFlyAction;
  /** 前进步骤. */
  private List<Integer> walkingList;
}
