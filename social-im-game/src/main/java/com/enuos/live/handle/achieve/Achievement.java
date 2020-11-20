package com.enuos.live.handle.achieve;

import com.enuos.live.manager.AchievementEnum;
import com.enuos.live.manager.LoggerManager;
import com.enuos.live.manager.TaskEnum;
import com.enuos.live.rest.UserRemote;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * TODO 玩家成就.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/11/4 14:44
 */

@Component
public class Achievement implements Achieve {

  @Resource
  private UserRemote userRemote;

  private static Achievement achieveTrigger;

  @PostConstruct
  public void init() {
    achieveTrigger = this;
    achieveTrigger.userRemote = this.userRemote;
  }

  /**
   * TODO ==> 每日任务.
   *
   * @param params [玩家ID, 游戏ID, 触发CODE, 累积数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:23
   * @update 2020/11/4 16:23
   */
  @Override
  public void everyDay(Object... params) {
    Long userId = (Long) params[0];
    Integer gameId = (Integer) params[1];
    Integer action = (Integer) params[2];
    Integer quantity;
    switch (gameId) {
      // 五子棋
      case 30001:
        everyDay30001(userId, action);
        break;
      // 斗兽棋
      case 30011:
        quantity = (Integer) params[3];
        everyDay30011(userId, action, quantity);
        break;
      // 飞行棋
      case 30021:
        everyDay30021(userId, action);
        break;
      // 扫雷
      case 30031:
        quantity = (Integer) params[3];
        everyDay30031(userId, action, quantity);
        break;
      // 你画我猜
      case 30041:
        everyDay30041(userId, action);
        break;
      // 谁是卧底
      case 30051:
        everyDay30051(userId, action);
        break;
      // 你说我猜
      case 30061:
        quantity = (Integer) params[3];
        everyDay30061(userId, action, quantity);
        break;
      // 一站到底
      case 30071:
        everyDay30071(userId, action);
        break;
      // 狼人杀
      case 30081:
        everyDay30081(userId, action);
        break;
      // 优乐
      case 30251:
        LoggerManager.warn("任务不存在!!!");
        break;
      // 炸弹猫
      case 30291:
        LoggerManager.warn("任务不存在!!!");
        break;
      default:
        LoggerManager.warn("任务不存在!!!");
        break;
    }
  }

  /**
   * TODO 每日任务.五子棋
   *
   * @param userId [玩家ID]
   * @param action [操作CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:55
   * @update 2020/11/4 16:55
   */
  private void everyDay30001(Long userId, Integer action) {
    Map<String, Object> taskInfo = Maps.newHashMap();
    taskInfo.put("userId", userId);
    // 玩3局五子棋
    if (action == 1) {
      taskInfo.put("code", TaskEnum.PGT0001.getCode());
    } else {
      // 赢1局五子棋
      taskInfo.put("code", TaskEnum.PGT0010.getCode());
    }
    taskInfo.put("progress", 1);
    taskInfo.put("isReset", 0);
    achieveTrigger.userRemote.taskHandler(taskInfo);
  }

  /**
   * TODO 每日任务.斗兽棋
   *
   * @param userId [玩家ID]
   * @param action [操作CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:55
   * @update 2020/11/4 16:55
   */
  private void everyDay30011(Long userId, Integer action, Integer quantity) {
    Map<String, Object> taskInfo = Maps.newHashMap();
    taskInfo.put("userId", userId);
    switch (action) {
      // 玩3局斗兽棋
      case 1:
        taskInfo.put("code", TaskEnum.PGT0002.getCode());
        break;
      // 赢1局斗兽棋
      case 2:
        taskInfo.put("code", TaskEnum.PGT0011.getCode());
        break;
      // 在斗兽棋中，使用狗吃掉其他棋子3次
      default:
        taskInfo.put("code", TaskEnum.PGT0018.getCode());
        break;
    }
    taskInfo.put("progress", quantity);
    taskInfo.put("isReset", 0);
    achieveTrigger.userRemote.taskHandler(taskInfo);
  }

  /**
   * TODO 每日任务.飞行棋
   *
   * @param userId [玩家ID]
   * @param action [操作CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:55
   * @update 2020/11/4 16:55
   */
  private void everyDay30021(Long userId, Integer action) {
    Map<String, Object> taskInfo = Maps.newHashMap();
    taskInfo.put("userId", userId);
    // 玩1局飞行棋
    if (action == 1) {
      taskInfo.put("code", TaskEnum.PGT0003.getCode());
    } else {
      // 在飞行棋中，将对手撞回基地1次
      taskInfo.put("code", TaskEnum.PGT0019.getCode());
    }
    taskInfo.put("progress", 1);
    taskInfo.put("isReset", 0);
    achieveTrigger.userRemote.taskHandler(taskInfo);
  }

  /**
   * TODO 每日任务.扫雷
   *
   * @param userId [玩家ID]
   * @param action [操作CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:55
   * @update 2020/11/4 16:55
   */
  private void everyDay30031(Long userId, Integer action, Integer quantity) {
    Map<String, Object> taskInfo = Maps.newHashMap();
    taskInfo.put("userId", userId);
    switch (action) {
      // 玩3局扫雷
      case 1:
        taskInfo.put("code", TaskEnum.PGT0004.getCode());
        break;
      // 赢1局扫雷
      case 2:
        taskInfo.put("code", TaskEnum.PGT0012.getCode());
        break;
      // 在扫雷中，累计正确标记地雷5次
      default:
        taskInfo.put("code", TaskEnum.PGT0020.getCode());
        break;
    }
    taskInfo.put("progress", quantity);
    taskInfo.put("isReset", 0);
    achieveTrigger.userRemote.taskHandler(taskInfo);
  }

  /**
   * TODO 每日任务.你画我猜
   *
   * @param userId [玩家ID]
   * @param action [操作CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:55
   * @update 2020/11/4 16:55
   */
  private void everyDay30041(Long userId, Integer action) {
    Map<String, Object> taskInfo = Maps.newHashMap();
    // 玩1局你画我猜
    taskInfo.put("userId", userId);
    if (action == 1) {
      taskInfo.put("code", TaskEnum.PGT0005.getCode());
    } else {
      // 在你画我猜中，累计猜中5次
      taskInfo.put("code", TaskEnum.PGT0021.getCode());
    }
    taskInfo.put("progress", 1);
    taskInfo.put("isReset", 0);
    achieveTrigger.userRemote.taskHandler(taskInfo);
  }

  /**
   * TODO 每日任务.谁是卧底
   *
   * @param userId [玩家ID]
   * @param action [操作CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:55
   * @update 2020/11/4 16:55
   */
  private void everyDay30051(Long userId, Integer action) {
    Map<String, Object> taskInfo = Maps.newHashMap();
    taskInfo.put("userId", userId);
    // 玩1局谁是卧底
    if (action == 1) {
      taskInfo.put("code", TaskEnum.PGT0007.getCode());
    } else {
      // 在谁是卧底中，存活3轮及以上
      taskInfo.put("code", TaskEnum.PGT0023.getCode());
    }
    taskInfo.put("progress", 1);
    taskInfo.put("isReset", 0);
    achieveTrigger.userRemote.taskHandler(taskInfo);
  }

  /**
   * TODO 每日任务.你说我猜
   *
   * @param userId [玩家ID]
   * @param action [操作CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:55
   * @update 2020/11/4 16:55
   */
  private void everyDay30061(Long userId, Integer action, Integer quantity) {
    Map<String, Object> taskInfo = Maps.newHashMap();
    taskInfo.put("userId", userId);
    // 玩1局你说我猜
    if (action == 1) {
      taskInfo.put("code", TaskEnum.PGT0006.getCode());
    } else {
      // 在你说我猜中，累计猜中5次
      taskInfo.put("code", TaskEnum.PGT0022.getCode());
    }
    taskInfo.put("progress", quantity);
    taskInfo.put("isReset", 0);
    achieveTrigger.userRemote.taskHandler(taskInfo);
  }

  /**
   * TODO 每日任务.一站到底
   *
   * @param userId [玩家ID]
   * @param action [操作CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:55
   * @update 2020/11/4 16:55
   */
  private void everyDay30071(Long userId, Integer action) {
    Map<String, Object> taskInfo = Maps.newHashMap();
    taskInfo.put("userId", userId);
    switch (action) {
      // 玩3局一站到底"
      case 1:
        taskInfo.put("code", TaskEnum.PGT0008.getCode());
        break;
      // 赢1局一站到底
      case 2:
        taskInfo.put("code", TaskEnum.PGT0013.getCode());
        break;
      // 在一站到底中，连续答对5道题
      default:
        taskInfo.put("code", TaskEnum.PGT0024.getCode());
        break;
    }
    taskInfo.put("progress", 1);
    taskInfo.put("isReset", 0);
    achieveTrigger.userRemote.taskHandler(taskInfo);
  }

  /**
   * TODO 每日任务.狼人杀
   *
   * @param userId [玩家ID]
   * @param action [操作CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:55
   * @update 2020/11/4 16:55
   */
  private void everyDay30081(Long userId, Integer action) {
    Map<String, Object> taskInfo = Maps.newHashMap();
    taskInfo.put("userId", userId);
    switch (action) {
      // 玩1局狼人杀
      case 1:
        taskInfo.put("code", TaskEnum.PGT0009.getCode());
        break;
      // 在狼人杀中，当1次狼人
      case 2:
        taskInfo.put("code", TaskEnum.PGT0025.getCode());
        break;
      // 在狼人杀中，竞选1次警长
      default:
        taskInfo.put("code", TaskEnum.PGT0026.getCode());
        break;
    }
    taskInfo.put("progress", 1);
    taskInfo.put("isReset", 0);
    achieveTrigger.userRemote.taskHandler(taskInfo);
  }

  /**
   * TODO ==> 单次成就.
   *
   * @param params [玩家ID, 游戏ID, 触发CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:25
   * @update 2020/11/4 16:25
   */
  @Override
  public void complete(Object... params) {
    Long userId = (Long) params[0];
    Integer gameId = (Integer) params[1];
    Integer action = (Integer) params[2];
    switch (gameId) {
      // 五子棋
      case 30001:
        complete30001(userId, action);
        break;
      // 斗兽棋
      case 30011:
        LoggerManager.warn("成就不存在!!!");
        break;
      // 飞行棋
      case 30021:
        complete30021(userId, action);
        break;
      // 扫雷
      case 30031:
        complete30031(userId, action);
        break;
      // 你画我猜
      case 30041:
        LoggerManager.warn("成就不存在!!!");
        break;
      // 谁是卧底
      case 30051:
        LoggerManager.warn("成就不存在!!!");
        break;
      // 你说我猜
      case 30061:
        LoggerManager.warn("成就不存在!!!");
        break;
      // 一站到底
      case 30071:
        complete30071(userId, action);
        break;
      // 狼人杀
      case 30081:
        LoggerManager.warn("任务不存在!!!");
        break;
      // 优乐
      case 30251:
        LoggerManager.warn("任务不存在!!!");
        break;
      // 炸弹猫
      case 30291:
        complete30291(userId, action);
        break;
      default:
        LoggerManager.warn("任务不存在!!!");
        break;
    }
  }

  /**
   * TODO 单次成就. 五子棋
   *
   * @param userId [玩家ID]
   * @param action [触发CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 9:05
   * @update 2020/11/5 9:05
   */
  private void complete30001(Long userId, Integer action) {
    Map<String, Object> achieveInfo = Maps.newHashMap();
    achieveInfo.put("userId", userId);
    // 玩家成就 [半壁江山] 在五子棋中，单局双方棋子占据超过一半的棋盘
    if (action == 1) {
      achieveInfo.put("code", AchievementEnum.AMT0003.getCode());
    } else {
      // 玩家成就 [棋逢对手] 在五子棋中，单局双方共有8个及以上4连
      achieveInfo.put("code", AchievementEnum.AMT0004.getCode());
    }
    achieveInfo.put("progress", 1);
    achieveInfo.put("isReset", 0);
    achieveTrigger.userRemote.achievementHandlers(achieveInfo);
  }

  /**
   * TODO 单次成就. 飞行棋
   *
   * @param userId [玩家ID]
   * @param action [触发CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 9:05
   * @update 2020/11/5 9:05
   */
  private void complete30021(Long userId, Integer action) {
    Map<String, Object> achieveInfo = Maps.newHashMap();
    if (action == 1) {
      // 玩家成就 [原地转圈] 在飞行棋中，单局在终点区域兜圈3圈及以上到达终点
      achieveInfo.put("userId", userId);
      achieveInfo.put("code", AchievementEnum.AMT0012.getCode());
      achieveInfo.put("progress", 1);
      achieveInfo.put("isReset", 0);
      achieveTrigger.userRemote.achievementHandlers(achieveInfo);
    }
  }

  /**
   * TODO 单次成就. 扫雷
   *
   * @param userId [玩家ID]
   * @param action [触发CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 9:05
   * @update 2020/11/5 9:05
   */
  private void complete30031(Long userId, Integer action) {
    Map<String, Object> achieveInfo = Maps.newHashMap();
    if (action == 1) {
      // 玩家成就 [拆弹专家] 在扫雷中，单局正确标记地雷12颗及以上
      achieveInfo.put("userId", userId);
      achieveInfo.put("code", AchievementEnum.AMT0016.getCode());
      achieveInfo.put("progress", 1);
      achieveInfo.put("isReset", 0);
      achieveTrigger.userRemote.achievementHandlers(achieveInfo);
    }
  }

  /**
   * TODO 单次成就. 一站到底
   *
   * @param userId [玩家ID]
   * @param action [触发CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 9:05
   * @update 2020/11/5 9:05
   */
  private void complete30071(Long userId, Integer action) {
    Map<String, Object> achieveInfo = Maps.newHashMap();
    achieveInfo.put("userId", userId);
    if (action == 1) {
      // 玩家成就 [快狠准稳] 在一站到底中，单局答对全部10道题目且每道题用时小于2秒
      achieveInfo.put("code", AchievementEnum.AMT0031.getCode());
    } else {
      // 玩家成就 [大智若愚] 在一站到底中，单局答错全部10道题目
      achieveInfo.put("code", AchievementEnum.AMT0032.getCode());
    }
    achieveInfo.put("progress", 1);
    achieveInfo.put("isReset", 0);
    achieveTrigger.userRemote.achievementHandlers(achieveInfo);
  }

  /**
   * TODO 单次成就. 炸弹猫
   *
   * @param userId [玩家ID]
   * @param action [触发CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 9:05
   * @update 2020/11/5 9:05
   */
  private void complete30291(Long userId, Integer action) {
    Map<String, Object> achieveInfo = Maps.newHashMap();
    if (action == 1) {
      // 玩家成就 [神选英杰] 在炸弹猫中，单局没有摸到1次炸弹获得第一
      achieveInfo.put("userId", userId);
      achieveInfo.put("code", AchievementEnum.AMT0038.getCode());
      achieveInfo.put("progress", 1);
      achieveInfo.put("isReset", 0);
      achieveTrigger.userRemote.achievementHandlers(achieveInfo);
    }
  }

  /**
   * TODO ==> 累计任务.
   *
   * @param params [玩家ID, 游戏ID, 触发CODE, 累积数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:24
   * @update 2020/11/4 16:24
   */
  @Override
  public void grandTotal(Object... params) {
    Long userId = (Long) params[0];
    Integer gameId = (Integer) params[1];
    if (gameId <= 1) {
      if (gameId == 0) {
        // 高级玩家
        advancedPlayer(userId);
      } else {
        // 头号玩家
        numberOnePlayer(userId);
      }
    } else {
      Integer action = (Integer) params[2];
      Integer quantity = (Integer) params[3];
      switch (gameId) {
        // 五子棋
        case 30001:
          grandTotal30001(userId, action, quantity);
          break;
        // 斗兽棋
        case 30011:
          grandTotal30011(userId, action, quantity);
          break;
        // 飞行棋
        case 30021:
          grandTotal30021(userId, action, quantity);
          break;
        // 扫雷
        case 30031:
          grandTotal30031(userId, action, quantity);
          break;
        // 你画我猜
        case 30041:
          grandTotal30041(userId, action, quantity);
          break;
        // 谁是卧底
        case 30051:
          grandTotal30051(userId, action, quantity);
          break;
        // 你说我猜
        case 30061:
          grandTotal30061(userId, action, quantity);
          break;
        // 一站到底
        case 30071:
          grandTotal30071(userId, action, quantity);
          break;
        // 狼人杀
        case 30081:
          LoggerManager.warn("任务不存在!!!");
          break;
        // 优乐
        case 30251:
          grandTotal30251(userId, action, quantity);
          break;
        // 炸弹猫
        case 30291:
          grandTotal30291(userId, action, quantity);
          break;
        default:
          LoggerManager.warn("任务不存在!!!");
          break;
      }
    }
  }

  /**
   * TODO 玩家成就. 高级玩家
   *
   * @param userId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 9:50
   * @update 2020/11/5 9:50
   */
  private void advancedPlayer(Long userId) {
    Map<String, Object> achieveInfo = Maps.newHashMap();
    achieveInfo.put("userId", userId);
    achieveInfo.put("code", AchievementEnum.AMT0041.getCode());
    achieveInfo.put("progress", 1);
    achieveInfo.put("isReset", 0);
    achieveTrigger.userRemote.achievementHandlers(achieveInfo);
  }

  /**
   * TODO 玩家成就. 头号玩家
   *
   * @param userId 玩家ID
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 9:51
   * @update 2020/11/5 9:51
   */
  private void numberOnePlayer(Long userId) {
    Map<String, Object> achieveInfo = Maps.newHashMap();
    achieveInfo.put("userId", userId);
    achieveInfo.put("code", AchievementEnum.AMT0042.getCode());
    achieveInfo.put("progress", 1);
    achieveInfo.put("isReset", 0);
    achieveTrigger.userRemote.achievementHandlers(achieveInfo);
  }

  /**
   * TODO 玩家成就. 五子棋
   *
   * @param userId [玩家ID]
   * @param action [触发CODE]
   * @param quantity [累积数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 9:58
   * @update 2020/11/5 9:58
   */
  private void grandTotal30001(Long userId, Integer action, Integer quantity) {
    Map<String, Object> achieveInfo = Maps.newHashMap();
    achieveInfo.put("userId", userId);
    if (action == 1) {
      // 玩家成就 [星罗棋布] 在五子棋中，累计落子3000颗
      achieveInfo.put("code", AchievementEnum.AMT0001.getCode());
    } else {
      // 玩家成就 [天人合一] 累计占据中央天元200次
      achieveInfo.put("code", AchievementEnum.AMT0002.getCode());
    }
    achieveInfo.put("progress", quantity);
    achieveInfo.put("isReset", 0);
    achieveTrigger.userRemote.achievementHandlers(achieveInfo);
  }

  /**
   * TODO 玩家成就. 斗兽棋
   *
   * @param userId [玩家ID]
   * @param action [触发CODE]
   * @param quantity [累积数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 9:58
   * @update 2020/11/5 9:58
   */
  private void grandTotal30011(Long userId, Integer action, Integer quantity) {
    Map<String, Object> achieveInfo = Maps.newHashMap();
    achieveInfo.put("userId", userId);
    switch (action) {
      // 玩家成就 [河东狮吼] 在斗兽棋中，累计使用狮吃掉其他棋子200次
      case 1:
        achieveInfo.put("code", AchievementEnum.AMT0005.getCode());
        break;
      // 玩家成就 [驱虎吞狼] 累计使用虎吃掉狼100次
      case 2:
        achieveInfo.put("code", AchievementEnum.AMT0006.getCode());
        break;
      // 玩家成就 [猫捉老鼠] 累计使用猫吃掉鼠30次
      case 3:
        achieveInfo.put("code", AchievementEnum.AMT0007.getCode());
        break;
      // 玩家成就 [鼠胆英雄] 累计使用鼠吃掉象20次
      default:
        achieveInfo.put("code", AchievementEnum.AMT0008.getCode());
        break;
    }
    achieveInfo.put("progress", quantity);
    achieveInfo.put("isReset", 0);
    achieveTrigger.userRemote.achievementHandlers(achieveInfo);
  }

  /**
   * TODO 玩家成就. 飞行棋
   *
   * @param userId [玩家ID]
   * @param action [触发CODE]
   * @param quantity [累积数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 9:58
   * @update 2020/11/5 9:58
   */
  private void grandTotal30021(Long userId, Integer action, Integer quantity) {
    Map<String, Object> achieveInfo = Maps.newHashMap();
    achieveInfo.put("userId", userId);
    switch (action) {
      // 玩家成就 [弹射起步] 在飞行棋中，累计跳子100次
      case 1:
        achieveInfo.put("code", AchievementEnum.AMT0009.getCode());
        break;
      // 玩家成就 [回到原点] 在飞行棋中，累计被撞子20次
      case 2:
        achieveInfo.put("code", AchievementEnum.AMT0010.getCode());
        break;
      // 玩家成就 [穿越虫洞] 在飞行棋中，累计飞棋10次
      default:
        achieveInfo.put("code", AchievementEnum.AMT0011.getCode());
        break;
    }
    achieveInfo.put("progress", quantity);
    achieveInfo.put("isReset", 0);
    achieveTrigger.userRemote.achievementHandlers(achieveInfo);
  }

  /**
   * TODO 玩家成就. 扫雷
   *
   * @param userId [玩家ID]
   * @param action [触发CODE]
   * @param quantity [累积数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 9:58
   * @update 2020/11/5 9:58
   */
  private void grandTotal30031(Long userId, Integer action, Integer quantity) {
    Map<String, Object> achieveInfo = Maps.newHashMap();
    achieveInfo.put("userId", userId);
    switch (action) {
      // 玩家成就 [排雷先锋] 在扫雷中，累计正确标记地雷200次
      case 1:
        achieveInfo.put("code", AchievementEnum.AMT0013.getCode());
        break;
      // 玩家成就 [我是炮灰] 在扫雷中，累计踩地雷50次
      case 2:
        achieveInfo.put("code", AchievementEnum.AMT0014.getCode());
        break;
      // 玩家成就 [重复工作] 在扫雷中，累计与对手进行相同操作10次
      default:
        achieveInfo.put("code", AchievementEnum.AMT0015.getCode());
        break;
    }
    achieveInfo.put("progress", quantity);
    achieveInfo.put("isReset", 0);
    achieveTrigger.userRemote.achievementHandlers(achieveInfo);
  }

  /**
   * TODO 玩家成就. 你画我猜
   *
   * @param userId [玩家ID]
   * @param action [触发CODE]
   * @param quantity [累积数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 9:58
   * @update 2020/11/5 9:58
   */
  private void grandTotal30041(Long userId, Integer action, Integer quantity) {
    Map<String, Object> achieveInfo = Maps.newHashMap();
    achieveInfo.put("userId", userId);
    switch (action) {
      // 玩家成就 [慧眼如炬] 在你画我猜中，累计猜中200次
      case 1:
        achieveInfo.put("code", AchievementEnum.AMT0017.getCode());
        break;
      // 玩家成就 [抽象画家] 在你画我猜中，累计让所有其他玩家都猜不中10次
      case 2:
        achieveInfo.put("code", AchievementEnum.AMT0018.getCode());
        break;
      // 玩家成就 [灵魂画师] 在你画我猜中，累计让所有其他玩家都猜中10次
      case 3:
        achieveInfo.put("code", AchievementEnum.AMT0019.getCode());
        break;
      // 玩家成就 [心有灵犀] 在你画我猜中，累计只有1个玩家猜中10次
      default:
        achieveInfo.put("code", AchievementEnum.AMT0020.getCode());
        break;
    }
    achieveInfo.put("progress", quantity);
    achieveInfo.put("isReset", 0);
    achieveTrigger.userRemote.achievementHandlers(achieveInfo);
  }

  /**
   * TODO 玩家成就. 谁是卧底
   *
   * @param userId [玩家ID]
   * @param action [触发CODE]
   * @param quantity [累积数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 9:58
   * @update 2020/11/5 9:58
   */
  private void grandTotal30051(Long userId, Integer action, Integer quantity) {
    Map<String, Object> achieveInfo = Maps.newHashMap();
    achieveInfo.put("userId", userId);
    switch (action) {
      // 玩家成就 [无间风云] 在谁是卧底中，累计作为卧底并获胜30次
      case 1:
        achieveInfo.put("code", AchievementEnum.AMT0025.getCode());
        break;
      // 玩家成就 [躺枪群众] 在谁是卧底中，累计作为平民被投死20次
      case 2:
        achieveInfo.put("code", AchievementEnum.AMT0026.getCode());
        break;
      // 玩家成就 [利刃出鞘] 在谁是卧底中，累计作为平民在第一回合投出卧底并获胜10次
      case 3:
        achieveInfo.put("code", AchievementEnum.AMT0027.getCode());
        break;
      // 玩家成就 [决战黎明] 在谁是卧底中，累计作为卧底并猜出平民词10次
      default:
        achieveInfo.put("code", AchievementEnum.AMT0028.getCode());
        break;
    }
    achieveInfo.put("progress", quantity);
    achieveInfo.put("isReset", 0);
    achieveTrigger.userRemote.achievementHandlers(achieveInfo);
  }

  /**
   * TODO 玩家成就. 你说我猜
   *
   * @param userId [玩家ID]
   * @param action [触发CODE]
   * @param quantity [累积数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 9:58
   * @update 2020/11/5 9:58
   */
  private void grandTotal30061(Long userId, Integer action, Integer quantity) {
    Map<String, Object> achieveInfo = Maps.newHashMap();
    achieveInfo.put("userId", userId);
    switch (action) {
      // 玩家成就 [心领神会] 在你说我猜中，累计猜中200次
      case 1:
        achieveInfo.put("code", AchievementEnum.AMT0021.getCode());
        break;
      // 玩家成就 [天花乱坠] 在你说我猜中，累计让所有其他玩家都猜不中10次
      case 2:
        achieveInfo.put("code", AchievementEnum.AMT0022.getCode());
        break;
      // 玩家成就 [绘声绘色] 在你说我猜中，累计让所有其他玩家都猜中10次
      case 3:
        achieveInfo.put("code", AchievementEnum.AMT0023.getCode());
        break;
      // 玩家成就 [心心相印] 在你说我猜中，累计只有1个玩家猜中10次
      default:
        achieveInfo.put("code", AchievementEnum.AMT0024.getCode());
        break;
    }
    achieveInfo.put("progress", quantity);
    achieveInfo.put("isReset", 0);
    achieveTrigger.userRemote.achievementHandlers(achieveInfo);
  }

  /**
   * TODO 玩家成就. 一站到底
   *
   * @param userId [玩家ID]
   * @param action [触发CODE]
   * @param quantity [累积数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 9:58
   * @update 2020/11/5 9:58
   */
  private void grandTotal30071(Long userId, Integer action, Integer quantity) {
    Map<String, Object> achieveInfo = Maps.newHashMap();
    achieveInfo.put("userId", userId);
    if (action == 1) {
      // 玩家成就 [双倍快乐] 在一站到底中，累计使用加倍并答对100次
      achieveInfo.put("code", AchievementEnum.AMT0029.getCode());
    } else {
      // 玩家成就 [站神无双] 在一站到底中，累计答对全部10道题目并获胜10次
      achieveInfo.put("code", AchievementEnum.AMT0030.getCode());
    }
    achieveInfo.put("progress", quantity);
    achieveInfo.put("isReset", 0);
    achieveTrigger.userRemote.achievementHandlers(achieveInfo);
  }

  /**
   * TODO 玩家成就. 优乐
   *
   * @param userId [玩家ID]
   * @param action [触发CODE]
   * @param quantity [累积数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 9:58
   * @update 2020/11/5 9:58
   */
  private void grandTotal30251(Long userId, Integer action, Integer quantity) {
    Map<String, Object> achieveInfo = Maps.newHashMap();
    achieveInfo.put("userId", userId);
    switch (action) {
      // 玩家成就 [质疑精神] 在UNO中，累计质疑上家50次
      case 1:
        achieveInfo.put("code", AchievementEnum.AMT0033.getCode());
        break;
      // 玩家成就 [反将一军] 在UNO中，累计质疑成功20次
      case 2:
        achieveInfo.put("code", AchievementEnum.AMT0034.getCode());
        break;
      // 玩家成就 [牌差一着] 累计在UNO状态下失去第一10次
      case 3:
        achieveInfo.put("code", AchievementEnum.AMT0035.getCode());
        break;
      // 玩家成就 [抽卡人生] 在UNO中，累计主动抽牌50次
      default:
        achieveInfo.put("code", AchievementEnum.AMT0036.getCode());
        break;
    }
    achieveInfo.put("progress", quantity);
    achieveInfo.put("isReset", 0);
    achieveTrigger.userRemote.achievementHandlers(achieveInfo);
  }

  /**
   * TODO 玩家成就. 炸弹猫
   *
   * @param userId [玩家ID]
   * @param action [触发CODE]
   * @param quantity [累积数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 9:58
   * @update 2020/11/5 9:58
   */
  private void grandTotal30291(Long userId, Integer action, Integer quantity) {
    Map<String, Object> achieveInfo = Maps.newHashMap();
    achieveInfo.put("userId", userId);
    switch (action) {
      // 玩家成就 [大预言术] 在炸弹猫中，累计使用先知50次
      case 1:
        achieveInfo.put("code", AchievementEnum.AMT0037.getCode());
        break;
      // 玩家成就 [有求必应] 在炸弹猫中，累计被使用祈求送出20张卡
      case 2:
        achieveInfo.put("code", AchievementEnum.AMT0039.getCode());
        break;
      // 玩家成就 [巫毒诅咒] 在炸弹猫中，累计使用诅咒100次
      default:
        achieveInfo.put("code", AchievementEnum.AMT0040.getCode());
        break;
    }
    achieveInfo.put("progress", quantity);
    achieveInfo.put("isReset", 0);
    achieveTrigger.userRemote.achievementHandlers(achieveInfo);
  }
}
