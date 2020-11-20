package com.enuos.live.core;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.stereotype.Component;

/**
 * @author WangCaiWen Created on 2019/10/21 13:49
 */

@Component
public class NettyCtxGroup {

  public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

  public static ChannelGroup vipGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
}
