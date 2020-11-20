package com.enuos.live.service;

import com.enuos.live.result.Result;
import java.util.Map;

/**
 * TODO 游戏管理服务接口.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020-05-19 09:25:45
 */

public interface GameService {

  /**
   * TODO 主页游戏列表.
   *
   * @param params 参数
   * @return 游戏列表
   * @author WangCaiWen
   * @since 2020/7/20 - 2020/7/20
   */
  Result getHomeGameList(Map<String, Object> params);

  /**
   * TODO 邀请游戏列表.
   * 
   * @param params 参数
   * @return 游戏列表
   * @author WangCaiWen
   * @since 2020/7/20 - 2020/7/20
   */
  Result getChatGameList(Map<String, Object> params);

  /**
   * TODO 获得游戏详情.
   * 
   * @param gameId 游戏ID
   * @return 游戏详情
   * @author WangCaiWen
   * @since 2020/7/20 - 2020/7/20
   */
  Result getGameInfo(Long gameId);

  /**
   * TODO 获得游戏详情.
   *
   * @param params [游戏ID]
   * @return [游戏详情]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/12 13:58
   * @update 2020/11/12 13:58
   */
  Result getGameInfo(Map<String, Object> params);

}