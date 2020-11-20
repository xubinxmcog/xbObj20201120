package com.enuos.live.handle.game.f30251;

/**
 * TODO 优诺指令.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V2.0.0
 * @since 2020/8/13 11:09
 */

@SuppressWarnings("WeakerAccess")
public class UnoCmd {

  /**
   * 进入房间.
   */
  public static final short ENTER_ROOM = 0;
  /**
   * 开始准备.
   */
  public static final short START_READY = 1;
  /**
   * 开始游戏.
   */
  public static final short START_GAME = 2;
  /**
   * 玩家出牌.
   */
  public static final short PLAY_CARDS = 3;
  /**
   * 选择颜色.
   */
  public static final short SELECT_COLOR = 4;
  /**
   * 玩家摸牌.
   */
  public static final short TOUCH_CARDS = 5;
  /**
   * 关闭加载.
   */
  public static final short LOADING = 5;
  /**
   * 质疑玩家.
   */
  public static final short QUESTION_CARDS = 6;
  /**
   * 游戏结算.
   */
  public static final short GAME_SETTLE = 7;
  /**
   * 玩家退出.
   */
  public static final short PLAYER_EXIT = 8;
  /**
   * 观战信息.
   */
  public static final short WATCH_INFO = 9;
  /**
   * 玩家操作.
   */
  public static final short JOIN_LEAVE = 10;
  /**
   * 玩家处理.
   */
  public static final short PLAYER_HANDLE = 11;
  /**
   * 断线重连.
   */
  public static final short DISCONNECTED = 12;
  /**
   * 质疑选色.
   */
  public static final short QUESTION_COLOR = 13;
  /**
   * 跳过出牌.
   */
  public static final short SKIP_CARD = 14;
  /**
   * 玩家金币.
   */
  public static final short PLAYER_GOLD = 15;
  /**
   * 游玩数据.
   */
  public static final short PLAY_INFO = 16;
  /**
   * 发牌数据.
   */
  public static final short DEAL_INFO = 17;
  /**
   * 观战断线.
   */
  public static final short DISCONNECTED_WATCH = 18;
}
