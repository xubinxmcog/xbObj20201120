package com.enuos.live.action;

/**
 * TODO 聊天指令.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/4/9 11:10
 */

public class ChatActionSet {
  /** 进入聊天. */
  public static final short ENTER_CHAT_ROOM = 0;
  /** 发送消息. */
  public static final short SEND_MESSAGE = 1;
  /** 接收消息. */
  public static final short RECEIVE_MESSAGE = 2;
  /** 游戏邀请. */
  public static final short SEND_GAME_INVITE = 3;
  /** 聆听语音  */
  public static final short LISTEN_VOICE = 4;
  /** 接受邀请. */
  public static final short ACCEPT_INVITE = 5;
  /** 接收数据. */
  public static final short RECEIVE_GAME_DATA = 6;
  /** 离开聊天. */
  public static final short LEAVE_CHAT_ROOM = 7;
  /** 取消约战. */
  public static final short CANCEL_INVITE = 8;
  /** 点击进入. */
  public static final short CLICK_ENTER = 9;
  /** 群聊解散. */
  public static final short DISSOLVE_CHAT = 10;
}
