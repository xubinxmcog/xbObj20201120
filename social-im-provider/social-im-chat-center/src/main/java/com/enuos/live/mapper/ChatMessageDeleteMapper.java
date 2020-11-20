package com.enuos.live.mapper;

import com.enuos.live.pojo.ChatMessageDelete;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/5/12 16:20
 */
public interface ChatMessageDeleteMapper {

  /**
   * 新增数据
   *
   * @param list Map={userId, targetId}
   */
  void insertMessage(@Param("list") List<Map<String, Object>> list);

  void insertChatMessageRelation(ChatMessageDelete chatMessageDelete);

  /**
   * 修改数据
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   */
  void updateSignNum(@Param("userId") Long userId, @Param("targetId") Long targetId);

  /**
   * 获得最后时间
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @return 实例对象 最后的时间
   */
  String queryMessageDateTime(@Param("userId") Long userId, @Param("targetId") Long targetId);

  void deleteMessage(@Param("userId") Long userId, @Param("targetId") Long targetId);

  void deleteMessageAll(Long userId);
}
