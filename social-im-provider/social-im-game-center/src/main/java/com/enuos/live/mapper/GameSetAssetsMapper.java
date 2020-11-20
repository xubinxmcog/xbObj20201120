package com.enuos.live.mapper;

import java.util.Map;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * TODO 游戏套装数据库访问层.
 *
 * @author wangcaiwen - missiw@163.com
 * @version 2.0
 * @since 2020-07-30 20:42:07
 */

public interface GameSetAssetsMapper {

  /**
   * TODO 游戏套装.
   *
   * @param productId 产品ID
   * @param gameId 游戏ID
   * @return 套装列表
   * @author WangCaiWen
   * @date 2020/7/30
   */
  List<Map<String, Object>> getGameSetAssetsList(@Param("productId") Long productId, @Param("gameId") Long gameId);

}