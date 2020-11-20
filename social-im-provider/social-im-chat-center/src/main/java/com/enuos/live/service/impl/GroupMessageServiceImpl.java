package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.pojo.GroupMember;
import com.enuos.live.mapper.GroupMessageMapper;
import com.enuos.live.pojo.GroupMessage;
import com.enuos.live.pojo.GroupMessageFile;
import com.enuos.live.rest.SocketRemote;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.service.*;
import com.enuos.live.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/17 17:23
 */
@Slf4j
@Service("groupMessageService")
public class GroupMessageServiceImpl implements GroupMessageService {

  @Resource
  private GroupMessageMapper groupMessageMapper;
  @Resource
  private GroupMessageFileService groupMessageFileService;
  @Resource
  private UserRemote userRemote;
  @Resource
  private SocketRemote socketRemote;
  @Resource
  private GroupMessageDeleteService groupMessageDeleteService;
  @Resource
  private GroupMemberService groupMemberService;

  @Override
  public Result newGroupMessage(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error();
    }
    Long groupId = ((Number) params.get("groupId")).longValue();
    Long sendId = ((Number) params.get("sendId")).longValue();
    if (groupId <= 0 || sendId <= 0) {
      return Result.error();
    }
    GroupMessage groupMessage = new GroupMessage();
    groupMessage.setGroupId(groupId);
    groupMessage.setSendId(sendId);
    groupMessage.setSendName(StringUtils.nvl(params.get("sendName")));
    // 聊天类型 0-文本 1-Emoji 2-图片 3-语音 4-视频
    Integer messageType = (Integer) params.get("messageType");
    groupMessage.setMessageType(messageType);
    if (messageType == 0) {
      groupMessage.setMessage(StringUtils.nvl(params.get("message")));
    }
    if (messageType == 1) {
      groupMessage.setMessageEmojiUrl(StringUtils.nvl(params.get("emojiUrl")));
      groupMessage.setMessageEmojiName(StringUtils.nvl(params.get("emoji")));
    }
    groupMessage.setMessageId(StringUtils.nvl(params.get("messageId")));
    groupMessage.setMessageSort(0);
    DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime ldt = LocalDateTime.parse(StringUtils.nvl(params.get("sendTime")), df);
    groupMessage.setCreateTime(ldt);
    this.groupMessageMapper.newGroupMessage(groupMessage);
    if (messageType > 1) {
      params.put("recordId", groupMessage.getId());
      this.groupMessageFileService.newGroupMessageFile(params);
    }
    return Result.success(groupMessage.getId());
  }

  @Override
  public void newGroupMessageBySort(Map<String, Object> params) {
    if (params != null && !params.isEmpty()) {
      Integer sort = (Integer) params.get("sort");
      if (sort == 1) {
        GroupMessage groupMessage = new GroupMessage();
        groupMessage.setGroupId(((Number) params.get("groupId")).longValue());
        groupMessage.setMessage("欢迎\"" + StringUtils.nvl(params.get("message")) + "\"加入群聊");
        groupMessage.setMessageSort(1);
        groupMessage.setMessageType(0);
        groupMessage.setCreateTime(LocalDateTime.now());
        this.groupMessageMapper.newGroupMessageBySort(groupMessage);
        // 更新群末尾消息
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(groupMessage.getGroupId());
        groupMember.setMessageId(groupMessage.getId());
        groupMember.setMessageTime(groupMessage.getCreateTime());
        this.groupMemberService.updateGroupLastMessage(groupMember);
        Map<String, Object> groupNotice = Maps.newHashMap();
        groupNotice.put("groupId", groupMessage.getGroupId());
        groupNotice.put("message", groupMessage.getMessage());
        this.socketRemote.groupNoticeMessage(groupNotice);
      } else {
        GroupMessage groupMessage = new GroupMessage();
        groupMessage.setGroupId(((Number) params.get("groupId")).longValue());
        groupMessage.setMessage("\"" + StringUtils.nvl(params.get("message")) + "\"离开群聊");
        groupMessage.setMessageSort(1);
        groupMessage.setMessageType(0);
        groupMessage.setCreateTime(LocalDateTime.now());
        this.groupMessageMapper.newGroupMessageBySort(groupMessage);
        // 更新群末尾消息
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(groupMessage.getGroupId());
        groupMember.setMessageId(groupMessage.getId());
        groupMember.setMessageTime(groupMessage.getCreateTime());
        this.groupMemberService.updateGroupLastMessage(groupMember);
        Map<String, Object> groupNotice = Maps.newHashMap();
        groupNotice.put("groupId", groupMessage.getGroupId());
        groupNotice.put("message", groupMessage.getMessage());
        this.socketRemote.groupNoticeMessage(groupNotice);
      }
    }
  }

  @Override
  public Result getGroupMessage(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    long groupId = ((Number) params.get("groupId")).longValue();
    long userId = ((Number) params.get("userId")).longValue();
    if (groupId <= 0 || userId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    params.put("indexTime", this.groupMessageDeleteService.queryMessageDateTime(groupId, userId));
    PageHelper.startPage((Integer) params.get("pageNum"), (Integer) params.get("pageSize"));
    List<Map<String, Object>> messageList = this.groupMessageMapper.getGroupMessageList(params);
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
   * @return 消息列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/11 19:02
   * @update 2020/8/11 19:02
   */
  private List<Map<String, Object>> sortMessage(List<Map<String, Object>> messageList) {
    if (CollectionUtils.isNotEmpty(messageList)) {
      GroupMessageFile groupMessageFile;
      Map<String, Object> resultMessage;
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      for (Map<String, Object> message : messageList) {
        resultMessage = Maps.newHashMap();
        long userId = ((Number) message.get("userId")).longValue();
        if (userId > 0) {
          Map<String, Object> userDress = this.userRemote.getUserFrame(userId);
          if (userDress != null) {
            userDress.put("iconURL", userDress.get("iconUrl"));
            userDress.remove("iconUrl");
            message.put("messageOwner", userDress);
          }
        }
        Long recordId = ((Number) message.get("recordId")).longValue();
        Integer messageType = (Integer) message.get("messageType");
        switch (messageType) {
          case 1:
            resultMessage.put("emName", message.get("emojiName"));
            resultMessage.put("animURL", message.get("emojiUrl"));
            message.put("emojiInfo", resultMessage);
            break;
          case 2:
            groupMessageFile = this.groupMessageFileService.getMessageFileInfo(recordId);
            resultMessage.put("width", groupMessageFile.getFileWidth());
            resultMessage.put("height", groupMessageFile.getFileHeight());
            resultMessage.put("imageUrl", groupMessageFile.getFileUrl() + "?p=0");
            resultMessage.put("litimg", groupMessageFile.getFileUrl() + "?w=400");
            message.put("imageInfo", resultMessage);
            break;
          case 3:
            groupMessageFile = this.groupMessageFileService.getMessageFileInfo(recordId);
            resultMessage.put("duration", groupMessageFile.getFileDuration());
            resultMessage.put("voiceUrl", groupMessageFile.getFileUrl());
            message.put("voiceInfo", resultMessage);
            break;
          case 4:
            groupMessageFile = this.groupMessageFileService.getMessageFileInfo(recordId);
            resultMessage.put("width", groupMessageFile.getFileWidth());
            resultMessage.put("height", groupMessageFile.getFileHeight());
            resultMessage.put("duration", groupMessageFile.getFileDuration());
            resultMessage.put("videoUrl", groupMessageFile.getFileUrl());
            resultMessage.put("coverUrl", groupMessageFile.getFileCoverUrl());
            message.put("videoInfo", resultMessage);
            break;
          default:
            break;
        }
        Timestamp time = (Timestamp) message.get("createTime");
        message.put("createTime", dtf.format(time.toLocalDateTime()));
        message.remove("emojiUrl");
        message.remove("emojiName");
      }
    }
    return messageList;
  }

  @Override
  public void deleteGroupMessage(Long groupId) {
    this.groupMessageMapper.deleteGroupMessage(groupId);
  }

}
