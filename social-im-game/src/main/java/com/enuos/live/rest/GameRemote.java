package com.enuos.live.rest;

import com.enuos.live.rest.fallback.GameRemoteFallback;
import com.enuos.live.result.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * TODO 游戏中心.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/5/15 21:09
 */

@Component
@FeignClient(contextId = "gameTelnet", name = "GAME-CENTER", fallback = GameRemoteFallback.class)
public interface GameRemote {

  /**
   * TODO 游戏详情.
   *
   * @param gameId [游戏ID]
   * @return [游戏详情]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/26 16:09
   * @update 2020/8/26 16:09
   */
  @GetMapping(value = "/feign/game/getGameInfo")
  Result getGameInfo(@RequestParam("gameId") Long gameId);

  /**
   * TODO 加入游戏.
   *
   * @param params [userId,gameId,roomId]
   * @return [新增结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/18 6:09
   * @update 2020/8/21 10:17
   */
  @PostMapping(value = "/feign/game/enterRoom")
  Result enterRoom(@RequestBody Map<String, Object> params);

  /**
   * TODO 离开房间.
   *
   * @param userId [用户ID]
   * @return [离开结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/18 21:10
   * @update 2020/8/21 10:20
   */
  @PostMapping(value = "/feign/game/leaveRoom")
  Result leaveRoom(@RequestParam("userId") Long userId);

  /**
   * TODO 移除房间.
   *
   * @param roomId [房间ID]
   * @return [移除结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/21 21:34
   * @update 2020/8/21 10:26
   */
  @PostMapping(value = "/feign/game/deleteRoom")
  Result deleteRoom(@RequestParam("roomId") Long roomId);

  /**
   * TODO 游玩次数.
   *
   * @param params [userId,gameId,roomId]
   * @return [刷新结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/21 21:34
   * @update 2020/8/21 10:29
   */
  @PostMapping(value = "/feign/game/refreshUserRecord")
  Result refreshUserRecord(@RequestBody Map<String, Object> params);

  /**
   * TODO 谁是卧底词汇.
   *
   * @return [游戏词汇]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/1 21:30
   * @update 2020/8/21 10:31
   */
  @GetMapping(value = "/feign/game/getWhoIsSpyWords")
  Result getWhoIsSpyWords();

  /**
   * TODO 一站到底题目.
   *
   * @return [游戏题目]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/8 21:08
   * @update 2020/8/21 10:32
   */
  @GetMapping(value = "/feign/game/getMustStandProblem")
  Result getMustStandProblem();

  /**
   * TODO 你说我猜词汇.
   *
   * @return [词汇列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 13:13
   * @update 2020/8/21 10:33
   */
  @GetMapping(value = "/feign/game/getGuessedSaidWords")
  Result getGuessedSaidWords();

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
  @GetMapping(value = "/feign/game/getGameSetAssetsList")
  Result getGameSetAssetsList(@RequestParam("productId") Long productId, @RequestParam("gameId") Long gameId);

  /**
   * TODO 获得机器人.
   *
   * @param number [机器数量]
   * @return [机器列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/11 10:24
   * @update 2020/9/11 10:24
   */
  @GetMapping(value = "/feign/game/getRandomGameRobot")
  Result getRandomGameRobot(@RequestParam("number") Integer number);
}
