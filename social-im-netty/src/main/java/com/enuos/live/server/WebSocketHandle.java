package com.enuos.live.server;

import com.enuos.live.action.ChannelSet;
import com.enuos.live.core.NettyCtxGroup;
import com.enuos.live.core.Packet;
import com.enuos.live.manager.ChannelManager;
import com.enuos.live.manager.HandlerManager;
import com.enuos.live.utils.ExceptionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * TODO SOCKET处理.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/3/17 - 2020/7/28
 */

@Slf4j
@Configuration
public class WebSocketHandle extends SimpleChannelInboundHandler<Packet> {

  private final HandlerManager handlerManager;

  public WebSocketHandle(HandlerManager handlerManager) {
    super();
    this.handlerManager = handlerManager;
  }

  private static EventLoopGroup workGroup = new NioEventLoopGroup(4);

  /**
   * Handler活跃状态，表示连接成功
   *
   * @param ctx 通道
   * @throws Exception 异常
   */
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);
    log.warn("WebSocketHandle.channelActive [新设备连接]", ctx.channel());
    NettyCtxGroup.group.add(ctx.channel());
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
    workGroup.next().execute(() -> handler(ctx.channel(), packet));
  }

  /**
   * 处理请求分发
   *
   * @param channel 通道
   * @param packet 数据包
   */
  private void handler(Channel channel, Packet packet) {
    try {
      switch (packet.channel) {
        // 心跳
        case ChannelSet.CMD_HEART:
          handlerManager.doHeart(channel, packet);
          break;
        // 单聊&群聊
        case ChannelSet.CMD_CHAT:
          handlerManager.doChat(channel, packet);
          break;
        // 语音房
        case ChannelSet.CMD_VOICE:
          handlerManager.doVoice(channel, packet);
          break;
        // 语音房操作
        case ChannelSet.CMD_VOICE_HANDLE:
          handlerManager.handleVoice(channel, packet);
          break;
        // 宠物
        case ChannelSet.CMD_PETS:
          handlerManager.doPets(channel, packet);
          break;
        default:
          log.warn("暂不支持的服务指令{}, handler", packet.channel);
          break;
      }
    } catch (Exception e) {
      log.error("WebSocketHandle.handler [receive userId: {}, chanel: {}, child: {}, error: {}]",
          packet.userId, packet.channel, packet.child, e.getMessage());
      log.error(ExceptionUtil.getStackTrace(e));
    }
    if (packet.channel != ChannelSet.CMD_HEART) {
      log.info("WebSocketHandle.handler [receive userId: {}, chanel: {}, child:{}]", packet.userId,
          packet.channel, packet.child);
    }
  }

  /**
   * 未注册状态
   *
   * @param ctx 通道
   * @throws Exception 异常
   */
  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    super.channelUnregistered(ctx);
    log.warn("WebSocketHandle.channelUnregistered [等待设备连接]");
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    super.handlerRemoved(ctx);
    String asShortText = ctx.channel().id().asShortText();
    log.warn("WebSocketHandle.handlerRemoved [移除设备: {}]", asShortText);
    // 移除通道数据
    NettyCtxGroup.group.remove(ctx.channel());
    NettyCtxGroup.vipGroup.remove(ctx.channel());
  }

  /**
   * 非活跃状态，没有连接远程主机的时候。
   *
   * @param ctx 通道
   * @throws Exception 异常
   */
  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    String asShortText = ctx.channel().id().asShortText();
    log.warn("WebSocketHandle.channelInactive [设备关闭]: {}]", asShortText);
    Long userId = ChannelManager.removeChannelScheme2(ctx.channel());
    if (userId != null && userId > 0) {
      handlerManager.doLeaveChat(userId, ctx.channel());
      // handlerManager.roomLeave(userId)
      handlerManager.exceptionExitRoom(ctx.channel(), userId);
    }
    // 关闭通道
    ctx.channel().close();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    super.exceptionCaught(ctx, cause);
    log.warn("WebSocketHandle.exceptionCaught [与设备连接异常: {}]", cause.getMessage());
    ctx.close();
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    super.userEventTriggered(ctx, evt);
    if (evt instanceof IdleStateEvent) {
      IdleState e = ((IdleStateEvent) evt).state();
      if (e == IdleState.READER_IDLE) {
        ctx.channel().close();
        log.warn("userEventTriggered");
      }
    }
  }
}

