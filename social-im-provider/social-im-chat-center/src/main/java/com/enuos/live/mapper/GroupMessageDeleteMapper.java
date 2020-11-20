package com.enuos.live.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/5/12 16:20
 */
public interface GroupMessageDeleteMapper {

  /**
   * 新增数据
   *
   * @param list Map={GroupId, UserId}
   */
  void insertMessage(@Param("list") List<Map<String, Object>> list);

  /**
   * 修改数据
   *
   * @param groupId 群聊ID
   * @param userId 用户ID
   */
  void updateSignNum(@Param("groupId") Long groupId, @Param("userId") Long userId);

  /**
   * 获得最后时间
   *
   * @param groupId 群聊ID
   * @param userId 用户ID
   * @return 实例对象 最后的时间
   */
  String queryMessageDateTime(@Param("groupId") Long groupId, @Param("userId") Long userId);

  /**
   * 删除聊天记录
   *
   * @param groupId 群聊ID
   */
  void deleteGroupMessage(Long groupId);


  void deleteMessage(@Param("groupId") Long groupId, @Param("userId") Long userId);

  void deleteGroupMessageList(@Param("groupId") Long groupId, @Param("list") List<Long> list);
}
