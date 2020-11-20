package com.enuos.live.controller;

import com.enuos.live.result.Result;
import com.enuos.live.service.ChatMessageInviteService;
import com.enuos.live.service.ChatMessageService;
import com.enuos.live.service.ChatService;
import com.enuos.live.service.GroupMemberService;
import com.enuos.live.service.GroupMessageService;
import com.enuos.live.service.GroupMessageVoiceService;
import com.enuos.live.service.GroupService;
import com.enuos.live.service.NoticeInteractService;
import com.enuos.live.service.NoticeMemberService;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO 内部接口.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.0.0
 * @since 2020/5/8 6:52
 */

@RestController
@RequestMapping("/feign")
public class FeignController {

  /** 单聊服务. */
  @Resource
  private ChatService chatService;
  @Resource
  private ChatMessageService chatMessageService;
  @Resource
  private ChatMessageInviteService chatMessageInviteService;
  /** 群聊服务. */
  @Resource
  private GroupService groupService;
  @Resource
  private GroupMemberService groupMemberService;
  @Resource
  private GroupMessageService groupMessageService;
  @Resource
  private GroupMessageVoiceService groupMessageVoiceService;
  /** 通知服务. */
  @Resource
  private NoticeMemberService noticeMemberService;
  @Resource
  private NoticeInteractService noticeInteractService;

  /**
   * TODO 更新状态.
   *
   * @param params 状态信息
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/chat/updateChatStatus")
  public Result updateChatStatus(@RequestBody Map<String, Object> params) {
    return this.chatService.updUserChatSetting(params);
  }

  /**
   * TODO 目标状态.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @return 目标状态
   * @author WangCaiWen
   * @date 2020/7/24
   */
  @GetMapping(value = "/chat/getUserChatStatus")
  public Result getUserChatStatus(@RequestParam("userId") Long userId,
      @RequestParam("targetId") Long targetId) {
    return this.chatService.getUserChatStatus(userId, targetId);
  }

  /**
   * TODO 聊天信息.
   *
   * @param params 发送信息
   * @return 记录ID
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/chat/insChatMessage")
  public Result insChatMessage(@RequestBody Map<String, Object> params) {
    return this.chatMessageService.newChatMessage(params);
  }

  /**
   * TODO 更新记录.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/chat/updateChatListMessage")
  public Result updateChatListMessage(@RequestBody Map<String, Object> params) {
    return this.chatService.updateChatLastMessage(params);
  }

  /**
   * TODO 聆听语音.
   *
   * @param params 消息ID
   * @return 聆听结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/chat/listenToVoice")
  public Result listenToVoice(@RequestBody Map<String, Object> params) {
    return this.chatMessageService.listenToVoice(params);
  }

  /**
   * TODO 取消约战.
   *
   * @param params 约战消息
   * @return 取消结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/chat/updateInviteStatus")
  public Result updateInviteStatus(@RequestBody Map<String, Object> params) {
    return this.chatMessageInviteService.updateInviteStatus(params);
  }

  /**
   * TODO 更新邀请.
   *
   * @param params 更新参数
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/chat/updateChatMessageInvite")
  public Result updateChatMessageInvite(@RequestBody Map<String, Object> params) {
    return this.chatMessageInviteService.updateChatMessageInvite(params);
  }

  /**
   * TODO 离开服务.
   *
   * @param userId 用户ID
   * @return 离开结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/chat/leaveService")
  public Result leaveService(@RequestParam("userId") Long userId) {
    return this.chatService.leaveService(userId);
  }

  /**
   * TODO 验证邀请.
   *
   * @param recordId 记录ID
   * @return 验证结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @GetMapping(value = "/chat/clickToEnter")
  public Result clickToEnter(@RequestParam("recordId") Long recordId) {
    return this.chatMessageInviteService.clickToEnter(recordId);
  }

  /**
   * TODO 验证联系.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @return 聊天联系
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @GetMapping(value = "/chat/userRelationIsExist")
  public Result userRelationIsExist(@RequestParam("userId") Long userId, @RequestParam("targetId") Long targetId) {
    return this.chatService.userRelationIsExist(userId, targetId);
  }

  /**
   * TODO 群聊状态.
   *
   * @param params 状态信息
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/group/updateGroupChat")
  public Result updateGroupChat(@RequestBody Map<String, Object> params) {
    return this.groupMemberService.updateGroupChat(params);
  }

  /**
   * TODO 聆听语音.
   *
   * @param params 记录信息
   * @return 聆听结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/group/listenToVoice")
  public Result listenToVoiceGroup(@RequestBody Map<String, Object> params) {
    return this.groupMessageVoiceService.listenToVoiceGroup(params);
  }

  /**
   * TODO 群聊成员.
   *
   * @param groupId 群聊ID
   * @return 查询结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/11 9:51
   * @since 2020/8/11 9:51
   */
  @GetMapping(value = "/group/getGroupUserId")
  public Result getGroupUserId(@RequestParam("groupId") Long groupId) {
    return this.groupMemberService.getGroupMemberIdList(groupId);
  }

  /**
   * TODO 群聊信息.
   *
   * @param groupId 群聊ID
   * @return 群聊信息
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/7 13:23
   * @since 2020/8/11 13:23
   */
  @GetMapping(value = "/group/getGroupInfoResult")
  public Result getGroupInfoResult(@RequestParam("groupId") Long groupId) {
    return this.groupService.getGroupInfoMap(groupId);
  }

  /**
   * TODO 聊天信息.
   *
   * @param params 发送信息
   * @return 记录ID
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/11 14:09
   * @since 2020/8/11 14:09
   */
  @PostMapping(value = "/group/newGroupMessage")
  public Result newGroupMessage(@RequestBody Map<String, Object> params) {
    return this.groupMessageService.newGroupMessage(params);
  }

  /**
   * TODO 更新未读.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/11 14:22
   * @since 2020/8/11 14:22
   */
  @PostMapping(value = "/group/updateUserGroupUnreadNum")
  public Result updateUserGroupUnreadNum(@RequestBody Map<String, Object> params) {
    return this.groupMemberService.updateUserGroupUnreadNum(params);
  }

  /**
   * TODO 更新消息.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/11 14:27
   * @since 2020/8/11 14:27
   */
  @PostMapping(value = "/group/updateGroupLastMessageMap")
  public Result updateGroupLastMessageMap(@RequestBody Map<String, Object> params) {
    return this.groupMemberService.updateGroupLastMessageMap(params);
  }

  /**
   * TODO 聊天状态.
   *
   * @param groupId 群聊ID
   * @param userId 用户ID
   * @return 聊天状态
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/11 15:13
   * @since 2020/8/11 15:13
   */
  @GetMapping(value = "/group/getUserGroupStatus")
  public Result getUserGroupStatus(@RequestParam("groupId") Long groupId, @RequestParam("userId") Long userId) {
    return this.groupMemberService.getUserGroupStatus(groupId, userId);
  }

  /**
   * TODO 未读语音.
   *
   * @param params 语音信息
   * @return 新增结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/11 15:17
   * @since 2020/8/11 15:17
   */
  @PostMapping(value = "/group/insertVoiceUnreadInfo")
  public Result insertVoiceUnreadInfo(@RequestBody Map<String, Object> params) {
    return this.groupMessageVoiceService.insertMessageUnread(params);
  }

  // =============================================================================================
  // 通知

  /**
   * TODO 通知状态.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @return 通知状态
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @GetMapping(value = "/chat/getUserNoticeStatus")
  public Result getUserNoticeStatus(@RequestParam("userId") Long userId, @RequestParam("targetId") Long targetId) {
    return this.chatService.getUserNoticeStatus(userId, targetId);
  }

  /**
   * TODO 通知状态.
   *
   * @param groupId 群聊状态
   * @param userId 用户ID
   * @return 通知状态
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @GetMapping(value = "/group/getUserGroupNoticeStatus")
  public Result getUserGroupNoticeStatus(@RequestParam("groupId") Long groupId, @RequestParam("userId") Long userId) {
    return this.groupMemberService.getUserGroupNoticeStatus(groupId, userId);
  }

  /**
   * TODO 建立关系.
   *
   * @param params 用户信息.
   * @return 建立结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/chat/buildRelationships")
  public Result buildRelationships(@RequestBody Map<String, Object> params) {
    return this.chatService.buildRelationships(params);
  }

  /**
   * TODO 更新展示.
   *
   * @param params 用户信息
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/chat/updateExhibition")
  public Result updateExhibition(@RequestBody Map<String, Object> params) {
    return this.chatService.updateExhibition(params);
  }

  /**
   * TODO 清除信息.
   *
   * @param userId 用户ID
   * @return 清除结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/chat/logoutToDeleteChat")
  public Result logoutToDeleteChat(@RequestParam("userId") Long userId) {
    return this.chatService.logoutToDeleteChat(userId);
  }

  /**
   * TODO 搜索群名.
   *
   * @param params 互动信息
   * @return 搜索结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/11 16:32
   * @since 2020/8/11 16:32
   */
  @PostMapping(value = "/group/searchGroup")
  public Result searchGroup(@RequestBody Map<String, Object> params) {
    return this.groupService.searchGroupByName(params);
  }

  /**
   * TODO 互动通知.
   *
   * @param params 互动信息
   * @return 通知结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @PostMapping(value = "/notice/saveInteractNotice")
  public Result saveInteractNotice(@RequestBody Map<String, Object> params) {
    return this.noticeInteractService.saveInteractNotice(params);
  }

  /**
   * TODO 活动信息.
   *
   * @param params [receiveId, activityName, activityCode, activityStatus]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 12:40
   * @update 2020/11/13 12:40
   */
  @PostMapping(value = "/notice/activity")
  public Result activityNotice(@RequestBody Map<String, Object> params) {
    return this.noticeMemberService.activityNotice(params);
  }

  /**
   * TODO 物品信息.
   *
   * @param params [receiveId, itemName, itemType]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 12:46
   * @update 2020/11/13 12:46
   */
  @PostMapping(value = "/notice/item")
  public Result itemNotice(@RequestBody Map<String, Object> params) {
    return this.noticeMemberService.itemNotice(params);
  }

  /**
   * TODO 奖励信息.
   *
   * @param params [receiveId, coinReward]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 12:50
   * @update 2020/11/13 12:50
   */
  @PostMapping(value = "/notice/reward")
  public Result rewardNotice(@RequestBody Map<String, Object> params) {
    return this.noticeMemberService.rewardNotice(params);
  }

  /**
   * TODO 开播信息.
   *
   * @param params [receiveId, roomId, roomOwnerName, roomName]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 12:53
   * @update 2020/11/13 12:53
   */
  @PostMapping(value = "/notice/openLive")
  public Result openLiveNotice(@RequestBody Map<String, Object> params) {
    return this.noticeMemberService.openLiveNotice(params);
  }

  /**
   * TODO 礼物信息.
   *
   * @param params [receiveId, donorName, giftName, giftNum, rewardName, rewardNum]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 12:58
   * @update 2020/11/13 12:58
   */
  @PostMapping(value = "/notice/gift")
  public Result giftNotice(@RequestBody Map<String, Object> params) {
    return this.noticeMemberService.giftNotice(params);
  }

  /**
   * TODO 会员信息.
   *
   * @param params [receiveId, noticeType, level]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 13:05
   * @update 2020/11/13 13:05
   */
  @PostMapping(value = "/notice/member")
  public Result memberNotice(@RequestBody Map<String, Object> params) {
    return this.noticeMemberService.memberNotice(params);
  }

}
