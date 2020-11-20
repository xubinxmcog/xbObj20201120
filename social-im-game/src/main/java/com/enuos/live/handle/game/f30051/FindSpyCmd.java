package com.enuos.live.handle.game.f30051;

/**
 * TODO 谁是卧底指令.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/7/1 21:30
 */

@SuppressWarnings("WeakerAccess")
public class FindSpyCmd {
  /** 进入房间. */
  public static final short ENTER_ROOM = 0;
  /** 玩家准备. */
  public static final short PLAYER_READY = 1;
  /** 系统信息. */
  public static final short SYSTEM_INFO = 2;
  /** 轮次开始. */
  public static final short ROUND_START = 2;
  /** 推送词汇. */
  public static final short PUSH_WORDS = 3;
  /** 换词申请. */
  public static final short CHANGE_WORDS = 4;
  /** 换词操作. */
  public static final short CHANGE_ACTION = 5;
  /** 关闭加载. */
  public static final short LOADING = 5;
  /** 开始描述. */
  public static final short START_WORDS = 6;
  /** 描述信息. */
  public static final short SPEAK_WORDS = 7;
  /** 开始说话. */
  public static final short START_SPEAK = 8;
  /** 开始投票. */
  public static final short START_VOTE = 9;
  /** 玩家投票. */
  public static final short PLAYERS_VOTE = 10;
  /** 平票PK. */
  public static final short VOTE_PK = 11;
  /** 投票结果. */
  public static final short VOTE_RESULTS = 12;
  /** 是否爆词. */
  public static final short OPEN_WORDS = 13;
  /** 描述爆词. */
  public static final short SPEAK_OPEN_WORDS = 14;
  /** 游戏结算. */
  public static final short GAME_SETTLE = 15;
  /** 玩家退出. */
  public static final short PLAYER_EXIT = 16;
  /** 断线重连. */
  public static final short DISCONNECTED = 17;
  /** 观战信息. */
  public static final short WATCH_INFO = 18;
  /** 玩家操作. */
  public static final short JOIN_LEAVE = 19;
  /** 加入观战. */
  public static final short JOIN_WATCH = 20;
  /** 玩家选择. */
  public static final short PLAYER_SELECT = 21;
  /** 玩家聊天. */
  public static final short PLAYER_CHAT = 22;
  /** 结束说话. */
  public static final short FINISH_TALKING = 23;

  /** 准备检测.  */
  public static final short INIT_CHECK = 100;
  /** 开始检测. */
  public static final short START_CHECK = 200;
  /** 玩家检测. */
  public static final short PLAYER_CHECK = 300;
  /** 选择检测. */
  public static final short SELECT_CHECK = 400;
  /** 平票检测. */
  public static final short DRAW_CHECK = 500;
  /** 击杀展示. */
  public static final short KILL_INDEX = 700;
  /** 描述展示. */
  public static final short WORDS_INDEX = 701;
}
