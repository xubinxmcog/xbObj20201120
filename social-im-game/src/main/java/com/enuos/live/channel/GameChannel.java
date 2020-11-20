package com.enuos.live.channel;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * TODO 游戏通讯管道.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/20 21:07
 */

@Component
public class GameChannel {

  /** 玩家管道. */
  private static ConcurrentHashMap<Long, Channel> GAME_CHANNEL = new ConcurrentHashMap<>();
  /** 玩家ID. */
  private static AttributeKey<Long> USER_ID = AttributeKey.newInstance("USER_ID");

  /**
   * TODO 添加管道.
   *
   * @param userId [玩家ID]
   * @param channel [玩家管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/20 21:07
   * @update 2020/5/20 21:07
   */
  public static void addChannel(Long userId, Channel channel) {
    channel.attr(USER_ID).set(userId);
    GAME_CHANNEL.put(userId, channel);
  }

  /**
   * TODO 清除管道.
   *
   * @param channel [当前管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 15:20
   * @update 2020/9/15 15:20
   */
  public static void clearChannel(Channel channel) {
    long userId = channel.attr(USER_ID).get();
    if (userId > 0) {
      GAME_CHANNEL.remove(userId);
    }
    channel.attr(USER_ID).set(0L);
  }

  /**
   * TODO 清除管道.
   *
   * @param channel [旧管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 15:20
   * @update 2020/9/15 15:20
   */
  public static void clearOldChannel(Channel channel) {
    long userId = channel.attr(USER_ID).get();
    if (userId > 0) {
      GAME_CHANNEL.remove(userId);
    }
    channel.attr(USER_ID).set(0L);
  }

  /**
   * TODO 获得管道.
   *
   * @param userId [玩家ID]
   * @return [玩家管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/27 6:31
   * @update 2020/5/27 6:31
   */
  public static Channel getChannel(Long userId) {
    return GAME_CHANNEL.get(userId);
  }

  /**
   * TODO 获得玩家ID.
   *
   * @param channel [玩家管道]
   * @return [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/20 21:07
   * @update 2020/9/17 21:07
   */
  public static long getUserId(Channel channel) {
    return channel.attr(USER_ID).get() != null ? channel.attr(USER_ID).get() : 0L;
  }

  /**
   * TODO 在线玩家.
   *
   * @return [玩家数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/9 18:11
   * @update 2020/10/9 18:11
   */
  public static int onlinePlayers() {
    return GAME_CHANNEL.size();
  }

}
