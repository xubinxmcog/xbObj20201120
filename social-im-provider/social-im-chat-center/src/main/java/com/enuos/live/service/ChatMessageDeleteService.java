package com.enuos.live.service;

import com.enuos.live.pojo.ChatMessageDelete;
import com.enuos.live.result.Result;

import java.util.List;
import java.util.Map;

/**
 * TODO 聊天删除服务.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020-05-12 - 2020-07-28
 */

public interface ChatMessageDeleteService {

  /**
   * TODO 新增数据.
   *
   * @param list 用户列表
   * @author WangCaiWen
   * @date 2020/7/28
   */
  void insertMessage(List<Map<String, Object>> list);

  /**
   * TODO 新增数据.
   *
   * @param chatMessageDelete 新增信息
   * @author WangCaiWen
   * @date 2020/7/28
   */
  void insertChatMessageRelation(ChatMessageDelete chatMessageDelete);

  /**
   * TODO 更新标记.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result updateSignNum(Map<String, Object> params);

  /**
   * TODO 标记时间.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @return 标记时间
   * @author WangCaiWen
   * @date 2020/7/28
   */
  String queryMessageDateTime(Long userId, Long targetId);

  /**
   * TODO 删除数据,
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @author WangCaiWen
   * @date 2020/7/28
   */
  void deleteMessage(Long userId, Long targetId);

  /**
   * TODO 移除所有.
   *
   * @param userId 用户ID
   * @author WangCaiWen
   * @date 2020/7/28
   */
  void deleteMessageAll(Long userId);

}