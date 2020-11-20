package com.enuos.live.service;

import com.enuos.live.result.Result;

/**
 * TODO 游戏套装服务接口.
 *
 * @author wangcaiwen - missiw@163.com
 * @version 2.0
 * @since 2020-07-30 20:42:08
 */

public interface GameSetAssetsService {

  /**
   * TODO 游戏套装.
   *
   * @param productId 产品ID
   * @param gameId 游戏ID
   * @return 套装列表
   * @author WangCaiWen
   * @date 2020/7/30
   */
  Result getGameSetAssetsList(Long productId, Long gameId);
}