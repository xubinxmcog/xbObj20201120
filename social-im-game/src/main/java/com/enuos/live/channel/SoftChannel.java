package com.enuos.live.channel;

import com.enuos.live.codec.Packet;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * TODO 软件通讯通道.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/19 16:28
 */

@Component
public class SoftChannel {

  /** 软件管道. */
  private static ConcurrentHashMap<Long, Channel> SOFT_CHANNEL = new ConcurrentHashMap<>();
  /** 软件ID(=用户ID). */
  private static AttributeKey<Long> SOFT_ID = AttributeKey.newInstance("SOFT_ID");

  /**
   * TODO 添加管道.
   *
   * @param userId [用户ID]
   * @param channel [用户管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/19 21:09
   * @update 2020/5/19 21:09
   */
  public static void addChannel(Long userId, Channel channel) {
    channel.attr(SOFT_ID).set(userId);
    SOFT_CHANNEL.put(userId, channel);
  }

  /**
   * TODO 清除管道.
   *
   * @param channel [当前管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/19 21:09
   * @update 2020/5/20 21:07
   */
  public static void clearChannel(Channel channel) {
    long userId = channel.attr(SOFT_ID).get();
    if (userId > 0) {
      SOFT_CHANNEL.remove(userId);
    }
    channel.attr(SOFT_ID).set(0L);
  }

  /**
   * TODO 清除管道.
   *
   * @param channel [旧管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 15:20
   * @update 2020/9/17 21:07
   */
  public static void clearOldChannel(Channel channel) {
    long userId = channel.attr(SOFT_ID).get();
    if (userId > 0) {
      SOFT_CHANNEL.remove(userId);
    }
    channel.attr(SOFT_ID).set(0L);
  }

  /**
   * TODO 获得管道.
   *
   * @param userId [用户ID]
   * @return [用户管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/19 21:09
   * @update 2020/9/17 21:07
   */
  public static Channel getChannel(Long userId) {
    return SOFT_CHANNEL.get(userId);
  }
  
  /**
   * TODO 获得用户ID.
   *
   * @param channel [用户管道]
   * @return [用户ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/19 21:09
   * @update 2020/9/17 21:07
   */
  public static long getUserId(Channel channel) {
    return channel.attr(SOFT_ID).get() != null ? channel.attr(SOFT_ID).get() : 0L;
  }

  /**
   * TODO 发送数据.
   *
   * @param packet [数据包]
   * @param userId [用户ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/21 21:34
   * @update 2020/7/8 21:08
   */
  public static void sendPacketToUserId(Packet packet, Long userId) {
    Channel channel = getChannel(userId);
    if (Objects.nonNull(channel)) {
      if (channel.isActive()) {
        channel.writeAndFlush(packet);
      }
    }
  }

  /**
   * TODO 在线用户.
   *
   * @return [用户数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/9 18:11
   * @update 2020/10/9 18:11
   */
  public static int onlinePlayers() {
    return SOFT_CHANNEL.size();
  }
}
