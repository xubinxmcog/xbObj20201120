package com.enuos.live.service.impl;

import com.enuos.live.constants.GameInvite;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.ChatMessageMapper;
import com.enuos.live.pojo.ChatMessage;
import com.enuos.live.pojo.ChatMessageFile;
import com.enuos.live.pojo.ChatMessageInvite;
import com.enuos.live.result.Result;
import com.enuos.live.service.*;
import com.enuos.live.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * TODO 聊天消息服务实现.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/4/15 9:24
 */

@Service("chatMessageService")
public class ChatMessageServiceImpl implements ChatMessageService {

  @Resource
  private ChatMessageMapper chatMessageMapper;
  @Resource
  private ChatMessageInviteService chatMessageInviteService;
  @Resource
  private ChatMessageFileService chatMessageFileService;
  @Resource
  private ChatMessageDeleteService chatMessageDeleteService;

  private static final int INDEX_TWO = 2;
  private static final int INDEX_THREE = 3;
  private static final int INDEX_FIVE = 5;

  @Override
  public long newAddMessage(Long userId, Long targetId, Integer type) {
    ChatMessage chatMessage = new ChatMessage();
    chatMessage.setUserId(userId);
    chatMessage.setTargetId(targetId);
    String messageId = userId + "_to_" + targetId + "_" + System.currentTimeMillis();
    chatMessage.setMessageId(messageId);
    if (type == 8) {
      chatMessage.setMessage("添加好友成功！");
    } else {
      chatMessage.setMessage("Hi！我加你好友了！");
    }
    chatMessage.setMessageType(type);
    chatMessage.setMessageRead(1);
    chatMessage.setMessageAction(1);
    chatMessage.setCreateTime(LocalDateTime.now());
    this.chatMessageMapper.newChatMessage(chatMessage);
    return chatMessage.getId();
  }

  /**
   * TODO 新增聊天信息.
   *
   * @param params 聊天信息
   * @return 新增结果
   * @author WangCaiWen
   * @since 2020/4/15 - 2020/4/15
   */
  @Override
  public Result newChatMessage(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long userId = ((Number) params.get("userId")).longValue();
    Long targetId = ((Number) params.get("targetId")).longValue();
    if (userId <= 0 || targetId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    // 消息类型 0=文本 1=Emoji 2=图片 3=语音 4=视频 5=游戏邀请
    Integer messageType = (Integer) params.get("messageType");
    ChatMessage chatMessage = new ChatMessage();
    chatMessage.setMessageType(messageType);
    chatMessage.setUserId(userId);
    chatMessage.setTargetId(targetId);
    if (messageType == 0) {
      chatMessage.setMessage(StringUtils.nvl(params.get("message")));
    }
    if (messageType == 1) {
      chatMessage.setMessageEmojiUrl(StringUtils.nvl(params.get("emojiUrl")));
      chatMessage.setMessageEmojiName(StringUtils.nvl(params.get("emoji")));
    }
    chatMessage.setMessageId(StringUtils.nvl(params.get("messageId")));
    chatMessage.setMessageRead((Integer) params.get("messageRead"));
    // 语音标记
    if (messageType == INDEX_THREE) {
      chatMessage.setMessageAction(0);
    }
    // 转换时间
    DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime ldt = LocalDateTime.parse(StringUtils.nvl(params.get("sendTime")), df);
    chatMessage.setCreateTime(ldt);
    // 保存消息
    this.chatMessageMapper.newChatMessage(chatMessage);
    params.put("recordId", chatMessage.getId());
    if (messageType >= INDEX_TWO && messageType < INDEX_FIVE) {
      // 文件信息
      this.chatMessageFileService.newChatMessageFile(params);
    } else if (messageType == INDEX_FIVE) {
      // 邀请信息
      this.chatMessageInviteService.newChatMessageInvite(params);
    }
    return Result.success(chatMessage.getId());
  }

  /**
   * TODO 获得聊天记录.
   *
   * @param params 目标参数
   * @return 聊天记录
   * @author WangCaiWen
   * @since 2020/4/15 - 2020/4/15
   */
  @Override
  public Result getChatMessage(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    long userId = ((Number) params.get("userId")).longValue();
    long targetId = ((Number) params.get("targetId")).longValue();
    if (userId <= 0 || targetId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    params.put("indexTime", this.chatMessageDeleteService.queryMessageDateTime(userId, targetId));
    PageHelper.startPage((Integer) params.get("pageNum"), (Integer) params.get("pageSize"));
    List<Map<String, Object>> messageList = this.chatMessageMapper.getChatMessageList(params);
    PageInfo<Map<String, Object>> messagePageInfo = new PageInfo<>(sortMessage(messageList));
    Map<String, Object> result = Maps.newHashMap();
    result.put("list", messagePageInfo.getList());
    result.put("total", messagePageInfo.getTotal());
    result.put("pageNum", messagePageInfo.getPageNum());
    result.put("pageSize", messagePageInfo.getPageSize());
    result.put("pages", messagePageInfo.getPages());
    return Result.success(result);
  }

  /**
   * TODO 分拣消息.
   *
   * @param messageList 消息列表
   * @author WangCaiWen
   * @since 2020/7/21 - 2020/7/21
   */
  private List<Map<String, Object>> sortMessage(List<Map<String, Object>> messageList) {
    if (CollectionUtils.isNotEmpty(messageList)) {
      ChatMessageFile chatMessageFile;
      ChatMessageInvite chatMessageInvite;
      Map<String, Object> resultMessage;
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      for (Map<String, Object> objectMap : messageList) {
        resultMessage = Maps.newHashMap();
        Long recordId = (Long) objectMap.get("recordId");
        Integer messageType = (Integer) objectMap.get("messageType");
        // 消息类型 0=文本 1=Emoji 2=图片 3=语音 4=视频 5=游戏邀请
        switch (messageType) {
          // Emoji
          case 1:
            resultMessage.put("emName", objectMap.get("emojiName"));
            resultMessage.put("animURL", objectMap.get("emojiUrl"));
            objectMap.put("emojiInfo", resultMessage);
            break;
          // 图片
          case 2:
            chatMessageFile = this.chatMessageFileService.getMessageFileInfo(recordId);
            if (chatMessageFile != null) {
              resultMessage.put("width", chatMessageFile.getFileWidth());
              resultMessage.put("height", chatMessageFile.getFileHeight());
              resultMessage.put("imageURL", chatMessageFile.getFileUrl() + "?p=0");
              resultMessage.put("litimg", chatMessageFile.getFileUrl() + "?w=400");
              objectMap.put("imageInfo", resultMessage);
            }
            break;
          // 语音
          case 3:
            chatMessageFile = this.chatMessageFileService.getMessageFileInfo(recordId);
            if (chatMessageFile != null) {
              resultMessage.put("duration", chatMessageFile.getFileDuration());
              resultMessage.put("voiceURL", chatMessageFile.getFileUrl());
              objectMap.put("voiceInfo", resultMessage);
            }
            break;
          // 视频
          case 4:
            chatMessageFile = this.chatMessageFileService.getMessageFileInfo(recordId);
            if (chatMessageFile != null) {
              resultMessage.put("width", chatMessageFile.getFileWidth());
              resultMessage.put("height", chatMessageFile.getFileHeight());
              resultMessage.put("duration", chatMessageFile.getFileDuration());
              resultMessage.put("videoURL", chatMessageFile.getFileUrl());
              resultMessage.put("coverURL", chatMessageFile.getFileCoverUrl() + "?w=400");
              objectMap.put("videoInfo", resultMessage);
            }
            break;
          // 游戏邀请
          case 5:
            chatMessageInvite = this.chatMessageInviteService.getMessageInviteInfo(recordId);
            if (chatMessageInvite != null) {
              // [0-双人 1-多人]
              if (GameInvite.isDouble(chatMessageInvite.getGameId())) {
                resultMessage.put("sort", 1);
              } else {
                resultMessage.put("sort", 0);
              }
              resultMessage.put("roomId", chatMessageInvite.getRoomId());
              resultMessage.put("gameCode", chatMessageInvite.getGameId());
              resultMessage.put("inviteTitle", chatMessageInvite.getInviteTitle());
              resultMessage.put("inviteImageURL", chatMessageInvite.getInviteImage());
              resultMessage.put("inviteStatus", chatMessageInvite.getInviteStatus());
              resultMessage.put("acceptStatus", chatMessageInvite.getAcceptStatus());
              objectMap.put("inviteInfo", resultMessage);
            }
            break;
          default:
            break;
        }
        Timestamp time = (Timestamp) objectMap.get("createTime");
        objectMap.put("createTime", dtf.format(time.toLocalDateTime()));
        objectMap.remove("emojiUrl");
        objectMap.remove("emojiName");
      }
    }
    return messageList;
  }

  @Override
  public Result listenToVoice(Map<String, Object> params) {
    Long recordId = ((Number) params.get("recordId")).longValue();
    this.chatMessageMapper.updateMessageAction(recordId);
    return Result.success();
  }

}
