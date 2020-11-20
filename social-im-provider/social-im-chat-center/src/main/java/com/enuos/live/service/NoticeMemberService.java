package com.enuos.live.service;

import com.enuos.live.result.Result;
import java.util.Map;

/**
 * TODO 通知接口.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.0.0
 * @since 2020/4/27 13:45
 */

public interface NoticeMemberService {

  /**
   * TODO 用户未读信息.
   *
   * @param userId [用户ID]
   * @return [未读信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/4/29 18:20
   * @update 2020/11/13 9:28
   */
  Map<String, Object> getMemberUnreadInfo(Long userId);

  /**
   * TODO 软件通知列表.
   *
   * @param params [pageNum, pageSize, userId]
   * @return [通知列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/4/29 18:20
   * @update 2020/11/13 9:30
   */
  Result getMemberNoticeList(Map<String, Object> params);

  /**
   * TODO 获得通知详情.
   *
   * @param params [noticeId]
   * @return [通知详情]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/4/29 18:20
   * @update 2020/11/13 9:34
   */
  Result getNoticeInfo(Map<String, Object> params);

  /**
   * TODO 删除通知信息.
   *
   * @param params [noticeId]
   * @return [删除结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/4/29 18:20
   * @update 2020/11/13 9:36
   */
  Result deleteMemberNotice(Map<String, Object> params);

  /**
   * TODO 软件通知信息.
   *
   * @param params [noticeType, contentNo, contentTitle, contentIntro, createAdminId, createAdminName]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/4/29 18:20
   * @update 2020/11/13 9:38
   */
  Result saveMemberNoticeList(Map<String, Object> params);

  /**
   * TODO 群聊解散提醒.
   *
   * @param params [userIds, groupName]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/14 21:14
   * @update 2020/11/13 9:50
   */
  void sendGroupDissolveNotice(Map<String, Object> params);

  /**
   * TODO 歌曲审核.
   *
   * @param params [receiveId, songName, auditResult]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 10:36
   * @update 2020/11/13 10:36
   */
  Result songAuditNotice(Map<String, Object> params);

  /**
   * TODO 题目审核.
   *
   * @param params [receiveId, topicType, auditResult, coinReward]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 10:37
   * @update 2020/11/13 10:37
   */
  Result topicAuditNotice(Map<String, Object> params);

  /**
   * TODO 信息处理.
   *
   * @param params [receiveId, handleType, targetTime]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 10:37
   * @update 2020/11/13 10:37
   */
  Result infoAuditNotice(Map<String, Object> params);

  /**
   * TODO 举报反馈.
   *
   * @param params [receiveId, reportUser]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 10:40
   * @update 2020/11/13 10:40
   */
  Result memberAuditNotice(Map<String, Object> params);

  /**
   * TODO 处罚信息.
   *
   * @param params [receiveId, handleType, bannedDays, handleMessage]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 10:41
   * @update 2020/11/13 10:41
   */
  Result punishmentNotice(Map<String, Object> params);

  /**
   * TODO 活动信息.
   *
   * @param params [receiveId, activityName, activityCode, activityStatus]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 13:09
   * @update 2020/11/13 13:09
   */
  Result activityNotice(Map<String, Object> params);

  /**
   * TODO 物品信息.
   *
   * @param params [receiveId, itemName, itemType]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 13:09
   * @update 2020/11/13 13:09
   */
  Result itemNotice(Map<String, Object> params);

  /**
   * TODO 奖励信息.
   *
   * @param params [receiveId, coinReward]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 13:09
   * @update 2020/11/13 13:09
   */
  Result rewardNotice(Map<String, Object> params);

  /**
   * TODO 开播信息.
   *
   * @param params [receiveId, roomId, roomOwnerName, roomName]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 13:09
   * @update 2020/11/13 13:09
   */
  Result openLiveNotice(Map<String, Object> params);

  /**
   * TODO 礼物信息.
   *
   * @param params [receiveId, donorName, giftName, giftNum, rewardName, rewardNum]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 13:10
   * @update 2020/11/13 13:10
   */
  Result giftNotice(Map<String, Object> params);

  /**
   * TODO 会员信息.
   *
   * @param params [receiveId, noticeType, level]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 13:10
   * @update 2020/11/13 13:10
   */
  Result memberNotice(Map<String, Object> params);
}
