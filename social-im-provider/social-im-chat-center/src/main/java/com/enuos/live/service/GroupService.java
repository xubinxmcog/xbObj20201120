package com.enuos.live.service;

import com.enuos.live.pojo.Group;
import com.enuos.live.result.Result;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/15 18:08
 */
public interface GroupService {


  /**
   * 更新群消息
   *
   * @param group the param
   */
  void updateGroupInfo(Group group);

  /**
   * 删除群
   *
   * @param groupId the Group_Id
   */
  void deleteGroupAdmin(Long groupId);

  /**
   * 解除群聊
   *
   * @param userId 用户ID
   */
  void dissolveGroup(Long userId);

  /**
   * TODO 获得聊天设置.
   *
   * @param params [groupId, userId]
   * @return 聊天设置
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/4/20 18:18
   * @update 2020/8/12 10:03
   */
  Result getUserGroupSetting(Map<String, Object> params);

  /**
   * 根据群聊ID获得群聊详情
   *
   * @param groupId the Group_Id
   * @return is Group_Info
   */
  Group getGroupInfo(Long groupId);

  /**
   * 创建群聊
   *
   * @param params the Admin_Id && invite User_Ids
   * @return is Group_id
   */
  Result newGroupChat(Map<String, Object> params);

  /**
   * 邀请加入群聊
   *
   * @param params the Group_Id && User_Ids
   * @return is add success code
   */
  Result inviteJoinGroup(Map<String, Object> params);

  /**
   * 获得群信息
   *
   * @param groupId the Group_Id.
   * @return is  Group_Name
   */
  Result getGroupInfoMap(Long groupId);

  /**
   * 获得用户所在的群聊列表
   *
   * @param params the User_Id
   * @return is Group_List(icon + name + group_id)
   */
  Result getUserTheGroupListByUserId(Map<String, Object> params);

  /**
   * 搜索群根据群名称
   *
   * @param params the Group_Name
   * @return is Group_List(icon + name)
   */
  Result searchGroupByName(Map<String, Object> params);

  /**
   * 更新群聊名称或者公告以及简介、群内昵称
   *
   * @param params the
   * @return is update success code
   */
  Result updGroupNameOrNoticeAndIntro(Map<String, Object> params);

  /**
   * TODO 提升等级.
   *
   * @param params 用户信息
   * @return 提升结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/14 9:49
   * @update 2020/8/14 9:49
   */
  Result upgradeGroupGrade(Map<String, Object> params);

}
