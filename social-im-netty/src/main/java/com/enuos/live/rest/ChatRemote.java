package com.enuos.live.rest;

import com.enuos.live.rest.impl.ChatRemoteFallback;
import com.enuos.live.result.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * TODO 聊天中心.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/4/14 - 2020/7/24
 */

@Component
@FeignClient(contextId = "chatRemote", name = "CHAT-CENTER", fallback = ChatRemoteFallback.class)
public interface ChatRemote {

  /**
   * TODO 单聊状态.
   *
   * @param params 状态信息
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/24
   */
  @PostMapping(value = "/feign/chat/updateChatStatus")
  Result updateChatStatus(@RequestBody Map<String, Object> params);

  /**
   * TODO 目标状态.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @return 目标状态
   * @author WangCaiWen
   * @date 2020/7/24
   */
  @GetMapping(value = "/feign/chat/getUserChatStatus")
  Result getUserChatStatus(@RequestParam("userId") Long userId, @RequestParam("targetId") Long targetId);

  /**
   * TODO 聊天信息.
   *
   * @param params 发送信息
   * @return 记录ID
   * @author WangCaiWen
   * @date 2020/7/24
   */
  @PostMapping(value = "/feign/chat/insChatMessage")
  Result insChatMessage(@RequestBody Map<String, Object> params);

  /**
   * TODO 更新记录.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/24
   */
  @PostMapping(value = "/feign/chat/updateChatListMessage")
  Result updateChatListMessage(@RequestBody Map<String, Object> params);

  /**
   * TODO 聆听语音.
   *
   * @param params 消息ID
   * @return 聆听结果
   * @author WangCaiWen
   * @date 2020/7/24
   */
  @PostMapping(value = "/feign/chat/listenToVoice")
  Result listenToVoice(@RequestBody Map<String, Object> params);

  /**
   * TODO 取消约战.
   *
   * @param params 约战消息
   * @return 取消结果
   * @author WangCaiWen
   * @date 2020/7/24
   */
  @PostMapping(value = "/feign/chat/updateInviteStatus")
  Result updateInviteStatus(@RequestBody Map<String, Object> params);

  /**
   * TODO 更新邀请.
   *
   * @param params 更新参数
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/24
   */
  @PostMapping(value = "/feign/chat/updateChatMessageInvite")
  Result updateChatMessageInvite(@RequestBody Map<String, Object> params);

  /**
   * TODO 验证邀请.
   *
   * @param recordId 记录ID
   * @return 验证结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @GetMapping(value = "/feign/chat/clickToEnter")
  Result clickToEnter(@RequestParam("recordId") Long recordId);

  /**
   * TODO 验证联系.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @return 聊天联系
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @GetMapping(value = "/feign/chat/userRelationIsExist")
  Result userRelationIsExist(@RequestParam("userId") Long userId, @RequestParam("targetId") Long targetId);

  /**
   * TODO 离开服务.
   *
   * @param userId 用户ID
   * @return 离开结果
   * @author WangCaiWen
   * @date 2020/7/29
   */
  @PostMapping(value = "/feign/chat/leaveService")
  Result leaveService(@RequestParam("userId") Long userId);

  /**
   * TODO 群聊状态.
   *
   * @param params 状态信息
   * @return 更新结果
   * @author WangCaiWen
   * @date 2020/7/24
   */
  @PostMapping(value = "/feign/group/updateGroupChat")
  Result updateGroupChat(@RequestBody Map<String, Object> params);

  /**
   * TODO 聆听语音.
   *
   * @param params 记录信息
   * @return 聆听结果
   * @author WangCaiWen
   * @date 2020/7/24
   */
  @PostMapping(value = "/feign/group/listenToVoice")
  Result listenToVoiceGroup(@RequestBody Map<String, Object> params);

  /**
   * TODO 群聊成员.
   *
   * @param groupId 群聊ID
   * @return 查询结果
   * @author wangcaiwen|1443710411@qq.com
   * @since 2020/8/11 9:51
   * @date 2020/8/11 9:51
   */
  @GetMapping(value = "/feign/group/getGroupUserId")
  Result getGroupUserId(@RequestParam("groupId") Long groupId);

  /**
   * TODO 群聊信息.
   *
   * @param groupId  群聊ID
   * @return 群聊信息
   * @author wangcaiwen|1443710411@qq.com
   * @since 2020/8/11 13:23
   * @date 2020/8/11 13:23
   */
  @GetMapping(value = "/feign/group/getGroupInfoResult")
  Result getGroupInfoResult(@RequestParam("groupId") Long groupId);

  /**
   * TODO 聊天信息.
   *
   * @param params 发送信息
   * @return 记录ID
   * @author wangcaiwen|1443710411@qq.com
   * @since 2020/8/11 14:09
   * @date 2020/8/11 14:09
   */
  @PostMapping(value = "/feign/group/newGroupMessage")
  Result newGroupMessage(@RequestBody Map<String, Object> params);

  /**
   * TODO 更新未读.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author wangcaiwen|1443710411@qq.com
   * @since 2020/8/11 14:22
   * @date 2020/8/11 14:22
   */
  @PostMapping(value = "/feign/group/updateUserGroupUnreadNum")
  Result updateUserGroupUnreadNum(@RequestBody Map<String, Object> params);

  /**
   * TODO 更新消息.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author wangcaiwen|1443710411@qq.com
   * @since 2020/8/11 14:27
   * @date 2020/8/11 14:27
   */
  @PostMapping(value = "/feign/group/updateGroupLastMessageMap")
  Result updateGroupLastMessageMap(@RequestBody Map<String, Object> params);

  /**
   * TODO 聊天状态.
   *
   * @param groupId 群聊ID
   * @param userId 用户ID
   * @return 聊天状态
   * @author wangcaiwen|1443710411@qq.com
   * @since 2020/8/11 15:13
   * @date 2020/8/11 15:13
   */
  @GetMapping(value = "/feign/group/getUserGroupStatus")
  Result getUserGroupStatus(@RequestParam("groupId") Long groupId, @RequestParam("userId") Long userId);

  /**
   * TODO 未读语音.
   *
   * @param params 语音信息
   * @return 新增结果
   * @author wangcaiwen|1443710411@qq.com
   * @since 2020/8/11 15:17
   * @date 2020/8/11 15:17
   */
  @PostMapping(value = "/feign/group/insertVoiceUnreadInfo")
  Result insertVoiceUnreadInfo(@RequestBody Map<String, Object> params);

  /**
   * TODO 通知状态.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @return 通知状态
   * @author WangCaiWen
   * @date 2020/7/24
   */
  @GetMapping(value = "/feign/chat/getUserNoticeStatus")
  Result getUserNoticeStatus(@RequestParam("userId") Long userId, @RequestParam("targetId") Long targetId);

  /**
   * TODO 通知状态.
   *
   * @param groupId 群聊状态
   * @param userId 用户ID
   * @return 通知状态
   * @author WangCaiWen
   * @date 2020/7/24
   */
  @GetMapping(value = "/feign/group/getUserGroupNoticeStatus")
  Result getUserGroupNoticeStatus(@RequestParam("groupId") Long groupId, @RequestParam("userId") Long userId);

}
