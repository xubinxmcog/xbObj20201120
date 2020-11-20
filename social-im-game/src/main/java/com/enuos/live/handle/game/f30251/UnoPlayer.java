package com.enuos.live.handle.game.f30251;

import com.enuos.live.codec.Packet;
import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * TODO 优诺玩家.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V2.0.0
 * @since 2020/8/13 11:10
 */

@Data
@SuppressWarnings("WeakerAccess")
public class UnoPlayer {

  /**
   * 玩家ID.
   */
  private Long playerId = 0L;
  /**
   * 玩家昵称.
   */
  private String playerName = "小易@SX";
  /**
   * 玩家性别 1-男 2-女
   */
  private Integer playerSex = 1;
  /**
   * 玩家头像.
   */
  private String playerAvatar = " ";
  /**
   * 玩家金币.
   */
  private Integer playerGold = 0;
  /**
   * 玩家头像框.
   */
  private String avatarFrame;
  /**
   * 玩家通讯通道.
   */
  private Channel channel;
  /**
   * 座位号.
   */
  private Integer seatNumber;
  /**
   * 玩家身份 0-玩家 1-观众.
   */
  private Integer identity = 0;
  /**
   * 连接状态 0-连接中 1-已断开.
   */
  private Integer linkStatus = 0;
  /**
   * 用户状态 0-未准备 1-已准备 2-游戏中 3-已离开.
   */
  private Integer playerStatus = 0;
  /**
   * 临时时间「例如：2020-07-08T16:58:53.978」.
   */
  private LocalDateTime readyTime;
  /**
   * 玩家操作 0-默认 1-选择颜色 2-发动质疑 3-摸牌.
   */
  private Integer playerAction = 0;
  /**
   * 收到质疑 红-1 黄-2 蓝-3 绿-4.
   */
  private Integer receiveQuestion = 0;
  /**
   * 卡背皮肤.
   */
  private String cardBackSkin;
  /**
   * 玩家得分「例如：0/-25」.
   */
  private Integer gameScore = 0;
  /**
   * 金币获得「例如：40/-40」.
   */
  private Integer gameGold = 0;
  /**
   * 摸牌数据.
   */
  private UnoPoker touchCards;
  /**
   * 玩家扑克.
   */
  private List<UnoPoker> playerPoker = Lists.newCopyOnWriteArrayList();
  /**
   * Uno.
   */
  private Integer isUno = 0;

  /**
   * TODO 数据初始.
   *
   * @param seatNumber 座位号
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 13:57
   * @update 2020/8/17 13:57
   */
  UnoPlayer(Integer seatNumber) {
    this.seatNumber = seatNumber;
  }

  /**
   * TODO 玩家检测.
   *
   * @param playerId 玩家ID
   * @return boolean
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 13:57
   * @update 2020/8/17 13:57
   */
  public boolean isBoolean(Long playerId) {
    return this.playerId.equals(playerId);
  }

  /**
   * TODO 发送数据.
   *
   * @param packet 数据包
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 13:59
   * @update 2020/8/17 13:59
   */
  public void sendPacket(Packet packet) {
    if (channel.isActive()) {
      channel.writeAndFlush(packet);
    }
  }

  /**
   * TODO 移除扑克.
   *
   * @param poker 扑克信息
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/18 13:48
   * @update 2020/8/18 13:48
   */
  public void removePoker(UnoPoker poker) {
    int index = 0;
    for (UnoPoker unoPoker : playerPoker) {
      if (unoPoker.getPokerId().equals(poker.getPokerId())
          && unoPoker.getPokerColors().equals(poker.getPokerColors())) {
        playerPoker.remove(index);
        break;
      }
      index++;
    }
  }

  /**
   * TODO 初始数据.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/20 9:55
   * @update 2020/8/20 9:55
   */
  public void init() {
    this.playerStatus = 0;
    this.readyTime = null;
    this.playerAction = 0;
    this.receiveQuestion = 0;
    this.gameScore = 0;
    this.gameGold = 0;
    this.touchCards = null;
    this.playerPoker = Lists.newCopyOnWriteArrayList();
    this.isUno = 0;
  }
}
