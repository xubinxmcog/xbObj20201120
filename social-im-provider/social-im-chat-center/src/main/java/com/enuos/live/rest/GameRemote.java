package com.enuos.live.rest;

import com.enuos.live.rest.fallback.GameRemoteFallback;
import com.enuos.live.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author WangCaiWen Created on 2020/4/24 9:34
 */
@Component
@FeignClient(name = "GAME-CENTER", fallback = GameRemoteFallback.class)
public interface GameRemote {

  /**
   * 获得游戏封面
   *
   * @param gameId the Game_Id
   * @return is Game_Cover
   */
  @RequestMapping(value = "/feign/game/getGameInfo", method = RequestMethod.GET)
  Result getGameInfo(@RequestParam("gameId") Long gameId);

}
