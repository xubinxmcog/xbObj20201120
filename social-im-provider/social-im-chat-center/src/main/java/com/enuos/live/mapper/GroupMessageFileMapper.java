package com.enuos.live.mapper;

import com.enuos.live.pojo.GroupMessageFile;

public interface GroupMessageFileMapper {

  void newGroupMessageFile(GroupMessageFile groupMessageFile);

  GroupMessageFile getMessageFileInfo(Long recordId);

  /**
   * 删除聊天记录
   *
   * @param groupId 群聊ID
   */
  void deleteGroupMessage(Long groupId);
}
