package com.enuos.live.handle.game.f30001;

import com.enuos.live.codec.Packet;
import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import java.util.List;
import java.util.Objects;
import lombok.Data;

/**
 * TODO 参与者信息.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/5/16 11:37
 */

@Data
@SuppressWarnings("WeakerAccess")
public class GoBangPlayer {
  /** 用户ID. */
  private Long userId;
  /** 玩家昵称. */
  private String userName;
  /** 玩家头像. */
  private String userIcon;
  /** 玩家性别 [1-男 2-女]. */
  private Integer userSex;
  /** 通信管道. */
  private Channel channel;
  /** 用户身份 [1-黑 2-白]. */
  private Integer identity;
  /** 成就标记. */
  private Integer successIndex = 0;
  /** 玩家操作. */
  private List<GoBangCoords> actionCoords = Lists.newCopyOnWriteArrayList();
  /** 游戏装饰. */
  private String gameDecorate;
  /** 机器人 [0-是 1-否]. */
  private Integer isRobot = 1;

  /**
   * TODO 玩家判断.
   *
   * @param userId [玩家ID]
   * @return [判断结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/16 17:09
   * @update 2020/5/16 17:09
   */
  public boolean isBoolean(Long userId) {
    return this.userId.equals(userId);
  }

  /**
   * TODO 发送数据.
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/16 17:09
   * @update 2020/9/17 21:07
   */
  public void sendPacket(Packet packet) {
    if (Objects.nonNull(channel)) {
      if (channel.isActive()) {
        channel.writeAndFlush(packet);
      }
    }
  }

  /**
   * TODO 玩家操作.
   *
   * @param coords [坐标数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/23 21:12
   * @update 2020/7/23 21:12
   */
  public void playerAction(GoBangCoords coords) {
    this.actionCoords.add(coords);
  }

  /**
   * TODO 最后操作.
   *
   * @return [坐标数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/23 21:12
   * @update 2020/7/23 21:12
   */
  public GoBangCoords lastAction() {
    return actionCoords.get(actionCoords.size() - 1);
  }

  /**
   * TODO 删除操作.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/23 21:12
   * @update 2020/7/23 21:12
   */
  public void deleteAction() {
    this.actionCoords.remove(actionCoords.size() - 1);
  }

  /**
   * TODO 4/5连记录.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/30 21:28
   * @update 2020/7/30 21:28
   */
  public void is4LinkRecord() {
    this.successIndex = successIndex + 1;
  }

}
