package com.enuos.live.handle.game.f30021;

/**
 * TODO 飞空战棋指令.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.0.0
 * @since 2020/9/25 15:02
 */

@SuppressWarnings("WeakerAccess")
public class AirplaneCmd {
  /** 进入房间. */
  public static final short ENTER_ROOM = 0;
  /** 开始游戏. */
  public static final short START_GAME = 1;
  /** 开始掷骰. */
  public static final short START_DICE = 2;
  /** 移动位置. */
  public static final short MOVES_CHESS = 3;
  /** 游戏结束. */
  public static final short GAME_FINISH = 4;
  /** 玩家认输. */
  public static final short ADMITS_DEFEAT = 5;
  /** 关闭加载. */
  public static final short CLOSE_LOADING = 5;
  /** 断线重连. */
  public static final short DISCONNECTED = 6;
  /** 玩家退出. */
  public static final short PLAYER_EXIT = 7;

  /** 等待定时. */
  public static final short WAIT_TIME = 100;
  /** 自动定时. */
  public static final short AUTO_TIME = 200;
  /** 机器定时. */
  public static final short ROBOT_TIME = 300;
  /** 操作定时. */
  public static final short ACTION_TIME = 400;
}
