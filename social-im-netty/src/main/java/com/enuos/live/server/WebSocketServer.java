package com.enuos.live.server;

import com.enuos.live.core.PacketDecoder;
import com.enuos.live.core.PacketEncoder;
import com.enuos.live.manager.HandlerManager;
import com.enuos.live.utils.ExceptionUtil;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * TODO SOCKET服务.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/3/17 - 2020/7/28
 */

@Slf4j
@Configuration
public class WebSocketServer {

  @Value("${social.port}")
  private Integer port;

  @Autowired
  private HandlerManager handlerManager;

  private EventLoopGroup bossGroup;
  private EventLoopGroup workGroup;

  public void startServer() {
    try {
      bossGroup = new NioEventLoopGroup();
      workGroup = new NioEventLoopGroup();
      ServerBootstrap serverBootstrap = new ServerBootstrap();
      serverBootstrap.group(bossGroup, workGroup);
      serverBootstrap.channel(NioServerSocketChannel.class);
      serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));
      serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true)
          .option(ChannelOption.SO_BACKLOG, 1024 * 1024 * 10);
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
              .addLast("websocket",
                  new WebSocketServerProtocolHandler("/visit", null, true, 30 * 1024 * 1024));
          // 读空闲超时, 写空闲超时, 读写空闲超时
          ch.pipeline()
              .addLast("ping", new IdleStateHandler(1800, 0, 0, TimeUnit.SECONDS));
          // 自定义协议包解码
          ch.pipeline()
              .addLast("dec", new PacketDecoder());
          // 自定义协议包编码
          ch.pipeline()
              .addLast("enc", new PacketEncoder());
          // 自定义handler
          ch.pipeline()
              .addLast("handler", new WebSocketHandle(handlerManager));
        }
      });
      Channel channel = serverBootstrap.bind(port).sync().channel();
      channel.closeFuture().sync();
    } catch (Exception e) {
      log.error("WebSocketServer Error: {}]", e.getMessage());
      log.error(ExceptionUtil.getStackTrace(e));
    } finally {
      bossGroup.shutdownGracefully();
      workGroup.shutdownGracefully();
    }
  }
}
