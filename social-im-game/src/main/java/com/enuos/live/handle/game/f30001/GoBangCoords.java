package com.enuos.live.handle.game.f30001;

import lombok.Data;

/**
 * TODO 五子棋坐标.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/16 11:37
 */

@Data
@SuppressWarnings("WeakerAccess")
public class GoBangCoords {

  /** x坐标. */
  private int positionX;
  /** y坐标. */
  private int positionY;
  /** 颜色 [1-黑 2-白]. */
  private int color = 0;
  /** 权值. */
  private int weight = 0;

  GoBangCoords(int positionX, int positionY) {
    this.positionX = positionX;
    this.positionY = positionY;
  }

}
