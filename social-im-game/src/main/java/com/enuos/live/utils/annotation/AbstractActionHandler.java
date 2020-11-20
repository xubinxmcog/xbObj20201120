package com.enuos.live.utils.annotation;

import com.enuos.live.task.GameEventLoop;
import com.enuos.live.codec.Packet;
import io.netty.channel.Channel;
import java.util.List;

/**
 * TODO 抽象处理.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.0.0
 * @since 2020/5/15 14:26
 */

public abstract class AbstractActionHandler extends GameEventLoop {

  /**
   * TODO 操作处理.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/15 21:09
   * @update 2020/8/13 20:33
   */
  abstract public void handle(Channel channel, Packet packet);

  /**
   * TODO 关闭处理.
   *
   * @param userId [用户ID]
   * @param attachId [附属ID(或房间Id)]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/19 21:09
   * @update 2020/8/13 20:34
   */
  abstract public void shutOff(Long userId, Long attachId);

  /**
   * TODO 清除处理.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/3 18:36
   * @update 2020/8/13 20:35
   */
  abstract public void cleaning(Long roomId);

  /**
   * TODO 陪玩处理.
   *
   * @param roomId [房间ID]
   * @param playerIds [机器人信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/21 10:39
   * @update 2020/8/21 10:39
   */
  abstract public void joinRobot(Long roomId, List<Long> playerIds);
}
