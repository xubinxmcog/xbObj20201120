package com.enuos.live.assets;

import com.google.common.collect.Maps;
import java.util.HashMap;

/**
 * TODO 游戏任务.
 *
 * @author WangCaiWen - 1443710411@qq.com
 * @version 1.0
 * @since 2020/7/27 11:04
 */

public class GameQuestAssets {

  private static HashMap<Long, String> GAME_QUEST = Maps.newHashMap();
  static {
    GAME_QUEST.put(30001L, "五子棋");
    GAME_QUEST.put(30011L, "斗兽棋");
    GAME_QUEST.put(30031L, "扫雷");
    GAME_QUEST.put(30071L, "一站到底");
    GAME_QUEST.put(30091L, "连连看");
    GAME_QUEST.put(30141L, "黄金矿工");
    GAME_QUEST.put(30151L, "草原争霸");
    GAME_QUEST.put(30171L, "小熊快跑");
    GAME_QUEST.put(30191L, "最强小熊");
  }

  /**
   * TODO 每日任务.
   *
   * @param questId 任务ID
   * @return 每日任务
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  public static String getDailyQuest(Long questId) {
    String quest = null;
    if (GAME_QUEST.containsKey(questId)) {
      quest = GAME_QUEST.get(questId);
    }
    return quest;
  }
}
