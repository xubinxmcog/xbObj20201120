package com.enuos.live.handle.game.f30051.dddd;

import com.enuos.live.codec.Packet;
import io.netty.channel.Channel;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Data;

/**
 * TODO 玩家信息.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/11/6 10:57
 */

@Data
@SuppressWarnings("WeakerAccess")
public class FindUndercoverPlayer {

  /** 通信管道. */
  private Channel channel;
  /** 座位编号. */
  private Integer seatNumber;

  /** 用户ID. */
  private Long playerId = 0L;
  /** 玩家性别 1-男 2-女. */
  private Integer playerSex = 1;
  /** 玩家昵称. */
  private String playerName = "e@Undercover.com";
  /** 用户状态 0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开. */
  private Integer playerStatus = 0;

  /** 玩家头像. */
  private String avatarIcon = " ";
  /** 头像边框. */
  private String avatarFrame;

  /** 玩家身份 0-玩家 1-观众. */
  private Integer identity = 0;
  /** 游戏身份 0-未分配 1-平民 2-卧底. */
  private Integer gameIdentity = 0;

  /** 爆词描述. */
  private String openWords;
  /** 玩家描述. */
  private String speakWords;

  /** 投票标记. */
  private Integer voteIndex = 0;
  /** 平票标记. */
  private Integer battleIndex = 0;
  /** 换词操作 0-同意 1-不同意 2-未操作. */
  private Integer changeIndex = 2;

  /** 准备时间. */
  private LocalDateTime readinessTime;

  /** 存活次数. */
  private Integer survivalTimes = 0;
  /** 投出卧底. */
  private Integer findUndercover = 0;

  /**
   * TODO 初始信息.
   *
   * @param seatNumber [座位编号]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 12:53
   * @update 2020/11/6 12:53
   */
  FindUndercoverPlayer(Integer seatNumber) {
    this.seatNumber = seatNumber;
  }

  /**
   * TODO 是否相等.
   *
   * @param playerId [玩家ID]
   * @return [相等结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 12:53
   * @update 2020/11/6 12:53
   */
  public boolean isEquals(Long playerId) {
    return Objects.equals(this.playerId, playerId);
  }

  /**
   * TODO 发送数据.
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 12:56
   * @update 2020/11/6 12:56
   */
  public void sendPacket(Packet packet) {
    if (Objects.nonNull(channel)) {
      if (channel.isActive()) {
        channel.writeAndFlush(packet);
      }
    }
  }

  /**
   * TODO 初始信息.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 12:56
   * @update 2020/11/6 12:56
   */
  public void init() {
    this.identity = 0;
    this.gameIdentity = 0;
    this.openWords = null;
    this.speakWords = null;
    this.playerStatus = 0;
    this.voteIndex = 0;
    this.battleIndex = 0;
    this.changeIndex = 0;
    this.readinessTime = null;
    this.survivalTimes = 0;
    this.findUndercover = 0;
  }
}
