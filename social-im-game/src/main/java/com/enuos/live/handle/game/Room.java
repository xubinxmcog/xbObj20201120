package com.enuos.live.handle.game;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.utils.annotation.AbstractAction;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import com.enuos.live.codec.Packet;
import com.enuos.live.constants.GameKey;
import com.enuos.live.proto.f20011msg.F20011;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.utils.RoomUtils;
import com.enuos.live.utils.JsonUtils;
import com.enuos.live.manager.LoggerManager;
import com.enuos.live.utils.RedisUtils;
import io.netty.channel.Channel;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * TODO 创建房间.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/7/17 15:17
 */

@Component
@AbstractAction(cmd = ActionCmd.CREATE_ROOM)
public class Room extends AbstractActionHandler {

  @Resource
  private RedisUtils redisUtils;
  @Resource
  private UserRemote userRemote;

  /**
   * TODO 处理分发.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/17 20:56
   * @update 2020/9/30 15:08
   */
  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      switch (packet.child) {
        case RoomCmd.WHO_IS_SPY:
          whoIsSpy(channel, packet);
          break;
        case RoomCmd.WEREWOLF:
          werewolf(channel, packet);
          break;
        case RoomCmd.POKER_ROOM:
          pokeRoom(channel, packet);
          break;
        case RoomCmd.SPARROW_ROOM:
          sparrowRoom(channel, packet);
          break;
        case RoomCmd.LANDLORDS_ROOM:
          landlordsRoom(channel, packet);
          break;
        default:
          LoggerManager.warn("[ROOM 20011 HANDLE] CHILD ERROR: [{}]", packet.child);
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
   * @param attachId [附属ID(或房间Id)]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/17 20:56
   * @update 2020/9/30 15:12
   */
  @Override
  public void shutOff(Long userId, Long attachId) {
    // MATCH-EMPTY-METHOD
  }

  /**
   * TODO 清除处理.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/30 15:12
   * @update 2020/9/30 15:12
   */
  @Override
  public void cleaning(Long roomId) {
    // MATCH-EMPTY-METHOD
  }

  /**
   * TODO 陪玩处理.
   *
   * @param roomId [房间ID]
   * @param playerIds [机器人消息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/30 15:13
   * @update 2020/9/30 15:13
   */
  @Override
  public void joinRobot(Long roomId, List<Long> playerIds) {
    // MATCH-EMPTY-METHOD
  }

  /**
   * TODO 谁是卧底.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/30 15:13
   * @update 2020/9/30 15:13
   */
  private void whoIsSpy(Channel channel, Packet packet) {
    try {
      F20011.F200111S2C.Builder builder = F20011.F200111S2C.newBuilder();
      long roomId = RoomUtils.getRandomRoomNo();
      boolean isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
      while (!isExists) {
        roomId = RoomUtils.getRandomRoomNo();
        isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
      }
      channel.writeAndFlush(
          new Packet(ActionCmd.CREATE_ROOM, RoomCmd.WHO_IS_SPY,
              builder.setResult(0).setRoomId(roomId).build().toByteArray()));
      I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
      joinInfo.setRoomId(roomId).setGameId(30291L);
      byte[] joinByte = joinInfo.build().toByteArray();
      this.redisUtils.setByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId, joinByte);
      F20011.F200111C2S request = F20011.F200111C2S.parseFrom(packet.bytes);
      I10001.RoomRecord.Builder roomRecord = I10001.RoomRecord.newBuilder();
      roomRecord.setGameId(30291L)
          .setRoomId(roomId)
          .setOpenWay(1)
          .setRoomType(request.getPlayerNum())
          .setGameMode(0)
          .setSpeakMode(request.getSpeakMode())
          .setGameNumber(0)
          .setGameSession(0);
      this.redisUtils.setByte(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId, roomRecord.build().toByteArray());
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 狼人杀.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/30 15:14
   * @update 2020/9/30 15:14
   */
  private void werewolf(Channel channel, Packet packet) {
    // MATCH-EMPTY-METHOD
  }

  private static final int UNO_GOLD_TICKETS = 120;
  private static final int EXPLODING_KITTENS_GOLD_TICKETS = 20;

  /**
   * TODO 扑克游戏.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/17 20:56
   * @update 2020/9/30 15:15
   */
  private void pokeRoom(Channel channel, Packet packet) {
    try {
      F20011.F200113C2S request = F20011.F200113C2S.parseFrom(packet.bytes);
      F20011.F200113S2C.Builder builder = F20011.F200113S2C.newBuilder();
      Result result = this.userRemote.getCurrency(packet.userId);
      if (Objects.nonNull(result)) {
        Map<String, Object> currencyInfo = JsonUtils.toObjectMap(result.getData());
        if (Objects.nonNull(currencyInfo)) {
          Integer playerGold = (Integer) currencyInfo.get("gold");
          switch ((int) request.getGameCode()) {
            case 30251:
              if (playerGold >= UNO_GOLD_TICKETS) {
                long roomId = RoomUtils.getRandomRoomNo();
                boolean isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
                while (!isExists) {
                  roomId = RoomUtils.getRandomRoomNo();
                  isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
                }
                channel.writeAndFlush(
                    new Packet(ActionCmd.CREATE_ROOM, RoomCmd.POKER_ROOM,
                        builder.setRoomId(roomId).build().toByteArray()));
                I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
                joinInfo.setRoomId(roomId).setGameId(30251L);
                byte[] joinByte = joinInfo.build().toByteArray();
                this.redisUtils.setByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId, joinByte);
                I10001.RoomRecord.Builder roomRecord = I10001.RoomRecord.newBuilder();
                roomRecord.setGameId(30251L).setRoomId(roomId).setOpenWay(1).setRoomType(0)
                    .setGameMode(0).setSpeakMode(0).setGameNumber(0).setGameSession(0);
                byte[] roomByte = roomRecord.build().toByteArray();
                this.redisUtils.setByte(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId, roomByte);
              } else {
                channel.writeAndFlush(new Packet(ActionCmd.CREATE_ROOM, RoomCmd.POKER_ROOM,
                    builder.setError("创建房间失败，您的游戏金币不足120！").setRoomId(0).build().toByteArray()));
              }
              break;
            case 30291:
              if (playerGold >= EXPLODING_KITTENS_GOLD_TICKETS) {
                long roomId = RoomUtils.getRandomRoomNo();
                boolean isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
                while (!isExists) {
                  roomId = RoomUtils.getRandomRoomNo();
                  isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
                }
                channel.writeAndFlush(
                    new Packet(ActionCmd.CREATE_ROOM, RoomCmd.POKER_ROOM,
                        builder.setRoomId(roomId).build().toByteArray()));
                I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
                joinInfo.setRoomId(roomId).setGameId(30291L);
                byte[] joinByte = joinInfo.build().toByteArray();
                this.redisUtils.setByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId, joinByte);
                I10001.RoomRecord.Builder roomRecord = I10001.RoomRecord.newBuilder();
                roomRecord.setGameId(30291L).setRoomId(roomId).setOpenWay(1).setRoomType(0)
                    .setGameMode(0).setSpeakMode(0).setGameNumber(0).setGameSession(0);
                byte[] roomByte = roomRecord.build().toByteArray();
                this.redisUtils.setByte(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId, roomByte);
              } else {
                channel.writeAndFlush(new Packet(ActionCmd.CREATE_ROOM, RoomCmd.POKER_ROOM,
                    builder.setError("创建房间失败，您的游戏金币不足20！").setRoomId(0).build().toByteArray()));
              }
              break;
            default:
              LoggerManager.warn("[ROOM 20011 HANDLE] GAME ERROR: [{}]", packet.child);
              break;
          }
        } else {
          channel.writeAndFlush(new Packet(ActionCmd.CREATE_ROOM, RoomCmd.POKER_ROOM,
              builder.setError("当前游戏服务正在维护，请稍后尝试！").setRoomId(0).build().toByteArray()));
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 七乐麻将.
   *
   * @param channel [通讯管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443710411@qq.com
   * @create 2020/8/27 15:11
   * @update 2020/8/27 15:11
   */
  private void sparrowRoom(Channel channel, Packet packet) {
    // MATCH-EMPTY-METHOD
  }

  private static final int PLAYER_GOLD_1 = 1000;
  private static final int PLAYER_GOLD_2 = 3000;
  private static final int PLAYER_GOLD_3 = 10000;
  private static final int PLAYER_GOLD_4 = 40000;

  /**
   * TODO 七乐地主.
   *
   * @param channel [通讯管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443710411@qq.com
   * @create 2020/8/27 15:11
   * @update 2020/8/27 15:11
   */
  private void landlordsRoom(Channel channel, Packet packet) {
    try {
      F20011.F200115C2S request = F20011.F200115C2S.parseFrom(packet.bytes);
      F20011.F200115S2C.Builder builder = F20011.F200115S2C.newBuilder();
      Result result = this.userRemote.getCurrency(packet.userId);
      if (Objects.nonNull(result)) {
        Map<String, Object> currencyInfo = JsonUtils.toObjectMap(result.getData());
        if (Objects.nonNull(currencyInfo)) {
          Integer playerGold = (Integer) currencyInfo.get("gold");
          switch (request.getScreening()) {
            case 1:
              if (playerGold >= PLAYER_GOLD_1) {
                roomCreateSuccess(channel, packet, request.getGameMode(), request.getScreening());
              } else {
                channel.writeAndFlush(new Packet(ActionCmd.CREATE_ROOM, RoomCmd.LANDLORDS_ROOM,
                    builder.setError("创建房间失败，您的游戏金币不足1000！").setRoomId(0).build().toByteArray()));
              }
              break;
            case 2:
              if (playerGold >= PLAYER_GOLD_2) {
                roomCreateSuccess(channel, packet, request.getGameMode(), request.getScreening());
              } else {
                channel.writeAndFlush(new Packet(ActionCmd.CREATE_ROOM, RoomCmd.LANDLORDS_ROOM,
                    builder.setError("创建房间失败，您的游戏金币不足3000！").setRoomId(0).build().toByteArray()));
              }
              break;
            case 3:
              if (playerGold >=  PLAYER_GOLD_3) {
                roomCreateSuccess(channel, packet, request.getGameMode(), request.getScreening());
              } else {
                channel.writeAndFlush(new Packet(ActionCmd.CREATE_ROOM, RoomCmd.LANDLORDS_ROOM,
                    builder.setError("创建房间失败，您的游戏金币不足10000！").setRoomId(0).build().toByteArray()));
              }
              break;
            default:
              if (playerGold >= PLAYER_GOLD_4) {
                roomCreateSuccess(channel, packet, request.getGameMode(), request.getScreening());
              } else {
                channel.writeAndFlush(new Packet(ActionCmd.CREATE_ROOM, RoomCmd.LANDLORDS_ROOM,
                    builder.setError("创建房间失败，您的游戏金币不足40000！").setRoomId(0).build().toByteArray()));
              }
              break;
          }
        } else {
          channel.writeAndFlush(new Packet(ActionCmd.CREATE_ROOM, RoomCmd.LANDLORDS_ROOM,
              builder.setError("游戏服务正在维护！请稍后尝试！").setRoomId(0).build().toByteArray()));
        }
      } else {
        channel.writeAndFlush(new Packet(ActionCmd.CREATE_ROOM, RoomCmd.LANDLORDS_ROOM,
            builder.setError("游戏服务正在维护！请稍后尝试！").setRoomId(0).build().toByteArray()));
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 创建地主.
   *
   * @param channel [通讯管道]
   * @param packet [数据包]
   * @param gameMode [0-经典 1-癞子]
   * @param session [1-初级场 2-中级场 3-高级场 4-王者场]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 15:28
   * @update 2020/10/28 15:28
   */
  private void roomCreateSuccess(Channel channel, Packet packet, Integer gameMode, Integer session) {
    long roomId = RoomUtils.getRandomRoomNo();
    boolean isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
    while (!isExists) {
      roomId = RoomUtils.getRandomRoomNo();
      isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
    }
    F20011.F200115S2C.Builder builder = F20011.F200115S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.CREATE_ROOM, RoomCmd.LANDLORDS_ROOM,
            builder.setRoomId(roomId).build().toByteArray()));
    I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
    joinInfo.setRoomId(roomId).setGameId(30101L);
    byte[] joinByte = joinInfo.build().toByteArray();
    this.redisUtils.setByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId, joinByte);
    I10001.RoomRecord.Builder roomRecord = I10001.RoomRecord.newBuilder();
    roomRecord.setGameId(30101L)
        .setRoomId(roomId)
        .setOpenWay(1)
        .setRoomType(0)
        .setGameMode(gameMode)
        .setSpeakMode(0)
        .setGameNumber(0)
        .setGameSession(session);
    byte[] roomByte = roomRecord.build().toByteArray();
    this.redisUtils.setByte(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId, roomByte);
  }

}
