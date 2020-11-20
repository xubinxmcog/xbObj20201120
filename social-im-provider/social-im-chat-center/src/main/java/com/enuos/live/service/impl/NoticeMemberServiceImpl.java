package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.NoticeMemberMapper;
import com.enuos.live.pojo.NoticeMember;
import com.enuos.live.rest.SocketRemote;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.service.NoticeMemberService;
import com.enuos.live.service.NoticeService;
import com.enuos.live.utils.JsonUtils;
import com.enuos.live.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * TODO 系统通知实现.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.0.0
 * @since 2020/4/27 13:46
 */

@Slf4j
@Service("noticeMemberService")
public class NoticeMemberServiceImpl implements NoticeMemberService {

  @Resource
  private NoticeMemberMapper noticeMemberMapper;
  @Resource
  private NoticeService noticeService;
  @Resource
  private SocketRemote socketRemote;
  @Resource
  private UserRemote userRemote;

  /**
   * TODO 用户未读信息.
   *
   * @param userId [用户ID]
   * @return [未读信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/4/29 18:20
   * @update 2020/11/13 9:28
   */
  @Override
  public Map<String, Object> getMemberUnreadInfo(Long userId) {
    Map<String, Object> result = null;
    String title = this.noticeMemberMapper.getLastMemberNoticeTitle(userId);
    if (StringUtils.isNotEmpty(title)) {
      result = Maps.newHashMap();
      Integer unreadNum = this.noticeMemberMapper.getUnreadNum(userId);
      result.put("unreadNum", unreadNum);
      result.put("title", title);
    }
    return Objects.nonNull(result) ? result : Collections.emptyMap();
  }

  /**
   * TODO 软件通知列表.
   *
   * @param params [pageNum, pageSize, userId]
   * @return [通知列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/4/29 18:20
   * @update 2020/11/13 9:30
   */
  @Override
  public Result getMemberNoticeList(Map<String, Object> params) {
    Map<String, Object> result = Maps.newHashMap();
    if (params == null || params.isEmpty()) {
      return Result.error();
    }
    Long userId = ((Number) params.get("userId")).longValue();
    if (userId <= 0) {
      return Result.error();
    }
    PageHelper.startPage((Integer) params.get("pageNum"), (Integer) params.get("pageSize"));
    List<Map<String, Object>> memberNoticeList = this.noticeMemberMapper.memberNoticeList(userId);
    if (CollectionUtils.isNotEmpty(memberNoticeList)) {
      memberNoticeList.forEach(stringObjectMap -> {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Timestamp time = (Timestamp) stringObjectMap.get("lastTime");
        stringObjectMap.put("lastTime", dtf.format(time.toLocalDateTime()));
      });
    }
    PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(memberNoticeList);
    result.put("list", pageInfo.getList());
    result.put("total", pageInfo.getTotal());
    result.put("pageNum", pageInfo.getPageNum());
    result.put("pageSize", pageInfo.getPageSize());
    result.put("pages", pageInfo.getPages());
    // 更新未读
    this.noticeMemberMapper.updateUnreadNotice(userId);
    return Result.success(result);
  }

  /**
   * TODO 获得通知详情.
   *
   * @param params [noticeId]
   * @return [通知详情]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/4/29 18:20
   * @update 2020/11/13 9:34
   */
  @Override
  public Result getNoticeInfo(Map<String, Object> params) {
    Map<String, Object> result = Maps.newHashMap();
    if (params == null || params.isEmpty()) {
      return Result.error();
    }
    long noticeId = ((Number) params.get("noticeId")).longValue();
    if (noticeId <= 0) {
      return Result.error();
    }
    NoticeMember noticeMember = this.noticeMemberMapper.getNoticeMemberInfo(noticeId);
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    if (Objects.nonNull(noticeMember)) {
      result.put("contentType", noticeMember.getContentType());
      // 通知类型 [0-审核 1-系统 2-会员]
      Long contentNo = noticeMember.getContentNo();
      if (contentNo > 0) {
        Map<String, Object> notice = this.noticeService.getNoticeInfo(contentNo);
        if (Objects.nonNull(notice)) {
          result.putAll(notice);
        }
      } else {
        // 通知类型 0-普通 1-跳转
        if (noticeMember.getContentType() == 1) {
          result.put("roomId", noticeMember.getRoomId());
        }
        result.put("contentTitle", noticeMember.getContentTitle());
        result.put("contentIntro", noticeMember.getContentIntro());
        result.put("createTime", dtf.format(noticeMember.getCreateTime()));
      }
      return Result.success(result);
    }
    return Result.error();
  }

  /**
   * TODO 删除通知信息.
   *
   * @param params [noticeId]
   * @return [删除结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/4/29 18:20
   * @update 2020/11/13 9:36
   */
  @Override
  public Result deleteMemberNotice(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error();
    }
    Long noticeId = ((Number) params.get("noticeId")).longValue();
    if (noticeId <= 0) {
      return Result.error();
    }
    this.noticeMemberMapper.deleteMemberNoticeById(noticeId);
    return Result.success();
  }

  /**
   * TODO 软件通知信息.
   *
   * @param params [noticeType, contentNo, contentTitle, contentIntro, adminId, adminName]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/4/29 18:20
   * @update 2020/11/13 9:38
   */
  @Override
  public Result saveMemberNoticeList(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long contentNo = ((Number) params.get("contentNo")).longValue();
    if (contentNo <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    List<Long> userIdList = this.userRemote.getUserIdList();
    if (CollectionUtils.isNotEmpty(userIdList)) {
      Map<String, Object> result;
      // 一次插入的条数，也就是分批的list大小
      int pointsDataLimit = 1000;
      int listSize = userIdList.size();
      int maxSize = listSize - 1;
      List<Map<String, Object>> memberNoticeList = Lists.newLinkedList();
      for (int i = 0, length = userIdList.size(); i < length; i++) {
        //分批次处理
        result = Maps.newHashMap();
        result.put("userId", userIdList.get(i));
        result.put("noticeType", 1);
        result.put("contentNo", contentNo);
        result.put("contentTitle", params.get("contentTitle"));
        result.put("contentIntro", params.get("contentIntro"));
        result.put("createAdminId", params.get("adminId"));
        result.put("createAdminName", params.get("adminName"));
        memberNoticeList.add(result);
        //载体list达到要求,进行批量操作
        if (pointsDataLimit == memberNoticeList.size() || i == maxSize) {
          //调用批量插入
          this.noticeMemberMapper.saveMemberNoticeList(memberNoticeList);
          //每次批量操作后,清空载体list,等待下次的数据填入
          memberNoticeList.clear();
        }
      }
      //  远程调用通知接口
      this.socketRemote.newSystemNotice(params);
      return Result.success();
    } else {
      return Result.error(-1, "服务出现错误!推送失败");
    }
  }

  /**
   * TODO 群聊解散提醒.
   *
   * @param params [userIds, groupName]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/14 21:14
   * @update 2020/11/13 9:50
   */
  @Override
  public void sendGroupDissolveNotice(Map<String, Object> params) {
    List<Long> userIdList = JsonUtils.toListType(params.get("userIds"), Long.class);
    List<Map<String, Object>> noticeList = Lists.newLinkedList();
    String groupName = StringUtils.nvl(params.get("groupName"));
    String message = "您加入的群聊 " + groupName + " 已被群主解散.";
    String title = "群聊信息";
    Map<String, Object> result;
    for (Long userId : userIdList) {
      result = Maps.newHashMap();
      result.put("userId", userId);
      result.put("noticeType", 1);
      result.put("contentTitle", title);
      result.put("contentIntro", message);
      noticeList.add(result);
    }
    this.noticeMemberMapper.saveGroupDissolveNotice(noticeList);
    this.socketRemote.dissolveChatNotice(params);
  }

  /**
   * TODO 歌曲审核.
   *
   * @param params [receiveId, songName, auditResult, adminId, adminName]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 10:36
   * @update 2020/11/13 10:36
   */
  @Override
  public Result songAuditNotice(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long receiveId = ((Number) params.get("receiveId")).longValue();
    Integer auditResult = (Integer) params.get("auditResult");
    String songName = StringUtils.nvl(params.get("songName"));
    String message;
    // 审核结果 1-成功 2-失败
    if (auditResult == 1) {
      message = "您上传的歌曲 《" + songName + "》 已通过审核，快去语音房听听吧。";
    } else {
      message = "您上传的歌曲 《" + songName + "》 未通过审核。";
    }
    NoticeMember noticeMember = new NoticeMember();
    noticeMember.setUserId(receiveId);
    noticeMember.setNoticeType(0);
    noticeMember.setContentTitle("歌曲审核");
    noticeMember.setContentIntro(message);
    noticeMember.setAdminId((Integer) params.get("adminId"));
    noticeMember.setAdminName(StringUtils.nvl(params.get("adminName")));
    this.noticeMemberMapper.saveMemberNotice(noticeMember);
    return Result.success();
  }

  /**
   * TODO 题目审核.
   *
   * @param params [receiveId, topicType, auditResult, coinReward, adminId, adminName]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 10:37
   * @update 2020/11/13 10:37
   */
  @Override
  public Result topicAuditNotice(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long receiveId = ((Number) params.get("receiveId")).longValue();
    Integer auditResult = (Integer) params.get("auditResult");
    Integer topicType = (Integer) params.get("topicType");
    String gameName;
    // 题目类型 1-你画我猜 2-谁是卧底 3-你说我猜 4-一站到底
    switch (topicType) {
      case 1:
        gameName = "你画我猜";
        break;
      case 2:
        gameName = "谁是卧底";
        break;
      case 3:
        gameName = "你说我猜";
        break;
      default:
        gameName = "一站到底";
        break;
    }
    String message;
    // 审核结果 1-成功 2-失败
    if (auditResult == 1) {
      Integer coinReward = (Integer) params.get("coinReward");
      message = "您提交的-" + gameName + "-题目已通过审核，" + coinReward + "金币奖励已发至您的账号";
    } else {
      message = "您提交的-" + gameName + "-题目不符合规范/已存在，未通过审核。";
    }
    NoticeMember noticeMember = new NoticeMember();
    noticeMember.setUserId(receiveId);
    noticeMember.setNoticeType(0);
    noticeMember.setContentTitle("题目审核");
    noticeMember.setContentIntro(message);
    noticeMember.setAdminId((Integer) params.get("adminId"));
    noticeMember.setAdminName(StringUtils.nvl(params.get("adminName")));
    this.noticeMemberMapper.saveMemberNotice(noticeMember);
    return Result.success();
  }

  /**
   * TODO 信息处理.
   *
   * @param params [receiveId, handleType, targetTime, adminId, adminName]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 10:37
   * @update 2020/11/13 10:37
   */
  @Override
  public Result infoAuditNotice(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long receiveId = ((Number) params.get("receiveId")).longValue();
    Integer handleType = (Integer) params.get("handleType");
    String message;
    // 处理类型 1-动态 2-头像/背景 3-语音房封面
    switch (handleType) {
      case 1:
        String targetTime = StringUtils.nvl(params.get("targetTime"));
        message = "您于 " + targetTime + " 发布的动态可能涉嫌违规，已被删除，请下次注意！";
        break;
      case 2:
        message = "您的 头像/背景 可能涉嫌违规，已被重置，请下次注意！";
        break;
      default:
        message = "您上传的 语音房封面 可能涉嫌违规，已被重置，请下次注意！";
        break;
    }
    NoticeMember noticeMember = new NoticeMember();
    noticeMember.setUserId(receiveId);
    noticeMember.setNoticeType(0);
    noticeMember.setContentTitle("信息处理");
    noticeMember.setContentIntro(message);
    noticeMember.setAdminId((Integer) params.get("adminId"));
    noticeMember.setAdminName(StringUtils.nvl(params.get("adminName")));
    this.noticeMemberMapper.saveMemberNotice(noticeMember);
    return Result.success();
  }

  /**
   * TODO 举报反馈.
   *
   * @param params [receiveId, reportUser, adminId, adminName]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 10:40
   * @update 2020/11/13 10:40
   */
  @Override
  public Result memberAuditNotice(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long receiveId = ((Number) params.get("receiveId")).longValue();
    String reportUser = StringUtils.nvl(params.get("reportUser"));
    String message = "您举报的 " + reportUser + " 已被处理，感谢您的反馈。";
    NoticeMember noticeMember = new NoticeMember();
    noticeMember.setUserId(receiveId);
    noticeMember.setNoticeType(0);
    noticeMember.setContentTitle("举报反馈");
    noticeMember.setContentIntro(message);
    noticeMember.setAdminId((Integer) params.get("adminId"));
    noticeMember.setAdminName(StringUtils.nvl(params.get("adminName")));
    this.noticeMemberMapper.saveMemberNotice(noticeMember);
    return Result.success();
  }

  /**
   * TODO 处罚信息.
   *
   * @param params [receiveId, handleType, bannedDays, handleMessage, adminId, adminName]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 10:41
   * @update 2020/11/13 10:41
   */
  @Override
  public Result punishmentNotice(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long receiveId = ((Number) params.get("receiveId")).longValue();
    Integer handleType = (Integer) params.get("handleType");
    String handleMessage = StringUtils.nvl(params.get("handleMessage"));
    String message;
    // 处罚类型 1-警告 2-永封
    if (handleType == 1) {
      Integer bannedDays = (Integer) params.get("bannedDays");
      message = "您因 " + handleMessage + " 被封号" + bannedDays + " 天，作为警告，希望遵循相关规定。";
    } else {
      message = "您因 " + handleMessage + " 被永久封号，希望遵循相关规定，如有疑问，请联系官方！";
    }
    NoticeMember noticeMember = new NoticeMember();
    noticeMember.setUserId(receiveId);
    noticeMember.setNoticeType(0);
    noticeMember.setContentTitle("处罚信息");
    noticeMember.setContentIntro(message);
    noticeMember.setAdminId((Integer) params.get("adminId"));
    noticeMember.setAdminName(StringUtils.nvl(params.get("adminName")));
    this.noticeMemberMapper.saveMemberNotice(noticeMember);
    return Result.success();
  }

  /**
   * TODO 活动信息.
   *
   * @param params [receiveId, activityName, activityCode, activityStatus]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 13:09
   * @update 2020/11/13 13:09
   */
  @Override
  public Result activityNotice(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long receiveId = ((Number) params.get("receiveId")).longValue();
    Integer activityStatus = (Integer) params.get("activityStatus");
    String activityName = StringUtils.nvl(params.get("activityName"));
    String message;
    // 活动状态 1-活动开启 2-即将结束 3-活动结束
    switch (activityStatus) {
      case 1:
        message = "活动 " + activityName + " 现已开启，快来参加吧！";
        break;
      case 2:
        message = "活动 " + activityName + " 即将结束, 部分道具将被清除，请及时兑换或使用。";
        break;
      default:
        message = "活动 " + activityName + " 现已结束，感谢您的参加！";
        break;
    }
    NoticeMember noticeMember = new NoticeMember();
    noticeMember.setUserId(receiveId);
    noticeMember.setNoticeType(1);
    noticeMember.setContentTitle("活动信息");
    noticeMember.setContentIntro(message);
    this.noticeMemberMapper.saveMemberNotice(noticeMember);
    return Result.success();
  }

  /**
   * TODO 物品信息.
   *
   * @param params [receiveId, itemName, itemType]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 13:09
   * @update 2020/11/13 13:09
   */
  @Override
  public Result itemNotice(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long receiveId = ((Number) params.get("receiveId")).longValue();
    Integer itemType = (Integer) params.get("itemType");
    String itemName = StringUtils.nvl(params.get("itemName"));
    String message;
    // 物品类型 1-礼物卷 2-装饰
    if (itemType == 1) {
      message = "您的 " + itemName + " 礼物卷即将到期，请尽快使用。";
    } else {
      message = "您的装饰 " + itemName + " 即将到期，快去商城看看吧。”";
    }
    NoticeMember noticeMember = new NoticeMember();
    noticeMember.setUserId(receiveId);
    noticeMember.setNoticeType(1);
    noticeMember.setContentTitle("物品信息");
    noticeMember.setContentIntro(message);
    this.noticeMemberMapper.saveMemberNotice(noticeMember);
    return Result.success();
  }

  /**
   * TODO 奖励信息.
   *
   * @param params [receiveId, coinReward]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 13:09
   * @update 2020/11/13 13:09
   */
  @Override
  public Result rewardNotice(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long receiveId = ((Number) params.get("receiveId")).longValue();
    Integer coinReward = (Integer) params.get("coinReward");
    String message = coinReward + "金币奖励已发到您的账户，请注意查看。";
    NoticeMember noticeMember = new NoticeMember();
    noticeMember.setUserId(receiveId);
    noticeMember.setNoticeType(1);
    noticeMember.setContentTitle("奖励信息");
    noticeMember.setContentIntro(message);
    this.noticeMemberMapper.saveMemberNotice(noticeMember);
    return Result.success();
  }

  /**
   * TODO 开播信息.
   *
   * @param params [receiveId, roomId, roomOwnerName, roomName]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 13:09
   * @update 2020/11/13 13:09
   */
  @Override
  public Result openLiveNotice(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long receiveId = ((Number) params.get("receiveId")).longValue();
    Long roomId = ((Number) params.get("roomId")).longValue();
    String roomOwnerName = StringUtils.nvl(params.get("roomOwnerName"));
    String roomName = StringUtils.nvl(params.get("roomName"));
    String message = "您关注的 " + roomOwnerName + "-" + roomName + " 开播啦！立即前往。";
    NoticeMember noticeMember = new NoticeMember();
    noticeMember.setUserId(receiveId);
    noticeMember.setRoomId(roomId);
    noticeMember.setNoticeType(1);
    noticeMember.setContentTitle("开播信息");
    noticeMember.setContentIntro(message);
    noticeMember.setContentType(1);
    this.noticeMemberMapper.saveMemberNotice(noticeMember);
    return Result.success();
  }

  /**
   * TODO 礼物信息.
   *
   * @param params [receiveId, donorName, giftName, giftNum, rewardName, rewardNum]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 13:10
   * @update 2020/11/13 13:10
   */
  @Override
  public Result giftNotice(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long receiveId = ((Number) params.get("receiveId")).longValue();
    String donorName = StringUtils.nvl(params.get("donorName"));
    String giftName = StringUtils.nvl(params.get("giftName"));
    Integer giftNum = (Integer) params.get("giftNum");
    String rewardName = StringUtils.nvl(params.get("rewardName"));
    Integer rewardNum = (Integer) params.get("rewardNum");
    String message = donorName + "送给你" + giftNum + "个" + giftName + "，获得" + rewardName + "x" + rewardNum + "。";
    NoticeMember noticeMember = new NoticeMember();
    noticeMember.setUserId(receiveId);
    noticeMember.setNoticeType(1);
    noticeMember.setContentTitle("礼物信息");
    noticeMember.setContentIntro(message);
    this.noticeMemberMapper.saveMemberNotice(noticeMember);
    return Result.success();
  }

  /**
   * TODO 会员信息.
   *
   * @param params [receiveId, noticeType, level]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/13 13:10
   * @update 2020/11/13 13:10
   */
  @Override
  public Result memberNotice(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long receiveId = ((Number) params.get("receiveId")).longValue();
    Integer noticeType = (Integer) params.get("noticeType");
    String message;
    // 通知类型 1-开通会员 2-提升等级 3-到期提醒 4-会员过期
    switch (noticeType) {
      case 1:
        message = "恭喜您成功开通会员";
        break;
      case 2:
        Integer level = (Integer) params.get("level");
        message = "恭喜您会员等级成功升到" + level + "级";
        break;
      case 3:
        message = "您的会员即将到期， 续费可继续享受会员特权";
        break;
      default:
        message = "您的会员已过期，每天将扣除一定成长值，重新开通将继承当前成长值";
        break;
    }
    NoticeMember noticeMember = new NoticeMember();
    noticeMember.setUserId(receiveId);
    noticeMember.setNoticeType(2);
    noticeMember.setContentTitle("会员信息");
    noticeMember.setContentIntro(message);
    this.noticeMemberMapper.saveMemberNotice(noticeMember);
    return Result.success();
  }
}
