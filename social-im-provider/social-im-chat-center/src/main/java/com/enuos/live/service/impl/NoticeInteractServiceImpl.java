package com.enuos.live.service.impl;

import com.enuos.live.mapper.NoticeInteractMapper;
import com.enuos.live.pojo.NoticeInteract;
import com.enuos.live.rest.SocketRemote;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.service.NoticeInteractService;
import com.enuos.live.utils.JsonUtils;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/27 13:46
 */
@Slf4j
@Service("noticeInteractService")
public class NoticeInteractServiceImpl implements NoticeInteractService {

  @Resource
  private NoticeInteractMapper noticeInteractMapper;
  @Resource
  private UserRemote userRemote;
  @Resource
  private SocketRemote socketRemote;

  @Override
  public Map<String, Object> getInteractUnreadInfo(Long userId) {
    List<Long> userIdList = new LinkedList<>();
    Map<String, Object> result = this.noticeInteractMapper.getLastInteractUserId(userId);
    if (result != null) {
      Long sponsorId = ((Number) result.get("userId")).longValue();
      userIdList.add(sponsorId);
      List<Map<String, Object>> userInfoList = this.userRemote.getUserList(userIdList);
      if (CollectionUtils.isNotEmpty(userInfoList)) {
        Integer unreadNum = this.noticeInteractMapper.unreadInteractNum(userId);
        result.put("nickName", userInfoList.get(0).get("nickName"));
        result.put("unreadNum", unreadNum);
        result.remove("sponsorId");
      }
    }
    return result;
  }

  @Override
  public Result getInteractNoticeList(Map<String, Object> params) {
    Map<String, Object> result = new HashMap<>(16);
    if (params == null || params.isEmpty()) {
      return Result.error();
    }
    Long userId = ((Number) params.get("userId")).longValue();
    if (userId <= 0) {
      return Result.error();
    }
    List<Long> userIdList = new LinkedList<>();
    PageHelper.startPage((Integer) params.get("pageNum"), (Integer) params.get("pageSize"));
    List<Map<String, Object>> interactiveNoticeList = this.noticeInteractMapper.interactNoticeList(userId);
    if (interactiveNoticeList.size() > 0) {
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      interactiveNoticeList.forEach(stringObjectMap -> {
        Timestamp time = (Timestamp) stringObjectMap.get("lastTime");
        stringObjectMap.put("lastTime", dtf.format(time.toLocalDateTime()));
        userIdList.add(((Number) stringObjectMap.get("userId")).longValue());
      });
    }
    // 远程调用用户信息
    List<Map<String, Object>> userInfoList = this.userRemote.getUserList(userIdList);
    if (userInfoList != null) {
      for (Map<String, Object> objectMap : userInfoList) {
        long sendId = ((Number)objectMap.get("userId")).longValue();
        for (Map<String, Object> map : interactiveNoticeList) {
          long sendUser = ((Number)map.get("userId")).longValue();
          if (sendId == sendUser) {
            map.put("nickName", objectMap.get("nickName"));
            map.put("thumbIconUrl", objectMap.get("iconUrl"));
            map.put("sex", objectMap.get("sex"));
          }
        }
      }
    }
    PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(interactiveNoticeList);
    result.put("list", pageInfo.getList());
    result.put("total", pageInfo.getTotal());
    result.put("pageNum", pageInfo.getPageNum());
    result.put("pageSize", pageInfo.getPageSize());
    result.put("pages", pageInfo.getPages());
    // 更新未读
    this.noticeInteractMapper.updateUnreadNotice(userId);
    return Result.success(result);
  }

  @Override
  public Result saveInteractNotice(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error();
    }
    Long sponsorId = ((Number) params.get("sponsorId")).longValue();
    if (sponsorId <= 0) {
      return Result.error();
    }
    // 通知来源 [0 关注用户 1 评论动态 2 回复评论 3 转发动态 4 点赞动态 5 点赞评论 6 @xxx用户 ]
    Integer source = (Integer) params.get("source");
    NoticeInteract noticeInteract = new NoticeInteract();
    switch (source) {
      case 0:
        noticeInteract.setSponsorId(sponsorId);
        noticeInteract.setReceiverId(((Number) params.get("receiverId")).longValue());
        noticeInteract.setNoticeSource(0);
        this.noticeInteractMapper.saveInteractNotice(noticeInteract);
        break;
      case 1:
        noticeInteract.setSponsorId(sponsorId);
        noticeInteract.setReceiverId(((Number) params.get("receiverId")).longValue());
        noticeInteract.setStoryId((Integer) params.get("storyId"));
        noticeInteract.setNoticeSource(1);
        this.noticeInteractMapper.saveInteractNotice(noticeInteract);
        break;
      case 2:
        noticeInteract.setSponsorId(sponsorId);
        noticeInteract.setReceiverId(((Number) params.get("receiverId")).longValue());
        noticeInteract.setStoryId((Integer) params.get("storyId"));
        noticeInteract.setAttachId((Integer) params.get("attachId"));
        noticeInteract.setNoticeSource(2);
        this.noticeInteractMapper.saveInteractNotice(noticeInteract);
        break;
      case 3:
        noticeInteract.setSponsorId(sponsorId);
        noticeInteract.setReceiverId(((Number) params.get("receiverId")).longValue());
        noticeInteract.setStoryId((Integer) params.get("storyId"));
        noticeInteract.setNoticeSource(3);
        this.noticeInteractMapper.saveInteractNotice(noticeInteract);
        break;
      case 4:
        noticeInteract.setSponsorId(sponsorId);
        noticeInteract.setReceiverId(((Number) params.get("receiverId")).longValue());
        noticeInteract.setStoryId((Integer) params.get("storyId"));
        noticeInteract.setNoticeSource(4);
        this.noticeInteractMapper.saveInteractNotice(noticeInteract);
        break;
      case 5:
        noticeInteract.setSponsorId(sponsorId);
        noticeInteract.setReceiverId(((Number) params.get("receiverId")).longValue());
        noticeInteract.setStoryId((Integer) params.get("storyId"));
        noticeInteract.setAttachId((Integer) params.get("attachId"));
        noticeInteract.setNoticeSource(5);
        this.noticeInteractMapper.saveInteractNotice(noticeInteract);
        break;
      default:
        List<Integer> userIds = JsonUtils.toListType(params.get("userIds"), Integer.class);
        Integer storyId = (Integer) params.get("storyId");
        if (CollectionUtils.isNotEmpty(userIds) && storyId != null && storyId > 0) {
          List<Map<String, Object>> interactiveList = Lists.newLinkedList();
          userIds.forEach(userId -> {
            Map<String, Object> result = Maps.newHashMap();
            result.put("sponsorId", sponsorId);
            result.put("receiverId", userId);
            result.put("storyId", storyId);
            result.put("noticeSource", 6);
            interactiveList.add(result);
          });
          if (CollectionUtils.isNotEmpty(interactiveList)) {
            this.noticeInteractMapper.saveInteractNoticeList(interactiveList);
          }
        }
        break;
    }
    //  调用提醒通知用户
    this.socketRemote.newInteractNotice(params);
    return Result.success();
  }

  @Override
  public Result deleteInteractNotice(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error();
    }
    Long noticeId = ((Number) params.get("noticeId")).longValue();
    if (noticeId <= 0) {
      return Result.error();
    }
    this.noticeInteractMapper.deleteInteractNoticeById(noticeId);
    return Result.success();
  }
}
