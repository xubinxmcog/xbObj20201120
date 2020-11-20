package com.enuos.live.mapper;

import com.enuos.live.pojo.ChatMessageInvite;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author WangCaiWen Created on 2020/4/23 16:49
 */
public interface ChatMessageInviteMapper {

  /**
   * 新的邀请记录
   *
   * @param chatMessageInvite the Record_Id && Invite_Id && Invite_Status && Accept_Status
   */
  void newChatMessageInvite(ChatMessageInvite chatMessageInvite);

  /**
   * 更新邀请记录
   *
   * @param chatMessageInvite the Record_Id && Accept_Room_Id || Accept_Status || Invite_Status
   */
  void updateChatMessageInvite(ChatMessageInvite chatMessageInvite);

  ChatMessageInvite getMessageInviteInfo(Long recordId);

  void updateInviteStatus(@Param("list") List<Integer> list);
}
