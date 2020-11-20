package com.enuos.live.server;

import com.enuos.live.codec.PacketDecoder;
import com.enuos.live.codec.PacketEncoder;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.manager.LoggerManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * TODO 游戏服务.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/15 21:09
 */

@Component
public class GameServer {

  @Value("${game.port}")
  private Integer port;

  private EventLoopGroup bossGroup;
  private EventLoopGroup workGroup;

  /**
   * TODO 开启游戏服务器.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/15 21:09
   * @update 2020/5/15 21:09
   */
  public void startServer() {
    try {
      bossGroup = new NioEventLoopGroup(1);
      workGroup = new NioEventLoopGroup(4);
      ServerBootstrap serverBootstrap = new ServerBootstrap();
      serverBootstrap.group(bossGroup, workGroup);
      serverBootstrap.channel(NioServerSocketChannel.class);
      serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));
      serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true)
          .option(ChannelOption.SO_BACKLOG, 1024 * 1024 * 10);
          // 容量动态调整的接收缓冲区分配器 以节约内存;
          //.option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
      serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
        @Override
        public void initChannel(SocketChannel ch) throws Exception {
          // Http解码器
          ch.pipeline()
              .addLast("http-codec", new HttpServerCodec());
          // 块处理器
          ch.pipeline()
              .addLast("http-chunked", new ChunkedWriteHandler());
          // 消息聚合
          ch.pipeline()
              .addLast("aggregator", new HttpObjectAggregator(1024 * 1024 * 1024));
          // websocket
          ch.pipeline()
              .addLast("websocket", new WebSocketServerProtocolHandler("/online", null, true, 30 * 1024 * 1024));
          // 读空闲超时, 写空闲超时, 读写空闲超时
          ch.pipeline()
              .addLast("ping", new IdleStateHandler(3600, 0, 0, TimeUnit.SECONDS));
          // 自定义协议包解码
          ch.pipeline()
              .addLast("dec", new PacketDecoder());
          // 自定义协议包编码
          ch.pipeline()
              .addLast("enc", new PacketEncoder());
          // 自定义handler
          ch.pipeline()
              .addLast("handler", new GameHandle());
        }
      });
      Channel channel = serverBootstrap.bind(port).sync().channel();
      channel.closeFuture().sync();
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    } finally {
      bossGroup.shutdownGracefully();
      workGroup.shutdownGracefully();
    }
  }
}
