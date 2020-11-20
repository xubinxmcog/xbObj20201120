package com.enuos.live.handle.game.f30031;

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
import com.enuos.live.proto.f30031msg.F30031;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.rest.ActivityRemote;
import com.enuos.live.rest.GameRemote;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

/**
 * TODO 排雷先锋.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/5/15 14:37
 */

@Component
@AbstractAction(cmd = ActionCmd.GAME_MINESWEEPER)
public class Minesweeper extends AbstractActionHandler {

  /** 房间游戏数据. */
  private static ConcurrentHashMap<Long, MinesweeperRoom> GAME_DATA = new ConcurrentHashMap<>();

  /** Feign调用. */
  @Resource
  private GameRemote gameRemote;
  @Resource
  private UserRemote userRemote;
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
        case MinesweeperCmd.ENTER_ROOM:
          enterRoom(channel, packet);
          break;
        case MinesweeperCmd.PLAYER_MOVES:
          playerMoves(channel, packet);
          break;
        case MinesweeperCmd.CONFESS:
          confess(channel, packet);
          break;
        case MinesweeperCmd.CLOSE_GAME:
          closeGame(channel, packet);
          break;
        default:
          LoggerManager.warn("[GAME 30031 HANDLE] CHILD ERROR: [{}]", packet.child);
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
   * @create 2020/9/23 15:53
   * @update 2020/9/23 15:53
   */
  @Override
  public void shutOff(Long userId, Long attachId) {
    try {
      MinesweeperRoom roomInfo = GAME_DATA.get(attachId);
      if (roomInfo != null) {
        if (roomInfo.getRoomStatus() == 0) {
          if (roomInfo.getPlayerList().size() == 1) {
            roomInfo.destroy();
            MinesweeperPlayer player = roomInfo.getPlayerList().get(0);
            if (player.getIsRobot() == 0) {
              RobotManager.deleteGameRobot(player.getUserId());
            }
            clearData(attachId);
          } else {
            if (roomInfo.getTimeOutMap().containsKey((int) MinesweeperCmd.START_GAME)) {
              roomInfo.cancelTimeOut((int) MinesweeperCmd.START_GAME);
            }
            roomInfo.leaveGame(userId);
            MinesweeperPlayer player = roomInfo.getPlayerList().get(0);
            if (player.getIsRobot() == 0) {
              RobotManager.deleteGameRobot(player.getUserId());
              clearData(roomInfo.getRoomId());
            } else {
              F30031.F300314S2C.Builder builder = F30031.F300314S2C.newBuilder();
              builder.setUserID(player.getUserId()).setAddExp(0).setUserID(10);
              if (MemManager.isExists(player.getUserId())) {
                builder.setIsDouble(1);
              }
              player.sendPacket(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.SETTLEMENT,
                  builder.build().toByteArray()));
              clearData(roomInfo.getRoomId());
            }
          }
        } else if (roomInfo.getRoomStatus() == 1) {
          roomInfo.destroy();
          List<MinesweeperPlayer> playerList = roomInfo.getPlayerList();
          playerList.stream().filter(player -> player.getIsRobot() == 1)
              .forEach(player -> {
                // 每日任务 玩3局扫雷]
                Map<String, Object> taskInfo = Maps.newHashMap();
                taskInfo.put("userId", player.getUserId());
                taskInfo.put("code", TaskEnum.PGT0004.getCode());
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
          achievementTask(roomInfo.getRoomId());
          roomInfo.leaveGame(userId);
          if (roomInfo.getPlayerList().size() > 0) {
            MinesweeperPlayer player = roomInfo.getPlayerList().get(0);
            if (player.getIsRobot() == 0) {
              RobotManager.deleteGameRobot(player.getUserId());
              clearData(attachId);
            } else {
              F30031.F300314S2C.Builder builder = F30031.F300314S2C.newBuilder();
              LocalDateTime startTime = roomInfo.getStartTime();
              LocalDateTime newTime = LocalDateTime.now();
              Duration duration = Duration.between(startTime, newTime);
              int durations = (int) duration.getSeconds();
              builder.setUserID(player.getUserId());
              if (durations > MinesweeperAssets.getInt(MinesweeperAssets.SETTLEMENT_TIME)) {
                if (MemManager.isExists(player.getUserId())) {
                  builder.setAddExp(gainExperience(player.getUserId(), 4));
                  builder.setIsDouble(1);
                } else {
                  builder.setAddExp(gainExperience(player.getUserId(), 2));
                }
                builder.setAddGold(10);
              } else {
                builder.setAddExp(0);
                builder.setAddGold(0);
              }
              player.sendPacket(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.SETTLEMENT,
                  builder.build().toByteArray()));
              // 每日任务 赢1局扫雷
              Map<String, Object> taskInfo = Maps.newHashMap();
              taskInfo.put("userId", builder.getUserID());
              taskInfo.put("code", TaskEnum.PGT0012.getCode());
              taskInfo.put("progress", 1);
              taskInfo.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo);
              clearData(roomInfo.getRoomId());
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
      GAME_DATA.computeIfAbsent(roomId, key -> new MinesweeperRoom(roomId));
      MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
      long playerId = playerIds.get(0);
      GameRobot gameRobot = RobotManager.getGameRobot(playerId);
      roomInfo.joinRobot(gameRobot);
      // 添加定时 时间到.清除房间
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
        boolean checkTest = (packet.roomId == MinesweeperAssets.getLong(MinesweeperAssets.TEST_ID));
        if (checkRoom || checkTest) {
          MinesweeperRoom minesweeperRoom = GAME_DATA.get(packet.roomId);
          if (Objects.nonNull(minesweeperRoom)) {
            MinesweeperPlayer player = minesweeperRoom.getPlayerInfo(packet.userId);
            if (minesweeperRoom.getRoomStatus() == 1) {
              if (Objects.nonNull(player)) {
                player.setChannel(channel);
                refreshData(channel, packet);
                disconnected(channel, packet);
                return;
              } else {
                playerNotExist(channel);
                return;
              }
            } else {
              if (Objects.nonNull(player)) {
                player.setChannel(channel);
                refreshData(channel, packet);
              } else {
                if (minesweeperRoom.getTimeOutMap().containsKey((int) MinesweeperCmd.WAIT_PLAYER)) {
                  minesweeperRoom.cancelTimeOut((int) MinesweeperCmd.WAIT_PLAYER);
                }
                refreshData(channel, packet);
                byte[] bytes;
                if (isTestUser) {
                  bytes = this.redisUtils.getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
                } else {
                  bytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
                }
                I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
                minesweeperRoom.joinRoom(channel, playerInfo);
                joinAnimalRoom(packet);
              }
            }
          } else {
            register(TimerEventLoop.timeGroup.next());
            GAME_DATA.computeIfAbsent(packet.roomId, key -> new MinesweeperRoom(packet.roomId));
            minesweeperRoom = GAME_DATA.get(packet.roomId);
            refreshData(channel, packet);
            byte[] bytes;
            if (isTestUser) {
              bytes = this.redisUtils.getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
            } else {
              bytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
            }
            I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
            minesweeperRoom.joinRoom(channel, playerInfo);
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
   * @create 2020/7/22 18:19
   * @update 2020/9/17 21:07
   */
  private void pushPlayerInfo(Channel channel, Packet packet) {
    try {
      MinesweeperRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        F30031.F30031S2C.Builder builder = F30031.F30031S2C.newBuilder();
        List<MinesweeperPlayer> playerList = roomInfo.getPlayerList();
        if (CollectionUtils.isNotEmpty(playerList)) {
          F30031.PlayerInfo.Builder playerInfo;
          for (MinesweeperPlayer player : playerList) {
            playerInfo = F30031.PlayerInfo.newBuilder();
            playerInfo.setNick(player.getUserName());
            playerInfo.setUserID(player.getUserId());
            playerInfo.setUrl(player.getUserIcon());
            playerInfo.setSex(player.getUserSex());
            playerInfo.setRed(player.getIdentity());
            builder.addPlayer(playerInfo);
          }
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.ENTER_ROOM,
                  builder.setResult(0).build().toByteArray()), roomInfo.getRoomId());
          if (playerList.size() == 1) {
            waitTimeout(roomInfo.getRoomId());
          } else {
            startTimeout(roomInfo.getRoomId());
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
   * TODO 玩家点击. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:57
   */
  private void playerMoves(Channel channel, Packet packet) {
    try {
      MinesweeperRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        MinesweeperPlayer checkPlayer = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30031.F300312C2S request = F30031.F300312C2S.parseFrom(packet.bytes);
          F30031.F300312S2C.Builder builder = F30031.F300312S2C.newBuilder();
          if (roomInfo.getRoomStatus() == 1) {
            MinesweeperCoords coordsInfo = roomInfo.getPlayerAction().get(packet.userId);
            if (Objects.nonNull(coordsInfo)) {
              channel.writeAndFlush(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.PLAYER_MOVES,
                  builder.setResult(1).build().toByteArray()));
            } else {
              F30031.ChoicePosition position = request.getChoicePosition();
              boolean canChoose = roomInfo.canChoose(position);
              if (!canChoose) {
                channel.writeAndFlush(
                    new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.PLAYER_MOVES,
                        builder.setResult(1).build().toByteArray()));
              } else {
                roomInfo.playerSelect(request, packet.userId);
                MinesweeperPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
                playerInfo.initFlagNum();
                builder.setResult(0);
                builder.setChoicePosition(request.getChoicePosition());
                builder.setActionUser(packet.userId);
                builder.setIsMine(request.getIsMine());
                channel.writeAndFlush(
                    new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.PLAYER_MOVES,
                        builder.build().toByteArray()));
                int playerAction = roomInfo.getPlayerAction().size();
                if (playerAction > 1) {
                  // 取消定时
                  roomInfo.cancelTimeOut(MinesweeperCmd.PLAYER_MOVES);
                  List<MinesweeperCoords> coordsList = new LinkedList<>(roomInfo.getPlayerAction().values());
                  MinesweeperCoords coords1 = coordsList.get(0);
                  MinesweeperCoords coords2 = coordsList.get(1);
                  // 相同操作-标记 1 选择 2 标雷
                  if (coords1.getIndex() == coords2.getIndex()) {
                    List<MinesweeperPlayer> playerList = roomInfo.getPlayerList();
                    for (MinesweeperPlayer player : playerList) {
                      if (player.getSameOperation() < 10) {
                        player.setSameOperation(player.getSameOperation() + 1);
                      }
                    }
                  }
                  if (coords1.equals(coords2)) {
                    // 翻开位置
                    roomInfo.playerMoves(coords1);
                  } else {
                    // 翻开位置1
                    roomInfo.playerMoves(coords1);
                    // 翻开位置2
                    roomInfo.playerMoves(coords2);
                  }
                  F30031.F300313S2C.Builder roundBuilder = F30031.F300313S2C.newBuilder();
                  F30031.PlayerChoiceInfo.Builder playerChoice;
                  List<Long> actionOrder = roomInfo.getActionOrder();
                  for (Long userId : actionOrder) {
                    playerChoice = playerAction(roomInfo.getRoomId(), userId);
                    roundBuilder.addPlayerChoiceInfo(playerChoice);
                  }
                  if (roomInfo.numberOfRemaining() == 0) {
                    roundBuilder.setLastTimes(0);
                    roundBuilder.setLastRound(roomInfo.getRoomRound());
                    roundBuilder.setLastMine(0);
                    GroupManager.sendPacketToGroup(
                        new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.END_OF_ROUND,
                            roundBuilder.build().toByteArray()), roomInfo.getRoomId());
                    roomInfo.destroy();
                    settlement(roomInfo.getRoomId());
                  } else {
                    roundBuilder.setLastTimes(MinesweeperAssets.getInt(MinesweeperAssets.SHOW_TIMEOUT));
                    roundBuilder.setLastRound(roomInfo.getRoomRound() + 1);
                    roundBuilder.setLastMine(roomInfo.getBombNum());
                    GroupManager.sendPacketToGroup(
                        new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.END_OF_ROUND,
                            roundBuilder.build().toByteArray()), roomInfo.getRoomId());
                    roomInfo.initRoomInfo();
                    int round = roomInfo.refreshRound();
                    actionTimeout(roomInfo.getRoomId(), round);
                    MinesweeperPlayer player = roomInfo.getPlayerList().get(0);
                    if (player.getIsRobot() == 0) {
                      robotActionTimeout(roomInfo.getRoomId());
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
   * TODO 玩家认输. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:57
   */
  private void confess(Channel channel, Packet packet) {
    try {
      MinesweeperRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        MinesweeperPlayer checkPlayer = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30031.F300315S2C.Builder builder = F30031.F300315S2C.newBuilder();
          if (roomInfo.getRoomStatus() == 1) {
            builder.setResult(0);
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.CONFESS,
                    builder.build().toByteArray()));
            roomInfo.destroy();
            confessSettlement(packet.roomId, packet.userId);
          } else if (roomInfo.getRoomStatus() == 0 ||  roomInfo.getRoomStatus() > 1) {
            channel.writeAndFlush(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.CONFESS,
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
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:57
   */
  private void confessSettlement(Long roomId, Long userId) {
    try {
      MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        LocalDateTime startTime = roomInfo.getStartTime();
        LocalDateTime newTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, newTime);
        int durations = (int) duration.getSeconds();
        if (durations > MinesweeperAssets.getInt(MinesweeperAssets.SETTLEMENT_TIME)) {
          confessSettlementReally(roomId, userId);
        } else {
          confessSettlementDummy(roomId, userId);
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
   * @create 2020/9/17 21:07
   * @update 2020/7/22 18:19
   */
  private void disconnected(Channel channel, Packet packet) {
    try {
      MinesweeperRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        F30031.F300316S2C.Builder builder = F30031.F300316S2C.newBuilder();
        List<MinesweeperPlayer> playerList = roomInfo.getPlayerList();
        F30031.PlayerInfo.Builder playerInfo;
        for (MinesweeperPlayer player : playerList) {
          playerInfo = F30031.PlayerInfo.newBuilder();
          playerInfo.setNick(player.getUserName());
          playerInfo.setUserID(player.getUserId());
          playerInfo.setUrl(player.getUserIcon());
          playerInfo.setSex(player.getUserSex());
          playerInfo.setRed(player.getIdentity());
          builder.addBreakLineInfo(playerInfo);
        }
        List<MinesweeperCoords> minesweeperCoordsList = roomInfo.getDesktopData();
        if (minesweeperCoordsList != null) {
          F30031.MPositionInfo.Builder position;
          for (MinesweeperCoords coords : minesweeperCoordsList) {
            position = F30031.MPositionInfo.newBuilder();
            position.setPositionX(coords.getPositionOne());
            position.setPositionY(coords.getPositionTwo());
            if (coords.getIsBomb() == 1) {
              position.setIsMine(2);
              if (coords.getIndex() == 2) {
                position.setIsCorrect(1);
              }
            } else {
              position.setIsMine(1);
            }
            position.setValue(coords.getCodeValue());
            builder.addMPositionInfo(position);
          }
        }
        if (roomInfo.getRoomStatus() == 1) {
          LocalDateTime udt = roomInfo.getLatestTime().plusSeconds(25L);
          LocalDateTime nds = LocalDateTime.now();
          Duration duration = Duration.between(nds, udt);
          int second = Math.toIntExact(duration.getSeconds());
          builder.setActionTime(second);
          channel.writeAndFlush(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.DISCONNECTED,
              builder.build().toByteArray())
          );
        } else {
          builder.setActionTime(0);
          channel.writeAndFlush(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.DISCONNECTED,
              builder.build().toByteArray())
          );
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
   * @create 2020/7/22 18:19
   * @update 2020/10/10 18:32
   */
  private void closeGame(Channel channel, Packet packet) {
    try {
      MinesweeperRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        MinesweeperPlayer checkPlayer = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          if (roomInfo.getRoomStatus() == 1) {
            roomInfo.destroy();
            List<MinesweeperPlayer> playerList = roomInfo.getPlayerList();
            playerList.stream().filter(player -> player.getIsRobot() == 1)
                .forEach(player -> {
                  // 每日任务 玩3局扫雷]
                  Map<String, Object> taskInfo = Maps.newHashMap();
                  taskInfo.put("userId", player.getUserId());
                  taskInfo.put("code", TaskEnum.PGT0004.getCode());
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
            achievementTask(roomInfo.getRoomId());
            roomInfo.leaveGame(packet.userId);
            closePage(channel, packet);
            if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
              this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
            }
            MinesweeperPlayer player = roomInfo.getPlayerList().get(0);
            if (player.getIsRobot() == 0) {
              RobotManager.deleteGameRobot(player.getUserId());
              clearData(packet.roomId);
            } else {
              F30031.F300314S2C.Builder builder = F30031.F300314S2C.newBuilder();
              LocalDateTime startTime = roomInfo.getStartTime();
              LocalDateTime newTime = LocalDateTime.now();
              Duration duration = Duration.between(startTime, newTime);
              int durations = (int) duration.getSeconds();
              builder.setUserID(player.getUserId());
              if (durations > MinesweeperAssets.getInt(MinesweeperAssets.SETTLEMENT_TIME)) {
                if (MemManager.isExists(player.getUserId())) {
                  builder.setAddExp(gainExperience(player.getUserId(), 4));
                  builder.setIsDouble(1);
                } else {
                  builder.setAddExp(gainExperience(player.getUserId(), 2));
                }
                builder.setAddGold(10);
              } else {
                builder.setAddExp(0);
                builder.setAddGold(0);
              }
              player.sendPacket(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.SETTLEMENT,
                  builder.build().toByteArray()));
              // 每日任务 赢1局扫雷
              Map<String, Object> taskInfo = Maps.newHashMap();
              taskInfo.put("userId", builder.getUserID());
              taskInfo.put("code", TaskEnum.PGT0012.getCode());
              taskInfo.put("progress", 1);
              taskInfo.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo);
              clearData(roomInfo.getRoomId());
            }
          } else {
            if (roomInfo.getPlayerList().size() == 1) {
              roomInfo.destroy();
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              MinesweeperPlayer player = roomInfo.getPlayerList().get(0);
              if (player.getIsRobot() == 0) {
                RobotManager.deleteGameRobot(player.getUserId());
              }
              clearData(roomInfo.getRoomId());
            } else {
              if (roomInfo.getTimeOutMap().containsKey((int) MinesweeperCmd.START_GAME)) {
                roomInfo.cancelTimeOut((int) MinesweeperCmd.START_GAME);
              }
              roomInfo.leaveGame(packet.userId);
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              MinesweeperPlayer player = roomInfo.getPlayerList().get(0);
              if (player.getIsRobot() == 0) {
                RobotManager.deleteGameRobot(player.getUserId());
                clearData(roomInfo.getRoomId());
              } else {
                F30031.F300314S2C.Builder builder = F30031.F300314S2C.newBuilder();
                builder.setUserID(player.getUserId()).setAddExp(0).setAddGold(0);
                if (MemManager.isExists(player.getUserId())) {
                  builder.setIsDouble(1);
                }
                player.sendPacket(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.SETTLEMENT,
                    builder.build().toByteArray()));
                clearData(roomInfo.getRoomId());
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
   * @update 2020/9/23 20:57
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
   * TODO 游玩记录.
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/30 18:11
   */
  private void joinAnimalRoom(Packet packet) {
    try {
      if (packet.roomId > MinesweeperAssets.getLong(MinesweeperAssets.TEST_ID)) {
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
   * @update 2020/9/23 20:57
   */
  private void startGame(Long roomId) {
    MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.setRoomStatus(1);
      F30031.F300311S2C.Builder builder = F30031.F300311S2C.newBuilder();
      builder.setGameRound(roomInfo.getRoomRound());
      builder.setLastTimes(MinesweeperAssets.getInt(MinesweeperAssets.SHOW_TIMEOUT));
      GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.START_GAME,
          builder.build().toByteArray()), roomInfo.getRoomId());
      roomInfo.setUpStartTime();
      // 回合定时
      actionTimeout(roomId, roomInfo.getRoomRound());
      // 机器人监测
      MinesweeperPlayer player = roomInfo.getPlayerList().get(0);
      if (player.getIsRobot() == 0) {
        robotActionTimeout(roomId);
      }
    }
  }

  /**
   * TODO 清除数据.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/30 18:11
   */
  private void clearData(Long roomId) {
    try {
      MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        GAME_DATA.remove(roomId);
        this.gameRemote.deleteRoom(roomId);
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
   * TODO 玩家操作.
   *
   * @param roomId [房间ID]
   * @param userId [玩家ID]
   * @return [操作数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/17 21:07
   */
  private F30031.PlayerChoiceInfo.Builder playerAction(Long roomId, Long userId) {
    F30031.PlayerChoiceInfo.Builder builder = F30031.PlayerChoiceInfo.newBuilder();
    MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      MinesweeperCoords coords = roomInfo.getPlayerAction().get(userId);
      int score = getPlayerScore(coords, roomId, userId);
      MinesweeperPlayer playerInfo = roomInfo.getPlayerInfo(userId);
      // 刷新得分
      playerInfo.refreshScore(score);
      builder.setScore(score);
      builder.setUserID(userId);
      builder.setAllScore(playerInfo.getUserScore());
      MinesweeperCoords target =
          roomInfo.getCoordinateData(coords.getPositionOne(), coords.getPositionTwo());
      // 是雷 标记是 1 简单选择
      if (target.getIsBomb() == 1 && coords.getIndex() == 1) {
        builder.setPositionResult(1);
        builder.setIsBlast(0);
        F30031.PositionInfo.Builder position = F30031.PositionInfo.newBuilder();
        position.setPositionX(target.getPositionOne());
        position.setPositionY(target.getPositionTwo());
        position.setValue(target.getCodeValue());
        builder.addPositionInfo(position);
      } else if (target.getIsBomb() == 1 && coords.getIndex() == 2) {
        //  是雷 标记是 2 标记雷
        builder.setPositionResult(1);
        builder.setIsBlast(1);
        F30031.PositionInfo.Builder position = F30031.PositionInfo.newBuilder();
        position.setPositionX(target.getPositionOne());
        position.setPositionY(target.getPositionTwo());
        position.setValue(target.getCodeValue());
        builder.addPositionInfo(position);
      } else {
        builder.setPositionResult(0);
        builder.setIsBlast(1);
        F30031.PositionInfo.Builder position = F30031.PositionInfo.newBuilder();
        position.setPositionX(target.getPositionOne());
        position.setPositionY(target.getPositionTwo());
        position.setValue(target.getCodeValue());
        builder.addPositionInfo(position);
        // 选择到空
        if (target.getIsBomb() == 0 && target.getCodeValue() == 0) {
          // 从空白数据中移除
          roomInfo.removeCoords(target.getPositionOne(), target.getPositionTwo());
          // 获得关联数据
          List<MinesweeperCoords> coordsList = roomInfo
              .getLinkCoords(target.getPositionOne(), target.getPositionTwo());
          if (coordsList != null && coordsList.size() > 0) {
            // 添加记录
            roomInfo.addRecord(coordsList);
            for (MinesweeperCoords object : coordsList) {
              position.setPositionX(object.getPositionOne());
              position.setPositionY(object.getPositionTwo());
              position.setValue(object.getCodeValue());
              builder.addPositionInfo(position);
            }
          }
        }
      }
    }
    return builder;
  }

  /**
   * TODO 获得得分.
   *
   * @param action 玩家操作
   * @param roomId 房间ID
   * @param userId 玩家ID
   * @return [玩家得分]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/17 21:07
   */
  private int getPlayerScore(MinesweeperCoords action, Long roomId, Long userId) {
    int score = 0;
    MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      MinesweeperCoords coords = roomInfo.getCoordinateData(action.getPositionOne(), action.getPositionTwo());
      MinesweeperPlayer player = roomInfo.getPlayerInfo(userId);
      // 标记 1 选择 2 标雷
      if (action.getIndex() == 1) {
        // 标记错误
        if (coords.getIsBomb() == 1) {
          score = -5;
          player.setStepOnBomb(player.getStepOnBomb() + 1);
        } else {
          // 标记正确
          score = 3;
        }
      } else {
        // 标记正确
        if (coords.getIsBomb() == 1) {
          score = 6;
          player.setIndexBomb(player.getIndexBomb() + 1);
        } else {
          // 标记错误
          score = -3;
        }
      }
    }
    return score;
  }

  /**
   * TODO 玩家成就.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/30 18:11
   */
  private void achievementTask(Long roomId) {
    MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      List<MinesweeperPlayer> playerList = roomInfo.getPlayerList();
      if (CollectionUtils.isNotEmpty(playerList)) {
        for (MinesweeperPlayer player : playerList) {
          if (player.getIsRobot() == 1) {
            if (player.getIndexBomb() > 0) {
              // 玩家成就.排雷先锋
              Map<String, Object> taskSuc0013 = Maps.newHashMap();
              taskSuc0013.put("userId", player.getUserId());
              taskSuc0013.put("code", AchievementEnum.AMT0013.getCode());
              taskSuc0013.put("progress", player.getIndexBomb());
              taskSuc0013.put("isReset", 0);
              this.userRemote.achievementHandlers(taskSuc0013);
              Map<String, Object> taskInfo = Maps.newHashMap();
              // 每日任务.累计正确标记地雷5次
              taskInfo.put("userId", player.getUserId());
              taskInfo.put("code", TaskEnum.PGT0020.getCode());
              taskInfo.put("progress", player.getIndexBomb());
              taskInfo.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo);
            }
            if (player.getStepOnBomb() > 0) {
              // 玩家成就.我是炮灰
              Map<String, Object> taskSuc0014 = Maps.newHashMap();
              taskSuc0014.put("userId", player.getUserId());
              taskSuc0014.put("code", AchievementEnum.AMT0014.getCode());
              taskSuc0014.put("progress", player.getStepOnBomb());
              taskSuc0014.put("isReset", 0);
              this.userRemote.achievementHandlers(taskSuc0014);
            }
            if (player.getSameOperation() > 0) {
              // 玩家成就.重复工作
              Map<String, Object> taskSuc0015 = Maps.newHashMap();
              taskSuc0015.put("userId", player.getUserId());
              taskSuc0015.put("code", AchievementEnum.AMT0015.getCode());
              taskSuc0015.put("progress", player.getSameOperation());
              taskSuc0015.put("isReset", 0);
              this.userRemote.achievementHandlers(taskSuc0015);
            }
            if (player.getIndexBomb() >= 12) {
              // 玩家成就.拆弹专家
              Map<String, Object> taskSuc0016 = Maps.newHashMap();
              taskSuc0016.put("userId", player.getUserId());
              taskSuc0016.put("code", AchievementEnum.AMT0016.getCode());
              taskSuc0016.put("progress", 1);
              taskSuc0016.put("isReset", 0);
              this.userRemote.achievementHandlers(taskSuc0016);
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
   * TODO 空的玩家.
   *
   * @param channel [通信管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/7 21:05
   * @update 2020/9/7 21:05
   */
  private void playerNotExist(Channel channel) {
    F30031.F30031S2C.Builder builder = F30031.F30031S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.ENTER_ROOM,
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
    F30031.F30031S2C.Builder builder = F30031.F30031S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.ENTER_ROOM,
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
          new Packet(ActionCmd.APP_HEART, MinesweeperCmd.CLOSE_LOADING, null), playerId);
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 游戏经验.
   *
   * @param playerId [玩家ID]
   * @return [游戏经验]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/30 18:11
   */
  private int gainExperience(Long playerId, Integer exp) {
    if (StringUtils.nvl(playerId).length() >= 9) {
      Map<String, Object> result = Maps.newHashMap();
      result.put("userId", playerId);
      result.put("experience", exp);
      result.put("gold", 10);
      Result resultExp = this.userRemote.gameHandler(result);
      Map<String, Object> objectMap =
          resultExp.getCode().equals(0) ? JsonUtils.toObjectMap(resultExp.getData()) : null;
      if (objectMap != null) {
        Integer remainderExp = (Integer) objectMap.get("remainderExp");
        if (remainderExp > 0) {
          return exp;
        } else {
          return 0;
        }
      } else {
        // 远程调用失败 展示获得经验
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
   * @update 2020/10/10 18:32
   */
  private void settlement(Long roomId) {
    try {
      MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        roomInfo.updateRoomStatus(2);
        MinesweeperPlayer onePlayer = roomInfo.getPlayerList().get(0);
        MinesweeperPlayer twoPlayer = roomInfo.getPlayerList().get(1);
        Long userId;
        if (onePlayer.getUserScore().equals(twoPlayer.getUserScore())) {
          userId = 0L;
        } else if (onePlayer.getUserScore() > twoPlayer.getUserScore()) {
          userId = onePlayer.getUserId();
        } else {
          userId = twoPlayer.getUserId();
        }
        if (userId == 0) {
          F30031.F300314S2C.Builder builder = F30031.F300314S2C.newBuilder();
          builder.setUserID(0).setAddExp(0).setAddGold(0);
          List<MinesweeperPlayer> playerList = roomInfo.getPlayerList();
          playerList.forEach(player -> {
            if (MemManager.isExists(player.getUserId())) {
              builder.setIsDouble(1);
            }
            player.sendPacket(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.SETTLEMENT,
                builder.build().toByteArray()));
            builder.clearIsDouble(); });
          playerList.stream().filter(player -> player.getIsRobot() == 1)
              .forEach(player -> {
                // 每日任务.玩3局扫雷
                Map<String, Object> taskInfo = Maps.newHashMap();
                taskInfo.put("userId", player.getUserId());
                taskInfo.put("code", TaskEnum.PGT0004.getCode());
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
          achievementTask(roomInfo.getRoomId());
          clearData(roomInfo.getRoomId());
        } else {
          LocalDateTime startTime = roomInfo.getStartTime();
          LocalDateTime newTime = LocalDateTime.now();
          Duration duration = Duration.between(startTime, newTime);
          int durations = (int) duration.getSeconds();
          if (durations > MinesweeperAssets.getInt(MinesweeperAssets.SETTLEMENT_TIME)) {
            settlementReally(roomId, userId);
          } else {
            settlementDummy(roomId, userId);
          }
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
   * @param userId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/10/10 18:32
   */
  private void settlementReally(Long roomId, Long userId) {
    MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      F30031.F300314S2C.Builder builder = F30031.F300314S2C.newBuilder();
      builder.setUserID(userId);
      if (MemManager.isExists(userId)) {
        builder.setAddExp(gainExperience(userId, 4));
        builder.setIsDouble(1);
      } else {
        builder.setAddExp(gainExperience(userId, 2));
      }
      builder.setAddGold(10);
      MinesweeperPlayer winPlayer = roomInfo.getPlayerInfo(userId);
      winPlayer.sendPacket(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.SETTLEMENT,
          builder.build().toByteArray()));
      builder.clearIsDouble();
      if (winPlayer.getIdentity() == 1) {
        MinesweeperPlayer losePlayer = roomInfo.getPlayerList().get(1);
        builder.setAddExp(0).setAddGold(0);
        if (MemManager.isExists(losePlayer.getUserId())) {
          builder.setIsDouble(1);
        }
        losePlayer.sendPacket(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.SETTLEMENT,
            builder.build().toByteArray()));
      } else {
        MinesweeperPlayer losePlayer = roomInfo.getPlayerList().get(0);
        builder.setAddExp(0).setAddGold(0);
        if (MemManager.isExists(losePlayer.getUserId())) {
          builder.setIsDouble(1);
        }
        losePlayer.sendPacket(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.SETTLEMENT,
            builder.build().toByteArray()));
      }
      List<MinesweeperPlayer> playerList = roomInfo.getPlayerList();
      playerList.stream().filter(player -> player.getIsRobot() == 1)
          .forEach(player -> {
            // 每日任务.玩3局扫雷
            Map<String, Object> taskInfo = Maps.newHashMap();
            taskInfo.put("userId", player.getUserId());
            taskInfo.put("code", TaskEnum.PGT0004.getCode());
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
      // 每日任务.赢1局扫雷]
      MinesweeperPlayer indexPlayer = roomInfo.getPlayerInfo(builder.getUserID());
      if (indexPlayer.getIsRobot() == 1) {
        Map<String, Object> taskInfo = Maps.newHashMap();
        taskInfo.put("userId", builder.getUserID());
        taskInfo.put("code", TaskEnum.PGT0012.getCode());
        taskInfo.put("progress", 1);
        taskInfo.put("isReset", 0);
        this.userRemote.taskHandler(taskInfo);
      }
      achievementTask(roomInfo.getRoomId());
      clearData(roomInfo.getRoomId());
    }
  }

  /**
   * TODO 游戏结算. 假
   *
   * @param roomId [房间ID]
   * @param userId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/10/10 18:32
   */
  private void settlementDummy(Long roomId, Long userId) {
    MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      F30031.F300314S2C.Builder builder = F30031.F300314S2C.newBuilder();
      builder.setUserID(userId).setAddExp(0).setAddGold(0);
      List<MinesweeperPlayer> playerList = roomInfo.getPlayerList();
      playerList.forEach(player -> {
        if (MemManager.isExists(player.getUserId())) {
          builder.setIsDouble(1);
        }
        player.sendPacket(
            new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.SETTLEMENT,
                builder.build().toByteArray()));
        builder.clearIsDouble();
      });
      playerList.stream().filter(player -> player.getIsRobot() == 1)
          .forEach(player -> {
            // 每日任务.玩3局扫雷
            Map<String, Object> taskInfo = Maps.newHashMap();
            taskInfo.put("userId", player.getUserId());
            taskInfo.put("code", TaskEnum.PGT0004.getCode());
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
      // 每日任务.赢1局扫雷]
      MinesweeperPlayer indexPlayer = roomInfo.getPlayerInfo(builder.getUserID());
      if (indexPlayer.getIsRobot() == 1) {
        Map<String, Object> taskInfo = Maps.newHashMap();
        taskInfo.put("userId", builder.getUserID());
        taskInfo.put("code", TaskEnum.PGT0012.getCode());
        taskInfo.put("progress", 1);
        taskInfo.put("isReset", 0);
        this.userRemote.taskHandler(taskInfo);
      }
      achievementTask(roomInfo.getRoomId());
      clearData(roomInfo.getRoomId());
    }
  }

  /**
   * TODO 认输结算. 真
   *
   * @param roomId [房间数据]
   * @param userId [玩家数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/10/10 18:32
   */
  private void confessSettlementReally(Long roomId, Long userId) {
    MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      F30031.F300314S2C.Builder builder = F30031.F300314S2C.newBuilder();
      MinesweeperPlayer player = roomInfo.getPlayerInfo(userId);
      if (player.getIdentity() == 1) {
        MinesweeperPlayer partaker = roomInfo.getPlayerList().get(1);
        builder.setUserID(partaker.getUserId());
        if (MemManager.isExists(partaker.getUserId())) {
          builder.setAddExp(gainExperience(partaker.getUserId(), 4));
          builder.setIsDouble(1);
        } else {
          builder.setAddExp(gainExperience(partaker.getUserId(), 2));
        }
        builder.setAddGold(10);
        partaker.sendPacket(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.SETTLEMENT,
            builder.build().toByteArray()));
        builder.clearIsDouble();
        if (MemManager.isExists(player.getUserId())) {
          builder.setIsDouble(1);
        }
        builder.setAddExp(0).setAddGold(0);
        player.sendPacket(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.SETTLEMENT,
            builder.build().toByteArray()));
      } else {
        MinesweeperPlayer partaker = roomInfo.getPlayerList().get(0);
        builder.setUserID(partaker.getUserId());
        if (MemManager.isExists(partaker.getUserId())) {
          builder.setAddExp(gainExperience(partaker.getUserId(), 4));
          builder.setIsDouble(1);
        } else {
          builder.setAddExp(gainExperience(partaker.getUserId(), 2));
        }
        builder.setAddGold(10);
        partaker.sendPacket(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.SETTLEMENT,
            builder.build().toByteArray()));
        builder.clearIsDouble();
        if (MemManager.isExists(player.getUserId())) {
          builder.setIsDouble(1);
        }
        builder.setAddExp(0).setAddGold(0);
        player.sendPacket(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.SETTLEMENT,
            builder.build().toByteArray()));
      }
      List<MinesweeperPlayer> playerList = roomInfo.getPlayerList();
      playerList.stream().filter(s -> s.getIsRobot() == 1)
          .forEach(s -> {
            // 每日任务.玩3局扫雷
            Map<String, Object> taskInfo = Maps.newHashMap();
            taskInfo.put("userId", player.getUserId());
            taskInfo.put("code", TaskEnum.PGT0004.getCode());
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
      // 每日任务.赢1局扫雷]
      MinesweeperPlayer indexPlayer = roomInfo.getPlayerInfo(builder.getUserID());
      if (indexPlayer.getIsRobot() == 1) {
        Map<String, Object> taskInfo = Maps.newHashMap();
        taskInfo.put("userId", builder.getUserID());
        taskInfo.put("code", TaskEnum.PGT0012.getCode());
        taskInfo.put("progress", 1);
        taskInfo.put("isReset", 0);
        this.userRemote.taskHandler(taskInfo);
      }
      achievementTask(roomInfo.getRoomId());
      clearData(roomId);
    }
  }

  /**
   * TODO 认输结算. 假
   *
   * @param roomId [房间数据]
   * @param userId [玩家数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/10/10 18:32
   */
  private void confessSettlementDummy(Long roomId, Long userId) {
    MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      F30031.F300314S2C.Builder builder = F30031.F300314S2C.newBuilder();
      MinesweeperPlayer player = roomInfo.getPlayerInfo(userId);
      if (player.getIdentity() == 1) {
        // 蓝色玩家胜利
        builder.setUserID(roomInfo.getPlayerList().get(1).getUserId());
      } else {
        // 红色玩家胜利
        builder.setUserID(roomInfo.getPlayerList().get(0).getUserId());
      }
      builder.setAddExp(0).setAddGold(0);
      List<MinesweeperPlayer> playerList = roomInfo.getPlayerList();
      playerList.forEach(players -> {
        if (MemManager.isExists(players.getUserId())) {
          builder.setIsDouble(1);
        }
        players.sendPacket(
            new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.SETTLEMENT,
                builder.build().toByteArray()));
        builder.clearIsDouble();
      });
      playerList.stream().filter(s -> s.getIsRobot() == 1)
          .forEach(s -> {
            // 每日任务.玩3局扫雷
            Map<String, Object> taskInfo = Maps.newHashMap();
            taskInfo.put("userId", player.getUserId());
            taskInfo.put("code", TaskEnum.PGT0004.getCode());
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
      // 每日任务.赢1局扫雷]
      MinesweeperPlayer indexPlayer = roomInfo.getPlayerInfo(builder.getUserID());
      if (indexPlayer.getIsRobot() == 1) {
        Map<String, Object> taskInfo = Maps.newHashMap();
        taskInfo.put("userId", builder.getUserID());
        taskInfo.put("code", TaskEnum.PGT0012.getCode());
        taskInfo.put("progress", 1);
        taskInfo.put("isReset", 0);
        this.userRemote.taskHandler(taskInfo);
      }
      achievementTask(roomInfo.getRoomId());
      clearData(roomId);
    }
  }

  /**
   * TODO 等待玩家. 30(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:57
   */
  private void waitTimeout(Long roomId) {
    try {
      MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) MinesweeperCmd.WAIT_PLAYER)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> waitExamine(roomId)
              ), 30, TimeUnit.SECONDS);
          roomInfo.addTimeOut(MinesweeperCmd.WAIT_PLAYER, timeout);
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
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  private void waitExamine(Long roomId) {
    MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(MinesweeperCmd.WAIT_PLAYER);
      int playerSize = roomInfo.getPlayerList().size();
      if (playerSize == 0) {
        clearData(roomId);
      } else if (playerSize == 1) {
        roomInfo.destroy();
        MinesweeperPlayer player = roomInfo.getPlayerList().get(0);
        F30031.F300314S2C.Builder builder = F30031.F300314S2C.newBuilder();
        builder.setUserID(player.getUserId());
        builder.setAddExp(0);
        builder.setAddGold(0);
        player.sendPacket(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.SETTLEMENT,
            builder.build().toByteArray()));
        clearData(roomId);
      }
    }
  }

  /**
   * TODO 开始游戏. 3(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:57
   */
  private void startTimeout(Long roomId) {
    try {
      MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) MinesweeperCmd.START_GAME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> startExamine(roomId)
              ), 3, TimeUnit.SECONDS);
          roomInfo.addTimeOut(MinesweeperCmd.START_GAME, timeout);
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
   * @update 2020/9/23 20:57
   */
  private void startExamine(Long roomId) {
    MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(MinesweeperCmd.START_GAME);
      int playerSize = roomInfo.getPlayerList().size();
      if (playerSize == 0) {
        clearData(roomId);
      } else if (playerSize == 1) {
        roomInfo.destroy();
        MinesweeperPlayer player = roomInfo.getPlayerList().get(0);
        F30031.F300314S2C.Builder builder = F30031.F300314S2C.newBuilder();
        builder.setUserID(player.getUserId());
        builder.setAddExp(0);
        builder.setAddGold(0);
        player.sendPacket(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.SETTLEMENT,
            builder.build().toByteArray()));
        clearData(roomId);
      } else {
        // 开始游戏
        startGame(roomId);
      }
    }
  }

  /**
   * TODO 操作定时. 25(s)
   *
   * @param roomId 房间ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:57
   */
  private void actionTimeout(Long roomId, Integer round) {
    try {
      MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) MinesweeperCmd.PLAYER_MOVES)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> actionExamine(roomId, round)
              ), 25, TimeUnit.SECONDS);
          roomInfo.addTimeOut(MinesweeperCmd.PLAYER_MOVES, timeout);
          roomInfo.setLatestTime(LocalDateTime.now());
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
   * @param round [房间回合]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/23 20:57
   */
  private void actionExamine(Long roomId, Integer round) {
    MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(MinesweeperCmd.PLAYER_MOVES);
      boolean isFinish = false;
      List<MinesweeperPlayer> playerList = roomInfo.getPlayerList();
      for (MinesweeperPlayer player : playerList) {
        if (player.getNoOperations() == MinesweeperAssets.getInt(MinesweeperAssets.FLAG_NUM)) {
          isFinish = true;
          break;
        }
      }
      if (isFinish) {
        roomInfo.destroy();
        settlement(roomId);
      } else {
        // 回合数检验
        if (roomInfo.getRoomRound().equals(round)) {
          F30031.F300313S2C.Builder roundBuilder = F30031.F300313S2C.newBuilder();
          if (roomInfo.getPlayerAction().size() == 0) {
            for (MinesweeperPlayer player : playerList) {
              // 未操作 添加标记
              player.refreshFlagNum();
            }
          } else if (roomInfo.getPlayerAction().size() == 1) {
            List<Long> userList = new LinkedList<>(roomInfo.getPlayerAction().keySet());
            MinesweeperCoords coords = roomInfo.getPlayerAction().get(userList.get(0));
            // 翻开位置
            roomInfo.playerMoves(coords);
            F30031.PlayerChoiceInfo.Builder playerChoice;
            List<Long> actionOrder = roomInfo.getActionOrder();
            for (Long userId : actionOrder) {
              playerChoice = playerAction(roomInfo.getRoomId(), userId);
              roundBuilder.addPlayerChoiceInfo(playerChoice);
            }
          }
          // 剩余数量
          if (roomInfo.numberOfRemaining() == 0) {
            roundBuilder.setLastTimes(0);
            roundBuilder.setLastRound(roomInfo.getRoomRound());
            roundBuilder.setLastMine(0);
            // 发送消息
            GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.END_OF_ROUND,
                roundBuilder.build().toByteArray()), roomInfo.getRoomId()
            );
            roomInfo.destroy();
            // 游戏结算
            settlement(roomInfo.getRoomId());
          } else {
            // 回合结束
            int rounds = roomInfo.refreshRound();
            roundBuilder.setLastTimes(MinesweeperAssets.getInt(MinesweeperAssets.SHOW_TIMEOUT));
            roundBuilder.setLastRound(rounds);
            roundBuilder.setLastMine(roomInfo.getBombNum());
            GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.END_OF_ROUND,
                roundBuilder.build().toByteArray()), roomInfo.getRoomId()
            );
            // 初始数据
            roomInfo.initRoomInfo();
            // 游戏定时
            actionTimeout(roomInfo.getRoomId(), rounds);
            MinesweeperPlayer player = roomInfo.getPlayerList().get(0);
            if (player.getIsRobot() == 0) {
              robotActionTimeout(roomInfo.getRoomId());
            }
          }
        }
      }
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
      MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        if (!roomInfo.getTimeOutMap().containsKey((int) MinesweeperCmd.ROBOT_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> robotActionExamine(roomId)
              ), 3, TimeUnit.SECONDS);
          roomInfo.addTimeOut(MinesweeperCmd.ROBOT_TIME, timeout);
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
      MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        roomInfo.removeTimeOut((int) MinesweeperCmd.ROBOT_TIME);
        MinesweeperCoords coords = roomInfo.randomSelectPosition();
        F30031.F300312C2S.Builder robotSelect = F30031.F300312C2S.newBuilder();
        F30031.ChoicePosition.Builder choice = F30031.ChoicePosition.newBuilder();
        choice.setChoicePositionX(coords.getPositionOne());
        choice.setChoicePositionY(coords.getPositionTwo());
        robotSelect.setChoicePosition(choice);
        // 是否地雷 1-不是炸弹 2-是炸弹
        boolean flag = RandomUtils.nextBoolean();
        if (flag) {
          robotSelect.setIsMine(2);
        } else {
          robotSelect.setIsMine(1);
        }
        byte[] robotByte = robotSelect.build().toByteArray();
        F30031.F300312C2S request = F30031.F300312C2S.parseFrom(robotByte);
        long robotId = roomInfo.getPlayerList().get(0).getUserId();
        robotMoveAction(roomId, robotId, request);
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
   * @param request [机器人选择]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/22 20:02
   * @update 2020/9/22 20:02
   */
  private void robotMoveAction(Long roomId, Long robotId, F30031.F300312C2S request) {
    try {
      MinesweeperRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        roomInfo.playerSelect(request, robotId);
        MinesweeperPlayer playerInfo = roomInfo.getPlayerInfo(robotId);
        playerInfo.initFlagNum();
        int playerAction = roomInfo.getPlayerAction().size();
        if (playerAction > 1) {
          roomInfo.cancelTimeOut(MinesweeperCmd.PLAYER_MOVES);
          List<MinesweeperCoords> coordsList = new LinkedList<>(roomInfo.getPlayerAction().values());
          MinesweeperCoords coords1 = coordsList.get(0);
          MinesweeperCoords coords2 = coordsList.get(1);
          // 相同操作-标记 1 选择 2 标雷
          if (coords1.getIndex() == coords2.getIndex()) {
            List<MinesweeperPlayer> playerList = roomInfo.getPlayerList();
            for (MinesweeperPlayer player : playerList) {
              if (player.getSameOperation() < 10) {
                player.setSameOperation(player.getSameOperation() + 1);
              }
            }
          }
          if (coords1.equals(coords2)) {
            // 翻开位置
            roomInfo.playerMoves(coords1);
          } else {
            // 翻开位置1
            roomInfo.playerMoves(coords1);
            // 翻开位置2
            roomInfo.playerMoves(coords2);
          }
          F30031.F300313S2C.Builder roundBuilder = F30031.F300313S2C.newBuilder();
          F30031.PlayerChoiceInfo.Builder playerChoice;
          List<Long> actionOrder = roomInfo.getActionOrder();
          for (Long userId : actionOrder) {
            playerChoice = playerAction(roomInfo.getRoomId(), userId);
            roundBuilder.addPlayerChoiceInfo(playerChoice);
          }
          if (roomInfo.numberOfRemaining() == 0) {
            roundBuilder.setLastTimes(0);
            roundBuilder.setLastRound(roomInfo.getRoomRound());
            roundBuilder.setLastMine(0);
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.END_OF_ROUND,
                    roundBuilder.build().toByteArray()), roomInfo.getRoomId());
            roomInfo.destroy();
            settlement(roomInfo.getRoomId());
          } else {
            roundBuilder.setLastTimes(MinesweeperAssets.getInt(MinesweeperAssets.SHOW_TIMEOUT));
            roundBuilder.setLastRound(roomInfo.getRoomRound() + 1);
            roundBuilder.setLastMine(roomInfo.getBombNum());
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_MINESWEEPER, MinesweeperCmd.END_OF_ROUND,
                    roundBuilder.build().toByteArray()), roomInfo.getRoomId());
            roomInfo.initRoomInfo();
            int round = roomInfo.refreshRound();
            actionTimeout(roomInfo.getRoomId(), round);
            MinesweeperPlayer player = roomInfo.getPlayerList().get(0);
            if (player.getIsRobot() == 0) {
              robotActionTimeout(roomId);
            }
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }
}
