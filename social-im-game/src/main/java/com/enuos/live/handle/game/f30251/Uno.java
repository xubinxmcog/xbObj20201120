package com.enuos.live.handle.game.f30251;

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
import com.enuos.live.proto.f30251msg.F30251;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * TODO 一起优诺.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/8/10 4:41
 */

@Component
@AbstractAction(cmd = ActionCmd.GAME_UNO)
public class Uno extends AbstractActionHandler {

  /** 房间游戏数据. */
  private static ConcurrentHashMap<Long, UnoRoom> GAME_DATA = new ConcurrentHashMap<>();

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
   * @create 2020/8/13 19:22
   * @update 2020/8/13 19:22
   */
  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      switch (packet.child) {
        case UnoCmd.ENTER_ROOM:
          enterRoom(channel, packet);
          break;
        case UnoCmd.START_READY:
          startReady(channel, packet);
          break;
        case UnoCmd.PLAY_CARDS:
          playCards(channel, packet);
          break;
        case UnoCmd.SELECT_COLOR:
          selectColor(channel, packet);
          break;
        case UnoCmd.TOUCH_CARDS:
          touchCards(channel, packet);
          break;
        case UnoCmd.QUESTION_CARDS:
          questionCards(channel, packet);
          break;
        case UnoCmd.PLAYER_EXIT:
          playerExit(channel, packet);
          break;
        case UnoCmd.WATCH_INFO:
          watchInfo(channel, packet);
          break;
        case UnoCmd.JOIN_LEAVE:
          joinLeave(channel, packet);
          break;
        case UnoCmd.SKIP_CARD:
          skipCard(channel, packet);
          break;
        default:
          LoggerManager.warn("[GAME 30251 HANDLE] CHILD ERROR: [{}]", packet.child);
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
      UnoRoom unoRoom = GAME_DATA.get(attachId);
      if (Objects.nonNull(unoRoom)) {
        UnoPlayer checkPlayer = unoRoom.getPlayerInfo(userId);
        if (Objects.nonNull(checkPlayer)) {
          if (unoRoom.getRoomStatus() == 1) {
            if (checkPlayer.getIdentity() == 0) {
              checkPlayer.setLinkStatus(1);
              if (checkPlayer.getPlayerStatus() == 2) {
                // 中途退出扣 -120金币
                gainExperience(userId, 0, -120);
              }
              checkPlayer.setPlayerStatus(3);
              ChatManager.removeChannel(attachId, checkPlayer.getChannel());
              GroupManager.removeChannel(attachId, checkPlayer.getChannel());
              F30251.F302518S2C.Builder response = F30251.F302518S2C.newBuilder();
              GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAYER_EXIT,
                  response.setUserID(userId).build().toByteArray()), unoRoom.getRoomId());
              this.gameRemote.leaveRoom(userId);
              if (unoRoom.seatedPlayersIsDisconnected() == unoRoom.seatedPlayersNum()
                  && unoRoom.seatedPlayers().size() == 0 && unoRoom.getWatchList().size() == 0) {
                clearData(unoRoom.getRoomId());
              }
            } else {
              unoRoom.leaveGame(userId, 1);
              ChatManager.removeChannel(attachId, checkPlayer.getChannel());
              GroupManager.removeChannel(attachId, checkPlayer.getChannel());
              this.gameRemote.leaveRoom(userId);
              if (unoRoom.seatedPlayersIsDisconnected() == unoRoom.seatedPlayersNum()
                  && unoRoom.seatedPlayers().size() == 0 && unoRoom.getWatchList().size() == 0) {
                clearData(unoRoom.getRoomId());
              }
            }
          } else {
            if (checkPlayer.getIdentity() ==  0) {
              if (checkPlayer.getPlayerStatus() == 0) {
                unoRoom.cancelTimeOut(userId.intValue());
              } else if (checkPlayer.getPlayerStatus() == 1) {
                if (unoRoom.getTimeOutMap().containsKey((int) UnoCmd.START_GAME)) {
                  unoRoom.cancelTimeOut(UnoCmd.START_GAME);
                }
              }
              unoRoom.leaveGame(userId, 0);
              ChatManager.removeChannel(attachId, checkPlayer.getChannel());
              GroupManager.removeChannel(attachId, checkPlayer.getChannel());
              this.gameRemote.leaveRoom(userId);
              F30251.F302518S2C.Builder response = F30251.F302518S2C.newBuilder();
              GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAYER_EXIT,
                  response.setUserID(userId).build().toByteArray()), unoRoom.getRoomId());
              if (unoRoom.seatedPlayers().size() == 0 && unoRoom.getWatchList().size() == 0) {
                clearData(unoRoom.getRoomId());
              } else {
                roomOpenOrCancelMatch(unoRoom.getRoomId());
                // 清理检测
                List<UnoPlayer> playerList = unoRoom.getPlayerList();
                int playGameSize = (int) playerList.stream()
                    .filter(s -> s.getPlayerId() > 0).count();
                if (playGameSize == 0) {
                  clearTimeout(unoRoom.getRoomId());
                }
              }
            } else {
              unoRoom.leaveGame(userId, 1);
              ChatManager.removeChannel(attachId, checkPlayer.getChannel());
              GroupManager.removeChannel(attachId, checkPlayer.getChannel());
              this.gameRemote.leaveRoom(userId);
              if (unoRoom.seatedPlayersIsDisconnected() == unoRoom.seatedPlayersNum()
                  && unoRoom.seatedPlayers().size() == 0 && unoRoom.getWatchList().size() == 0) {
                clearData(unoRoom.getRoomId());
              } else if (unoRoom.seatedPlayers().size() == 0 && unoRoom.getWatchList().size() == 0) {
                clearData(unoRoom.getRoomId());
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
   * @create 2020/8/13 19:27
   * @update 2020/8/13 19:27
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
   * @create 2020/8/13 19:27
   * @update 2020/8/13 19:27
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
   * @create 2020/8/13 20:52
   * @update 2020/8/13 20:52
   */
  private void enterRoom(Channel channel, Packet packet) {
    try {
      boolean isTestUser = this.redisUtils.hasKey(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
      boolean isPlayer = this.redisUtils.hasKey(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
      if (isTestUser || isPlayer) {
        closeLoading(packet.userId);
        boolean checkRoom = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + packet.roomId);
        boolean checkTest = (packet.roomId == UnoAssets.getLong(UnoAssets.TEST_ID));
        if (checkRoom || checkTest) {
          UnoRoom unoRoom = GAME_DATA.get(packet.roomId);
          if (Objects.nonNull(unoRoom)) {
            UnoPlayer player = unoRoom.getPlayerInfo(packet.userId);
            // 房间状态 0-未开始 1-已开始.
            if (unoRoom.getRoomStatus() == 1) {
              if (Objects.nonNull(player)) {
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
                byte[] bytes;
                if (isTestUser) {
                  bytes = this.redisUtils.getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
                } else {
                  bytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
                }
                I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
                unoRoom.enterWatch(channel, playerInfo);
                pullDecorateInfo(packet);
                joinUnoRoom(packet);
              }
            } else {
              // 销毁清理定时
              unoRoom.cancelTimeOut((int) packet.roomId);
              if (Objects.nonNull(player)) {
                player.setChannel(channel);
                refreshData(channel, packet);
                // 清除检测
                List<UnoPlayer> playerList = unoRoom.getPlayerList();
                int playGameSize = (int) playerList.stream()
                    .filter(s -> s.getPlayerId() > 0).count();
                if (playGameSize == 0) {
                  clearTimeout(unoRoom.getRoomId());
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
                unoRoom.enterSeat(channel, playerInfo);
                pullDecorateInfo(packet);
                joinUnoRoom(packet);
                roomOpenOrCancelMatch(packet.roomId);
              }
            }
          } else {
            // 添加定时线程
            register(TimerEventLoop.timeGroup.next());
            GAME_DATA.computeIfAbsent(packet.roomId, key -> new UnoRoom(packet.roomId));
            unoRoom = GAME_DATA.get(packet.roomId);
            refreshData(channel, packet);
            byte[] bytes;
            if (isTestUser) {
              bytes = this.redisUtils.getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
            } else {
              bytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
            }
            I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
            unoRoom.enterSeat(channel, playerInfo);
            pullDecorateInfo(packet);
            joinUnoRoom(packet);
            if (checkTest) {
              unoRoom.setOpenWay(1);
            } else {
              byte[] roomByte = this.redisUtils.getByte(GameKey.KEY_GAME_ROOM_RECORD.getName() + packet.roomId);
              I10001.RoomRecord roomRecord = I10001.RoomRecord.parseFrom(roomByte);
              unoRoom.setOpenWay(roomRecord.getOpenWay());
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
   * @create 2020/8/17 18:39
   * @update 2020/8/17 18:39
   */
  private void pushPlayerInfo(Channel channel, Packet packet) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(unoRoom)) {
        UnoPlayer player = unoRoom.getPlayerInfo(packet.userId);
        F30251.F30251S2C.Builder builder = F30251.F30251S2C.newBuilder();
        if (player.getIdentity() == 1) {
          watchRoomInfo(channel, packet);
        } else {
          List<UnoPlayer> playerList = unoRoom.getPlayerList();
          if (CollectionUtils.isNotEmpty(playerList)) {
            F30251.PlayerInfo.Builder playerInfo;
            for (UnoPlayer unoPlayer : playerList) {
              playerInfo = F30251.PlayerInfo.newBuilder();
              playerInfo.setNick(unoPlayer.getPlayerName());
              playerInfo.setUserID(unoPlayer.getPlayerId());
              playerInfo.setUrl(unoPlayer.getPlayerAvatar());
              playerInfo.setSex(unoPlayer.getPlayerSex());
              playerInfo.setDeviPosition(unoPlayer.getSeatNumber());
              playerInfo.setState(unoPlayer.getPlayerStatus());
              playerInfo.setCoin(unoPlayer.getPlayerGold());
              if (StringUtils.isNotEmpty(unoPlayer.getAvatarFrame())) {
                playerInfo.setIconFrame(unoPlayer.getAvatarFrame());
              }
              if (StringUtils.isNotEmpty(unoPlayer.getCardBackSkin())) {
                F30251.GameStyle.Builder gameStyle = F30251.GameStyle.newBuilder();
                gameStyle.setCardBack(unoPlayer.getCardBackSkin());
                playerInfo.setGameStyle(gameStyle);
              }
              if (unoPlayer.getPlayerStatus() == 0) {
                if (unoPlayer.getReadyTime() != null) {
                  LocalDateTime udt = unoPlayer.getReadyTime().plusSeconds(15L);
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
          GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_UNO, UnoCmd.ENTER_ROOM,
              builder.setResult(0).build().toByteArray()), unoRoom.getRoomId());
          if (player.getPlayerStatus() == 0) {
            if (!unoRoom.getTimeOutMap().containsKey((int) packet.userId)) {
              readyTimeout(unoRoom.getRoomId(), packet.userId);
            }
          }
          F30251.F3025115S2C.Builder goldInfo = F30251.F3025115S2C.newBuilder();
          channel.writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAYER_GOLD,
              goldInfo.setPlayGold(player.getPlayerGold()).build().toByteArray()));
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
      UnoRoom unoRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(unoRoom)) {
        F30251.F30251S2C.Builder builder = F30251.F30251S2C.newBuilder();
        List<UnoPlayer> playerList = unoRoom.getPlayerList();
        if (CollectionUtils.isNotEmpty(playerList)) {
          F30251.PlayerInfo.Builder playerInfo;
          for (UnoPlayer unoPlayer : playerList) {
            playerInfo = F30251.PlayerInfo.newBuilder();
            playerInfo.setNick(unoPlayer.getPlayerName());
            playerInfo.setUserID(unoPlayer.getPlayerId());
            playerInfo.setUrl(unoPlayer.getPlayerAvatar());
            playerInfo.setSex(unoPlayer.getPlayerSex());
            playerInfo.setDeviPosition(unoPlayer.getSeatNumber());
            playerInfo.setState(unoPlayer.getPlayerStatus());
            playerInfo.setCoin(unoPlayer.getPlayerGold());
            if (StringUtils.isNotEmpty(unoPlayer.getAvatarFrame())) {
              playerInfo.setIconFrame(unoPlayer.getAvatarFrame());
            }
            if (StringUtils.isNotEmpty(unoPlayer.getCardBackSkin())) {
              F30251.GameStyle.Builder gameStyle = F30251.GameStyle.newBuilder();
              gameStyle.setCardBack(unoPlayer.getCardBackSkin());
              playerInfo.setGameStyle(gameStyle);
            }
            builder.addSeatPlayer(playerInfo);
          }
        }
        channel.writeAndFlush(
            new Packet(ActionCmd.GAME_UNO, UnoCmd.ENTER_ROOM,
                builder.setResult(0).build().toByteArray()));
        UnoPlayer player = unoRoom.getPlayerInfo(packet.userId);
        F30251.F3025115S2C.Builder goldInfo = F30251.F3025115S2C.newBuilder();
        channel.writeAndFlush(
            new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAYER_GOLD,
                goldInfo.setPlayGold(player.getPlayerGold()).build().toByteArray()));
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 开始准备.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/17 9:53
   * @update 2020/8/17 9:53
   */
  private void startReady(Channel channel, Packet packet) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(unoRoom)) {
        UnoPlayer checkPlayer = unoRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30251.F302511C2S request = F30251.F302511C2S.parseFrom(packet.bytes);
          F30251.F302511S2C.Builder response = F30251.F302511S2C.newBuilder();
          if (unoRoom.getRoomStatus() == 1) {
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_UNO, UnoCmd.START_READY,
                    response.setResult(1).setIsReady(request.getIsReady()).build().toByteArray()));
          } else {
            UnoPlayer player = unoRoom.getPlayerInfo(packet.userId);
            if (player.getIdentity() == 1) {
              channel.writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.START_READY,
                  response.setResult(1).setIsReady(request.getIsReady()).build().toByteArray()));
            } else {
              // 准备状态 0-准备 1-取消
              if (request.getIsReady() == 0) {
                unoRoom.cancelTimeOut((int) packet.userId);
                response.setResult(0).setIsReady(request.getIsReady()).setUserID(packet.userId);
                player.setPlayerStatus(1);
                int unReady = unoRoom.unprepared();
                int isReady = unoRoom.preparations();
                if ( unReady == 0 && isReady == UnoAssets.getInt(UnoAssets.ROOM_NUM)) {
                  response.setGameTime(3);
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_UNO, UnoCmd.START_READY,
                          response.build().toByteArray()), unoRoom.getRoomId());
                  startTimeout(unoRoom.getRoomId());
                } else {
                  GroupManager.sendPacketToGroup(
                      new Packet(ActionCmd.GAME_UNO, UnoCmd.START_READY,
                          response.build().toByteArray()), unoRoom.getRoomId());
                }
              } else {
                unoRoom.cancelTimeOut(UnoCmd.START_GAME);
                player.setPlayerStatus(0);
                response.setIsReady(request.getIsReady()).setUserID(packet.userId).setReadyTime(15);
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_UNO, UnoCmd.START_READY,
                        response.setResult(0).build().toByteArray()), unoRoom.getRoomId());
                readyTimeout(unoRoom.getRoomId(), player.getPlayerId());
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
   * @create 2020/8/17 10:03
   * @update 2020/8/17 10:03
   */
  private void playCards(Channel channel, Packet packet) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(unoRoom)) {
        UnoPlayer checkPlayer = unoRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          if (unoRoom.getNowActionId() == packet.userId) {
            UnoPlayer nowPlayer = unoRoom.getPlayerInfo(packet.userId);
            nowPlayer.setPlayerAction(0);
            nowPlayer.setTouchCards(null);
            List<UnoPoker> pokerList = nowPlayer.getPlayerPoker();
            List<Integer> pokerIds = Lists.newLinkedList();
            pokerList.forEach(s -> pokerIds.add(s.getPokerId()));
            F30251.F302513C2S request = F30251.F302513C2S.parseFrom(packet.bytes);
            F30251.CardInfos cardInfos = request.getPlayCardId();
            UnoPoker lastPoker = unoRoom.getLastPoker();
            int isTrue;
            if (cardInfos.getHandCardID() == 13 || cardInfos.getHandCardID() == 14) {
              // 可以出牌
              isTrue = 0;
            } else if (cardInfos.getHandCardDecor() == unoRoom.getNowColors()) {
              // 可以出牌
              isTrue = 0;
            } else if (cardInfos.getHandCardDecor() != unoRoom.getNowColors()
                && cardInfos.getHandCardID() == lastPoker.getPokerId()) {
              // 可以出牌
              isTrue = 0;
            } else {
              // 不可出牌
              isTrue = 1;
            }
            if (isTrue == 1) {
              F30251.F302513S2C.Builder response = F30251.F302513S2C.newBuilder();
              response.setResult(1);
              response.setActionUserId(packet.userId);
              response.setPlayCardId(request.getPlayCardId());
              F30251.CardInfos.Builder lastHandCard;
              for (UnoPoker poker : pokerList) {
                lastHandCard = F30251.CardInfos.newBuilder();
                lastHandCard.setHandCardID(poker.getPokerId());
                lastHandCard.setHandCardDecor(poker.getPokerColors());
                lastHandCard.setCardFunc(poker.getPokerFunction());
                response.addLastHandCard(lastHandCard);
              }
              response.setNextUser(packet.userId);
              LocalDateTime udt = unoRoom.getActionTime().plusSeconds(15L);
              LocalDateTime nds = LocalDateTime.now();
              Duration duration = Duration.between(nds, udt);
              int second = Math.toIntExact(duration.getSeconds());
              response.setNextTimes(second);
              List<UnoPoker> hintPoker = unoRoom.hintPoker(pokerList);
              if (CollectionUtils.isNotEmpty(hintPoker)) {
                F30251.CardInfos.Builder hintHandCard;
                for (UnoPoker poker : hintPoker) {
                  hintHandCard = F30251.CardInfos.newBuilder();
                  hintHandCard.setHandCardID(poker.getPokerId());
                  hintHandCard.setHandCardDecor(poker.getPokerColors());
                  hintHandCard.setCardFunc(poker.getPokerFunction());
                  response.addHintHandCard(hintHandCard);
                }
              }
              channel.writeAndFlush(new Packet(
                  ActionCmd.GAME_UNO, UnoCmd.PLAY_CARDS, response.build().toByteArray()));
            } else {
              if (pokerIds.contains(cardInfos.getHandCardID())) {
                UnoPoker cardInfo = new UnoPoker();
                cardInfo.setPokerId(cardInfos.getHandCardID());
                cardInfo.setPokerColors(cardInfos.getHandCardDecor());
                cardInfo.setPokerFunction(cardInfos.getCardFunc());
                // 牌功能 0-数字 1-禁止 2-倒转 3-加2 4-改变颜色 5-质疑.
                switch (cardInfos.getCardFunc()) {
                  case 1:
                    // 打出跳过
                    unoRoom.cancelTimeOut((int) packet.userId);
                    skipPlayer(packet.roomId, packet.userId, cardInfo);
                    break;
                  case 2:
                    // 打出换向
                    unoRoom.cancelTimeOut((int) packet.userId);
                    changeDirection(packet.roomId, packet.userId, cardInfo);
                    break;
                  case 3:
                    // 打出加牌
                    unoRoom.cancelTimeOut((int) packet.userId);
                    increaseCard(packet.roomId, packet.userId, cardInfo);
                    break;
                  case 4:
                    // 打出换色
                    changeColor(packet.roomId, packet.userId, cardInfo);
                    break;
                  case 5:
                    // 打出质疑
                    questionCard(packet.roomId, packet.userId, cardInfo);
                    break;
                  default:
                    // 打出数字
                    unoRoom.cancelTimeOut((int) packet.userId);
                    digitalCard(packet.roomId, packet.userId, cardInfo);
                    break;
                }
              } else {
                F30251.F302513S2C.Builder response = F30251.F302513S2C.newBuilder();
                response.setResult(1);
                response.setActionUserId(packet.userId);
                response.setPlayCardId(request.getPlayCardId());
                F30251.CardInfos.Builder lastHandCard;
                for (UnoPoker poker : pokerList) {
                  lastHandCard = F30251.CardInfos.newBuilder();
                  lastHandCard.setHandCardID(poker.getPokerId());
                  lastHandCard.setHandCardDecor(poker.getPokerColors());
                  lastHandCard.setCardFunc(poker.getPokerFunction());
                  response.addLastHandCard(lastHandCard);
                }
                response.setNextUser(packet.userId);
                LocalDateTime udt = unoRoom.getActionTime().plusSeconds(15L);
                LocalDateTime nds = LocalDateTime.now();
                Duration duration = Duration.between(nds, udt);
                int second = Math.toIntExact(duration.getSeconds());
                response.setNextTimes(second);
                List<UnoPoker> hintPoker = unoRoom.hintPoker(pokerList);
                if (CollectionUtils.isNotEmpty(hintPoker)) {
                  F30251.CardInfos.Builder hintHandCard;
                  for (UnoPoker poker : hintPoker) {
                    hintHandCard = F30251.CardInfos.newBuilder();
                    hintHandCard.setHandCardID(poker.getPokerId());
                    hintHandCard.setHandCardDecor(poker.getPokerColors());
                    hintHandCard.setCardFunc(poker.getPokerFunction());
                    response.addHintHandCard(hintHandCard);
                  }
                }
                channel.writeAndFlush(new Packet(
                    ActionCmd.GAME_UNO, UnoCmd.PLAY_CARDS, response.build().toByteArray()));
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
   * TODO 跳过玩家. 
   *
   * @param roomId [房间ID]
   * @param playerId [玩家ID]
   * @param cardInfo [卡牌信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/18 14:44
   * @update 2020/8/18 14:44
   */
  private void skipPlayer(Long roomId, Long playerId, UnoPoker cardInfo) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        unoRoom.initByte();
        F30251.CardInfos.Builder cardInfos = F30251.CardInfos.newBuilder();
        cardInfos.setHandCardID(cardInfo.getPokerId());
        cardInfos.setHandCardDecor(cardInfo.getPokerColors());
        cardInfos.setCardFunc(cardInfo.getPokerFunction());
        F30251.F302513S2C.Builder response = F30251.F302513S2C.newBuilder();
        response.setResult(0);
        response.setActionUserId(playerId);
        response.setPlayCardId(cardInfos);
        F30251.CardAbility.Builder ability = F30251.CardAbility.newBuilder();
        long targetId = unoRoom.getNextActionUser(unoRoom.getNowActionId());
        ability.setCardFunc(0).setAttackedPlayer(targetId);
        response.setCardAbility(ability);
        unoRoom.setNowColors(cardInfo.getPokerColors());
        unoRoom.playCards(cardInfo, playerId);
        unoRoom.setUpActionPlayer(targetId, unoRoom.getDirection());
        response.setNextUser(unoRoom.getNowActionId());
        response.setNextTimes(15);
        response.setNowColor(unoRoom.getNowColors());
        response.setDirection(unoRoom.getDirection());
        UnoPlayer nextPlayer = unoRoom.getPlayerInfo(response.getNextUser());
        List<UnoPoker> hintPoker = unoRoom.hintPoker(nextPlayer.getPlayerPoker());
        if (CollectionUtils.isNotEmpty(hintPoker)) {
          F30251.CardInfos.Builder hintHandCard;
          for (UnoPoker poker : hintPoker) {
            hintHandCard = F30251.CardInfos.newBuilder();
            hintHandCard.setHandCardID(poker.getPokerId());
            hintHandCard.setHandCardDecor(poker.getPokerColors());
            hintHandCard.setCardFunc(poker.getPokerFunction());
            response.addHintHandCard(hintHandCard);
          }
        }
        if (unoRoom.getUnoPokerDepot().size() > 0) {
          response.setNextGrabCards(true);
        } else {
          response.setNextGrabCards(false);
        }
        F30251.F302513S2C.Builder realData = F30251.F302513S2C.newBuilder();
        F30251.F302513S2C.Builder fakeData = F30251.F302513S2C.newBuilder();
        UnoPlayer player = unoRoom.getPlayerInfo(playerId);
        if (player.getPlayerPoker().size() == 1) {
          response.setIsHintUno(true);
        } else {
          response.setIsHintUno(false);
        }
        if (response.getIsHintUno()) {
          player.setIsUno(1);
        }
        List<UnoPoker> unoPokers = player.getPlayerPoker();
        if (CollectionUtils.isNotEmpty(unoPokers)) {
          List<UnoPoker> playerPoker = unoRoom.pokerSort(unoPokers);
          player.setPlayerPoker(playerPoker);
          F30251.CardInfos.Builder lastHandCard;
          F30251.CardInfos.Builder fakeHandCard;
          for (UnoPoker poker : playerPoker) {
            lastHandCard = F30251.CardInfos.newBuilder();
            fakeHandCard = F30251.CardInfos.newBuilder();
            lastHandCard.setHandCardID(poker.getPokerId());
            lastHandCard.setHandCardDecor(poker.getPokerColors());
            lastHandCard.setCardFunc(poker.getPokerFunction());
            fakeHandCard.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
            realData.addLastHandCard(lastHandCard);
            fakeData.addLastHandCard(fakeHandCard);
          }
        }
        response.setCardStock(unoRoom.getUnoPokerDepot().size());
        List<UnoPlayer> playerList = unoRoom.getPlayerList();
        if (CollectionUtils.isNotEmpty(playerList)) {
          for (UnoPlayer unoPlayer : playerList) {
            if (unoPlayer.getLinkStatus() == 0) {
              if (unoPlayer.getPlayerId().equals(playerId)) {
                response.addAllLastHandCard(realData.getLastHandCardList());
              } else {
                response.addAllLastHandCard(fakeData.getLastHandCardList());
              }
              unoPlayer.sendPacket(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_CARDS,
                  response.build().toByteArray()));
              response.clearLastHandCard();
            }
          }
        }
        unoRoom.setPlayCard(response.build().toByteArray());
        F30251.F3025116S2C.Builder watchInfo = F30251.F3025116S2C.newBuilder();
        watchInfo.setPlayerAction(0);
        watchInfo.setActionUserId(response.getActionUserId());
        watchInfo.setPlayCardId(response.getPlayCardId());
        watchInfo.setDirection(unoRoom.getDirection());
        watchInfo.setNowColor(unoRoom.getNowColors());
        watchInfo.setCardAbility(response.getCardAbility());
        F30251.PlayCard.Builder playCard;
        for (UnoPlayer unoPlayer : playerList) {
          playCard = F30251.PlayCard.newBuilder();
          if (unoPlayer.getPlayerId() > 0) {
            playCard.setPlayerId(unoPlayer.getPlayerId());
            playCard.setCardNum(unoPlayer.getPlayerPoker().size());
            watchInfo.addPlayLastCard(playCard);
          }
        }
        watchInfo.setNextUser(response.getNextUser());
        watchInfo.setNextTimes(response.getNextTimes());
        watchInfo.setIsHintUno(response.getIsHintUno());
        unoRoom.getWatchGroup().writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_INFO,
            watchInfo.build().toByteArray()));
        unoRoom.setWatchInfo(watchInfo.build().toByteArray());
        UnoPlayer checkPlayer = unoRoom.getPlayerInfo(unoRoom.getNowActionId());
        if (player.getPlayerPoker().size() == 0) {
          gameFinish(roomId);
        } else if (unoRoom.getUnoPokerDepot().size() == 0) {
          gameFinish(roomId);
        } else if (response.getHintHandCardList().size() == 0) {
          if (checkPlayer.getLinkStatus() == 1) {
            // 托管定时
            trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          } else {
            // 自动摸牌
            autoTouchCardTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
            // 操作定时
            actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          }
        } else {
          if (checkPlayer.getLinkStatus() == 1) {
            // 托管定时
            trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          } else {
            // 操作定时
            actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 改变方向. 
   *
   * @param roomId [房间ID]
   * @param playerId [玩家ID]
   * @param cardInfo [卡牌信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/18 15:20
   * @update 2020/8/18 15:20
   */
  private void changeDirection(Long roomId, Long playerId, UnoPoker cardInfo) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        unoRoom.initByte();
        F30251.CardInfos.Builder cardInfos = F30251.CardInfos.newBuilder();
        cardInfos.setHandCardID(cardInfo.getPokerId());
        cardInfos.setHandCardDecor(cardInfo.getPokerColors());
        cardInfos.setCardFunc(cardInfo.getPokerFunction());
        F30251.F302513S2C.Builder response = F30251.F302513S2C.newBuilder();
        response.setResult(0);
        response.setActionUserId(playerId);
        response.setPlayCardId(cardInfos);
        // 旋转方向 1-顺时针 2-逆时针
        Integer direction = unoRoom.getDirection();
        if (direction == 1) {
          unoRoom.setDirection(2);
        } else {
          unoRoom.setDirection(1);
        }
        unoRoom.setNowColors(response.getPlayCardId().getHandCardDecor());
        F30251.CardAbility.Builder ability = F30251.CardAbility.newBuilder();
        ability.setCardFunc(4);
        response.setCardAbility(ability);
        response.setDirection(unoRoom.getDirection());
        response.setNowColor(unoRoom.getNowColors());
        unoRoom.playCards(cardInfo, playerId);
        unoRoom.setUpActionPlayer(unoRoom.getNowActionId(), unoRoom.getDirection());
        response.setNextUser(unoRoom.getNowActionId());
        response.setNextTimes(15);
        UnoPlayer nextPlayer = unoRoom.getPlayerInfo(response.getNextUser());
        List<UnoPoker> hintPoker = unoRoom.hintPoker(nextPlayer.getPlayerPoker());
        if (CollectionUtils.isNotEmpty(hintPoker)) {
          F30251.CardInfos.Builder hintHandCard;
          for (UnoPoker poker : hintPoker) {
            hintHandCard = F30251.CardInfos.newBuilder();
            hintHandCard.setHandCardID(poker.getPokerId());
            hintHandCard.setHandCardDecor(poker.getPokerColors());
            hintHandCard.setCardFunc(poker.getPokerFunction());
            response.addHintHandCard(hintHandCard);
          }
        }
        if (unoRoom.getUnoPokerDepot().size() > 0) {
          response.setNextGrabCards(true);
        } else {
          response.setNextGrabCards(false);
        }
        F30251.F302513S2C.Builder realData = F30251.F302513S2C.newBuilder();
        F30251.F302513S2C.Builder fakeData = F30251.F302513S2C.newBuilder();
        UnoPlayer player = unoRoom.getPlayerInfo(playerId);
        if (player.getPlayerPoker().size() == 1) {
          response.setIsHintUno(true);
        } else {
          response.setIsHintUno(false);
        }
        if (response.getIsHintUno()) {
          player.setIsUno(1);
        }
        List<UnoPoker> unoPokers = player.getPlayerPoker();
        if (CollectionUtils.isNotEmpty(unoPokers)) {
          List<UnoPoker> playerPoker = unoRoom.pokerSort(unoPokers);
          player.setPlayerPoker(playerPoker);
          F30251.CardInfos.Builder lastHandCard;
          F30251.CardInfos.Builder fakeHandCard;
          for (UnoPoker poker : playerPoker) {
            lastHandCard = F30251.CardInfos.newBuilder();
            fakeHandCard = F30251.CardInfos.newBuilder();
            lastHandCard.setHandCardID(poker.getPokerId());
            lastHandCard.setHandCardDecor(poker.getPokerColors());
            lastHandCard.setCardFunc(poker.getPokerFunction());
            fakeHandCard.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
            realData.addLastHandCard(lastHandCard);
            fakeData.addLastHandCard(fakeHandCard);
          }
        }
        response.setCardStock(unoRoom.getUnoPokerDepot().size());
        List<UnoPlayer> playerList = unoRoom.getPlayerList();
        if (CollectionUtils.isNotEmpty(playerList)) {
          for (UnoPlayer unoPlayer : playerList) {
            if (unoPlayer.getLinkStatus() == 0) {
              if (unoPlayer.getPlayerId().equals(playerId)) {
                response.addAllLastHandCard(realData.getLastHandCardList());
              } else {
                response.addAllLastHandCard(fakeData.getLastHandCardList());
              }
              unoPlayer.sendPacket(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_CARDS,
                  response.build().toByteArray()));
              response.clearLastHandCard();
            }
          }
        }
        unoRoom.setPlayCard(response.build().toByteArray());
        F30251.F3025116S2C.Builder watchInfo = F30251.F3025116S2C.newBuilder();
        watchInfo.setPlayerAction(0);
        watchInfo.setActionUserId(response.getActionUserId());
        watchInfo.setPlayCardId(response.getPlayCardId());
        watchInfo.setDirection(unoRoom.getDirection());
        watchInfo.setNowColor(unoRoom.getNowColors());
        F30251.PlayCard.Builder playCard;
        for (UnoPlayer unoPlayer : playerList) {
          playCard = F30251.PlayCard.newBuilder();
          if (unoPlayer.getPlayerId() > 0) {
            playCard.setPlayerId(unoPlayer.getPlayerId());
            playCard.setCardNum(unoPlayer.getPlayerPoker().size());
            watchInfo.addPlayLastCard(playCard);
          }
        }
        watchInfo.setNextUser(response.getNextUser());
        watchInfo.setNextTimes(response.getNextTimes());
        watchInfo.setIsHintUno(response.getIsHintUno());
        unoRoom.getWatchGroup().writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_INFO,
            watchInfo.build().toByteArray()));
        unoRoom.setWatchInfo(watchInfo.build().toByteArray());
        UnoPlayer checkPlayer = unoRoom.getPlayerInfo(unoRoom.getNowActionId());
        if (player.getPlayerPoker().size() == 0) {
          gameFinish(roomId);
        } else if (unoRoom.getUnoPokerDepot().size() == 0) {
          gameFinish(roomId);
        } else if (response.getHintHandCardList().size() == 0) {
          if (checkPlayer.getLinkStatus() == 1) {
            // 托管定时
            trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          } else {
            // 自动摸牌
            autoTouchCardTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
            // 操作定时
            actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          }
        } else {
          if (checkPlayer.getLinkStatus() == 1) {
            // 托管定时
            trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          } else {
            // 操作定时
            actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 增加卡牌. 
   *
   * @param roomId [房间ID]
   * @param playerId [玩家ID]
   * @param cardInfo [卡牌信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/18 15:25
   * @update 2020/8/18 15:25
   */
  private void increaseCard(Long roomId, Long playerId, UnoPoker cardInfo) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        unoRoom.initByte();
        F30251.CardInfos.Builder cardInfos = F30251.CardInfos.newBuilder();
        cardInfos.setHandCardID(cardInfo.getPokerId());
        cardInfos.setHandCardDecor(cardInfo.getPokerColors());
        cardInfos.setCardFunc(cardInfo.getPokerFunction());
        F30251.F302513S2C.Builder response = F30251.F302513S2C.newBuilder();
        response.setResult(0);
        response.setActionUserId(playerId);
        response.setPlayCardId(cardInfos);
        unoRoom.setNowColors(cardInfo.getPokerColors());
        response.setNowColor(unoRoom.getNowColors());
        response.setDirection(unoRoom.getDirection());
        // 特效
        F30251.CardAbility.Builder ability = F30251.CardAbility.newBuilder();
        long targetId = unoRoom.getNextActionUser(unoRoom.getNowActionId());
        ability.setCardFunc(1).setAttackedPlayer(targetId);
        List<UnoPoker> newPokers = Lists.newLinkedList();
        while (newPokers.size() < 2) {
          if (CollectionUtils.isNotEmpty(unoRoom.getUnoPokerDepot())) {
            newPokers.add(unoRoom.getUnoPokerDepot().remove(0));
          } else {
            break;
          }
        }
        F30251.CardInfos.Builder addCardId;
        for (UnoPoker newPoker : newPokers) {
          addCardId = F30251.CardInfos.newBuilder();
          addCardId.setHandCardID(newPoker.getPokerId());
          addCardId.setHandCardDecor(newPoker.getPokerColors());
          addCardId.setCardFunc(newPoker.getPokerFunction());
          ability.addAddCardId(addCardId);
        }
        UnoPlayer targetPlayer = unoRoom.getPlayerInfo(targetId);
        targetPlayer.getPlayerPoker().addAll(newPokers);
        List<UnoPoker> targetPlayerPoker = targetPlayer.getPlayerPoker();
        targetPlayerPoker = unoRoom.pokerSort(targetPlayerPoker);
        F30251.CardInfos.Builder handCards;
        for (UnoPoker unoPoker : targetPlayerPoker) {
          handCards = F30251.CardInfos.newBuilder();
          handCards.setHandCardID(unoPoker.getPokerId());
          handCards.setHandCardDecor(unoPoker.getPokerColors());
          handCards.setCardFunc(unoPoker.getPokerFunction());
          ability.addHandCards(handCards);
        }
        response.setCardAbility(ability);
        unoRoom.playCards(cardInfo, playerId);
        unoRoom.setUpActionPlayer(targetId, unoRoom.getDirection());
        response.setNextUser(unoRoom.getNowActionId());
        response.setNextTimes(15);
        UnoPlayer nextPlayer = unoRoom.getPlayerInfo(response.getNextUser());
        List<UnoPoker> hintPoker = unoRoom.hintPoker(nextPlayer.getPlayerPoker());
        if (CollectionUtils.isNotEmpty(hintPoker)) {
          F30251.CardInfos.Builder hintHandCard;
          for (UnoPoker poker : hintPoker) {
            hintHandCard = F30251.CardInfos.newBuilder();
            hintHandCard.setHandCardID(poker.getPokerId());
            hintHandCard.setHandCardDecor(poker.getPokerColors());
            hintHandCard.setCardFunc(poker.getPokerFunction());
            response.addHintHandCard(hintHandCard);
          }
        }
        if (unoRoom.getUnoPokerDepot().size() > 0) {
          response.setNextGrabCards(true);
        } else {
          response.setNextGrabCards(false);
        }
        List<Long> playerIds = Lists.newLinkedList();
        playerIds.add(playerId);
        playerIds.add(targetId);
        F30251.F302513S2C.Builder realDataOne = F30251.F302513S2C.newBuilder();
        F30251.F302513S2C.Builder realDataTwo = F30251.F302513S2C.newBuilder();
        F30251.F302513S2C.Builder fakeData = F30251.F302513S2C.newBuilder();
        for (Long userId : playerIds) {
          UnoPlayer player = unoRoom.getPlayerInfo(userId);
          List<UnoPoker> unoPokers = player.getPlayerPoker();
          if (CollectionUtils.isNotEmpty(unoPokers)) {
            List<UnoPoker> playerPoker = unoRoom.pokerSort(unoPokers);
            player.setPlayerPoker(playerPoker);
            if (userId.equals(playerId)) {
              if (player.getPlayerPoker().size() == 1) {
                response.setIsHintUno(true);
              } else {
                response.setIsHintUno(false);
              }
              if (response.getIsHintUno()) {
                player.setIsUno(1);
              }
              F30251.CardInfos.Builder lastHandCard;
              F30251.CardInfos.Builder fakeHandCard;
              for (UnoPoker poker : playerPoker) {
                lastHandCard = F30251.CardInfos.newBuilder();
                fakeHandCard = F30251.CardInfos.newBuilder();
                lastHandCard.setHandCardID(poker.getPokerId());
                lastHandCard.setHandCardDecor(poker.getPokerColors());
                lastHandCard.setCardFunc(poker.getPokerFunction());
                fakeHandCard.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
                realDataOne.addLastHandCard(lastHandCard);
                fakeData.addLastHandCard(fakeHandCard);
              }
            } else {
              F30251.CardInfos.Builder lastHandCard;
              for (UnoPoker poker : playerPoker) {
                lastHandCard = F30251.CardInfos.newBuilder();
                lastHandCard.setHandCardID(poker.getPokerId());
                lastHandCard.setHandCardDecor(poker.getPokerColors());
                lastHandCard.setCardFunc(poker.getPokerFunction());
                realDataTwo.addLastHandCard(lastHandCard);
              }
            }
          }
        }
        response.setCardStock(unoRoom.getUnoPokerDepot().size());
        List<UnoPlayer> playerList = unoRoom.getPlayerList();
        if (CollectionUtils.isNotEmpty(playerList)) {
          for (UnoPlayer unoPlayer : playerList) {
            if (unoPlayer.getLinkStatus() == 0) {
              if (unoPlayer.getPlayerId().equals(playerId)) {
                response.addAllLastHandCard(realDataOne.getLastHandCardList());
              } else if (unoPlayer.getPlayerId().equals(targetId)) {
                response.addAllLastHandCard(realDataTwo.getLastHandCardList());
              } else {
                response.addAllLastHandCard(fakeData.getLastHandCardList());
              }
              unoPlayer.sendPacket(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_CARDS,
                  response.build().toByteArray()));
              response.clearLastHandCard();
            }
          }
        }
        unoRoom.setPlayCard(response.build().toByteArray());
        F30251.F3025116S2C.Builder watchInfo = F30251.F3025116S2C.newBuilder();
        watchInfo.setPlayerAction(0);
        watchInfo.setActionUserId(response.getActionUserId());
        watchInfo.setPlayCardId(response.getPlayCardId());
        watchInfo.setDirection(unoRoom.getDirection());
        watchInfo.setNowColor(unoRoom.getNowColors());
        watchInfo.setCardAbility(response.getCardAbility());
        F30251.PlayCard.Builder playCard;
        for (UnoPlayer unoPlayer : playerList) {
          playCard = F30251.PlayCard.newBuilder();
          if (unoPlayer.getPlayerId() > 0) {
            playCard.setPlayerId(unoPlayer.getPlayerId());
            playCard.setCardNum(unoPlayer.getPlayerPoker().size());
            watchInfo.addPlayLastCard(playCard);
          }
        }
        watchInfo.setNextUser(response.getNextUser());
        watchInfo.setNextTimes(response.getNextTimes());
        watchInfo.setIsHintUno(response.getIsHintUno());
        unoRoom.getWatchGroup().writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_INFO,
            watchInfo.build().toByteArray()));
        unoRoom.setWatchInfo(watchInfo.build().toByteArray());
        UnoPlayer player = unoRoom.getPlayerInfo(playerId);
        UnoPlayer checkPlayer = unoRoom.getPlayerInfo(unoRoom.getNowActionId());
        if (player.getPlayerPoker().size() == 0) {
          gameFinish(roomId);
        } else if (unoRoom.getUnoPokerDepot().size() == 0) {
          gameFinish(roomId);
        } else if (response.getHintHandCardList().size() == 0) {
          if (checkPlayer.getLinkStatus() == 1) {
            // 托管定时
            trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          } else {
            // 自动摸牌
            autoTouchCardTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
            // 操作定时
            actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          }
        } else {
          if (checkPlayer.getLinkStatus() == 1) {
            // 托管定时
            trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          } else {
            // 操作定时
            actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 改变颜色. 
   *
   * @param roomId [房间ID]
   * @param playerId [玩家ID]
   * @param cardInfo [卡牌信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/18 15:43
   * @update 2020/8/18 15:43
   */
  private void changeColor(Long roomId, Long playerId, UnoPoker cardInfo) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        unoRoom.initByte();
        F30251.CardInfos.Builder cardInfos = F30251.CardInfos.newBuilder();
        cardInfos.setHandCardID(cardInfo.getPokerId());
        cardInfos.setHandCardDecor(cardInfo.getPokerColors());
        cardInfos.setCardFunc(cardInfo.getPokerFunction());
        F30251.F302513S2C.Builder response = F30251.F302513S2C.newBuilder();
        response.setResult(0);
        response.setActionUserId(playerId);
        response.setPlayCardId(cardInfos);
        F30251.CardAbility.Builder ability = F30251.CardAbility.newBuilder();
        response.setCardAbility(ability.setCardFunc(3));
        unoRoom.playCards(cardInfo, playerId);
        response.setNextUser(unoRoom.getNowActionId());
        LocalDateTime udt = unoRoom.getActionTime().plusSeconds(15L);
        LocalDateTime nds = LocalDateTime.now();
        Duration duration = Duration.between(nds, udt);
        int second = Math.toIntExact(duration.getSeconds());
        response.setNextTimes(second);
        F30251.F302513S2C.Builder realData = F30251.F302513S2C.newBuilder();
        F30251.F302513S2C.Builder fakeData = F30251.F302513S2C.newBuilder();
        UnoPlayer player = unoRoom.getPlayerInfo(playerId);
        player.setPlayerAction(1);
        if (player.getPlayerPoker().size() == 1) {
          response.setIsHintUno(true);
        } else {
          response.setIsHintUno(false);
        }
        if (response.getIsHintUno()) {
          player.setIsUno(1);
        }
        List<UnoPoker> unoPokers = player.getPlayerPoker();
        if (CollectionUtils.isNotEmpty(unoPokers)) {
          List<UnoPoker> playerPoker = unoRoom.pokerSort(unoPokers);
          player.setPlayerPoker(playerPoker);
          F30251.CardInfos.Builder lastHandCard;
          F30251.CardInfos.Builder fakeHandCard;
          for (UnoPoker poker : playerPoker) {
            lastHandCard = F30251.CardInfos.newBuilder();
            fakeHandCard = F30251.CardInfos.newBuilder();
            lastHandCard.setHandCardID(poker.getPokerId());
            lastHandCard.setHandCardDecor(poker.getPokerColors());
            lastHandCard.setCardFunc(poker.getPokerFunction());
            fakeHandCard.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
            realData.addLastHandCard(lastHandCard);
            fakeData.addLastHandCard(fakeHandCard);
          }
        }
        response.setCardStock(unoRoom.getUnoPokerDepot().size());
        List<UnoPlayer> playerList = unoRoom.getPlayerList();
        if (CollectionUtils.isNotEmpty(playerList)) {
          for (UnoPlayer unoPlayer : playerList) {
            if (unoPlayer.getLinkStatus() == 0) {
              if (unoPlayer.getPlayerId().equals(playerId)) {
                response.addAllLastHandCard(realData.getLastHandCardList());
              } else {
                response.addAllLastHandCard(fakeData.getLastHandCardList());
              }
              unoPlayer.sendPacket(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_CARDS,
                  response.build().toByteArray()));
              response.clearLastHandCard();
            }
          }
        }
        unoRoom.setPlayCard(response.build().toByteArray());
        F30251.F3025116S2C.Builder watchInfo = F30251.F3025116S2C.newBuilder();
        watchInfo.setPlayerAction(0);
        watchInfo.setActionUserId(response.getActionUserId());
        watchInfo.setPlayCardId(response.getPlayCardId());
        watchInfo.setDirection(unoRoom.getDirection());
        watchInfo.setNowColor(unoRoom.getNowColors());
        watchInfo.setCardAbility(response.getCardAbility());
        F30251.PlayCard.Builder playCard;
        for (UnoPlayer unoPlayer : playerList) {
          playCard = F30251.PlayCard.newBuilder();
          if (unoPlayer.getPlayerId() > 0) {
            playCard.setPlayerId(unoPlayer.getPlayerId());
            playCard.setCardNum(unoPlayer.getPlayerPoker().size());
            watchInfo.addPlayLastCard(playCard);
          }
        }
        watchInfo.setNextUser(response.getNextUser());
        watchInfo.setNextTimes(response.getNextTimes());
        watchInfo.setIsHintUno(response.getIsHintUno());
        unoRoom.getWatchGroup().writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_INFO,
            watchInfo.build().toByteArray()));
        unoRoom.setWatchInfo(watchInfo.build().toByteArray());
        if (!unoRoom.getTimeOutMap().containsKey(playerId.intValue())) {
          rightNowSelectColor(roomId, playerId);
        } else if (player.getLinkStatus() == 1) {
          rightNowSelectColor(roomId, playerId);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 质疑卡牌. 
   *
   * @param roomId [房间ID]
   * @param playerId [玩家ID]
   * @param cardInfo [卡牌信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/18 15:44
   * @update 2020/8/18 15:44
   */
  private void questionCard(Long roomId, Long playerId, UnoPoker cardInfo) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        unoRoom.playCards(cardInfo, playerId);
        UnoPlayer player = unoRoom.getPlayerInfo(playerId);
        player.setPlayerAction(2);
        if (unoRoom.getTimeOutMap().isEmpty()) {
          rightNowSelectColor(roomId, playerId);
        } else if (player.getLinkStatus() == 1) {
          rightNowSelectColor(roomId, playerId);
        } else {
          player.sendPacket(new Packet(ActionCmd.GAME_UNO, UnoCmd.QUESTION_COLOR, null));
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 数字卡牌. 
   *
   * @param roomId [房间ID]
   * @param playerId [玩家ID]
   * @param cardInfo [卡牌信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/18 17:14
   * @update 2020/8/18 17:14
   */
  private void digitalCard(Long roomId, Long playerId, UnoPoker cardInfo) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        unoRoom.initByte();
        F30251.CardInfos.Builder cardInfos = F30251.CardInfos.newBuilder();
        cardInfos.setHandCardID(cardInfo.getPokerId());
        cardInfos.setHandCardDecor(cardInfo.getPokerColors());
        cardInfos.setCardFunc(cardInfo.getPokerFunction());
        F30251.F302513S2C.Builder response = F30251.F302513S2C.newBuilder();
        response.setResult(0);
        response.setActionUserId(playerId);
        response.setPlayCardId(cardInfos);
        UnoPoker lastPoker = unoRoom.getLastPoker();
        if (cardInfos.getHandCardID() == lastPoker.getPokerId()
            && cardInfos.getHandCardDecor() != unoRoom.getNowColors()) {
          response.setNowColor(cardInfos.getHandCardDecor());
          unoRoom.setNowColors(response.getNowColor());
        }
        response.setDirection(unoRoom.getDirection());
        unoRoom.playCards(cardInfo, playerId);
        unoRoom.setUpActionPlayer(unoRoom.getNowActionId(), unoRoom.getDirection());
        response.setNextUser(unoRoom.getNowActionId());
        response.setNextTimes(15);
        UnoPlayer nextPlayer = unoRoom.getPlayerInfo(response.getNextUser());
        List<UnoPoker> hintPoker = unoRoom.hintPoker(nextPlayer.getPlayerPoker());
        if (CollectionUtils.isNotEmpty(hintPoker)) {
          F30251.CardInfos.Builder hintHandCard;
          for (UnoPoker poker : hintPoker) {
            hintHandCard = F30251.CardInfos.newBuilder();
            hintHandCard.setHandCardID(poker.getPokerId());
            hintHandCard.setHandCardDecor(poker.getPokerColors());
            hintHandCard.setCardFunc(poker.getPokerFunction());
            response.addHintHandCard(hintHandCard);
          }
        }
        if (unoRoom.getUnoPokerDepot().size() > 0) {
          response.setNextGrabCards(true);
        } else {
          response.setNextGrabCards(false);
        }
        F30251.F302513S2C.Builder realData = F30251.F302513S2C.newBuilder();
        F30251.F302513S2C.Builder fakeData = F30251.F302513S2C.newBuilder();
        UnoPlayer player = unoRoom.getPlayerInfo(playerId);
        if (player.getPlayerPoker().size() == 1) {
          response.setIsHintUno(true);
        } else {
          response.setIsHintUno(false);
        }
        if (response.getIsHintUno()) {
          player.setIsUno(1);
        }
        List<UnoPoker> unoPokers = player.getPlayerPoker();
        if (CollectionUtils.isNotEmpty(unoPokers)) {
          List<UnoPoker> playerPoker = unoRoom.pokerSort(unoPokers);
          player.setPlayerPoker(playerPoker);
          F30251.CardInfos.Builder lastHandCard;
          F30251.CardInfos.Builder fakeHandCard;
          for (UnoPoker poker : playerPoker) {
            lastHandCard = F30251.CardInfos.newBuilder();
            fakeHandCard = F30251.CardInfos.newBuilder();
            lastHandCard.setHandCardID(poker.getPokerId());
            lastHandCard.setHandCardDecor(poker.getPokerColors());
            lastHandCard.setCardFunc(poker.getPokerFunction());
            fakeHandCard.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
            realData.addLastHandCard(lastHandCard);
            fakeData.addLastHandCard(fakeHandCard);
          }
        }
        response.setCardStock(unoRoom.getUnoPokerDepot().size());
        List<UnoPlayer> playerList = unoRoom.getPlayerList();
        if (CollectionUtils.isNotEmpty(playerList)) {
          for (UnoPlayer unoPlayer : playerList) {
            if (unoPlayer.getLinkStatus() == 0) {
              if (unoPlayer.getPlayerId().equals(playerId)) {
                response.addAllLastHandCard(realData.getLastHandCardList());
              } else {
                response.addAllLastHandCard(fakeData.getLastHandCardList());
              }
              unoPlayer.sendPacket(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_CARDS,
                  response.build().toByteArray()));
              response.clearLastHandCard();
            }
          }
        }
        unoRoom.setPlayCard(response.build().toByteArray());
        F30251.F3025116S2C.Builder watchInfo = F30251.F3025116S2C.newBuilder();
        watchInfo.setPlayerAction(0);
        watchInfo.setActionUserId(response.getActionUserId());
        watchInfo.setPlayCardId(response.getPlayCardId());
        watchInfo.setDirection(unoRoom.getDirection());
        watchInfo.setNowColor(unoRoom.getNowColors());
        F30251.PlayCard.Builder playCard;
        for (UnoPlayer unoPlayer : playerList) {
          playCard = F30251.PlayCard.newBuilder();
          if (unoPlayer.getPlayerId() > 0) {
            playCard.setPlayerId(unoPlayer.getPlayerId());
            playCard.setCardNum(unoPlayer.getPlayerPoker().size());
            watchInfo.addPlayLastCard(playCard);
          }
        }
        watchInfo.setNextUser(response.getNextUser());
        watchInfo.setNextTimes(response.getNextTimes());
        watchInfo.setIsHintUno(response.getIsHintUno());
        unoRoom.getWatchGroup().writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_INFO,
            watchInfo.build().toByteArray()));
        unoRoom.setWatchInfo(watchInfo.build().toByteArray());
        UnoPlayer checkPlayer = unoRoom.getPlayerInfo(unoRoom.getNowActionId());
        if (player.getPlayerPoker().size() == 0) {
          gameFinish(roomId);
        } else if (unoRoom.getUnoPokerDepot().size() == 0) {
          gameFinish(roomId);
        } else if (response.getHintHandCardList().size() == 0) {
          if (checkPlayer.getLinkStatus() == 1) {
            // 托管定时
            trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          } else {
            // 自动摸牌
            autoTouchCardTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
            // 操作定时
            actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          }
        } else {
          if (checkPlayer.getLinkStatus() == 1) {
            // 托管定时
            trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          } else {
            // 操作定时
            actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 选择颜色. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/17 10:04
   * @update 2020/8/17 10:04
   */
  private void selectColor(Channel channel, Packet packet) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(unoRoom)) {
        UnoPlayer checkPlayer = unoRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          if (unoRoom.getNowActionId() == packet.userId) {
            unoRoom.initByte();
            unoRoom.cancelTimeOut((int) packet.userId);
            F30251.F302514C2S request = F30251.F302514C2S.parseFrom(packet.bytes);
            F30251.F302514S2C.Builder response = F30251.F302514S2C.newBuilder();
            response.setActionUserID(packet.userId);
            response.setDecor(request.getDecor());
            UnoPlayer player = unoRoom.getPlayerInfo(packet.userId);
            if (player.getPlayerAction() > 1) {
              if (player.getPlayerPoker().size() == 1) {
                response.setIsHintUno(true);
              } else {
                response.setIsHintUno(false);
              }
              if (response.getIsHintUno()) {
                player.setIsUno(1);
              }
              UnoPoker unoPoker = unoRoom.getLastPoker();
              F30251.CardInfos.Builder playCardId = F30251.CardInfos.newBuilder();
              playCardId.setHandCardID(unoPoker.getPokerId());
              playCardId.setHandCardDecor(unoPoker.getPokerColors());
              playCardId.setCardFunc(unoPoker.getPokerFunction());
              response.setPlayCardId(playCardId);
              F30251.CardAbility.Builder ability = F30251.CardAbility.newBuilder();
              long targetId = unoRoom.getNextActionUser(unoRoom.getNowActionId());
              UnoPlayer targetPlayer = unoRoom.getPlayerInfo(targetId);
              int color = unoRoom.getNowColors();
              unoRoom.setNowColors(request.getDecor());
              targetPlayer.setReceiveQuestion(color);
              ability.setCardFunc(2).setAttackedPlayer(targetId).setColor(color);
              response.setCardAbility(ability);
              unoRoom.setUpActionPlayer(unoRoom.getNowActionId(), unoRoom.getDirection());
              response.setNextUserID(unoRoom.getNowActionId());
              response.setNextTimes(15);
              F30251.F302514S2C.Builder realData = F30251.F302514S2C.newBuilder();
              F30251.F302514S2C.Builder fakeData = F30251.F302514S2C.newBuilder();
              List<UnoPoker> unoPokers = player.getPlayerPoker();
              if (CollectionUtils.isNotEmpty(unoPokers)) {
                List<UnoPoker> playerPoker = unoRoom.pokerSort(player.getPlayerPoker());
                player.setPlayerPoker(playerPoker);
                F30251.CardInfos.Builder lastHandCard;
                F30251.CardInfos.Builder fakeHandCard;
                for (UnoPoker poker : playerPoker) {
                  lastHandCard = F30251.CardInfos.newBuilder();
                  lastHandCard.setHandCardID(poker.getPokerId());
                  lastHandCard.setHandCardDecor(poker.getPokerColors());
                  lastHandCard.setCardFunc(poker.getPokerFunction());
                  realData.addLastHandCard(lastHandCard);
                  fakeHandCard = F30251.CardInfos.newBuilder();
                  fakeHandCard.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
                  fakeData.addLastHandCard(fakeHandCard);
                }
              }
              response.setCardStock(unoRoom.getUnoPokerDepot().size());
              response.setDirection(unoRoom.getDirection());
              List<UnoPlayer> playerList = unoRoom.getPlayerList();
              if (CollectionUtils.isNotEmpty(playerList)) {
                for (UnoPlayer unoPlayer : playerList) {
                  if (unoPlayer.getLinkStatus() == 0) {
                    if (unoPlayer.getPlayerId() == packet.userId) {
                      response.addAllLastHandCard(realData.getLastHandCardList());
                    } else {
                      response.addAllLastHandCard(fakeData.getLastHandCardList());
                    }
                    unoPlayer.sendPacket(new Packet(ActionCmd.GAME_UNO, UnoCmd.SELECT_COLOR,
                        response.build().toByteArray()));
                    response.clearLastHandCard();
                  }
                }
              }
              unoRoom.setSelectColor(response.build().toByteArray());
              F30251.F3025116S2C.Builder watchInfo = F30251.F3025116S2C.newBuilder();
              watchInfo.setPlayerAction(1);
              watchInfo.setActionUserId(response.getActionUserID());
              watchInfo.setPlayCardId(response.getPlayCardId());
              watchInfo.setDirection(unoRoom.getDirection());
              watchInfo.setNowColor(unoRoom.getNowColors());
              watchInfo.setCardAbility(response.getCardAbility());
              F30251.PlayCard.Builder playCard;
              for (UnoPlayer unoPlayer : playerList) {
                playCard = F30251.PlayCard.newBuilder();
                if (unoPlayer.getPlayerId() > 0) {
                  playCard.setPlayerId(unoPlayer.getPlayerId());
                  playCard.setCardNum(unoPlayer.getPlayerPoker().size());
                  watchInfo.addPlayLastCard(playCard);
                }
              }
              watchInfo.setNextUser(response.getNextUserID());
              watchInfo.setNextTimes(response.getNextTimes());
              watchInfo.setIsHintUno(response.getIsHintUno());
              unoRoom.getWatchGroup().writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_INFO,
                  watchInfo.build().toByteArray()));
              unoRoom.setWatchInfo(watchInfo.build().toByteArray());
            } else {
              unoRoom.setNowColors(request.getDecor());
              unoRoom.setUpActionPlayer(unoRoom.getNowActionId(), unoRoom.getDirection());
              response.setNextUserID(unoRoom.getNowActionId());
              response.setNextTimes(15);
              UnoPlayer nextPlayer = unoRoom.getPlayerInfo(response.getNextUserID());
              List<UnoPoker> hintPoker = unoRoom.hintPoker(nextPlayer.getPlayerPoker());
              if (CollectionUtils.isNotEmpty(hintPoker)) {
                F30251.CardInfos.Builder hintHandCard;
                for (UnoPoker poker : hintPoker) {
                  hintHandCard = F30251.CardInfos.newBuilder();
                  hintHandCard.setHandCardID(poker.getPokerId());
                  hintHandCard.setHandCardDecor(poker.getPokerColors());
                  hintHandCard.setCardFunc(poker.getPokerFunction());
                  response.addHintHandCard(hintHandCard);
                }
              }
              if (unoRoom.getUnoPokerDepot().size() > 0) {
                response.setNextGrabCards(true);
              } else {
                response.setNextGrabCards(false);
              }
              F30251.F302514S2C.Builder realData = F30251.F302514S2C.newBuilder();
              F30251.F302514S2C.Builder fakeData = F30251.F302514S2C.newBuilder();
              List<UnoPoker> unoPokers = player.getPlayerPoker();
              if (CollectionUtils.isNotEmpty(unoPokers)) {
                List<UnoPoker> playerPoker = unoRoom.pokerSort(player.getPlayerPoker());
                player.setPlayerPoker(playerPoker);
                F30251.CardInfos.Builder lastHandCard;
                F30251.CardInfos.Builder fakeHandCard;
                for (UnoPoker poker : playerPoker) {
                  lastHandCard = F30251.CardInfos.newBuilder();
                  lastHandCard.setHandCardID(poker.getPokerId());
                  lastHandCard.setHandCardDecor(poker.getPokerColors());
                  lastHandCard.setCardFunc(poker.getPokerFunction());
                  realData.addLastHandCard(lastHandCard);
                  fakeHandCard = F30251.CardInfos.newBuilder();
                  fakeHandCard.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
                  fakeData.addLastHandCard(fakeHandCard);
                }
              }
              response.setDirection(unoRoom.getDirection());
              List<UnoPlayer> playerList = unoRoom.getPlayerList();
              if (CollectionUtils.isNotEmpty(playerList)) {
                for (UnoPlayer unoPlayer : playerList) {
                  if (unoPlayer.getLinkStatus() == 0) {
                    if (unoPlayer.getPlayerId() == packet.userId) {
                      response.addAllLastHandCard(realData.getLastHandCardList());
                    } else {
                      response.addAllLastHandCard(fakeData.getLastHandCardList());
                    }
                    unoPlayer.sendPacket(new Packet(ActionCmd.GAME_UNO, UnoCmd.SELECT_COLOR,
                        response.build().toByteArray()));
                    response.clearLastHandCard();
                  }
                }
              }
              unoRoom.setSelectColor(response.build().toByteArray());
              F30251.F3025116S2C.Builder watchInfo = F30251.F3025116S2C.newBuilder();
              watchInfo.setPlayerAction(1);
              watchInfo.setActionUserId(response.getActionUserID());
              watchInfo.setDirection(unoRoom.getDirection());
              watchInfo.setNowColor(unoRoom.getNowColors());
              F30251.PlayCard.Builder playCard;
              for (UnoPlayer unoPlayer : playerList) {
                playCard = F30251.PlayCard.newBuilder();
                if (unoPlayer.getPlayerId() > 0) {
                  playCard.setPlayerId(unoPlayer.getPlayerId());
                  playCard.setCardNum(unoPlayer.getPlayerPoker().size());
                  watchInfo.addPlayLastCard(playCard);
                }
              }
              watchInfo.setNextUser(response.getNextUserID());
              watchInfo.setNextTimes(response.getNextTimes());
              watchInfo.setIsHintUno(response.getIsHintUno());
              unoRoom.getWatchGroup().writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_INFO,
                  watchInfo.build().toByteArray()));
              unoRoom.setWatchInfo(watchInfo.build().toByteArray());
            }
            UnoPlayer nextPlayer = unoRoom.getPlayerInfo(unoRoom.getNowActionId());
            if (player.getPlayerPoker().size() == 0) {
              player.setPlayerAction(0);
              gameFinish(packet.roomId);
            } else if (unoRoom.getUnoPokerDepot().size() == 0) {
              player.setPlayerAction(0);
              gameFinish(packet.roomId);
            } else if (response.getHintHandCardList().size() == 0 && player.getPlayerAction() <= 1) {
              player.setPlayerAction(0);
              if (nextPlayer.getLinkStatus() == 1) {
                // 托管定时
                trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
              } else {
                // 自动摸牌
                autoTouchCardTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
                // 操作定时
                actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
              }
            } else if (player.getPlayerAction() > 0) {
              player.setPlayerAction(0);
              if (nextPlayer.getLinkStatus() == 1) {
                // 托管定时
                trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
              } else {
                // 操作定时
                actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
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
   * @create 2020/8/17 10:04
   * @update 2020/8/17 10:04
   */
  private void touchCards(Channel channel, Packet packet) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(unoRoom)) {
        UnoPlayer checkPlayer = unoRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          if (unoRoom.getNowActionId() == packet.userId) {
            if (Objects.isNull(checkPlayer.getTouchCards())) {
              F30251.F302515S2C.Builder builder = F30251.F302515S2C.newBuilder();
              builder.setPlayerId(packet.userId);
              UnoPlayer player = unoRoom.getPlayerInfo(packet.userId);
              if (player.getLinkStatus() == 0) {
                // 玩家成就.抽卡人生
                Map<String, Object> taskSuc0036 = Maps.newHashMap();
                taskSuc0036.put("userId", packet.userId);
                taskSuc0036.put("code", AchievementEnum.AMT0036.getCode());
                taskSuc0036.put("progress", 1);
                taskSuc0036.put("isReset", 0);
                this.userRemote.achievementHandlers(taskSuc0036);
              }
              player.setPlayerAction(3);
              UnoPoker lastPoker = unoRoom.getLastPoker();
              UnoPoker unoPoker = unoRoom.touchCards();
              if (unoPoker.getPokerId() == 13 || unoPoker.getPokerId() == 14) {
                // 可以出牌
                builder.setIsPlay(0);
                player.getPlayerPoker().add(unoPoker);
              } else if (unoPoker.getPokerColors().equals(unoRoom.getNowColors())) {
                // 可以出牌
                builder.setIsPlay(0);
                player.getPlayerPoker().add(unoPoker);
              } else if (!unoPoker.getPokerColors().equals(lastPoker.getPokerColors())
                  && unoPoker.getPokerId().equals(lastPoker.getPokerId())) {
                // 可以出牌
                builder.setIsPlay(0);
                player.getPlayerPoker().add(unoPoker);
              } else {
                // 不可出牌
                unoRoom.cancelTimeOut((int) packet.userId);
                builder.setIsPlay(1);
                player.getPlayerPoker().add(unoPoker);
              }
              F30251.CardInfos.Builder touchCard = F30251.CardInfos.newBuilder();
              touchCard.setHandCardID(unoPoker.getPokerId());
              touchCard.setHandCardDecor(unoPoker.getPokerColors());
              touchCard.setCardFunc(unoPoker.getPokerFunction());
              builder.setCardId(touchCard);
              if (builder.getIsPlay() == 1) {
                unoRoom.setUpActionPlayer(unoRoom.getNowActionId(), unoRoom.getDirection());
                builder.setNextUserID(unoRoom.getNowActionId());
                builder.setNextTimes(15);
                UnoPlayer nextPlayer = unoRoom.getPlayerInfo(builder.getNextUserID());
                List<UnoPoker> hintPoker = unoRoom.hintPoker(nextPlayer.getPlayerPoker());
                if (CollectionUtils.isNotEmpty(hintPoker)) {
                  F30251.CardInfos.Builder hintHandCard;
                  for (UnoPoker poker : hintPoker) {
                    hintHandCard = F30251.CardInfos.newBuilder();
                    hintHandCard.setHandCardID(poker.getPokerId());
                    hintHandCard.setHandCardDecor(poker.getPokerColors());
                    hintHandCard.setCardFunc(poker.getPokerFunction());
                    builder.addHintHandCard(hintHandCard);
                  }
                }
                if (unoRoom.getUnoPokerDepot().size() > 0) {
                  builder.setNextGrabCards(true);
                } else {
                  builder.setNextGrabCards(false);
                }
              } else {
                LocalDateTime udt = unoRoom.getActionTime().plusSeconds(15L);
                LocalDateTime nds = LocalDateTime.now();
                Duration duration = Duration.between(nds, udt);
                int second = Math.toIntExact(duration.getSeconds());
                builder.setNextTimes(second);
                player.setTouchCards(unoPoker);
              }
              F30251.F302515S2C.Builder realData = F30251.F302515S2C.newBuilder();
              F30251.F302515S2C.Builder fakeData = F30251.F302515S2C.newBuilder();
              List<UnoPoker> unoPokers = player.getPlayerPoker();
              if (CollectionUtils.isNotEmpty(unoPokers)) {
                List<UnoPoker> playerPoker = unoRoom.pokerSort(unoPokers);
                player.setPlayerPoker(playerPoker);
                F30251.CardInfos.Builder lastHandCard;
                F30251.CardInfos.Builder fakeHandCard;
                for (UnoPoker poker : playerPoker) {
                  lastHandCard = F30251.CardInfos.newBuilder();
                  fakeHandCard = F30251.CardInfos.newBuilder();
                  lastHandCard.setHandCardID(poker.getPokerId());
                  lastHandCard.setHandCardDecor(poker.getPokerColors());
                  lastHandCard.setCardFunc(poker.getPokerFunction());
                  fakeHandCard.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
                  realData.addLastHandCard(lastHandCard);
                  fakeData.addLastHandCard(fakeHandCard);
                }
              }
              builder.setCardStock(unoRoom.getUnoPokerDepot().size());
              List<UnoPlayer> playerList = unoRoom.getPlayerList();
              if (CollectionUtils.isNotEmpty(playerList)) {
                for (UnoPlayer unoPlayer : playerList) {
                  if (unoPlayer.getLinkStatus() == 0) {
                    if (unoPlayer.getPlayerId() == packet.userId) {
                      builder.addAllLastHandCard(realData.getLastHandCardList());
                    } else {
                      builder.addAllLastHandCard(fakeData.getLastHandCardList());
                    }
                    unoPlayer.sendPacket(new Packet(ActionCmd.GAME_UNO, UnoCmd.TOUCH_CARDS,
                        builder.build().toByteArray()));
                    builder.clearLastHandCard();
                  }
                }
              }
              unoRoom.setTouchCard(builder.build().toByteArray());
              F30251.F3025116S2C.Builder watchInfo = F30251.F3025116S2C.newBuilder();
              watchInfo.setPlayerAction(2);
              watchInfo.setActionUserId(builder.getPlayerId());
              watchInfo.setDirection(unoRoom.getDirection());
              watchInfo.setNowColor(unoRoom.getNowColors());
              if (CollectionUtils.isNotEmpty(playerList)) {
                F30251.PlayCard.Builder playCard;
                for (UnoPlayer unoPlayer : playerList) {
                  playCard = F30251.PlayCard.newBuilder();
                  if (unoPlayer.getPlayerId() > 0) {
                    playCard.setPlayerId(unoPlayer.getPlayerId());
                    playCard.setCardNum(unoPlayer.getPlayerPoker().size());
                    watchInfo.addPlayLastCard(playCard);
                  }
                }
              }
              watchInfo.setNextUser(builder.getNextUserID());
              watchInfo.setNextTimes(builder.getNextTimes());
              unoRoom.getWatchGroup().writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_INFO,
                  watchInfo.build().toByteArray()));
              unoRoom.setWatchInfo(watchInfo.build().toByteArray());
              player.setPlayerAction(0);
              if (player.getPlayerPoker().size() == 0) {
                gameFinish(packet.roomId);
              } else if (unoRoom.getUnoPokerDepot().size() == 0) {
                gameFinish(packet.roomId);
              } else if (builder.getIsPlay() == 1) {
                UnoPlayer nextPlayer = unoRoom.getPlayerInfo(unoRoom.getNowActionId());
                if (builder.getHintHandCardList().size() == 0) {
                  if (nextPlayer.getLinkStatus() == 1) {
                    // 托管定时
                    trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
                  } else {
                    // 自动摸牌
                    autoTouchCardTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
                    // 操作定时
                    actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
                  }
                } else {
                  if (nextPlayer.getLinkStatus() == 1) {
                    // 托管定时
                    trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
                  } else {
                    // 操作定时
                    actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
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
   * TODO 质疑玩家. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/17 10:05
   * @update 2020/8/17 10:05
   */
  private void questionCards(Channel channel, Packet packet) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(unoRoom)) {
        UnoPlayer checkPlayer = unoRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          if (unoRoom.getNowActionId() == packet.userId) {
            F30251.F302516C2S request = F30251.F302516C2S.parseFrom(packet.bytes);
            F30251.F302516S2C.Builder response = F30251.F302516S2C.newBuilder();
            response.setQueryAction(request.getQuery());
            response.setQueryUsreID(packet.userId);
            UnoPlayer nowPlayer = unoRoom.getPlayerInfo(packet.userId);
            long previous = unoRoom.getPreviousActionUser(nowPlayer.getPlayerId());
            UnoPlayer previousPlayer = unoRoom.getPlayerInfo(previous);
            // 1:质疑  2.直接加牌
            if (request.getQuery() == 1) {
              if (nowPlayer.getLinkStatus() == 0) {
                // 玩家成就.质疑精神
                Map<String, Object> taskSuc0033 = Maps.newHashMap();
                taskSuc0033.put("userId", packet.userId);
                taskSuc0033.put("code", AchievementEnum.AMT0033.getCode());
                taskSuc0033.put("progress", 1);
                taskSuc0033.put("isReset", 0);
                this.userRemote.achievementHandlers(taskSuc0033);
              }
              Integer color = nowPlayer.getReceiveQuestion();
              List<UnoPoker> pokerList = previousPlayer.getPlayerPoker();
              List<Integer> colorList = Lists.newLinkedList();
              pokerList.forEach(s -> colorList.add(s.getPokerColors()));
              if (colorList.contains(color)) {
                if (nowPlayer.getLinkStatus() == 0) {
                  // 玩家成就.反将一军
                  Map<String, Object> taskSuc0034 = Maps.newHashMap();
                  taskSuc0034.put("userId", packet.userId);
                  taskSuc0034.put("code", AchievementEnum.AMT0034.getCode());
                  taskSuc0034.put("progress", 1);
                  taskSuc0034.put("isReset", 0);
                  this.userRemote.achievementHandlers(taskSuc0034);
                }
                // 一起优诺.质疑成功
                List<UnoPoker> newPokers = Lists.newLinkedList();
                while (newPokers.size() < 4) {
                  if (CollectionUtils.isNotEmpty(unoRoom.getUnoPokerDepot())) {
                    newPokers.add(unoRoom.getUnoPokerDepot().remove(0));
                  } else {
                    break;
                  }
                }
                F30251.CardInfos.Builder cardId;
                for (UnoPoker newPoker : newPokers) {
                  cardId = F30251.CardInfos.newBuilder();
                  cardId.setHandCardID(newPoker.getPokerId());
                  cardId.setHandCardDecor(newPoker.getPokerColors());
                  cardId.setCardFunc(newPoker.getPokerFunction());
                  response.addCardId(cardId);
                }
                previousPlayer.getPlayerPoker().addAll(newPokers);
                response.setUserId(previous);
                response.setNextUserId(unoRoom.getNowActionId());
                LocalDateTime udt = unoRoom.getActionTime().plusSeconds(15L);
                LocalDateTime nds = LocalDateTime.now();
                Duration duration = Duration.between(nds, udt);
                int second = Math.toIntExact(duration.getSeconds());
                response.setNextTimes(second);
                List<UnoPoker> hintPoker = unoRoom.hintPoker(nowPlayer.getPlayerPoker());
                if (CollectionUtils.isNotEmpty(hintPoker)) {
                  F30251.CardInfos.Builder hintHandCard;
                  for (UnoPoker poker : hintPoker) {
                    hintHandCard = F30251.CardInfos.newBuilder();
                    hintHandCard.setHandCardID(poker.getPokerId());
                    hintHandCard.setHandCardDecor(poker.getPokerColors());
                    hintHandCard.setCardFunc(poker.getPokerFunction());
                    response.addHintHandCard(hintHandCard);
                  }
                }
                if (unoRoom.getUnoPokerDepot().size() > 0) {
                  response.setNextGrabCards(true);
                } else {
                  response.setNextGrabCards(false);
                }
                response.setQueryResult(0);

              } else {
                unoRoom.cancelTimeOut((int) packet.userId);
                // 质疑失败
                List<UnoPoker> newPokers = Lists.newLinkedList();
                while (newPokers.size() < 6) {
                  if (CollectionUtils.isNotEmpty(unoRoom.getUnoPokerDepot())) {
                    newPokers.add(unoRoom.getUnoPokerDepot().remove(0));
                  } else {
                    break;
                  }
                }
                nowPlayer.getPlayerPoker().addAll(newPokers);
                response.setUserId(nowPlayer.getPlayerId());
                unoRoom.setUpActionPlayer(unoRoom.getNowActionId(), unoRoom.getDirection());
                response.setNextUserId(unoRoom.getNowActionId());
                response.setNextTimes(15);
                UnoPlayer nextPlayer = unoRoom.getPlayerInfo(response.getNextUserId());
                List<UnoPoker> hintPoker = unoRoom.hintPoker(nextPlayer.getPlayerPoker());
                if (CollectionUtils.isNotEmpty(hintPoker)) {
                  F30251.CardInfos.Builder hintHandCard;
                  for (UnoPoker poker : hintPoker) {
                    hintHandCard = F30251.CardInfos.newBuilder();
                    hintHandCard.setHandCardID(poker.getPokerId());
                    hintHandCard.setHandCardDecor(poker.getPokerColors());
                    hintHandCard.setCardFunc(poker.getPokerFunction());
                    response.addHintHandCard(hintHandCard);
                  }
                }
                if (unoRoom.getUnoPokerDepot().size() > 0) {
                  response.setNextGrabCards(true);
                } else {
                  response.setNextGrabCards(false);
                }
                response.setQueryResult(1);
              }
            } else {
              unoRoom.cancelTimeOut((int) packet.userId);
              // 接受加牌
              List<UnoPoker> newPokers = Lists.newLinkedList();
              while (newPokers.size() < 4) {
                if (CollectionUtils.isNotEmpty(unoRoom.getUnoPokerDepot())) {
                  newPokers.add(unoRoom.getUnoPokerDepot().remove(0));
                } else {
                  break;
                }
              }
              nowPlayer.getPlayerPoker().addAll(newPokers);
              response.setUserId(nowPlayer.getPlayerId());
              unoRoom.setUpActionPlayer(unoRoom.getNowActionId(), unoRoom.getDirection());
              response.setNextUserId(unoRoom.getNowActionId());
              response.setNextTimes(15);
              UnoPlayer nextPlayer = unoRoom.getPlayerInfo(response.getNextUserId());
              List<UnoPoker> hintPoker = unoRoom.hintPoker(nextPlayer.getPlayerPoker());
              if (CollectionUtils.isNotEmpty(hintPoker)) {
                F30251.CardInfos.Builder hintHandCard;
                for (UnoPoker poker : hintPoker) {
                  hintHandCard = F30251.CardInfos.newBuilder();
                  hintHandCard.setHandCardID(poker.getPokerId());
                  hintHandCard.setHandCardDecor(poker.getPokerColors());
                  hintHandCard.setCardFunc(poker.getPokerFunction());
                  response.addHintHandCard(hintHandCard);
                }
              }
              if (unoRoom.getUnoPokerDepot().size() > 0) {
                response.setNextGrabCards(true);
              } else {
                response.setNextGrabCards(false);
              }
              response.setQueryResult(0);
            }
            F30251.F302516S2C.Builder realData = F30251.F302516S2C.newBuilder();
            F30251.F302516S2C.Builder fakeData = F30251.F302516S2C.newBuilder();
            List<UnoPoker> unoPokers;
            if (response.getUserId() == packet.userId) {
              unoPokers = nowPlayer.getPlayerPoker();
            } else {
              unoPokers = previousPlayer.getPlayerPoker();
            }
            if (CollectionUtils.isNotEmpty(unoPokers)) {
              List<UnoPoker> playerPoker = unoRoom.pokerSort(unoPokers);
              if (response.getUserId() == packet.userId) {
                nowPlayer.setPlayerPoker(playerPoker);
              } else {
                previousPlayer.setPlayerPoker(playerPoker);
              }
              F30251.CardInfos.Builder lastHandCard;
              F30251.CardInfos.Builder fakeHandCard;
              for (UnoPoker poker : playerPoker) {
                lastHandCard = F30251.CardInfos.newBuilder();
                fakeHandCard = F30251.CardInfos.newBuilder();
                lastHandCard.setHandCardID(poker.getPokerId());
                lastHandCard.setHandCardDecor(poker.getPokerColors());
                lastHandCard.setCardFunc(poker.getPokerFunction());
                fakeHandCard.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
                realData.addLastHandCard(lastHandCard);
                fakeData.addLastHandCard(fakeHandCard);
              }
            }
            response.setCardStock(unoRoom.getUnoPokerDepot().size());
            List<UnoPlayer> playerList = unoRoom.getPlayerList();
            if (CollectionUtils.isNotEmpty(playerList)) {
              for (UnoPlayer unoPlayer : playerList) {
                if (unoPlayer.getLinkStatus() == 0) {
                  if (unoPlayer.getPlayerId() == response.getUserId()) {
                    response.addAllLastHandCard(realData.getLastHandCardList());
                  } else {
                    response.addAllLastHandCard(fakeData.getLastHandCardList());
                  }
                  unoPlayer.sendPacket(new Packet(ActionCmd.GAME_UNO, UnoCmd.QUESTION_CARDS,
                      response.build().toByteArray()));
                  response.clearLastHandCard();
                }
              }
            }
            unoRoom.setQuestionCards(response.build().toByteArray());
            F30251.F3025116S2C.Builder watchInfo = F30251.F3025116S2C.newBuilder();
            watchInfo.setPlayerAction(3);
            watchInfo.setActionUserId(packet.userId);
            watchInfo.setDirection(unoRoom.getDirection());
            watchInfo.setNowColor(unoRoom.getNowColors());
            F30251.PlayCard.Builder playCard;
            for (UnoPlayer unoPlayer : playerList) {
              playCard = F30251.PlayCard.newBuilder();
              if (unoPlayer.getPlayerId() > 0) {
                playCard.setPlayerId(unoPlayer.getPlayerId());
                playCard.setCardNum(unoPlayer.getPlayerPoker().size());
                watchInfo.addPlayLastCard(playCard);
              }
            }
            watchInfo.setNextUser(response.getNextUserId());
            watchInfo.setNextTimes(response.getNextTimes());
            unoRoom.getWatchGroup().writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_INFO,
                watchInfo.build().toByteArray()));
            unoRoom.setWatchInfo(watchInfo.build().toByteArray());
            nowPlayer.setReceiveQuestion(0);
            if (nowPlayer.getPlayerPoker().size() == 0) {
              gameFinish(packet.roomId);
            } else if (unoRoom.getUnoPokerDepot().size() == 0) {
              gameFinish(packet.roomId);
            } else if (response.getUserId() == packet.userId) {
              UnoPlayer nextPlayer = unoRoom.getPlayerInfo(unoRoom.getNowActionId());
              if (response.getHintHandCardList().size() == 0) {
                if (nextPlayer.getLinkStatus() == 1) {
                  // 托管定时
                  trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
                } else {
                  // 自动摸牌
                  autoTouchCardTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
                  // 操作定时
                  actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
                }
              } else {
                if (nextPlayer.getLinkStatus() == 1) {
                  trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
                } else {
                  actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
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
   * TODO 玩家退出. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/17 10:05
   * @update 2020/8/17 10:05
   */
  private void playerExit(Channel channel, Packet packet) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(unoRoom)) {
        UnoPlayer checkPlayer = unoRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          if (unoRoom.getRoomStatus() == 1) {
            if (checkPlayer.getIdentity() == 0) {
              checkPlayer.setLinkStatus(1);
              if (checkPlayer.getPlayerStatus() == 2) {
                // 中途退出扣 -120金币
                gainExperience(packet.userId, 0, -120);
              }
              checkPlayer.setPlayerStatus(3);
              closePage(channel, packet);
              F30251.F302518S2C.Builder response = F30251.F302518S2C.newBuilder();
              GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAYER_EXIT,
                  response.setUserID(packet.userId).build().toByteArray()), unoRoom.getRoomId());
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              this.gameRemote.leaveRoom(packet.userId);
              if (unoRoom.seatedPlayersIsDisconnected() == unoRoom.seatedPlayersNum()
                  && unoRoom.seatedPlayers().size() == 0 && unoRoom.getWatchList().size() == 0) {
                clearData(unoRoom.getRoomId());
              }
            } else {
              unoRoom.leaveGame(packet.userId, 1);
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              this.gameRemote.leaveRoom(packet.userId);
              if (unoRoom.seatedPlayersIsDisconnected() == unoRoom.seatedPlayersNum()
                  && unoRoom.seatedPlayers().size() == 0 && unoRoom.getWatchList().size() == 0) {
                clearData(unoRoom.getRoomId());
              }
            }
          } else {
            if (checkPlayer.getIdentity() ==  0) {
              if (checkPlayer.getPlayerStatus() == 0) {
                unoRoom.cancelTimeOut((int) packet.userId);
              } if (checkPlayer.getPlayerStatus() == 1) {
                if (unoRoom.getTimeOutMap().containsKey((int) UnoCmd.START_GAME)) {
                  unoRoom.cancelTimeOut(UnoCmd.START_GAME);
                }
              }
              F30251.F302518S2C.Builder response = F30251.F302518S2C.newBuilder();
              response.setUserID(packet.userId);
              GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAYER_EXIT,
                  response.build().toByteArray()), unoRoom.getRoomId());
              unoRoom.leaveGame(packet.userId, 0);
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              this.gameRemote.leaveRoom(packet.userId);
              if (unoRoom.seatedPlayers().size() == 0 && unoRoom.getWatchList().size() == 0) {
                clearData(unoRoom.getRoomId());
              } else {
                roomOpenOrCancelMatch(unoRoom.getRoomId());
                // 清理检测
                List<UnoPlayer> playerList = unoRoom.getPlayerList();
                int playGameSize = (int) playerList.stream()
                    .filter(s -> s.getPlayerId() > 0).count();
                if (playGameSize == 0) {
                  clearTimeout(unoRoom.getRoomId());
                }
              }
            } else {
              unoRoom.leaveGame(packet.userId, 1);
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              this.gameRemote.leaveRoom(packet.userId);
              if (unoRoom.seatedPlayersIsDisconnected() == unoRoom.seatedPlayersNum()
                  && unoRoom.seatedPlayers().size() == 0 && unoRoom.getWatchList().size() == 0) {
                clearData(unoRoom.getRoomId());
              } else if (unoRoom.seatedPlayers().size() == 0 && unoRoom.getWatchList().size() == 0) {
                clearData(unoRoom.getRoomId());
              }
            }
          }
        } else {
          closePage(channel, packet);
          if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
            this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
          }
          this.gameRemote.leaveRoom(packet.userId);
          if (unoRoom.seatedPlayersIsDisconnected() == unoRoom.seatedPlayersNum()
              && unoRoom.seatedPlayers().size() == 0 && unoRoom.getWatchList().size() == 0) {
            clearData(unoRoom.getRoomId());
          } else if (unoRoom.seatedPlayers().size() == 0 && unoRoom.getWatchList().size() == 0) {
            clearData(unoRoom.getRoomId());
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
   * @create 2020/8/19 13:16
   * @update 2020/8/19 13:16
   */
  private void watchInfo(Channel channel, Packet packet) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(unoRoom)) {
        UnoPlayer checkPlayer = unoRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          UnoPlayer player = unoRoom.getPlayerInfo(packet.userId);
          F30251.PlayerInfo.Builder playerInfo = F30251.PlayerInfo.newBuilder();
          playerInfo.setNick(player.getPlayerName());
          playerInfo.setUserID(player.getPlayerId());
          playerInfo.setUrl(player.getPlayerAvatar());
          playerInfo.setSex(player.getPlayerSex());
          playerInfo.setDeviPosition(player.getSeatNumber());
          playerInfo.setState(player.getPlayerStatus());
          playerInfo.setCoin(player.getPlayerGold());
          F30251.F3025190S2C.Builder response = F30251.F3025190S2C.newBuilder();
          response.setNowUser(playerInfo);
          if (unoRoom.getRoomStatus() == 0) {
            if (player.getPlayerStatus() >= 1) {
              response.setIsCanAction(false);
            } else {
              response.setIsCanAction(true);
            }
          } else {
            response.setIsCanAction(false);
          }
          response.setNowStatus(player.getIdentity());
          if (player.getIdentity() == 0) {
            response.addAllWatchUser(unoRoom.getWatchPlayerIcon());
          } else {
            response.addAllWatchUser(unoRoom.getWatchPlayerIcon()
                .stream()
                .filter(s -> !s.equals(player.getPlayerAvatar()))
                .collect(Collectors.toList()));
          }
          channel.writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.WATCH_INFO,
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
   * TODO 站起坐下. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/19 13:17
   * @update 2020/8/19 13:17
   */
  private void joinLeave(Channel channel, Packet packet) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(unoRoom)) {
        UnoPlayer checkPlayer = unoRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          F30251.F3025110C2S request = F30251.F3025110C2S.parseFrom(packet.bytes);
          F30251.F3025110S2C.Builder response = F30251.F3025110S2C.newBuilder();
          if (unoRoom.getRoomStatus() == 1) {
            response.setResult(1).setStand(request.getIsStand());
            channel.writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.JOIN_LEAVE,
                response.build().toByteArray()));
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
   * TODO 进入观战. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/19 13:19
   * @update 2020/8/19 13:19
   */
  private void enterWatch(Channel channel, Packet packet) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(unoRoom)) {
        F30251.F3025110C2S request = F30251.F3025110C2S.parseFrom(packet.bytes);
        F30251.F3025110S2C.Builder response = F30251.F3025110S2C.newBuilder();
        UnoPlayer player = unoRoom.getPlayerInfo(packet.userId);
        if (player.getPlayerStatus() > 0) {
          response.setResult(1).setStand(request.getIsStand());
          channel.writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.JOIN_LEAVE,
              response.build().toByteArray()));
        } else {
          unoRoom.cancelTimeOut((int) packet.userId);
          int seat = unoRoom.leaveSeat(player.getPlayerId());
          response.setResult(0);
          response.setStand(request.getIsStand());
          response.setSeatNo(seat);
          player = unoRoom.getPlayerInfo(packet.userId);
          F30251.PlayerInfo.Builder playerInfo = F30251.PlayerInfo.newBuilder();
          playerInfo.setNick(player.getPlayerName());
          playerInfo.setUserID(player.getPlayerId());
          playerInfo.setUrl(player.getPlayerAvatar());
          playerInfo.setSex(player.getPlayerSex());
          playerInfo.setDeviPosition(player.getSeatNumber());
          playerInfo.setState(player.getPlayerStatus());
          playerInfo.setCoin(player.getPlayerGold());
          if (StringUtils.isNotEmpty(player.getAvatarFrame())) {
            playerInfo.setIconFrame(player.getAvatarFrame());
          }
          if (StringUtils.isNotEmpty(player.getCardBackSkin())) {
            F30251.GameStyle.Builder gameStyle = F30251.GameStyle.newBuilder();
            gameStyle.setCardBack(player.getCardBackSkin());
            playerInfo.setGameStyle(gameStyle);
          }
          response.setJoinInfo(playerInfo);
          GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_UNO, UnoCmd.JOIN_LEAVE,
              response.build().toByteArray()), unoRoom.getRoomId());
          // 清理检测
          List<UnoPlayer> playerList = unoRoom.getPlayerList();
          int playGameSize = (int) playerList.stream()
              .filter(s -> s.getPlayerId() > 0).count();
          if (playGameSize == 0) {
            clearTimeout(unoRoom.getRoomId());
          }
          // 开放方式
          boolean testRoom = (packet.roomId == UnoAssets.getLong(UnoAssets.TEST_ID));
          if (!testRoom) {
            if (unoRoom.getOpenWay() == 0) {
              if (unoRoom.getOpenWay() == 0) {
                if (unoRoom.remainingSeat() > 0) {
                  MatchRoom matchRoom = new MatchRoom();
                  matchRoom.setRoomId(packet.roomId);
                  matchRoom.setPeopleNum(unoRoom.remainingSeat());
                  MatchManager.refreshUnoPokerMatch(matchRoom);
                } else {
                  MatchManager.delUnoPokerMatch(packet.roomId);
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
   * TODO 进入座位. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/19 13:19
   * @update 2020/8/19 13:19
   */
  private void enterSeat(Channel channel, Packet packet) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(unoRoom)) {
        F30251.F3025110C2S request = F30251.F3025110C2S.parseFrom(packet.bytes);
        F30251.F3025110S2C.Builder response = F30251.F3025110S2C.newBuilder();
        // 剩余座位
        int remaining = unoRoom.remainingSeat();
        if (remaining > 0) {
          UnoPlayer player = unoRoom.getPlayerInfo(packet.userId);
          if (player.getIdentity() != 1 || player.getPlayerGold() < 120) {
            channel.writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.JOIN_LEAVE,
                response.setResult(2).setStand(request.getIsStand()).build().toByteArray()));
          } else {
            // 销毁清理定时
            unoRoom.cancelTimeOut((int) packet.roomId);
            int seat = unoRoom.joinSeat(packet.userId);
            response.setResult(0);
            response.setStand(request.getIsStand());
            response.setSeatNo(seat);
            player = unoRoom.getPlayerInfo(packet.userId);
            F30251.PlayerInfo.Builder playerInfo = F30251.PlayerInfo.newBuilder();
            playerInfo.setNick(player.getPlayerName());
            playerInfo.setUserID(player.getPlayerId());
            playerInfo.setUrl(player.getPlayerAvatar());
            playerInfo.setSex(player.getPlayerSex());
            playerInfo.setDeviPosition(player.getSeatNumber());
            playerInfo.setState(player.getPlayerStatus());
            playerInfo.setCoin(player.getPlayerGold());
            if (StringUtils.isNotEmpty(player.getAvatarFrame())) {
              playerInfo.setIconFrame(player.getAvatarFrame());
            }
            if (StringUtils.isNotEmpty(player.getCardBackSkin())) {
              F30251.GameStyle.Builder gameStyle = F30251.GameStyle.newBuilder();
              gameStyle.setCardBack(player.getCardBackSkin());
              playerInfo.setGameStyle(gameStyle);
            }
            playerInfo.setReadyTime(15);
            response.setJoinInfo(playerInfo);
            GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_UNO, UnoCmd.JOIN_LEAVE,
                response.build().toByteArray()), unoRoom.getRoomId());
            // 添加准备倒计
            readyTimeout(unoRoom.getRoomId(), player.getPlayerId());
            if (unoRoom.getOpenWay() == 0) {
              if (unoRoom.remainingSeat() > 0) {
                MatchRoom matchRoom = new MatchRoom();
                matchRoom.setRoomId(packet.roomId);
                matchRoom.setPeopleNum(unoRoom.remainingSeat());
                MatchManager.refreshUnoPokerMatch(matchRoom);
              } else {
                MatchManager.delUnoPokerMatch(packet.roomId);
              }
            }
          }
        } else {
          channel.writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.JOIN_LEAVE,
              response.setResult(1).setStand(request.getIsStand()).build().toByteArray()));
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 跳过出牌. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/19 11:33
   * @update 2020/8/19 11:33
   */
  private void skipCard(Channel channel, Packet packet) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(unoRoom)) {
        UnoPlayer checkPlayer = unoRoom.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          if (unoRoom.getNowActionId() == packet.userId) {
            unoRoom.initByte();
            unoRoom.cancelTimeOut((int) packet.userId);
            UnoPlayer nowPlayer = unoRoom.getPlayerInfo(packet.userId);
            nowPlayer.setPlayerAction(0);
            nowPlayer.setTouchCards(null);
            unoRoom.setUpActionPlayer(unoRoom.getNowActionId(), unoRoom.getDirection());
            F30251.F3025114S2C.Builder response = F30251.F3025114S2C.newBuilder();
            response.setNextUserId(unoRoom.getNowActionId());
            response.setNextTimes(15);
            UnoPlayer nextPlayer = unoRoom.getPlayerInfo(response.getNextUserId());
            List<UnoPoker> hintPoker = unoRoom.hintPoker(nextPlayer.getPlayerPoker());
            if (CollectionUtils.isNotEmpty(hintPoker)) {
              F30251.CardInfos.Builder hintHandCard;
              for (UnoPoker poker : hintPoker) {
                hintHandCard = F30251.CardInfos.newBuilder();
                hintHandCard.setHandCardID(poker.getPokerId());
                hintHandCard.setHandCardDecor(poker.getPokerColors());
                hintHandCard.setCardFunc(poker.getPokerFunction());
                response.addHintHandCard(hintHandCard);
              }
            }
            if (unoRoom.getUnoPokerDepot().size() > 0) {
              response.setNextGrabCards(true);
            } else {
              response.setNextGrabCards(false);
            }
            GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_UNO, UnoCmd.SKIP_CARD,
                response.build().toByteArray()), unoRoom.getRoomId());
            UnoPlayer indexPlayer = unoRoom.getPlayerInfo(unoRoom.getNowActionId());
            if (nowPlayer.getPlayerPoker().size() == 0) {
              gameFinish(packet.roomId);
            } else if (unoRoom.getUnoPokerDepot().size() == 0) {
              gameFinish(packet.roomId);
            } else if (indexPlayer.getLinkStatus() == 1) {
              // 托管定时
              trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
            } else {
              // 操作定时
              actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
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
   * @create 2020/8/17 10:05
   * @update 2020/8/17 10:05
   */
  private void disconnected(Channel channel, Packet packet) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(unoRoom)) {
        UnoPlayer player = unoRoom.getPlayerInfo(packet.userId);
        if (player.getIdentity() == 1) {
          F30251.F3025118S2C.Builder builder = F30251.F3025118S2C.newBuilder();
          List<UnoPlayer> playerList = unoRoom.getPlayerList();
          if (CollectionUtils.isNotEmpty(playerList)) {
            F30251.PlayerInfo.Builder playerInfo;
            for (UnoPlayer unoPlayer : playerList) {
              playerInfo = F30251.PlayerInfo.newBuilder();
              playerInfo.setNick(unoPlayer.getPlayerName());
              playerInfo.setUserID(unoPlayer.getPlayerId());
              playerInfo.setUrl(unoPlayer.getPlayerAvatar());
              playerInfo.setSex(unoPlayer.getPlayerSex());
              playerInfo.setDeviPosition(unoPlayer.getSeatNumber());
              playerInfo.setState(unoPlayer.getPlayerStatus());
              playerInfo.setCoin(unoPlayer.getPlayerGold());
              if (StringUtils.isNotEmpty(unoPlayer.getAvatarFrame())) {
                playerInfo.setIconFrame(unoPlayer.getAvatarFrame());
              }
              List<UnoPoker> pokerList = unoPlayer.getPlayerPoker();
              if (CollectionUtils.isNotEmpty(pokerList)) {
                int pokerSize = pokerList.size();
                F30251.CardInfos.Builder cardInfo;
                for (int i = 0; i < pokerSize; i++) {
                  cardInfo = F30251.CardInfos.newBuilder();
                  cardInfo.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
                  playerInfo.addHandCard(cardInfo);
                }
              }
              if (StringUtils.isNotEmpty(unoPlayer.getCardBackSkin())) {
                F30251.GameStyle.Builder gameStyle = F30251.GameStyle.newBuilder();
                gameStyle.setCardBack(unoPlayer.getCardBackSkin());
                playerInfo.setGameStyle(gameStyle);
              }
              builder.addSeatPlayer(playerInfo);
            }
          }
          if (unoRoom.getWatchInfo() != null) {
            F30251.F3025116S2C watchInfo = F30251.F3025116S2C.parseFrom(unoRoom.getWatchInfo());
            builder.setWatchInfo(watchInfo);
          }
          if (unoRoom.getDealCard() != null) {
            F30251.F3025117S2C dealCardInfo = F30251.F3025117S2C.parseFrom(unoRoom.getDealCard());
            builder.setDealCard(dealCardInfo);
          }
          channel.writeAndFlush(new Packet(
              ActionCmd.GAME_UNO, UnoCmd.DISCONNECTED_WATCH, builder.build().toByteArray()));
          F30251.F3025115S2C.Builder goldInfo = F30251.F3025115S2C.newBuilder();
          channel.writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAYER_GOLD,
              goldInfo.setPlayGold(player.getPlayerGold()).build().toByteArray()));
        } else {
          F30251.F3025112S2C.Builder builder = F30251.F3025112S2C.newBuilder();
          List<UnoPlayer> playerList = unoRoom.getPlayerList();
          if (CollectionUtils.isNotEmpty(playerList)) {
            F30251.PlayerInfo.Builder playerInfo;
            for (UnoPlayer unoPlayer : playerList) {
              playerInfo = F30251.PlayerInfo.newBuilder();
              playerInfo.setNick(unoPlayer.getPlayerName());
              playerInfo.setUserID(unoPlayer.getPlayerId());
              playerInfo.setUrl(unoPlayer.getPlayerAvatar());
              playerInfo.setSex(unoPlayer.getPlayerSex());
              playerInfo.setDeviPosition(unoPlayer.getSeatNumber());
              playerInfo.setState(unoPlayer.getPlayerStatus());
              playerInfo.setCoin(unoPlayer.getPlayerGold());
              if (StringUtils.isNotEmpty(unoPlayer.getAvatarFrame())) {
                playerInfo.setIconFrame(unoPlayer.getAvatarFrame());
              }
              List<UnoPoker> pokerList = unoPlayer.getPlayerPoker();
              if (CollectionUtils.isNotEmpty(pokerList)) {
                F30251.CardInfos.Builder cardInfo;
                for (UnoPoker unoPoker : pokerList) {
                  cardInfo = F30251.CardInfos.newBuilder();
                  cardInfo.setHandCardID(unoPoker.getPokerId());
                  cardInfo.setHandCardDecor(unoPoker.getPokerColors());
                  cardInfo.setCardFunc(unoPoker.getPokerFunction());
                  playerInfo.addHandCard(cardInfo);
                }
              }
              if (StringUtils.isNotEmpty(unoPlayer.getCardBackSkin())) {
                F30251.GameStyle.Builder gameStyle = F30251.GameStyle.newBuilder();
                gameStyle.setCardBack(unoPlayer.getCardBackSkin());
                playerInfo.setGameStyle(gameStyle);
              }
              if (unoPlayer.getPlayerStatus() == 0) {
                LocalDateTime udt = unoPlayer.getReadyTime().plusSeconds(15L);
                LocalDateTime nds = LocalDateTime.now();
                Duration duration = Duration.between(nds, udt);
                int second = Math.toIntExact(duration.getSeconds());
                playerInfo.setReadyTime(second);
              }
              builder.addBreakLineInfo(playerInfo);
            }
          }
          if (unoRoom.getPlayCard() != null) {
            F30251.F302513S2C request = F30251.F302513S2C.parseFrom(unoRoom.getPlayCard());
            F30251.F302513S2C.Builder playBuilder = F30251.F302513S2C.newBuilder();
            playBuilder.setResult(request.getResult());
            playBuilder.setActionUserId(request.getActionUserId());
            playBuilder.setPlayCardId(request.getPlayCardId());
            if (request.getDirection() > 0) {
              playBuilder.setDirection(request.getDirection());
            }
            if (request.getNowColor() > 0) {
              playBuilder.setNowColor(request.getNowColor());
            }
            int func = request.getPlayCardId().getCardFunc();
            if (func == 1 || func == 2 || func == 3 || func ==4) {
              playBuilder.setCardAbility(request.getCardAbility());
            }
            F30251.F302513S2C.Builder realData = F30251.F302513S2C.newBuilder();
            F30251.F302513S2C.Builder fakeData = F30251.F302513S2C.newBuilder();
            UnoPlayer actionPlayer = unoRoom.getPlayerInfo(request.getActionUserId());
            List<UnoPoker> unoPokers = actionPlayer.getPlayerPoker();
            if (CollectionUtils.isNotEmpty(unoPokers)) {
              List<UnoPoker> playerPoker = unoRoom.pokerSort(unoPokers);
              F30251.CardInfos.Builder lastHandCard;
              F30251.CardInfos.Builder fakeHandCard;
              for (UnoPoker poker : playerPoker) {
                lastHandCard = F30251.CardInfos.newBuilder();
                fakeHandCard = F30251.CardInfos.newBuilder();
                lastHandCard.setHandCardID(poker.getPokerId());
                lastHandCard.setHandCardDecor(poker.getPokerColors());
                lastHandCard.setCardFunc(poker.getPokerFunction());
                fakeHandCard.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
                realData.addLastHandCard(lastHandCard);
                fakeData.addLastHandCard(fakeHandCard);
              }
            }
            if (func == 3) {
              F30251.F302513S2C.Builder realDataTwo = F30251.F302513S2C.newBuilder();
              long target = unoRoom.getPreviousActionUser(request.getNextUser());
              UnoPlayer targetPlayer = unoRoom.getPlayerInfo(target);
              if (target == packet.userId) {
                List<UnoPoker> pokerList = targetPlayer.getPlayerPoker();
                List<UnoPoker> playerPoker = unoRoom.pokerSort(pokerList);
                if (CollectionUtils.isNotEmpty(unoPokers)) {
                  F30251.CardInfos.Builder lastHandCard;
                  for (UnoPoker poker : playerPoker) {
                    lastHandCard = F30251.CardInfos.newBuilder();
                    lastHandCard.setHandCardID(poker.getPokerId());
                    lastHandCard.setHandCardDecor(poker.getPokerColors());
                    lastHandCard.setCardFunc(poker.getPokerFunction());
                    realDataTwo.addLastHandCard(lastHandCard);
                  }
                }
              }
              playBuilder.addAllLastHandCard(realDataTwo.getLastHandCardList());
            } else if (packet.userId == actionPlayer.getPlayerId()) {
              playBuilder.addAllLastHandCard(realData.getLastHandCardList());
            } else {
              playBuilder.addAllLastHandCard(fakeData.getLastHandCardList());
            }
            playBuilder.setNextUser(request.getNextUser());
            playBuilder.setNextTimes(request.getNextTimes());
            playBuilder.addAllHintHandCard(request.getHintHandCardList());
            playBuilder.setIsHintUno(request.getIsHintUno());
            playBuilder.setNextGrabCards(request.getNextGrabCards());
            playBuilder.setCardStock(request.getCardStock());
            builder.setPlayCardInfo(playBuilder);
          } else if (unoRoom.getSelectColor() != null) {
            F30251.F302514S2C request = F30251.F302514S2C.parseFrom(unoRoom.getSelectColor());
            F30251.F302514S2C.Builder selectBuilder = F30251.F302514S2C.newBuilder();
            selectBuilder.setDecor(request.getDecor());
            selectBuilder.setActionUserID(request.getActionUserID());
            if (request.getPlayCardId() != null) {
              selectBuilder.setPlayCardId(request.getPlayCardId());
              selectBuilder.setCardAbility(request.getCardAbility());
            }
            F30251.F302513S2C.Builder realData = F30251.F302513S2C.newBuilder();
            F30251.F302513S2C.Builder fakeData = F30251.F302513S2C.newBuilder();
            UnoPlayer actionPlayer = unoRoom.getPlayerInfo(request.getActionUserID());
            List<UnoPoker> unoPokers = actionPlayer.getPlayerPoker();
            if (CollectionUtils.isNotEmpty(unoPokers)) {
              List<UnoPoker> playerPoker = unoRoom.pokerSort(unoPokers);
              F30251.CardInfos.Builder lastHandCard;
              F30251.CardInfos.Builder fakeHandCard;
              for (UnoPoker poker : playerPoker) {
                lastHandCard = F30251.CardInfos.newBuilder();
                fakeHandCard = F30251.CardInfos.newBuilder();
                lastHandCard.setHandCardID(poker.getPokerId());
                lastHandCard.setHandCardDecor(poker.getPokerColors());
                lastHandCard.setCardFunc(poker.getPokerFunction());
                fakeHandCard.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
                realData.addLastHandCard(lastHandCard);
                fakeData.addLastHandCard(fakeHandCard);
              }
            }
            if (request.getActionUserID() == packet.userId) {
              selectBuilder.addAllLastHandCard(realData.getLastHandCardList());
            } else {
              selectBuilder.addAllLastHandCard(fakeData.getLastHandCardList());
            }
            selectBuilder.setNextUserID(request.getNextUserID());
            selectBuilder.setNextTimes(request.getNextTimes());
            selectBuilder.addAllHintHandCard(request.getHintHandCardList());
            selectBuilder.setIsHintUno(request.getIsHintUno());
            selectBuilder.setNextGrabCards(request.getNextGrabCards());
            selectBuilder.setCardStock(request.getCardStock());
            selectBuilder.setDirection(request.getDirection());
            builder.setChangeColorInfo(selectBuilder);
          } else if (unoRoom.getTouchCard() != null) {
            F30251.F302515S2C request = F30251.F302515S2C.parseFrom(unoRoom.getTouchCard());
            F30251.F302515S2C.Builder touchBuilder = F30251.F302515S2C.newBuilder();
            touchBuilder.setPlayerId(request.getPlayerId());
            touchBuilder.setCardId(request.getCardId());
            touchBuilder.setIsPlay(request.getIsPlay());
            F30251.F302513S2C.Builder realData = F30251.F302513S2C.newBuilder();
            F30251.F302513S2C.Builder fakeData = F30251.F302513S2C.newBuilder();
            UnoPlayer actionPlayer = unoRoom.getPlayerInfo(request.getPlayerId());
            List<UnoPoker> unoPokers = actionPlayer.getPlayerPoker();
            if (CollectionUtils.isNotEmpty(unoPokers)) {
              List<UnoPoker> playerPoker = unoRoom.pokerSort(unoPokers);
              F30251.CardInfos.Builder lastHandCard;
              F30251.CardInfos.Builder fakeHandCard;
              for (UnoPoker poker : playerPoker) {
                lastHandCard = F30251.CardInfos.newBuilder();
                fakeHandCard = F30251.CardInfos.newBuilder();
                lastHandCard.setHandCardID(poker.getPokerId());
                lastHandCard.setHandCardDecor(poker.getPokerColors());
                lastHandCard.setCardFunc(poker.getPokerFunction());
                fakeHandCard.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
                realData.addLastHandCard(lastHandCard);
                fakeData.addLastHandCard(fakeHandCard);
              }
            }
            if (request.getPlayerId() == packet.userId) {
              touchBuilder.addAllLastHandCard(realData.getLastHandCardList());
            } else {
              touchBuilder.addAllLastHandCard(fakeData.getLastHandCardList());
            }
            touchBuilder.setNextUserID(request.getNextUserID());
            touchBuilder.setNextTimes(request.getNextTimes());
            touchBuilder.addAllHintHandCard(request.getHintHandCardList());
            touchBuilder.setNextGrabCards(request.getNextGrabCards());
            touchBuilder.setCardStock(request.getCardStock());
            builder.setMoCardInfo(touchBuilder);
          } else if (unoRoom.getQuestionCards() != null) {
            F30251.F302516S2C request = F30251.F302516S2C.parseFrom(unoRoom.getQuestionCards());
            F30251.F302516S2C.Builder questionBuilder = F30251.F302516S2C.newBuilder();
            questionBuilder.setQueryResult(request.getQueryResult());
            questionBuilder.addAllCardId(request.getCardIdList());
            questionBuilder.setUserId(request.getUserId());
            questionBuilder.setNextUserId(request.getNextUserId());
            questionBuilder.setNextTimes(request.getNextTimes());
            questionBuilder.addAllHintHandCard(request.getHintHandCardList());
            questionBuilder.setNextGrabCards(request.getNextGrabCards());
            F30251.F302513S2C.Builder realData = F30251.F302513S2C.newBuilder();
            F30251.F302513S2C.Builder fakeData = F30251.F302513S2C.newBuilder();
            UnoPlayer actionPlayer = unoRoom.getPlayerInfo(request.getUserId());
            List<UnoPoker> unoPokers = actionPlayer.getPlayerPoker();
            if (CollectionUtils.isNotEmpty(unoPokers)) {
              List<UnoPoker> playerPoker = unoRoom.pokerSort(unoPokers);
              F30251.CardInfos.Builder lastHandCard;
              F30251.CardInfos.Builder fakeHandCard;
              for (UnoPoker poker : playerPoker) {
                lastHandCard = F30251.CardInfos.newBuilder();
                fakeHandCard = F30251.CardInfos.newBuilder();
                lastHandCard.setHandCardID(poker.getPokerId());
                lastHandCard.setHandCardDecor(poker.getPokerColors());
                lastHandCard.setCardFunc(poker.getPokerFunction());
                fakeHandCard.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
                realData.addLastHandCard(lastHandCard);
                fakeData.addLastHandCard(fakeHandCard);
              }
            }
            if (request.getUserId() == packet.userId) {
              questionBuilder.addAllLastHandCard(realData.getLastHandCardList());
            } else {
              questionBuilder.addAllLastHandCard(fakeData.getLastHandCardList());
            }
            questionBuilder.setCardStock(request.getCardStock());
            questionBuilder.setQueryAction(request.getQueryAction());
            questionBuilder.setQueryUsreID(request.getQueryUsreID());
            builder.setZyInfo(questionBuilder);
          }
          channel.writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.DISCONNECTED, builder.build().toByteArray()));
          F30251.F3025115S2C.Builder goldInfo = F30251.F3025115S2C.newBuilder();
          channel.writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAYER_GOLD,
              goldInfo.setPlayGold(player.getPlayerGold()).build().toByteArray()));
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 游戏结束. 
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/19 18:21
   * @update 2020/8/19 18:21
   */
  private void gameFinish(Long roomId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        unoRoom.destroy();
        F30251.F302517S2C.Builder builder = F30251.F302517S2C.newBuilder();
        List<UnoPlayer> playerList = unoRoom.getScoreRanking();
        F30251.EnterPlayerInfo.Builder playerInfo;
        int index = 0;
        int ranking = 1;
        for (UnoPlayer player : playerList) {
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
          // 活动处理 丹枫迎秋
          Map<String, Object> activity = Maps.newHashMap();
          activity.put("userId", player.getPlayerId());
          activity.put("code", ActivityEnum.ACT000103.getCode());
          activity.put("progress", 1);
          this.activityRemote.openHandler(activity);
          playerInfo = F30251.EnterPlayerInfo.newBuilder();
          playerInfo.setIcon(player.getPlayerAvatar());
          playerInfo.setNick(player.getPlayerName());
          playerInfo.setCoin(player.getGameGold());
          if (index ==  0) {
            int exp;
            if (MemManager.isExists(player.getPlayerId())) {
              playerInfo.setIsDouble(1);
              if (player.getLinkStatus() == 0) {
                exp =  gainExperience(player.getPlayerId(), 8, player.getGameGold());
              } else {
                exp = 8;
              }
            } else {
              if (player.getLinkStatus() == 0) {
                exp =  gainExperience(player.getPlayerId(), 4, player.getGameGold());
              } else {
                exp = 4;
              }
            }
            playerInfo.setExpNum(exp);
          } else if (index == 1) {
            int exp;
            if (MemManager.isExists(player.getPlayerId())) {
              playerInfo.setIsDouble(1);
              if (player.getLinkStatus() == 0) {
                exp =  gainExperience(player.getPlayerId(), 4, player.getGameGold());
                if (player.getIsUno() == 1) {
                  // 玩家成就.牌差一着
                  Map<String, Object> taskSuc0035 = Maps.newHashMap();
                  taskSuc0035.put("userId", player.getPlayerId());
                  taskSuc0035.put("code", AchievementEnum.AMT0035.getCode());
                  taskSuc0035.put("progress", 1);
                  taskSuc0035.put("isReset", 0);
                  this.userRemote.achievementHandlers(taskSuc0035);
                }
              } else {
                exp = 4;
              }
            } else {
              if (player.getLinkStatus() == 0) {
                exp =  gainExperience(player.getPlayerId(), 2, player.getGameGold());
                if (player.getIsUno() == 1) {
                  // 玩家成就.牌差一着
                  Map<String, Object> taskSuc0035 = Maps.newHashMap();
                  taskSuc0035.put("userId", player.getPlayerId());
                  taskSuc0035.put("code", AchievementEnum.AMT0035.getCode());
                  taskSuc0035.put("progress", 1);
                  taskSuc0035.put("isReset", 0);
                  this.userRemote.achievementHandlers(taskSuc0035);
                }
              } else {
                exp = 2;
              }
            }
            playerInfo.setExpNum(exp);
          } else {
            if (player.getLinkStatus() == 0) {
              gainExperience(player.getPlayerId(), 0, player.getGameGold());
              if (player.getIsUno() == 1) {
                // 玩家成就.牌差一着
                Map<String, Object> taskSuc0035 = Maps.newHashMap();
                taskSuc0035.put("userId", player.getPlayerId());
                taskSuc0035.put("code", AchievementEnum.AMT0035.getCode());
                taskSuc0035.put("progress", 1);
                taskSuc0035.put("isReset", 0);
                this.userRemote.achievementHandlers(taskSuc0035);
              }
            }
            if (MemManager.isExists(player.getPlayerId())) {
              playerInfo.setIsDouble(1);
            }
            playerInfo.setExpNum(0);
          }
          playerInfo.setPlayGold(player.getPlayerGold());
          playerInfo.setSex(player.getPlayerSex());
          playerInfo.setRanking(ranking);
          builder.addEnterPlayerInfo(playerInfo);
          index++;
          ranking++;
        }
        GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_UNO, UnoCmd.GAME_SETTLE,
            builder.build().toByteArray()), unoRoom.getRoomId());
        F30251.F3025111S2C.Builder clearList = F30251.F3025111S2C.newBuilder();
        clearList.addAllUserID(unoRoom.offlinePlayers());
        clearList.addAllUserID(unoRoom.goldShortage());
        if (clearList.getUserIDList().size() > 0) {
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAYER_HANDLE,
                  clearList.build().toByteArray()), unoRoom.getRoomId());
        }
        // 初始游戏
        unoRoom.initGame();
        if (CollectionUtils.isNotEmpty(unoRoom.seatedPlayers())) {
          List<UnoPlayer> players = unoRoom.getPlayerList();
          players = players.stream()
              .filter(player -> player.getPlayerId() > 0)
              .collect(Collectors.toList());
          players.forEach(player -> {
            F30251.F3025115S2C.Builder goldInfo = F30251.F3025115S2C.newBuilder();
            player.sendPacket(
                new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAYER_GOLD,
                    goldInfo.setPlayGold(player.getPlayerGold()).build().toByteArray())); });
          List<Long> seatedPlayers = unoRoom.seatedPlayers();
          seatedPlayers.forEach(playerId -> initTimeout(roomId, playerId));
          boolean testRoom = (roomId == UnoAssets.getLong(UnoAssets.TEST_ID));
          if (!testRoom) {
            if (unoRoom.getOpenWay() == 0) {
              if (unoRoom.remainingSeat() > 0) {
                MatchRoom matchRoom = new MatchRoom();
                matchRoom.setRoomId(roomId);
                matchRoom.setPeopleNum(unoRoom.remainingSeat());
                MatchManager.refreshUnoPokerMatch(matchRoom);
              } else {
                MatchManager.delUnoPokerMatch(roomId);
              }
            }
          }
        } else {
          if (unoRoom.seatedPlayers().size() == 0 && unoRoom.getWatchList().size() == 0) {
            clearData(roomId);
          } else {
            // 清理检测
            List<UnoPlayer> unoPlayers = unoRoom.getPlayerList();
            int playGameSize = (int) unoPlayers.stream()
                .filter(s -> s.getPlayerId() > 0).count();
            if (playGameSize == 0) {
              clearTimeout(unoRoom.getRoomId());
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
   * TODO 关闭页面. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/20 15:23
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
   * TODO 清除数据. 
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/27 9:53
   * @update 2020/8/27 9:53
   */
  private void clearData(Long roomId) {
    UnoRoom unoRoom = GAME_DATA.get(roomId);
    if (Objects.nonNull(unoRoom)) {
      GAME_DATA.remove(roomId);
      this.gameRemote.deleteRoom(roomId);
      if (this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId)) {
        this.redisUtils.del(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
      }
      MatchManager.delUnoPokerMatch(roomId);
      ChatManager.delChatGroup(roomId);
      GroupManager.delRoomGroup(roomId);
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
   * @create 2020/8/20 9:15
   * @update 2020/8/20 9:15
   */
  private int gainExperience(Long playerId, Integer exp, Integer gold) {
    if (StringUtils.nvl(playerId).length() >= UnoAssets.getInt(UnoAssets.USER_ID)) {
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
          new Packet(ActionCmd.APP_HEART, UnoCmd.LOADING, null), playerId);
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
   * TODO 游玩优诺. 
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/17 17:57
   * @update 2020/8/17 17:57
   */
  private void joinUnoRoom(Packet packet) {
    try {
      if (packet.roomId > UnoAssets.getLong(UnoAssets.TEST_ID)) {
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
   * @create 2020/8/17 18:27
   * @update 2020/8/17 18:27
   */
  private void pullDecorateInfo(Packet packet) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(unoRoom)) {
        UnoPlayer player = unoRoom.getPlayerInfo(packet.userId);
        if (unoRoom.getRoomId() == UnoAssets.getLong(UnoAssets.TEST_ID)) {
          player.setPlayerGold(500);
        } else {
          Result result = this.userRemote.getCurrency(packet.userId);
          if (result != null) {
            Map<String, Object> currencyInfo = JsonUtils.toObjectMap(result.getData());
            if (currencyInfo != null && !currencyInfo.isEmpty()) {
              player.setPlayerGold((Integer) currencyInfo.get("gold"));
            }
          }
        }
        boolean checkTest = (packet.roomId == UnoAssets.getLong(UnoAssets.TEST_ID));
        if (checkTest) {
          player.setAvatarFrame("https://7lestore.oss-cn-hangzhou.aliyuncs.com/file/admin/7ed8d12e9a13491aac07301324d2a562.svga");
        } else {
          Map<String, Object> dressInfo = this.userRemote.getUserFrame(packet.userId);
          if (dressInfo != null && !dressInfo.isEmpty()) {
            player.setAvatarFrame(StringUtils.nvl(dressInfo.get("iconFrame")));
          }
        }
        List gameDecorate = this.orderRemote.gameDecorate(player.getPlayerId(), ActionCmd.GAME_UNO);
        if (CollectionUtils.isNotEmpty(gameDecorate)) {
          List<Map<String, Object>> decorateList = JsonUtils.listMap(gameDecorate);
          if (CollectionUtils.isNotEmpty(decorateList)) {
            for (Map<String, Object> decorate : decorateList) {
              Integer labelCode = (Integer) decorate.get("labelCode");
              if (labelCode != 1000 && labelCode != 2000) {
                player.setCardBackSkin(StringUtils.nvl(decorate.get("adornUrl")));
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
   * TODO 匹配控制.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/3 11:29
   * @update 2020/9/3 11:29
   */
  private void roomOpenOrCancelMatch(Long roomId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        boolean testRoom = (Objects.equals(roomId, UnoAssets.getLong(UnoAssets.TEST_ID)));
        if (!testRoom) {
          if (unoRoom.getOpenWay() == 0) {
            if (unoRoom.remainingSeat() > 0) {
              MatchRoom matchRoom = new MatchRoom();
              matchRoom.setRoomId(roomId);
              matchRoom.setPeopleNum(unoRoom.remainingSeat());
              MatchManager.refreshUnoPokerMatch(matchRoom);
            } else {
              MatchManager.delUnoPokerMatch(roomId);
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
   * @create 2020/9/7 21:05
   * @update 2020/9/7 21:05
   */
  private void playerNotExist(Channel channel) {
    F30251.F30251S2C.Builder response = F30251.F30251S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_UNO, UnoCmd.ENTER_ROOM,
            response.setResult(2).build().toByteArray()));
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
    F30251.F30251S2C.Builder response = F30251.F30251S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_UNO, UnoCmd.ENTER_ROOM,
            response.setResult(1).build().toByteArray()));
  }

  /**
   * TODO 准备定时. 15(s)
   *
   * @param roomId 房间ID
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/17 19:19
   * @update 2020/8/17 19:19
   */
  private void readyTimeout(Long roomId, Long playerId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        if (!unoRoom.getTimeOutMap().containsKey(playerId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> readyExamine(roomId, playerId)
              ), 15, TimeUnit.SECONDS);
          unoRoom.addTimeOut(playerId.intValue(), timeout);
          UnoPlayer player = unoRoom.getPlayerInfo(playerId);
          player.setReadyTime(LocalDateTime.now());
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
   * @param roomId 房间ID
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/20 11:01
   * @update 2020/8/20 11:01
   */
  private void initTimeout(Long roomId, Long playerId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        if (!unoRoom.getTimeOutMap().containsKey(playerId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> readyExamine(roomId, playerId)
              ), 18, TimeUnit.SECONDS);
          unoRoom.addTimeOut(playerId.intValue(), timeout);
          UnoPlayer player = unoRoom.getPlayerInfo(playerId);
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
   * @param roomId 房间ID
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/17 19:39
   * @update 2020/8/17 19:39
   */
  private void readyExamine(Long roomId, Long playerId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        // 准备超时
        unoRoom.removeTimeOut(playerId.intValue());
        UnoPlayer player = unoRoom.getPlayerInfo(playerId);
        int seat = unoRoom.leaveSeat(player.getPlayerId());
        F30251.F3025110S2C.Builder builder = F30251.F3025110S2C.newBuilder();
        builder.setResult(0).setStand(0).setSeatNo(seat);
        player = unoRoom.getPlayerInfo(playerId);
        F30251.PlayerInfo.Builder playerInfo = F30251.PlayerInfo.newBuilder();
        playerInfo.setNick(player.getPlayerName());
        playerInfo.setUserID(player.getPlayerId());
        playerInfo.setUrl(player.getPlayerAvatar());
        playerInfo.setSex(player.getPlayerSex());
        playerInfo.setDeviPosition(player.getSeatNumber());
        playerInfo.setState(player.getPlayerStatus());
        playerInfo.setCoin(player.getPlayerGold());
        if (StringUtils.isNotEmpty(player.getAvatarFrame())) {
          playerInfo.setIconFrame(player.getAvatarFrame());
        }
        if (StringUtils.isNotEmpty(player.getCardBackSkin())) {
          F30251.GameStyle.Builder gameStyle = F30251.GameStyle.newBuilder();
          gameStyle.setCardBack(player.getCardBackSkin());
          playerInfo.setGameStyle(gameStyle);
        }
        builder.setJoinInfo(playerInfo);
        // 进入观战
        GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_UNO, UnoCmd.JOIN_LEAVE,
            builder.build().toByteArray()), unoRoom.getRoomId());
        player = unoRoom.getPlayerInfo(playerId);
        F30251.PlayerInfo.Builder watchInfo = F30251.PlayerInfo.newBuilder();
        watchInfo.setNick(player.getPlayerName());
        watchInfo.setUserID(player.getPlayerId());
        watchInfo.setUrl(player.getPlayerAvatar());
        watchInfo.setSex(player.getPlayerSex());
        watchInfo.setDeviPosition(player.getSeatNumber());
        watchInfo.setState(player.getPlayerStatus());
        watchInfo.setCoin(player.getPlayerGold());
        F30251.F3025190S2C.Builder response = F30251.F3025190S2C.newBuilder();
        response.setNowUser(watchInfo);
        if (unoRoom.getRoomStatus() == 0) {
          if (player.getPlayerStatus() >= 1) {
            response.setIsCanAction(false);
          } else {
            response.setIsCanAction(true);
          }
        } else {
          response.setIsCanAction(false);
        }
        response.setNowStatus(player.getIdentity());
        if (player.getIdentity() == 0) {
          response.addAllWatchUser(unoRoom.getWatchPlayerIcon());
        } else {
          UnoPlayer finalPlayer = player;
          response.addAllWatchUser(unoRoom.getWatchPlayerIcon()
              .stream()
              .filter(s -> !s.equals(finalPlayer.getPlayerAvatar()))
              .collect(Collectors.toList()));
        }
        player.sendPacket(new Packet(ActionCmd.GAME_UNO, UnoCmd.WATCH_INFO, response.build().toByteArray()));
        // 清理检测
        List<UnoPlayer> playerList = unoRoom.getPlayerList();
        int playGameSize = (int) playerList.stream()
            .filter(s -> s.getPlayerId() > 0).count();
        if (playGameSize == 0) {
          clearTimeout(unoRoom.getRoomId());
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
   * @param roomId 房间ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/18 9:14
   * @update 2020/8/18 9:14
   */
  private void startTimeout(Long roomId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        if (!unoRoom.getTimeOutMap().containsKey((int) UnoCmd.START_GAME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> startExamine(roomId)
              ), 3, TimeUnit.SECONDS);
          unoRoom.addTimeOut(UnoCmd.START_GAME, timeout);
          unoRoom.setStartTime(LocalDateTime.now());
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
   * @param roomId 房间ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/18 9:14
   * @update 2020/8/18 9:14
   */
  private void startExamine(Long roomId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        unoRoom.removeTimeOut(UnoCmd.START_GAME);
        int isReady = unoRoom.preparations();
        if (isReady == UnoAssets.getInt(UnoAssets.ROOM_NUM)) {
          // 开始游戏
          unoRoom.startGame();
          unoRoom.setUpActionPlayer(0L, unoRoom.getDirection());
          List<UnoPlayer> playerList = unoRoom.getPlayerList();
          if (CollectionUtils.isNotEmpty(playerList)) {
            F30251.F302512S2C.Builder builder;
            for (UnoPlayer player : playerList) {
              builder = F30251.F302512S2C.newBuilder();
              builder.setNextUsrdId(unoRoom.getNowActionId());
              builder.setDirection(unoRoom.getDirection());
              List<UnoPoker> playerPoker = player.getPlayerPoker();
              if (CollectionUtils.isNotEmpty(playerPoker)) {
                F30251.CardInfos.Builder handCards;
                for (UnoPoker unoPoker : playerPoker) {
                  handCards = F30251.CardInfos.newBuilder();
                  handCards.setHandCardID(unoPoker.getPokerId());
                  handCards.setHandCardDecor(unoPoker.getPokerColors());
                  handCards.setCardFunc(unoPoker.getPokerFunction());
                  builder.addHandCards(handCards);
                }
              }
              UnoPoker onePoker = unoRoom.getLastPoker();
              F30251.CardInfos.Builder tableCards = F30251.CardInfos.newBuilder();
              tableCards.setHandCardID(onePoker.getPokerId());
              tableCards.setHandCardDecor(onePoker.getPokerColors());
              tableCards.setCardFunc(onePoker.getPokerFunction());
              builder.setTableCards(tableCards);
              if (builder.getNextUsrdId() == player.getPlayerId()) {
                List<UnoPoker> hintPoker = unoRoom.hintPoker(playerPoker);
                if (CollectionUtils.isNotEmpty(hintPoker)) {
                  F30251.CardInfos.Builder hintHandCard;
                  for (UnoPoker unoPoker : hintPoker) {
                    hintHandCard = F30251.CardInfos.newBuilder();
                    hintHandCard.setHandCardID(unoPoker.getPokerId());
                    hintHandCard.setHandCardDecor(unoPoker.getPokerColors());
                    hintHandCard.setCardFunc(unoPoker.getPokerFunction());
                    builder.addHintHandCard(hintHandCard);
                  }
                }
              }
              builder.setTimes(15);
              builder.setIsGrabCards(true);
              player.sendPacket(new Packet(ActionCmd.GAME_UNO, UnoCmd.START_GAME,
                  builder.build().toByteArray()));
            }
            F30251.F3025117S2C.Builder watchInfo = F30251.F3025117S2C.newBuilder();
            F30251.PlayCard.Builder playCard;
            for (UnoPlayer unoPlayer : playerList) {
              playCard = F30251.PlayCard.newBuilder();
              if (unoPlayer.getPlayerId() > 0) {
                playCard.setPlayerId(unoPlayer.getPlayerId());
                playCard.setCardNum(unoPlayer.getPlayerPoker().size());
                watchInfo.addPlayLastCard(playCard);
              }
            }
            UnoPoker onePoker = unoRoom.getLastPoker();
            F30251.CardInfos.Builder tableCards = F30251.CardInfos.newBuilder();
            tableCards.setHandCardID(onePoker.getPokerId());
            tableCards.setHandCardDecor(onePoker.getPokerColors());
            tableCards.setCardFunc(onePoker.getPokerFunction());
            watchInfo.setTableCards(tableCards);
            watchInfo.setTimes(15);
            watchInfo.setNextUsrdId(unoRoom.getNowActionId());
            watchInfo.setDirection(unoRoom.getDirection());
            unoRoom.getWatchGroup().writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.DEAL_INFO,
                watchInfo.build().toByteArray()));
            unoRoom.setDealCard(watchInfo.build().toByteArray());
            UnoPlayer checkPlayer = unoRoom.getPlayerInfo(unoRoom.getNowActionId());
            List<UnoPoker> pokerList = unoRoom.hintPoker(checkPlayer.getPlayerPoker());
            if (pokerList.size() == 0) {
              if (checkPlayer.getLinkStatus() == 1) {
                // 托管定时
                trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
              } else {
                // 自动摸牌
                autoTouchCardTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
                // 操作定时
                actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
              }
            } else {
              if (checkPlayer.getLinkStatus() == 1) {
                // 托管定时
                trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
              } else {
                // 操作定时
                actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
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
   * TODO 操作定时. 15(s)
   *
   * @param roomId 房间ID
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/18 11:03
   * @update 2020/8/18 11:03
   */
  private void actionTimeout(Long roomId, Long playerId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        if (!unoRoom.getTimeOutMap().containsKey(playerId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> actionExamine(roomId, playerId)
              ), 15, TimeUnit.SECONDS);
          unoRoom.addTimeOut(playerId.intValue(), timeout);
          unoRoom.setActionTime(LocalDateTime.now());
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
   * @param roomId 房间ID
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/18 11:04
   * @update 2020/8/18 11:04
   */
  private void actionExamine(Long roomId, Long playerId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        unoRoom.removeTimeOut(playerId.intValue());
        // 操作超时
        UnoPlayer player = unoRoom.getPlayerInfo(playerId);
        Integer receiveQuestion = player.getReceiveQuestion();
        Integer playerAction = player.getPlayerAction();
        if (receiveQuestion > 0) {
          player.setReceiveQuestion(0);
          acceptAddCard(roomId, playerId);
        } else {
          if (playerAction == 3 && player.getTouchCards() != null) {
            player.setPlayerAction(0);
            touchCardsExamine(roomId, playerId);
          } else if (playerAction == 2 || playerAction == 1) {
//            player.setPlayerAction(0);
            rightNowSelectColor(roomId, playerId);
          } else {
            player.setPlayerAction(0);
            List<UnoPoker> hintPoker = unoRoom.hintPoker(player.getPlayerPoker());
            if (CollectionUtils.isNotEmpty(hintPoker)) {
              rightNowPlayCard(roomId, playerId);
            } else {
              rightNowTouchCards(roomId, playerId);
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
   * TODO 托管定时. 2(s)
   *
   * @param roomId 房间ID
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/19 14:42
   * @update 2020/8/19 14:42
   */
  private void trusteeshipTimeout(Long roomId, Long playerId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        if (!unoRoom.getTimeOutMap().containsKey(playerId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> trusteeshipAction(roomId, playerId)
              ), 2, TimeUnit.SECONDS);
          unoRoom.addTimeOut(playerId.intValue(), timeout);
          unoRoom.setActionTime(LocalDateTime.now());
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
   * @param roomId 房间ID
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/19 14:45
   * @update 2020/8/19 14:45
   */
  private void trusteeshipAction(Long roomId, Long playerId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        unoRoom.removeTimeOut(playerId.intValue());
        UnoPlayer player = unoRoom.getPlayerInfo(playerId);
        if (player.getReceiveQuestion() > 0) {
          player.setReceiveQuestion(0);
          acceptAddCard(roomId, playerId);
        } else {
          List<UnoPoker> hintPoker = unoRoom.hintPoker(player.getPlayerPoker());
          if (CollectionUtils.isNotEmpty(hintPoker)) {
            rightNowPlayCard(roomId, playerId);
          } else {
            rightNowTouchCards(roomId, playerId);
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }


  /**
   * TODO 立刻出牌. ◕
   *
   * @param roomId 房间ID
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/19 14:45
   * @update 2020/8/19 14:45
   */
  private void rightNowPlayCard(Long roomId, Long playerId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        unoRoom.initByte();
        UnoPlayer player = unoRoom.getPlayerInfo(playerId);
        List<UnoPoker> hintPoker = unoRoom.hintPoker(player.getPlayerPoker());
        UnoPoker lastPoker = unoRoom.getLastPoker();
        List<UnoPoker> universalCard = hintPoker.stream()
            .filter(s -> s.getPokerId() == 13 || s.getPokerId() == 14)
            .collect(Collectors.toList());
        List<UnoPoker> colorCard = hintPoker.stream()
            .filter(s -> s.getPokerColors().equals(unoRoom.getNowColors()))
            .collect(Collectors.toList());
        List<UnoPoker> linkCard = null;
        if (lastPoker.getPokerId() < 13) {
          linkCard = hintPoker.stream()
              .filter(s -> !s.getPokerColors().equals(unoRoom.getNowColors())
                  && s.getPokerId().equals(lastPoker.getPokerId()))
              .collect(Collectors.toList());
        }
        UnoPoker cardInfos = null;
        if (CollectionUtils.isNotEmpty(universalCard)) {
          cardInfos = universalCard.get(0);
        } else if (CollectionUtils.isNotEmpty(colorCard)) {
          colorCard.sort(Comparator.comparing(UnoPoker::getPokerId).reversed());
          cardInfos = colorCard.get(0);
        } else if (CollectionUtils.isNotEmpty(linkCard)) {
          cardInfos = linkCard.get(0);
        }
        if (cardInfos != null) {
          // 牌功能 0-数字 1-禁止 2-倒转 3-加2 4-改变颜色 5-质疑.
          switch (cardInfos.getPokerFunction()) {
            case 1:
              // 打出跳过
              unoRoom.cancelTimeOut(playerId.intValue());
              skipPlayer(roomId, playerId, cardInfos);
              break;
            case 2:
              // 打出换向
              unoRoom.cancelTimeOut(playerId.intValue());
              changeDirection(roomId, playerId, cardInfos);
              break;
            case 3:
              // 打出加牌
              unoRoom.cancelTimeOut(playerId.intValue());
              increaseCard(roomId, playerId, cardInfos);
              break;
            case 4:
              // 打出换色
              changeColor(roomId, playerId, cardInfos);
              break;
            case 5:
              // 打出质疑
              questionCard(roomId, playerId, cardInfos);
              break;
            default:
              // 打出数字
              unoRoom.cancelTimeOut(playerId.intValue());
              digitalCard(roomId, playerId, cardInfos);
              break;
          }
          player.setPlayerAction(0);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 立刻选色. ◕
   *
   * @param roomId 房间ID
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/19 14:47
   * @update 2020/8/19 14:47
   */
  private void rightNowSelectColor(Long roomId, Long playerId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        unoRoom.initByte();
        F30251.F302514S2C.Builder builder = F30251.F302514S2C.newBuilder();
        builder.setActionUserID(playerId);
        int color = ThreadLocalRandom.current().nextInt(4) + 1;
        builder.setDecor(color);
        unoRoom.setNowColors(color);
        UnoPlayer player = unoRoom.getPlayerInfo(playerId);
        // 玩家操作 0-默认 1-选择颜色 2-发动质疑 3-摸牌.
        if (player.getPlayerAction() > 1) {
          if (player.getPlayerPoker().size() == 1) {
            builder.setIsHintUno(true);
          } else {
            builder.setIsHintUno(false);
          }
          UnoPoker unoPoker = unoRoom.getLastPoker();
          F30251.CardInfos.Builder playCardId = F30251.CardInfos.newBuilder();
          playCardId.setHandCardID(unoPoker.getPokerId());
          playCardId.setHandCardDecor(unoPoker.getPokerColors());
          playCardId.setCardFunc(unoPoker.getPokerFunction());
          builder.setPlayCardId(playCardId);
          F30251.CardAbility.Builder ability = F30251.CardAbility.newBuilder();
          long targetId = unoRoom.getNextActionUser(unoRoom.getNowActionId());
          UnoPlayer targetPlayer = unoRoom.getPlayerInfo(targetId);
          int randomColor = ThreadLocalRandom.current().nextInt(4) + 1;
          targetPlayer.setReceiveQuestion(color);
          ability.setCardFunc(2).setAttackedPlayer(targetId).setColor(randomColor);
          builder.setCardAbility(ability);
          // 设置下一个玩家
          unoRoom.setUpActionPlayer(unoRoom.getNowActionId(), unoRoom.getDirection());
          builder.setNextUserID(unoRoom.getNowActionId());
          builder.setNextTimes(15);
          F30251.F302514S2C.Builder realData = F30251.F302514S2C.newBuilder();
          F30251.F302514S2C.Builder fakeData = F30251.F302514S2C.newBuilder();
          List<UnoPoker> unoPokers = player.getPlayerPoker();
          if (CollectionUtils.isNotEmpty(unoPokers)) {
            List<UnoPoker> playerPoker = unoRoom.pokerSort(player.getPlayerPoker());
            player.setPlayerPoker(playerPoker);
            F30251.CardInfos.Builder lastHandCard;
            F30251.CardInfos.Builder fakeHandCard;
            for (UnoPoker poker : playerPoker) {
              lastHandCard = F30251.CardInfos.newBuilder();
              lastHandCard.setHandCardID(poker.getPokerId());
              lastHandCard.setHandCardDecor(poker.getPokerColors());
              lastHandCard.setCardFunc(poker.getPokerFunction());
              realData.addLastHandCard(lastHandCard);
              fakeHandCard = F30251.CardInfos.newBuilder();
              fakeHandCard.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
              fakeData.addLastHandCard(fakeHandCard);
            }
          }
          builder.setDirection(unoRoom.getDirection());
          List<UnoPlayer> playerList = unoRoom.getPlayerList();
          if (CollectionUtils.isNotEmpty(playerList)) {
            for (UnoPlayer unoPlayer : playerList) {
              if (unoPlayer.getPlayerId().equals(playerId)) {
                builder.addAllLastHandCard(realData.getLastHandCardList());
              } else {
                builder.addAllLastHandCard(fakeData.getLastHandCardList());
              }
              unoPlayer.sendPacket(new Packet(ActionCmd.GAME_UNO, UnoCmd.SELECT_COLOR,
                  builder.build().toByteArray()));
              builder.clearLastHandCard();
            }
          }
          unoRoom.setSelectColor(builder.build().toByteArray());
          F30251.F3025116S2C.Builder watchInfo = F30251.F3025116S2C.newBuilder();
          watchInfo.setPlayerAction(1);
          watchInfo.setActionUserId(builder.getActionUserID());
          watchInfo.setPlayCardId(builder.getPlayCardId());
          watchInfo.setDirection(unoRoom.getDirection());
          watchInfo.setNowColor(unoRoom.getNowColors());
          watchInfo.setCardAbility(builder.getCardAbility());
          F30251.PlayCard.Builder playCard;
          for (UnoPlayer unoPlayer : playerList) {
            playCard = F30251.PlayCard.newBuilder();
            if (unoPlayer.getPlayerId() > 0) {
              playCard.setPlayerId(unoPlayer.getPlayerId());
              playCard.setCardNum(unoPlayer.getPlayerPoker().size());
              watchInfo.addPlayLastCard(playCard);
            }
          }
          watchInfo.setNextUser(builder.getNextUserID());
          watchInfo.setNextTimes(builder.getNextTimes());
          watchInfo.setIsHintUno(builder.getIsHintUno());
          unoRoom.getWatchGroup().writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_INFO,
              watchInfo.build().toByteArray()));
          unoRoom.setWatchInfo(watchInfo.build().toByteArray());
        } else {
          unoRoom.setUpActionPlayer(unoRoom.getNowActionId(), unoRoom.getDirection());
          builder.setNextUserID(unoRoom.getNowActionId());
          builder.setNextTimes(15);
          UnoPlayer nextPlayer = unoRoom.getPlayerInfo(builder.getNextUserID());
          List<UnoPoker> hintPoker = unoRoom.hintPoker(nextPlayer.getPlayerPoker());
          if (CollectionUtils.isNotEmpty(hintPoker)) {
            F30251.CardInfos.Builder hintHandCard;
            for (UnoPoker poker : hintPoker) {
              hintHandCard = F30251.CardInfos.newBuilder();
              hintHandCard.setHandCardID(poker.getPokerId());
              hintHandCard.setHandCardDecor(poker.getPokerColors());
              hintHandCard.setCardFunc(poker.getPokerFunction());
              builder.addHintHandCard(hintHandCard);
            }
          }
          if (unoRoom.getUnoPokerDepot().size() > 0) {
            builder.setNextGrabCards(true);
          } else {
            builder.setNextGrabCards(false);
          }
          builder.setDirection(unoRoom.getDirection());
          GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_UNO, UnoCmd.SELECT_COLOR,
              builder.build().toByteArray()), unoRoom.getRoomId());
          unoRoom.setSelectColor(builder.build().toByteArray());
        }
        if (player.getPlayerPoker().size() == 0) {
          player.setPlayerAction(0);
          gameFinish(roomId);
        } else if (unoRoom.getUnoPokerDepot().size() == 0) {
          player.setPlayerAction(0);
          gameFinish(roomId);
        } else {
          UnoPlayer nextPlayer = unoRoom.getPlayerInfo(unoRoom.getNowActionId());
          if (builder.getHintHandCardList().size() == 0 && player.getPlayerAction() <= 1) {
            player.setPlayerAction(0);
            if (nextPlayer.getLinkStatus() == 1) {
              // 托管定时
              trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
            } else {
              // 自动摸牌
              autoTouchCardTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
              // 操作定时
              actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
            }
          } else {
            player.setPlayerAction(0);
            if (nextPlayer.getLinkStatus() == 1) {
              // 托管定时
              trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
            } else {
              // 操作定时
              actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
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
   * TODO 立刻摸牌. ◕
   *
   * @param roomId 房间ID
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/19 14:52
   * @update 2020/8/19 14:52
   */
  private void rightNowTouchCards(Long roomId, Long playerId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        unoRoom.initByte();
        UnoPlayer player = unoRoom.getPlayerInfo(playerId);
        if (player.getLinkStatus() == 0) {
          // 玩家成就.抽卡人生
          Map<String, Object> taskSuc0036 = Maps.newHashMap();
          taskSuc0036.put("userId", playerId);
          taskSuc0036.put("code", AchievementEnum.AMT0036.getCode());
          taskSuc0036.put("progress", 1);
          taskSuc0036.put("isReset", 0);
          this.userRemote.achievementHandlers(taskSuc0036);
        }
        player.setPlayerAction(3);
        F30251.F302515S2C.Builder builder = F30251.F302515S2C.newBuilder();
        builder.setPlayerId(playerId);
        UnoPoker lastPoker = unoRoom.getLastPoker();
        UnoPoker unoPoker = unoRoom.touchCards();
        F30251.CardInfos.Builder touchCard = F30251.CardInfos.newBuilder();
        touchCard.setHandCardID(unoPoker.getPokerId());
        touchCard.setHandCardDecor(unoPoker.getPokerColors());
        touchCard.setCardFunc(unoPoker.getPokerFunction());
        builder.setCardId(touchCard);
        if (unoPoker.getPokerId() == 13 || unoPoker.getPokerId() == 14) {
          // 可以出牌
          builder.setIsPlay(0);
          player.getPlayerPoker().add(unoPoker);
        } else if (unoPoker.getPokerColors().equals(unoRoom.getNowColors())) {
          // 可以出牌
          builder.setIsPlay(0);
          player.getPlayerPoker().add(unoPoker);
        } else if (!unoPoker.getPokerColors().equals(unoRoom.getNowColors())
            && unoPoker.getPokerId().equals(lastPoker.getPokerId())) {
          // 可以出牌
          builder.setIsPlay(0);
          player.getPlayerPoker().add(unoPoker);
        } else {
          // 不可出牌
          unoRoom.cancelTimeOut(playerId.intValue());
          builder.setIsPlay(1);
          player.getPlayerPoker().add(unoPoker);
        }
        if (builder.getIsPlay() == 1) {
          unoRoom.setUpActionPlayer(unoRoom.getNowActionId(), unoRoom.getDirection());
          builder.setNextUserID(unoRoom.getNowActionId());
          builder.setNextTimes(15);
          UnoPlayer nextPlayer = unoRoom.getPlayerInfo(builder.getNextUserID());
          List<UnoPoker> hintPoker = unoRoom.hintPoker(nextPlayer.getPlayerPoker());
          if (CollectionUtils.isNotEmpty(hintPoker)) {
            F30251.CardInfos.Builder hintHandCard;
            for (UnoPoker poker : hintPoker) {
              hintHandCard = F30251.CardInfos.newBuilder();
              hintHandCard.setHandCardID(poker.getPokerId());
              hintHandCard.setHandCardDecor(poker.getPokerColors());
              hintHandCard.setCardFunc(poker.getPokerFunction());
              builder.addHintHandCard(hintHandCard);
            }
          }
          if (unoRoom.getUnoPokerDepot().size() > 0) {
            builder.setNextGrabCards(true);
          } else {
            builder.setNextGrabCards(false);
          }
          player.setPlayerAction(0);
        } else {
          player.setTouchCards(unoPoker);
        }
        F30251.F302515S2C.Builder realData = F30251.F302515S2C.newBuilder();
        F30251.F302515S2C.Builder fakeData = F30251.F302515S2C.newBuilder();
        List<UnoPoker> unoPokers = player.getPlayerPoker();
        if (CollectionUtils.isNotEmpty(unoPokers)) {
          List<UnoPoker> playerPoker = unoRoom.pokerSort(unoPokers);
          player.setPlayerPoker(playerPoker);
          F30251.CardInfos.Builder lastHandCard;
          F30251.CardInfos.Builder fakeHandCard;
          for (UnoPoker poker : playerPoker) {
            lastHandCard = F30251.CardInfos.newBuilder();
            fakeHandCard = F30251.CardInfos.newBuilder();
            lastHandCard.setHandCardID(poker.getPokerId());
            lastHandCard.setHandCardDecor(poker.getPokerColors());
            lastHandCard.setCardFunc(poker.getPokerFunction());
            fakeHandCard.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
            realData.addLastHandCard(lastHandCard);
            fakeData.addLastHandCard(fakeHandCard);
          }
        }
        builder.setCardStock(unoRoom.getUnoPokerDepot().size());
        List<UnoPlayer> playerList = unoRoom.getPlayerList();
        if (CollectionUtils.isNotEmpty(playerList)) {
          for (UnoPlayer unoPlayer : playerList) {
            if (unoPlayer.getLinkStatus() == 0) {
              if (unoPlayer.getPlayerId().equals(playerId)) {
                builder.addAllLastHandCard(realData.getLastHandCardList());
              } else {
                builder.addAllLastHandCard(fakeData.getLastHandCardList());
              }
              unoPlayer.sendPacket(new Packet(ActionCmd.GAME_UNO, UnoCmd.TOUCH_CARDS,
                  builder.build().toByteArray()));
              builder.clearLastHandCard();
            }
          }
        }
        unoRoom.setTouchCard(builder.build().toByteArray());
        F30251.F3025116S2C.Builder watchInfo = F30251.F3025116S2C.newBuilder();
        watchInfo.setPlayerAction(2);
        watchInfo.setActionUserId(builder.getPlayerId());
        watchInfo.setDirection(unoRoom.getDirection());
        watchInfo.setNowColor(unoRoom.getNowColors());
        F30251.PlayCard.Builder playCard;
        for (UnoPlayer unoPlayer : playerList) {
          playCard = F30251.PlayCard.newBuilder();
          if (unoPlayer.getPlayerId() > 0) {
            playCard.setPlayerId(unoPlayer.getPlayerId());
            playCard.setCardNum(unoPlayer.getPlayerPoker().size());
            watchInfo.addPlayLastCard(playCard);
          }
        }
        watchInfo.setNextUser(builder.getNextUserID());
        watchInfo.setNextTimes(builder.getNextTimes());
        unoRoom.getWatchGroup().writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_INFO,
            watchInfo.build().toByteArray()));
        unoRoom.setWatchInfo(watchInfo.build().toByteArray());
        if (player.getPlayerPoker().size() == 0) {
          gameFinish(roomId);
        } else if (unoRoom.getUnoPokerDepot().size() == 0) {
          gameFinish(roomId);
        } else if (builder.getIsPlay() == 1) {
          UnoPlayer checkPlayer = unoRoom.getPlayerInfo(unoRoom.getNowActionId());
          if (builder.getHintHandCardList().size() == 0) {
            if (checkPlayer.getLinkStatus() == 1) {
              // 托管定时
              trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
            } else {
              // 自动摸牌
              autoTouchCardTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
              // 操作定时
              actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
            }
          } else {
            if (checkPlayer.getLinkStatus() == 1) {
              // 托管定时
              trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
            } else {
              // 操作定时
              actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
            }
          }
        } else {
          playCardTimeout(roomId, playerId);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }


  /**
   * TODO 出牌定时. 1(s)
   *
   * @param roomId 房间ID
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/27 12:45
   * @update 2020/8/27 12:45
   */
  private void playCardTimeout(Long roomId, Long playerId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        if (!unoRoom.getTimeOutMap().containsKey(playerId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> touchCardsExamine(roomId, playerId)
              ), 1, TimeUnit.SECONDS);
          unoRoom.addTimeOut(playerId.intValue(), timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 摸牌校验. ◕
   *
   * @param roomId 房间ID
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/19 15:12
   * @update 2020/8/19 15:12
   */
  private void touchCardsExamine(Long roomId, Long playerId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        unoRoom.removeTimeOut(playerId.intValue());
        unoRoom.initByte();
        UnoPlayer nowPlayer = unoRoom.getPlayerInfo(playerId);
        UnoPoker unoPoker = nowPlayer.getTouchCards();
        // 牌功能 0-数字 1-禁止 2-倒转 3-加2 4-改变颜色 5-质疑.
        switch (unoPoker.getPokerFunction()) {
          case 1:
            // 打出跳过
            skipPlayer(roomId, playerId, unoPoker);
            break;
          case 2:
            // 打出换向
            changeDirection(roomId, playerId, unoPoker);
            break;
          case 3:
            // 打出加牌
            increaseCard(roomId, playerId, unoPoker);
            break;
          case 4:
            // 打出换色
            changeColor(roomId, playerId, unoPoker);
            break;
          case 5:
            // 打出质疑
            questionCard(roomId, playerId, unoPoker);
            break;
          default:
            // 打出数字
            digitalCard(roomId, playerId, unoPoker);
            break;
        }
        nowPlayer.setPlayerAction(0);
        nowPlayer.setTouchCards(null);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 接受加牌. ◕
   *
   * @param roomId 房间ID
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/19 15:01
   * @update 2020/8/19 15:01
   */
  private void acceptAddCard(Long roomId, Long playerId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        unoRoom.initByte();
        F30251.F302516S2C.Builder response = F30251.F302516S2C.newBuilder();
        response.setQueryAction(2);
        response.setQueryUsreID(playerId);
        UnoPlayer nowPlayer = unoRoom.getPlayerInfo(playerId);
        List<UnoPoker> newPokers = Lists.newLinkedList();
        while (newPokers.size() < 4) {
          if (CollectionUtils.isNotEmpty(unoRoom.getUnoPokerDepot())) {
            newPokers.add(unoRoom.getUnoPokerDepot().remove(0));
          } else {
            break;
          }
        }
        nowPlayer.getPlayerPoker().addAll(newPokers);
        response.setUserId(nowPlayer.getPlayerId());
        unoRoom.setUpActionPlayer(unoRoom.getNowActionId(), unoRoom.getDirection());
        response.setNextUserId(unoRoom.getNowActionId());
        response.setNextTimes(15);
        UnoPlayer nextPlayer = unoRoom.getPlayerInfo(response.getNextUserId());
        List<UnoPoker> hintPoker = unoRoom.hintPoker(nextPlayer.getPlayerPoker());
        if (CollectionUtils.isNotEmpty(hintPoker)) {
          F30251.CardInfos.Builder hintHandCard;
          for (UnoPoker poker : hintPoker) {
            hintHandCard = F30251.CardInfos.newBuilder();
            hintHandCard.setHandCardID(poker.getPokerId());
            hintHandCard.setHandCardDecor(poker.getPokerColors());
            hintHandCard.setCardFunc(poker.getPokerFunction());
            response.addHintHandCard(hintHandCard);
          }
        }
        if (unoRoom.getUnoPokerDepot().size() > 0) {
          response.setNextGrabCards(true);
        } else {
          response.setNextGrabCards(false);
        }
        F30251.F302516S2C.Builder realData = F30251.F302516S2C.newBuilder();
        F30251.F302516S2C.Builder fakeData = F30251.F302516S2C.newBuilder();
        List<UnoPoker> unoPokers = nowPlayer.getPlayerPoker();
        if (CollectionUtils.isNotEmpty(unoPokers)) {
          List<UnoPoker> playerPoker = unoRoom.pokerSort(unoPokers);
          nowPlayer.setPlayerPoker(playerPoker);
          F30251.CardInfos.Builder lastHandCard;
          F30251.CardInfos.Builder fakeHandCard;
          for (UnoPoker poker : playerPoker) {
            lastHandCard = F30251.CardInfos.newBuilder();
            fakeHandCard = F30251.CardInfos.newBuilder();
            lastHandCard.setHandCardID(poker.getPokerId());
            lastHandCard.setHandCardDecor(poker.getPokerColors());
            lastHandCard.setCardFunc(poker.getPokerFunction());
            fakeHandCard.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
            realData.addLastHandCard(lastHandCard);
            fakeData.addLastHandCard(fakeHandCard);
          }
        }
        response.setQueryResult(0);
        response.setCardStock(unoRoom.getUnoPokerDepot().size());
        List<UnoPlayer> playerList = unoRoom.getPlayerList();
        if (CollectionUtils.isNotEmpty(playerList)) {
          for (UnoPlayer unoPlayer : playerList) {
            if (unoPlayer.getLinkStatus() == 0) {
              if (unoPlayer.getPlayerId() == response.getUserId()) {
                response.addAllLastHandCard(realData.getLastHandCardList());
              } else {
                response.addAllLastHandCard(fakeData.getLastHandCardList());
              }
              unoPlayer.sendPacket(new Packet(ActionCmd.GAME_UNO, UnoCmd.QUESTION_CARDS,
                  response.build().toByteArray()));
              response.clearLastHandCard();
            }
          }
        }
        unoRoom.setQuestionCards(response.build().toByteArray());
        F30251.F3025116S2C.Builder watchInfo = F30251.F3025116S2C.newBuilder();
        watchInfo.setPlayerAction(3);
        watchInfo.setActionUserId(playerId);
        watchInfo.setDirection(unoRoom.getDirection());
        watchInfo.setNowColor(unoRoom.getNowColors());
        F30251.PlayCard.Builder playCard;
        for (UnoPlayer unoPlayer : playerList) {
          playCard = F30251.PlayCard.newBuilder();
          if (unoPlayer.getPlayerId() > 0) {
            playCard.setPlayerId(unoPlayer.getPlayerId());
            playCard.setCardNum(unoPlayer.getPlayerPoker().size());
            watchInfo.addPlayLastCard(playCard);
          }
        }
        watchInfo.setNextUser(response.getNextUserId());
        watchInfo.setNextTimes(response.getNextTimes());
        unoRoom.getWatchGroup().writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_INFO,
            watchInfo.build().toByteArray()));
        unoRoom.setWatchInfo(watchInfo.build().toByteArray());
        nowPlayer.setReceiveQuestion(0);
        UnoPlayer checkPlayer = unoRoom.getPlayerInfo(unoRoom.getNowActionId());
        if (nowPlayer.getPlayerPoker().size() == 0) {
          gameFinish(roomId);
        } else if (unoRoom.getUnoPokerDepot().size() == 0) {
          gameFinish(roomId);
        } else if (response.getHintHandCardList().size() == 0) {
          if (checkPlayer.getLinkStatus() == 1) {
            // 托管定时
            trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          } else {
            // 自动摸牌
            autoTouchCardTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
            // 操作定时
            actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          }
        } else {
          if (checkPlayer.getLinkStatus() == 1) {
            // 托管定时
            trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          } else {
            // 操作定时
            actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 自动出牌. 1(s)
   *
   * @param roomId 房间ID
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/28 14:58
   * @update 2020/8/28 14:58
   */
  private void autoTouchCardTimeout(Long roomId, Long playerId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        if (!unoRoom.getTimeOutMap().containsKey(playerId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> autoTouchCardExamine(roomId, playerId)
              ), 1, TimeUnit.SECONDS);
          unoRoom.addTimeOut(UnoCmd.TOUCH_CARDS, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 自动校验. ◕
   *
   * @param roomId 房间ID
   * @param playerId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/28 14:59
   * @update 2020/8/28 14:59
   */
  private void autoTouchCardExamine(Long roomId, Long playerId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        unoRoom.removeTimeOut(UnoCmd.TOUCH_CARDS);
        if (unoRoom.getNowActionId().equals(playerId)) {
          unoRoom.initByte();
          F30251.F302515S2C.Builder builder = F30251.F302515S2C.newBuilder();
          builder.setPlayerId(playerId);
          UnoPlayer player = unoRoom.getPlayerInfo(playerId);
          if (player.getLinkStatus() == 0) {
            // 玩家成就.抽卡人生
            Map<String, Object> taskSuc0036 = Maps.newHashMap();
            taskSuc0036.put("userId", playerId);
            taskSuc0036.put("code", AchievementEnum.AMT0036.getCode());
            taskSuc0036.put("progress", 1);
            taskSuc0036.put("isReset", 0);
            this.userRemote.achievementHandlers(taskSuc0036);
          }
          player.setPlayerAction(3);
          UnoPoker lastPoker = unoRoom.getLastPoker();
          UnoPoker unoPoker = unoRoom.touchCards();
          if (unoPoker.getPokerId() == 13 || unoPoker.getPokerId() == 14) {
            // 可以出牌
            builder.setIsPlay(0);
            player.getPlayerPoker().add(unoPoker);
          } else if (unoPoker.getPokerColors().equals(unoRoom.getNowColors())) {
            // 可以出牌
            builder.setIsPlay(0);
            player.getPlayerPoker().add(unoPoker);
          } else if (!unoPoker.getPokerColors().equals(lastPoker.getPokerColors())
              && unoPoker.getPokerId().equals(lastPoker.getPokerId())) {
            // 可以出牌
            builder.setIsPlay(0);
            player.getPlayerPoker().add(unoPoker);
          } else {
            // 不可出牌
            unoRoom.cancelTimeOut(playerId.intValue());
            builder.setIsPlay(1);
            player.getPlayerPoker().add(unoPoker);
          }
          F30251.CardInfos.Builder touchCard = F30251.CardInfos.newBuilder();
          touchCard.setHandCardID(unoPoker.getPokerId());
          touchCard.setHandCardDecor(unoPoker.getPokerColors());
          touchCard.setCardFunc(unoPoker.getPokerFunction());
          builder.setCardId(touchCard);
          if (builder.getIsPlay() == 1) {
            unoRoom.setUpActionPlayer(unoRoom.getNowActionId(), unoRoom.getDirection());
            builder.setNextUserID(unoRoom.getNowActionId());
            builder.setNextTimes(15);
            UnoPlayer nextPlayer = unoRoom.getPlayerInfo(builder.getNextUserID());
            List<UnoPoker> hintPoker = unoRoom.hintPoker(nextPlayer.getPlayerPoker());
            if (CollectionUtils.isNotEmpty(hintPoker)) {
              F30251.CardInfos.Builder hintHandCard;
              for (UnoPoker poker : hintPoker) {
                hintHandCard = F30251.CardInfos.newBuilder();
                hintHandCard.setHandCardID(poker.getPokerId());
                hintHandCard.setHandCardDecor(poker.getPokerColors());
                hintHandCard.setCardFunc(poker.getPokerFunction());
                builder.addHintHandCard(hintHandCard);
              }
            }
            if (unoRoom.getUnoPokerDepot().size() > 0) {
              builder.setNextGrabCards(true);
            } else {
              builder.setNextGrabCards(false);
            }
          } else {
            LocalDateTime udt = unoRoom.getActionTime().plusSeconds(15L);
            LocalDateTime nds = LocalDateTime.now();
            Duration duration = Duration.between(nds, udt);
            int second = Math.toIntExact(duration.getSeconds());
            builder.setNextTimes(second);
            player.setTouchCards(unoPoker);
          }
          F30251.F302515S2C.Builder realData = F30251.F302515S2C.newBuilder();
          F30251.F302515S2C.Builder fakeData = F30251.F302515S2C.newBuilder();
          List<UnoPoker> unoPokers = player.getPlayerPoker();
          if (CollectionUtils.isNotEmpty(unoPokers)) {
            List<UnoPoker> playerPoker = unoRoom.pokerSort(unoPokers);
            player.setPlayerPoker(playerPoker);
            F30251.CardInfos.Builder lastHandCard;
            F30251.CardInfos.Builder fakeHandCard;
            for (UnoPoker poker : playerPoker) {
              lastHandCard = F30251.CardInfos.newBuilder();
              fakeHandCard = F30251.CardInfos.newBuilder();
              lastHandCard.setHandCardID(poker.getPokerId());
              lastHandCard.setHandCardDecor(poker.getPokerColors());
              lastHandCard.setCardFunc(poker.getPokerFunction());
              fakeHandCard.setHandCardID(-1).setHandCardDecor(0).setCardFunc(0);
              realData.addLastHandCard(lastHandCard);
              fakeData.addLastHandCard(fakeHandCard);
            }
          }
          builder.setCardStock(unoRoom.getUnoPokerDepot().size());
          List<UnoPlayer> playerList = unoRoom.getPlayerList();
          if (CollectionUtils.isNotEmpty(playerList)) {
            for (UnoPlayer unoPlayer : playerList) {
              if (unoPlayer.getLinkStatus() == 0) {
                if (unoPlayer.getPlayerId().equals(playerId)) {
                  builder.addAllLastHandCard(realData.getLastHandCardList());
                } else {
                  builder.addAllLastHandCard(fakeData.getLastHandCardList());
                }
                unoPlayer.sendPacket(new Packet(ActionCmd.GAME_UNO, UnoCmd.TOUCH_CARDS,
                    builder.build().toByteArray()));
                builder.clearLastHandCard();
              }
            }
          }
          unoRoom.setTouchCard(builder.build().toByteArray());
          F30251.F3025116S2C.Builder watchInfo = F30251.F3025116S2C.newBuilder();
          watchInfo.setPlayerAction(2);
          watchInfo.setActionUserId(builder.getPlayerId());
          watchInfo.setDirection(unoRoom.getDirection());
          watchInfo.setNowColor(unoRoom.getNowColors());
          F30251.PlayCard.Builder playCard;
          for (UnoPlayer unoPlayer : playerList) {
            playCard = F30251.PlayCard.newBuilder();
            if (unoPlayer.getPlayerId() > 0) {
              playCard.setPlayerId(unoPlayer.getPlayerId());
              playCard.setCardNum(unoPlayer.getPlayerPoker().size());
              watchInfo.addPlayLastCard(playCard);
            }
          }
          watchInfo.setNextUser(builder.getNextUserID());
          watchInfo.setNextTimes(builder.getNextTimes());
          unoRoom.getWatchGroup().writeAndFlush(new Packet(ActionCmd.GAME_UNO, UnoCmd.PLAY_INFO,
              watchInfo.build().toByteArray()));
          unoRoom.setWatchInfo(watchInfo.build().toByteArray());
          player.setPlayerAction(0);
          if (player.getPlayerPoker().size() == 0) {
            gameFinish(roomId);
          } else if (unoRoom.getUnoPokerDepot().size() == 0) {
            gameFinish(roomId);
          } else if (builder.getIsPlay() == 1) {
            UnoPlayer checkPlayer = unoRoom.getPlayerInfo(unoRoom.getNowActionId());
            if (builder.getHintHandCardList().size() == 0) {
              if (checkPlayer.getLinkStatus() == 1) {
                // 托管定时
                trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
              } else {
                // 自动摸牌
                autoTouchCardTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
                // 操作定时
                actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
              }
            } else {
              if (checkPlayer.getLinkStatus() == 1) {
                // 自动摸牌
                trusteeshipTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
              } else {
                // 操作定时
                actionTimeout(unoRoom.getRoomId(), unoRoom.getNowActionId());
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
   * TODO 清理定时. 10(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/19 13:31
   * @update 2020/10/19 13:31
   */
  private void clearTimeout(Long roomId) {
    try {
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        if (!unoRoom.getTimeOutMap().containsKey(roomId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> clearExamine(roomId)
              ), 10, TimeUnit.SECONDS);
          unoRoom.addTimeOut(roomId.intValue(), timeout);
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
      UnoRoom unoRoom = GAME_DATA.get(roomId);
      if (Objects.nonNull(unoRoom)) {
        unoRoom.removeTimeOut(roomId.intValue());
        List<UnoPlayer> playerList = unoRoom.getPlayerList();
        int playGameSize = (int) playerList.stream()
            .filter(player -> player.getPlayerId() > 0).count();
        LoggerManager.info("<== 30251 [一起优诺.清除检测] DELETE: [{}] PLAY: [{}]", roomId, playGameSize);
        if (playGameSize == 0) {
          List<UnoPlayer> watchList = unoRoom.getWatchList();
          if (watchList.size() > 0) {
            F20000.F200007S2C.Builder builder = F20000.F200007S2C.newBuilder();
            builder.setMsg("(oﾟvﾟ)ノ 房间已解散!");
            watchList.forEach(unoPlayer -> {
              SoftChannel.sendPacketToUserId(
                  new Packet(ActionCmd.APP_HEART, (short) 7,
                      builder.build().toByteArray()), unoPlayer.getPlayerId());
              this.redisUtils
                  .del(GameKey.KEY_GAME_JOIN_RECORD.getName() + unoPlayer.getPlayerId());
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
