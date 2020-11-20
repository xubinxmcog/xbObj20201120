package com.enuos.live.handle.game.f30001;

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
import com.enuos.live.proto.f30001msg.F30001;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Component;

/**
 * TODO 五子棋.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/5/19 21:09
 */

@Component
@AbstractAction(cmd = ActionCmd.GAME_GO_BANG)
public class GoBang extends AbstractActionHandler {

  /** 房间游戏数据. */
  private static ConcurrentHashMap<Long, GoBangRoom> GAME_DATA = new ConcurrentHashMap<>();

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
   * TODO 操作处理.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/11 13:59
   */
  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      switch (packet.child) {
        case GoBangCmd.ENTER_ROOM:
          enterRoom(channel, packet);
          break;
        case GoBangCmd.PLAYER_PLACEMENT:
          playerAction(channel, packet);
          break;
        case GoBangCmd.REGRET_CHESS:
          regretChess(channel, packet);
          break;
        case GoBangCmd.CONFIRM_APPLICATION:
          confirmApplication(channel, packet);
          break;
        case GoBangCmd.CONFESS:
          confess(channel, packet);
          break;
        case GoBangCmd.CLOSE_GAME:
          closeGame(channel, packet);
          break;
        default:
          LoggerManager.warn("[GAME 30001 HANDLE] CHILD ERROR: [{}]", packet.child);
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
   * @create 2020/9/14 20:59
   * @update 2020/9/14 20:59
   */
  @Override
  public void shutOff(Long userId, Long attachId) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(attachId);
      if (Objects.nonNull(goBangRoom)) {
        GoBangPlayer checkPlayer = goBangRoom.getPlayerInfo(userId);
        if (Objects.nonNull(checkPlayer)) {
          if (goBangRoom.getRoomStatus() == 1) {
            goBangRoom.destroy();
            goBangRoom.leaveGame(userId);
            GoBangPlayer player = goBangRoom.getPlayerList().get(0);
            if (player.getIsRobot() == 0) {
              RobotManager.deleteGameRobot(player.getUserId());
              clearData(attachId);
            } else {
              F30001.F300013S2C.Builder builder = F30001.F300013S2C.newBuilder();
              builder.setUserID(player.getUserId());
              LocalDateTime startTime = goBangRoom.getStartTime();
              LocalDateTime newTime = LocalDateTime.now();
              Duration duration = Duration.between(startTime, newTime);
              int durations = (int) duration.getSeconds();
              if (durations > GoBangAssets.getInt(GoBangAssets.SETTLEMENT_TIME)) {
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
              player.sendPacket(
                  new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.GAME_SETTLEMENT,
                      builder.build().toByteArray()));
              // 每日任务.玩3局五子棋
              Map<String, Object> taskInfo1 = Maps.newHashMap();
              taskInfo1.put("userId", player.getUserId());
              taskInfo1.put("code", TaskEnum.PGT0001.getCode());
              taskInfo1.put("progress", 1);
              taskInfo1.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo1);
              // 活动处理 丹枫迎秋
              Map<String, Object> activity = Maps.newHashMap();
              activity.put("userId", player.getUserId());
              activity.put("code", ActivityEnum.ACT000102.getCode());
              activity.put("progress", 1);
              this.activityRemote.openHandler(activity);
              // 每日任务.赢1局五子棋
              Map<String, Object> taskInfo2 = Maps.newHashMap();
              taskInfo2.put("userId", builder.getUserID());
              taskInfo2.put("code", TaskEnum.PGT0010.getCode());
              taskInfo2.put("progress", 1);
              taskInfo2.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo2);
              achievementTask(attachId);
              clearData(goBangRoom.getRoomId());
            }
          } else {
            if (goBangRoom.getPlayerList().size() == 1) {
              goBangRoom.destroy();
              clearData(goBangRoom.getRoomId());
            } else {
              if (goBangRoom.getTimeOutMap().containsKey((int) GoBangCmd.START_GAME)) {
                goBangRoom.cancelTimeOut((int) GoBangCmd.START_GAME);
              }
              goBangRoom.leaveGame(userId);
              GoBangPlayer player = goBangRoom.getPlayerList().get(0);
              if (player.getIsRobot() == 0) {
                RobotManager.deleteGameRobot(player.getUserId());
                clearData(goBangRoom.getRoomId());
              } else {
                F30001.F300013S2C.Builder builder = F30001.F300013S2C.newBuilder();
                builder.setUserID(player.getUserId()).setAddExp(0).setAddGlod(0);
                if (MemManager.isExists(player.getUserId())) {
                  builder.setIsDouble(1);
                }
                player.sendPacket(
                    new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.GAME_SETTLEMENT,
                        builder.build().toByteArray()));
                clearData(goBangRoom.getRoomId());
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
      GAME_DATA.computeIfAbsent(roomId, key -> new GoBangRoom(roomId));
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      long playerId = playerIds.get(0);
      GameRobot gameRobot = RobotManager.getGameRobot(playerId);
      goBangRoom.joinRobot(gameRobot);
      goBangRoom.setStartRobot(1);
      // 添加定时 时间到.清除房间
      playWaitTimeout(roomId);
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
   * @create 2020/7/22 18:19
   * @update 2020/9/15 16:18
   */
  private void enterRoom(Channel channel, Packet packet) {
    try {
      boolean isTestUser = this.redisUtils.hasKey(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
      boolean isPlayer = this.redisUtils.hasKey(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
      if (isTestUser || isPlayer) {
        closeLoading(packet.userId);
        boolean checkRoom = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + packet.roomId);
        boolean checkTest = (packet.roomId == GoBangAssets.getLong(GoBangAssets.TEST_ID));
        if (checkRoom || checkTest) {
          GoBangRoom goBangRoom = GAME_DATA.get(packet.roomId);
          if (Objects.nonNull(goBangRoom)) {
            GoBangPlayer goBangPlayer = goBangRoom.getPlayerInfo(packet.userId);
            if (goBangRoom.getRoomStatus() == 1) {
              if (Objects.nonNull(goBangPlayer)) {
                goBangPlayer.setChannel(channel);
                refreshData(channel, packet);
                disconnected(channel, packet);
                return;
              } else {
                playerNotExist(channel);
                return;
              }
            } else {
              if (Objects.nonNull(goBangPlayer)) {
                goBangPlayer.setChannel(channel);
                refreshData(channel, packet);
              } else {
                if (goBangRoom.getTimeOutMap().containsKey((int) GoBangCmd.WAIT_PLAYER)) {
                  goBangRoom.cancelTimeOut((int) GoBangCmd.WAIT_PLAYER);
                }
                refreshData(channel, packet);
                byte[] bytes;
                if (isTestUser) {
                  bytes = this.redisUtils.getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
                } else {
                  bytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
                }
                I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
                goBangRoom.joinRoom(channel, playerInfo);
                pullDecorateInfo(packet);
                joinGoBangRoom(packet);
              }
            }
          } else {
            register(TimerEventLoop.timeGroup.next());
            GAME_DATA.computeIfAbsent(packet.roomId, key -> new GoBangRoom(packet.roomId));
            goBangRoom = GAME_DATA.get(packet.roomId);
            refreshData(channel, packet);
            byte[] bytes;
            if (isTestUser) {
              bytes = this.redisUtils.getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
            } else {
              bytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
            }
            I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
            goBangRoom.joinRoom(channel, playerInfo);
            pullDecorateInfo(packet);
            joinGoBangRoom(packet);
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
   * @create 2020/7/22 18:19
   * @update 2020/9/23 13:05
   */
  private void pushPlayerInfo(Channel channel, Packet packet) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(goBangRoom)) {
        F30001.F30001S2C.Builder builder = F30001.F30001S2C.newBuilder();
        List<GoBangPlayer> playerList = goBangRoom.getPlayerList();
        if (CollectionUtils.isNotEmpty(playerList)) {
          F30001.PlayerInfo.Builder playerInfo;
          for (GoBangPlayer goBangPlayer : playerList) {
            playerInfo = F30001.PlayerInfo.newBuilder();
            playerInfo.setNick(goBangPlayer.getUserName());
            playerInfo.setUserID(goBangPlayer.getUserId());
            playerInfo.setUrl(goBangPlayer.getUserIcon());
            playerInfo.setSex(goBangPlayer.getUserSex());
            playerInfo.setIdentity(goBangPlayer.getIdentity());
            if (StringUtils.isNotEmpty(goBangPlayer.getGameDecorate())) {
              F30001.GameStyle.Builder gameStyle = F30001.GameStyle.newBuilder();
              gameStyle.setChessUrl(StringUtils.nvl(goBangPlayer.getGameDecorate()));
              playerInfo.setGameStyle(gameStyle);
            }
            builder.addPlayer(playerInfo);
          }
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.ENTER_ROOM,
                  builder.setResult(0).build().toByteArray()), goBangRoom.getRoomId());
          if (playerList.size() == 1) {
            waitTimeout(goBangRoom.getRoomId());
          } else {
            startTimeout(goBangRoom.getRoomId());
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
   * TODO 玩家操作.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/10/13 10:43
   */
  private void playerAction(Channel channel, Packet packet) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(goBangRoom)) {
        GoBangPlayer checkPlayer = goBangRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30001.F300012C2S request = F30001.F300012C2S.parseFrom(packet.bytes);
          F30001.F300012S2C.Builder builder = F30001.F300012S2C.newBuilder();
          if (goBangRoom.getRoomStatus() == 1) {
            if (goBangRoom.getNextActionId().equals(packet.userId)) {
              goBangRoom.cancelTimeOut((int) GoBangCmd.PLAYER_PLACEMENT);
              // 重置标记
              goBangRoom.setIndexRegret(0);
              goBangRoom.setRegretUser(0L);
              GoBangPlayer player = goBangRoom.getPlayerInfo(packet.userId);
              F30001.DPosition position = request.getDposiion();
              int x = position.getPositionX();
              int y = position.getPositionY();
              GoBangCoords coords = goBangRoom.getCheckedPoint(x, y);
              if (coords.getColor() > 0) {
                // 当前位置不可操作
                channel.writeAndFlush(
                    new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.PLAYER_PLACEMENT,
                        builder.setResult(1).build().toByteArray()));
                return;
              }
              // 玩家操作 >>> 是否胜利
              boolean isWinner = goBangRoom.actionPlacement(position, packet.userId);
              builder.setResult(0).setDposition(position)
                  .setActionUserId(player.getUserId())
                  .setNextUserID(goBangRoom.getNextActionId())
                  .setActionTime(GoBangAssets.getInt(GoBangAssets.SHOW_TIMEOUT));
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.PLAYER_PLACEMENT,
                      builder.build().toByteArray()), goBangRoom.getRoomId());
              if (isWinner) {
                goBangRoom.destroy();
                LocalDateTime startTime = goBangRoom.getStartTime();
                LocalDateTime newTime = LocalDateTime.now();
                Duration duration = Duration.between(startTime, newTime);
                int durations = (int) duration.getSeconds();
                if (durations > GoBangAssets.getInt(GoBangAssets.SETTLEMENT_TIME)) {
                  victoryReally(position, packet.roomId, packet.userId);
                } else {
                  victoryDummy(position, packet.roomId, packet.userId);
                }
              } else if (goBangRoom.remainingPiece() == 0) {
                F30001.F300013S2C.Builder settlement = F30001.F300013S2C.newBuilder();
                settlement.setUserID(0).setAddExp(0).setAddGlod(0);
                List<GoBangPlayer> playerList = goBangRoom.getPlayerList();
                playerList.forEach(players -> {
                  if (MemManager.isExists(players.getUserId())) {
                    settlement.setIsDouble(1);
                  }
                  players.sendPacket(
                      new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.GAME_SETTLEMENT,
                          settlement.build().toByteArray()));
                  settlement.clearIsDouble();
                });
                playerList.stream().filter(players -> players.getIsRobot() == 1)
                    .forEach(players -> {
                      Map<String, Object> taskInfo = Maps.newHashMap();
                      taskInfo.put("userId", players.getUserId());
                      taskInfo.put("code", TaskEnum.PGT0001.getCode());
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
                achievementTask(packet.roomId);
                clearData(packet.roomId);
              } else {
                GoBangPlayer nextPlayer = goBangRoom.getPlayerInfo(goBangRoom.getNextActionId());
                if (nextPlayer.getIsRobot() == 0) {
                  robotActionTimeout(goBangRoom.getRoomId());
                } else {
                  actionTimeout(goBangRoom.getRoomId());
                }
              }
            } else {
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.PLAYER_PLACEMENT,
                      builder.setResult(1).build().toByteArray()));
            }
          } else {
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.PLAYER_PLACEMENT,
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
   * TODO 机器操作.
   *
   * @param roomId [房间ID]
   * @param robotId [机器人ID]
   * @param coords [坐标信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 17:47
   * @update 2020/9/15 17:47
   */
  private void robotAction(Long roomId, Long robotId, GoBangCoords coords) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        if (goBangRoom.getRoomStatus() == 1) {
          if (Objects.equals(goBangRoom.getNextActionId(), robotId)) {
            // 重置标记
            goBangRoom.setIndexRegret(0);
            goBangRoom.setRegretUser(0L);
            F30001.DPosition.Builder dPosition = F30001.DPosition.newBuilder();
            dPosition.setColor(1).setPositionX(coords.getPositionX())
                .setPositionY(coords.getPositionY());
            F30001.DPosition position = F30001.DPosition.parseFrom(dPosition.build().toByteArray());
            // 机器操作 >>> 是否胜利
            boolean isWinner = goBangRoom.actionPlacement(position, robotId);
            F30001.F300012S2C.Builder builder = F30001.F300012S2C.newBuilder();
            builder.setResult(0).setDposition(position)
                .setActionUserId(robotId)
                .setNextUserID(goBangRoom.getNextActionId())
                .setActionTime(GoBangAssets.getInt(GoBangAssets.SHOW_TIMEOUT));
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.PLAYER_PLACEMENT,
                    builder.build().toByteArray()), goBangRoom.getRoomId());
            if (isWinner) {
              goBangRoom.destroy();
              LocalDateTime startTime = goBangRoom.getStartTime();
              LocalDateTime newTime = LocalDateTime.now();
              Duration duration = Duration.between(startTime, newTime);
              int durations = (int) duration.getSeconds();
              if (durations > GoBangAssets.getInt(GoBangAssets.SETTLEMENT_TIME)) {
                victoryReally(position, roomId, robotId);
              } else {
                victoryDummy(position, roomId, robotId);
              }
            } else if (goBangRoom.remainingPiece() == 0) {
              F30001.F300013S2C.Builder settlement = F30001.F300013S2C.newBuilder();
              settlement.setUserID(0).setAddExp(0).setAddGlod(0);
              List<GoBangPlayer> playerList = goBangRoom.getPlayerList();
              playerList.forEach(players -> {
                if (MemManager.isExists(players.getUserId())) {
                  settlement.setIsDouble(1);
                }
                players.sendPacket(
                    new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.GAME_SETTLEMENT,
                        settlement.build().toByteArray()));
                settlement.clearIsDouble();
              });
              playerList.stream().filter(players -> players.getIsRobot() == 1)
                  .forEach(players -> {
                    Map<String, Object> taskInfo = Maps.newHashMap();
                    taskInfo.put("userId", players.getUserId());
                    taskInfo.put("code", TaskEnum.PGT0001.getCode());
                    taskInfo.put("progress", 1);
                    taskInfo.put("isReset", 0);
                    this.userRemote.taskHandler(taskInfo);
                    // 活动处理 丹枫迎秋
                    Map<String, Object> activity = Maps.newHashMap();
                    activity.put("userId", players.getUserId());
                    activity.put("code", ActivityEnum.ACT000102.getCode());
                    activity.put("progress", 1);
                    this.activityRemote.openHandler(activity);
                  });
              achievementTask(roomId);
              clearData(roomId);
            } else {
              actionTimeout(goBangRoom.getRoomId());
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
   * TODO 玩家认输.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/23 20:09
   */
  private void confess(Channel channel, Packet packet) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(goBangRoom)) {
        GoBangPlayer checkPlayer = goBangRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30001.F300017S2C.Builder builder = F30001.F300017S2C.newBuilder();
          if (goBangRoom.getRoomStatus() == 1) {
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.CONFESS,
                    builder.setResult(0).build().toByteArray()));
            goBangRoom.destroy();
            confessSettlement(packet.roomId, packet.userId);
          } else if (goBangRoom.getRoomStatus() == 0 || goBangRoom.getRoomStatus() > 1) {
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.CONFESS,
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
   * TODO 认输结算.
   *
   * @param roomId [房间ID]
   * @param userId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/23 20:10
   */
  private void confessSettlement(Long roomId, Long userId) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (goBangRoom != null) {
        LocalDateTime startTime = goBangRoom.getStartTime();
        LocalDateTime newTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, newTime);
        int durations = (int) duration.getSeconds();
        if (durations > GoBangAssets.getInt(GoBangAssets.SETTLEMENT_TIME)) {
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
   * TODO 申请悔棋.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:10
   */
  private void regretChess(Channel channel, Packet packet) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(goBangRoom)) {
        GoBangPlayer checkPlayer = goBangRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30001.F300014S2C.Builder builder = F30001.F300014S2C.newBuilder();
          builder.setUserID(packet.userId);
          if (goBangRoom.getIndexRegret() == 0) {
            GoBangPlayer player = goBangRoom.getPlayerInfo(packet.userId);
            if (player.getActionCoords().size() > 0) {
              goBangRoom.setIndexRegret(1);
              goBangRoom.setRegretUser(packet.userId);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.REGRET_CHESS,
                      builder.setResult(0).build().toByteArray()), goBangRoom.getRoomId());
              long nextPlayer = goBangRoom.getNextGamePlayer(packet.userId);
              GoBangPlayer goBangPlayer = goBangRoom.getPlayerInfo(nextPlayer);
              if (goBangPlayer.getIsRobot() == 0) {
                robotConfirmTimeout(goBangRoom.getRoomId());
              }
            } else {
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.REGRET_CHESS,
                      builder.setResult(1).build().toByteArray()));
            }
          } else {
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.REGRET_CHESS,
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
   * TODO 悔棋确认.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:11
   */
  private void confirmApplication(Channel channel, Packet packet) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(goBangRoom)) {
        GoBangPlayer checkPlayer = goBangRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30001.F300015C2S request = F30001.F300015C2S.parseFrom(packet.bytes);
          F30001.F300015S2C.Builder builder = F30001.F300015S2C.newBuilder();
          builder.setResult(request.getIsAgree());
          if (request.getIsAgree() == 0) {
            goBangRoom.cancelTimeOut(GoBangCmd.PLAYER_PLACEMENT);
            List<GoBangPlayer> playerList = goBangRoom.getPlayerList();
            // 申请玩家
            GoBangPlayer player = goBangRoom.getPlayerInfo(goBangRoom.getRegretUser());
            // 当前操作玩家
            if (player.getUserId().equals(goBangRoom.getNextActionId())) {
              List<GoBangCoords> coordsList = Lists.newLinkedList();
              playerList.stream().filter(s -> s.getActionCoords().size() > 0)
                  .forEach(s -> {
                    coordsList.add(s.lastAction());
                    s.deleteAction();
                  });
              if (CollectionUtils.isNotEmpty(coordsList)) {
                F30001.DPosition.Builder position;
                for (GoBangCoords coords : coordsList) {
                  position = F30001.DPosition.newBuilder();
                  position.setPositionX(coords.getPositionX());
                  position.setPositionY(coords.getPositionY());
                  position.setColor(coords.getColor());
                  builder.addBreakPosition(position);
                  goBangRoom.initCoords(coords);
                }
              }
            } else {
              GoBangPlayer nowPlayer = goBangRoom.getPlayerInfo(goBangRoom.getNextActionId());
              List<GoBangCoords> coordsList = Lists.newLinkedList();
              // 用户身份 1「黑」 2「白」.
              if (nowPlayer.getIdentity() == 1) {
                GoBangPlayer whitePlayer = goBangRoom.getPlayerList().get(1);
                coordsList.add(whitePlayer.lastAction());
                whitePlayer.deleteAction();
              } else {
                GoBangPlayer blackPlayer = goBangRoom.getPlayerList().get(0);
                coordsList.add(blackPlayer.lastAction());
                blackPlayer.deleteAction();
              }
              if (CollectionUtils.isNotEmpty(coordsList)) {
                F30001.DPosition.Builder position;
                for (GoBangCoords coords : coordsList) {
                  position = F30001.DPosition.newBuilder();
                  position.setPositionX(coords.getPositionX());
                  position.setPositionY(coords.getPositionY());
                  position.setColor(coords.getColor());
                  builder.addBreakPosition(position);
                  goBangRoom.initCoords(coords);
                }
              }
            }
            builder.setActionTime(GoBangAssets.getInt(GoBangAssets.SHOW_TIMEOUT));
            builder.setNextUserID(goBangRoom.getRegretUser());
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.CONFIRM_APPLICATION,
                    builder.build().toByteArray()), goBangRoom.getRoomId());
            goBangRoom.setNextActionId(goBangRoom.getRegretUser());
            // 添加定时
            actionTimeout(goBangRoom.getRoomId());
          } else {
            goBangRoom.setIndexRegret(0);
            goBangRoom.setRegretUser(0L);
            LocalDateTime endTime = goBangRoom.getActionTime().plusSeconds(25L);
            LocalDateTime newTime = LocalDateTime.now();
            Duration duration = Duration.between(newTime, endTime);
            int durations = (int) duration.getSeconds();
            builder.setActionTime(durations);
            F30001.DPosition.Builder position = F30001.DPosition.newBuilder();
            position.setPositionX(-1).setPositionY(-1).setColor(-1);
            builder.addBreakPosition(position);
            builder.setNextUserID(goBangRoom.getNextActionId());
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.CONFIRM_APPLICATION,
                    builder.build().toByteArray()), goBangRoom.getRoomId());
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
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:13
   */
  private void disconnected(Channel channel, Packet packet) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(goBangRoom)) {
        F30001.F300018S2C.Builder builder = F30001.F300018S2C.newBuilder();
        List<GoBangPlayer> playerList = goBangRoom.getPlayerList();
        F30001.PlayerInfo.Builder playerInfo;
        F30001.GameStyle.Builder gameStyle;
        for (GoBangPlayer player : playerList) {
          playerInfo = F30001.PlayerInfo.newBuilder();
          gameStyle = F30001.GameStyle.newBuilder();
          playerInfo.setNick(player.getUserName());
          playerInfo.setUserID(player.getUserId());
          playerInfo.setUrl(player.getUserIcon());
          playerInfo.setSex(player.getUserSex());
          playerInfo.setIdentity(player.getIdentity());
          if (StringUtils.isNotEmpty(player.getGameDecorate())) {
            gameStyle.setChessUrl(player.getGameDecorate());
            playerInfo.setGameStyle(gameStyle);
          }
          List<GoBangCoords> coordsList = player.getActionCoords();
          if (CollectionUtils.isNotEmpty(coordsList)) {
            F30001.DPosition.Builder position;
            for (GoBangCoords coords : coordsList) {
              position = F30001.DPosition.newBuilder();
              position.setPositionX(coords.getPositionX())
                  .setPositionY(coords.getPositionY())
                  .setColor(coords.getColor());
              playerInfo.addDPosition(position);
            }
          }
          builder.addBreakLineInfo(playerInfo);
        }
        // 游戏已开始
        if (goBangRoom.getRoomStatus() == 1) {
          builder.setActionUserId(goBangRoom.getNextActionId());
          LocalDateTime udt = goBangRoom.getActionTime().plusSeconds(25L);
          LocalDateTime nds = LocalDateTime.now();
          Duration duration = Duration.between(nds, udt);
          int second = Math.toIntExact(duration.getSeconds());
          builder.setActionTime(second);
          channel.writeAndFlush(
              new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.DISCONNECTED,
                  builder.build().toByteArray()));
        } else {
          // 游戏已结束 && 游戏未开始
          builder.setActionUserId(0).setActionTime(0);
          channel.writeAndFlush(
              new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.DISCONNECTED,
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
   * @create 2020/7/23 21:12
   * @update 2020/9/23 20:13
   */
  private void closeGame(Channel channel, Packet packet) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(goBangRoom)) {
        GoBangPlayer checkPlayer = goBangRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          if (goBangRoom.getRoomStatus() == 1) {
            goBangRoom.destroy();
            goBangRoom.leaveGame(packet.userId);
            closePage(channel, packet);
            if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
              this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
            }
            GoBangPlayer player = goBangRoom.getPlayerList().get(0);
            if (player.getIsRobot() == 0) {
              RobotManager.deleteGameRobot(player.getUserId());
              clearData(goBangRoom.getRoomId());
            } else {
              F30001.F300013S2C.Builder builder = F30001.F300013S2C.newBuilder();
              builder.setUserID(player.getUserId());
              LocalDateTime startTime = goBangRoom.getStartTime();
              LocalDateTime newTime = LocalDateTime.now();
              Duration duration = Duration.between(startTime, newTime);
              int durations = (int) duration.getSeconds();
              if (durations > GoBangAssets.getInt(GoBangAssets.SETTLEMENT_TIME)) {
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
              player.sendPacket(
                  new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.GAME_SETTLEMENT,
                      builder.build().toByteArray()));
              // 每日任务.玩3局五子棋
              Map<String, Object> taskInfo1 = Maps.newHashMap();
              taskInfo1.put("userId", player.getUserId());
              taskInfo1.put("code", TaskEnum.PGT0001.getCode());
              taskInfo1.put("progress", 1);
              taskInfo1.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo1);
              // 活动处理 丹枫迎秋
              Map<String, Object> activity = Maps.newHashMap();
              activity.put("userId", player.getUserId());
              activity.put("code", ActivityEnum.ACT000102.getCode());
              activity.put("progress", 1);
              this.activityRemote.openHandler(activity);
              // 每日任务.赢1局五子棋
              Map<String, Object> taskInfo2 = Maps.newHashMap();
              taskInfo2.put("userId", builder.getUserID());
              taskInfo2.put("code", TaskEnum.PGT0010.getCode());
              taskInfo2.put("progress", 1);
              taskInfo2.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo2);
              achievementTask(packet.roomId);
              clearData(goBangRoom.getRoomId());
            }
          } else {
            if (goBangRoom.getPlayerList().size() == 1) {
              goBangRoom.destroy();
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              clearData(goBangRoom.getRoomId());
            } else {
              if (goBangRoom.getTimeOutMap().containsKey((int) GoBangCmd.START_GAME)) {
                goBangRoom.cancelTimeOut((int) GoBangCmd.START_GAME);
              }
              goBangRoom.leaveGame(packet.userId);
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              GoBangPlayer player = goBangRoom.getPlayerList().get(0);
              if (player.getIsRobot() == 0) {
                RobotManager.deleteGameRobot(player.getUserId());
                clearData(packet.roomId);
              } else {
                F30001.F300013S2C.Builder builder = F30001.F300013S2C.newBuilder();
                builder.setUserID(player.getUserId()).setAddExp(0).setAddGlod(0);
                if (MemManager.isExists(player.getUserId())) {
                  builder.setIsDouble(1);
                }
                player.sendPacket(
                    new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.GAME_SETTLEMENT,
                        builder.build().toByteArray()));
                clearData(goBangRoom.getRoomId());
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
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:14
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
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:14
   */
  private void clearData(Long roomId) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
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
   * TODO 游玩记录.
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:15
   */
  private void joinGoBangRoom(Packet packet) {
    try {
      if (packet.roomId > GoBangAssets.getLong(GoBangAssets.TEST_ID)) {
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
   * TODO 开始游戏.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:15
   */
  private void startGame(Long roomId) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        goBangRoom.setRoomStatus(1);
        F30001.F300011S2C.Builder builder = F30001.F300011S2C.newBuilder();
        builder.setActionUserId(goBangRoom.getNextActionId());
        builder.setActionTime(GoBangAssets.getInt(GoBangAssets.SHOW_TIMEOUT));
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.START_GAME,
                builder.build().toByteArray()), goBangRoom.getRoomId());
        goBangRoom.setUpGameStartTime();
        GoBangPlayer goBangPlayer = goBangRoom.getPlayerInfo(goBangRoom.getNextActionId());
        if (goBangPlayer.getIsRobot() == 0) {
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
   * TODO 游戏经验.
   *
   * @param playerId [玩家ID]
   * @param exp [游戏经验]
   * @return [游戏经验]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/16 14:57
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
   * TODO 游戏结算.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:16
   */
  private void gameSettlement(Long roomId) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        if (goBangRoom.getNextActionId() != null && goBangRoom.getNextActionId() > 0) {
          goBangRoom.destroy();
          long userId = goBangRoom.getNextActionId();
          LocalDateTime startTime = goBangRoom.getStartTime();
          LocalDateTime newTime = LocalDateTime.now();
          Duration duration = Duration.between(startTime, newTime);
          int durations = (int) duration.getSeconds();
          if (durations > GoBangAssets.getInt(GoBangAssets.SETTLEMENT_TIME)) {
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
   * TODO 玩家成就.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/29 21:07
   * @update 2020/9/23 20:16
   */
  private void achievementTask(Long roomId) {
    GoBangRoom goBangRoom = GAME_DATA.get(roomId);
    if (Objects.nonNull(goBangRoom)) {
      List<GoBangPlayer> playerList = goBangRoom.getPlayerList();
      if (CollectionUtils.isNotEmpty(playerList)) {
        int sum = 0;
        for (GoBangPlayer player : playerList) {
          if (player.getIsRobot() == 1) {
            int placeNum = player.getActionCoords().size();
            if (placeNum > 0) {
              // 玩家成就.星罗棋布 落子3000颗
              Map<String, Object> taskSuc0001 = Maps.newHashMap();
              taskSuc0001.put("userId", player.getUserId());
              taskSuc0001.put("code", AchievementEnum.AMT0001.getCode());
              taskSuc0001.put("progress", placeNum);
              taskSuc0001.put("isReset", 0);
              this.userRemote.achievementHandlers(taskSuc0001);
              // 累计天元200次 (7,7)
              List<GoBangCoords> coordsList = player.getActionCoords();
              int index = 0;
              if (CollectionUtils.isNotEmpty(coordsList)) {
                for (GoBangCoords coords : coordsList) {
                  if (coords.getPositionX() == 7 && coords.getPositionY() == 7) {
                    index = 1;
                    break;
                  }
                }
              }
              if (index == 1) {
                // 玩家成就.天人合一
                Map<String, Object> taskSuc0002 = Maps.newHashMap();
                taskSuc0002.put("userId", player.getUserId());
                taskSuc0002.put("code", AchievementEnum.AMT0002.getCode());
                taskSuc0002.put("progress", 1);
                taskSuc0002.put("isReset", 0);
                this.userRemote.achievementHandlers(taskSuc0002);
              }
              sum = sum + placeNum;
            }
            // 8次以上4连
            if (player.getSuccessIndex() >= 8) {
              // 玩家成就.棋逢对手
              Map<String, Object> taskSuc0004 = Maps.newHashMap();
              taskSuc0004.put("userId", player.getUserId());
              taskSuc0004.put("code", AchievementEnum.AMT0004.getCode());
              taskSuc0004.put("progress", 1);
              taskSuc0004.put("isReset", 0);
              this.userRemote.achievementHandlers(taskSuc0004);
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
          } else {
            int placeNum = player.getActionCoords().size();
            sum = sum + placeNum;
          }
        }
        if (sum >= GoBangAssets.getInt(GoBangAssets.TAKE_UP_HALF)) {
          for (GoBangPlayer player : playerList) {
            if (player.getIsRobot() == 1) {
              // 玩家成就.半壁江山
              Map<String, Object> taskSuc0003 = Maps.newHashMap();
              taskSuc0003.put("userId", player.getUserId());
              taskSuc0003.put("code", AchievementEnum.AMT0003.getCode());
              taskSuc0003.put("progress", 1);
              taskSuc0003.put("isReset", 0);
              this.userRemote.achievementHandlers(taskSuc0003);
            }
          }
        }
      }
    }
  }

  /**
   * TODO 胜利结算. 真
   *
   * @param position [坐标数据]
   * @param roomId [房间ID]
   * @param userId [用户ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:17
   */
  private void victoryReally(F30001.DPosition position, Long roomId, Long userId) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        F30001.F300013S2C.Builder builder = F30001.F300013S2C.newBuilder();
        builder.setUserID(userId);
        // 五连坐标
        List<GoBangCoords> goBangCoordsList = goBangRoom.getIs4LinkRecord();
        GoBangCoords coords = new GoBangCoords(position.getPositionX(), position.getPositionY());
        coords.setColor(position.getColor());
        goBangCoordsList.add(coords);
        F30001.DPosition.Builder positions;
        for (GoBangCoords coordinate : goBangCoordsList) {
          positions = F30001.DPosition.newBuilder();
          positions.setPositionX(coordinate.getPositionX());
          positions.setPositionY(coordinate.getPositionY());
          positions.setColor(coordinate.getColor());
          builder.addPosiList(positions);
        }
        if (MemManager.isExists(userId)) {
          builder.setAddExp(gainExperience(userId, 4));
          builder.setIsDouble(1);
        } else {
          builder.setAddExp(gainExperience(userId, 2));
        }
        builder.setAddGlod(10);
        GoBangPlayer winPlayer = goBangRoom.getPlayerInfo(userId);
        winPlayer.sendPacket(
            new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.GAME_SETTLEMENT,
                builder.build().toByteArray()));
        // 1 黑 2 白
        if (winPlayer.getIdentity() == 1) {
          GoBangPlayer losePlayer = goBangRoom.getPlayerList().get(1);
          if (MemManager.isExists(losePlayer.getUserId())) {
            builder.setIsDouble(1);
          } else {
            builder.setIsDouble(0);
          }
          builder.setAddExp(0).setAddGlod(0);
          losePlayer.sendPacket(
              new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.GAME_SETTLEMENT,
                  builder.build().toByteArray()));
        } else {
          GoBangPlayer losePlayer = goBangRoom.getPlayerList().get(0);
          if (MemManager.isExists(losePlayer.getUserId())) {
            builder.setIsDouble(1);
          } else {
            builder.setIsDouble(0);
          }
          builder.setAddExp(0).setAddGlod(0);
          losePlayer.sendPacket(
              new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.GAME_SETTLEMENT,
                  builder.build().toByteArray()));
        }
        List<GoBangPlayer> playerList = goBangRoom.getPlayerList();
        playerList.stream().filter(player -> player.getIsRobot() == 1)
            .forEach(player -> {
              // 每日任务.玩3局五子棋
              Map<String, Object> taskInfo = Maps.newHashMap();
              taskInfo.put("userId", player.getUserId());
              taskInfo.put("code", TaskEnum.PGT0001.getCode());
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
        GoBangPlayer indexPlayer = goBangRoom.getPlayerInfo(builder.getUserID());
        if (indexPlayer.getIsRobot() == 1) {
          // 每日任务.赢1局五子棋]
          Map<String, Object> taskInfo = Maps.newHashMap();
          taskInfo.put("userId", builder.getUserID());
          taskInfo.put("code", TaskEnum.PGT0010.getCode());
          taskInfo.put("progress", 1);
          taskInfo.put("isReset", 0);
          this.userRemote.taskHandler(taskInfo);
        }
        achievementTask(roomId);
        clearData(roomId);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 胜利结算. 假
   *
   * @param position [棋坐标]
   * @param roomId [房间ID]
   * @param userId [用户ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:17
   */
  private void victoryDummy(F30001.DPosition position, Long roomId, Long userId) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        F30001.F300013S2C.Builder builder = F30001.F300013S2C.newBuilder();
        builder.setUserID(userId).setAddExp(0).setAddGlod(0);
        // 五连坐标
        List<GoBangCoords> goBangCoordsList = goBangRoom.getIs4LinkRecord();
        GoBangCoords coords = new GoBangCoords(position.getPositionX(), position.getPositionY());
        coords.setColor(position.getColor());
        goBangCoordsList.add(coords);
        F30001.DPosition.Builder positions;
        for (GoBangCoords coordinate : goBangCoordsList) {
          positions = F30001.DPosition.newBuilder();
          positions.setPositionX(coordinate.getPositionX());
          positions.setPositionY(coordinate.getPositionY());
          positions.setColor(coordinate.getColor());
          builder.addPosiList(positions);
        }
        List<GoBangPlayer> playerList = goBangRoom.getPlayerList();
        playerList.forEach(player -> {
          if (MemManager.isExists(player.getUserId())) {
            builder.setIsDouble(1);
          }
          player.sendPacket(
              new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.GAME_SETTLEMENT,
                  builder.build().toByteArray()));
          builder.clearIsDouble();
        });
        playerList.stream().filter(player -> player.getIsRobot() == 1)
            .forEach(player -> {
              // 每日任务.玩3局五子棋
              Map<String, Object> taskInfo = Maps.newHashMap();
              taskInfo.put("userId", player.getUserId());
              taskInfo.put("code", TaskEnum.PGT0001.getCode());
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
        GoBangPlayer indexPlayer = goBangRoom.getPlayerInfo(builder.getUserID());
        if (indexPlayer.getIsRobot() == 1) {
          // 每日任务.赢1局五子棋
          Map<String, Object> taskInfo = Maps.newHashMap();
          taskInfo.put("userId", builder.getUserID());
          taskInfo.put("code", TaskEnum.PGT0010.getCode());
          taskInfo.put("progress", 1);
          taskInfo.put("isReset", 0);
          this.userRemote.taskHandler(taskInfo);
        }
        achievementTask(roomId);
        clearData(roomId);
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
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:18
   */
  private void concedeReally(Long roomId, Long userId) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        F30001.F300013S2C.Builder builder = F30001.F300013S2C.newBuilder();
        GoBangPlayer losePlayer = goBangRoom.getPlayerInfo(userId);
        // 1 黑 2 白
        if (losePlayer.getIdentity() == 1) {
          GoBangPlayer winPlayer = goBangRoom.getPlayerList().get(1);
          builder.setUserID(winPlayer.getUserId());
          if (MemManager.isExists(winPlayer.getUserId())) {
            builder.setAddExp(gainExperience(winPlayer.getUserId(), 4));
            builder.setIsDouble(1);
          } else {
            builder.setAddExp(2);
          }
          builder.setAddGlod(10);
          winPlayer.sendPacket(
              new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.GAME_SETTLEMENT,
                  builder.build().toByteArray()));
          builder.clearIsDouble();
        } else {
          GoBangPlayer winPlayer = goBangRoom.getPlayerList().get(0);
          builder.setUserID(winPlayer.getUserId());
          if (MemManager.isExists(winPlayer.getUserId())) {
            builder.setAddExp(gainExperience(winPlayer.getUserId(), 4));
            builder.setIsDouble(1);
          } else {
            builder.setAddExp(2);
          }
          builder.setAddGlod(10);
          winPlayer.sendPacket(
              new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.GAME_SETTLEMENT,
                  builder.build().toByteArray()));
          builder.clearIsDouble();
        }
        if (MemManager.isExists(losePlayer.getUserId())) {
          builder.setIsDouble(1);
        } else {
          builder.setIsDouble(0);
        }
        builder.setAddExp(0).setAddGlod(0);
        losePlayer.sendPacket(
            new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.GAME_SETTLEMENT,
                builder.build().toByteArray()));
        List<GoBangPlayer> playerList = goBangRoom.getPlayerList();
        playerList.stream().filter(player -> player.getIsRobot() == 1)
            .forEach(player -> {
              // 每日任务.玩3局五子棋
              Map<String, Object> taskInfo = Maps.newHashMap();
              taskInfo.put("userId", player.getUserId());
              taskInfo.put("code", TaskEnum.PGT0001.getCode());
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
        GoBangPlayer indexPlayer = goBangRoom.getPlayerInfo(builder.getUserID());
        if (indexPlayer.getIsRobot() == 1) {
          // 每日任务.赢1局五子棋
          Map<String, Object> taskInfo = Maps.newHashMap();
          taskInfo.put("userId", builder.getUserID());
          taskInfo.put("code", TaskEnum.PGT0010.getCode());
          taskInfo.put("progress", 1);
          taskInfo.put("isReset", 0);
          this.userRemote.taskHandler(taskInfo);
        }
        achievementTask(roomId);
        clearData(roomId);
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
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:18
   */
  private void concedeDummy(Long roomId, Long userId) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        F30001.F300013S2C.Builder builder = F30001.F300013S2C.newBuilder();
        builder.setAddExp(0).setAddGlod(0);
        // 1 黑 2 白
        GoBangPlayer losePlayer = goBangRoom.getPlayerInfo(userId);
        if (losePlayer.getIdentity() == 1) {
          GoBangPlayer winPlayer = goBangRoom.getPlayerList().get(1);
          builder.setUserID(winPlayer.getUserId());
        } else {
          GoBangPlayer winPlayer = goBangRoom.getPlayerList().get(0);
          builder.setUserID(winPlayer.getUserId());
        }
        List<GoBangPlayer> playerList = goBangRoom.getPlayerList();
        playerList.forEach(player -> {
          if (MemManager.isExists(player.getUserId())) {
            builder.setIsDouble(1);
          }
          player.sendPacket(
              new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.GAME_SETTLEMENT,
                  builder.build().toByteArray()));
          builder.clearIsDouble();
        });
        playerList.stream().filter(player -> player.getIsRobot() == 1)
            .forEach(player -> {
              // 每日任务.玩3局五子棋
              Map<String, Object> taskInfo = Maps.newHashMap();
              taskInfo.put("userId", player.getUserId());
              taskInfo.put("code", TaskEnum.PGT0001.getCode());
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
        GoBangPlayer indexPlayer = goBangRoom.getPlayerInfo(builder.getUserID());
        if (indexPlayer.getIsRobot() == 1) {
          // 每日任务.赢1局五子棋
          Map<String, Object> taskInfo = Maps.newHashMap();
          taskInfo.put("userId", builder.getUserID());
          taskInfo.put("code", TaskEnum.PGT0010.getCode());
          taskInfo.put("progress", 1);
          taskInfo.put("isReset", 0);
          this.userRemote.taskHandler(taskInfo);
        }
        achievementTask(roomId);
        clearData(roomId);
      }
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
    F30001.F30001S2C.Builder builder = F30001.F30001S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.ENTER_ROOM,
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
    F30001.F30001S2C.Builder builder = F30001.F30001S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.ENTER_ROOM,
            builder.setResult(1).build().toByteArray()));
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
          new Packet(ActionCmd.APP_HEART, GoBangCmd.CLOSE_LOADING, null), playerId);
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
      GoBangRoom goBangRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(goBangRoom)) {
        GoBangPlayer goBangPlayer = goBangRoom.getPlayerInfo(packet.userId);
        List gameDecorate = this.orderRemote.gameDecorate((packet.userId), ActionCmd.GAME_GO_BANG);
        if (CollectionUtils.isNotEmpty(gameDecorate)) {
          List<Map<String, Object>> decorateList = JsonUtils.listMap(gameDecorate);
          if (CollectionUtils.isNotEmpty(decorateList)) {
            for (Map<String, Object> objectMap : decorateList) {
              Integer labelCode = (Integer) objectMap.get("labelCode");
              if (labelCode == 1000) {
                long productId = ((Number) objectMap.get("productId")).longValue();
                Result result = this.gameRemote.getGameSetAssetsList(productId, 30001L);
                List<Map<String, Object>> assetsList =
                    result.getCode().equals(0) ? JsonUtils.listMap(result.getData()) : null;
                if (CollectionUtils.isNotEmpty(assetsList)) {
                  if (assetsList.size() > 0) {
                    for (Map<String, Object> stringObjectMap : assetsList) {
                      if (goBangPlayer.getIdentity().equals(stringObjectMap.get("assetsLabel"))) {
                        String assetsUrl = StringUtils.nvl(stringObjectMap.get("assetsUrl"));
                        goBangPlayer.setGameDecorate(assetsUrl);
                        break;
                      }
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
   * TODO 等待玩家. 30(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/14 17:10
   * @update 2020/9/14 17:10
   */
  private void playWaitTimeout(Long roomId) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        if (!goBangRoom.getTimeOutMap().containsKey((int) GoBangCmd.WAIT_PLAYER)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> waitExamine(roomId)
              ), 30, TimeUnit.SECONDS);
          goBangRoom.addTimeOut(GoBangCmd.WAIT_PLAYER, timeout);
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
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:19
   */
  private void waitTimeout(Long roomId) {
    try {
      GoBangRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        Timeout timeout = GroupManager.newTimeOut(
            task -> execute(()
                -> waitExamine(roomId)
            ), 15, TimeUnit.SECONDS);
        roomInfo.addTimeOut(GoBangCmd.WAIT_PLAYER, timeout);
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
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:20
   */
  private void waitExamine(Long roomId) {
    GoBangRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(GoBangCmd.WAIT_PLAYER);
      int playerSize = roomInfo.getPlayerList().size();
      if (playerSize == 0) {
        clearData(roomId);
      } else if (playerSize == 1) {
        roomInfo.destroy();
        GoBangPlayer player = roomInfo.getPlayerList().get(0);
        if (player.getIsRobot() == 0) {
          RobotManager.deleteGameRobot(player.getUserId());
          clearData(roomId);
        } else {
          F30001.F300013S2C.Builder builder = F30001.F300013S2C.newBuilder();
          builder.setUserID(player.getUserId()).setAddExp(0).setAddGlod(0);
          if (MemManager.isExists(player.getUserId())) {
            builder.setIsDouble(1);
          }
          player.sendPacket(
              new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.GAME_SETTLEMENT,
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
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:20
   */
  private void startTimeout(Long roomId) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        if (!goBangRoom.getTimeOutMap().containsKey((int) GoBangCmd.START_GAME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> startExamine(roomId)
              ), 3, TimeUnit.SECONDS);
          goBangRoom.addTimeOut(GoBangCmd.START_GAME, timeout);
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
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:21
   */
  private void startExamine(Long roomId) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        goBangRoom.removeTimeOut((int) GoBangCmd.START_GAME);
        int playerSize = goBangRoom.getPlayerList().size();
        if (playerSize == 0) {
          clearData(roomId);
        } else if (playerSize == 1) {
          goBangRoom.destroy();
          GoBangPlayer goBangPlayer = goBangRoom.getPlayerList().get(0);
          if (goBangPlayer.getIsRobot() == 0) {
            RobotManager.deleteGameRobot(goBangPlayer.getUserId());
            clearData(goBangRoom.getRoomId());
          } else {
            F30001.F300013S2C.Builder builder = F30001.F300013S2C.newBuilder();
            builder.setUserID(goBangPlayer.getUserId()).setAddExp(0).setAddGlod(0);
            if (MemManager.isExists(goBangPlayer.getUserId())) {
              builder.setIsDouble(1);
            }
            goBangPlayer.sendPacket(new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.GAME_SETTLEMENT,
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
   * @create 2020/7/23 21:12
   * @update 2020/9/23 20:21
   */
  private void actionTimeout(Long roomId) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        if (!goBangRoom.getTimeOutMap().containsKey((int) GoBangCmd.PLAYER_PLACEMENT)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> actionExamine(roomId)
              ), 25, TimeUnit.SECONDS);
          goBangRoom.addTimeOut(GoBangCmd.PLAYER_PLACEMENT, timeout);
          goBangRoom.setActionTime(LocalDateTime.now());
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
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:21
   */
  private void actionExamine(Long roomId) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        goBangRoom.removeTimeOut((int) GoBangCmd.PLAYER_PLACEMENT);
        goBangRoom.destroy();
        gameSettlement(roomId);
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
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        if (!goBangRoom.getTimeOutMap().containsKey((int) GoBangCmd.PLAYER_PLACEMENT)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> robotStartExamine(roomId)
              ), 3, TimeUnit.SECONDS);
          goBangRoom.addTimeOut(GoBangCmd.PLAYER_PLACEMENT, timeout);
          goBangRoom.setActionTime(LocalDateTime.now());
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
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        goBangRoom.removeTimeOut((int) GoBangCmd.PLAYER_PLACEMENT);
        robotAction(roomId, goBangRoom.getNextActionId(), new GoBangCoords(7, 7));
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
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        if (!goBangRoom.getTimeOutMap().containsKey((int) GoBangCmd.PLAYER_PLACEMENT)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> robotActionExamine(roomId)
              ), 3, TimeUnit.SECONDS);
          goBangRoom.addTimeOut(GoBangCmd.PLAYER_PLACEMENT, timeout);
          goBangRoom.setActionTime(LocalDateTime.now());
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
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        goBangRoom.removeTimeOut((int) GoBangCmd.PLAYER_PLACEMENT);
        GoBangCoords goBangCoords = goBangRoom.explore();
        robotAction(roomId, goBangRoom.getNextActionId(), goBangCoords);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 机器定时. 1(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 17:15
   * @update 2020/9/15 17:15
   */
  private void robotConfirmTimeout(Long roomId) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        if (!goBangRoom.getTimeOutMap().containsKey((int) GoBangCmd.CONFIRM_APPLICATION)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> robotConfirmExamine(roomId)
              ), 1, TimeUnit.SECONDS);
          goBangRoom.addTimeOut(GoBangCmd.CONFIRM_APPLICATION, timeout);
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
  private void robotConfirmExamine(Long roomId) {
    try {
      GoBangRoom goBangRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(goBangRoom)) {
        goBangRoom.removeTimeOut((int) GoBangCmd.CONFIRM_APPLICATION);
        F30001.F300015S2C.Builder builder = F30001.F300015S2C.newBuilder();
        boolean flag = RandomUtils.nextBoolean();
        if (flag) {
          goBangRoom.cancelTimeOut(GoBangCmd.PLAYER_PLACEMENT);
          List<GoBangPlayer> playerList = goBangRoom.getPlayerList();
          // 申请玩家
          GoBangPlayer player = goBangRoom.getPlayerInfo(goBangRoom.getRegretUser());
          // 当前操作玩家
          if (player.getUserId().equals(goBangRoom.getNextActionId())) {
            List<GoBangCoords> coordsList = Lists.newLinkedList();
            playerList.stream()
                .filter(s -> s.getActionCoords().size() > 0)
                .forEach(s -> {
                  coordsList.add(s.lastAction());
                  s.deleteAction(); });
            if (CollectionUtils.isNotEmpty(coordsList)) {
              F30001.DPosition.Builder position;
              for (GoBangCoords coords : coordsList) {
                position = F30001.DPosition.newBuilder();
                position.setPositionX(coords.getPositionX());
                position.setPositionY(coords.getPositionY());
                position.setColor(coords.getColor());
                builder.addBreakPosition(position);
                goBangRoom.initCoords(coords);
              }
            }
          } else {
            GoBangPlayer nowPlayer = goBangRoom.getPlayerInfo(goBangRoom.getNextActionId());
            List<GoBangCoords> coordsList = Lists.newLinkedList();
            // 用户身份 1「黑」 2「白」.
            if (nowPlayer.getIdentity() == 1) {
              GoBangPlayer whitePlayer = goBangRoom.getPlayerList().get(1);
              coordsList.add(whitePlayer.lastAction());
              whitePlayer.deleteAction();
            } else {
              GoBangPlayer blackPlayer = goBangRoom.getPlayerList().get(0);
              coordsList.add(blackPlayer.lastAction());
              blackPlayer.deleteAction();
            }
            if (CollectionUtils.isNotEmpty(coordsList)) {
              F30001.DPosition.Builder position;
              for (GoBangCoords coords : coordsList) {
                position = F30001.DPosition.newBuilder();
                position.setPositionX(coords.getPositionX());
                position.setPositionY(coords.getPositionY());
                position.setColor(coords.getColor());
                builder.addBreakPosition(position);
                goBangRoom.initCoords(coords);
              }
            }
          }
          builder.setActionTime(GoBangAssets.getInt(GoBangAssets.SHOW_TIMEOUT));
          builder.setNextUserID(goBangRoom.getRegretUser());
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.CONFIRM_APPLICATION,
                  builder.setResult(0).build().toByteArray()), goBangRoom.getRoomId());
          goBangRoom.setNextActionId(goBangRoom.getRegretUser());
          // 添加定时
          actionTimeout(goBangRoom.getRoomId());
        } else {
          goBangRoom.setIndexRegret(0);
          goBangRoom.setRegretUser(0L);
          LocalDateTime endTime = goBangRoom.getActionTime().plusSeconds(25L);
          LocalDateTime newTime = LocalDateTime.now();
          Duration duration = Duration.between(newTime, endTime);
          int durations = (int) duration.getSeconds();
          builder.setActionTime(durations);
          F30001.DPosition.Builder position = F30001.DPosition.newBuilder();
          position.setPositionX(-1).setPositionY(-1).setColor(-1);
          builder.addBreakPosition(position);
          builder.setNextUserID(goBangRoom.getNextActionId());
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_GO_BANG, GoBangCmd.CONFIRM_APPLICATION,
                  builder.setResult(1).build().toByteArray()), goBangRoom.getRoomId());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }
}
