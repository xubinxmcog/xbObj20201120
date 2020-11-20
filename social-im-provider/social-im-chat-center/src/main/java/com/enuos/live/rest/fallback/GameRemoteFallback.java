package com.enuos.live.rest.fallback;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.rest.GameRemote;
import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/24 9:35
 */
@Slf4j
@Component
public class GameRemoteFallback implements GameRemote {

  @Override
  public Result getGameInfo(Long gameId) {
    return null;
  }
}
