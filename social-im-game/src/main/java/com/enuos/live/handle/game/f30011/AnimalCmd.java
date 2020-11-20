package com.enuos.live.handle.game.f30011;

/**
 * TODO 斗兽棋指令.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/20 12:59
 */

@SuppressWarnings("WeakerAccess")
public class AnimalCmd {

  /** 进入房间. */
  public static final short ENTER_ROOM = 0;
  /** 开始游戏. */
  public static final short START_GAME = 1;
  /** 玩家动作. */
  public static final short PLAYER_MOVES = 2;
  /** 翻转位置. */
  public static final short FLIP_POSITION = 3;
  /** 游戏结算. */
  public static final short SETTLEMENT = 4;
  /** 玩家认输. */
  public static final short CONFESS = 5;
  /** 关闭加载. */
  public static final short CLOSE_LOADING = 5;
  /** 断线重连. */
  public static final short DISCONNECTED = 6;
  /** 关闭游戏. */
  public static final short CLOSE_GAME = 7;
  /** 操作定时. */
  public static final short TASK_TIME = 8;

  /** 等待玩家. */
  public static final short WAIT_PLAYER = 100;
}
