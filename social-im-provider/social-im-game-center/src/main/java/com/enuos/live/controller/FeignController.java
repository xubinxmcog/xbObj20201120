package com.enuos.live.controller;

import com.enuos.live.result.Result;
import com.enuos.live.service.GamePlayTimeService;
import com.enuos.live.service.GamePlayerService;
import com.enuos.live.service.GameRobotService;
import com.enuos.live.service.GameService;
import com.enuos.live.service.GameSetAssetsService;
import com.enuos.live.service.GameUserRecordService;
import com.enuos.live.service.Thesaurus30051Service;
import com.enuos.live.service.Thesaurus30061Service;
import com.enuos.live.service.Thesaurus30071Service;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO 远程调用(Feign)控制器.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/5/9 13:22
 */

@RestController
@RequestMapping("/feign")
public class FeignController {

  /**
   * 数据服务.
   */
  @Resource
  private GameService gameService;
  @Resource
  private GamePlayerService gamePlayerService;
  @Resource
  private GamePlayTimeService gamePlayTimeService;
  @Resource
  private GameSetAssetsService gameSetAssetsService;
  @Resource
  private GameUserRecordService gameUserRecordService;
  /**
   * 游戏服务.
   */
  @Resource
  private Thesaurus30051Service thesaurus30051Service;
  @Resource
  private Thesaurus30061Service thesaurus30061Service;
  @Resource
  private Thesaurus30071Service thesaurus30071Service;
  /**
   * 机器人.
   */
  @Resource
  private GameRobotService gameRobotService;

  /**
   * TODO 获得游戏详情.
   *
   * @param gameId 游戏ID
   * @return 游戏信息
   * @author WangCaiWen
   * @since 2020/5/9 - 2020/5/9
   */
  @RequestMapping(value = "/game/getGameInfo", method = RequestMethod.GET)
  public Result getGameInfo(@RequestParam("gameId") Long gameId) {
    return this.gameService.getGameInfo(gameId);
  }

  /**
   * TODO 加入游戏.
   *
   * @param params map参数
   * @return 加入结果
   * @author WangCaiWen
   * @since 2020/5/9 - 2020/5/9
   */
  @RequestMapping(value = "/game/enterRoom", method = RequestMethod.POST)
  public Result enterRoom(@RequestBody Map<String, Object> params) {
    return this.gamePlayerService.enterRoom(params);
  }

  /**
   * TODO 离开房间.
   *
   * @param userId 用户ID
   * @return 离开结果
   * @author WangCaiWen
   * @since 2020/5/9 - 2020/5/9
   */
  @RequestMapping(value = "/game/leaveRoom", method = RequestMethod.POST)
  public Result leaveRoom(@RequestParam("userId") Long userId) {
    return this.gamePlayerService.leaveRoom(userId);
  }


  /**
   * TODO 移除房间，并移除关于房间的所有玩家.
   *
   * @param roomId 房间ID
   * @return 删除结果
   * @author WangCaiWen
   * @since 2020/5/9 - 2020/5/9
   */
  @RequestMapping(value = "/game/deleteRoom", method = RequestMethod.POST)
  public Result deleteRoom(@RequestParam("roomId") Long roomId) {
    return this.gamePlayerService.deleteRoom(roomId);
  }

  /**
   * TODO 根据玩家游玩游戏，刷新游玩次数.
   *
   * @param params map参数
   * @return 刷新结果
   * @author WangCaiWen
   * @since 2020/5/9 - 2020/5/9
   */
  @RequestMapping(value = "/game/refreshUserRecord", method = RequestMethod.POST)
  public Result refreshUserRecord(@RequestBody Map<String, Object> params) {
    return this.gameUserRecordService.refreshUserRecord(params);
  }

  /**
   * TODO 获得谁是卧底词汇.
   *
   * @return words
   * @author WangCaiWen
   * @since 2020/7/1 - 2020/7/1
   */
  @RequestMapping(value = "/game/getWhoIsSpyWords", method = RequestMethod.GET)
  public Result getWhoIsSpyWords() {
    return this.thesaurus30051Service.getWhoIsSpyWords();
  }

  /**
   * TODO 获得一站到底题目.
   *
   * @return problem
   * @author WangCaiWen
   * @since 2020/7/8 - 2020/7/8
   */
  @RequestMapping(value = "/game/getMustStandProblem", method = RequestMethod.GET)
  public Result getMustStandProblem() {
    return this.thesaurus30071Service.getMustStandProblem();
  }


  /**
   * TODO 获得你说我猜词汇.
   *
   * @return words 词汇列表
   * @author WangCaiWen - 1443710411@qq.com
   * @date 2020/8/4 13:13
   * @since 2020/8/4 13:13
   */
  @RequestMapping(value = "/game/getGuessedSaidWords", method = RequestMethod.GET)
  public Result getGuessedSaidWords() {
    return this.thesaurus30061Service.getGuessedSaidWords();
  }

  /**
   * TODO 游戏套装.
   *
   * @param productId 产品ID
   * @param gameId 游戏ID
   * @return 套装列表
   * @author WangCaiWen
   * @date 2020/7/30
   */
  @GetMapping(value = "/game/getGameSetAssetsList")
  public Result getGameSetAssetsList(@RequestParam("productId") Long productId, @RequestParam("gameId") Long gameId) {
    return this.gameSetAssetsService.getGameSetAssetsList(productId, gameId);
  }

  /**
   * TODO 获得机器人信息.
   *
   * @return 机器人信息
   * @author wangcaiwen|1443710411@qq.com
   * @create 2020/9/11 10:24
   * @update 2020/9/11 10:24
   */
  @GetMapping(value = "/game/getRandomGameRobot")
  public Result getRandomGameRobot(@RequestParam("number") Integer number) {
    return this.gameRobotService.getRandomGameRobot(number);
  }

  /**
   * TODO 更新时间.
   *
   * @param params [userId, playTime]
   * @return [更新结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 15:28
   * @update 2020/11/5 15:28
   */
  @PostMapping(value = "/game/updatePlayTime")
  public Result updatePlayTime(@RequestBody Map<String, Object> params) {
    return this.gamePlayTimeService.update(params);
  }

  /**
   * TODO 获得时间.
   *
   * @param userId [用户ID]
   * @return [查询结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 15:26
   * @update 2020/11/5 15:26
   */
  @GetMapping(value = "/game/playGameTime")
  public Result playGameTime(@RequestParam("userId") Long userId) {
    return this.gamePlayTimeService.queryTimeByUserId(userId);
  }

}
