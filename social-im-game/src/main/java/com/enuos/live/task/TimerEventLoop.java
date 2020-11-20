package com.enuos.live.task;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.stereotype.Component;

/**
 * TODO 游戏定时.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version V1.0.0
 * @since 2020/5/16 11:12
 */

@Component
public class TimerEventLoop {

  /**
   * 设置定时线程组.
   */
  public static EventLoopGroup timeGroup = new NioEventLoopGroup(4);
}
