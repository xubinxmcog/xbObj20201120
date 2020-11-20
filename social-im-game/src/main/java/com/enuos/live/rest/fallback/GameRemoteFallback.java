package com.enuos.live.rest.fallback;

import com.enuos.live.handle.game.f30051.FindSpyPlan;
import com.enuos.live.handle.game.f30061.GuessedSaidPlan;
import com.enuos.live.rest.GameRemote;
import com.enuos.live.result.Result;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * TODO 熔断处理.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/5/15 21:09
 */

@Component
public class GameRemoteFallback implements GameRemote {

  /**
   * TODO 游戏详情.
   *
   * @param gameId [游戏ID]
   * @return [游戏详情]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/26 16:09
   * @update 2020/8/26 16:09
   */
  @Override
  public Result getGameInfo(Long gameId) {
    return null;
  }

  /**
   * TODO 加入游戏.
   *
   * @param params [userId,gameId,roomId]
   * @return [新增结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/18 6:09
   * @update 2020/8/21 10:17
   */
  @Override
  public Result enterRoom(Map<String, Object> params) {
    return null;
  }

  /**
   * TODO 离开房间.
   *
   * @param userId [用户ID]
   * @return [离开结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/18 21:10
   * @update 2020/8/21 10:20
   */
  @Override
  public Result leaveRoom(Long userId) {
    return null;
  }

  /**
   * TODO 移除房间.
   *
   * @param roomId [房间ID]
   * @return [移除结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/21 21:34
   * @update 2020/8/21 10:26
   */
  @Override
  public Result deleteRoom(Long roomId) {
    return null;
  }

  /**
   * TODO 游玩次数.
   *
   * @param params [userId,gameId,roomId]
   * @return [刷新结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/21 21:34
   * @update 2020/8/21 10:29
   */
  @Override
  public Result refreshUserRecord(Map<String, Object> params) {
    return null;
  }

  /**
   * TODO 谁是卧底词汇.
   *
   * @return [游戏词汇]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/1 21:30
   * @update 2020/8/21 10:31
   */
  @Override
  public Result getWhoIsSpyWords() {
    Map<String, Object> result = FindSpyPlan.getVocabulary();
    return Result.success(result);
  }

  /**
   * TODO 一站到底题目.
   *
   * @return [游戏题目]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/8 21:08
   * @update 2020/8/21 10:32
   */
  @Override
  public Result getMustStandProblem() {
    return null;
  }

  /**
   * TODO 你说我猜词汇.
   *
   * @return [词汇列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 13:13
   * @update 2020/8/21 10:33
   */
  @Override
  public Result getGuessedSaidWords() {
    List<Map<String, Object>> resultList = GuessedSaidPlan.getVocabulary();
    return Result.success(resultList);
  }

  /**
   * TODO 游戏资源套装.
   *
   * @param productId [产品ID]
   * @param gameId [游戏ID]
   * @return [游戏资源]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/30 21:28
   * @update 2020/8/21 10:35
   */
  @Override
  public Result getGameSetAssetsList(Long productId, Long gameId) {
    return null;
  }

  /**
   * TODO 获得机器人.
   *
   * @param number [机器数量]
   * @return [机器列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/11 10:24
   * @update 2020/9/11 10:24
   */
  @Override
  public Result getRandomGameRobot(Integer number) {
    return null;
  }

}
