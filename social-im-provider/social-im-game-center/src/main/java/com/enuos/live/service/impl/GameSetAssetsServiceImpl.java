package com.enuos.live.service.impl;

import com.enuos.live.mapper.GameSetAssetsMapper;
import com.enuos.live.result.Result;
import com.enuos.live.service.GameSetAssetsService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * TODO 游戏套装实现类.
 *
 * @author wangcaiwen - missiw@163.com
 * @version 2.0
 * @since 2020-07-30 20:42:08
 */

@Service("gameSetAssetsService")
public class GameSetAssetsServiceImpl implements GameSetAssetsService {

  @Resource
  private GameSetAssetsMapper gameSetAssetsMapper;

  /**
   * TODO 游戏套装.
   *
   * @param productId 产品ID
   * @param gameId 游戏ID
   * @return 套装列表
   * @author WangCaiWen
   * @date 2020/7/30
   */
  @Override
  public Result getGameSetAssetsList(Long productId, Long gameId) {
    return Result.success(this.gameSetAssetsMapper.getGameSetAssetsList(productId, gameId));
  }
}