package com.enuos.live.mapper;

import com.enuos.live.pojo.GamePlayTime;

/**
 * TODO 游玩时间数据访问层.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/11/5 15:32
 */

public interface GamePlayTimeMapper {

  /**
   * TODO 是否存在.
   *
   * @param userId [用户ID]
   * @return [查询结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 15:58
   * @update 2020/11/5 15:58
   */
  Integer findTimeIsExist(Long userId);

  /**
   * TODO 查询时间.
   *
   * @param userId [用户ID]
   * @return [查询结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 15:59
   * @update 2020/11/5 15:59
   */
  Long queryTimeByUserId(Long userId);
  
  /**
   * TODO 新增时间.
   *
   * @param gamePlayTime [实例对象]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 16:02
   * @update 2020/11/5 16:02
   */
  void insert(GamePlayTime gamePlayTime);

  /**
   * TODO 更新时间.
   *
   * @param gamePlayTime [实例对象]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/5 16:03
   * @update 2020/11/5 16:03
   */
  void update(GamePlayTime gamePlayTime);

}
