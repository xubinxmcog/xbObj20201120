package com.enuos.live.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * TODO 编码器.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/10 14:36
 */

@Component
public class PacketEncoder extends MessageToMessageEncoder<Packet> {

  /**
   * TODO 消息编码.
   *
   * @param ctx [通讯管道]
   * @param packet [数据包]
   * @param out [输出消息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/10 14:36
   * @update 2020/10/10 10:37
   */
  @Override
  protected void encode(ChannelHandlerContext ctx, Packet packet, List<Object> out) throws Exception {
    ByteBuf msgBuffer = Unpooled.buffer();
    msgBuffer.writeInt(packet.channel);
    msgBuffer.writeShort(packet.child);
    if (Objects.nonNull(packet.bytes)) {
      msgBuffer.writeBytes(packet.bytes);
    }
    WebSocketFrame frame = new BinaryWebSocketFrame(msgBuffer);
    out.add(frame);
  }
}
