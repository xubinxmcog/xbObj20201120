package com.enuos.live.handle.game.f30061;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.utils.annotation.AbstractAction;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import com.enuos.live.channel.SoftChannel;
import com.enuos.live.codec.Packet;
import com.enuos.live.constants.GameKey;
import com.enuos.live.pojo.MatchRoom;
import com.enuos.live.manager.AchievementEnum;
import com.enuos.live.manager.ActivityEnum;
import com.enuos.live.manager.TaskEnum;
import com.enuos.live.proto.f10001msg.F10001;
import com.enuos.live.proto.f20000msg.F20000;
import com.enuos.live.proto.f30061msg.F30061;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.rest.ActivityRemote;
import com.enuos.live.rest.GameRemote;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.task.TimerEventLoop;
import com.enuos.live.manager.ChatManager;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.manager.GroupManager;
import com.enuos.live.manager.MatchManager;
import com.enuos.live.manager.MemManager;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * TODO 你说我猜.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/5/15 14:45
 */

@Component
@AbstractAction(cmd = ActionCmd.GAME_GUESS_SAID)
public class GuessedSaid extends AbstractActionHandler {

  /** 房间游戏数据. */
  private static ConcurrentHashMap<Long, GuessedSaidRoom> GAME_DATA = new ConcurrentHashMap<>();

  /** 大部分敏感词汇在10个以内，直接返回缓存的字符串. */
  private static final String[] STAR_ARR = {"*", "**", "***", "****", "*****", "******", "*******",
      "********", "*********", "**********"};

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
   * TODO 操作分发.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 10:02
   * @update 2020/8/10 15:07
   */
  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      switch (packet.child) {
        case GuessedSaidCmd.ENTER_ROOM:
          enterRoom(channel, packet);
          break;
        case GuessedSaidCmd.PLAYER_READY:
          playerReady(channel, packet);
          break;
        case GuessedSaidCmd.SELECT_WORD:
          selectWord(channel, packet);
          break;
        case GuessedSaidCmd.START_SPEAK:
          startSpeak(channel, packet);
          break;
        case GuessedSaidCmd.STOP_SPEAK:
          stopSpeak(channel, packet);
          break;
        case GuessedSaidCmd.PLAYER_SPEAK:
          playerSpeak(channel, packet);
          break;
        case GuessedSaidCmd.PLAYER_LIKES:
          playerLikes(channel, packet);
          break;
        case GuessedSaidCmd.WATCH_INFO:
          watchInfo(channel, packet);
          break;
        case GuessedSaidCmd.JOIN_LEAVE:
          joinLeave(channel, packet);
          break;
        case GuessedSaidCmd.CHANGE_WORDS:
          changeWords(channel, packet);
          break;
        case GuessedSaidCmd.PLAYER_EXIT:
          playerExit(channel, packet);
          break;
        case GuessedSaidCmd.PLAYER_SELECT:
          playerSelect(channel, packet);
          break;
        case GuessedSaidCmd.PLAYER_CHAT:
          playerChat(channel, packet);
          break;
        default:
          LoggerManager.warn("[GAME 30061 HANDLE] CHILD ERROR: [{}]", packet.child);
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
   * @create 2020/8/13 19:25
   * @update 2020/8/13 19:25
   */
  @Override
  public void shutOff(Long userId, Long attachId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(attachId);
      if (Objects.nonNull(roomInfo)) {
        GuessedSaidPlayer player = roomInfo.getPlayerInfo(userId);
        if (Objects.nonNull(player)) {
          if (roomInfo.getRoomStatus() == 1) {
            if (player.getIdentity() == 0) {
              player.setLinkStatus(1);
              player.setPlayerStatus(3);
              GroupManager.removeChannel(attachId, player.getPlayerChannel());
              ChatManager.removeChatChannel(attachId, player.getPlayerChannel());
              this.gameRemote.leaveRoom(userId);
              F30061.F3006118S2C.Builder builder = F30061.F3006118S2C.newBuilder();
              builder.setResult(0).setUserID(userId);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_EXIT,
                      builder.build().toByteArray()), roomInfo.getRoomId());
              this.gameRemote.leaveRoom(userId);
              if (roomInfo.seatedPlayersIsDisconnected() == roomInfo.seatedPlayersNum()
                  && roomInfo.seatedPlayers().size() == 0 && roomInfo.getWatchList().size() == 0) {
                clearData(roomInfo.getRoomId());
              }
            } else {
              roomInfo.leaveGame(userId, 1);
              GroupManager.removeChannel(attachId, player.getPlayerChannel());
              ChatManager.removeChatChannel(attachId, player.getPlayerChannel());
              this.gameRemote.leaveRoom(userId);
              if (roomInfo.seatedPlayersIsDisconnected() == roomInfo.seatedPlayersNum()
                  && roomInfo.seatedPlayers().size() == 0 && roomInfo.getWatchList().size() == 0) {
                clearData(roomInfo.getRoomId());
              }
            }
          } else {
            if (player.getIdentity() == 0) {
              if (player.getPlayerStatus() == 0) {
                roomInfo.cancelTimeOut(userId.intValue());
              } else {
                roomInfo.cancelTimeOut(GuessedSaidCmd.START_GAME);
              }
              GroupManager.removeChannel(attachId, player.getPlayerChannel());
              ChatManager.removeChatChannel(attachId, player.getPlayerChannel());
              F30061.F3006118S2C.Builder builder = F30061.F3006118S2C.newBuilder();
              builder.setResult(0).setUserID(userId);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_EXIT,
                      builder.build().toByteArray()), roomInfo.getRoomId());
              // [法官：xxx离开房间！]
              F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
              message.setMessage(" " + player.getPlayerName() + " 离开房间！");
              ChatManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_CHAT, (short) 2,
                      message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
              roomInfo.leaveGame(userId, 0);
              boolean testRoom = (attachId == GuessedSaidAssets.getLong(GuessedSaidAssets.TEST_ID));
              if (roomInfo.seatedPlayers().size() == 0 && roomInfo.getWatchList().size() == 0) {
                clearData(roomInfo.getRoomId());
              } else if (!testRoom) {
                publicMatch(roomInfo.getRoomId());
                int unReady = roomInfo.unprepared();
                int isReady = roomInfo.preparations();
                if (unReady == 0 && isReady >= GuessedSaidAssets.getInt(GuessedSaidAssets.MIN_PEOPLE)) {
                  // [法官：玩家已完成准备, 游戏即将开始！]
                  F10001.F100012S2C.Builder startMessage = F10001.F100012S2C.newBuilder();
                  startMessage.setMessage(" 玩家已完成准备，游戏即将开始！");
                  ChatManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_CHAT, (short) 2,
                          startMessage.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
                  F30061.F3006117S2C.Builder timeBuilder = F30061.F3006117S2C.newBuilder();
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.HINT_TIME,
                          timeBuilder.setTimes(3).build().toByteArray()), roomInfo.getRoomId());
                  startTimeout(roomInfo.getRoomId());
                } else {
                  // 清理检测
                  List<GuessedSaidPlayer> playerList = roomInfo.getPlayerList();
                  int playGameSize = (int) playerList.stream()
                      .filter(s -> s.getPlayerId() > 0).count();
                  if (playGameSize == 0) {
                    clearTimeout(roomInfo.getRoomId());
                  }
                }
              }
            } else {
              roomInfo.leaveGame(userId, 1);
              GroupManager.removeChannel(attachId, player.getPlayerChannel());
              ChatManager.removeChatChannel(attachId, player.getPlayerChannel());
              this.gameRemote.leaveRoom(userId);
              if (roomInfo.seatedPlayers().size() == 0 && roomInfo.getWatchList().size() == 0) {
                clearData(roomInfo.getRoomId());
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
   * TODO 清除数据.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 10:03
   * @update 2020/10/9 18:11
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
   * @create 2020/8/21 18:11
   * @update 2020/9/17 21:07
   */
  @Override
  public void joinRobot(Long roomId, List<Long> playerIds) {
    // MATCH-EMPTY-METHOD
  }

  /**
   * TODO 房间信息.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 16:30
   * @update 2020/8/10 15:06
   */
  private void enterRoom(Channel channel, Packet packet) {
    try {
      boolean isTestUser = this.redisUtils.hasKey(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
      boolean isPlayer = this.redisUtils.hasKey(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
      if (isTestUser || isPlayer) {
        closeLoading(packet.userId);
        boolean checkRoom = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + packet.roomId);
        boolean checkTest = (packet.roomId == GuessedSaidAssets.getLong(GuessedSaidAssets.TEST_ID));
        if (checkRoom || checkTest) {
          GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
          if (Objects.nonNull(roomInfo)) {
            GuessedSaidPlayer player = roomInfo.getPlayerInfo(packet.userId);
            if (roomInfo.getRoomStatus() == 1) {
              if (Objects.nonNull(player)) {
                if (player.getIdentity() == 0 && player.getLinkStatus() == 1) {
                  playerNotExist(channel);
                  return;
                } else {
                  player.setPlayerChannel(channel);
                  refreshData(channel, packet);
                  disconnected(channel, packet);
                  return;
                }
              } else {
                refreshData(channel, packet);
                byte[] bytes;
                if (isTestUser) {
                  bytes = this.redisUtils.getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
                } else {
                  bytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
                }
                I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
                roomInfo.enterWatch(channel, playerInfo);
                pullDecorateInfo(packet);
                joinGuessedSaidRoom(packet);
              }
            } else {
              // 销毁清理定时
              roomInfo.cancelTimeOut((int) packet.roomId);
              if (Objects.nonNull(player)) {
                player.setPlayerChannel(channel);
                refreshData(channel, packet);
              } else {
                if (roomInfo.getTimeOutMap().containsKey((int) GuessedSaidCmd.START_GAME)) {
                  roomInfo.cancelTimeOut((int) GuessedSaidCmd.START_GAME);
                }
                refreshData(channel, packet);
                byte[] bytes;
                if (isTestUser) {
                  bytes = this.redisUtils.getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
                } else {
                  bytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
                }
                I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
                roomInfo.enterSeat(channel, playerInfo);
                pullDecorateInfo(packet);
                joinGuessedSaidRoom(packet);
                publicMatch(packet.roomId);
              }
            }
          } else {
            register(TimerEventLoop.timeGroup.next());
            GAME_DATA.computeIfAbsent(packet.roomId, key -> new GuessedSaidRoom(packet.roomId));
            roomInfo = GAME_DATA.get(packet.roomId);
            refreshData(channel, packet);
            byte[] bytes;
            if (isTestUser) {
              bytes = this.redisUtils.getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
            } else {
              bytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
            }
            I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
            roomInfo.enterSeat(channel, playerInfo);
            pullDecorateInfo(packet);
            joinGuessedSaidRoom(packet);
            publicMatch(packet.roomId);
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
   * @create 2020/8/5 10:25
   * @update 2020/8/10 15:06
   */
  private void pushPlayerInfo(Channel channel, Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        F30061.F30061S2C.Builder builder = F30061.F30061S2C.newBuilder();
        GuessedSaidPlayer player = roomInfo.getPlayerInfo(packet.userId);
        if (player.getIdentity() == 1) {
          watchInfoAndPull(channel, packet);
        } else {
          List<GuessedSaidPlayer> playerList = roomInfo.getPlayerList();
          if (CollectionUtils.isNotEmpty(playerList)) {
            F30061.PlayerInfo.Builder playerInfo;
            for (GuessedSaidPlayer saidPlayer : playerList) {
              playerInfo = F30061.PlayerInfo.newBuilder();
              playerInfo.setNick(saidPlayer.getPlayerName());
              playerInfo.setUserID(saidPlayer.getPlayerId());
              playerInfo.setUrl(saidPlayer.getPlayerAvatar());
              playerInfo.setSex(saidPlayer.getPlayerSex());
              playerInfo.setDeviPosition(saidPlayer.getSeatNumber());
              playerInfo.setState(saidPlayer.getPlayerStatus());
              if (saidPlayer.getPlayerStatus() == 0) {
                if (saidPlayer.getReadyTime() != null) {
                  LocalDateTime udt = saidPlayer.getReadyTime().plusSeconds(15L);
                  LocalDateTime nds = LocalDateTime.now();
                  Duration duration = Duration.between(nds, udt);
                  int second = Math.toIntExact(duration.getSeconds());
                  playerInfo.setStartTime(second);
                } else {
                  playerInfo.setStartTime(15);
                }
              }
              if (StringUtils.isNotEmpty(saidPlayer.getPlayerAvatarFrame())) {
                playerInfo.setIconFrame(saidPlayer.getPlayerAvatarFrame());
              }
              builder.addSeatPlayer(playerInfo);
            }
          }
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.ENTER_ROOM,
                  builder.setResult(0).build().toByteArray()), roomInfo.getRoomId());
          // [法官：xxx 进入房间！]
          F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
          message.setMessage(" " + player.getPlayerName() + " 进入房间！");
          ChatManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_CHAT, (short) 2,
                  message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
          if (player.getPlayerStatus() == 0) {
            if (!roomInfo.getTimeOutMap().containsKey((int) packet.userId)) {
              readyTimeout(roomInfo.getRoomId(), packet.userId);
            }
          }
          playerReadyCheck(roomInfo.getRoomId());
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
   * TODO 房间信息.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/5 15:49
   * @update 2020/8/10 15:06
   */
  private void watchInfoAndPull(Channel channel, Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        F30061.F30061S2C.Builder builder = F30061.F30061S2C.newBuilder();
        GuessedSaidPlayer player = roomInfo.getPlayerInfo(packet.userId);
        List<GuessedSaidPlayer> playerList = roomInfo.getPlayerList();
        if (CollectionUtils.isNotEmpty(playerList)) {
          F30061.PlayerInfo.Builder playerInfo;
          for (GuessedSaidPlayer saidPlayer : playerList) {
            playerInfo = F30061.PlayerInfo.newBuilder();
            playerInfo.setNick(saidPlayer.getPlayerName());
            playerInfo.setUserID(saidPlayer.getPlayerId());
            playerInfo.setUrl(saidPlayer.getPlayerAvatar());
            playerInfo.setSex(saidPlayer.getPlayerSex());
            playerInfo.setDeviPosition(saidPlayer.getSeatNumber());
            playerInfo.setState(saidPlayer.getPlayerStatus());
            if (StringUtils.isNotEmpty(saidPlayer.getPlayerAvatarFrame())) {
              playerInfo.setIconFrame(saidPlayer.getPlayerAvatarFrame());
            }
            builder.addSeatPlayer(playerInfo);
          }
        }
        channel.writeAndFlush(
            new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.ENTER_ROOM,
                builder.setResult(0).build().toByteArray()));
        // [法官：xxx 进入房间！]
        F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
        message.setMessage(" " + player.getPlayerName() + " 进入房间！");
        ChatManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_CHAT, (short) 2,
                message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
      } else {
        roomNotExist(channel);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 玩家准备. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 16:45
   * @update 2020/8/10 15:05
   */
  private void playerReady(Channel channel, Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        GuessedSaidPlayer player = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(player)) {
          F30061.F300611C2S request = F30061.F300611C2S.parseFrom(packet.bytes);
          F30061.F300611S2C.Builder builder = F30061.F300611S2C.newBuilder();
          if (roomInfo.getRoomStatus() == 0) {
            // 准备状态 0-准备 1-取消
            if (player.getIdentity() == 0) {
              if (request.getIsReady() == 0) {
                roomInfo.cancelTimeOut((int) packet.userId);
                builder.setResult(0).setIsReady(request.getIsReady()).setUserID(packet.userId);
                player.setPlayerStatus(1);
                int unReady = roomInfo.unprepared();
                int isReady = roomInfo.preparations();
                if (unReady > 0) {
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_READY,
                          builder.build().toByteArray()), roomInfo.getRoomId());
                } else if (isReady >= GuessedSaidAssets.getInt(GuessedSaidAssets.MIN_PEOPLE)) {
                  builder.setGameTime(3);
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_READY,
                          builder.build().toByteArray()), roomInfo.getRoomId());
                  // [法官：玩家已完成准备, 游戏即将开始！]
                  F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
                  message.setMessage(" 玩家已完成准备，游戏即将开始！");
                  ChatManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_CHAT, (short) 2,
                          message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
                  startTimeout(roomInfo.getRoomId());
                } else {
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_READY,
                          builder.build().toByteArray()), roomInfo.getRoomId());
                }
              } else {
                roomInfo.cancelTimeOut(GuessedSaidCmd.START_GAME);
                player.setPlayerStatus(0);
                builder.setResult(0).setIsReady(request.getIsReady()).setUserID(packet.userId);
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_READY,
                        builder.build().toByteArray()), roomInfo.getRoomId());
                F30061.F3006117S2C.Builder timeBuilder = F30061.F3006117S2C.newBuilder();
                channel.writeAndFlush(
                    new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.HINT_TIME,
                        timeBuilder.setTimes(15).build().toByteArray()));
                // 添加准备倒计
                readyTimeout(roomInfo.getRoomId(), player.getPlayerId());
              }
            } else {
              builder.setResult(1).setIsReady(request.getIsReady());
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_READY,
                      builder.build().toByteArray()));
            }
          } else {
            builder.setResult(1).setIsReady(request.getIsReady());
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_READY,
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
   * TODO 玩家选词. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 16:45
   * @update 2020/8/10 15:05
   */
  private void selectWord(Channel channel, Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        GuessedSaidPlayer checkPlayer = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          if (roomInfo.getActionPlayer() == packet.userId) {
            roomInfo.cancelTimeOut(GuessedSaidCmd.SELECT_WORD);
            F30061.F300613C2S request = F30061.F300613C2S.parseFrom(packet.bytes);
            String words = request.getWords();
            roomInfo.playerSelect(words);
            F30061.F300613S2C.Builder builder = F30061.F300613S2C.newBuilder();
            builder.setResult(0);
            builder.setUserID(roomInfo.getActionPlayer());
            builder.setWordHint(roomInfo.getCurrentWord().getLexiconWords() + "个字");
            builder.setWordNum(roomInfo.getCurrentWord().getLexiconWords());
            builder.addAllAnswerUserID(roomInfo.getDescribePlayerList());
            builder.setDescribeTimes(90);
            builder.setWords(request.getWords());
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.SELECT_WORD,
                    builder.setResult(0).build().toByteArray()), roomInfo.getRoomId());
            GuessedSaidPlayer player = roomInfo.getPlayerInfo(packet.userId);
            // [法官：xxx选词已结束，请开始描述！]
            F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
            message.setMessage(" " + player.getPlayerName() + " 选词已结束，请开始描述！");
            ChatManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_CHAT, (short) 2,
                    message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
            roundTimeout(roomInfo.getRoomId());
            hintTimeout(roomInfo.getRoomId());
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
   * TODO 开始说话. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 16:45
   * @update 2020/8/10 15:04
   */
  private void startSpeak(Channel channel, Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        GuessedSaidPlayer checkPlayer = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.START_SPEAK, null), roomInfo.getRoomId());
          // 打开MIC
          F20000.F200003S2C.Builder newBuilder = F20000.F200003S2C.newBuilder();
          SoftChannel.sendPacketToUserId(
              new Packet(ActionCmd.APP_HEART, (short) 3,
                  newBuilder.setType(1).build().toByteArray()), packet.userId);
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
   * TODO 停止说话. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 16:45
   * @update 2020/8/10 15:04
   */
  private void stopSpeak(Channel channel, Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        GuessedSaidPlayer checkPlayer = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.STOP_SPEAK, null), roomInfo.getRoomId());
          // 关闭MIC
          F20000.F200003S2C.Builder newBuilder = F20000.F200003S2C.newBuilder();
          SoftChannel.sendPacketToUserId(
              new Packet(ActionCmd.APP_HEART, (short) 3,
                  newBuilder.setType(0).build().toByteArray()), packet.userId);
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
   * TODO 玩家描述. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 16:45
   * @update 2020/8/10 15:03
   */
  private void playerSpeak(Channel channel, Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        GuessedSaidPlayer player = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(player)) {
          if (player.getIdentity() == 0 && player.getWordIsTrue() == 1) {
            F30061.F300616C2S request = F30061.F300616C2S.parseFrom(packet.bytes);
            player.setPlayerWords(request.getWord());
            String playerWords = request.getWord();
            playerWords = playerWords.toUpperCase();
            GuessedSaidWord word = roomInfo.getCurrentWord();
            String lexicon = word.getLexicon();
            lexicon = lexicon.toUpperCase();
            boolean isTrue = Objects.equals(lexicon, playerWords);
            F30061.F300616S2C.Builder builder = F30061.F300616S2C.newBuilder();
            builder.setUserID(packet.userId);
            builder.setDescribeResult(isTrue ? 0 : 1);
            if (isTrue) {
              player.setWordIsTrue(0);
              player.setGuessedIsTrue(player.getGuessedIsTrue() + 1);
              // 7、5、4、3、2
              int ranking = roomInfo.getScoreRanking().size();
              switch (ranking) {
                case 0:
                  builder.setScore(7);
                  break;
                case 1:
                  builder.setScore(5);
                  break;
                case 2:
                  builder.setScore(4);
                  break;
                case 3:
                  builder.setScore(3);
                  break;
                default:
                  builder.setScore(2);
                  break;
              }
              roomInfo.getScoreRanking().add(player.getPlayerId());
              player.setPlayerScore(player.getPlayerScore() + builder.getScore());
            } else {
              builder.setScore(0);
            }
            builder.setAllScore(player.getPlayerScore());
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_SPEAK,
                    builder.build().toByteArray()), roomInfo.getRoomId());
            // 玩家消息
            F30061.F3006115S2C.Builder message = F30061.F3006115S2C.newBuilder();
            message.setSpeakId(roomInfo.getActionPlayer());
            message.setWordsId(player.getPlayerId());
            message.setIcon(player.getPlayerAvatar());
            message.setNick(player.getPlayerName());
            message.setIsTrue(isTrue);
            message.setHideMessage(getStarChar(word.getLexiconWords()));
            message.setShowMessage(request.getWord());
            message.setFilterMessage(replaceMessage(word.getLexicon(), request.getWord()));
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.DEPICT_INFO,
                    message.build().toByteArray()), roomInfo.getRoomId());
            Integer isTrueNum = roomInfo.describeIsTrue();
            if (isTrueNum.equals(roomInfo.getDescribePlayerList().size() - 1)) {
              LocalDateTime udt = roomInfo.getRoundTime().plusSeconds(90L);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              if (second > GuessedSaidAssets.getInt(GuessedSaidAssets.SPECIAL_TIME)) {
                roomInfo.cancelTimeOut(GuessedSaidCmd.ROUND_START);
                // [法官：时间调整为10s.请继续加油噢~]
                F10001.F100012S2C.Builder chatMessage = F10001.F100012S2C.newBuilder();
                chatMessage.setMessage(" 时间调整为10s，请继续加油噢！");
                ChatManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_CHAT, (short) 2,
                        chatMessage.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
                F30061.F3006117S2C.Builder timeBuilder = F30061.F3006117S2C.newBuilder();
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.HINT_TIME,
                        timeBuilder.setTimes(10).build().toByteArray()), roomInfo.getRoomId());
                specialTimeout(roomInfo.getRoomId());
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
   * TODO 玩家点赞. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 16:45
   * @update 2020/8/10 15:03
   */
  private void playerLikes(Channel channel, Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        GuessedSaidPlayer player = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(player)) {
          GuessedSaidPlayer target = roomInfo.getPlayerInfo(roomInfo.getActionPlayer());
          F30061.F300618S2C.Builder builder = F30061.F300618S2C.newBuilder();
          builder.setIcon(player.getPlayerAvatar()).setNick(player.getPlayerName());
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_LIKES,
                  builder.build().toByteArray()), roomInfo.getRoomId());
          // [法官：Lake赠送给Jeffrey一朵美丽的小花~]
          F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
          message.setMessage(" " + player.getPlayerName() + " 赠送给 " + target.getPlayerName() + " 一朵美丽的小花！");
          ChatManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_CHAT, (short) 2,
                  message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
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
   * TODO 观战信息. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 16:46
   * @update 2020/8/10 15:03
   */
  private void watchInfo(Channel channel, Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        GuessedSaidPlayer player = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(player)) {
          F30061.PlayerInfo.Builder playerInfo = F30061.PlayerInfo.newBuilder();
          playerInfo.setNick(player.getPlayerName());
          playerInfo.setUserID(player.getPlayerId());
          playerInfo.setUrl(player.getPlayerAvatar());
          playerInfo.setSex(player.getPlayerSex());
          playerInfo.setDeviPosition(player.getSeatNumber());
          playerInfo.setState(player.getPlayerStatus());
          F30061.F3006110S2C.Builder builder = F30061.F3006110S2C.newBuilder();
          builder.setNowUser(playerInfo);
          if (roomInfo.getRoomStatus() == 0) {
            if (player.getPlayerStatus() >= 1) {
              builder.setIsCanAction(false);
            } else {
              builder.setIsCanAction(true);
            }
          } else {
            builder.setIsCanAction(false);
          }
          builder.setNowStatus(player.getIdentity());
          if (player.getIdentity() == 0) {
            builder.addAllWatchUser(roomInfo.getWatchPlayerIcon());
          } else {
            builder.addAllWatchUser(roomInfo.getWatchPlayerIcon()
                .stream()
                .filter(s -> !s.equals(player.getPlayerAvatar()))
                .collect(Collectors.toList()));
          }
          channel.writeAndFlush(
              new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.WATCH_INFO,
                  builder.build().toByteArray()));
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
   * TODO 站起坐下. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 16:46
   * @update 2020/8/10 15:02
   */
  private void joinLeave(Channel channel, Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        GuessedSaidPlayer checkPlayer = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30061.F3006111C2S request = F30061.F3006111C2S.parseFrom(packet.bytes);
          F30061.F3006111S2C.Builder builder = F30061.F3006111S2C.newBuilder();
          if (roomInfo.getRoomStatus() == 1) {
            builder.setResult(1).setStand(request.getIsStand());
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.JOIN_LEAVE,
                    builder.build().toByteArray()));
          } else {
            // 玩家操作 0-站起 1-坐下
            if (request.getIsStand() == 0) {
              enterWatch(channel, packet);
            } else {
              enterSeat(channel, packet);
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
   * TODO 玩家站起.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/10 18:57
   * @update 2020/8/10 18:57
   */
  private void enterWatch(Channel channel, Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (roomInfo != null) {
        F30061.F3006111C2S request = F30061.F3006111C2S.parseFrom(packet.bytes);
        F30061.F3006111S2C.Builder builder = F30061.F3006111S2C.newBuilder();
        GuessedSaidPlayer player = roomInfo.getPlayerInfo(packet.userId);
        if (player.getPlayerStatus() > 0) {
          builder.setResult(1).setStand(request.getIsStand());
          channel.writeAndFlush(new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.JOIN_LEAVE,
              builder.build().toByteArray()));
        } else {
          roomInfo.cancelTimeOut((int) packet.userId);
          int seat = roomInfo.leaveSeat(player.getPlayerId());
          builder.setResult(0);
          builder.setStand(request.getIsStand());
          builder.setSeatNo(seat);
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.JOIN_LEAVE,
                  builder.build().toByteArray()), roomInfo.getRoomId());
          // [法官：Jeffrey进入观战]
          F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
          message.setMessage(" " + player.getPlayerName() + " 进入观战！");
          ChatManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_CHAT, (short) 2,
                  message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
          watchInfo(channel, packet);
          int unReady = roomInfo.unprepared();
          int isReady = roomInfo.preparations();
          // 最低开始人数 4 人
          if (unReady == 0 && isReady >= GuessedSaidAssets.getInt(GuessedSaidAssets.MIN_PEOPLE)) {
            F30061.F3006117S2C.Builder timeBuilder = F30061.F3006117S2C.newBuilder();
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.HINT_TIME,
                    timeBuilder.setTimes(3).build().toByteArray()), roomInfo.getRoomId());
            // [法官：玩家已完成准备, 游戏即将开始！]
            F10001.F100012S2C.Builder chatMessage = F10001.F100012S2C.newBuilder();
            chatMessage.setMessage(" 玩家已完成准备，游戏即将开始！");
            ChatManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_CHAT, (short) 2,
                    chatMessage.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
            startTimeout(roomInfo.getRoomId());
          } else {
            // 清理检测
            List<GuessedSaidPlayer> playerList = roomInfo.getPlayerList();
            int playGameSize = (int) playerList.stream()
                .filter(s -> s.getPlayerId() > 0).count();
            if (playGameSize == 0) {
              clearTimeout(roomInfo.getRoomId());
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
   * TODO 玩家坐下.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/10 18:54
   * @update 2020/8/10 18:54
   */
  private void enterSeat(Channel channel, Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (roomInfo != null) {
        F30061.F3006111C2S request = F30061.F3006111C2S.parseFrom(packet.bytes);
        F30061.F3006111S2C.Builder builder = F30061.F3006111S2C.newBuilder();
        if (roomInfo.remainingSeat() > 0) {
          GuessedSaidPlayer player = roomInfo.getPlayerInfo(packet.userId);
          if (player.getIdentity() != 1) {
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.JOIN_LEAVE,
                    builder.setResult(1).setStand(request.getIsStand()).build().toByteArray()));
          } else {
            // 销毁清理定时
            roomInfo.cancelTimeOut((int) packet.roomId);
            roomInfo.cancelTimeOut((int) GuessedSaidCmd.START_GAME);
            int seat = roomInfo.joinSeat(packet.userId);
            builder.setResult(0);
            builder.setStand(request.getIsStand());
            builder.setSeatNo(seat);
            player = roomInfo.getPlayerInfo(packet.userId);
            F30061.PlayerInfo.Builder playerInfo = F30061.PlayerInfo.newBuilder();
            playerInfo.setNick(player.getPlayerName());
            playerInfo.setUserID(player.getPlayerId());
            playerInfo.setUrl(player.getPlayerAvatar());
            playerInfo.setSex(player.getPlayerSex());
            playerInfo.setDeviPosition(player.getSeatNumber());
            playerInfo.setState(0);
            if (StringUtils.isNotEmpty(player.getPlayerAvatarFrame())) {
              playerInfo.setIconFrame(player.getPlayerAvatarFrame());
            }
            playerInfo.setStartTime(15);
            builder.setJoinInfo(playerInfo);
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.JOIN_LEAVE,
                    builder.build().toByteArray()), roomInfo.getRoomId());
            // [法官：Jeffrey进入座位]
            F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
            message.setMessage(" " + player.getPlayerName() + " 进入座位！");
            ChatManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_CHAT, (short) 2,
                    message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
            watchInfo(channel, packet);
            readyTimeout(roomInfo.getRoomId(), packet.userId);
            boolean testRoom = (packet.roomId == GuessedSaidAssets.getLong(GuessedSaidAssets.TEST_ID));
            if (!testRoom) {
              if (roomInfo.remainingSeat() > 0) {
                MatchRoom matchRoom = new MatchRoom();
                matchRoom.setRoomId(roomInfo.getRoomId());
                matchRoom.setPeopleNum(roomInfo.remainingSeat());
                MatchManager.refreshGuessedSaidMatch(matchRoom);
              } else {
                MatchManager.delGuessedSaidMatch(packet.roomId);
              }
            }
          }
        } else {
          channel.writeAndFlush(
              new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.JOIN_LEAVE,
                  builder.setResult(1).setStand(request.getIsStand()).build().toByteArray()));
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
   * @create 2020/8/4 16:46
   * @update 2020/8/10 15:02
   */
  private void disconnected(Channel channel, Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (roomInfo != null) {
        F30061.F3006113S2C.Builder builder = F30061.F3006113S2C.newBuilder();
        List<GuessedSaidPlayer> playerList = roomInfo.getPlayerList();
        if (CollectionUtils.isNotEmpty(playerList)) {
          F30061.PlayerInfo.Builder playerInfo;
          for (GuessedSaidPlayer saidPlayer : playerList) {
            playerInfo = F30061.PlayerInfo.newBuilder();
            playerInfo.setNick(saidPlayer.getPlayerName());
            playerInfo.setUserID(saidPlayer.getPlayerId());
            playerInfo.setUrl(saidPlayer.getPlayerAvatar());
            playerInfo.setSex(saidPlayer.getPlayerSex());
            playerInfo.setDeviPosition(saidPlayer.getSeatNumber());
            playerInfo.setState(saidPlayer.getPlayerStatus());
            if (StringUtils.isNotEmpty(saidPlayer.getPlayerAvatarFrame())) {
              playerInfo.setIconFrame(saidPlayer.getPlayerAvatarFrame());
            }
            builder.addBreakLineInfo(playerInfo);
          }
        }
        // 开始游戏
        if (roomInfo.getTimeOutMap().containsKey((int) GuessedSaidCmd.START_GAME)) {
          LocalDateTime udt = roomInfo.getStartTime().plusSeconds(3L);
          LocalDateTime nds = LocalDateTime.now();
          Duration duration = Duration.between(nds, udt);
          int second = Math.toIntExact(duration.getSeconds());
          builder.setGameTime(second);
        }
        // 选词信息
        if (roomInfo.getTimeOutMap().containsKey((int) GuessedSaidCmd.SELECT_WORD)) {
          LocalDateTime udt = roomInfo.getStartTime().plusSeconds(20L);
          LocalDateTime nds = LocalDateTime.now();
          Duration duration = Duration.between(nds, udt);
          int second = Math.toIntExact(duration.getSeconds());
          GuessedSaidPlayer saidPlayer = roomInfo.getPlayerInfo(roomInfo.getActionPlayer());
          F30061.F300612S2C.Builder selectWords = F30061.F300612S2C.newBuilder();
          selectWords.setNextUserID(roomInfo.getActionPlayer());
          selectWords.setTimes(second);
          selectWords.addAllWordList(roomInfo.getTempWordsList());
          selectWords.setLastChangeNum(saidPlayer.getChangeWords());
          builder.setSelectWords(selectWords);
        }
        // 开始描述
        boolean roundTime = roomInfo.getTimeOutMap().containsKey((int) GuessedSaidCmd.ROUND_START);
        boolean specialTime = roomInfo.getTimeOutMap().containsKey((int) GuessedSaidCmd.SPECIAL_TIME);
        if (roundTime || specialTime) {
          F30061.F300613S2C.Builder startSpeak = F30061.F300613S2C.newBuilder();
          startSpeak.setResult(0);
          startSpeak.setUserID(roomInfo.getActionPlayer());
          startSpeak.setWordNum(roomInfo.getCurrentWord().getLexiconWords());
          startSpeak.addAllAnswerUserID(roomInfo.getDescribePlayerList());
          LocalDateTime udt = null;
          if (roundTime) {
            udt = roomInfo.getRoundTime().plusSeconds(90L);
          }
          if (specialTime) {
            udt = roomInfo.getRoundTime().plusSeconds(10L);
          }
          LocalDateTime nds = LocalDateTime.now();
          Duration duration = Duration.between(nds, udt);
          int second = Math.toIntExact(duration.getSeconds());
          if (second < GuessedSaidAssets.getInt(GuessedSaidAssets.TIME_CHECK)) {
            GuessedSaidWord words = roomInfo.getCurrentWord();
            String hintWord = words.getLexiconHint() + "|" + words.getLexiconWords() + "个字";
            startSpeak.setWordHint(hintWord);
          } else {
            startSpeak.setWordHint(roomInfo.getCurrentWord().getLexiconWords() + "个字");
          }
          startSpeak.setDescribeTimes(second);
          builder.setStartSpeak(startSpeak);
          List<GuessedSaidPlayer> saidPlayerList = roomInfo.playGameList();
          F30061.F300616S2C.Builder speakInfo;
          if (CollectionUtils.isNotEmpty(saidPlayerList)) {
            for (GuessedSaidPlayer saidPlayer : saidPlayerList) {
              speakInfo = F30061.F300616S2C.newBuilder();
              speakInfo.setAllScore(saidPlayer.getPlayerScore());
              speakInfo.setScore(0);
              speakInfo.setUserID(saidPlayer.getPlayerId());
              speakInfo.setDescribeResult(0);
              builder.addSpeakInfo(speakInfo);
            }
          }
        }
        // 玩家展示
        if (roomInfo.getTimeOutMap().containsKey((int) GuessedSaidCmd.PLAYER_INFO)) {
          GuessedSaidPlayer player = roomInfo.getPlayerInfo(roomInfo.getActionPlayer());
          F30061.F300617S2C.Builder playerInfo = F30061.F300617S2C.newBuilder();
          playerInfo.setNick(player.getPlayerName());
          playerInfo.setIcon(player.getPlayerAvatar());
          playerInfo.setSex(player.getPlayerSex());
          playerInfo.setDescribeWord(roomInfo.getCurrentWord().getLexicon());
          playerInfo.setUserID(player.getPlayerId());
          builder.setPlayerInfo(playerInfo);
        }
        channel.writeAndFlush(
            new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.DISCONNECTED,
                builder.build().toByteArray()));
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 换词申请. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 16:47
   * @update 2020/8/10 15:02
   */
  private void changeWords(Channel channel, Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        GuessedSaidPlayer player = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(player)) {
          if (player.getChangeWords() == 1) {
            roomInfo.initWordsInfo();
            Result result = this.gameRemote.getGuessedSaidWords();
            List<Map<String, Object>> wordsList = JsonUtils.listMap(result.getData());
            roomInfo.wordsHandler(wordsList);
            player.setChangeWords(0);
            F30061.F3006114S2C.Builder builder = F30061.F3006114S2C.newBuilder();
            builder.setLastChangeNum(0);
            builder.addAllWordList(roomInfo.getTempWordsList());
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.CHANGE_WORDS,
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
   * TODO 玩家退出. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 16:47
   * @update 2020/8/10 15:01
   */
  private void playerExit(Channel channel, Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        GuessedSaidPlayer player = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(player)) {
          // 房间状态 [0-未开始 1-已开始]
          if (roomInfo.getRoomStatus() == 1) {
            if (player.getIdentity() == 0) {
              player.setLinkStatus(1);
              player.setPlayerStatus(3);
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              F30061.F3006118S2C.Builder builder = F30061.F3006118S2C.newBuilder();
              builder.setResult(0).setUserID(packet.userId);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_EXIT,
                      builder.build().toByteArray()), roomInfo.getRoomId());
              this.gameRemote.leaveRoom(packet.userId);
              if (roomInfo.seatedPlayersIsDisconnected() == roomInfo.seatedPlayersNum()
                  && roomInfo.seatedPlayers().size() == 0 && roomInfo.getWatchList().size() == 0) {
                clearData(roomInfo.getRoomId());
              }
            } else {
              roomInfo.leaveGame(packet.userId, 1);
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              this.gameRemote.leaveRoom(packet.userId);
              if (roomInfo.seatedPlayersIsDisconnected() == roomInfo.seatedPlayersNum()
                  && roomInfo.seatedPlayers().size() == 0 && roomInfo.getWatchList().size() == 0) {
                clearData(roomInfo.getRoomId());
              }
            }
          } else {
            // 玩家身份 [0-玩家 1-观众]
            if (player.getIdentity() == 0) {
              if (player.getPlayerStatus() == 0) {
                roomInfo.cancelTimeOut((int) packet.userId);
              } else {
                roomInfo.cancelTimeOut(GuessedSaidCmd.START_GAME);
              }
              F30061.F3006118S2C.Builder builder = F30061.F3006118S2C.newBuilder();
              builder.setResult(0).setUserID(packet.userId);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_EXIT,
                      builder.build().toByteArray()), roomInfo.getRoomId());
              // [法官：xxx离开房间！]
              F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
              message.setMessage(" " + player.getPlayerName() + " 离开房间！");
              ChatManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_CHAT, (short) 2,
                      message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
              roomInfo.leaveGame(packet.userId, 0);
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              if (roomInfo.seatedPlayers().size() == 0 && roomInfo.getWatchList().size() == 0) {
                clearData(roomInfo.getRoomId());
              } else {
                publicMatch(roomInfo.getRoomId());
                int unReady = roomInfo.unprepared();
                int isReady = roomInfo.preparations();
                // 最低开始人数 4 人
                if (unReady == 0 && isReady >= GuessedSaidAssets.getInt(GuessedSaidAssets.MIN_PEOPLE)) {
                  F30061.F3006117S2C.Builder timeBuilder = F30061.F3006117S2C.newBuilder();
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.HINT_TIME,
                          timeBuilder.setTimes(3).build().toByteArray()), roomInfo.getRoomId());
                  // [法官：玩家已完成准备, 游戏即将开始！]
                  F10001.F100012S2C.Builder startMessage = F10001.F100012S2C.newBuilder();
                  startMessage.setMessage(" 玩家已完成准备，游戏即将开始！");
                  ChatManager.sendPacketToGroup(new Packet(ActionCmd.GAME_CHAT, (short) 2,
                      startMessage.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
                  startTimeout(roomInfo.getRoomId());
                } else {
                  // 清理检测
                  List<GuessedSaidPlayer> playerList = roomInfo.getPlayerList();
                  int playGameSize = (int) playerList.stream()
                      .filter(s -> s.getPlayerId() > 0).count();
                  if (playGameSize == 0) {
                    clearTimeout(roomInfo.getRoomId());
                  }
                }
              }
            } else {
              roomInfo.leaveGame(packet.userId, 1);
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              this.gameRemote.leaveRoom(packet.userId);
              if (roomInfo.seatedPlayers().size() == 0 && roomInfo.getWatchList().size() == 0) {
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
        clearData(packet.roomId);
        closePage(channel, packet);
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
   * TODO 选择座位.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @update 2020/8/31 9:26
   * @update 2020/8/31 9:26
   */
  private void playerSelect(Channel channel, Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        GuessedSaidPlayer player = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(player)) {
          F30061.F3006119C2S request = F30061.F3006119C2S.parseFrom(packet.bytes);
          F30061.F3006119S2C.Builder response = F30061.F3006119S2C.newBuilder();
          if (roomInfo.getRoomStatus() == 1) {
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_SELECT,
                response.setResult(1).setUserID(packet.userId).build().toByteArray()));
          } else {
            // 玩家身份 [0-玩家 1-观众]
            if (player.getIdentity() == 0) {
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_SELECT,
                      response.setResult(1).setUserID(packet.userId).build().toByteArray()));
            } else {
              // 目标位置
              GuessedSaidPlayer targetSeat = roomInfo.getTargetSeat(request.getSeatNo());
              if (targetSeat.getPlayerId() > 0) {
                channel.writeAndFlush(
                    new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_SELECT,
                        response.setResult(1).setUserID(packet.userId).build().toByteArray()));
              } else {
                // 销毁清理定时
                roomInfo.cancelTimeOut((int) packet.roomId);
                roomInfo.joinSeat(packet.userId, targetSeat.getSeatNumber());
                player = roomInfo.getPlayerInfo(packet.userId);
                F30061.PlayerInfo.Builder playerInfo = F30061.PlayerInfo.newBuilder();
                playerInfo.setNick(player.getPlayerName());
                playerInfo.setUserID(player.getPlayerId());
                playerInfo.setUrl(player.getPlayerAvatar());
                playerInfo.setSex(player.getPlayerSex());
                playerInfo.setDeviPosition(player.getSeatNumber());
                playerInfo.setState(player.getPlayerStatus());
                if (StringUtils.isNotEmpty(player.getPlayerAvatarFrame())) {
                  playerInfo.setIconFrame(player.getPlayerAvatarFrame());
                }
                playerInfo.setStartTime(15);
                response.setJoinInfo(playerInfo);
                response.setUserID(packet.userId);
                response.setSeatNo(request.getSeatNo());
                response.setResult(0);
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_SELECT,
                        response.build().toByteArray()), roomInfo.getRoomId());
                // [法官：Jeffrey进入座位]
                F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
                message.setMessage(" " + player.getPlayerName() + " 进入座位！");
                ChatManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_CHAT, (short) 2,
                        message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
                readyTimeout(roomInfo.getRoomId(), packet.userId);
                watchInfo(channel, packet);
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
   * TODO 玩家聊天.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/16 10:40
   * @update 2020/9/16 10:40
   */
  private void playerChat(Channel channel, Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        GuessedSaidPlayer player = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(player)) {
          F30061.F3006120C2S request = F30061.F3006120C2S.parseFrom(packet.bytes);
          F30061.F3006120S2C.Builder builder = F30061.F3006120S2C.newBuilder();
          builder.setType(1);
          builder.setIcon(player.getPlayerAvatar());
          builder.setNick(player.getPlayerName());
          // 房间状态 0-未开始 1-已开始
          if (roomInfo.getRoomStatus() == 0) {
            builder.setMessage(request.getMessage());
          } else {
            String message = request.getMessage();
            String word = roomInfo.getCurrentWord().getLexicon();
            builder.setMessage(replaceMessage(word, message));
          }
          builder.setUserID(packet.userId);
          ChatManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_CHAT,
                  builder.build().toByteArray()), packet.roomId);
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
   * TODO 替换内容.
   *
   * @param keyWords [目标词汇]
   * @param replaceWords [目标内容]
   * @return [替换内容]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/30 17:29
   * @update 2020/9/30 17:29
   */
  private String replaceMessage(String keyWords, String replaceWords) {
    String tempWords = replaceWords.toUpperCase();
    String tempLexicon = keyWords.toUpperCase();
    if (Objects.equals(tempWords, tempLexicon)) {
      return getStarChar(tempLexicon.length());
    }
    int chatLength = tempWords.length();
    int wordLength = tempLexicon.length();
    if (chatLength > wordLength) {
      int index = (tempWords.length() - wordLength) + 1;
      int flag = 1;
      for (int i = 0; i < chatLength; i++) {
        if (flag <= index) {
          String indexString;
          int intercept = i + wordLength;
          if (intercept > chatLength) {
            indexString = tempWords.substring(i, chatLength);
          } else {
            indexString = tempWords.substring(i, intercept);
          }
          if (Objects.equals(indexString, keyWords)) {
            StringBuilder sb = new StringBuilder(replaceWords);
            sb.replace(i, intercept, getStarChar(wordLength));
            replaceWords = sb.toString();
          }
          flag++;
        } else {
          break;
        }
      }
    }
    return replaceWords;
  }

  /**
   * TODO 关闭页面. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 16:29
   * @update 2020/8/10 15:15
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
   * TODO 空的玩家.
   *
   * @param channel [通信管道]
   * @author wangcaiwen|1443****11@qq.com
   * @update 2020/9/7 21:05
   * @update 2020/9/7 21:05
   */
  private void playerNotExist(Channel channel) {
    F30061.F30061S2C.Builder builder = F30061.F30061S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.ENTER_ROOM,
            builder.setResult(2).build().toByteArray()));
  }

  /**
   * TODO 空的房间.
   *
   * @param channel [通信管道]
   * @author wangcaiwen|1443****11@qq.com
   * @update 2020/9/8 14:16
   * @update 2020/9/8 14:16
   */
  private void roomNotExist(Channel channel) {
    F30061.F30061S2C.Builder builder = F30061.F30061S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.ENTER_ROOM,
            builder.setResult(1).build().toByteArray()));
  }

  /**
   * TODO 是否开放.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 15:55
   * @update 2020/10/14 15:55
   */
  private void publicMatch(Long roomId) {
    GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
    if (Objects.nonNull(roomInfo)) {
      boolean checkTest = (roomId == GuessedSaidAssets.getLong(GuessedSaidAssets.TEST_ID));
      if (!checkTest) {
        if (roomInfo.remainingSeat() > 0) {
          MatchRoom matchRoom = new MatchRoom();
          matchRoom.setRoomId(roomInfo.getRoomId());
          matchRoom.setPeopleNum(roomInfo.remainingSeat());
          MatchManager.refreshGuessedSaidMatch(matchRoom);
        } else {
          MatchManager.delGuessedSaidMatch(roomId);
        }
      }
    }
  }

  /**
   * TODO 关闭加载.
   *
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @update 2020/8/17 9:50
   * @update 2020/8/17 9:50
   */
  private void closeLoading(Long playerId) {
    try {
      SoftChannel.sendPacketToUserId(
          new Packet(ActionCmd.APP_HEART, GuessedSaidCmd.LOADING, null), playerId);
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
   * @update 2020/8/24 13:38
   * @update 2020/8/24 13:38
   */
  private void pullDecorateInfo(Packet packet) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (roomInfo != null) {
        GuessedSaidPlayer player = roomInfo.getPlayerInfo(packet.userId);
        boolean checkTest = (packet.roomId == GuessedSaidAssets.getLong(GuessedSaidAssets.TEST_ID));
        if (checkTest) {
          player.setPlayerAvatarFrame("https://7lestore.oss-cn-hangzhou.aliyuncs.com/file/admin/7ed8d12e9a13491aac07301324d2a562.svga");
        } else {
          Map<String, Object> dressInfo = this.userRemote.getUserFrame(packet.userId);
          if (dressInfo != null && !dressInfo.isEmpty()) {
            player.setPlayerAvatarFrame(StringUtils.nvl(dressInfo.get("iconFrame")));
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 准备检测.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/5 16:59
   * @update 2020/8/10 15:14
   */
  private void playerReadyCheck(Long roomId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (roomInfo.numberOfSeats() == roomInfo.getMaxPlayerNum()) {
          // [法官：座位已坐满，请大家尽快准备！]
          F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
          message.setMessage(" 座位已坐满，请大家尽快准备！");
          ChatManager.sendPacketToGroup(new Packet(ActionCmd.GAME_CHAT, (short) 2,
              message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 加密字符.
   *
   * @param length [字符长度]
   * @return [加密字符]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 13:21
   * @update 2020/8/10 15:14
   */
  private String getStarChar(int length) {
    if (length <= 0) {
      return "";
    }
    // 大部分敏感词汇在10个以内，直接返回缓存的字符串
    if (length <= GuessedSaidAssets.getInt(GuessedSaidAssets.SENSITIVE_WORD)) {
      return STAR_ARR[length - 1];
    }
    // 生成n个星号的字符串
    char[] arr = new char[length];
    for (int i = 0; i < length; i++) {
      arr[i] = '*';
    }
    return new String(arr);
  }

  /**
   * TODO 游玩记录.
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 16:33
   * @update 2020/8/10 15:13
   */
  private void joinGuessedSaidRoom(Packet packet) {
    try {
      if (packet.roomId > GuessedSaidAssets.getLong(GuessedSaidAssets.TEST_ID)) {
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
   * TODO 清除数据.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 17:00
   * @update 2020/8/10 15:13
   */
  private void clearData(Long roomId) {
    try {
      GuessedSaidRoom saidRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(saidRoom)) {
        GAME_DATA.remove(roomId);
        this.gameRemote.deleteRoom(roomId);
        if (this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId)) {
          this.redisUtils.del(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
        }
        MatchManager.delGuessedSaidMatch(roomId);
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
   * @param gold [游戏金币]
   * @return [游戏经验]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 17:09
   * @update 2020/8/10 15:13
   */
  private int gainExperience(Long playerId, Integer exp, Integer gold) {
    if (StringUtils.nvl(playerId).length() >= GuessedSaidAssets.getInt(GuessedSaidAssets.USER_ID_LENGTH)) {
      Map<String, Object> experienceInfo = Maps.newHashMap();
      experienceInfo.put("userId", playerId);
      experienceInfo.put("experience", exp);
      experienceInfo.put("gold", gold);
      Result result = this.userRemote.gameHandler(experienceInfo);
      if (result != null) {
        Map<String, Object> callbackInfo = result.getCode().equals(0) ? JsonUtils.toObjectMap(result.getData()) : null;
        if (callbackInfo != null) {
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
    // 测试账号·显示经验
    return exp;
  }

  /**
   * TODO 刷新数据.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/6 10:56
   * @update 2020/8/10 15:13
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
   * TODO 玩家成就.
   *
   * @param player [玩家信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/10 17:30
   * @update 2020/8/10 17:30
   */
  private void achievementTask(GuessedSaidPlayer player) {
    if (player.getGuessedIsTrue() > 0) {
      // 玩家成就.心领神会
      Map<String, Object> taskSuc0021 = Maps.newHashMap();
      taskSuc0021.put("userId", player.getPlayerId());
      taskSuc0021.put("code", AchievementEnum.AMT0021.getCode());
      taskSuc0021.put("progress", player.getGuessedIsTrue());
      taskSuc0021.put("isReset", 0);
      this.userRemote.achievementHandlers(taskSuc0021);
      // 每日任务.在你说我猜中.累计猜中5次
      Map<String, Object> taskInfo = Maps.newHashMap();
      taskInfo.put("userId", player.getPlayerId());
      taskInfo.put("code", TaskEnum.PGT0022.getCode());
      taskInfo.put("progress", player.getGuessedIsTrue());
      taskInfo.put("isReset", 0);
      this.userRemote.taskHandler(taskInfo);
    }
    // 每日任务.玩1局你说我猜
    Map<String, Object> taskInfo = Maps.newHashMap();
    taskInfo.put("userId", player.getPlayerId());
    taskInfo.put("code", TaskEnum.PGT0006.getCode());
    taskInfo.put("progress", 1);
    taskInfo.put("isReset", 0);
    this.userRemote.taskHandler(taskInfo);
    // 活动处理 丹枫迎秋
    Map<String, Object> activity = Maps.newHashMap();
    activity.put("userId", player.getPlayerId());
    activity.put("code", ActivityEnum.ACT000103.getCode());
    activity.put("progress", 1);
    this.activityRemote.openHandler(activity);
    // 玩家成就.高级玩家
    Map<String, Object> taskSuc0041 = Maps.newHashMap();
    taskSuc0041.put("userId", player.getPlayerId());
    taskSuc0041.put("code", AchievementEnum.AMT0041.getCode());
    taskSuc0041.put("progress", 1);
    taskSuc0041.put("isReset", 0);
    this.userRemote.achievementHandlers(taskSuc0041);
    // 玩家成就.头号玩家
    Map<String, Object> taskSuc0042 = Maps.newHashMap();
    taskSuc0042.put("userId", player.getPlayerId());
    taskSuc0042.put("code", AchievementEnum.AMT0042.getCode());
    taskSuc0042.put("progress", 1);
    taskSuc0042.put("isReset", 0);
    this.userRemote.achievementHandlers(taskSuc0042);
  }

  /**
   * TODO 四人结算.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/10 4:03
   * @update 2020/8/10 15:13
   */
  private void fourPlayersEndGame(Long roomId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        // EXP 4/3/2/0/ -> GOLD 10/5/0/0
        List<GuessedSaidPlayer> playerList = roomInfo.getRanking();
        F30061.F300619S2C.Builder builder = F30061.F300619S2C.newBuilder();
        F30061.SettlementPlayerInf.Builder playerInfo;
        int index = 0;
        for (GuessedSaidPlayer player : playerList) {
          playerInfo = F30061.SettlementPlayerInf.newBuilder();
          achievementTask(player);
          switch (index) {
            case 0:
              playerInfo.setNick(player.getPlayerName());
              playerInfo.setIcon(player.getPlayerAvatar());
              playerInfo.setSex(player.getPlayerSex());
              if (MemManager.isExists(player.getPlayerId())) {
                int exp = 4 * 2;
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), exp, 10));
                playerInfo.setIsDouble(1);
              } else {
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), 4, 10));
              }
              builder.addSettlementInf(playerInfo);
              index++;
              break;
            case 1:
              playerInfo.setNick(player.getPlayerName());
              playerInfo.setIcon(player.getPlayerAvatar());
              playerInfo.setSex(player.getPlayerSex());
              if (MemManager.isExists(player.getPlayerId())) {
                int exp = 3 * 2;
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), exp, 5));
                playerInfo.setIsDouble(1);
              } else {
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), 4, 5));
              }
              builder.addSettlementInf(playerInfo);
              index++;
              break;
            case 2:
              playerInfo.setNick(player.getPlayerName());
              playerInfo.setIcon(player.getPlayerAvatar());
              playerInfo.setSex(player.getPlayerSex());
              if (MemManager.isExists(player.getPlayerId())) {
                int exp = 2 * 2;
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), exp, 0));
                playerInfo.setIsDouble(1);
              } else {
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), 2, 0));
              }
              builder.addSettlementInf(playerInfo);
              index++;
              break;
            default:
              playerInfo.setNick(player.getPlayerName());
              playerInfo.setIcon(player.getPlayerAvatar());
              playerInfo.setSex(player.getPlayerSex());
              if (MemManager.isExists(player.getPlayerId())) {
                playerInfo.setIsDouble(1);
              }
              playerInfo.setExpNumber(0);
              builder.addSettlementInf(playerInfo);
              break;
          }
        }
        GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.GAME_SETTLE,
            builder.build().toByteArray()), roomInfo.getRoomId());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 五人结算.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/10 4:06
   * @update 2020/8/10 15:12
   */
  private void fivePlayersEndGame(Long roomId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        // EXP 4/3/2/0/0 -> GOLD 12/8/4/0/0
        List<GuessedSaidPlayer> playerList = roomInfo.getRanking();
        F30061.F300619S2C.Builder builder = F30061.F300619S2C.newBuilder();
        F30061.SettlementPlayerInf.Builder playerInfo;
        int index = 0;
        for (GuessedSaidPlayer player : playerList) {
          playerInfo = F30061.SettlementPlayerInf.newBuilder();
          achievementTask(player);
          switch (index) {
            case 0:
              playerInfo.setNick(player.getPlayerName());
              playerInfo.setIcon(player.getPlayerAvatar());
              playerInfo.setSex(player.getPlayerSex());
              if (MemManager.isExists(player.getPlayerId())) {
                int exp = 4 * 2;
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), exp, 12));
                playerInfo.setIsDouble(1);
              } else {
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), 4, 12));
              }
              builder.addSettlementInf(playerInfo);
              index++;
              break;
            case 1:
              playerInfo.setNick(player.getPlayerName());
              playerInfo.setIcon(player.getPlayerAvatar());
              playerInfo.setSex(player.getPlayerSex());
              if (MemManager.isExists(player.getPlayerId())) {
                int exp = 3 * 2;
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), exp, 8));
                playerInfo.setIsDouble(1);
              } else {
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), 3, 8));
              }
              builder.addSettlementInf(playerInfo);
              index++;
              break;
            case 2:
              playerInfo.setNick(player.getPlayerName());
              playerInfo.setIcon(player.getPlayerAvatar());
              playerInfo.setSex(player.getPlayerSex());
              if (MemManager.isExists(player.getPlayerId())) {
                int exp = 2 * 2;
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), exp, 4));
                playerInfo.setIsDouble(1);
              } else {
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), 2, 4));
              }
              builder.addSettlementInf(playerInfo);
              index++;
              break;
            case 3:
              playerInfo.setNick(player.getPlayerName());
              playerInfo.setIcon(player.getPlayerAvatar());
              playerInfo.setSex(player.getPlayerSex());
              if (MemManager.isExists(player.getPlayerId())) {
                playerInfo.setIsDouble(1);
              }
              playerInfo.setExpNumber(0);
              builder.addSettlementInf(playerInfo);
              index++;
              break;
            default:
              playerInfo.setNick(player.getPlayerName());
              playerInfo.setIcon(player.getPlayerAvatar());
              playerInfo.setSex(player.getPlayerSex());
              if (MemManager.isExists(player.getPlayerId())) {
                playerInfo.setIsDouble(1);
              }
              playerInfo.setExpNumber(0);
              builder.addSettlementInf(playerInfo);
              break;
          }
        }
        GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.GAME_SETTLE,
            builder.build().toByteArray()), roomInfo.getRoomId());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 六人结算.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/10 4:06
   * @update 2020/8/10 15:12
   */
  private void sixPlayersEndGame(Long roomId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        // EXP 4/3/2/1/0/0 -> GOLD 15/10/5/0/0/0
        List<GuessedSaidPlayer> playerList = roomInfo.getRanking();
        F30061.F300619S2C.Builder builder = F30061.F300619S2C.newBuilder();
        F30061.SettlementPlayerInf.Builder playerInfo;
        int index = 0;
        for (GuessedSaidPlayer player : playerList) {
          playerInfo = F30061.SettlementPlayerInf.newBuilder();
          achievementTask(player);
          switch (index) {
            case 0:
              playerInfo.setNick(player.getPlayerName());
              playerInfo.setIcon(player.getPlayerAvatar());
              playerInfo.setSex(player.getPlayerSex());
              if (MemManager.isExists(player.getPlayerId())) {
                int exp = 4 * 2;
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), exp, 15));
                playerInfo.setIsDouble(1);
              } else {
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), 4, 15));
              }
              builder.addSettlementInf(playerInfo);
              index++;
              break;
            case 1:
              playerInfo.setNick(player.getPlayerName());
              playerInfo.setIcon(player.getPlayerAvatar());
              playerInfo.setSex(player.getPlayerSex());
              if (MemManager.isExists(player.getPlayerId())) {
                int exp = 3 * 2;
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), exp, 10));
                playerInfo.setIsDouble(1);
              } else {
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), 3, 10));
              }
              builder.addSettlementInf(playerInfo);
              index++;
              break;
            case 2:
              playerInfo.setNick(player.getPlayerName());
              playerInfo.setIcon(player.getPlayerAvatar());
              playerInfo.setSex(player.getPlayerSex());
              if (MemManager.isExists(player.getPlayerId())) {
                int exp = 2 * 2;
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), exp, 5));
                playerInfo.setIsDouble(1);
              } else {
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), 2, 5));
              }
              builder.addSettlementInf(playerInfo);
              index++;
              break;
            case 3:
              playerInfo.setNick(player.getPlayerName());
              playerInfo.setIcon(player.getPlayerAvatar());
              playerInfo.setSex(player.getPlayerSex());
              if (MemManager.isExists(player.getPlayerId())) {
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), 2, 0));
                playerInfo.setIsDouble(1);
              } else {
                playerInfo.setExpNumber(gainExperience(player.getPlayerId(), 1, 0));
              }
              builder.addSettlementInf(playerInfo);
              index++;
              break;
            case 4:
              playerInfo.setNick(player.getPlayerName());
              playerInfo.setIcon(player.getPlayerAvatar());
              playerInfo.setSex(player.getPlayerSex());
              if (MemManager.isExists(player.getPlayerId())) {
                playerInfo.setIsDouble(1);
              }
              playerInfo.setExpNumber(0);
              builder.addSettlementInf(playerInfo);
              index++;
              break;
            default:
              playerInfo.setNick(player.getPlayerName());
              playerInfo.setIcon(player.getPlayerAvatar());
              playerInfo.setSex(player.getPlayerSex());
              if (MemManager.isExists(player.getPlayerId())) {
                playerInfo.setIsDouble(1);
              }
              playerInfo.setExpNumber(0);
              builder.addSettlementInf(playerInfo);
              break;
          }
        }
        GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.GAME_SETTLE,
            builder.build().toByteArray()), roomInfo.getRoomId());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 准备定时. 15(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/6 9:03
   * @update 2020/8/10 15:12
   */
  private void readyTimeout(Long roomId, Long playerId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey(playerId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> readyExamine(roomId, playerId)
              ), 15, TimeUnit.SECONDS);
          roomInfo.addTimeOut(playerId.intValue(), timeout);
          GuessedSaidPlayer player = roomInfo.getPlayerInfo(playerId);
          player.setReadyTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 准备检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/6 9:08
   * @update 2020/8/10 15:11
   */
  private void readyExamine(Long roomId, Long playerId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        roomInfo.removeTimeOut(playerId.intValue());
        GuessedSaidPlayer player = roomInfo.getPlayerInfo(playerId);
        if (player != null && player.getSeatNumber() > 0) {
          int seat = roomInfo.leaveSeat(player.getPlayerId());
          F30061.F3006111S2C.Builder builder = F30061.F3006111S2C.newBuilder();
          builder.setResult(0).setStand(0).setSeatNo(seat);
          GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.JOIN_LEAVE,
              builder.build().toByteArray()), roomInfo.getRoomId());
          // 剩余准备数量
          int unReady = roomInfo.unprepared();
          int isReady = roomInfo.preparations();
          if (unReady == 0 && isReady >= GuessedSaidAssets.getInt(GuessedSaidAssets.MIN_PEOPLE)) {
            F30061.F3006117S2C.Builder timeBuilder = F30061.F3006117S2C.newBuilder();
            GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.HINT_TIME,
                timeBuilder.setTimes(3).build().toByteArray()), roomInfo.getRoomId());
            // [法官：玩家已完成准备, 游戏即将开始！]
            F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
            message.setMessage(" 玩家已完成准备，游戏即将开始！");
            ChatManager.sendPacketToGroup(new Packet(ActionCmd.GAME_CHAT, (short) 2,
                message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
            // 开始定时
            startTimeout(roomInfo.getRoomId());
          } else {
            List<GuessedSaidPlayer> playerList = roomInfo.getPlayerList();
            int playGameSize = (int) playerList.stream()
                .filter(s -> s.getPlayerId() > 0).count();
            if (playGameSize == 0) {
              clearTimeout(roomInfo.getRoomId());
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
   * TODO 开始定时. 3(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 11:37
   * @update 2020/8/10 15:11
   */
  private void startTimeout(Long roomId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) GuessedSaidCmd.START_GAME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> startExamine(roomId)
              ), 3, TimeUnit.SECONDS);
          roomInfo.addTimeOut(GuessedSaidCmd.START_GAME, timeout);
          roomInfo.setStartTime(LocalDateTime.now());
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
   * @create 2020/8/7 11:50
   * @update 2020/8/10 15:11
   */
  private void startExamine(Long roomId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        roomInfo.removeTimeOut(GuessedSaidCmd.START_GAME);
        int unReady = roomInfo.unprepared();
        if (unReady == 0) {
          MatchManager.delGuessedSaidMatch(roomId);
          roomInfo.startGame();
          roomInfo.setUpActionPlayer(0L);
          Result result = this.gameRemote.getGuessedSaidWords();
          List<Map<String, Object>> wordsList = JsonUtils.listMap(result.getData());
          roomInfo.wordsHandler(wordsList);
          F30061.F300612S2C.Builder builder = F30061.F300612S2C.newBuilder();
          builder.setNextUserID(roomInfo.getActionPlayer());
          builder.setTimes(20);
          builder.addAllWordList(roomInfo.getTempWordsList());
          builder.setLastChangeNum(1);
          GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.ROUND_START,
              builder.build().toByteArray()), roomInfo.getRoomId());
          GuessedSaidPlayer player = roomInfo.getPlayerInfo(roomInfo.getActionPlayer());
          // [法官：xxx请开始选词！]
          F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
          message.setMessage(" " + player.getPlayerName() + " 请开始选词！");
          ChatManager.sendPacketToGroup(new Packet(ActionCmd.GAME_CHAT, (short) 2,
              message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
          // 选择定时
          selectTimeout(roomInfo.getRoomId());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 选词定时. 20(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 11:37
   * @update 2020/8/10 15:11
   */
  private void selectTimeout(Long roomId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) GuessedSaidCmd.SELECT_WORD)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> selectExamine(roomId)
              ), 20, TimeUnit.SECONDS);
          roomInfo.addTimeOut(GuessedSaidCmd.SELECT_WORD, timeout);
          roomInfo.setSelectTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 选择检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 11:51
   * @update 2020/8/10 15:10
   */
  private void selectExamine(Long roomId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        roomInfo.removeTimeOut(GuessedSaidCmd.SELECT_WORD);
        GuessedSaidPlayer player = roomInfo.getPlayerInfo(roomInfo.getActionPlayer());
        if (player.getLinkStatus() == 0) {
          List<String> wordsList = roomInfo.getTempWordsList();
          int wordIndex = ThreadLocalRandom.current().nextInt(wordsList.size());
          String words = wordsList.get(wordIndex);
          roomInfo.playerSelect(words);
          F30061.F300613S2C.Builder builder = F30061.F300613S2C.newBuilder();
          builder.setResult(0);
          builder.setUserID(roomInfo.getActionPlayer());
          builder.setWordHint(roomInfo.getCurrentWord().getLexiconWords() + "个字");
          builder.setWordNum(roomInfo.getCurrentWord().getLexiconWords());
          builder.addAllAnswerUserID(roomInfo.getDescribePlayerList());
          builder.setDescribeTimes(90);
          builder.setWords(words);
          GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.SELECT_WORD,
              builder.setResult(0).build().toByteArray()), roomInfo.getRoomId());
          // [法官：xxx选词已结束，请开始描述！]
          F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
          message.setMessage(" " + player.getPlayerName() + " 选词已结束，请开始描述！");
          ChatManager.sendPacketToGroup(new Packet(ActionCmd.GAME_CHAT, (short) 2,
              message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
          roundTimeout(roomInfo.getRoomId());
          hintTimeout(roomInfo.getRoomId());
        } else {
          roomInfo.initRound();
          roomInfo.setUpActionPlayer(roomInfo.getActionPlayer());
          Result result = this.gameRemote.getGuessedSaidWords();
          List<Map<String, Object>> wordsList = JsonUtils.listMap(result.getData());
          roomInfo.wordsHandler(wordsList);
          F30061.F300612S2C.Builder builder = F30061.F300612S2C.newBuilder();
          builder.setNextUserID(roomInfo.getActionPlayer());
          builder.setTimes(20);
          builder.addAllWordList(roomInfo.getTempWordsList());
          builder.setLastChangeNum(1);
          GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.ROUND_START,
              builder.build().toByteArray()), roomInfo.getRoomId());
          player = roomInfo.getPlayerInfo(roomInfo.getActionPlayer());
          // [法官：xxx请开始选词！]
          F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
          message.setMessage(" " + player.getPlayerName() + " 请开始选词！");
          ChatManager.sendPacketToGroup(new Packet(ActionCmd.GAME_CHAT, (short) 2,
              message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
          selectTimeout(roomInfo.getRoomId());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 回合定时. 90(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 11:35
   * @update 2020/8/10 15:10
   */
  private void roundTimeout(Long roomId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) GuessedSaidCmd.ROUND_START)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> describeExamine(roomId)
              ), 90, TimeUnit.SECONDS);
          roomInfo.addTimeOut(GuessedSaidCmd.ROUND_START, timeout);
          roomInfo.setRoundTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 特殊定时. 10(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 11:34
   * @update 2020/8/10 15:10
   */
  private void specialTimeout(Long roomId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) GuessedSaidCmd.SPECIAL_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> describeExamine(roomId)
              ), 10, TimeUnit.SECONDS);
          roomInfo.addTimeOut(GuessedSaidCmd.SPECIAL_TIME, timeout);
          roomInfo.setRoundTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 描述检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 11:34
   * @update 2020/8/10 15:10
   */
  private void describeExamine(Long roomId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        roomInfo.removeTimeOut(GuessedSaidCmd.ROUND_START);
        roomInfo.removeTimeOut(GuessedSaidCmd.SPECIAL_TIME);
        GuessedSaidPlayer player = roomInfo.getPlayerInfo(roomInfo.getActionPlayer());
        Integer isTrueNum = roomInfo.describeIsTrue();
        F30061.F300616S2C.Builder userScore = F30061.F300616S2C.newBuilder();
        userScore.setUserID(roomInfo.getActionPlayer());
        userScore.setDescribeResult(0);
        if (isTrueNum == 0) {
          // 玩家成就.天花乱坠
          Map<String, Object> taskSuc0022 = Maps.newHashMap();
          taskSuc0022.put("userId", player.getPlayerId());
          taskSuc0022.put("code", AchievementEnum.AMT0022.getCode());
          taskSuc0022.put("progress", 1);
          taskSuc0022.put("isReset", 0);
          this.userRemote.achievementHandlers(taskSuc0022);
          // [法官：有0个人猜中.Jeffrey加0分]
          F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
          message.setMessage(" 有0个人猜中！" + player.getPlayerName() + " 加0分");
          ChatManager.sendPacketToGroup(new Packet(ActionCmd.GAME_CHAT, (short) 2,
              message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
          userScore.setScore(0);
          userScore.setAllScore(player.getPlayerScore());
        } else if (isTrueNum.equals(roomInfo.getDescribePlayerList().size())) {
          // 玩家成就.绘声绘色
          Map<String, Object> taskSuc0023 = Maps.newHashMap();
          taskSuc0023.put("userId", player.getPlayerId());
          taskSuc0023.put("code", AchievementEnum.AMT0023.getCode());
          taskSuc0023.put("progress", 1);
          taskSuc0023.put("isReset", 0);
          this.userRemote.achievementHandlers(taskSuc0023);
          // [法官：所有人都猜中.Jeffrey不加分]
          F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
          message.setMessage(" 所有人都猜中！" + player.getPlayerName() + " 不加分");
          ChatManager.sendPacketToGroup(new Packet(ActionCmd.GAME_CHAT, (short) 2,
              message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
          userScore.setScore(0);
          userScore.setAllScore(player.getPlayerScore());
        } else {
          if (isTrueNum == 1) {
            // 玩家成就.心心相印
            Map<String, Object> taskSuc0024 = Maps.newHashMap();
            taskSuc0024.put("userId", player.getPlayerId());
            taskSuc0024.put("code", AchievementEnum.AMT0024.getCode());
            taskSuc0024.put("progress", 1);
            taskSuc0024.put("isReset", 0);
            this.userRemote.achievementHandlers(taskSuc0024);
          }
          userScore.setScore(isTrueNum * 2);
          // [法官：有3个人猜中.Jeffrey加6分]
          F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
          message.setMessage(" 有" + isTrueNum + "个人猜中！" + player.getPlayerName() + " 加" + userScore.getScore() + "分");
          ChatManager.sendPacketToGroup(new Packet(ActionCmd.GAME_CHAT, (short) 2,
              message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
          userScore.setAllScore(player.getPlayerScore() + userScore.getScore());
          player.setPlayerScore(player.getPlayerScore() + userScore.getScore());
        }
        GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_SPEAK,
            userScore.build().toByteArray()), roomInfo.getRoomId());
        // [法官：本轮结束. 答案是xxx]
        F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
        message.setMessage(" 本轮结束，答案是 " + roomInfo.getCurrentWord().getLexicon() + " ！");
        ChatManager.sendPacketToGroup(new Packet(ActionCmd.GAME_CHAT, (short) 2,
            message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
        showTimeout(roomInfo.getRoomId());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 信息展示. 2(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 17:02
   * @update 2020/8/10 15:09
   */
  private void showTimeout(Long roomId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) GuessedSaidCmd.SHOW_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> showExamine(roomId)
              ), 2, TimeUnit.SECONDS);
          roomInfo.addTimeOut(GuessedSaidCmd.SHOW_TIME, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 发送展示. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 17:03
   * @update 2020/8/10 15:09
   */
  private void showExamine(Long roomId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        roomInfo.removeTimeOut(GuessedSaidCmd.SHOW_TIME);
        GuessedSaidPlayer player = roomInfo.getPlayerInfo(roomInfo.getActionPlayer());
        F30061.F300617S2C.Builder builder = F30061.F300617S2C.newBuilder();
        builder.setNick(player.getPlayerName());
        builder.setIcon(player.getPlayerAvatar());
        builder.setSex(player.getPlayerSex());
        builder.setDescribeWord(roomInfo.getCurrentWord().getLexicon());
        builder.setUserID(player.getPlayerId());
        GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.PLAYER_INFO,
            builder.build().toByteArray()), roomInfo.getRoomId());
        // 玩家展示
        likeTimeout(roomInfo.getRoomId());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 提示展示. 5(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 12:35
   * @update 2020/8/10 15:09
   */
  private void hintTimeout(Long roomId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) GuessedSaidCmd.HINT_WORDS)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> hintExamine(roomId)
              ), 5, TimeUnit.SECONDS);
          roomInfo.addTimeOut(GuessedSaidCmd.HINT_WORDS, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 发送提示. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 12:36
   * @update 2020/8/10 15:08
   */
  private void hintExamine(Long roomId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        roomInfo.removeTimeOut(GuessedSaidCmd.HINT_WORDS);
        F30061.F3006116S2C.Builder builder = F30061.F3006116S2C.newBuilder();
        GuessedSaidWord words = roomInfo.getCurrentWord();
        String hintWord = words.getLexiconHint() + "|" + words.getLexiconWords() + "个字";
        builder.setHintWord(hintWord);
        builder.setWordNum(words.getLexiconWords());
        GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.HINT_WORDS,
            builder.build().toByteArray()), roomInfo.getRoomId());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 玩家展示. 5(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 13:02
   * @update 2020/8/10 15:08
   */
  private void likeTimeout(Long roomId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) GuessedSaidCmd.PLAYER_INFO)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> gameExamine(roomId)
              ), 5, TimeUnit.SECONDS);
          roomInfo.addTimeOut(GuessedSaidCmd.PLAYER_INFO, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 游戏检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 13:06
   * @update 2020/8/10 15:08
   */
  private void gameExamine(Long roomId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        roomInfo.removeTimeOut(GuessedSaidCmd.PLAYER_INFO);
        if (roomInfo.getRoomRound() < roomInfo.getNowPlayerNum()) {
          roomInfo.initRound();
          roomInfo.setUpActionPlayer(roomInfo.getActionPlayer());
          Result result = this.gameRemote.getGuessedSaidWords();
          List<Map<String, Object>> wordsList = JsonUtils.listMap(result.getData());
          roomInfo.wordsHandler(wordsList);
          F30061.F300612S2C.Builder builder = F30061.F300612S2C.newBuilder();
          builder.setNextUserID(roomInfo.getActionPlayer());
          builder.setTimes(20);
          builder.addAllWordList(roomInfo.getTempWordsList());
          builder.setLastChangeNum(1);
          GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.ROUND_START,
              builder.build().toByteArray()), roomInfo.getRoomId());
          GuessedSaidPlayer player = roomInfo.getPlayerInfo(roomInfo.getActionPlayer());
          // [法官：xxx请开始选词！]
          F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
          message.setMessage(" " + player.getPlayerName() + " 请开始选词！");
          ChatManager.sendPacketToGroup(new Packet(ActionCmd.GAME_CHAT, (short) 2,
              message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
          // 选择定时
          selectTimeout(roomInfo.getRoomId());
        } else {
          // [法官：游戏结束！新的回合即将到来！]
          F10001.F100012S2C.Builder message = F10001.F100012S2C.newBuilder();
          message.setMessage(" 游戏结束，新的回合即将到来！");
          ChatManager.sendPacketToGroup(new Packet(ActionCmd.GAME_CHAT, (short) 2,
              message.setType(0).setUserID(0).build().toByteArray()), roomInfo.getRoomId());
          roomInfo.destroy();
          switch (roomInfo.getNowPlayerNum()) {
            case 4:
              fourPlayersEndGame(roomInfo.getRoomId());
              break;
            case 5:
              fivePlayersEndGame(roomInfo.getRoomId());
              break;
            default:
              sixPlayersEndGame(roomInfo.getRoomId());
              break;
          }
          F30061.F3006112S2C.Builder clearList = F30061.F3006112S2C.newBuilder();
          if (CollectionUtils.isNotEmpty(roomInfo.offlinePlayers())) {
            clearList.addAllUserID(roomInfo.offlinePlayers());
            GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.CLEAR_PLAYER,
                    clearList.build().toByteArray()), roomInfo.getRoomId());
          }
          // 初始游戏
          roomInfo.initGame();
          if (CollectionUtils.isNotEmpty(roomInfo.seatedPlayers())) {
            List<Long> seatedPlayers = roomInfo.seatedPlayers();
            seatedPlayers.forEach(s -> initTimeout(roomId, s));
            F30061.F3006117S2C.Builder timeBuilder = F30061.F3006117S2C.newBuilder();
            GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_GUESS_SAID, GuessedSaidCmd.HINT_TIME,
                timeBuilder.setTimes(18).build().toByteArray()), roomInfo.getRoomId());
            boolean testRoom = (roomId == GuessedSaidAssets.getLong(GuessedSaidAssets.TEST_ID));
            if (!testRoom) {
              if (roomInfo.remainingSeat() > 0) {
                MatchRoom matchRoom = new MatchRoom();
                matchRoom.setRoomId(roomInfo.getRoomId());
                matchRoom.setPeopleNum(roomInfo.remainingSeat());
                MatchManager.refreshGuessedSaidMatch(matchRoom);
              } else {
                MatchManager.delGuessedSaidMatch(roomId);
              }
            }
          } else {
            if (roomInfo.seatedPlayers().size() == 0 && roomInfo.getWatchList().size() == 0) {
              clearData(roomInfo.getRoomId());
            } else {
              List<GuessedSaidPlayer> playerList = roomInfo.getPlayerList();
              int playGameSize = (int) playerList.stream()
                  .filter(s -> s.getPlayerId() > 0).count();
              if (playGameSize == 0) {
                clearTimeout(roomInfo.getRoomId());
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
   * TODO 初始定时. 18(s)
   *
   * @param roomId [房间ID]
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/10 10:28
   * @update 2020/8/10 15:08
   */
  private void initTimeout(Long roomId, Long playerId) {
    try {
      GuessedSaidRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey(playerId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> readyExamine(roomId, playerId)
              ), 18, TimeUnit.SECONDS);
          roomInfo.addTimeOut(playerId.intValue(), timeout);
          GuessedSaidPlayer player = roomInfo.getPlayerInfo(playerId);
          player.setReadyTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 清理定时. 10(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/19 13:31
   * @update 2020/10/19 13:31
   */
  private void clearTimeout(Long roomId) {
    try {
      GuessedSaidRoom saidRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(saidRoom)) {
        if (!saidRoom.getTimeOutMap().containsKey(roomId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> clearExamine(roomId)
              ), 10, TimeUnit.SECONDS);
          saidRoom.addTimeOut(roomId.intValue(), timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 清理检验.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/19 13:42
   * @update 2020/10/19 13:42
   */
  private void clearExamine(Long roomId) {
    try {
      GuessedSaidRoom saidRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(saidRoom)) {
        saidRoom.removeTimeOut(roomId.intValue());
        List<GuessedSaidPlayer> playerList = saidRoom.getPlayerList();
        int playGameSize = (int) playerList.stream()
            .filter(player -> player.getPlayerId() > 0).count();
        LoggerManager.info("<== 30061 [你说我猜.清除检测] DELETE: [{}] PLAY: [{}]", roomId, playGameSize);
        if (playGameSize == 0) {
          List<GuessedSaidPlayer> watchList = saidRoom.getWatchList();
          if (watchList.size() > 0) {
            F20000.F200007S2C.Builder builder = F20000.F200007S2C.newBuilder();
            builder.setMsg("(oﾟvﾟ)ノ 房间已解散！");
            watchList.forEach(saidPlayer -> {
              SoftChannel.sendPacketToUserId(
                  new Packet(ActionCmd.APP_HEART, (short) 7,
                      builder.build().toByteArray()), saidPlayer.getPlayerId());
              this.redisUtils
                  .del(GameKey.KEY_GAME_JOIN_RECORD.getName() + saidPlayer.getPlayerId());
            });
            clearData(roomId);
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }
}
