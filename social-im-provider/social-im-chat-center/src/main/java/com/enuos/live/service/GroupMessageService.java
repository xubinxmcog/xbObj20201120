package com.enuos.live.service;

import com.enuos.live.result.Result;

import java.util.List;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/17 17:22
 */
public interface GroupMessageService {

  /**
   * 聊天提示
   *
   * @param params the groupId && type && content
   */
  void newGroupMessageBySort(Map<String, Object> params);

  /**
   * 新增聊天信息
   *
   * @param params the groupId && userId && content && chatType && messageId.
   * @return is add success code
   */
  Result newGroupMessage(Map<String, Object> params);

  /**
   * 获得聊天记录
   *
   * @param params the Group_Id && Page_Num &&  Page_Size.
   * @return is Message_List
   */
  Result getGroupMessage(Map<String, Object> params);

  /**
   * 删除聊天记录
   *
   * @param groupId 群聊ID
   */
  void deleteGroupMessage(Long groupId);
}
