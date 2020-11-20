package com.enuos.live.mapper;

import com.enuos.live.pojo.Chat;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/9 16:37
 */
public interface ChatMapper {

  /**
   * 新增用户和用户聊天联系
   *
   * @param list the Chat_Info_List
   */
  void newUserChatLink(@Param("list") List<Chat> list);

  /**
   * 更新聊天设置
   *
   * @param chat chatId userId 其他字段
   */
  void updUserChatSetting(Chat chat);

  /**
   * 更新聊天信息
   *
   * @param chat chatId userId unread chatMessage
   */
  void updUserChatMessage(Chat chat);

  /**
   * TODO 更新设定.
   *
   * @param params 添加消息
   * @author WangCaiWen
   * @date 2020/8/3
   */
  void updateUserChatSetting(Map<String, Object> params);

  /**
   * 统一改变用户聊天状态
   *
   * @param userId 用户ID
   */
  void updateUserChatStatusAll(Long userId);

  /**
   * 获得聊天设置
   *
   * @param params chatId userId
   * @return info
   */
  Chat getChatInfo(Map<String, Object> params);

  /**
   * 获得聊天列表
   *
   * @param userId 用户ID
   * @return chatList
   */
  List<Map<String, Object>> getChatNoticeList(Long userId);

  /**
   * 解除所有联系
   *
   * @param userId 用户ID
   */
  void relieveChatRelation(Long userId);

  /**
   * 解除联系
   *
   * @param userId 用户ID
   * @param targetId 目标用户
   */
  void relieveRelation(@Param("userId") Long userId, @Param("targetId") Long targetId);

  /**
   * 检查彼此之间的联系
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @return 是否存在数据
   */
  Integer checkUserRelation(@Param("userId") Long userId, @Param("targetId") Long targetId);

  /**
   * 新增用户联系
   *
   * @param chat 实例数据
   */
  void insertUserRelation(Chat chat);

  /**
   * 清空残留消息
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   */
  void emptyResidualMessage(@Param("userId") Long userId, @Param("targetId") Long targetId);


  Integer userRelationIsExist(@Param("userId") Long userId, @Param("targetId") Long targetId);

  /**
   * TODO 获得未读数量.
   *
   * @param userId 用户ID
   * @return java.lang.Integer
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/19 9:02
   * @update 2020/8/19 9:02
   */
  Integer getUserUnreadNum(Long userId);
}
