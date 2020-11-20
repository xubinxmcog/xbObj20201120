package com.enuos.live.service.impl;

import com.enuos.live.mapper.GamePlayerMapper;
import com.enuos.live.pojo.GamePlayer;
import com.enuos.live.result.Result;
import com.enuos.live.service.GamePlayerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * TODO 玩家服务实现.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V1.0.0
 * @since 2020-05-19 10:37
 */

@Service("gamePlayerService")
public class GamePlayerServiceImpl implements GamePlayerService {

  @Resource
  private GamePlayerMapper gamePlayerMapper;

  /**
   * TODO 加入房间.
   *
   * @param params [userId,gameId,roomId]
   * @return 加入结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/21 21:34
   * @update 2020/8/21 12:39
   */
  @Override
  public Result enterRoom(Map<String, Object> params) {
    Long gameCode = ((Number) params.get("gameId")).longValue();
    Long userId = ((Number) params.get("userId")).longValue();
    Long roomId = ((Number) params.get("roomId")).longValue();
    if (gameCode <= 0 || userId <= 0 || roomId <= 0) {
      return Result.error();
    }
    GamePlayer gamePlayer = new GamePlayer();
    gamePlayer.setRoomId(roomId);
    gamePlayer.setGameCode(gameCode);
    gamePlayer.setUserId(userId);
    this.gamePlayerMapper.insertPlayer(gamePlayer);
    return Result.success();
  }

  /**
   * TODO 离开房间.
   *
   * @param userId 用户ID
   * @return 离开结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/21 21:34
   * @update 2020/8/21 12:37
   */
  @Override
  public Result leaveRoom(Long userId) {
    this.gamePlayerMapper.deletePlayer(userId);
    return Result.success();
  }

  /**
   * TODO 删除房间.
   *
   * @param roomId 房间ID
   * @return 删除结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/21 21:34
   * @update 2020/8/21 12:36
   */
  @Override
  public Result deleteRoom(Long roomId) {
    this.gamePlayerMapper.deletePlayers(roomId);
    return Result.success();
  }
}