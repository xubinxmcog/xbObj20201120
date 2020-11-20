package com.enuos.live.codec;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO 数据包.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/15 21:09
 */

@Data
@NoArgsConstructor
public class Packet implements Serializable {
  private static final long serialVersionUID = -354303583334661287L;

  /** 消息号. */
  public int channel;
  /** 子消息号. */
  public short child;
  /** 用户ID. */
  public long userId;
  /** 房间ID. */
  public long roomId;
  /** 消息体. */
  public byte[] bytes;

  /**
   * TODO 消息聚合.
   *
   * @param channel [消息号]
   * @param child [子消息号]
   * @param bytes [消息体]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/15 21:09
   * @update 2020/6/15 18:19
   */
  public Packet(int channel, short child, byte[] bytes) {
    this.channel = channel;
    this.child = child;
    this.bytes = bytes;
  }

}
