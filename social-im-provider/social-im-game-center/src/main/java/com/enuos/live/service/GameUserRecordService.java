package com.enuos.live.service;

import com.enuos.live.result.Result;

import java.util.Map;

/**
 * 用户游玩记录(GameUserRecord)表服务接口
 *
 * @author makejava
 * @since 2020-05-19 10:45:13
 */
public interface GameUserRecordService {

  /**
   * 最近游玩.
   *
   * @param params 用户ID
   * @return 游戏列表
   */
  Result getRecentlyGameList(Map<String, Object> params);

  /**
   * 常玩游戏.
   *
   * @param params 用户ID
   * @return 游戏列表
   */
  Result getFrequentlyGameList(Map<String, Object> params);

  /**
   * 根据玩家游玩游戏，刷新游玩次数.
   *
   * @param params map参数
   * @return 刷新结果
   */
  Result refreshUserRecord(Map<String, Object> params);

}