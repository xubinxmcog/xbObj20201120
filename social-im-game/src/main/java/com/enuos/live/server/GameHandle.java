package com.enuos.live.server;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import com.enuos.live.codec.Packet;
import com.enuos.live.constants.GameKey;
import com.enuos.live.handle.match.MatchScan;
import com.enuos.live.channel.GameChannel;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.manager.MemManager;
import com.enuos.live.manager.NoticeManager;
import com.enuos.live.channel.SoftChannel;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.manager.LoggerManager;
import com.enuos.live.utils.RedisUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * TODO 连接中心.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/15 21:09
 */

@Component
public class GameHandle extends SimpleChannelInboundHandler<Packet> {

  @Resource
  private RedisUtils redisUtils;
  @Resource
  private HandlerContext handlerContext;

  private static GameHandle gameHandle;

  @PostConstruct
  public void init() {
    gameHandle = this;
    gameHandle.redisUtils = this.redisUtils;
    gameHandle.handlerContext = this.handlerContext;
  }

  /** 双人/多人游戏线程组. */
  private static EventLoopGroup gameGroup = new NioEventLoopGroup(4);

  /**
   * TODO 注册管道.
   *
   * @param ctx [管道信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/15 21:09
   * @update 2020/9/10 13:43
   */
  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    super.channelRegistered(ctx);
    LoggerManager.info("{} REGISTERED", ctx.channel());
  }

  /**
   * TODO 激活管道.
   *
   * @param ctx [管道信息]
   * @throws Exception [连接异常]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/15 21:09
   * @update 2020/9/30 18:06
   */
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);
    LoggerManager.info("{} ACTIVE", ctx.channel());
  }

  /**
   * TODO 读取数据.
   * channel在读取数据,或者接收 到链接之后的回调
   *
   * @param ctx [管道信息]
   * @param packet [数据包]
   * @throws Exception [连接异常]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/15 21:09
   * @update 2020/9/30 18:06
   */
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
    // 心跳相关
    if ((packet.channel == ActionCmd.GAME_HEART) || (packet.channel == ActionCmd.APP_HEART) ) {
      AbstractActionHandler instance = gameHandle.handlerContext.getInstance(packet.channel);
      if (instance != null) {
        instance.handle(ctx.channel(), packet);
      } else {
        LoggerManager.warn("[HANDLE ERROR] CHANNEL ERROR: [{}]", packet.channel);
      }
    } else if (packet.channel == ActionCmd.GAME_CHAT){
      // 聊天相关
      AbstractActionHandler instance = gameHandle.handlerContext.getInstance(packet.channel);
      if (instance != null) {
        instance.handle(ctx.channel(), packet);
      } else {
        LoggerManager.warn("[HANDLE ERROR] CHANNEL ERROR: [{}]", packet.channel);
      }
    } else {
      gameGroup.next().execute(() -> actionHandler(ctx.channel(), packet));
    }
  }

  /**
   * TODO 数据分发.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/15 21:09
   * @update 2020/9/30 18:08
   */
  private void actionHandler(Channel channel, Packet packet) {
    try {
      AbstractActionHandler instance = gameHandle.handlerContext.getInstance(packet.channel);
      if (instance != null) {
        instance.handle(channel, packet);
      } else {
        LoggerManager.warn("[HANDLE ERROR] CHANNEL ERROR: [{}]", packet.channel);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
    if (packet.channel != ActionCmd.GAME_HEART && packet.channel != ActionCmd.APP_HEART) {
      LoggerManager
          .info("[DECODE HANDLE] PACKET: [CHANEL: {}, CHILD: {}, ID: {}, ROOM/OTHER: {}]", packet.channel, packet.child, packet.userId, packet.roomId);
    }
  }

  /**
   * TODO 读取完成.
   * channel 通道 Read 读取 Complete 完成 在通道读取完成后会在这个方法里通知，对应可以做刷新操作 ctx.flush()
   *
   * @param ctx [管道信息]
   * @throws Exception [连接异常]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/15 21:09
   * @update 2020/9/30 18:07
   */
  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    ctx.flush();
  }

  /**
   * TODO 解除注册.
   * channel 解除注册到NioEventLoop上的回调
   *
   * @param ctx [管道信息]
   * @throws Exception [连接异常]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/15 21:09
   * @update 2020/9/30 18:09
   */
  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    super.channelUnregistered(ctx);
  }

  /**
   * TODO 移除设备.
   *
   * @param ctx [管道信息]
   * @throws Exception [移除异常]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/15 21:09
   * @update 2020/9/30 18:09
   */
  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    super.handlerRemoved(ctx);
  }

  /**
   * TODO 注销管道.
   * channel 通道 Inactive 不活跃的 当客户端主动断开服务端的链接后，这个通道就是不活跃的。
   * 也就是说客户端与服务端关闭了通信通道并且不可以传输数据
   *
   * @param ctx [管道信息]
   * @throws Exception [注销异常]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/15 21:09
   * @update 2020/10/9 10:13
   */
  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    LoggerManager.warn("{} INACTIVE", ctx.channel());
    // 关闭游戏
    closeGame(ctx);
    // 关闭软件
    closeSoft(ctx);
    // 关闭管道
    closeChannel(ctx);
  }

  /**
   * TODO 关闭游戏.
   *
   * @param ctx [管道信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/15 21:09
   * @update 2020/10/9 10:58
   */
  private void closeGame(ChannelHandlerContext ctx) {
    try {
      long playerId = GameChannel.getUserId(ctx.channel());
      if (playerId > 0) {
        // 测试用户
        if (gameHandle.redisUtils.hasKey(GameKey.KEY_GAME_TEST_LOGIN.getName() + playerId)) {
          gameHandle.redisUtils.del(GameKey.KEY_GAME_TEST_LOGIN.getName() + playerId);
        }
        // 关闭游戏
        MemManager.delMemberRec(playerId);
        GameChannel.clearChannel(ctx.channel());
        NoticeManager.GAME_GROUP.remove(ctx.channel());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 关闭软件.
   *
   * @param ctx [管道信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/15 21:09
   * @update 2020/10/9 10:58
   */
  private void closeSoft(ChannelHandlerContext ctx) {
    try {
      long userId = SoftChannel.getUserId(ctx.channel());
      if (userId > 0) {
        if (gameHandle.redisUtils.hasKey(GameKey.KEY_GAME_USER_LOGIN.getName() + userId)) {
          gameHandle.redisUtils.del(GameKey.KEY_GAME_USER_LOGIN.getName() + userId);
        }
        // 关闭软件
        if (gameHandle.redisUtils.hasKey(GameKey.KEY_GAME_USER_MATCH.getName() + userId)) {
          Object gameBase = gameHandle.redisUtils.get(GameKey.KEY_GAME_USER_MATCH.getName() + userId);
          long gameCode = ((Number) gameBase).longValue() ;
          gameHandle.redisUtils.del(GameKey.KEY_GAME_USER_MATCH.getName() + userId);
          // 取消匹配
          int matchCode = MatchScan.getMatchCode(gameCode);
          AbstractActionHandler instance = gameHandle.handlerContext.getInstance(matchCode);
          if ((Objects.nonNull(instance))) {
            instance.shutOff(userId, gameCode);
          }
        }
        if (gameHandle.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + userId)) {
          byte[] bytes = gameHandle.redisUtils.getByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + userId);
          // 离开游戏
          gameHandle.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + userId);
          I10001.JoinGame joinInfo = I10001.JoinGame.parseFrom(bytes);
          int gameId = Math.toIntExact(joinInfo.getGameId());
          AbstractActionHandler instance = gameHandle.handlerContext.getInstance(gameId);
          if ((Objects.nonNull(instance))) {
            instance.shutOff(userId,joinInfo.getRoomId());
          }
        }
        SoftChannel.clearChannel(ctx.channel());
        NoticeManager.SOFT_GROUP.remove(ctx.channel());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 异常连接.
   *
   * @param ctx [管道信息]
   * @param cause [异常信息]
   * @throws Exception [连接异常]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/15 21:09
   * @update 2020/10/9 12:51
   */
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    super.exceptionCaught(ctx, cause);
    LoggerManager.warn(cause.getMessage());
    Channel channel = ctx.channel();
    if(channel.isActive()) {
      closeChannel(ctx);
    }
  }

  /**
   * TODO 事件触发.
   *
   * @param ctx [管道信息]
   * @param evt [状态事件]
   * @throws Exception [连接异常]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/15 21:09
   * @update 2020/10/9 12:53
   */
  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    super.userEventTriggered(ctx, evt);
    if (evt instanceof IdleStateEvent) {
      IdleState e = ((IdleStateEvent) evt).state();
      if (e == IdleState.READER_IDLE) {
        // 读空闲,链路持续时间内 没有读取到任何消息
        LoggerManager.warn("{} READER IDLE", ctx.channel());
        closeChannel(ctx);
      } else if (e == IdleState.WRITER_IDLE) {
        // 写空闲,链路持续时间内 没有发送任何消息
        LoggerManager.warn("{} WRITER IDLE", ctx.channel());
      }  else if (e == IdleState.ALL_IDLE) {
        // 读写空闲,链路持续时间内 没有接收或者发送任何消息
        LoggerManager.warn("{} ALL IDLE", ctx.channel());
      }
    }
  }

  /**
   * TODO 关闭通道.
   *
   * @param ctx [管道信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/15 21:09
   * @update 2020/10/9 12:58
   */
  private void closeChannel(ChannelHandlerContext ctx) {
    try {
      Channel channel = ctx.channel();
      if (Objects.nonNull(channel)) {
        channel.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

