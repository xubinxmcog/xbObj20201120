package com.enuos.live.service;

import com.enuos.live.pojo.GroupMember;
import com.enuos.live.result.Result;
import java.util.List;
import java.util.Map;

/**
 * TODO 群聊成员管理服务.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V1.0.0
 * @since 2020/4/21 10:29
 */

public interface GroupMemberService {

  /**
   * TODO 批量保存.
   *
   * @param list 用户列表.
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  void saveGroupUserList(List<Map<String, Object>> list);

  /**
   * TODO 更新图标.
   *
   * @param groupId 群聊ID
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  void updateGroupNameOrIcon(Long groupId);

  /**
   * TODO 解散群聊.
   *
   * @param groupId 群聊ID
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  void dissolveGroupChat(Long groupId);

  /**
   * TODO 离开群聊.
   *
   * @param userId 用户ID
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  void leaveJoinGroupChat(Long userId);

  /**
   * TODO 成员头像.
   *
   * @param groupId 群聊ID
   * @return 头像列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 17:59
   */
  List<String> getGroupIcon(Long groupId);

  /**
   * TODO 展示成员.
   *
   * @param groupId 群聊ID
   * @return 用户列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 17:47
   */
  List<Long> getShowUserIdList(Long groupId);

  /**
   * TODO 成员信息.
   *
   * @param groupId 群聊ID
   * @param userId 用户ID
   * @return 成员信息
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @since 2020/7/28 10:29
   */
  GroupMember getGroupUserInfo(Long groupId, Long userId);

  /**
   * TODO 离开群聊.
   *
   * @param params 离开信息
   * @return 离开结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @since 2020/7/28 10:29
   */
  Result leaveGroupChat(Map<String, Object> params);

  /**
   * TODO 移交权限.
   *
   * @param params 移交消息
   * @return 移交结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  Result handOverGroupAdminLimit(Map<String, Object> params);

  /**
   * TODO 删除成员.
   *
   * @param params 成员信息
   * @return 删除结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  Result deleteGroupUser(Map<String, Object> params);

  /**
   * TODO 更新状态.
   *
   * @param params 状态信息
   * @return 更新结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  Result updateGroupChat(Map<String, Object> params);

  /**
   * TODO 末尾信息.
   *
   * @param groupMember 末尾信息
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  void updateGroupLastMessage(GroupMember groupMember);

  /**
   * TODO 清空消息.
   *
   * @param groupId 群聊ID
   * @param userId 用户ID
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  void emptyResidualMessage(Long groupId, Long userId);

  /**
   * TODO 通知状态.
   *
   * @param groupId 群聊ID
   * @param userId 用户ID
   * @return 通知状态
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  Result getUserGroupNoticeStatus(Long groupId, Long userId);

  /**
   * TODO 更新设置.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  Result updUserGroupSetting(Map<String, Object> params);

  /**
   * TODO 管理列表.
   *
   * @param params 群聊ID
   * @return 成员列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  Result getGroupAdminList(Map<String, Object> params);

  /**
   * TODO 成员列表.
   *
   * @param params 群聊ID
   * @return 成员列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  Result getGroupUserList(Map<String, Object> params);

  /**
   * TODO 成员列表.
   *
   * @param groupId 群聊ID
   * @return ID列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  Result getGroupMemberIdList(Long groupId);

  /**
   * TODO 成员列表.
   *
   * @param groupId 群聊ID
   * @return ID列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  List<Long> getGroupUserIdList(Long groupId);

  /**
   * TODO 设置管理.
   *
   * @param params 设置信息
   * @return 设置结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/7/28 10:29
   * @update 2020/8/11 18:00
   */
  Result updateGroupAdmin(Map<String, Object> params);

  /**
   * TODO 更新状态.
   *
   * @param userId 用户ID
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/7/28 10:29
   * @update 2020/8/11 18:00
   */
  void updateUserChatStatusAll(Long userId);

  /**
   * TODO 更新未读.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/7/28 10:29
   * @update 2020/8/11 18:00
   */
  Result updateUserGroupUnreadNum(Map<String, Object> params);

  /**
   * TODO 更新消息.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  Result updateGroupLastMessageMap(Map<String, Object> params);

  /**
   * TODO 聊天状态.
   *
   * @param groupId 群聊ID
   * @param userId 用户ID
   * @return 聊天状态
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  Result getUserGroupStatus(Long groupId, Long userId);

  /**
   * TODO 获得未读数量.
   *
   * @param userId 用户ID
   * @return java.lang.Integer
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/19 9:03
   * @update 2020/8/19 9:03
   */
  Integer getUserUnreadNum(Long userId);
}
