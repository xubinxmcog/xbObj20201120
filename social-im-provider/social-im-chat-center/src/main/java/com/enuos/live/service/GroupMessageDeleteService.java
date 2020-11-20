package com.enuos.live.service;


import com.enuos.live.result.Result;

import java.util.List;
import java.util.Map;

/**
 * 聊天标记(GroupMessageDelete)表服务接口
 *
 * @author makejava
 * @since 2020-05-12 16:16:54
 */
public interface GroupMessageDeleteService {

  /**
   * 新增数据
   */
  void insertMessage(List<Map<String, Object>> list);

  /**
   * 修改数据
   *
   * @param params groupId 群聊ID、userId 用户ID
   * @return 更新结果
   */
  Result updateSignNum(Map<String, Object> params);

  /**
   * 获得最后时间
   *
   * @param groupId 群聊ID
   * @param userId 用户ID
   * @return 实例对象 最后的时间
   */
  String queryMessageDateTime(Long groupId, Long userId);


  void deleteMessage(Long groupId, Long userId);


  /**
   * 删除聊天记录
   *
   * @param groupId 群聊ID
   */
  void deleteGroupMessage(Long groupId);

  void deleteGroupMessageList(Long groupId, List<Long> list);

}