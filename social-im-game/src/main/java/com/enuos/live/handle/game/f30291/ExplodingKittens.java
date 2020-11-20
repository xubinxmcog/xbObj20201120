package com.enuos.live.handle.game.f30291;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.codec.Packet;
import com.enuos.live.constants.GameKey;
import com.enuos.live.pojo.MatchRoom;
import com.enuos.live.manager.AchievementEnum;
import com.enuos.live.manager.ActivityEnum;
import com.enuos.live.proto.f20000msg.F20000;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.rest.ActivityRemote;
import com.enuos.live.manager.MatchManager;
import com.enuos.live.manager.GroupManager;
import com.enuos.live.utils.annotation.AbstractAction;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import com.enuos.live.manager.ChatManager;
import com.enuos.live.channel.SoftChannel;
import com.enuos.live.proto.f30291msg.F30291;
import com.enuos.live.rest.GameRemote;
import com.enuos.live.rest.OrderRemote;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.task.TimerEventLoop;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.manager.MemManager;
import com.enuos.live.utils.JsonUtils;
import com.enuos.live.utils.StringUtils;
import com.enuos.live.manager.LoggerManager;
import com.enuos.live.utils.RedisUtils;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.util.Timeout;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
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
 * TODO 炸弹猫.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/8/27 17:31
 */

@Component
@AbstractAction(cmd = ActionCmd.GAME_EXPLODING_KITTENS)
public class ExplodingKittens extends AbstractActionHandler {

  /** 房间游戏数据. */
  private static ConcurrentHashMap<Long, ExplodingKittensRoom> GAME_DATA = new ConcurrentHashMap<>();

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
   * @create 2020/8/31 15:13
   * @update 2020/8/31 15:13
   */
  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      switch (packet.child) {
        case ExplodingKittensCmd.ENTER_ROOM:
          enterRoom(channel, packet);
          break;
        case ExplodingKittensCmd.PLAYER_READY:
          playerReady(channel, packet);
          break;
        case ExplodingKittensCmd.PLAYERS_PLAY_CARDS:
          playersPlayCards(channel, packet);
          break;
        case ExplodingKittensCmd.CHOOSE_PLAYER:
          choosePlayer(channel, packet);
          break;
        case ExplodingKittensCmd.GIVE_PLAYER_CARD:
          givePlayerCard(channel, packet);
          break;
        case ExplodingKittensCmd.CHOOSE_TOUCH_CARD:
          chooseTouchCard(channel, packet);
          break;
        case ExplodingKittensCmd.JOIN_OR_WATCH:
          joinOrWatch(channel, packet);
          break;
        case ExplodingKittensCmd.AUDIENCE_INFO:
          audienceInfo(channel, packet);
          break;
        case ExplodingKittensCmd.PLAYER_LEAVES:
          playerLeaves(channel, packet);
          break;
        case ExplodingKittensCmd.PLACE_EXPLODING:
          placeExploding(channel, packet);
          break;
        default:
          LoggerManager.warn("[GAME 30291 HANDLE] CHILD ERROR: [{}]", packet.child);
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
   * @param userId 用户ID
   * @param attachId 附属ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/31 15:13
   * @update 2020/8/31 15:13
   */
  @Override
  public void shutOff(Long userId, Long attachId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(attachId);
      if (Objects.nonNull(kittensRoom)) {
        ExplodingKittensPlayer checkPlayer = kittensRoom.getPlayerInfo(userId);
        if (Objects.nonNull(checkPlayer)) {
          if (kittensRoom.getRoomStatus() == 1) {
            if (checkPlayer.getIdentity() == 0) {
              if (checkPlayer.getPlayerStatus() == ExplodingKittensAssets.getInt(ExplodingKittensAssets.PLAYER_IN_GAME)) {
                // 中途退出 扣除20金币
                gainExperience(userId, 0, -20);
              }
              checkPlayer.setPlayerStatus(4);
              checkPlayer.setLinkStatus(1);
              ChatManager.removeChannel(attachId, checkPlayer.getChannel());
              GroupManager.removeChannel(attachId, checkPlayer.getChannel());
              F30291.F3029110S2C.Builder response = F30291.F3029110S2C.newBuilder();
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYER_LEAVES,
                      response.setUserID(userId).build().toByteArray()), kittensRoom.getRoomId());
              this.gameRemote.leaveRoom(userId);
              if (kittensRoom.seatedPlayersIsDisconnected() == kittensRoom.seatedPlayersNum()
                  && kittensRoom.seatedPlayers().size() == 0 && kittensRoom.getAudienceList().size() == 0) {
                clearData(kittensRoom.getRoomId());
              }
            } else {
              kittensRoom.leaveGame(userId, 1);
              ChatManager.removeChannel(attachId, checkPlayer.getChannel());
              GroupManager.removeChannel(attachId, checkPlayer.getChannel());
              this.gameRemote.leaveRoom(userId);
              if (kittensRoom.seatedPlayersIsDisconnected() == kittensRoom.seatedPlayersNum()
                  && kittensRoom.seatedPlayers().size() == 0 && kittensRoom.getAudienceList().size() == 0) {
                clearData(kittensRoom.getRoomId());
              }
            }
          } else {
            if (checkPlayer.getIdentity() == 0) {
              if (checkPlayer.getPlayerStatus() == 0) {
                kittensRoom.cancelTimeOut(userId.intValue());
              } else if (checkPlayer.getPlayerStatus() == 1) {
                if (kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.START_LICENSING)) {
                  kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.START_LICENSING);
                }
              }
              kittensRoom.leaveGame(userId, 0);
              ChatManager.removeChannel(attachId, checkPlayer.getChannel());
              GroupManager.removeChannel(attachId, checkPlayer.getChannel());
              this.gameRemote.leaveRoom(userId);
              F30291.F3029110S2C.Builder response = F30291.F3029110S2C.newBuilder();
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYER_LEAVES,
                      response.setUserID(userId).build().toByteArray()), kittensRoom.getRoomId());
              if (kittensRoom.seatedPlayers().size() == 0 && kittensRoom.getAudienceList().size() == 0) {
                clearData(kittensRoom.getRoomId());
              } else {
                roomOpenOrCancelMatch(kittensRoom.getRoomId());
                // 清理检测
                List<ExplodingKittensPlayer> playerList = kittensRoom.getPlayerList();
                int playGameSize = (int) playerList.stream()
                    .filter(s -> s.getPlayerId() > 0).count();
                if (playGameSize == 0) {
                  clearTimeout(kittensRoom.getRoomId());
                }
              }
            } else {
              kittensRoom.leaveGame(userId, 1);
              ChatManager.removeChannel(attachId, checkPlayer.getChannel());
              GroupManager.removeChannel(attachId, checkPlayer.getChannel());
              this.gameRemote.leaveRoom(userId);
              if (kittensRoom.seatedPlayersIsDisconnected() == kittensRoom.seatedPlayersNum()
                  && kittensRoom.seatedPlayers().size() == 0 && kittensRoom.getAudienceList().size() == 0) {
                clearData(kittensRoom.getRoomId());
              } else if (kittensRoom.seatedPlayers().size() == 0 && kittensRoom.getAudienceList().size() == 0) {
                clearData(kittensRoom.getRoomId());
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
   * @create 2020/8/31 15:13
   * @update 2020/8/31 15:13
   */
  @Override
  public void cleaning(Long roomId) {
    GAME_DATA.remove(roomId);
    this.gameRemote.deleteRoom(roomId);
    ChatManager.delChatGroup(roomId);
    GroupManager.delRoomGroup(roomId);
  }

  /**
   * TODO 创建陪玩.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/31 15:11
   * @update 2020/8/31 15:11
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
   * @create 2020/8/31 15:36
   * @update 2020/8/31 15:36
   */
  private void enterRoom(Channel channel, Packet packet) {
    try {
      boolean isTestUser = this.redisUtils.hasKey(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
      boolean isPlayer = this.redisUtils.hasKey(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
      if (isTestUser || isPlayer) {
        closeLoading(packet.userId);
        boolean checkRoom = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + packet.roomId);
        boolean checkTest = (Objects.equals(packet.roomId, ExplodingKittensAssets.getLong(ExplodingKittensAssets.TEST_ID)));
        if (checkRoom || checkTest) {
          ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
          if (Objects.nonNull(kittensRoom)) {
            ExplodingKittensPlayer kittensPlayer = kittensRoom.getPlayerInfo(packet.userId);
            //
            if (kittensRoom.getRoomStatus() == 1) {
              if (Objects.nonNull(kittensPlayer)) {
                if (kittensPlayer.getIdentity() == 0 && kittensPlayer.getLinkStatus() == 1) {
                  playerNotExist(channel);
                  return;
                } else {
                  // 断线重连
                  kittensPlayer.setChannel(channel);
                  refreshData(channel, packet);
                  disconnected(channel, packet);
                  return;
                }
              } else {
                // 进入观战
                refreshData(channel, packet);
                byte[] bytes;
                if (isTestUser) {
                  bytes = this.redisUtils.getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
                } else {
                  bytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
                }
                I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
                kittensRoom.enterAudience(channel, playerInfo);
                pullDecorateInfo(packet);
                joinExplodingKittensRoom(packet);
              }
            } else {
              // 销毁清理定时
              kittensRoom.cancelTimeOut((int) packet.roomId);
              if (Objects.nonNull(kittensPlayer)) {
                kittensPlayer.setChannel(channel);
                refreshData(channel, packet);
                // 清理检测
                List<ExplodingKittensPlayer> playerList = kittensRoom.getPlayerList();
                int playGameSize = (int) playerList.stream()
                    .filter(s -> s.getPlayerId() > 0).count();
                if (playGameSize == 0) {
                  clearTimeout(kittensRoom.getRoomId());
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
                kittensRoom.enterSeat(channel, playerInfo);
                pullDecorateInfo(packet);
                joinExplodingKittensRoom(packet);
                roomOpenOrCancelMatch(packet.roomId);
              }
            }
          } else {
            register(TimerEventLoop.timeGroup.next());
            GAME_DATA.computeIfAbsent(packet.roomId, key -> new ExplodingKittensRoom(packet.roomId));
            kittensRoom = GAME_DATA.get(packet.roomId);
            refreshData(channel, packet);
            byte[] bytes;
            if (isTestUser) {
              bytes = this.redisUtils.getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
            } else {
              bytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
            }
            I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
            kittensRoom.enterSeat(channel, playerInfo);
            joinExplodingKittensRoom(packet);
            pullDecorateInfo(packet);
            if (checkTest) {
              kittensRoom.setOpenWay(1);
            } else {
              byte[] roomByte = this.redisUtils.getByte(GameKey.KEY_GAME_ROOM_RECORD.getName() + packet.roomId);
              I10001.RoomRecord roomRecord = I10001.RoomRecord.parseFrom(roomByte);
              kittensRoom.setOpenWay(roomRecord.getOpenWay());
              roomOpenOrCancelMatch(packet.roomId);
            }
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
   * TODO 推送玩家.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/17 18:39
   * @update 2020/8/17 18:39
   */
  private void pushPlayerInfo(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        F30291.F30291S2C.Builder response = F30291.F30291S2C.newBuilder();
        ExplodingKittensPlayer kittensPlayer = kittensRoom.getPlayerInfo(packet.userId);
        if (kittensPlayer.getIdentity() == 1) {
          audienceRoomInfo(channel, packet);
        } else {
          List<ExplodingKittensPlayer> explodingKittensPlayers = kittensRoom.getPlayerList();
          if (CollectionUtils.isNotEmpty(explodingKittensPlayers)) {
            F30291.PlayerInfo.Builder playerInfo;
            for (ExplodingKittensPlayer player : explodingKittensPlayers) {
              playerInfo = F30291.PlayerInfo.newBuilder();
              playerInfo.setNick(player.getPlayerName());
              playerInfo.setUserID(player.getPlayerId());
              playerInfo.setUrl(player.getPlayerAvatar());
              playerInfo.setSex(player.getPlayerSex());
              playerInfo.setDeviPosition(player.getSeatNumber());
              playerInfo.setState(player.getPlayerStatus());
              playerInfo.setCoin(player.getPlayerGold());
              if (StringUtils.isNotEmpty(player.getAvatarFrame())) {
                playerInfo.setUrlFrame(player.getAvatarFrame());
              }
              if (StringUtils.isNotEmpty(player.getCardBackSkin())) {
                F30291.GameSkin.Builder gameSkin = F30291.GameSkin.newBuilder();
                playerInfo.setGameSkin(gameSkin.setCardBack(player.getCardBackSkin()));
              }
              if (player.getPlayerStatus() == 0) {
                if (Objects.nonNull(player.getReadyTime())) {
                  LocalDateTime udt = player.getReadyTime().plusSeconds(15L);
                  LocalDateTime nds = LocalDateTime.now();
                  Duration duration = Duration.between(nds, udt);
                  int second = Math.toIntExact(duration.getSeconds());
                  playerInfo.setReadyTime(second);
                } else {
                  playerInfo.setReadyTime(15);
                }
              }
              response.addSeatPlayer(playerInfo);
            }
          }
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.ENTER_ROOM,
                  response.setResult(0).build().toByteArray()), kittensRoom.getRoomId());
          if (kittensPlayer.getPlayerStatus() == 0) {
            if (!kittensRoom.getTimeOutMap().containsKey((int) packet.userId)) {
              readyTimeout(packet.roomId, packet.userId);
            }
          }
          F30291.F3029112S2C.Builder goldInfo = F30291.F3029112S2C.newBuilder();
          channel.writeAndFlush(
              new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.UPDATE_PLAYER_GOLD,
                  goldInfo.setPlayGold(kittensPlayer.getPlayerGold()).build().toByteArray()));
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
   * @date 2020/8/17 20:51
   * @update 2020/8/17 20:51
   */
  private void audienceRoomInfo(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        F30291.F30291S2C.Builder response = F30291.F30291S2C.newBuilder();
        List<ExplodingKittensPlayer> explodingKittensPlayers = kittensRoom.getPlayerList();
        if (CollectionUtils.isNotEmpty(explodingKittensPlayers)) {
          F30291.PlayerInfo.Builder playerInfo;
          for (ExplodingKittensPlayer player : explodingKittensPlayers) {
            playerInfo = F30291.PlayerInfo.newBuilder();
            playerInfo.setNick(player.getPlayerName());
            playerInfo.setUserID(player.getPlayerId());
            playerInfo.setUrl(player.getPlayerAvatar());
            playerInfo.setSex(player.getPlayerSex());
            playerInfo.setDeviPosition(player.getSeatNumber());
            playerInfo.setState(player.getPlayerStatus());
            playerInfo.setCoin(player.getPlayerGold());
            if (StringUtils.isNotEmpty(player.getAvatarFrame())) {
              playerInfo.setUrlFrame(player.getAvatarFrame());
            }
            if (StringUtils.isNotEmpty(player.getCardBackSkin())) {
              F30291.GameSkin.Builder gameSkin = F30291.GameSkin.newBuilder();
              playerInfo.setGameSkin(gameSkin.setCardBack(player.getCardBackSkin()));
            }
            response.addSeatPlayer(playerInfo);
          }
        }
        channel.writeAndFlush(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.ENTER_ROOM,
                response.setResult(0).build().toByteArray()));
        F30291.F3029112S2C.Builder goldInfo = F30291.F3029112S2C.newBuilder();
        ExplodingKittensPlayer kittensPlayer = kittensRoom.getPlayerInfo(packet.userId);
        channel.writeAndFlush(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.UPDATE_PLAYER_GOLD,
                goldInfo.setPlayGold(kittensPlayer.getPlayerGold()).build().toByteArray()));
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
   * @date 2020/8/31 15:45
   * @update 2020/8/31 15:45
   */
  private void playerReady(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        ExplodingKittensPlayer checkPlayer = kittensRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30291.F302911C2S request = F30291.F302911C2S.parseFrom(packet.bytes);
          F30291.F302911S2C.Builder response = F30291.F302911S2C.newBuilder();
          if (kittensRoom.getRoomStatus() == 1) {
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYER_READY,
                    response.setResult(1).setIsReady(request.getIsReady()).build().toByteArray()));
          } else {
            ExplodingKittensPlayer kittensPlayer = kittensRoom.getPlayerInfo(packet.userId);
            if (kittensPlayer.getIdentity() == 1) {
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYER_READY,
                      response.setResult(1).setIsReady(request.getIsReady()).build().toByteArray()));
            } else {
              // 准备状态 0-准备 1-取消
              if (request.getIsReady() == 0) {
                kittensPlayer.setPlayerStatus(1);
                kittensRoom.cancelTimeOut((int) packet.userId);
                response.setResult(0).setIsReady(request.getIsReady()).setUserID(packet.userId);
                int unReady = kittensRoom.unprepared();
                int isReady = kittensRoom.preparations();
                if (unReady == 0 && isReady == ExplodingKittensAssets.getInt(ExplodingKittensAssets.ROOM_START_NUM)) {
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYER_READY,
                          response.setStartTime(3).build().toByteArray()), kittensRoom.getRoomId());
                  startTimeout(packet.roomId);
                } else {
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYER_READY,
                          response.build().toByteArray()), kittensRoom.getRoomId());
                }
              } else {
                kittensPlayer.setPlayerStatus(0);
                if (kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.START_LICENSING)) {
                  kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.START_LICENSING);
                }
                response.setResult(0).setIsReady(request.getIsReady()).setUserID(packet.userId);
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYER_READY,
                        response.build().toByteArray()), kittensRoom.getRoomId());
                readyTimeout(packet.roomId, packet.userId);
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
   * TODO 玩家出牌.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/31 15:46
   * @update 2020/8/31 15:46
   */
  private void playersPlayCards(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        ExplodingKittensPlayer checkPlayer = kittensRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          if (checkPlayer.getPlayerStatus() == ExplodingKittensAssets.getInt(ExplodingKittensAssets.PLAYER_IN_GAME)) {
            if (Objects.equals(kittensRoom.getNowActionPlayer(), packet.userId)) {
              F30291.F302913C2S request = F30291.F302913C2S.parseFrom(packet.bytes);
              F30291.CardInfos cardInfos = request.getPlayCardId();
              // 1-炸弹 2-拆弹 3-先知 4-占星 5-洗牌 6-抽底 7-跳过 8-转向 9-诅咒 10诅咒*2 11-祈求 12-束缚 13-交换
              if (cardInfos.getCardFunc() > ExplodingKittensAssets.getInt(ExplodingKittensAssets.BOMB_DISPOSAL)) {
                switch (cardInfos.getCardFunc()) {
                  // 查看位于牌堆顶的三张牌
                  case 3:
                    // 打出先知
                    useProphetCard(channel, packet);
                    break;
                  // 得知最近的一张炸弹牌的位置
                  case 4:
                    // 打出占星
                    useAstrologyCard(channel, packet);
                    break;
                  // 对牌堆进行随机洗牌
                  case 5:
                    // 打出洗牌
                    useShuffleCard(channel, packet);
                    break;
                  // 从牌堆底部抽取一张牌，结束你的当前回合
                  case 6:
                    // 打出抽底
                    useBottomingCard(channel, packet);
                    break;
                  // 结束你的当前回合且无需从牌堆抽牌
                  case 7:
                    // 打出跳过
                    useSkipCard(channel, packet);
                    break;
                  // 结束你的当前回合且无需从牌堆抽牌，改变出牌顺序（顺变逆，逆变顺）
                  case 8:
                    // 打出转向
                    useSteeringCard(channel, packet);
                    break;
                  // 并指定任一玩家，结束你的当前回合且无需从牌堆抽牌，被指定的玩家将立刻进行1个回合（可对自己使用）
                  case 9:
                    // 打出诅咒
                    useCurseCard(channel, packet);
                    break;
                  // 并指定任一玩家，结束你的当前回合且无需从牌堆抽牌，被指定的玩家将立刻进行2个回合（可对自己使用）
                  case 10:
                    // 打出诅咒*2
                    useDoubleCurseCard(channel, packet);
                    break;
                  // 选择任一玩家，被选中玩家必须从自己的手牌中挑选一张卡牌交给你
                  case 11:
                    // 打出祈求
                    usePrayerCard(channel, packet);
                    break;
                  // 选择任一玩家，该玩家的回合必须先抽牌再进行其他操作
                  case 12:
                    //  打出束缚
                    useRestraintCard(channel, packet);
                    break;
                  // 选择任一玩家，与他交换手牌
                  default:
                    // 打出交换
                    useSwapCard(channel, packet);
                    break;
                }
              } else {
                F30291.F302913S2C.Builder response = F30291.F302913S2C.newBuilder();
                response.setResult(1);
                response.setPlayCardId(request.getPlayCardId());
                response.setOptionUserID(packet.userId);
                LocalDateTime udt = kittensRoom.getActionTime().plusSeconds(15L);
                LocalDateTime nds = LocalDateTime.now();
                Duration duration = Duration.between(nds, udt);
                int second = Math.toIntExact(duration.getSeconds());
                response.setTimes(second);
                response.setDirection(kittensRoom.getDirection());
                response.setRemainingCard(kittensRoom.getRemainingCard().size());
                channel.writeAndFlush(
                    new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYERS_PLAY_CARDS,
                        response.build().toByteArray()));
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
   * TODO 使用拆弹.
   *
   * @param roomId [房间ID]
   * @param playerId 玩家ID
   * @param kittensCard 玩家出牌
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 7:04
   * @since 2020/9/7 7:04
   */
  private void useBombDisposalCards(Long roomId, Long playerId, ExplodingKittensCard kittensCard) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.PLAYERS_PLAY_CARDS);
        F30291.CardInfos.Builder cardInfos = F30291.CardInfos.newBuilder();
        cardInfos.setCardFunc(kittensCard.getCardFunction());
        cardInfos.setHandCardID(kittensCard.getCardId());
        F30291.F302913S2C.Builder response = F30291.F302913S2C.newBuilder();
        kittensRoom.setDesktopCardInfo(kittensCard);
        kittensRoom.playCards(kittensCard.getCardId(), playerId);
        // 数据处理
        response.setResult(0);
        response.setPlayCardId(cardInfos);
        response.setOptionUserID(playerId);
        response.setTimes(15);
        // 位置选择
        int cardNum = kittensRoom.getRemainingCard().size();
        if (cardNum == 0) {
          response.setSelectNum(1);
        } else {
          if (cardNum >= ExplodingKittensAssets.getInt(ExplodingKittensAssets.BOMB_SEAT_NUM)) {
            response.setSelectNum(5);
          } else {
            response.setSelectNum(cardNum);
          }
        }
        response.setDirection(kittensRoom.getDirection());
        // 拆除炸弹 重置诅咒
        if (kittensRoom.getCursesNum() > 0) {
          response.setRemainingCurse(0);
        }
        kittensRoom.setCursesNum(0);
        kittensRoom.setCursedPlayer(0L);
        response.setNextUserId(playerId);
        ExplodingKittensPlayer actionPlayer = kittensRoom.getPlayerInfo(playerId);
        List<ExplodingKittensCard> actionPlayerCard = actionPlayer.getPlayerCard();
        if (CollectionUtils.isNotEmpty(actionPlayerCard)) {
          F30291.CardInfos.Builder handCard;
          for (ExplodingKittensCard explodingKittensCard : actionPlayerCard) {
            handCard = F30291.CardInfos.newBuilder();
            handCard.setHandCardID(explodingKittensCard.getCardId());
            handCard.setCardFunc(explodingKittensCard.getCardFunction());
            response.addHandCardId(handCard);
          }
        } else {
          response.addAllHandCardId(F30291.F302913S2C.newBuilder().getHandCardIdList());
        }
        response.setRemainingCard(kittensRoom.getRemainingCard().size());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYERS_PLAY_CARDS,
                response.build().toByteArray()), kittensRoom.getRoomId());
        if (actionPlayer.getLinkStatus() == 0) {
          // 添加定时
          bombDisposalTimeout(kittensRoom.getRoomId());
        } else {
          // 添加托管
          trusteeshipTimeout(roomId, playerId, 1);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 使用先知.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 7:09
   * @since 2020/9/7 7:09
   */
  @SuppressWarnings("unused")
  private void useProphetCard(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.PLAYERS_PLAY_CARDS);
        F30291.F302913C2S request = F30291.F302913C2S.parseFrom(packet.bytes);
        F30291.CardInfos cardInfos = request.getPlayCardId();
        ExplodingKittensCard kittensCard = new ExplodingKittensCard();
        kittensCard.setCardId(cardInfos.getHandCardID());
        kittensCard.setCardFunction(cardInfos.getCardFunc());
        F30291.F302913S2C.Builder response = F30291.F302913S2C.newBuilder();
        if (kittensRoom.getCursesNum() > 0) {
          response.setRemainingCurse(kittensRoom.getCursesNum());
        }
        // 数据处理
        response.setResult(0);
        response.setPlayCardId(cardInfos);
        kittensRoom.setDesktopCardInfo(kittensCard);
        kittensRoom.playCards(kittensCard.getCardId(), packet.userId);
        List<ExplodingKittensCard> kittensCardList = kittensRoom.performProphet();
        if (CollectionUtils.isNotEmpty(kittensCardList)) {
          F30291.CardInfos.Builder threeCardInfo;
          for (ExplodingKittensCard card : kittensCardList) {
            threeCardInfo = F30291.CardInfos.newBuilder();
            threeCardInfo.setHandCardID(card.getCardId());
            threeCardInfo.setCardFunc(card.getCardFunction());
            response.addXzCardId(threeCardInfo);
          }
        }
        response.setTimes(15);
        response.setOptionUserID(packet.userId);
        response.setDirection(kittensRoom.getDirection());
        response.setNextUserId(packet.userId);
        ExplodingKittensPlayer actionPlayer = kittensRoom.getPlayerInfo(packet.userId);
        if (actionPlayer.getLinkStatus() == 0) {
          // 玩家成就.大预言术
          Map<String, Object> taskSuc0037 = Maps.newHashMap();
          taskSuc0037.put("userId", packet.userId);
          taskSuc0037.put("code", AchievementEnum.AMT0037.getCode());
          taskSuc0037.put("progress", 1);
          taskSuc0037.put("isReset", 0);
          this.userRemote.achievementHandlers(taskSuc0037);
        }
        List<ExplodingKittensCard> actionPlayerCard = actionPlayer.getPlayerCard();
        if (CollectionUtils.isNotEmpty(actionPlayerCard)) {
          F30291.CardInfos.Builder handCard;
          for (ExplodingKittensCard explodingKittensCard : actionPlayerCard) {
            handCard = F30291.CardInfos.newBuilder();
            handCard.setHandCardID(explodingKittensCard.getCardId());
            handCard.setCardFunc(explodingKittensCard.getCardFunction());
            response.addHandCardId(handCard);
          }
        } else {
          response.addAllHandCardId(F30291.F302913S2C.newBuilder().getHandCardIdList());
        }
        response.setRemainingCard(kittensRoom.getRemainingCard().size());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYERS_PLAY_CARDS,
                response.build().toByteArray()), kittensRoom.getRoomId());
        ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
        if (nextPlayer.getLinkStatus() == 0) {
          // 操作定时
          actionTimeout(kittensRoom.getRoomId());
        } else {
          trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 使用占星.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 7:13
   * @since 2020/9/7 7:13
   */
  @SuppressWarnings("unused")
  private void useAstrologyCard(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.PLAYERS_PLAY_CARDS);
        F30291.F302913C2S request = F30291.F302913C2S.parseFrom(packet.bytes);
        F30291.CardInfos cardInfos = request.getPlayCardId();
        ExplodingKittensCard kittensCard = new ExplodingKittensCard();
        kittensCard.setCardId(cardInfos.getHandCardID());
        kittensCard.setCardFunction(cardInfos.getCardFunc());
        F30291.F302913S2C.Builder response = F30291.F302913S2C.newBuilder();
        if (kittensRoom.getCursesNum() > 0) {
          response.setRemainingCurse(kittensRoom.getCursesNum());
        }
        kittensRoom.setDesktopCardInfo(kittensCard);
        kittensRoom.playCards(kittensCard.getCardId(), packet.userId);
        // 数据处理
        response.setResult(0);
        response.setPlayCardId(cardInfos);
        response.setTimes(15);
        response.setOptionUserID(packet.userId);
        response.setLastExploding(kittensRoom.explodingSeat());
        response.setDirection(kittensRoom.getDirection());
        response.setNextUserId(packet.userId);
        ExplodingKittensPlayer actionPlayer = kittensRoom.getPlayerInfo(packet.userId);
        List<ExplodingKittensCard> actionPlayerCard = actionPlayer.getPlayerCard();
        if (CollectionUtils.isNotEmpty(actionPlayerCard)) {
          F30291.CardInfos.Builder handCard;
          for (ExplodingKittensCard explodingKittensCard : actionPlayerCard) {
            handCard = F30291.CardInfos.newBuilder();
            handCard.setHandCardID(explodingKittensCard.getCardId());
            handCard.setCardFunc(explodingKittensCard.getCardFunction());
            response.addHandCardId(handCard);
          }
        } else {
          response.addAllHandCardId(F30291.F302913S2C.newBuilder().getHandCardIdList());
        }
        response.setRemainingCard(kittensRoom.getRemainingCard().size());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYERS_PLAY_CARDS,
                response.build().toByteArray()), kittensRoom.getRoomId());
        ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
        if (nextPlayer.getLinkStatus() == 0) {
          // 操作定时
          actionTimeout(kittensRoom.getRoomId());
        } else {
          trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 使用洗牌.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 7:15
   * @since 2020/9/7 7:15
   */
  @SuppressWarnings("unused")
  private void useShuffleCard(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.PLAYERS_PLAY_CARDS);
        F30291.F302913C2S request = F30291.F302913C2S.parseFrom(packet.bytes);
        F30291.CardInfos cardInfos = request.getPlayCardId();
        ExplodingKittensCard kittensCard = new ExplodingKittensCard();
        kittensCard.setCardId(cardInfos.getHandCardID());
        kittensCard.setCardFunction(cardInfos.getCardFunc());
        F30291.F302913S2C.Builder response = F30291.F302913S2C.newBuilder();
        if (kittensRoom.getCursesNum() > 0) {
          response.setRemainingCurse(kittensRoom.getCursesNum());
        }
        kittensRoom.setDesktopCardInfo(kittensCard);
        kittensRoom.playCards(kittensCard.getCardId(), packet.userId);
        // 随机洗牌
        kittensRoom.startShuffling();
        response.setResult(0);
        response.setPlayCardId(cardInfos);
        response.setOptionUserID(packet.userId);
        response.setTimes(15);
        response.setDirection(kittensRoom.getDirection());
        response.setNextUserId(packet.userId);
        ExplodingKittensPlayer actionPlayer = kittensRoom.getPlayerInfo(packet.userId);
        List<ExplodingKittensCard> actionPlayerCard = actionPlayer.getPlayerCard();
        if (CollectionUtils.isNotEmpty(actionPlayerCard)) {
          F30291.CardInfos.Builder handCard;
          for (ExplodingKittensCard explodingKittensCard : actionPlayerCard) {
            handCard = F30291.CardInfos.newBuilder();
            handCard.setHandCardID(explodingKittensCard.getCardId());
            handCard.setCardFunc(explodingKittensCard.getCardFunction());
            response.addHandCardId(handCard);
          }
        } else {
          response.addAllHandCardId(F30291.F302913S2C.newBuilder().getHandCardIdList());
        }
        response.setRemainingCard(kittensRoom.getRemainingCard().size());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYERS_PLAY_CARDS,
                response.build().toByteArray()), kittensRoom.getRoomId());
        ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
        if (nextPlayer.getLinkStatus() == 0) {
          // 操作定时
          actionTimeout(kittensRoom.getRoomId());
        } else {
          trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 使用抽底.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 7:18
   * @since 2020/9/7 7:18
   */
  @SuppressWarnings("unused")
  private void useBottomingCard(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.PLAYERS_PLAY_CARDS);
        F30291.F302913C2S request = F30291.F302913C2S.parseFrom(packet.bytes);
        F30291.CardInfos cardInfos = request.getPlayCardId();
        ExplodingKittensCard kittensCard = new ExplodingKittensCard();
        kittensCard.setCardId(cardInfos.getHandCardID());
        kittensCard.setCardFunction(cardInfos.getCardFunc());
        F30291.F302913S2C.Builder response = F30291.F302913S2C.newBuilder();
        kittensRoom.setDesktopCardInfo(kittensCard);
        kittensRoom.playCards(kittensCard.getCardId(), packet.userId);
        // 消除诅咒
        kittensRoom.eliminateCurse();
        if (kittensRoom.getCursesNum() > 0) {
          response.setRemainingCurse(kittensRoom.getCursesNum());
        } else {
          response.setRemainingCurse(0);
        }
        // 数据处理
        response.setResult(0);
        response.setPlayCardId(cardInfos);
        response.setOptionUserID(packet.userId);
        response.setTimes(15);
        response.setDirection(kittensRoom.getDirection());
        response.setRemainingCard(kittensRoom.getRemainingCard().size());
        ExplodingKittensPlayer actionPlayer = kittensRoom.getPlayerInfo(packet.userId);
        List<ExplodingKittensCard> actionPlayerCard = actionPlayer.getPlayerCard();
        if (CollectionUtils.isNotEmpty(actionPlayerCard)) {
          F30291.CardInfos.Builder handCard;
          for (ExplodingKittensCard explodingKittensCard : actionPlayerCard) {
            handCard = F30291.CardInfos.newBuilder();
            handCard.setHandCardID(explodingKittensCard.getCardId());
            handCard.setCardFunc(explodingKittensCard.getCardFunction());
            response.addHandCardId(handCard);
          }
        } else {
          response.addAllHandCardId(F30291.F302913S2C.newBuilder().getHandCardIdList());
        }
        response.setRemainingCard(kittensRoom.getRemainingCard().size());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYERS_PLAY_CARDS,
                response.build().toByteArray()), kittensRoom.getRoomId());
        // 当前操作玩家
        ExplodingKittensPlayer nowActionPlayer = kittensRoom.getPlayerInfo(packet.userId);
        ExplodingKittensCard newKittensCard = kittensRoom.useBottomingCard();
        if (Objects.nonNull(newKittensCard)) {
          F30291.F3029116S2C.Builder builder = F30291.F3029116S2C.newBuilder();
          builder.setResult(0);
          builder.setActionUserId(packet.userId);
          if (newKittensCard.getCardFunction() == 1) {
            actionPlayer.setTouchExploding(1);
            kittensRoom.setExplodingCardInfo(newKittensCard);
            List<ExplodingKittensCard> kittensCardList = actionPlayerCard.stream()
                .filter(card -> card.getCardFunction() == 2)
                .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(kittensCardList)) {
              builder.setTimes(15);
              builder.setBombPro(kittensRoom.getProbability());
              builder.setIsExplosion(2);
              builder.setNextUserId(packet.userId);
              builder.setRemainingCard(kittensRoom.getRemainingCard().size());
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.BOTTOM_BACK,
                      builder.build().toByteArray()), kittensRoom.getRoomId());
              // 立刻拆弹
              useBombDisposalCards(kittensRoom.getRoomId(), packet.userId, kittensCardList.get(0));
            } else {
              long nextId = kittensRoom.getNextActionPlayer(packet.userId);
              // 下一个玩家 当前玩家出局 炸弹-1
              kittensRoom.playerOut(packet.userId);
              if (kittensRoom.getRankingList().size() < ExplodingKittensAssets.getInt(ExplodingKittensAssets.RANKING_INDEX_NUM)) {
                kittensRoom.setNowActionPlayer(nextId);
                builder.setNextUserId(kittensRoom.getNowActionPlayer());
                builder.setTimes(15);
                ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
                if (nextPlayer.getBondageIndex() > 0) {
                  builder.setRightNowTouchCard(1);
                } else {
                  builder.setRightNowTouchCard(2);
                }
                kittensRoom.explodingProbability();
                builder.setBombPro(kittensRoom.getProbability());
                builder.setRemainingCurse(0);
                // 重置诅咒
                kittensRoom.setCursesNum(0);
                kittensRoom.setCursedPlayer(0L);
                builder.setRemainingCard(kittensRoom.getRemainingCard().size());
                builder.setIsExplosion(1);
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.BOTTOM_BACK,
                        builder.build().toByteArray()), kittensRoom.getRoomId());
                if (nextPlayer.getLinkStatus() == 0) {
                  actionTimeout(kittensRoom.getRoomId());
                } else {
                  trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
                }
                outShowTimeout(kittensRoom.getRoomId(), packet.userId);
              } else {
                kittensRoom.destroy();
                builder.setNextUserId(0L);
                builder.setTimes(0);
                builder.setBombPro(kittensRoom.getProbability());
                builder.setRemainingCurse(0);
                // 重置诅咒
                kittensRoom.setCursesNum(0);
                kittensRoom.setCursedPlayer(0L);
                builder.setRemainingCard(kittensRoom.getRemainingCard().size());
                builder.setIsExplosion(1);
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.BOTTOM_BACK,
                        builder.build().toByteArray()), kittensRoom.getRoomId());
                gameFinishTimeout(kittensRoom.getRoomId());
              }
            }
          } else {
            nowActionPlayer.getPlayerCard().add(newKittensCard);
            actionPlayerCard = nowActionPlayer.getPlayerCard();
            if (CollectionUtils.isNotEmpty(actionPlayerCard)) {
              F30291.CardInfos.Builder handCard;
              for (ExplodingKittensCard explodingKittensCard : actionPlayerCard) {
                handCard = F30291.CardInfos.newBuilder();
                handCard.setCardFunc(explodingKittensCard.getCardFunction());
                handCard.setHandCardID(explodingKittensCard.getCardId());
                builder.addHandCardId(handCard);
              }
            }
            kittensRoom.explodingProbability();
            builder.setBombPro(kittensRoom.getProbability());
            if (kittensRoom.getCursesNum() > 0) {
              builder.setRemainingCurse(kittensRoom.getCursesNum());
              builder.setNextUserId(kittensRoom.getNowActionPlayer());
            } else {
              kittensRoom.setActionPlayer(kittensRoom.getNowActionPlayer(), kittensRoom.getDirection());
              builder.setNextUserId(kittensRoom.getNowActionPlayer());
            }
            builder.setTimes(15);
            ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
            if (nextPlayer.getBondageIndex() > 0) {
              builder.setRightNowTouchCard(1);
            } else {
              builder.setRightNowTouchCard(2);
            }
            builder.setRemainingCard(kittensRoom.getRemainingCard().size());
            builder.setIsExplosion(2);
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.BOTTOM_BACK,
                    builder.build().toByteArray()), kittensRoom.getRoomId());
            if (nextPlayer.getLinkStatus() == 0) {
              actionTimeout(kittensRoom.getRoomId());
            } else {
              trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
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
   * TODO 使用跳过.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 7:20
   * @since 2020/9/7 7:20
   */
  @SuppressWarnings("unused")
  private void useSkipCard(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.PLAYERS_PLAY_CARDS);
        F30291.F302913C2S request = F30291.F302913C2S.parseFrom(packet.bytes);
        F30291.CardInfos cardInfos = request.getPlayCardId();
        ExplodingKittensCard kittensCard = new ExplodingKittensCard();
        kittensCard.setCardId(cardInfos.getHandCardID());
        kittensCard.setCardFunction(cardInfos.getCardFunc());
        F30291.F302913S2C.Builder response = F30291.F302913S2C.newBuilder();
        kittensRoom.setDesktopCardInfo(kittensCard);
        kittensRoom.playCards(kittensCard.getCardId(), packet.userId);
        // 消除诅咒
        kittensRoom.eliminateCurse();
        if (kittensRoom.getCursesNum() > 0) {
          response.setRemainingCurse(kittensRoom.getCursesNum());
          response.setNextUserId(packet.userId);
        } else {
          response.setRemainingCurse(0);
          kittensRoom.setActionPlayer(kittensRoom.getNowActionPlayer(), kittensRoom.getDirection());
          response.setNextUserId(kittensRoom.getNowActionPlayer());
        }
        // 数据处理
        response.setResult(0);
        response.setPlayCardId(cardInfos);
        response.setOptionUserID(packet.userId);
        response.setTimes(15);
        response.setDirection(kittensRoom.getDirection());
        ExplodingKittensPlayer actionPlayer = kittensRoom.getPlayerInfo(packet.userId);
        List<ExplodingKittensCard> actionPlayerCard = actionPlayer.getPlayerCard();
        if (CollectionUtils.isNotEmpty(actionPlayerCard)) {
          F30291.CardInfos.Builder handCard;
          for (ExplodingKittensCard explodingKittensCard : actionPlayerCard) {
            handCard = F30291.CardInfos.newBuilder();
            handCard.setHandCardID(explodingKittensCard.getCardId());
            handCard.setCardFunc(explodingKittensCard.getCardFunction());
            response.addHandCardId(handCard);
          }
        } else {
          response.addAllHandCardId(F30291.F302913S2C.newBuilder().getHandCardIdList());
        }
        response.setRemainingCard(kittensRoom.getRemainingCard().size());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYERS_PLAY_CARDS,
                response.build().toByteArray()), kittensRoom.getRoomId());
        ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
        if (nextPlayer.getLinkStatus() == 0) {
          // 操作定时
          actionTimeout(kittensRoom.getRoomId());
        } else {
          trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 使用转向.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 7:22
   * @since 2020/9/7 7:22
   */
  @SuppressWarnings("unused")
  private void useSteeringCard(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.PLAYERS_PLAY_CARDS);
        F30291.F302913C2S request = F30291.F302913C2S.parseFrom(packet.bytes);
        F30291.CardInfos cardInfos = request.getPlayCardId();
        ExplodingKittensCard kittensCard = new ExplodingKittensCard();
        kittensCard.setCardId(cardInfos.getHandCardID());
        kittensCard.setCardFunction(cardInfos.getCardFunc());
        F30291.F302913S2C.Builder response = F30291.F302913S2C.newBuilder();
        kittensRoom.setDesktopCardInfo(kittensCard);
        kittensRoom.playCards(kittensCard.getCardId(), packet.userId);
        kittensRoom.useSteering();
        // 消除诅咒
        kittensRoom.eliminateCurse();
        if (kittensRoom.getCursesNum() > 0) {
          response.setRemainingCurse(kittensRoom.getCursesNum());
          response.setNextUserId(packet.userId);
        } else {
          response.setRemainingCurse(0);
          kittensRoom.setActionPlayer(kittensRoom.getNowActionPlayer(), kittensRoom.getDirection());
          response.setNextUserId(kittensRoom.getNowActionPlayer());
        }
        response.setResult(0);
        response.setPlayCardId(cardInfos);
        response.setOptionUserID(packet.userId);
        response.setTimes(15);
        response.setDirection(kittensRoom.getDirection());
        ExplodingKittensPlayer actionPlayer = kittensRoom.getPlayerInfo(packet.userId);
        List<ExplodingKittensCard> actionPlayerCard = actionPlayer.getPlayerCard();
        if (CollectionUtils.isNotEmpty(actionPlayerCard)) {
          F30291.CardInfos.Builder handCard;
          for (ExplodingKittensCard explodingKittensCard : actionPlayerCard) {
            handCard = F30291.CardInfos.newBuilder();
            handCard.setHandCardID(explodingKittensCard.getCardId());
            handCard.setCardFunc(explodingKittensCard.getCardFunction());
            response.addHandCardId(handCard);
          }
        } else {
          response.addAllHandCardId(F30291.F302913S2C.newBuilder().getHandCardIdList());
        }
        response.setRemainingCard(kittensRoom.getRemainingCard().size());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYERS_PLAY_CARDS,
                response.build().toByteArray()), kittensRoom.getRoomId());
        ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
        if (nextPlayer.getLinkStatus() == 0) {
          // 操作定时
          actionTimeout(kittensRoom.getRoomId());
        } else {
          trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 使用诅咒.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 7:24
   * @since 2020/9/7 7:24
   */
  @SuppressWarnings("unused")
  private void useCurseCard(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.PLAYERS_PLAY_CARDS);
        F30291.F302913C2S request = F30291.F302913C2S.parseFrom(packet.bytes);
        F30291.CardInfos cardInfos = request.getPlayCardId();
        ExplodingKittensCard kittensCard = new ExplodingKittensCard();
        kittensCard.setCardId(cardInfos.getHandCardID());
        kittensCard.setCardFunction(cardInfos.getCardFunc());
        F30291.F302913S2C.Builder response = F30291.F302913S2C.newBuilder();
        kittensRoom.setDesktopCardInfo(kittensCard);
        kittensRoom.playCards(kittensCard.getCardId(), packet.userId);
        kittensRoom.setCursesNum(kittensRoom.getCursesNum() + 1);
        response.setRemainingCurse(kittensRoom.getCursesNum());
        if (CollectionUtils.isNotEmpty(kittensRoom.optionalPlayersOne())) {
          response.addAllSkillUserIds(kittensRoom.optionalPlayersOne());
        }
        response.setResult(0);
        response.setPlayCardId(cardInfos);
        response.setOptionUserID(packet.userId);
        response.setTimes(15);
        response.setDirection(kittensRoom.getDirection());
        response.setNextUserId(packet.userId);
        ExplodingKittensPlayer actionPlayer = kittensRoom.getPlayerInfo(packet.userId);
        if (actionPlayer.getLinkStatus() == 0) {
          // 玩家成就.巫毒诅咒
          Map<String, Object> taskSuc0040 = Maps.newHashMap();
          taskSuc0040.put("userId", packet.userId);
          taskSuc0040.put("code", AchievementEnum.AMT0040.getCode());
          taskSuc0040.put("progress", 1);
          taskSuc0040.put("isReset", 0);
          this.userRemote.achievementHandlers(taskSuc0040);
        }
        List<ExplodingKittensCard> actionPlayerCard = actionPlayer.getPlayerCard();
        if (CollectionUtils.isNotEmpty(actionPlayerCard)) {
          F30291.CardInfos.Builder handCard;
          for (ExplodingKittensCard explodingKittensCard : actionPlayerCard) {
            handCard = F30291.CardInfos.newBuilder();
            handCard.setHandCardID(explodingKittensCard.getCardId());
            handCard.setCardFunc(explodingKittensCard.getCardFunction());
            response.addHandCardId(handCard);
          }
        } else {
          response.addAllHandCardId(F30291.F302913S2C.newBuilder().getHandCardIdList());
        }
        response.setRemainingCard(kittensRoom.getRemainingCard().size());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYERS_PLAY_CARDS,
                response.build().toByteArray()), kittensRoom.getRoomId());
        ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
        if (nextPlayer.getLinkStatus() == 0) {
          // 诅咒定时
          curseTimeout(kittensRoom.getRoomId());
        } else {
          trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 3);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 使用诅咒*2.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 7:24
   * @since 2020/9/7 7:24
   */
  @SuppressWarnings("unused")
  private void useDoubleCurseCard(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.PLAYERS_PLAY_CARDS);
        F30291.F302913C2S request = F30291.F302913C2S.parseFrom(packet.bytes);
        F30291.CardInfos cardInfos = request.getPlayCardId();
        ExplodingKittensCard kittensCard = new ExplodingKittensCard();
        kittensCard.setCardId(cardInfos.getHandCardID());
        kittensCard.setCardFunction(cardInfos.getCardFunc());
        F30291.F302913S2C.Builder response = F30291.F302913S2C.newBuilder();
        kittensRoom.setDesktopCardInfo(kittensCard);
        kittensRoom.playCards(kittensCard.getCardId(), packet.userId);
        kittensRoom.setCursesNum(kittensRoom.getCursesNum() + 2);
        response.setRemainingCurse(kittensRoom.getCursesNum());
        if (CollectionUtils.isNotEmpty(kittensRoom.optionalPlayersOne())) {
          response.addAllSkillUserIds(kittensRoom.optionalPlayersOne());
        }
        response.setResult(0);
        response.setPlayCardId(cardInfos);
        response.setOptionUserID(packet.userId);
        response.setTimes(15);
        response.setDirection(kittensRoom.getDirection());
        response.setNextUserId(packet.userId);
        ExplodingKittensPlayer actionPlayer = kittensRoom.getPlayerInfo(packet.userId);
        if (actionPlayer.getLinkStatus() == 0) {
          // 玩家成就.巫毒诅咒
          Map<String, Object> taskSuc0040 = Maps.newHashMap();
          taskSuc0040.put("userId", packet.userId);
          taskSuc0040.put("code", AchievementEnum.AMT0040.getCode());
          taskSuc0040.put("progress", 1);
          taskSuc0040.put("isReset", 0);
          this.userRemote.achievementHandlers(taskSuc0040);
        }
        List<ExplodingKittensCard> actionPlayerCard = actionPlayer.getPlayerCard();
        if (CollectionUtils.isNotEmpty(actionPlayerCard)) {
          F30291.CardInfos.Builder handCard;
          for (ExplodingKittensCard explodingKittensCard : actionPlayerCard) {
            handCard = F30291.CardInfos.newBuilder();
            handCard.setHandCardID(explodingKittensCard.getCardId());
            handCard.setCardFunc(explodingKittensCard.getCardFunction());
            response.addHandCardId(handCard);
          }
        } else {
          response.addAllHandCardId(F30291.F302913S2C.newBuilder().getHandCardIdList());
        }
        response.setRemainingCard(kittensRoom.getRemainingCard().size());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYERS_PLAY_CARDS,
                response.build().toByteArray()), kittensRoom.getRoomId());
        ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
        if (nextPlayer.getLinkStatus() == 0) {
          // 诅咒定时
          curseTimeout(kittensRoom.getRoomId());
        } else {
          trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 3);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 使用祈求.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 7:28
   * @since 2020/9/7 7:28
   */
  @SuppressWarnings("unused")
  private void usePrayerCard(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.PLAYERS_PLAY_CARDS);
        F30291.F302913C2S request = F30291.F302913C2S.parseFrom(packet.bytes);
        F30291.CardInfos cardInfos = request.getPlayCardId();
        ExplodingKittensCard kittensCard = new ExplodingKittensCard();
        kittensCard.setCardId(cardInfos.getHandCardID());
        kittensCard.setCardFunction(cardInfos.getCardFunc());
        F30291.F302913S2C.Builder response = F30291.F302913S2C.newBuilder();
        kittensRoom.setDesktopCardInfo(kittensCard);
        kittensRoom.playCards(kittensCard.getCardId(), packet.userId);
        if (kittensRoom.getCursesNum() > 0) {
          response.setRemainingCurse(kittensRoom.getCursesNum());
        }
        if (CollectionUtils.isNotEmpty(kittensRoom.optionalPlayersThree(packet.userId))) {
          response.addAllSkillUserIds(kittensRoom.optionalPlayersThree(packet.userId));
        }
        response.setResult(0);
        response.setPlayCardId(cardInfos);
        response.setOptionUserID(packet.userId);
        response.setTimes(15);
        response.setDirection(kittensRoom.getDirection());
        response.setNextUserId(packet.userId);
        ExplodingKittensPlayer actionPlayer = kittensRoom.getPlayerInfo(packet.userId);
        List<ExplodingKittensCard> actionPlayerCard = actionPlayer.getPlayerCard();
        if (CollectionUtils.isNotEmpty(actionPlayerCard)) {
          F30291.CardInfos.Builder handCard;
          for (ExplodingKittensCard explodingKittensCard : actionPlayerCard) {
            handCard = F30291.CardInfos.newBuilder();
            handCard.setHandCardID(explodingKittensCard.getCardId());
            handCard.setCardFunc(explodingKittensCard.getCardFunction());
            response.addHandCardId(handCard);
          }
        } else {
          response.addAllHandCardId(F30291.F302913S2C.newBuilder().getHandCardIdList());
        }
        response.setRemainingCard(kittensRoom.getRemainingCard().size());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYERS_PLAY_CARDS,
                response.build().toByteArray()), kittensRoom.getRoomId());
        ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
        if (response.getSkillUserIdsList().size() == 0) {
          if (nextPlayer.getLinkStatus() == 0) {
            actionTimeout(kittensRoom.getRoomId());
          } else {
            trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
          }
        } else {
          if (nextPlayer.getLinkStatus() == 0) {
            // 祈求定时
            prayForTimeout(kittensRoom.getRoomId());
          } else {
            trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 4);
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 使用束缚.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 7:30
   * @since 2020/9/7 7:30
   */
  @SuppressWarnings("unused")
  private void useRestraintCard(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.PLAYERS_PLAY_CARDS);
        F30291.F302913C2S request = F30291.F302913C2S.parseFrom(packet.bytes);
        F30291.CardInfos cardInfos = request.getPlayCardId();
        ExplodingKittensCard kittensCard = new ExplodingKittensCard();
        kittensCard.setCardId(cardInfos.getHandCardID());
        kittensCard.setCardFunction(cardInfos.getCardFunc());
        F30291.F302913S2C.Builder response = F30291.F302913S2C.newBuilder();
        kittensRoom.setDesktopCardInfo(kittensCard);
        kittensRoom.playCards(kittensCard.getCardId(), packet.userId);
        if (kittensRoom.getCursesNum() > 0) {
          response.setRemainingCurse(kittensRoom.getCursesNum());
        }
        if (CollectionUtils.isNotEmpty(kittensRoom.optionalPlayersTwo(packet.userId))) {
          response.addAllSkillUserIds(kittensRoom.optionalPlayersTwo(packet.userId));
        }
        response.setResult(0);
        response.setPlayCardId(cardInfos);
        response.setOptionUserID(packet.userId);
        response.setTimes(15);
        response.setDirection(kittensRoom.getDirection());
        response.setNextUserId(packet.userId);
        ExplodingKittensPlayer actionPlayer = kittensRoom.getPlayerInfo(packet.userId);
        List<ExplodingKittensCard> actionPlayerCard = actionPlayer.getPlayerCard();
        if (CollectionUtils.isNotEmpty(actionPlayerCard)) {
          F30291.CardInfos.Builder handCard;
          for (ExplodingKittensCard explodingKittensCard : actionPlayerCard) {
            handCard = F30291.CardInfos.newBuilder();
            handCard.setHandCardID(explodingKittensCard.getCardId());
            handCard.setCardFunc(explodingKittensCard.getCardFunction());
            response.addHandCardId(handCard);
          }
        } else {
          response.addAllHandCardId(F30291.F302913S2C.newBuilder().getHandCardIdList());
        }
        response.setRemainingCard(kittensRoom.getRemainingCard().size());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYERS_PLAY_CARDS,
                response.build().toByteArray()), kittensRoom.getRoomId());
        ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
        if (response.getSkillUserIdsList().size() == 0) {
          if (nextPlayer.getLinkStatus() == 0) {
            actionTimeout(kittensRoom.getRoomId());
          } else {
            trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
          }
        } else {
          if (nextPlayer.getLinkStatus() == 0) {
            // 束缚定时
            manacleTimeout(kittensRoom.getRoomId());
          } else {
            trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 5);
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 使用交换.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/31 15:47
   * @update 2020/8/31 15:47
   */
  @SuppressWarnings("unused")
  private void useSwapCard(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.PLAYERS_PLAY_CARDS);
        F30291.F302913C2S request = F30291.F302913C2S.parseFrom(packet.bytes);
        F30291.CardInfos cardInfos = request.getPlayCardId();
        ExplodingKittensCard kittensCard = new ExplodingKittensCard();
        kittensCard.setCardId(cardInfos.getHandCardID());
        kittensCard.setCardFunction(cardInfos.getCardFunc());
        F30291.F302913S2C.Builder response = F30291.F302913S2C.newBuilder();
        kittensRoom.setDesktopCardInfo(kittensCard);
        kittensRoom.playCards(kittensCard.getCardId(), packet.userId);
        if (kittensRoom.getCursesNum() > 0) {
          response.setRemainingCurse(kittensRoom.getCursesNum());
        }
        if (CollectionUtils.isNotEmpty(kittensRoom.optionalPlayersThree(packet.userId))) {
          response.addAllSkillUserIds(kittensRoom.optionalPlayersThree(packet.userId));
        }
        response.setResult(0);
        response.setPlayCardId(cardInfos);
        response.setOptionUserID(packet.userId);
        response.setTimes(15);
        response.setDirection(kittensRoom.getDirection());
        response.setNextUserId(packet.userId);
        ExplodingKittensPlayer actionPlayer = kittensRoom.getPlayerInfo(packet.userId);
        List<ExplodingKittensCard> actionPlayerCard = actionPlayer.getPlayerCard();
        if (CollectionUtils.isNotEmpty(actionPlayerCard)) {
          F30291.CardInfos.Builder handCard;
          for (ExplodingKittensCard explodingKittensCard : actionPlayerCard) {
            handCard = F30291.CardInfos.newBuilder();
            handCard.setHandCardID(explodingKittensCard.getCardId());
            handCard.setCardFunc(explodingKittensCard.getCardFunction());
            response.addHandCardId(handCard);
          }
        } else {
          response.addAllHandCardId(F30291.F302913S2C.newBuilder().getHandCardIdList());
        }
        response.setRemainingCard(kittensRoom.getRemainingCard().size());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYERS_PLAY_CARDS,
                response.build().toByteArray()), kittensRoom.getRoomId());
        ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
        if (response.getSkillUserIdsList().size() == 0) {
          if (nextPlayer.getLinkStatus() == 0) {
            actionTimeout(kittensRoom.getRoomId());
          } else {
            trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
          }
        } else {
          if (nextPlayer.getLinkStatus() == 0) {
            // 交换定时
            exchangeTimeout(kittensRoom.getRoomId());
          } else {
            trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 6);
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 选择玩家.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/31 15:47
   * @update 2020/8/31 15:47
   */
  private void choosePlayer(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        ExplodingKittensPlayer checkPlayer = kittensRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          if (checkPlayer.getPlayerStatus() == ExplodingKittensAssets.getInt(ExplodingKittensAssets.PLAYER_IN_GAME)) {
            F30291.F302914C2S request = F30291.F302914C2S.parseFrom(packet.bytes);
            F30291.F302914S2C.Builder response = F30291.F302914S2C.newBuilder();
            ExplodingKittensCard desktopCardInfo = kittensRoom.getDesktopCardInfo();
            if (Objects.equals(kittensRoom.getNowActionPlayer(), packet.userId)) {
              if (desktopCardInfo.getCardFunction() == ExplodingKittensAssets.getInt(ExplodingKittensAssets.CARD_FUNC_09)) {
                List<Long> usePlayerList = kittensRoom.optionalPlayersOne();
                if (usePlayerList.contains(request.getUseUserID())) {
                  kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.CURSE_TIME);
                  response.setResult(0);
                  response.setActionUserId(packet.userId);
                  response.setSkillIndex(desktopCardInfo.getCardFunction());
                  response.setBeUseUserID(request.getUseUserID());
                  kittensRoom.setCursesNum(kittensRoom.getCursesNum());
                  kittensRoom.setCursedPlayer(request.getUseUserID());
                  kittensRoom.setNowActionPlayer(request.getUseUserID());
                  response.setTimes(15);
                  response.setRemainingCurse(kittensRoom.getCursesNum());
                  response.setNextUserId(kittensRoom.getNowActionPlayer());
                  ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
                  if (nextPlayer.getBondageIndex() > 0) {
                    response.setRightNowTouchCard(1);
                  } else {
                    response.setRightNowTouchCard(2);
                  }
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_PLAYER,
                          response.build().toByteArray()), kittensRoom.getRoomId());
                  if (nextPlayer.getLinkStatus() == 0) {
                    actionTimeout(kittensRoom.getRoomId());
                  } else {
                    trusteeshipTimeout(packet.roomId, request.getUseUserID(), 0);
                  }
                } else {
                  response.setResult(1);
                  response.setActionUserId(packet.userId);
                  response.setSkillIndex(desktopCardInfo.getCardFunction());
                  response.setBeUseUserID(request.getUseUserID());
                  channel.writeAndFlush(
                      new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_PLAYER,
                          response.build().toByteArray()));
                }
              } else if (desktopCardInfo.getCardFunction() == ExplodingKittensAssets.getInt(ExplodingKittensAssets.CARD_FUNC_10)) {
                List<Long> usePlayerList = kittensRoom.optionalPlayersOne();
                if (usePlayerList.contains(request.getUseUserID())) {
                  kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.CURSE_TIME);
                  response.setResult(0);
                  response.setActionUserId(packet.userId);
                  response.setSkillIndex(desktopCardInfo.getCardFunction());
                  response.setBeUseUserID(request.getUseUserID());
                  kittensRoom.setCursesNum(kittensRoom.getCursesNum());
                  kittensRoom.setCursedPlayer(request.getUseUserID());
                  kittensRoom.setNowActionPlayer(request.getUseUserID());
                  response.setTimes(15);
                  response.setRemainingCurse(kittensRoom.getCursesNum());
                  response.setNextUserId(kittensRoom.getNowActionPlayer());
                  ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
                  if (nextPlayer.getBondageIndex() > 0) {
                    response.setRightNowTouchCard(1);
                  } else {
                    response.setRightNowTouchCard(2);
                  }
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_PLAYER,
                          response.build().toByteArray()), kittensRoom.getRoomId());
                  if (nextPlayer.getLinkStatus() == 0) {
                    actionTimeout(kittensRoom.getRoomId());
                  } else {
                    trusteeshipTimeout(packet.roomId, request.getUseUserID(), 0);
                  }
                } else {
                  response.setResult(1);
                  response.setActionUserId(packet.userId);
                  response.setSkillIndex(desktopCardInfo.getCardFunction());
                  response.setBeUseUserID(request.getUseUserID());
                  channel.writeAndFlush(
                      new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_PLAYER,
                          response.build().toByteArray()));
                }
              } else if (desktopCardInfo.getCardFunction() == ExplodingKittensAssets.getInt(ExplodingKittensAssets.CARD_FUNC_11)) {
                List<Long> usePlayerList = kittensRoom.optionalPlayersThree(kittensRoom.getNowActionPlayer());
                if (usePlayerList.contains(request.getUseUserID())) {
                  kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.PRAY_TIME);
                  response.setResult(0);
                  response.setActionUserId(packet.userId);
                  response.setSkillIndex(desktopCardInfo.getCardFunction());
                  response.setBeUseUserID(request.getUseUserID());
                  kittensRoom.setPrayForPlayers(request.getUseUserID());
                  response.setNextUserId(request.getUseUserID());
                  response.setTimes(15);
                  if (kittensRoom.getCursesNum() > 0) {
                    response.setRemainingCurse(kittensRoom.getCursesNum());
                  }
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_PLAYER,
                          response.build().toByteArray()), kittensRoom.getRoomId());
                  ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(request.getUseUserID());
                  if (nextPlayer.getLinkStatus() == 0) {
                    // 玩家成就.有求必应
                    Map<String, Object> taskSuc0039 = Maps.newHashMap();
                    taskSuc0039.put("userId", request.getUseUserID());
                    taskSuc0039.put("code", AchievementEnum.AMT0039.getCode());
                    taskSuc0039.put("progress", 1);
                    taskSuc0039.put("isReset", 0);
                    this.userRemote.achievementHandlers(taskSuc0039);
                    deliverTimeout(kittensRoom.getRoomId());
                  } else {
                    trusteeshipTimeout(packet.roomId, request.getUseUserID(), 2);
                  }
                } else {
                  response.setResult(1);
                  response.setActionUserId(packet.userId);
                  response.setSkillIndex(desktopCardInfo.getCardFunction());
                  response.setBeUseUserID(request.getUseUserID());
                  channel.writeAndFlush(
                      new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_PLAYER,
                          response.build().toByteArray()));
                }
              } else if (desktopCardInfo.getCardFunction() == ExplodingKittensAssets.getInt(ExplodingKittensAssets.CARD_FUNC_12)) {
                List<Long> usePlayerList = kittensRoom.optionalPlayersTwo(kittensRoom.getNowActionPlayer());
                if (usePlayerList.contains(request.getUseUserID())) {
                  kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.MANACLE_TIME);
                  response.setResult(0);
                  response.setActionUserId(packet.userId);
                  response.setSkillIndex(desktopCardInfo.getCardFunction());
                  response.setBeUseUserID(request.getUseUserID());
                  response.setTimes(15);
                  if (kittensRoom.getCursesNum() > 0) {
                    response.setRemainingCurse(kittensRoom.getCursesNum());
                    response.setNextUserId(kittensRoom.getNowActionPlayer());
                  } else {
                    kittensRoom.setActionPlayer(kittensRoom.getNowActionPlayer(), kittensRoom.getDirection());
                    response.setNextUserId(kittensRoom.getNowActionPlayer());
                  }
                  ExplodingKittensPlayer targetPlayer = kittensRoom.getPlayerInfo(request.getUseUserID());
                  targetPlayer.setBondageIndex(1);
                  ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
                  if (nextPlayer.getBondageIndex() > 0) {
                    response.setRightNowTouchCard(1);
                  } else {
                    response.setRightNowTouchCard(2);
                  }
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_PLAYER,
                          response.build().toByteArray()), kittensRoom.getRoomId());
                  if (nextPlayer.getLinkStatus() == 0) {
                    actionTimeout(kittensRoom.getRoomId());
                  } else {
                    trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
                  }
                } else {
                  response.setResult(1);
                  response.setActionUserId(packet.userId);
                  response.setSkillIndex(desktopCardInfo.getCardFunction());
                  response.setBeUseUserID(request.getUseUserID());
                  channel.writeAndFlush(
                      new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_PLAYER,
                          response.build().toByteArray()));
                }
              } else if (desktopCardInfo.getCardFunction() == 13) {
                List<Long> usePlayerList = kittensRoom.optionalPlayersThree(kittensRoom.getNowActionPlayer());
                if (usePlayerList.contains(request.getUseUserID())) {
                  kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.EXCHANGE_TIME);
                  response.setResult(0);
                  response.setActionUserId(packet.userId);
                  response.setSkillIndex(desktopCardInfo.getCardFunction());
                  response.setBeUseUserID(request.getUseUserID());
                  response.setNextUserId(kittensRoom.getNowActionPlayer());
                  response.setTimes(15);
                  ExplodingKittensPlayer targetPlayer = kittensRoom.getPlayerInfo(request.getUseUserID());
                  List<ExplodingKittensCard> targetCard = targetPlayer.getPlayerCard();
                  ExplodingKittensPlayer actionPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
                  List<ExplodingKittensCard> actionCard = actionPlayer.getPlayerCard();
                  targetPlayer.setPlayerCard(actionCard);
                  actionPlayer.setPlayerCard(targetCard);
                  targetCard = targetPlayer.getPlayerCard();
                  if (CollectionUtils.isNotEmpty(targetCard)) {
                    F30291.CardInfos.Builder handCard;
                    for (ExplodingKittensCard kittensCard : targetCard) {
                      handCard = F30291.CardInfos.newBuilder();
                      handCard.setHandCardID(kittensCard.getCardId());
                      handCard.setCardFunc(kittensCard.getCardFunction());
                      response.addBeUseHandCard(handCard);
                    }
                  }
                  actionCard = actionPlayer.getPlayerCard();
                  if (CollectionUtils.isNotEmpty(actionCard)) {
                    F30291.CardInfos.Builder handCard;
                    for (ExplodingKittensCard kittensCard : actionCard) {
                      handCard = F30291.CardInfos.newBuilder();
                      handCard.setHandCardID(kittensCard.getCardId());
                      handCard.setCardFunc(kittensCard.getCardFunction());
                      response.addActionHandCard(handCard);
                    }
                  }
                  if (kittensRoom.getCursesNum() > 0) {
                    response.setRemainingCurse(kittensRoom.getCursesNum());
                  }
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_PLAYER,
                          response.build().toByteArray()), kittensRoom.getRoomId());
                  if (actionPlayer.getLinkStatus() == 0) {
                    actionTimeout(kittensRoom.getRoomId());
                  } else {
                    trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
                  }
                } else {
                  response.setResult(1);
                  response.setActionUserId(packet.userId);
                  response.setSkillIndex(desktopCardInfo.getCardFunction());
                  response.setBeUseUserID(request.getUseUserID());
                  channel.writeAndFlush(
                      new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_PLAYER,
                          response.build().toByteArray()));
                }
              }
            } else {
              response.setResult(1);
              response.setActionUserId(packet.userId);
              response.setSkillIndex(desktopCardInfo.getCardFunction());
              response.setBeUseUserID(request.getUseUserID());
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_PLAYER,
                      response.build().toByteArray()));
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
   * TODO 赠送卡牌.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/31 15:49
   * @update 2020/8/31 15:49
   */
  private void givePlayerCard(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        ExplodingKittensPlayer checkPlayer = kittensRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          if (checkPlayer.getPlayerStatus() == ExplodingKittensAssets.getInt(ExplodingKittensAssets.PLAYER_IN_GAME)) {
            if (Objects.equals(kittensRoom.getPrayForPlayers(), packet.userId)) {
              F30291.F302915C2S request = F30291.F302915C2S.parseFrom(packet.bytes);
              F30291.CardInfos cardInfos = request.getGiveCardId();
              if (kittensRoom.verifyCardIsExists(cardInfos.getHandCardID(), packet.userId)) {
                kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.GIVE_PLAYER_CARD);
                F30291.F302915S2C.Builder response = F30291.F302915S2C.newBuilder();
                ExplodingKittensCard kittensCard = new ExplodingKittensCard();
                kittensCard.setCardId(cardInfos.getHandCardID());
                kittensCard.setCardFunction(cardInfos.getCardFunc());
                ExplodingKittensPlayer givePlayer = kittensRoom.getPlayerInfo(packet.userId);
                givePlayer.removeCard(cardInfos.getHandCardID());
                F30291.CardInfos.Builder handCard;
                List<ExplodingKittensCard> givePlayerCards = givePlayer.getPlayerCard();
                if (CollectionUtils.isNotEmpty(givePlayerCards)) {
                  for (ExplodingKittensCard card : givePlayerCards) {
                    handCard = F30291.CardInfos.newBuilder();
                    handCard.setHandCardID(card.getCardId());
                    handCard.setCardFunc(card.getCardFunction());
                    response.addGiveHandCardid(handCard);
                  }
                }
                ExplodingKittensPlayer nowPlayer = kittensRoom
                    .getPlayerInfo(kittensRoom.getNowActionPlayer());
                nowPlayer.getPlayerCard().add(kittensCard);
                List<ExplodingKittensCard> nowPlayerCards = nowPlayer.getPlayerCard();
                for (ExplodingKittensCard card : nowPlayerCards) {
                  handCard = F30291.CardInfos.newBuilder();
                  handCard.setHandCardID(card.getCardId());
                  handCard.setCardFunc(card.getCardFunction());
                  response.addReceiveHandCardid(handCard);
                }
                response.setGiveUsrdId(packet.userId);
                response.setReceiveUserId(kittensRoom.getNowActionPlayer());
                response.setTimes(15);
                response.setActionUserId(kittensRoom.getNowActionPlayer());
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.GIVE_PLAYER_CARD,
                        response.build().toByteArray()), kittensRoom.getRoomId());
                kittensRoom.setPrayForPlayers(0L);
                ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
                if (nextPlayer.getLinkStatus() == 0) {
                  actionTimeout(kittensRoom.getRoomId());
                } else {
                  trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
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
   * TODO 玩家摸牌.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/31 15:55
   * @update 2020/8/31 15:55
   */
  private void chooseTouchCard(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        ExplodingKittensPlayer checkPlayer = kittensRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          if (checkPlayer.getPlayerStatus() == ExplodingKittensAssets.getInt(ExplodingKittensAssets.PLAYER_IN_GAME)) {
            if (Objects.equals(kittensRoom.getNowActionPlayer(), packet.userId)) {
              kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.PLAYERS_PLAY_CARDS);
              F30291.F302916S2C.Builder response = F30291.F302916S2C.newBuilder();
              ExplodingKittensPlayer actionPlayer = kittensRoom.getPlayerInfo(packet.userId);
              actionPlayer.setBondageIndex(0);
              // 消除诅咒
              kittensRoom.eliminateCurse();
              response.setUserID(packet.userId);
              ExplodingKittensCard newKittensCard = kittensRoom.getRemainingCard().remove(0);
              if (newKittensCard.getCardFunction() == 1) {
                checkPlayer.setTouchExploding(1);
                kittensRoom.setExplodingCardInfo(newKittensCard);
                // 是否有保命牌
                List<ExplodingKittensCard> actionPlayerCard = actionPlayer.getPlayerCard();
                List<ExplodingKittensCard> kittensCardList = actionPlayerCard.stream()
                    .filter(card -> card.getCardFunction() == 2)
                    .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(kittensCardList)) {
                  response.setIsExplosion(2);
                  response.setNextUserId(packet.userId);
                  response.setTimes(15);
                  response.setBombPro(kittensRoom.getProbability());
                  response.setRemainingCard(kittensRoom.getRemainingCard().size());
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_TOUCH_CARD,
                          response.build().toByteArray()), kittensRoom.getRoomId());
                  // 立刻拆弹
                  useBombDisposalCards(packet.roomId, packet.userId, kittensCardList.remove(0));
                } else {
                  long nextId = kittensRoom.getNextActionPlayer(packet.userId);
                  // 下一个玩家 当前玩家出局 炸弹-1
                  kittensRoom.playerOut(packet.userId);
                  if (kittensRoom.getRankingList().size() < 4) {
                    kittensRoom.setNowActionPlayer(nextId);
                    response.setNextUserId(kittensRoom.getNowActionPlayer());
                    response.setTimes(15);
                    ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
                    if (nextPlayer.getBondageIndex() > 0) {
                      response.setRightNowTouchCard(1);
                    } else {
                      response.setRightNowTouchCard(2);
                    }
                    kittensRoom.explodingProbability();
                    response.setBombPro(kittensRoom.getProbability());
                    response.setRemainingCurse(0);
                    // 重置诅咒
                    kittensRoom.setCursesNum(0);
                    kittensRoom.setCursedPlayer(0L);
                    response.setIsExplosion(1);
                    response.setRemainingCard(kittensRoom.getRemainingCard().size());
                    GroupManager.sendPacketToGroup(
                        new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_TOUCH_CARD,
                            response.build().toByteArray()), kittensRoom.getRoomId());
                    if (nextPlayer.getLinkStatus() == 0) {
                      actionTimeout(kittensRoom.getRoomId());
                    } else {
                      trusteeshipTimeout(packet.roomId, nextPlayer.getPlayerId(), 0);
                    }
                    outShowTimeout(kittensRoom.getRoomId(), packet.userId);
                  } else {
                    kittensRoom.destroy();
                    response.setNextUserId(0L);
                    response.setTimes(0);
                    response.setBombPro(kittensRoom.getProbability());
                    response.setRemainingCurse(0);
                    // 重置诅咒
                    kittensRoom.setCursesNum(0);
                    kittensRoom.setCursedPlayer(0L);
                    response.setIsExplosion(1);
                    response.setRemainingCard(kittensRoom.getRemainingCard().size());
                    GroupManager.sendPacketToGroup(
                        new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_TOUCH_CARD,
                            response.build().toByteArray()), kittensRoom.getRoomId());
                    gameFinishTimeout(kittensRoom.getRoomId());
                  }
                }
              } else {
                actionPlayer.getPlayerCard().add(newKittensCard);
                List<ExplodingKittensCard> actionPlayerCard = actionPlayer.getPlayerCard();
                if (CollectionUtils.isNotEmpty(actionPlayerCard)) {
                  F30291.CardInfos.Builder handCard;
                  for (ExplodingKittensCard kittensCard : actionPlayerCard) {
                    handCard = F30291.CardInfos.newBuilder();
                    handCard.setCardFunc(kittensCard.getCardFunction());
                    handCard.setHandCardID(kittensCard.getCardId());
                    response.addHandCardId(handCard);
                  }
                }
                kittensRoom.explodingProbability();
                response.setBombPro(kittensRoom.getProbability());
                if (kittensRoom.getCursesNum() > 0) {
                  response.setRemainingCurse(kittensRoom.getCursesNum());
                  response.setNextUserId(packet.userId);
                } else {
                  kittensRoom.setActionPlayer(kittensRoom.getNowActionPlayer(), kittensRoom.getDirection());
                  response.setNextUserId(kittensRoom.getNowActionPlayer());
                }
                response.setTimes(15);
                ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
                if (nextPlayer.getBondageIndex() > 0) {
                  response.setRightNowTouchCard(1);
                } else {
                  response.setRightNowTouchCard(2);
                }
                response.setIsExplosion(2);
                response.setRemainingCard(kittensRoom.getRemainingCard().size());
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_TOUCH_CARD,
                        response.build().toByteArray()), kittensRoom.getRoomId());
                if (nextPlayer.getLinkStatus() == 0) {
                  actionTimeout(kittensRoom.getRoomId());
                } else {
                  trusteeshipTimeout(packet.roomId, nextPlayer.getPlayerId(), 0);
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
   * TODO 玩家摸牌.
   *
   * @param roomId [房间ID]
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 13:25
   * @update 2020/9/7 13:25
   */
  private void rightNowTouchCard(Long roomId, Long playerId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        if (Objects.equals(kittensRoom.getNowActionPlayer(), playerId)) {
          kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.PLAYERS_PLAY_CARDS);
          F30291.F302916S2C.Builder response = F30291.F302916S2C.newBuilder();
          ExplodingKittensPlayer actionPlayer = kittensRoom.getPlayerInfo(playerId);
          actionPlayer.setBondageIndex(0);
          // 消除诅咒
          kittensRoom.eliminateCurse();
          response.setUserID(playerId);
          ExplodingKittensCard newKittensCard = kittensRoom.getRemainingCard().remove(0);
          if (newKittensCard.getCardFunction() == 1) {
            actionPlayer.setTouchExploding(1);
            kittensRoom.setExplodingCardInfo(newKittensCard);
            // 是否有保命牌
            List<ExplodingKittensCard> actionPlayerCard = actionPlayer.getPlayerCard();
            List<ExplodingKittensCard> kittensCardList = actionPlayerCard.stream()
                .filter(card -> card.getCardFunction() == 2)
                .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(kittensCardList)) {
              response.setIsExplosion(2);
              response.setNextUserId(playerId);
              response.setTimes(15);
              response.setBombPro(kittensRoom.getProbability());
              response.setRemainingCard(kittensRoom.getRemainingCard().size());
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_TOUCH_CARD,
                      response.build().toByteArray()), kittensRoom.getRoomId());
              // 立刻拆弹
              useBombDisposalCards(roomId, playerId, kittensCardList.get(0));
            } else {
              long nextId = kittensRoom.getNextActionPlayer(playerId);
              // 下一个玩家 当前玩家出局 炸弹-1
              kittensRoom.playerOut(playerId);
              if (kittensRoom.getRankingList().size() < 4) {
                kittensRoom.setNowActionPlayer(nextId);
                response.setNextUserId(kittensRoom.getNowActionPlayer());
                response.setTimes(15);
                ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
                if (nextPlayer.getBondageIndex() > 0) {
                  response.setRightNowTouchCard(1);
                } else {
                  response.setRightNowTouchCard(2);
                }
                kittensRoom.explodingProbability();
                response.setBombPro(kittensRoom.getProbability());
                response.setRemainingCurse(0);
                // 重置诅咒
                kittensRoom.setCursesNum(0);
                kittensRoom.setCursedPlayer(0L);
                response.setIsExplosion(1);
                response.setRemainingCard(kittensRoom.getRemainingCard().size());
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_TOUCH_CARD,
                        response.build().toByteArray()), kittensRoom.getRoomId());
                if (nextPlayer.getLinkStatus() == 0) {
                  actionTimeout(kittensRoom.getRoomId());
                } else {
                  trusteeshipTimeout(roomId, kittensRoom.getNowActionPlayer(), 0);
                }
                outShowTimeout(kittensRoom.getRoomId(), playerId);
              } else {
                kittensRoom.destroy();
                response.setNextUserId(0L);
                response.setTimes(0);
                response.setBombPro(kittensRoom.getProbability());
                response.setRemainingCurse(0);
                // 重置诅咒
                kittensRoom.setCursesNum(0);
                kittensRoom.setCursedPlayer(0L);
                response.setIsExplosion(1);
                response.setRemainingCard(kittensRoom.getRemainingCard().size());
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_TOUCH_CARD,
                        response.build().toByteArray()), kittensRoom.getRoomId());
                gameFinishTimeout(kittensRoom.getRoomId());
              }
            }
          } else {
            actionPlayer.getPlayerCard().add(newKittensCard);
            List<ExplodingKittensCard> actionPlayerCard = actionPlayer.getPlayerCard();
            if (CollectionUtils.isNotEmpty(actionPlayerCard)) {
              F30291.CardInfos.Builder handCard;
              for (ExplodingKittensCard kittensCard : actionPlayerCard) {
                handCard = F30291.CardInfos.newBuilder();
                handCard.setCardFunc(kittensCard.getCardFunction());
                handCard.setHandCardID(kittensCard.getCardId());
                response.addHandCardId(handCard);
              }
            }
            kittensRoom.explodingProbability();
            response.setBombPro(kittensRoom.getProbability());
            if (kittensRoom.getCursesNum() > 0) {
              response.setRemainingCurse(kittensRoom.getCursesNum());
              response.setNextUserId(playerId);
            } else {
              kittensRoom.setActionPlayer(kittensRoom.getNowActionPlayer(), kittensRoom.getDirection());
              response.setNextUserId(kittensRoom.getNowActionPlayer());
            }
            response.setTimes(15);
            ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
            if (nextPlayer.getBondageIndex() > 0) {
              response.setRightNowTouchCard(1);
            } else {
              response.setRightNowTouchCard(2);
            }
            response.setIsExplosion(2);
            response.setRemainingCard(kittensRoom.getRemainingCard().size());
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_TOUCH_CARD,
                    response.build().toByteArray()), kittensRoom.getRoomId());
            if (nextPlayer.getLinkStatus() == 0) {
              actionTimeout(kittensRoom.getRoomId());
            } else {
              trusteeshipTimeout(roomId, kittensRoom.getNowActionPlayer(), 0);
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
   * @date 2020/8/31 15:57
   * @update 2020/8/31 15:57
   */
  private void joinOrWatch(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        ExplodingKittensPlayer checkPlayer = kittensRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30291.F302918C2S request = F30291.F302918C2S.parseFrom(packet.bytes);
          F30291.F302918S2C.Builder response = F30291.F302918S2C.newBuilder();
          if (kittensRoom.getRoomStatus() == 1) {
            response.setResult(1).setStand(request.getIsStand());
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.JOIN_OR_WATCH,
                    response.build().toByteArray()));
          } else {
            // 玩家操作 0-站起 1-坐下
            if (request.getIsStand() == 0) {
              becomeAudience(channel, packet);
            } else {
              becomePlayer(channel, packet);
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
   * TODO 成为观众.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/19 13:19
   * @update 2020/8/19 13:19
   */
  private void becomeAudience(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        F30291.F302918C2S request = F30291.F302918C2S.parseFrom(packet.bytes);
        F30291.F302918S2C.Builder response = F30291.F302918S2C.newBuilder();
        ExplodingKittensPlayer kittensPlayer = kittensRoom.getPlayerInfo(packet.userId);
        if (kittensPlayer.getPlayerStatus() > 0) {
          response.setResult(1).setStand(request.getIsStand());
          channel.writeAndFlush(
              new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.JOIN_OR_WATCH,
                  response.build().toByteArray()));
        } else {
          kittensRoom.cancelTimeOut((int) packet.userId);
          int seatNo = kittensRoom.leaveSeat(packet.userId);
          response.setResult(0).setStand(request.getIsStand()).setStand(seatNo);
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.JOIN_OR_WATCH,
                  response.build().toByteArray()), kittensRoom.getRoomId());
          audienceInfo(channel, packet);
          roomOpenOrCancelMatch(kittensRoom.getRoomId());
          // 清理检测
          List<ExplodingKittensPlayer> playerList = kittensRoom.getPlayerList();
          int playGameSize = (int) playerList.stream()
              .filter(s -> s.getPlayerId() > 0).count();
          if (playGameSize == 0) {
            clearTimeout(kittensRoom.getRoomId());
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 成为玩家.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/19 13:19
   * @update 2020/8/19 13:19
   */
  private void becomePlayer(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        F30291.F302918C2S request = F30291.F302918C2S.parseFrom(packet.bytes);
        F30291.F302918S2C.Builder response = F30291.F302918S2C.newBuilder();
        if (kittensRoom.remainingSeat() > 0) {
          ExplodingKittensPlayer kittensPlayer = kittensRoom.getPlayerInfo(packet.userId);
          if (kittensPlayer.getIdentity() != 1) {
            response.setResult(1).setStand(request.getIsStand());
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.JOIN_OR_WATCH,
                    response.build().toByteArray()));
          } else if (kittensPlayer.getPlayerGold() < 20) {
            response.setResult(2).setStand(request.getIsStand());
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.JOIN_OR_WATCH,
                    response.build().toByteArray()));
          } else {
            // 销毁清理定时
            kittensRoom.cancelTimeOut((int) packet.roomId);
            int seatNo = kittensRoom.joinSeat(packet.userId);
            response.setResult(0).setStand(request.getIsStand()).setStand(seatNo);
            kittensPlayer = kittensRoom.getPlayerInfo(packet.userId);
            F30291.PlayerInfo.Builder playerInfo = F30291.PlayerInfo.newBuilder();
            playerInfo.setNick(kittensPlayer.getPlayerName());
            playerInfo.setUserID(kittensPlayer.getPlayerId());
            playerInfo.setUrl(kittensPlayer.getPlayerAvatar());
            playerInfo.setSex(kittensPlayer.getPlayerSex());
            playerInfo.setDeviPosition(kittensPlayer.getSeatNumber());
            playerInfo.setState(kittensPlayer.getPlayerStatus());
            playerInfo.setCoin(kittensPlayer.getPlayerGold());
            if (StringUtils.isNotEmpty(kittensPlayer.getAvatarFrame())) {
              playerInfo.setUrlFrame(kittensPlayer.getAvatarFrame());
            }
            if (StringUtils.isNotEmpty(kittensPlayer.getCardBackSkin())) {
              F30291.GameSkin.Builder gameSkin = F30291.GameSkin.newBuilder();
              playerInfo.setGameSkin(gameSkin.setCardBack(kittensPlayer.getCardBackSkin()));
            }
            playerInfo.setReadyTime(15);
            response.setJoinInfo(playerInfo);
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.JOIN_OR_WATCH,
                    response.build().toByteArray()), kittensRoom.getRoomId());
            audienceInfo(channel, packet);
            roomOpenOrCancelMatch(kittensRoom.getRoomId());
          }
        } else {
          response.setResult(1).setStand(request.getIsStand());
          channel.writeAndFlush(
              new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.JOIN_OR_WATCH,
                  response.build().toByteArray()));
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 观众信息.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/31 15:59
   * @update 2020/8/31 15:59
   */
  private void audienceInfo(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        ExplodingKittensPlayer checkPlayer = kittensRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          ExplodingKittensPlayer kittensPlayer = kittensRoom.getPlayerInfo(packet.userId);
          F30291.PlayerInfo.Builder playerInfo = F30291.PlayerInfo.newBuilder();
          playerInfo.setNick(kittensPlayer.getPlayerName());
          playerInfo.setUserID(kittensPlayer.getPlayerId());
          playerInfo.setUrl(kittensPlayer.getPlayerAvatar());
          playerInfo.setSex(kittensPlayer.getPlayerSex());
          playerInfo.setDeviPosition(kittensPlayer.getSeatNumber());
          playerInfo.setState(kittensPlayer.getPlayerStatus());
          playerInfo.setCoin(kittensPlayer.getPlayerGold());
          F30291.F302919S2C.Builder response = F30291.F302919S2C.newBuilder();
          response.setNowUser(playerInfo);
          if (kittensRoom.getRoomStatus() == 0) {
            if (kittensPlayer.getPlayerStatus() >= 1) {
              response.setIsCanAction(false);
            } else {
              response.setIsCanAction(true);
            }
          } else {
            response.setIsCanAction(false);
          }
          response.setNowStatus(kittensPlayer.getIdentity());
          if (kittensPlayer.getIdentity() == 0) {
            response.addAllWatchUser(kittensRoom.getAudienceIcon());
          } else {
            response.addAllWatchUser(kittensRoom.getAudienceIcon()
                .stream()
                .filter(icon -> !Objects.equals(icon, kittensPlayer.getPlayerAvatar()))
                .collect(Collectors.toList()));
          }
          channel.writeAndFlush(
              new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.AUDIENCE_INFO,
                  response.build().toByteArray()));
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
   * TODO 观众信息.
   *
   * @param roomId [房间ID]
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/31 15:59
   * @update 2020/8/31 15:59
   */
  private void audienceInfo(Long roomId, Long playerId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        ExplodingKittensPlayer kittensPlayer = kittensRoom.getPlayerInfo(playerId);
        F30291.PlayerInfo.Builder playerInfo = F30291.PlayerInfo.newBuilder();
        playerInfo.setNick(kittensPlayer.getPlayerName());
        playerInfo.setUserID(kittensPlayer.getPlayerId());
        playerInfo.setUrl(kittensPlayer.getPlayerAvatar());
        playerInfo.setSex(kittensPlayer.getPlayerSex());
        playerInfo.setDeviPosition(kittensPlayer.getSeatNumber());
        playerInfo.setState(kittensPlayer.getPlayerStatus());
        playerInfo.setCoin(kittensPlayer.getPlayerGold());
        F30291.F302919S2C.Builder response = F30291.F302919S2C.newBuilder();
        response.setNowUser(playerInfo);
        if (kittensRoom.getRoomStatus() == 0) {
          if (kittensPlayer.getPlayerStatus() >= 1) {
            response.setIsCanAction(false);
          } else {
            response.setIsCanAction(true);
          }
        } else {
          response.setIsCanAction(false);
        }
        response.setNowStatus(kittensPlayer.getIdentity());
        if (kittensPlayer.getIdentity() == 0) {
          response.addAllWatchUser(kittensRoom.getAudienceIcon());
        } else {
          response.addAllWatchUser(kittensRoom.getAudienceIcon()
              .stream()
              .filter(icon -> Objects.equals(icon, kittensPlayer.getPlayerAvatar()))
              .collect(Collectors.toList()));
        }
        kittensPlayer.sendPacket(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.AUDIENCE_INFO,
                response.build().toByteArray()));
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 玩家离开.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/31 16:02
   * @update 2020/8/31 16:02
   */
  private void playerLeaves(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        ExplodingKittensPlayer checkPlayer = kittensRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          // 房间状态 [0-未开始 1-已开始]
          if (kittensRoom.getRoomStatus() == 1) {
            // 玩家身份 [0-玩家 1-观众]
            if (checkPlayer.getIdentity() == 0) {
              if (checkPlayer.getPlayerStatus() == 2) {
                // 中途退出 扣除20金币
                gainExperience(packet.userId, 0, -20);
              }
              // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
              if (checkPlayer.getPlayerStatus() == 2) {
                checkPlayer.setPlayerStatus(4);
              }
              // 连接状态 [0-连接中 1-已断开]
              checkPlayer.setLinkStatus(1);
              checkPlayer.setChannel(null);
              // 关闭页面
              closePage(channel, packet);
              // 加入记录
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              F30291.F3029110S2C.Builder response = F30291.F3029110S2C.newBuilder();
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYER_LEAVES,
                      response.setUserID(packet.userId).build().toByteArray()), kittensRoom.getRoomId());
              this.gameRemote.leaveRoom(packet.userId);
              //               断线玩家                          入座玩家
              if ((kittensRoom.seatedPlayersIsDisconnected() == kittensRoom.seatedPlayersNum())
                  && kittensRoom.seatedPlayers().size() == 0
                  && kittensRoom.getAudienceList().size() == 0) {
                clearData(kittensRoom.getRoomId());
              }
            } else {
              // 删除数据
              kittensRoom.leaveGame(packet.userId, 1);
              // 关闭页面
              closePage(channel, packet);
              // 加入记录
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              this.gameRemote.leaveRoom(packet.userId);
              //               断线玩家                          入座玩家
              if ((kittensRoom.seatedPlayersIsDisconnected() == kittensRoom.seatedPlayersNum())
                  && kittensRoom.seatedPlayers().size() == 0
                  && kittensRoom.getAudienceList().size() == 0) {
                clearData(kittensRoom.getRoomId());
              }
            }
          } else {
            if (checkPlayer.getIdentity() == 0) {
              // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
              if (checkPlayer.getPlayerStatus() == 0) {
                kittensRoom.cancelTimeOut((int) packet.userId);
              } else if (checkPlayer.getPlayerStatus() == 1) {
                if (kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.START_LICENSING)) {
                  kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.START_LICENSING);
                }
              }
              kittensRoom.leaveGame(packet.userId, 0);
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              this.gameRemote.leaveRoom(packet.userId);
              F30291.F3029110S2C.Builder response = F30291.F3029110S2C.newBuilder();
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYER_LEAVES,
                      response.setUserID(packet.userId).build().toByteArray()), kittensRoom.getRoomId());
              if (kittensRoom.seatedPlayers().size() == 0 && kittensRoom.getAudienceList().size() == 0) {
                clearData(kittensRoom.getRoomId());
              } else {
                roomOpenOrCancelMatch(kittensRoom.getRoomId());
                // 清理检测
                List<ExplodingKittensPlayer> playerList = kittensRoom.getPlayerList();
                int playGameSize = (int) playerList.stream()
                    .filter(s -> s.getPlayerId() > 0).count();
                if (playGameSize == 0) {
                  clearTimeout(kittensRoom.getRoomId());
                }
              }
            } else {
              kittensRoom.leaveGame(packet.userId, 1);
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              this.gameRemote.leaveRoom(packet.userId);
              if ((kittensRoom.seatedPlayersIsDisconnected() == kittensRoom.seatedPlayersNum())
                  && kittensRoom.seatedPlayers().size() == 0
                  && kittensRoom.getAudienceList().size() == 0) {
                clearData(kittensRoom.getRoomId());
              } else if (kittensRoom.seatedPlayers().size() == 0
                  && kittensRoom.getAudienceList().size() == 0) {
                clearData(kittensRoom.getRoomId());
              }
            }
          }
        } else {
          closePage(channel, packet);
          if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
            this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
          }
          this.gameRemote.leaveRoom(packet.userId);
          if ((kittensRoom.seatedPlayersIsDisconnected() == kittensRoom.seatedPlayersNum())
              && kittensRoom.seatedPlayers().size() == 0
              && kittensRoom.getAudienceList().size() == 0) {
            clearData(kittensRoom.getRoomId());
          } else if (kittensRoom.seatedPlayers().size() == 0
              && kittensRoom.getAudienceList().size() == 0) {
            clearData(kittensRoom.getRoomId());
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
   * TODO 放置炸弹.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/31 16:05
   * @update 2020/8/31 16:05
   */
  private void placeExploding(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        ExplodingKittensPlayer checkPlayer = kittensRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          if (checkPlayer.getPlayerStatus() == ExplodingKittensAssets.getInt(ExplodingKittensAssets.PLAYER_IN_GAME)) {
            if (Objects.equals(kittensRoom.getNowActionPlayer(), packet.userId)) {
              kittensRoom.cancelTimeOut((int) ExplodingKittensCmd.PLACE_EXPLODING);
              F30291.F3029113C2S request = F30291.F3029113C2S.parseFrom(packet.bytes);
              kittensRoom.placeExploding(request.getSelect());
              F30291.F3029113S2C.Builder response = F30291.F3029113S2C.newBuilder();
              kittensRoom.setActionPlayer(kittensRoom.getNowActionPlayer(), kittensRoom.getDirection());
              response.setNextUserId(kittensRoom.getNowActionPlayer());
              response.setTimes(15);
              response.setBombPro(kittensRoom.getProbability());
              ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
              if (nextPlayer.getBondageIndex() > 0) {
                response.setRightNowTouchCard(1);
              } else {
                response.setRightNowTouchCard(2);
              }
              response.setRemainingCard(kittensRoom.getRemainingCard().size());
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLACE_EXPLODING,
                      response.build().toByteArray()), kittensRoom.getRoomId());
              if (nextPlayer.getLinkStatus() == 0) {
                actionTimeout(kittensRoom.getRoomId());
              } else {
                trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
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
   * TODO 断线重连.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/17 10:05
   * @update 2020/8/17 10:05
   */
  private void disconnected(Channel channel, Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        ExplodingKittensPlayer checkPlayer = kittensRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30291.F3029111S2C.Builder response = F30291.F3029111S2C.newBuilder();
          List<ExplodingKittensPlayer> kittensPlayers = kittensRoom.getPlayerList();
          if (CollectionUtils.isNotEmpty(kittensPlayers)) {
            F30291.PlayerInfo.Builder playerInfo;
            for (ExplodingKittensPlayer player : kittensPlayers) {
              playerInfo = F30291.PlayerInfo.newBuilder();
              playerInfo.setNick(player.getPlayerName());
              playerInfo.setUserID(player.getPlayerId());
              playerInfo.setUrl(player.getPlayerAvatar());
              playerInfo.setSex(player.getPlayerSex());
              playerInfo.setDeviPosition(player.getSeatNumber());
              playerInfo.setState(player.getPlayerStatus());
              playerInfo.setCoin(player.getPlayerGold());
              if (StringUtils.isNotEmpty(player.getAvatarFrame())) {
                playerInfo.setUrlFrame(player.getAvatarFrame());
              }
              if (StringUtils.isNotEmpty(player.getCardBackSkin())) {
                F30291.GameSkin.Builder gameSkin = F30291.GameSkin.newBuilder();
                playerInfo.setGameSkin(gameSkin.setCardBack(player.getCardBackSkin()));
              }
              if (CollectionUtils.isNotEmpty(player.getPlayerCard())) {
                List<ExplodingKittensCard> kittensCards = player.getPlayerCard();
                F30291.CardInfos.Builder handCard;
                for (ExplodingKittensCard kittensCard : kittensCards) {
                  handCard = F30291.CardInfos.newBuilder();
                  handCard.setHandCardID(kittensCard.getCardId());
                  handCard.setCardFunc(kittensCard.getCardFunction());
                  playerInfo.addHandCard(handCard);
                }
              }
              response.addBreakLineInfo(playerInfo);
              if (player.getBondageIndex() > 0) {
                response.addBoundPlayer(player.getPlayerId());
              }
            }
          }
          if (kittensRoom.getCursesNum() > 0) {
            response.setCursedPlayer(kittensRoom.getCursedPlayer());
            response.setRemainingCurse(kittensRoom.getCursesNum());
          }
          if (kittensRoom.getPrayForPlayers() > 0) {
            response.setGivingPlayer(kittensRoom.getPrayForPlayers());
          }
          ExplodingKittensCard desktopCardInfo = kittensRoom.getDesktopCardInfo();
          if (Objects.nonNull(desktopCardInfo)) {
            F30291.CardInfos.Builder desktopCards = F30291.CardInfos.newBuilder();
            desktopCards.setCardFunc(desktopCardInfo.getCardFunction());
            desktopCards.setHandCardID(desktopCardInfo.getCardId());
            response.setDesktopCards(desktopCards);
          }
          LocalDateTime udt = kittensRoom.getActionTime().plusSeconds(15L);
          LocalDateTime nds = LocalDateTime.now();
          Duration duration = Duration.between(nds, udt);
          int second = Math.toIntExact(duration.getSeconds());
          response.setTimes(second);
          response.setActionUserId(kittensRoom.getNowActionPlayer());
          response.setBombPro(kittensRoom.getProbability());
          response.setRemainingCard(kittensRoom.getRemainingCard().size());
          response.setDirection(kittensRoom.getDirection());
          long nowActionId = kittensRoom.getNowActionPlayer();
          boolean isNowAction = nowActionId == packet.userId;
          if (isNowAction) {
            ExplodingKittensPlayer nowActionPlayer = kittensRoom.getPlayerInfo(nowActionId);
            if (nowActionPlayer.getBondageIndex() > 0) {
              response.setRightNowTouchCard(1);
            } else {
              response.setRightNowTouchCard(2);
            }
          }
          if (kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.PLACE_EXPLODING) && isNowAction) {
            int cardNum = kittensRoom.getRemainingCard().size();
            if (cardNum == 0) {
              response.setSelectNum(1);
            } else {
              if (cardNum >= 5) {
                response.setSelectNum(5);
              } else {
                response.setSelectNum(cardNum);
              }
            }
          }
          if (kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.PRAY_TIME) && isNowAction) {
            if (CollectionUtils.isNotEmpty(kittensRoom.optionalPlayersThree(packet.userId))) {
              response.addAllSkillUserIds(kittensRoom.optionalPlayersThree(packet.userId));
            }
          }
          if (kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.CURSE_TIME) && isNowAction) {
            if (CollectionUtils.isNotEmpty(kittensRoom.optionalPlayersOne())) {
              response.addAllSkillUserIds(kittensRoom.optionalPlayersOne());
            }
          }
          if (kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.MANACLE_TIME) && isNowAction) {
            if (CollectionUtils.isNotEmpty(kittensRoom.optionalPlayersTwo(packet.userId))) {
              response.addAllSkillUserIds(kittensRoom.optionalPlayersTwo(packet.userId));
            }
          }

          if (kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.EXCHANGE_TIME) && isNowAction) {
            if (CollectionUtils.isNotEmpty(kittensRoom.optionalPlayersThree(packet.userId))) {
              response.addAllSkillUserIds(kittensRoom.optionalPlayersThree(packet.userId));
            }
          }
          channel.writeAndFlush(
              new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.DISCONNECTED,
                  response.build().toByteArray()));
          F30291.F3029112S2C.Builder goldInfo = F30291.F3029112S2C.newBuilder();
          ExplodingKittensPlayer kittensPlayer = kittensRoom.getPlayerInfo(packet.userId);
          channel.writeAndFlush(
              new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.UPDATE_PLAYER_GOLD,
                  goldInfo.setPlayGold(kittensPlayer.getPlayerGold()).build().toByteArray()));
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
   * TODO 关闭加载.
   *
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/17 9:50
   * @update 2020/8/17 9:50
   */
  private void closeLoading(Long playerId) {
    try {
      SoftChannel.sendPacketToUserId(
          new Packet(ActionCmd.APP_HEART, ExplodingKittensCmd.LOADING, null), playerId);
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
   * @date 2020/8/20 15:23
   * @update 2020/8/20 15:23
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
   * TODO 刷新数据.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/17 17:44
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
   * TODO 拉取装饰.
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/17 18:27
   * @update 2020/8/17 18:27
   */
  private void pullDecorateInfo(Packet packet) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(kittensRoom)) {
        ExplodingKittensPlayer kittensPlayer = kittensRoom.getPlayerInfo(packet.userId);
        // 金币
        if (kittensRoom.getRoomId() == ExplodingKittensAssets.getLong(ExplodingKittensAssets.TEST_ID)) {
          kittensPlayer.setPlayerGold(500);
        } else {
          Result result = this.userRemote.getCurrency(packet.userId);
          if (Objects.nonNull(result)) {
            Map<String, Object> currencyInfo = JsonUtils.toObjectMap(result.getData());
            if (Objects.nonNull(currencyInfo)) {
              kittensPlayer.setPlayerGold((Integer) currencyInfo.get("gold"));
            }
          }
        }
        // 头像框
        Map<String, Object> dressInfo = this.userRemote.getUserFrame(packet.userId);
        if (Objects.nonNull(dressInfo)) {
          kittensPlayer.setAvatarFrame(StringUtils.nvl(dressInfo.get("iconFrame")));
        }
        // 卡背
        List gameDecorate = this.orderRemote
            .gameDecorate(packet.userId, ActionCmd.GAME_EXPLODING_KITTENS);
        if (CollectionUtils.isNotEmpty(gameDecorate)) {
          List<Map<String, Object>> decorateList = JsonUtils.listMap(gameDecorate);
          if (CollectionUtils.isNotEmpty(decorateList)) {
            for (Map<String, Object> decorate : decorateList) {
              Integer labelCode = (Integer) decorate.get("labelCode");
              if (labelCode != 1000 && labelCode != 2000) {
                kittensPlayer.setCardBackSkin(StringUtils.nvl(decorate.get("adornUrl")));
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
   * TODO 游玩记录.
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/17 17:57
   * @update 2020/8/17 17:57
   */
  private void joinExplodingKittensRoom(Packet packet) {
    try {
      if (packet.roomId > ExplodingKittensAssets.getLong(ExplodingKittensAssets.TEST_ID)) {
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
   * TODO 匹配控制.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/3 11:29
   * @update 2020/9/3 11:29
   */
  private void roomOpenOrCancelMatch(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        boolean testRoom = (Objects.equals(roomId, ExplodingKittensAssets.getLong(ExplodingKittensAssets.TEST_ID)));
        if (!testRoom) {
          if (kittensRoom.getOpenWay() == 0) {
            if (kittensRoom.remainingSeat() > 0) {
              MatchRoom matchRoom = new MatchRoom();
              matchRoom.setRoomId(roomId);
              matchRoom.setPeopleNum(kittensRoom.remainingSeat());
              MatchManager.refreshExplodingKittensMatch(matchRoom);
            } else {
              MatchManager.delExplodingKittensMatch(roomId);
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
   * TODO 空的玩家.
   *
   * @param channel [通信管道]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 21:05
   * @update 2020/9/7 21:05
   */
  private void playerNotExist(Channel channel) {
    F30291.F30291S2C.Builder response = F30291.F30291S2C.newBuilder();
    channel.writeAndFlush(new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.ENTER_ROOM,
        response.setResult(2).build().toByteArray()));
  }

  /**
   * TODO 空的房间.
   *
   * @param channel [通信管道]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/8 14:16
   * @update 2020/9/8 14:16
   */
  private void roomNotExist(Channel channel) {
    F30291.F30291S2C.Builder response = F30291.F30291S2C.newBuilder();
    channel.writeAndFlush(new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.ENTER_ROOM,
        response.setResult(1).build().toByteArray()));
  }

  /**
   * TODO 清除数据.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/27 9:53
   * @update 2020/8/27 9:53
   */
  private void clearData(Long roomId) {
    ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
    if (Objects.nonNull(kittensRoom)) {
      GAME_DATA.remove(roomId);
      this.gameRemote.deleteRoom(roomId);
      if (this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId)) {
        this.redisUtils.del(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
      }
      MatchManager.delExplodingKittensMatch(roomId);
      ChatManager.delChatGroup(roomId);
      GroupManager.delRoomGroup(roomId);
    }
  }

  /**
   * TODO 游戏经验.
   *
   * @param playerId 玩家ID
   * @param exp 游戏经验
   * @param gold 游戏金币
   * @return java.lang.Integer
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/20 9:15
   * @update 2020/8/20 9:15
   */
  private int gainExperience(Long playerId, Integer exp, Integer gold) {
    if (StringUtils.nvl(playerId).length() >= ExplodingKittensAssets
        .getInt(ExplodingKittensAssets.USER_ID)) {
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
    // 测试账号
    return exp;
  }

  /**
   * TODO 准备定时. 15(s)
   *
   * @param roomId [房间ID]
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/17 19:19
   * @update 2020/8/17 19:19
   */
  private void readyTimeout(Long roomId, Long playerId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        if (!kittensRoom.getTimeOutMap().containsKey(playerId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> readyExamine(roomId, playerId)
              ), 15, TimeUnit.SECONDS);
          kittensRoom.addTimeOut(playerId.intValue(), timeout);
          ExplodingKittensPlayer kittensPlayer = kittensRoom.getPlayerInfo(playerId);
          kittensPlayer.setReadyTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 准备定时. 18(s)
   *
   * @param roomId [房间ID]
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/20 11:01
   * @update 2020/8/20 11:01
   */
  private void initTimeout(Long roomId, Long playerId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        if (!kittensRoom.getTimeOutMap().containsKey(playerId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> readyExamine(roomId, playerId)
              ), 18, TimeUnit.SECONDS);
          kittensRoom.addTimeOut(playerId.intValue(), timeout);
          ExplodingKittensPlayer kittensPlayer = kittensRoom.getPlayerInfo(playerId);
          kittensPlayer.setReadyTime(LocalDateTime.now());
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
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/17 19:39
   * @update 2020/8/17 19:39
   */
  private void readyExamine(Long roomId, Long playerId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.removeTimeOut(playerId.intValue());
        ExplodingKittensPlayer kittensPlayer = kittensRoom.getPlayerInfo(playerId);
        int seat = kittensRoom.leaveSeat(kittensPlayer.getPlayerId());
        F30291.F302918S2C.Builder response = F30291.F302918S2C.newBuilder();
        response.setResult(0).setStand(0).setSeatNo(seat);
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.JOIN_OR_WATCH,
                response.build().toByteArray()), kittensRoom.getRoomId());
        audienceInfo(roomId, playerId);
        // 清理检测
        List<ExplodingKittensPlayer> playerList = kittensRoom.getPlayerList();
        int playGameSize = (int) playerList.stream()
            .filter(s -> s.getPlayerId() > 0).count();
        if (playGameSize == 0) {
          clearTimeout(kittensRoom.getRoomId());
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
   * @date 2020/8/18 9:14
   * @update 2020/8/18 9:14
   */
  private void startTimeout(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        if (!kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.START_LICENSING)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> startExamine(roomId)
              ), 3, TimeUnit.SECONDS);
          kittensRoom.addTimeOut(ExplodingKittensCmd.START_LICENSING, timeout);
          kittensRoom.setStartTime(LocalDateTime.now());
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
   * @date 2020/8/18 9:14
   * @update 2020/8/18 9:14
   */
  private void startExamine(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.removeTimeOut(ExplodingKittensCmd.START_LICENSING);
        int unReady = kittensRoom.unprepared();
        int isReady = kittensRoom.preparations();
        if (unReady == 0 && isReady == ExplodingKittensAssets
            .getInt(ExplodingKittensAssets.ROOM_START_NUM)) {
          kittensRoom.startGame();
          kittensRoom.setActionPlayer(kittensRoom.getNowActionPlayer(), kittensRoom.getDirection());
          kittensRoom.explodingProbability();
          List<ExplodingKittensPlayer> kittensPlayers = kittensRoom.getPlayerList();
          if (CollectionUtils.isNotEmpty(kittensPlayers)) {
            F30291.F302912S2C.Builder response = F30291.F302912S2C.newBuilder();
            response.setDirection(kittensRoom.getDirection());
            response.setNextUserId(kittensRoom.getNowActionPlayer());
            response.setTimes(15);
            int index = 1;
            for (ExplodingKittensPlayer kittensPlayer : kittensPlayers) {
              List<ExplodingKittensCard> kittensCards = kittensPlayer.getPlayerCard();
              if (CollectionUtils.isNotEmpty(kittensCards)) {
                F30291.CardInfos.Builder handCards;
                for (ExplodingKittensCard kittensCard : kittensCards) {
                  handCards = F30291.CardInfos.newBuilder();
                  handCards.setHandCardID(kittensCard.getCardId());
                  handCards.setCardFunc(kittensCard.getCardFunction());
                  response.addHandCards(handCards);
                }
              }
              response.setBombPro(kittensRoom.getProbability());
              response.setRemainingCard(kittensRoom.getRemainingCard().size());
              if (kittensPlayer.getLinkStatus() == 0) {
                kittensPlayer.sendPacket(
                    new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.START_LICENSING,
                        response.build().toByteArray()));
              }
              if (index < 5) {
                response.clearHandCards();
                index++;
              }
            }
            if (Objects.nonNull(kittensRoom.getAudienceGroup())) {
              kittensRoom.getAudienceGroup().writeAndFlush(new Packet(ActionCmd.GAME_EXPLODING_KITTENS,
                  ExplodingKittensCmd.START_LICENSING, response.build().toByteArray()));
            }
            ExplodingKittensPlayer kittensPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
            if (kittensPlayer.getLinkStatus() == 0) {
              actionTimeout(kittensRoom.getRoomId());
            } else {
              trusteeshipTimeout(kittensRoom.getRoomId(), kittensPlayer.getPlayerId(), 0);
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
   * TODO 操作定时. 15(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/4 9:13
   * @update 2020/9/4 9:13
   */
  private void actionTimeout(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        if (!kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.PLAYERS_PLAY_CARDS)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> actionExamine(roomId)
              ), 15, TimeUnit.SECONDS);
          kittensRoom.addTimeOut(ExplodingKittensCmd.PLAYERS_PLAY_CARDS, timeout);
          kittensRoom.setActionTime(LocalDateTime.now());
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
   * @date 2020/9/4 9:15
   * @update 2020/9/4 9:15
   */
  private void actionExamine(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.removeTimeOut(ExplodingKittensCmd.PLAYERS_PLAY_CARDS);
        rightNowTouchCard(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 托管定时. 2(s)
   *
   * @param roomId [房间ID]
   * @param playerId 玩家ID
   * @param actionCmd 操作命令[0.立即摸牌，1.放置炸弹，2.送出卡牌，3.诅咒选择，4.祈求选择，5.束缚选择，6.交换选择]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/19 14:42
   * @update 2020/8/19 14:42
   */
  private void trusteeshipTimeout(Long roomId, Long playerId, Integer actionCmd) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        if (!kittensRoom.getTimeOutMap().containsKey(playerId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> trusteeshipAction(roomId, playerId, actionCmd)
              ), 2, TimeUnit.SECONDS);
          kittensRoom.addTimeOut(playerId.intValue(), timeout);
          kittensRoom.setActionTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 托管操作. ◕
   *
   * @param roomId [房间ID]
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/8/19 14:45
   * @update 2020/8/19 14:45
   */
  private void trusteeshipAction(Long roomId, Long playerId, Integer actionCmd) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.removeTimeOut(playerId.intValue());
        // 操作命令[0.立即摸牌，1.放置炸弹，2.送出卡牌，3.诅咒选择，4.祈求选择，5.束缚选择，6.交换选择]
        switch (actionCmd) {
          case 0:
            rightNowTouchCard(roomId, playerId);
            break;
          case 1:
            bombDisposalExamine(roomId);
            break;
          case 2:
            deliverExamine(roomId);
            break;
          case 3:
            curseExamine(roomId);
            break;
          case 4:
            prayForExamine(roomId);
            break;
          case 5:
            manacleExamine(roomId);
            break;
          default:
            exchangeExamine(roomId);
            break;
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 祈求定时. 15(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/4 9:22
   * @update 2020/9/4 9:22
   */
  private void prayForTimeout(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        if (!kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.PRAY_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> prayForExamine(roomId)
              ), 15, TimeUnit.SECONDS);
          kittensRoom.addTimeOut(ExplodingKittensCmd.PRAY_TIME, timeout);
          kittensRoom.setActionTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 祈求检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/4 9:23
   * @update 2020/9/4 9:23
   */
  private void prayForExamine(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.removeTimeOut(ExplodingKittensCmd.PRAY_TIME);
        F30291.F302914S2C.Builder response = F30291.F302914S2C.newBuilder();
        ExplodingKittensCard desktopCardInfo = kittensRoom.getDesktopCardInfo();
        List<Long> usePlayerList = kittensRoom.optionalPlayersThree(kittensRoom.getNowActionPlayer());
        if (CollectionUtils.isNotEmpty(usePlayerList)) {
          long target = usePlayerList.get(ThreadLocalRandom.current().nextInt(usePlayerList.size()));
          response.setResult(0);
          response.setActionUserId(kittensRoom.getNowActionPlayer());
          response.setSkillIndex(desktopCardInfo.getCardFunction());
          response.setBeUseUserID(target);
          kittensRoom.setPrayForPlayers(target);
          response.setNextUserId(target);
          response.setTimes(15);
          if (kittensRoom.getCursesNum() > 0) {
            response.setRemainingCurse(kittensRoom.getCursesNum());
          }
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_PLAYER,
                  response.build().toByteArray()), kittensRoom.getRoomId());
          ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(target);
          if (nextPlayer.getLinkStatus() == 0) {
            // 玩家成就.有求必应
            Map<String, Object> taskSuc0039 = Maps.newHashMap();
            taskSuc0039.put("userId", target);
            taskSuc0039.put("code", AchievementEnum.AMT0039.getCode());
            taskSuc0039.put("progress", 1);
            taskSuc0039.put("isReset", 0);
            this.userRemote.achievementHandlers(taskSuc0039);
            deliverTimeout(kittensRoom.getRoomId());
          } else {
            trusteeshipTimeout(kittensRoom.getRoomId(), target, 2);
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 拆弹定时. 15(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 4:45
   * @since 2020/9/7 4:45
   */
  private void bombDisposalTimeout(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        if (!kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.PLACE_EXPLODING)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> bombDisposalExamine(roomId)
              ), 15, TimeUnit.SECONDS);
          kittensRoom.addTimeOut(ExplodingKittensCmd.PLACE_EXPLODING, timeout);
          kittensRoom.setActionTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 拆弹检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/4 9:23
   * @update 2020/9/4 9:23
   */
  private void bombDisposalExamine(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.removeTimeOut(ExplodingKittensCmd.PLACE_EXPLODING);
        kittensRoom.placeExploding(7);
        F30291.F3029113S2C.Builder response = F30291.F3029113S2C.newBuilder();
        kittensRoom.setActionPlayer(kittensRoom.getNowActionPlayer(), kittensRoom.getDirection());
        response.setNextUserId(kittensRoom.getNowActionPlayer());
        response.setTimes(15);
        response.setBombPro(kittensRoom.getProbability());
        ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
        if (nextPlayer.getBondageIndex() > 0) {
          response.setRightNowTouchCard(1);
        } else {
          response.setRightNowTouchCard(2);
        }
        response.setRemainingCard(kittensRoom.getRemainingCard().size());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLACE_EXPLODING,
                response.build().toByteArray()), kittensRoom.getRoomId());
        if (nextPlayer.getLinkStatus() == 0) {
          actionTimeout(kittensRoom.getRoomId());
        } else {
          trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 诅咒定时. 15(S)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 9:18
   * @update 2020/9/7 9:18
   */
  private void curseTimeout(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        if (!kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.CURSE_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> curseExamine(roomId)
              ), 15, TimeUnit.SECONDS);
          kittensRoom.addTimeOut(ExplodingKittensCmd.CURSE_TIME, timeout);
          kittensRoom.setActionTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 诅咒检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 9:05
   * @update 2020/9/7 9:05
   */
  private void curseExamine(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.removeTimeOut(ExplodingKittensCmd.CURSE_TIME);
        F30291.F302914S2C.Builder response = F30291.F302914S2C.newBuilder();
        ExplodingKittensCard desktopCardInfo = kittensRoom.getDesktopCardInfo();
        List<Long> usePlayerList = kittensRoom.optionalPlayersOne();
        if (CollectionUtils.isNotEmpty(usePlayerList)) {
          long target = usePlayerList.get(ThreadLocalRandom.current().nextInt(usePlayerList.size()));
          response.setResult(0);
          response.setActionUserId(kittensRoom.getNowActionPlayer());
          response.setSkillIndex(desktopCardInfo.getCardFunction());
          response.setBeUseUserID(target);
          if (desktopCardInfo.getCardFunction() == 9) {
            kittensRoom.setCursesNum(kittensRoom.getCursesNum());
          } else {
            kittensRoom.setCursesNum(kittensRoom.getCursesNum());
          }
          kittensRoom.setCursedPlayer(target);
          kittensRoom.setNowActionPlayer(target);
          response.setTimes(15);
          response.setRemainingCurse(kittensRoom.getCursesNum());
          response.setNextUserId(kittensRoom.getNowActionPlayer());
          ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
          if (nextPlayer.getBondageIndex() > 0) {
            response.setRightNowTouchCard(1);
          } else {
            response.setRightNowTouchCard(2);
          }
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_PLAYER,
                  response.build().toByteArray()), kittensRoom.getRoomId());
          if (nextPlayer.getLinkStatus() == 0) {
            actionTimeout(kittensRoom.getRoomId());
          } else {
            trusteeshipTimeout(kittensRoom.getRoomId(), target, 0);
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 束缚定时. 15(S)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 9:13
   * @update 2020/9/7 9:13
   */
  private void manacleTimeout(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        if (!kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.MANACLE_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> manacleExamine(roomId)
              ), 15, TimeUnit.SECONDS);
          kittensRoom.addTimeOut(ExplodingKittensCmd.MANACLE_TIME, timeout);
          kittensRoom.setActionTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 束缚检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 9:05
   * @update 2020/9/7 9:05
   */
  private void manacleExamine(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.removeTimeOut(ExplodingKittensCmd.MANACLE_TIME);
        F30291.F302914S2C.Builder response = F30291.F302914S2C.newBuilder();
        ExplodingKittensCard desktopCardInfo = kittensRoom.getDesktopCardInfo();
        List<Long> usePlayerList = kittensRoom.optionalPlayersTwo(kittensRoom.getNowActionPlayer());
        if (CollectionUtils.isNotEmpty(usePlayerList)) {
          long target = usePlayerList.get(ThreadLocalRandom.current().nextInt(usePlayerList.size()));
          response.setResult(0);
          response.setActionUserId(kittensRoom.getNowActionPlayer());
          response.setSkillIndex(desktopCardInfo.getCardFunction());
          response.setBeUseUserID(target);
          response.setTimes(15);
          if (kittensRoom.getCursesNum() > 0) {
            response.setRemainingCurse(kittensRoom.getCursesNum());
            response.setNextUserId(kittensRoom.getNowActionPlayer());
          } else {
            kittensRoom.setActionPlayer(kittensRoom.getNowActionPlayer(), kittensRoom.getDirection());
            response.setNextUserId(kittensRoom.getNowActionPlayer());
          }
          ExplodingKittensPlayer targetPlayer = kittensRoom.getPlayerInfo(target);
          targetPlayer.setBondageIndex(1);
          ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
          if (nextPlayer.getBondageIndex() > 0) {
            response.setRightNowTouchCard(1);
          } else {
            response.setRightNowTouchCard(2);
          }
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_PLAYER,
                  response.build().toByteArray()), kittensRoom.getRoomId());
          if (nextPlayer.getLinkStatus() == 0) {
            actionTimeout(kittensRoom.getRoomId());
          } else {
            trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 交换定时. 15(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 9:00
   * @update 2020/9/7 9:00
   */
  private void exchangeTimeout(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        if (!kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.EXCHANGE_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> exchangeExamine(roomId)
              ), 15, TimeUnit.SECONDS);
          kittensRoom.addTimeOut(ExplodingKittensCmd.EXCHANGE_TIME, timeout);
          kittensRoom.setActionTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 交换检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 9:05
   * @update 2020/9/7 9:05
   */
  private void exchangeExamine(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.removeTimeOut(ExplodingKittensCmd.EXCHANGE_TIME);
        F30291.F302914S2C.Builder response = F30291.F302914S2C.newBuilder();
        ExplodingKittensCard desktopCardInfo = kittensRoom.getDesktopCardInfo();
        List<Long> usePlayerList = kittensRoom.optionalPlayersThree(kittensRoom.getNowActionPlayer());
        if (CollectionUtils.isNotEmpty(usePlayerList)) {
          long target = usePlayerList.get(ThreadLocalRandom.current().nextInt(usePlayerList.size()));
          response.setResult(0);
          response.setActionUserId(kittensRoom.getNowActionPlayer());
          response.setSkillIndex(desktopCardInfo.getCardFunction());
          response.setBeUseUserID(target);
          response.setNextUserId(kittensRoom.getNowActionPlayer());
          response.setTimes(15);
          ExplodingKittensPlayer targetPlayer = kittensRoom.getPlayerInfo(target);
          List<ExplodingKittensCard> targetCard = targetPlayer.getPlayerCard();
          ExplodingKittensPlayer actionPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
          List<ExplodingKittensCard> actionCard = actionPlayer.getPlayerCard();
          targetPlayer.setPlayerCard(actionCard);
          actionPlayer.setPlayerCard(targetCard);
          targetCard = targetPlayer.getPlayerCard();
          if (CollectionUtils.isNotEmpty(targetCard)) {
            F30291.CardInfos.Builder handCard;
            for (ExplodingKittensCard kittensCard : targetCard) {
              handCard = F30291.CardInfos.newBuilder();
              handCard.setHandCardID(kittensCard.getCardId());
              handCard.setCardFunc(kittensCard.getCardFunction());
              response.addBeUseHandCard(handCard);
            }
          }
          actionCard = actionPlayer.getPlayerCard();
          if (CollectionUtils.isNotEmpty(actionCard)) {
            F30291.CardInfos.Builder handCard;
            for (ExplodingKittensCard kittensCard : actionCard) {
              handCard = F30291.CardInfos.newBuilder();
              handCard.setHandCardID(kittensCard.getCardId());
              handCard.setCardFunc(kittensCard.getCardFunction());
              response.addActionHandCard(handCard);
            }
          }
          if (kittensRoom.getCursesNum() > 0) {
            response.setRemainingCurse(kittensRoom.getCursesNum());
          }
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.CHOOSE_PLAYER,
                  response.build().toByteArray()), kittensRoom.getRoomId());
          if (actionPlayer.getLinkStatus() == 0) {
            actionTimeout(kittensRoom.getRoomId());
          } else {
            trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 交付定时. 15(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 14:21
   * @update 2020/9/7 14:21
   */
  private void deliverTimeout(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        if (!kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.GIVE_PLAYER_CARD)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> deliverExamine(roomId)
              ), 15, TimeUnit.SECONDS);
          kittensRoom.addTimeOut(ExplodingKittensCmd.GIVE_PLAYER_CARD, timeout);
          kittensRoom.setActionTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 交付检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 14:22
   * @update 2020/9/7 14:22
   */
  private void deliverExamine(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.removeTimeOut(ExplodingKittensCmd.GIVE_PLAYER_CARD);
        F30291.F302915S2C.Builder response = F30291.F302915S2C.newBuilder();
        ExplodingKittensPlayer actionPlayer = kittensRoom.getPlayerInfo(kittensRoom.getPrayForPlayers());
        ExplodingKittensCard deliverCard = actionPlayer.getPlayerCard().remove(ThreadLocalRandom.current().nextInt(actionPlayer.getPlayerCard().size()));
        ExplodingKittensPlayer deliverPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
        deliverPlayer.getPlayerCard().add(deliverCard);
        F30291.CardInfos.Builder handCard;
        List<ExplodingKittensCard> givePlayerCards = actionPlayer.getPlayerCard();
        if (CollectionUtils.isNotEmpty(givePlayerCards)) {
          for (ExplodingKittensCard card : givePlayerCards) {
            handCard = F30291.CardInfos.newBuilder();
            handCard.setHandCardID(card.getCardId());
            handCard.setCardFunc(card.getCardFunction());
            response.addGiveHandCardid(handCard);
          }
        }
        List<ExplodingKittensCard> nowPlayerCards = deliverPlayer.getPlayerCard();
        for (ExplodingKittensCard card : nowPlayerCards) {
          handCard = F30291.CardInfos.newBuilder();
          handCard.setHandCardID(card.getCardId());
          handCard.setCardFunc(card.getCardFunction());
          response.addReceiveHandCardid(handCard);
        }
        response.setGiveUsrdId(kittensRoom.getPrayForPlayers());
        response.setReceiveUserId(kittensRoom.getNowActionPlayer());
        response.setTimes(15);
        response.setActionUserId(kittensRoom.getNowActionPlayer());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.GIVE_PLAYER_CARD,
                response.build().toByteArray()), kittensRoom.getRoomId());
        kittensRoom.setPrayForPlayers(0L);
        ExplodingKittensPlayer nextPlayer = kittensRoom.getPlayerInfo(kittensRoom.getNowActionPlayer());
        if (nextPlayer.getLinkStatus() == 0) {
          actionTimeout(kittensRoom.getRoomId());
        } else {
          trusteeshipTimeout(kittensRoom.getRoomId(), kittensRoom.getNowActionPlayer(), 0);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 出局展示. 2(s)
   *
   * @param roomId [房间ID]
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 16:53
   * @update 2020/9/7 16:53
   */
  private void outShowTimeout(Long roomId, Long playerId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        if (!kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.PLAYER_OUT_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> outShowExamine(roomId, playerId)
              ), 2, TimeUnit.SECONDS);
          kittensRoom.addTimeOut(ExplodingKittensCmd.PLAYER_OUT_TIME, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 出局检验. ◕
   *
   * @param roomId [房间ID]
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/8 16:16
   * @update 2020/9/8 16:16
   */
  private void outShowExamine(Long roomId, Long playerId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.removeTimeOut((int) ExplodingKittensCmd.PLAYER_OUT_TIME);
        List<Long> rankingList = kittensRoom.getRankingList();
        ExplodingKittensPlayer kittensPlayer = kittensRoom.getPlayerInfo(playerId);
        F30291.F3029114S2C.Builder response = F30291.F3029114S2C.newBuilder();
        if (kittensPlayer.getLinkStatus() == 0) {
          response.setFinishId(playerId);
          response.setIcon(kittensPlayer.getPlayerAvatar());
          response.setNick(kittensPlayer.getPlayerName());
          response.setSex(kittensPlayer.getPlayerSex());
          if (rankingList.size() == 1) {
            response.setRanking(5);
            response.setCoin(-20);
            if (MemManager.isExists(kittensPlayer.getPlayerId())) {
              response.setIsDouble(1);
            }
            response.setExp(gainExperience(playerId, 0, -20));
            kittensRoom.updateGold(playerId, -20);
          } else if (rankingList.size() == 2) {
            response.setRanking(4);
            response.setCoin(-10);
            if (MemManager.isExists(kittensPlayer.getPlayerId())) {
              response.setIsDouble(1);
            }
            response.setExp(gainExperience(playerId, 0, -10));
            kittensRoom.updateGold(playerId, -10);
          } else if (rankingList.size() == 3) {
            response.setRanking(3);
            response.setCoin(0);
            if (MemManager.isExists(kittensPlayer.getPlayerId())) {
              response.setIsDouble(1);
            }
            response.setExp(gainExperience(playerId, 1, 0));
            kittensRoom.updateGold(playerId, 0);
          }
          kittensPlayer.sendPacket(
              new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.PLAYER_FINISH,
                  response.build().toByteArray()));
        } else {
          if (rankingList.size() == 1) {
            kittensRoom.updateGold(playerId, -20);
          } else if (rankingList.size() == 2) {
            kittensRoom.updateGold(playerId, -10);
          } else if (rankingList.size() == 3) {
            kittensRoom.updateGold(playerId, 0);
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 结束展示. 2(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/7 16:53
   * @update 2020/9/7 16:53
   */
  private void gameFinishTimeout(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        if (!kittensRoom.getTimeOutMap().containsKey((int) ExplodingKittensCmd.GAME_OVER_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> gameFinishExamine(roomId)
              ), 2, TimeUnit.SECONDS);
          kittensRoom.addTimeOut(ExplodingKittensCmd.GAME_OVER_TIME, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 结束检验. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @date 2020/9/8 16:25
   * @update 2020/9/8 16:25
   */
  private void gameFinishExamine(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.removeTimeOut((int) ExplodingKittensCmd.GAME_OVER_TIME);
        kittensRoom.destroy();
        List<Long> rankingList = kittensRoom.getRankingList();
        Collections.reverse(rankingList);
        List<ExplodingKittensPlayer> kittensPlayers = kittensRoom.getPlayerList()
            .stream()
            // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
            .filter(player -> player.getPlayerStatus() != 3)
            .collect(Collectors.toList());
        rankingList.add(0, kittensPlayers.get(0).getPlayerId());
        List<ExplodingKittensPlayer> kittensPlayerList = kittensRoom.getPlayerList();
        kittensPlayerList.sort((o1, o2) -> {
          long flag1 = o1.getPlayerId();
          long flag2 = o2.getPlayerId();
          int io1 = rankingList.indexOf(flag1);
          int io2 = rankingList.indexOf(flag2);
          if (io1 != -1) {
            io1 = kittensPlayerList.size() - io1;
          }
          if (io2 != -1) {
            io2 = kittensPlayerList.size() - io2;
          }
          return io2 - io1;
        });
        int index = 1;
        F30291.F3029117S2C.Builder gameFinish = F30291.F3029117S2C.newBuilder();
        F30291.EnterPlayerInfo.Builder playerInfo;
        for (ExplodingKittensPlayer kittensPlayer : kittensPlayerList) {
          // 活动处理 丹枫迎秋
          Map<String, Object> activity = Maps.newHashMap();
          activity.put("userId", kittensPlayer.getPlayerId());
          activity.put("code", ActivityEnum.ACT000103.getCode());
          activity.put("progress", 1);
          this.activityRemote.openHandler(activity);
          playerInfo = F30291.EnterPlayerInfo.newBuilder();
          playerInfo.setIcon(kittensPlayer.getPlayerAvatar());
          playerInfo.setNick(kittensPlayer.getPlayerName());
          playerInfo.setSex(kittensPlayer.getPlayerSex());
          playerInfo.setRanking(index);
          switch (index) {
            case 1:
              if (MemManager.isExists(kittensPlayer.getPlayerId())) {
                playerInfo.setIsDouble(1);
                if (kittensPlayer.getLinkStatus() == 0) {
                  playerInfo.setExpNum(gainExperience(kittensPlayer.getPlayerId(), 8, 20));
                  if (kittensPlayer.getTouchExploding() == 0) {
                    // 爆炸猫咪.玩家成就.神选英杰
                    Map<String, Object> taskSuc0038 = Maps.newHashMap();
                    taskSuc0038.put("userId", kittensPlayer.getPlayerId());
                    taskSuc0038.put("code", AchievementEnum.AMT0038.getCode());
                    taskSuc0038.put("progress", 1);
                    taskSuc0038.put("isReset", 0);
                    this.userRemote.achievementHandlers(taskSuc0038);
                  }
                } else {
                  playerInfo.setExpNum(8);
                }
              } else {
                if (kittensPlayer.getLinkStatus() == 0) {
                  playerInfo.setIsDouble(1);
                  playerInfo.setExpNum(gainExperience(kittensPlayer.getPlayerId(), 4, 20));
                  if (kittensPlayer.getTouchExploding() == 0) {
                    // 爆炸猫咪.玩家成就.神选英杰
                    Map<String, Object> taskSuc0038 = Maps.newHashMap();
                    taskSuc0038.put("userId", kittensPlayer.getPlayerId());
                    taskSuc0038.put("code", AchievementEnum.AMT0038.getCode());
                    taskSuc0038.put("progress", 1);
                    taskSuc0038.put("isReset", 0);
                    this.userRemote.achievementHandlers(taskSuc0038);
                  }
                } else {
                  playerInfo.setExpNum(4);
                }
              }
              playerInfo.setCoin(20);
              kittensRoom.updateGold(kittensPlayer.getPlayerId(), 20);
              break;
            case 2:
              if (MemManager.isExists(kittensPlayer.getPlayerId())) {
                if (kittensPlayer.getLinkStatus() == 0) {
                  playerInfo.setIsDouble(1);
                  playerInfo.setExpNum(gainExperience(kittensPlayer.getPlayerId(), 4, 10));
                } else {
                  playerInfo.setExpNum(4);
                }
              } else {
                if (kittensPlayer.getLinkStatus() == 0) {
                  playerInfo.setExpNum(gainExperience(kittensPlayer.getPlayerId(), 2, 10));
                } else {
                  playerInfo.setExpNum(2);
                }
              }
              playerInfo.setCoin(10);
              kittensRoom.updateGold(kittensPlayer.getPlayerId(), 10);
              break;
            case 3:
              if (MemManager.isExists(kittensPlayer.getPlayerId())) {
                playerInfo.setIsDouble(1);
              }
              playerInfo.setExpNum(1);
              playerInfo.setCoin(0);
              break;
            case 4:
              if (MemManager.isExists(kittensPlayer.getPlayerId())) {
                playerInfo.setIsDouble(1);
              }
              playerInfo.setExpNum(0);
              playerInfo.setCoin(-10);
              break;
            default:
              if (MemManager.isExists(kittensPlayer.getPlayerId())) {
                playerInfo.setIsDouble(1);
              }
              playerInfo.setExpNum(0);
              playerInfo.setCoin(-20);
              break;
          }
          gameFinish.addEnterPlayerInfo(playerInfo);
          index++;
          // 爆炸猫咪.玩家成就.高级玩家
          Map<String, Object> taskSuc0041 = Maps.newHashMap();
          taskSuc0041.put("userId", kittensPlayer.getPlayerId());
          taskSuc0041.put("code", AchievementEnum.AMT0041.getCode());
          taskSuc0041.put("progress", 1);
          taskSuc0041.put("isReset", 0);
          this.userRemote.achievementHandlers(taskSuc0041);
          // 爆炸猫咪.玩家成就.头号玩家
          Map<String, Object> taskSuc0042 = Maps.newHashMap();
          taskSuc0042.put("userId", kittensPlayer.getPlayerId());
          taskSuc0042.put("code", AchievementEnum.AMT0042.getCode());
          taskSuc0042.put("progress", 1);
          taskSuc0042.put("isReset", 0);
          this.userRemote.achievementHandlers(taskSuc0042);
        }
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.GAME_FINISH,
                gameFinish.build().toByteArray()), kittensRoom.getRoomId());
        List<ExplodingKittensPlayer> playerList = kittensRoom.getPlayerList()
            .stream()
            .filter(player -> player.getPlayerId() > 0 && player.getLinkStatus() == 0)
            .collect(Collectors.toList());
        // 金币信息
        if (CollectionUtils.isNotEmpty(playerList)) {
          playerList.forEach(player -> {
            F30291.F3029112S2C.Builder goldInfo = F30291.F3029112S2C.newBuilder();
            player.sendPacket(
                new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.UPDATE_PLAYER_GOLD,
                    goldInfo.setPlayGold(player.getPlayerGold()).build().toByteArray()));
          });
        }
        // 爆炸猫咪.玩家处理
        F30291.F302917S2C.Builder clearList = F30291.F302917S2C.newBuilder();
        clearList.addAllUserID(kittensRoom.offlinePlayers());
        clearList.addAllUserID(kittensRoom.goldShortage());
        if (clearList.getUserIDList().size() > 0) {
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_EXPLODING_KITTENS, ExplodingKittensCmd.HANDLE_PLAYER,
                  clearList.build().toByteArray()), kittensRoom.getRoomId());
        }
        // 爆炸猫咪.初始游戏
        kittensRoom.initGame();
        if (CollectionUtils.isNotEmpty(kittensRoom.seatedPlayers())) {
          List<Long> seatedPlayers = kittensRoom.seatedPlayers();
          seatedPlayers.forEach(playerId -> initTimeout(roomId, playerId));
          roomOpenOrCancelMatch(kittensRoom.getRoomId());
        } else {
          if (kittensRoom.seatedPlayers().size() == 0 && kittensRoom.getAudienceList().size() == 0) {
            // 爆炸猫咪.删除房间
            clearData(roomId);
          } else {
            // 清理检测
            List<ExplodingKittensPlayer> explodingKittensPlayers = kittensRoom.getPlayerList();
            int playGameSize = (int) explodingKittensPlayers.stream()
                .filter(s -> s.getPlayerId() > 0).count();
            if (playGameSize == 0) {
              clearTimeout(kittensRoom.getRoomId());
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
   * TODO 清理定时. 10(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/19 13:31
   * @update 2020/10/19 13:31
   */
  private void clearTimeout(Long roomId) {
    try {
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        if (!kittensRoom.getTimeOutMap().containsKey(roomId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> clearExamine(roomId)
              ), 10, TimeUnit.SECONDS);
          kittensRoom.addTimeOut(roomId.intValue(), timeout);
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
      ExplodingKittensRoom kittensRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(kittensRoom)) {
        kittensRoom.removeTimeOut(roomId.intValue());
        List<ExplodingKittensPlayer> playerList = kittensRoom.getPlayerList();
        int playGameSize = (int) playerList.stream()
            .filter(player -> player.getPlayerId() > 0).count();
        LoggerManager.info("<== 30291 [爆炸猫咪.清除检测] DELETE: [{}] PLAY: [{}]", roomId, playGameSize);
        if (playGameSize == 0) {
          List<ExplodingKittensPlayer> watchList = kittensRoom.getAudienceList();
          if (watchList.size() > 0) {
            F20000.F200007S2C.Builder builder = F20000.F200007S2C.newBuilder();
            builder.setMsg("(oﾟvﾟ)ノ 房间已解散!");
            watchList.forEach(kittensPlayer -> {
              SoftChannel.sendPacketToUserId(
                  new Packet(ActionCmd.APP_HEART, (short) 7,
                      builder.build().toByteArray()), kittensPlayer.getPlayerId());
              this.redisUtils
                  .del(GameKey.KEY_GAME_JOIN_RECORD.getName() + kittensPlayer.getPlayerId());
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
