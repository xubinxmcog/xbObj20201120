package com.enuos.live.handle.match.f30051;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.utils.annotation.AbstractAction;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import com.enuos.live.codec.Packet;
import com.enuos.live.constants.GameKey;
import com.enuos.live.pojo.MatchRoom;
import com.enuos.live.handle.match.MatchCmd;
import com.enuos.live.proto.f20001msg.F20001;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.server.HandlerContext;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.manager.MatchManager;
import com.enuos.live.utils.RoomUtils;
import com.enuos.live.manager.LoggerManager;
import com.enuos.live.utils.RedisUtils;
import io.netty.channel.Channel;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

/**
 * TODO 谁是卧底[玩家匹配 + 快速开始].
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version v2.0.0
 * @since 2020/6/17 16:19
 */

@Component
@AbstractAction(cmd = 100051)
public class F30051Match extends AbstractActionHandler {

  @Resource
  private RedisUtils redisUtils;
  @Resource
  private HandlerContext handlerContext;

  /**
   * TODO 匹配处理.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/10 21:11
   * @update 2020/8/26 16:39
   */
  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      switch (packet.child) {
        // 房间匹配
        case 3:
          roomMatch(channel, packet);
          break;
        // 快速开始
        case 6:
          quickStart(channel, packet);
          break;
        default:
          LoggerManager.warn("[MATCH 30051 HANDLE] CHILD ERROR: [{}]", packet.child);
          break;
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 房间匹配.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/2 13:29
   * @update 2020/11/2 13:29
   */
  private void roomMatch(Channel channel, Packet packet) {
    try {
      F20001.F200013C2S request = F20001.F200013C2S.parseFrom(packet.bytes);
      // 0-获取 1-创建
      int index = checkResidualAuthenticity(request.getNumberMatch(), request.getMeetMode());
      if (index == 1) {
        roomCreateSuccess(channel, packet, request.getNumberMatch(), request.getMeetMode());
      } else {
        roomMatchSuccess(channel, packet, request.getNumberMatch(), request.getMeetMode());
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 创建成功.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @param matchNumber [0-{4-6人} 1-{7-8人}]
   * @param gameMode [0-文字 1-语音]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/2 13:29
   * @update 2020/11/2 13:29
   */
  private void roomCreateSuccess(Channel channel, Packet packet, Integer matchNumber, Integer gameMode) {
    long roomId = RoomUtils.getRandomRoomNo();
    // 监测房间ID是否存在
    boolean isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
    while (!isExists) {
      roomId = RoomUtils.getRandomRoomNo();
      isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
    }
    MatchRoom matchRoom = new MatchRoom();
    matchRoom.setRoomId(roomId);
    if (matchNumber == 0) {
      matchRoom.setPeopleNum(5);
    } else {
      matchRoom.setPeopleNum(7);
    }
    F20001.F200013S2C.Builder builder = F20001.F200013S2C.newBuilder();
    builder.setRoomId(matchRoom.getRoomId());
    builder.setResult(0);
    builder.setRoomId(roomId);
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_MATCH, MatchCmd.WHO_IS_SPY_MATCH,
            builder.build().toByteArray()));
    MatchManager.updFindSpyMatch(matchRoom, matchNumber, gameMode);
    I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
    joinInfo.setRoomId(matchRoom.getRoomId());
    joinInfo.setGameId(30051L);
    byte[] joinByte = joinInfo.build().toByteArray();
    this.redisUtils.setByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId, joinByte);
    I10001.RoomRecord.Builder roomRecord = I10001.RoomRecord.newBuilder();
    roomRecord.setGameId(30051L)
        .setRoomId(matchRoom.getRoomId())
        .setOpenWay(0)
        .setRoomType(matchNumber)
        .setGameMode(0)
        .setSpeakMode(gameMode)
        .setGameNumber(0)
        .setGameSession(0);
    byte[] roomByte = roomRecord.build().toByteArray();
    this.redisUtils.setByte(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId(), roomByte);
  }

  /**
   * TODO 匹配成功.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @param matchNumber [0-{4-6人} 1-{7-8人}]
   * @param gameMode [0-文字 1-语音]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/2 13:29
   * @update 2020/11/2 13:29
   */
  private void roomMatchSuccess(Channel channel, Packet packet, Integer matchNumber, Integer gameMode) {
    MatchRoom matchRoom;
    if (gameMode == 0) {
      if (matchNumber == 0) {
        matchRoom = MatchManager.MATCH_FIND_SPY_ONE_WORDS.get(0);
      } else {
        matchRoom = MatchManager.MATCH_FIND_SPY_TWO_WORDS.get(0);
      }
    } else {
      if (matchNumber == 0) {
        matchRoom = MatchManager.MATCH_FIND_SPY_ONE_SPECK.get(0);
      } else {
        matchRoom = MatchManager.MATCH_FIND_SPY_TWO_SPECK.get(0);
      }
    }
    matchRoom.setPeopleNum(matchRoom.getPeopleNum() - 1);
    if (matchRoom.getPeopleNum() == 0) {
      MatchManager.delFindSpyMatch(matchRoom.getRoomId(), matchNumber, gameMode);
    }
    F20001.F200013S2C.Builder builder = F20001.F200013S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_MATCH, MatchCmd.WHO_IS_SPY_MATCH,
            builder.setResult(0).setRoomId(matchRoom.getRoomId()).build().toByteArray()));
    I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
    joinInfo.setRoomId(matchRoom.getRoomId());
    joinInfo.setGameId(30051L);
    byte[] joinByte = joinInfo.build().toByteArray();
    this.redisUtils.setByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId, joinByte);
  }

  /**
   * TODO 检查残余.
   *
   * @param matchNumber [0-{4-6人} 1-{7-8人}]
   * @param gameMode [0-文字 1-语音]
   * @return [0-获取 1-创建]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/2 11:27
   * @update 2020/11/2 11:27
   */
  private int checkResidualAuthenticity(Integer matchNumber, Integer gameMode) {
    int index;
    if (gameMode == 0) {
      index = findSpyGamePlayText(matchNumber, gameMode);
    } else {
      index = findSpyGamePlayVoice(matchNumber, gameMode);
    }
    return index;
  }

  /**
   * TODO 文字场次.
   *
   * @param matchNumber [0-{4-6人} 1-{7-8人}]
   * @param gameMode [0-文字 1-语音]
   * @return [0-获取 1-创建]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/2 12:41
   * @update 2020/11/2 12:41
   */
  private int findSpyGamePlayText(Integer matchNumber, Integer gameMode) {
    MatchRoom matchRoom;
    boolean isExists;
    int index = 0;
    if (matchNumber == 0) {
      if (MatchManager.MATCH_FIND_SPY_ONE_WORDS.size() > 0) {
        matchRoom = MatchManager.MATCH_FIND_SPY_ONE_WORDS.get(0);
        isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
        while (!isExists) {
          cleanUpResidue(matchRoom.getRoomId(), matchNumber, gameMode);
          if (MatchManager.MATCH_FIND_SPY_ONE_WORDS.size() > 0) {
            matchRoom = MatchManager.MATCH_FIND_SPY_ONE_WORDS.get(0);
            isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
          } else {
            index = 1;
            isExists = true;
          }
        }
      } else {
        index = 1;
      }
    } else {
      if (MatchManager.MATCH_FIND_SPY_TWO_WORDS.size() > 0) {
        matchRoom = MatchManager.MATCH_FIND_SPY_TWO_WORDS.get(0);
        isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
        while (!isExists) {
          cleanUpResidue(matchRoom.getRoomId(), matchNumber, gameMode);
          if (MatchManager.MATCH_FIND_SPY_TWO_WORDS.size() > 0) {
            matchRoom = MatchManager.MATCH_FIND_SPY_TWO_WORDS.get(0);
            isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
          } else {
            index = 1;
            isExists = true;
          }
        }
      } else {
        index = 1;
      }
    }
    return index;
  }

  /**
   * TODO 语音场次.
   *
   * @param matchNumber [0-{4-6人} 1-{7-8人}]
   * @param gameMode [0-文字 1-语音]
   * @return [0-获取 1-创建]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/2 12:41
   * @update 2020/11/2 12:41
   */
  private int findSpyGamePlayVoice(Integer matchNumber, Integer gameMode) {
    MatchRoom matchRoom;
    boolean isExists;
    int index = 0;
    if (matchNumber == 0) {
      if (MatchManager.MATCH_FIND_SPY_ONE_SPECK.size() > 0) {
        matchRoom = MatchManager.MATCH_FIND_SPY_ONE_SPECK.get(0);
        isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
        while (!isExists) {
          cleanUpResidue(matchRoom.getRoomId(), matchNumber, gameMode);
          if (MatchManager.MATCH_FIND_SPY_ONE_SPECK.size() > 0) {
            matchRoom = MatchManager.MATCH_FIND_SPY_ONE_SPECK.get(0);
            isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
          } else {
            index = 1;
            isExists = true;
          }
        }
      } else {
        index = 1;
      }
    } else {
      if (MatchManager.MATCH_FIND_SPY_TWO_SPECK.size() > 0) {
        matchRoom = MatchManager.MATCH_FIND_SPY_TWO_SPECK.get(0);
        isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
        while (!isExists) {
          cleanUpResidue(matchRoom.getRoomId(), matchNumber, gameMode);
          if (MatchManager.MATCH_FIND_SPY_TWO_SPECK.size() > 0) {
            matchRoom = MatchManager.MATCH_FIND_SPY_TWO_SPECK.get(0);
            isExists = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId());
          } else {
            index = 1;
            isExists = true;
          }
        }
      } else {
        index = 1;
      }
    }
    return index;
  }

  /**
   * TODO 清理残留.
   *
   * @param roomId [房间ID]
   * @param matchNumber [0-{4-6人} 1-{7-8人}]
   * @param gameMode [0-文字 1-语音]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/2 13:04
   * @update 2020/11/2 13:04
   */
  private void cleanUpResidue(Long roomId, Integer matchNumber, Integer gameMode) {
    MatchManager.delFindSpyMatch(roomId, matchNumber, gameMode);
    AbstractActionHandler instance = this.handlerContext.getInstance(30251);
    instance.cleaning(roomId);
  }

  /**
   * TODO 快速开始.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/2 13:51
   * @update 2020/11/2 13:51
   */
  private void quickStart(Channel channel, Packet packet) {
    try {
      // 0-文字 1-语音
      boolean flag = RandomUtils.nextBoolean();
      if (flag) {
        // 0-{4-6人} 1-{7-8人}
        boolean mode = RandomUtils.nextBoolean();
        if (mode) {
          randomSearch(channel, packet, 0, 0);
        } else {
          randomSearch(channel, packet, 0, 1);
        }
      } else {
        // 0-{4-6人} 1-{7-8人}
        boolean mode = RandomUtils.nextBoolean();
        if (mode) {
          randomSearch(channel, packet, 1, 0);
        } else {
          randomSearch(channel, packet, 1, 1);
        }
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
   * @param matchNumber [0-{4-6人} 1-{7-8人}]
   * @param gameMode [0-文字 1-语音]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/2 13:55
   * @update 2020/11/2 13:55
   */
  private void randomSearch(Channel channel, Packet packet, Integer matchNumber, Integer gameMode) {
    int index = checkResidualAuthenticity(matchNumber, gameMode);
    if (index == 1) {
      quickStartCreateSuccess(channel, packet, matchNumber, gameMode);
    } else {
      quickStartMatchSuccess(channel, packet, matchNumber, gameMode);
    }
  }

  /**
   * TODO 创建成功.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @param matchNumber [0-{4-6人} 1-{7-8人}]
   * @param gameMode [0-文字 1-语音]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/2 13:52
   * @update 2020/11/2 13:52
   */
  private void quickStartCreateSuccess(Channel channel, Packet packet, Integer matchNumber, Integer gameMode) {
    long roomId = RoomUtils.getRandomRoomNo();
    // 监测房间ID是否存在
    boolean isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
    while (!isExists) {
      roomId = RoomUtils.getRandomRoomNo();
      isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
    }
    MatchRoom matchRoom = new MatchRoom();
    matchRoom.setRoomId(roomId);
    if (matchNumber == 0) {
      matchRoom.setPeopleNum(5);
    } else {
      matchRoom.setPeopleNum(7);
    }
    F20001.F200016S2C.Builder builder = F20001.F200016S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_MATCH, MatchCmd.QUICK_START,
            builder.setRoomId(roomId).build().toByteArray()));
    MatchManager.updFindSpyMatch(matchRoom, matchNumber, gameMode);
    I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
    joinInfo.setRoomId(matchRoom.getRoomId());
    joinInfo.setGameId(30051L);
    byte[] joinByte = joinInfo.build().toByteArray();
    this.redisUtils.setByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId, joinByte);
    I10001.RoomRecord.Builder roomRecord = I10001.RoomRecord.newBuilder();
    roomRecord.setGameId(30051L)
        .setRoomId(matchRoom.getRoomId())
        .setOpenWay(0)
        .setRoomType(matchNumber)
        .setGameMode(0)
        .setSpeakMode(gameMode)
        .setGameNumber(0)
        .setGameSession(0);
    byte[] roomByte = roomRecord.build().toByteArray();
    this.redisUtils.setByte(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId, roomByte);
  }

  /**
   * TODO 匹配成功.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @param matchNumber [0-{4-6人} 1-{7-8人}]
   * @param gameMode [0-文字 1-语音]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/2 13:53
   * @update 2020/11/2 13:53
   */
  private void quickStartMatchSuccess(Channel channel, Packet packet, Integer matchNumber, Integer gameMode) {
    MatchRoom matchRoom;
    if (gameMode == 0) {
      if (matchNumber == 0) {
        matchRoom = MatchManager.MATCH_FIND_SPY_ONE_WORDS.get(0);
      } else {
        matchRoom = MatchManager.MATCH_FIND_SPY_TWO_WORDS.get(0);
      }
    } else {
      if (matchNumber == 0) {
        matchRoom = MatchManager.MATCH_FIND_SPY_ONE_SPECK.get(0);
      } else {
        matchRoom = MatchManager.MATCH_FIND_SPY_TWO_SPECK.get(0);
      }
    }
    matchRoom.setPeopleNum(matchRoom.getPeopleNum() - 1);
    if (matchRoom.getPeopleNum() == 0) {
      MatchManager.delFindSpyMatch(matchRoom.getRoomId(), matchNumber, gameMode);
    }
    F20001.F200016S2C.Builder builder = F20001.F200016S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_MATCH, MatchCmd.QUICK_START,
            builder.setRoomId(matchRoom.getRoomId()).build().toByteArray()));
    I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
    joinInfo.setRoomId(matchRoom.getRoomId());
    joinInfo.setGameId(30051L);
    byte[] joinByte = joinInfo.build().toByteArray();
    this.redisUtils.setByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId, joinByte);
  }

  /**
   * TODO 关闭处理.
   *
   * @param userId [用户ID]
   * @param attachId [附属ID(或房间Id)]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/19 21:09
   * @update 2020/9/15 14:56
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
   * @create 2020/9/15 15:54
   * @update 2020/9/15 15:54
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
   * @create 2020/9/15 15:55
   * @update 2020/9/15 15:55
   */
  @Override
  public void joinRobot(Long roomId, List<Long> playerIds) {
    // MATCH-EMPTY-METHOD
  }

}
