package com.enuos.live.handle.game.f30011;

import com.enuos.live.codec.Packet;
import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Data;

/**
 * TODO 参与者信息.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/5/21 19:59
 */

@Data
public class AnimalPlayer {
  /** 用户ID. */
  private Long userId;
  /** 玩家昵称. */
  private String userName;
  /** 玩家头像. */
  private String userIcon;
  /** 玩家性别 [1-男 2-女]. */
  private Integer userSex;
  /** 玩家身份. */
  private Integer identity;
  /** 通信管道. */
  private Channel channel;
  /** 操作次数. */
  private Integer actionNum = 0;
  /** 最新操作. */
  private AnimalCoords newAnimalCoords;
  /** 最新翻转. */
  private AnimalCoords flipAnimalCoords;
  /** 狮子吃动物. */
  private Integer lionEatAnimal = 0;
  /** 虎吃狼. */
  private Integer tigerEatWolf = 0;
  /** 狗吃猫/鼠. */
  private Integer dogEatCatOrMouse = 0;
  /** 猫吃鼠. */
  private Integer catEatMouse = 0;
  /** 鼠吃象. */
  private Integer mouseEatElephant = 0;
  /** 机器人 [0-是 1-否]. */
  private Integer isRobot = 1;
  /** 棋子皮肤. */
  private List<Map<String, Object>> chessStyle = Lists.newCopyOnWriteArrayList();

  /**
   * TODO 玩家判断.
   *
   * @param userId [玩家ID]
   * @return [判断结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/25 6:26
   * @update 2020/5/25 6:26
   */
  public boolean isBoolean(Long userId) {
    return this.userId.equals(userId);
  }

  /**
   * TODO 发送数据.
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/25 6:26
   * @update 2020/9/22 21:08
   */
  public void sendPacket(Packet packet) {
    if (Objects.nonNull(channel)) {
      if (channel.isActive()) {
        channel.writeAndFlush(packet);
      }
    }
  }
}
