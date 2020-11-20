package com.enuos.live.handle.game.f30021;

import com.enuos.live.pojo.GameRobot;
import com.enuos.live.proto.f30021msg.F30021;
import com.enuos.live.proto.i10001msg.I10001;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.util.Timeout;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;

/**
 * TODO 房间数据.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/9/24 20:29
 */

@Data
@SuppressWarnings("WeakerAccess")
public class AirplaneRoom {
  /** 房间ID. */
  private Long roomId;
  /** 房间类型 [0-双人房间 1-四人房间]. */
  private Integer roomType = 0;
  /** 房间状态 [0-未开始 1-已开始]. */
  private Integer roomStatus = 0;
  /** 当前玩家. */
  private Long nowActionPlayer = 0L;
  /** 开始时间. */
  private LocalDateTime startTimeIndex;
  /** 操作时间. */
  private LocalDateTime actionTimeIndex;
  /** 结束排名. */
  private List<Long> finishRanking = Lists.newArrayList();
  /** 玩家列表. */
  private List<AirplanePlayer> playerList = Lists.newCopyOnWriteArrayList();
  /** 棋盘数据. */
  private HashMap<Integer, AirplaneTrack> trackAirplane = Maps.newHashMap();
  /** 定时数据. */
  private HashMap<Integer, Timeout> timeoutMap = Maps.newHashMap();

  /** 跑道长度. */
  private static final int TRACK_LENGTH = 52;
  /** 跑道标记. */
  private static final int TRACK_INDEX = 100;
  /** 结束标记. */
  private static final int TRACK_FINISH = 6;

  // 初始化棋盘.
  {
    int trackColor = 2;
    int trackIndex = 1;
    for (int i = 1; i < TRACK_LENGTH + 1; i++) {
      AirplaneTrack airplaneTrack = new AirplaneTrack(i);
      airplaneTrack.setTrackColor(trackColor);
      switch (i) {
        case 3:
          airplaneTrack.setTrackZone(0);
          airplaneTrack.setTrackLinkSeat(i + 4);
          break;
        case 7:
          airplaneTrack.setTrackFlight(19);
          airplaneTrack.setTrackLinkSeat(0);
          break;
        case 13:
          airplaneTrack.setTrackLinkSeat(0);
          break;
        case 16:
          airplaneTrack.setTrackZone(0);
          airplaneTrack.setTrackLinkSeat(i + 4);
          break;
        case 20:
          airplaneTrack.setTrackFlight(32);
          airplaneTrack.setTrackLinkSeat(0);
          break;
        case 26:
          airplaneTrack.setTrackLinkSeat(0);
          break;
        case 29:
          airplaneTrack.setTrackZone(0);
          airplaneTrack.setTrackLinkSeat(i + 4);
          break;
        case 33:
          airplaneTrack.setTrackFlight(45);
          airplaneTrack.setTrackLinkSeat(0);
          break;
        case 39:
          airplaneTrack.setTrackLinkSeat(0);
          break;
        case 42:
          airplaneTrack.setTrackZone(0);
          airplaneTrack.setTrackLinkSeat(i + 4);
          break;
        case 46:
          airplaneTrack.setTrackFlight(6);
          airplaneTrack.setTrackLinkSeat(0);
          break;
        case 52:
          airplaneTrack.setTrackLinkSeat(0);
          break;
        default:
          airplaneTrack.setTrackLinkSeat(i + 4);
          break;
      }
      if (i >= 49 && i < 52) {
        airplaneTrack.setTrackLinkSeat(trackIndex);
        trackIndex++;
      }
      trackAirplane.put(i, airplaneTrack);
      if (trackColor == 4) {
        trackColor = 1;
      } else {
        trackColor++;
      }
    }
    int index = 60;
    int color = 1;
    while (index < TRACK_INDEX) {
      for (int i = 0; i < TRACK_FINISH; i++) {
        AirplaneTrack airplaneTrack = new AirplaneTrack(index + i);
        airplaneTrack.setTrackColor(color);
        airplaneTrack.setTrackZone(2);
        trackAirplane.put(airplaneTrack.getTrackId(), airplaneTrack);
      }
      index = index + 10;
      color++;
    }
  }

  /** 飞机颜色. */
  private static HashMap<Integer, Integer> airplaneColor = Maps.newHashMap();
  static {
    airplaneColor.put(1, 1);
    airplaneColor.put(2, 1);
    airplaneColor.put(3, 1);
    airplaneColor.put(4, 1);
    airplaneColor.put(5, 2);
    airplaneColor.put(6, 2);
    airplaneColor.put(7, 2);
    airplaneColor.put(8, 2);
    airplaneColor.put(9, 3);
    airplaneColor.put(10, 3);
    airplaneColor.put(11, 3);
    airplaneColor.put(12, 3);
    airplaneColor.put(13, 4);
    airplaneColor.put(14, 4);
    airplaneColor.put(15, 4);
    airplaneColor.put(16, 4);
  }

  /**
   * TODO 初始房间.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 10:53
   * @update 2020/9/25 10:53
   */
  AirplaneRoom(Long roomId) {
    this.roomId = roomId;
  }

  /**
   * TODO 进入房间.
   *
   * @param channel [通信管道]
   * @param playerInfo [玩家信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 11:19
   * @update 2020/9/25 11:19
   */
  public void enterAirplaneRoom(Channel channel, I10001.PlayerInfo playerInfo) {
    if (Objects.nonNull(playerInfo)) {
      // 房间类型 [0-双人房间 1-四人房间]
      if (roomType == 0) {
        enterAirplaneRoomTypeOne(channel, playerInfo);
      } else {
        enterAirplaneRoomTypeTwo(channel, playerInfo);
      }
    }
  }

  /**
   * TODO 进入房间. 一
   *
   * @param channel [通信管道]
   * @param playerInfo [玩家信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 11:21
   * @update 2020/9/25 11:21
   */
  private void enterAirplaneRoomTypeOne(Channel channel, I10001.PlayerInfo playerInfo) {
    if (Objects.nonNull(playerInfo)) {
      if (CollectionUtils.isNotEmpty(playerList)) {
        AirplanePlayer airplanePlayer = playerList.get(0);
        if (airplanePlayer.getPlayerColor() == 1) {
          AirplanePlayer bluePlayer = new AirplanePlayer(29, 85);
          bluePlayer.setPlayerId(playerInfo.getUserId());
          bluePlayer.setPlayerName(playerInfo.getNickName());
          bluePlayer.setPlayerIcon(playerInfo.getIconUrl());
          bluePlayer.setPlayerSex(playerInfo.getSex());
          bluePlayer.setPlayerColor(3);
          bluePlayer.setChannel(channel);
          bluePlayer.createAirplaneBase(3);
          this.playerList.add(bluePlayer);
        } else {
          AirplanePlayer greenPlayer = new AirplanePlayer(42, 95);
          greenPlayer.setPlayerId(playerInfo.getUserId());
          greenPlayer.setPlayerName(playerInfo.getNickName());
          greenPlayer.setPlayerIcon(playerInfo.getIconUrl());
          greenPlayer.setPlayerSex(playerInfo.getSex());
          greenPlayer.setPlayerColor(4);
          greenPlayer.setChannel(channel);
          greenPlayer.createAirplaneBase(4);
          this.playerList.add(greenPlayer);
        }
      } else {
        boolean color = RandomUtils.nextBoolean();
        if (color) {
          AirplanePlayer redPlayer = new AirplanePlayer(3, 65);
          redPlayer.setPlayerId(playerInfo.getUserId());
          redPlayer.setPlayerName(playerInfo.getNickName());
          redPlayer.setPlayerIcon(playerInfo.getIconUrl());
          redPlayer.setPlayerSex(playerInfo.getSex());
          redPlayer.setPlayerColor(1);
          redPlayer.setChannel(channel);
          redPlayer.createAirplaneBase(1);
          this.playerList.add(redPlayer);
        } else {
          AirplanePlayer yellowPlayer = new AirplanePlayer(16, 75);
          yellowPlayer.setPlayerId(playerInfo.getUserId());
          yellowPlayer.setPlayerName(playerInfo.getNickName());
          yellowPlayer.setPlayerIcon(playerInfo.getIconUrl());
          yellowPlayer.setPlayerSex(playerInfo.getSex());
          yellowPlayer.setPlayerColor(2);
          yellowPlayer.setChannel(channel);
          yellowPlayer.createAirplaneBase(2);
          this.playerList.add(yellowPlayer);
        }
      }
    }
  }

  /**
   * TODO 进入房间. 二
   *
   * @param channel [通信管道]
   * @param playerInfo [玩家信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 11:21
   * @update 2020/9/25 11:21
   */
  private void enterAirplaneRoomTypeTwo(Channel channel, I10001.PlayerInfo playerInfo) {
    if (Objects.nonNull(playerInfo)) {
      if (CollectionUtils.isNotEmpty(playerList)) {
        if (playerList.size() == 1) {
          AirplanePlayer yellowPlayer = new AirplanePlayer(16, 75);
          yellowPlayer.setPlayerId(playerInfo.getUserId());
          yellowPlayer.setPlayerName(playerInfo.getNickName());
          yellowPlayer.setPlayerIcon(playerInfo.getIconUrl());
          yellowPlayer.setPlayerSex(playerInfo.getSex());
          yellowPlayer.setPlayerColor(2);
          yellowPlayer.setChannel(channel);
          yellowPlayer.createAirplaneBase(2);
          this.playerList.add(yellowPlayer);
        } else if (playerList.size() == 2) {
          AirplanePlayer bluePlayer = new AirplanePlayer(29, 85);
          bluePlayer.setPlayerId(playerInfo.getUserId());
          bluePlayer.setPlayerName(playerInfo.getNickName());
          bluePlayer.setPlayerIcon(playerInfo.getIconUrl());
          bluePlayer.setPlayerSex(playerInfo.getSex());
          bluePlayer.setPlayerColor(3);
          bluePlayer.setChannel(channel);
          bluePlayer.createAirplaneBase(3);
          this.playerList.add(bluePlayer);
        } else if (playerList.size() == 3) {
          AirplanePlayer greenPlayer = new AirplanePlayer(42, 95);
          greenPlayer.setPlayerId(playerInfo.getUserId());
          greenPlayer.setPlayerName(playerInfo.getNickName());
          greenPlayer.setPlayerIcon(playerInfo.getIconUrl());
          greenPlayer.setPlayerSex(playerInfo.getSex());
          greenPlayer.setPlayerColor(4);
          greenPlayer.setChannel(channel);
          greenPlayer.createAirplaneBase(4);
          this.playerList.add(greenPlayer);
        }
      } else {
        AirplanePlayer redPlayer = new AirplanePlayer(3, 65);
        redPlayer.setPlayerId(playerInfo.getUserId());
        redPlayer.setPlayerName(playerInfo.getNickName());
        redPlayer.setPlayerIcon(playerInfo.getIconUrl());
        redPlayer.setPlayerSex(playerInfo.getSex());
        redPlayer.setPlayerColor(1);
        redPlayer.setChannel(channel);
        redPlayer.createAirplaneBase(1);
        this.playerList.add(redPlayer);
      }
    }
  }

  /**
   * TODO 开启陪玩. 一
   *
   * @param gameRobot [机器人信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 12:59
   * @update 2020/9/25 12:59
   */
  public void airplaneRoomTypeOneJoinRobot(GameRobot gameRobot) {
    if (Objects.nonNull(gameRobot)) {
      boolean color = RandomUtils.nextBoolean();
      if (color) {
        AirplanePlayer redPlayer = new AirplanePlayer(3, 65);
        redPlayer.setPlayerId(gameRobot.getRobotId());
        redPlayer.setPlayerName(gameRobot.getRobotName());
        redPlayer.setPlayerIcon(gameRobot.getRobotAvatar());
        redPlayer.setPlayerSex(gameRobot.getRobotSex());
        redPlayer.setPlayerColor(1);
        redPlayer.setChannel(null);
        redPlayer.createAirplaneBase(1);
        redPlayer.setIsRobot(0);
        this.playerList.add(redPlayer);
      } else {
        AirplanePlayer yellowPlayer = new AirplanePlayer(16, 75);
        yellowPlayer.setPlayerId(gameRobot.getRobotId());
        yellowPlayer.setPlayerName(gameRobot.getRobotName());
        yellowPlayer.setPlayerIcon(gameRobot.getRobotAvatar());
        yellowPlayer.setPlayerSex(gameRobot.getRobotSex());
        yellowPlayer.setPlayerColor(2);
        yellowPlayer.setChannel(null);
        yellowPlayer.createAirplaneBase(2);
        yellowPlayer.setIsRobot(0);
        this.playerList.add(yellowPlayer);
      }
    }
  }

  /**
   * TODO 开启陪玩. 二
   *
   * @param gameRobot [机器人信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 12:59
   * @update 2020/9/25 12:59
   */
  public void airplaneRoomTypeTwoJoinRobot(GameRobot gameRobot) {
    if (Objects.nonNull(gameRobot)) {
      if (CollectionUtils.isNotEmpty(playerList)) {
        if (playerList.size() == 1) {
          AirplanePlayer yellowPlayer = new AirplanePlayer(16, 75);
          yellowPlayer.setPlayerId(gameRobot.getRobotId());
          yellowPlayer.setPlayerName(gameRobot.getRobotName());
          yellowPlayer.setPlayerIcon(gameRobot.getRobotAvatar());
          yellowPlayer.setPlayerSex(gameRobot.getRobotSex());
          yellowPlayer.setPlayerColor(2);
          yellowPlayer.setChannel(null);
          yellowPlayer.createAirplaneBase(2);
          yellowPlayer.setIsRobot(0);
          this.playerList.add(yellowPlayer);
        } else if (playerList.size() == 2) {
          AirplanePlayer bluePlayer = new AirplanePlayer(29, 85);
          bluePlayer.setPlayerId(gameRobot.getRobotId());
          bluePlayer.setPlayerName(gameRobot.getRobotName());
          bluePlayer.setPlayerIcon(gameRobot.getRobotAvatar());
          bluePlayer.setPlayerSex(gameRobot.getRobotSex());
          bluePlayer.setPlayerColor(3);
          bluePlayer.setChannel(null);
          bluePlayer.createAirplaneBase(3);
          bluePlayer.setIsRobot(0);
          this.playerList.add(bluePlayer);
        }
      } else {
        AirplanePlayer redPlayer = new AirplanePlayer(3, 65);
        redPlayer.setPlayerId(gameRobot.getRobotId());
        redPlayer.setPlayerName(gameRobot.getRobotName());
        redPlayer.setPlayerIcon(gameRobot.getRobotAvatar());
        redPlayer.setPlayerSex(gameRobot.getRobotSex());
        redPlayer.setPlayerColor(1);
        redPlayer.setChannel(null);
        redPlayer.createAirplaneBase(1);
        redPlayer.setIsRobot(0);
        this.playerList.add(redPlayer);
      }
    }
  }

  /**
   * TODO 获取玩家.
   *
   * @param color [目标颜色]
   * @return [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 13:20
   * @update 2020/9/25 13:20
   */
  public long getPlayerIdByColor(Integer color) {
    List<AirplanePlayer> airplanePlayers = playerList;
    AirplanePlayer airplanePlayer = airplanePlayers.stream()
        .filter(player -> Objects.equals(player.getPlayerColor(), color))
        .findAny().orElse(null);
    return Objects.nonNull(airplanePlayer) ? airplanePlayer.getPlayerId() : 0L;
  }

  /**
   * TODO 获取玩家.
   *
   * @param playerId [玩家ID]
   * @return [玩家信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 13:20
   * @update 2020/9/25 13:20
   */
  public AirplanePlayer getPlayerInfoById(Long playerId) {
    List<AirplanePlayer> airplanePlayers = playerList;
    return airplanePlayers.stream()
        // 连接状态 [0-连接中 1-已断开]
        .filter(player -> player.isBoolean(playerId))
        .findAny().orElse(null);
  }

  /**
   * TODO 下一玩家.
   *
   * @param playerId [玩家ID]
   * @return [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/27 4:52
   * @update 2020/9/27 4:52
   */
  public long getNextActionPlayer(Long playerId) {
    List<AirplanePlayer> airplanePlayers = playerList.stream()
        .filter(player -> player.getPlayerId() > 0 && player.getPlayerStatus() == 0)
        .sorted(Comparator.comparing(AirplanePlayer::getPlayerColor))
        .collect(Collectors.toList());
    int playerIndex = 0;
    for (AirplanePlayer airplanePlayer : airplanePlayers) {
      if (airplanePlayer.isBoolean(playerId)) {
        break;
      }
      playerIndex++;
    }
    List<Long> players = Lists.newLinkedList();
    airplanePlayers.forEach(player -> players.add(player.getPlayerId()));
    if (playerIndex == players.size() - 1) {
      return players.get(0);
    } else {
      return players.get(playerIndex + 1);
    }
  }

  /**
   * TODO 设置玩家.
   *
   * @param oldPlayerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 13:40
   * @update 2020/9/25 13:40
   */
  public void setUpActionPlayer(Long oldPlayerId) {
    if (oldPlayerId == 0L) {
      this.playerList.sort(Comparator.comparing(AirplanePlayer::getPlayerColor));
      this.nowActionPlayer = playerList.get(0).getPlayerId();
    } else {
      // 房间类型 [0-双人房间 1-四人房间]
      if (roomType == 0) {
        setUpActionPlayerTypeOne(oldPlayerId);
      } else {
        setUpActionPlayerTypeTwo(oldPlayerId);
      }
    }
  }

  /**
   * TODO 设置玩家. 一
   *
   * @param oldPlayerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 13:41
   * @update 2020/9/25 13:41
   */
  private void setUpActionPlayerTypeOne(Long oldPlayerId) {
    List<AirplanePlayer> airplanePlayers = playerList.stream()
        // 连接状态 [0-连接中 1-已断开]          游戏状态 [0-游戏中 1-已结束 2-已离开(已认输)]
        .filter(player -> player.getLinkStatus() == 0 && player.getPlayerStatus() == 0)
        .sorted(Comparator.comparing(AirplanePlayer::getPlayerColor))
        .collect(Collectors.toList());
    int index = 0;
    for (AirplanePlayer airplanePlayer : airplanePlayers) {
      if (airplanePlayer.isBoolean(oldPlayerId)) {
        break;
      }
      index++;
    }
    if (index == airplanePlayers.size() - 1) {
      this.nowActionPlayer = airplanePlayers.get(0).getPlayerId();
    } else {
      this.nowActionPlayer = airplanePlayers.get(index + 1).getPlayerId();
    }
  }

  /**
   * TODO 设置玩家. 二
   *
   * @param oldPlayerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 13:41
   * @update 2020/9/25 13:41
   */
  private void setUpActionPlayerTypeTwo(Long oldPlayerId) {
    List<AirplanePlayer> airplanePlayers = playerList.stream()
        // 连接状态 [0-连接中 1-已断开]          游戏状态 [0-游戏中 1-已结束 2-已离开(已认输)]
        .filter(player -> player.getLinkStatus() == 0 && player.getPlayerStatus() == 0)
        .sorted(Comparator.comparing(AirplanePlayer::getPlayerColor))
        .collect(Collectors.toList());
    int index = 0;
    for (AirplanePlayer airplanePlayer : airplanePlayers) {
      if (airplanePlayer.isBoolean(oldPlayerId)) {
        break;
      }
      index++;
    }
    if (index == airplanePlayers.size() - 1) {
      this.nowActionPlayer = airplanePlayers.get(0).getPlayerId();
    } else {
      this.nowActionPlayer = airplanePlayers.get(index + 1).getPlayerId();
    }
  }

  /**
   * TODO 机器数量.
   *
   * @return [机器人数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/28 18:18
   * @update 2020/9/28 18:18
   */
  public int getRobotNum() {
    return (int) playerList.stream()
        // 是否机器 [0-是 1-否]
        .filter(player -> player.getIsRobot() == 0).count();
  }

  /**
   * TODO 真实玩家.
   *
   * @return [玩家数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/13 14:08
   * @update 2020/10/13 14:08
   */
  public int getRealPlayerNum() {
    return (int) playerList.stream()
        // 是否机器 [0-是 1-否].
        .filter(player -> player.getIsRobot() == 1
            //  连接状态 [0-连接中 1-已断开]   游戏状态 [0-游戏中 1-已结束 2-已离开(已认输)]
            && player.getLinkStatus() == 0 && player.getPlayerStatus() == 0)
        .count();
  }

  public int getRemainingPlayerNum() {
    return (int) playerList.stream()
        //  连接状态 [0-连接中 1-已断开]   游戏状态 [0-游戏中 1-已结束 2-已离开(已认输)]
        .filter(player -> player.getLinkStatus() == 0 && player.getPlayerStatus() == 0)
        .count();
  }

  public int getWatchPlayerNum() {
    return (int) playerList.stream()
        //  连接状态 [0-连接中 1-已断开]   游戏状态 [0-游戏中 1-已结束 2-已离开(已认输)]
        .filter(player -> player.getLinkStatus() == 0 && player.getPlayerStatus() == 1)
        .count();
  }

  /**
   * TODO 离开房间.
   *
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 13:34
   * @update 2020/9/25 13:34
   */
  public void leaveGameRoom(Long playerId) {
    this.playerList.removeIf(player -> player.isBoolean(playerId));
  }

  /**
   * TODO 飞机信息.
   *
   * @param playerId [玩家ID]
   * @param airplaneId [飞机ID]
   * @return [飞机信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 17:40
   * @update 2020/9/25 17:40
   */
  public AirplaneEntity getAirplaneInfo(Long playerId, Integer airplaneId) {
    AirplanePlayer airplanePlayer = getPlayerInfoById(playerId);
    if (Objects.nonNull(airplanePlayer)) {
      if (airplanePlayer.getAirplaneBase().containsKey(airplaneId)) {
        return airplanePlayer.getAirplaneBase().get(airplaneId);
      } else {
        return airplanePlayer.getAirplaneFly().get(airplaneId);
      }
    }
    return null;
  }

  /**
   * TODO 飞机列表.
   *
   * @param playerId [玩家ID]
   * @return [飞机信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 13:58
   * @update 2020/9/25 13:58
   */
  public F30021.F300212S2C.Builder airplaneFlyList(Long playerId) {
    F30021.F300212S2C.Builder builder = F30021.F300212S2C.newBuilder();
    AirplanePlayer airplanePlayer = getPlayerInfoById(playerId);
    if (Objects.nonNull(airplanePlayer)) {
      List<AirplaneEntity> airplaneEntities = Lists.newLinkedList();
      airplaneEntities.addAll(airplanePlayer.getAirplaneFlyList());
      if (CollectionUtils.isNotEmpty(airplaneEntities)) {
        F30021.AircraftInfo.Builder airplaneBuilder;
        for (AirplaneEntity airplaneEntity : airplaneEntities) {
          airplaneBuilder = F30021.AircraftInfo.newBuilder();
          airplaneBuilder.setAircraftID(airplaneEntity.getAirplaneId());
          airplaneBuilder.setLocalPosition(airplaneEntity.getPositional());
          airplaneBuilder.setIsFly(0);
          builder.addAircraftInfo(airplaneBuilder);
        }
      }
    }
    return builder;
  }

  /**
   * TODO 飞机列表.
   *
   * @param playerId [玩家ID]
   * @return [飞机信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 13:58
   * @update 2020/9/25 13:58
   */
  public F30021.F300212S2C.Builder airplaneAllList(Long playerId) {
    F30021.F300212S2C.Builder builder = F30021.F300212S2C.newBuilder();
    AirplanePlayer airplanePlayer = getPlayerInfoById(playerId);
    if (Objects.nonNull(airplanePlayer)) {
      List<AirplaneEntity> airplaneEntities = Lists.newLinkedList();
      airplaneEntities.addAll(airplanePlayer.getAirplaneBaseList());
      airplaneEntities.addAll(airplanePlayer.getAirplaneFlyList());
      if (CollectionUtils.isNotEmpty(airplaneEntities)) {
        F30021.AircraftInfo.Builder airplaneBuilder;
        for (AirplaneEntity airplaneEntity : airplaneEntities) {
          airplaneBuilder = F30021.AircraftInfo.newBuilder();
          airplaneBuilder.setAircraftID(airplaneEntity.getAirplaneId());
          airplaneBuilder.setLocalPosition(airplaneEntity.getPositional());
          airplaneBuilder.setIsFly(0);
          builder.addAircraftInfo(airplaneBuilder);
        }
      }
    }
    return builder;
  }

  /**
   * TODO 离开位置.
   *
   * @param initPositionalId [初始位置]
   * @param targetPositionalId [目标位置]
   * @param airplaneId [飞机ID]
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 14:10
   * @update 2020/9/25 14:10
   */
  public void leavePositional(Integer initPositionalId, Integer targetPositionalId,
      Integer airplaneId, Long playerId) {
    AirplaneTrack airplaneTrack = trackAirplane.get(initPositionalId);
    List<Integer> airplaneList = airplaneTrack.getAirplaneList();
    if (CollectionUtils.isNotEmpty(airplaneList)) {
      airplaneList.removeIf(airplane -> Objects.equals(airplane, airplaneId));
    }
    this.trackAirplane.put(initPositionalId, airplaneTrack);
    AirplanePlayer airplanePlayer = getPlayerInfoById(playerId);
    // [飞机ID, 位置ID]
    airplanePlayer.updatePositional(airplaneId, targetPositionalId);
    if (airplanePlayer.getLatestDice() < 6) {
      airplanePlayer.setLatestDice(0);
    }
  }

  /**
   * TODO 抵达位置.
   *
   * @param positionalId [位置ID]
   * @param airplaneId [飞机ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 14:16
   * @update 2020/9/25 14:16
   */
  public void arrivedPositional(Integer positionalId, Integer airplaneId) {
    AirplaneTrack airplaneTrack = trackAirplane.get(positionalId);
    List<Integer> airplaneList = airplaneTrack.getAirplaneList();
    airplaneList.add(airplaneId);
    airplaneTrack.setAirplaneList(airplaneList);
    this.trackAirplane.put(positionalId, airplaneTrack);
  }

  /**
   * TODO 抵达终点.
   *
   * @param positionalId [位置ID]
   * @param airplaneId [飞机ID]
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 14:24
   * @update 2020/9/25 14:24
   */
  public void arrivedEndPositional(Integer positionalId, Integer airplaneId, Long playerId) {
    AirplanePlayer airplanePlayer = getPlayerInfoById(playerId);
    if (Objects.nonNull(airplanePlayer)) {
      airplanePlayer.arrivedEndPositional(airplaneId, positionalId);
      if (airplanePlayer.getLatestDice() < 6) {
        airplanePlayer.setLatestDice(0);
      }
    }
  }

  /**
   * TODO 击落飞机.
   *
   * @param positionalId [位置ID]
   * @param airplaneId [飞机ID]
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 14:40
   * @update 2020/9/25 14:40
   */
  public void shootDownAirplane(Integer positionalId, Integer airplaneId, Long playerId) {
    AirplanePlayer airplanePlayer = getPlayerInfoById(playerId);
    if (Objects.nonNull(airplanePlayer)) {
      AirplaneTrack airplaneTrack = trackAirplane.get(positionalId);
      List<Integer> airplaneList = airplaneTrack.getAirplaneList();
      if (CollectionUtils.isNotEmpty(airplaneList)) {
        airplanePlayer.comeBackBase(airplaneList);
        airplaneList.clear();
      }
      airplaneList.add(airplaneId);
      airplaneTrack.setAirplaneList(airplaneList);
      this.trackAirplane.put(positionalId, airplaneTrack);
    }
  }

  /**
   * TODO 终点数量.
   *
   * @param playerId [玩家ID]
   * @return [飞机数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 14:37
   * @update 2020/9/25 14:37
   */
  public int getArrivedEndAirplaneNum(Long playerId) {
    AirplanePlayer airplanePlayer = getPlayerInfoById(playerId);
    if (Objects.nonNull(airplanePlayer)) {
      return airplanePlayer.getArrivedEndAirplaneNum();
    }
    return 0;
  }

  /**
   * TODO 清除飞机.
   *
   * @param airplaneList [飞机列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 11:12
   * @update 2020/9/25 11:12
   */
  public void clearTrackAirplane(List<AirplaneEntity> airplaneList) {
    if (CollectionUtils.isNotEmpty(airplaneList)) {
      airplaneList.stream()
          // 完成飞行 [0-飞行 1-结束]
          .filter(airplane -> airplane.getPositional() > 0 && airplane.getIsFinish() == 0)
          .forEach(airplane -> {
            AirplaneTrack airplaneTrack = trackAirplane.get(airplane.getPositional());
            List<Integer> airplanes = airplaneTrack.getAirplaneList();
            airplanes.removeIf(airplaneId -> Objects.equals(airplaneId, airplane.getAirplaneId()));
            this.trackAirplane.put(airplane.getPositional(), airplaneTrack);
          });
    }
  }

  /**
   * TODO 飞机颜色.
   *
   * @param airplaneId [飞机ID]
   * @return [1-红 2-黄 3-蓝 4-绿]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 11:01
   * @update 2020/9/25 11:01
   */
  public int getAirplaneColor(int airplaneId) {
    return airplaneColor.get(airplaneId);
  }

  /**
   * TODO 添加定时.
   *
   * @param taskId [任务ID].
   * @param timeout [定时任务].
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 10:58
   * @update 2020/9/25 10:58
   */
  public void addTimeOut(int taskId, Timeout timeout) {
    if (timeoutMap.containsKey(taskId)) {
      return;
    }
    this.timeoutMap.put(taskId, timeout);
  }

  /**
   * TODO 取消定时.
   *
   * @param taskId [任务ID].
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 10:58
   * @update 2020/9/25 10:58
   */
  public void cancelTimeout(int taskId) {
    if (timeoutMap.containsKey(taskId)) {
      this.timeoutMap.get(taskId).cancel();
      this.timeoutMap.remove(taskId);
    }
  }

  /**
   * TODO 移除定时.
   *
   * @param taskId [任务ID].
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 10:57
   * @update 2020/9/25 10:57
   */
  public void removeTimeout(int taskId) {
    if (!timeoutMap.containsKey(taskId)) {
      return;
    }
    this.timeoutMap.remove(taskId);
  }

  /**
   * TODO 销毁定时.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 10:55
   * @update 2020/9/25 10:55
   */
  public void destroy() {
    timeoutMap.values().forEach(Timeout::cancel);
    this.timeoutMap.clear();
  }

  /**
   * TODO 分析数据.
   *
   * @param playerId [玩家ID]
   * @return [飞机ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/27 3:26
   * @update 2020/9/27 3:26
   */
  public int explore(Long playerId) {
    AirplanePlayer airplanePlayer = getPlayerInfoById(playerId);
    // 骰值
    Integer diceNum = airplanePlayer.getLatestDice();
    // 玩家颜色 [1-红 2-黄 3-蓝 4-绿]
    Integer playerColor = airplanePlayer.getPlayerColor();
    int colorIndex;
    switch (playerColor) {
      case 1:
        colorIndex = 52;
        break;
      case 2:
        colorIndex = 13;
        break;
      case 3:
        colorIndex = 26;
        break;
      default:
        colorIndex = 39;
        break;
    }
    // 起飞数据
    List<AirplaneEntity> airplaneFlyList = airplanePlayer.getAirplaneFlyList();
    // 基地数据
    List<AirplaneEntity> airplaneBases = airplanePlayer.getAirplaneBaseList();
    airplaneBases = airplaneBases.stream()
        // 完成飞行 [0-飞行 1-结束]
        .filter(airplaneEntity -> airplaneEntity.getIsFinish() == 0)
        .collect(Collectors.toList());
    if (diceNum == 6) {
      // 如果在场只有两架以下的飞机，尽量让飞机从机场起飞；
      if (airplaneFlyList.size() <= 2 && airplaneBases.size() > 0) {
        // 随机选择一架飞机起飞
        return airplaneBases.get(ThreadLocalRandom.current().nextInt(airplaneBases.size())).getAirplaneId();
      } else {
        Integer airplaneId = 0;
        // 0、如果前方是结束区 优先进入结束区
        for (AirplaneEntity airplane : airplaneFlyList) {
          if (Objects.equals(airplane.getPositional(), colorIndex)) {
            airplaneId = airplane.getAirplaneId();
            break;
          }
        }
        if (airplaneId > 0) {
          return airplaneId;
        }
        // 1、优先选择飞棋
        for (AirplaneEntity airplane : airplaneFlyList) {
          int positional = airplane.getPositional() + diceNum;
          // 正常飞行区最大值
          if (positional <= 52) {
            AirplaneTrack track = trackAirplane.get(positional);
            // [1-红 2-黄 3-蓝 4-绿]
            if (playerColor == 1) {
              if (track.getTrackId() == 20) {
                airplaneId = airplane.getAirplaneId();
                break;
              }
            } else if (playerColor == 2) {
              if (track.getTrackId() == 33) {
                airplaneId = airplane.getAirplaneId();
                break;
              }
            } else if (playerColor == 3) {
              if (track.getTrackId() == 46) {
                airplaneId = airplane.getAirplaneId();
                break;
              }
            } else {
              if (track.getTrackId() == 7) {
                airplaneId = airplane.getAirplaneId();
                break;
              }
            }
          }
        }
        if (airplaneId > 0) {
          return airplaneId;
        }
        // 2、优先选择跳棋
        for (AirplaneEntity airplane : airplaneFlyList) {
          // 前进位置
          int positional = airplane.getPositional() + diceNum;
          if (positional > 52) {
            positional = positional - 52;
          }
          // 跑道信息
          AirplaneTrack track = trackAirplane.get(positional);
          if (Objects.equals(playerColor, track.getTrackColor())) {
            airplaneId = airplane.getAirplaneId();
            break;
          }
        }
        if (airplaneId > 0) {
          return airplaneId;
        }
        // 3、优先选择撞机
        for (AirplaneEntity airplane : airplaneFlyList) {
          // 前进位置
          int positional = airplane.getPositional() + diceNum;
          if (positional > 52) {
            positional = positional - 52;
          }
          // 跑道信息
          AirplaneTrack track = trackAirplane.get(positional);
          if (track.getAirplaneList().size() > 0) {
            airplaneId = airplane.getAirplaneId();
            break;
          }
        }
        if (airplaneId > 0) {
          return airplaneId;
        }
        // 0、如果前方是结束区 优先进入结束区
        for (AirplaneEntity airplane : airplaneFlyList) {
          Integer initNum = airplane.getPositional();
          if (initNum >= 60 && initNum < 70) {
            airplaneId = airplane.getAirplaneId();
            break;
          } else if (initNum >= 70 && initNum < 80) {
            airplaneId = airplane.getAirplaneId();
            break;
          } else if (initNum >= 80 && initNum < 90) {
            airplaneId = airplane.getAirplaneId();
            break;
          } else if (initNum >= 90 && initNum < 100) {
            airplaneId = airplane.getAirplaneId();
            break;
          }
        }
        if (airplaneId > 0) {
          return airplaneId;
        }
        return airplaneBases.size() > 0 ? airplaneBases.get(ThreadLocalRandom.current().nextInt(airplaneBases.size())).getAirplaneId()
            : airplaneFlyList.get(ThreadLocalRandom.current().nextInt(airplaneFlyList.size())).getAirplaneId();
        // 4、尽量保持距离
        // 5、判断前方是否有敌机 前进的颜色是否是敌机的颜色
        // 6、判断前方是否有敌机 当前为6 继续 前进
      }
    } else {
      Integer airplaneId = 0;
      // 0、如果前方是结束区 优先进入结束区
      for (AirplaneEntity airplane : airplaneFlyList) {
        if (Objects.equals(airplane.getPositional(), colorIndex)) {
          airplaneId = airplane.getAirplaneId();
          break;
        }
      }
      if (airplaneId > 0) {
        return airplaneId;
      }
      // 1、优先选择飞棋
      for (AirplaneEntity airplane : airplaneFlyList) {
        int positional = airplane.getPositional() + diceNum;
        // 正常飞行区最大值
        if (positional <= 52) {
          AirplaneTrack track = trackAirplane.get(positional);
          // [1-红 2-黄 3-蓝 4-绿]
          if (playerColor == 1) {
            if (track.getTrackId() == 20) {
              airplaneId = airplane.getAirplaneId();
              break;
            }
          } else if (playerColor == 2) {
            if (track.getTrackId() == 33) {
              airplaneId = airplane.getAirplaneId();
              break;
            }
          } else if (playerColor == 3) {
            if (track.getTrackId() == 46) {
              airplaneId = airplane.getAirplaneId();
              break;
            }
          } else {
            if (track.getTrackId() == 7) {
              airplaneId = airplane.getAirplaneId();
              break;
            }
          }
        }
      }
      if (airplaneId > 0) {
        return airplaneId;
      }
      // 2、优先选择跳棋
      for (AirplaneEntity airplane : airplaneFlyList) {
        // 前进位置
        int positional = airplane.getPositional() + diceNum;
        if (positional > 52) {
          positional = positional - 52;
        }
        // 跑道信息
        AirplaneTrack track = trackAirplane.get(positional);
        if (Objects.equals(playerColor, track.getTrackColor())) {
          airplaneId = airplane.getAirplaneId();
          break;
        }
      }
      if (airplaneId > 0) {
        return airplaneId;
      }
      // 3、优先选择撞机
      for (AirplaneEntity airplane : airplaneFlyList) {
        // 前进位置
        int positional = airplane.getPositional() + diceNum;
        if (positional > 52) {
          positional = positional - 52;
        }
        // 跑道信息
        AirplaneTrack track = trackAirplane.get(positional);
        if (track.getAirplaneList().size() > 0) {
          airplaneId = airplane.getAirplaneId();
          break;
        }
      }
      if (airplaneId > 0) {
        return airplaneId;
      }
      // 结束区飞行
      for (AirplaneEntity airplane : airplaneFlyList) {
        Integer initNum = airplane.getPositional();
        if (initNum >= 60 && initNum < 70) {
          airplaneId = airplane.getAirplaneId();
          break;
        } else if (initNum >= 70 && initNum < 80) {
          airplaneId = airplane.getAirplaneId();
          break;
        } else if (initNum >= 80 && initNum < 90) {
          airplaneId = airplane.getAirplaneId();
          break;
        } else if (initNum >= 90 && initNum < 100) {
          airplaneId = airplane.getAirplaneId();
          break;
        }
      }
      if (airplaneId > 0) {
        return airplaneId;
      }
      // 4、尽量保持距离
      // 5、判断前方是否有敌机 前进的颜色是否是敌机的颜色
      // 6、判断前方是否有敌机 当前>5 继续 前进
    }
    return airplaneFlyList.get(ThreadLocalRandom.current().nextInt(airplaneFlyList.size())).getAirplaneId();
  }

}
