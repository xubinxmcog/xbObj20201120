package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.GamePlayTimeMapper;
import com.enuos.live.pojo.GamePlayTime;
import com.enuos.live.result.Result;
import com.enuos.live.service.GamePlayTimeService;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;


/**
 * TODO 游玩时间实现.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020-11-05 15:16:14
 */

@Service("gamePlayTimeService")
public class GamePlayTimeServiceImpl implements GamePlayTimeService {

  @Resource
  private GamePlayTimeMapper gamePlayTimeMapper;

  /**
   * TODO 更新时间.
   *
   * @param params [userId, playTime]
   * @return [更新结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 15:28
   * @update 2020/11/5 15:28
   */
  @Override
  public Result update(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.QUESTIONS_PARAM);
    }
    long userId = ((Number) params.get("userId")).longValue();
    long playTime = ((Number) params.get("playTime")).longValue();
    GamePlayTime gamePlayTime = new GamePlayTime();
    gamePlayTime.setUserId(userId);
    gamePlayTime.setTotalTime(playTime);
    Integer isExist = this.gamePlayTimeMapper.findTimeIsExist(userId);
    if (isExist == 0) {
      this.gamePlayTimeMapper.insert(gamePlayTime);
    } else {
      this.gamePlayTimeMapper.update(gamePlayTime);
    }
    return Result.success();
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
  @Override
  public Result queryTimeByUserId(Long userId) {
    Integer isExist = this.gamePlayTimeMapper.findTimeIsExist(userId);
    return isExist == 0 ? Result.success(0)
        : Result.success(this.gamePlayTimeMapper.queryTimeByUserId(userId));
  }
}