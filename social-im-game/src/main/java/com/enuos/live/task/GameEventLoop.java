package com.enuos.live.task;

import io.netty.channel.EventLoop;

/**
 * TODO 事件循环.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/15 16:45
 */

public class GameEventLoop {

  private EventLoop loop;

  public void register(EventLoop loop) {
    this.loop = loop;
  }

  public EventLoop getLoop() {
    return loop;
  }

  public void execute(Runnable runnable) {
    loop.execute(runnable);
  }
}
