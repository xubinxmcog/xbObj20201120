package com.enuos.live.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author WangCaiWen Created on 2019/10/21 13:40
 */

@Component
public class PacketEncoder extends MessageToMessageEncoder<Packet> {

  @Override
  protected void encode(ChannelHandlerContext ctx, Packet packet, List<Object> out)
      throws Exception {
    ByteBuf msgBuffer = Unpooled.buffer();
    msgBuffer.writeInt(packet.channel);
    msgBuffer.writeShort(packet.child);
    if (packet.bytes != null) {
      msgBuffer.writeBytes(packet.bytes);
    }
    WebSocketFrame frame = new BinaryWebSocketFrame(msgBuffer);
    out.add(frame);
  }
}
