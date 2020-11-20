package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.rest.SocketRemote;
import com.enuos.live.result.Result;
import com.enuos.live.service.GameService;
import com.enuos.live.service.InviteService;
import com.enuos.live.utils.JsonUtils;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * TODO 游戏邀请服务实现.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/7/20 21:02
 */

@Service("inviteService")
public class InviteServiceImpl implements InviteService {

  @Resource
  private SocketRemote socketRemote;
  @Resource
  private GameService gameService;

  /**
   * TODO 发送游戏邀请.
   *
   * @param params 参数
   * @return 发送结果
   * @author WangCaiWen
   * @since 2020/7/20 - 2020/7/20
   */
  @Override
  public Result sendGameInvite(Map<String, Object> params) {
    long gameId = ((Number) params.get("gameCode")).longValue();
    long roomId = ((Number) params.get("roomId")).longValue();
    long targetId = ((Number)params.get("targetId")).longValue();
    long userId = ((Number)params.get("userId")).longValue();
    if (gameId <= 0 || roomId <= 0 || targetId <= 0 || userId <= 0) {
      return Result.error(ErrorCode.QUESTIONS_PARAM);
    }
    Result result = this.gameService.getGameInfo(gameId);
    Map<String, Object> gameObject = JsonUtils.toObjectMap(result.getData());
    params.putAll(gameObject);
    // 调用通知
    return this.socketRemote.gameInviteNotice(params);
  }
}
