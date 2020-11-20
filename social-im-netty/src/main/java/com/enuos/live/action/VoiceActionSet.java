package com.enuos.live.action;

/**
 * @author WangCaiWen Created on 2020/4/9 11:10
 */
public class VoiceActionSet {

  /**
   * 开播
   */
  public static final short START_BROADCAST = 0;

  /**
   * 观众进入房间
   */
  public static final short ENTER_ROOM = 1;

  /**
   * 发送消息
   */
  public static final short SEND_MESSAGE = 2;

  /**
   * 接收消息
   */
  public static final short RECEIVE_MESSAGE = 3;

  /**
   * 房间信息改变
   */
  public static final short UPDATE_ROOM = 4;

  /**
   * 座位信息改变
   */
  public static final short UP_ROOM_SEAT_LIST = 5;

  /**
   * 正在说话状态
   */
  public static final short SPEAK_STATUS = 6;

  /**
   * 主播下播
   */
  public static final short END_BROADCAST = 7;

  /**
   * 观众退出房间
   */
  public static final short EXIT_ROOM = 8;

  /**
   * 排麦列表改变
   */
  public static final short UP_MIC_LIST = 9;
}
