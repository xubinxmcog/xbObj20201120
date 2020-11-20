package com.enuos.live.handle.game.f30051;

import com.enuos.live.codec.Packet;
import io.netty.channel.Channel;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Data;

/**
 * TODO 玩家信息.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/7/1 9:02
 */

@Data
@SuppressWarnings("WeakerAccess")
public class FindSpyPlayer {
  /** 用户ID. */
  private Long playerId = 0L;
  /** 玩家昵称. */
  private String playerName = "小易@spy";
  /** 玩家头像. */
  private String playerAvatar = " ";
  /** 玩家性别〖1.男 2.女〗. */
  private Integer playerSex = 1;
  /** 头像框. */
  private String avatarFrame;
  /** 玩家座位号. */
  private Integer seatNumber;
  /** 通信管道. */
  private Channel channel;
  /** 玩家身份 [0-玩家 1-观众]. */
  private Integer identity = 0;
  /** 游戏身份 [1-平民 2-卧底]. */
  private Integer gameIdentity = 0;
  /** 爆词描述. */
  private String openWords;
  /** 用户描述. */
  private String speakWords;
  /** 玩家投票. */
  private Integer playerVoteIndex = 0;
  /** 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]. */
  private Integer playerStatus = 0;
  /** 玩家标记 [0-游戏中 1-已出局]. */
  private Integer playerIndex = 0;
  /** 连接状态 [0-连接中 1-已断开]. */
  private Integer linkStatus = 0;
  /** 换词操作 [0-同意 1-不同意 2-未操作]. */
  private Integer changeAction = 2;
  /** 准备时间. */
  private LocalDateTime readyTime;
  /** 投出卧底. */
  private Integer voteIndexSpy = 0;
  /** 存活次数. */
  private Integer playerLiveOn = 0;

  /**
   * TODO 初始玩家.
   *
   * @param seatNumber [座位编号]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/1 21:30
   * @update 2020/7/1 21:30
   */
  FindSpyPlayer(Integer seatNumber) {
    this.seatNumber = seatNumber;
  }

  /**
   * TODO 玩家判断.
   *
   * @param playerId [玩家ID]
   * @return [判断结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/1 21:30
   * @update 2020/7/1 21:30
   */
  public boolean isBoolean(Long playerId) {
    return this.playerId.equals(playerId);
  }

  /**
   * TODO 发送数据.
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/1 21:30
   * @update 2020/7/1 21:30
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
   * @create 2020/7/7 21:29
   * @update 2020/9/1 21:09
   */
  public void finishInit() {
    // 用户身份
    this.identity = 0;
    // 游戏身份
    this.gameIdentity = 0;
    // 爆词描述
    this.openWords = null;
    // 用户描述
    this.speakWords = null;
    // 用户状态
    this.playerStatus = 0;
    // 玩家标记
    this.playerIndex = 0;
    // 换词操作
    this.changeAction = 2;
    // 连接状态
    this.linkStatus = 0;
    // 投出卧底
    this.voteIndexSpy = 0;
    // 存活次数
    this.playerLiveOn = 0;
  }
}
