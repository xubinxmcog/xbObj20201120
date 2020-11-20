package com.enuos.live.manager;

import com.enuos.live.codec.Packet;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * TODO 群组管理.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/20 12:37
 */

@Component
public class GroupManager {

  /** 房间管道组. */
  private static ConcurrentHashMap<Long, ChannelGroup> ROOM_GROUP = new ConcurrentHashMap<>();

  /** 房间计划任务. */
  private static Timer ROOM_TIMER = new HashedWheelTimer();

  /**
   * TODO 定时任务.
   *
   * @param task [执行任务]
   * @param delay [定时时间]
   * @param unit [时间类型]
   * @return [计划任务]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/20 12:37
   * @update 2020/5/20 12:37
   */
  public static Timeout newTimeOut(TimerTask task, long delay, TimeUnit unit) {
    return ROOM_TIMER.newTimeout(task, delay, unit);
  }

  /**
   * TODO 刷新群组.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/20 12:37
   * @update 2020/5/20 12:37
   */
  public static void refreshRoomData(Channel channel, Packet packet) {
    if (!ROOM_GROUP.containsKey(packet.roomId)) {
      // 房间组
      ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
      group.add(channel);
      ROOM_GROUP.putIfAbsent(packet.roomId, group);
    } else {
      // 房间组
      ChannelGroup group = getRoomGroup(packet.roomId);
      group.add(channel);
    }
  }

  /**
   * TODO 获得群组.
   *
   * @param roomId [房间ID]
   * @return [群组]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/20 12:37
   * @update 2020/5/20 12:37
   */
  private static ChannelGroup getRoomGroup(Long roomId) {
    return ROOM_GROUP.get(roomId);
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
    ChannelGroup group = getRoomGroup(roomId);
    if (Objects.nonNull(group)) {
      group.remove(channel);
    }
  }

  /**
   * TODO 删除房间数据.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/20 12:37
   * @update 2020/5/20 12:37
   */
  public static void delRoomGroup(Long roomId) {
    ROOM_GROUP.remove(roomId);
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
    ChannelGroup group = getRoomGroup(roomId);
    if (Objects.nonNull(group)) {
      group.writeAndFlush(packet);
    }
  }

}
