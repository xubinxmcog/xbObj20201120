package com.enuos.live.handle.game.f30291;

import lombok.Data;

/**
 * TODO 炸弹猫牌组.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V2.0.0
 * @since 2020/8/31 8:42
 */

@Data
@SuppressWarnings("WeakerAccess")
public class ExplodingKittensCard {

  /**
   * 扑克ID.
   */
  private Integer cardId;
  /**
   * 扑克功能 1-炸弹 2-拆弹 3-先知 4-占星 5-洗牌 6-抽底 7-跳过 8-转向 9-诅咒 10诅咒*2 11-祈求 12-束缚 13-交换
   */
  private Integer cardFunction;

}
