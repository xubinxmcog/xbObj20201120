package com.enuos.live.handle.game.f30011;

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
import com.enuos.live.proto.f30011msg.F30011;
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
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.util.Timeout;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * TODO 斗兽战纪.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/5/15 14:36
 */

@Component
@AbstractAction(cmd = ActionCmd.GAME_ANIMAL)
public class Animal extends AbstractActionHandler {

  /** 房间游戏数据. */
  private static ConcurrentHashMap<Long, AnimalRoom> GAME_DATA = new ConcurrentHashMap<>();

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
  private RedisUtils redisUtils;

  /**
   * TODO 操作分发.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/5 7:03
   * @update 2020/9/17 15:23
   */
  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      switch (packet.child) {
        case AnimalCmd.ENTER_ROOM:
          enterRoom(channel, packet);
          break;
        case AnimalCmd.PLAYER_MOVES:
          playerMoves(channel, packet);
          break;
        case AnimalCmd.FLIP_POSITION:
          flipPosition(channel, packet);
          break;
        case AnimalCmd.CONFESS:
          confess(channel, packet);
          break;
        case AnimalCmd.CLOSE_GAME:
          closeGame(channel, packet);
          break;
        default:
          LoggerManager.warn("[GAME 30011 HANDLE] CHILD ERROR: [{}]", packet.child);
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
   * @create 2020/5/25 6:26
   * @update 2020/9/23 14:08
   */
  @Override
  public void shutOff(Long userId, Long attachId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(attachId);
      if (Objects.nonNull(animalRoom)) {
        AnimalPlayer checkPlayer = animalRoom.getGamePlayer(userId);
        if (Objects.nonNull(checkPlayer)) {
          if (animalRoom.getGameStatus() == 1) {
            animalRoom.destroy();
            animalRoom.leaveGame(userId);
            AnimalPlayer player = animalRoom.getPlayerList().get(0);
            if (player.getIsRobot() == 0) {
              RobotManager.deleteGameRobot(player.getUserId());
              clearData(attachId);
            } else {
              F30011.F300114S2C.Builder builder = F30011.F300114S2C.newBuilder();
              builder.setUserID(player.getUserId());
              LocalDateTime startTime = animalRoom.getStartTime();
              LocalDateTime newTime = LocalDateTime.now();
              Duration duration = Duration.between(startTime, newTime);
              int durations = (int) duration.getSeconds();
              if (durations > AnimalAssets.getInt(AnimalAssets.SETTLEMENT_TIME)) {
                if (MemManager.isExists(player.getUserId())) {
                  builder.setAddExp(gainExperience(player.getUserId(), 4));
                  builder.setIsDouble(1);
                } else {
                  builder.setAddExp(gainExperience(player.getUserId(), 2));
                }
                builder.setAddGlod(10);
              } else {
                if (MemManager.isExists(player.getUserId())) {
                  builder.setIsDouble(1);
                }
                builder.setAddExp(0);
                builder.setAddGlod(0);
              }
              player.sendPacket(new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.SETTLEMENT, builder.build().toByteArray()));
              // 每日任务.玩3局斗兽棋
              Map<String, Object> taskInfo1 = Maps.newHashMap();
              taskInfo1.put("userId", player.getUserId());
              taskInfo1.put("code", TaskEnum.PGT0002.getCode());
              taskInfo1.put("progress", 1);
              taskInfo1.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo1);
              // 活动处理 丹枫迎秋
              Map<String, Object> activity = Maps.newHashMap();
              activity.put("userId", player.getUserId());
              activity.put("code", ActivityEnum.ACT000102.getCode());
              activity.put("progress", 1);
              this.activityRemote.openHandler(activity);
              // 每日任务.赢1局斗兽棋
              Map<String, Object> taskInfo2 = Maps.newHashMap();
              taskInfo2.put("userId", builder.getUserID());
              taskInfo2.put("code", TaskEnum.PGT0011.getCode());
              taskInfo2.put("progress", 1);
              taskInfo2.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo2);
              achievementTask(attachId);
              clearData(attachId);
            }
          } else {
            if (animalRoom.getPlayerList().size() == 1) {
              animalRoom.destroy();
              AnimalPlayer player = animalRoom.getPlayerList().get(0);
              if (player.getIsRobot() == 0) {
                RobotManager.deleteGameRobot(player.getUserId());
              }
              clearData(animalRoom.getRoomId());
            } else {
              if (animalRoom.getTimeOutMap().containsKey((int) AnimalCmd.START_GAME)) {
                animalRoom.cancelTimeOut((int) AnimalCmd.START_GAME);
              }
              animalRoom.leaveGame(userId);
              AnimalPlayer player = animalRoom.getPlayerList().get(0);
              if (player.getIsRobot() == 0) {
                RobotManager.deleteGameRobot(player.getUserId());
                clearData(animalRoom.getRoomId());
              } else {
                F30011.F300114S2C.Builder builder = F30011.F300114S2C.newBuilder();
                builder.setUserID(player.getUserId()).setAddExp(0).setAddGlod(0);
                if (MemManager.isExists(player.getUserId())) {
                  builder.setIsDouble(1);
                }
                player.sendPacket(
                    new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.SETTLEMENT,
                        builder.build().toByteArray()));
                clearData(animalRoom.getRoomId());
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
   * TODO 清除处理.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/14 21:00
   * @update 2020/9/14 21:00
   */
  @Override
  public void cleaning(Long roomId) {
    GAME_DATA.remove(roomId);
    this.gameRemote.deleteRoom(roomId);
    ChatManager.delChatGroup(roomId);
    GroupManager.delRoomGroup(roomId);
  }

  /**
   * TODO 陪玩处理.
   *
   * @param roomId [房间ID]
   * @param playerIds [机器人信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/21 10:41
   * @update 2020/8/21 10:41
   */
  @Override
  public void joinRobot(Long roomId, List<Long> playerIds) {
    try {
      register(TimerEventLoop.timeGroup.next());
      GAME_DATA.computeIfAbsent(roomId, key -> new AnimalRoom(roomId));
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      long playerId = playerIds.get(0);
      GameRobot gameRobot = RobotManager.getGameRobot(playerId);
      animalRoom.joinRobot(gameRobot);
      // 添加定时 时间到.清除房间
      robotWaitTimeout(roomId);
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
   * @create 2020/5/25 6:26
   * @update 2020/9/23 14:36
   */
  private void enterRoom(Channel channel, Packet packet) {
    try {
      boolean isTestUser = this.redisUtils.hasKey(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
      boolean isPlayer = this.redisUtils.hasKey(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
      if (isTestUser || isPlayer) {
        closeLoading(packet.userId);
        boolean checkRoom = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + packet.roomId);
        boolean checkTest = (packet.roomId == AnimalAssets.getLong(AnimalAssets.TEST_ID));
        if (checkRoom || checkTest) {
          AnimalRoom animalRoom = GAME_DATA.get(packet.roomId);
          if (Objects.nonNull(animalRoom)) {
            AnimalPlayer animalPlayer = animalRoom.getGamePlayer(packet.userId);
            if (animalRoom.getGameStatus() == 1) {
              if (Objects.nonNull(animalPlayer)) {
                animalPlayer.setChannel(channel);
                refreshData(channel, packet);
                disconnected(channel, packet);
                return;
              } else {
                playerNotExist(channel);
                return;
              }
            } else {
              if (Objects.nonNull(animalPlayer)) {
                animalPlayer.setChannel(channel);
                refreshData(channel, packet);
              } else {
                if (animalRoom.getTimeOutMap().containsKey((int) AnimalCmd.WAIT_PLAYER)) {
                  animalRoom.cancelTimeOut((int) AnimalCmd.WAIT_PLAYER);
                }
                refreshData(channel, packet);
                byte[] bytes;
                if (isTestUser) {
                  bytes = this.redisUtils.getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
                } else {
                  bytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
                }
                I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
                animalRoom.joinRoom(channel, playerInfo);
                pullDecorateInfo(packet);
                joinAnimalRoom(packet);
              }
            }
          } else {
            register(TimerEventLoop.timeGroup.next());
            GAME_DATA.computeIfAbsent(packet.roomId, key -> new AnimalRoom(packet.roomId));
            animalRoom = GAME_DATA.get(packet.roomId);
            refreshData(channel, packet);
            byte[] bytes;
            if (isTestUser) {
              bytes = this.redisUtils.getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
            } else {
              bytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
            }
            I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
            animalRoom.joinRoom(channel, playerInfo);
            pullDecorateInfo(packet);
            joinAnimalRoom(packet);
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
   * @create 2020/5/27 21:47
   * @update 2020/9/23 14:37
   */
  private void pushPlayerInfo(Channel channel, Packet packet) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(animalRoom)) {
        F30011.F30011S2C.Builder builder = F30011.F30011S2C.newBuilder();
        List<AnimalPlayer> playerList = animalRoom.getPlayerList();
        if (CollectionUtils.isNotEmpty(playerList)) {
          F30011.PlayerInfo.Builder playerInfo;
          for (AnimalPlayer player : playerList) {
            playerInfo = F30011.PlayerInfo.newBuilder();
            playerInfo.setNick(player.getUserName());
            playerInfo.setUserID(player.getUserId());
            playerInfo.setUrl(player.getUserIcon());
            playerInfo.setSex(player.getUserSex());
            playerInfo.setRed(player.getIdentity());
            if (CollectionUtils.isNotEmpty(player.getChessStyle())) {
              F30011.GameStyle.Builder gameStyle = F30011.GameStyle.newBuilder();
              F30011.ChessStyle.Builder chessStyle;
              List<Map<String, Object>> skinStyle = player.getChessStyle();
              for (Map<String, Object> skin : skinStyle) {
                chessStyle = F30011.ChessStyle.newBuilder();
                chessStyle.setBeastId((Integer) skin.get("beastId"));
                chessStyle.setSheetNumber(StringUtils.nvl(skin.get("skinUrl")));
                gameStyle.addChessStyle(chessStyle);
              }
              playerInfo.setGameStyle(gameStyle);
            }
            builder.addPlayer(playerInfo);
          }
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.ENTER_ROOM,
                  builder.setResult(0).build().toByteArray()), animalRoom.getRoomId());
          if (playerList.size() == 1) {
            waitTimeout(animalRoom.getRoomId());
          } else {
            startTimeout(animalRoom.getRoomId());
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
   * TODO 玩家移动. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/27 21:47
   * @update 2020/9/23 14:38
   */
  private void playerMoves(Channel channel, Packet packet) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(animalRoom)) {
        AnimalPlayer checkPlayer = animalRoom.getGamePlayer(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30011.F300112C2S request = F30011.F300112C2S.parseFrom(packet.bytes);
          F30011.F300112S2C.Builder builder = F30011.F300112S2C.newBuilder();
          if (animalRoom.getNextActionId().equals(packet.userId)) {
            if (!animalRoom.isAction(request)) {
              builder.setIsMove(0).setInitBeastId(request.getInitBeastId());
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.PLAYER_MOVES,
                      builder.build().toByteArray()));
            } else {
              animalRoom.cancelTimeOut(AnimalCmd.TASK_TIME);
              forwardOperationData(request, packet.roomId, packet.userId);
              // 验证回合数 平局结算
              boolean redActionNnm = animalRoom.getPlayerList().get(0).getActionNum() == AnimalAssets.getInt(AnimalAssets.GAME_ROUND);
              boolean blueActionNnm = animalRoom.getPlayerList().get(1).getActionNum() == AnimalAssets.getInt(AnimalAssets.GAME_ROUND);
              if (redActionNnm && blueActionNnm) {
                animalRoom.destroy();
                drawSettlement(animalRoom.getRoomId());
              } else {
                int indexWin = animalRoom.isWinOrDraw(packet.userId);
                // indexWin [0-未胜利 1-胜利 2-平局 3-输了]
                switch (indexWin) {
                  case 0:
                    AnimalPlayer nextPlayer = animalRoom.getGamePlayer(animalRoom.getNextActionId());
                    if (nextPlayer.getIsRobot() == 0) {
                      robotActionTimeout(animalRoom.getRoomId());
                    } else {
                      actionTimeout(animalRoom.getRoomId());
                    }
                    break;
                  case 1:
                    animalRoom.destroy();
                    settlement(animalRoom.getRoomId(), packet.userId);
                    break;
                  case 2:
                    animalRoom.destroy();
                    drawSettlement(animalRoom.getRoomId());
                    break;
                  default:
                    animalRoom.destroy();
                    confessSettlement(animalRoom.getRoomId(), packet.userId);
                    break;
                }
              }
            }
          } else {
            builder.setIsMove(0).setInitBeastId(request.getInitBeastId());
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.PLAYER_MOVES,
                    builder.build().toByteArray()));
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
   * TODO 翻转位置. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/25 6:26
   * @update 2020/9/23 14:38
   */
  private void flipPosition(Channel channel, Packet packet) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(animalRoom)) {
        AnimalPlayer checkPlayer = animalRoom.getGamePlayer(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30011.F300113C2S request = F30011.F300113C2S.parseFrom(packet.bytes);
          F30011.F300113S2C.Builder builder = F30011.F300113S2C.newBuilder();
          F30011.MPosition initPosition = request.getDownPosition();
          if (animalRoom.getNextActionId().equals(packet.userId)) {
            animalRoom.cancelTimeOut(AnimalCmd.TASK_TIME);
            int x = initPosition.getPositionX();
            int y = initPosition.getPositionY();
            AnimalCoords animalCoords = animalRoom.flipPosition(x, y, packet.userId);
            builder.setActionUserId(packet.userId);
            builder.setDownPosition(initPosition);
            builder.setBeastId(animalCoords.getAnimal());
            builder.setNextUserID(animalRoom.getNextActionId());
            builder.setActionTime(AnimalAssets.getInt(AnimalAssets.SHOW_TIMEOUT));
            builder.setStatus(0);
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.FLIP_POSITION,
                    builder.build().toByteArray()), animalRoom.getRoomId());
            AnimalPlayer nextPlayer = animalRoom.getGamePlayer(animalRoom.getNextActionId());
            if (nextPlayer.getIsRobot() == 0) {
              robotActionTimeout(animalRoom.getRoomId());
            } else {
              actionTimeout(animalRoom.getRoomId());
            }
          } else {
            builder.setStatus(1);
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.FLIP_POSITION,
                    builder.build().toByteArray()));
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
   * TODO 玩家认输. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/25 6:26
   * @update 2020/9/23 14:39
   */
  private void confess(Channel channel, Packet packet) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(animalRoom)) {
        AnimalPlayer checkPlayer = animalRoom.getGamePlayer(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30011.F300115S2C.Builder builder = F30011.F300115S2C.newBuilder();
          if (animalRoom.getGameStatus() == 1) {
            builder.setResult(0);
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.CONFESS,
                    builder.build().toByteArray()));
            animalRoom.destroy();
            confessSettlement(packet.roomId, packet.userId);
          } else {
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.CONFESS,
                    builder.setResult(1).build().toByteArray()));
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
   * TODO 断线重连. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/27 21:47
   * @update 2020/9/23 14:41
   */
  private void disconnected(Channel channel, Packet packet) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(animalRoom)) {
        AnimalPlayer partaker = animalRoom.getGamePlayer(packet.userId);
        partaker.setChannel(channel);
        F30011.F300116S2C.Builder builder = F30011.F300116S2C.newBuilder();
        F30011.PlayerInfo.Builder tempInfo = F30011.PlayerInfo.newBuilder();
        List<AnimalCoords> coordsList = animalRoom.getDesktopData();
        if (CollectionUtils.isNotEmpty(coordsList)) {
          F30011.MPosition.Builder position;
          for (AnimalCoords animalCoords : coordsList) {
            position = F30011.MPosition.newBuilder();
            position.setPositionX(animalCoords.getPositionX());
            position.setPositionY(animalCoords.getPositionY());
            position.setBeastId(animalCoords.getAnimal());
            position.setIsnull(animalCoords.getStatus());
            tempInfo.addMPosition(position);
          }
        }
        List<AnimalPlayer> playerList = animalRoom.getPlayerList();
        F30011.PlayerInfo.Builder playerInfo;
        F30011.GameStyle.Builder gameStyle;
        for (AnimalPlayer player : playerList) {
          playerInfo = F30011.PlayerInfo.newBuilder();
          gameStyle = F30011.GameStyle.newBuilder();
          playerInfo.setNick(player.getUserName());
          playerInfo.setUserID(player.getUserId());
          playerInfo.setUrl(player.getUserIcon());
          playerInfo.setSex(player.getUserSex());
          playerInfo.setRed(player.getIdentity());
          playerInfo.addAllMPosition(tempInfo.getMPositionList());
          List<Map<String, Object>> skinStyle = player.getChessStyle();
          if (CollectionUtils.isNotEmpty(skinStyle)) {
            F30011.ChessStyle.Builder chessStyle;
            for (Map<String, Object> skin : skinStyle) {
              chessStyle = F30011.ChessStyle.newBuilder();
              chessStyle.setBeastId((Integer) skin.get("beastId"));
              chessStyle.setSheetNumber(StringUtils.nvl(skin.get("skinUrl")));
              gameStyle.addChessStyle(chessStyle);
            }
            playerInfo.setGameStyle(gameStyle);
          }
          builder.addBreakLineInfo(playerInfo);
        }
        if (animalRoom.getGameStatus() == 1) {
          builder.setActionUserId(animalRoom.getNextActionId());
          LocalDateTime udt = animalRoom.getActionTime().plusSeconds(25L);
          LocalDateTime nds = LocalDateTime.now();
          Duration duration = Duration.between(nds, udt);
          int second = Math.toIntExact(duration.getSeconds());
          builder.setActionTime(second);
          channel.writeAndFlush(
              new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.DISCONNECTED,
                  builder.build().toByteArray()));
        } else {
          builder.setActionUserId(0).setActionTime(0);
          channel.writeAndFlush(
              new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.DISCONNECTED,
                  builder.build().toByteArray()));
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 关闭游戏. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/25 6:26
   * @update 2020/9/23 14:59
   */
  private void closeGame(Channel channel, Packet packet) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(animalRoom)) {
        AnimalPlayer checkPlayer = animalRoom.getGamePlayer(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          if (animalRoom.getGameStatus() == 1) {
            animalRoom.destroy();
            animalRoom.leaveGame(packet.userId);
            closePage(channel, packet);
            if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
              this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
            }
            AnimalPlayer player = animalRoom.getPlayerList().get(0);
            if (player.getIsRobot() == 0) {
              RobotManager.deleteGameRobot(player.getUserId());
              clearData(animalRoom.getRoomId());
            } else {
              F30011.F300114S2C.Builder builder = F30011.F300114S2C.newBuilder();
              builder.setUserID(player.getUserId());
              LocalDateTime startTime = animalRoom.getStartTime();
              LocalDateTime newTime = LocalDateTime.now();
              Duration duration = Duration.between(startTime, newTime);
              int durations = (int) duration.getSeconds();
              if (durations > AnimalAssets.getInt(AnimalAssets.SETTLEMENT_TIME)) {
                if (MemManager.isExists(player.getUserId())) {
                  builder.setAddExp(gainExperience(player.getUserId(), 4));
                  builder.setIsDouble(1);
                } else {
                  builder.setAddExp(gainExperience(player.getUserId(), 2));
                }
                builder.setAddGlod(10);
              } else {
                if (MemManager.isExists(player.getUserId())) {
                  builder.setIsDouble(1);
                }
                builder.setAddExp(0).setAddGlod(0);
              }
              player.sendPacket(
                  new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.SETTLEMENT,
                      builder.build().toByteArray()));
              // 每日任务.玩3局斗兽棋
              Map<String, Object> taskInfo1 = Maps.newHashMap();
              taskInfo1.put("userId", player.getUserId());
              taskInfo1.put("code", TaskEnum.PGT0002.getCode());
              taskInfo1.put("progress", 1);
              taskInfo1.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo1);
              // 活动处理 丹枫迎秋
              Map<String, Object> activity = Maps.newHashMap();
              activity.put("userId", player.getUserId());
              activity.put("code", ActivityEnum.ACT000102.getCode());
              activity.put("progress", 1);
              this.activityRemote.openHandler(activity);
              // 每日任务.赢1局斗兽棋
              Map<String, Object> taskInfo2 = Maps.newHashMap();
              taskInfo2.put("userId", builder.getUserID());
              taskInfo2.put("code", TaskEnum.PGT0011.getCode());
              taskInfo2.put("progress", 1);
              taskInfo2.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo2);
              achievementTask(packet.roomId);
              clearData(animalRoom.getRoomId());
            }
          } else {
            if (animalRoom.getPlayerList().size() == 1) {
              animalRoom.destroy();
              AnimalPlayer player = animalRoom.getPlayerList().get(0);
              if (player.getIsRobot() == 0) {
                RobotManager.deleteGameRobot(player.getUserId());
              } else {
                if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                  this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
                }
              }
              closePage(channel, packet);
              clearData(animalRoom.getRoomId());
            } else {
              if (animalRoom.getTimeOutMap().containsKey((int) AnimalCmd.START_GAME)) {
                animalRoom.cancelTimeOut((int) AnimalCmd.START_GAME);
              }
              animalRoom.leaveGame(packet.userId);
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              AnimalPlayer player = animalRoom.getPlayerList().get(0);
              if (player.getIsRobot() == 0) {
                RobotManager.deleteGameRobot(player.getUserId());
                clearData(packet.roomId);
              } else {
                F30011.F300114S2C.Builder builder = F30011.F300114S2C.newBuilder();
                builder.setUserID(player.getUserId()).setAddExp(0).setAddGlod(0);
                if (MemManager.isExists(player.getUserId())) {
                  builder.setIsDouble(1);
                }
                player.sendPacket(
                    new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.SETTLEMENT,
                        builder.build().toByteArray()));
                clearData(animalRoom.getRoomId());
              }
            }
          }
        } else {
          closePage(channel, packet);
          if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
            this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
          }
        }
      } else {
        closePage(channel, packet);
        clearData(packet.roomId);
        if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
          this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
        }
      }
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
   * @create 2020/5/27 21:47
   * @update 2020/9/23 15:02
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
   * TODO 清除数据.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/5 7:03
   * @update 2020/9/23 15:03
   */
  private void clearData(Long roomId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        GAME_DATA.remove(roomId);
        this.gameRemote.deleteRoom(roomId);
        if (this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId)) {
          this.redisUtils.del(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
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
   * TODO 游戏经验.
   *
   * @param playerId [玩家ID]
   * @param exp [游戏经验]
   * @return [游戏经验]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/29 14:08
   * @update 2020/9/23 15:06
   */
  private int gainExperience(Long playerId, Integer exp) {
    if (StringUtils.nvl(playerId).length() >= 9) {
      Map<String, Object> result = Maps.newHashMap();
      result.put("userId", playerId);
      result.put("experience", exp);
      result.put("gold", 10);
      Result resultExp = this.userRemote.gameHandler(result);
      if (Objects.nonNull(resultExp)) {
        Map<String, Object> objectMap =
            resultExp.getCode().equals(0) ? JsonUtils.toObjectMap(resultExp.getData()) : null;
        if (Objects.nonNull(objectMap)) {
          Integer remainderExp = (Integer) objectMap.get("remainderExp");
          if (remainderExp > 0) {
            return exp;
          } else {
            return 0;
          }
        } else {
          return exp;
        }
      } else {
        return exp;
      }
    }
    return exp;
  }

  /**
   * TODO 开始游戏. 
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/29 19:15
   * @update 2020/9/23 15:08
   */
  private void startGame(Long roomId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        animalRoom.setGameStatus(1);
        F30011.F300111S2C.Builder builder = F30011.F300111S2C.newBuilder();
        builder.setActionUserId(animalRoom.getNextActionId());
        builder.setActionTime(AnimalAssets.getInt(AnimalAssets.SHOW_TIMEOUT));
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.START_GAME,
                builder.build().toByteArray()), animalRoom.getRoomId());
        animalRoom.setUpGameStartTime();
        AnimalPlayer animalPlayer = animalRoom.getGamePlayer(animalRoom.getNextActionId());
        if (animalPlayer.getIsRobot() == 0) {
          robotStartTimeout(roomId);
        } else {
          actionTimeout(roomId);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 数据转发.
   *
   * @param request [客户端数据]
   * @param roomId [房间ID]
   * @param userId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/29 19:15
   * @update 2020/9/23 15:10
   */
  private void forwardOperationData(F30011.F300112C2S request, Long roomId, Long userId) {
    AnimalRoom animalRoom = GAME_DATA.get(roomId);
    if (Objects.nonNull(animalRoom)) {
      AnimalPlayer player = animalRoom.getGamePlayer(userId);
      F30011.F300112S2C.Builder builder = F30011.F300112S2C.newBuilder();
      F30011.MPosition initPosition = request.getInitPosition();
      F30011.MPosition movePosition = request.getMovePosition();
      AnimalCoords userCoords = animalRoom.getCheckedPoint(initPosition.getPositionX(), initPosition.getPositionY());
      AnimalCoords moveCoords = animalRoom.getCheckedPoint(movePosition.getPositionX(), movePosition.getPositionY());
      int userAnimal = userCoords.getRoles();
      int targetAnimal  = moveCoords.getRoles();
      int action = animalRoom.playerMoves(request, userId);
      switch (action) {
        case 1:
          builder.setInitBeastId(request.getInitBeastId());
          builder.setIsMove(1);
          break;
        case 2:
          // 狮子
          if (userAnimal == 7) {
            player.setLionEatAnimal(player.getLionEatAnimal() + 1);
          }
          // 老虎 > 狼
          if (userAnimal == 6 && targetAnimal == 4) {
            player.setTigerEatWolf(player.getTigerEatWolf() + 1);
          }
          // 狗 > 猫/鼠
          if (targetAnimal == 2 || targetAnimal == 1) {
            if (userAnimal == 3) {
              player.setDogEatCatOrMouse(player.getDogEatCatOrMouse() + 1);
            }
          }
          // 猫 > 鼠
          if (userAnimal == 2 && targetAnimal == 1) {
            player.setCatEatMouse(player.getCatEatMouse() + 1);
          }
          // 鼠 > 象
          if (userAnimal == 1 && targetAnimal == 8) {
            player.setMouseEatElephant(player.getMouseEatElephant() + 1);
          }
          builder.setMoveBeastId(request.getMoveBeastId());
          builder.setInitBeastId(request.getInitBeastId());
          builder.setIsMove(2);
          break;
        case 3:
          builder.setMoveBeastId(request.getMoveBeastId());
          builder.setInitBeastId(request.getInitBeastId());
          builder.setIsMove(3);
          break;
        default:
          builder.setMoveBeastId(request.getMoveBeastId());
          builder.setInitBeastId(request.getInitBeastId());
          builder.setIsMove(4);
          break;
      }
      builder.setInitPosition(initPosition);
      builder.setMovePosition(movePosition);
      builder.setActionUserId(userId);
      builder.setActionTime(AnimalAssets.getInt(AnimalAssets.SHOW_TIMEOUT));
      builder.setNextUserID(animalRoom.getNextActionId());
      GroupManager.sendPacketToGroup(
          new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.PLAYER_MOVES,
              builder.build().toByteArray()), animalRoom.getRoomId());
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
    F30011.F30011S2C.Builder builder = F30011.F30011S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.ENTER_ROOM,
            builder.setResult(2).build().toByteArray()));
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
    F30011.F30011S2C.Builder builder = F30011.F30011S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.ENTER_ROOM,
            builder.setResult(1).build().toByteArray()));
  }

  /**
   * TODO 保存数据.
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/30 18:11
   */
  private void joinAnimalRoom(Packet packet) {
    try {
      if (packet.roomId > AnimalAssets.getLong(AnimalAssets.TEST_ID)) {
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
   * TODO 刷新数据.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/17 17:44
   * @update 2020/8/17 17:44
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
   * TODO 关闭加载.
   *
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/17 9:50
   * @update 2020/8/17 9:50
   */
  private void closeLoading(Long playerId) {
    try {
      SoftChannel.sendPacketToUserId(
          new Packet(ActionCmd.APP_HEART, AnimalCmd.CLOSE_LOADING, null), playerId);
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
   * @create 2020/8/17 18:27
   * @update 2020/8/17 18:27
   */
  private void pullDecorateInfo(Packet packet) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(animalRoom)) {
        AnimalPlayer animalPlayer = animalRoom.getGamePlayer(packet.userId);
        List gameDecorate = this.orderRemote.gameDecorate(animalPlayer.getUserId(), ActionCmd.GAME_ANIMAL);
        if (CollectionUtils.isNotEmpty(gameDecorate)) {
          List<Map<String, Object>> decorateList = JsonUtils.listMap(gameDecorate);
          if (CollectionUtils.isNotEmpty(decorateList)) {
            Map<String, Object> tempMap;
            for (Map<String, Object> objectMap : decorateList) {
              Integer labelCode = (Integer) objectMap.get("labelCode");
              if (labelCode != 1000 && labelCode != 2000) {
                tempMap = Maps.newHashMap();
                if (animalPlayer.getIdentity() == 1) {
                  tempMap.put("beastId", labelCode);
                } else {
                  tempMap.put("beastId", animalRoom.getAnimalId(labelCode));
                }
                tempMap.put("skinUrl", StringUtils.nvl(objectMap.get("adornUrl")));
                animalPlayer.getChessStyle().add(tempMap);
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
   * TODO 玩家成就. 
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/23 15:17
   * @update 2020/9/23 15:17
   */
  private void achievementTask(Long roomId) {
    AnimalRoom animalRoom = GAME_DATA.get(roomId);
    if (animalRoom != null) {
      List<AnimalPlayer> playerList = animalRoom.getPlayerList();
      if (CollectionUtils.isNotEmpty(playerList)) {
        for (AnimalPlayer player : playerList) {
          if (player.getIsRobot() == 1) {
            // 狮子
            if (player.getLionEatAnimal() > 0) {
              // 玩家成就.河东狮吼
              Map<String, Object> taskSuc0005 = Maps.newHashMap();
              taskSuc0005.put("userId", player.getUserId());
              taskSuc0005.put("code", AchievementEnum.AMT0005.getCode());
              taskSuc0005.put("progress", player.getLionEatAnimal());
              taskSuc0005.put("isReset", 0);
              this.userRemote.achievementHandlers(taskSuc0005);
            }
            // 老虎 > 狼
            if (player.getTigerEatWolf() > 0) {
              // 玩家成就.驱虎吞狼
              Map<String, Object> taskSuc0006 = Maps.newHashMap();
              taskSuc0006.put("userId", player.getUserId());
              taskSuc0006.put("code", AchievementEnum.AMT0006.getCode());
              taskSuc0006.put("progress", player.getTigerEatWolf());
              taskSuc0006.put("isReset", 0);
              this.userRemote.achievementHandlers(taskSuc0006);
            }
            // 狗 > 猫/鼠
            if (player.getDogEatCatOrMouse() > 0) {
              // 每日任务.使用狗吃掉其他棋子3次
              Map<String, Object> taskInfo = Maps.newHashMap();
              taskInfo.put("userId", player.getUserId());
              taskInfo.put("code", TaskEnum.PGT0018.getCode());
              taskInfo.put("progress", player.getDogEatCatOrMouse());
              taskInfo.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo);
            }
            // 猫 > 鼠
            if (player.getCatEatMouse() > 0) {
              // 玩家成就.猫捉老鼠
              Map<String, Object> taskSuc0006 = Maps.newHashMap();
              taskSuc0006.put("userId", player.getUserId());
              taskSuc0006.put("code", AchievementEnum.AMT0007.getCode());
              taskSuc0006.put("progress", player.getCatEatMouse());
              taskSuc0006.put("isReset", 0);
              this.userRemote.achievementHandlers(taskSuc0006);
            }
            // 鼠 > 象
            if (player.getMouseEatElephant() > 0) {
              // 玩家成就.鼠胆英雄
              Map<String, Object> taskSuc0006 = Maps.newHashMap();
              taskSuc0006.put("userId", player.getUserId());
              taskSuc0006.put("code", AchievementEnum.AMT0008.getCode());
              taskSuc0006.put("progress", player.getMouseEatElephant());
              taskSuc0006.put("isReset", 0);
              this.userRemote.achievementHandlers(taskSuc0006);
            }
            // 玩家成就.高级玩家
            Map<String, Object> taskSuc0041 = Maps.newHashMap();
            taskSuc0041.put("userId", player.getUserId());
            taskSuc0041.put("code", AchievementEnum.AMT0041.getCode());
            taskSuc0041.put("progress", 1);
            taskSuc0041.put("isReset", 0);
            this.userRemote.achievementHandlers(taskSuc0041);
            // 玩家成就.头号玩家
            Map<String, Object> taskSuc0042 = Maps.newHashMap();
            taskSuc0042.put("userId", player.getUserId());
            taskSuc0042.put("code", AchievementEnum.AMT0042.getCode());
            taskSuc0042.put("progress", 1);
            taskSuc0042.put("isReset", 0);
            this.userRemote.achievementHandlers(taskSuc0042);
          }
        }
      }
    }
  }

  /**
   * TODO 平局结算.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/5 7:03
   * @update 2020/9/23 15:21
   */
  private void drawSettlement(Long roomId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        F30011.F300114S2C.Builder builder = F30011.F300114S2C.newBuilder();
        builder.setAddGlod(0).setAddExp(0).setUserID(0);
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.SETTLEMENT,
                builder.build().toByteArray()), animalRoom.getRoomId());
        List<AnimalPlayer> playerList = animalRoom.getPlayerList();
        playerList.stream().filter(player -> player.getIsRobot() == 1)
            .forEach(player -> {
              // 每日任务.玩3局斗兽棋
              Map<String, Object> taskInfo = Maps.newHashMap();
              taskInfo.put("userId", player.getUserId());
              taskInfo.put("code", TaskEnum.PGT0002.getCode());
              taskInfo.put("progress", 1);
              taskInfo.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo);
              // 活动处理 丹枫迎秋
              Map<String, Object> activity = Maps.newHashMap();
              activity.put("userId", player.getUserId());
              activity.put("code", ActivityEnum.ACT000102.getCode());
              activity.put("progress", 1);
              this.activityRemote.openHandler(activity);
            });
        achievementTask(animalRoom.getRoomId());
        clearData(animalRoom.getRoomId());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 超时结算.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/29 19:15
   * @update 2020/9/23 15:21
   */
  private void actionSettlement(Long roomId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        if (animalRoom.getNextActionId() != null && animalRoom.getNextActionId() > 0) {
          animalRoom.destroy();
          long userId = animalRoom.getNextActionId();
          LocalDateTime startTime = animalRoom.getStartTime();
          LocalDateTime newTime = LocalDateTime.now();
          Duration duration = Duration.between(startTime, newTime);
          int durations = (int) duration.getSeconds();
          if (durations > AnimalAssets.getInt(AnimalAssets.SETTLEMENT_TIME)) {
            concedeReally(roomId, userId);
          } else {
            concedeDummy(roomId, userId);
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 认输结算. 
   *
   * @param roomId [房间ID]
   * @param userId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/25 6:26
   * @update 2020/9/23 15:22
   */
  private void confessSettlement(Long roomId, Long userId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        LocalDateTime startTime = animalRoom.getStartTime();
        LocalDateTime newTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, newTime);
        int durations = (int) duration.getSeconds();
        if (durations > AnimalAssets.getInt(AnimalAssets.SETTLEMENT_TIME)) {
          concedeReally(roomId, userId);
        } else {
          concedeDummy(roomId, userId);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 认输结算. 真
   *
   * @param roomId [房间ID]
   * @param userId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/5 7:03
   * @update 2020/9/23 15:26
   */
  private void concedeReally(Long roomId, Long userId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        F30011.F300114S2C.Builder builder = F30011.F300114S2C.newBuilder();
        AnimalPlayer losePlayer = animalRoom.getGamePlayer(userId);
        //identity [1-红 2-蓝]
        if (losePlayer.getIdentity() == 1) {
          AnimalPlayer winPlayer = animalRoom.getPlayerList().get(1);
          builder.setUserID(winPlayer.getUserId());
          if (MemManager.isExists(winPlayer.getUserId())) {
            builder.setAddExp(gainExperience(winPlayer.getUserId(), 4));
            builder.setIsDouble(1);
          } else {
            builder.setAddExp(gainExperience(winPlayer.getUserId(), 2));
          }
          builder.setAddGlod(10);
          winPlayer.sendPacket(
              new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.SETTLEMENT,
                  builder.build().toByteArray()));
          builder.clearIsDouble();
        } else {
          AnimalPlayer winPlayer = animalRoom.getPlayerList().get(0);
          builder.setUserID(winPlayer.getUserId());
          if (MemManager.isExists(winPlayer.getUserId())) {
            builder.setAddExp(gainExperience(winPlayer.getUserId(), 4));
            builder.setIsDouble(1);
          } else {
            builder.setAddExp(gainExperience(winPlayer.getUserId(), 2));
          }
          builder.setAddGlod(10);
          winPlayer.sendPacket(
              new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.SETTLEMENT,
                  builder.build().toByteArray()));
          builder.clearIsDouble();
        }
        if (MemManager.isExists(losePlayer.getUserId())) {
          builder.setIsDouble(1);
        }
        builder.setAddExp(0).setAddGlod(0);
        losePlayer.sendPacket(
            new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.SETTLEMENT,
                builder.build().toByteArray()));
        List<AnimalPlayer> playerList = animalRoom.getPlayerList();
        playerList.stream().filter(player -> player.getIsRobot() == 1)
            .forEach(player -> {
              // 每日任务.玩3局斗兽棋
              Map<String, Object> taskInfo = Maps.newHashMap();
              taskInfo.put("userId", player.getUserId());
              taskInfo.put("code", TaskEnum.PGT0002.getCode());
              taskInfo.put("progress", 1);
              taskInfo.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo);
              // 活动处理 丹枫迎秋
              Map<String, Object> activity = Maps.newHashMap();
              activity.put("userId", player.getUserId());
              activity.put("code", ActivityEnum.ACT000102.getCode());
              activity.put("progress", 1);
              this.activityRemote.openHandler(activity);
            });
        // 每日任务.赢1局斗兽棋
        AnimalPlayer indexPlayer = animalRoom.getGamePlayer(builder.getUserID());
        if (indexPlayer.getIsRobot() == 1) {
          Map<String, Object> taskInfo = Maps.newHashMap();
          taskInfo.put("userId", builder.getUserID());
          taskInfo.put("code", TaskEnum.PGT0011.getCode());
          taskInfo.put("progress", 1);
          taskInfo.put("isReset", 0);
          this.userRemote.taskHandler(taskInfo);
        }
        achievementTask(animalRoom.getRoomId());
        clearData(animalRoom.getRoomId());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 认输结算. 假
   *
   * @param roomId [房间ID]
   * @param userId [玩家ID]
   * @author WangCaiWen
   * @since 2020/5/15 - 2020/6/5
   */
  private void concedeDummy(Long roomId, Long userId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        F30011.F300114S2C.Builder builder = F30011.F300114S2C.newBuilder();
        builder.setAddExp(0).setAddGlod(0);
        //identity [1-红 2-蓝]
        AnimalPlayer losePlayer = animalRoom.getGamePlayer(userId);
        if (losePlayer.getIdentity() == 1) {
          AnimalPlayer winPlayer = animalRoom.getPlayerList().get(1);
          builder.setUserID(winPlayer.getUserId());
        } else {
          AnimalPlayer winPlayer = animalRoom.getPlayerList().get(0);
          builder.setUserID(winPlayer.getUserId());
        }
        List<AnimalPlayer> playerList = animalRoom.getPlayerList();
        playerList.forEach(player -> {
          if (MemManager.isExists(player.getUserId())) {
            builder.setIsDouble(1);
          }
          player.sendPacket(
              new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.SETTLEMENT,
                  builder.build().toByteArray()));
          builder.clearIsDouble();
        });
        playerList.stream().filter(player -> player.getIsRobot() == 1)
            .forEach(player -> {
              // 每日任务.玩3局斗兽棋
              Map<String, Object> taskInfo = Maps.newHashMap();
              taskInfo.put("userId", player.getUserId());
              taskInfo.put("code", TaskEnum.PGT0002.getCode());
              taskInfo.put("progress", 1);
              taskInfo.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo);
              // 活动处理 丹枫迎秋
              Map<String, Object> activity = Maps.newHashMap();
              activity.put("userId", player.getUserId());
              activity.put("code", ActivityEnum.ACT000102.getCode());
              activity.put("progress", 1);
              this.activityRemote.openHandler(activity);
            });
        // 每日任务.赢1局斗兽棋
        AnimalPlayer indexPlayer = animalRoom.getGamePlayer(builder.getUserID());
        if (indexPlayer.getIsRobot() == 1) {
          Map<String, Object> taskInfo = Maps.newHashMap();
          taskInfo.put("userId", builder.getUserID());
          taskInfo.put("code", TaskEnum.PGT0011.getCode());
          taskInfo.put("progress", 1);
          taskInfo.put("isReset", 0);
          this.userRemote.taskHandler(taskInfo);
        }
        achievementTask(animalRoom.getRoomId());
        clearData(animalRoom.getRoomId());
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
   * @param userId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/5 7:03
   * @update 2020/9/23 15:30
   */
  private void settlement(Long roomId, Long userId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        LocalDateTime startTime = animalRoom.getStartTime();
        LocalDateTime newTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, newTime);
        int durations = (int) duration.getSeconds();
        if (durations > AnimalAssets.getInt(AnimalAssets.SETTLEMENT_TIME)) {
          settlementReally(roomId, userId);
        } else {
          settlementDummy(roomId, userId);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 胜利结算. 真
   *
   * @param roomId [房间ID]
   * @param userId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/5 7:03
   * @update 2020/9/23 15:30
   */
  private void settlementReally(Long roomId, Long userId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        F30011.F300114S2C.Builder builder = F30011.F300114S2C.newBuilder();
        builder.setUserID(userId);
        if (MemManager.isExists(userId)) {
          builder.setAddExp(gainExperience(userId, 4));
          builder.setIsDouble(1);
        } else {
          builder.setAddExp(gainExperience(userId, 2));
        }
        builder.setAddGlod(10);
        AnimalPlayer winPlayer = animalRoom.getGamePlayer(userId);
        winPlayer.sendPacket(
            new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.SETTLEMENT,
                builder.build().toByteArray()));
        builder.clearIsDouble();
        if (winPlayer.getIdentity() == 1) {
          AnimalPlayer losePlayer = animalRoom.getPlayerList().get(1);
          if (MemManager.isExists(losePlayer.getUserId())) {
            builder.setIsDouble(1);
          }
          builder.setAddExp(0).setAddGlod(0);
          losePlayer.sendPacket(
              new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.SETTLEMENT,
                  builder.build().toByteArray()));
        } else {
          AnimalPlayer losePlayer = animalRoom.getPlayerList().get(0);
          if (MemManager.isExists(losePlayer.getUserId())) {
            builder.setIsDouble(1);
          }
          builder.setAddExp(0).setAddGlod(0);
          losePlayer.sendPacket(
              new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.SETTLEMENT,
                  builder.build().toByteArray()));
        }
        List<AnimalPlayer> playerList = animalRoom.getPlayerList();
        playerList.stream().filter(player -> player.getIsRobot() == 1)
            .forEach(player -> {
              // 每日任务.玩3局斗兽棋
              Map<String, Object> taskInfo = Maps.newHashMap();
              taskInfo.put("userId", player.getUserId());
              taskInfo.put("code", TaskEnum.PGT0002.getCode());
              taskInfo.put("progress", 1);
              taskInfo.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo);
              // 活动处理 丹枫迎秋
              Map<String, Object> activity = Maps.newHashMap();
              activity.put("userId", player.getUserId());
              activity.put("code", ActivityEnum.ACT000102.getCode());
              activity.put("progress", 1);
              this.activityRemote.openHandler(activity);
            });
        // 每日任务.赢1局斗兽棋
        AnimalPlayer indexPlayer = animalRoom.getGamePlayer(builder.getUserID());
        if (indexPlayer.getIsRobot() == 1) {
          Map<String, Object> taskInfo = Maps.newHashMap();
          taskInfo.put("userId", builder.getUserID());
          taskInfo.put("code", TaskEnum.PGT0011.getCode());
          taskInfo.put("progress", 1);
          taskInfo.put("isReset", 0);
          this.userRemote.taskHandler(taskInfo);
        }
        achievementTask(animalRoom.getRoomId());
        clearData(animalRoom.getRoomId());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 胜利结算. 假
   *
   * @param roomId [房间ID]
   * @param userId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/5 7:03
   * @update 2020/9/23 15:30
   */
  private void settlementDummy(Long roomId, Long userId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        F30011.F300114S2C.Builder builder = F30011.F300114S2C.newBuilder();
        builder.setUserID(userId).setAddExp(0).setAddGlod(0);
        List<AnimalPlayer> playerList = animalRoom.getPlayerList();
        playerList.forEach(player -> {
          if (MemManager.isExists(player.getUserId())) {
            builder.setIsDouble(1);
          }
          player.sendPacket(
              new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.SETTLEMENT,
                  builder.build().toByteArray()));
          builder.clearIsDouble();
        });
        playerList.stream().filter(player -> player.getIsRobot() == 1)
            .forEach(player -> {
              // 每日任务.玩3局斗兽棋
              Map<String, Object> taskInfo = Maps.newHashMap();
              taskInfo.put("userId", player.getUserId());
              taskInfo.put("code", TaskEnum.PGT0002.getCode());
              taskInfo.put("progress", 1);
              taskInfo.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo);
              // 活动处理 丹枫迎秋
              Map<String, Object> activity = Maps.newHashMap();
              activity.put("userId", player.getUserId());
              activity.put("code", ActivityEnum.ACT000102.getCode());
              activity.put("progress", 1);
              this.activityRemote.openHandler(activity);
            });
        // 每日任务.赢1局斗兽棋
        AnimalPlayer indexPlayer = animalRoom.getGamePlayer(builder.getUserID());
        if (indexPlayer.getIsRobot() == 1) {
          Map<String, Object> taskInfo = Maps.newHashMap();
          taskInfo.put("userId", builder.getUserID());
          taskInfo.put("code", TaskEnum.PGT0011.getCode());
          taskInfo.put("progress", 1);
          taskInfo.put("isReset", 0);
          this.userRemote.taskHandler(taskInfo);
        }
        achievementTask(animalRoom.getRoomId());
        clearData(animalRoom.getRoomId());
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
   * @create 2020/9/14 17:10
   * @update 2020/9/14 17:10
   */
  private void robotWaitTimeout(Long roomId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        if (!animalRoom.getTimeOutMap().containsKey((int) AnimalCmd.WAIT_PLAYER)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> waitExamine(roomId)
              ), 30, TimeUnit.SECONDS);
          animalRoom.addTimeOut(AnimalCmd.WAIT_PLAYER, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 等待玩家. 15(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/27 21:47
   * @update 2020/9/23 15:38
   */
  private void waitTimeout(Long roomId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        if (!animalRoom.getTimeOutMap().containsKey((int) AnimalCmd.WAIT_PLAYER)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> waitExamine(roomId)
              ), 15, TimeUnit.SECONDS);
          animalRoom.addTimeOut(AnimalCmd.WAIT_PLAYER, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 等待检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/30 21:28
   * @update 2020/9/23 15:39
   */
  private void waitExamine(Long roomId) {
    AnimalRoom animalRoom = GAME_DATA.get(roomId);
    if (animalRoom != null) {
      animalRoom.removeTimeOut((int) AnimalCmd.WAIT_PLAYER);
      int playerSize = animalRoom.getPlayerList().size();
      if (playerSize == 0) {
        clearData(roomId);
      } else if (playerSize == 1) {
        animalRoom.destroy();
        AnimalPlayer player = animalRoom.getPlayerList().get(0);
        if (player.getIsRobot() == 0) {
          RobotManager.deleteGameRobot(player.getUserId());
          clearData(roomId);
        } else {
          F30011.F300114S2C.Builder builder = F30011.F300114S2C.newBuilder();
          builder.setUserID(player.getUserId()).setAddExp(0).setAddGlod(0);
          if (MemManager.isExists(player.getUserId())) {
            builder.setIsDouble(1);
          }
          player.sendPacket(
              new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.SETTLEMENT,
                  builder.build().toByteArray()));
          clearData(roomId);
        }

      }
    }
  }

  /**
   * TODO 开始游戏. 3(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/30 21:28
   * @update 2020/9/23 15:40
   */
  private void startTimeout(Long roomId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        if (!animalRoom.getTimeOutMap().containsKey((int) AnimalCmd.START_GAME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> startExamine(roomId)
              ), 3, TimeUnit.SECONDS);
          animalRoom.addTimeOut(AnimalCmd.START_GAME, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 开始检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/27 21:47
   * @update 2020/9/23 15:40
   */
  private void startExamine(Long roomId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        animalRoom.removeTimeOut((int) AnimalCmd.START_GAME);
        int playerSize = animalRoom.getPlayerList().size();
        if (playerSize == 0) {
          clearData(roomId);
        } else if (playerSize == 1) {
          animalRoom.destroy();
          AnimalPlayer player = animalRoom.getPlayerList().get(0);
          if (player.getIsRobot() == 0) {
            RobotManager.deleteGameRobot(player.getUserId());
            clearData(player.getUserId());
          } else {
            F30011.F300114S2C.Builder builder = F30011.F300114S2C.newBuilder();
            builder.setUserID(player.getUserId()).setAddExp(0).setAddGlod(0);
            if (MemManager.isExists(player.getUserId())) {
              builder.setIsDouble(1);
            }
            player.sendPacket(
                new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.SETTLEMENT,
                    builder.build().toByteArray()));
            clearData(roomId);
          }
        } else {
          startGame(roomId);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 操作定时. 25(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/23 15:41
   * @update 2020/9/23 15:41
   */
  private void actionTimeout(Long roomId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        if (!animalRoom.getTimeOutMap().containsKey((int) AnimalCmd.TASK_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> actionExamine(roomId)
              ), 25, TimeUnit.SECONDS);
          animalRoom.addTimeOut(AnimalCmd.TASK_TIME, timeout);
          animalRoom.setActionTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 操作检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/30 21:28
   * @update 2020/9/23 15:41
   */
  private void actionExamine(Long roomId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        animalRoom.removeTimeOut((int) AnimalCmd.TASK_TIME);
        animalRoom.destroy();
        actionSettlement(roomId);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 机器定时. 3(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 17:15
   * @update 2020/9/15 17:15
   */
  private void robotStartTimeout(Long roomId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        if (!animalRoom.getTimeOutMap().containsKey((int) AnimalCmd.TASK_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> robotStartExamine(roomId)
              ), 3, TimeUnit.SECONDS);
          animalRoom.addTimeOut(AnimalCmd.TASK_TIME, timeout);
          animalRoom.setActionTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 机器检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 17:52
   * @update 2020/9/15 17:52
   */
  private void robotStartExamine(Long roomId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        animalRoom.removeTimeOut((int) AnimalCmd.TASK_TIME);
        AnimalCoords animalCoords = animalRoom.randomFlopPosition();
        robotOpenAction(animalRoom.getRoomId(), animalRoom.getNextActionId(), animalCoords);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 机器定时. 3(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 17:15
   * @update 2020/9/15 17:15
   */
  private void robotActionTimeout(Long roomId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        if (!animalRoom.getTimeOutMap().containsKey((int) AnimalCmd.TASK_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> robotActionExamine(roomId)
              ), 3, TimeUnit.SECONDS);
          animalRoom.addTimeOut(AnimalCmd.TASK_TIME, timeout);
          animalRoom.setActionTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 机器检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 17:52
   * @update 2020/9/15 17:52
   */
  private void robotActionExamine(Long roomId) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        animalRoom.removeTimeOut((int) AnimalCmd.TASK_TIME);
        List<AnimalCoords> animalCoords = animalRoom.explore();
        if (CollectionUtils.isNotEmpty(animalCoords)) {
          robotMoveAction(animalRoom.getRoomId(), animalRoom.getNextActionId(), animalCoords);
        } else {
          boolean flag = animalRoom.canFlop();
          if (flag) {
            AnimalCoords coords = animalRoom.randomFlopPosition();
            robotOpenAction(animalRoom.getRoomId(), animalRoom.getNextActionId(), coords);
          } else {
            confessSettlement(animalRoom.getRoomId(), animalRoom.getNextActionId());
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 机器翻牌. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/22 20:08
   * @update 2020/9/22 20:08
   */
  private void robotOpenAction(Long roomId, Long robotId, AnimalCoords animalCoords) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        F30011.MPosition.Builder initPosition = F30011.MPosition.newBuilder();
        initPosition.setPositionX(animalCoords.getPositionX());
        initPosition.setPositionY(animalCoords.getPositionY());
        initPosition.setBeastId(animalCoords.getAnimal());
        initPosition.setIsnull(animalCoords.getStatus());
        int x = initPosition.getPositionX();
        int y = initPosition.getPositionY();
        AnimalCoords coords = animalRoom.flipPosition(x, y, robotId);
        F30011.F300113S2C.Builder builder = F30011.F300113S2C.newBuilder();
        builder.setStatus(0);
        builder.setActionUserId(robotId);
        builder.setDownPosition(initPosition);
        builder.setBeastId(coords.getAnimal());
        builder.setNextUserID(animalRoom.getNextActionId());
        builder.setActionTime(AnimalAssets.getInt(AnimalAssets.SHOW_TIMEOUT));
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_ANIMAL, AnimalCmd.FLIP_POSITION,
                builder.build().toByteArray()), animalRoom.getRoomId());
        // 添加定时
        actionTimeout(roomId);
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
   * @param robotId [机器人ID]
   * @param animalCoords [棋子数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/22 20:02
   * @update 2020/9/22 20:02
   */
  private void robotMoveAction(Long roomId, Long robotId, List<AnimalCoords> animalCoords) {
    try {
      AnimalRoom animalRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(animalRoom)) {
        F30011.MPosition.Builder initPosition = F30011.MPosition.newBuilder();
        AnimalCoords userCoords = animalCoords.get(0);
        initPosition.setPositionX(userCoords.getPositionX());
        initPosition.setPositionY(userCoords.getPositionY());
        initPosition.setBeastId(userCoords.getAnimal());
        initPosition.setIsnull(userCoords.getStatus());
        F30011.F300112C2S.Builder moveInfo = F30011.F300112C2S.newBuilder();
        moveInfo.setInitPosition(initPosition);
        F30011.MPosition.Builder movePosition = F30011.MPosition.newBuilder();
        AnimalCoords moveCoords = animalCoords.get(1);
        movePosition.setPositionX(moveCoords.getPositionX());
        movePosition.setPositionY(moveCoords.getPositionY());
        movePosition.setBeastId(moveCoords.getAnimal());
        movePosition.setIsnull(moveCoords.getStatus());
        moveInfo.setMovePosition(movePosition);
        moveInfo.setInitBeastId(initPosition.getBeastId());
        moveInfo.setMoveBeastId(movePosition.getBeastId());
        F30011.F300112C2S request = F30011.F300112C2S.parseFrom(moveInfo.build().toByteArray());
        forwardOperationData(request, roomId, robotId);
        //  验证回合数 平局结算
        boolean redActionNnm = (animalRoom.getPlayerList().get(0).getActionNum() == AnimalAssets.getInt(AnimalAssets.GAME_ROUND));
        boolean blueActionNnm = (animalRoom.getPlayerList().get(1).getActionNum() == AnimalAssets.getInt(AnimalAssets.GAME_ROUND));
        if (redActionNnm && blueActionNnm) {
          animalRoom.destroy();
          drawSettlement(animalRoom.getRoomId());
        } else {
          int indexWin = animalRoom.isWinOrDraw(robotId);
          // 0 未胜利 1 胜利 2 平局 3 输了
          switch (indexWin) {
            case 0:
              // 添加定时
              actionTimeout(roomId);
              break;
            case 1:
              animalRoom.destroy();
              // 当前玩家胜利
              settlement(animalRoom.getRoomId(), robotId);
              break;
            case 2:
              animalRoom.destroy();
              // 平局
              drawSettlement(animalRoom.getRoomId());
              break;
            default:
              animalRoom.destroy();
              // 输了
              confessSettlement(animalRoom.getRoomId(), robotId);
              break;
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

}
