package com.enuos.live.handle.game.f30031;

import com.enuos.live.codec.Packet;
import io.netty.channel.Channel;
import java.util.Collections;
import java.util.Objects;
import lombok.Data;

/**
 * TODO 参与者信息.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/6/1 15:51
 */

@Data
@SuppressWarnings("WeakerAccess")
public class MinesweeperPlayer {
  /** 用户ID. */
  private Long userId;
  /** 玩家昵称. */
  private String userName;
  /** 玩家头像. */
  private String userIcon;
  /** 玩家性别 [1-男 2-女].*/
  private Integer userSex;
  /** 通信管道. */
  private Channel channel;
  /** 用户身份 [1-红 2-黑]. */
  private Integer identity;
  /** 获得分数. */
  private Integer userScore = 0;
  /** 未操作数. */
  private Integer noOperations = 0;
  /** 标记地雷. */
  private Integer indexBomb = 0;
  /** 踩地雷. */
  private Integer stepOnBomb = 0;
  /** 相同操作. */
  private Integer sameOperation = 0;
  /** 机器人 [0-是 1-否]. */
  private Integer isRobot = 1;

  /**
   * TODO 玩家判断.
   *
   * @param userId [玩家ID]
   * @return [判断结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/7/22 18:19
   */
  public boolean isBoolean(Long userId) {
    return this.userId.equals(userId);
  }

  /**
   * TODO 发送数据.
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:57
   */
  public void sendPacket(Packet packet) {
    if (Objects.nonNull(channel)) {
      if (channel.isActive()) {
        channel.writeAndFlush(packet);
      }
    }
  }

  /**
   * TODO 初始标记.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/7/22 18:19
   */
  public void initFlagNum() {
    this.noOperations = 0;
  }

  /**
   * TODO 刷新标记.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/7/22 18:19
   */
  public void refreshFlagNum() {
    this.noOperations = noOperations + 1;
  }

  /**
   * TODO 刷新得分.
   *
   * @param score [得分]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/7/22 18:19
   */
  public void refreshScore(Integer score) {
    this.userScore = userScore + score;
  }
}
