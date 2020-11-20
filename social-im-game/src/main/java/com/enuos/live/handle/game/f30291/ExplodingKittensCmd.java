package com.enuos.live.handle.game.f30291;

/**
 * TODO 炸弹猫指令.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V2.0.0
 * @since 2020/8/31 12:34
 */

@SuppressWarnings("WeakerAccess")
public class ExplodingKittensCmd {

  /**
   * 进入房间.
   */
  public static final short ENTER_ROOM = 0;
  /**
   * 玩家准备.
   */
  public static final short PLAYER_READY = 1;
  /**
   * 开始发牌.
   */
  public static final short START_LICENSING = 2;
  /**
   * 玩家出牌.
   */
  public static final short PLAYERS_PLAY_CARDS = 3;
  /**
   * 选择玩家.
   */
  public static final short CHOOSE_PLAYER = 4;
  /**
   * 送出手牌.
   */
  public static final short GIVE_PLAYER_CARD = 5;
  /**
   * 关闭加载.
   */
  public static final short LOADING = 5;
  /**
   * 玩家摸牌.
   */
  public static final short CHOOSE_TOUCH_CARD = 6;
  /**
   * 处理玩家.
   */
  public static final short HANDLE_PLAYER = 7;
  /**
   * 站起坐下.
   */
  public static final short JOIN_OR_WATCH = 8;
  /**
   * 观众信息.
   */
  public static final short AUDIENCE_INFO = 9;
  /**
   * 玩家离开.
   */
  public static final short PLAYER_LEAVES = 10;
  /**
   * 断线重连.
   */
  public static final short DISCONNECTED = 11;
  /**
   * 更新金币.
   */
  public static final short UPDATE_PLAYER_GOLD = 12;
  /**
   * 放置炸弹.
   */
  public static final short PLACE_EXPLODING = 13;
  /**
   * 玩家结束.
   */
  public static final short PLAYER_FINISH = 14;
  /**
   * 观战信息.
   */
  public static final short WATCH_INFO = 15;
  /**
   * 抽底返回.
   */
  public static final short BOTTOM_BACK = 16;
  /**
   * 游戏结束.
   */
  public static final short GAME_FINISH = 17;
  /**
   * 诅咒选择.
   */
  public static final short CURSE_TIME = 1001;
  /**
   * 祈求选择.
   */
  public static final short PRAY_TIME = 1002;
  /**
   * 束缚选择.
   */
  public static final short MANACLE_TIME = 1003;
  /**
   * 交换选择.
   */
  public static final short EXCHANGE_TIME = 1004;
  /**
   * 出局展示.
   */
  public static final short PLAYER_OUT_TIME = 1005;
  /**
   * 结束展示.
   */
  public static final short GAME_OVER_TIME = 1005;
}
