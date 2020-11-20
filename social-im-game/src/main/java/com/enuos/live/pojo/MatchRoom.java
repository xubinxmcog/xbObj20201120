package com.enuos.live.pojo;

import java.io.Serializable;
import lombok.Data;

/**
 * TODO 匹配房间.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/9/10 12:57
 */

@Data
public class MatchRoom implements Serializable {
  private static final long serialVersionUID = -4692324400091891236L;
  /** 房间ID. */
  private Long roomId;
  /** 剩余座位. */
  private Integer peopleNum;
}
