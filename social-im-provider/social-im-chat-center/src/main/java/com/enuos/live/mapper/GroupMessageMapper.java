package com.enuos.live.mapper;

import com.enuos.live.pojo.GroupMessage;

import java.util.List;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/20 8:50
 */
public interface GroupMessageMapper {

  /**
   * 新增聊天记录
   *
   * @param groupMessage 记录参数
   */
  void newGroupMessage(GroupMessage groupMessage);

  /**
   * 新增聊天提示
   *
   * @param groupMessage 记录参数
   */
  void newGroupMessageBySort(GroupMessage groupMessage);

  /**
   * 获得聊天记录
   *
   * @param params the Group_Id AND USERiD
   * @return is Message_List
   */
  List<Map<String, Object>> getGroupMessageList(Map<String, Object> params);

  /**
   * 删除聊天记录
   *
   * @param groupId 群聊ID
   */
  void deleteGroupMessage(Long groupId);
}
