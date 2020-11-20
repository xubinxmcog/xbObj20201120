package com.enuos.live.service;

import com.enuos.live.result.Result;

import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/27 13:45
 */
public interface NoticeInteractService {

  /**
   * 获得互动通知未读信息
   *
   * @param userId the User_Id
   * @return is User_Unread_Interactive_Info
   */
  Map<String, Object> getInteractUnreadInfo(Long userId);

  /**
   * 获得互动通知列表
   *
   * @param params the User_Id && Page_Num && Page_Size
   * @return is Interactive_Notice_List
   */
  Result getInteractNoticeList(Map<String, Object> params);

  /**
   * 保存互动通知数据
   *
   * @param params the Sponsor_Id && Receiver_Id && Story_Id || Attach_Id && Notice_Source
   * @return is Add Succeed Code
   */
  Result saveInteractNotice(Map<String, Object> params);

  /**
   * 根据通知ID - 删除请求通知
   *
   * @param params the Notice_Id
   * @return is Delete Succeed Code
   */
  Result deleteInteractNotice(Map<String, Object> params);

}
