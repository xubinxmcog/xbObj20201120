package com.enuos.live.handle.game.f30061;

/**
 * TODO 你说我猜指令.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/8/4 10:18
 */

@SuppressWarnings("WeakerAccess")
public class GuessedSaidCmd {
  /** 进入房间. */
  public static final short ENTER_ROOM = 0;
  /** 玩家准备. */
  public static final short PLAYER_READY = 1;
  /** 轮次开始. */
  public static final short ROUND_START = 2;
  /** 玩家选词. */
  public static final short SELECT_WORD = 3;
  /** 开始说话. */
  public static final short START_SPEAK = 4;
  /** 停止说话. */
  public static final short STOP_SPEAK = 5;
  /** 关闭加载. */
  public static final short LOADING = 5;
  /** 玩家描述. */
  public static final short PLAYER_SPEAK = 6;
  /** 玩家信息. */
  public static final short PLAYER_INFO = 7;
  /** 玩家点赞. */
  public static final short PLAYER_LIKES = 8;
  /** 游戏结算. */
  public static final short GAME_SETTLE = 9;
  /** 观战信息. */
  public static final short WATCH_INFO = 10;
  /** 玩家操作. */
  public static final short JOIN_LEAVE = 11;
  /** 加入观战. */
  public static final short CLEAR_PLAYER = 12;
  /** 断线重连. */
  public static final short DISCONNECTED = 13;
  /** 换词申请. */
  public static final short CHANGE_WORDS = 14;
  /** 描述信息. */
  public static final short DEPICT_INFO = 15;
  /** 提示信息. */
  public static final short HINT_WORDS = 16;
  /** 提示时间. */
  public static final short HINT_TIME = 17;
  /** 玩家退出. */
  public static final short PLAYER_EXIT = 18;
  /** 玩家选择. */
  public static final short PLAYER_SELECT = 19;
  /** 玩家聊天. */
  public static final short PLAYER_CHAT = 20;

  /** 开始时间. */
  public static final short START_GAME = 100;
  /** 特殊时间. */
  public static final short SPECIAL_TIME = 200;
  /** 展示时间. */
  public static final short SHOW_TIME = 300;
}
