package com.enuos.live.handle.speak;

/**
 * TODO 交流指令.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.0.0
 * @since 2020/6/24 16:08
 */

@SuppressWarnings("WeakerAccess")
public class ConverseCmd {

  /** 文字交流. */
  public static final short CONVERSE_TEXT = 0;
  /** 语音交流. */
  public static final short CONVERSE_VOICE = 1;
  /** 谁是卧底&你画我猜&你说我猜. */
  public static final short CONVERSE_ROOM = 2;
  /** 游戏邀请. */
  public static final short GAME_INVITE = 3;
  /** 扬声器状态. */
  public static final short CONVERSE_LOUDSPEAKER = 4;

}
