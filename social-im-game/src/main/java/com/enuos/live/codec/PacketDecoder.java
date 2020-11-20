package com.enuos.live.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * TODO 解码器.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.0.0
 * @since 2020/5/10 14:36
 */

@Component
public class PacketDecoder extends MessageToMessageDecoder<WebSocketFrame> {

  /** 阅读字节长度. */
  private static final int READABLE_BYTES= 14;
  /** 阅读字节长度.游戏. */
  private static final int READABLE_BYTES_GAME= 14;
  /** 阅读字节长度.软件. */
  private static final int READABLE_BYTES_SOFT= 22;
  /** 最小消息号. */
  private static final int MIN_CHANNEL = 10000;
  /** 最大消息号. */
  private static final int MAX_CHANNEL = 99999;
  /** 软件编码. */
  private static final int SOFT_CODE = 20000;
  /** 游戏编码. */
  private static final int GAME_CODE = 30000;

  /**
   * TODO 消息解码.
   *
   * @param ctx [通讯管道]
   * @param msg [管道数据]
   * @param out [输出消息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/10 14:36
   * @update 2020/10/10 10:33
   */
  @Override
  protected void decode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) throws Exception {
    // v2.0.0 消息解码
    ByteBuf buf = msg.content();
    // 读取标记
    buf.markReaderIndex();
    // 获取包头长度
    int baseLength = buf.readableBytes();
    if (baseLength >= READABLE_BYTES) {
      int channel = buf.readInt();
      if (channel >= MIN_CHANNEL && channel < MAX_CHANNEL) {
        short child = buf.readShort();
        long userId;
        long roomId;
        // 区分软件和游戏连接
        if (channel >= SOFT_CODE && channel < GAME_CODE) {
          userId = buf.readLong();
          roomId = buf.readLong();
          int residue = buf.readableBytes();
          if (baseLength - residue > READABLE_BYTES_SOFT || baseLength - residue < READABLE_BYTES_SOFT) {
            ctx.channel().close();
            buf.clear();
            return;
          }
        } else {
          int user = buf.readInt();
          int room = buf.readInt();
          userId = user;
          roomId = room;
          int residue = buf.readableBytes();
          if (baseLength - residue > READABLE_BYTES_GAME || baseLength - residue < READABLE_BYTES_GAME) {
            ctx.channel().close();
            buf.clear();
            return;
          }
        }
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        Packet packet = new Packet(channel, child, bytes);
        packet.userId = userId;
        packet.roomId = roomId;
        out.add(packet);
        decode(ctx, msg, out);
      } else {
        ctx.channel().close();
        buf.clear();
      }
    } else {
      buf.clear();
    }
  }

  // v1.0.0 消息解码
  //    ByteBuf buf = msg.content();
  //    // 开始读取协议开始标记
  //    buf.markReaderIndex();
  //    // 获取包头长度
  //    int baseLength = buf.readableBytes();
  //    if (baseLength >= READABLE_BYTES) {
  //      int channel = buf.readInt();
  //      short child = buf.readShort();
  //      long userId;
  //      long roomId;
  //      // 区分软件和游戏连接
  //      if (channel >= SOFT_CODE && channel < GAME_CODE) {
  //        userId = buf.readLong();
  //        roomId = buf.readLong();
  //      } else {
  //        int user = buf.readInt();
  //        int room = buf.readInt();
  //        userId = user;
  //        roomId = room;
  //      }
  //      byte[] bytes = new byte[buf.readableBytes()];
  //      buf.readBytes(bytes);
  //      Packet packet = new Packet(channel, child, bytes);
  //      packet.userId = userId;
  //      packet.roomId = roomId;
  //      out.add(packet);
  //      decode(ctx, msg, out);
  //    } else {
  //      buf.clear();
  //    }
  //  }

}
