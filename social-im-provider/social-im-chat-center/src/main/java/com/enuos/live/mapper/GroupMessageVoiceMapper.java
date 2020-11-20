package com.enuos.live.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/5/13 18:48
 */
public interface GroupMessageVoiceMapper {

  /**
   * 批量新增语音操作信息
   *
   * @param list userInfo
   */
  void saveUserMessageList(@Param("list") List<Map<String, Object>> list);

  /**
   * 更新消息状态
   *
   * @param params recordId groupId userId
   */
  void updateMessageVoice(Map<String, Object> params);

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
  void deleteGroupVoiceMessage(@Param("groupId") Long groupId, @Param("userId") Long userId);

  /**
   * 批量删除语音记录
   *
   * @param groupId 群聊ID
   * @param list 用户ID列表
   */
  void deleteGroupVoiceMessageByList(Long groupId, List<Long> list);
}
