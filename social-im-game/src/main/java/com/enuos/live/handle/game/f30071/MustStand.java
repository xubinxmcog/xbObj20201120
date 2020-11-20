package com.enuos.live.handle.game.f30071;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.codec.Packet;
import com.enuos.live.constants.GameKey;
import com.enuos.live.manager.AchievementEnum;
import com.enuos.live.manager.ActivityEnum;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.rest.ActivityRemote;
import com.enuos.live.manager.MemManager;
import com.enuos.live.manager.GroupManager;
import com.enuos.live.manager.TaskEnum;
import com.enuos.live.utils.annotation.AbstractAction;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import com.enuos.live.manager.ChatManager;
import com.enuos.live.channel.SoftChannel;
import com.enuos.live.proto.f30071msg.F30071;
import com.enuos.live.rest.GameRemote;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.task.TimerEventLoop;
import com.enuos.live.utils.ExceptionUtil;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * TODO 一站到底.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/5/15 14:49
 */

@Component
@AbstractAction(cmd = ActionCmd.GAME_MUST_STAND)
public class MustStand extends AbstractActionHandler {

  /** 房间游戏数据. */
  private static ConcurrentHashMap<Long, MustStandRoom> GAME_DATA = new ConcurrentHashMap<>();

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
   * @create 2020/7/8 21:08
   * @update 2020/9/23 20:57
   */
  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      switch (packet.child) {
        case MustStandCmd.ENTER_ROOM:
          enterRoom(channel, packet);
          break;
        case MustStandCmd.DOUBLE_SCORE:
          doubleScore(channel, packet);
          break;
        case MustStandCmd.SELECT_ANSWER:
          selectAnswer(channel, packet);
          break;
        case MustStandCmd.SUBMIT_PROBLEM:
          submitProblem(channel, packet);
          break;
        case MustStandCmd.LEAVE_ROOM:
          closeGame(channel, packet);
          break;
        default:
          LoggerManager.warn("[GAME 30071 HANDLE] CHILD ERROR: [{}]", packet.child);
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
   * @create 2020/7/8 21:08
   * @update 2020/9/30 18:11
   */
  @Override
  public void shutOff(Long userId, Long attachId) {
    try {
      MustStandRoom roomInfo = GAME_DATA.get(attachId);
      if (Objects.nonNull(roomInfo)) {
        MustStandPlayer checkPlayer = roomInfo.getPlayerInfo(userId);
        if (Objects.nonNull(checkPlayer)) {
          if (roomInfo.getRoomStatus() == 1) {
            roomInfo.destroy();
            List<MustStandPlayer> playerList = roomInfo.getPartakerList();
            playerList.forEach(player -> {
              // 每日任务.玩3局一站到底
              Map<String, Object> taskInfo = Maps.newHashMap();
              taskInfo.put("userId", player.getUserId());
              taskInfo.put("code", TaskEnum.PGT0008.getCode());
              taskInfo.put("progress", 1);
              taskInfo.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo); });
            achievementTask(roomInfo.getRoomId());
            roomInfo.leaveGame(userId);
            if (roomInfo.getPartakerList().size() > 0) {
              MustStandPlayer player = roomInfo.getPartakerList().get(0);
              F30071.F300717S2C.Builder builder = F30071.F300717S2C.newBuilder();
              LocalDateTime startTime = roomInfo.getStartTime();
              LocalDateTime newTime = LocalDateTime.now();
              Duration duration = Duration.between(startTime, newTime);
              int durations = (int) duration.getSeconds();
              builder.setUserID(player.getUserId());
              if (durations > MustStandAssets.getInt(MustStandAssets.FINISH_TIME)) {
                if (MemManager.isExists(player.getUserId())) {
                  builder.setAddExp(gainExperience(player.getUserId(), 4));
                  builder.setIsDouble(1);
                } else {
                  builder.setAddExp(gainExperience(player.getUserId(), 2));
                }
                builder.setAddGold(10);
              } else {
                if (MemManager.isExists(player.getUserId())) {
                  builder.setIsDouble(1);
                }
                builder.setAddExp(0).setAddGold(0);
              }
              player.sendPacket(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.GAME_FINISH,
                  builder.build().toByteArray()));
              // 每日任务.赢1局一站到底
              Map<String, Object> taskInfo = Maps.newHashMap();
              taskInfo.put("userId", builder.getUserID());
              taskInfo.put("code", TaskEnum.PGT0013.getCode());
              taskInfo.put("progress", 1);
              taskInfo.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo);
              clearData(roomInfo.getRoomId());
            }
          } else {
            if (roomInfo.getPartakerList().size() == 1) {
              roomInfo.destroy();
              clearData(roomInfo.getRoomId());
            } else {
              if (roomInfo.getTimeOutMap().containsKey((int) MustStandCmd.START_GAME)) {
                roomInfo.cancelTimeOut((int) MustStandCmd.START_GAME);
              }
              roomInfo.leaveGame(userId);
              MustStandPlayer player = roomInfo.getPartakerList().get(0);
              F30071.F300717S2C.Builder builder = F30071.F300717S2C.newBuilder();
              builder.setUserID(player.getUserId()).setAddExp(0).setAddGold(0);
              if (MemManager.isExists(player.getUserId())) {
                builder.setIsDouble(1);
              }
              player.sendPacket(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.GAME_FINISH,
                  builder.build().toByteArray()));
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
   * @create 2020/10/14 17:09
   * @update 2020/10/14 17:09
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
   * @create 2020/10/14 17:09
   * @update 2020/10/14 17:09
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
   * @create 2020/10/14 17:10
   * @update 2020/10/14 17:10
   */
  private void enterRoom(Channel channel, Packet packet) {
    try {
      boolean isTestUser = this.redisUtils.hasKey(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
      boolean isPlayer = this.redisUtils.hasKey(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
      if (isTestUser || isPlayer) {
        closeLoading(packet.userId);
        boolean checkRoom = this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + packet.roomId);
        boolean checkTest = (packet.roomId == MustStandAssets.getLong(MustStandAssets.TEST_ID));
        if (checkRoom || checkTest) {
          MustStandRoom roomInfo = GAME_DATA.get(packet.roomId);
          if (Objects.nonNull(roomInfo)) {
            MustStandPlayer player = roomInfo.getPlayerInfo(packet.userId);
            if (roomInfo.getRoomStatus() == 1) {
              if (Objects.nonNull(player)) {
                // 断线重连
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
                if (roomInfo.getTimeOutMap().containsKey((int) MustStandCmd.WAIT_PLAYER)) {
                  roomInfo.cancelTimeOut((int) MustStandCmd.WAIT_PLAYER);
                }
                refreshData(channel, packet);
                byte[] bytes;
                if (isTestUser) {
                  bytes = this.redisUtils.getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
                } else {
                  bytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
                }
                I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
                roomInfo.enterRoom(channel, playerInfo);
                joinMustStandRoom(packet);
              }
            }
          } else {
            register(TimerEventLoop.timeGroup.next());
            List<Map<String, Object>> problemList = Lists.newLinkedList();
            Result result = this.gameRemote.getMustStandProblem();
            if (Objects.nonNull(result)) {
              problemList = result.getCode().equals(0) ? JsonUtils.listMap(result.getData()) : null;
            }
            if (CollectionUtils.isNotEmpty(problemList)) {
              refreshData(channel, packet);
              GAME_DATA.computeIfAbsent(packet.roomId, key -> new MustStandRoom(packet.roomId));
              roomInfo = GAME_DATA.get(packet.roomId);
              roomInfo.addGameProblem(problemList);
              byte[] bytes;
              if (isTestUser) {
                bytes = this.redisUtils.getByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
              } else {
                bytes = this.redisUtils.getByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
              }
              I10001.PlayerInfo playerInfo = I10001.PlayerInfo.parseFrom(bytes);
              roomInfo.enterRoom(channel, playerInfo);
              joinMustStandRoom(packet);
            } else {
              F30071.F30071S2C.Builder builder = F30071.F30071S2C.newBuilder();
              channel.writeAndFlush(
                  new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.ENTER_ROOM,
                      builder.setResult(3).build().toByteArray()));
              return;
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
   * TODO 玩家信息.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:11
   * @update 2020/10/14 17:11
   */
  private void pushPlayerInfo(Channel channel, Packet packet) {
    try {
      MustStandRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        F30071.F30071S2C.Builder builder = F30071.F30071S2C.newBuilder();
        List<MustStandPlayer> playerList = roomInfo.getPartakerList();
        if (CollectionUtils.isNotEmpty(playerList)) {
          F30071.PlayerInfo.Builder playerInfo;
          for (MustStandPlayer partaker : playerList) {
            playerInfo = F30071.PlayerInfo.newBuilder();
            playerInfo.setNick(partaker.getUserName());
            playerInfo.setUserID(partaker.getUserId());
            playerInfo.setUrl(partaker.getUserIcon());
            playerInfo.setSex(partaker.getUserSex());
            playerInfo.setRed(partaker.getIdentity());
            builder.addPlayer(playerInfo);
          }
          builder.setResult(0);
          GroupManager.sendPacketToGroup(
              new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.ENTER_ROOM,
                  builder.build().toByteArray()), roomInfo.getRoomId());
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
   * TODO 进行加倍. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:11
   * @update 2020/10/14 17:11
   */
  private void doubleScore(Channel channel, Packet packet) {
    try {
      MustStandRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        MustStandPlayer player = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(player)) {
          if (roomInfo.getTimeOutMap().containsKey((int) MustStandCmd.SHOW_PROBLEM)) {
            if (player.getUseDouble() == 0) {
              player.useDoubleCard();
              F30071.F300712S2C.Builder builder = F30071.F300712S2C.newBuilder();
              builder.setDoubleUserID(packet.userId);
              GroupManager.sendPacketToGroup(
                  new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.DOUBLE_SCORE,
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
   * TODO 选择答案. 
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:12
   * @update 2020/10/14 17:12
   */
  private void selectAnswer(Channel channel, Packet packet) {
    try {
      MustStandRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        MustStandPlayer player = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(player)) {
          boolean roundGame = !roomInfo.getTimeOutMap().containsKey((int) MustStandCmd.ROUND_FINISH);
          boolean endGame = !roomInfo.getTimeOutMap().containsKey((int) MustStandCmd.GAME_FINISH);
          if (roundGame && endGame) {
            // 初始情况下才能选择
            if (player.getSelectIndex() == 0) {
              F30071.F300715C2S request = F30071.F300715C2S.parseFrom(packet.bytes);
              F30071.F300715S2C.Builder builder = F30071.F300715S2C.newBuilder();
              int seconds = player.selectAnswer(roomInfo.getRoundTime(), request.getAnswerIndex());
              if (roomInfo.actionExamine()) {
                List<MustStandPlayer> playerList = roomInfo.getPartakerList();
                if (CollectionUtils.isNotEmpty(playerList)) {
                  F30071.SelectAnswer.Builder selectAnswer;
                  for (MustStandPlayer standPartaker : playerList) {
                    selectAnswer = F30071.SelectAnswer.newBuilder();
                    selectAnswer.setActionUserID(standPartaker.getUserId());
                    selectAnswer.setActionTime(standPartaker.getActionTime());
                    selectAnswer.setIconPath(standPartaker.getUserIcon());
                    selectAnswer.setAnswerIndex(standPartaker.getSelectIndex());
                    builder.addSelectAnswer(selectAnswer);
                  }
                  GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.SELECT_ANSWER,
                      builder.build().toByteArray()), roomInfo.getRoomId());
                  roundFinish(packet.roomId);
                }
              } else {
                F30071.SelectAnswer.Builder selectAnswer = F30071.SelectAnswer.newBuilder();
                selectAnswer.setActionUserID(packet.userId);
                selectAnswer.setActionTime(seconds);
                selectAnswer.setIconPath(player.getUserIcon());
                selectAnswer.setAnswerIndex(request.getAnswerIndex());
                builder.addSelectAnswer(selectAnswer);
                channel.writeAndFlush(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.SELECT_ANSWER,
                    builder.build().toByteArray()));
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
   * @create 2020/10/14 17:13
   * @update 2020/10/14 17:13
   */
  private void disconnected(Channel channel, Packet packet) {
    try {
      MustStandRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (roomInfo != null) {
        MustStandPlayer player = roomInfo.getPlayerInfo(packet.userId);
        player.setChannel(channel);
        if (roomInfo.getTimeOutMap().containsKey((int) MustStandCmd.SHOW_PROBLEM)) {
          F30071.F300718S2C.Builder builder = F30071.F300718S2C.newBuilder();
          List<MustStandPlayer> partakerList = roomInfo.getPartakerList();
          F30071.PlayerInfo.Builder playerInfo;
          for (MustStandPlayer partaker : partakerList) {
            playerInfo = F30071.PlayerInfo.newBuilder();
            playerInfo.setNick(partaker.getUserName());
            playerInfo.setUserID(partaker.getUserId());
            playerInfo.setUrl(partaker.getUserIcon());
            playerInfo.setSex(partaker.getUserSex());
            playerInfo.setRed(partaker.getIdentity());
            builder.addBreakLineInfo(playerInfo);
          }
          builder.setCurrtTopic(roomInfo.getRoomRound());
          builder.setAllCurrtTopic(10);
          F30071.F300711S2C.Builder startInfo = F30071.F300711S2C.newBuilder();
          String label = roomInfo.getProblemMap().get(roomInfo.getRoomRound()).getLabel();
          startInfo.setQuestionType(label);
          startInfo.setGameRound(roomInfo.getRoomRound());
          startInfo.setAllGameRound(10);
          startInfo.setLastTimes(3);
          startInfo.setIsDoubleCard(1);
          startInfo.setDoubleCardNum(3);
          builder.setStartInfo(startInfo);
          channel.writeAndFlush(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.DISCONNECTED,
              builder.build().toByteArray()));
          return;
        }
        if (roomInfo.getTimeOutMap().containsKey((int) MustStandCmd.SELECT_ANSWER)) {
          F30071.F300718S2C.Builder builder = F30071.F300718S2C.newBuilder();
          List<MustStandPlayer> partakerList = roomInfo.getPartakerList();
          F30071.PlayerInfo.Builder playerInfo;
          for (MustStandPlayer partaker : partakerList) {
            playerInfo = F30071.PlayerInfo.newBuilder();
            playerInfo.setNick(partaker.getUserName());
            playerInfo.setUserID(partaker.getUserId());
            playerInfo.setUrl(partaker.getUserIcon());
            playerInfo.setSex(partaker.getUserSex());
            playerInfo.setRed(partaker.getIdentity());
            if (partaker.getUserScore() > 0) {
              playerInfo.setScore(partaker.getUserScore());
            }
            if (partaker.getUseDouble() == 1) {
              playerInfo.setIsDoubleCard(0);
            } else {
              playerInfo.setIsDoubleCard(1);
            }
            builder.addBreakLineInfo(playerInfo);
          }
          String problem = roomInfo.getProblemMap().get(roomInfo.getRoomRound()).getTitle();
          builder.setTopicDescribe(problem);
          List<String> answersList = new ArrayList<>(
              roomInfo.getProblemMap().get(roomInfo.getRoomRound()).getProblemMap().values());
          int index = 1;
          F30071.AnswerList.Builder answersBuilder;
          for (String answers : answersList) {
            answersBuilder = F30071.AnswerList.newBuilder();
            answersBuilder.setAnswerIndex(index);
            answersBuilder.setAnswerDescribe(answers);
            builder.addAnswers(answersBuilder);
            index++;
          }
          F30071.F300715S2C.Builder playerSelect = F30071.F300715S2C.newBuilder();
          F30071.SelectAnswer.Builder selectAnswer;
          for (MustStandPlayer partaker : partakerList) {
            if (partaker.getSelectIndex() > 0) {
              selectAnswer = F30071.SelectAnswer.newBuilder();
              selectAnswer.setActionUserID(partaker.getUserId());
              selectAnswer.setActionTime(partaker.getActionTime());
              selectAnswer.setIconPath(partaker.getUserIcon());
              selectAnswer.setAnswerIndex(partaker.getSelectIndex());
              playerSelect.addSelectAnswer(selectAnswer);
            }
          }
          builder.setPlayerSelect(playerSelect);
          LocalDateTime udt = roomInfo.getRoundTime();
          LocalDateTime nds = LocalDateTime.now();
          Duration duration = Duration.between(nds, udt);
          int second = Math.toIntExact(duration.getSeconds());
          builder.setLastTimes(second);
          builder.setCurrtTopic(roomInfo.getRoomRound());
          builder.setAllCurrtTopic(10);
          channel.writeAndFlush(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.DISCONNECTED,
              builder.build().toByteArray()));
          return;
        }
        F30071.F300718S2C.Builder builder = F30071.F300718S2C.newBuilder();
        List<MustStandPlayer> partakerList = roomInfo.getPartakerList();
        F30071.PlayerInfo.Builder playerInfo;
        for (MustStandPlayer partaker : partakerList) {
          playerInfo = F30071.PlayerInfo.newBuilder();
          playerInfo.setNick(partaker.getUserName());
          playerInfo.setUserID(partaker.getUserId());
          playerInfo.setUrl(partaker.getUserIcon());
          playerInfo.setSex(partaker.getUserSex());
          playerInfo.setRed(partaker.getIdentity());
          builder.addBreakLineInfo(playerInfo);
        }
        builder.setCurrtTopic(roomInfo.getRoomRound());
        builder.setAllCurrtTopic(10);
        channel.writeAndFlush(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.DISCONNECTED,
            builder.build().toByteArray()));
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 题目报错.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:13
   * @update 2020/10/14 17:13
   */
  private void submitProblem(Channel channel, Packet packet) {
    try {
      MustStandRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        MustStandPlayer player = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(player)) {
          F30071.F300719S2C.Builder builder = F30071.F300719S2C.newBuilder();
          builder.setResult(0);
          channel.writeAndFlush(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.SUBMIT_PROBLEM,
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
   * TODO 离开游戏.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:13
   * @update 2020/10/14 17:13
   */
  private void closeGame(Channel channel, Packet packet) {
    try {
      MustStandRoom roomInfo = GAME_DATA.get(packet.roomId);
      if (Objects.nonNull(roomInfo)) {
        MustStandPlayer checkPlayer = roomInfo.getPlayerInfo(packet.userId);
        if (Objects.nonNull(checkPlayer)) {
          if (roomInfo.getRoomStatus() == 1) {
            roomInfo.destroy();
            List<MustStandPlayer> playerList = roomInfo.getPartakerList();
            playerList.forEach(player -> {
              // 每日任务.玩3局一站到底
              Map<String, Object> taskInfo = Maps.newHashMap();
              taskInfo.put("userId", player.getUserId());
              taskInfo.put("code", TaskEnum.PGT0008.getCode());
              taskInfo.put("progress", 1);
              taskInfo.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo); });
            achievementTask(roomInfo.getRoomId());
            roomInfo.leaveGame(packet.userId);
            closePage(channel, packet);
            if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
              this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
            }
            if (roomInfo.getPartakerList().size() > 0) {
              MustStandPlayer player = roomInfo.getPartakerList().get(0);
              F30071.F300717S2C.Builder builder = F30071.F300717S2C.newBuilder();
              LocalDateTime startTime = roomInfo.getStartTime();
              LocalDateTime newTime = LocalDateTime.now();
              Duration duration = Duration.between(startTime, newTime);
              int durations = (int) duration.getSeconds();
              builder.setUserID(player.getUserId());
              if (durations > MustStandAssets.getInt(MustStandAssets.FINISH_TIME)) {
                if (MemManager.isExists(player.getUserId())) {
                  builder.setAddExp(gainExperience(player.getUserId(), 4));
                  builder.setIsDouble(1);
                } else {
                  builder.setAddExp(gainExperience(player.getUserId(), 2));
                }
                builder.setAddGold(10);
              } else {
                if (MemManager.isExists(player.getUserId())) {
                  builder.setIsDouble(1);
                }
                builder.setAddExp(0).setAddGold(0);
              }
              player.sendPacket(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.GAME_FINISH,
                  builder.build().toByteArray()));
              // 每日任务.赢1局一站到底
              Map<String, Object> taskInfo = Maps.newHashMap();
              taskInfo.put("userId", builder.getUserID());
              taskInfo.put("code", TaskEnum.PGT0013.getCode());
              taskInfo.put("progress", 1);
              taskInfo.put("isReset", 0);
              this.userRemote.taskHandler(taskInfo);
              clearData(roomInfo.getRoomId());
            }
          } else {
            if (roomInfo.getPartakerList().size() == 1) {
              roomInfo.destroy();
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              clearData(roomInfo.getRoomId());
            } else {
              if (roomInfo.getTimeOutMap().containsKey((int) MustStandCmd.START_GAME)) {
                roomInfo.cancelTimeOut((int) MustStandCmd.START_GAME);
              }
              roomInfo.leaveGame(packet.userId);
              closePage(channel, packet);
              if (this.redisUtils.hasKey(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId)) {
                this.redisUtils.del(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId);
              }
              MustStandPlayer player = roomInfo.getPartakerList().get(0);
              F30071.F300717S2C.Builder builder = F30071.F300717S2C.newBuilder();
              builder.setUserID(player.getUserId()).setAddExp(0).setAddGold(0);
              if (MemManager.isExists(player.getUserId())) {
                builder.setIsDouble(1);
              }
              player.sendPacket(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.GAME_FINISH,
                  builder.build().toByteArray()));
              clearData(roomInfo.getRoomId());
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
   * @create 2020/10/14 17:14
   * @update 2020/10/14 17:14
   */
  private void closePage(Channel channel, Packet packet) {
    ChatManager.removeChannel(packet.roomId, channel);
    GroupManager.removeChannel(packet.roomId, channel);
    SoftChannel.sendPacketToUserId(
        new Packet(ActionCmd.APP_HEART, (short) 2, null), packet.userId);
  }

  /**
   * TODO 游戏经验. 
   *
   * @param playerId [玩家ID]
   * @param exp [玩家经验]
   * @return [游戏经验]
   * @author wangcaiwen|1443****11@qq.com
   * @since 2020/7/9 - 2020/7/9
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
   * TODO 游玩记录.
   *
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:14
   * @update 2020/10/14 17:14
   */
  private void joinMustStandRoom(Packet packet) {
    Map<String, Object> result = Maps.newHashMap();
    result.put("userId", packet.userId);
    result.put("gameId", packet.channel);
    result.put("roomId", packet.roomId);
    this.gameRemote.enterRoom(result);
    this.gameRemote.refreshUserRecord(result);
  }

  /**
   * TODO 开始游戏. 
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:15
   * @update 2020/10/14 17:15
   */
  private void startGame(Long roomId) {
    MustStandRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.refreshStatus(1);
      F30071.F300711S2C.Builder builder = F30071.F300711S2C.newBuilder();
      String label = roomInfo.getProblemMap().get(roomInfo.getRoomRound()).getLabel();
      builder.setQuestionType(label);
      builder.setGameRound(roomInfo.getRoomRound());
      builder.setAllGameRound(10);
      builder.setLastTimes(3);
      builder.setIsDoubleCard(0);
      builder.setDoubleCardNum(3);
      GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.START_GAME,
          builder.build().toByteArray()), roomInfo.getRoomId());
      roomInfo.setUpStartTime();
      showProblem(roomId);
    }
  }

  /**
   * TODO 清除数据. 
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:15
   * @update 2020/10/14 17:15
   */
  private void clearData(Long roomId) {
    MustStandRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
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
          new Packet(ActionCmd.APP_HEART, MustStandCmd.CLOSE_LOADING, null), playerId);
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
    F30071.F30071S2C.Builder builder = F30071.F30071S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.ENTER_ROOM,
            builder.setResult(2).build().toByteArray()));
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
    F30071.F30071S2C.Builder builder = F30071.F30071S2C.newBuilder();
    channel.writeAndFlush(
        new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.ENTER_ROOM,
            builder.setResult(1).build().toByteArray()));
  }

  /**
   * TODO 刷新数据.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:17
   * @update 2020/10/14 17:17
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
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:17
   * @update 2020/10/14 17:17
   */
  private void achievementTask(Long roomId) {
    MustStandRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      List<MustStandPlayer> playerList = roomInfo.getPartakerList();
      if (CollectionUtils.isNotEmpty(playerList)) {
        for (MustStandPlayer player : playerList) {
          if (player.getUseDoubleAnswer() > 0) {
            // 玩家成就.双倍快乐
            Map<String, Object> taskSuc0029 = Maps.newHashMap();
            taskSuc0029.put("userId", player.getUserId());
            taskSuc0029.put("code", AchievementEnum.AMT0029.getCode());
            taskSuc0029.put("progress", player.getUseDoubleAnswer());
            taskSuc0029.put("isReset", 0);
            this.userRemote.achievementHandlers(taskSuc0029);
          }
          if (player.getCorrectAll() == 10) {
            // 玩家成就.站神无双
            Map<String, Object> taskSuc0030 = Maps.newHashMap();
            taskSuc0030.put("userId", player.getUserId());
            taskSuc0030.put("code", AchievementEnum.AMT0030.getCode());
            taskSuc0030.put("progress", 1);
            taskSuc0030.put("isReset", 0);
            this.userRemote.achievementHandlers(taskSuc0030);
          }
          if (player.getCorrectAllAndTime() == 10) {
            // 玩家成就.快狠准稳
            Map<String, Object> taskSuc0031 = Maps.newHashMap();
            taskSuc0031.put("userId", player.getUserId());
            taskSuc0031.put("code", AchievementEnum.AMT0031.getCode());
            taskSuc0031.put("progress", 1);
            taskSuc0031.put("isReset", 0);
            this.userRemote.achievementHandlers(taskSuc0031);
          }
          if (player.getWrongAnswer() == 10) {
            // 玩家成就.大智若愚
            Map<String, Object> taskSuc0032 = Maps.newHashMap();
            taskSuc0032.put("userId", player.getUserId());
            taskSuc0032.put("code", AchievementEnum.AMT0032.getCode());
            taskSuc0032.put("progress", 1);
            taskSuc0032.put("isReset", 0);
            this.userRemote.achievementHandlers(taskSuc0032);
          }
          if (player.getContinuousAnswer() == 5) {
            // 每日任务.连续答对5道题
            Map<String, Object> taskInfo = Maps.newHashMap();
            taskInfo.put("userId", player.getUserId());
            taskInfo.put("code", TaskEnum.PGT0024.getCode());
            taskInfo.put("progress", 1);
            taskInfo.put("isReset", 0);
            this.userRemote.taskHandler(taskInfo);
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
          // 活动处理 丹枫迎秋
          Map<String, Object> activity = Maps.newHashMap();
          activity.put("userId", player.getUserId());
          activity.put("code", ActivityEnum.ACT000102.getCode());
          activity.put("progress", 1);
          this.activityRemote.openHandler(activity);
        }
      }
    }
  }

  /**
   * TODO 等待玩家. 30(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:18
   * @update 2020/10/14 17:18
   */
  private void waitTimeout(Long roomId) {
    try {
      MustStandRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int)MustStandCmd.WAIT_PLAYER)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> waitExamine(roomId)
              ), 30, TimeUnit.SECONDS);
          roomInfo.addTimeOut(MustStandCmd.WAIT_PLAYER, timeout);
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
   * @create 2020/10/14 17:18
   * @update 2020/10/14 17:18
   */
  private void waitExamine(Long roomId) {
    MustStandRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(MustStandCmd.WAIT_PLAYER);
      int partakerSize = roomInfo.getPartakerList().size();
      if (partakerSize == 0) {
        clearData(roomId);
      } else if (partakerSize == 1) {
        roomInfo.destroy();
        roomInfo.refreshStatus(2);
        MustStandPlayer partaker = roomInfo.getPartakerList().get(0);
        F30071.F300717S2C.Builder builder = F30071.F300717S2C.newBuilder();
        builder.setUserID(partaker.getUserId());
        builder.setAddExp(0);
        builder.setAddGold(0);
        partaker.sendPacket(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.GAME_FINISH,
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
   * @create 2020/10/14 17:18
   * @update 2020/10/14 17:18
   */
  private void startTimeout(Long roomId) {
    try {
      MustStandRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int)MustStandCmd.START_GAME)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> startExamine(roomId)
              ), 3, TimeUnit.SECONDS);
          roomInfo.addTimeOut(MustStandCmd.START_GAME, timeout);
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
   * @create 2020/10/14 17:18
   * @update 2020/10/14 17:18
   */
  private void startExamine(Long roomId) {
    MustStandRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(MustStandCmd.START_GAME);
      int partakerSize = roomInfo.getPartakerList().size();
      if (partakerSize == 0) {
        clearData(roomId);
      } else if (partakerSize == 1) {
        roomInfo.destroy();
        roomInfo.refreshStatus(2);
        MustStandPlayer partaker = roomInfo.getPartakerList().get(0);
        F30071.F300717S2C.Builder builder = F30071.F300717S2C.newBuilder();
        builder.setUserID(partaker.getUserId());
        builder.setAddExp(0);
        builder.setAddGold(0);
        partaker.sendPacket(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.GAME_FINISH,
            builder.build().toByteArray()));
        clearData(roomId);
      } else {
        startGame(roomId);
      }
    }
  }

  /**
   * TODO 展示题目. 3(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:19
   * @update 2020/10/14 17:19
   */
  private void showProblem(Long roomId) {
    try {
      MustStandRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int)MustStandCmd.SHOW_PROBLEM)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> sendProblem(roomId)
              ), 3, TimeUnit.SECONDS);
          roomInfo.addTimeOut(MustStandCmd.SHOW_PROBLEM, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 发送题目. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:19
   * @update 2020/10/14 17:19
   */
  private void sendProblem(Long roomId) {
    MustStandRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(MustStandCmd.SHOW_PROBLEM);
      String problem = roomInfo.getProblemMap().get(roomInfo.getRoomRound()).getTitle();
      F30071.F300713S2C.Builder builder = F30071.F300713S2C.newBuilder();
      builder.setTopicDescribe(problem);
      GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.SHOW_PROBLEM,
          builder.build().toByteArray()), roomInfo.getRoomId());
      showAnswers(roomId);
    }
  }

  /**
   * TODO 展示选项. 1(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:19
   * @update 2020/10/14 17:19
   */
  private void showAnswers(Long roomId) {
    try {
      MustStandRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int)MustStandCmd.SHOW_ANSWERS)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> sendAnswers(roomId)
              ), 1, TimeUnit.SECONDS);
          roomInfo.addTimeOut(MustStandCmd.SHOW_ANSWERS, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 发送选项. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:19
   * @update 2020/10/14 17:19
   */
  private void sendAnswers(Long roomId) {
    MustStandRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(MustStandCmd.SHOW_ANSWERS);
      F30071.F300714S2C.Builder builder = F30071.F300714S2C.newBuilder();
      builder.setLastTimes(15);
      List<String> answersList = new ArrayList<>(
          roomInfo.getProblemMap().get(roomInfo.getRoomRound()).getProblemMap().values());
      int index = 1;
      F30071.AnswerList.Builder answersBuilder;
      for (String answers : answersList) {
        answersBuilder = F30071.AnswerList.newBuilder();
        answersBuilder.setAnswerIndex(index);
        answersBuilder.setAnswerDescribe(answers);
        builder.addAnswers(answersBuilder);
        index++;
      }
      GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.SHOW_ANSWERS,
          builder.build().toByteArray()), roomInfo.getRoomId());
      roomInfo.refreshRoundTime();
      actionTimeout(roomId);
    }
  }

  /**
   * TODO 操作定时. 15(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:20
   * @update 2020/10/14 17:20
   */
  private void actionTimeout(Long roomId) {
    try {
      MustStandRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int)MustStandCmd.SELECT_ANSWER)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> roundFinish(roomId)
              ), 15, TimeUnit.SECONDS);
          roomInfo.addTimeOut(MustStandCmd.SELECT_ANSWER, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 回合结束. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:20
   * @update 2020/10/14 17:20
   */
  private void roundFinish(Long roomId) {
    MustStandRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.cancelTimeOut(MustStandCmd.SELECT_ANSWER);
      int answerNo = roomInfo.getProblemMap().get(roomInfo.getRoomRound()).getCorrectAnswerNo();
      F30071.F300716S2C.Builder builder = F30071.F300716S2C.newBuilder();
      builder.setAnswerCorrect(answerNo);
      List<MustStandPlayer> partakerList = roomInfo.getPartakerList();
      F30071.EndInfo.Builder endInfo;
      for (MustStandPlayer partaker : partakerList) {
        endInfo = F30071.EndInfo.newBuilder();
        endInfo.setUserID(partaker.getUserId());
        endInfo.setAnswerIndex(partaker.getSelectIndex());
        if (answerNo == partaker.getSelectIndex()) {
          if (partaker.getContinuousAnswer() < 5) {
            // 连续答对
            partaker.setContinuousAnswer(partaker.getContinuousAnswer() + 1);
          }
          // 答对了
          partaker.setCorrectAll(partaker.getCorrectAll() + 1);
          int score = 25 - partaker.getActionTime();
          if (partaker.getUseDouble() == 1) {
            score = score * 2;
            // 使用加倍并答对
            partaker.setUseDoubleAnswer(partaker.getUseDoubleAnswer() + 1);
            if (partaker.getActionTime() < 2) {
              // 用时小于2秒
              partaker.setCorrectAllAndTime(partaker.getCorrectAllAndTime() + 1);
            }
          }
          endInfo.setCurrentScore(score);
        } else {
          if (partaker.getContinuousAnswer() < 5) {
            // 回答错误 重置标记
            partaker.setContinuousAnswer(0);
          }
          // 答错了
          endInfo.setCurrentScore(0);
          partaker.setWrongAnswer(partaker.getWrongAnswer() + 1);
        }
        endInfo.setAllScoreNum(partaker.getUserScore() + endInfo.getCurrentScore());
        builder.addSettlementInfo(endInfo);
        partaker.refreshScore(endInfo.getCurrentScore());
      }
      GroupManager.sendPacketToGroup(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.ROUND_FINISH,
          builder.build().toByteArray()), roomInfo.getRoomId());
      if (roomInfo.getRoomRound() == MustStandAssets.getInt(MustStandAssets.TOTAL_ROUND)) {
        endTimeout(roomId);
      } else {
        roundTimeout(roomId);
      }
    }
  }

  /**
   * TODO 回合倒计. 2(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:20
   * @update 2020/10/14 17:20
   */
  private void roundTimeout(Long roomId) {
    try {
      MustStandRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int)MustStandCmd.ROUND_FINISH)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> newRoomRound(roomId)
              ), 2, TimeUnit.SECONDS);
          roomInfo.addTimeOut(MustStandCmd.ROUND_FINISH, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 新的回合. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:20
   * @update 2020/10/14 17:20
   */
  private void newRoomRound(Long roomId) {
    MustStandRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.removeTimeOut(MustStandCmd.ROUND_FINISH);
      roomInfo.refreshRound();
      roomInfo.initUserInfo();
      List<MustStandPlayer> partakerList = roomInfo.getPartakerList();
      if (CollectionUtils.isNotEmpty(partakerList)) {
        F30071.F300711S2C.Builder builder;
        for (MustStandPlayer partaker : partakerList) {
          builder = F30071.F300711S2C.newBuilder();
          String label = roomInfo.getProblemMap().get(roomInfo.getRoomRound()).getLabel();
          builder.setQuestionType(label);
          builder.setGameRound(roomInfo.getRoomRound());
          builder.setAllGameRound(10);
          builder.setLastTimes(3);
          if (partaker.getDoubleCard() == 0) {
            builder.setIsDoubleCard(1);
            builder.setDoubleCardNum(0);
          } else {
            builder.setIsDoubleCard(0);
            builder.setDoubleCardNum(partaker.getDoubleCard());
          }
          partaker.sendPacket(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.START_GAME,
              builder.build().toByteArray()));
        }
        showProblem(roomId);
      } else {
        roomInfo.destroy();
        clearData(roomId);
      }
    }
  }

  /**
   * TODO 结束倒计. 2(s)
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:21
   * @update 2020/10/14 17:21
   */
  private void endTimeout(Long roomId) {
    try {
      MustStandRoom roomInfo = GAME_DATA.get(roomId);
      if (roomInfo != null) {
        if (!roomInfo.getTimeOutMap().containsKey((int)MustStandCmd.GAME_FINISH)) {
          Timeout timeout = GroupManager.newTimeOut(
              task -> execute(()
                  -> gameFinish(roomId)
              ), 2, TimeUnit.SECONDS);
          roomInfo.addTimeOut(MustStandCmd.GAME_FINISH, timeout);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 游戏结束. ◕
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 17:21
   * @update 2020/10/14 17:21
   */
  private void gameFinish(Long roomId) {
    MustStandRoom roomInfo = GAME_DATA.get(roomId);
    if (roomInfo != null) {
      roomInfo.destroy();
      roomInfo.refreshStatus(2);
      Long winUserId = roomInfo.getWinUserId();
      if (winUserId > 0) {
        List<MustStandPlayer> partakerList = roomInfo.getPartakerList();
        MustStandPlayer partaker = partakerList.get(0);
        if (partaker.getUserId().equals(winUserId)) {
          F30071.F300717S2C.Builder winBuilder = F30071.F300717S2C.newBuilder();
          winBuilder.setUserID(winUserId);
          if (MemManager.isExists(winUserId)) {
            winBuilder.setAddExp(gainExperience(winUserId, 4));
            winBuilder.setIsDouble(1);
          } else {
            winBuilder.setAddExp(gainExperience(winUserId, 2));
          }
          winBuilder.setAddGold(10);
          partaker.sendPacket(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.GAME_FINISH,
              winBuilder.build().toByteArray()));
          F30071.F300717S2C.Builder loseBuilder = F30071.F300717S2C.newBuilder();
          loseBuilder.setUserID(winUserId).setAddExp(0).setAddGold(0);
          MustStandPlayer loseUser = partakerList.get(1);
          if (MemManager.isExists(loseUser.getUserId())) {
            loseBuilder.setIsDouble(1);
          }
          loseUser.sendPacket(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.GAME_FINISH,
              loseBuilder.build().toByteArray()));
        } else {
          F30071.F300717S2C.Builder winBuilder = F30071.F300717S2C.newBuilder();
          MustStandPlayer winUser = partakerList.get(1);
          winBuilder.setUserID(winUserId);
          if (MemManager.isExists(winUserId)) {
            winBuilder.setAddExp(gainExperience(winUserId, 4));
            winBuilder.setIsDouble(1);
          } else {
            winBuilder.setAddExp(gainExperience(winUserId, 2));
          }
          winBuilder.setAddGold(10);
          winUser.sendPacket(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.GAME_FINISH,
              winBuilder.build().toByteArray()));
          F30071.F300717S2C.Builder loseBuilder = F30071.F300717S2C.newBuilder();
          loseBuilder.setUserID(winUserId).setAddExp(0).setAddGold(0);
          if (MemManager.isExists(partaker.getUserId())) {
            loseBuilder.setIsDouble(1);
          }
          partaker.sendPacket(new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.GAME_FINISH,
              loseBuilder.build().toByteArray()));
        }
        List<MustStandPlayer> playerList = roomInfo.getPartakerList();
        Map<String, Object> taskInfo1;
        for (MustStandPlayer player : playerList) {
          // 玩3局一站到底
          taskInfo1 = Maps.newHashMap();
          taskInfo1.put("userId", player.getUserId());
          taskInfo1.put("code", TaskEnum.PGT0008.getCode());
          taskInfo1.put("progress", 1);
          taskInfo1.put("isReset", 0);
          this.userRemote.taskHandler(taskInfo1);
        }
        // 赢1局一站到底
        Map<String, Object> taskInfo = Maps.newHashMap();
        taskInfo.put("userId", winUserId);
        taskInfo.put("code", TaskEnum.PGT0013.getCode());
        taskInfo.put("progress", 1);
        taskInfo.put("isReset", 0);
        this.userRemote.taskHandler(taskInfo);
        achievementTask(roomInfo.getRoomId());
        clearData(roomId);
      } else {
        F30071.F300717S2C.Builder builder = F30071.F300717S2C.newBuilder();
        builder.setUserID(0).setAddExp(0).setAddGold(0);
        List<MustStandPlayer> playerList = roomInfo.getPartakerList();
        playerList.forEach(players -> {
          if (MemManager.isExists(players.getUserId())) {
            builder.setIsDouble(1);
          }
          players.sendPacket(
              new Packet(ActionCmd.GAME_MUST_STAND, MustStandCmd.GAME_FINISH,
                  builder.build().toByteArray()));
          builder.clearIsDouble();
        });
        Map<String, Object> taskInfo1;
        for (MustStandPlayer player : playerList) {
          // 玩3局一站到底
          taskInfo1 = Maps.newHashMap();
          taskInfo1.put("userId", player.getUserId());
          taskInfo1.put("code", TaskEnum.PGT0008.getCode());
          taskInfo1.put("progress", 1);
          taskInfo1.put("isReset", 0);
          this.userRemote.taskHandler(taskInfo1);
        }
        achievementTask(roomInfo.getRoomId());
        clearData(roomId);
      }
    }
  }
}
