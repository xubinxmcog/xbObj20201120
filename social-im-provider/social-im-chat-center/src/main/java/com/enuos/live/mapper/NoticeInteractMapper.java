package com.enuos.live.mapper;

import com.enuos.live.pojo.NoticeInteract;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/27 13:47
 */
public interface NoticeInteractMapper {

  /**
   * 更新用户未读信息状态
   *
   * @param userId the User_Id
   */
  void updateUnreadNotice(Long userId);

  /**
   * 新增互动通知
   *
   * @param noticeInteract the Notice_Interactive
   */
  void saveInteractNotice(NoticeInteract noticeInteract);

  /**
   * 新增互动通知
   *
   * @param list the Notice_Info_List
   */
  void saveInteractNoticeList(@Param("list") List<Map<String, Object>> list);

  /**
   * 删除互动通知根据通知ID
   *
   * @param noticeId the Notice_Id
   */
  void deleteInteractNoticeById(Long noticeId);

  /**
   * 获得未读数量
   *
   * @param userId the User_Id
   * @return is count
   */
  Integer unreadInteractNum(Long userId);

  /**
   * 获取最后一条消息 的发起人
   *
   * @param userId the User_Id
   * @return is messageInfo
   */
  Map<String, Object> getLastInteractUserId(Long userId);

  /**
   * 获得请求通知列表
   *
   * @param userId the User_Id
   * @return is Notice_List
   */
  List<Map<String, Object>> interactNoticeList(Long userId);

}
