package com.enuos.live.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author WangCaiWen Created on 2019/10/21 13:39
 */
@Slf4j
@Component
public class PacketDecoder extends MessageToMessageDecoder<WebSocketFrame> {

  private static final int READABLE_BYTES = 10;

  @Override
  protected void decode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out)
      throws Exception {
    ByteBuf buf = msg.content();
    buf.markReaderIndex();
    if (buf.readableBytes() >= READABLE_BYTES) {
      int channel = buf.readInt();
      short child = buf.readShort();
      long userId = buf.readLong();
      long attachId = buf.readLong();
      byte[] bytes = new byte[buf.readableBytes()];
      buf.readBytes(bytes);
      Packet packet = new Packet(channel, child, bytes);
      packet.userId = userId;
      packet.attachId = attachId;
      out.add(packet);
      decode(ctx, msg, out);
    }
  }
}
