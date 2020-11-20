package com.enuos.live.rest;

import com.enuos.live.rest.impl.GameRemoteFallback;
import com.enuos.live.result.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

/**
 * TODO 游戏中心.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/4/16 - 2020/7/29
 */

@Component
@FeignClient(contextId = "gameRemote", name = "GAME-CENTER", fallback = GameRemoteFallback.class)
public interface GameRemote {

  /**
   * TODO 游戏房间.
   *
   * @param params 游戏信息
   * @return 新增结果
   * @author WangCaiWen
   * @date 2020/7/29
   */
  @RequestMapping(value = "/feign/game/insertRoom", method = RequestMethod.POST)
  Result insertRoom(@RequestBody Map<String, Object> params);

  /**
   * TODO 游戏详情.
   *
   * @param gameId 游戏ID
   * @return 游戏详情
   * @author WangCaiWen
   * @date 2020/7/29
   */
  @RequestMapping(value = "/feign/game/getGameInfo", method = RequestMethod.GET)
  Result getGameInfo(@RequestParam("gameId") Long gameId);


  /**
   * @MethodName: getInfo
   * @Description: TODO 宠物信息
   * @Param: [userId]
   * @Return: com.enuos.live.result.Result
   * @Author: xubin
   * @Date: 15:13 2020/8/24
  **/
  @GetMapping("/pets/getInfo")
  Result getPetsInfo(@RequestParam("userId") Long userId);

  /**
   * @MethodName: getOperation
   * @Description: TODO 获取操作
   * @Param: [userId, operation]
   * @Return: com.enuos.live.result.Result
   * @Author: xubin
   * @Date: 17:44 2020/8/26
   **/
  @GetMapping("/pets/getOperation")
  Result getOperation(@RequestParam("userId") Long userId, @RequestParam("operation") Integer operation);
}
