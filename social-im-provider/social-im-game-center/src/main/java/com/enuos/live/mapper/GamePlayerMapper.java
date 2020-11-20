package com.enuos.live.mapper;

import com.enuos.live.pojo.GamePlayer;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (GamePlayer)表数据库访问层
 *
 * @author makejava
 * @since 2020-05-19 10:37:52
 */
public interface GamePlayerMapper {

  /**
   * 新增数据.
   *
   * @param gamePlayer 实例对象
   */
  void insertPlayer(GamePlayer gamePlayer);

  /**
   * 移除玩家.
   *
   * @param roomId 房间ID
   */
  void deletePlayers(Long roomId);

  /**
   * 离开游戏.
   *
   * @param userId 玩家ID
   */
  void deletePlayer(Long userId);

}