package com.enuos.live.handle.game.f30071;

/**
 * TODO 一站到底指令.
 *
 * @author WangCaiWen
 * @since 2020/5/20 13:00
 */

@SuppressWarnings("WeakerAccess")
public class MustStandCmd {

  /**
   * 进入房间.
   */
  public static final short ENTER_ROOM = 0;
  /**
   * 开始游戏.
   */
  public static final short START_GAME = 1;
  /**
   * 双倍得分.
   */
  public static final short DOUBLE_SCORE = 2;
  /**
   * 展示题目.
   */
  public static final short SHOW_PROBLEM = 3;
  /**
   * 展示选项.
   */
  public static final short SHOW_ANSWERS = 4;
  /**
   * 选择答案.
   */
  public static final short SELECT_ANSWER = 5;
  /**
   * 关闭加载.
   */
  public static final short CLOSE_LOADING = 5;
  /**
   * 回合结束.
   */
  public static final short ROUND_FINISH = 6;
  /**
   * 游戏结束.
   */
  public static final short GAME_FINISH = 7;
  /**
   * 断线重连.
   */
  public static final short DISCONNECTED = 8;
  /**
   * 题目报错.
   */
  public static final short SUBMIT_PROBLEM = 9;
  /**
   * 关闭游戏.
   */
  public static final short LEAVE_ROOM = 10;
  /**
   * 等待玩家.
   */
  public static final short WAIT_PLAYER = 100;
}
