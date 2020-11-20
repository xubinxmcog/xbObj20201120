package com.enuos.live.mapper;

import java.util.List;
import java.util.Map;

/**
 * 游戏管理(Game)表数据库访问层
 *
 * @author WangCaiWen
 * @since 2020-05-11 09:35:00
 */
public interface GameMapper {

  /**
   * 获取全部游戏
   *
   * @param params 激活状态 用户等级 开启青少年模式
   * @return gameList
   */
  List<Map<String, Object>> getHomeGameList(Map<String, Object> params);

  /**
   * 获得游戏信息
   *
   * @param gameId 游戏ID
   * @return info
   */
  Map<String, Object> getGameInfo(Long gameId);

  /**
   * 获取全部游戏
   *
   * @return gameList
   */
  List<Map<String, Object>> getGameList();
}
