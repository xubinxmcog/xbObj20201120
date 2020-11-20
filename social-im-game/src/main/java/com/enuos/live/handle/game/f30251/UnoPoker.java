package com.enuos.live.handle.game.f30251;

import lombok.Data;

/**
 * TODO 优诺牌组.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version 2.0
 * @since 2020/8/10 4:44
 */

@Data
@SuppressWarnings("WeakerAccess")
public class UnoPoker {

  /**
   * 扑克ID.
   */
  private Integer pokerId;
  /**
   * 扑克颜色 无-0 红-1 黄-2 蓝-3 绿-4.
   */
  private Integer pokerColors;
  /**
   * 扑克功能 0-数字 1-禁止 2-倒转 3-加2 4-改变颜色 5-质疑.
   */
  private Integer pokerFunction;

}
