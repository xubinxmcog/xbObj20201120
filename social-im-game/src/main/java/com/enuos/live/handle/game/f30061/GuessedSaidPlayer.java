package com.enuos.live.handle.game.f30061;

import com.enuos.live.codec.Packet;
import io.netty.channel.Channel;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * TODO 参与玩家.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/8/4 10:21
 */

@Data
@SuppressWarnings("WeakerAccess")
public class GuessedSaidPlayer {
  /** 玩家ID. */
  private Long playerId = 0L;
  /** 玩家昵称. */
  private String playerName = "小轩@SX";
  /** 玩家性别 [1-男 2-女] */
  private Integer playerSex = 1;
  /** 玩家头像. */
  private String playerAvatar = " ";
  /** 玩家头像框. */
  private String playerAvatarFrame;
  /** 通信管道. */
  private Channel playerChannel;
  /** 座位编号. */
  private Integer seatNumber;
  /** 用户得分. */
  private Integer playerScore = 0;
  /** 玩家身份 [0-玩家 1-观众]. */
  private Integer identity = 0;
  /** 连接状态 [0-连接中 1-已断开]. */
  private Integer linkStatus = 0;
  /** 用户状态 [0-未准备 1-已准备 2-游戏中 3-已离开]. */
  private Integer playerStatus = 0;
  /** 准备次数. */
  private Integer playerReady = 0;
  /** 准备时间. */
  private LocalDateTime readyTime;
  /** 玩家描述. */
  private String playerWords;
  /** 是否正确 [0-正确 1-错误]. */
  private Integer wordIsTrue = 1;
  /** 换词申请. */
  private Integer changeWords = 1;
  /** 猜中次数. */
  private Integer guessedIsTrue = 0;

  /**
   * TODO 初始座位.
   *
   * @param seatNumber [座位编号]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 10:34
   * @update 2020/8/10 17:26
   */
  GuessedSaidPlayer(Integer seatNumber) {
    this.seatNumber = seatNumber;
  }

  /**
   * TODO 玩家判断.
   *
   * @param playerId [玩家ID]
   * @return [判断结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 10:34
   * @update 2020/8/10 17:26
   */
  public boolean isBoolean(Long playerId) {
    return this.playerId.equals(playerId);
  }

  /**
   * TODO 发送数据.
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 10:34
   * @update 2020/8/10 17:26
   */
  public void sendPacket(Packet packet) {
    if (playerChannel.isActive()) {
      playerChannel.writeAndFlush(packet);
    }
  }

  /**
   * TODO 初始信息.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @create 2020/8/4 10:34
   * @update 2020/8/10 17:26
   */
  public void init() {
    this.playerScore = 0;
    this.playerStatus = 0;
    this.playerReady = 0;
    this.readyTime = null;
    this.playerWords = null;
    this.wordIsTrue = 1;
    this.changeWords = 1;
    this.guessedIsTrue = 0;
  }
}
