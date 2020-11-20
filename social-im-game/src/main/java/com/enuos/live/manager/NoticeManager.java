package com.enuos.live.manager;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.stereotype.Component;

/**
 * TODO 群组通道.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/15 17:42
 */

@Component
public class NoticeManager {

  public static ChannelGroup GAME_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

  public static ChannelGroup SOFT_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
}
