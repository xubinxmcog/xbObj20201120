package com.enuos.live.handle.game.f30051.dddd;

/**
 * TODO 谁是卧底指令.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/11/5 17:17
 */

@SuppressWarnings("WeakerAccess")
public class FindUndercoverCmd {
  /** 房间数据. */
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
  /** 开始描述. */
  public static final short START_WORDS = 6;
  /** 描述信息. */
  public static final short WORDS_DESCRIPTION = 7;
  /** 玩家说话. */
  public static final short VOICE_DESCRIPTION = 8;
  /** 开始投票. */
  public static final short PLAYER_START_VOTE = 9;
  /** 玩家投票. */
  public static final short PLAYER_VOTE = 10;
  /** 平票决斗. */
  public static final short VOTE_BATTLE = 11;
  /** 投票结果. */
  public static final short VOTE_RESULTS = 12;
  /** 是否爆词. */
  public static final short OPEN_WORDS = 13;
  /** 描述爆词. */
  public static final short SPY_OPEN_WORDS = 14;
  /** 游戏结算. */
  public static final short GAME_SETTLE = 15;
  /** 玩家退出. */
  public static final short PLAYER_EXIT = 16;
  /** 断线重连. */
  public static final short DISCONNECTED = 17;
  /** 观战信息. */
  public static final short WATCH_INFO = 18;
  /** 入座站起. */
  public static final short JOIN_LEAVE = 19;
  /** 玩家处理. */
  public static final short PLAYER_HANDLE = 20;
  /** 选择座位. */
  public static final short SELECT_SEAT = 21;
  /** 玩家聊天. */
  public static final short PLAYER_CHAT = 22;
  /** 结束说话. */
  public static final short FINISH_SPEAK = 23;

  /** 开始游戏 3s. */
  public static final short START_GAME_TIME = 100;
  /** 玩家平票 20s. */
  public static final short PLAYER_BATTLE_TIME = 200;
  /** 选择爆词 5s. */
  public static final short SELECT_EXPOSURE_TIME = 300;

  /** 出局展示 2s. */
  public static final short OUT_SHOW_TIME = 1000;
  /** 投票展示 2s. */
  public static final short VOTE_SHOW_TIME = 2000;
  /** 回合展示 2s. */
  public static final short ROUND_SHOW_TIME = 2000;
  /** 词汇展示 5s. */
  public static final short WORD_SHOW_TIME = 3000;
  /** 平票展示 2s. */
  public static final short BATTLE_SHOW_TIME = 4000;

}
