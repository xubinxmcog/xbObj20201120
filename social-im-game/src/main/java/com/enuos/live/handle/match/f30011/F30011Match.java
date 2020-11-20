package com.enuos.live.handle.match.f30011;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.utils.annotation.AbstractAction;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import com.enuos.live.codec.Packet;
import com.enuos.live.constants.GameKey;
import com.enuos.live.pojo.GameRobot;
import com.enuos.live.pojo.MatchUser;
import com.enuos.live.handle.match.MatchCmd;
import com.enuos.live.proto.f20001msg.F20001;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.proto.i10001msg.I10001.PlayerInfo;
import com.enuos.live.rest.GameRemote;
import com.enuos.live.result.Result;
import com.enuos.live.server.HandlerContext;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.manager.RobotManager;
import com.enuos.live.utils.StringUtils;
import com.enuos.live.utils.RoomUtils;
import com.enuos.live.utils.JsonUtils;
import com.enuos.live.manager.LoggerManager;
import com.enuos.live.utils.RedisUtils;
import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.stereotype.Component;

/**
 * TODO 斗兽战纪.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V1.0.0
 * @since 2020/5/27 12:47
 */

@Component
@AbstractAction(cmd = 100011)
public class F30011Match extends AbstractActionHandler {

  /**
   * 游戏编码.
   */
  private static final int GAME_CODE = 30011;
  /**
   * 超时时间.
   */
  private static final long MAX_MILLIS = 10000;
  /**
   * 匹配玩家列表.
   */
  private static CopyOnWriteArrayList<MatchUser> MATCH_PLAYER = Lists.newCopyOnWriteArrayList();
  /**
   * 创建匹配的线程.
   */
  private static ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1,
      new BasicThreadFactory.Builder().namingPattern("robotUp-30011-%d").daemon(true).build());

  // 初始化计时器 延时1秒启动，每5秒扫描一次
  {
    executor.scheduleWithFixedDelay(this::turnOnRobot, 1, 4, TimeUnit.SECONDS);
  }

  @Resource
  private GameRemote gameRemote;
  @Resource
  private RedisUtils redisUtils;
  @Resource
  private HandlerContext handlerContext;

  /**
   * TODO 匹配玩家.
   *
   * @param channel 快速通道
   * @param packet 数据包
   * @author WangCaiWen
   * @since 2020/5/27 - 2020/5/29
   */
  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      // 是否存在用户信息
      if (this.redisUtils.hasKey(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId)) {
        List<MatchUser> matchUserList = Lists.newLinkedList();
        List<I10001.PlayerInfo> playerInfoList = Lists.newLinkedList();
        MatchUser newMatchUser = new MatchUser();
        newMatchUser.setChannel(channel);
        newMatchUser.setUserId(packet.userId);
        newMatchUser.setStartMatchTime(System.currentTimeMillis());
        // 当前用户信息byte
        byte[] nowBytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
        I10001.PlayerInfo nowPlayerInfo = I10001.PlayerInfo.parseFrom(nowBytes);
        if (MATCH_PLAYER.size() > 0) {
          long roomId = RoomUtils.getRandomRoomNo();
          // 监测房间ID是否存在
          boolean isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
          while (!isExists) {
            roomId = RoomUtils.getRandomRoomNo();
            isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
          }
          MatchUser targetMatchUser = MATCH_PLAYER.get(0);
          // 是否存在用户信息
          if (this.redisUtils.hasKey(GameKey.KEY_GAME_USER_LOGIN.getName() + targetMatchUser.getUserId())) {
            targetMatchUser = MATCH_PLAYER.remove(0);
            matchUserList.add(newMatchUser);
            matchUserList.add(targetMatchUser);
            playerInfoList.add(nowPlayerInfo);
            // 目标用户信息byte
            byte[] targetBytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + targetMatchUser.getUserId());
            I10001.PlayerInfo targetPlayerInfo = I10001.PlayerInfo.parseFrom(targetBytes);
            playerInfoList.add(targetPlayerInfo);
            F20001.F200010S2C.Builder response = F20001.F200010S2C.newBuilder();
            response.setRoomId(roomId).setResult(0);
            F20001.ESMatchPlayer.Builder playerInfo;
            for (PlayerInfo player : playerInfoList) {
              playerInfo = F20001.ESMatchPlayer.newBuilder();
              playerInfo.setUserId(player.getUserId());
              playerInfo.setThumbIconURL(player.getIconUrl());
              playerInfo.setNickName(player.getNickName());
              playerInfo.setSex(player.getSex());
              playerInfo.setLevel(player.getLevel());
              response.addMatchPlayer(playerInfo);
            }
            I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
            joinInfo.setRoomId(response.getRoomId());
            joinInfo.setGameId(GAME_CODE);
            byte[] joinByte = joinInfo.build().toByteArray();
            matchUserList.forEach(player -> {
              player.getChannel().writeAndFlush(
                  new Packet(ActionCmd.GAME_MATCH, MatchCmd.UNIVERSAL_MATCH,
                      response.build().toByteArray()));
              this.redisUtils.setByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + player.getUserId(), joinByte);
              // 移除匹配标记
              this.redisUtils.del(GameKey.KEY_GAME_USER_MATCH.getName() + player.getUserId()); });
            // 房间信息
            I10001.RoomRecord.Builder builder = I10001.RoomRecord.newBuilder();
            builder.setGameId(GAME_CODE).setRoomId(response.getRoomId()).setOpenWay(0).setRoomType(0)
                .setGameMode(0).setSpeakMode(1).setGameNumber(0).setGameSession(0);
            byte[] roomByte = builder.build().toByteArray();
            this.redisUtils.setByte(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId, roomByte);
          } else {
            // 移除匹配标记
            this.redisUtils.del(GameKey.KEY_GAME_USER_MATCH.getName() + targetMatchUser.getUserId());
            F20001.F200010S2C.Builder builder = F20001.F200010S2C.newBuilder();
            targetMatchUser.getChannel().writeAndFlush(
                new Packet(ActionCmd.GAME_MATCH, MatchCmd.UNIVERSAL_MATCH,
                    builder.setResult(1).setRoomId(0).build().toByteArray()));
            MATCH_PLAYER.remove(0);
            joinMatch(newMatchUser);
          }
        } else {
          joinMatch(newMatchUser);
        }
      } else {
        F20001.F200010S2C.Builder builder = F20001.F200010S2C.newBuilder();
        channel.writeAndFlush(new Packet(ActionCmd.GAME_MATCH, MatchCmd.UNIVERSAL_MATCH,
                builder.setResult(1).setRoomId(0).build().toByteArray()));
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 加入匹配.
   *
   * @param matchUser 匹配玩家
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/11 10:18
   * @update 2020/9/11 10:18
   */
  private void joinMatch(MatchUser matchUser) {
    // 添加匹配标记
    if (this.redisUtils.hasKey(GameKey.KEY_GAME_USER_MATCH.getName() + matchUser.getUserId())) {
      this.redisUtils.del(GameKey.KEY_GAME_USER_MATCH.getName() + matchUser.getUserId());
      this.redisUtils.set(GameKey.KEY_GAME_USER_MATCH.getName() + matchUser.getUserId(), GAME_CODE);
    } else {
      this.redisUtils.set(GameKey.KEY_GAME_USER_MATCH.getName() + matchUser.getUserId(), GAME_CODE);
    }
    MATCH_PLAYER.add(matchUser);
  }

  /**
   * TODO 其他处理.
   *
   * @param userId 用户ID
   * @param attachId 附属ID
   * @author WangCaiWen
   * @since 2020/5/27 - 2020/5/27
   */
  @Override
  public void shutOff(Long userId, Long attachId) {
    try {
      MATCH_PLAYER.removeIf(matchUser -> Objects.equals(matchUser.getUserId(), userId));
      this.redisUtils.del(GameKey.KEY_GAME_USER_MATCH.getName() + userId);
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  @Override
  public void cleaning(Long roomId) {
    // MATCH-EMPTY-METHOD
  }

  @Override
  public void joinRobot(Long roomId, List<Long> playerIds) {
    // MATCH-EMPTY-METHOD
  }

  /**
   * TODO 开启陪玩.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @create 2020/9/10 16:31
   * @update 2020/9/10 16:31
   */
  private void turnOnRobot() {
    try {
      if (MATCH_PLAYER.size() == 1) {
        F20001.F200010S2C.Builder builder = F20001.F200010S2C.newBuilder();
        MatchUser matchUser = MATCH_PLAYER.get(0);
        if ((System.currentTimeMillis() - matchUser.getStartMatchTime()) > MAX_MILLIS) {
          Result result = this.gameRemote.getRandomGameRobot(1);
          if (Objects.nonNull(result)) {
            // 是否存在用户信息
            if (this.redisUtils.hasKey(GameKey.KEY_GAME_USER_LOGIN.getName() + matchUser.getUserId())) {
              matchUser = MATCH_PLAYER.remove(0);
              long roomId = RoomUtils.getRandomRoomNo();
              boolean isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
              while (!isExists) {
                roomId = RoomUtils.getRandomRoomNo();
                isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
              }
              byte[] bytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + matchUser.getUserId());
              I10001.PlayerInfo targetPlayer = I10001.PlayerInfo.parseFrom(bytes);
              F20001.F200010S2C.Builder response = F20001.F200010S2C.newBuilder();
              response.setRoomId(roomId).setResult(0);
              F20001.ESMatchPlayer.Builder playerInfo = F20001.ESMatchPlayer.newBuilder();
              playerInfo.setUserId(targetPlayer.getUserId());
              playerInfo.setThumbIconURL(targetPlayer.getIconUrl());
              playerInfo.setNickName(targetPlayer.getNickName());
              playerInfo.setSex(targetPlayer.getSex());
              playerInfo.setLevel(targetPlayer.getLevel());
              response.addMatchPlayer(playerInfo);
              playerInfo = F20001.ESMatchPlayer.newBuilder();
              List<Map<String, Object>> gameRobotList = JsonUtils.listMap(result.getData());
              Map<String, Object> gameRobotMap = gameRobotList.get(0);
              playerInfo.setUserId(((Number) gameRobotMap.get("robotId")).longValue());
              playerInfo.setThumbIconURL(StringUtils.nvl(gameRobotMap.get("robotAvatar")));
              playerInfo.setNickName(StringUtils.nvl(gameRobotMap.get("robotName")));
              playerInfo.setSex(((Number) gameRobotMap.get("robotSex")).intValue());
              playerInfo.setLevel(ThreadLocalRandom.current().nextInt(6) + 1);
              response.addMatchPlayer(playerInfo);
              matchUser.getChannel().writeAndFlush(
                  new Packet(ActionCmd.GAME_MATCH, MatchCmd.UNIVERSAL_MATCH,
                      response.build().toByteArray()));
              I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
              joinInfo.setRoomId(response.getRoomId());
              joinInfo.setGameId(GAME_CODE);
              byte[] joinByte = joinInfo.build().toByteArray();
              this.redisUtils.setByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + matchUser.getUserId(), joinByte);
              // 移除匹配标记
              this.redisUtils.del(GameKey.KEY_GAME_USER_MATCH.getName() + matchUser.getUserId());
              GameRobot gameRobot = new GameRobot();
              gameRobot.setRobotId(((Number) gameRobotMap.get("robotId")).longValue());
              gameRobot.setRobotSex(((Number) gameRobotMap.get("robotSex")).intValue());
              gameRobot.setRobotName(StringUtils.nvl(gameRobotMap.get("robotName")));
              gameRobot.setRobotAvatar(StringUtils.nvl(gameRobotMap.get("robotAvatar")));
              RobotManager.addGameRobot(gameRobot);
              // 房间信息
              I10001.RoomRecord.Builder roomRecord = I10001.RoomRecord.newBuilder();
              roomRecord.setGameId(GAME_CODE).setRoomId(response.getRoomId()).setOpenWay(0).setRoomType(0)
                  .setGameMode(0).setSpeakMode(1).setGameNumber(0).setGameSession(0);
              byte[] roomByte = roomRecord.build().toByteArray();
              this.redisUtils.setByte(GameKey.KEY_GAME_ROOM_RECORD.getName() + response.getRoomId(), roomByte);
              AbstractActionHandler instance = this.handlerContext.getInstance(GAME_CODE);
              List<Long> playerIds = Lists.newLinkedList();
              playerIds.add(gameRobot.getRobotId());
              instance.joinRobot(roomId, playerIds);
            } else {
              matchUser = MATCH_PLAYER.remove(0);
              matchUser.getChannel().writeAndFlush(
                  new Packet(ActionCmd.GAME_MATCH, MatchCmd.UNIVERSAL_MATCH,
                      builder.setResult(1).setRoomId(0).build().toByteArray()));
            }
          } else {
            matchUser.getChannel().writeAndFlush(
                new Packet(ActionCmd.GAME_MATCH, MatchCmd.UNIVERSAL_MATCH,
                    builder.setResult(1).setRoomId(0).build().toByteArray()));
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  //  private void turnOnRobot() {
  //    try {
  //      if (MATCH_PLAYER.size() == 1) {
  //        F20001.F200010S2C.Builder builder = F20001.F200010S2C.newBuilder();
  //        MatchUser matchUser = MATCH_PLAYER.get(0);
  //        if ((System.currentTimeMillis() - matchUser.getStartMatchTime()) > MAX_MILLIS) {
  //          matchUser = MATCH_PLAYER.remove(0);
  //          matchUser.getChannel().writeAndFlush(
  //              new Packet(ActionCmd.GAME_MATCH, MatchCmd.UNIVERSAL_MATCH,
  //                  builder.setResult(1).setRoomId(0).build().toByteArray()));
  //        }
  //      }
  //    } catch (Exception e) {
  //      LoggerUtils.error("<== 30011 [斗兽战纪.开启陪玩] ERROR: [{}]", e.getMessage());
  //      LoggerUtils.error(ExceptionUtil.getStackTrace(e));
  //    }
  //  }

}
