package com.enuos.live.mapper;

import com.enuos.live.pojo.Group;

import java.util.List;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/15 18:09
 */
public interface GroupMapper {

  /**
   * 添加新的群聊
   *
   * @param group the param.
   */
  void newGroupChat(Group group);

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
   * 根据群聊ID获得群聊消息
   *
   * @param groupId the Group_Id
   * @return is Group_Info
   */
  Group getGroupInfo(Long groupId);

  /**
   * 获得对外公开的群信息
   *
   * @param params the Group_Id && User_Id.
   * @return is Group_Info
   */
  Map<String, Object> getOpenGroupInfo(Map<String, Object> params);

  /**
   * 获得用户所在的群聊列表
   *
   * @param userId the User_Id
   * @return is Group_List(name + group_id)
   */
  List<Map<String, Object>> getGroupListByUserId(Long userId);

  /**
   * 获得用户所在的群聊列表 根据群名称
   *
   * @param params the User_Id && Group_Name
   * @return is Group_List(name + group_id)l
   */
  List<Map<String, Object>> searchGroupByName(Map<String, Object> params);

  /**
   * 获得当前用户创建的群聊ID
   *
   * @param userId 用户ID
   * @return 群聊ID列表
   */
  List<Long> getUserCreateGroupIdList(Long userId);

  /**
   * TODO 降低等级.
   *
   * @param userId 用户ID
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/14 10:39
   * @update 2020/8/14 10:39
   */
  void lowerGroupGraded(Long userId);
}
