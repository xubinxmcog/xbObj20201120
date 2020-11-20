package com.enuos.live.handle.match;

/**
 * TODO 匹配指令.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/6/24 15:29
 */

@SuppressWarnings("WeakerAccess")
public class MatchCmd {

  /** 双人匹配 「普通游戏」.*/
  public static final short UNIVERSAL_MATCH = 0;
  /** 取消匹配 「全局通用」.*/
  public static final short CANCEL_MATCH = 1;
  /** 飞行棋. */
  public static final short AEROPLANE_MATCH = 2;
  /** 谁是卧底. */
  public static final short WHO_IS_SPY_MATCH = 3;
  /** 狼人杀. */
  public static final short WEREWOLF_MATCH = 4;
  /** 你画我猜/你说我猜. */
  public static final short GUESS_MATCH = 5;
  /** 快速开始. */
  public static final short QUICK_START = 6;
  /** 一起优诺/炸弹猫. */
  public static final short POKER_MATCH = 7;
  /** 七乐麻将. */
  public static final short SPARROW_MATCH = 8;
  /** 七乐斗地主. */
  public static final short LANDLORDS_MATCH = 9;
}
