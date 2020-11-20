package com.enuos.live.manager;

import com.enuos.live.core.Packet;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * TODO 通道管理.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/3/20 - 2020/7/21
 */

@Slf4j
@Component
public class ChannelManager {

  private static Map<Long, Channel> CHANNEL = new ConcurrentHashMap<>();
  private static Map<Long, ChannelGroup> CHAT_GROUP = new ConcurrentHashMap<>();
  private static Map<Long, ChannelGroup> VOICE_GROUP = new ConcurrentHashMap<>();
  private static AttributeKey<Long> USER_ID = AttributeKey.newInstance("USER_ID");

  /**
   * TODO 用户通道.
   *
   * @param userId 用户ID
   * @author WangCaiWen
   * @since 2020/7/21 - 2020/7/21
   */
  public static Channel getChannel(Long userId) {
    return CHANNEL.get(userId);
  }

  /**
   * TODO 添加通道.
   *
   * @param userId 用户ID
   * @param channel 用户通道
   * @author WangCaiWen
   * @since 2020/7/21 - 2020/7/21
   */
  public static void addChannel(Long userId, Channel channel) {
    channel.attr(USER_ID).set(userId);
    CHANNEL.put(userId, channel);
  }

  /**
   * TODO 移除通道.
   *
   * @param channel 通道
   * @return 通道
   * @author WangCaiWen
   * @since 2020/7/21 - 2020/7/21
   */
  public static Long removeChannelScheme2(Channel channel) {
    Long userId = channel.attr(USER_ID).get();
    log.warn("用户 " + userId + " 与服务断开连接");
    if (userId != null && userId > 0) {
      CHANNEL.remove(userId);
    }
    channel.attr(USER_ID).set(null);
    return userId;
  }

  /**
   * TODO 删除管道.
   *
   * @param channel 用户管道
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/6 17:38
   * @since 2020/8/6 17:38
   */
  public static void removeChannel(Channel channel) {
    log.info("移除管道 -> 管道: {}", channel);
    Long userId = channel.attr(USER_ID).get();
    log.info("重置用户绑定：[{}]", userId);
    if (userId != null && userId > 0) {
      CHANNEL.remove(userId);
    }
    channel.attr(USER_ID).set(null);
  }

  /**
   * 获取与channel 关联的用户ID
   *
   * @param channel channel
   * @return userId
   */
  public static Long getUserId(Channel channel) {
    return channel.attr(USER_ID).get();
  }

  /**
   * TODO 聊天群组.
   *
   * @param groupId [群聊ID]
   * @return [群组通道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 15:30
   * @update 2020/11/11 15:30
   */
  private static ChannelGroup getChatGroup(Long groupId) {
    return CHAT_GROUP.get(groupId);
  }

  /**
   * TODO 移除管道.
   *
   * @param groupId [群聊ID]
   * @param channel [通信管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 15:31
   * @update 2020/11/11 15:31
   */
  public static void removeChatChannel(Long groupId, Channel channel) {
    ChannelGroup group = getChatGroup(groupId);
    if (Objects.nonNull(group)) {
      group.remove(channel);
    }
  }

  /**
   * TODO 发送消息.
   *
   * @param packet [数据包]
   * @param groupId [群聊ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 15:32
   * @update 2020/11/11 15:32
   */
  public static void sendPacketToChatGroup(Packet packet, Long groupId) {
    ChannelGroup group = getChatGroup(groupId);
    if (group != null) {
      group.writeAndFlush(packet);
    }
  }

  /**
   * TODO 刷新群组.
   *
   * @param groupId [群聊ID]
   * @param channel [通讯管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/20 11:24
   * @update 2020/5/20 11:24
   */
  public static void refreshChatGroup(Long groupId, Channel channel) {
    if (!CHAT_GROUP.containsKey(groupId)) {
      // 聊天组
      ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
      group.add(channel);
      CHAT_GROUP.putIfAbsent(groupId, group);
    } else {
      // 聊天组
      ChannelGroup group = getChatGroup(groupId);
      group.add(channel);
    }
  }

  /**
   * 获得群组
   *
   * @param roomId roomId
   * @return ChannelGroup
   */
  public static ChannelGroup getVoiceGroup(Long roomId) {
    return VOICE_GROUP.get(roomId);
  }

  /**
   * 获得群组长度
   *
   * @param roomId roomId
   * @return ChannelGroup
   */
  public static int getVoiceGroupSize(Long roomId) {
    ChannelGroup voiceGroup = getVoiceGroup(roomId);
    if (null != voiceGroup) {
      return voiceGroup.size();
    }
    return 0;
  }

  /**
   * 创建群组
   *
   * @param roomId groupId
   * @param channelGroup 通道群组
   */
  private static void createVoiceGroup(Long roomId, ChannelGroup channelGroup) {
    VOICE_GROUP.putIfAbsent(roomId, channelGroup);
  }

  /**
   * 更新群组
   *
   * @param roomId roomId
   * @param channel channel
   */
  public static void addVoiceGroup(Long roomId, Channel channel) {
    if (!VOICE_GROUP.containsKey(roomId)) {
      ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
      group.add(channel);
      createVoiceGroup(roomId, group);
    } else {
      ChannelGroup group = getVoiceGroup(roomId);
      group.add(channel);
    }
  }

  /**
   * 移除群组
   *
   * @param roomId roomId
   */
  public static void removeVoiceGroup(Long roomId) {
    VOICE_GROUP.remove(roomId);
  }

  /**
   * 移除群组中的channel
   *
   * @param roomId roomId
   * @param channel channel
   */
  public static void removeVoiceChannel(Long roomId, Channel channel) {
    ChannelGroup group = getVoiceGroup(roomId);
    if (null != group && group.size() > 0) {
      log.info("移除房间{}的channel={}", roomId, channel);
      group.remove(channel);
    }
  }

  /**
   * 发送消息·首先获得channel,根据通道活跃状态发送
   *
   * @param packet packet
   * @param userId userId
   */
  public static void sendPacketToUserId(Packet packet, Long userId) {
    Channel channel = getChannel(userId);
    if (channel != null) {
      if (channel.isActive()) {
        channel.writeAndFlush(packet);
      }
    }
  }

  /**
   * TODO 发送消息.
   *
   * @param packet 消息包
   * @param roomId 房间ID
   * @author WangCaiWen
   * @date 2020/8/3
   */
  public static void sendPacketToVoiceGroup(Packet packet, Long roomId) {
    ChannelGroup group = getVoiceGroup(roomId);
    if (group != null) {
      group.writeAndFlush(packet);
    }
  }

  /**
   * 发送消息·首先获得ChannelGroup
   *
   * @param packet1 packet
   * @param packet2 packet
   * @param roomId roomId
   */
  public static void sendPacketToVoiceGroup(Packet packet1, Packet packet2, Long roomId, Channel channel) {
    // 获得群组
    ChannelGroup group = getVoiceGroup(roomId);
    if (group != null) {
      group.forEach(ch -> {
        if (channel != ch) {
          ch.writeAndFlush(packet1); // 发送群内所有人, 除自己
        } else {
          ch.writeAndFlush(packet2);// 发送个人
        }
      });
    }
  }

  public static void remove(Long roomId) {
    VOICE_GROUP.remove(roomId);
  }
}
