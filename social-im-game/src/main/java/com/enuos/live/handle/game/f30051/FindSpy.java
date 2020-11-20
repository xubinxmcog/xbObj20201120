package com.enuos.live.handle.game.f30051;

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
import com.enuos.live.proto.f30051msg.F30051;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.util.Timeout;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

/**
 * TODO 谁是卧底.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/5/15 14:47
 */

@Component
@AbstractAction(cmd = ActionCmd.GAME_WHO_IS_SPY)
public class FindSpy extends AbstractActionHandler {

  /**
   * 房间游戏数据.
   */
  private static ConcurrentHashMap<Long, FindSpyRoom> GAME_DATA = new ConcurrentHashMap<>();

  /**
   * 大部分敏感词汇在10个以内，直接返回缓存的字符串.
   */
  private static final String[] STAR_ARR = {"*", "**", "***", "****", "*****", "******", "*******",
      "********", "*********", "**********"};

  /**
   * Feign调用.
   */
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
   * @create 2020/7/1 21:30
   * @update 2020/9/1 8:58
   */
  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      switch (packet.child) {
        case FindSpyCmd.ENTER_ROOM:
          enterRoom(channel, packet);
          break;
        case FindSpyCmd.PLAYER_READY:
          playerReady(channel, packet);
          break;
        case FindSpyCmd.CHANGE_WORDS:
          changeWords(channel, packet);
          break;
        case FindSpyCmd.CHANGE_ACTION:
          changeAction(channel, packet);
          break;
        case FindSpyCmd.SPEAK_WORDS:
          speakWords(channel, packet);
          break;
        case FindSpyCmd.START_SPEAK:
          startSpeak(channel, packet);
          break;
        case FindSpyCmd.PLAYERS_VOTE:
          playersVote(channel, packet);
          break;
        case FindSpyCmd.OPEN_WORDS:
          openWords(channel, packet);
          break;
        case FindSpyCmd.SPEAK_OPEN_WORDS:
          speakOpenWords(channel, packet);
          break;
        case FindSpyCmd.PLAYER_EXIT:
          playerExit(channel, packet);
          break;
        case FindSpyCmd.WATCH_INFO:
          watchInfo(channel, packet);
          break;
        case FindSpyCmd.JOIN_LEAVE:
          joinOrLeave(channel, packet);
          break;
        case FindSpyCmd.PLAYER_SELECT:
          playerSelect(channel, packet);
          break;
        case FindSpyCmd.PLAYER_CHAT:
          playerChat(channel, packet);
          break;
        case FindSpyCmd.FINISH_TALKING:
          finishTalking(channel, packet);
          break;
        default:
          LoggerManager.warn("[GAME 30051 HANDLE] CHILD ERROR: [{}]", packet.child);
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
   * @create 2020/7/1 21:30
   * @update 2020/9/1 9:03
   */
  @Override
  public void shutOff(Long userId, Long attachId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(attachId);
      if (Objects.nonNull(roomInfo)) {
        FindSpyPlayer playerInfo = roomInfo.getPlayerInfo(userId);
        if (Objects.nonNull(playerInfo)) {
          if (roomInfo.getRoomStatus() == 1) {
            if (playerInfo.getIdentity() == 0) {
              playerInfo.setPlayerStatus(4);
              playerInfo.setLinkStatus(1);
              ChatManager.removeChannel(attachId, playerInfo.getChannel());
              GroupManager.removeChannel(attachId, playerInfo.getChannel());
              this.gameRemote.leaveRoom(userId);
              F30051.F3005116S2C.Builder builder = F30051.F3005116S2C.newBuilder();
              builder.setResult(0).setUserID(userId);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_EXIT,
                      builder.build().toByteArray()), roomInfo.getRoomId());
              if (roomInfo.seatedPlayersIsDisconnected() == roomInfo.seatedPlayersNum()
                  && roomInfo.seatedPlayers().size() == 0
                  && roomInfo.getAudienceList().size() == 0) {
                clearData(roomInfo.getRoomId());
              }
            } else {
              roomInfo.leaveGame(userId, 1);
              ChatManager.removeChannel(attachId, playerInfo.getChannel());
              GroupManager.removeChannel(attachId, playerInfo.getChannel());
              this.gameRemote.leaveRoom(userId);
              if (roomInfo.seatedPlayersIsDisconnected() == roomInfo.seatedPlayersNum()
                  && roomInfo.seatedPlayers().size() == 0
                  && roomInfo.getAudienceList().size() == 0) {
                clearData(roomInfo.getRoomId());
              }
            }
          } else {
            if (playerInfo.getIdentity() == 0) {
              if (playerInfo.getPlayerStatus() == 0) {
                roomInfo.cancelTimeOut(userId.intValue());
              } else {
                roomInfo.cancelTimeOut(FindSpyCmd.START_CHECK);
              }
              F30051.F3005116S2C.Builder builder = F30051.F3005116S2C.newBuilder();
              builder.setResult(0).setUserID(userId);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_EXIT,
                      builder.build().toByteArray()), roomInfo.getRoomId());
              // 法官消息
              sendMessage(14, attachId, playerInfo.getPlayerName(), null);
              roomInfo.leaveGame(userId, 0);
              ChatManager.removeChannel(attachId, playerInfo.getChannel());
              GroupManager.removeChannel(attachId, playerInfo.getChannel());
              this.gameRemote.leaveRoom(userId);
              if (roomInfo.seatedPlayers().size() == 0
                  && roomInfo.getAudienceList().size() == 0) {
                clearData(roomInfo.getRoomId());
              } else {
                publicMatch(roomInfo.getRoomId());
                int unReady = roomInfo.unprepared();
                int isReady = roomInfo.preparations();
                if (unReady == 0
                    && roomInfo.getRoomType() == 0
                    && isReady >= FindSpyAssets.getInt(FindSpyAssets.PEOPLE_MIN_1)) {
                  // 法官消息
                  sendMessage(1, roomInfo.getRoomId(), null, null);
                  startTimeout(roomInfo.getRoomId());
                } else if (unReady == 0
                    && roomInfo.getRoomType() == 1
                    && isReady >= FindSpyAssets.getInt(FindSpyAssets.PEOPLE_MIN_2)) {
                  // 法官消息
                  sendMessage(1, roomInfo.getRoomId(), null, null);
                  startTimeout(roomInfo.getRoomId());
                } else {
                  // 清理检测
                  List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
                  int playGameSize = (int) playerList.stream()
                      .filter(s -> s.getPlayerId() > 0).count();
                  if (playGameSize == 0) {
                    clearTimeout(roomInfo.getRoomId());
                  }
                }
              }
            } else {
              roomInfo.leaveGame(userId, 1);
              ChatManager.removeChannel(attachId, playerInfo.getChannel());
              GroupManager.removeChannel(attachId, playerInfo.getChannel());
              this.gameRemote.leaveRoom(userId);
              if (roomInfo.seatedPlayers().size() == 0 && roomInfo.getAudienceList().size() == 0) {
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
   * @create 2020/8/3 18:36
   * @update 2020/9/1 9:07
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
   * @create 2020/9/17 21:07
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
   * @create 2020/7/1 21:30
   * @update 2020/8/24 13:07
   */
  private void enterRoom(Channel channel, Packet packet) {
    try {
      boolean isTestUser = this.redisUtils
          .hasKey(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
      boolean isPlayer = this.redisUtils
          .hasKey(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
      if (isTestUser || isPlayer) {
        // 关闭加载
        closeLoading(packet.userId);
        boolean checkRoom = this.redisUtils
            .hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + packet.roomId);
        boolean checkTest = (packet.roomId == FindSpyAssets.getLong(FindSpyAssets.TEST_ID));
        if (checkRoom || checkTest) {
          FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
          if (Objects.nonNull(roomInfo)) {
            FindSpyPlayer player = roomInfo.getPlayerInfo(packet.userId);
            // 房间状态 [0-未开始 1-已开始]
            if (roomInfo.getRoomStatus() == 1) {
              if (Objects.nonNull(player)) {
                // 玩家身份 [0-玩家 1-观众]        连接状态 [0-连接中 1-已断开]
                if (player.getIdentity() == 0 && player.getLinkStatus() == 1) {
                  playerNotExist(channel);
                  return;
                } else {
                  player.setChannel(channel);
                  refreshData(channel, packet);
                  disconnected(channel, packet);
                  return;
                }
              } else {
                refreshData(channel, packet);
                byte[] bytes;
                if (isTestUser) {
                  bytes = this.redisUtils
                      .getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
                } else {
                  bytes = this.redisUtils
                      .getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
                }
                I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
                roomInfo.enterAudience(channel, playerInfo);
                pullDecorateInfo(packet);
                joinWhoIsSpyRoom(packet);
              }
            } else {
              // 销毁清理定时
              roomInfo.cancelTimeOut((int) packet.roomId);
              if (Objects.nonNull(player)) {
                player.setChannel(channel);
                refreshData(channel, packet);
              } else {
                if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.START_CHECK)) {
                  roomInfo.cancelTimeOut((int) FindSpyCmd.START_CHECK);
                }
                refreshData(channel, packet);
                byte[] bytes;
                if (isTestUser) {
                  bytes = this.redisUtils
                      .getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
                } else {
                  bytes = this.redisUtils
                      .getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
                }
                I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
                roomInfo.enterSeat(channel, playerInfo);
                pullDecorateInfo(packet);
                joinWhoIsSpyRoom(packet);
                publicMatch(packet.roomId);
              }
            }
          } else {
            register(TimerEventLoop.timeGroup.next());
            if (checkTest) {
              GAME_DATA.computeIfAbsent(packet.roomId, key -> new FindSpyRoom(packet.roomId, 0));
              roomInfo = GAME_DATA.get(packet.roomId);
              roomInfo.setOpenWay(1);
              roomInfo.setSpeakMode(0);
            } else {
              byte[] roomByte = this.redisUtils
                  .getByte(GameKey.KEY_GAME_ROOM_RECORD.getName() + packet.roomId);
              I10001.RoomRecord roomRecord = I10001.RoomRecord.parseFrom(roomByte);
              GAME_DATA.computeIfAbsent(packet.roomId,
                  key -> new FindSpyRoom(packet.roomId, roomRecord.getRoomType()));
              roomInfo = GAME_DATA.get(packet.roomId);
              roomInfo.setOpenWay(roomRecord.getOpenWay());
              roomInfo.setSpeakMode(roomRecord.getSpeakMode());
            }
            refreshData(channel, packet);
            byte[] bytes;
            if (isTestUser) {
              bytes = this.redisUtils
                  .getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
            } else {
              bytes = this.redisUtils
                  .getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
            }
            I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
            roomInfo.enterSeat(channel, playerInfo);
            pullDecorateInfo(packet);
            joinWhoIsSpyRoom(packet);
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
   * @create 2020/7/10 21:11
   * @update 2020/7/10 21:11
   */
  private void pushPlayerInfo(Channel channel, Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        F30051.F30051S2C.Builder builder = F30051.F30051S2C.newBuilder();
        FindSpyPlayer player = roomInfo.getPlayerInfo(packet.userId);
        if (player.getIdentity() == 1) {
          watchRoomInfo(channel, packet);
        } else {
          List<FindSpyPlayer> partakerList = roomInfo.getPlayerList();
          if (CollectionUtils.isNotEmpty(partakerList)) {
            F30051.PlayerInfo.Builder playerInfo;
            for (FindSpyPlayer findSpyPlayer : partakerList) {
              playerInfo = F30051.PlayerInfo.newBuilder();
              playerInfo.setNick(findSpyPlayer.getPlayerName());
              playerInfo.setUserID(findSpyPlayer.getPlayerId());
              playerInfo.setUrl(findSpyPlayer.getPlayerAvatar());
              playerInfo.setSex(findSpyPlayer.getPlayerSex());
              playerInfo.setDeviPosition(findSpyPlayer.getSeatNumber());
              playerInfo.setState(findSpyPlayer.getPlayerStatus());
              if (StringUtils.isNotEmpty(findSpyPlayer.getAvatarFrame())) {
                playerInfo.setIconFrame(findSpyPlayer.getAvatarFrame());
              }
              if (findSpyPlayer.getPlayerStatus() == 0) {
                if (findSpyPlayer.getReadyTime() != null) {
                  LocalDateTime udt = findSpyPlayer.getReadyTime().plusSeconds(15L);
                  LocalDateTime nds = LocalDateTime.now();
                  Duration duration = Duration.between(nds, udt);
                  int second = Math.toIntExact(duration.getSeconds());
                  playerInfo.setReadyTime(second);
                } else {
                  playerInfo.setReadyTime(15);
                }
              }
              builder.addSeatPlayer(playerInfo);
            }
          }
          builder.setResult(0);
          builder.setNumberMode(roomInfo.getRoomType());
          builder.setSpeakMode(roomInfo.getSpeakMode());
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.ENTER_ROOM,
                  builder.build().toByteArray()), roomInfo.getRoomId());
          if (player.getPlayerStatus() == 0) {
            readyTimeout(roomInfo.getRoomId(), packet.userId);
          }
          // 法官消息
          sendMessage(0, roomInfo.getRoomId(), player.getPlayerName(), null);
          readyInfo(roomInfo.getRoomId());
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
   * @create 2020/8/17 20:51
   * @update 2020/8/17 20:51
   */
  private void watchRoomInfo(Channel channel, Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        F30051.F30051S2C.Builder builder = F30051.F30051S2C.newBuilder();
        List<FindSpyPlayer> partakerList = roomInfo.getPlayerList();
        if (CollectionUtils.isNotEmpty(partakerList)) {
          F30051.PlayerInfo.Builder playerInfo;
          for (FindSpyPlayer partaker : partakerList) {
            playerInfo = F30051.PlayerInfo.newBuilder();
            playerInfo.setNick(partaker.getPlayerName());
            playerInfo.setUserID(partaker.getPlayerId());
            playerInfo.setUrl(partaker.getPlayerAvatar());
            playerInfo.setSex(partaker.getPlayerSex());
            playerInfo.setDeviPosition(partaker.getSeatNumber());
            playerInfo.setState(partaker.getPlayerStatus());
            if (StringUtils.isNotEmpty(partaker.getAvatarFrame())) {
              playerInfo.setIconFrame(partaker.getAvatarFrame());
            }
            builder.addSeatPlayer(playerInfo);
          }
        }
        builder.setResult(0);
        builder.setNumberMode(roomInfo.getRoomType());
        builder.setSpeakMode(roomInfo.getSpeakMode());
        channel.writeAndFlush(
            new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.ENTER_ROOM,
                builder.setResult(0).build().toByteArray()));
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
   * @create 2020/7/1 21:30
   * @update 2020/8/25 9:04
   */
  private void playerReady(Channel channel, Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindSpyPlayer checkPlayer = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30051.F300511C2S request = F30051.F300511C2S.parseFrom(packet.bytes);
          F30051.F300511S2C.Builder builder = F30051.F300511S2C.newBuilder();
          if (roomInfo.getRoomStatus() == 0) {
            FindSpyPlayer player = roomInfo.getPlayerInfo(packet.userId);
            // 玩家身份 [0-玩家 1-观众]
            if (player.getIdentity() == 0) {
              // 准备状态 0-准备 1-取消
              if (request.getIsReady() == 0) {
                roomInfo.cancelTimeOut((int) packet.userId);
                builder.setResult(0).setIsReady(request.getIsReady()).setUserID(packet.userId);
                player.setPlayerStatus(1);
                builder.setDeviPosition(player.getSeatNumber());
                int unReady = roomInfo.unprepared();
                int isReady = roomInfo.preparations();
                if (unReady > 0) {
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_READY,
                          builder.build().toByteArray()), roomInfo.getRoomId());
                } else if (roomInfo.getRoomType() == 0
                    && isReady >= FindSpyAssets.getInt(FindSpyAssets.PEOPLE_MIN_1)) {
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_READY,
                          builder.setGameTime(3).build().toByteArray()), roomInfo.getRoomId());
                  // 法官消息
                  sendMessage(1, roomInfo.getRoomId(), null, null);
                  startTimeout(roomInfo.getRoomId());
                } else if (roomInfo.getRoomType() == 1
                    && isReady >= FindSpyAssets.getInt(FindSpyAssets.PEOPLE_MIN_2)) {
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_READY,
                          builder.setGameTime(3).build().toByteArray()), roomInfo.getRoomId());
                  // 法官消息
                  sendMessage(1, roomInfo.getRoomId(), null, null);
                  startTimeout(roomInfo.getRoomId());
                } else {
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_READY,
                          builder.build().toByteArray()), roomInfo.getRoomId());
                }
              } else {
                roomInfo.cancelTimeOut(FindSpyCmd.START_CHECK);
                player.setPlayerStatus(0);
                builder.setDeviPosition(player.getSeatNumber());
                builder.setResult(0).setIsReady(request.getIsReady()).setUserID(packet.userId)
                    .setReadyTime(15);
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_READY,
                        builder.build().toByteArray()), roomInfo.getRoomId());
                readyTimeout(roomInfo.getRoomId(), player.getPlayerId());
              }
            } else {
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_READY,
                      builder.setResult(1).setIsReady(request.getIsReady()).build().toByteArray()));
            }
          } else {
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_READY,
                    builder.setResult(1).setIsReady(request.getIsReady()).build().toByteArray()));
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
   * TODO 换词申请.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/8/25 12:51
   */
  private void changeWords(Channel channel, Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindSpyPlayer checkPlayer = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          // 换词标记 0-未开始 1-已开始 2-已结束
          if (roomInfo.getChangeIndex() == 0) {
            roomInfo.setChangeIndex(1);
            roomInfo.cancelTimeOut(FindSpyCmd.PUSH_WORDS);
            roomInfo.setChangeUser(packet.userId);
            FindSpyPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
            playerInfo.setChangeAction(0);
            // 法官消息
            sendMessage(2, packet.roomId, null, playerInfo.getSeatNumber());
            F30051.F300514S2C.Builder builder = F30051.F300514S2C.newBuilder();
            builder.setApplyNo(playerInfo.getSeatNumber());
            List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
            playerList.sort(Comparator.comparing(FindSpyPlayer::getSeatNumber));
            F30051.ChangeInfo.Builder changeInfo;
            for (FindSpyPlayer player : playerList) {
              if (player.getPlayerStatus() > 0) {
                changeInfo = F30051.ChangeInfo.newBuilder();
                changeInfo.setPlayerNo(player.getSeatNumber());
                changeInfo.setPlayerIcon(player.getPlayerAvatar());
                changeInfo.setIsChangeWord(player.getChangeAction());
                builder.addChangeInfo(changeInfo);
              }
            }
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.CHANGE_WORDS,
                    builder.build().toByteArray()), roomInfo.getRoomId());
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
   * TODO 换词操作.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/8/25 12:51
   */
  private void changeAction(Channel channel, Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindSpyPlayer checkPlayer = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30051.F300515C2S request = F30051.F300515C2S.parseFrom(packet.bytes);
          F30051.F300515S2C.Builder builder = F30051.F300515S2C.newBuilder();
          FindSpyPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
          playerInfo.setChangeAction(request.getIsChangeWord());
          int agreeNum = roomInfo.agreeChangeWordsNum();
          int disagreeNum = roomInfo.disagreeChangeWordsNum();
          int playNum = roomInfo.seatedPlayers().size();
          if (agreeNum + disagreeNum == playNum) {
            // 换词标记 [0-未开始 1-已开始 2-已结束]
            roomInfo.setChangeIndex(2);
            // 销毁定时
            roomInfo.cancelTimeOut(FindSpyCmd.ROUND_START);
            if (roomInfo.getRoomType() == 0) {
              if (playNum == FindSpyAssets.getInt(FindSpyAssets.PEOPLE_MIN_1)) {
                if (agreeNum > 2) {
                  wordsSuccess(roomInfo.getRoomId(), packet.userId, request.getIsChangeWord());
                } else {
                  wordsFailed(roomInfo.getRoomId(), packet.userId, request.getIsChangeWord());
                }
              } else {
                if (agreeNum > 3) {
                  wordsSuccess(roomInfo.getRoomId(), packet.userId, request.getIsChangeWord());
                } else {
                  wordsFailed(roomInfo.getRoomId(), packet.userId, request.getIsChangeWord());
                }
              }
            } else {
              if (playNum == FindSpyAssets.getInt(FindSpyAssets.PEOPLE_MIN_2)) {
                if (agreeNum >= 4) {
                  wordsSuccess(roomInfo.getRoomId(), packet.userId, request.getIsChangeWord());
                } else {
                  wordsFailed(roomInfo.getRoomId(), packet.userId, request.getIsChangeWord());
                }
              } else {
                if (agreeNum > 4) {
                  wordsSuccess(roomInfo.getRoomId(), packet.userId, request.getIsChangeWord());
                } else {
                  wordsFailed(roomInfo.getRoomId(), packet.userId, request.getIsChangeWord());
                }
              }
            }
            return;
          }
          builder.setPlayerNo(playerInfo.getSeatNumber());
          builder.setIsChangeWord(request.getIsChangeWord());
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.CHANGE_ACTION,
                  builder.build().toByteArray()), roomInfo.getRoomId());
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
   * TODO 描述信息.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/8/25 12:52
   */
  private void speakWords(Channel channel, Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindSpyPlayer checkPlayer = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          FindSpyPlayer player = roomInfo.getPlayerInfo(packet.userId);
          if (player.getSpeakWords() == null) {
            F30051.F300517C2S request = F30051.F300517C2S.parseFrom(packet.bytes);
            String words = request.getWord();
            if (player.getGameIdentity() == 1) {
              if (roomInfo.getRoomLexicon().getLexiconMass().equals(words)) {
                words = getStarChar(words.length());
              }
            } else if (player.getGameIdentity() == 2) {
              if (roomInfo.getRoomLexicon().getLexiconSpy().equals(words)) {
                words = getStarChar(words.length());
              }
            }
            player.setSpeakWords(words);
            F30051.F300517S2C.Builder builder = F30051.F300517S2C.newBuilder();
            builder.setWord(words);
            builder.setUserID(packet.userId);
            builder.setSpeakPlayer(player.getSeatNumber());
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.SPEAK_WORDS,
                    builder.build().toByteArray()), roomInfo.getRoomId());
            if (roomInfo.getRoomRound() == 1) {
              if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.ROUND_START)) {
                List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
                playerList = playerList.stream()
                    .filter(findSpyPlayer -> findSpyPlayer.getPlayerId() > 0
                        // 玩家标记 [0-游戏中 1-已出局]
                        && findSpyPlayer.getPlayerIndex() == 0
                        // 玩家身份 [0-玩家 1-观众]
                        && findSpyPlayer.getIdentity() == 0
                        // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
                        && (findSpyPlayer.getPlayerStatus() == 2
                        || findSpyPlayer.getPlayerStatus() == 4)
                        // 连接状态 [0-连接中 1-已断开]
                        && (findSpyPlayer.getLinkStatus() == 0
                        || findSpyPlayer.getLinkStatus() == 1)
                    ).collect(Collectors.toList());
                int speakSize = (int) playerList.stream()
                    .filter(findSpyPlayer -> Objects.nonNull(findSpyPlayer.getSpeakWords()))
                    .count();
                if (speakSize == playerList.size()) {
                  roomInfo.cancelTimeOut(FindSpyCmd.ROUND_START);
                  beginExamine(roomInfo.getRoomId());
                }
              } else if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.START_WORDS)) {
                List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
                playerList = playerList.stream()
                    .filter(findSpyPlayer -> findSpyPlayer.getPlayerId() > 0
                        // 玩家标记 [0-游戏中 1-已出局]
                        && findSpyPlayer.getPlayerIndex() == 0
                        // 玩家身份 [0-玩家 1-观众]
                        && findSpyPlayer.getIdentity() == 0
                        // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
                        && (findSpyPlayer.getPlayerStatus() == 2
                        || findSpyPlayer.getPlayerStatus() == 4)
                        // 连接状态 [0-连接中 1-已断开]
                        && (findSpyPlayer.getLinkStatus() == 0
                        || findSpyPlayer.getLinkStatus() == 1)
                    ).collect(Collectors.toList());
                int speakSize = (int) playerList.stream()
                    .filter(findSpyPlayer -> Objects.nonNull(findSpyPlayer.getSpeakWords()))
                    .count();
                if (speakSize == playerList.size()) {
                  roomInfo.cancelTimeOut(FindSpyCmd.START_WORDS);
                  specialExamine(roomInfo.getRoomId());
                }
              } else if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.DRAW_CHECK)) {
                List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
                List<Long> speakList = roomInfo.getTempList();
                int speakSize = (int) playerList.stream()
                    .filter(findSpyPlayer -> speakList.contains(findSpyPlayer.getPlayerId())
                        && Objects.nonNull(findSpyPlayer.getSpeakWords())).count();
                if (speakSize == speakList.size()) {
                  roomInfo.cancelTimeOut(FindSpyCmd.DRAW_CHECK);
                  wordsByBattleExamine(roomInfo.getRoomId(), speakList);
                }
              }
            } else {
              if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.ROUND_START)) {
                List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
                playerList = playerList.stream()
                    .filter(findSpyPlayer -> findSpyPlayer.getPlayerId() > 0
                        // 玩家标记 [0-游戏中 1-已出局]
                        && findSpyPlayer.getPlayerIndex() == 0
                        // 玩家身份 [0-玩家 1-观众]
                        && findSpyPlayer.getIdentity() == 0
                        // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
                        && (findSpyPlayer.getPlayerStatus() == 2
                        || findSpyPlayer.getPlayerStatus() == 4)
                        // 连接状态 [0-连接中 1-已断开]
                        && (findSpyPlayer.getLinkStatus() == 0
                        || findSpyPlayer.getLinkStatus() == 1)
                    ).collect(Collectors.toList());
                int speakSize = (int) playerList.stream()
                    .filter(findSpyPlayer -> Objects.nonNull(findSpyPlayer.getSpeakWords()))
                    .count();
                if (speakSize == playerList.size()) {
                  roomInfo.cancelTimeOut(FindSpyCmd.ROUND_START);
                  beginExamine(roomInfo.getRoomId());
                }
              } else if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.DRAW_CHECK)) {
                List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
                List<Long> speakList = roomInfo.getTempList();
                int speakSize = (int) playerList.stream()
                    .filter(findSpyPlayer -> speakList.contains(findSpyPlayer.getPlayerId())
                        && Objects.nonNull(findSpyPlayer.getSpeakWords())).count();
                if (speakSize == speakList.size()) {
                  roomInfo.cancelTimeOut(FindSpyCmd.DRAW_CHECK);
                  wordsByBattleExamine(roomInfo.getRoomId(), speakList);
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
   * TODO 玩家说话.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/3 16:27
   * @update 2020/11/3 16:27
   */
  private void startSpeak(Channel channel, Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {

      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 玩家投票.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/8/25 12:53
   */
  private void playersVote(Channel channel, Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindSpyPlayer checkPlayer = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          roomInfo.setHandleIndex(0);
          F30051.F3005110C2S request = F30051.F3005110C2S.parseFrom(packet.bytes);
          FindSpyPlayer targetPlayerInfo = roomInfo.getPlayerInfo(request.getUserID());
          F10001.F100012S2C.Builder chatBuilder = F10001.F100012S2C.newBuilder();
          chatBuilder.setType(0).setUserID(0)
              .setMessage(" 你投给了【" + targetPlayerInfo.getSeatNumber() + "】号！");
          channel.writeAndFlush(
              new Packet(ActionCmd.GAME_CHAT, FindSpyCmd.SYSTEM_INFO,
                  chatBuilder.build().toByteArray()));
          FindSpyPlayer nowPlayerInfo = roomInfo.getPlayerInfo(packet.userId);
          // 投票
          checkPlayer.setPlayerVoteIndex(1);
          roomInfo.playerVote(request.getUserID(), packet.userId, nowPlayerInfo.getPlayerAvatar());
          // 投票玩家
          F30051.F3005110S2C.Builder builder = F30051.F3005110S2C.newBuilder();
          List<FindSpyPlayer> playerList = roomInfo.getPlayerList().stream()
              .filter(player -> player.getPlayerId() > 0
                  // 玩家投票
                  && player.getPlayerVoteIndex() == 1
                  // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
                  && player.getPlayerStatus() == 2)
              .collect(Collectors.toList());
          // 投票信息
          List<FindSpyVote> voteList = roomInfo.getVoteList();
          F30051.VoteInfo.Builder voteInfo;
          for (FindSpyVote findSpyVote : voteList) {
            voteInfo = F30051.VoteInfo.newBuilder();
            voteInfo.setUserID(findSpyVote.getUserId());
            voteInfo.addAllUserIcon(findSpyVote.getVoteList());
            builder.addVoteInfo(voteInfo);
          }
          // 投票数据
          byte[] voteByte = builder.build().toByteArray();
          playerList.forEach(player -> player.sendPacket(
              new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYERS_VOTE, voteByte)));
          List<FindSpyVote> findSpyVotes = roomInfo.getVoteList();
          int findSpyVoteSize = findSpyVotes.size();
          int voteNum = findSpyVotes.stream()
              .filter(findSpyVote -> findSpyVote.getVoteList().size() > 0)
              .mapToInt(findSpyVote -> findSpyVote.getVoteList().size()).sum();
          if (findSpyVoteSize == voteNum) {
            roomInfo.cancelTimeOut((int) FindSpyCmd.START_VOTE);
            voteExamine(roomInfo.getRoomId());
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
   * TODO 是否爆词.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/10/12 18:13
   */
  private void openWords(Channel channel, Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindSpyPlayer checkPlayer = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.SELECT_CHECK)) {
            roomInfo.cancelTimeOut(FindSpyCmd.SELECT_CHECK);
            F30051.F3005113C2S request = F30051.F3005113C2S.parseFrom(packet.bytes);
            FindSpyPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
            // 是否爆词 0-是 1-否
            if (request.getIsBlastWord() == 0) {
              roomInfo.setOpenWordsUser(packet.userId);
              // 法官消息
              sendMessage(16, packet.roomId, null, playerInfo.getSeatNumber());
              F30051.F3005113S2C.Builder builder = F30051.F3005113S2C.newBuilder();
              builder.setUserID(packet.userId);
              builder.setBlastTime(20);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.OPEN_WORDS,
                      builder.build().toByteArray()), roomInfo.getRoomId());
              // 爆词定时
              openWordsTimeOut(roomInfo.getRoomId(), packet.userId);
            } else {
              // 法官消息
              sendMessage(15, packet.roomId, null, playerInfo.getSeatNumber());
              if (roomInfo.getSpyPlayerList().size() > 0) {
                // 下一回合
                roundStart(roomInfo.getRoomId());
              } else {
                // 平民胜利
                roomInfo.destroy();
                if (roomInfo.getRoomRound() == 1) {
                  List<FindSpyPlayer> findSpyPlayers = roomInfo.getPlayerList().stream()
                      .filter(s -> s.getPlayerId() > 0 && s.getGameIdentity() == 1)
                      .collect(Collectors.toList());
                  if (CollectionUtils.isNotEmpty(findSpyPlayers)) {
                    findSpyPlayers.stream().filter(player -> player.getVoteIndexSpy() == 1)
                        .forEach(player -> {
                          // 玩家成就.利刃出鞘
                          Map<String, Object> taskSuc0027 = Maps.newHashMap();
                          taskSuc0027.put("userId", player.getPlayerId());
                          taskSuc0027.put("code", AchievementEnum.AMT0027.getCode());
                          taskSuc0027.put("progress", 1);
                          taskSuc0027.put("isReset", 0);
                          this.userRemote.achievementHandlers(taskSuc0027);
                        });
                  }
                }
                List<FindSpyPlayer> findSpyPlayers = roomInfo.getPlayerList().stream()
                    .filter(s -> s.getPlayerId() > 0).collect(Collectors.toList());
                findSpyPlayers.forEach(player -> {
                  // 每日任务.玩1局谁是卧底
                  Map<String, Object> taskInfo = Maps.newHashMap();
                  taskInfo.put("userId", player.getPlayerId());
                  taskInfo.put("code", TaskEnum.PGT0007.getCode());
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
                });
                if (roomInfo.getRoomRound() >= 3) {
                  findSpyPlayers.stream().filter(player -> player.getPlayerLiveOn() >= 3)
                      .forEach(player -> {
                        // 每日任务.在谁是卧底中.存活3轮及以上
                        Map<String, Object> taskInfo = Maps.newHashMap();
                        taskInfo.put("userId", player.getPlayerId());
                        taskInfo.put("code", TaskEnum.PGT0023.getCode());
                        taskInfo.put("progress", 1);
                        taskInfo.put("isReset", 0);
                        this.userRemote.taskHandler(taskInfo);
                      });
                }
                // 法官消息
                sendMessage(200, packet.roomId, null, null);
                // 谁是卧底.推送结算
                pushSettlement(roomInfo.getRoomId());
                F30051.F3005120S2C.Builder clearList = F30051.F3005120S2C.newBuilder();
                if (CollectionUtils.isNotEmpty(roomInfo.offlinePlayers())) {
                  clearList.addAllUserID(roomInfo.offlinePlayers());
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.JOIN_WATCH,
                          clearList.build().toByteArray()), roomInfo.getRoomId());
                }
                roomInfo.initRoomInfo();
                // 谁是卧底.初始游戏
                if (CollectionUtils.isNotEmpty(roomInfo.seatedPlayers())) {
                  List<Long> seatedPlayers = roomInfo.seatedPlayers();
                  seatedPlayers.forEach(playerId -> initTimeout(packet.roomId, playerId));
                  // 法官消息
                  sendMessage(23, packet.roomId, null, null);
                  boolean testRoom = (packet.roomId == FindSpyAssets
                      .getLong(FindSpyAssets.TEST_ID));
                  if (!testRoom) {
                    if (roomInfo.getOpenWay() == 0) {
                      if (roomInfo.remainingSeat() > 0) {
                        refreshSpyMath(packet.roomId);
                      }
                    }
                  }
                } else {
                  if (roomInfo.seatedPlayers().size() == 0
                      && roomInfo.getAudienceList().size() == 0) {
                    clearData(roomInfo.getRoomId());
                  } else {
                    // 清理检测
                    List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
                    int playGameSize = (int) playerList.stream()
                        .filter(s -> s.getPlayerId() > 0).count();
                    if (playGameSize == 0) {
                      clearTimeout(roomInfo.getRoomId());
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
   * TODO 描述爆词.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/10/12 18:13
   */
  private void speakOpenWords(Channel channel, Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindSpyPlayer checkPlayer = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          roomInfo.cancelTimeOut(FindSpyCmd.OPEN_WORDS);
          F30051.F3005114C2S request = F30051.F3005114C2S.parseFrom(packet.bytes);
          FindSpyPlayer player = roomInfo.getPlayerInfo(packet.userId);
          player.setOpenWords(request.getBlastWord());
          // 法官消息
          sendMessage(17, packet.roomId, null, player.getSeatNumber());
          F30051.F3005114S2C.Builder builder = F30051.F3005114S2C.newBuilder();
          if (roomInfo.getRoomLexicon().getLexiconMass().equals(request.getBlastWord())) {
            // 爆词成功
            builder.setResult(0).setUserID(packet.userId);
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.SPEAK_OPEN_WORDS,
                    builder.build().toByteArray()), roomInfo.getRoomId());
            // 法官消息
            sendMessage(18, packet.roomId, null, player.getSeatNumber());
            // 卧底胜利
            speakOpenWordsSuccess(packet.roomId, packet.userId);
          } else {
            // 爆词失败
            builder.setResult(1).setUserID(packet.userId);
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.SPEAK_OPEN_WORDS,
                    builder.build().toByteArray()), roomInfo.getRoomId());
            // 法官消息
            sendMessage(19, packet.roomId, null, player.getSeatNumber());
            if (player.getPlayerStatus() == 4) {
              F30051.F3005116S2C.Builder logout = F30051.F3005116S2C.newBuilder();
              logout.setResult(0).setUserID(packet.userId);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_EXIT,
                      logout.build().toByteArray()), roomInfo.getRoomId());
            } else {
              // 玩家出局
              player.setPlayerStatus(3);
              player.setPlayerIndex(1);
            }
            speakOpenWordsFailed(packet.roomId);
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
   * TODO 爆词成功.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 15:53
   * @update 2020/10/12 18:13
   */
  private void speakOpenWordsSuccess(Long roomId, Long playerId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        roomInfo.destroy();
        List<FindSpyPlayer> findSpyPlayers = roomInfo.getPlayerList().stream()
            .filter(player -> player.getPlayerId() > 0)
            .collect(Collectors.toList());
        findSpyPlayers.forEach(player -> {
          // 每日任务.玩1局谁是卧底
          Map<String, Object> taskInfo = Maps.newHashMap();
          taskInfo.put("userId", player.getPlayerId());
          taskInfo.put("code", TaskEnum.PGT0007.getCode());
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
        });
        if (roomInfo.getRoomRound() >= 3) {
          findSpyPlayers.stream().filter(player -> player.getPlayerLiveOn() >= 3)
              .forEach(player -> {
                // 每日任务.在谁是卧底中.存活3轮及以上
                Map<String, Object> taskInfo = Maps.newHashMap();
                taskInfo.put("userId", player.getPlayerId());
                taskInfo.put("code", TaskEnum.PGT0023.getCode());
                taskInfo.put("progress", 1);
                taskInfo.put("isReset", 0);
                this.userRemote.taskHandler(taskInfo);
              });
        }
        List<FindSpyPlayer> spyPartakerList = findSpyPlayers.stream()
            .filter(s -> s.getGameIdentity() == 2).collect(Collectors.toList());
        // 房间类型 0「4-6人»1卧底」 1「7-8人»2卧底」.
        if (roomInfo.getRoomType() == 0) {
          FindSpyPlayer findSpyPlayer = spyPartakerList.get(0);
          // 玩家成就.无间风云
          Map<String, Object> taskSuc0025 = Maps.newHashMap();
          taskSuc0025.put("userId", findSpyPlayer.getPlayerId());
          taskSuc0025.put("code", AchievementEnum.AMT0025.getCode());
          taskSuc0025.put("progress", 1);
          taskSuc0025.put("isReset", 0);
          this.userRemote.achievementHandlers(taskSuc0025);
        } else {
          spyPartakerList.forEach(player -> {
            // 玩家成就.无间风云
            Map<String, Object> taskSuc0025 = Maps.newHashMap();
            taskSuc0025.put("userId", player.getPlayerId());
            taskSuc0025.put("code", AchievementEnum.AMT0025.getCode());
            taskSuc0025.put("progress", 1);
            taskSuc0025.put("isReset", 0);
            this.userRemote.achievementHandlers(taskSuc0025);
          });
        }
        // 玩家成就.决战黎明
        Map<String, Object> taskSuc0028 = Maps.newHashMap();
        taskSuc0028.put("userId", playerId);
        taskSuc0028.put("code", AchievementEnum.AMT0028.getCode());
        taskSuc0028.put("progress", 1);
        taskSuc0028.put("isReset", 0);
        this.userRemote.achievementHandlers(taskSuc0028);
        F30051.F3005115S2C.Builder builderWin = F30051.F3005115S2C.newBuilder();
        F30051.SettlementPlayerInfo.Builder settlement;
        for (FindSpyPlayer findSpyPlayer : findSpyPlayers) {
          settlement = F30051.SettlementPlayerInfo.newBuilder();
          settlement.setNick(findSpyPlayer.getPlayerName());
          settlement.setUrl(findSpyPlayer.getPlayerAvatar());
          settlement.setSex(findSpyPlayer.getPlayerSex());
          if (findSpyPlayer.getGameIdentity() == 2) {
            if (MemManager.isExists(findSpyPlayer.getPlayerId())) {
              settlement.setScore(gainExperience(findSpyPlayer.getPlayerId(), 8));
              settlement.setIsDouble(1);
            } else {
              settlement.setScore(gainExperience(findSpyPlayer.getPlayerId(), 4));
            }
            settlement.setIsUndercover(0);
          } else {
            if (MemManager.isExists(findSpyPlayer.getPlayerId())) {
              settlement.setIsDouble(1);
            }
            settlement.setScore(0);
            settlement.setIsUndercover(1);
          }
          // 爆词描述
          if (StringUtils.isEmpty(findSpyPlayer.getOpenWords())) {
            settlement.setIsBlast(1);
          } else {
            settlement.setIsBlast(0);
          }
          builderWin.addPlayerInfo(settlement);
        }
        builderWin.setIsUncoverWin(0);
        builderWin.setCivilianWord(roomInfo.getRoomLexicon().getLexiconMass());
        builderWin.setUniCoverWord(roomInfo.getRoomLexicon().getLexiconSpy());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.GAME_SETTLE,
                builderWin.build().toByteArray()), roomInfo.getRoomId());
        // 法官消息
        sendMessage(200, roomId, null, null);
        // 谁是卧底.推送结算
        F30051.F3005120S2C.Builder clearList = F30051.F3005120S2C.newBuilder();
        if (CollectionUtils.isNotEmpty(roomInfo.offlinePlayers())) {
          clearList.addAllUserID(roomInfo.offlinePlayers());
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.JOIN_WATCH,
                  clearList.build().toByteArray()), roomInfo.getRoomId());
        }
        // 谁是卧底.初始游戏
        roomInfo.initRoomInfo();
        if (CollectionUtils.isNotEmpty(roomInfo.seatedPlayers())) {
          List<Long> seatedPlayers = roomInfo.seatedPlayers();
          seatedPlayers.forEach(s -> initTimeout(roomId, s));
          // 法官消息
          sendMessage(23, roomId, null, null);
          boolean testRoom = (roomId == FindSpyAssets.getLong(FindSpyAssets.TEST_ID));
          if (!testRoom) {
            if (roomInfo.getOpenWay() == 0) {
              if (roomInfo.remainingSeat() > 0) {
                refreshSpyMath(roomId);
              }
            }
          }
        } else {
          if (roomInfo.seatedPlayers().size() == 0 && roomInfo.getAudienceList().size() == 0) {
            clearData(roomInfo.getRoomId());
          } else {
            // 清理检测
            List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
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
   * TODO 爆词失败.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 15:54
   * @update 2020/10/12 18:13
   */
  private void speakOpenWordsFailed(Long roomId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (roomInfo.getSpyPlayerList().size() > 0) {
          // 两个间谍 继续下一个回合
          roundStart(roomInfo.getRoomId());
        } else {
          roomInfo.destroy();
          // 谁是卧底.平民胜利
          if (roomInfo.getRoomRound() == 1) {
            List<FindSpyPlayer> findSpyPlayers = roomInfo.getPlayerList().stream()
                .filter(s -> s.getPlayerId() > 0 && s.getGameIdentity() == 1)
                .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(findSpyPlayers)) {
              findSpyPlayers.stream()
                  // 投出卧底
                  .filter(player -> player.getVoteIndexSpy() == 1)
                  .forEach(player -> {
                    // 玩家成就.利刃出鞘
                    Map<String, Object> taskSuc0027 = Maps.newHashMap();
                    taskSuc0027.put("userId", player.getPlayerId());
                    taskSuc0027.put("code", AchievementEnum.AMT0027.getCode());
                    taskSuc0027.put("progress", 1);
                    taskSuc0027.put("isReset", 0);
                    this.userRemote.achievementHandlers(taskSuc0027);
                  });
            }
          }
          List<FindSpyPlayer> findSpyPlayers = roomInfo.getPlayerList().stream()
              .filter(s -> s.getPlayerId() > 0).collect(Collectors.toList());
          findSpyPlayers.forEach(player -> {
            // 每日任务.玩1局谁是卧底
            Map<String, Object> taskInfo = Maps.newHashMap();
            taskInfo.put("userId", player.getPlayerId());
            taskInfo.put("code", TaskEnum.PGT0007.getCode());
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
          });
          if (roomInfo.getRoomRound() >= 3) {
            findSpyPlayers.stream()
                // 存活次数
                .filter(player -> player.getPlayerLiveOn() >= 3)
                .forEach(player -> {
                  // 每日任务.在谁是卧底中.存活3轮及以上
                  Map<String, Object> taskInfo = Maps.newHashMap();
                  taskInfo.put("userId", player.getPlayerId());
                  taskInfo.put("code", TaskEnum.PGT0023.getCode());
                  taskInfo.put("progress", 1);
                  taskInfo.put("isReset", 0);
                  this.userRemote.taskHandler(taskInfo);
                });
          }
          // 法官消息
          sendMessage(200, roomId, null, null);
          // 谁是卧底.推送结算
          pushSettlement(roomInfo.getRoomId());
          F30051.F3005120S2C.Builder clearList = F30051.F3005120S2C.newBuilder();
          if (CollectionUtils.isNotEmpty(roomInfo.offlinePlayers())) {
            clearList.addAllUserID(roomInfo.offlinePlayers());
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.JOIN_WATCH,
                    clearList.build().toByteArray()), roomInfo.getRoomId());
          }
          // 谁是卧底.初始游戏
          roomInfo.initRoomInfo();
          if (CollectionUtils.isNotEmpty(roomInfo.seatedPlayers())) {
            List<Long> seatedPlayers = roomInfo.seatedPlayers();
            seatedPlayers.forEach(playerId -> initTimeout(roomId, playerId));
            // 法官消息
            sendMessage(23, roomId, null, null);
            boolean testRoom = (roomId == FindSpyAssets.getLong(FindSpyAssets.TEST_ID));
            if (!testRoom) {
              if (roomInfo.getOpenWay() == 0) {
                if (roomInfo.remainingSeat() > 0) {
                  refreshSpyMath(roomId);
                }
              }
            }
          } else {
            if (roomInfo.seatedPlayers().size() == 0 && roomInfo.getAudienceList().size() == 0) {
              clearData(roomInfo.getRoomId());
            } else {
              // 清理检测
              List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
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
   * TODO 玩家退出.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/23 20:57
   */
  private void playerExit(Channel channel, Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindSpyPlayer player = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(player)) {
          if (roomInfo.getRoomStatus() == 1) {
            if (player.getIdentity() == 0) {
              player.setPlayerStatus(4);
              player.setLinkStatus(1);
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              F30051.F3005116S2C.Builder builder = F30051.F3005116S2C.newBuilder();
              builder.setResult(0).setUserID(packet.userId);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_EXIT,
                      builder.build().toByteArray()), roomInfo.getRoomId());
              this.gameRemote.leaveRoom(packet.userId);
              if (roomInfo.seatedPlayersIsDisconnected() == roomInfo.seatedPlayersNum()
                  && roomInfo.seatedPlayers().size() == 0
                  && roomInfo.getAudienceList().size() == 0) {
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
                  && roomInfo.seatedPlayers().size() == 0
                  && roomInfo.getAudienceList().size() == 0) {
                clearData(roomInfo.getRoomId());
              }
            }
          } else {
            if (player.getIdentity() == 0) {
              if (player.getPlayerStatus() == 0) {
                roomInfo.cancelTimeOut((int) packet.userId);
              } else {
                roomInfo.cancelTimeOut(FindSpyCmd.START_CHECK);
              }
              F30051.F3005116S2C.Builder builder = F30051.F3005116S2C.newBuilder();
              builder.setResult(0).setUserID(packet.userId);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_EXIT,
                      builder.build().toByteArray()), roomInfo.getRoomId());
              // 法官消息
              sendMessage(14, packet.roomId, player.getPlayerName(), null);
              roomInfo.leaveGame(packet.userId, 0);
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              this.gameRemote.leaveRoom(packet.userId);
              if (roomInfo.seatedPlayers().size() == 0 && roomInfo.getAudienceList().size() == 0) {
                clearData(roomInfo.getRoomId());
              } else {
                publicMatch(roomInfo.getRoomId());
                int unReady = roomInfo.unprepared();
                int isReady = roomInfo.preparations();
                if (unReady == 0 && roomInfo.getRoomType() == 0
                    && isReady >= FindSpyAssets.getInt(FindSpyAssets.PEOPLE_MIN_1)) {
                  // 法官消息
                  sendMessage(1, roomInfo.getRoomId(), null, null);
                  startTimeout(roomInfo.getRoomId());
                } else if (unReady == 0 && roomInfo.getRoomType() == 1
                    && isReady >= FindSpyAssets.getInt(FindSpyAssets.PEOPLE_MIN_2)) {
                  // 法官消息
                  sendMessage(1, roomInfo.getRoomId(), null, null);
                  startTimeout(roomInfo.getRoomId());
                } else {
                  // 清理检测
                  List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
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
              if (roomInfo.seatedPlayers().size() == 0 && roomInfo.getAudienceList().size() == 0) {
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
   * TODO 观战信息.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/23 20:57
   */
  private void watchInfo(Channel channel, Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindSpyPlayer checkPlayer = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          FindSpyPlayer nowPlayer = roomInfo.getPlayerInfo(packet.userId);
          F30051.PlayerInfo.Builder playerInfo = F30051.PlayerInfo.newBuilder();
          playerInfo.setNick(nowPlayer.getPlayerName());
          playerInfo.setUserID(nowPlayer.getPlayerId());
          playerInfo.setUrl(nowPlayer.getPlayerAvatar());
          playerInfo.setSex(nowPlayer.getPlayerSex());
          playerInfo.setDeviPosition(nowPlayer.getSeatNumber());
          playerInfo.setState(nowPlayer.getPlayerStatus());
          F30051.F3005118S2C.Builder builder = F30051.F3005118S2C.newBuilder();
          builder.setNowUser(playerInfo);
          if (roomInfo.getRoomStatus() == 0) {
            if (nowPlayer.getPlayerStatus() >= 1) {
              builder.setIsCanAction(false);
            } else {
              builder.setIsCanAction(true);
            }
          } else {
            builder.setIsCanAction(false);
          }
          builder.setNowStatus(nowPlayer.getIdentity());
          if (nowPlayer.getIdentity() == 0) {
            builder.addAllWatchUser(roomInfo.getAudienceIcon());
          } else {
            builder.addAllWatchUser(roomInfo.getAudienceIcon()
                .stream()
                .filter(s -> !s.equals(nowPlayer.getPlayerAvatar()))
                .collect(Collectors.toList()));
          }
          channel.writeAndFlush(new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.WATCH_INFO,
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
   * TODO 断线重连.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/1 21:30
   * @update 2020/9/1 21:09
   */
  private void disconnected(Channel channel, Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (roomInfo != null) {
        F30051.F3005117S2C.Builder builder = F30051.F3005117S2C.newBuilder();
        FindSpyPlayer player = roomInfo.getPlayerInfo(packet.userId);
        Integer speakMode = roomInfo.getSpeakMode();
        List<FindSpyPlayer> findSpyPlayers = roomInfo.getPlayerList();
        F30051.PlayerInfo.Builder playerInfo;
        for (FindSpyPlayer findSpyPlayer : findSpyPlayers) {
          playerInfo = F30051.PlayerInfo.newBuilder();
          playerInfo.setNick(findSpyPlayer.getPlayerName());
          playerInfo.setUserID(findSpyPlayer.getPlayerId());
          playerInfo.setUrl(findSpyPlayer.getPlayerAvatar());
          playerInfo.setSex(findSpyPlayer.getPlayerSex());
          playerInfo.setDeviPosition(findSpyPlayer.getSeatNumber());
          playerInfo.setState(findSpyPlayer.getPlayerStatus());
          if (speakMode == 0 && findSpyPlayer.getPlayerId() > 0) {
            if (StringUtils.isNotEmpty(findSpyPlayer.getSpeakWords())) {
              playerInfo.setSpeakWords(findSpyPlayer.getSpeakWords());
            }
          }
          if (StringUtils.isNotEmpty(findSpyPlayer.getAvatarFrame())) {
            playerInfo.setIconFrame(findSpyPlayer.getAvatarFrame());
          }
          builder.addBreakLineInfo(playerInfo);
        }
        builder.setRotationNum(roomInfo.getRoomRound());
        if (player.getIdentity() == 0) {
          if (speakMode == 0) {
            // 词汇展示 F300513S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.PUSH_WORDS)) {
              LocalDateTime udt = roomInfo.getRoundTime().plusSeconds(40);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              builder.setRoundTimes(second);
              F30051.F300513S2C.Builder wordsShow = F30051.F300513S2C.newBuilder();
              if (player.getGameIdentity() == 1) {
                wordsShow.setType(0);
                wordsShow.setWord(roomInfo.getRoomLexicon().getLexiconMass());
              } else if (player.getGameIdentity() == 2) {
                wordsShow.setType(0);
                wordsShow.setWord(roomInfo.getRoomLexicon().getLexiconSpy());
              }
              if (roomInfo.getChangeIndex() == 0) {
                wordsShow.setIsChange(true);
              } else if (roomInfo.getChangeIndex() == 2) {
                wordsShow.setIsChange(false);
              }
              builder.setWordsShow(wordsShow);
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 换词信息 F300514S2C
            if (roomInfo.getChangeIndex() == 1) {
              LocalDateTime udt = roomInfo.getRoundTime().plusSeconds(40);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              builder.setRoundTimes(second);
              F30051.F300514S2C.Builder changeWords = F30051.F300514S2C.newBuilder();
              FindSpyPlayer changeUser = roomInfo.getPlayerInfo(roomInfo.getChangeUser());
              changeWords.setApplyNo(changeUser.getSeatNumber());
              List<FindSpyPlayer> playerList = roomInfo.getPlayerList().stream()
                  .filter(s -> s.getPlayerId() > 0)
                  .sorted(Comparator.comparing(FindSpyPlayer::getSeatNumber))
                  .collect(Collectors.toList());
              F30051.ChangeInfo.Builder changeInfo;
              for (FindSpyPlayer players : playerList) {
                changeInfo = F30051.ChangeInfo.newBuilder();
                changeInfo.setPlayerNo(players.getSeatNumber());
                changeInfo.setPlayerIcon(players.getPlayerAvatar());
                changeInfo.setIsChangeWord(players.getChangeAction());
                changeWords.addChangeInfo(changeInfo);
              }
              builder.setChangeWords(changeWords);
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 开始描述 F300516S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.ROUND_START)) {
              LocalDateTime udt = roomInfo.getRoundTime().plusSeconds(40);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              builder.setRoundTimes(second);
              F30051.F300516S2C.Builder startWords = F30051.F300516S2C.newBuilder();
              List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
              playerList.stream().filter(s -> s.getPlayerId() > 0
                  // 玩家标记 [0-游戏中 1-已出局]
                  && s.getPlayerIndex() == 0
                  // 玩家身份 [0-玩家 1-观众]
                  && s.getIdentity() == 0
                  // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
                  && (s.getPlayerStatus() == 2 || s.getPlayerStatus() == 4)
                  // 连接状态 [0-连接中 1-已断开]
                  && (s.getLinkStatus() == 0 || s.getLinkStatus() == 1)
              ).forEach(s -> startWords.addPlayerUser(s.getPlayerId()));
              builder.setStartWords(startWords);
              if (player.getGameIdentity() == 0) {
                builder.setWord(roomInfo.getRoomLexicon().getLexiconMass());
              } else {
                builder.setWord(roomInfo.getRoomLexicon().getLexiconSpy());
              }
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 额外描述 F300516S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.START_WORDS)) {
              F30051.F300516S2C.Builder startWords = F30051.F300516S2C.newBuilder();
              List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
              playerList.stream().filter(s -> s.getPlayerId() > 0
                  // 玩家标记 [0-游戏中 1-已出局]
                  && s.getPlayerIndex() == 0
                  // 玩家身份 [0-玩家 1-观众]
                  && s.getIdentity() == 0
                  // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
                  && (s.getPlayerStatus() == 2 || s.getPlayerStatus() == 4)
                  // 连接状态 [0-连接中 1-已断开]
                  && (s.getLinkStatus() == 0 || s.getLinkStatus() == 1)
              ).forEach(s -> startWords.addPlayerUser(s.getPlayerId()));
              // 20s
              LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              startWords.setSpeakTime(second);
              builder.setStartWords(startWords);
              if (player.getGameIdentity() == 0) {
                builder.setWord(roomInfo.getRoomLexicon().getLexiconMass());
              } else {
                builder.setWord(roomInfo.getRoomLexicon().getLexiconSpy());
              }
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 平票描述 F300516S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.DRAW_CHECK)) {
              F30051.F300516S2C.Builder startWords = F30051.F300516S2C.newBuilder();
              List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
              List<Long> speakList = roomInfo.getTempList();
              playerList.stream().filter(s -> speakList.contains(s.getPlayerId()))
                  .forEach(s -> startWords.addPlayerUser(s.getPlayerId()));
              // 20s
              LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              startWords.setSpeakTime(second);
              builder.setStartWords(startWords);
              if (player.getGameIdentity() == 0) {
                builder.setWord(roomInfo.getRoomLexicon().getLexiconMass());
              } else {
                builder.setWord(roomInfo.getRoomLexicon().getLexiconSpy());
              }
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 开始投票 F300519S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.START_VOTE)) {
              F30051.F300519S2C.Builder startVote = F30051.F300519S2C.newBuilder();
              List<FindSpyVote> voteList = roomInfo.getVoteList();
              List<Long> userIds = Lists.newLinkedList();
              voteList.forEach(vote -> userIds.add(vote.getUserId()));
              if (userIds.contains(player.getPlayerId())) {
                startVote.addAllDoUserId(userIds);
                List<Long> excludeList = userIds.stream()
                    .filter(s -> !s.equals(player.getPlayerId()))
                    .collect(Collectors.toList());
                startVote.addAllKillUserId(excludeList);
              }
              LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              startVote.setVoteTime(second);
              builder.setStartVote(startVote);
              // 投票人员信息
              F30051.VoteInfo.Builder voteInfo;
              for (FindSpyVote spyVote : voteList) {
                voteInfo = F30051.VoteInfo.newBuilder();
                voteInfo.setUserID(spyVote.getUserId());
                voteInfo.addAllUserIcon(spyVote.getVoteList());
                builder.addVoteinfos(voteInfo);
              }
              if (player.getGameIdentity() == 0) {
                builder.setWord(roomInfo.getRoomLexicon().getLexiconMass());
              } else {
                builder.setWord(roomInfo.getRoomLexicon().getLexiconSpy());
              }
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 玩家爆词 F3005113S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.OPEN_WORDS)) {
              F30051.F3005113S2C.Builder openWords = F30051.F3005113S2C.newBuilder();
              openWords.setUserID(roomInfo.getOpenWordsUser());
              LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              openWords.setBlastTime(second);
              builder.setOpenWords(openWords);
              if (player.getGameIdentity() == 0) {
                builder.setWord(roomInfo.getRoomLexicon().getLexiconMass());
              } else {
                builder.setWord(roomInfo.getRoomLexicon().getLexiconSpy());
              }
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
            }
          } else {
            // 词汇展示 F300513S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.PUSH_WORDS)) {
              LocalDateTime udt = roomInfo.getRoundTime().plusSeconds(40);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              builder.setRoundTimes(second);
              F30051.F300513S2C.Builder wordsShow = F30051.F300513S2C.newBuilder();
              if (player.getGameIdentity() == 1) {
                wordsShow.setType(0);
                wordsShow.setWord(roomInfo.getRoomLexicon().getLexiconMass());
              } else if (player.getGameIdentity() == 2) {
                wordsShow.setType(0);
                wordsShow.setWord(roomInfo.getRoomLexicon().getLexiconSpy());
              }
              if (roomInfo.getChangeIndex() == 0) {
                wordsShow.setIsChange(true);
              } else if (roomInfo.getChangeIndex() == 2) {
                wordsShow.setIsChange(false);
              }
              builder.setWordsShow(wordsShow);
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 换词信息 F300514S2C
            if (roomInfo.getChangeIndex() == 1) {
              LocalDateTime udt = roomInfo.getRoundTime().plusSeconds(40);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              builder.setRoundTimes(second);
              F30051.F300514S2C.Builder changeWords = F30051.F300514S2C.newBuilder();
              FindSpyPlayer changeUser = roomInfo.getPlayerInfo(roomInfo.getChangeUser());
              changeWords.setApplyNo(changeUser.getSeatNumber());
              List<FindSpyPlayer> playerList = roomInfo.getPlayerList().stream()
                  .filter(s -> s.getPlayerId() > 0)
                  .sorted(Comparator.comparing(FindSpyPlayer::getSeatNumber))
                  .collect(Collectors.toList());
              F30051.ChangeInfo.Builder changeInfo;
              for (FindSpyPlayer players : playerList) {
                changeInfo = F30051.ChangeInfo.newBuilder();
                changeInfo.setPlayerNo(players.getSeatNumber());
                changeInfo.setPlayerIcon(players.getPlayerAvatar());
                changeInfo.setIsChangeWord(players.getChangeAction());
                changeWords.addChangeInfo(changeInfo);
              }
              builder.setChangeWords(changeWords);
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 开始说话 F300518S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.START_SPEAK)) {
              F30051.F300518S2C.Builder startSpeak = F30051.F300518S2C.newBuilder();
              Long speakUserId = roomInfo.getLastActionId();
              Long lastUserId = roomInfo.getLastUserId();
              if (!speakUserId.equals(lastUserId)) {
                FindSpyPlayer nowPlayer = roomInfo.getPlayerInfo(roomInfo.getLastActionId());
                // FindSpyPlayer nextPlayer = roomInfo.getPlayerInfo(roomInfo.getNextActionPlayer(roomInfo.getLastActionId()));
                // startSpeak.setNowSpeakPlayer(nowPlayer.getSeatNumber());
                // startSpeak.setNextSpeakPlayer(nextPlayer.getSeatNumber());
                startSpeak.setSpeakPlayer(nowPlayer.getSeatNumber());
                LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
                LocalDateTime nds = LocalDateTime.now();
                Duration duration = Duration.between(nds, udt);
                int second = Math.toIntExact(duration.getSeconds());
                startSpeak.setSpeakTime(second);
                builder.setStartSpeak(startSpeak);
                if (player.getGameIdentity() == 0) {
                  builder.setWord(roomInfo.getRoomLexicon().getLexiconMass());
                } else {
                  builder.setWord(roomInfo.getRoomLexicon().getLexiconSpy());
                }
                channel.writeAndFlush(
                    new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                        builder.build().toByteArray()));
              } else {
                FindSpyPlayer nowPlayer = roomInfo.getPlayerInfo(roomInfo.getLastActionId());
                startSpeak.setSpeakPlayer(nowPlayer.getSeatNumber());
                //startSpeak.setNextSpeakPlayer(0);
                LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
                LocalDateTime nds = LocalDateTime.now();
                Duration duration = Duration.between(nds, udt);
                int second = Math.toIntExact(duration.getSeconds());
                startSpeak.setSpeakTime(second);
                builder.setStartSpeak(startSpeak);
                if (player.getGameIdentity() == 0) {
                  builder.setWord(roomInfo.getRoomLexicon().getLexiconMass());
                } else {
                  builder.setWord(roomInfo.getRoomLexicon().getLexiconSpy());
                }
                channel.writeAndFlush(
                    new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                        builder.build().toByteArray()));
              }
              return;
            }
            // 平票说话 F300518S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.DRAW_CHECK)) {
              F30051.F300518S2C.Builder startSpeak = F30051.F300518S2C.newBuilder();
              Long speakUserId = roomInfo.getLastActionId();
              Long lastUserId = roomInfo.getLastUserId();
              if (!speakUserId.equals(lastUserId)) {
                FindSpyPlayer nowPlayer = roomInfo.getPlayerInfo(roomInfo.getLastActionId());
                //FindSpyPlayer nextPlayer = roomInfo.getPlayerInfo(roomInfo.getNextActionPlayer(roomInfo.getLastActionId()));
                startSpeak.setSpeakPlayer(nowPlayer.getSeatNumber());
                //startSpeak.setNextSpeakPlayer(nextPlayer.getSeatNumber());
                LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
                LocalDateTime nds = LocalDateTime.now();
                Duration duration = Duration.between(nds, udt);
                int second = Math.toIntExact(duration.getSeconds());
                startSpeak.setSpeakTime(second);
                builder.setStartSpeak(startSpeak);
                if (player.getGameIdentity() == 0) {
                  builder.setWord(roomInfo.getRoomLexicon().getLexiconMass());
                } else {
                  builder.setWord(roomInfo.getRoomLexicon().getLexiconSpy());
                }
                channel.writeAndFlush(
                    new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                        builder.build().toByteArray()));
              } else {
                FindSpyPlayer nowPlayer = roomInfo.getPlayerInfo(roomInfo.getLastActionId());
                startSpeak.setSpeakPlayer(nowPlayer.getSeatNumber());
                //startSpeak.setNextSpeakPlayer(0);
                LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
                LocalDateTime nds = LocalDateTime.now();
                Duration duration = Duration.between(nds, udt);
                int second = Math.toIntExact(duration.getSeconds());
                startSpeak.setSpeakTime(second);
                builder.setStartSpeak(startSpeak);
                if (player.getGameIdentity() == 0) {
                  builder.setWord(roomInfo.getRoomLexicon().getLexiconMass());
                } else {
                  builder.setWord(roomInfo.getRoomLexicon().getLexiconSpy());
                }
                channel.writeAndFlush(
                    new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                        builder.build().toByteArray()));
              }
              return;
            }
            // 开始投票 F300519S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.START_VOTE)) {
              F30051.F300519S2C.Builder startVote = F30051.F300519S2C.newBuilder();
              List<FindSpyVote> voteList = roomInfo.getVoteList();
              List<Long> userIds = Lists.newLinkedList();
              voteList.forEach(vote -> userIds.add(vote.getUserId()));
              if (userIds.contains(player.getPlayerId())) {
                startVote.addAllDoUserId(userIds);
                List<Long> excludeList = userIds.stream()
                    .filter(s -> !s.equals(player.getPlayerId()))
                    .collect(Collectors.toList());
                startVote.addAllKillUserId(excludeList);
              }
              LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              startVote.setVoteTime(second);
              builder.setStartVote(startVote);
              // 投票人员信息 VoteInfo
              F30051.VoteInfo.Builder voteInfo;
              for (FindSpyVote spyVote : voteList) {
                voteInfo = F30051.VoteInfo.newBuilder();
                voteInfo.setUserID(spyVote.getUserId());
                voteInfo.addAllUserIcon(spyVote.getVoteList());
                builder.addVoteinfos(voteInfo);
              }
              if (player.getGameIdentity() == 0) {
                builder.setWord(roomInfo.getRoomLexicon().getLexiconMass());
              } else {
                builder.setWord(roomInfo.getRoomLexicon().getLexiconSpy());
              }
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 玩家爆词 F3005113S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.OPEN_WORDS)) {
              F30051.F3005113S2C.Builder openWords = F30051.F3005113S2C.newBuilder();
              openWords.setUserID(roomInfo.getOpenWordsUser());
              LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              openWords.setBlastTime(second);
              builder.setOpenWords(openWords);
              if (player.getGameIdentity() == 0) {
                builder.setWord(roomInfo.getRoomLexicon().getLexiconMass());
              } else {
                builder.setWord(roomInfo.getRoomLexicon().getLexiconSpy());
              }
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
            }
          }
        } else {
          if (speakMode == 0) {
            // 词汇展示 F300513S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.PUSH_WORDS)) {
              LocalDateTime udt = roomInfo.getRoundTime().plusSeconds(40);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              builder.setRoundTimes(second);
              F30051.F300513S2C.Builder wordsShow = F30051.F300513S2C.newBuilder();
              builder.setWordsShow(wordsShow.setType(1));
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 换词信息 F300514S2C
            if (roomInfo.getChangeIndex() == 1) {
              LocalDateTime udt = roomInfo.getRoundTime().plusSeconds(40);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              builder.setRoundTimes(second);
              F30051.F300514S2C.Builder changeWords = F30051.F300514S2C.newBuilder();
              FindSpyPlayer changeUser = roomInfo.getPlayerInfo(roomInfo.getChangeUser());
              changeWords.setApplyNo(changeUser.getSeatNumber());
              List<FindSpyPlayer> playerList = roomInfo.getPlayerList().stream()
                  .filter(s -> s.getPlayerId() > 0)
                  .sorted(Comparator.comparing(FindSpyPlayer::getSeatNumber))
                  .collect(Collectors.toList());
              F30051.ChangeInfo.Builder changeInfo;
              for (FindSpyPlayer players : playerList) {
                changeInfo = F30051.ChangeInfo.newBuilder();
                changeInfo.setPlayerNo(players.getSeatNumber());
                changeInfo.setPlayerIcon(players.getPlayerAvatar());
                changeInfo.setIsChangeWord(players.getChangeAction());
                changeWords.addChangeInfo(changeInfo);
              }
              builder.setChangeWords(changeWords);
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 开始描述 F300516S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.ROUND_START)) {
              LocalDateTime udt = roomInfo.getRoundTime().plusSeconds(40);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              builder.setRoundTimes(second);
              F30051.F300516S2C.Builder startWords = F30051.F300516S2C.newBuilder();
              List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
              playerList.stream().filter(s -> s.getPlayerId() > 0)
                  .forEach(s -> startWords.addPlayerUser(s.getPlayerId()));
              builder.setStartWords(startWords);
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 额外描述 F300516S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.START_WORDS)) {
              F30051.F300516S2C.Builder startWords = F30051.F300516S2C.newBuilder();
              List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
              playerList.stream().filter(s -> s.getPlayerId() > 0)
                  .forEach(s -> startWords.addPlayerUser(s.getPlayerId()));
              // 20s
              LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              startWords.setSpeakTime(second);
              builder.setStartWords(startWords);
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 平票描述 F300516S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.DRAW_CHECK)) {
              F30051.F300516S2C.Builder startWords = F30051.F300516S2C.newBuilder();
              List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
              playerList.stream().filter(s -> s.getPlayerId() > 0)
                  .forEach(s -> startWords.addPlayerUser(s.getPlayerId()));
              // 20s
              LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              startWords.setSpeakTime(second);
              builder.setStartWords(startWords);
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 开始投票 F300519S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.START_VOTE)) {
              F30051.F300519S2C.Builder startVote = F30051.F300519S2C.newBuilder();
              List<FindSpyVote> voteList = roomInfo.getVoteList();
              List<Long> userIds = Lists.newLinkedList();
              voteList.forEach(vote -> userIds.add(vote.getUserId()));
              if (userIds.contains(player.getPlayerId())) {
                startVote.addAllDoUserId(userIds);
                List<Long> excludeList = userIds.stream()
                    .filter(s -> !s.equals(player.getPlayerId()))
                    .collect(Collectors.toList());
                startVote.addAllKillUserId(excludeList);
              }
              LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              startVote.setVoteTime(second);
              builder.setStartVote(startVote);
              // 投票人员信息 VoteInfo
              F30051.VoteInfo.Builder voteInfo;
              for (FindSpyVote spyVote : voteList) {
                voteInfo = F30051.VoteInfo.newBuilder();
                voteInfo.setUserID(spyVote.getUserId());
                voteInfo.addAllUserIcon(spyVote.getVoteList());
                builder.addVoteinfos(voteInfo);
              }
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 玩家爆词 F3005113S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.OPEN_WORDS)) {
              F30051.F3005113S2C.Builder openWords = F30051.F3005113S2C.newBuilder();
              openWords.setUserID(roomInfo.getOpenWordsUser());
              LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              openWords.setBlastTime(second);
              builder.setOpenWords(openWords);
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
            }
          } else {
            // 词汇展示 F300513S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.PUSH_WORDS)) {
              LocalDateTime udt = roomInfo.getRoundTime().plusSeconds(40);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              builder.setRoundTimes(second);
              F30051.F300513S2C.Builder wordsShow = F30051.F300513S2C.newBuilder();
              builder.setWordsShow(wordsShow.setType(1));
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 换词信息 F300514S2C
            if (roomInfo.getChangeIndex() == 1) {
              LocalDateTime udt = roomInfo.getRoundTime().plusSeconds(40);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              builder.setRoundTimes(second);
              F30051.F300514S2C.Builder changeWords = F30051.F300514S2C.newBuilder();
              FindSpyPlayer changeUser = roomInfo.getPlayerInfo(roomInfo.getChangeUser());
              changeWords.setApplyNo(changeUser.getSeatNumber());
              List<FindSpyPlayer> playerList = roomInfo.getPlayerList().stream()
                  .filter(s -> s.getPlayerId() > 0)
                  .sorted(Comparator.comparing(FindSpyPlayer::getSeatNumber))
                  .collect(Collectors.toList());
              F30051.ChangeInfo.Builder changeInfo;
              for (FindSpyPlayer players : playerList) {
                changeInfo = F30051.ChangeInfo.newBuilder();
                changeInfo.setPlayerNo(players.getSeatNumber());
                changeInfo.setPlayerIcon(players.getPlayerAvatar());
                changeInfo.setIsChangeWord(players.getChangeAction());
                changeWords.addChangeInfo(changeInfo);
              }
              builder.setChangeWords(changeWords);
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 开始说话 F300518S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.START_SPEAK)) {
              F30051.F300518S2C.Builder startSpeak = F30051.F300518S2C.newBuilder();
              Long speakUserId = roomInfo.getLastActionId();
              Long lastUserId = roomInfo.getLastUserId();
              if (!speakUserId.equals(lastUserId)) {
                FindSpyPlayer nowPlayer = roomInfo.getPlayerInfo(roomInfo.getLastActionId());
                //FindSpyPlayer nextPlayer = roomInfo.getPlayerInfo(roomInfo.getNextActionPlayer(roomInfo.getLastActionId()));
                startSpeak.setSpeakPlayer(nowPlayer.getSeatNumber());
                //startSpeak.setNextSpeakPlayer(nextPlayer.getSeatNumber());
                LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
                LocalDateTime nds = LocalDateTime.now();
                Duration duration = Duration.between(nds, udt);
                int second = Math.toIntExact(duration.getSeconds());
                startSpeak.setSpeakTime(second);
                builder.setStartSpeak(startSpeak);
                channel.writeAndFlush(
                    new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                        builder.build().toByteArray()));
              } else {
                FindSpyPlayer nowPlayer = roomInfo.getPlayerInfo(roomInfo.getLastActionId());
                startSpeak.setSpeakPlayer(nowPlayer.getSeatNumber());
//                startSpeak.setNextSpeakPlayer(0);
                LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
                LocalDateTime nds = LocalDateTime.now();
                Duration duration = Duration.between(nds, udt);
                int second = Math.toIntExact(duration.getSeconds());
                startSpeak.setSpeakTime(second);
                builder.setStartSpeak(startSpeak);
                channel.writeAndFlush(
                    new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                        builder.build().toByteArray()));
              }
              return;
            }
            // 平票说话 F300518S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.DRAW_CHECK)) {
              F30051.F300518S2C.Builder startSpeak = F30051.F300518S2C.newBuilder();
              Long speakUserId = roomInfo.getLastActionId();
              Long lastUserId = roomInfo.getLastUserId();
              if (!speakUserId.equals(lastUserId)) {
                FindSpyPlayer nowPlayer = roomInfo.getPlayerInfo(roomInfo.getLastActionId());
                //FindSpyPlayer nextPlayer = roomInfo.getPlayerInfo(roomInfo.getNextActionPlayer(roomInfo.getLastActionId()));
                startSpeak.setSpeakPlayer(nowPlayer.getSeatNumber());
//                startSpeak.setNextSpeakPlayer(nextPlayer.getSeatNumber());
                LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
                LocalDateTime nds = LocalDateTime.now();
                Duration duration = Duration.between(nds, udt);
                int second = Math.toIntExact(duration.getSeconds());
                startSpeak.setSpeakTime(second);
                builder.setStartSpeak(startSpeak);
                channel.writeAndFlush(
                    new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                        builder.build().toByteArray()));
              } else {
                FindSpyPlayer nowPlayer = roomInfo.getPlayerInfo(roomInfo.getLastActionId());
                startSpeak.setSpeakPlayer(nowPlayer.getSeatNumber());
//                startSpeak.setNextSpeakPlayer(0);
                LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
                LocalDateTime nds = LocalDateTime.now();
                Duration duration = Duration.between(nds, udt);
                int second = Math.toIntExact(duration.getSeconds());
                startSpeak.setSpeakTime(second);
                builder.setStartSpeak(startSpeak);
                channel.writeAndFlush(
                    new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                        builder.build().toByteArray()));
              }
              return;
            }
            // 开始投票 F300519S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.START_VOTE)) {
              F30051.F300519S2C.Builder startVote = F30051.F300519S2C.newBuilder();
              List<FindSpyVote> voteList = roomInfo.getVoteList();
              List<Long> userIds = Lists.newLinkedList();
              voteList.forEach(vote -> userIds.add(vote.getUserId()));
              if (userIds.contains(player.getPlayerId())) {
                startVote.addAllDoUserId(userIds);
                List<Long> excludeList = userIds.stream()
                    .filter(s -> !s.equals(player.getPlayerId()))
                    .collect(Collectors.toList());
                startVote.addAllKillUserId(excludeList);
              }
              LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              startVote.setVoteTime(second);
              builder.setStartVote(startVote);
              // 投票人员信息 VoteInfo
              F30051.VoteInfo.Builder voteInfo;
              for (FindSpyVote spyVote : voteList) {
                voteInfo = F30051.VoteInfo.newBuilder();
                voteInfo.setUserID(spyVote.getUserId());
                voteInfo.addAllUserIcon(spyVote.getVoteList());
                builder.addVoteinfos(voteInfo);
              }
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
              return;
            }
            // 玩家爆词 F3005113S2C
            if (roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.OPEN_WORDS)) {
              F30051.F3005113S2C.Builder openWords = F30051.F3005113S2C.newBuilder();
              openWords.setUserID(roomInfo.getOpenWordsUser());
              LocalDateTime udt = roomInfo.getSpecialTime().plusSeconds(20);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              openWords.setBlastTime(second);
              builder.setOpenWords(openWords);
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.DISCONNECTED,
                      builder.build().toByteArray()));
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
   * TODO 站起坐下.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/10/13 18:10
   */
  private void joinOrLeave(Channel channel, Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindSpyPlayer checkPlayer = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30051.F3005119C2S request = F30051.F3005119C2S.parseFrom(packet.bytes);
          F30051.F3005119S2C.Builder builder = F30051.F3005119S2C.newBuilder();
          if (roomInfo.getRoomStatus() == 1) {
            builder.setResult(1).setStand(request.getIsStand());
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.JOIN_LEAVE,
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
   * @create 2020/9/1 16:58
   * @update 2020/9/23 20:57
   */
  private void enterWatch(Channel channel, Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (roomInfo != null) {
        F30051.F3005119C2S request = F30051.F3005119C2S.parseFrom(packet.bytes);
        F30051.F3005119S2C.Builder builder = F30051.F3005119S2C.newBuilder();
        FindSpyPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (playerInfo.getPlayerStatus() > 0) {
          builder.setResult(request.getIsStand()).setResult(1).setStand(request.getIsStand());
          channel.writeAndFlush(
              new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.JOIN_LEAVE,
                  builder.build().toByteArray()));
        } else {
          roomInfo.cancelTimeOut((int) packet.userId);
          int seat = roomInfo.leaveSeat(playerInfo.getPlayerId());
          builder.setSeatNo(seat);
          builder.setStand(request.getIsStand());
          builder.setResult(0);
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.JOIN_LEAVE,
                  builder.build().toByteArray()), roomInfo.getRoomId());
          // 法官消息
          sendMessage(11, packet.roomId, playerInfo.getPlayerName(), null);
          watchInfo(channel, packet);
          boolean testRoom = (packet.roomId == FindSpyAssets.getLong(FindSpyAssets.TEST_ID));
          if (!testRoom) {
            if (roomInfo.getOpenWay() == 0) {
              if (roomInfo.remainingSeat() > 0) {
                refreshSpyMath(packet.roomId);
              }
            }
            int unReady = roomInfo.unprepared();
            int isReady = roomInfo.preparations();
            if (unReady == 0 && roomInfo.getRoomType() == 0 && isReady >= FindSpyAssets
                .getInt(FindSpyAssets.PEOPLE_MIN_1)) {
              // 法官消息
              sendMessage(1, roomInfo.getRoomId(), null, null);
              startTimeout(roomInfo.getRoomId());
            } else if (unReady == 0 && roomInfo.getRoomType() == 1 && isReady >= FindSpyAssets
                .getInt(FindSpyAssets.PEOPLE_MIN_2)) {
              // 法官消息
              sendMessage(1, roomInfo.getRoomId(), null, null);
              startTimeout(roomInfo.getRoomId());
            }
            // 清理检测
            List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
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
   * @create 2020/9/1 16:58
   * @update 2020/9/23 20:57
   */
  private void enterSeat(Channel channel, Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (roomInfo != null) {
        F30051.F3005119C2S request = F30051.F3005119C2S.parseFrom(packet.bytes);
        F30051.F3005119S2C.Builder builder = F30051.F3005119S2C.newBuilder();
        if (roomInfo.remainingSeat() > 0) {
          FindSpyPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
          if (playerInfo.getIdentity() != 1) {
            builder.setResult(request.getIsStand()).setResult(1);
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.JOIN_LEAVE,
                    builder.build().toByteArray()));
          } else {
            // 销毁清理定时
            roomInfo.cancelTimeOut((int) packet.roomId);
            roomInfo.cancelTimeOut((int) FindSpyCmd.START_SPEAK);
            int seat = roomInfo.joinSeat(packet.userId);
            builder.setSeatNo(seat);
            builder.setStand(request.getIsStand());
            builder.setResult(0);
            playerInfo = roomInfo.getPlayerInfo(packet.userId);
            F30051.PlayerInfo.Builder playerBuilder = F30051.PlayerInfo.newBuilder();
            playerBuilder.setNick(playerInfo.getPlayerName());
            playerBuilder.setUserID(playerInfo.getPlayerId());
            playerBuilder.setUrl(playerInfo.getPlayerAvatar());
            playerBuilder.setSex(playerInfo.getPlayerSex());
            playerBuilder.setDeviPosition(playerInfo.getSeatNumber());
            playerBuilder.setState(playerInfo.getPlayerStatus());
            if (StringUtils.isNotEmpty(playerInfo.getAvatarFrame())) {
              playerBuilder.setIconFrame(playerInfo.getAvatarFrame());
            }
            playerBuilder.setReadyTime(15);
            builder.setJoinInfo(playerBuilder);
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.JOIN_LEAVE,
                    builder.build().toByteArray()), roomInfo.getRoomId());
            // 法官消息
            sendMessage(20, packet.roomId, playerInfo.getPlayerName(), null);
            readyTimeout(packet.roomId, packet.userId);
            boolean testRoom = (packet.roomId == FindSpyAssets.getLong(FindSpyAssets.TEST_ID));
            if (!testRoom) {
              if (roomInfo.getOpenWay() == 0) {
                if (roomInfo.remainingSeat() > 0) {
                  refreshSpyMath(packet.roomId);
                } else {
                  cancelSpyMatch(packet.roomId, roomInfo.getRoomType(), roomInfo.getSpeakMode());
                }
              }
            }
          }
        } else {
          builder.setResult(request.getIsStand()).setResult(1);
          channel.writeAndFlush(
              new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.JOIN_LEAVE,
                  builder.build().toByteArray()));
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
   * @create 2020/8/31 9:26
   * @update 2020/9/23 20:57
   */
  private void playerSelect(Channel channel, Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindSpyPlayer player = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(player)) {
          F30051.F3005121C2S request = F30051.F3005121C2S.parseFrom(packet.bytes);
          F30051.F3005121S2C.Builder response = F30051.F3005121S2C.newBuilder();
          if (roomInfo.getRoomStatus() == 1) {
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_SELECT,
                    response.setResult(1).setUserID(packet.userId).build().toByteArray()));
          } else {
            // 玩家身份 [0-玩家 1-观众]
            if (player.getIdentity() == 0) {
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_SELECT,
                      response.setResult(1).setUserID(packet.userId).build().toByteArray()));
            } else {
              FindSpyPlayer targetSeat = roomInfo.getTargetSeat(request.getSeatNo());
              if (targetSeat.getPlayerId() > 0) {
                channel.writeAndFlush(
                    new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_SELECT,
                        response.setResult(1).setUserID(packet.userId).build().toByteArray()));
              } else {
                // 销毁清理定时
                roomInfo.cancelTimeOut((int) packet.roomId);
                roomInfo.joinSeat(packet.userId, targetSeat.getSeatNumber());
                player = roomInfo.getPlayerInfo(packet.userId);
                F30051.PlayerInfo.Builder playerInfo = F30051.PlayerInfo.newBuilder();
                playerInfo.setNick(player.getPlayerName());
                playerInfo.setUserID(player.getPlayerId());
                playerInfo.setUrl(player.getPlayerAvatar());
                playerInfo.setSex(player.getPlayerSex());
                playerInfo.setDeviPosition(player.getSeatNumber());
                playerInfo.setState(player.getPlayerStatus());
                if (StringUtils.isNotEmpty(player.getAvatarFrame())) {
                  playerInfo.setIconFrame(player.getAvatarFrame());
                }
                playerInfo.setReadyTime(15);
                response.setJoinInfo(playerInfo);
                response.setUserID(packet.userId);
                response.setSeatNo(request.getSeatNo());
                response.setResult(0);
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_SELECT,
                        response.build().toByteArray()), roomInfo.getRoomId());
                // 法官消息
                sendMessage(20, packet.roomId, player.getPlayerName(), null);
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
   * @update 2020/9/23 20:57
   */
  private void playerChat(Channel channel, Packet packet) {
    try {
      FindSpyRoom findSpyRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(findSpyRoom)) {
        FindSpyPlayer playerInfo = findSpyRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(playerInfo)) {
          F30051.F3005122C2S request = F30051.F3005122C2S.parseFrom(packet.bytes);
          F30051.F3005122S2C.Builder response = F30051.F3005122S2C.newBuilder();
          response.setType(1);
          response.setIcon(playerInfo.getPlayerAvatar());
          response.setNick(playerInfo.getPlayerName());
          if (findSpyRoom.getRoomStatus() == 0) {
            response.setMessage(request.getMessage());
          } else {
            String message = request.getMessage();
            message = message.toUpperCase();
            // 游戏身份 1-平民 2-卧底.
            if (playerInfo.getGameIdentity() == 1) {
              String word = findSpyRoom.getRoomLexicon().getLexiconMass();
              word = word.toUpperCase();
              boolean isExists = message.startsWith(word);
              if (isExists) {
                message = message.replace(word, getStarChar(word.length()));
              }
            } else {
              String word = findSpyRoom.getRoomLexicon().getLexiconSpy();
              word = word.toUpperCase();
              boolean isExists = message.startsWith(word);
              if (isExists) {
                message = message.replace(word, getStarChar(word.length()));
              }
            }
            response.setMessage(message);
          }
          response.setUserID(packet.userId);
          ChatManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_CHAT,
                  response.build().toByteArray()), packet.roomId);
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
   * TODO 结束说话.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/3 16:21
   * @update 2020/11/3 16:21
   */
  private void finishTalking(Channel channel, Packet packet) {
    try {
      FindSpyRoom findSpyRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(findSpyRoom)) {

      }
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
   * @create 2020/7/14 21:23
   * @update 2020/9/23 20:57
   */
  private void closeLoading(Long playerId) {
    try {
      SoftChannel.sendPacketToUserId(
          new Packet(ActionCmd.APP_HEART, FindSpyCmd.LOADING, null), playerId);
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
   * @create 2020/9/18 21:07
   * @update 2020/9/18 21:07
   */
  private void playerNotExist(Channel channel) {
    F30051.F30051S2C.Builder builder = F30051.F30051S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.ENTER_ROOM,
            builder.setResult(2).build().toByteArray()));
  }

  /**
   * TODO 空的房间.
   *
   * @param channel [通信管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/18 21:07
   * @update 2020/9/18 21:07
   */
  private void roomNotExist(Channel channel) {
    F30051.F30051S2C.Builder builder = F30051.F30051S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.ENTER_ROOM,
            builder.setResult(1).build().toByteArray()));
  }

  /**
   * TODO 是否开放.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 13:41
   * @update 2020/10/14 13:41
   */
  private void publicMatch(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (Objects.nonNull(roomInfo)) {
      if (roomInfo.getOpenWay() == 0) {
        if (roomInfo.remainingSeat() > 0) {
          refreshSpyMath(roomId);
        } else {
          cancelSpyMatch(roomId, roomInfo.getRoomType(), roomInfo.getSpeakMode());
        }
      }
    }
  }

  /**
   * TODO 刷新数据.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/24 18:29
   * @update 2020/8/24 18:29
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
   * TODO 拉取装饰.
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/24 13:38
   * @update 2020/8/24 13:38
   */
  private void pullDecorateInfo(Packet packet) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (roomInfo != null) {
        FindSpyPlayer player = roomInfo.getPlayerInfo(packet.userId);
        boolean checkTest = (packet.roomId == FindSpyAssets.getLong(FindSpyAssets.TEST_ID));
        if (checkTest) {
          boolean flag = RandomUtils.nextBoolean();
          if (flag) {
            player.setAvatarFrame(
                "https://7lestore.oss-cn-hangzhou.aliyuncs.com/file/admin/7ed8d12e9a13491aac07301324d2a562.svga");
          }
        } else {
          Map<String, Object> dressInfo = this.userRemote.getUserFrame(packet.userId);
          if (dressInfo != null) {
            player.setAvatarFrame(StringUtils.nvl(dressInfo.get("iconFrame")));
          }
        }
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
   * @create 2020/8/24 13:57
   * @update 2020/8/24 13:57
   */
  private void joinWhoIsSpyRoom(Packet packet) {
    try {
      if (packet.roomId > FindSpyAssets.getLong(FindSpyAssets.TEST_ID)) {
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
   * TODO 转换字符.
   *
   * @param length [字符长度]
   * @return [替换内容]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/7/14 21:23
   */
  private String getStarChar(int length) {
    if (length <= 0) {
      return "";
    }
    // 大部分敏感词汇在10个以内，直接返回缓存的字符串
    if (length <= 10) {
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
   * TODO 发送信息.
   *
   * @param selectIndex [选择编号]
   * @param roomId [房间ID]
   * @param userName [玩家名称]
   * @param number [玩家编号]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/20 21:05
   * @update 2020/9/16 21:07
   */
  private void sendMessage(Integer selectIndex, Long roomId, String userName, Integer number) {
    F10001.F100012S2C.Builder builder = F10001.F100012S2C.newBuilder();
    switch (selectIndex) {
      case 0:
        builder.setType(0).setUserID(0).setMessage(" " + userName + " 进入房间！");
        break;
      case 1:
        builder.setType(0).setUserID(0).setMessage(" 玩家已完成准备，游戏即将开始！");
        break;
      case 2:
        builder.setType(0).setUserID(0).setMessage(" 【" + number + "】号玩家，申请换词！");
        break;
      case 3:
        builder.setType(0).setUserID(0).setMessage(" 申请换词成功！");
        break;
      case 4:
        builder.setType(0).setUserID(0).setMessage(" 申请换词失败！");
        break;
      case 5:
        builder.setType(0).setUserID(0).setMessage(" 描述结束，开始投票！");
        break;
      case 6:
        builder.setType(0).setUserID(0).setMessage(" 第 " + number + " 回合，请描述词汇！");
        break;
      case 7:
        builder.setType(0).setUserID(0).setMessage(" 【" + number + "】号玩家为平民！");
        break;
      case 8:
        builder.setType(0).setUserID(0).setMessage(" 【" + number + "】号玩家为卧底，卧底玩家是否爆词！");
        break;
      case 9:
        builder.setType(0).setUserID(0).setMessage(" 平票决斗，请开始描述！");
        break;
      case 10:
        builder.setType(0).setUserID(0).setMessage(" 【" + number + "】号玩家，请描述词汇！");
        break;
      case 11:
        builder.setType(0).setUserID(0).setMessage(" " + userName + " 离开座位！");
        break;
      case 12:
        builder.setType(0).setUserID(0).setMessage(" 游戏开始，请描述词汇！");
        break;
      case 13:
        builder.setType(0).setUserID(0).setMessage(" 【" + number + "】号玩家爆词失败！");
        break;
      case 14:
        builder.setType(0).setUserID(0).setMessage(" " + userName + " 离开房间！");
        break;
      case 15:
        builder.setType(0).setUserID(0).setMessage(" 【" + number + "】号玩家取消爆词！");
        break;
      case 16:
        builder.setType(0).setUserID(0).setMessage(" 【" + number + "】号玩家选择爆词！");
        break;
      case 17:
        builder.setType(0).setUserID(0).setMessage(" 【" + number + "】号玩家开始爆词！");
        break;
      case 18:
        builder.setType(0).setUserID(0).setMessage(" 【" + number + "】号玩家爆词成功！");
        break;
      case 19:
        builder.setType(0).setUserID(0).setMessage(" 【" + number + "】号玩家爆词失败！");
        break;
      case 20:
        builder.setType(0).setUserID(0).setMessage(" " + userName + " 坐上座位！");
        break;
      case 21:
        builder.setType(0).setUserID(0).setMessage(" 座位已坐满，请大家尽快准备！");
        break;
      case 22:
        builder.setType(0).setUserID(0).setMessage(" 【" + number + "】号玩家为卧底！");
        break;
      case 23:
        builder.setType(0).setUserID(0).setMessage(" 请大家尽快准备，新一轮比赛准备中！");
        break;
      default:
        builder.setType(0).setUserID(0).setMessage(" 游戏结束！");
        break;
    }
    ChatManager.sendPacketToGroup(
        new Packet(ActionCmd.GAME_CHAT, FindSpyCmd.SYSTEM_INFO,
            builder.build().toByteArray()), roomId);
  }

  /**
   * TODO 关闭页面.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/18 18:23
   * @update 2020/9/18 21:07
   */
  private void closePage(Channel channel, Packet packet) {
    ChatManager.removeChannel(packet.roomId, channel);
    GroupManager.removeChannel(packet.roomId, channel);
    SoftChannel.sendPacketToUserId(
        new Packet(ActionCmd.APP_HEART, (short) 2, null), packet.userId);
  }

  /**
   * TODO 清除数据.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/26 9:11
   * @update 2020/9/30 18:11
   */
  private void clearData(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      // 房间类型 [0-{4-6人>1卧底} 1-{7-8人>2卧底}]         交流方式 [0-文字 1-语音]
      MatchManager.delFindSpyMatch(roomId, roomInfo.getRoomType(), roomInfo.getSpeakMode());
      GAME_DATA.remove(roomId);
      this.gameRemote.deleteRoom(roomId);
      if (this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId)) {
        this.redisUtils.del(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
      }
      ChatManager.delChatGroup(roomId);
      GroupManager.delRoomGroup(roomId);
    }
  }

  /**
   * TODO 刷新匹配.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/17 20:56
   * @update 2020/7/17 20:56
   */
  private void refreshSpyMath(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      MatchRoom matchRoom = new MatchRoom();
      matchRoom.setRoomId(roomId);
      matchRoom.setPeopleNum(roomInfo.remainingSeat());
      MatchManager.updFindSpyMatch(matchRoom, roomInfo.getRoomType(), roomInfo.getSpeakMode());
    }
  }

  /**
   * TODO 关闭匹配.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/10 21:11
   * @update 2020/9/18 21:07
   */
  private void cancelSpyMatch(Long roomId, Integer roomType, Integer speakMode) {
    // 房间类型 [0-{4-6人>1卧底} 1-{7-8人>2卧底}]         交流方式 [0-文字 1-语音]
    MatchManager.delFindSpyMatch(roomId, roomType, speakMode);
  }

  /**
   * TODO 准备信息.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/10 21:11
   * @update 2020/9/1 21:09
   */
  private void readyInfo(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      if (roomInfo.getRoomType() == 0) {
        if (roomInfo.seatedPlayers().size() == FindSpyAssets.getInt(FindSpyAssets.PEOPLE_MAX_1)) {
          // 法官消息
          sendMessage(21, roomId, null, null);
        }
      } else {
        if (roomInfo.seatedPlayers().size() == FindSpyAssets.getInt(FindSpyAssets.PEOPLE_MAX_2)) {
          // 法官消息
          sendMessage(21, roomId, null, null);
        }
      }
    }
  }

  /**
   * TODO 推送词汇.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/9/30 18:11
   */
  private void pushWords(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      Result result = gameRemote.getWhoIsSpyWords();
      roomInfo.setRoomLexicon(JsonUtils.toObjectMap(result.getData()));
      List<FindSpyPlayer> playerList = roomInfo.getPlayerList().stream()
          .filter(player -> player.getPlayerId() > 0)
          .collect(Collectors.toList());
      F30051.F300513S2C.Builder playerBuilder;
      for (FindSpyPlayer playerInfo : playerList) {
        playerBuilder = F30051.F300513S2C.newBuilder();
        if (playerInfo.getGameIdentity() == 1) {
          playerBuilder.setType(0);
          playerBuilder.setWord(roomInfo.getRoomLexicon().getLexiconMass());
        } else if (playerInfo.getGameIdentity() == 2) {
          playerBuilder.setType(0);
          playerBuilder.setWord(roomInfo.getRoomLexicon().getLexiconSpy());
        }
        if (roomInfo.getChangeIndex() == 0) {
          playerBuilder.setIsChange(true);
        } else if (roomInfo.getChangeIndex() == 2) {
          playerBuilder.setIsChange(false);
        }
        playerBuilder.setShowTime(5);
        playerInfo.sendPacket(
            new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PUSH_WORDS,
                playerBuilder.build().toByteArray()));
      }
      // 推送观战
      F30051.F300513S2C.Builder watchBuilder = F30051.F300513S2C.newBuilder();
      if (roomInfo.getAudienceGroup() != null) {
        roomInfo.getAudienceGroup().writeAndFlush(
            new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PUSH_WORDS,
                watchBuilder.setShowTime(5).setType(1).build().toByteArray()));
      }
      wordsTimeout(roomInfo.getRoomId());
    }
  }

  /**
   * TODO 开始描述.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/10/12 18:13
   */
  private void startWords(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      F30051.F300516S2C.Builder builder = F30051.F300516S2C.newBuilder();
      roomInfo.getPlayerList().stream()
          .filter(player -> player.getPlayerId() > 0
              // 玩家标记 [0-游戏中 1-已出局]
              && player.getPlayerIndex() == 0
              // 玩家身份 [0-玩家 1-观众]
              && player.getIdentity() == 0
              // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
              && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
              // 连接状态 [0-连接中 1-已断开]
              && (player.getLinkStatus() == 0 || player.getLinkStatus() == 1)
          ).forEach(player -> builder.addPlayerUser(player.getPlayerId()));
      GroupManager.sendPacketToGroup(
          new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.START_WORDS,
              builder.build().toByteArray()), roomInfo.getRoomId());
    }
  }

  /**
   * TODO 换词描述.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/10/13 18:10
   */
  private void beginWords(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      F30051.F300516S2C.Builder builder = F30051.F300516S2C.newBuilder();
      roomInfo.getPlayerList().stream()
          .filter(player -> player.getPlayerId() > 0
              // 玩家标记 [0-游戏中 1-已出局]
              && player.getPlayerIndex() == 0
              // 玩家身份 [0-玩家 1-观众]
              && player.getIdentity() == 0
              // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
              && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
              // 连接状态 [0-连接中 1-已断开]
              && (player.getLinkStatus() == 0 || player.getLinkStatus() == 1)
          ).forEach(player -> builder.addPlayerUser(player.getPlayerId()));
      builder.setSpeakTime(20);
      GroupManager.sendPacketToGroup(
          new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.START_WORDS,
              builder.build().toByteArray()), roomInfo.getRoomId());
      // 定时操作
      specialTimeout(roomInfo.getRoomId());
    }
  }

  /**
   * TODO 开始说话.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/9/16 21:07
   */
  private void startSpeak(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      long lastUserId = roomInfo.getLastUserId();
      long nowUserId = roomInfo.getLastActionId();
      if (nowUserId != lastUserId) {
        roomInfo.setActionPlayer(roomInfo.getLastActionId());
        long speakUserId = roomInfo.getLastActionId();
        FindSpyPlayer playerInfo = roomInfo.getPlayerInfo(speakUserId);
        // 法官消息
        sendMessage(10, roomId, null, playerInfo.getSeatNumber());
        F30051.F300518S2C.Builder builder = F30051.F300518S2C.newBuilder();
//        builder.setNowSpeakPlayer(playerInfo.getSeatNumber());
        builder.setSpeakPlayer(playerInfo.getSeatNumber());
//        long nextPlayerId = roomInfo.getNextActionPlayer(roomInfo.getLastActionId());
//        FindSpyPlayer nextPlayer = roomInfo.getPlayerInfo(nextPlayerId);
//        builder.setNextSpeakPlayer(nextPlayer.getSeatNumber());
        builder.setSpeakTime(20);
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.START_SPEAK,
                builder.build().toByteArray()), roomInfo.getRoomId());
        normalSpeakTimeout(roomInfo.getRoomId());
      } else {
        lastSpeakExamine(roomId);
      }
    }
  }

  /**
   * TODO 换词成功.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/10/13 18:10
   */
  private void changeSuccess(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      // 法官消息
      sendMessage(3, roomId, null, null);
      F30051.F300515S2C.Builder builder = F30051.F300515S2C.newBuilder();
      GroupManager.sendPacketToGroup(
          new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.CHANGE_ACTION,
              builder.setChangeResult(1).build().toByteArray()), roomInfo.getRoomId());
      if (roomInfo.getSpeakMode() == 0) {
        // 新的游戏
        F30051.F300512S2C.Builder startBuilder = F30051.F300512S2C.newBuilder();
        startBuilder.setRotationNum(roomInfo.getRoomRound());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.ROUND_START,
                startBuilder.build().toByteArray()), roomInfo.getRoomId());
        roundTimeout(roomInfo.getRoomId());
        pushWords(roomInfo.getRoomId());
      } else {
        pushWords(roomInfo.getRoomId());
      }
    }
  }

  /**
   * TODO 申请成功.
   *
   * @param roomId [房间ID]
   * @param userId [用户ID]
   * @param action [是否同意 0-同意 1-不同意]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/10/13 18:10
   */
  private void wordsSuccess(Long roomId, Long userId, Integer action) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      FindSpyPlayer playerInfo = roomInfo.getPlayerInfo(userId);
      // 法官消息
      sendMessage(3, roomId, null, null);
      F30051.F300515S2C.Builder builder = F30051.F300515S2C.newBuilder();
      builder.setPlayerNo(playerInfo.getSeatNumber()).setIsChangeWord(action).setChangeResult(1);
      GroupManager.sendPacketToGroup(
          new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.CHANGE_ACTION,
              builder.build().toByteArray()), roomInfo.getRoomId());
      if (roomInfo.getSpeakMode() == 0) {
        // 新的游戏
        F30051.F300512S2C.Builder startBuilder = F30051.F300512S2C.newBuilder();
        startBuilder.setRotationNum(roomInfo.getRoomRound());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.ROUND_START,
                startBuilder.build().toByteArray()), roomInfo.getRoomId());
        roundTimeout(roomInfo.getRoomId());
        pushWords(roomInfo.getRoomId());
      } else {
        pushWords(roomInfo.getRoomId());
      }
    }
  }

  /**
   * TODO 换词失败.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/10/13 18:10
   */
  private void changeFailed(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      // 法官消息
      sendMessage(4, roomId, null, null);
      F30051.F300515S2C.Builder builder = F30051.F300515S2C.newBuilder();
      GroupManager.sendPacketToGroup(
          new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.CHANGE_ACTION,
              builder.setChangeResult(2).build().toByteArray()), roomInfo.getRoomId());
      // 交流方式 0-文字 1-语音
      if (roomInfo.getSpeakMode() == 0) {
        beginWords(roomInfo.getRoomId());
      } else {
        startSpeak(roomInfo.getRoomId());
      }
    }
  }

  /**
   * TODO 申请失败.
   *
   * @param roomId [房间ID]
   * @param userId [用户ID]
   * @param action [是否同意 0-同意 1-不同意]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/10/13 18:10
   */
  private void wordsFailed(Long roomId, Long userId, Integer action) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      FindSpyPlayer playerInfo = roomInfo.getPlayerInfo(userId);
      // 法官消息
      sendMessage(4, roomId, null, null);
      F30051.F300515S2C.Builder builder = F30051.F300515S2C.newBuilder();
      builder.setPlayerNo(playerInfo.getSeatNumber()).setIsChangeWord(action).setChangeResult(2);
      GroupManager.sendPacketToGroup(
          new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.CHANGE_ACTION,
              builder.build().toByteArray()), roomInfo.getRoomId());
      // 交流方式 0-文字 1-语音
      if (roomInfo.getSpeakMode() == 0) {
        beginWords(roomInfo.getRoomId());
      } else {
        startSpeak(roomInfo.getRoomId());
      }
    }
  }

  /**
   * TODO 开始投票.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/9/1 21:09
   */
  private void startVote(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      // 法官消息
      sendMessage(5, roomId, null, null);
      List<Long> userIds = roomInfo.whoCanVote();
      List<FindSpyPlayer> playerList = roomInfo.getPlayerList().stream()
          .filter(player -> player.getPlayerId() > 0)
          .collect(Collectors.toList());
      F30051.F300519S2C.Builder builder;
      for (FindSpyPlayer player : playerList) {
        builder = F30051.F300519S2C.newBuilder();
        if (userIds.contains(player.getPlayerId())) {
          builder.addAllDoUserId(userIds);
          List<Long> excludeList = userIds.stream()
              .filter(s -> !s.equals(player.getPlayerId()))
              .collect(Collectors.toList());
          builder.addAllKillUserId(excludeList);
          builder.setVoteTime(20);
          player.sendPacket(
              new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.START_VOTE,
                  builder.build().toByteArray()));
        } else {
          builder.addAllDoUserId(userIds);
          builder.addAllKillUserId(userIds);
          builder.setVoteTime(20);
          player.sendPacket(
              new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.START_VOTE,
                  builder.build().toByteArray()));
        }
      }
      // 观战玩家推送
      builder = F30051.F300519S2C.newBuilder();
      builder.addAllDoUserId(userIds);
      builder.addAllKillUserId(userIds);
      builder.setVoteTime(20);
      if (roomInfo.getAudienceGroup() != null) {
        roomInfo.getAudienceGroup().writeAndFlush(
            new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.START_VOTE,
                builder.build().toByteArray()));
      }
      // 投票定时
      voteTimeout(roomInfo.getRoomId());
    }
  }

  /**
   * TODO 新的回合.
   *
   * @param roomId [用户ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/10 21:11
   * @update 2020/10/13 18:10
   */
  private void roundStart(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      if (roomInfo.getHandleIndex() >= roomInfo.getMaxRoomRound()) {
        // 游戏结束 平民胜利
        roomInfo.destroy();
        if (roomInfo.getRoomRound() == 1) {
          List<FindSpyPlayer> findSpyPlayers = roomInfo.getPlayerList().stream()
              .filter(s -> s.getGameIdentity() == 1 && s.getVoteIndexSpy() == 1)
              .collect(Collectors.toList());
          if (CollectionUtils.isNotEmpty(findSpyPlayers)) {
            findSpyPlayers.forEach(player -> {
              // 玩家成就.利刃出鞘
              Map<String, Object> taskSuc0027 = Maps.newHashMap();
              taskSuc0027.put("userId", player.getPlayerId());
              taskSuc0027.put("code", AchievementEnum.AMT0027.getCode());
              taskSuc0027.put("progress", 1);
              taskSuc0027.put("isReset", 0);
              this.userRemote.achievementHandlers(taskSuc0027);
            });
          }
        }
        List<FindSpyPlayer> findSpyPlayers = roomInfo.getPlayerList().stream()
            .filter(player -> player.getPlayerId() > 0)
            .collect(Collectors.toList());
        findSpyPlayers.forEach(player -> {
          // 每日任务.玩1局谁是卧底
          Map<String, Object> taskInfo = Maps.newHashMap();
          taskInfo.put("userId", player.getPlayerId());
          taskInfo.put("code", TaskEnum.PGT0007.getCode());
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
        });
        if (roomInfo.getRoomRound() >= 3) {
          findSpyPlayers.stream().filter(player -> player.getPlayerLiveOn() >= 3)
              .forEach(player -> {
                // 每日任务.在谁是卧底中.存活3轮及以上
                Map<String, Object> taskInfo = Maps.newHashMap();
                taskInfo.put("userId", player.getPlayerId());
                taskInfo.put("code", TaskEnum.PGT0023.getCode());
                taskInfo.put("progress", 1);
                taskInfo.put("isReset", 0);
                this.userRemote.taskHandler(taskInfo);
              });
        }
        // 法官消息
        sendMessage(200, roomId, null, null);
        pushSettlement(roomId);
        F30051.F3005120S2C.Builder clearList = F30051.F3005120S2C.newBuilder();
        if (CollectionUtils.isNotEmpty(roomInfo.offlinePlayers())) {
          clearList.addAllUserID(roomInfo.offlinePlayers());
          GroupManager
              .sendPacketToGroup(new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.JOIN_WATCH,
                  clearList.build().toByteArray()), roomInfo.getRoomId());
        }
        // 初始游戏
        roomInfo.initRoomInfo();
        if (CollectionUtils.isNotEmpty(roomInfo.seatedPlayers())) {
          List<Long> seatedPlayers = roomInfo.seatedPlayers();
          seatedPlayers.forEach(playerId -> initTimeout(roomId, playerId));
          // 法官消息
          sendMessage(23, roomId, null, null);
          boolean testRoom = (roomId == FindSpyAssets.getLong(FindSpyAssets.TEST_ID));
          if (!testRoom) {
            if (roomInfo.getOpenWay() == 0) {
              if (roomInfo.remainingSeat() > 0) {
                refreshSpyMath(roomId);
              }
            }
          }
        } else {
          if (roomInfo.seatedPlayers().size() == 0 && roomInfo.getAudienceList().size() == 0) {
            clearData(roomInfo.getRoomId());
          } else {
            // 清理检测
            List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
            int playGameSize = (int) playerList.stream()
                .filter(s -> s.getPlayerId() > 0).count();
            if (playGameSize == 0) {
              clearTimeout(roomInfo.getRoomId());
            }
          }
        }
      } else {
        roomInfo.setBattleIndex(0);
        roomInfo.setOpenWordsUser(0L);
        roomInfo.removeTimeOut(FindSpyCmd.KILL_INDEX);
        int spyNum = roomInfo.spyNum();
        int civiliansNum = roomInfo.civiliansNum();
        // 0-[4-6人>1卧底] 1-[7-8人>2卧底].
        if (civiliansNum == spyNum) {
          // 游戏结算 卧底胜利
          roomInfo.destroy();
          List<FindSpyPlayer> findSpyPlayers = roomInfo.getPlayerList().stream()
              .filter(player -> player.getPlayerId() > 0)
              .collect(Collectors.toList());
          findSpyPlayers.forEach(player -> {
            // 每日任务.玩1局谁是卧底
            Map<String, Object> taskInfo = Maps.newHashMap();
            taskInfo.put("userId", player.getPlayerId());
            taskInfo.put("code", TaskEnum.PGT0007.getCode());
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
          });
          if (roomInfo.getRoomRound() >= 3) {
            findSpyPlayers.stream().filter(player -> player.getPlayerLiveOn() >= 3)
                .forEach(player -> {
                  // 每日任务.在谁是卧底中.存活3轮及以上
                  Map<String, Object> taskInfo = Maps.newHashMap();
                  taskInfo.put("userId", player.getPlayerId());
                  taskInfo.put("code", TaskEnum.PGT0023.getCode());
                  taskInfo.put("progress", 1);
                  taskInfo.put("isReset", 0);
                  this.userRemote.taskHandler(taskInfo);
                });
          }
          List<FindSpyPlayer> spyPartakerList = findSpyPlayers.stream()
              .filter(s -> s.getGameIdentity() == 2).collect(Collectors.toList());
          // 房间类型 0「4-6人»1卧底」 1「7-8人»2卧底」.
          if (roomInfo.getRoomType() == 0) {
            FindSpyPlayer findSpyPlayer = spyPartakerList.get(0);
            // 玩家成就.无间风云
            Map<String, Object> taskSuc0025 = Maps.newHashMap();
            taskSuc0025.put("userId", findSpyPlayer.getPlayerId());
            taskSuc0025.put("code", AchievementEnum.AMT0025.getCode());
            taskSuc0025.put("progress", 1);
            taskSuc0025.put("isReset", 0);
            this.userRemote.achievementHandlers(taskSuc0025);
          } else {
            spyPartakerList.forEach(player -> {
              // 玩家成就.无间风云
              Map<String, Object> taskSuc0025 = Maps.newHashMap();
              taskSuc0025.put("userId", player.getPlayerId());
              taskSuc0025.put("code", AchievementEnum.AMT0025.getCode());
              taskSuc0025.put("progress", 1);
              taskSuc0025.put("isReset", 0);
              this.userRemote.achievementHandlers(taskSuc0025);
            });
          }
          // 法官消息
          sendMessage(200, roomId, null, null);
          // 推送结算
          F30051.F3005115S2C.Builder builderWin = F30051.F3005115S2C.newBuilder();
          F30051.SettlementPlayerInfo.Builder settlement;
          for (FindSpyPlayer playerInfo : findSpyPlayers) {
            settlement = F30051.SettlementPlayerInfo.newBuilder();
            settlement.setNick(playerInfo.getPlayerName());
            settlement.setUrl(playerInfo.getPlayerAvatar());
            settlement.setSex(playerInfo.getPlayerSex());
            if (playerInfo.getGameIdentity() == 2) {
              if (MemManager.isExists(playerInfo.getPlayerId())) {
                settlement.setScore(gainExperience(playerInfo.getPlayerId(), 8));
              } else {
                settlement.setScore(gainExperience(playerInfo.getPlayerId(), 4));
              }
              settlement.setIsUndercover(0);
            } else {
              if (MemManager.isExists(playerInfo.getPlayerId())) {
                settlement.setIsDouble(1);
              }
              settlement.setScore(0);
              settlement.setIsUndercover(1);
            }
            // 爆词描述
            if (StringUtils.isEmpty(playerInfo.getOpenWords())) {
              settlement.setIsBlast(1);
            } else {
              settlement.setIsBlast(0);
            }
            builderWin.addPlayerInfo(settlement);
          }
          builderWin.setIsUncoverWin(0);
          builderWin.setCivilianWord(roomInfo.getRoomLexicon().getLexiconMass());
          builderWin.setUniCoverWord(roomInfo.getRoomLexicon().getLexiconSpy());
          GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_WHO_IS_SPY,
              FindSpyCmd.GAME_SETTLE, builderWin.build().toByteArray()), roomInfo.getRoomId());
          F30051.F3005120S2C.Builder clearList = F30051.F3005120S2C.newBuilder();
          if (CollectionUtils.isNotEmpty(roomInfo.offlinePlayers())) {
            clearList.addAllUserID(roomInfo.offlinePlayers());
            GroupManager
                .sendPacketToGroup(new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.JOIN_WATCH,
                    clearList.build().toByteArray()), roomInfo.getRoomId());
          }
          // 初始游戏
          roomInfo.initRoomInfo();
          if (CollectionUtils.isNotEmpty(roomInfo.seatedPlayers())) {
            List<Long> seatedPlayers = roomInfo.seatedPlayers();
            seatedPlayers.forEach(playerId -> initTimeout(roomId, playerId));
            // 法官消息
            sendMessage(23, roomId, null, null);
            boolean testRoom = (roomId == FindSpyAssets.getLong(FindSpyAssets.TEST_ID));
            if (!testRoom) {
              if (roomInfo.getOpenWay() == 0) {
                if (roomInfo.remainingSeat() > 0) {
                  refreshSpyMath(roomId);
                }
              }
            }
          } else {
            if (roomInfo.seatedPlayers().size() == 0 && roomInfo.getAudienceList().size() == 0) {
              clearData(roomInfo.getRoomId());
            } else {
              // 清理检测
              List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
              int playGameSize = (int) playerList.stream()
                  .filter(s -> s.getPlayerId() > 0).count();
              if (playGameSize == 0) {
                clearTimeout(roomInfo.getRoomId());
              }
            }
          }
        } else {
          // 下一回合
          roomInfo.roundInit();
          // 刷新回合
          roomInfo.refreshRounds();
          // 法官消息
          sendMessage(6, roomId, null, roomInfo.getRoomRound());
          F30051.F300512S2C.Builder builder = F30051.F300512S2C.newBuilder();
          builder.setRotationNum(roomInfo.getRoomRound());
          GroupManager
              .sendPacketToGroup(new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.ROUND_START,
                  builder.build().toByteArray()), roomInfo.getRoomId());
          // 延时2s
          speakOrWordsTimeout(roomInfo.getRoomId());
        }
      }
    }
  }

  /**
   * TODO 投票结果.
   *
   * @param roomId [房间ID]
   * @param userId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/10 21:11
   * @update 2020/10/13 18:10
   */
  private void voteResults(Long roomId, Long userId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      FindSpyPlayer player = roomInfo.getPlayerInfo(userId);
      // 游戏身份〖1.平民 2.卧底〗.
      if (player.getGameIdentity() == 1) {
        // 玩家成就.躺枪群众
        Map<String, Object> taskSuc0026 = Maps.newHashMap();
        taskSuc0026.put("userId", player.getPlayerId());
        taskSuc0026.put("code", AchievementEnum.AMT0026.getCode());
        taskSuc0026.put("progress", 1);
        taskSuc0026.put("isReset", 0);
        this.userRemote.achievementHandlers(taskSuc0026);
        // 法官消息
        sendMessage(7, roomId, null, player.getSeatNumber());
        F30051.F3005112S2C.Builder builder = F30051.F3005112S2C.newBuilder();
        builder.setOutPlayerUserID(player.getPlayerId());
        builder.setPlayerIdentity(1);
        builder.setIsBlastWord(false);
        GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_WHO_IS_SPY,
            FindSpyCmd.VOTE_RESULTS, builder.build().toByteArray()), roomInfo.getRoomId());
        if (player.getPlayerStatus() == 4) {
          F30051.F3005116S2C.Builder logout = F30051.F3005116S2C.newBuilder();
          logout.setResult(0).setUserID(userId);
          GroupManager
              .sendPacketToGroup(new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_EXIT,
                  logout.build().toByteArray()), roomInfo.getRoomId());
        } else {
          // 玩家出局
          player.setPlayerStatus(3);
          player.setPlayerIndex(1);
        }
        // 下一个回合
        killTimeout(roomInfo.getRoomId());
      } else {
        // 投票玩家
        if (roomInfo.getRoomRound() == 1) {
          List<FindSpyVote> voteList = roomInfo.getVoteList();
          if (CollectionUtils.isNotEmpty(voteList)) {
            FindSpyVote spyPlayer = voteList.stream()
                .filter(s -> s.getUserId().equals(userId))
                .findAny().orElse(voteList.get(voteList.size() - 1));
            List<Long> playerList = spyPlayer.getUserIds();
            for (Long voteUser : playerList) {
              FindSpyPlayer playerInfo = roomInfo.getPlayerInfo(voteUser);
              // 作为平民在第一回合投出卧底
              if (playerInfo.getGameIdentity() == 1) {
                playerInfo.setVoteIndexSpy(1);
              }
            }
          }
        }
        roomInfo.findOutSpy(player.getPlayerId());
        if (roomInfo.getRoomRound() <= roomInfo.getOpenRound()) {
          // 法官消息
          sendMessage(8, roomId, null, player.getSeatNumber());
          F30051.F3005112S2C.Builder builder = F30051.F3005112S2C.newBuilder();
          builder.setOutPlayerUserID(player.getPlayerId());
          builder.setPlayerIdentity(2);
          builder.setIsBlastWord(true);
          builder.setSelectTime(5);
          GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_WHO_IS_SPY,
              FindSpyCmd.VOTE_RESULTS, builder.build().toByteArray()), roomInfo.getRoomId());
          // 选择定时
          selectTimeout(roomInfo.getRoomId(), player.getPlayerId());
        } else {
          // 法官消息
          sendMessage(22, roomId, null, player.getSeatNumber());
          speakOpenWordsFailed(roomId);
        }
      }
    }
  }

  /**
   * TODO 平票结果.
   *
   * @param roomId [房间ID]
   * @param userIds [玩家列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/10 21:11
   * @update 2020/9/3 21:10
   */
  private void drawVote(Long roomId, List<Long> userIds) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      // 法官消息
      sendMessage(9, roomId, null, null);
      F30051.F3005111S2C.Builder builder = F30051.F3005111S2C.newBuilder();
      builder.addAllUserID(userIds);
      for (Long userId : userIds) {
        FindSpyPlayer player = roomInfo.getPlayerInfo(userId);
        builder.addIconUrl(player.getPlayerAvatar());
      }
      GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.VOTE_PK,
          builder.build().toByteArray()), roomInfo.getRoomId());
      if (roomInfo.getSpeakMode() == 0) {
        // 开始描述
        startWordsByBattle(roomId, userIds);
      } else {
        // 开始说话
        startSpeakByBattle(roomId, userIds);
      }
    }
  }

  /**
   * TODO 平票描述.
   *
   * @param roomId [房间ID]
   * @param userIds [玩家列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/16 21:07
   */
  private void startWordsByBattle(Long roomId, List<Long> userIds) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      F30051.F300516S2C.Builder builder = F30051.F300516S2C.newBuilder();
      for (Long userId : userIds) {
        builder.addPlayerUser(userId);
      }
      builder.setSpeakTime(20);
      GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.START_WORDS,
          builder.build().toByteArray()), roomInfo.getRoomId());
      // 描述定时
      wordsTimeoutByBattle(roomId, userIds);
      // 清空之前描述
      userIds.forEach(playerId -> {
        FindSpyPlayer player = roomInfo.getPlayerInfo(playerId);
        player.setSpeakWords(null);
      });
    }
  }

  /**
   * TODO 平票说话.
   *
   * @param roomId [房间ID]
   * @param userIds [玩家列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/16 21:07
   */
  private void startSpeakByBattle(Long roomId, List<Long> userIds) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      F30051.F300518S2C.Builder builder = F30051.F300518S2C.newBuilder();
      FindSpyPlayer nowPlayer = roomInfo.getPlayerInfo(userIds.get(0));
      // 法官消息
      sendMessage(10, roomId, null, nowPlayer.getSeatNumber());
      builder.setSpeakPlayer(nowPlayer.getSeatNumber());

//      FindSpyPlayer nextPlayer = roomInfo.getPlayerInfo(userIds.get(1));
//      builder.setNextSpeakPlayer(nextPlayer.getSeatNumber());
      builder.setSpeakTime(20);
      GroupManager.sendPacketToGroup(
          new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.START_SPEAK,
              builder.build().toByteArray()), roomInfo.getRoomId());
      // 说话定时
      speakTimeoutByBattle(roomId, userIds);
    }
  }

  /**
   * TODO 游戏经验.
   *
   * @param playerId [玩家ID]
   * @param exp [游戏经验]
   * @return [游戏经验]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/16 21:07
   * @update 2020/9/30 18:11
   */
  private int gainExperience(Long playerId, Integer exp) {
    Integer idLength = StringUtils.nvl(playerId).length();
    boolean checkUserId = idLength >= FindSpyAssets.getInt(FindSpyAssets.PEOPLE_MAX_2) + 1;
    if (checkUserId) {
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
   * TODO 准备定时. 15(s)
   *
   * @param roomId [房间ID]
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/17 19:19
   * @update 2020/8/17 19:19
   */
  private void readyTimeout(Long roomId, Long playerId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey(playerId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> readyExamine(roomId, playerId)
              ), 15, TimeUnit.SECONDS);
          roomInfo.addTimeOut(playerId.intValue(), timeout);
          FindSpyPlayer player = roomInfo.getPlayerInfo(playerId);
          player.setReadyTime(LocalDateTime.now());
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
   * @create 2020/10/14 13:54
   * @update 2020/10/14 13:54
   */
  private void initTimeout(Long roomId, Long playerId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey(playerId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> readyExamine(roomId, playerId)
              ), 18, TimeUnit.SECONDS);
          roomInfo.addTimeOut(playerId.intValue(), timeout);
          FindSpyPlayer player = roomInfo.getPlayerInfo(playerId);
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
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/17 19:39
   * @update 2020/8/17 19:39
   */
  private void readyExamine(Long roomId, Long playerId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        roomInfo.removeTimeOut(playerId.intValue());
        FindSpyPlayer player = roomInfo.getPlayerInfo(playerId);
        int seat = roomInfo.leaveSeat(player.getPlayerId());
        F30051.F3005119S2C.Builder builder = F30051.F3005119S2C.newBuilder();
        builder.setResult(0).setStand(0).setSeatNo(seat);
        GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.JOIN_LEAVE,
            builder.build().toByteArray()), roomInfo.getRoomId());
        // 法官消息
        sendMessage(11, roomId, player.getPlayerName(), null);
        // 开始校验
        int unReady = roomInfo.unprepared();
        int isReady = roomInfo.preparations();
        if (unReady == 0 && roomInfo.getRoomType() == 0 && isReady >= FindSpyAssets
            .getInt(FindSpyAssets.PEOPLE_MIN_1)) {
          // 法官消息
          sendMessage(1, roomInfo.getRoomId(), null, null);
          startTimeout(roomInfo.getRoomId());
        } else if (unReady == 0 && roomInfo.getRoomType() == 1 && isReady >= FindSpyAssets
            .getInt(FindSpyAssets.PEOPLE_MIN_2)) {
          // 法官消息
          sendMessage(1, roomInfo.getRoomId(), null, null);
          startTimeout(roomInfo.getRoomId());
        } else {
          // 清理检测
          List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
          int playGameSize = (int) playerList.stream()
              .filter(s -> s.getPlayerId() > 0).count();
          if (playGameSize == 0) {
            clearTimeout(roomInfo.getRoomId());
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
   * @create 2020/7/15 21:14
   * @update 2020/9/23 20:57
   */
  private void startTimeout(Long roomId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.START_CHECK)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> startExamine(roomId)
              ), 3, TimeUnit.SECONDS);
          roomInfo.addTimeOut(FindSpyCmd.START_CHECK, timeout);
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
   * @create 2020/7/10 21:11
   * @update 2020/10/13 18:10
   */
  private void startExamine(Long roomId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        roomInfo.removeTimeOut(FindSpyCmd.START_CHECK);
        cancelSpyMatch(roomId, roomInfo.getRoomType(), roomInfo.getSpeakMode());
        int unReady = roomInfo.unprepared();
        int isReady = roomInfo.preparations();
        boolean start = false;
        if (unReady == 0 && roomInfo.getRoomType() == 0
            && isReady >= FindSpyAssets.getInt(FindSpyAssets.PEOPLE_MIN_1)) {
          start = true;
        } else if (unReady == 0 && roomInfo.getRoomType() == 1
            && isReady >= FindSpyAssets.getInt(FindSpyAssets.PEOPLE_MIN_2)) {
          start = true;
        }
        if (start) {
          // 房间回合
          roomInfo.refreshRounds();
          // 最大回合
          roomInfo.setMaxGameRound();
          // 房间状态
          roomInfo.refreshRoomStatus(1);
          // 玩家状态
          roomInfo.refreshPlayerStatus();
          // 开始游戏
          F30051.F300512S2C.Builder builder = F30051.F300512S2C.newBuilder();
          builder.setRotationNum(roomInfo.getRoomRound());
          GroupManager
              .sendPacketToGroup(new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.ROUND_START,
                  builder.build().toByteArray()), roomInfo.getRoomId());
          // 法官消息
          sendMessage(12, roomId, null, null);
          roomInfo.assignIdentity();
          if (roomInfo.getSpeakMode() == 0) {
            beginTimeout(roomId);
            pushWords(roomId);
          } else {
            pushWords(roomId);
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 首回定时. 40(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/9/23 20:57
   */
  private void beginTimeout(Long roomId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.ROUND_START)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> beginExamine(roomId)
              ), 40, TimeUnit.SECONDS);
          roomInfo.addTimeOut(FindSpyCmd.ROUND_START, timeout);
          roomInfo.setRoundTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 首回检测. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/10/13 18:10
   */
  private void beginExamine(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(FindSpyCmd.ROUND_START);
      if (roomInfo.getChangeIndex() == 1) {
        roomInfo.setChangeIndex(2);
        int agreeNum = roomInfo.agreeChangeWordsNum();
        int playNum = roomInfo.seatedPlayers().size();
        if (roomInfo.getRoomType() == 0) {
          if (playNum == FindSpyAssets.getInt(FindSpyAssets.PEOPLE_MIN_1)) {
            if (agreeNum > 2) {
              // 换词成功
              changeSuccess(roomInfo.getRoomId());
            } else {
              // 换词失败
              changeFailed(roomInfo.getRoomId());
            }
          } else {
            if (agreeNum > 3) {
              // 换词成功
              changeSuccess(roomInfo.getRoomId());
            } else {
              // 换词失败
              changeFailed(roomInfo.getRoomId());
            }
          }
        } else {
          if (playNum == FindSpyAssets.getInt(FindSpyAssets.PEOPLE_MIN_2)) {
            if (agreeNum >= 4) {
              // 换词成功
              changeSuccess(roomInfo.getRoomId());
            } else {
              // 换词失败
              changeFailed(roomInfo.getRoomId());
            }
          } else {
            if (agreeNum > 4) {
              // 换词成功
              changeSuccess(roomInfo.getRoomId());
            } else {
              // 换词失败
              changeFailed(roomInfo.getRoomId());
            }
          }
        }
        return;
      }
      roomInfo.completionDesc();
      startVote(roomInfo.getRoomId());
    }
  }

  /**
   * TODO 回合定时. 40(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/9/23 20:57
   */
  private void roundTimeout(Long roomId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.ROUND_START)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> roundExamine(roomId)
              ), 40, TimeUnit.SECONDS);
          roomInfo.addTimeOut(FindSpyCmd.ROUND_START, timeout);
          roomInfo.setRoundTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 回合检测. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/9/1 21:09
   */
  private void roundExamine(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(FindSpyCmd.ROUND_START);
      // 补全描述
      roomInfo.completionDesc();
      // 开始投票
      startVote(roomInfo.getRoomId());
    }
  }

  /**
   * TODO 词汇定时. 5(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/9/23 20:57
   */
  private void wordsTimeout(Long roomId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.PUSH_WORDS)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> startDescribe(roomId)
              ), 5, TimeUnit.SECONDS);
          roomInfo.addTimeOut(FindSpyCmd.PUSH_WORDS, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 开始描述. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/9/1 21:09
   */
  private void startDescribe(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(FindSpyCmd.PUSH_WORDS);
      if (roomInfo.getSpeakMode() == 0) {
        // 开始描述
        startWords(roomInfo.getRoomId());
      } else {
        // 开始说话
        startSpeak(roomInfo.getRoomId());
      }
    }
  }

  /**
   * TODO 特殊定时. 20(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/9/23 20:57
   */
  private void specialTimeout(Long roomId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.START_WORDS)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> specialExamine(roomId)
              ), 20, TimeUnit.SECONDS);
          roomInfo.addTimeOut(FindSpyCmd.START_WORDS, timeout);
          roomInfo.setSpecialTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 特殊检测. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/9/1 21:09
   */
  private void specialExamine(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(FindSpyCmd.START_WORDS);
      // 补全描述
      roomInfo.completionDesc();
      // 开始投票
      startVote(roomInfo.getRoomId());
    }
  }

  /**
   * TODO 正常说话. 20(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/14 21:23
   * @update 2020/9/23 20:57
   */
  private void normalSpeakTimeout(Long roomId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.START_SPEAK)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> normalSpeakExamine(roomId)
              ), 20, TimeUnit.SECONDS);
          roomInfo.addTimeOut(FindSpyCmd.START_SPEAK, timeout);
          roomInfo.setSpecialTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 说话检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/1 21:09
   */
  private void normalSpeakExamine(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(FindSpyCmd.START_SPEAK);
      // 下一个玩家说话
      startSpeak(roomInfo.getRoomId());
    }
  }

  /**
   * TODO 说话检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 14:04
   * @update 2020/10/14 14:04
   */
  private void lastSpeakExamine(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(FindSpyCmd.START_SPEAK);
      startVote(roomInfo.getRoomId());
    }
  }

  /**
   * TODO 投票定时. 20(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/23 20:57
   */
  private void voteTimeout(Long roomId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.START_VOTE)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> voteExamine(roomId)
              ), 20, TimeUnit.SECONDS);
          roomInfo.addTimeOut(FindSpyCmd.START_VOTE, timeout);
          roomInfo.setSpecialTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 投票检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/10/13 18:10
   */
  private void voteExamine(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(FindSpyCmd.START_VOTE);
      // 开始检验投票
      List<Long> voteList = roomInfo.voteCalculation();
      int voteSize = voteList.size();
      if (voteSize == 1) {
        // 投出一个玩家
        voteResults(roomInfo.getRoomId(), voteList.get(0));
      } else if (voteSize == 2) {
        if (roomInfo.getBattleIndex() == 0) {
          // PK标记
          roomInfo.setBattleIndex(1);
          // 初始描述 平票战斗
          roomInfo.initSpeakWords();
          // 开始PK
          drawVote(roomInfo.getRoomId(), voteList);
        } else {
          // 新的一轮
          roundStart(roomInfo.getRoomId());
        }
      } else {
        //不满足上述任何一个条件，进行下一轮描述
        roundStart(roomInfo.getRoomId());
      }
    }
  }

  /**
   * TODO 爆词定时. 20(s)
   *
   * @param roomId [房间ID]
   * @param userId [爆词玩家]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/23 20:57
   */
  private void openWordsTimeOut(Long roomId, Long userId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.OPEN_WORDS)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> openWordsExamine(roomId, userId)
              ), 20, TimeUnit.SECONDS);
          roomInfo.addTimeOut(FindSpyCmd.OPEN_WORDS, timeout);
          roomInfo.setSpecialTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 爆词检验. ◕
   *
   * @param roomId [房间ID]
   * @param playerId [爆词玩家]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/10/13 18:10
   */
  private void openWordsExamine(Long roomId, Long playerId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(FindSpyCmd.OPEN_WORDS);
      FindSpyPlayer playerInfo = roomInfo.getPlayerInfo(playerId);
      // 法官消息
      sendMessage(19, roomId, null, playerInfo.getSeatNumber());
      if (roomInfo.getSpyPlayerList().size() > 0) {
        // 下一回合
        roundStart(roomInfo.getRoomId());
      } else {
        // 平民胜利
        roomInfo.destroy();
        if (roomInfo.getRoomRound() == 1) {
          List<FindSpyPlayer> findSpyPlayers = roomInfo.getPlayerList().stream()
              .filter(s -> s.getPlayerId() > 0 && s.getGameIdentity() == 1)
              .collect(Collectors.toList());
          if (CollectionUtils.isNotEmpty(findSpyPlayers)) {
            findSpyPlayers.stream().filter(player -> player.getVoteIndexSpy() == 1)
                .forEach(player -> {
                  // 玩家成就.利刃出鞘
                  Map<String, Object> taskSuc0027 = Maps.newHashMap();
                  taskSuc0027.put("userId", player.getPlayerId());
                  taskSuc0027.put("code", AchievementEnum.AMT0027.getCode());
                  taskSuc0027.put("progress", 1);
                  taskSuc0027.put("isReset", 0);
                  this.userRemote.achievementHandlers(taskSuc0027);
                });
          }
        }
        List<FindSpyPlayer> findSpyPlayers = roomInfo.getPlayerList().stream()
            .filter(s -> s.getPlayerId() > 0).collect(Collectors.toList());
        findSpyPlayers.forEach(player -> {
          // 每日任务.玩1局谁是卧底
          Map<String, Object> taskInfo = Maps.newHashMap();
          taskInfo.put("userId", player.getPlayerId());
          taskInfo.put("code", TaskEnum.PGT0007.getCode());
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
        });
        if (roomInfo.getRoomRound() >= 3) {
          findSpyPlayers.stream().filter(player -> player.getPlayerLiveOn() >= 3)
              .forEach(player -> {
                // 每日任务.在谁是卧底中.存活3轮及以上
                Map<String, Object> taskInfo = Maps.newHashMap();
                taskInfo.put("userId", player.getPlayerId());
                taskInfo.put("code", TaskEnum.PGT0023.getCode());
                taskInfo.put("progress", 1);
                taskInfo.put("isReset", 0);
                this.userRemote.taskHandler(taskInfo);
              });
        }
        // 法官消息
        sendMessage(200, roomId, null, null);
        // 推送结算
        pushSettlement(roomId);
        F30051.F3005120S2C.Builder clearList = F30051.F3005120S2C.newBuilder();
        if (CollectionUtils.isNotEmpty(roomInfo.offlinePlayers())) {
          clearList.addAllUserID(roomInfo.offlinePlayers());
          GroupManager
              .sendPacketToGroup(new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.JOIN_WATCH,
                  clearList.build().toByteArray()), roomInfo.getRoomId());
        }
        // 初始游戏
        roomInfo.initRoomInfo();
        if (CollectionUtils.isNotEmpty(roomInfo.seatedPlayers())) {
          List<Long> seatedPlayers = roomInfo.seatedPlayers();
          seatedPlayers.forEach(s -> initTimeout(roomId, s));
          // 法官消息
          sendMessage(23, roomId, null, null);
          boolean testRoom = (roomId == FindSpyAssets.getLong(FindSpyAssets.TEST_ID));
          if (!testRoom) {
            if (roomInfo.getOpenWay() == 0) {
              if (roomInfo.remainingSeat() > 0) {
                refreshSpyMath(roomId);
              }
            }
          }
        } else {
          if (roomInfo.seatedPlayers().size() == 0 && roomInfo.getAudienceList().size() == 0) {
            clearData(roomInfo.getRoomId());
          } else {
            // 清理检测
            List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
            int playGameSize = (int) playerList.stream()
                .filter(s -> s.getPlayerId() > 0).count();
            if (playGameSize == 0) {
              clearTimeout(roomInfo.getRoomId());
            }
          }
        }
      }
    }
  }

  /**
   * TODO 选择定时. 5(s)
   *
   * @param roomId [房间ID]
   * @param userId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/23 20:57
   */
  private void selectTimeout(Long roomId, Long userId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.SELECT_CHECK)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> selectExamine(roomId, userId)
              ), 5, TimeUnit.SECONDS);
          roomInfo.addTimeOut(FindSpyCmd.SELECT_CHECK, timeout);
          roomInfo.setSpecialTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 选择超时. ◕
   *
   * @param roomId [房间ID]
   * @param userId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/10/13 18:10
   */
  private void selectExamine(Long roomId, Long userId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(FindSpyCmd.SELECT_CHECK);
      FindSpyPlayer playerInfo = roomInfo.getPlayerInfo(userId);
      // 法官消息
      sendMessage(15, roomId, null, playerInfo.getSeatNumber());
      if (roomInfo.getSpyPlayerList().size() > 0) {
        if (playerInfo.getPlayerStatus() == 4) {
          F30051.F3005116S2C.Builder logout = F30051.F3005116S2C.newBuilder();
          logout.setResult(0).setUserID(userId);
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.PLAYER_EXIT,
                  logout.build().toByteArray()), roomInfo.getRoomId());
        } else {
          // 玩家出局
          playerInfo.setPlayerStatus(3);
          playerInfo.setPlayerIndex(1);
        }
        // 下一回合
        roundStart(roomInfo.getRoomId());
      } else {
        // 平民胜利
        roomInfo.destroy();
        if (roomInfo.getRoomRound() == 1) {
          List<FindSpyPlayer> findSpyPlayers = roomInfo.getPlayerList().stream()
              .filter(s -> s.getPlayerId() > 0 && s.getGameIdentity() == 1)
              .collect(Collectors.toList());
          if (CollectionUtils.isNotEmpty(findSpyPlayers)) {
            findSpyPlayers.stream().filter(player -> player.getVoteIndexSpy() == 1)
                .forEach(player -> {
                  // 玩家成就.利刃出鞘
                  Map<String, Object> taskSuc0027 = Maps.newHashMap();
                  taskSuc0027.put("userId", player.getPlayerId());
                  taskSuc0027.put("code", AchievementEnum.AMT0027.getCode());
                  taskSuc0027.put("progress", 1);
                  taskSuc0027.put("isReset", 0);
                  this.userRemote.achievementHandlers(taskSuc0027);
                });
          }
        }
        List<FindSpyPlayer> findSpyPlayers = roomInfo.getPlayerList().stream()
            .filter(s -> s.getPlayerId() > 0).collect(Collectors.toList());
        findSpyPlayers.forEach(player -> {
          // 每日任务.玩1局谁是卧底
          Map<String, Object> taskInfo = Maps.newHashMap();
          taskInfo.put("userId", player.getPlayerId());
          taskInfo.put("code", TaskEnum.PGT0007.getCode());
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
        });
        if (roomInfo.getRoomRound() >= 3) {
          findSpyPlayers.stream().filter(player -> player.getPlayerLiveOn() >= 3)
              .forEach(player -> {
                // 每日任务.在谁是卧底中.存活3轮及以上
                Map<String, Object> taskInfo = Maps.newHashMap();
                taskInfo.put("userId", player.getPlayerId());
                taskInfo.put("code", TaskEnum.PGT0023.getCode());
                taskInfo.put("progress", 1);
                taskInfo.put("isReset", 0);
                this.userRemote.taskHandler(taskInfo);
              });
        }
        // 法官消息
        sendMessage(200, roomId, null, null);
        // 推送结算
        pushSettlement(roomId);
        F30051.F3005120S2C.Builder clearList = F30051.F3005120S2C.newBuilder();
        if (CollectionUtils.isNotEmpty(roomInfo.offlinePlayers())) {
          clearList.addAllUserID(roomInfo.offlinePlayers());
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.JOIN_WATCH,
                  clearList.build().toByteArray()), roomInfo.getRoomId());
        }
        // 初始游戏
        roomInfo.initRoomInfo();
        if (CollectionUtils.isNotEmpty(roomInfo.seatedPlayers())) {
          List<Long> seatedPlayers = roomInfo.seatedPlayers();
          seatedPlayers.forEach(playerId -> initTimeout(roomId, playerId));
          sendMessage(23, roomId, null, null);
          boolean testRoom = (roomId == FindSpyAssets.getLong(FindSpyAssets.TEST_ID));
          if (!testRoom) {
            if (roomInfo.getOpenWay() == 0) {
              if (roomInfo.remainingSeat() > 0) {
                refreshSpyMath(roomId);
              }
            }
          }
        } else {
          if (roomInfo.seatedPlayers().size() == 0 && roomInfo.getAudienceList().size() == 0) {
            clearData(roomInfo.getRoomId());
          } else {
            // 清理检测
            List<FindSpyPlayer> playerList = roomInfo.getPlayerList();
            int playGameSize = (int) playerList.stream()
                .filter(s -> s.getPlayerId() > 0).count();
            if (playGameSize == 0) {
              clearTimeout(roomInfo.getRoomId());
            }
          }
        }
      }
    }
  }

  /**
   * TODO 推送结算. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/16 21:07
   */
  private void pushSettlement(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      // 推送结算
      F30051.F3005115S2C.Builder builderWin = F30051.F3005115S2C.newBuilder();
      List<FindSpyPlayer> playerList = roomInfo.getPlayerList()
          .stream()
          .filter(player -> player.getPlayerId() > 0)
          .collect(Collectors.toList());
      F30051.SettlementPlayerInfo.Builder settlement;
      for (FindSpyPlayer player : playerList) {
        settlement = F30051.SettlementPlayerInfo.newBuilder();
        settlement.setNick(player.getPlayerName());
        settlement.setUrl(player.getPlayerAvatar());
        settlement.setSex(player.getPlayerSex());
        if (player.getGameIdentity() == 1) {
          if (MemManager.isExists(player.getPlayerId())) {
            settlement.setScore(gainExperience(player.getPlayerId(), 8));
            settlement.setIsDouble(1);
          } else {
            settlement.setScore(gainExperience(player.getPlayerId(), 4));
          }
          settlement.setIsUndercover(1);
        } else {
          if (MemManager.isExists(player.getPlayerId())) {
            settlement.setIsDouble(1);
          }
          settlement.setScore(0);
          settlement.setIsUndercover(0);
        }
        if (StringUtils.isEmpty(player.getOpenWords())) {
          settlement.setIsBlast(1);
        } else {
          settlement.setIsBlast(0);
        }
        builderWin.addPlayerInfo(settlement);
      }
      builderWin.setIsUncoverWin(1);
      builderWin.setCivilianWord(roomInfo.getRoomLexicon().getLexiconMass());
      builderWin.setUniCoverWord(roomInfo.getRoomLexicon().getLexiconSpy());
      GroupManager.sendPacketToGroup(
          new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.GAME_SETTLE,
              builderWin.build().toByteArray()), roomInfo.getRoomId());
    }
  }

  /**
   * TODO 描述定时. 20(s)
   *
   * @param roomId [房间ID]
   * @param userIds [玩家列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/23 20:57
   */
  private void wordsTimeoutByBattle(Long roomId, List<Long> userIds) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.DRAW_CHECK)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> wordsByBattleExamine(roomId, userIds)
              ), 20, TimeUnit.SECONDS);
          roomInfo.addTimeOut(FindSpyCmd.DRAW_CHECK, timeout);
          roomInfo.setSpecialTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 描述检测. ◕
   *
   * @param roomId [房间ID]
   * @param userIds [玩家列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/16 21:07
   */
  private void wordsByBattleExamine(Long roomId, List<Long> userIds) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(FindSpyCmd.DRAW_CHECK);
      // 补全描述
      roomInfo.completionDesc();
      // 法官消息
      sendMessage(5, roomId, null, null);
      List<Long> userIdList = roomInfo.whoCanVote(userIds);
      F30051.F300519S2C.Builder builder = F30051.F300519S2C.newBuilder();
      // 可以被投票玩家
      builder.addAllKillUserId(userIds);
      // 可以投票玩家
      builder.addAllDoUserId(userIdList);
      builder.setVoteTime(20);
      GroupManager.sendPacketToGroup(
          new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.START_VOTE,
              builder.build().toByteArray()), roomInfo.getRoomId());
      // 投票定时
      voteTimeout(roomInfo.getRoomId());
    }
  }

  /**
   * TODO 说话定时. 20(s)
   *
   * @param roomId [房间ID]
   * @param userIds [玩家列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/23 20:57
   */
  private void speakTimeoutByBattle(Long roomId, List<Long> userIds) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.DRAW_CHECK)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> speakByBattleExamine(roomId, userIds)
              ), 20, TimeUnit.SECONDS);
          roomInfo.addTimeOut(FindSpyCmd.DRAW_CHECK, timeout);
          roomInfo.setSpecialTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 说话检测. ◕
   *
   * @param roomId [房间ID]
   * @param userIds [玩家列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/16 21:07
   */
  private void speakByBattleExamine(Long roomId, List<Long> userIds) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(FindSpyCmd.DRAW_CHECK);
      FindSpyPlayer nowPlayer = roomInfo.getPlayerInfo(userIds.get(1));
      // 法官消息
      sendMessage(10, roomId, null, nowPlayer.getSeatNumber());
      F30051.F300518S2C.Builder builder = F30051.F300518S2C.newBuilder();
      builder.setSpeakPlayer(nowPlayer.getSeatNumber());
//      builder.setNextSpeakPlayer(0);
      builder.setSpeakTime(20);
      GroupManager.sendPacketToGroup(
          new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.START_SPEAK,
              builder.build().toByteArray()), roomInfo.getRoomId());
      // 说话定时
      lastTimeoutByBattle(roomId, userIds);
    }
  }

  /**
   * TODO 说话定时. 20(s)
   *
   * @param roomId [房间ID]
   * @param userIds [玩家列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/23 20:57
   */
  private void lastTimeoutByBattle(Long roomId, List<Long> userIds) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.DRAW_CHECK)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> lastBattleExamine(roomId, userIds)
              ), 20, TimeUnit.SECONDS);
          roomInfo.addTimeOut(FindSpyCmd.DRAW_CHECK, timeout);
          roomInfo.setSpecialTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 平票最后. ◕
   *
   * @param roomId [房间ID]
   * @param userIds [玩家列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/16 21:07
   */
  private void lastBattleExamine(Long roomId, List<Long> userIds) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(FindSpyCmd.DRAW_CHECK);
      // 法官消息
      sendMessage(5, roomId, null, null);
      List<Long> userIdList = roomInfo.whoCanVote(userIds);
      F30051.F300519S2C.Builder builder = F30051.F300519S2C.newBuilder();
      // 可以被投票玩家
      builder.addAllKillUserId(userIds);
      // 可以投票玩家
      builder.addAllDoUserId(userIdList);
      builder.setVoteTime(20);
      GroupManager.sendPacketToGroup(
          new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.START_VOTE,
              builder.build().toByteArray()), roomInfo.getRoomId());
      // 投票定时
      voteTimeout(roomInfo.getRoomId());
    }
  }

  /**
   * TODO 击杀展示. 3(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/23 20:57
   */
  private void killTimeout(Long roomId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.KILL_INDEX)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> roundStart(roomId)
              ), 3, TimeUnit.SECONDS);
          roomInfo.addTimeOut(FindSpyCmd.KILL_INDEX, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 描述展示. 2(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/23 20:57
   */
  private void speakOrWordsTimeout(Long roomId) {
    try {
      FindSpyRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindSpyCmd.WORDS_INDEX)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> speakOrWordsExamine(roomId)
              ), 2, TimeUnit.SECONDS);
          roomInfo.addTimeOut(FindSpyCmd.WORDS_INDEX, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 新的描述. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/15 21:14
   * @update 2020/9/1 21:0
   */
  private void speakOrWordsExamine(Long roomId) {
    FindSpyRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(FindSpyCmd.WORDS_INDEX);
      if (roomInfo.getSpeakMode() == 0) {
        // 回合定时
        roundTimeout(roomInfo.getRoomId());
        //开始描述
        startWords(roomInfo.getRoomId());
      } else {
        //开始说话
        startSpeak(roomInfo.getRoomId());
      }
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
      FindSpyRoom spyRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(spyRoom)) {
        if (!spyRoom.getTimeOutMap().containsKey(roomId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> clearExamine(roomId)
              ), 10, TimeUnit.SECONDS);
          spyRoom.addTimeOut(roomId.intValue(), timeout);
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
      FindSpyRoom spyRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(spyRoom)) {
        spyRoom.removeTimeOut(roomId.intValue());
        List<FindSpyPlayer> playerList = spyRoom.getPlayerList();
        int playGameSize = (int) playerList.stream()
            .filter(player -> player.getPlayerId() > 0).count();
        LoggerManager.info("<== 30051 [谁是卧底.清除检测] DELETE: [{}] PLAY: [{}]", roomId, playGameSize);
        if (playGameSize == 0) {
          List<FindSpyPlayer> watchList = spyRoom.getAudienceList();
          if (watchList.size() > 0) {
            F20000.F200007S2C.Builder builder = F20000.F200007S2C.newBuilder();
            builder.setMsg("(oﾟvﾟ)ノ 房间已解散！");
            watchList.forEach(findSpyPlayer -> {
              SoftChannel.sendPacketToUserId(
                  new Packet(ActionCmd.APP_HEART, (short) 7,
                      builder.build().toByteArray()), findSpyPlayer.getPlayerId());
              this.redisUtils
                  .del(GameKey.KEY_GAME_JOIN_RECORD.getName() + findSpyPlayer.getPlayerId());
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
