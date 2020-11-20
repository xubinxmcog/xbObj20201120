package com.enuos.live.handle.game.f30031;

import lombok.Data;

/**
 * TODO 扫雷坐标.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/6/1 15:51
 */

@Data
@SuppressWarnings("WeakerAccess")
public class MinesweeperCoords {

  /** x坐标. */
  private int positionOne;
  /** y坐标. */
  private int positionTwo;
  /** 坐标值. */
  private int codeValue = 0;
  /** 是否炸弹 [0-否 1-是]. */
  private int isBomb = 0;
  /** 状态 [1-关闭 2-翻开]. */
  private int status = 1;
  /** 标记 [1-选择 2-标雷]. */
  private int index = 0;

  MinesweeperCoords(int positionOne, int positionTwo) {
    this.positionOne = positionOne;
    this.positionTwo = positionTwo;
  }
}
