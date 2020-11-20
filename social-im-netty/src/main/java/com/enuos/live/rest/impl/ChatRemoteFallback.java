package com.enuos.live.rest.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.rest.ChatRemote;
import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * TODO 熔断处理.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/4/14 - 2020/7/28
 */

@Component
public class ChatRemoteFallback implements ChatRemote {

  @Override
  public Result updateChatStatus(Map<String, Object> params) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result getUserChatStatus(Long userId, Long targetId) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result insChatMessage(Map<String, Object> params) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result updateChatListMessage(Map<String, Object> params) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result listenToVoice(Map<String, Object> params) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result updateInviteStatus(Map<String, Object> params) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result updateChatMessageInvite(Map<String, Object> params) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result clickToEnter(Long recordId) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result userRelationIsExist(Long userId, Long targetId) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result leaveService(Long userId) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result updateGroupChat(Map<String, Object> params) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result listenToVoiceGroup(Map<String, Object> params) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result getGroupUserId(Long groupId) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result getGroupInfoResult(Long groupId) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result newGroupMessage(Map<String, Object> params) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result updateUserGroupUnreadNum(Map<String, Object> params) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result updateGroupLastMessageMap(Map<String, Object> params) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result getUserGroupStatus(Long groupId, Long userId) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result insertVoiceUnreadInfo(Map<String, Object> params) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result getUserNoticeStatus(Long userId, Long targetId) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }

  @Override
  public Result getUserGroupNoticeStatus(Long groupId, Long userId) {
    return Result.error(ErrorCode.NETWORK_ERROR);
  }
}
