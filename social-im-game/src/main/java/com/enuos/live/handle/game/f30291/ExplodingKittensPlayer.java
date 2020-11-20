package com.enuos.live.handle.game.f30291;

import com.enuos.live.codec.Packet;
import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

/**
 * TODO 炸弹猫玩家.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V2.0.0
 * @since 2020/8/31 12:39
 */

@Data
@SuppressWarnings("WeakerAccess")
public class ExplodingKittensPlayer {
  /** 玩家ID. */
  private Long playerId = 0L;
  /** 玩家昵称. */
  private String playerName = "小易@kittens";
  /** 玩家性别 1-男 2-女. */
  private Integer playerSex = 1;
  /** 玩家头像. */
  private String playerAvatar = " ";
  /** 玩家金币. */
  private Integer playerGold = 0;
  /** 玩家头像框. */
  private String avatarFrame;
  /** 玩家通讯通道. */
  private Channel channel;
  /** 座位号. */
  private Integer seatNumber;
  /** 玩家身份 [0-玩家 1-观众]. */
  private Integer identity = 0;
  /** 连接状态 [0-连接中 1-已断开]. */
  private Integer linkStatus = 0;
  /** 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]. */
  private Integer playerStatus = 0;
  /** 卡背皮肤. */
  private String cardBackSkin;
  /** 准备时间. */
  private LocalDateTime readyTime;
  /** 触摸标记. */
  private Integer touchExploding = 0;
  /** 束缚标记. */
  private Integer bondageIndex = 0;
  /** 玩家手牌. */
  private List<ExplodingKittensCard> playerCard = Lists.newCopyOnWriteArrayList();

  /**
   * TODO 初始数据.
   *
   * @param seatNumber 座位号
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 16:53
   * @update 2020/8/31 16:53
   */
  ExplodingKittensPlayer(Integer seatNumber) {
    this.seatNumber = seatNumber;
  }

  /**
   * TODO 玩家检查.
   *
   * @param playerId 玩家ID
   * @return boolean 相等
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 16:53
   * @update 2020/8/31 16:53
   */
  public boolean isBoolean(Long playerId) {
    return this.playerId.equals(playerId);
  }

  /**
   * TODO 发送数据.
   *
   * @param packet 数据包
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 16:54
   * @update 2020/8/31 16:54
   */
  public void sendPacket(Packet packet) {
    if (Objects.nonNull(channel)) {
      if (channel.isActive()) {
        channel.writeAndFlush(packet);
      }
    }
  }

  /**
   * TODO 初始数据.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 16:55
   * @update 2020/8/31 16:55
   */
  public void init() {
    this.playerStatus = 0;
    this.readyTime = null;
    this.bondageIndex = 0;
    this.touchExploding = 0;
    this.playerCard = Lists.newCopyOnWriteArrayList();
  }

  /**
   * TODO 移除扑克.
   *
   * @param cardId 扑克Id
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/18 13:48
   * @update 2020/8/18 13:48
   */
  public void removeCard(Integer cardId) {
    if (CollectionUtils.isNotEmpty(playerCard)) {
      playerCard.removeIf(card -> Objects.equals(card.getCardId(), cardId));
    }
  }

}
