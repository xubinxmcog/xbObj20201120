package com.enuos.live.service;

import com.enuos.live.result.Result;

import java.util.List;
import java.util.Map;

/**
 * 用户接收语音消息操作(GroupMessageVoice)表服务接口.
 *
 * @author WangCaiWen
 * @since 2020-05-13 15:31:32
 */
public interface GroupMessageVoiceService {

  /**
   * 新增数据.
   *
   * @param params 实例对象
   * @return 新增结果
   */
  Result insertMessageUnread(Map<String, Object> params);

  /**
   * 聆听语音.
   *
   * @param params 实例对象
   * @return 更新结果
   */
  Result listenToVoiceGroup(Map<String, Object> params);

  /**
   * 删除聊天记录
   *
   * @param groupId 群聊ID
   */
  void deleteGroupMessage(Long groupId);

  /**
   * 删除语音记录
   *
   * @param groupId 群聊ID
   * @param userId 用户ID
   */
  void deleteGroupVoiceMessage(Long groupId, Long userId);

  /**
   * 批量删除语音记录
   *
   * @param groupId 群聊ID
   * @param list 用户ID列表
   */
  void deleteGroupVoiceMessageByList(Long groupId, List<Long> list);
}