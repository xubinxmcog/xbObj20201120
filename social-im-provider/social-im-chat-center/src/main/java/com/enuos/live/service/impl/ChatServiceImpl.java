package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.ChatMapper;
import com.enuos.live.pojo.Chat;
import com.enuos.live.rest.SocketRemote;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.service.*;
import com.enuos.live.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.time.LocalDateTime;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * TODO 单聊服务实现.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/4/9 - 2020/7/28
 */

@Service("chatService")
public class ChatServiceImpl implements ChatService {

  @Resource
  private ChatMapper chatMapper;
  @Resource
  private UserRemote userRemote;
  @Resource
  private SocketRemote socketRemote;
  @Resource
  private GroupService groupService;
  @Resource
  private GroupMemberService groupMemberService;
  @Resource
  private ChatMessageService chatMessageService;
  @Resource
  private NoticeMemberService noticeMemberService;
  @Resource
  private NoticeInteractService noticeInteractService;
  @Resource
  private ChatMessageDeleteService chatMessageDeleteService;

  private static final String CHAT_STATUS = "chatStatus";
  private static final String FLAG_TOP = "flagTop";
  private static final String FLAG_DELETE = "flagDelete";
  private static final String NOT_DISTURB = "notDisturb";

  /**
   * TODO 获得信息列表.
   *
   * @param params 分页参数等
   * @return 信息列表
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result getChatNoticeList(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long userId = MapUtils.getLong(params, "userId");
    if (userId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    PageHelper.startPage((Integer) params.get("pageNum"), (Integer) params.get("pageSize"));
    List<Map<String, Object>> messageList = this.chatMapper.getChatNoticeList(userId);
    PageInfo<Map<String, Object>> gamePageInfo = new PageInfo<>(messageHandle(messageList, userId));
    Map<String, Object> result = Maps.newHashMap();
    result.put("chatList", gamePageInfo.getList());
    result.put("total", gamePageInfo.getTotal());
    result.put("pageNum", gamePageInfo.getPageNum());
    result.put("pageSize", gamePageInfo.getPageSize());
    result.put("pages", gamePageInfo.getPages());
    Map<String, Object> interact = this.noticeInteractService.getInteractUnreadInfo(userId);
    result.put("interactNotice", interact);
    Integer interactUnRead = 0;
    if (interact != null && interact.containsKey("unreadNum")) {
      interactUnRead = (Integer) interact.get("unreadNum");
    }
    Map<String, Object> system = this.noticeMemberService.getMemberUnreadInfo(userId);
    result.put("systemNotice", system);
    Integer systemUnRead = 0;
    if (system != null && system.containsKey("unreadNum")) {
      systemUnRead = (Integer) system.get("unreadNum");
    }
    Integer chatUnread = this.chatMapper.getUserUnreadNum(userId);
    if (chatUnread == null) {
      chatUnread = 0;
    }
    Integer groupUnread = this.groupMemberService.getUserUnreadNum(userId);
    result.put("unRead", chatUnread + groupUnread + interactUnRead + systemUnRead);
    return Result.success(result);
  }

  /**
   * TODO 消息处理.
   *
   * @param messageList 消息列表
   * @param userId 用户ID
   * @return 消息列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/12 20:51
   * @update 2020/8/12 20:51
   */
  private List<Map<String, Object>> messageHandle(List<Map<String, Object>> messageList, Long userId) {
    if (CollectionUtils.isNotEmpty(messageList)) {
      for (Map<String, Object> message : messageList) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Timestamp time = (Timestamp) message.get("lastTime");
        if (time != null) {
          message.put("lastTime", dtf.format(time.toLocalDateTime()));
        }
        // 1=单聊 2=群聊
        if (((Number) message.get("sort")).intValue() == 1) {
          long targetId = ((Number) message.get("targetId")).longValue();
          Map<String, Object> newResult = this.userRemote.getUserBase(userId, targetId);
          if (!newResult.isEmpty()) {
            message.put("thumbIconURL", StringUtils.nvl(newResult.get("iconUrl")));
            String remark = String.valueOf(newResult.get("remark"));
            if (remark.length() > 0) {
              message.put("alias", remark);
            } else {
              message.put("alias", newResult.get("nickName"));
            }
            message.put("level", newResult.get("level"));
          }
          int messageType = ((Number)message.get("messageType")).intValue();
          if (messageType >= 8) {
            message.put("messageType", 0);
          } else {
            if (messageType > 0) {
              switch (messageType) {
                case 1:
                  message.put("message", "[表情]");
                  break;
                case 2:
                  message.put("message", "[图片]");
                  break;
                case 3:
                  message.put("message", "[语音]");
                  break;
                case 4:
                  message.put("message", "[视频]");
                  break;
                default:
                  message.put("message", "[游戏邀请]");
                  break;
              }
            }
          }
        } else {
          String messageUser = String.valueOf(message.get("messageUser"));
          if (messageUser.length() > 0) {
            int messageType = ((Number)message.get("messageType")).intValue();
            String messageInfo = String.valueOf(message.get("message"));
            if (messageType > 0) {
              switch (messageType) {
                case 1:
                  messageInfo = "[表情]";
                  break;
                case 2:
                  messageInfo = "[图片]";
                  break;
                case 3:
                  messageInfo = "[语音]";
                  break;
                default:
                  messageInfo = "[视频]";
                  break;
              }
            }
            message.put("message", messageUser + ": " + messageInfo);
          }
        }
      }
    }
    return messageList;
  }

  /**
   * TODO 更新聊天设置.
   *
   * @param params 更新信息.
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result updUserChatSetting(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long userId = ((Number) params.get("userId")).longValue();
    Long targetId = ((Number) params.get("targetId")).longValue();
    if (userId <= 0 || targetId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    Chat chat = new Chat();
    chat.setUserId(userId);
    chat.setLinkId(targetId);
    if (params.containsKey(CHAT_STATUS)) {
      // 聊天状态 0=闲置中 1=聊天中
      chat.setChatStatus((Integer) params.get(CHAT_STATUS));
      if (chat.getChatStatus() == 1) {
        // 进入聊天更新未读数量
        chat.setUnreadNum(0);
      }
    }
    if (params.containsKey(FLAG_TOP)) {
      // 标记置顶 0=否 1=是
      chat.setFlagTop((Integer) params.get(FLAG_TOP));
    }
    if (params.containsKey(FLAG_DELETE)) {
      // 标记移除 0=否 1=是
      chat.setFlagDelete((Integer) params.get(FLAG_DELETE));
      if (chat.getFlagDelete() == 1) {
        // 清空聊天
        this.chatMessageDeleteService.updateSignNum(params);
      }
    }
    if (params.containsKey(NOT_DISTURB)) {
      // 免打扰 0=关闭 1=开启
      chat.setNotDisturb((Integer) params.get(NOT_DISTURB));
    }
    this.chatMapper.updUserChatSetting(chat);
    return Result.success();
  }

  /**
   * TODO 获得聊天设置.
   *
   * @param params 用户信息.
   * @return 聊天设置
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result getUserChatSetting(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    long userId = ((Number) params.get("userId")).longValue();
    long targetId = ((Number) params.get("targetId")).longValue();
    if (userId <= 0 || targetId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    // 是否好友
    Integer relation1 = this.userRemote.getRelation(userId, targetId, 0);
    if (relation1 == null) {
      return Result.error(ErrorCode.NETWORK_ERROR);
    }
    // 是否拉黑
    Integer relation2 = this.userRemote.getRelation(userId, targetId, 1);
    if (relation2 == null) {
      return Result.error(ErrorCode.NETWORK_ERROR);
    }
    // 获得用户信息
    Map<String, Object> newResult = this.userRemote.getUserBase(userId, targetId);
    Chat chat = this.chatMapper.getChatInfo(params);
    if (newResult != null && chat != null) {
      newResult.put("isFriend", relation1);
      newResult.put("flagBlack", relation2);
      newResult.put("thumbIconURL", StringUtils.nvl(newResult.get("iconUrl")));
      newResult.put("userId", newResult.get("friendId"));
      newResult.put("flagTop", chat.getFlagTop());
      newResult.put("notDisturb", chat.getNotDisturb());
      newResult.remove("friendId");
      newResult.remove("iconUrl");
      return Result.success(newResult);
    }
    return Result.error(ErrorCode.CHAT_PARAM_EXIST);
  }

  /**
   * TODO 聊天状态.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @return 聊天状态
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result getUserChatStatus(Long userId, Long targetId) {
    if (userId <= 0 || targetId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    Map<String, Object> newResult = new HashMap<>(16);
    newResult.put("userId", userId);
    newResult.put("targetId", targetId);
    Chat chat = this.chatMapper.getChatInfo(newResult);
    return Result.success(chat.getChatStatus());
  }

  /**
   * TODO 更新末尾信息.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result updateChatLastMessage(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Chat chatInfo = new Chat();
    long recordId = ((Number) params.get("recordId")).longValue();
    DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime ldt = LocalDateTime.parse(StringUtils.nvl(params.get("sendTime")), df);
    chatInfo.setMessageId(recordId);
    chatInfo.setMessageTime(ldt);
    // A -- > B 根据unread类型判断 0 未读 1 已读
    // 0 未读 A 在线 B 不在线
    // 数据1 userId targetId 0
    //      targetId userId unread+1
    Integer readType = (Integer) params.get("readType");
    if (readType == 0) {
      long userId = ((Number) params.get("userId")).longValue();
      long targetId = ((Number) params.get("targetId")).longValue();
      if (userId <= 0 || targetId <= 0) {
        return Result.error(ErrorCode.CHAT_PARAM_ERROR);
      }
      // 1) 数据
      chatInfo.setUnreadNum(0);
      chatInfo.setUserId(userId);
      chatInfo.setLinkId(targetId);
      chatInfo.setFlagDelete(0);
      this.chatMapper.updUserChatMessage(chatInfo);
      // 2) 数据
      Map<String, Object> newResult = new HashMap<>(16);
      newResult.put("userId", params.get("targetId"));
      newResult.put("targetId", params.get("userId"));
      Chat chat = this.chatMapper.getChatInfo(newResult);
      chat.setUserId(targetId);
      chat.setLinkId(userId);
      chat.setMessageId(chatInfo.getMessageId());
      chat.setMessageTime(chatInfo.getMessageTime());
      chat.setUnreadNum(chat.getUnreadNum() + 1);
      chat.setFlagDelete(0);
      this.chatMapper.updUserChatMessage(chat);
    } else {
      // 数据2 userId targetId 0
      //       targetId userId 0
      chatInfo.setUnreadNum(0);
      chatInfo.setUserId(((Number) params.get("userId")).longValue());
      chatInfo.setLinkId(((Number) params.get("targetId")).longValue());
      chatInfo.setFlagDelete(0);
      this.chatMapper.updUserChatMessage(chatInfo);
      chatInfo.setUnreadNum(0);
      chatInfo.setUserId(((Number) params.get("targetId")).longValue());
      chatInfo.setLinkId(((Number) params.get("userId")).longValue());
      chatInfo.setFlagDelete(0);
      this.chatMapper.updUserChatMessage(chatInfo);
    }
    return Result.success();
  }

  /**
   * TODO 通知状态.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @return 通知状态
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result getUserNoticeStatus(Long userId, Long targetId) {
    if (userId <= 0 || targetId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    Map<String, Object> newResult = new HashMap<>(16);
    newResult.put("userId", userId);
    newResult.put("targetId", targetId);
    Chat chat = this.chatMapper.getChatInfo(newResult);
    return Result.success(chat.getNotDisturb());
  }

  /**
   * TODO 建立关系.
   *
   * @param params 用户信息
   * @return 建立结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result buildRelationships(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long userId = ((Number) params.get("userId")).longValue();
    Long targetId = ((Number) params.get("targetId")).longValue();
    if (userId <= 0 || targetId <= 0) {
      Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    // action 0=建立 1=取消
    Integer action = (Integer) params.get("action");
    if (action != 0) {
      Chat chat = new Chat();
      chat.setUserId(userId);
      chat.setLinkId(targetId);
      chat.setFlagDelete(1);
      // 移除聊天
      this.chatMapper.updUserChatSetting(chat);
      // 清空聊天
      this.chatMessageDeleteService.updateSignNum(params);
    } else {
      this.buildRelation(userId, targetId);
      Integer isNotice = (Integer) params.get("isNotice");
      // notice 0=发送 1=取消
      if (isNotice == 0) {
        // 发送添加提醒
        this.socketRemote.newAddFriendNotice(params);
      }
    }
    return Result.success();
  }

  /**
   * TODO 清空信息.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public void emptyResidualMessage(Long userId, Long targetId) {
    this.chatMapper.emptyResidualMessage(userId, targetId);
  }

  /**
   * TODO 关系判断.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @author WangCaiWen
   * @date 2020/7/28
   */
  private void buildRelation(Long userId, Long targetId) {
    Integer check1 = this.chatMapper.checkUserRelation(userId, targetId);
    Integer check2 = this.chatMapper.checkUserRelation(targetId, userId);
    List<Chat> userList = Lists.newLinkedList();
    if (check1 == 0 && check2 == 0) {
      LocalDateTime nowTime = LocalDateTime.now();
      long recordId1 = this.chatMessageService.newAddMessage(userId, targetId, 8);
      long recordId2 = this.chatMessageService.newAddMessage(targetId, userId, 9);
      Chat chatLink1 = new Chat();
      chatLink1.setUserId(userId);
      chatLink1.setLinkId(targetId);
      chatLink1.setUnreadNum(0);
      chatLink1.setMessageId(recordId1);
      chatLink1.setMessageTime(nowTime);
      userList.add(chatLink1);
      Chat chatLink2 = new Chat();
      chatLink2.setUserId(targetId);
      chatLink2.setLinkId(userId);
      chatLink2.setUnreadNum(0);
      chatLink2.setMessageId(recordId2);
      chatLink2.setMessageTime(nowTime);
      userList.add(chatLink2);
      this.chatMapper.newUserChatLink(userList);
      this.chatMessageDeleteService.insertMessage(messageRelationList(userId, targetId));
      return;
    }
    LocalDateTime nowTime = LocalDateTime.now();
    long recordId = this.chatMessageService.newAddMessage(userId, targetId, 8);
    // 关系一建立.永不删除
    Map<String, Object> messageMap = Maps.newHashMap();
    messageMap.put("userId", userId);
    messageMap.put("linkId", targetId);
    messageMap.put("unreadNum", 0);
    messageMap.put("messageId", recordId);
    messageMap.put("messageTime", nowTime);
    this.chatMapper.updateUserChatSetting(messageMap);
  }

  /**
   * TODO 消息关联列表.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @author WangCaiWen
   * @date 2020/7/28
   */
  private List<Map<String, Object>> messageRelationList(Long userId, Long targetId) {
    List<Map<String, Object>> messageRelationList = new LinkedList<>();
    Map<String, Object> newResult = Maps.newHashMap();
    newResult.put("userId", userId);
    newResult.put("targetId", targetId);
    messageRelationList.add(newResult);
    newResult = Maps.newHashMap();
    newResult.put("userId", targetId);
    newResult.put("targetId", userId);
    messageRelationList.add(newResult);
    return messageRelationList;
  }

  /**
   * TODO 更新展示.
   *
   * @param params 用户信息
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result updateExhibition(Map<String, Object> params) {
    Long userId = ((Number) params.get("userId")).longValue();
    Long targetId = ((Number) params.get("targetId")).longValue();
    if (userId <= 0 || targetId <= 0) {
      Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    Chat chat = new Chat();
    chat.setUserId(userId);
    chat.setLinkId(targetId);
    // 标记移除 0=否 1=是
    chat.setFlagDelete((Integer) params.get("show"));
    this.chatMapper.updUserChatSetting(chat);
    return Result.success();
  }

  /**
   * TODO 清除信息.
   *
   * @param userId 用户ID
   * @return 清除结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result logoutToDeleteChat(Long userId) {
    // 解除与其他用户的聊天联系
    this.chatMapper.relieveChatRelation(userId);
    this.chatMessageDeleteService.deleteMessageAll(userId);
    // 解除与群聊关系
    this.groupService.dissolveGroup(userId);
    return Result.success();
  }

  /**
   * TODO 离开服务.
   *
   * @param userId 用户ID
   * @return 离开结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result leaveService(Long userId) {
    this.chatMapper.updateUserChatStatusAll(userId);
    this.groupMemberService.updateUserChatStatusAll(userId);
    return Result.success();
  }

  @Override
  public Result userRelationIsExist(Long userId, Long targetId) {
    return Result.success(this.chatMapper.userRelationIsExist(userId, targetId));
  }
}
