package com.enuos.live.service;

import com.enuos.live.result.Result;
import java.util.Map;

/**
 * TODO 游戏邀请服务接口.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/7/20 20:56
 */

public interface InviteService {

  /**
   * TODO 发送游戏邀请.
   *
   * @param params 参数
   * @return 发送结果
   * @author WangCaiWen
   * @since 2020/7/20 - 2020/7/20
   */
  Result sendGameInvite(Map<String, Object> params);
}
