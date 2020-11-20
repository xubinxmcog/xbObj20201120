package com.enuos.live.handle.game.f30051.dddd;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.channel.SoftChannel;
import com.enuos.live.codec.Packet;
import com.enuos.live.constants.GameRedisKey;
import com.enuos.live.manager.ChatManager;
import com.enuos.live.manager.GroupManager;
import com.enuos.live.manager.LoggerManager;
import com.enuos.live.manager.MatchManager;
import com.enuos.live.pojo.MatchRoom;
import com.enuos.live.proto.f10001msg.F10001;
import com.enuos.live.proto.f20000msg.F20000;
import com.enuos.live.proto.f30051msg.F30051;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.rest.GameRemote;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.task.TimerEventLoop;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.utils.JsonUtils;
import com.enuos.live.utils.RedisUtils;
import com.enuos.live.utils.StringUtils;
import com.enuos.live.utils.annotation.AbstractActionHandler;
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
 * @version v1.0.0
 * @since 2020/11/4 10:08
 */

@Component
//@AbstractAction(cmd = ActionCmd.GAME_FIND_UNDERCOVER)
public class FindUndercover extends AbstractActionHandler {

  /**
   * 房间游戏数据.
   */
  private static ConcurrentHashMap<Long, FindUndercoverRoom> GAME_DATA = new ConcurrentHashMap<>();

  /**
   * 大部分敏感词汇在10个以内，直接返回缓存的字符串.
   */
  private static final String[] STAR_ARR = {"*", "**", "***", "****", "*****", "******", "*******",
      "********", "*********", "**********"};

  @Resource
  private GameRemote gameRemote;
  @Resource
  private UserRemote userRemote;
  @Resource
  private RedisUtils redisUtils;

  /**
   * TODO 操作分发.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 10:27
   * @update 2020/11/4 10:27
   */
  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      switch (packet.child) {
        case FindUndercoverCmd.ENTER_ROOM:
          enterRoom(channel, packet);
          break;
        case FindUndercoverCmd.PLAYER_READY:
          playerReady(channel, packet);
          break;
        case FindUndercoverCmd.CHANGE_WORDS:
          changeWords(channel, packet);
          break;
        case FindUndercoverCmd.CHANGE_ACTION:
          changeAction(channel, packet);
          break;
        case FindUndercoverCmd.WORDS_DESCRIPTION:
          wordsDescription(channel, packet);
          break;
        case FindUndercoverCmd.VOICE_DESCRIPTION:
          voiceDescription(channel, packet);
          break;
        case FindUndercoverCmd.PLAYER_VOTE:
          playerVote(channel, packet);
          break;
        case FindUndercoverCmd.OPEN_WORDS:
          openWords(channel, packet);
          break;
        case FindUndercoverCmd.SPY_OPEN_WORDS:
          spyOpenWords(channel, packet);
          break;
        case FindUndercoverCmd.PLAYER_EXIT:
          playerExit(channel, packet);
          break;
        case FindUndercoverCmd.WATCH_INFO:
          watchInfo(channel, packet);
          break;
        case FindUndercoverCmd.JOIN_LEAVE:
          joinLeave(channel, packet);
          break;
        case FindUndercoverCmd.SELECT_SEAT:
          selectSeat(channel, packet);
          break;
        case FindUndercoverCmd.PLAYER_CHAT:
          playerChat(channel, packet);
          break;
        case FindUndercoverCmd.FINISH_SPEAK:
          finishSpeak(channel, packet);
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

  @Override
  public void shutOff(Long userId, Long attachId) {

  }

  @Override
  public void cleaning(Long roomId) {
    FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
    if (Objects.nonNull(roomInfo)) {
      roomInfo.destroy();
      GAME_DATA.remove(roomId);
      this.gameRemote.deleteRoom(roomId);
      ChatManager.delChatGroup(roomId);
      GroupManager.delRoomGroup(roomId);
    }
  }

  @Override
  public void joinRobot(Long roomId, List<Long> playerIds) {
    // MATCH-EMPTY-METHOD
  }

  /**
   * TODO 进入房间.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 12:42
   * @update 2020/11/6 12:42
   */
  private void enterRoom(Channel channel, Packet packet) {
    try {
      boolean isTestUser = this.redisUtils.hasKey(GameRedisKey.KEY_GAME_TEST_LOGIN + packet.userId);
      boolean isPlayer = this.redisUtils.hasKey(GameRedisKey.KEY_GAME_USER_LOGIN + packet.userId);
      if (isTestUser || isPlayer) {
        boolean isRoom = this.redisUtils.hasKey(GameRedisKey.KEY_GAME_ROOM_RECORD + packet.roomId);
        boolean isTest = (packet.roomId == UndercoverStatic.getLong(UndercoverStatic.TEST_ID));
        if (isRoom || isTest) {
          FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
          if (Objects.nonNull(roomInfo)) {
            FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
            // 0-未开始 1-已开始
            if (roomInfo.getRoomStatus() == 1) {
              if (Objects.nonNull(playerInfo)) {
                if (Objects.isNull(playerInfo.getChannel())) {
                  playerNotExist(channel);
                } else {
                  playerInfo.setChannel(channel);
                  refreshChannel(channel, packet);
                  disconnected(channel, packet);
                  return;
                }
              } else {
                refreshChannel(channel, packet);
                enterSpectator(channel, packet, isTestUser);
              }
            } else {
              roomInfo.cancelTimeOut((int) packet.roomId);
              roomInfo.cancelTimeOut((int) FindUndercoverCmd.START_GAME_TIME);
              if (Objects.nonNull(playerInfo)) {
                playerInfo.setChannel(channel);
                refreshChannel(channel, packet);
              } else {
                refreshChannel(channel, packet);
                enterSeat(channel, packet, isTestUser);
              }
            }
          } else {
            register(TimerEventLoop.timeGroup.next());
            createRoom(packet.roomId);
            refreshChannel(channel, packet);
            enterSeat(channel, packet, isTestUser);
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
   * TODO 创建房间.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 12:41
   * @update 2020/11/6 12:41
   */
  private void createRoom(Long roomId) {
    try {
      boolean checkTest = (roomId == UndercoverStatic.getLong(UndercoverStatic.TEST_ID));
      FindUndercoverRoom roomInfo;
      if (checkTest) {
        GAME_DATA.computeIfAbsent(roomId, key -> new FindUndercoverRoom(roomId, 0));
        roomInfo = GAME_DATA.get(roomId);
        roomInfo.setRoomMatch(1);
        roomInfo.setSpeakMode(0);
      } else {
        byte[] roomByte = this.redisUtils.getByte(GameRedisKey.KEY_GAME_ROOM_RECORD + roomId);
        I10001.RoomRecord roomRecord = I10001.RoomRecord.parseFrom(roomByte);
        Integer roomType = roomRecord.getRoomType();
        GAME_DATA.computeIfAbsent(roomId, key -> new FindUndercoverRoom(roomId, roomType));
        roomInfo = GAME_DATA.get(roomId);
        roomInfo.setRoomMatch(roomRecord.getOpenWay());
        roomInfo.setSpeakMode(roomRecord.getSpeakMode());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 刷新管道.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/24 18:29
   * @update 2020/8/24 18:29
   */
  private void refreshChannel(Channel channel, Packet packet) {
    ChatManager.refreshChatGroup(packet.roomId, channel);
    GroupManager.refreshRoomData(channel, packet);
  }

  /**
   * TODO 进入座位.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @param isTestUser [玩家判断]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 14:39
   * @update 2020/11/6 14:39
   */
  private void enterSeat(Channel channel, Packet packet, boolean isTestUser) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        byte[] bytes;
        if (isTestUser) {
          bytes = this.redisUtils.getByte(GameRedisKey.KEY_GAME_TEST_LOGIN + packet.userId);
        } else {
          bytes = this.redisUtils.getByte(GameRedisKey.KEY_GAME_USER_LOGIN + packet.userId);
        }
        I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
        roomInfo.enterSeat(channel, playerInfo);
        pullDecorateInfo(packet);
        joinFindUndercoverRoom(packet);
        refreshRoomMatching(packet.roomId);
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
   * @param isTestUser [玩家判断]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 14:39
   * @update 2020/11/6 14:39
   */
  private void enterSpectator(Channel channel, Packet packet, boolean isTestUser) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        byte[] bytes;
        if (isTestUser) {
          bytes = this.redisUtils.getByte(GameRedisKey.KEY_GAME_TEST_LOGIN + packet.userId);
        } else {
          bytes = this.redisUtils.getByte(GameRedisKey.KEY_GAME_USER_LOGIN + packet.userId);
        }
        I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
        roomInfo.enterSpectator(channel, playerInfo);
        pullDecorateInfo(packet);
        joinFindUndercoverRoom(packet);
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
   * @create 2020/11/6 14:44
   * @update 2020/11/6 14:44
   */
  private void pushPlayerInfo(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        F30051.F30051S2C.Builder builder = F30051.F30051S2C.newBuilder();
        FindUndercoverPlayer nowPlayer = roomInfo.getPlayerInfo(packet.userId);
        // 玩家身份 0-玩家 1-观众
        if (nowPlayer.getIdentity() == 0) {
          List<FindUndercoverPlayer> playerList = roomInfo.getPlayerList();
          if (CollectionUtils.isNotEmpty(playerList)) {
            F30051.PlayerInfo.Builder playerInfo;
            for (FindUndercoverPlayer undercoverPlayer : playerList) {
              playerInfo = F30051.PlayerInfo.newBuilder();
              playerInfo.setNick(undercoverPlayer.getPlayerName());
              playerInfo.setUserID(undercoverPlayer.getPlayerId());
              playerInfo.setUrl(undercoverPlayer.getAvatarIcon());
              playerInfo.setSex(undercoverPlayer.getPlayerSex());
              playerInfo.setDeviPosition(undercoverPlayer.getSeatNumber());
              playerInfo.setState(undercoverPlayer.getPlayerStatus());
              if (StringUtils.isNotEmpty(undercoverPlayer.getAvatarFrame())) {
                playerInfo.setIconFrame(undercoverPlayer.getAvatarFrame());
              }
              // 用户状态 0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开
              if (undercoverPlayer.getPlayerStatus() == 0) {
                if (Objects.nonNull(undercoverPlayer.getReadinessTime())) {
                  LocalDateTime udt = undercoverPlayer.getReadinessTime().plusSeconds(15L);
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
              new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.ENTER_ROOM,
                  builder.build().toByteArray()), roomInfo.getRoomId());
          // 用户状态 0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开
          if (nowPlayer.getPlayerStatus() == 0) {
            readyTimeout(packet.roomId, packet.userId);
          }
          sendMessage(0, packet.roomId, nowPlayer.getPlayerName(), null);
          readyMessage(packet.roomId);
        } else {
          pushRoomInfo(channel, packet);
        }
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
   * @create 2020/11/6 14:44
   * @update 2020/11/6 14:44
   */
  private void pushRoomInfo(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        F30051.F30051S2C.Builder builder = F30051.F30051S2C.newBuilder();
        List<FindUndercoverPlayer> playerList = roomInfo.getPlayerList();
        if (CollectionUtils.isNotEmpty(playerList)) {
          F30051.PlayerInfo.Builder playerInfo;
          for (FindUndercoverPlayer undercoverPlayer : playerList) {
            playerInfo = F30051.PlayerInfo.newBuilder();
            playerInfo.setNick(undercoverPlayer.getPlayerName());
            playerInfo.setUserID(undercoverPlayer.getPlayerId());
            playerInfo.setUrl(undercoverPlayer.getAvatarIcon());
            playerInfo.setSex(undercoverPlayer.getPlayerSex());
            playerInfo.setDeviPosition(undercoverPlayer.getSeatNumber());
            playerInfo.setState(undercoverPlayer.getPlayerStatus());
            if (StringUtils.isNotEmpty(undercoverPlayer.getAvatarFrame())) {
              playerInfo.setIconFrame(undercoverPlayer.getAvatarFrame());
            }
            builder.addSeatPlayer(playerInfo);
          }
        }
        builder.setResult(0);
        builder.setNumberMode(roomInfo.getRoomType());
        builder.setSpeakMode(roomInfo.getSpeakMode());
        channel.writeAndFlush(
            new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.ENTER_ROOM,
                builder.build().toByteArray()));
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
   * @create 2020/11/6 14:14
   * @update 2020/11/6 14:14
   */
  private void pullDecorateInfo(Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        boolean checkTest = (packet.roomId == UndercoverStatic.getLong(UndercoverStatic.TEST_ID));
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (checkTest) {
          boolean flag = RandomUtils.nextBoolean();
          if (flag) {
            playerInfo.setAvatarFrame(
                "https://7lestore.oss-cn-hangzhou.aliyuncs.com/file/admin/7ed8d12e9a13491aac07301324d2a562.svga");
          }
        } else {
          Map<String, Object> dressInfo = this.userRemote.getUserFrame(packet.userId);
          if (dressInfo != null && !dressInfo.isEmpty()) {
            playerInfo.setAvatarFrame(StringUtils.nvl(dressInfo.get("iconFrame")));
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
   * @create 2020/11/6 14:23
   * @update 2020/11/6 14:23
   */
  private void joinFindUndercoverRoom(Packet packet) {
    boolean checkTest = (packet.roomId == UndercoverStatic.getLong(UndercoverStatic.TEST_ID));
    if (!checkTest) {
      Map<String, Object> result = Maps.newHashMap();
      result.put("userId", packet.userId);
      result.put("gameId", packet.channel);
      result.put("roomId", packet.roomId);
      this.gameRemote.enterRoom(result);
      this.gameRemote.refreshUserRecord(result);
    }
  }

  /**
   * TODO 刷新匹配.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 14:35
   * @update 2020/11/6 14:35
   */
  private void refreshRoomMatching(Long roomId) {
    boolean checkTest = (roomId == UndercoverStatic.getLong(UndercoverStatic.TEST_ID));
    if (!checkTest) {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        // 房间匹配 0-开启 1-关闭
        if (roomInfo.getRoomMatch() == 0) {
          int remainingSeat = roomInfo.remainingSeat();
          if (remainingSeat > 0) {
            MatchRoom match = new MatchRoom();
            match.setRoomId(roomId);
            match.setPeopleNum(remainingSeat);
            MatchManager.updFindSpyMatch(match, roomInfo.getRoomType(), roomInfo.getSpeakMode());
          } else {
            MatchManager.delFindSpyMatch(roomId, roomInfo.getRoomType(), roomInfo.getSpeakMode());
          }
        }
      }
    }
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
        new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.ENTER_ROOM,
            builder.setResult(1).build().toByteArray()));
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
        new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.ENTER_ROOM,
            builder.setResult(2).build().toByteArray()));
  }

  /**
   * TODO 准备信息.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 16:19
   * @update 2020/11/6 16:19
   */
  private void readyMessage(Long roomId) {
    FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
    if (Objects.nonNull(roomInfo)) {
      if (roomInfo.getRoomType() == 0) {
        if (roomInfo.seatedPlayersNum() == UndercoverStatic.getInt(UndercoverStatic.PEOPLE_MAX_1)) {
          // 法官消息
          sendMessage(21, roomId, null, null);
        }
      } else {
        if (roomInfo.seatedPlayersNum() == UndercoverStatic.getInt(UndercoverStatic.PEOPLE_MAX_2)) {
          // 法官消息
          sendMessage(21, roomId, null, null);
        }
      }
    }
  }

  /**
   * TODO 发送信息.
   *
   * @param select [选择编号]
   * @param roomId [房间ID]
   * @param userName [玩家名称]
   * @param number [目标编号]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/20 21:05
   * @update 2020/9/16 21:07
   */
  private void sendMessage(Integer select, Long roomId, String userName, Integer number) {
    if (select < UndercoverStatic.getInt(UndercoverStatic.MESSAGE_INDEX)) {
      F10001.F100012S2C.Builder builder = F10001.F100012S2C.newBuilder();
      switch (select) {
        case 0:
          builder.setMessage(" " + userName + " 进入房间！");
          break;
        case 1:
          builder.setMessage(" 玩家已完成准备，游戏即将开始！");
          break;
        case 2:
          builder.setMessage(" 【" + number + "】号玩家，申请换词！");
          break;
        case 3:
          builder.setMessage(" 申请换词成功！");
          break;
        case 4:
          builder.setMessage(" 申请换词失败！");
          break;
        case 5:
          builder.setMessage(" 描述结束，开始投票！");
          break;
        case 6:
          builder.setMessage(" 第 " + number + " 回合，请描述词汇！");
          break;
        case 7:
          builder.setMessage(" 【" + number + "】号玩家为平民！");
          break;
        case 8:
          builder.setMessage(" 【" + number + "】号玩家为卧底，卧底玩家是否爆词！");
          break;
        case 9:
          builder.setMessage(" 平票决斗，请开始描述！");
          break;
        case 10:
          builder.setMessage(" 【" + number + "】号玩家，请描述词汇！");
          break;
        case 11:
          builder.setMessage(" " + userName + " 离开座位！");
          break;
        case 12:
          builder.setMessage(" 游戏开始，请描述词汇！");
          break;
        case 13:
          builder.setMessage(" 【" + number + "】号玩家爆词失败！");
          break;
        case 14:
          builder.setMessage(" " + userName + " 离开房间！");
          break;
        default:
          builder.setMessage(" 【" + number + "】号玩家取消爆词！");
          break;
      }
      ChatManager.sendPacketToGroup(
          new Packet(ActionCmd.GAME_CHAT, FindUndercoverCmd.SYSTEM_INFO,
              builder.setType(0).setUserID(0).build().toByteArray()), roomId);
    } else {
      sendMessageTwo(select, roomId, userName, number);
    }
  }

  /**
   * TODO 发送信息.
   *
   * @param select [选择编号]
   * @param roomId [房间ID]
   * @param userName [玩家名称]
   * @param number [目标编号]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/20 21:05
   * @update 2020/9/16 21:07
   */
  private void sendMessageTwo(Integer select, Long roomId, String userName, Integer number) {
    F10001.F100012S2C.Builder builder = F10001.F100012S2C.newBuilder();
    switch (select) {
      case 16:
        builder.setMessage(" 【" + number + "】号玩家选择爆词！");
        break;
      case 17:
        builder.setMessage(" 【" + number + "】号玩家开始爆词！");
        break;
      case 18:
        builder.setMessage(" 【" + number + "】号玩家爆词成功！");
        break;
      case 19:
        builder.setMessage(" 【" + number + "】号玩家爆词失败！");
        break;
      case 20:
        builder.setMessage(" " + userName + " 坐上座位！");
        break;
      case 21:
        builder.setMessage(" 座位已坐满，请大家尽快准备！");
        break;
      case 22:
        builder.setMessage(" 【" + number + "】号玩家为卧底！");
        break;
      case 23:
        builder.setMessage(" 请大家尽快准备，新一轮比赛准备中！");
        break;
      default:
        builder.setMessage(" 游戏结束！");
        break;
    }
    ChatManager.sendPacketToGroup(
        new Packet(ActionCmd.GAME_CHAT, FindUndercoverCmd.SYSTEM_INFO,
            builder.setType(0).setUserID(0).build().toByteArray()), roomId);
  }

  /**
   * TODO 玩家准备.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 16:33
   * @update 2020/11/6 16:33
   */
  private void playerReady(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(playerInfo)) {
          F30051.F300511C2S request = F30051.F300511C2S.parseFrom(packet.bytes);
          F30051.F300511S2C.Builder response = F30051.F300511S2C.newBuilder();
          // 房间状态 0-未开始 1-已开始           玩家身份 0-玩家 1-观众
          if (roomInfo.getRoomStatus() == 0 && playerInfo.getIdentity() == 0) {
            // 准备状态 0-准备 1-取消
            if (request.getIsReady() == 0) {
              playerInfo.setPlayerStatus(1);
              roomInfo.cancelTimeOut((int) packet.userId);
              response.setResult(0)
                  .setIsReady(request.getIsReady())
                  .setUserID(packet.userId)
                  .setDeviPosition(playerInfo.getSeatNumber());
              int unReady = roomInfo.unprepared();
              int isReady = roomInfo.preparations();
              if (unReady > 0) {
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PLAYER_READY,
                        response.build().toByteArray()), roomInfo.getRoomId());
              }
              boolean startWordSession = roomInfo.getRoomType() == 0
                  && isReady >= UndercoverStatic.getInt(UndercoverStatic.PEOPLE_MIN_1);
              boolean startVoiceSession = roomInfo.getRoomType() == 1
                  && isReady >= UndercoverStatic.getInt(UndercoverStatic.PEOPLE_MIN_2);
              if (startWordSession) {
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PLAYER_READY,
                        response.setGameTime(3).build().toByteArray()), roomInfo.getRoomId());
                sendMessage(1, roomInfo.getRoomId(), null, null);
                startTimeout(roomInfo.getRoomId());
              } else if (startVoiceSession) {
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PLAYER_READY,
                        response.setGameTime(3).build().toByteArray()), roomInfo.getRoomId());
                sendMessage(1, roomInfo.getRoomId(), null, null);
                startTimeout(roomInfo.getRoomId());
              } else {
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PLAYER_READY,
                        response.build().toByteArray()), roomInfo.getRoomId());
              }
            } else {
              playerInfo.setPlayerStatus(0);
              roomInfo.cancelTimeOut((int) FindUndercoverCmd.START_GAME_TIME);
              response.setResult(0)
                  .setIsReady(request.getIsReady())
                  .setUserID(packet.userId)
                  .setDeviPosition(playerInfo.getSeatNumber())
                  .setReadyTime(15);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PLAYER_READY,
                      response.build().toByteArray()), roomInfo.getRoomId());
              readyTimeout(packet.roomId, packet.userId);
            }
          } else {
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PLAYER_READY,
                    response.setResult(1).setIsReady(request.getIsReady()).build().toByteArray()));
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
   * @create 2020/11/9 10:01
   * @update 2020/11/9 10:01
   */
  private void changeWords(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(playerInfo)) {
          F30051.F300514S2C.Builder response = F30051.F300514S2C.newBuilder();
          // 换词标记 0-未开始 1-已开始 2-已结束
          boolean changeIndex = (roomInfo.getChangeIndex() == 0);
          boolean showTimeIndex = roomInfo.getTimeOutMap().containsKey((int) FindUndercoverCmd.WORD_SHOW_TIME);
          if (changeIndex && showTimeIndex) {
            roomInfo.cancelTimeOut((int) FindUndercoverCmd.WORD_SHOW_TIME);
            roomInfo.setChangeIndex(1);
            roomInfo.setChangePlayer(packet.userId);
            // 换词操作 0-同意 1-不同意 2-未操作
            playerInfo.setChangeIndex(0);
            sendMessage(2, packet.roomId, null, playerInfo.getSeatNumber());
            response.setApplyNo(playerInfo.getSeatNumber());
            List<FindUndercoverPlayer> playerList = roomInfo.getPlayerList();
            playerList = playerList.stream()
                .filter(player -> player.getPlayerId() > 0)
                .sorted(Comparator.comparing(FindUndercoverPlayer::getSeatNumber))
                .collect(Collectors.toList());
            F30051.ChangeInfo.Builder changeInfo;
            for (FindUndercoverPlayer player : playerList) {
              changeInfo = F30051.ChangeInfo.newBuilder();
              changeInfo.setPlayerNo(player.getSeatNumber());
              changeInfo.setPlayerIcon(player.getAvatarIcon());
              changeInfo.setIsChangeWord(player.getChangeIndex());
              response.addChangeInfo(changeInfo);
            }
            response.setResult(0);
            response.setChangeTime(15);
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.CHANGE_WORDS,
                    response.build().toByteArray()), roomInfo.getRoomId());
            // 换词定时
            changeTimeout(packet.roomId);
          } else {
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.CHANGE_WORDS,
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
   * TODO 换词操作.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 10:24
   * @update 2020/11/9 10:24
   */
  private void changeAction(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(playerInfo)) {
          F30051.F300515C2S request = F30051.F300515C2S.parseFrom(packet.bytes);
          F30051.F300515S2C.Builder response = F30051.F300515S2C.newBuilder();
          // IsChangeWord 是否同意 0-同意 1-不同意
          playerInfo.setChangeIndex(request.getIsChangeWord());
          int agreeNum = roomInfo.agreeChangeWordsNum();
          int disagreeNum = roomInfo.disagreeChangeWordsNum();
          int playersNum = roomInfo.seatedPlayersNum();
          if (agreeNum + disagreeNum == playersNum) {
            roomInfo.cancelTimeOut((int) FindUndercoverCmd.CHANGE_WORDS);
            // 换词标记 0-未开始 1-已开始 2-已结束.
            roomInfo.setChangeIndex(2);
            roomInfo.setChangePlayer(0L);
            // 房间类型 0-{4-6人>1卧底} 1-{7-8人>2卧底}
            if (roomInfo.getRoomType() == 0) {
              if (playersNum == UndercoverStatic.getInt(UndercoverStatic.PEOPLE_MIN_1)) {
                if (agreeNum > 2) {
                  changeWordsSuccess(packet.roomId, packet.userId, request.getIsChangeWord());
                } else {
                  changeWordsFailed(packet.roomId, packet.userId, request.getIsChangeWord());
                }
              } else {
                if (agreeNum > 3) {
                  changeWordsSuccess(packet.roomId, packet.userId, request.getIsChangeWord());
                } else {
                  changeWordsFailed(packet.roomId, packet.userId, request.getIsChangeWord());
                }
              }
            } else {
              if (playersNum == UndercoverStatic.getInt(UndercoverStatic.PEOPLE_MIN_2)) {
                if (agreeNum >= 4) {
                  changeWordsSuccess(packet.roomId, packet.userId, request.getIsChangeWord());
                } else {
                  changeWordsFailed(packet.roomId, packet.userId, request.getIsChangeWord());
                }
              } else {
                if (agreeNum > 4) {
                  changeWordsSuccess(packet.roomId, packet.userId, request.getIsChangeWord());
                } else {
                  changeWordsFailed(packet.roomId, packet.userId, request.getIsChangeWord());
                }
              }
            }
            return;
          }
          response.setPlayerNo(playerInfo.getSeatNumber());
          response.setIsChangeWord(request.getIsChangeWord());
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.CHANGE_ACTION,
                  response.build().toByteArray()), roomInfo.getRoomId());
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
   * TODO 换词成功.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 12:32
   * @update 2020/11/9 12:32
   */
  private void changeWordsSuccess(Long roomId) {
    FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
    if (Objects.nonNull(roomInfo)) {
      sendMessage(3, roomId, null, null);
      F30051.F300515S2C.Builder response = F30051.F300515S2C.newBuilder();
      GroupManager.sendPacketToGroup(
          new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.CHANGE_ACTION,
              // 换词结果 1-换词成功 2-换词失败
              response.setChangeResult(1).build().toByteArray()), roomInfo.getRoomId());
      // 推送词汇 延时 1 s
      changeShowTimeout(roomId, true);
    }
  }

  /**
   * TODO 换词成功.
   *
   * @param roomId [房间ID]
   * @param userId [用户ID]
   * @param isChangeWord [是否同意 0-同意 1-不同意]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 12:32
   * @update 2020/11/9 12:32
   */
  private void changeWordsSuccess(Long roomId, Long userId, Integer isChangeWord) {
    FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
    if (Objects.nonNull(roomInfo)) {
      FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(userId);
      if (Objects.nonNull(playerInfo)) {
        sendMessage(3, roomId, null, null);
        F30051.F300515S2C.Builder response = F30051.F300515S2C.newBuilder();
        response.setPlayerNo(playerInfo.getSeatNumber())
            .setIsChangeWord(isChangeWord)
            // 换词结果 1-换词成功 2-换词失败
            .setChangeResult(1);
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.CHANGE_ACTION,
                response.build().toByteArray()), roomInfo.getRoomId());
        // 推送词汇 延时 1 s
        changeShowTimeout(roomId, true);
      }
    }
  }

  /**
   * TODO 换词失败.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 12:34
   * @update 2020/11/9 12:34
   */
  private void changeWordsFailed(Long roomId) {
    FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
    if (Objects.nonNull(roomInfo)) {
      sendMessage(4, roomId, null, null);
      F30051.F300515S2C.Builder response = F30051.F300515S2C.newBuilder();
      GroupManager.sendPacketToGroup(
          new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.CHANGE_ACTION,
              // 换词结果 1-换词成功 2-换词失败
              response.setChangeResult(2).build().toByteArray()), roomInfo.getRoomId());
      // 回合开始 延时 1 s
      changeShowTimeout(roomId, false);
    }
  }

  /**
   * TODO 换词失败.
   *
   * @param roomId [房间ID]
   * @param userId [用户ID]
   * @param isChangeWord [是否同意 0-同意 1-不同意]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 12:34
   * @update 2020/11/9 12:34
   */
  private void changeWordsFailed(Long roomId, Long userId, Integer isChangeWord) {
    FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
    if (Objects.nonNull(roomInfo)) {
      FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(userId);
      if (Objects.nonNull(playerInfo)) {
        sendMessage(4, roomId, null, null);
        F30051.F300515S2C.Builder response = F30051.F300515S2C.newBuilder();
        response.setPlayerNo(playerInfo.getSeatNumber())
            .setIsChangeWord(isChangeWord)
            // 换词结果 1-换词成功 2-换词失败
            .setChangeResult(2);
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.CHANGE_ACTION,
                response.build().toByteArray()), roomInfo.getRoomId());
        // 回合开始 延时 1 s
        changeShowTimeout(roomId, false);
      }
    }
  }

  /**
   * TODO 文字描述.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 9:56
   * @update 2020/11/10 9:56
   */
  private void wordsDescription(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(playerInfo)) {
          if (StringUtils.isEmpty(playerInfo.getSpeakWords())) {
            F30051.F300517C2S request = F30051.F300517C2S.parseFrom(packet.bytes);
            String words = request.getWord();
            String playerSpeak = "";
            // 游戏身份 0-未分配 1-平民 2-卧底
            if (playerInfo.getGameIdentity() == 1) {
              playerSpeak = replaceMessage(roomInfo.getRoomLexicon().getLexiconMass(), words);
            } else if (playerInfo.getGameIdentity() == 2) {
              playerSpeak = replaceMessage(roomInfo.getRoomLexicon().getLexiconSpy(), words);
            }
            playerInfo.setSpeakWords(playerSpeak);
            F30051.F300517S2C.Builder response = F30051.F300517S2C.newBuilder();
            response.setWord(playerSpeak);
            response.setUserID(playerInfo.getPlayerId());
            response.setSpeakPlayer(playerInfo.getSeatNumber());
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.WORDS_DESCRIPTION,
                    response.build().toByteArray()), roomInfo.getRoomId());
            List<FindUndercoverPlayer> playerList = roomInfo.getPlayerList();
            // 正常描述
            if (roomInfo.getTimeOutMap().containsKey((int) FindUndercoverCmd.WORDS_DESCRIPTION)) {
              playerList = playerList.stream()
                  .filter(player -> player.getPlayerId() > 0
                      // 用户状态 0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开
                      && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
                  ).collect(Collectors.toList());
              int speakSize = (int) playerList.stream()
                  .filter(player -> Objects.nonNull(player.getSpeakWords())).count();
              if (playerList.size() == speakSize) {
                roomInfo.cancelTimeOut((int) FindUndercoverCmd.WORDS_DESCRIPTION);
                // 开始投票
                startVote(packet.roomId);
              }
            } else if (roomInfo.getTimeOutMap().containsKey((int) FindUndercoverCmd.VOTE_BATTLE)) {
              // 平票描述
              List<Long> battleList = roomInfo.getBattleList();
              int speakSize = (int) playerList.stream()
                  .filter(player -> battleList.contains(player.getPlayerId())
                      && Objects.nonNull(player.getSpeakWords())).count();
              if (battleList.size() == speakSize) {
                roomInfo.cancelTimeOut((int) FindUndercoverCmd.VOTE_BATTLE);
                // 平票投票 TODO --------------------------------------------------
                startBattleVote(packet.roomId);
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
   * TODO 语音描述.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 9:57
   * @update 2020/11/10 9:57
   */
  private void voiceDescription(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(playerInfo)) {
          if (roomInfo.getTimeOutMap().containsKey((int) FindUndercoverCmd.VOICE_DESCRIPTION)) {
            if (packet.userId == roomInfo.getSpeakPlayer()) {
              F30051.F300518S2C.Builder builder = F30051.F300518S2C.newBuilder();
              builder.setSpeakPlayer(playerInfo.getSeatNumber());
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.VOICE_DESCRIPTION,
                      builder.build().toByteArray()), roomInfo.getRoomId());
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
   * TODO 玩家投票.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 9:58
   * @update 2020/11/10 9:58
   */
  private void playerVote(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(playerInfo)) {
          // 重置标记
          roomInfo.setHandleIndex(0);
          F30051.F3005110C2S request = F30051.F3005110C2S.parseFrom(packet.bytes);
          FindUndercoverPlayer targetPlayerInfo = roomInfo.getPlayerInfo(request.getUserID());
          F10001.F100012S2C.Builder chatBuilder = F10001.F100012S2C.newBuilder();
          chatBuilder.setType(0).setUserID(0)
              .setMessage(" 你投给了【" + targetPlayerInfo.getSeatNumber() + "】号！");
          channel.writeAndFlush(
              new Packet(ActionCmd.GAME_CHAT, FindUndercoverCmd.SYSTEM_INFO,
                  chatBuilder.setType(0).setUserID(0).build().toByteArray()));
          // 投票标记
          playerInfo.setVoteIndex(1);
          // 开始投票
          roomInfo.playerVote(request.getUserID(), playerInfo.getPlayerId(), playerInfo.getAvatarIcon());
          F30051.F3005110S2C.Builder builder = F30051.F3005110S2C.newBuilder();
          List<FindUndercoverPlayer> playerList = roomInfo.getPlayerList();
          playerList = playerList.stream()
              .filter(player -> player.getPlayerId() > 0
                  // 用户状态 0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开
                  && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
                  // 投票标记
                  && player.getVoteIndex() == 1
              ).collect(Collectors.toList());
          // 投票信息
          List<FindUndercoverVote> voteList = roomInfo.getVoteList();
          F30051.VoteInfo.Builder voteInfo;
          for (FindUndercoverVote vote : voteList) {
            voteInfo = F30051.VoteInfo.newBuilder();
            voteInfo.setUserID(vote.getUserId());
            voteInfo.addAllUserIcon(vote.getVoteList());
            builder.addVoteInfo(voteInfo);
          }
          // 投票数据
          byte[] voteByte = builder.build().toByteArray();
          playerList.forEach(player -> player.sendPacket(
              new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PLAYER_VOTE, voteByte)));
          List<Long> votePlayers = voteList.stream()
              .map(FindUndercoverVote::getUserId)
              .collect(Collectors.toCollection(Lists::newLinkedList));
          int voteNum = voteList.stream()
              .filter(vote -> vote.getVoteList().size() > 0)
              .mapToInt(vote -> vote.getVoteList().size()).sum();
          playerList = roomInfo.getPlayerList();
          int canNum = (int) playerList.stream()
              .filter(player -> player.getPlayerId() > 0
                  // 可以投票的玩家
                  && !votePlayers.contains(player.getPlayerId())
                  // 用户状态 0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开
                  && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
              ).count();
          if (voteNum == canNum) {
            // 正常投票
            roomInfo.cancelTimeOut((int) FindUndercoverCmd.PLAYER_START_VOTE);
            // 平票投票
            roomInfo.cancelTimeOut((int) FindUndercoverCmd.PLAYER_BATTLE_TIME);
            // 投票结果 TODO --------------------------------------------------
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
   * @create 2020/11/10 9:59
   * @update 2020/11/10 9:59
   */
  private void openWords(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(playerInfo)) {
          // 选择爆词 TODO ---------------------------------------------------
          if (roomInfo.getTimeOutMap().containsKey((int) FindUndercoverCmd.SELECT_EXPOSURE_TIME)) {
            roomInfo.cancelTimeOut((int) FindUndercoverCmd.SELECT_EXPOSURE_TIME);
            F30051.F3005113C2S request = F30051.F3005113C2S.parseFrom(packet.bytes);
            // 是否爆词 0-是 1-否
            if (request.getIsBlastWord() == 0) {
              // 爆词玩家
              roomInfo.setOpenWordsPlayer(packet.userId);
              // 法官消息
              sendMessage(16, packet.roomId, null, playerInfo.getSeatNumber());
              F30051.F3005113S2C.Builder response = F30051.F3005113S2C.newBuilder();
              response.setUserID(packet.userId);
              // SPY_OPEN_WORDS
              response.setBlastTime(20);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.OPEN_WORDS,
                      response.build().toByteArray()), roomInfo.getRoomId());
              // 爆词定时 TODO --------------------------------------------------------
              openWordTimeOut(packet.roomId, packet.userId);
            } else {
              // 法官消息
              sendMessage(15, packet.roomId, null, playerInfo.getSeatNumber());
              if (roomInfo.getUndercoverList().size() > 0) {
                // 下一回合 TODO -----------------------------------------------------------
                roundDetection(packet.roomId);
                // roundShowTimeout(packet.roomId);
              } else {
                // 游戏结束 平民胜利 TODO --------------------------------------------------------
                roomInfo.destroy();
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
   * TODO 间谍爆词.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 10:00
   * @update 2020/11/10 10:00
   */
  private void spyOpenWords(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(playerInfo)) {
          roomInfo.cancelTimeOut((int) FindUndercoverCmd.SPY_OPEN_WORDS);
          F30051.F3005114C2S request = F30051.F3005114C2S.parseFrom(packet.bytes);
          playerInfo.setOpenWords(request.getBlastWord());
          // 法官消息
          sendMessage(17, packet.roomId, null, playerInfo.getSeatNumber());
          F30051.F3005114S2C.Builder builder = F30051.F3005114S2C.newBuilder();
          if (roomInfo.getRoomLexicon().getLexiconMass().equals(request.getBlastWord())) {
            // 爆词成功
            builder.setResult(0).setUserID(packet.userId);
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.SPY_OPEN_WORDS,
                    builder.build().toByteArray()), roomInfo.getRoomId());
            // 法官消息
            sendMessage(18, packet.roomId, null, playerInfo.getSeatNumber());
            // 卧底胜利 TODO ------------------------------------------------------------
          } else {
            // 爆词失败
            builder.setResult(1).setUserID(packet.userId);
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.SPY_OPEN_WORDS,
                    builder.build().toByteArray()), roomInfo.getRoomId());
            // 法官消息
            sendMessage(19, packet.roomId, null, playerInfo.getSeatNumber());
            // 玩家出局
            playerInfo.setPlayerStatus(3);
            // 胜利判断 TODO ------------------------------------------------------------
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
   * TODO 回合检测.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 16:37
   * @update 2020/11/10 16:37
   */
  private void roundDetection(Long roomId) {
    FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
    if (Objects.nonNull(roomInfo)) {
      if (roomInfo.getHandleIndex() >= roomInfo.getMaxRoomRound()) {
        // 长时间没人投票 没人描述 游戏结束 平民胜利

      } else {
        // 重置平票
        roomInfo.setBattleIndex(0);
        // 重置说话玩家
        roomInfo.setSpeakPlayer(0L);
        // 重置爆词玩家
        roomInfo.setOpenWordsPlayer(0L);
        // 剩余间谍
        int spiesRemaining = roomInfo.spiesRemaining();
        //  剩余平民
        int civiliansRemaining = roomInfo.civiliansRemaining();
        // 0-[4-6人>1卧底] 1-[7-8人>2卧底].
        if (civiliansRemaining == spiesRemaining) {
          // 游戏结束 卧底胜利 TODO -----------------------------------------------
          roomInfo.destroy();



        } else {
          // 回合初始
          roomInfo.roundInit();
          // 刷新回合
          roomInfo.refreshRounds();
          // 法官消息
          sendMessage(6, roomId, null, roomInfo.getRoomRound());
          F30051.F300512S2C.Builder builder = F30051.F300512S2C.newBuilder();
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.ROUND_START,
                  builder.setRotationNum(roomInfo.getRoomRound()).build().toByteArray()), roomInfo.getRoomId());
          // 新的回合
          roundShowTimeout(roomId);
        }
      }
    }
  }

  /**
   * TODO 玩家退出.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 10:01
   * @update 2020/11/10 10:01
   */
  private void playerExit(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(playerInfo)) {
          // 房间状态 0-未开始 1-已开始
          if (roomInfo.getRoomStatus() == 1) {
            // 玩家身份 0-玩家 1-观众
            if (playerInfo.getIdentity() == 0) {
              playerInfo.setPlayerStatus(4);
              playerInfo.setChannel(null);
              clearChannel(channel, packet);
              if (this.redisUtils.hasKey(GameRedisKey.KEY_GAME_JOIN_RECORD + packet.userId)) {
                this.redisUtils.del(GameRedisKey.KEY_GAME_JOIN_RECORD + packet.userId);
              }
              this.gameRemote.leaveRoom(packet.userId);
              F30051.F3005116S2C.Builder response = F30051.F3005116S2C.newBuilder();
              response.setResult(0).setUserID(packet.userId);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PLAYER_EXIT,
                      response.build().toByteArray()), packet.roomId);
              // 离开的玩家 == 入座的玩家
              if ((roomInfo.seatedLeavePlayerNum() == roomInfo.seatedPlayersNum())
                  // 观战的玩家
                  && roomInfo.getSpectatorList().size() == 0) {
                clearRoomInfo(packet.roomId);
              }
            } else {
              roomInfo.leaveGame(packet.userId, 1, playerInfo.getSeatNumber());
              clearChannel(channel, packet);
              if (this.redisUtils.hasKey(GameRedisKey.KEY_GAME_JOIN_RECORD + packet.userId)) {
                this.redisUtils.del(GameRedisKey.KEY_GAME_JOIN_RECORD + packet.userId);
              }
              this.gameRemote.leaveRoom(packet.userId);
              // 离开的玩家 == 入座的玩家
              if ((roomInfo.seatedLeavePlayerNum() == roomInfo.seatedPlayersNum())
                  // 观战的玩家
                  && roomInfo.getSpectatorList().size() == 0) {
                clearRoomInfo(packet.roomId);
              }
            }
          } else {
            // 玩家身份 0-玩家 1-观众
            if (playerInfo.getIdentity() == 0) {
              // 用户状态 0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开
              if (playerInfo.getPlayerStatus() == 0) {
                roomInfo.cancelTimeOut((int) packet.userId);
              } else {
                // 关闭开始游戏
                roomInfo.cancelTimeOut((int) FindUndercoverCmd.START_GAME_TIME);
              }
              // 法官消息
              sendMessage(14, packet.roomId, playerInfo.getPlayerName(), null);
              roomInfo.leaveGame(packet.userId, 0, playerInfo.getSeatNumber());
              clearChannel(channel, packet);
              if (this.redisUtils.hasKey(GameRedisKey.KEY_GAME_JOIN_RECORD + packet.userId)) {
                this.redisUtils.del(GameRedisKey.KEY_GAME_JOIN_RECORD + packet.userId);
              }
              this.gameRemote.leaveRoom(packet.userId);
              F30051.F3005116S2C.Builder response = F30051.F3005116S2C.newBuilder();
              response.setResult(0).setUserID(packet.userId);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PLAYER_EXIT,
                      response.build().toByteArray()), packet.roomId);
              if (roomInfo.seatedPlayersNum() == 0 && roomInfo.getSpectatorList().size() == 0) {
                clearRoomInfo(packet.roomId);
              } else {
                int unReady = roomInfo.unprepared();
                int isReady = roomInfo.preparations();
                boolean startWordSession = (unReady == 0 && roomInfo.getRoomType() == 0
                    && isReady >= UndercoverStatic.getInt(UndercoverStatic.PEOPLE_MIN_1));
                boolean startVoiceSession = (unReady == 0 && roomInfo.getRoomType() == 1
                    && isReady >= UndercoverStatic.getInt(UndercoverStatic.PEOPLE_MIN_2));
                if (startWordSession) {
                  sendMessage(1, roomInfo.getRoomId(), null, null);
                  startTimeout(roomInfo.getRoomId());
                } else if (startVoiceSession) {
                  sendMessage(1, roomInfo.getRoomId(), null, null);
                  startTimeout(roomInfo.getRoomId());
                } else {
                  // 刷新匹配
                  refreshRoomMatching(packet.roomId);
                  // 清理检测
                  List<FindUndercoverPlayer> playerList = roomInfo.getPlayerList();
                  int playGameSize = (int) playerList.stream()
                      .filter(s -> s.getPlayerId() > 0).count();
                  if (playGameSize == 0) {
                    clearTimeout(roomInfo.getRoomId());
                  }
                }
              }
            } else {
              roomInfo.leaveGame(packet.userId, 1, playerInfo.getSeatNumber());
              clearChannel(channel, packet);
              if (this.redisUtils.hasKey(GameRedisKey.KEY_GAME_JOIN_RECORD + packet.userId)) {
                this.redisUtils.del(GameRedisKey.KEY_GAME_JOIN_RECORD + packet.userId);
              }
              this.gameRemote.leaveRoom(packet.userId);
              if (roomInfo.seatedPlayersNum() == 0 && roomInfo.getSpectatorList().size() == 0) {
                clearRoomInfo(packet.roomId);
              }
            }
          }
        } else {
          clearChannel(channel, packet);
          if (this.redisUtils.hasKey(GameRedisKey.KEY_GAME_JOIN_RECORD + packet.userId)) {
            this.redisUtils.del(GameRedisKey.KEY_GAME_JOIN_RECORD + packet.userId);
          }
        }
      } else {
        clearChannel(channel, packet);
        clearRoomInfo(packet.roomId);
        if (this.redisUtils.hasKey(GameRedisKey.KEY_GAME_JOIN_RECORD + packet.userId)) {
          this.redisUtils.del(GameRedisKey.KEY_GAME_JOIN_RECORD + packet.userId);
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
   * @create 2020/11/10 10:02
   * @update 2020/11/10 10:02
   */
  private void watchInfo(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(playerInfo)) {
          F30051.PlayerInfo.Builder playerBuilder = F30051.PlayerInfo.newBuilder();
          playerBuilder.setNick(playerInfo.getPlayerName());
          playerBuilder.setUserID(playerInfo.getPlayerId());
          playerBuilder.setUrl(playerInfo.getAvatarIcon());
          playerBuilder.setSex(playerInfo.getPlayerSex());
          playerBuilder.setDeviPosition(playerInfo.getSeatNumber());
          playerBuilder.setState(playerInfo.getPlayerStatus());
          F30051.F3005118S2C.Builder builder = F30051.F3005118S2C.newBuilder();
          builder.setNowUser(playerBuilder);
          // 房间状态 0-未开始 1-已开始
          if (roomInfo.getRoomStatus() == 0) {
            if (playerInfo.getPlayerStatus() >= 1) {
              builder.setIsCanAction(false);
            } else {
              builder.setIsCanAction(true);
            }
          } else {
            builder.setIsCanAction(false);
          }
          builder.setNowStatus(playerInfo.getIdentity());
          if (playerInfo.getIdentity() == 0) {
            builder.addAllWatchUser(roomInfo.getSpectatorIcon());
          } else {
            builder.addAllWatchUser(roomInfo.getSpectatorIcon()
                .stream()
                .filter(icon -> !icon.equals(playerInfo.getAvatarIcon()))
                .collect(Collectors.toList()));
          }
          channel.writeAndFlush(
              new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.WATCH_INFO,
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
   * @create 2020/11/10 10:03
   * @update 2020/11/10 10:03
   */
  private void joinLeave(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(playerInfo)) {
          F30051.F3005119C2S request = F30051.F3005119C2S.parseFrom(packet.bytes);
          F30051.F3005119S2C.Builder response = F30051.F3005119S2C.newBuilder();
          // 房间状态 0-未开始 1-已开始
          if (roomInfo.getRoomStatus() == 1) {
            response.setResult(1).setStand(request.getIsStand());
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.JOIN_LEAVE,
                    response.build().toByteArray()));
          } else {
            // 玩家操作 0-站起 1-坐下
            if (request.getIsStand() == 0) {
              playersStandUp(channel, packet);
            } else {
              playerSitsDown(channel, packet);
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
   * @create 2020/11/11 13:48
   * @update 2020/11/11 13:48
   */
  private void playersStandUp(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(playerInfo)) {
          F30051.F3005119C2S request = F30051.F3005119C2S.parseFrom(packet.bytes);
          F30051.F3005119S2C.Builder response = F30051.F3005119S2C.newBuilder();
          // 用户状态 0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开
          if (playerInfo.getPlayerStatus() > 0) {
            response.setResult(1).setStand(request.getIsStand());
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.JOIN_LEAVE,
                    response.build().toByteArray()));
          } else {
            roomInfo.cancelTimeOut((int) packet.userId);
            int seatNumber = roomInfo.leaveSeat(playerInfo.getPlayerId());
            response.setResult(0).setSeatNo(seatNumber).setStand(request.getIsStand());
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.JOIN_LEAVE,
                    response.build().toByteArray()));
            // 法官消息
            sendMessage(11, packet.roomId, playerInfo.getPlayerName(), null);
            // 观战信息
            watchInfo(channel, packet);
            int unReady = roomInfo.unprepared();
            int isReady = roomInfo.preparations();
            boolean startWordSession = (unReady == 0 && roomInfo.getRoomType() == 0
                && isReady >= UndercoverStatic.getInt(UndercoverStatic.PEOPLE_MIN_1));
            boolean startVoiceSession = (unReady == 0 && roomInfo.getRoomType() == 1
                && isReady >= UndercoverStatic.getInt(UndercoverStatic.PEOPLE_MIN_2));
            if (startWordSession) {
              sendMessage(1, roomInfo.getRoomId(), null, null);
              startTimeout(roomInfo.getRoomId());
            } else if (startVoiceSession) {
              sendMessage(1, roomInfo.getRoomId(), null, null);
              startTimeout(roomInfo.getRoomId());
            } else {
              // 刷新匹配
              refreshRoomMatching(packet.roomId);
              // 清理检测
              List<FindUndercoverPlayer> playerList = roomInfo.getPlayerList();
              int playGameSize = (int) playerList.stream()
                  .filter(s -> s.getPlayerId() > 0).count();
              if (playGameSize == 0) {
                clearTimeout(roomInfo.getRoomId());
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
   * TODO 玩家坐下.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 13:48
   * @update 2020/11/11 13:48
   */
  private void playerSitsDown(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(playerInfo)) {
          F30051.F3005119C2S request = F30051.F3005119C2S.parseFrom(packet.bytes);
          F30051.F3005119S2C.Builder response = F30051.F3005119S2C.newBuilder();
          if (roomInfo.remainingSeat() > 0) {
            // 玩家身份 0-玩家 1-观众.
            if (playerInfo.getIdentity() != 0) {
              response.setResult(1).setStand(request.getIsStand());
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.JOIN_LEAVE,
                      response.build().toByteArray()));
            } else {
              // 销毁清理定时
              roomInfo.cancelTimeOut((int) packet.roomId);
              // 销毁开始定时
              roomInfo.cancelTimeOut((int) FindUndercoverCmd.START_GAME_TIME);
              int seatNumber = roomInfo.joinSeat(packet.userId);
              response.setResult(0).setStand(request.getIsStand()).setSeatNo(seatNumber);
              F30051.PlayerInfo.Builder playerBuilder = F30051.PlayerInfo.newBuilder();
              playerBuilder.setNick(playerInfo.getPlayerName());
              playerBuilder.setUserID(playerInfo.getPlayerId());
              playerBuilder.setUrl(playerInfo.getAvatarIcon());
              playerBuilder.setSex(playerInfo.getPlayerSex());
              playerBuilder.setDeviPosition(playerInfo.getSeatNumber());
              playerBuilder.setState(playerInfo.getPlayerStatus());
              if (StringUtils.isNotEmpty(playerInfo.getAvatarFrame())) {
                playerBuilder.setIconFrame(playerInfo.getAvatarFrame());
              }
              playerBuilder.setReadyTime(15);
              response.setJoinInfo(playerBuilder);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.JOIN_LEAVE,
                      response.build().toByteArray()), roomInfo.getRoomId());
              // 法官消息
              sendMessage(20, packet.roomId, playerInfo.getPlayerName(), null);
              // 准备定时
              readyTimeout(packet.roomId, packet.userId);
              // 刷新匹配
              refreshRoomMatching(packet.roomId);
            }
          } else {
            response.setResult(1).setStand(request.getIsStand());
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.JOIN_LEAVE,
                    response.build().toByteArray()));
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
   * TODO 选择座位.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 10:04
   * @update 2020/11/10 10:04
   */
  private void selectSeat(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(playerInfo)) {
          F30051.F3005121C2S request = F30051.F3005121C2S.parseFrom(packet.bytes);
          F30051.F3005121S2C.Builder response = F30051.F3005121S2C.newBuilder();
          // 房间状态 0-未开始 1-已开始
          if (roomInfo.getRoomStatus() == 1) {
            channel.writeAndFlush(
                new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.SELECT_SEAT,
                    response.setResult(1).setUserID(packet.userId).build().toByteArray()));
          } else {
            // 玩家身份 0-玩家 1-观众
            if (playerInfo.getIdentity() == 0) {
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.SELECT_SEAT,
                      response.setResult(1).setUserID(packet.userId).build().toByteArray()));
            } else {
              FindUndercoverPlayer targetSeat = roomInfo.getTargetSeat(request.getSeatNo());
              if (targetSeat.getPlayerId() > 0) {
                channel.writeAndFlush(
                    new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.SELECT_SEAT,
                        response.setResult(1).setUserID(packet.userId).build().toByteArray()));
              } else {
                // 销毁清理定时
                roomInfo.cancelTimeOut((int) packet.roomId);
                roomInfo.joinSeat(packet.userId, targetSeat.getSeatNumber());
                playerInfo = roomInfo.getPlayerInfo(packet.userId);
                F30051.PlayerInfo.Builder playerBuilder = F30051.PlayerInfo.newBuilder();
                playerBuilder.setNick(playerInfo.getPlayerName());
                playerBuilder.setUserID(playerInfo.getPlayerId());
                playerBuilder.setUrl(playerInfo.getAvatarIcon());
                playerBuilder.setSex(playerInfo.getPlayerSex());
                playerBuilder.setDeviPosition(playerInfo.getSeatNumber());
                playerBuilder.setState(playerInfo.getPlayerStatus());
                if (StringUtils.isNotEmpty(playerInfo.getAvatarFrame())) {
                  playerBuilder.setIconFrame(playerInfo.getAvatarFrame());
                }
                playerBuilder.setReadyTime(15);
                response.setJoinInfo(playerBuilder);
                response.setResult(0).setSeatNo(request.getSeatNo()).setUserID(packet.userId);
                GroupManager.sendPacketToGroup(
                    new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.SELECT_SEAT,
                        response.build().toByteArray()), roomInfo.getRoomId());
                // 法官消息
                sendMessage(20, packet.roomId, playerInfo.getPlayerName(), null);
                // 准备定时
                readyTimeout(packet.roomId, packet.userId);
                // 刷新匹配
                refreshRoomMatching(packet.roomId);
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
   * @create 2020/11/10 10:05
   * @update 2020/11/10 10:05
   */
  private void playerChat(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(playerInfo)) {
          F30051.F3005122C2S request = F30051.F3005122C2S.parseFrom(packet.bytes);
          F30051.F3005122S2C.Builder response = F30051.F3005122S2C.newBuilder();
          response.setType(1);
          response.setIcon(playerInfo.getAvatarIcon());
          response.setNick(playerInfo.getPlayerName());
          // 房间状态 0-未开始 1-已开始
          if (roomInfo.getRoomStatus() == 0) {
            response.setMessage(request.getMessage());
          } else {
            String message = request.getMessage();
            String lexiconSpy = roomInfo.getRoomLexicon().getLexiconSpy();
            String lexiconMass = roomInfo.getRoomLexicon().getLexiconMass();
            // 游戏身份 0-未分配 1-平民 2-卧底
            if (playerInfo.getGameIdentity() == 1) {
              response.setMessage(replaceMessage(lexiconMass, message));
            } else {
              response.setMessage(replaceMessage(lexiconSpy, message));
            }
          }
          response.setUserID(packet.userId);
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PLAYER_CHAT,
                  response.build().toByteArray()), roomInfo.getRoomId());
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
   * @create 2020/11/10 10:06
   * @update 2020/11/10 10:06
   */
  private void finishSpeak(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(playerInfo)) {
          roomInfo.cancelTimeOut((int) FindUndercoverCmd.VOICE_DESCRIPTION);
          long nowPlayer = roomInfo.getSpeakPlayer();
          long nextPlayer = roomInfo.getNextPlayer(nowPlayer);
          if (nextPlayer != 0L) {
            // 开始说话
            roomInfo.setSpeakPlayer(roomInfo.getSpeakPlayer());
            FindUndercoverPlayer targetPlayer = roomInfo.getPlayerInfo(roomInfo.getSpeakPlayer());
            // 法官消息
            sendMessage(10, packet.roomId, null, playerInfo.getSeatNumber());
            F30051.F3005123S2C.Builder response = F30051.F3005123S2C.newBuilder();
            response.setNextPlayer(targetPlayer.getSeatNumber());
            response.setSpeakTime(20);
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.FINISH_SPEAK,
                    response.build().toByteArray()), roomInfo.getRoomId());
            // 说话定时
            speakTimeout(packet.roomId);
          } else {
            // 开始投票
            startVote(packet.roomId);
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
   * @create 2020/11/6 15:22
   * @update 2020/11/6 15:22
   */
  private void disconnected(Channel channel, Packet packet) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {

      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 清理房间.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 16:37
   * @update 2020/11/9 16:37
   */
  private void clearRoomInfo(Long roomId) {
    FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
    if (Objects.nonNull(roomInfo)) {
      roomInfo.destroy();
      // 房间类型 [0-{4-6人>1卧底} 1-{7-8人>2卧底}]         交流方式 [0-文字 1-语音]
      MatchManager.delFindSpyMatch(roomId, roomInfo.getRoomType(), roomInfo.getSpeakMode());
      GAME_DATA.remove(roomId);
      this.gameRemote.deleteRoom(roomId);
      if (this.redisUtils.hasKey(GameRedisKey.KEY_GAME_ROOM_RECORD + roomId)) {
        this.redisUtils.del(GameRedisKey.KEY_GAME_ROOM_RECORD + roomId);
      }
      ChatManager.delChatGroup(roomId);
      GroupManager.delRoomGroup(roomId);
    }
  }

  /**
   * TODO 清除管道.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 18:06
   * @update 2020/11/10 18:06
   */
  private void clearChannel(Channel channel, Packet packet) {
    ChatManager.removeChannel(packet.roomId, channel);
    GroupManager.removeChannel(packet.roomId, channel);
  }

  /**
   * TODO 玩家准备. 15(s)
   *
   * @param roomId [房间ID]
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 15:45
   * @update 2020/11/6 15:45
   */
  private void readyTimeout(Long roomId, Long playerId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        if (!roomInfo.getTimeOutMap().containsKey(playerId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> readyExecute(roomId, playerId)
              ), 15, TimeUnit.SECONDS);
          roomInfo.addTimeOut(playerId.intValue(), timeout);
          FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(playerId);
          playerInfo.setReadinessTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 准备执行.
   *
   * @param roomId [房间ID]
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 15:47
   * @update 2020/11/6 15:47
   */
  private void readyExecute(Long roomId, Long playerId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        roomInfo.removeTimeOut(playerId.intValue());
        FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(playerId);
        int seatNumber = playerInfo.getSeatNumber();
        F30051.F3005119S2C.Builder builder = F30051.F3005119S2C.newBuilder();
        builder.setResult(0).setStand(0).setSeatNo(seatNumber);
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.JOIN_LEAVE,
                builder.build().toByteArray()), roomInfo.getRoomId());
        playerInfo = roomInfo.getPlayerInfo(playerId);
        sendMessage(11, roomId, playerInfo.getPlayerName(), null);
        int unReady = roomInfo.unprepared();
        int isReady = roomInfo.preparations();
        boolean startWordSession = (unReady == 0 && roomInfo.getRoomType() == 0
            && isReady >= UndercoverStatic.getInt(UndercoverStatic.PEOPLE_MIN_1));
        boolean startVoiceSession = (unReady == 0 && roomInfo.getRoomType() == 1
            && isReady >= UndercoverStatic.getInt(UndercoverStatic.PEOPLE_MIN_2));
        if (startWordSession) {
          sendMessage(1, roomInfo.getRoomId(), null, null);
          startTimeout(roomInfo.getRoomId());
        } else if (startVoiceSession) {
          sendMessage(1, roomInfo.getRoomId(), null, null);
          startTimeout(roomInfo.getRoomId());
        } else {
          // 刷新匹配
          refreshRoomMatching(roomId);
          // 清理检测
          List<FindUndercoverPlayer> playerList = roomInfo.getPlayerList();
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
   * TODO 清理定时. 10(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 16:30
   * @update 2020/11/9 16:30
   */
  private void clearTimeout(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        if (!roomInfo.getTimeOutMap().containsKey(roomId.intValue())) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> clearExecute(roomId)
              ), 10, TimeUnit.SECONDS);
          roomInfo.addTimeOut(roomId.intValue(), timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 清理执行.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/19 13:42
   * @update 2020/10/19 13:42
   */
  private void clearExecute(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        roomInfo.removeTimeOut(roomId.intValue());
        List<FindUndercoverPlayer> playerList = roomInfo.getPlayerList();
        int playGameSize = (int) playerList.stream()
            .filter(player -> player.getPlayerId() > 0).count();
        if (playGameSize == 0) {
          List<FindUndercoverPlayer> watchList = roomInfo.getSpectatorList();
          if (watchList.size() > 0) {
//            F20000.F200007S2C.Builder builder = F20000.F200007S2C.newBuilder();
//            builder.setMsg("(oﾟvﾟ)ノ 房间已解散！");
//            watchList.forEach(findSpyPlayer -> {
//              SoftChannel.sendPacketToUserId(
//                  new Packet(ActionCmd.APP_HEART, (short) 7,
//                      builder.build().toByteArray()), findSpyPlayer.getPlayerId());
//              this.redisUtils.del(GameRedisKey.KEY_GAME_JOIN_RECORD + findSpyPlayer.getPlayerId());
//            });
            clearRoomInfo(roomId);
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 开始游戏. 3(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 12:51
   * @update 2020/11/9 12:51
   */
  private void startTimeout(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindUndercoverCmd.START_GAME_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> startExecute(roomId)
              ), 3, TimeUnit.SECONDS);
          roomInfo.addTimeOut((int) FindUndercoverCmd.START_GAME_TIME, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 游戏执行.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 12:52
   * @update 2020/11/9 12:52
   */
  private void startExecute(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        roomInfo.removeTimeOut((int) FindUndercoverCmd.START_GAME_TIME);
        // 房间状态 0-未开始 1-已开始
        roomInfo.setRoomStatus(1);
        // 最大回合
        roomInfo.setMaxGameRound();
        // 玩家状态
        roomInfo.refreshPlayerStatus();
        // 拉取词汇
        Result result = gameRemote.getWhoIsSpyWords();
        roomInfo.setRoomLexicon(JsonUtils.toObjectMap(result.getData()));
        // 推送词汇
        List<FindUndercoverPlayer> playerList = roomInfo.getPlayerList();
        playerList = playerList.stream()
            .filter(player -> player.getPlayerId() > 0)
            .collect(Collectors.toList());
        // 换词标记 0-未开始 1-已开始 2-已结束
        Integer changeIndex = roomInfo.getChangeIndex();
        F30051.F300513S2C.Builder wordBuilder;
        for (FindUndercoverPlayer player : playerList) {
          wordBuilder = F30051.F300513S2C.newBuilder();
          wordBuilder.setType(0);
          wordBuilder.setShowTime(5);
          // 游戏身份 0-未分配 1-平民 2-卧底
          if (player.getGameIdentity() == 1) {
            wordBuilder.setWord(roomInfo.getRoomLexicon().getLexiconMass());
          } else if (player.getGameIdentity() == 2) {
            wordBuilder.setWord(roomInfo.getRoomLexicon().getLexiconSpy());
          }
          if (changeIndex == 0) {
            wordBuilder.setIsChange(true);
          } else if (changeIndex == 2) {
            wordBuilder.setIsChange(false);
          }
          // 推送玩家
          player.sendPacket(
              new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PUSH_WORDS,
                  wordBuilder.build().toByteArray()));
        }
        // 推送观战
        wordBuilder = F30051.F300513S2C.newBuilder();
        wordBuilder.setType(1);
        wordBuilder.setShowTime(5);
        if (Objects.nonNull(roomInfo.getSpectatorGroup())) {
          roomInfo.getSpectatorGroup().writeAndFlush(
              new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PUSH_WORDS,
                  wordBuilder.build().toByteArray()));
        }
        // 词汇展示
        wordShowTimeout(roomId);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 词汇展示. 5(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 14:11
   * @update 2020/11/9 14:11
   */
  private void wordShowTimeout(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindUndercoverCmd.WORD_SHOW_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> wordShowExecute(roomId)
              ), 5, TimeUnit.SECONDS);
          roomInfo.addTimeOut((int) FindUndercoverCmd.WORD_SHOW_TIME, timeout);
          roomInfo.setShowWordTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 展示执行.词汇
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 14:12
   * @update 2020/11/9 14:12
   */
  private void wordShowExecute(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        roomInfo.removeTimeOut((int) FindUndercoverCmd.WORD_SHOW_TIME);
        roomInfo.setRoomRound(1);
        F30051.F300512S2C.Builder builder = F30051.F300512S2C.newBuilder();
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.ROUND_START,
                builder.setRotationNum(1).build().toByteArray()), roomInfo.getRoomId());
        // 第一回合
        roundShowTimeout(roomId);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 换词定时. 15(S)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 14:01
   * @update 2020/11/9 14:01
   */
  private void changeTimeout(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindUndercoverCmd.CHANGE_WORDS)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> changeExecute(roomId)
              ), 15, TimeUnit.SECONDS);
          roomInfo.addTimeOut((int) FindUndercoverCmd.CHANGE_WORDS, timeout);
          roomInfo.setChangeTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 换词执行.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 14:03
   * @update 2020/11/9 14:03
   */
  private void changeExecute(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        roomInfo.removeTimeOut((int) FindUndercoverCmd.CHANGE_WORDS);
        roomInfo.setChangeIndex(2);
        int agreeNum = roomInfo.agreeChangeWordsNum();
        int playersNum = roomInfo.seatedPlayersNum();
        // 房间类型 0-{4-6人>1卧底} 1-{7-8人>2卧底}
        if (roomInfo.getRoomType() == 0) {
          if (playersNum == UndercoverStatic.getInt(UndercoverStatic.PEOPLE_MIN_1)) {
            if (agreeNum > 2) {
              changeWordsSuccess(roomId);
            } else {
              changeWordsFailed(roomId);
            }
          } else {
            if (agreeNum > 3) {
              changeWordsSuccess(roomId);
            } else {
              changeWordsFailed(roomId);
            }
          }
        } else {
          if (playersNum == UndercoverStatic.getInt(UndercoverStatic.PEOPLE_MIN_2)) {
            if (agreeNum >= 4) {
              changeWordsSuccess(roomId);
            } else {
              changeWordsFailed(roomId);
            }
          } else {
            if (agreeNum > 4) {
              changeWordsSuccess(roomId);
            } else {
              changeWordsFailed(roomId);
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
   * TODO 换词显示. 1(s)
   *
   * @param roomId [房间ID]
   * @param isSuccess [是否成功]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 13:01
   * @update 2020/11/9 13:01
   */
  private void changeShowTimeout(Long roomId, boolean isSuccess) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindUndercoverCmd.CHANGE_ACTION)) {
          Timeout timeout;
          if (isSuccess) {
            timeout = GroupManager.newTimeOut(
                task -> execute(()
                    -> changeShowTrueExecute(roomId)
                ), 1, TimeUnit.SECONDS);
          } else {
            timeout = GroupManager.newTimeOut(
                task -> execute(()
                    -> changeShowFalseExecute(roomId)
                ), 1, TimeUnit.SECONDS);
          }
          roomInfo.addTimeOut((int) FindUndercoverCmd.CHANGE_ACTION, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 成功显示.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 14:45
   * @update 2020/11/9 14:45
   */
  private void changeShowTrueExecute(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        roomInfo.removeTimeOut((int) FindUndercoverCmd.CHANGE_ACTION);
        roomInfo.setChangeIndex(2);
        // 推送词汇
        List<FindUndercoverPlayer> playerList = roomInfo.getPlayerList();
        playerList = playerList.stream()
            .filter(player -> player.getPlayerId() > 0)
            .collect(Collectors.toList());
        // 换词标记 0-未开始 1-已开始 2-已结束
        Integer changeIndex = roomInfo.getChangeIndex();
        F30051.F300513S2C.Builder wordBuilder;
        for (FindUndercoverPlayer player : playerList) {
          wordBuilder = F30051.F300513S2C.newBuilder();
          wordBuilder.setType(0);
          wordBuilder.setShowTime(5);
          // 游戏身份 0-未分配 1-平民 2-卧底
          if (player.getGameIdentity() == 1) {
            wordBuilder.setWord(roomInfo.getRoomLexicon().getLexiconMass());
          } else if (player.getGameIdentity() == 2) {
            wordBuilder.setWord(roomInfo.getRoomLexicon().getLexiconSpy());
          }
          if (changeIndex == 0) {
            wordBuilder.setIsChange(true);
          } else if (changeIndex == 2) {
            wordBuilder.setIsChange(false);
          }
          player.sendPacket(
              new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PUSH_WORDS,
                  wordBuilder.build().toByteArray()));
        }
        // 推送观战
        wordBuilder = F30051.F300513S2C.newBuilder();
        wordBuilder.setType(1);
        wordBuilder.setShowTime(5);
        if (Objects.nonNull(roomInfo.getSpectatorGroup())) {
          roomInfo.getSpectatorGroup().writeAndFlush(
              new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PUSH_WORDS,
                  wordBuilder.build().toByteArray()));
        }
        // 词汇展示
        pushShowTimeout(roomId);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 失败显示.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 14:46
   * @update 2020/11/9 14:46
   */
  private void changeShowFalseExecute(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        roomInfo.removeTimeOut((int) FindUndercoverCmd.CHANGE_ACTION);
        roomInfo.setChangeIndex(2);
        roomInfo.setRoomRound(1);
        F30051.F300512S2C.Builder builder = F30051.F300512S2C.newBuilder();
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.ROUND_START,
                builder.setRotationNum(1).build().toByteArray()), roomInfo.getRoomId());
        // 第一回合
        roundShowTimeout(roomId);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 新的词汇. 5(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 15:18
   * @update 2020/11/9 15:18
   */
  private void pushShowTimeout(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindUndercoverCmd.WORD_SHOW_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> pushShowExecute(roomId)
              ), 5, TimeUnit.SECONDS);
          roomInfo.addTimeOut((int) FindUndercoverCmd.WORD_SHOW_TIME, timeout);
          roomInfo.setShowWordTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 展示结束.换词
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 15:20
   * @update 2020/11/9 15:20
   */
  private void pushShowExecute(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        roomInfo.removeTimeOut((int) FindUndercoverCmd.WORD_SHOW_TIME);
        roomInfo.setRoomRound(1);
        F30051.F300512S2C.Builder builder = F30051.F300512S2C.newBuilder();
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.ROUND_START,
                builder.setRotationNum(1).build().toByteArray()), roomInfo.getRoomId());
        // 第一回合
        roundShowTimeout(roomId);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 回合展示. 2(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 14:50
   * @update 2020/11/9 14:50
   */
  private void roundShowTimeout(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindUndercoverCmd.ROUND_SHOW_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> roundExecute(roomId)
              ), 2, TimeUnit.SECONDS);
          roomInfo.addTimeOut((int) FindUndercoverCmd.ROUND_SHOW_TIME, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 展示执行.回合
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 14:52
   * @update 2020/11/9 14:52
   */
  private void roundExecute(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        roomInfo.removeTimeOut((int) FindUndercoverCmd.ROUND_SHOW_TIME);
        // 交流方式 0-文字 1-语音
        if (roomInfo.getSpeakMode() == 0) {
          // 开始描述
          F30051.F300516S2C.Builder builder = F30051.F300516S2C.newBuilder();
          List<FindUndercoverPlayer> playerList = roomInfo.getPlayerList();
          playerList.stream()
              .filter(player -> player.getPlayerId() > 0
                  // 用户状态 0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开
                  && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
              ).forEach(player -> builder.addPlayerUser(player.getPlayerId()));
          builder.setSpeakTime(40);
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.START_WORDS,
                  builder.build().toByteArray()), roomInfo.getRoomId());
          // 描述定时
          depictTimeout(roomId);
        } else {
          // 开始说话
          roomInfo.setSpeakPlayer(roomInfo.getSpeakPlayer());
          FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(roomInfo.getSpeakPlayer());
          sendMessage(10, roomId, null, playerInfo.getSeatNumber());
          F30051.F300518S2C.Builder builder = F30051.F300518S2C.newBuilder();
          builder.setSpeakPlayer(playerInfo.getSeatNumber());
          builder.setSpeakTime(20);
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.VOICE_DESCRIPTION,
                  builder.build().toByteArray()), roomInfo.getRoomId());
          // 说话定时
          speakTimeout(roomId);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 开始描述. 40(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 15:00
   * @update 2020/11/9 15:00
   */
  private void depictTimeout(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindUndercoverCmd.WORDS_DESCRIPTION)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> depictExecute(roomId)
              ), 40, TimeUnit.SECONDS);
          roomInfo.addTimeOut((int) FindUndercoverCmd.WORDS_DESCRIPTION, timeout);
          roomInfo.setDepictTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 描述执行.正常
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 15:10
   * @update 2020/11/9 15:10
   */
  private void depictExecute(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        roomInfo.removeTimeOut((int) FindUndercoverCmd.WORDS_DESCRIPTION);
        // 补充描述
        roomInfo.completionDepict();
        // 开始投票
        startVote(roomId);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 开始描述. 20(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 15:00
   * @update 2020/11/9 15:00
   */
  private void depictBattleTimeout(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindUndercoverCmd.WORDS_DESCRIPTION)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> depictBattleExecute(roomId)
              ), 40, TimeUnit.SECONDS);
          roomInfo.addTimeOut((int) FindUndercoverCmd.WORDS_DESCRIPTION, timeout);
          roomInfo.setDepictBattleTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 描述执行.平票
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 17:10
   * @update 2020/11/11 17:10
   */
  private void depictBattleExecute(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        roomInfo.removeTimeOut((int) FindUndercoverCmd.WORDS_DESCRIPTION);


        // 开始投票
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 开始说话. 20(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 15:06
   * @update 2020/11/9 15:06
   */
  private void speakTimeout(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindUndercoverCmd.VOICE_DESCRIPTION)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> speakExecute(roomId)
              ), 20, TimeUnit.SECONDS);
          roomInfo.addTimeOut((int) FindUndercoverCmd.VOICE_DESCRIPTION, timeout);
          roomInfo.setSpeakTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 说话执行.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 15:14
   * @update 2020/11/9 15:14
   */
  private void speakExecute(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        roomInfo.removeTimeOut((int) FindUndercoverCmd.VOICE_DESCRIPTION);
        long nowPlayer = roomInfo.getSpeakPlayer();
        long nextPlayer = roomInfo.getNextPlayer(nowPlayer);
        if (nextPlayer != 0L) {
          // 开始说话
          roomInfo.setSpeakPlayer(roomInfo.getSpeakPlayer());
          FindUndercoverPlayer playerInfo = roomInfo.getPlayerInfo(roomInfo.getSpeakPlayer());
          sendMessage(10, roomId, null, playerInfo.getSeatNumber());
          F30051.F300518S2C.Builder builder = F30051.F300518S2C.newBuilder();
          builder.setSpeakPlayer(playerInfo.getSeatNumber());
          builder.setSpeakTime(20);
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.VOICE_DESCRIPTION,
                  builder.build().toByteArray()), roomInfo.getRoomId());
          // 说话定时
          speakTimeout(roomId);
        } else {
          // 开始投票
          startVote(roomId);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 开始投票.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 18:04
   * @update 2020/11/9 18:04
   */
  private void startVote(Long roomId) {
    FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
    if (Objects.nonNull(roomInfo)) {
      // 法官消息
      sendMessage(5, roomId, null, null);
      List<Long> userIds = roomInfo.whoCanVote();
      List<FindUndercoverPlayer> playerList = roomInfo.getPlayerList();
      playerList = playerList.stream()
          .filter(player -> player.getPlayerId() > 0
              // 用户状态 0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开
              && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
          ).collect(Collectors.toList());
      F30051.F300519S2C.Builder builder;
      for (FindUndercoverPlayer player : playerList) {
        builder = F30051.F300519S2C.newBuilder();
        if (userIds.contains(player.getPlayerId())) {
          builder.addAllDoUserId(userIds);
          List<Long> excludeList = userIds.stream()
              .filter(playerId -> !playerId.equals(player.getPlayerId()))
              .collect(Collectors.toList());
          builder.addAllKillUserId(excludeList);
          builder.setVoteTime(20);
          player.sendPacket(
              new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PLAYER_START_VOTE,
                  builder.build().toByteArray()));
        } else {
          builder.addAllDoUserId(userIds);
          builder.addAllKillUserId(userIds);
          builder.setVoteTime(20);
          player.sendPacket(
              new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PLAYER_START_VOTE,
                  builder.build().toByteArray()));
        }
      }
      // 观战推送
      builder = F30051.F300519S2C.newBuilder();
      builder.addAllDoUserId(userIds);
      builder.addAllKillUserId(userIds);
      builder.setVoteTime(20);
      if (Objects.nonNull(roomInfo.getSpectatorGroup())) {
        roomInfo.getSpectatorGroup().writeAndFlush(
            new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PLAYER_START_VOTE,
                builder.build().toByteArray()));
      }
      // 投票定时
      voteTimeout(roomId);
    }
  }

  /**
   * TODO 平票投票.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 18:05
   * @update 2020/11/9 18:05
   */
  private void startBattleVote(Long roomId) {
    FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
    if (Objects.nonNull(roomInfo)) {
      // 法官消息
      sendMessage(5, roomId, null, null);
      List<Long> userIdList = roomInfo.whoCanVote(roomInfo.getBattleList());
      F30051.F300519S2C.Builder builder = F30051.F300519S2C.newBuilder();
      // 可以被投票玩家
      builder.addAllKillUserId(roomInfo.getBattleList());
      // 可以投票玩家
      builder.addAllDoUserId(userIdList);
      builder.setVoteTime(20);
      GroupManager.sendPacketToGroup(
          new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.PLAYER_START_VOTE,
              builder.build().toByteArray()), roomInfo.getRoomId());
      // 投票定时
      battleVoteTimeout(roomId);
    }
  }

  /**
   * TODO 投票定时. 20(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 18:07
   * @update 2020/11/9 18:07
   */
  private void voteTimeout(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindUndercoverCmd.PLAYER_START_VOTE)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> voteExecute(roomId)
              ), 20, TimeUnit.SECONDS);
          roomInfo.addTimeOut((int) FindUndercoverCmd.PLAYER_START_VOTE, timeout);
          roomInfo.setVoteTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 投票执行.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 15:01
   * @update 2020/11/11 15:01
   */
  private void voteExecute(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        roomInfo.removeTimeOut((int) FindUndercoverCmd.PLAYER_START_VOTE);
        // 开始检验投票
        List<Long> voteList = roomInfo.voteCalculation();
        int voteSize = voteList.size();
        if (voteSize == 1) {
          // 投出玩家
        } else if (voteSize == 2) {
          // 平票校验
          if (roomInfo.getBattleIndex() == 0) {
            // 平票标记
            roomInfo.setBattleIndex(1);
            // 重置描述
            roomInfo.initSpeakWords();
            // 法官消息
            sendMessage(9, roomId, null, null);
            F30051.F3005111S2C.Builder builder = F30051.F3005111S2C.newBuilder();
            builder.addAllUserID(voteList);
            for (Long playerId : voteList) {
              FindUndercoverPlayer targetPlayer = roomInfo.getPlayerInfo(playerId);
              builder.addIconUrl(targetPlayer.getAvatarIcon());
            }
            GroupManager.sendPacketToGroup(
                new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.VOTE_RESULTS,
                    builder.build().toByteArray()), roomInfo.getRoomId());
            // 交流方式 0-文字 1-语音
            if (roomInfo.getSpeakMode() == 0) {
              // 平票描述
              depictBattleTimeout(roomInfo.getRoomId());
            } else {
              // 开始说话
              // 按位置重新排序
              List<FindUndercoverPlayer> undercoverPlayers = voteList.stream()
                  .map(roomInfo::getPlayerInfo)
                  .sorted(Comparator.comparing(FindUndercoverPlayer::getSeatNumber))
                  .collect(Collectors.toCollection(Lists::newLinkedList));
              FindUndercoverPlayer nowPlayer = roomInfo.getPlayerInfo(undercoverPlayers.get(0).getPlayerId());
              // 法官消息
              sendMessage(10, roomId, null, nowPlayer.getSeatNumber());
              roomInfo.setSpeakPlayer(nowPlayer.getPlayerId());
              F30051.F300518S2C.Builder speakBuilder = F30051.F300518S2C.newBuilder();


//              sendMessage(10, roomId, null, playerInfo.getSeatNumber());
//
//              builder.setSpeakPlayer(playerInfo.getSeatNumber());
//              builder.setSpeakTime(20);
//              GroupManager.sendPacketToGroup(
//                  new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.VOICE_DESCRIPTION,
//                      builder.build().toByteArray()), roomInfo.getRoomId());



              // 平票说话
              speakTimeout(roomInfo.getRoomId());
            }
          } else {
            // 新的回合 TODO ----------------------------------------------------------
          }
        } else {
          // 新的回合 TODO --------------------------------------------------------------

        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 平票定时. 20(s)
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 16:31
   * @update 2020/11/10 16:31
   */
  private void battleVoteTimeout(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindUndercoverCmd.PLAYER_BATTLE_TIME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> battleVoteExecute(roomId)
              ), 20, TimeUnit.SECONDS);
          roomInfo.addTimeOut((int) FindUndercoverCmd.PLAYER_BATTLE_TIME, timeout);
          roomInfo.setVoteBattleTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 平票执行.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 14:58
   * @update 2020/11/11 14:58
   */
  private void battleVoteExecute(Long roomId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        roomInfo.removeTimeOut((int) FindUndercoverCmd.PLAYER_BATTLE_TIME);
        // 投票结果 TODO ---------------------------------------------------



      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 爆词定时. 20(s)
   *
   * @param roomId [房间ID]
   * @param playerId [爆词玩家]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 16:25
   * @update 2020/11/10 16:25
   */
  private void openWordTimeOut(Long roomId, Long playerId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        if (!roomInfo.getTimeOutMap().containsKey((int) FindUndercoverCmd.SPY_OPEN_WORDS)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> openWordExecute(roomId, playerId)
              ), 20, TimeUnit.SECONDS);
          roomInfo.addTimeOut((int) FindUndercoverCmd.SPY_OPEN_WORDS, timeout);
          roomInfo.setOpenWordTime(LocalDateTime.now());
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 爆词执行.
   *
   * @param roomId [房间ID]
   * @param playerId [爆词玩家]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 16:29
   * @update 2020/11/10 16:29
   */
  private void openWordExecute(Long roomId, Long playerId) {
    try {
      FindUndercoverRoom roomInfo = GAME_DATA.get(roomId);
      if (Objects.nonNull(roomInfo)) {
        // 爆词失败 TODO ---------------------------------------------------



      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 出局展示. 3(s)
   *
   * @param roomId [房间ID]
   * @param action [0-下一回合 1-游戏结算]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 14:39
   * @update 2020/11/11 14:39
   */
  private void outShowTimeout(Long roomId, Integer action) {

  }

  /**
   * TODO 展示执行.出局
   *
   * @param roomId [房间ID]
   * @param action [0-下一回合 1-游戏结算]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 14:42
   * @update 2020/11/11 14:42
   */
  private void outShowExecute(Long roomId, Integer action) {

  }

  /**
   * TODO 爆词展示. 2(s)
   *
   * @param roomId [房间ID]
   * @param action [0-下一回合 1-游戏结算]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 14:52
   * @update 2020/11/11 14:52
   */
  private void openShowTimeout(Long roomId, Integer action) {

  }

  /**
   * TODO 展示执行.爆词
   *
   * @param roomId [房间ID]
   * @param action [0-下一回合 1-游戏结算]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 14:53
   * @update 2020/11/11 14:53
   */
  private void openShowExecute(Long roomId, Integer action) {

  }

}
