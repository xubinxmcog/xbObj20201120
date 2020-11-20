package com.enuos.live.manager;

import com.enuos.live.codec.Packet;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * TODO 聊天管理.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/20 11:24
 */

@Component
public class ChatManager {

  /** 房间聊天组. */
  private static ConcurrentHashMap<Long, ChannelGroup> CHAT_GROUP = new ConcurrentHashMap<>();

  /**
   * TODO 获得群组.
   *
   * @param roomId [房间ID]
   * @return [群组]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/20 11:24
   * @update 2020/5/20 11:24
   */
  private static ChannelGroup getChatGroup(Long roomId) {
    return CHAT_GROUP.get(roomId);
  }

  /**
   * TODO 刷新群组.
   *
   * @param roomId [房间ID]
   * @param channel [通讯管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/20 11:24
   * @update 2020/5/20 11:24
   */
  public static void refreshChatGroup(Long roomId, Channel channel) {
    if (!CHAT_GROUP.containsKey(roomId)) {
      // 聊天组
      ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
      group.add(channel);
      CHAT_GROUP.putIfAbsent(roomId, group);
    } else {
      // 聊天组
      ChannelGroup group = getChatGroup(roomId);
      group.add(channel);
    }
  }

  /**
   * TODO 删除房间数据.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/20 11:24
   * @update 2020/5/20 11:24
   */
  public static void delChatGroup(Long roomId) {
    CHAT_GROUP.remove(roomId);
  }

  /**
   * TODO 移除玩家.
   *
   * @param roomId [房间ID]
   * @param channel [通信管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/20 12:37
   * @update 2020/5/20 12:37
   */
  public static void removeChannel(Long roomId, Channel channel) {
    ChannelGroup group = getChatGroup(roomId);
    if (Objects.nonNull(group)) {
      group.remove(channel);
    }
  }

  /**
   * TODO 移除玩家.
   *
   * @param roomId [房间ID]
   * @param channel [通信管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/20 12:37
   * @update 2020/5/20 12:37
   */
  public static void removeChatChannel(Long roomId, Channel channel) {
    ChannelGroup group = getChatGroup(roomId);
    if (Objects.nonNull(group)) {
      group.remove(channel);
    }
  }

  /**
   * TODO 发送消息.
   *
   * @param packet [数据包]
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/20 12:37
   * @update 2020/5/20 12:37
   */
  public static void sendPacketToGroup(Packet packet, Long roomId) {
    ChannelGroup group = getChatGroup(roomId);
    if (group != null) {
      group.writeAndFlush(packet);
    }
  }
}
