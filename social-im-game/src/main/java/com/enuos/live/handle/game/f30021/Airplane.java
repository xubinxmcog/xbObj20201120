package com.enuos.live.handle.game.f30021;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.utils.annotation.AbstractAction;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import com.enuos.live.channel.SoftChannel;
import com.enuos.live.codec.Packet;
import com.enuos.live.constants.GameKey;
import com.enuos.live.pojo.GameRobot;
import com.enuos.live.manager.AchievementEnum;
import com.enuos.live.manager.ActivityEnum;
import com.enuos.live.manager.TaskEnum;
import com.enuos.live.proto.f30021msg.F30021;
import com.enuos.live.proto.f30021msg.F30021.AircraftInfo;
import com.enuos.live.proto.f30021msg.F30021.KillInfo;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.rest.ActivityRemote;
import com.enuos.live.rest.GameRemote;
import com.enuos.live.rest.OrderRemote;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.task.TimerEventLoop;
import com.enuos.live.manager.ChatManager;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.manager.GroupManager;
import com.enuos.live.manager.MemManager;
import com.enuos.live.manager.RobotManager;
import com.enuos.live.utils.StringUtils;
import com.enuos.live.utils.JsonUtils;
import com.enuos.live.manager.LoggerManager;
import com.enuos.live.utils.RedisUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.util.Timeout;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * TODO 飞空战棋.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/9/24 20:26
 */

@Component
@AbstractAction(cmd = ActionCmd.GAME_AEROPLANE)
public class Airplane extends AbstractActionHandler {

  /** 房间游戏数据. */
  private static ConcurrentHashMap<Long, AirplaneRoom> GAME_DATA = new ConcurrentHashMap<>();

  /** Feign调用. */
  @Resource
  private GameRemote gameRemote;
  @Resource
  private UserRemote userRemote;
  @Resource
  private OrderRemote orderRemote;
  @Resource
  private ActivityRemote activityRemote;
  @Resource
  private RedisUtils redis;

  /**
   * TODO 操作处理.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/24 20:26
   * @update 2020/9/24 20:26
   */
  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      switch (packet.child) {
        case AirplaneCmd.ENTER_ROOM:
          enterRoom(channel, packet);
          break;
        case AirplaneCmd.START_DICE:
          startDice(channel, packet);
          break;
        case AirplaneCmd.MOVES_CHESS:
          movesChess(channel, packet);
          break;
        case AirplaneCmd.ADMITS_DEFEAT:
          admitsDefeat(channel, packet);
          break;
        case AirplaneCmd.PLAYER_EXIT:
          playerExit(channel, packet);
          break;
        default:
          LoggerManager.warn("[GAME 30021 HANDLE] CHILD ERROR: [{}]", packet.child);
          break;
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 关闭处理.
   *
   * @param userId [用户ID]
   * @param attachId [附属ID(或[房间ID])]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/24 20:26
   * @update 2020/9/24 20:26
   */
  @Override
  public void shutOff(Long userId, Long attachId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(attachId);
      if (Objects.nonNull(airplaneRoom)) {
        AirplanePlayer isPlayer = airplaneRoom.getPlayerInfoById(userId);
        if (Objects.nonNull(isPlayer)) {
          //  房间类型 [0-双人房间 1-四人房间]
          if (airplaneRoom.getRoomType() == 0) {
            playerExitRoomById0(attachId, userId);
          } else {
            if (isPlayer.getPlayerStatus() == 0) {
              playerExitRoomById1(attachId, userId);
            }
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 清除处理.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/24 20:26
   * @update 2020/9/24 20:26
   */
  @Override
  public void cleaning(Long roomId) {
    try {
      GAME_DATA.remove(roomId);
      this.gameRemote.deleteRoom(roomId);
      ChatManager.delChatGroup(roomId);
      GroupManager.delRoomGroup(roomId);
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 陪玩处理.
   *
   * @param roomId [房间ID]
   * @param playerIds [机器人信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/24 20:26
   * @update 2020/9/24 20:26
   */
  @Override
  public void joinRobot(Long roomId, List<Long> playerIds) {
    try {
      register(TimerEventLoop.timeGroup.next());
      GAME_DATA.computeIfAbsent(roomId, key -> new AirplaneRoom(roomId));
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      byte[] roomByte = this.redis.getByte(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
      I10001.RoomRecord roomRecord = I10001.RoomRecord.parseFrom(roomByte);
      // 房间类型 [0-双人房间 1-四人房间]
      airplaneRoom.setRoomType(roomRecord.getRoomType());
      if (airplaneRoom.getRoomType() == 0) {
        long playerId = playerIds.get(0);
        GameRobot gameRobot = RobotManager.getGameRobot(playerId);
        airplaneRoom.airplaneRoomTypeOneJoinRobot(gameRobot);
      } else {
        for (Long playerId : playerIds) {
          GameRobot gameRobot = RobotManager.getGameRobot(playerId);
          airplaneRoom.airplaneRoomTypeTwoJoinRobot(gameRobot);
        }
      }
      // 添加等待定时
      waitTimeout(roomId);
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 房间信息.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:16
   * @update 2020/9/25 16:16
   */
  private void enterRoom(Channel channel, Packet packet) {
    try {
      boolean isTestUser = this.redis.hasKey(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
      boolean isPlayer = this.redis.hasKey(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
      if (isTestUser || isPlayer) {
        closeLoading(packet.userId);
        boolean checkRoom = this.redis.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + packet.roomId);
        boolean checkTest = (packet.roomId == AirplaneAssets.getLong(AirplaneAssets.TEST_ID));
        if (checkRoom || checkTest) {
          AirplaneRoom airplaneRoom = GAME_DATA.get(packet.roomId);
          if (Objects.nonNull(airplaneRoom)) {
            AirplanePlayer airplanePlayer = airplaneRoom.getPlayerInfoById(packet.userId);
            // 房间状态 [0-未开始 1-已开始].
            if (airplaneRoom.getRoomStatus() == 1) {
              if (Objects.nonNull(airplanePlayer)) {
                // 连接状态 [0-连接中 1-已断开]
                if (airplanePlayer.getLinkStatus() == 1) {
                  playerNotExist(channel);
                  return;
                } else {
                  airplanePlayer.setChannel(channel);
                  refreshData(channel, packet);
                  disconnected(channel, packet);
                  return;
                }
              } else {
                playerNotExist(channel);
                return;
              }
            } else {
              if (Objects.nonNull(airplanePlayer)) {
                airplanePlayer.setChannel(channel);
                refreshData(channel, packet);
              } else {
                if (airplaneRoom.getTimeoutMap().containsKey((int) AirplaneCmd.WAIT_TIME)) {
                  airplaneRoom.cancelTimeout((int) AirplaneCmd.WAIT_TIME);
                }
                refreshData(channel, packet);
                byte[] bytes;
                if (isTestUser) {
                  bytes = this.redis.getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
                } else {
                  bytes = this.redis.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
                }
                I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
                airplaneRoom.enterAirplaneRoom(channel, playerInfo);
                pullDecorateInfo(packet);
                joinAirplaneRoom(packet);
              }
            }
          } else {
            register(TimerEventLoop.timeGroup.next());
            GAME_DATA.computeIfAbsent(packet.roomId, key -> new AirplaneRoom(packet.roomId));
            airplaneRoom = GAME_DATA.get(packet.roomId);
            if (checkTest) {
              airplaneRoom.setRoomType(1);
            } else {
              byte[] roomByte = this.redis.getByte(GameKey.KEY_GAME_ROOM_RECORD.getName() + packet.roomId);
              I10001.RoomRecord roomRecord = I10001.RoomRecord.parseFrom(roomByte);
              airplaneRoom.setRoomType(roomRecord.getRoomType());
            }
            refreshData(channel, packet);
            byte[] bytes;
            if (isTestUser) {
              bytes = this.redis.getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
            } else {
              bytes = this.redis.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
            }
            I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
            airplaneRoom.enterAirplaneRoom(channel, playerInfo);
            pullDecorateInfo(packet);
            joinAirplaneRoom(packet);
          }
          pushPlayerInfo(channel, packet);
        } else {
          roomNotExist(channel);
        }
      } else {
        playerNotExist(channel);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 玩家信息.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:38
   * @update 2020/9/25 16:38
   */
  private void pushPlayerInfo(Channel channel, Packet packet) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(airplaneRoom)) {
        F30021.F30021S2C.Builder response = F30021.F30021S2C.newBuilder();
        List<AirplanePlayer> playerList = airplaneRoom.getPlayerList();
        if (CollectionUtils.isNotEmpty(playerList)) {
          F30021.PlayerInfo.Builder playerInfo;
          for (AirplanePlayer airplanePlayer : playerList) {
            playerInfo = F30021.PlayerInfo.newBuilder();
            playerInfo.setNick(airplanePlayer.getPlayerName());
            playerInfo.setUserID(airplanePlayer.getPlayerId());
            playerInfo.setUrl(airplanePlayer.getPlayerIcon());
            playerInfo.setSex(airplanePlayer.getPlayerSex());
            playerInfo.setRed(airplanePlayer.getPlayerColor());
            playerInfo.setPlayerStatus(airplanePlayer.getPlayerStatus());
            List<AirplaneEntity> aircraftEntities = airplanePlayer.getAirplaneBaseList();
            aircraftEntities.sort(Comparator.comparing(AirplaneEntity::getAirplaneId));
            F30021.MAircraftInfo.Builder aircraftBuilder;
            for (AirplaneEntity aircraft : aircraftEntities) {
              aircraftBuilder = F30021.MAircraftInfo.newBuilder();
              aircraftBuilder.setAircraftID(aircraft.getAirplaneId());
              aircraftBuilder.setNewPosition(0);
              aircraftBuilder.setIsFinish(0);
              playerInfo.addAircraftInfo(aircraftBuilder);
            }
            if (StringUtils.isNotEmpty(airplanePlayer.getAirplaneSkin())) {
              F30021.GameStyle.Builder gameStyle = F30021.GameStyle.newBuilder();
              gameStyle.setAircraftStyle(airplanePlayer.getAirplaneSkin());
              playerInfo.setGameStyle(gameStyle);
            }
            response.addPlayer(playerInfo);
          }
          response.setResult(0).setRoomType(airplaneRoom.getRoomType());
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.ENTER_ROOM,
                  response.build().toByteArray()), airplaneRoom.getRoomId());
          // 房间类型 [0-双人房间 1-四人房间]
          if (airplaneRoom.getRoomType() == 0) {
            if (playerList.size() == 1) {
              waitTimeout(airplaneRoom.getRoomId());
            } else {
              startTimeout(airplaneRoom.getRoomId());
            }
          } else {
            if (playerList.size() >= 1 && playerList.size() < 4) {
              waitTimeout(airplaneRoom.getRoomId());
            } else {
              startTimeout(airplaneRoom.getRoomId());
            }
          }
        } else {
          playerNotExist(channel);
        }
      } else {
        roomNotExist(channel);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 开始掷骰.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:17
   * @update 2020/9/25 16:17
   */
  private void startDice(Channel channel, Packet packet) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(airplaneRoom)) {
        AirplanePlayer isPlayer = airplaneRoom.getPlayerInfoById(packet.userId);
        if (Objects.nonNull(isPlayer)) {
          if (Objects.equals(airplaneRoom.getNowActionPlayer(), packet.userId)) {
            if (isPlayer.getLatestDice() == 0 || isPlayer.getLatestDice() == 6) {
              // 销毁托管定时
              airplaneRoom.cancelTimeout(AirplaneCmd.AUTO_TIME);
              // 销毁操作定时
              airplaneRoom.cancelTimeout(AirplaneCmd.ACTION_TIME);
              // 骰子数值
              int diceNumber = ThreadLocalRandom.current().nextInt(6) + 1;
              AirplanePlayer airplanePlayer = airplaneRoom.getPlayerInfoById(packet.userId);
              // 开启托管 [0-关闭 1-开启]
              airplanePlayer.setStartEscrow(0);
              // 托管次数
              airplanePlayer.setTrusteeship(0);
              airplanePlayer.setLatestDice(diceNumber);
              F30021.F300212S2C.Builder response = F30021.F300212S2C.newBuilder();
              response.setDiceNum(diceNumber);
              response.setActionUserId(packet.userId);
              if (diceNumber == 6) {
                F30021.F300212S2C.Builder airplaneBuilder = airplaneRoom
                    .airplaneAllList(packet.userId);
                response.addAllAircraftInfo(airplaneBuilder.getAircraftInfoList());
                response.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
                response.setNextUserID(packet.userId);
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.START_DICE,
                        response.build().toByteArray()), airplaneRoom.getRoomId());
                airplanePlayer.setPlayActionIndex(0);
                // 添加操作定时
                actionTimeout(airplaneRoom.getRoomId());
              } else {
                F30021.F300212S2C.Builder airplaneBuilder = airplaneRoom
                    .airplaneFlyList(packet.userId);
                if (CollectionUtils.isNotEmpty(airplaneBuilder.getAircraftInfoList())) {
                  response.addAllAircraftInfo(airplaneBuilder.getAircraftInfoList());
                  response.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
                  response.setNextUserID(packet.userId);
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.START_DICE,
                          response.build().toByteArray()), airplaneRoom.getRoomId());
                  airplanePlayer.setPlayActionIndex(0);
                  // 添加操作定时
                  actionTimeout(airplaneRoom.getRoomId());
                } else {
                  airplanePlayer.setLatestDice(0);
                  airplaneRoom.setUpActionPlayer(packet.userId);
                  response.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
                  response.setNextUserID(airplaneRoom.getNowActionPlayer());
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.START_DICE,
                          response.build().toByteArray()), airplaneRoom.getRoomId());
                  AirplanePlayer nextPlayer = airplaneRoom
                      .getPlayerInfoById(response.getNextUserID());
                  // 是否机器 [0-是 1-否]
                  if (nextPlayer.getIsRobot() == 0) {
                    robotStartDiceTimeout(airplaneRoom.getRoomId());
                  } else {
                    // 开启托管 [0-关闭 1-开启]
                    if (nextPlayer.getStartEscrow() == 1) {
                      autoStartDiceTimeout(airplaneRoom.getRoomId());
                    } else {
                      // 添加操作定时
                      actionTimeout(airplaneRoom.getRoomId());
                    }
                  }
                }
              }
            }
          }
        } else {
          playerNotExist(channel);
        }
      } else {
        roomNotExist(channel);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 移动飞机.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:17
   * @update 2020/9/25 16:17
   */
  private void movesChess(Channel channel, Packet packet) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(airplaneRoom)) {
        AirplanePlayer isPlayer = airplaneRoom.getPlayerInfoById(packet.userId);
        if (Objects.nonNull(isPlayer)) {
          if (Objects.equals(airplaneRoom.getNowActionPlayer(), packet.userId)) {
            if (isPlayer.getPlayActionIndex() == 0) {
              isPlayer.setPlayActionIndex(1);
              // 销毁托管定时
              airplaneRoom.cancelTimeout(AirplaneCmd.AUTO_TIME);
              // 销毁操作定时
              airplaneRoom.cancelTimeout(AirplaneCmd.ACTION_TIME);
              F30021.F300213C2S request = F30021.F300213C2S.parseFrom(packet.bytes);
              AirplaneEntity airplane = airplaneRoom.getAirplaneInfo(packet.userId, request.getAircraftID());
              if (Objects.nonNull(airplane)) {
                Integer initNumber = airplane.getPositional();
                if (!Objects.equals(initNumber, isPlayer.getFinishPositional())) {
                  int diceNumber = isPlayer.getLatestDice();
                  if (diceNumber > 0) {
                    int endNumber = initNumber + diceNumber;
                    FlightProcess flightProcess = new FlightProcess();
                    flightProcess.setRoomId(packet.roomId);
                    flightProcess.setPlayerId(packet.userId);
                    flightProcess.setInitValue(initNumber);
                    flightProcess.setEndValue(endNumber);
                    flightProcess.setAirplaneId(request.getAircraftID());
                    if (initNumber >= 60 && initNumber < 70) {
                      flightProcess.setMaxValue(65);
                      goInEndZone(flightProcess);
                    } else if (initNumber >= 70 && initNumber < 80) {
                      flightProcess.setMaxValue(75);
                      goInEndZone(flightProcess);
                    } else if (initNumber >= 80 && initNumber < 90) {
                      flightProcess.setMaxValue(85);
                      goInEndZone(flightProcess);
                    } else if (initNumber >= 90 && initNumber < 100) {
                      flightProcess.setMaxValue(95);
                      goInEndZone(flightProcess);
                    } else {
                      flightProcess.setDiceValue(diceNumber);
                      goInFlyZone(flightProcess, airplane);
                    }
                  }
                }
              }
            }
          }
        } else {
          playerNotExist(channel);
        }
      } else {
        roomNotExist(channel);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 终点区域.
   *
   * @param process [飞行过程]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 10:45
   * @update 2020/10/14 10:45
   */
  private void goInEndZone(FlightProcess process) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(process.getRoomId());
      if (Objects.nonNull(airplaneRoom)) {
        // 玩家信息
        AirplanePlayer airplanePlayer = airplaneRoom.getPlayerInfoById(process.getPlayerId());
        // 更新成就标记
        airplanePlayer.setRegionalCircle(airplanePlayer.getRegionalCircle() + 1);
        // 飞行步骤
        List<Integer> walkingList = terminusWalkList(process.getInitValue(), process.getEndValue(), process.getMaxValue());
        Integer positionalId = walkingList.get(walkingList.size() - 1);
        // 更新位置 同时 更新骰值
        airplaneRoom.leavePositional(process.getInitValue(), positionalId, process.getAirplaneId(), process.getPlayerId());
        F30021.AircraftInfo.Builder airplaneBuilder = F30021.AircraftInfo.newBuilder();
        if (Objects.equals(positionalId, process.getMaxValue())) {
          // 到达终点
          airplaneRoom.arrivedEndPositional(positionalId, process.getAirplaneId(), process.getPlayerId());
          // 该飞机飞行任务结束
          airplaneBuilder.setIsFinish(1);
        } else {
          // 到达位置
          airplaneRoom.arrivedPositional(positionalId, process.getAirplaneId());
        }
        airplaneBuilder.setAircraftID(process.getAirplaneId());
        airplaneBuilder.setLocalPosition(process.getInitValue());
        airplaneBuilder.setIsFly(0);
        F30021.KillInfo.Builder killBuilder;
        for (Integer integer : walkingList) {
          killBuilder = F30021.KillInfo.newBuilder();
          killBuilder.setStartFlyPosi(integer);
          airplaneBuilder.addKillInfo(killBuilder);
        }
        F30021.F300213S2C.Builder response = F30021.F300213S2C.newBuilder();
        // 当前玩家已结束飞行
        if (airplaneRoom.getArrivedEndAirplaneNum(process.getPlayerId()) == 4) {
          // 房间类型 [0-双人房间 1-四人房间]
          if (airplaneRoom.getRoomType() == 0) {
            airplaneRoom.destroy();
            response.setActionUserId(process.getPlayerId());
            response.setMoveAircraftInfo(airplaneBuilder);
            response.setNextUserID(0);
            response.setActionTime(0);
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.MOVES_CHESS,
                    response.build().toByteArray()), airplaneRoom.getRoomId());
            // 延时1s显示结算
            gameFinish0(process.getRoomId(), process.getPlayerId());
            return;
          } else {
            // 加入排名
            airplaneRoom.getFinishRanking().add(process.getPlayerId());
            switch (airplaneRoom.getFinishRanking().size()) {
              case 1:
                if (MemManager.isExists(process.getPlayerId())) {
                  gainExperience(process.getPlayerId(), 12, 15);
                } else {
                  gainExperience(process.getPlayerId(), 6, 15);
                }
                break;
              case 2:
                if (MemManager.isExists(process.getPlayerId())) {
                  gainExperience(process.getPlayerId(), 8, 10);
                } else {
                  gainExperience(process.getPlayerId(), 4, 10);
                }
                break;
              default:
                if (MemManager.isExists(process.getPlayerId())) {
                  gainExperience(process.getPlayerId(), 4, 5);
                } else {
                  gainExperience(process.getPlayerId(), 2, 5);
                }
                break;
            }
            List<AirplanePlayer> airplanePlayers = airplaneRoom.getPlayerList();
            airplanePlayers = airplanePlayers.stream()
                // 连接状态 [0-连接中 1-已断开]           游戏状态 [0-游戏中 1-已结束 2-已离开(已认输)]
                .filter(player -> player.getLinkStatus() == 0 && (player.getPlayerStatus() == 0 || player.getPlayerStatus() == 1))
                .collect(Collectors.toList());
            if (airplaneRoom.getFinishRanking().size() == (airplanePlayers.size() - 1)) {
              // 游戏完全结束
              airplaneRoom.destroy();
              response.setActionUserId(process.getPlayerId());
              response.setMoveAircraftInfo(airplaneBuilder);
              response.setNextUserID(0);
              response.setActionTime(0);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.MOVES_CHESS,
                      response.build().toByteArray()), airplaneRoom.getRoomId());
              // 延时1s显示结算
              gameFinish1(process.getRoomId());
              return;
            } else {
              // 设置玩家
              airplaneRoom.setUpActionPlayer(process.getPlayerId());
              airplanePlayer.setPlayerStatus(1);
              airplanePlayer.setFinishIndex(1);
              response.setActionUserId(process.getPlayerId());
              response.setMoveAircraftInfo(airplaneBuilder);
              response.setNextUserID(airplaneRoom.getNowActionPlayer());
              response.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.MOVES_CHESS,
                      response.build().toByteArray()), airplaneRoom.getRoomId());
            }
          }
        } else {
          if (airplanePlayer.getLatestDice() < 6) {
            airplaneRoom.setUpActionPlayer(process.getPlayerId());
          }
          response.setActionUserId(process.getPlayerId());
          response.setMoveAircraftInfo(airplaneBuilder);
          response.setNextUserID(airplaneRoom.getNowActionPlayer());
          response.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.MOVES_CHESS,
                  response.build().toByteArray()), airplaneRoom.getRoomId());
        }
        AirplanePlayer nextPlayer = airplaneRoom.getPlayerInfoById(response.getNextUserID());
        // 是否机器 [0-是 1-否]
        if (nextPlayer.getIsRobot() == 0) {
          robotStartDiceTimeout(airplaneRoom.getRoomId());
        } else {
          // 开启托管 [0-关闭 1-开启]
          if (nextPlayer.getStartEscrow() == 1) {
            autoStartDiceTimeout(airplaneRoom.getRoomId());
          } else {
            // 添加操作定时
            actionTimeout(airplaneRoom.getRoomId());
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 飞行区域.
   *
   * @param process [飞行过程]
   * @param airplane [飞机信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 12:45
   * @update 2020/10/14 12:45
   */
  private void goInFlyZone(FlightProcess process, AirplaneEntity airplane) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(process.getRoomId());
      if (Objects.nonNull(airplaneRoom)) {
        AirplanePlayer airplanePlayer = airplaneRoom.getPlayerInfoById(process.getPlayerId());
        int diceNumber = airplanePlayer.getLatestDice();
        if (diceNumber > 0) {
          if (diceNumber == 6) {
            // 完成飞行 [0-飞行 1-结束]
            if (airplane.getIsFinish() == 0) {
              // 当前位置
              if (airplane.getPositional() == 0) {
                // 开始起飞
                startToTakeOff(process);
              } else {
                // 飞呀飞呀
                flightProcess(process);
              }
            }
          } else {
            // 已起飞的飞机  判断飞机是否存在??????
            if (airplanePlayer.getAirplaneFly().containsKey(airplane.getAirplaneId())) {
              // 飞呀飞呀
              flightProcess(process);
            }
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 开始起飞.
   *
   * @param process [飞行过程]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 11:23
   * @update 2020/10/14 11:23
   */
  private void startToTakeOff(FlightProcess process) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(process.getRoomId());
      if (Objects.nonNull(airplaneRoom)) {
        AirplanePlayer airplanePlayer = airplaneRoom.getPlayerInfoById(process.getPlayerId());
        airplanePlayer.airplaneStartFly(process.getAirplaneId());
        airplanePlayer.setLatestDice(0);
        F30021.F300213S2C.Builder response = F30021.F300213S2C.newBuilder();
        response.setActionUserId(process.getPlayerId());
        F30021.AircraftInfo.Builder airplaneBuilder = F30021.AircraftInfo.newBuilder();
        airplaneBuilder.setAircraftID(process.getAirplaneId());
        airplaneBuilder.setLocalPosition(process.getInitValue());
        airplaneBuilder.setIsFly(0);
        F30021.KillInfo.Builder killBuilder = F30021.KillInfo.newBuilder();
        killBuilder.setStartFlyPosi(airplanePlayer.getStartPositional());
        airplaneBuilder.addKillInfo(killBuilder);
        response.setMoveAircraftInfo(airplaneBuilder);
        response.setNextUserID(airplaneRoom.getNowActionPlayer());
        response.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.MOVES_CHESS,
                response.build().toByteArray()), airplaneRoom.getRoomId());
        AirplanePlayer nextPlayer = airplaneRoom.getPlayerInfoById(response.getNextUserID());
        // 是否机器 [0-是 1-否]
        if (nextPlayer.getIsRobot() == 0) {
          robotStartDiceTimeout(airplaneRoom.getRoomId());
        } else {
          // 开启托管 [0-关闭 1-开启]
          if (nextPlayer.getStartEscrow() == 1) {
            autoStartDiceTimeout(airplaneRoom.getRoomId());
          } else {
            // 添加操作定时
            actionTimeout(airplaneRoom.getRoomId());
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 飞行过程.
   *
   * @param process [飞行过程]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/27 9:47
   * @update 2020/9/27 9:47
   */
  private void flightProcess(FlightProcess process) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(process.getRoomId());
      if (Objects.nonNull(airplaneRoom)) {
        AirplanePlayer player = airplaneRoom.getPlayerInfoById(process.getPlayerId());
        List<Integer> walkingList = flyWalkingList(process, player.getPlayerColor());
        // 最后的位置
        int positional = walkingList.get(walkingList.size() - 1);
        AirplaneTrack airplaneTrack = airplaneRoom.getTrackAirplane().get(positional);
        process.setWalkingList(walkingList);
        // 跑道区域 [0-起飞区 1-飞行区 2-结束区].
        switch (airplaneTrack.getTrackZone()) {
          case 0:
            //〖安全区〗...〖校验颜色〗---»「飞棋」
            safeZoneProcess(process);
            break;
          case 1:
            //〖飞行区〗...〖校验颜色〗---»「跳棋/飞棋」
            flightZoneProcess(process);
            break;
          default:
            //〖结束区〗
            process.setIsFlyAction(0);
            endZoneProcess(process);
            break;
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 安全流程.
   *
   * @param process [飞行过程]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 18:48
   * @update 2020/9/25 18:48
   */
  private void safeZoneProcess(FlightProcess process) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(process.getRoomId());
      if (Objects.nonNull(airplaneRoom)) {
        int positional = process.getWalkingList().get(process.getWalkingList().size() - 1);
        AirplaneTrack airplaneTrack = airplaneRoom.getTrackAirplane().get(positional);
        int airplaneColor = airplaneRoom.getAirplaneColor(process.getAirplaneId());
        // 颜色相同 跳棋
        if (Objects.equals(airplaneTrack.getTrackColor(), airplaneColor)) {
          F30021.AircraftInfo.Builder airplaneBuilder = F30021.AircraftInfo.newBuilder();
          airplaneBuilder.setIsFly(0);
          airplaneBuilder.setAircraftID(0);
          airplaneBuilder.setLocalPosition(0);
          F30021.KillInfo.Builder killInfo;
          for (Integer integer : process.getWalkingList()) {
            killInfo = F30021.KillInfo.newBuilder();
            killInfo.setStartFlyPosi(integer);
            airplaneBuilder.addKillInfo(killInfo);
          }
          // 当前位置的关联值
          int trackLink = airplaneTrack.getTrackLinkSeat();
          process.getWalkingList().add(trackLink);
          airplaneTrack = airplaneRoom.getTrackAirplane().get(trackLink);
          // 判断颜色 属于自己的跳跃区 如果有其他飞机 干掉
          List<Integer> airplaneList = airplaneTrack.getAirplaneList();
          if (CollectionUtils.isNotEmpty(airplaneList)) {
            int airColor = airplaneRoom.getAirplaneColor(airplaneList.get(0));
            if (airColor != airplaneColor) {
              killInfo = F30021.KillInfo.newBuilder();
              killInfo.setStartFlyPosi(trackLink);
              // 是否击落对方
              killInfo.setIsBeatRival(true);
              // 被击落玩家ID
              long killUser = airplaneRoom.getPlayerIdByColor(airColor);
              killInfo.setBeatPosition(killUser);
              // 被击落玩家飞机ID
              killInfo.addAllBeatAircraID(airplaneList);
              airplaneBuilder.addKillInfo(killInfo);
            } else {
              killInfo = F30021.KillInfo.newBuilder();
              killInfo.setStartFlyPosi(trackLink);
              airplaneBuilder.addKillInfo(killInfo);
            }
          } else {
            killInfo = F30021.KillInfo.newBuilder();
            killInfo.setStartFlyPosi(trackLink);
            airplaneBuilder.addKillInfo(killInfo);
          }
          int trackFlight = airplaneTrack.getTrackFlight();
          process.getWalkingList().add(trackFlight);
          airplaneTrack = airplaneRoom.getTrackAirplane().get(trackFlight);
          List<Integer> airplaneEndList = airplaneTrack.getAirplaneList();
          if (CollectionUtils.isNotEmpty(airplaneEndList)) {
            int airColor = airplaneRoom.getAirplaneColor(airplaneEndList.get(0));
            if (airColor != airplaneColor) {
              killInfo = F30021.KillInfo.newBuilder();
              killInfo.setStartFlyPosi(trackFlight);
              // 是否击落对方
              killInfo.setIsBeatRival(true);
              // 被击落玩家ID
              long killUser = airplaneRoom.getPlayerIdByColor(airColor);
              killInfo.setBeatPosition(killUser);
              // 被击落玩家飞机ID
              killInfo.addAllBeatAircraID(airplaneEndList);
              airplaneBuilder.addKillInfo(killInfo);
            } else {
              killInfo = F30021.KillInfo.newBuilder();
              killInfo.setStartFlyPosi(trackFlight);
              airplaneBuilder.addKillInfo(killInfo);
            }
          } else {
            killInfo = F30021.KillInfo.newBuilder();
            killInfo.setStartFlyPosi(trackFlight);
            airplaneBuilder.addKillInfo(killInfo);
          }
          byte[] airplaneByte = airplaneBuilder.build().toByteArray();
          if (airplaneList.size() >= 1 || airplaneEndList.size() >= 1) {
            // 玩家成就 穿越虫洞
            Map<String, Object> taskSuc0011 = Maps.newHashMap();
            taskSuc0011.put("userId", process.getPlayerId());
            taskSuc0011.put("code", AchievementEnum.AMT0011.getCode());
            taskSuc0011.put("progress", 1);
            taskSuc0011.put("isReset", 0);
            this.userRemote.achievementHandlers(taskSuc0011);
            process.setIsFlyAction(2);
            F30021.AircraftInfo aircraftInfo = F30021.AircraftInfo.parseFrom(airplaneByte);
            killEachOther(process, aircraftInfo);
          } else {
            // 玩家成就 穿越虫洞
            Map<String, Object> taskSuc0011 = Maps.newHashMap();
            taskSuc0011.put("userId", process.getPlayerId());
            taskSuc0011.put("code", AchievementEnum.AMT0011.getCode());
            taskSuc0011.put("progress", 1);
            taskSuc0011.put("isReset", 0);
            this.userRemote.achievementHandlers(taskSuc0011);
            F30021.AircraftInfo aircraftInfo = F30021.AircraftInfo.parseFrom(airplaneByte);
            endZoneProcess(process, aircraftInfo);
          }
        } else {
          // 颜色不同 到达位置
          process.setIsFlyAction(0);
          endZoneProcess(process);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 飞行流程.
   *
   * @param process [飞行过程]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 18:48
   * @update 2020/9/25 18:48
   */
  private void flightZoneProcess(FlightProcess process) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(process.getRoomId());
      if (Objects.nonNull(airplaneRoom)) {
        int positional = process.getWalkingList().get(process.getWalkingList().size() - 1);
        AirplaneTrack airplaneTrack = airplaneRoom.getTrackAirplane().get(positional);
        int airplaneColor = airplaneRoom.getAirplaneColor(process.getAirplaneId());
        if (airplaneTrack.getTrackColor() == airplaneColor) {
          int trackLink = airplaneTrack.getTrackLinkSeat();
          int trackFlight = airplaneTrack.getTrackFlight();
          if (trackLink > 0 && trackFlight == 0) {
            //---»「跳棋」
            F30021.AircraftInfo.Builder airplaneBuilder = F30021.AircraftInfo.newBuilder();
            airplaneBuilder.setIsFly(0);
            airplaneBuilder.setAircraftID(0);
            airplaneBuilder.setLocalPosition(0);
            F30021.KillInfo.Builder killInfo;
            for (Integer integer : process.getWalkingList()) {
              killInfo = F30021.KillInfo.newBuilder();
              killInfo.setStartFlyPosi(integer);
              airplaneBuilder.addKillInfo(killInfo);
            }
            process.getWalkingList().add(trackLink);
            airplaneTrack = airplaneRoom.getTrackAirplane().get(trackLink);
            // 跑道区域 [0-起飞区 1-飞行区 2-结束区].
            if (airplaneTrack.getTrackZone() == 0) {
              // 玩家成就 弹射起步
              Map<String, Object> taskSuc0009 = Maps.newHashMap();
              taskSuc0009.put("userId", process.getPlayerId());
              taskSuc0009.put("code", AchievementEnum.AMT0009.getCode());
              taskSuc0009.put("progress", 1);
              taskSuc0009.put("isReset", 0);
              this.userRemote.achievementHandlers(taskSuc0009);
              // 显示效果[0.正常 1.跳越 2.飞行]
              process.setIsFlyAction(1);
              endZoneProcess(process);
            } else {
              List<Integer> airplaneList = airplaneTrack.getAirplaneList();
              if (CollectionUtils.isNotEmpty(airplaneList)) {
                int airColor = airplaneRoom.getAirplaneColor(airplaneList.get(0));
                if (airplaneColor != airColor) {
                  // 玩家成就 弹射起步
                  Map<String, Object> taskSuc0009 = Maps.newHashMap();
                  taskSuc0009.put("userId", process.getPlayerId());
                  taskSuc0009.put("code", AchievementEnum.AMT0009.getCode());
                  taskSuc0009.put("progress", 1);
                  taskSuc0009.put("isReset", 0);
                  this.userRemote.achievementHandlers(taskSuc0009);
                  // 击杀信息
                  killInfo = F30021.KillInfo.newBuilder();
                  // 击落位置
                  killInfo.setStartFlyPosi(trackLink);
                  // 是否击落对方
                  killInfo.setIsBeatRival(true);
                  // 被击落玩家ID
                  long killUser = airplaneRoom.getPlayerIdByColor(airColor);
                  killInfo.setBeatPosition(killUser);
                  // 被击落玩家飞机ID
                  killInfo.addAllBeatAircraID(airplaneList);
                  airplaneBuilder.addKillInfo(killInfo);
                  byte[] airplaneByte = airplaneBuilder.build().toByteArray();
                  F30021.AircraftInfo aircraftInfo = F30021.AircraftInfo.parseFrom(airplaneByte);
                  // [0.正常 1.跳越 2.飞行]
                  process.setIsFlyAction(1);
                  killEachOther(process, aircraftInfo);
                } else {
                  // 玩家成就 弹射起步
                  Map<String, Object> taskSuc0009 = Maps.newHashMap();
                  taskSuc0009.put("userId", process.getPlayerId());
                  taskSuc0009.put("code", AchievementEnum.AMT0009.getCode());
                  taskSuc0009.put("progress", 1);
                  taskSuc0009.put("isReset", 0);
                  this.userRemote.achievementHandlers(taskSuc0009);
                  // [0.正常 1.跳越 2.飞行]
                  process.setIsFlyAction(1);
                  endZoneProcess(process);
                }
              } else {
                // 玩家成就 弹射起步
                Map<String, Object> taskSuc0009 = Maps.newHashMap();
                taskSuc0009.put("userId", process.getPlayerId());
                taskSuc0009.put("code", AchievementEnum.AMT0009.getCode());
                taskSuc0009.put("progress", 1);
                taskSuc0009.put("isReset", 0);
                this.userRemote.achievementHandlers(taskSuc0009);
                // [0.正常 1.跳越 2.飞行]
                process.setIsFlyAction(1);
                endZoneProcess(process);
              }
            }
          } else if (trackLink == 0 && trackFlight == 0) {
            //---»「结束」
            List<Integer> airplaneList = airplaneTrack.getAirplaneList();
            if (CollectionUtils.isNotEmpty(airplaneList)) {
              int airColor = airplaneRoom.getAirplaneColor(airplaneList.get(0));
              if (airplaneColor != airColor) {
                F30021.AircraftInfo.Builder airplaneBuilder = F30021.AircraftInfo.newBuilder();
                airplaneBuilder.setIsFly(0);
                airplaneBuilder.setAircraftID(0);
                airplaneBuilder.setLocalPosition(0);
                long killUser = airplaneRoom.getPlayerIdByColor(airColor);
                F30021.KillInfo.Builder killInfo = F30021.KillInfo.newBuilder();
                // 击落位置
                killInfo.setStartFlyPosi(positional);
                // 是否击落对方
                killInfo.setIsBeatRival(true);
                // 被击落玩家ID
                killInfo.setBeatPosition(killUser);
                // 被击落玩家飞机ID
                killInfo.addAllBeatAircraID(airplaneList);
                airplaneBuilder.addKillInfo(killInfo);
                byte[] airplaneByte = airplaneBuilder.build().toByteArray();
                F30021.AircraftInfo aircraftInfo = F30021.AircraftInfo.parseFrom(airplaneByte);
                // [0.正常 1.跳越 2.飞行]
                process.setIsFlyAction(0);
                killEachOther(process, aircraftInfo);
              } else {
                process.setIsFlyAction(0);
                endZoneProcess(process);
              }
            } else {
              process.setIsFlyAction(0);
              endZoneProcess(process);
            }
          } else if (trackLink == 0 && trackFlight > 0) {
            //---»「飞棋」
            F30021.AircraftInfo.Builder airplaneBuilder = F30021.AircraftInfo.newBuilder();
            airplaneBuilder.setIsFly(0);
            airplaneBuilder.setAircraftID(0);
            airplaneBuilder.setLocalPosition(0);
            F30021.KillInfo.Builder killInfo;
            for (Integer integer : process.getWalkingList()) {
              killInfo = F30021.KillInfo.newBuilder();
              killInfo.setStartFlyPosi(integer);
              airplaneBuilder.addKillInfo(killInfo);
            }
            process.getWalkingList().add(trackFlight);
            airplaneTrack = airplaneRoom.getTrackAirplane().get(trackFlight);
            List<Integer> airplaneList = airplaneTrack.getAirplaneList();
            if (CollectionUtils.isNotEmpty(airplaneList)) {
              int airColor = airplaneRoom.getAirplaneColor(airplaneList.get(0));
              if (airplaneColor != airColor) {
                killInfo = F30021.KillInfo.newBuilder();
                // 击落位置
                killInfo.setStartFlyPosi(trackFlight);
                // 是否击落对方
                killInfo.setIsBeatRival(true);
                // 被击落玩家ID
                long killUser = airplaneRoom.getPlayerIdByColor(airColor);
                killInfo.setBeatPosition(killUser);
                // 被击落玩家飞机ID
                killInfo.addAllBeatAircraID(airplaneList);
                airplaneBuilder.addKillInfo(killInfo);
              } else {
                killInfo = F30021.KillInfo.newBuilder();
                killInfo.setStartFlyPosi(trackFlight);
                airplaneBuilder.addKillInfo(killInfo);
              }
            } else {
              killInfo = F30021.KillInfo.newBuilder();
              killInfo.setStartFlyPosi(trackFlight);
              airplaneBuilder.addKillInfo(killInfo);
            }
            int trackLink2 = airplaneTrack.getTrackLinkSeat();
            process.getWalkingList().add(trackLink2);
            airplaneTrack = airplaneRoom.getTrackAirplane().get(trackLink2);
            List<Integer> airplaneEndList = airplaneTrack.getAirplaneList();
            if (CollectionUtils.isNotEmpty(airplaneEndList)) {
              int airColor = airplaneRoom.getAirplaneColor(airplaneEndList.get(0));
              if (airplaneColor != airColor) {
                killInfo = F30021.KillInfo.newBuilder();
                // 击落位置
                killInfo.setStartFlyPosi(trackLink2);
                // 是否击落对方
                killInfo.setIsBeatRival(true);
                // 被击落玩家ID
                long killUser = airplaneRoom.getPlayerIdByColor(airColor);
                killInfo.setBeatPosition(killUser);
                // 被击落玩家飞机ID
                killInfo.addAllBeatAircraID(airplaneEndList);
                airplaneBuilder.addKillInfo(killInfo);
              } else {
                killInfo = F30021.KillInfo.newBuilder();
                killInfo.setStartFlyPosi(trackLink2);
                airplaneBuilder.addKillInfo(killInfo);
              }
            } else {
              killInfo = F30021.KillInfo.newBuilder();
              killInfo.setStartFlyPosi(trackLink2);
              airplaneBuilder.addKillInfo(killInfo);
            }
            byte[] airplaneByte = airplaneBuilder.build().toByteArray();
            if (airplaneList.size() >= 1 || airplaneEndList.size() >= 1) {
              // 玩家成就 穿越虫洞
              Map<String, Object> taskSuc0011 = Maps.newHashMap();
              taskSuc0011.put("userId", process.getPlayerId());
              taskSuc0011.put("code", AchievementEnum.AMT0011.getCode());
              taskSuc0011.put("progress", 1);
              taskSuc0011.put("isReset", 0);
              this.userRemote.achievementHandlers(taskSuc0011);
              F30021.AircraftInfo aircraftInfo = F30021.AircraftInfo.parseFrom(airplaneByte);
              process.setIsFlyAction(2);
              killEachOther(process, aircraftInfo);
            } else {
              // 玩家成就 穿越虫洞
              Map<String, Object> taskSuc0011 = Maps.newHashMap();
              taskSuc0011.put("userId", process.getPlayerId());
              taskSuc0011.put("code", AchievementEnum.AMT0011.getCode());
              taskSuc0011.put("progress", 1);
              taskSuc0011.put("isReset", 0);
              this.userRemote.achievementHandlers(taskSuc0011);
              F30021.AircraftInfo aircraftInfo = F30021.AircraftInfo.parseFrom(airplaneByte);
              endZoneProcess(process, aircraftInfo);
            }
          }
        } else {
          List<Integer> airplaneList = airplaneTrack.getAirplaneList();
          if (CollectionUtils.isNotEmpty(airplaneList)) {
            int airColor = airplaneRoom.getAirplaneColor(airplaneList.get(0));
            if (airplaneColor != airColor) {
              F30021.AircraftInfo.Builder airplaneBuilder = F30021.AircraftInfo.newBuilder();
              airplaneBuilder.setIsFly(0);
              airplaneBuilder.setAircraftID(0);
              airplaneBuilder.setLocalPosition(0);
              F30021.KillInfo.Builder killInfo;
              for (Integer integer : process.getWalkingList()) {
                killInfo = F30021.KillInfo.newBuilder();
                killInfo.setStartFlyPosi(integer);
                if (integer == positional) {
                  // 是否击落对方
                  killInfo.setIsBeatRival(true);
                  // 被击落玩家ID
                  long killUser = airplaneRoom.getPlayerIdByColor(airColor);
                  killInfo.setBeatPosition(killUser);
                  // 被击落玩家飞机ID
                  killInfo.addAllBeatAircraID(airplaneList);
                }
                airplaneBuilder.addKillInfo(killInfo);
              }
              byte[] airplaneByte = airplaneBuilder.build().toByteArray();
              F30021.AircraftInfo aircraftInfo = F30021.AircraftInfo.parseFrom(airplaneByte);
              // [0.正常 1.跳越 2.飞行]
              process.setIsFlyAction(0);
              killEachOther(process, aircraftInfo);
            } else {
              process.setIsFlyAction(0);
              endZoneProcess(process);
            }
          } else {
            process.setIsFlyAction(0);
            endZoneProcess(process);
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 终点流程.
   *
   * @param process [飞行过程]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/27 14:14
   * @update 2020/9/27 14:14
   */
  private void endZoneProcess(FlightProcess process) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(process.getRoomId());
      if (Objects.nonNull(airplaneRoom)) {
        AirplanePlayer airplanePlayer = airplaneRoom.getPlayerInfoById(process.getPlayerId());
        Integer positional = process.getWalkingList().get(process.getWalkingList().size() - 1);
        airplaneRoom.leavePositional(process.getInitValue(), positional, process.getAirplaneId(), process.getPlayerId());
        F30021.AircraftInfo.Builder airplaneBuilder = F30021.AircraftInfo.newBuilder();
        if (positional == 65 || positional == 75 || positional == 85 || positional == 95) {
          airplaneBuilder.setIsFinish(1);
          airplaneRoom.arrivedEndPositional(positional, process.getAirplaneId(), process.getPlayerId());
        } else {
          airplaneRoom.arrivedPositional(positional, process.getAirplaneId());
        }
        airplaneBuilder.setAircraftID(process.getAirplaneId())
            .setLocalPosition(process.getInitValue())
            .setIsFly(process.getIsFlyAction());
        F30021.KillInfo.Builder killBuilder;
        for (Integer integer : process.getWalkingList()) {
          killBuilder = F30021.KillInfo.newBuilder();
          killBuilder.setStartFlyPosi(integer);
          airplaneBuilder.addKillInfo(killBuilder);
        }
        F30021.F300213S2C.Builder response = F30021.F300213S2C.newBuilder();
        // 当前玩家已结束飞行
        if (airplaneRoom.getArrivedEndAirplaneNum(process.getPlayerId()) == 4) {
          // 房间类型 [0-双人房间 1-四人房间]
          if (airplaneRoom.getRoomType() == 0) {
            airplaneRoom.destroy();
            response.setActionUserId(process.getPlayerId());
            response.setMoveAircraftInfo(airplaneBuilder);
            response.setNextUserID(0);
            response.setActionTime(0);
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.MOVES_CHESS,
                    response.build().toByteArray()), airplaneRoom.getRoomId());
            // 延时1s显示结算
            gameFinish0(process.getRoomId(), process.getPlayerId());
            return;
          } else {
            // 加入排名
            airplaneRoom.getFinishRanking().add(process.getPlayerId());
            switch (airplaneRoom.getFinishRanking().size()) {
              case 1:
                if (MemManager.isExists(process.getPlayerId())) {
                  gainExperience(process.getPlayerId(), 12, 15);
                } else {
                  gainExperience(process.getPlayerId(), 6, 15);
                }
                break;
              case 2:
                if (MemManager.isExists(process.getPlayerId())) {
                  gainExperience(process.getPlayerId(), 8, 10);
                } else {
                  gainExperience(process.getPlayerId(), 4, 10);
                }
                break;
              default:
                if (MemManager.isExists(process.getPlayerId())) {
                  gainExperience(process.getPlayerId(), 4, 5);
                } else {
                  gainExperience(process.getPlayerId(), 2, 5);
                }
                break;
            }
            List<AirplanePlayer> airplanePlayers = airplaneRoom.getPlayerList();
            airplanePlayers = airplanePlayers.stream()
                // 连接状态 [0-连接中 1-已断开]          游戏状态 [0-游戏中 1-已结束 2-已离开(已认输)]
                .filter(player -> player.getLinkStatus() == 0 && (player.getPlayerStatus() == 0 || player.getPlayerStatus() == 1))
                .collect(Collectors.toList());
            if (airplaneRoom.getFinishRanking().size() == (airplanePlayers.size() - 1)) {
              // 游戏完全结束
              airplaneRoom.destroy();
              response.setActionUserId(process.getPlayerId());
              response.setMoveAircraftInfo(airplaneBuilder);
              response.setNextUserID(0);
              response.setActionTime(0);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.MOVES_CHESS,
                      response.build().toByteArray()), airplaneRoom.getRoomId());
              // 延时1s显示结算
              gameFinish1(process.getRoomId());
              return;
            } else {
              // 设置玩家
              airplaneRoom.setUpActionPlayer(process.getPlayerId());
              airplanePlayer.setPlayerStatus(1);
              airplanePlayer.setFinishIndex(1);
              response.setActionUserId(process.getPlayerId());
              response.setMoveAircraftInfo(airplaneBuilder);
              response.setNextUserID(airplaneRoom.getNowActionPlayer());
              response.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.MOVES_CHESS,
                      response.build().toByteArray()), airplaneRoom.getRoomId());
            }
          }
        } else {
          if (airplanePlayer.getLatestDice() < 6) {
            airplaneRoom.setUpActionPlayer(process.getPlayerId());
          }
          response.setActionUserId(process.getPlayerId());
          response.setMoveAircraftInfo(airplaneBuilder);
          response.setNextUserID(airplaneRoom.getNowActionPlayer());
          response.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.MOVES_CHESS,
                  response.build().toByteArray()), airplaneRoom.getRoomId());
        }
        AirplanePlayer nextPlayer = airplaneRoom.getPlayerInfoById(response.getNextUserID());
        // 是否机器 [0-是 1-否]
        if (nextPlayer.getIsRobot() == 0) {
          robotStartDiceTimeout(airplaneRoom.getRoomId());
        } else {
          // 开启托管 [0-关闭 1-开启]
          if (nextPlayer.getStartEscrow() == 1) {
            autoStartDiceTimeout(airplaneRoom.getRoomId());
          } else {
            // 添加操作定时
            actionTimeout(airplaneRoom.getRoomId());
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 终点流程.
   *
   * @param process [飞行过程]
   * @param aircraftInfo [步骤信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/27 14:12
   * @update 2020/9/27 14:12
   */
  private void endZoneProcess(FlightProcess process, AircraftInfo aircraftInfo) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(process.getRoomId());
      if (Objects.nonNull(airplaneRoom)) {
        AirplanePlayer airplanePlayer = airplaneRoom.getPlayerInfoById(process.getPlayerId());
        Integer positional = process.getWalkingList().get(process.getWalkingList().size() - 1);
        airplaneRoom.leavePositional(process.getInitValue(), positional, process.getAirplaneId(), process.getPlayerId());
        F30021.AircraftInfo.Builder airplaneBuilder = F30021.AircraftInfo.newBuilder();
        if (positional == 65 || positional == 75 || positional == 85 || positional == 95) {
          airplaneBuilder.setIsFinish(1);
          airplaneRoom.arrivedEndPositional(positional, process.getAirplaneId(), process.getPlayerId());
        } else {
          airplaneRoom.arrivedPositional(positional, process.getAirplaneId());
        }
        airplaneBuilder.setAircraftID(process.getAirplaneId()).setLocalPosition(process.getInitValue()).setIsFly(2);
        airplaneBuilder.addAllKillInfo(aircraftInfo.getKillInfoList());
        F30021.F300213S2C.Builder response = F30021.F300213S2C.newBuilder();
        // 当前玩家已结束飞行
        if (airplaneRoom.getArrivedEndAirplaneNum(process.getPlayerId()) == 4) {
          // 房间类型 [0-双人房间 1-四人房间]
          if (airplaneRoom.getRoomType() == 0) {
            airplaneRoom.destroy();
            response.setActionUserId(process.getPlayerId());
            response.setMoveAircraftInfo(airplaneBuilder);
            response.setNextUserID(0);
            response.setActionTime(0);
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.MOVES_CHESS,
                    response.build().toByteArray()), airplaneRoom.getRoomId());
            // 延时1s显示结算
            gameFinish0(process.getRoomId(), process.getPlayerId());
            return;
          } else {
            // 加入排名
            airplaneRoom.getFinishRanking().add(process.getPlayerId());
            switch (airplaneRoom.getFinishRanking().size()) {
              case 1:
                if (MemManager.isExists(process.getPlayerId())) {
                  gainExperience(process.getPlayerId(), 12, 15);
                } else {
                  gainExperience(process.getPlayerId(), 6, 15);
                }
                break;
              case 2:
                if (MemManager.isExists(process.getPlayerId())) {
                  gainExperience(process.getPlayerId(), 8, 10);
                } else {
                  gainExperience(process.getPlayerId(), 4, 10);
                }
                break;
              default:
                if (MemManager.isExists(process.getPlayerId())) {
                  gainExperience(process.getPlayerId(), 4, 5);
                } else {
                  gainExperience(process.getPlayerId(), 2, 5);
                }
                break;
            }
            List<AirplanePlayer> airplanePlayers = airplaneRoom.getPlayerList();
            airplanePlayers = airplanePlayers.stream()
                // 连接状态 [0-连接中 1-已断开]              游戏状态 [0-游戏中 1-已结束 2-已离开(已认输)]
                .filter(player -> player.getLinkStatus() == 0 && (player.getPlayerStatus() == 0 || player.getPlayerStatus() == 1))
                .collect(Collectors.toList());
            if (airplaneRoom.getFinishRanking().size() == (airplanePlayers.size() - 1)) {
              // 游戏完全结束
              airplaneRoom.destroy();
              response.setActionUserId(process.getPlayerId());
              response.setMoveAircraftInfo(airplaneBuilder);
              response.setNextUserID(0);
              response.setActionTime(0);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.MOVES_CHESS,
                      response.build().toByteArray()), airplaneRoom.getRoomId());
              // 延时1s显示结算
              gameFinish1(process.getRoomId());
              return;
            } else {
              // 设置玩家
              airplaneRoom.setUpActionPlayer(process.getPlayerId());
              airplanePlayer.setPlayerStatus(1);
              airplanePlayer.setFinishIndex(1);
              response.setActionUserId(process.getPlayerId());
              response.setMoveAircraftInfo(airplaneBuilder);
              response.setNextUserID(airplaneRoom.getNowActionPlayer());
              response.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.MOVES_CHESS,
                      response.build().toByteArray()), airplaneRoom.getRoomId());
            }
          }
        } else {
          if (airplanePlayer.getLatestDice() < 6) {
            airplaneRoom.setUpActionPlayer(process.getPlayerId());
          }
          response.setActionUserId(process.getPlayerId());
          response.setMoveAircraftInfo(airplaneBuilder);
          response.setNextUserID(airplaneRoom.getNowActionPlayer());
          response.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.MOVES_CHESS,
                  response.build().toByteArray()), airplaneRoom.getRoomId());
        }
        AirplanePlayer nextPlayer = airplaneRoom.getPlayerInfoById(response.getNextUserID());
        // 是否机器 [0-是 1-否]
        if (nextPlayer.getIsRobot() == 0) {
          robotStartDiceTimeout(airplaneRoom.getRoomId());
        } else {
          // 开启托管 [0-关闭 1-开启]
          if (nextPlayer.getStartEscrow() == 1) {
            autoStartDiceTimeout(airplaneRoom.getRoomId());
          } else {
            // 添加操作定时
            actionTimeout(airplaneRoom.getRoomId());
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 击落飞机.
   *
   * @param process [飞行过程]
   * @param aircraftInfo [步骤信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/27 14:09
   * @update 2020/9/27 14:09
   */
  private void killEachOther(FlightProcess process, F30021.AircraftInfo aircraftInfo) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(process.getRoomId());
      if (Objects.nonNull(airplaneRoom)) {
        AirplanePlayer airplanePlayer = airplaneRoom.getPlayerInfoById(process.getPlayerId());
        if (airplanePlayer.getLatestDice() < 6) {
          // 设置玩家
          airplaneRoom.setUpActionPlayer(process.getPlayerId());
        }
        if (airplanePlayer.getHitBackToBase() == 0) {
          // 每日任务 在飞行棋中，将对手撞回基地1次
          Map<String, Object> taskInfo = Maps.newHashMap();
          taskInfo.put("userId", process.getPlayerId());
          taskInfo.put("code", TaskEnum.PGT0019.getCode());
          taskInfo.put("progress", 1);
          taskInfo.put("isReset", 0);
          this.userRemote.taskHandler(taskInfo);
          airplanePlayer.setHitBackToBase(1);
        }
        F30021.AircraftInfo.Builder aircraftBuilder = F30021.AircraftInfo.newBuilder();
        aircraftBuilder.setAircraftID(process.getAirplaneId());
        aircraftBuilder.setLocalPosition(process.getInitValue());
        aircraftBuilder.setIsFly(process.getIsFlyAction());
        aircraftBuilder.addAllKillInfo(aircraftInfo.getKillInfoList());
        List<F30021.KillInfo> killInfoList = aircraftInfo.getKillInfoList();
        if (CollectionUtils.isNotEmpty(killInfoList)) {
          for (KillInfo killInfo : killInfoList) {
            if (killInfo.getBeatPosition() > 0L) {
              // 玩家成就 回到原点
              Map<String, Object> taskSuc0010 = Maps.newHashMap();
              taskSuc0010.put("userId", killInfo.getBeatPosition());
              taskSuc0010.put("code", AchievementEnum.AMT0010.getCode());
              taskSuc0010.put("progress", 1);
              taskSuc0010.put("isReset", 0);
              this.userRemote.achievementHandlers(taskSuc0010);
              airplaneRoom.shootDownAirplane(killInfo.getStartFlyPosi(), process.getAirplaneId(), killInfo.getBeatPosition());
            }
          }
        }
        Integer positional = process.getWalkingList().get(process.getWalkingList().size() - 1);
        airplaneRoom.leavePositional(process.getInitValue(), positional, process.getAirplaneId(), process.getPlayerId());
        F30021.F300213S2C.Builder builder = F30021.F300213S2C.newBuilder();
        builder.setMoveAircraftInfo(aircraftBuilder);
        builder.setActionUserId(process.getPlayerId());
        builder.setNextUserID(airplaneRoom.getNowActionPlayer());
        builder.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.MOVES_CHESS,
                builder.build().toByteArray()), airplaneRoom.getRoomId());
        AirplanePlayer nextPlayer = airplaneRoom.getPlayerInfoById(builder.getNextUserID());
        // 是否机器 [0-是 1-否]
        if (nextPlayer.getIsRobot() == 0) {
          robotStartDiceTimeout(airplaneRoom.getRoomId());
        } else {
          // 开启托管 [0-关闭 1-开启]
          if (nextPlayer.getStartEscrow() == 1) {
            autoStartDiceTimeout(airplaneRoom.getRoomId());
          } else {
            // 添加操作定时
            actionTimeout(airplaneRoom.getRoomId());
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 终点步骤.
   *
   * @param initNumber [初始位置]
   * @param endNumber [结束位置]
   * @param maxNumber [终点区最大值]
   * @return [前行步骤]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 18:48
   * @update 2020/9/25 18:48
   */
  private List<Integer> terminusWalkList(int initNumber, int endNumber, int maxNumber) {
    List<Integer> walkingList = Lists.newLinkedList();
    if (endNumber > maxNumber) {
      int diff1 = maxNumber - initNumber;
      int diff2 = endNumber - maxNumber;
      for (int i = 1; i < diff1 + 1; i++) {
        walkingList.add(initNumber + i);
      }
      for (int i = 1; i < diff2 + 1; i++) {
        walkingList.add(maxNumber - i);
      }
    } else if (endNumber == maxNumber) {
      int diff = maxNumber - initNumber;
      for (int i = 1; i < diff + 1; i++) {
        walkingList.add(initNumber + i);
      }
    } else {
      int diff = endNumber - initNumber;
      for (int i = 1; i < diff + 1; i++) {
        walkingList.add(initNumber + i);
      }
    }
    return walkingList;
  }

  /**
   * TODO 飞行步骤.
   *
   * @param process [飞行过程]
   * @param color [颜色]
   * @return [飞行步骤]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 18:48
   * @update 2020/9/25 18:48
   */
  private List<Integer> flyWalkingList(FlightProcess process, Integer color) {
    List<Integer> walkingList;
    switch (color) {
      case 1:
        int redDoor = 52;
        int redIndex = 59;
        int redEndZone = 60;
        walkingList = walkingList(process, redDoor, redIndex, redEndZone);
        break;
      case 2:
        int yellowDoor = 13;
        int yellowIndex = 20;
        int yellowEndZone = 70;
        walkingList = walkingList(process, yellowDoor, yellowIndex, yellowEndZone);
        break;
      case 3:
        int blueDoor = 26;
        int blueIndex = 33;
        int blueEndZone = 80;
        walkingList = walkingList(process, blueDoor, blueIndex, blueEndZone);
        break;
      default:
        int greenDoor = 39;
        int greenIndex = 46;
        int greenEndZone = 90;
        walkingList = walkingList(process, greenDoor, greenIndex, greenEndZone);
        break;
    }
    return walkingList;
  }

  /**
   * TODO 步骤解析.
   *
   * @param process [飞行过程]
   * @param entrance [结束入口值]
   * @param indexNum [标记值]
   * @param endZone [结束区初始值]
   * @return [飞行步骤]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 13:03
   * @update 2020/10/14 13:03
   */
  private List<Integer> walkingList(FlightProcess process, Integer entrance, Integer indexNum, Integer endZone) {
    List<Integer> walkingList = Lists.newLinkedList();
    // 世界地图位置判断
    int board = 52;
    if (process.getEndValue() < indexNum && process.getInitValue() <= entrance) {
      if (process.getEndValue() > entrance && !process.getInitValue().equals(entrance)) {
        int dd = process.getEndValue() - entrance;
        int bb = process.getDiceValue() - dd;
        for (int i = 1; i < bb + 1; i++) {
          walkingList.add(process.getInitValue() + i);
        }
        for (int i = 0; i < dd; i++) {
          walkingList.add(endZone + i);
        }
      }
      if (process.getEndValue() > entrance && process.getInitValue().equals(entrance)) {
        for (int i = 0; i < process.getDiceValue(); i++) {
          walkingList.add(endZone + i);
        }
      }
      if (process.getEndValue() <= entrance) {
        for (int i = 1; i < process.getDiceValue() + 1; i++) {
          walkingList.add(process.getInitValue() + i);
        }
      }
    } else {
      if (process.getEndValue() > board && process.getInitValue() != board) {
        int positional = process.getEndValue() - board;
        int difference = process.getDiceValue() - positional;
        for (int i = 1; i < difference + 1; i++) {
          walkingList.add(process.getInitValue() + i);
        }
        for (int i = 1; i < positional + 1; i++) {
          walkingList.add(i);
        }
      }
      if (process.getEndValue() > board && process.getInitValue() == board) {
        for (int i = 1; i < process.getDiceValue() + 1; i++) {
          walkingList.add(i);
        }
      }
      if (process.getEndValue() <= board) {
        for (int i = 1; i < process.getDiceValue() + 1; i++) {
          walkingList.add(process.getInitValue() + i);
        }
      }
    }
    return walkingList;
  }

  /**
   * TODO 玩家认输.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:18
   * @update 2020/9/25 16:18
   */
  private void admitsDefeat(Channel channel, Packet packet) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(airplaneRoom)) {
        AirplanePlayer isPlayer = airplaneRoom.getPlayerInfoById(packet.userId);
        if (Objects.nonNull(isPlayer)) {
          F30021.F300215S2C.Builder response = F30021.F300215S2C.newBuilder();
          if (airplaneRoom.getRoomStatus() == 1) {
            channel.writeAndFlush(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.ADMITS_DEFEAT,
                response.setResult(0).build().toByteArray()));
            //  房间类型 [0-双人房间 1-四人房间]
            if (airplaneRoom.getRoomType() == 0) {
              airplaneRoom.destroy();
              admitsDefeatRoom0(packet.roomId, packet.userId);
            } else {
              admitsDefeatRoom1(packet.roomId, packet.userId);
            }
          } else {
            channel.writeAndFlush(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.ADMITS_DEFEAT,
                response.setResult(1).build().toByteArray()));
          }
        } else {
          playerNotExist(channel);
        }
      } else {
        roomNotExist(channel);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 认输类型. 0
   *
   * @param roomId [房间ID]
   * @param playerId [认输玩家]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/2 12:19
   * @update 2020/8/2 12:19
   */
  private void admitsDefeatRoom0(Long roomId, Long playerId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        LocalDateTime startTime = airplaneRoom.getStartTimeIndex();
        LocalDateTime newTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, newTime);
        int seconds = (int) duration.getSeconds();
        if (seconds > AirplaneAssets.getInt(AirplaneAssets.SETTLEMENT_TIME)) {
          // 真结算
          admitsDefeatReally(roomId, playerId);
        } else {
          // 假结算
          admitsDefeatDummy(roomId, playerId);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 游戏结算. 真
   *
   * @param roomId [房间ID]
   * @param playerId [认输玩家]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/27 3:51
   * @update 2020/9/27 3:51
   */
  private void admitsDefeatReally(Long roomId, Long playerId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        F30021.F300214S2C.Builder response = F30021.F300214S2C.newBuilder();
        List<AirplanePlayer> playerList = airplaneRoom.getPlayerList();
        // 认输玩家判断
        if (Objects.equals(playerId, playerList.get(0).getPlayerId())) {
          AirplanePlayer win = playerList.get(1);
          response.setUserID(win.getPlayerId());
          if (MemManager.isExists(win.getPlayerId())) {
            response.setAddExp(gainExperience(win.getPlayerId(), 8, 10));
            response.setIsDouble(1);
          } else {
            response.setAddExp(gainExperience(win.getPlayerId(), 4, 10));
          }
          win.sendPacket(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
              response.setAddGold(10).build().toByteArray()));
          response.clearIsDouble();
          AirplanePlayer lose = playerList.get(0);
          if (MemManager.isExists(lose.getPlayerId())) {
            response.setIsDouble(1);
          }
          lose.sendPacket(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
              response.setAddExp(0).setAddGold(0).build().toByteArray()));
        } else {
          AirplanePlayer win = playerList.get(0);
          response.setUserID(win.getPlayerId());
          if (MemManager.isExists(win.getPlayerId())) {
            response.setAddExp(gainExperience(win.getPlayerId(), 8, 10));
            response.setIsDouble(1);
          } else {
            response.setAddExp(gainExperience(win.getPlayerId(), 4, 10));
          }
          win.sendPacket(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
              response.setAddGold(10).build().toByteArray()));
          response.clearIsDouble();
          AirplanePlayer lose = playerList.get(1);
          if (MemManager.isExists(lose.getPlayerId())) {
            response.setIsDouble(1);
          }
          lose.sendPacket(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
              response.setAddExp(0).setAddGold(0).build().toByteArray()));
        }
        // 玩家成就
        playerList.stream().filter(player -> player.getIsRobot() == 1)
            .forEach(player -> {
            });
        clearRoom(airplaneRoom.getRoomId());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 游戏结算. 假
   *
   * @param roomId [房间ID]
   * @param playerId [认输玩家]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/27 3:51
   * @update 2020/9/27 3:51
   */
  private void admitsDefeatDummy(Long roomId, Long playerId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        F30021.F300214S2C.Builder response = F30021.F300214S2C.newBuilder();
        List<AirplanePlayer> playerList = airplaneRoom.getPlayerList();
        // 认输玩家判断
        if (Objects.equals(playerList.get(0).getPlayerId(), playerId)) {
          response.setUserID(playerList.get(1).getPlayerId());
        } else {
          response.setUserID(playerList.get(0).getPlayerId());
        }
        response.setAddGold(0).setAddExp(0);
        playerList.forEach(player -> {
          if (MemManager.isExists(player.getPlayerId())) {
            response.setIsDouble(1);
          }
          player.sendPacket(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
              response.build().toByteArray()));
          response.clearIsDouble();
        });
        // 玩家成就
        playerList.stream().filter(player -> player.getIsRobot() == 1)
            .forEach(player -> {
            });
        clearRoom(airplaneRoom.getRoomId());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 认输类型. 1
   *
   * @param roomId [房间ID]
   * @param playerId [认输玩家]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/2 12:19
   * @update 2020/8/2 12:19
   */
  private void admitsDefeatRoom1(Long roomId, Long playerId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        long nextPlayerId = airplaneRoom.getNextActionPlayer(playerId);
        AirplanePlayer losePlayer = airplaneRoom.getPlayerInfoById(playerId);
        losePlayer.setLinkStatus(1);
        losePlayer.setPlayerStatus(2);
        F30021.F300214S2C.Builder response = F30021.F300214S2C.newBuilder();
        response.setUserID(1000).setAddExp(0).setAddGold(0);
        if (MemManager.isExists(playerId)) {
          response.setIsDouble(1);
        }
        losePlayer.sendPacket(
            new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
                response.setAddExp(0).setAddGold(0).build().toByteArray()));
        losePlayer.setChannel(null);
        ChatManager.removeChannel(roomId, losePlayer.getChannel());
        GroupManager.removeChannel(roomId, losePlayer.getChannel());
        // 成就检测
        achievementTask(roomId, playerId);
        // 玩家离开
        F30021.F300217S2C.Builder exitBuilder = F30021.F300217S2C.newBuilder();
        exitBuilder.setResult(0).setUserID(playerId);
        // 起飞的飞机
        List<AirplaneEntity> aircraftFlyList = losePlayer.getAirplaneFlyList();
        if (CollectionUtils.isNotEmpty(aircraftFlyList)) {
          for (AirplaneEntity airplaneEntity : aircraftFlyList) {
            exitBuilder.addAircraftFly(airplaneEntity.getAirplaneId());
          }
          airplaneRoom.clearTrackAirplane(aircraftFlyList);
        }
        // 基地的飞机
        List<AirplaneEntity> aircraftBaseList = losePlayer.getAirplaneBaseList();
        if (CollectionUtils.isNotEmpty(aircraftBaseList)) {
          for (AirplaneEntity airplaneEntity : aircraftBaseList) {
            exitBuilder.addAircraftFly(airplaneEntity.getAirplaneId());
          }
        }
        if (Objects.equals(airplaneRoom.getNowActionPlayer(), playerId)) {
          List<AirplanePlayer> playerList = airplaneRoom.getPlayerList();
          int finishNum = airplaneRoom.getFinishRanking().size();
          int robotNum = airplaneRoom.getRobotNum();
          int lastRobotNum = (int) playerList.stream()
              .filter(robot -> robot.getIsRobot() == 0
                  && robot.getPlayerStatus() == 0).count();
          // 真实玩家 区分身份  区分状态 = 1
          int realNum = airplaneRoom.getRealPlayerNum();
          int watchNum = (int) playerList.stream()
              .filter(player -> player.getIsRobot() == 1
                  && player.getPlayerStatus() == 1
                  && player.getLinkStatus() == 0).count();
          switch (robotNum) {
            case 3:
              if (realNum == 0) {
                // 当前玩家离开 剩余玩家 == 机器人数量 清除房间
                airplaneRoom.destroy();
                playerList.stream().filter(player -> player.getIsRobot() == 0)
                    .forEach(player -> MemManager.delMemberRec(player.getPlayerId()));
                clearRoom(airplaneRoom.getRoomId());
              }
              break;
            case 2:
              boolean last0 = (realNum == 1 && lastRobotNum == 2);
              boolean last1 = (realNum == 1 && lastRobotNum == 1);
              if (last0 || last1) {
                // 游戏继续
                airplaneRoom.setNowActionPlayer(nextPlayerId);
                exitBuilder.setNextUserID(nextPlayerId);
                exitBuilder.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.PLAYER_EXIT,
                        exitBuilder.build().toByteArray()), airplaneRoom.getRoomId());
              } else if (realNum == 0 && watchNum == 1 && lastRobotNum == 2) {
                // 游戏继续
                airplaneRoom.setNowActionPlayer(nextPlayerId);
                exitBuilder.setNextUserID(nextPlayerId);
                exitBuilder.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.PLAYER_EXIT,
                        exitBuilder.build().toByteArray()), airplaneRoom.getRoomId());
              } else {
                if (finishNum > 0) {
                  gameFinish1(roomId);
                } else {
                  compute(roomId);
                }
              }
              break;
            case 1:
              boolean last2 = (realNum == 2 && lastRobotNum == 1);
              boolean last3 = (realNum == 2 && lastRobotNum == 0);
              boolean last4 = (realNum == 1 && lastRobotNum == 0);
              if (last2 || last3) {
                // 游戏继续
                airplaneRoom.setNowActionPlayer(nextPlayerId);
                exitBuilder.setNextUserID(nextPlayerId);
                exitBuilder.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.PLAYER_EXIT,
                        exitBuilder.build().toByteArray()), airplaneRoom.getRoomId());
              } else if (realNum == 1 && lastRobotNum == 1) {
                // 游戏继续
                airplaneRoom.setNowActionPlayer(nextPlayerId);
                exitBuilder.setNextUserID(nextPlayerId);
                exitBuilder.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.PLAYER_EXIT,
                        exitBuilder.build().toByteArray()), airplaneRoom.getRoomId());
              } else if (last4 || realNum == 0) {
                if (finishNum > 0) {
                  gameFinish1(roomId);
                } else {
                  compute(roomId);
                }
              }
              break;
            default:
              if (realNum == 3) {
                // 游戏继续
                airplaneRoom.setNowActionPlayer(nextPlayerId);
                exitBuilder.setNextUserID(nextPlayerId);
                exitBuilder.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.PLAYER_EXIT,
                        exitBuilder.build().toByteArray()), airplaneRoom.getRoomId());
              } else if (realNum == 2) {
                // 游戏继续
                airplaneRoom.setNowActionPlayer(nextPlayerId);
                exitBuilder.setNextUserID(nextPlayerId);
                exitBuilder.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.PLAYER_EXIT,
                        exitBuilder.build().toByteArray()), airplaneRoom.getRoomId());
              } else {
                if (finishNum > 0) {
                  gameFinish1(roomId);
                } else {
                  compute(roomId);
                }
              }
              break;
          }
        } else {
          AirplanePlayer nowPlayer = airplaneRoom
              .getPlayerInfoById(airplaneRoom.getNowActionPlayer());
          List<AirplanePlayer> playerList = airplaneRoom.getPlayerList();
          int finishNum = airplaneRoom.getFinishRanking().size();
          int robotNum = airplaneRoom.getRobotNum();
          int lastRobotNum = (int) playerList.stream()
              .filter(robot -> robot.getIsRobot() == 0 && robot.getPlayerStatus() == 0).count();
          // 真实玩家 区分身份  区分状态 = 1
          int realNum = airplaneRoom.getRealPlayerNum();
          int watchNum = (int) playerList.stream()
              .filter(player -> player.getIsRobot() == 1
                  && player.getPlayerStatus() == 1
                  && player.getLinkStatus() == 0).count();
          switch (robotNum) {
            case 3:
              if (realNum == 0) {
                // 当前玩家离开 剩余玩家 == 机器人数量 清除房间
                airplaneRoom.destroy();
                playerList.stream().filter(player -> player.getIsRobot() == 0)
                    .forEach(player -> MemManager.delMemberRec(player.getPlayerId()));
                clearRoom(airplaneRoom.getRoomId());
              }
              break;
            case 2:
              boolean last0 = (realNum == 1 && lastRobotNum == 2);
              boolean last1 = (realNum == 1 && lastRobotNum == 1);
              if (last0 || last1) {
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.PLAYER_EXIT,
                        exitBuilder.build().toByteArray()), airplaneRoom.getRoomId());
                if (nowPlayer.getLatestDice() > 0 && nowPlayer.getLatestDice() < 6) {
                  airplaneRoom.setUpActionPlayer(nowPlayer.getPlayerId());
                }
              } else if (realNum == 0 && watchNum == 1 && lastRobotNum == 2) {
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.PLAYER_EXIT,
                        exitBuilder.build().toByteArray()), airplaneRoom.getRoomId());
                if (nowPlayer.getLatestDice() > 0 && nowPlayer.getLatestDice() < 6) {
                  airplaneRoom.setUpActionPlayer(nowPlayer.getPlayerId());
                }
              } else {
                if (finishNum > 0) {
                  gameFinish1(roomId);
                } else {
                  compute(roomId);
                }
              }
              break;
            case 1:
              boolean last2 = (realNum == 2 && lastRobotNum == 1);
              boolean last3 = (realNum == 2 && lastRobotNum == 0);
              if (last2 || last3) {
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.PLAYER_EXIT,
                        exitBuilder.build().toByteArray()), airplaneRoom.getRoomId());
                if (nowPlayer.getLatestDice() > 0 && nowPlayer.getLatestDice() < 6) {
                  airplaneRoom.setUpActionPlayer(nowPlayer.getPlayerId());
                }
              } else if (realNum == 1 && lastRobotNum == 1) {
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.PLAYER_EXIT,
                        exitBuilder.build().toByteArray()), airplaneRoom.getRoomId());
                if (nowPlayer.getLatestDice() > 0 && nowPlayer.getLatestDice() < 6) {
                  airplaneRoom.setUpActionPlayer(nowPlayer.getPlayerId());
                }
              } else {
                if (finishNum > 0) {
                  gameFinish1(roomId);
                } else {
                  compute(roomId);
                }
              }
              break;
            default:
              if (realNum == 3) {
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.PLAYER_EXIT,
                        exitBuilder.build().toByteArray()), airplaneRoom.getRoomId());
                if (nowPlayer.getLatestDice() > 0 && nowPlayer.getLatestDice() < 6) {
                  airplaneRoom.setUpActionPlayer(nowPlayer.getPlayerId());
                }
              } else if (realNum == 2) {
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.PLAYER_EXIT,
                        exitBuilder.build().toByteArray()), airplaneRoom.getRoomId());
                if (nowPlayer.getLatestDice() > 0 && nowPlayer.getLatestDice() < 6) {
                  airplaneRoom.setUpActionPlayer(nowPlayer.getPlayerId());
                }
              } else {
                if (finishNum > 0) {
                  gameFinish1(roomId);
                } else {
                  compute(roomId);
                }
              }
              break;
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 游戏结算.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/10 17:17
   * @update 2020/10/10 17:17
   */
  private void compute(Long roomId) {
    AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
    if (Objects.nonNull(airplaneRoom)) {
      List<AirplanePlayer> playerList = airplaneRoom.getPlayerList();
      List<AirplanePlayer> airplanePlayers = playerList.stream()
          .filter(player -> player.getLinkStatus() == 0)
          // 按终点飞机数量排序
          .sorted(Comparator.comparing(AirplanePlayer::getArrivedEndAirplaneNum))
          .collect(Collectors.toList());
      int index = 1;
      F30021.F300214S2C.Builder builder;
      for (AirplanePlayer airplanePlayer : airplanePlayers) {
        builder = F30021.F300214S2C.newBuilder();
        switch (index) {
          case 1:
            if (airplanePlayer.getIsRobot() == 1) {
              builder.setUserID(airplanePlayer.getPlayerId());
              if (MemManager.isExists(airplanePlayer.getPlayerId())) {
                builder.setIsDouble(1);
                builder.setAddExp(gainExperience(airplanePlayer.getPlayerId(), 12, 15));
              } else {
                builder.setAddExp(gainExperience(airplanePlayer.getPlayerId(), 6, 15));
              }
              builder.setAddGold(15);
            }
            index++;
            break;
          case 2:
            if (airplanePlayer.getIsRobot() == 1) {
              builder.setUserID(airplanePlayer.getPlayerId());
              if (MemManager.isExists(airplanePlayer.getPlayerId())) {
                builder.setIsDouble(1);
                builder.setAddExp(gainExperience(airplanePlayer.getPlayerId(), 8, 10));
              } else {
                builder.setAddExp(gainExperience(airplanePlayer.getPlayerId(), 4, 10));
              }
              builder.setAddGold(10);
            }
            index++;
            break;
          case 3:
            if (airplanePlayer.getIsRobot() == 1) {
              builder.setUserID(airplanePlayer.getPlayerId());
              if (MemManager.isExists(airplanePlayer.getPlayerId())) {
                builder.setIsDouble(1);
                builder.setAddExp(gainExperience(airplanePlayer.getPlayerId(), 4, 5));
              } else {
                builder.setAddExp(gainExperience(airplanePlayer.getPlayerId(), 2, 5));
              }
              builder.setAddGold(5);
            }
            index++;
            break;
          default:
            builder.setUserID(10000L);
            if (MemManager.isExists(airplanePlayer.getPlayerId())) {
              builder.setIsDouble(1);
            }
            builder.setAddExp(0);
            builder.setAddGold(0);
            break;
        }
        // 是否机器 [0-是 1-否]
        if (airplanePlayer.getIsRobot() == 1) {
          airplanePlayer.sendPacket(
              new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
                  builder.build().toByteArray()));
        }
      }
      playerList.stream().filter(player -> player.getIsRobot() == 1)
          .forEach(player -> achievementTask(roomId, player.getPlayerId()));
      playerList.stream().filter(player -> player.getIsRobot() == 0)
          .forEach(player -> MemManager.delMemberRec(player.getPlayerId()));
      clearRoom(airplaneRoom.getRoomId());
    }
  }

  /**
   * TODO 玩家退出.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:18
   * @update 2020/9/25 16:18
   */
  private void playerExit(Channel channel, Packet packet) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(airplaneRoom)) {
        AirplanePlayer isPlayer = airplaneRoom.getPlayerInfoById(packet.userId);
        if (Objects.nonNull(isPlayer)) {
          //  房间类型 [0-双人房间 1-四人房间]
          if (airplaneRoom.getRoomType() == 0) {
            playerExitRoom0(channel, packet);
          } else {
            if (isPlayer.getPlayerStatus() == 0) {
              playerExitRoom1(channel, packet);
            } else {
              closePage(channel, packet);
              if (this.redis.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redis.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
            }
          }
        } else {
          closePage(channel, packet);
          if (this.redis.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
            this.redis.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
          }
        }
      } else {
        closePage(channel, packet);
        clearRoom(packet.roomId);
        if (this.redis.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
          this.redis.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 玩家退出. 0
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:18
   * @update 2020/9/25 16:18
   */
  private void playerExitRoom0(Channel channel, Packet packet) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(airplaneRoom)) {
        // 房间状态 [0-未开始 1-已开始]
        if (airplaneRoom.getRoomStatus() == 1) {
          airplaneRoom.destroy();
          closePage(channel, packet);
          if (this.redis.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
            this.redis.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
          }
          achievementTask(packet.roomId, packet.userId);
          airplaneRoom.leaveGameRoom(packet.userId);
          AirplanePlayer airplanePlayer = airplaneRoom.getPlayerList().get(0);
          if (airplanePlayer.getIsRobot() == 0) {
            RobotManager.deleteGameRobot(airplanePlayer.getPlayerId());
            clearRoom(airplaneRoom.getRoomId());
          } else {
            F30021.F300214S2C.Builder builder = F30021.F300214S2C.newBuilder();
            builder.setUserID(airplanePlayer.getPlayerId());
            LocalDateTime startTime = airplaneRoom.getStartTimeIndex();
            LocalDateTime newTime = LocalDateTime.now();
            Duration duration = Duration.between(startTime, newTime);
            int seconds = (int) duration.getSeconds();
            if (seconds > AirplaneAssets.getInt(AirplaneAssets.SETTLEMENT_TIME)) {
              if (MemManager.isExists(airplanePlayer.getPlayerId())) {
                builder.setAddExp(gainExperience(airplanePlayer.getPlayerId(), 8, 10));
                builder.setIsDouble(1);
              } else {
                builder.setAddExp(gainExperience(airplanePlayer.getPlayerId(), 4, 10));
              }
              builder.setAddGold(10);
            } else {
              if (MemManager.isExists(airplanePlayer.getPlayerId())) {
                builder.setIsDouble(1);
              }
              builder.setAddExp(0).setAddGold(0);
            }
            airplanePlayer.sendPacket(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
                builder.build().toByteArray()));
            achievementTask(packet.roomId, airplanePlayer.getPlayerId());
            clearRoom(packet.roomId);
          }
        } else {
          if (airplaneRoom.getPlayerList().size() == 1) {
            airplaneRoom.destroy();
            closePage(channel, packet);
            if (this.redis.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
              this.redis.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
            }
            clearRoom(airplaneRoom.getRoomId());
          } else {
            if (airplaneRoom.getTimeoutMap().containsKey((int) AirplaneCmd.START_GAME)) {
              airplaneRoom.cancelTimeout((int) AirplaneCmd.START_GAME);
            }
            closePage(channel, packet);
            airplaneRoom.leaveGameRoom(packet.userId);
            if (this.redis.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
              this.redis.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
            }
            AirplanePlayer airplanePlayer = airplaneRoom.getPlayerList().get(0);
            if (airplanePlayer.getIsRobot() == 0) {
              RobotManager.deleteGameRobot(airplanePlayer.getPlayerId());
              clearRoom(airplaneRoom.getRoomId());
            } else {
              F30021.F300214S2C.Builder builder = F30021.F300214S2C.newBuilder();
              builder.setUserID(airplanePlayer.getPlayerId()).setAddExp(0).setAddGold(0);
              if (MemManager.isExists(airplanePlayer.getPlayerId())) {
                builder.setIsDouble(1);
              }
              airplanePlayer
                  .sendPacket(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
                      builder.build().toByteArray()));
              clearRoom(airplaneRoom.getRoomId());
            }
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 玩家退出. 1
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:18
   * @update 2020/9/25 16:18
   */
  private void playerExitRoom1(Channel channel, Packet packet) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(airplaneRoom)) {
        AirplanePlayer exitPlayer = airplaneRoom.getPlayerInfoById(packet.userId);
        if (airplaneRoom.getRoomStatus() == 1) {
          admitsDefeatRoom1(packet.roomId, packet.userId);
          closePage(channel, packet);
          if (this.redis.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
            this.redis.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
          }
        } else {
          airplaneRoom.leaveGameRoom(packet.userId);
          closePage(channel, packet);
          this.gameRemote.leaveRoom(packet.userId);
          if (this.redis.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
            this.redis.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
          }
          // 玩家离开
          F30021.F300217S2C.Builder exitBuilder = F30021.F300217S2C.newBuilder();
          exitBuilder.setResult(0).setUserID(packet.userId);
          // 基地的飞机
          List<AirplaneEntity> aircraftBaseList = exitPlayer.getAirplaneBaseList();
          if (CollectionUtils.isNotEmpty(aircraftBaseList)) {
            for (AirplaneEntity airplaneEntity : aircraftBaseList) {
              exitBuilder.addAircraftFly(airplaneEntity.getAirplaneId());
            }
          }
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.PLAYER_EXIT,
                  exitBuilder.build().toByteArray()), airplaneRoom.getRoomId());
          List<AirplanePlayer> playerList = airplaneRoom.getPlayerList();
          playerList = playerList.stream()
              // 连接状态 [0-连接中 1-已断开]   游戏状态 [0-游戏中 1-已结束 2-已离开(已认输)]
              .filter(player -> player.getLinkStatus() == 0 && player.getPlayerStatus() == 0)
              .collect(Collectors.toList());
          if (playerList.size() == 1) {
            AirplanePlayer airplanePlayer = airplaneRoom
                .getPlayerInfoById(playerList.get(0).getPlayerId());
            F30021.F300214S2C.Builder builder = F30021.F300214S2C.newBuilder();
            builder.setUserID(airplanePlayer.getPlayerId()).setAddExp(0).setAddGold(0);
            if (MemManager.isExists(airplanePlayer.getPlayerId())) {
              builder.setIsDouble(1);
            }
            airplanePlayer.sendPacket(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
                builder.build().toByteArray()));
            clearRoom(airplaneRoom.getRoomId());
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 关闭软件. 0
   *
   * @param roomId [房间ID]
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:18
   * @update 2020/9/25 16:18
   */
  private void playerExitRoomById0(Long roomId, Long playerId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        // 房间状态 [0-未开始 1-已开始]
        if (airplaneRoom.getRoomStatus() == 1) {
          airplaneRoom.destroy();
          achievementTask(roomId, playerId);
          airplaneRoom.leaveGameRoom(playerId);
          AirplanePlayer airplanePlayer = airplaneRoom.getPlayerList().get(0);
          if (airplanePlayer.getIsRobot() == 0) {
            RobotManager.deleteGameRobot(airplanePlayer.getPlayerId());
            clearRoom(airplaneRoom.getRoomId());
          } else {
            F30021.F300214S2C.Builder builder = F30021.F300214S2C.newBuilder();
            builder.setUserID(airplanePlayer.getPlayerId());
            LocalDateTime startTime = airplaneRoom.getStartTimeIndex();
            LocalDateTime newTime = LocalDateTime.now();
            Duration duration = Duration.between(startTime, newTime);
            int seconds = (int) duration.getSeconds();
            if (seconds > AirplaneAssets.getInt(AirplaneAssets.SETTLEMENT_TIME)) {
              if (MemManager.isExists(airplanePlayer.getPlayerId())) {
                builder.setAddExp(gainExperience(airplanePlayer.getPlayerId(), 8, 10));
                builder.setIsDouble(1);
              } else {
                builder.setAddExp(gainExperience(airplanePlayer.getPlayerId(), 4, 10));
              }
              builder.setAddGold(10);
            } else {
              if (MemManager.isExists(airplanePlayer.getPlayerId())) {
                builder.setIsDouble(1);
              }
              builder.setAddExp(0).setAddGold(0);
            }
            airplanePlayer.sendPacket(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
                builder.build().toByteArray()));
            achievementTask(roomId, airplanePlayer.getPlayerId());
            clearRoom(roomId);
          }
        } else {
          if (airplaneRoom.getPlayerList().size() == 1) {
            airplaneRoom.destroy();
            clearRoom(airplaneRoom.getRoomId());
          } else {
            if (airplaneRoom.getTimeoutMap().containsKey((int) AirplaneCmd.START_GAME)) {
              airplaneRoom.cancelTimeout((int) AirplaneCmd.START_GAME);
            }
            airplaneRoom.leaveGameRoom(playerId);
            AirplanePlayer airplanePlayer = airplaneRoom.getPlayerList().get(0);
            if (airplanePlayer.getIsRobot() == 0) {
              RobotManager.deleteGameRobot(airplanePlayer.getPlayerId());
              clearRoom(airplaneRoom.getRoomId());
            } else {
              F30021.F300214S2C.Builder builder = F30021.F300214S2C.newBuilder();
              builder.setUserID(airplanePlayer.getPlayerId()).setAddExp(0).setAddGold(0);
              if (MemManager.isExists(airplanePlayer.getPlayerId())) {
                builder.setIsDouble(1);
              }
              airplanePlayer
                  .sendPacket(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
                      builder.build().toByteArray()));
              clearRoom(airplaneRoom.getRoomId());
            }
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 关闭软件. 1
   *
   * @param roomId [房间ID]
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:18
   * @update 2020/9/25 16:18
   */
  private void playerExitRoomById1(Long roomId, Long playerId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        AirplanePlayer exitPlayer = airplaneRoom.getPlayerInfoById(playerId);
        if (airplaneRoom.getRoomStatus() == 1) {
          admitsDefeatRoom1(roomId, playerId);
        } else {
          airplaneRoom.leaveGameRoom(playerId);
          this.gameRemote.leaveRoom(playerId);
          // 玩家离开
          F30021.F300217S2C.Builder exitBuilder = F30021.F300217S2C.newBuilder();
          exitBuilder.setResult(0).setUserID(playerId);
          // 基地的飞机
          List<AirplaneEntity> aircraftBaseList = exitPlayer.getAirplaneBaseList();
          if (CollectionUtils.isNotEmpty(aircraftBaseList)) {
            for (AirplaneEntity airplaneEntity : aircraftBaseList) {
              exitBuilder.addAircraftFly(airplaneEntity.getAirplaneId());
            }
          }
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.PLAYER_EXIT,
                  exitBuilder.build().toByteArray()), airplaneRoom.getRoomId());
          List<AirplanePlayer> playerList = airplaneRoom.getPlayerList();
          playerList = playerList.stream()
              // 连接状态 [0-连接中 1-已断开]   游戏状态 [0-游戏中 1-已结束 2-已离开(已认输)]
              .filter(player -> player.getLinkStatus() == 0 && player.getPlayerStatus() == 0)
              .collect(Collectors.toList());
          if (playerList.size() == 1) {
            AirplanePlayer airplanePlayer = airplaneRoom
                .getPlayerInfoById(playerList.get(0).getPlayerId());
            F30021.F300214S2C.Builder builder = F30021.F300214S2C.newBuilder();
            builder.setUserID(airplanePlayer.getPlayerId()).setAddExp(0).setAddGold(0);
            if (MemManager.isExists(airplanePlayer.getPlayerId())) {
              builder.setIsDouble(1);
            }
            airplanePlayer.sendPacket(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
                builder.build().toByteArray()));
            clearRoom(airplaneRoom.getRoomId());
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 断线重连.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:18
   * @update 2020/9/25 16:18
   */
  private void disconnected(Channel channel, Packet packet) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(airplaneRoom)) {
        F30021.F300216S2C.Builder builder = F30021.F300216S2C.newBuilder();
        List<AirplanePlayer> playerList = airplaneRoom.getPlayerList();
        if (CollectionUtils.isNotEmpty(playerList)) {
          F30021.PlayerInfo.Builder playerInfo;
          for (AirplanePlayer player : playerList) {
            playerInfo = F30021.PlayerInfo.newBuilder();
            playerInfo.setNick(player.getPlayerName());
            playerInfo.setUserID(player.getPlayerId());
            playerInfo.setUrl(player.getPlayerIcon());
            playerInfo.setSex(player.getPlayerSex());
            playerInfo.setRed(player.getPlayerColor());
            playerInfo.setPlayerStatus(player.getPlayerStatus());
            F30021.MAircraftInfo.Builder aircraftBuilder;
            List<AirplaneEntity> airplaneEntities = player.getAirplaneBaseList();
            for (AirplaneEntity airplane : airplaneEntities) {
              aircraftBuilder = F30021.MAircraftInfo.newBuilder();
              aircraftBuilder.setAircraftID(airplane.getAirplaneId());
              aircraftBuilder.setNewPosition(airplane.getPositional());
              aircraftBuilder.setIsFinish(airplane.getIsFinish());
              playerInfo.addAircraftInfo(aircraftBuilder);
            }
            if (StringUtils.isNotEmpty(player.getAirplaneSkin())) {
              F30021.GameStyle.Builder gameStyle = F30021.GameStyle.newBuilder();
              gameStyle.setAircraftStyle(player.getAirplaneSkin());
              playerInfo.setGameStyle(gameStyle);
            }
            builder.addBreakLineInfo(playerInfo);
          }
        }
        List<AirplaneTrack> coordsList = new LinkedList<>(airplaneRoom.getTrackAirplane().values());
        if (CollectionUtils.isNotEmpty(coordsList)) {
          F30021.MAircraftInfo.Builder aircraftBuilder;
          for (AirplaneTrack airplaneTrack : coordsList) {
            if (airplaneTrack.getAirplaneList().size() > 0) {
              aircraftBuilder = F30021.MAircraftInfo.newBuilder();
              List<Integer> aircraftList = airplaneTrack.getAirplaneList();
              for (Integer integer : aircraftList) {
                aircraftBuilder.setAircraftID(integer);
                aircraftBuilder.setNewPosition(airplaneTrack.getTrackId());
                aircraftBuilder.setIsFinish(0);
                builder.addAircraftFly(aircraftBuilder);
              }
            }
          }
        }
        if (airplaneRoom.getRoomStatus() == 1) {
          builder.setActionUserId(airplaneRoom.getNowActionPlayer());
          LocalDateTime udt = airplaneRoom.getActionTimeIndex().plusSeconds(20L);
          LocalDateTime nds = LocalDateTime.now();
          Duration duration = Duration.between(nds, udt);
          int second = Math.toIntExact(duration.getSeconds());
          builder.setActionTime(second);
          channel.writeAndFlush(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.DISCONNECTED,
              builder.build().toByteArray()));
        } else {
          builder.setActionUserId(0).setActionTime(0);
          channel.writeAndFlush(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.DISCONNECTED,
              builder.build().toByteArray()));
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 刷新数据.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:48
   * @update 2020/9/25 16:48
   */
  private void refreshData(Channel channel, Packet packet) {
    try {
      ChatManager.refreshChatGroup(packet.roomId, channel);
      GroupManager.refreshRoomData(channel, packet);
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 关闭页面.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:23
   * @update 2020/9/25 16:23
   */
  private void closePage(Channel channel, Packet packet) {
    try {
      ChatManager.removeChannel(packet.roomId, channel);
      GroupManager.removeChannel(packet.roomId, channel);
      SoftChannel.sendPacketToUserId(
          new Packet(ActionCmd.APP_HEART, (short) 2, null), packet.userId);
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 关闭加载.
   *
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:21
   * @update 2020/9/25 16:21
   */
  private void closeLoading(Long playerId) {
    try {
      SoftChannel.sendPacketToUserId(
          new Packet(ActionCmd.APP_HEART, AirplaneCmd.CLOSE_LOADING, null), playerId);
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 空的玩家.
   *
   * @param channel [通信管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/7 21:05
   * @update 2020/9/7 21:05
   */
  private void playerNotExist(Channel channel) {
    F30021.F30021S2C.Builder builder = F30021.F30021S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.ENTER_ROOM,
            builder.setRoomType(0).setResult(2).build().toByteArray()));
  }

  /**
   * TODO 空的房间.
   *
   * @param channel [通信管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/8 14:16
   * @update 2020/9/8 14:16
   */
  private void roomNotExist(Channel channel) {
    F30021.F30021S2C.Builder builder = F30021.F30021S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.ENTER_ROOM,
            builder.setRoomType(0).setResult(1).build().toByteArray()));
  }

  /**
   * TODO 成就检测.
   *
   * @param roomId [房间ID]
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/27 4:31
   * @update 2020/9/27 4:31
   */
  private void achievementTask(Long roomId, Long playerId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        AirplanePlayer airplanePlayer = airplaneRoom.getPlayerInfoById(playerId);
        // 每日任务 玩1局飞行棋
        Map<String, Object> taskInfo = Maps.newHashMap();
        taskInfo.put("userId", airplanePlayer.getPlayerId());
        taskInfo.put("code", TaskEnum.PGT0003.getCode());
        taskInfo.put("progress", 1);
        taskInfo.put("isReset", 0);
        this.userRemote.taskHandler(taskInfo);
        if (airplanePlayer.getRegionalCircle() > 3) {
          // 玩家成就 原地转圈
          Map<String, Object> taskSuc0012 = Maps.newHashMap();
          taskSuc0012.put("userId", airplanePlayer.getPlayerId());
          taskSuc0012.put("code", AchievementEnum.AMT0012.getCode());
          taskSuc0012.put("progress", 1);
          taskSuc0012.put("isReset", 0);
          this.userRemote.achievementHandlers(taskSuc0012);
        }
        // 活动处理 丹枫迎秋
        Map<String, Object> activity = Maps.newHashMap();
        activity.put("userId", airplanePlayer.getPlayerId());
        activity.put("code", ActivityEnum.ACT000102.getCode());
        activity.put("progress", 1);
        this.activityRemote.openHandler(activity);
        // 玩家成就.高级玩家
        Map<String, Object> taskSuc0041 = Maps.newHashMap();
        taskSuc0041.put("userId", airplanePlayer.getPlayerId());
        taskSuc0041.put("code", AchievementEnum.AMT0041.getCode());
        taskSuc0041.put("progress", 1);
        taskSuc0041.put("isReset", 0);
        this.userRemote.achievementHandlers(taskSuc0041);
        // 玩家成就.头号玩家
        Map<String, Object> taskSuc0042 = Maps.newHashMap();
        taskSuc0042.put("userId", airplanePlayer.getPlayerId());
        taskSuc0042.put("code", AchievementEnum.AMT0042.getCode());
        taskSuc0042.put("progress", 1);
        taskSuc0042.put("isReset", 0);
        this.userRemote.achievementHandlers(taskSuc0042);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 游戏记录.
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:30
   * @update 2020/9/25 16:30
   */
  private void joinAirplaneRoom(Packet packet) {
    try {
      boolean checkTest = packet.roomId == AirplaneAssets.getLong(AirplaneAssets.TEST_ID);
      if (!checkTest) {
        Map<String, Object> result = Maps.newHashMap();
        result.put("userId", packet.userId);
        result.put("gameId", packet.channel);
        result.put("roomId", packet.roomId);
        this.gameRemote.enterRoom(result);
        this.gameRemote.refreshUserRecord(result);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 拉取装饰.
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:32
   * @update 2020/9/25 16:32
   */
  private void pullDecorateInfo(Packet packet) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(airplaneRoom)) {
        AirplanePlayer airplanePlayer = airplaneRoom.getPlayerInfoById(packet.userId);
        List gameDecorate = this.orderRemote.gameDecorate(packet.userId, ActionCmd.GAME_AEROPLANE);
        if (CollectionUtils.isNotEmpty(gameDecorate)) {
          List<Map<String, Object>> decorateList = JsonUtils.listMap(gameDecorate);
          if (CollectionUtils.isNotEmpty(decorateList)) {
            Map<String, Object> decorate = decorateList.get(0);
            if (Objects.nonNull(decorate)) {
              long productId = ((Number) decorate.get("productId")).longValue();
              Result result = this.gameRemote
                  .getGameSetAssetsList(productId, (long) ActionCmd.GAME_AEROPLANE);
              if (Objects.nonNull(result)) {
                List<Map<String, Object>> assetsList =
                    result.getCode().equals(0) ? JsonUtils.listMap(result.getData()) : null;
                if (CollectionUtils.isNotEmpty(assetsList)) {
                  for (Map<String, Object> assets : assetsList) {
                    if (airplanePlayer.getPlayerColor().equals(assets.get("assetsLabel"))) {
                      airplanePlayer.setAirplaneSkin(StringUtils.nvl(assets.get("assetsUrl")));
                      break;
                    }
                  }
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 游戏经验.
   *
   * @param playerId [玩家ID]
   * @param exp [玩家经验]
   * @param gold [获得金币]
   * @return [获得经验]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 19:14
   * @update 2020/9/25 19:14
   */
  private int gainExperience(Long playerId, Integer exp, Integer gold) {
    if (StringUtils.nvl(playerId).length() >= 9) {
      Map<String, Object> experienceInfo = Maps.newHashMap();
      experienceInfo.put("userId", playerId);
      experienceInfo.put("experience", exp);
      experienceInfo.put("gold", gold);
      Result result = this.userRemote.gameHandler(experienceInfo);
      if (Objects.nonNull(result)) {
        Map<String, Object> callbackInfo =
            result.getCode().equals(0) ? JsonUtils.toObjectMap(result.getData()) : null;
        if (Objects.nonNull(callbackInfo)) {
          Integer remainderExp = (Integer) callbackInfo.get("remainderExp");
          if (remainderExp > 0) {
            return exp;
          } else {
            // 今日经验已满 返回0经验
            return 0;
          }
        } else {
          // 经验API调用失败
          return exp;
        }
      } else {
        // 经验API调用失败
        return exp;
      }
    }
    // 测试经验返回
    return exp;
  }

  /**
   * TODO 清除数据.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 19:14
   * @update 2020/9/25 19:14
   */
  private void clearRoom(Long roomId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        GAME_DATA.remove(roomId);
        this.gameRemote.deleteRoom(roomId);
        if (this.redis.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId)) {
          this.redis.del(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
        }
        ChatManager.delChatGroup(roomId);
        GroupManager.delRoomGroup(roomId);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 等待玩家. 30(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 15:20
   * @update 2020/9/25 15:20
   */
  private void waitTimeout(Long roomId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        if (!airplaneRoom.getTimeoutMap().containsKey((int) AirplaneCmd.WAIT_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> waitExamine(roomId)
              ), 30, TimeUnit.SECONDS);
          airplaneRoom.addTimeOut((int) AirplaneCmd.WAIT_TIME, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 等待操作. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 15:21
   * @update 2020/9/25 15:21
   */
  private void waitExamine(Long roomId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        airplaneRoom.removeTimeout((int) AirplaneCmd.WAIT_TIME);
        int playerSize = airplaneRoom.getPlayerList().size();
        if (playerSize == 0) {
          airplaneRoom.destroy();
          clearRoom(roomId);
        } else if (playerSize == 1) {
          airplaneRoom.destroy();
          AirplanePlayer airplanePlayer = airplaneRoom.getPlayerList().get(0);
          F30021.F300214S2C.Builder builder = F30021.F300214S2C.newBuilder();
          builder.setUserID(airplanePlayer.getPlayerId());
          if (MemManager.isExists(airplanePlayer.getPlayerId())) {
            builder.setIsDouble(1);
          }
          airplanePlayer.sendPacket(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
              builder.setAddExp(0).setAddGold(0).build().toByteArray()));
          clearRoom(roomId);
        } else {
          startTimeout(airplaneRoom.getRoomId());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 游戏定时. 3(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 15:55
   * @update 2020/9/25 15:55
   */
  private void startTimeout(Long roomId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        if (!airplaneRoom.getTimeoutMap().containsKey((int) AirplaneCmd.START_GAME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> startExamine(roomId)
              ), 3, TimeUnit.SECONDS);
          airplaneRoom.addTimeOut((int) AirplaneCmd.START_GAME, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 开始游戏. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 15:57
   * @update 2020/9/25 15:57
   */
  private void startExamine(Long roomId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        airplaneRoom.cancelTimeout((int) AirplaneCmd.WAIT_TIME);
        airplaneRoom.removeTimeout((int) AirplaneCmd.START_GAME);
        int playerSize = airplaneRoom.getPlayerList().size();
        if (playerSize == 0) {
          airplaneRoom.destroy();
          clearRoom(roomId);
        } else if (playerSize == 1) {
          airplaneRoom.destroy();
          AirplanePlayer airplanePlayer = airplaneRoom.getPlayerList().get(0);
          F30021.F300214S2C.Builder builder = F30021.F300214S2C.newBuilder();
          builder.setUserID(airplanePlayer.getPlayerId());
          if (MemManager.isExists(airplanePlayer.getPlayerId())) {
            builder.setIsDouble(1);
          }
          airplanePlayer.sendPacket(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
              builder.setAddExp(0).setAddGold(0).build().toByteArray()));
          clearRoom(roomId);
        } else {
          airplaneRoom.setRoomStatus(1);
          // 开始游戏 设置玩家
          airplaneRoom.setUpActionPlayer(0L);
          F30021.F300211S2C.Builder builder = F30021.F300211S2C.newBuilder();
          builder.setActionUserId(airplaneRoom.getNowActionPlayer());
          builder.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
          GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.START_GAME,
              builder.build().toByteArray()), airplaneRoom.getRoomId());
          airplaneRoom.setStartTimeIndex(LocalDateTime.now());
          AirplanePlayer nextPlayer = airplaneRoom.getPlayerInfoById(builder.getActionUserId());
          // 是否机器 [0-是 1-否]
          if (nextPlayer.getIsRobot() == 0) {
            robotStartDiceTimeout(airplaneRoom.getRoomId());
          } else {
            // 开启托管 [0-关闭 1-开启]
            if (nextPlayer.getStartEscrow() == 1) {
              autoStartDiceTimeout(airplaneRoom.getRoomId());
            } else {
              // 添加操作定时
              actionTimeout(airplaneRoom.getRoomId());
            }
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 操作定时. 20(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:02
   * @update 2020/9/25 16:02
   */
  private void actionTimeout(Long roomId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        if (!airplaneRoom.getTimeoutMap().containsKey((int) AirplaneCmd.ACTION_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> actionExamine(roomId)
              ), 20, TimeUnit.SECONDS);
          airplaneRoom.addTimeOut((int) AirplaneCmd.ACTION_TIME, timeout);
          airplaneRoom.setActionTimeIndex(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 操作超时. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 16:03
   * @update 2020/9/25 16:03
   */
  private void actionExamine(Long roomId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        airplaneRoom.removeTimeout((int) AirplaneCmd.ACTION_TIME);
        AirplanePlayer airplanePlayer = airplaneRoom
            .getPlayerInfoById(airplaneRoom.getNowActionPlayer());
        airplanePlayer.setStartEscrow(1);
        if (airplanePlayer.getLatestDice() == 0) {
          autoStartDiceExamine(roomId);
        } else {
          airplanePlayer.setTrusteeship(airplanePlayer.getTrusteeship() + 1);
          autoMovesChessExamine(roomId);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 自动掷骰. 10(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 15:20
   * @update 2020/9/25 15:20
   */
  private void autoStartDiceTimeout(Long roomId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        if (!airplaneRoom.getTimeoutMap().containsKey((int) AirplaneCmd.AUTO_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> autoStartDiceExamine(roomId)
              ), 8, TimeUnit.SECONDS);
          airplaneRoom.addTimeOut((int) AirplaneCmd.AUTO_TIME, timeout);
          airplaneRoom.setActionTimeIndex(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 自动操作. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 15:21
   * @update 2020/9/25 15:21
   */
  private void autoStartDiceExamine(Long roomId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        airplaneRoom.removeTimeout((int) AirplaneCmd.AUTO_TIME);
        long nowPlayer = airplaneRoom.getNowActionPlayer();
        AirplanePlayer airplanePlayer = airplaneRoom.getPlayerInfoById(nowPlayer);
        // 检查自动次数 托管次数 10
        if (airplanePlayer.getTrusteeship() >= 10) {
          if (airplaneRoom.getRoomType() == 0) {
            // 检查终点飞机数量
            AirplanePlayer opponentInfo = airplaneRoom.getPlayerList()
                .stream().filter(player -> !Objects.equals(player.getPlayerId(), nowPlayer))
                .findAny().orElse(null);
            if (Objects.nonNull(opponentInfo)) {
              // 平局结束游戏
              if (opponentInfo.getStartEscrow() == 1 && opponentInfo.getArrivedEndAirplaneNum() == 0
                  && airplanePlayer.getArrivedEndAirplaneNum() == 0) {
                airplaneRoom.destroy();
                F30021.F300214S2C.Builder finishBuilder = F30021.F300214S2C.newBuilder();
                finishBuilder.setUserID(0).setAddExp(0).setAddGold(0);
                GroupManager
                    .sendPacketToGroup(new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
                            finishBuilder.setAddExp(0).setAddGold(0).build().toByteArray()),
                        airplaneRoom.getRoomId());
                List<AirplanePlayer> playerList = airplaneRoom.getPlayerList();
                // 玩家成就
                playerList.stream().filter(player -> player.getIsRobot() == 1)
                    .forEach(player -> {
                    });
                clearRoom(roomId);
              } else {
                admitsDefeatRoom0(roomId, nowPlayer);
              }
            }
          } else {
            admitsDefeatRoom1(roomId, nowPlayer);
          }
        } else {
          F30021.F300212S2C.Builder builder = F30021.F300212S2C.newBuilder();
          // 骰子数值
          int diceNumber = ThreadLocalRandom.current().nextInt(6) + 1;
          airplanePlayer.setLatestDice(diceNumber);
          builder.setDiceNum(diceNumber);
          builder.setActionUserId(nowPlayer);
          if (diceNumber == 6) {
            F30021.F300212S2C.Builder airplaneBuilder = airplaneRoom
                .airplaneAllList(airplanePlayer.getPlayerId());
            builder.addAllAircraftInfo(airplaneBuilder.getAircraftInfoList());
            builder.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
            builder.setNextUserID(airplanePlayer.getPlayerId());
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.START_DICE,
                    builder.build().toByteArray()), airplaneRoom.getRoomId());
            autoMovesChessTimeout(roomId);
          } else {
            F30021.F300212S2C.Builder airplaneBuilder = airplaneRoom
                .airplaneFlyList(airplanePlayer.getPlayerId());
            if (CollectionUtils.isNotEmpty(airplaneBuilder.getAircraftInfoList())) {
              builder.addAllAircraftInfo(airplaneBuilder.getAircraftInfoList());
              builder.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
              builder.setNextUserID(airplanePlayer.getPlayerId());
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.START_DICE,
                      builder.build().toByteArray()), airplaneRoom.getRoomId());
              autoMovesChessTimeout(roomId);
            } else {
              airplanePlayer.setLatestDice(0);
              airplaneRoom.setUpActionPlayer(airplanePlayer.getPlayerId());
              builder.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
              builder.setNextUserID(airplaneRoom.getNowActionPlayer());
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.START_DICE,
                      builder.build().toByteArray()), airplaneRoom.getRoomId());
              AirplanePlayer nextPlayer = airplaneRoom.getPlayerInfoById(builder.getNextUserID());
              // 是否机器 [0-是 1-否]
              if (nextPlayer.getIsRobot() == 0) {
                robotStartDiceTimeout(airplaneRoom.getRoomId());
              } else {
                // 开启托管 [0-关闭 1-开启]
                if (nextPlayer.getStartEscrow() == 1) {
                  autoStartDiceTimeout(airplaneRoom.getRoomId());
                } else {
                  // 添加操作定时
                  actionTimeout(airplaneRoom.getRoomId());
                }
              }

            }
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 自动走棋. 10(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 15:20
   * @update 2020/9/25 15:20
   */
  private void autoMovesChessTimeout(Long roomId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        if (!airplaneRoom.getTimeoutMap().containsKey((int) AirplaneCmd.AUTO_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> autoMovesChessExamine(roomId)
              ), 5, TimeUnit.SECONDS);
          airplaneRoom.addTimeOut((int) AirplaneCmd.AUTO_TIME, timeout);
          airplaneRoom.setActionTimeIndex(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 自动操作. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 15:21
   * @update 2020/9/25 15:21
   */
  private void autoMovesChessExamine(Long roomId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        airplaneRoom.removeTimeout((int) AirplaneCmd.AUTO_TIME);
        AirplanePlayer airplanePlayer = airplaneRoom.getPlayerInfoById(airplaneRoom.getNowActionPlayer());
        airplanePlayer.setTrusteeship(airplanePlayer.getTrusteeship() + 1);
        int airplaneId = airplaneRoom.explore(airplanePlayer.getPlayerId());
        AirplaneEntity airplane = airplaneRoom.getAirplaneInfo(airplanePlayer.getPlayerId(), airplaneId);
        if (Objects.nonNull(airplane)) {
          Integer initNumber = airplane.getPositional();
          int diceNumber = airplanePlayer.getLatestDice();
          int endNumber = initNumber + diceNumber;
          FlightProcess flightProcess = new FlightProcess();
          flightProcess.setRoomId(roomId);
          flightProcess.setPlayerId(airplanePlayer.getPlayerId());
          flightProcess.setInitValue(initNumber);
          flightProcess.setEndValue(endNumber);
          flightProcess.setAirplaneId(airplaneId);
          if (initNumber >= 60 && initNumber < 70) {
            flightProcess.setMaxValue(65);
            goInEndZone(flightProcess);
          } else if (initNumber >= 70 && initNumber < 80) {
            flightProcess.setMaxValue(75);
            goInEndZone(flightProcess);
          } else if (initNumber >= 80 && initNumber < 90) {
            flightProcess.setMaxValue(85);
            goInEndZone(flightProcess);
          } else if (initNumber >= 90 && initNumber < 100) {
            flightProcess.setMaxValue(95);
            goInEndZone(flightProcess);
          } else {
            flightProcess.setDiceValue(diceNumber);
            goInFlyZone(flightProcess, airplane);
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 机器掷骰. 3(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 15:20
   * @update 2020/9/25 15:20
   */
  private void robotStartDiceTimeout(Long roomId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        if (!airplaneRoom.getTimeoutMap().containsKey((int) AirplaneCmd.ROBOT_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> robotStartDiceExamine(roomId)
              ), 3, TimeUnit.SECONDS);
          airplaneRoom.addTimeOut((int) AirplaneCmd.ROBOT_TIME, timeout);
          airplaneRoom.setActionTimeIndex(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 机器操作. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 15:21
   * @update 2020/9/25 15:21
   */
  private void robotStartDiceExamine(Long roomId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        airplaneRoom.removeTimeout((int) AirplaneCmd.ROBOT_TIME);
        // 骰子数值
        int diceNumber = ThreadLocalRandom.current().nextInt(6) + 1;
        AirplanePlayer airplanePlayer = airplaneRoom
            .getPlayerInfoById(airplaneRoom.getNowActionPlayer());
        airplanePlayer.setLatestDice(diceNumber);
        F30021.F300212S2C.Builder response = F30021.F300212S2C.newBuilder();
        response.setDiceNum(diceNumber);
        response.setActionUserId(airplanePlayer.getPlayerId());
        if (diceNumber == 6) {
          F30021.F300212S2C.Builder airplaneBuilder = airplaneRoom
              .airplaneAllList(airplanePlayer.getPlayerId());
          response.addAllAircraftInfo(airplaneBuilder.getAircraftInfoList());
          response.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
          response.setNextUserID(airplanePlayer.getPlayerId());
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.START_DICE,
                  response.build().toByteArray()), airplaneRoom.getRoomId());
          robotMovesChessTimeout(roomId);
        } else {
          F30021.F300212S2C.Builder airplaneBuilder = airplaneRoom
              .airplaneFlyList(airplanePlayer.getPlayerId());
          if (CollectionUtils.isNotEmpty(airplaneBuilder.getAircraftInfoList())) {
            response.addAllAircraftInfo(airplaneBuilder.getAircraftInfoList());
            response.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
            response.setNextUserID(airplanePlayer.getPlayerId());
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.START_DICE,
                    response.build().toByteArray()), airplaneRoom.getRoomId());
            robotMovesChessTimeout(roomId);
          } else {
            airplanePlayer.setLatestDice(0);
            airplaneRoom.setUpActionPlayer(airplanePlayer.getPlayerId());
            response.setActionTime(AirplaneAssets.getInt(AirplaneAssets.SHOW_TIMEOUT));
            response.setNextUserID(airplaneRoom.getNowActionPlayer());
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.START_DICE,
                    response.build().toByteArray()), airplaneRoom.getRoomId());
            AirplanePlayer nextPlayer = airplaneRoom.getPlayerInfoById(response.getNextUserID());
            // 是否机器 [0-是 1-否]
            if (nextPlayer.getIsRobot() == 0) {
              robotStartDiceTimeout(airplaneRoom.getRoomId());
            } else {
              // 开启托管 [0-关闭 1-开启]
              if (nextPlayer.getStartEscrow() == 1) {
                autoStartDiceTimeout(airplaneRoom.getRoomId());
              } else {
                // 添加操作定时
                actionTimeout(airplaneRoom.getRoomId());
              }
            }
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 机器走棋. 3(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 15:20
   * @update 2020/9/25 15:20
   */
  private void robotMovesChessTimeout(Long roomId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        if (!airplaneRoom.getTimeoutMap().containsKey((int) AirplaneCmd.ROBOT_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> robotMovesChessExamine(roomId)
              ), 3, TimeUnit.SECONDS);
          airplaneRoom.addTimeOut((int) AirplaneCmd.ROBOT_TIME, timeout);
          airplaneRoom.setActionTimeIndex(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 机器操作. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/25 15:21
   * @update 2020/9/25 15:21
   */
  private void robotMovesChessExamine(Long roomId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        airplaneRoom.removeTimeout((int) AirplaneCmd.ROBOT_TIME);
        AirplanePlayer airplanePlayer = airplaneRoom.getPlayerInfoById(airplaneRoom.getNowActionPlayer());
        int airplaneId = airplaneRoom.explore(airplanePlayer.getPlayerId());
        AirplaneEntity airplane = airplaneRoom.getAirplaneInfo(airplanePlayer.getPlayerId(), airplaneId);
        if (Objects.nonNull(airplane)) {
          Integer initNumber = airplane.getPositional();
          int diceNumber = airplanePlayer.getLatestDice();
          int endNumber = initNumber + diceNumber;
          FlightProcess flightProcess = new FlightProcess();
          flightProcess.setRoomId(roomId);
          flightProcess.setPlayerId(airplanePlayer.getPlayerId());
          flightProcess.setInitValue(initNumber);
          flightProcess.setEndValue(endNumber);
          flightProcess.setAirplaneId(airplaneId);
          if (initNumber >= 60 && initNumber < 70) {
            flightProcess.setMaxValue(65);
            goInEndZone(flightProcess);
          } else if (initNumber >= 70 && initNumber < 80) {
            flightProcess.setMaxValue(75);
            goInEndZone(flightProcess);
          } else if (initNumber >= 80 && initNumber < 90) {
            flightProcess.setMaxValue(85);
            goInEndZone(flightProcess);
          } else if (initNumber >= 90 && initNumber < 100) {
            flightProcess.setMaxValue(95);
            goInEndZone(flightProcess);
          } else {
            flightProcess.setDiceValue(diceNumber);
            goInFlyZone(flightProcess, airplane);
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 结算展示. 1(s)
   *
   * @param roomId [房间ID]
   * @param winPlayer [胜利玩家]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/28 19:50
   * @update 2020/9/28 19:50
   */
  private void gameFinish0(Long roomId, Long winPlayer) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        if (!airplaneRoom.getTimeoutMap().containsKey((int) AirplaneCmd.GAME_FINISH)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> pushGameFinishExamine0(roomId, winPlayer)
              ), 1, TimeUnit.SECONDS);
          airplaneRoom.addTimeOut((int) AirplaneCmd.GAME_FINISH, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 推送结算. ◕
   *
   * @param roomId [房间ID]
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/28 20:35
   * @update 2020/9/28 20:35
   */
  private void pushGameFinishExamine0(Long roomId, Long playerId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        airplaneRoom.removeTimeout((int) AirplaneCmd.GAME_FINISH);
        List<AirplanePlayer> airplanePlayers = airplaneRoom.getPlayerList();
        F30021.F300214S2C.Builder builder = F30021.F300214S2C.newBuilder();
        builder.setUserID(playerId);
        if (Objects.equals(airplanePlayers.get(0).getPlayerId(), playerId)) {
          AirplanePlayer winPlayer = airplanePlayers.get(0);
          if (winPlayer.getIsRobot() == 1) {
            if (MemManager.isExists(playerId)) {
              builder.setIsDouble(1);
              builder.setAddExp(gainExperience(playerId, 8, 10));
            } else {
              builder.setAddExp(4);
            }
            builder.setAddGold(10);
            winPlayer.sendPacket(
                new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
                    builder.build().toByteArray()));
            builder.clearIsDouble();
          }
          AirplanePlayer losePlayer = airplanePlayers.get(1);
          if (losePlayer.getIsRobot() == 1) {
            if (MemManager.isExists(losePlayer.getPlayerId())) {
              builder.setIsDouble(1);
            }
            builder.setAddExp(0).setAddGold(0);
            losePlayer.sendPacket(
                new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
                    builder.build().toByteArray()));
          }
        } else {
          AirplanePlayer winPlayer = airplanePlayers.get(1);
          if (winPlayer.getIsRobot() == 1) {
            if (MemManager.isExists(playerId)) {
              builder.setIsDouble(1);
              builder.setAddExp(gainExperience(playerId, 8, 10));
            } else {
              builder.setAddExp(4);
            }
            builder.setAddGold(10);
            winPlayer.sendPacket(
                new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
                    builder.build().toByteArray()));
            builder.clearIsDouble();
          }
          AirplanePlayer losePlayer = airplanePlayers.get(2);
          if (losePlayer.getIsRobot() == 1) {
            if (MemManager.isExists(losePlayer.getPlayerId())) {
              builder.setIsDouble(1);
            }
            builder.setAddExp(0).setAddGold(0);
            losePlayer.sendPacket(
                new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
                    builder.build().toByteArray()));
          }
        }
        airplanePlayers.stream().filter(player -> player.getIsRobot() == 1)
            .forEach(player -> achievementTask(roomId, player.getPlayerId()));
        airplanePlayers.stream().filter(player -> player.getIsRobot() == 0)
            .forEach(player -> MemManager.delMemberRec(player.getPlayerId()));
        clearRoom(airplaneRoom.getRoomId());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 结算展示. 1(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/28 19:50
   * @update 2020/9/28 19:50
   */
  private void gameFinish1(Long roomId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        if (!airplaneRoom.getTimeoutMap().containsKey((int) AirplaneCmd.GAME_FINISH)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> pushGameFinishExamine1(roomId)
              ), 1, TimeUnit.SECONDS);
          airplaneRoom.addTimeOut((int) AirplaneCmd.GAME_FINISH, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 推送结算. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/28 20:35
   * @update 2020/9/28 20:35
   */
  private void pushGameFinishExamine1(Long roomId) {
    try {
      AirplaneRoom airplaneRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(airplaneRoom)) {
        airplaneRoom.removeTimeout((int) AirplaneCmd.GAME_FINISH);
        List<Long> finishRanking = airplaneRoom.getFinishRanking();
        List<AirplanePlayer> airplanePlayers = airplaneRoom.getPlayerList();
        airplanePlayers = airplanePlayers.stream()
            // 连接状态 [0-连接中 1-已断开]
            .filter(player -> player.getLinkStatus() == 0
                //  游戏状态 [0-游戏中 1-已结束 2-已离开(已认输)]                             结束标记
                && (player.getPlayerStatus() == 0 || player.getPlayerStatus() == 1 || player.getFinishIndex() == 1))
            .sorted(Comparator.comparing(AirplanePlayer::getArrivedEndAirplaneNum))
            .collect(Collectors.toList());
        List<AirplanePlayer> finalAirplanePlayers = airplanePlayers;
        airplanePlayers.sort((o1, o2) -> {
          long flag1 = o1.getPlayerId();
          long flag2 = o2.getPlayerId();
          int io1 = finishRanking.indexOf(flag1);
          int io2 = finishRanking.indexOf(flag2);
          if (io1 != -1) {
            io1 = finalAirplanePlayers.size() - io1;
          }
          if (io2 != -1) {
            io2 = finalAirplanePlayers.size() - io2;
          }
          return io2 - io1;
        });
        int index = 1;
        F30021.F300214S2C.Builder builder;
        for (AirplanePlayer airplanePlayer : finalAirplanePlayers) {
          builder = F30021.F300214S2C.newBuilder();
          switch (index) {
            case 1:
              builder.setUserID(airplanePlayer.getPlayerId());
              if (MemManager.isExists(airplanePlayer.getPlayerId())) {
                builder.setIsDouble(1);
                builder.setAddExp(12);
              } else {
                builder.setAddExp(6);
              }
              builder.setAddGold(15);
              index++;
              break;
            case 2:
              builder.setUserID(airplanePlayer.getPlayerId());
              if (MemManager.isExists(airplanePlayer.getPlayerId())) {
                builder.setIsDouble(1);
                builder.setAddExp(8);
              } else {
                builder.setAddExp(4);
              }
              builder.setAddGold(10);
              index++;
              break;
            case 3:
              builder.setUserID(airplanePlayer.getPlayerId());
              if (MemManager.isExists(airplanePlayer.getPlayerId())) {
                builder.setIsDouble(1);
                builder.setAddExp(4);
              } else {
                builder.setAddExp(2);
              }
              builder.setAddGold(5);
              index++;
              break;
            default:
              builder.setUserID(10000L);
              if (MemManager.isExists(airplanePlayer.getPlayerId())) {
                builder.setIsDouble(1);
              }
              builder.setAddExp(0);
              builder.setAddGold(0);
              break;
          }
          // 是否机器 [0-是 1-否]
          if (airplanePlayer.getIsRobot() == 1) {
            airplanePlayer.sendPacket(
                new Packet(ActionCmd.GAME_AEROPLANE, AirplaneCmd.GAME_FINISH,
                    builder.build().toByteArray()));
          }
        }
        List<AirplanePlayer> playerList = airplaneRoom.getPlayerList();
        playerList.stream().filter(player -> player.getIsRobot() == 1)
            .forEach(player -> achievementTask(roomId, player.getPlayerId()));
        playerList.stream().filter(player -> player.getIsRobot() == 0)
            .forEach(player -> MemManager.delMemberRec(player.getPlayerId()));
        clearRoom(airplaneRoom.getRoomId());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }
}
