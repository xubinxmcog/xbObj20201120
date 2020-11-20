package com.enuos.live.handle.game.f30001;

/**
 * TODO 五子棋指令.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/19 21:09
 */

@SuppressWarnings("WeakerAccess")
public class GoBangCmd {
  /** 进入房间. */
  public static final short ENTER_ROOM = 0;
  /** 开始游戏. */
  public static final short START_GAME = 1;
  /** 玩家动作. */
  public static final short PLAYER_PLACEMENT = 2;
  /** 游戏结算. */
  public static final short GAME_SETTLEMENT = 3;
  /** 悔棋申请. */
  public static final short REGRET_CHESS = 4;
  /** 悔棋确认. */
  public static final short CONFIRM_APPLICATION = 5;
  /** 关闭加载. */
  public static final short CLOSE_LOADING = 5;
  /** 玩家退出. */
  public static final short PLAYER_EXIT = 6;
  /** 玩家认输. */
  public static final short CONFESS = 7;
  /** 断线重连. */
  public static final short DISCONNECTED = 8;
  /** 关闭游戏. */
  public static final short CLOSE_GAME = 9;

  /** 等待玩家. */
  public static final short WAIT_PLAYER = 100;
}
