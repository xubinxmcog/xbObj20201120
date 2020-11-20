package com.enuos.live.service;

import com.enuos.live.pojo.GroupMessageFile;

import java.util.List;
import java.util.Map;

public interface GroupMessageFileService {

  void newGroupMessageFile(Map<String, Object> params);

  GroupMessageFile getMessageFileInfo(Long recordId);

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
