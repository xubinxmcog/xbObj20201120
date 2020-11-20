package com.enuos.live.mapper;

import com.enuos.live.pojo.GroupMember;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/21 9:25
 */
public interface GroupMemberMapper {

  /**
   * 保存群用户
   *
   * @param list the Group_User_List
   */
  void saveGroupUserList(@Param("list") List<Map<String, Object>> list);

  /**
   * 删除群
   *
   * @param groupId the Group_Id
   */
  void deleteGroupUserAll(Long groupId);

  /**
   * 离开加入的群聊ID
   *
   * @param userId 用户Id
   */
  void leaveJoinGroupChat(Long userId);

  /**
   * 统一改变用户聊天状态
   *
   * @param userId 用户ID
   */
  void updateUserChatStatusAll(Long userId);

  /**
   * 删除群用户
   *
   * @param groupId the Group_id
   * @param list the User_Ids
   */
  void deleteGroupUserList(@Param("groupId") Long groupId, @Param("list") List<Long> list);

  /**
   * 删除用户
   *
   * @param groupId the Group_Id
   * @param userId the User_Id
   */
  void deleteGroupUser(@Param("groupId") Long groupId, @Param("userId") Long userId);

  /**
   * 更新群聊用户信息
   *
   * @param groupMember the params
   */
  void updateGroupUserInfo(GroupMember groupMember);

  /**
   * 更新用户未读数量
   *
   * @param groupId the Group_Id
   * @param userId the User_Id
   */
  void updateUserGroupUnReadNum(@Param("groupId") Long groupId, @Param("userId") Long userId);

  /**
   * 更新用户未读数量
   *
   * @param groupId the Group_id
   * @param list the User_Ids
   */
  void updateUserGroupUnReadNumAdd(@Param("groupId") Long groupId, @Param("list") List<Long> list);

  /**
   * 更新用户未读数量
   *
   * @param groupId the Group_id
   * @param list the User_Ids
   */
  void updateUserGroupUnReadNumEmpty(@Param("groupId") Long groupId,
      @Param("list") List<Long> list);


  /**
   * 更新群聊末尾信息
   *
   * @param groupMember 实例对象
   */
  void updateGroupLastMessage(GroupMember groupMember);

  /**
   * 清空残留消息
   *
   * @param groupId 群聊ID
   * @param userId 用户ID
   */
  void emptyResidualMessage(@Param("groupId") Long groupId, @Param("userId") Long userId);

  /**
   * 获得群用户信息
   *
   * @param groupId the Group_Id
   * @param userId the User_Id
   * @return is Group_User_Info
   */
  GroupMember getGroupUserInfo(@Param("groupId") Long groupId, @Param("userId") Long userId);

  /**
   * 获得群聊用户
   *
   * @param groupId the Group_Id.
   * @return is list of user_id
   */
  List<Long> getGroupUserId(Long groupId);

  /**
   * 获得展示的9名群内成员ID
   *
   * @param groupId the Group_Id
   * @return is User_Id_List
   */
  List<Long> getShowUserIdList(Long groupId);

  /**
   * 获得群管理员列表
   *
   * @param groupId the Group_Id
   * @return is User_List(user_id + user_alias)
   */
  List<Long> getGroupUserAdminList(Long groupId);

  /**
   * 获取群人员列表
   *
   * @param groupId the Group_Id
   * @return is User_List(user_id + user_alias)
   */
  List<Long> getGroupUserAllList(Long groupId);

  /**
   * 获得用户加入的群聊ID
   *
   * @param userId 用户ID
   * @return 实例列表
   */
  List<Long> userJoinGroupId(Long userId);


  /**
   * 获取群人员列表
   *
   * @param groupId the Group_Id
   * @return is User_List(user_id + user_alias)
   */
  List<Map<String, Object>> getGroupUserList(Long groupId);

  /**
   * 获得管理员人数
   *
   * @param groupId the Group_Id
   * @return is admin_num
   */
  Integer getGroupChatAdminNum(Long groupId);

  /**
   * TODO 获得未读数量.
   *
   * @param userId 用户ID
   * @return java.lang.Integer
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/19 9:04
   * @update 2020/8/19 9:04
   */
  Integer getUserUnreadNum(Long userId);
}
