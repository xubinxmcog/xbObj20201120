package com.enuos.live.service;

import com.enuos.live.result.Result;
import java.util.Map;

/**
 * TODO 单聊服务.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version 1.0
 * @since 2020/4/9 - 2020/7/28
 */

public interface ChatService {

  /**
   * TODO 获得信息列表.
   *
   * @param params 分页参数等
   * @return 信息列表
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result getChatNoticeList(Map<String, Object> params);

  /**
   * TODO 更新聊天设置.
   *
   * @param params 更新信息.
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result updUserChatSetting(Map<String, Object> params);

  /**
   * TODO 获得聊天设置.
   *
   * @param params 用户信息.
   * @return 聊天设置
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result getUserChatSetting(Map<String, Object> params);

  /**
   * TODO 聊天状态.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @return 聊天状态
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result getUserChatStatus(Long userId, Long targetId);

  /**
   * TODO 更新末尾信息.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result updateChatLastMessage(Map<String, Object> params);

  /**
   * TODO 通知状态.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @return 通知状态
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result getUserNoticeStatus(Long userId, Long targetId);

  /**
   * TODO 建立关系.
   *
   * @param params 用户信息
   * @return 建立结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result buildRelationships(Map<String, Object> params);

  /**
   * TODO 更新展示.
   *
   * @param params 用户信息
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result updateExhibition(Map<String, Object> params);

  /**
   * TODO 清除信息.
   *
   * @param userId 用户ID
   * @return 清除结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result logoutToDeleteChat(Long userId);

  /**
   * TODO 清空信息.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @author WangCaiWen
   * @date 2020/7/28
   */
  void emptyResidualMessage(Long userId, Long targetId);

  /**
   * TODO 离开服务,
   *
   * @param userId 用户ID
   * @return 离开结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result leaveService(Long userId);

  /**
   * TODO 验证联系.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @return 聊天联系
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result userRelationIsExist(Long userId, Long targetId);
}
