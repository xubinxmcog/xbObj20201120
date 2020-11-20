package com.enuos.live.handler.impl;

import com.enuos.live.assets.GameQuestAssets;
import com.enuos.live.handler.TaskHandler;
import com.enuos.live.manager.TaskEnum;
import com.enuos.live.rest.UserRemote;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * TODO 每日任务.
 *
 * @author WangCaiWen - 1443710411@qq.com
 * @version 1.0
 * @since 2020/7/27 14:07
 */

@Component
public class TaskHandlerImpl implements TaskHandler {

  private static final Logger logger = LoggerFactory.getLogger(TaskHandlerImpl.class);

  @Resource
  private UserRemote userRemote;

  /**
   * TODO 游戏每日任务分发处理.
   *
   * @param userId 玩家ID
   * @param gameId 游戏ID
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  @Override
  public void handle(Long userId, Long gameId) {
    // 每日任务校验
    if (GameQuestAssets.getDailyQuest(gameId) != null) {
      switch (gameId.intValue()) {
        // 五子棋
        case 30001:
          playWithFriendsGoBang(userId, gameId);
          break;
        // 斗兽棋
        case 30011:
          playWithFriendsAnimal(userId, gameId);
          break;
        // 扫雷
        case 30031:
          playWithFriendsMinesweeper(userId, gameId);
          break;
        // 一站到底
        case 30071:
          playWithFriendsMustStand(userId, gameId);
          break;
        default:
          logger.info("触发每日任务 ===> [其他游戏({})]. 玩家ID: {}", gameId , userId);
          break;
      }
    }
  }

  /**
   * TODO 与好友玩1局五子棋.
   *
   * @param userId 玩家ID
   * @param gameId 游戏ID
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  private void playWithFriendsGoBang(Long userId, Long gameId) {
    logger.info("触发每日任务 ===> [与好友玩1局五子棋({})]. 玩家ID: {}", gameId , userId);
    Map<String, Object> taskInfo = Maps.newHashMap();
    taskInfo.put("userId", userId);
    taskInfo.put("code", TaskEnum.PGT0014.getCode());
    taskInfo.put("progress", 1);
    taskInfo.put("isReset", 0);
    this.userRemote.taskHandler(taskInfo);
  }

  /**
   * TODO 与好友玩1局斗兽棋.
   *
   * @param userId 玩家ID
   * @param gameId 游戏ID
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  private void playWithFriendsAnimal(Long userId, Long gameId) {
    logger.info("触发每日任务 ===> [与好友玩1局斗兽棋({})]. 玩家ID: {}", gameId , userId);
    Map<String, Object> taskInfo = Maps.newHashMap();
    taskInfo.put("userId", userId);
    taskInfo.put("code", TaskEnum.PGT0015.getCode());
    taskInfo.put("progress", 1);
    taskInfo.put("isReset", 0);
    this.userRemote.taskHandler(taskInfo);
  }

  /**
   * TODO 与好友玩1局扫雷.
   *
   * @param userId 玩家ID
   * @param gameId 游戏ID
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  private void playWithFriendsMinesweeper(Long userId, Long gameId) {
    logger.info("触发每日任务 ===> [与好友玩1局扫雷({})]. 玩家ID: {}", gameId , userId);
    Map<String, Object> taskInfo = Maps.newHashMap();
    taskInfo.put("userId", userId);
    taskInfo.put("code", TaskEnum.PGT0016.getCode());
    taskInfo.put("progress", 1);
    taskInfo.put("isReset", 0);
    this.userRemote.taskHandler(taskInfo);
  }

  /**
   * TODO 与好友玩1局一站到底.
   *
   * @param userId 玩家ID
   * @param gameId 游戏ID
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  private void playWithFriendsMustStand(Long userId, Long gameId) {
    logger.info("触发每日任务 ===> [与好友玩1局一站到底({})]. 玩家ID: {}", gameId , userId);
    Map<String, Object> taskInfo = Maps.newHashMap();
    taskInfo.put("userId", userId);
    taskInfo.put("code", TaskEnum.PGT0017.getCode());
    taskInfo.put("progress", 1);
    taskInfo.put("isReset", 0);
    this.userRemote.taskHandler(taskInfo);
  }
}
