package com.enuos.live.handle.game.f30011;

import lombok.Data;

/**
 * TODO 斗兽棋坐标.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/21 19:58
 */

@Data
@SuppressWarnings("WeakerAccess")
public class AnimalCoords {

  /** x坐标. */
  private int positionX;
  /** y坐标. */
  private int positionY;
  /** 棋颜色 [1-红 2-蓝]. */
  private int color;
  /** 动物角色. */
  private int roles;
  /** 动物ID. */
  private int animal;
  /** 状态 [0-空 1-关闭 2-翻开]. */
  private int status = 0;

  AnimalCoords(int positionX, int positionY) {
    this.positionX = positionX;
    this.positionY = positionY;
  }

}
