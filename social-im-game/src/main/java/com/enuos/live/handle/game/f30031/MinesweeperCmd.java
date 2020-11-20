package com.enuos.live.handle.game.f30031;

/**
 * TODO 扫雷指令.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.0.0
 * @since 2020/5/20 12:03
 */

@SuppressWarnings("WeakerAccess")
public class MinesweeperCmd {
  /** 进入房间. */
  public static final short ENTER_ROOM = 0;
  /** 开始游戏. */
  public static final short START_GAME = 1;
  /** 玩家动作. */
  public static final short PLAYER_MOVES = 2;
  /** 回合结束. */
  public static final short END_OF_ROUND = 3;
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

  /** 等待玩家. */
  public static final short WAIT_PLAYER = 100;
  /** 机器人. */
  public static final short ROBOT_TIME = 200;
}
