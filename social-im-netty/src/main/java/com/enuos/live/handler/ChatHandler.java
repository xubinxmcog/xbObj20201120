package com.enuos.live.handler;

import com.enuos.live.core.Packet;
import com.enuos.live.result.Result;
import io.netty.channel.Channel;
import java.util.Map;

/**
 * TODO 聊天接口.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020-04-09 11:04:12
 */
public interface ChatHandler {

  /**
   * TODO 进入聊天.
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  void enterChatRoom(Channel channel, Packet packet);

  /**
   * TODO 发送信息.
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  void sendMessage(Channel channel, Packet packet);

  /**
   * TODO 游戏邀请.
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  void sendGameInvite(Channel channel, Packet packet);

  /**
   * TODO 聆听语音.
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  void listenVoice(Channel channel, Packet packet);

  /**
   * TODO 接受邀请.
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  void acceptInvite(Channel channel, Packet packet);

  /**
   * TODO 离开聊天.
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  void leaveChatRoom(Channel channel, Packet packet);

  /**
   * TODO 发送邀请.
   *
   * @param params 邀请信息
   * @return 发送结果
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  Result sendInvite(Map<String, Object> params);

  /**
   * TODO 点击进入.
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  void clickToEnter(Channel channel, Packet packet);
}
