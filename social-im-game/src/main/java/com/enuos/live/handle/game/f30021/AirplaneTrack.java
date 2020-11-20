package com.enuos.live.handle.game.f30021;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.Data;

/**
 * TODO  飞机跑道.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/9/25 10:04
 */

@Data
@SuppressWarnings("WeakerAccess")
public class AirplaneTrack {
  /** 跑道ID. */
  private Integer trackId;
  /** 跑道颜色 [1-红 2-黄 3-蓝 4-绿]. */
  private Integer trackColor;
  /** 跑道区域 [0-起飞区 1-飞行区 2-结束区]. */
  private Integer trackZone = 1;
  /** 跑道飞行值. */
  private Integer trackFlight = 0;
  /** 跑道关联位置. */
  private Integer trackLinkSeat = 0;
  /** 跑道飞机. */
  private List<Integer> airplaneList = Lists.newCopyOnWriteArrayList();

  /**
   * TODO 初始跑道.
   *
   * @param trackId [跑道编号]
   * @author wangcaiwen|1443710411@qq.com
   * @create 2020/9/25 10:18
   * @update 2020/9/25 10:18
   */
  AirplaneTrack(Integer trackId) {
    this.trackId = trackId;
  }
}
