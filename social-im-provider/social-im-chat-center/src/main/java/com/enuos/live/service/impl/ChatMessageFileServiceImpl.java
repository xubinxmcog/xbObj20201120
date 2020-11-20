package com.enuos.live.service.impl;

import com.enuos.live.mapper.ChatMessageFileMapper;
import com.enuos.live.pojo.ChatMessageFile;
import com.enuos.live.service.ChatMessageFileService;
import com.enuos.live.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 聊天文件(ChatMessageFile)表服务实现类
 *
 * @author WangCaiWen
 * @since 2020-05-12 16:16:15
 */
@Slf4j
@Service("chatMessageFileService")
public class ChatMessageFileServiceImpl implements ChatMessageFileService {

  @Resource
  private ChatMessageFileMapper chatMessageFileMapper;

  @Override
  public void newChatMessageFile(Map<String, Object> params) {
    Integer messageType = (Integer) params.get("messageType");
    Long recordId = ((Number) params.get("recordId")).longValue();
    ChatMessageFile chatMessageFile = new ChatMessageFile();
    chatMessageFile.setRecordId(recordId);
    switch (messageType) {
      case 2:
        chatMessageFile.setFileType(0);
        chatMessageFile.setFileWidth((Integer) params.get("fileWidth"));
        chatMessageFile.setFileHeight((Integer) params.get("fileHeight"));
        chatMessageFile.setFileUrl(StringUtils.nvl(params.get("fileUrl")));
        break;
      case 3:
        chatMessageFile.setFileType(1);
        chatMessageFile.setFileDuration((Integer) params.get("fileDuration"));
        chatMessageFile.setFileUrl(StringUtils.nvl(params.get("fileUrl")));
        break;
      default:
        chatMessageFile.setFileType(2);
        chatMessageFile.setFileWidth((Integer) params.get("fileWidth"));
        chatMessageFile.setFileHeight((Integer) params.get("fileHeight"));
        chatMessageFile.setFileDuration((Integer) params.get("fileDuration"));
        chatMessageFile.setFileUrl(StringUtils.nvl(params.get("fileUrl")));
        chatMessageFile.setFileCoverUrl(StringUtils.nvl(params.get("coverUrl")));
        break;
    }
    this.chatMessageFileMapper.newChatMessageFile(chatMessageFile);
  }

  @Override
  public ChatMessageFile getMessageFileInfo(Long recordId) {
    return this.chatMessageFileMapper.getMessageFileInfo(recordId);
  }
}
