package com.enuos.live.service;

import com.enuos.live.pojo.ChatMessageFile;

import java.util.Map;

/**
 * 聊天文件(ChatMessageFile)表服务接口
 *
 * @author WangCaiWen
 * @since 2020-05-12 16:16:15
 */
public interface ChatMessageFileService {

  void newChatMessageFile(Map<String, Object> params);

  ChatMessageFile getMessageFileInfo(Long recordId);
}
