package com.enuos.live.service.impl;

import com.enuos.live.mapper.ChatMessageInviteMapper;
import com.enuos.live.pojo.ChatMessageInvite;
import com.enuos.live.result.Result;
import com.enuos.live.service.ChatMessageInviteService;
import com.enuos.live.utils.JsonUtils;
import com.enuos.live.utils.StringUtils;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/23 16:51
 */

@Service("chatMessageInviteService")
public class ChatMessageInviteServiceImpl implements ChatMessageInviteService {

  @Resource
  private ChatMessageInviteMapper chatMessageInviteMapper;

  /**
   * TODO 新的邀请记录.
   *
   * @param params 邀请信息
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public void newChatMessageInvite(Map<String, Object> params) {
    if (params != null && !params.isEmpty()) {
      Long inviteId = ((Number) params.get("inviteId")).longValue();
      Long recordId = ((Number) params.get("recordId")).longValue();
      if (inviteId > 0 && recordId > 0) {
        ChatMessageInvite chatMessageInvite = new ChatMessageInvite();
        chatMessageInvite.setInviteTitle(StringUtils.nvl(params.get("inviteTitle")));
        chatMessageInvite.setInviteImage(StringUtils.nvl(params.get("inviteImageUrl")));
        chatMessageInvite.setRecordId(recordId);
        chatMessageInvite.setGameId(inviteId);
        if (params.containsKey("roomId")) {
          chatMessageInvite.setRoomId(((Number) params.get("roomId")).longValue());
        }
        chatMessageInvite.setInviteStatus(0);
        chatMessageInvite.setAcceptStatus(0);
        this.chatMessageInviteMapper.newChatMessageInvite(chatMessageInvite);
      }
    }
  }

  /**
   * TODO 获取消息邀请信息.
   *
   * @param recordId 记录ID
   * @return 邀请信息
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public ChatMessageInvite getMessageInviteInfo(Long recordId) {
    return this.chatMessageInviteMapper.getMessageInviteInfo(recordId);
  }

  private static final String INVITE_STATUS = "inviteStatus";
  private static final String ACCEPT_STATUS = "acceptStatus";

  /**
   * TODO 更新邀请记录.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result updateChatMessageInvite(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error();
    }
    Long recordId = ((Number) params.get("recordId")).longValue();
    if (recordId <= 0) {
      return Result.error();
    }
    ChatMessageInvite chatMessageInvite = new ChatMessageInvite();
    chatMessageInvite.setRecordId(recordId);
    // 邀请状态 [0 正常 1 取消]
    if (params.containsKey(INVITE_STATUS)) {
      chatMessageInvite.setInviteStatus((Integer) params.get(INVITE_STATUS));
    }
    // 接受状态 [0 未接受 1 已接受]
    if (params.containsKey(ACCEPT_STATUS)) {
      chatMessageInvite.setAcceptStatus((Integer) params.get(ACCEPT_STATUS));
    }
    this.chatMessageInviteMapper.updateChatMessageInvite(chatMessageInvite);
    return Result.success();
  }

  /**
   * TODO 取消约战.
   *
   * @param params 记录列表
   * @return 取消结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result updateInviteStatus(Map<String, Object> params) {
    List<Integer> userIdList = JsonUtils.toListType(params.get("recordIdList"), Integer.class);
    if (userIdList != null && userIdList.size() > 0) {
      this.chatMessageInviteMapper.updateInviteStatus(userIdList);
    }
    return Result.success();
  }

  /**
   * TODO 验证邀请.
   *
   * @param recordId 记录ID
   * @return 验证结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result clickToEnter(Long recordId) {
    ChatMessageInvite invite = this.chatMessageInviteMapper.getMessageInviteInfo(recordId);
    // 接受状态 [0 未接受 1 已接受]
    if (invite.getAcceptStatus() == 0) {
      invite.setAcceptStatus(1);
      this.chatMessageInviteMapper.updateChatMessageInvite(invite);
      // 验证时间
      LocalDateTime dtc = invite.getCreateTime().plusSeconds(60L);
      LocalDateTime now = LocalDateTime.now();
      if (now.isBefore(dtc)) {
        // 通过
        return Result.success(1);
      } else {
        // 不通过
        return Result.success(2);
      }
    }
    // 已失效
    return Result.success(2);
  }

}
