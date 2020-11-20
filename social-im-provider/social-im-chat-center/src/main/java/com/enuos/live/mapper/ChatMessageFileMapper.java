package com.enuos.live.mapper;

import com.enuos.live.pojo.ChatMessageFile;

public interface ChatMessageFileMapper {

  void newChatMessageFile(ChatMessageFile chatMessageFile);

  ChatMessageFile getMessageFileInfo(Long recordId);
}
