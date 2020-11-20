package com.enuos.live.pojo;

import io.netty.channel.Channel;
import java.io.Serializable;
import lombok.Data;

/**
 * TODO 匹配玩家.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/19 17:42
 */

@Data
public class MatchUser implements Serializable {
  private static final long serialVersionUID = 2147612366852959306L;
  /** 用户ID. */
  private Long userId;
  /** 用户管道. */
  private Channel channel;
  /** 匹配时间. */
  private Long startMatchTime;

}
