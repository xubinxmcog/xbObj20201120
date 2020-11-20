package com.enuos.live.handle.activity;

import com.enuos.live.manager.ActivityEnum;
import com.enuos.live.manager.LoggerManager;
import com.enuos.live.rest.ActivityRemote;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * TODO 丹枫迎秋(2020).游戏触发.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/11/4 14:45
 */

@Component
public class MidAutumn2020 implements Activity {

  @Resource
  private ActivityRemote activityRemote;

  private static MidAutumn2020 ActivityTrigger;

  @PostConstruct
  public void init() {
    ActivityTrigger = this;
    ActivityTrigger.activityRemote = this.activityRemote;
  }

  /**
   * TODO 活动游玩.
   *
   * @param params [玩家ID, 触发CODE]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/4 16:29
   * @update 2020/11/4 16:29
   */
  @Override
  public void playGame(Object... params) {
    Long userId = (Long) params[0];
    Integer trigger = (Integer) params[1];
    Map<String, Object> activity = Maps.newHashMap();
    activity.put("userId", userId);
    activity.put("progress", 1);
    switch (trigger) {
      // 玩一次对战游戏
      case 1:
        activity.put("code", ActivityEnum.ACT000102.getCode());
        ActivityTrigger.activityRemote.openHandler(activity);
        break;
      // 玩一次互动游戏
      case 2:
        activity.put("code", ActivityEnum.ACT000103.getCode());
        ActivityTrigger.activityRemote.openHandler(activity);
        break;
      default:
        LoggerManager.warn("活动任务不存在!!!");
        break;
    }
  }
}
