package com.enuos.live.handle.game.f30021;

import com.enuos.live.codec.Packet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

/**
 * TODO 参与者信息.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/9/24 20:41
 */

@Data
@SuppressWarnings("WeakerAccess")
public class AirplanePlayer {
  /** 玩家ID. */
  private Long playerId;
  /** 玩家名称. */
  private String playerName;
  /** 玩家头像. */
  private String playerIcon;
  /** 玩家性别. */
  private Integer playerSex;
  /** 玩家身份. */
  private Integer playerColor;
  /** 飞机皮肤. */
  private String airplaneSkin;
  /** 起飞位置. */
  private Integer startPositional;
  /** 结束位置. */
  private Integer finishPositional;
  /** 通信管道. */
  private Channel channel;
  /** 基地信息. */
  private Map<Integer, AirplaneEntity> airplaneBase = Maps.newHashMap();
  /** 起飞数据. */
  private Map<Integer, AirplaneEntity> airplaneFly = Maps.newHashMap();
  /** 最新骰子. */
  private Integer latestDice = 0;
  /** 开启托管 [0-关闭 1-开启]. */
  private Integer startEscrow = 0;
  /** 托管次数. */
  private Integer trusteeship = 0;
  /** 连接状态 [0-连接中 1-已断开]. */
  private Integer linkStatus = 0;
  /** 结束标记. */
  private Integer finishIndex = 0;
  /** 游戏状态 [0-游戏中 1-已结束 2-已离开(已认输)]. */
  private Integer playerStatus = 0;
  /** 是否机器 [0-是 1-否]. */
  private Integer isRobot = 1;
  /** 撞回基地. */
  private Integer hitBackToBase = 0;
  /** 区域兜圈. */
  private Integer regionalCircle = 0;
  /** 操作标记. */
  private Integer playActionIndex = 1;

  /** 飞机数量. */
  private static final int AIRPLANE_NUM = 4;

  /**
   * TODO 初始玩家.
   *
   * @param startPositional [起飞位置]
   * @param finishPositional [结束位置]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/24 21:08
   * @update 2020/9/24 21:08
   */
  AirplanePlayer(Integer startPositional, Integer finishPositional) {
    this.startPositional = startPositional;
    this.finishPositional = finishPositional;
  }

  /**
   * TODO 玩家判断.
   *
   * @param playerId [玩家ID]
   * @return [判断结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/24 21:11
   * @update 2020/9/24 21:11
   */
  public boolean isBoolean(Long playerId) {
    return Objects.equals(this.playerId, playerId);
  }

  /**
   * TODO 发送数据.
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 9:02
   * @update 2020/9/25 9:02
   */
  public void sendPacket(Packet packet) {
    if (Objects.nonNull(channel)) {
      if (channel.isActive()) {
        channel.writeAndFlush(packet);
      }
    }
  }

  /**
   * TODO 创建飞机.
   *
   * @param color [玩家身份]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 9:09
   * @update 2020/9/25 9:09
   */
  public void createAirplaneBase(Integer color) {
    switch (color) {
      case 1:
        int redChess = 1;
        for (int i = 0; i < AIRPLANE_NUM; i++) {
          AirplaneEntity airplane = new AirplaneEntity(redChess);
          this.airplaneBase.put(redChess, airplane);
          redChess++;
        }
        break;
      case 2:
        int yellowChess = 5;
        for (int i = 0; i < AIRPLANE_NUM; i++) {
          AirplaneEntity airplane = new AirplaneEntity(yellowChess);
          this.airplaneBase.put(yellowChess, airplane);
          yellowChess++;
        }
        break;
      case 3:
        int blueChess = 9;
        for (int i = 0; i < AIRPLANE_NUM; i++) {
          AirplaneEntity airplane = new AirplaneEntity(blueChess);
          this.airplaneBase.put(blueChess, airplane);
          blueChess++;
        }
        break;
      default:
        int greenChess = 13;
        for (int i = 0; i < AIRPLANE_NUM; i++) {
          AirplaneEntity airplane =  new AirplaneEntity(greenChess);
          this.airplaneBase.put(greenChess, airplane);
          greenChess++;
        }
        break;
    }
  }

  /**
   * TODO 回到基地. (被击落)
   *
   * @param airplaneList [飞机列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 9:17
   * @update 2020/9/25 9:17
   */
  public void comeBackBase(List<Integer> airplaneList) {
    if (CollectionUtils.isNotEmpty(airplaneList)) {
      for (Integer airplaneId : airplaneList) {
        AirplaneEntity airplane = airplaneFly.get(airplaneId);
        airplane.setPositional(0);
        // 返回基地
        this.airplaneBase.put(airplaneId, airplane);
        // 离开飞行
        this.airplaneFly.remove(airplaneId);
      }
    }
  }

  /**
   * TODO 飞机起飞.
   *
   * @param airplaneId [飞机ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 9:22
   * @update 2020/9/25 9:22
   */
  public void airplaneStartFly(Integer airplaneId) {
    AirplaneEntity airplane = new AirplaneEntity(airplaneId);
    airplane.setPositional(startPositional);
    // 进入飞行
    this.airplaneFly.put(airplaneId, airplane);
    // 离开基地
    this.airplaneBase.remove(airplaneId);
  }

  /**
   * TODO 更新位置.
   *
   * @param airplaneId [飞机ID]
   * @param positional [位置ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 9:30
   * @update 2020/9/25 9:30
   */
  public void updatePositional(Integer airplaneId, Integer positional) {
    AirplaneEntity airplane = airplaneFly.get(airplaneId);
    airplane.setPositional(positional);
    this.airplaneFly.put(airplaneId, airplane);
  }

  /**
   * TODO 抵达终点.
   *
   * @param airplaneId [飞机ID]
   * @param positional [位置ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 9:32
   * @update 2020/9/25 9:32
   */
  public void arrivedEndPositional(Integer airplaneId, Integer positional) {
    AirplaneEntity airplane = airplaneFly.get(airplaneId);
    airplane.setPositional(positional);
    // 完成飞行 [0-飞行 1-结束]
    airplane.setIsFinish(1);
    // 返回基地
    this.airplaneBase.put(airplaneId, airplane);
    // 离开飞行
    this.airplaneFly.remove(airplaneId);
  }

  /**
   * TODO 终点飞机.
   *
   * @return [终点数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 9:44
   * @update 2020/9/25 9:44
   */
  public int getArrivedEndAirplaneNum() {
    return (int) airplaneBase.values().stream()
        .filter(airplane -> airplane.getIsFinish() == 1).count();
  }

  /**
   * TODO 基地飞机.
   *
   * @return [飞机列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 9:50
   * @update 2020/9/25 9:50
   */
  public List<AirplaneEntity> getAirplaneBaseList() {
    return airplaneBase.values().stream()
        .filter(airplane -> airplane.getIsFinish() == 0)
        .collect(Collectors.toCollection(Lists::newLinkedList));
  }

  /**
   * TODO 起飞飞机.
   *
   * @return [飞机列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 9:54
   * @update 2020/9/25 9:54
   */
  public List<AirplaneEntity> getAirplaneFlyList() {
    return new LinkedList<>(airplaneFly.values());
  }
}
