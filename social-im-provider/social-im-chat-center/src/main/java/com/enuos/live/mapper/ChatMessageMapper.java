package com.enuos.live.mapper;

import com.enuos.live.pojo.ChatMessage;

import java.util.List;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/15 16:43
 */
public interface ChatMessageMapper {

  /**
   * 新增聊天记录
   *
   * @param chatMessage 记录参数
   */
  void newChatMessage(ChatMessage chatMessage);

  void updateMessageAction(Long recordId);

  /**
   * 获得聊天记录
   *
   * @param params the User_Id && Target_Id
   * @return is Message_List
   */
  List<Map<String, Object>> getChatMessageList(Map<String, Object> params);

}
