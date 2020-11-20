package com.enuos.live.handle.match.f30101;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.utils.annotation.AbstractAction;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import com.enuos.live.codec.Packet;
import com.enuos.live.constants.GameKey;
import com.enuos.live.pojo.MatchRoom;
import com.enuos.live.handle.match.MatchCmd;
import com.enuos.live.proto.f20001msg.F20001;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.server.HandlerContext;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.manager.MatchManager;
import com.enuos.live.utils.RoomUtils;
import com.enuos.live.utils.JsonUtils;
import com.enuos.live.manager.LoggerManager;
import com.enuos.live.utils.RedisUtils;
import io.netty.channel.Channel;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Resource;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

/**
 * TODO 斗地主匹配[玩家匹配 + 快速开始].
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/8/27 18:21
 */

@Component
@AbstractAction(cmd = 100101)
public class F30101Match extends AbstractActionHandler {

  private static final int PLAYER_GOLD_1 = 1000;
  private static final int PLAYER_GOLD_2 = 3000;
  private static final int PLAYER_GOLD_3 = 10000;
  private static final int PLAYER_GOLD_4 = 40000;

  @Resource
  private RedisUtils redisUtils;
  @Resource
  private UserRemote userRemote;
  @Resource
  private HandlerContext handlerContext;

  /**
   * TODO 匹配处理.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/27 15:18
   * @update 2020/10/27 15:18
   */
  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      switch (packet.child) {
        // 快速开始
        case 6:
          quickStart(channel, packet);
          break;
        // 房间匹配
        case 9:
          roomMatch(channel, packet);
          break;
        default:
          LoggerManager.warn("[MATCH 30101 HANDLE] CHILD ERROR: [{}]", packet.child);
          break;
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 快速开始.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/27 15:17
   * @update 2020/10/27 15:17
   */
  private void quickStart(Channel channel, Packet packet) {
    try {
      Result result = this.userRemote.getCurrency(packet.userId);
      if (Objects.nonNull(result)) {
        Map<String, Object> currencyInfo = JsonUtils.toObjectMap(result.getData());
        if (Objects.nonNull(currencyInfo)) {
          Integer playerGold = (Integer) currencyInfo.get("gold");
          // true 经典 false 癞子
          boolean flag = RandomUtils.nextBoolean();
          if (flag) {
            if (playerGold >= PLAYER_GOLD_4) {
              randomSearch(channel, packet, 0, 1);
            } else if (playerGold >= PLAYER_GOLD_3) {
              randomSearch(channel, packet, 0, 2);
            } else if (playerGold >= PLAYER_GOLD_2) {
              randomSearch(channel, packet, 0, 3);
            } else if (playerGold >= PLAYER_GOLD_1) {
              randomSearch(channel, packet, 0, 4);
            } else {
              goldShortage(channel, PLAYER_GOLD_1, MatchCmd.QUICK_START);
            }
          } else {
            if (playerGold >= PLAYER_GOLD_4) {
              randomSearch(channel, packet, 1, 1);
            } else if (playerGold >= PLAYER_GOLD_3) {
              randomSearch(channel, packet, 1, 2);
            } else if (playerGold >= PLAYER_GOLD_2) {
              randomSearch(channel, packet, 1, 3);
            } else if (playerGold >= PLAYER_GOLD_1) {
              randomSearch(channel, packet, 1, 4);
            } else {
              goldShortage(channel, PLAYER_GOLD_1, MatchCmd.QUICK_START);
            }
          }
        } else {
          connectionFailed(channel,  MatchCmd.QUICK_START);
        }
      } else {
        connectionFailed(channel,  MatchCmd.QUICK_START);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 随机搜索.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @param gameMode [0-经典 1-癞子]
   * @param randomType [1-王者场 2-高级场 3-中级场 4-初级场]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 10:31
   * @update 2020/10/28 10:31
   */
  private void randomSearch(Channel channel, Packet packet, Integer gameMode, Integer randomType) {
    int session;
    switch (randomType) {
      case 1:
        session = ThreadLocalRandom.current().nextInt(4) + 1;
        break;
      case 2:
        session = ThreadLocalRandom.current().nextInt(3) + 1;
        break;
      case 3:
        session = ThreadLocalRandom.current().nextInt(2) + 1;
        break;
      default:
        session = 1;
        break;
    }
    int index = checkResidualAuthenticity(gameMode, session);
    if (index == 1) {
      quickStartCreateSuccess(channel, packet, gameMode, session);
    } else {
      quickStartMatchSuccess(channel, packet, gameMode, session);
    }
  }

  /**
   * TODO 匹配成功.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @param gameMode [0-经典 1-癞子]
   * @param session [1-初级场 2-中级场 3-高级场 4-王者场]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 10:37
   * @update 2020/10/28 10:37
   */
  private void quickStartMatchSuccess(Channel channel, Packet packet, Integer gameMode, Integer session) {
    MatchRoom matchRoom;
    if (gameMode == 0) {
      switch (session) {
        case 1:
          matchRoom = MatchManager.LANDLORDS_SESSION_CLASSIC_1.get(0);
          break;
        case 2:
          matchRoom = MatchManager.LANDLORDS_SESSION_CLASSIC_2.get(0);
          break;
        case 3:
          matchRoom = MatchManager.LANDLORDS_SESSION_CLASSIC_3.get(0);
          break;
        default:
          matchRoom = MatchManager.LANDLORDS_SESSION_CLASSIC_4.get(0);
          break;
      }
    } else {
      switch (session) {
        case 1:
          matchRoom = MatchManager.LANDLORDS_SESSION_SHAMELESSLY_1.get(0);
          break;
        case 2:
          matchRoom = MatchManager.LANDLORDS_SESSION_SHAMELESSLY_2.get(0);
          break;
        case 3:
          matchRoom = MatchManager.LANDLORDS_SESSION_SHAMELESSLY_3.get(0);
          break;
        default:
          matchRoom = MatchManager.LANDLORDS_SESSION_SHAMELESSLY_4.get(0);
          break;
      }
    }
    matchRoom.setPeopleNum(matchRoom.getPeopleNum() - 1);
    if (matchRoom.getPeopleNum() == 0) {
      MatchManager.delLandlordsMatch(matchRoom.getRoomId(), gameMode, session);
    }
    F20001.F200016S2C.Builder builder = F20001.F200016S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_MATCH, MatchCmd.QUICK_START,
            builder.setRoomId(matchRoom.getRoomId()).build().toByteArray()));
    I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
    joinInfo.setRoomId(matchRoom.getRoomId());
    joinInfo.setGameId(30101L);
    byte[] joinByte = joinInfo.build().toByteArray();
    this.redisUtils.setByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId, joinByte);
  }

  /**
   * TODO 创建成功.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @param gameMode [0-经典 1-癞子]
   * @param session [1-初级场 2-中级场 3-高级场 4-王者场]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 10:38
   * @update 2020/10/28 10:38
   */
  private void quickStartCreateSuccess(Channel channel, Packet packet, Integer gameMode, Integer session) {
    long roomId = RoomUtils.getRandomRoomNo();
    boolean isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
    while (!isExists) {
      roomId = RoomUtils.getRandomRoomNo();
      isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
    }
    MatchRoom matchRoom = new MatchRoom();
    matchRoom.setRoomId(roomId);
    matchRoom.setPeopleNum(2);
    F20001.F200016S2C.Builder builder = F20001.F200016S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_MATCH, MatchCmd.QUICK_START,
            builder.setRoomId(roomId).build().toByteArray()));
    MatchManager.refreshLandlords(matchRoom, gameMode, session);
    I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
    joinInfo.setRoomId(matchRoom.getRoomId());
    joinInfo.setGameId(30101L);
    byte[] joinByte = joinInfo.build().toByteArray();
    this.redisUtils.setByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId, joinByte);
    I10001.RoomRecord.Builder roomRecord = I10001.RoomRecord.newBuilder();
    roomRecord.setGameId(30101L)
        .setRoomId(matchRoom.getRoomId())
        .setOpenWay(0)
        .setRoomType(0)
        .setGameMode(gameMode)
        .setSpeakMode(0)
        .setGameNumber(0)
        .setGameSession(session);
    byte[] roomByte = roomRecord.build().toByteArray();
    this.redisUtils.setByte(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId(), roomByte);
  }

  /**
   * TODO 检查残余.
   *
   * @param gameMode [0-经典 1-癞子]
   * @param session [1-初级场 2-中级场 3-高级场 4-王者场]
   * @return [0-获取 1-创建]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 10:39
   * @update 2020/10/28 10:39
   */
  private int checkResidualAuthenticity(Integer gameMode, Integer session) {
    int index;
    if (gameMode == 0) {
      index = landlordsClassicSession(gameMode, session);
    } else {
      index = landlordsShamelesslySession(gameMode, session);
    }
    return index;
  }

  /**
   * TODO 经典场次.
   *
   * @param gameMode [0-经典 1-癞子]
   * @param session [1-初级场 2-中级场 3-高级场 4-王者场]
   * @return [0-获取 1-创建]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 9:51
   * @update 2020/10/28 9:51
   */
  private int landlordsClassicSession(Integer gameMode, Integer session) {
    MatchRoom matchRoom;
    boolean isExists;
    int index = 0;
    switch (session) {
      case 1:
        if (MatchManager.LANDLORDS_SESSION_CLASSIC_1.size() > 0) {
          matchRoom = MatchManager.LANDLORDS_SESSION_CLASSIC_1.get(0);
          isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
          while (!isExists) {
            cleanUpResidue(matchRoom.getRoomId(), gameMode, session);
            if (MatchManager.LANDLORDS_SESSION_CLASSIC_1.size() > 0) {
              matchRoom = MatchManager.LANDLORDS_SESSION_CLASSIC_1.get(0);
              isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
            } else {
              index = 1;
              isExists = true;
            }
          }
        } else {
          index = 1;
        }
        break;
      case 2:
        if (MatchManager.LANDLORDS_SESSION_CLASSIC_2.size() > 0) {
          matchRoom = MatchManager.LANDLORDS_SESSION_CLASSIC_2.get(0);
          isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
          while (!isExists) {
            cleanUpResidue(matchRoom.getRoomId(), gameMode, session);
            if (MatchManager.LANDLORDS_SESSION_CLASSIC_2.size() > 0) {
              matchRoom = MatchManager.LANDLORDS_SESSION_CLASSIC_2.get(0);
              isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
            } else {
              index = 1;
              isExists = true;
            }
          }
        } else {
          index = 1;
        }
        break;
      case 3:
        if (MatchManager.LANDLORDS_SESSION_CLASSIC_3.size() > 0) {
          matchRoom = MatchManager.LANDLORDS_SESSION_CLASSIC_3.get(0);
          isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
          while (!isExists) {
            cleanUpResidue(matchRoom.getRoomId(), gameMode, session);
            if (MatchManager.LANDLORDS_SESSION_CLASSIC_3.size() > 0) {
              matchRoom = MatchManager.LANDLORDS_SESSION_CLASSIC_3.get(0);
              isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
            } else {
              index = 1;
              isExists = true;
            }
          }
        } else {
          index = 1;
        }
        break;
      default:
        if (MatchManager.LANDLORDS_SESSION_CLASSIC_4.size() > 0) {
          matchRoom = MatchManager.LANDLORDS_SESSION_CLASSIC_4.get(0);
          isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
          while (!isExists) {
            cleanUpResidue(matchRoom.getRoomId(), gameMode, session);
            if (MatchManager.LANDLORDS_SESSION_CLASSIC_4.size() > 0) {
              matchRoom = MatchManager.LANDLORDS_SESSION_CLASSIC_4.get(0);
              isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
            } else {
              index = 1;
              isExists = true;
            }
          }
        } else {
          index = 1;
        }
        break;
    }
    return index;
  }

  /**
   * TODO 癞子场次.
   *
   * @param gameMode [0-经典 1-癞子]
   * @param session [1-初级场 2-中级场 3-高级场 4-王者场]
   * @return [0-获取 1-创建]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 9:50
   * @update 2020/10/28 9:50
   */
  private int landlordsShamelesslySession(Integer gameMode, Integer session) {
    MatchRoom matchRoom;
    boolean isExists;
    int index = 0;
    switch (session) {
      case 1:
        if (MatchManager.LANDLORDS_SESSION_SHAMELESSLY_1.size() > 0) {
          matchRoom = MatchManager.LANDLORDS_SESSION_SHAMELESSLY_1.get(0);
          isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
          while (!isExists) {
            cleanUpResidue(matchRoom.getRoomId(), gameMode, session);
            if (MatchManager.LANDLORDS_SESSION_SHAMELESSLY_1.size() > 0) {
              matchRoom = MatchManager.LANDLORDS_SESSION_SHAMELESSLY_1.get(0);
              isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
            } else {
              index = 1;
              isExists = true;
            }
          }
        } else {
          index = 1;
        }
        break;
      case 2:
        if (MatchManager.LANDLORDS_SESSION_SHAMELESSLY_2.size() > 0) {
          matchRoom = MatchManager.LANDLORDS_SESSION_SHAMELESSLY_2.get(0);
          isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
          while (!isExists) {
            cleanUpResidue(matchRoom.getRoomId(), gameMode, session);
            if (MatchManager.LANDLORDS_SESSION_SHAMELESSLY_2.size() > 0) {
              matchRoom = MatchManager.LANDLORDS_SESSION_SHAMELESSLY_2.get(0);
              isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
            } else {
              index = 1;
              isExists = true;
            }
          }
        } else {
          index = 1;
        }
        break;
      case 3:
        if (MatchManager.LANDLORDS_SESSION_SHAMELESSLY_3.size() > 0) {
          matchRoom = MatchManager.LANDLORDS_SESSION_SHAMELESSLY_3.get(0);
          isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
          while (!isExists) {
            cleanUpResidue(matchRoom.getRoomId(), gameMode, session);
            if (MatchManager.LANDLORDS_SESSION_SHAMELESSLY_3.size() > 0) {
              matchRoom = MatchManager.LANDLORDS_SESSION_SHAMELESSLY_3.get(0);
              isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
            } else {
              index = 1;
              isExists = true;
            }
          }
        } else {
          index = 1;
        }
        break;
      default:
        if (MatchManager.LANDLORDS_SESSION_SHAMELESSLY_4.size() > 0) {
          matchRoom = MatchManager.LANDLORDS_SESSION_SHAMELESSLY_4.get(0);
          isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
          while (!isExists) {
            cleanUpResidue(matchRoom.getRoomId(), gameMode, session);
            if (MatchManager.LANDLORDS_SESSION_SHAMELESSLY_4.size() > 0) {
              matchRoom = MatchManager.LANDLORDS_SESSION_SHAMELESSLY_4.get(0);
              isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
            } else {
              index = 1;
              isExists = true;
            }
          }
        } else {
          index = 1;
        }
        break;
    }
    return index;
  }

  /**
   * TODO 清理残留.
   *
   * @param roomId [房间ID]
   * @param gameMode [0-经典 1-癞子]
   * @param session [1-初级场 2-中级场 3-高级场 4-王者场]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 9:36
   * @update 2020/10/28 9:36
   */
  private void cleanUpResidue(Long roomId, Integer gameMode, Integer session) {
    MatchManager.delLandlordsMatch(roomId, gameMode, session);
    AbstractActionHandler instance = this.handlerContext.getInstance(30101);
    instance.cleaning(roomId);
  }

  /**
   * TODO 房间匹配.
   *
   * @param channel 快速通道
   * @param packet 数据包
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/27 15:17
   * @update 2020/10/27 15:17
   */
  private void roomMatch(Channel channel, Packet packet) {
    try {
      F20001.F200019C2S request = F20001.F200019C2S.parseFrom(packet.bytes);
      Result result = this.userRemote.getCurrency(packet.userId);
      if (Objects.nonNull(result)) {
        Map<String, Object> currencyInfo = JsonUtils.toObjectMap(result.getData());
        if (Objects.nonNull(currencyInfo)) {
          Integer playerGold = (Integer) currencyInfo.get("gold");
          switch (request.getScreening()) {
            case 1:
              if (playerGold >= PLAYER_GOLD_1) {
                startRoomMatch(channel, packet, request.getGameMode(), request.getScreening());
              } else {
                goldShortage(channel, PLAYER_GOLD_1, MatchCmd.LANDLORDS_MATCH);
              }
              break;
            case 2:
              if (playerGold >= PLAYER_GOLD_2) {
                startRoomMatch(channel, packet, request.getGameMode(), request.getScreening());
              } else {
                goldShortage(channel, PLAYER_GOLD_2, MatchCmd.LANDLORDS_MATCH);
              }
              break;
            case 3:
              if (playerGold >= PLAYER_GOLD_3) {
                startRoomMatch(channel, packet, request.getGameMode(), request.getScreening());
              } else {
                goldShortage(channel, PLAYER_GOLD_3, MatchCmd.LANDLORDS_MATCH);
              }
              break;
            default:
              if (playerGold >= PLAYER_GOLD_4) {
                startRoomMatch(channel, packet, request.getGameMode(), request.getScreening());
              } else {
                goldShortage(channel, PLAYER_GOLD_4, MatchCmd.LANDLORDS_MATCH);
              }
              break;
          }
        } else {
          connectionFailed(channel,  MatchCmd.LANDLORDS_MATCH);
        }
      } else {
        connectionFailed(channel,  MatchCmd.LANDLORDS_MATCH);
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 金币不足.
   *
   * @param channel [通信管道]
   * @param gold [不足金币]
   * @param matchCmd [操作命令]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 12:51
   * @update 2020/10/28 12:51
   */
  private void goldShortage(Channel channel, Integer gold, short matchCmd) {
    String errorMessage = "(っ °Д °;)っ 无法加入游戏！您的游戏金币不足" + gold + "！";
    if (matchCmd == MatchCmd.QUICK_START) {
      F20001.F200016S2C.Builder builder = F20001.F200016S2C.newBuilder();
      builder.setError(errorMessage).setRoomId(0);
      channel.writeAndFlush(
          new Packet(ActionCmd.GAME_MATCH, matchCmd, builder.build().toByteArray()));
    } else {
      F20001.F200019S2C.Builder builder = F20001.F200019S2C.newBuilder();
      builder.setError(errorMessage).setRoomId(0);
      channel.writeAndFlush(
          new Packet(ActionCmd.GAME_MATCH, matchCmd, builder.build().toByteArray()));
    }
  }

  /**
   * TODO 连接失败.
   *
   * @param channel [通信管道]
   * @param matchCmd [操作命令]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 12:55
   * @update 2020/10/28 12:55
   */
  private void connectionFailed(Channel channel, short matchCmd) {
    String errorMessage = "o(￣▽￣)ｄ 游戏服务正在维护！请稍后尝试！";
    if (matchCmd == MatchCmd.QUICK_START) {
      F20001.F200016S2C.Builder builder = F20001.F200016S2C.newBuilder();
      builder.setError(errorMessage).setRoomId(0);
      channel.writeAndFlush(
          new Packet(ActionCmd.GAME_MATCH, MatchCmd.QUICK_START,
              builder.build().toByteArray()));
    } else {
      F20001.F200019S2C.Builder builder = F20001.F200019S2C.newBuilder();
      builder.setError(errorMessage).setRoomId(0);
      channel.writeAndFlush(
          new Packet(ActionCmd.GAME_MATCH, MatchCmd.LANDLORDS_MATCH,
              builder.build().toByteArray()));
    }
  }

  /**
   * TODO 开始匹配.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @param gameMode [0-经典 1-癞子]
   * @param session [1-初级场 2-中级场 3-高级场 4-王者场]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 12:50
   * @update 2020/10/28 12:50
   */
  private void startRoomMatch(Channel channel, Packet packet, Integer gameMode, Integer session) {
    int index = checkResidualAuthenticity(gameMode, session);
    if (index == 1) {
      roomCreateSuccess(channel, packet, gameMode, session);
    } else {
      roomMatchSuccess(channel, packet, gameMode, session);
    }
  }

  /**
   * TODO 匹配成功.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @param gameMode [0-经典 1-癞子]
   * @param session [1-初级场 2-中级场 3-高级场 4-王者场]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 10:37
   * @update 2020/10/28 10:37
   */
  private void roomMatchSuccess(Channel channel, Packet packet, Integer gameMode, Integer session) {
    MatchRoom matchRoom;
    if (gameMode == 0) {
      switch (session) {
        case 1:
          matchRoom = MatchManager.LANDLORDS_SESSION_CLASSIC_1.get(0);
          break;
        case 2:
          matchRoom = MatchManager.LANDLORDS_SESSION_CLASSIC_2.get(0);
          break;
        case 3:
          matchRoom = MatchManager.LANDLORDS_SESSION_CLASSIC_3.get(0);
          break;
        default:
          matchRoom = MatchManager.LANDLORDS_SESSION_CLASSIC_4.get(0);
          break;
      }
    } else {
      switch (session) {
        case 1:
          matchRoom = MatchManager.LANDLORDS_SESSION_SHAMELESSLY_1.get(0);
          break;
        case 2:
          matchRoom = MatchManager.LANDLORDS_SESSION_SHAMELESSLY_2.get(0);
          break;
        case 3:
          matchRoom = MatchManager.LANDLORDS_SESSION_SHAMELESSLY_3.get(0);
          break;
        default:
          matchRoom = MatchManager.LANDLORDS_SESSION_SHAMELESSLY_4.get(0);
          break;
      }
    }
    matchRoom.setPeopleNum(matchRoom.getPeopleNum() - 1);
    if (matchRoom.getPeopleNum() == 0) {
      MatchManager.delLandlordsMatch(matchRoom.getRoomId(), gameMode, session);
    }
    F20001.F200019S2C.Builder builder = F20001.F200019S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_MATCH, MatchCmd.LANDLORDS_MATCH,
            builder.setRoomId(matchRoom.getRoomId()).build().toByteArray()));
    I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
    joinInfo.setRoomId(matchRoom.getRoomId());
    joinInfo.setGameId(30101L);
    byte[] joinByte = joinInfo.build().toByteArray();
    this.redisUtils.setByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId, joinByte);
  }

  /**
   * TODO 创建成功.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @param gameMode [0-经典 1-癞子]
   * @param session [1-初级场 2-中级场 3-高级场 4-王者场]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 10:38
   * @update 2020/10/28 10:38
   */
  private void roomCreateSuccess(Channel channel, Packet packet, Integer gameMode, Integer session) {
    long roomId = RoomUtils.getRandomRoomNo();
    boolean isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
    while (!isExists) {
      roomId = RoomUtils.getRandomRoomNo();
      isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
    }
    MatchRoom matchRoom = new MatchRoom();
    matchRoom.setRoomId(roomId);
    matchRoom.setPeopleNum(2);
    F20001.F200019S2C.Builder builder = F20001.F200019S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_MATCH, MatchCmd.LANDLORDS_MATCH,
            builder.setRoomId(roomId).build().toByteArray()));
    MatchManager.refreshLandlords(matchRoom, gameMode, session);
    I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
    joinInfo.setRoomId(matchRoom.getRoomId());
    joinInfo.setGameId(30101L);
    byte[] joinByte = joinInfo.build().toByteArray();
    this.redisUtils.setByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId, joinByte);
    I10001.RoomRecord.Builder roomRecord = I10001.RoomRecord.newBuilder();
    roomRecord.setGameId(30101L)
        .setRoomId(matchRoom.getRoomId())
        .setOpenWay(0)
        .setRoomType(0)
        .setGameMode(gameMode)
        .setSpeakMode(0)
        .setGameNumber(0)
        .setGameSession(session);
    byte[] roomByte = roomRecord.build().toByteArray();
    this.redisUtils.setByte(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId(), roomByte);
  }

  /**
   * TODO 关闭处理.
   *
   * @param userId [用户ID]
   * @param attachId [附属ID(或房间Id)]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/27 14:56
   * @update 2020/10/27 14:56
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
   * @create 2020/10/27 14:57
   * @update 2020/10/27 14:57
   */
  @Override
  public void cleaning(Long roomId) {
    // MATCH-EMPTY-METHOD
  }

  /**
   * TODO 陪玩处理.
   *
   * @param roomId [房间ID]
   * @param playerIds [机器人信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/27 14:57
   * @update 2020/10/27 14:57
   */
  @Override
  public void joinRobot(Long roomId, List<Long> playerIds) {
    // MATCH-EMPTY-METHOD
  }
}
