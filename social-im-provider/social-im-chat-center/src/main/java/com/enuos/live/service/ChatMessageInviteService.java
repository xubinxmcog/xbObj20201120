package com.enuos.live.service;

import com.enuos.live.pojo.ChatMessageInvite;
import com.enuos.live.result.Result;
import java.util.Map;

/**
 * TODO 游戏邀请服务.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/4/23 - 2020/7/28
 */

public interface ChatMessageInviteService {

  /**
   * TODO 新的邀请记录.
   *
   * @param params 邀请信息
   * @author WangCaiWen
   * @date 2020/7/28
   */
  void newChatMessageInvite(Map<String, Object> params);

  /**
   * TODO 获取消息邀请信息.
   *
   * @param recordId 记录ID
   * @return 邀请信息
   * @author WangCaiWen
   * @date 2020/7/28
   */
  ChatMessageInvite getMessageInviteInfo(Long recordId);

  /**
   * TODO 更新邀请记录.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result updateChatMessageInvite(Map<String, Object> params);


  /**
   * TODO 取消约战.
   *
   * @param params 记录列表
   * @return 取消结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result updateInviteStatus(Map<String, Object> params);

  /**
   * TODO 验证邀请.
   *
   * @param recordId 记录ID
   * @return 验证结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result clickToEnter(Long recordId);
}
