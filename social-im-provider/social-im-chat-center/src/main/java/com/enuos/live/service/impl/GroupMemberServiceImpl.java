package com.enuos.live.service.impl;

import com.enuos.live.component.FeignMultipartFile;
import com.enuos.live.constants.ChatRedisKey;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.GroupMemberMapper;
import com.enuos.live.pojo.Group;
import com.enuos.live.pojo.GroupMember;
import com.enuos.live.pojo.PicInfo;
import com.enuos.live.proto.d10001msg.D10001;
import com.enuos.live.rest.SocketRemote;
import com.enuos.live.rest.UploadRemote;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.service.GroupMemberService;
import com.enuos.live.service.GroupMessageDeleteService;
import com.enuos.live.service.GroupMessageFileService;
import com.enuos.live.service.GroupMessageService;
import com.enuos.live.service.GroupMessageVoiceService;
import com.enuos.live.service.GroupService;
import com.enuos.live.service.NoticeMemberService;
import com.enuos.live.utils.ImageUtil;
import com.enuos.live.utils.JsonUtils;
import com.enuos.live.utils.RedisUtils;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * TODO 群聊成员管理服务实现.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V1.0.0
 * @since 2020/4/21 9:21
 */

@Slf4j
@Service("groupMemberService")
public class GroupMemberServiceImpl implements GroupMemberService {

  /** MAPPER. */
  @Resource
  private GroupMemberMapper groupMemberMapper;

  /** SERVICE. */
  @Resource
  private GroupService groupService;
  @Resource
  private GroupMessageService groupMessageService;
  @Resource
  private GroupMessageVoiceService groupMessageVoiceService;
  @Resource
  private GroupMessageFileService groupMessageFileService;
  @Resource
  private GroupMessageDeleteService groupMessageDeleteService;
  @Resource
  private NoticeMemberService noticeMemberService;

  /** FEIGN. */
  @Resource
  private UserRemote userRemote;
  @Resource
  private SocketRemote socketRemote;
  @Resource
  private UploadRemote uploadRemote;

  /** REDIS. */
  @Resource
  private RedisUtils redisUtils;

  private static final int FLAG_INDEX = 2;
  private static final int GROUP_ADMIN_MAX_1 = 3;
  private static final int GROUP_ADMIN_MAX_2 = 6;

  private static final String USER_FLAG_TOP = "flagTop";
  private static final String USER_FLAG_DELETE = "flagDelete";
  private static final String USER_NOT_DISTURB = "notDisturb";

  /**
   * TODO 成员头像.
   *
   * @param groupId [群聊ID]
   * @return [头像列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/13 10:29
   * @update 2020/8/11 17:59
   */
  @Override
  public List<String> getGroupIcon(Long groupId) {
    List<String> userIconUrl = Lists.newLinkedList();
    List<Long> userIdList = this.groupMemberMapper.getShowUserIdList(groupId);
    List<Map<String, Object>> userInfoList = this.userRemote.getUserList(userIdList);
    if (CollectionUtils.isNotEmpty(userInfoList)) {
      for (Map<String, Object> map : userInfoList) {
        userIconUrl.add(StringUtils.nvl(map.get("iconUrl")));
      }
    }
    return userIconUrl;
  }

  /**
   * TODO 展示成员.
   *
   * @param groupId [群聊ID]
   * @return [用户列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/13 10:29
   * @update 2020/8/11 17:47
   */
  @Override
  public List<Long> getShowUserIdList(Long groupId) {
    return this.groupMemberMapper.getShowUserIdList(groupId);
  }

  /**
   * TODO 批量保存.
   *
   * @param list [用户列表].
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public void saveGroupUserList(List<Map<String, Object>> list) {
    this.groupMemberMapper.saveGroupUserList(list);
  }

  /**
   * TODO 更新图标.
   *
   * @param groupId [群聊ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public void updateGroupNameOrIcon(Long groupId) {
    try {
      Group obj = new Group();
      obj.setGroupId(groupId);
      List<Long> userIdList = this.groupMemberMapper.getShowUserIdList(groupId);
      Group group = this.groupService.getGroupInfo(groupId);
      Integer groupAuto = group.getGroupAuto();
      if (groupAuto == 0) {
        List<Long> newUserInfoList;
        if (group.getGroupNum() <= 5) {
          newUserInfoList = userIdList.subList(0, group.getGroupNum());
          String groupName = this.userRemote.getNickName(newUserInfoList);
          if (groupName != null) {
            groupName = interceptGroupName(groupName);
            obj.setGroupName(groupName);
          }
        } else {
          newUserInfoList = userIdList.subList(0, 5);
          String groupName = this.userRemote.getNickName(newUserInfoList);
          if (groupName != null) {
            groupName = interceptGroupName(groupName);
            obj.setGroupName(groupName);
          }
        }
      }
      if (group.getGroupNum() <= 9) {
        List<Map<String, Object>> userInfoList = this.userRemote.getUserList(userIdList);
        if (userInfoList != null) {
          List<String> userIconUrl = Lists.newLinkedList();
          for (Map<String, Object> map : userInfoList) {
            userIconUrl.add(StringUtils.nvl(map.get("iconUrl")));
          }
          byte[] imageFile = ImageUtil.getCombinationOfHead(userIconUrl);
          InputStream inputStream = new ByteArrayInputStream(imageFile);
          String uuid = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
          MultipartFile file = new FeignMultipartFile("file", uuid + ".jpg", ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
          Result result = this.uploadRemote.uploadChatFile(file);
          PicInfo picInfo1 = result.getCode().equals(0) ? JsonUtils.toObjectPojo(result.getData(), PicInfo.class) : null;
          if (picInfo1 != null) {
            obj.setGroupIcon(picInfo1.getPicUrl());
          }
        }
      }
      if (StringUtils.isNotEmpty(obj.getGroupName()) || StringUtils.isNotEmpty(obj.getGroupIcon())) {
        this.groupService.updateGroupInfo(obj);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      log.error(ExceptionUtil.getStackTrace(e));
    }
  }


  private String interceptGroupName(String groupName) {
    if (Objects.nonNull(groupName)) {
      if (groupName.length() > 17) {
        String intercept = groupName.substring(0, 16);
        String lastString = intercept.substring(15, 16);
        if (Arrays.asList(",", ".", "，", "。").contains(lastString)) {
          intercept = intercept.substring(0, 15);
        }
        groupName = intercept + "...";
      }
    }
    return groupName;
  }

  /**
   * TODO 解散群聊.
   *
   * @param groupId [群聊ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public void dissolveGroupChat(Long groupId) {
    // 1) 删除群
    this.groupService.deleteGroupAdmin(groupId);
    // 3）删除群用户
    this.groupMemberMapper.deleteGroupUserAll(groupId);
    // 4) 删除聊天记录
    this.groupMessageService.deleteGroupMessage(groupId);
    // 5) 删除聊天文件
    this.groupMessageFileService.deleteGroupMessage(groupId);
    // 6) 删除语音记录
    this.groupMessageVoiceService.deleteGroupMessage(groupId);
    // 7) 删除其他记录
    this.groupMessageDeleteService.deleteGroupMessage(groupId);
  }

  /**
   * TODO 离开群聊.
   *
   * @param userId 用户ID
   * @author wangcaiwen|1443710411@qq.com
   * @create 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public void leaveJoinGroupChat(Long userId) {
    List<Long> groupIds = this.groupMemberMapper.userJoinGroupId(userId);
    this.groupMemberMapper.leaveJoinGroupChat(userId);
    if (groupIds != null && groupIds.size() > 0) {
      for (Long groupId : groupIds) {
        updateGroupNameAndGroupIcon(groupId);
      }
    }
  }

  /**
   * TODO 更新状态.
   *
   * @param userId 用户ID
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/7/28 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public void updateUserChatStatusAll(Long userId) {
    this.groupMemberMapper.updateUserChatStatusAll(userId);
  }

  /**
   * TODO 成员信息.
   *
   * @param groupId 群聊ID
   * @param userId 用户ID
   * @return 成员信息
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @since 2020/7/28 10:29
   */
  @Override
  public GroupMember getGroupUserInfo(Long groupId, Long userId) {
    return this.groupMemberMapper.getGroupUserInfo(groupId, userId);
  }

  /**
   * TODO 离开群聊.
   *
   * @param params 离开信息
   * @return 离开结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @since 2020/7/28 10:29
   */
  @Override
  public Result leaveGroupChat(Map<String, Object> params) {
    Map<String, Object> result;
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long groupId = ((Number) params.get("groupId")).longValue();
    Long userId = ((Number) params.get("userId")).longValue();
    if (groupId <= 0 && userId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    Group group = this.groupService.getGroupInfo(groupId);
    //  如若是群主 则为解散群
    if (group.getGroupAdmin().equals(userId)) {
      result = Maps.newHashMap();
      List<Long> userIds = this.groupMemberMapper.getGroupUserAllList(groupId);
      // 1) 删除群
      this.groupService.deleteGroupAdmin(groupId);
      // 3）删除群用户
      this.groupMemberMapper.deleteGroupUserAll(groupId);
      // 4) 删除聊天记录
      this.groupMessageService.deleteGroupMessage(groupId);
      // 5) 删除聊天文件
      this.groupMessageFileService.deleteGroupMessage(groupId);
      // 6) 删除语音记录
      this.groupMessageVoiceService.deleteGroupMessage(groupId);
      // 7) 删除其他记录
      this.groupMessageDeleteService.deleteGroupMessage(groupId);
      this.redisUtils.del(ChatRedisKey.KEY_CHAT_GROUP_MEMBER + groupId);
      for (Long memberId : userIds) {
        this.redisUtils.del(ChatRedisKey.KEY_CHAT_GROUP_LOGIN + memberId);
      }
      userIds.removeIf(along -> along.equals(userId));
      if (userIds.size() > 0) {
        result.put("groupName", group.getGroupName());
        result.put("userIds", userIds);
        // 发送通知
        this.noticeMemberService.sendGroupDissolveNotice(result);
      }
    } else {
      result = Maps.newHashMap();
      // 1）删除用户
      this.groupMemberMapper.deleteGroupUser(groupId, userId);
      // 2）生成记录
      List<Long> userIdList = Lists.newLinkedList();
      userIdList.add(userId);
      String leaveMessage = this.userRemote.getNickName(userIdList);
      if (StringUtils.isNotEmpty(leaveMessage)) {
        result.put("groupId", groupId);
        result.put("message", leaveMessage);
        result.put("sort", 2);
        this.groupMessageService.newGroupMessageBySort(result);
      }
      // 3）删除语音记录
      this.groupMessageVoiceService.deleteGroupVoiceMessage(groupId, userId);
      // 4）删除其他记录
      this.groupMessageDeleteService.deleteMessage(groupId, userId);
      // 5）更新房间数据
      updateGroupNameAndGroupIcon(groupId);
      // 刷新缓存
      this.redisUtils.del(ChatRedisKey.KEY_CHAT_GROUP_MEMBER + groupId);
      Group groupInfo = this.groupService.getGroupInfo(groupId);
      List<Long> groupMemberList = getGroupUserIdList(groupId);
      D10001.GroupMember.Builder groupBuilder = D10001.GroupMember.newBuilder();
      groupBuilder.setGroupIcon(groupInfo.getGroupIcon());
      groupBuilder.setGroupName(groupInfo.getGroupName());
      groupBuilder.addAllMemberId(groupMemberList);
      this.redisUtils.setByte(ChatRedisKey.KEY_CHAT_GROUP_MEMBER + groupId, groupBuilder.build().toByteArray());
    }
    return Result.success();
  }

  /**
   * TODO 移交权限.
   *
   * @param params 移交消息
   * @return 移交结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public Result handOverGroupAdminLimit(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    long groupId = ((Number) params.get("groupId")).longValue();
    long userId = ((Number) params.get("userId")).longValue();
    long targetId = ((Number) params.get("targetId")).longValue();
    if (groupId <= 0 || userId <= 0 || targetId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    Group group = this.groupService.getGroupInfo(groupId);
    if (!group.getGroupAdmin().equals(userId)) {
      return Result.error(ErrorCode.GROUP_CHAT_AUTHORITY_NULL);
    }
    // 群主是否会员 群聊等级
    Result adminResult = this.userRemote.getBase(userId);
    Map<String, Object> adminInfo = adminResult.getCode().equals(0) ? JsonUtils.toObjectMap(adminResult.getData()) : null;
    Integer adminMember;
    if (adminInfo != null) {
      adminMember = (Integer) adminInfo.get("isMember");
    } else {
      return Result.error(ErrorCode.NETWORK_ERROR);
    }
    //是否会员[-1 过期会员 0 否 1 是]
    if (adminMember < 1) {
      //  0 普通成员 1 管理员 2 群主
      GroupMember groupMember = new GroupMember();
      groupMember.setGroupId(groupId);
      groupMember.setUserId(targetId);
      // 转移群主身份
      groupMember.setFlagAuthority(2);
      this.groupMemberMapper.updateGroupUserInfo(groupMember);
      groupMember.setUserId(userId);
      // 降为普通成员
      groupMember.setFlagAuthority(0);
      this.groupMemberMapper.updateGroupUserInfo(groupMember);
      // 更新群信息
      Group obj = new Group();
      obj.setGroupId(group.getGroupId());
      obj.setGroupAdmin(targetId);
      this.groupService.updateGroupInfo(obj);
    } else {
      // 群聊等级 [0. 50人 1. 100人]
      if (group.getGroupGrade() == 1) {
        // 群主是会员 被转移目标也应该是会员 否则转移失败
        Result targetResult = this.userRemote.getBase(userId);
        Map<String, Object> targetInfo = targetResult.getCode().equals(0) ? JsonUtils.toObjectMap(targetResult.getData()) : null;
        Integer targetMember;
        if (targetInfo != null) {
          targetMember = (Integer) adminInfo.get("isMember");
        } else {
          return Result.error(ErrorCode.NETWORK_ERROR);
        }
        if (targetMember < 1) {
          return Result.error(ErrorCode.GROUP_ADMIN_LIMIT);
        } else {
          //  0 普通成员 1 管理员 2 群主
          GroupMember groupMember = new GroupMember();
          groupMember.setGroupId(groupId);
          groupMember.setUserId(targetId);
          // 转移群主身份
          groupMember.setFlagAuthority(2);
          this.groupMemberMapper.updateGroupUserInfo(groupMember);
          groupMember.setUserId(userId);
          // 降为普通成员
          groupMember.setFlagAuthority(0);
          this.groupMemberMapper.updateGroupUserInfo(groupMember);
          // 更新群信息
          Group obj = new Group();
          obj.setGroupId(group.getGroupId());
          obj.setGroupAdmin(targetId);
          this.groupService.updateGroupInfo(obj);
        }
      } else {
        //  0 普通成员 1 管理员 2 群主
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(groupId);
        groupMember.setUserId(targetId);
        // 转移群主身份
        groupMember.setFlagAuthority(2);
        this.groupMemberMapper.updateGroupUserInfo(groupMember);
        groupMember.setUserId(userId);
        // 降为普通成员
        groupMember.setFlagAuthority(0);
        this.groupMemberMapper.updateGroupUserInfo(groupMember);
        // 更新群信息
        Group obj = new Group();
        obj.setGroupId(group.getGroupId());
        obj.setGroupAdmin(targetId);
        this.groupService.updateGroupInfo(obj);
      }
    }
    return Result.success();
  }

  /**
   * TODO 更新图标.
   *
   * @param groupId 群聊ID
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  private void updateGroupNameAndGroupIcon(Long groupId) {
    try {
      Group obj = new Group();
      obj.setGroupId(groupId);
      List<Long> userIdList = this.groupMemberMapper.getShowUserIdList(groupId);
      Group group = this.groupService.getGroupInfo(groupId);
      Integer groupAuto = group.getGroupAuto();
      if (groupAuto == 0) {
        if (group.getGroupNum() <= 5) {
          String groupName = this.userRemote.getNickName(userIdList);
          if (groupName != null) {
            groupName = interceptGroupName(groupName);
            obj.setGroupName(groupName);
          }
        } else {
          List<Long> newUserInfoList = userIdList.subList(0, 5);
          String groupName = this.userRemote.getNickName(newUserInfoList);
          if (groupName != null) {
            groupName = interceptGroupName(groupName);
            obj.setGroupName(groupName);
          }
        }
      }
      if (group.getGroupNum() > 0) {
        List<Map<String, Object>> userInfoList = this.userRemote.getUserList(userIdList);
        if (userInfoList != null) {
          List<String> userIconUrl = Lists.newLinkedList();
          for (Map<String, Object> map : userInfoList) {
            userIconUrl.add(StringUtils.nvl(map.get("iconUrl")));
          }
          byte[] imageFile = ImageUtil.getCombinationOfHead(userIconUrl);
          InputStream inputStream = new ByteArrayInputStream(imageFile);
          String uuid = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
          MultipartFile file = new FeignMultipartFile("file", uuid + ".jpg", ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
          Result result = this.uploadRemote.uploadChatFile(file);
          PicInfo picInfo1 = result.getCode().equals(0) ? JsonUtils.toObjectPojo(result.getData(), PicInfo.class) : null;
          if (picInfo1 != null) {
            obj.setGroupIcon(picInfo1.getPicUrl());
          }
        }
      }
      if (StringUtils.isNotEmpty(obj.getGroupName()) || StringUtils.isNotEmpty(obj.getGroupIcon())) {
        this.groupService.updateGroupInfo(obj);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      log.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 删除成员.
   *
   * @param params 成员信息
   * @return 删除结果
   * @author wangcaiwen|1443710411@qq.com
   * @create 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public Result deleteGroupUser(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    long groupId = ((Number) params.get("groupId")).longValue();
    long userId = ((Number) params.get("userId")).longValue();
    if (groupId <= 0 || userId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    GroupMember groupMember = this.groupMemberMapper.getGroupUserInfo(groupId, userId);
    // 标记权限 [0普通 1管理 2房主]
    if (groupMember.getFlagAuthority() < 1) {
      return Result.error(ErrorCode.GROUP_CHAT_AUTHORITY_DELETE);
    }
    Group group = this.groupService.getGroupInfo(groupId);
    List<Long> userIdList = JsonUtils.toListType(params.get("userIds"), Long.class);
    if (CollectionUtils.isNotEmpty(userIdList)) {
      // 群组不可以删除自己
      if (group.getGroupAdmin() == userId && userIdList.contains(userId)) {
        return Result.error(ErrorCode.GROUP_CHAT_DELETE_ERROR);
      }
      List<Long> adminList = this.groupMemberMapper.getGroupUserAdminList(groupId);
      if (groupMember.getFlagAuthority() == 1) {
        userIdList = userIdList.stream()
            .filter(aLong -> !adminList.contains(aLong))
            .collect(Collectors.toCollection(Lists::newLinkedList));
      }
      this.groupMemberMapper.deleteGroupUserList(groupId, userIdList);
      // 删除语音操作记录
      this.groupMessageVoiceService.deleteGroupVoiceMessageByList(groupId, userIdList);
      // 删除其他记录
      this.groupMessageDeleteService.deleteGroupMessageList(groupId, userIdList);
      Map<String, Object> result;
      // 生成记录
      String leaveMessage = this.userRemote.getNickName(userIdList);
      if (StringUtils.isNotEmpty(leaveMessage)) {
        result = Maps.newHashMap();
        result.put("groupId", groupId);
        result.put("message", leaveMessage);
        result.put("sort", 2);
        this.groupMessageService.newGroupMessageBySort(result);
      }
      updateGroupNameAndGroupIcon(groupId);
      // 刷新缓存
      this.redisUtils.del(ChatRedisKey.KEY_CHAT_GROUP_MEMBER + groupId);
      Group groupInfo = this.groupService.getGroupInfo(groupId);
      List<Long> groupMemberList = getGroupUserIdList(groupId);
      D10001.GroupMember.Builder groupBuilder = D10001.GroupMember.newBuilder();
      groupBuilder.setGroupIcon(groupInfo.getGroupIcon());
      groupBuilder.setGroupName(groupInfo.getGroupName());
      groupBuilder.addAllMemberId(groupMemberList);
      this.redisUtils.setByte(ChatRedisKey.KEY_CHAT_GROUP_MEMBER + groupId, groupBuilder.build().toByteArray());
      return Result.success();
    } else {
      return Result.error(ErrorCode.GROUP_CHAT_DELETE_USER);
    }
  }

  /**
   * TODO 更新状态.
   *
   * @param params 状态信息
   * @return 更新结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public Result updateGroupChat(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error();
    }
    Long groupId = ((Number) params.get("groupId")).longValue();
    Long userId = ((Number) params.get("userId")).longValue();
    if (groupId <= 0 || userId <= 0) {
      return Result.error();
    }
    GroupMember groupMember = new GroupMember();
    groupMember.setGroupId(groupId);
    groupMember.setUserId(userId);
    groupMember.setChatStatus((Integer) params.get("chatStatus"));
    if (groupMember.getChatStatus() == 1) {
      groupMember.setUnreadNum(0);
    }
    this.groupMemberMapper.updateGroupUserInfo(groupMember);
    return Result.success();
  }

  /**
   * TODO 聊天状态.
   *
   * @param groupId 群聊ID
   * @param userId 用户ID
   * @return 聊天状态
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public Result getUserGroupStatus(Long groupId, Long userId) {
    if (groupId == null || groupId <= 0) {
      return Result.error();
    }
    if (userId == null || userId <= 0) {
      return Result.error();
    }
    GroupMember groupMember = this.groupMemberMapper.getGroupUserInfo(groupId, userId);
    if (groupMember != null) {
      Integer chatStatus = groupMember.getChatStatus();
      return Result.success(chatStatus);
    }
    return Result.error();
  }

  /**
   * TODO 未读数量.
   *
   * @param userId 用户ID
   * @return java.lang.Integer
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/19 9:04
   * @update 2020/8/19 9:04
   */
  @Override
  public Integer getUserUnreadNum(Long userId) {
    Integer unRead = this.groupMemberMapper.getUserUnreadNum(userId);
    if (unRead != null) {
      return unRead;
    }
    return 0;
  }

  /**
   * TODO 更新未读.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/7/28 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public Result updateUserGroupUnreadNum(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error();
    }
    Long groupId = ((Number) params.get("groupId")).longValue();
    if (groupId <= 0) {
      return Result.error();
    }
    List<Long> userIdList = JsonUtils.toListType(params.get("userList"), Long.class);
    if (CollectionUtils.isNotEmpty(userIdList)) {
      this.groupMemberMapper.updateUserGroupUnReadNumAdd(groupId, userIdList);
    }
    return Result.success();
  }

  /**
   * TODO 更新消息.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public Result updateGroupLastMessageMap(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error();
    }
    GroupMember groupMember = new GroupMember();
    groupMember.setGroupId(((Number) params.get("groupId")).longValue());
    groupMember.setMessageId(((Number) params.get("recordId")).longValue());
    DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime ldt = LocalDateTime.parse(StringUtils.nvl(params.get("sendTime")), df);
    groupMember.setMessageTime(ldt);
    this.groupMemberMapper.updateGroupLastMessage(groupMember);
    return Result.success();
  }

  /**
   * TODO 末尾信息.
   *
   * @param groupMember 末尾信息
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public void updateGroupLastMessage(GroupMember groupMember) {
    this.groupMemberMapper.updateGroupLastMessage(groupMember);
  }

  /**
   * TODO 清空消息.
   *
   * @param groupId 群聊ID
   * @param userId 用户ID
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public void emptyResidualMessage(Long groupId, Long userId) {
    this.groupMemberMapper.emptyResidualMessage(groupId, userId);
  }

  /**
   * TODO 通知状态.
   *
   * @param groupId 群聊ID
   * @param userId 用户ID
   * @return 通知状态
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public Result getUserGroupNoticeStatus(Long groupId, Long userId) {
    if (groupId == null || groupId <= 0) {
      return Result.error();
    }
    if (userId == null || userId <= 0) {
      return Result.error();
    }
    GroupMember groupMember = this.groupMemberMapper.getGroupUserInfo(groupId, userId);
    if (groupMember != null) {
      Integer noticeStatus = groupMember.getNotDisturb();
      return Result.success(noticeStatus);
    }
    return Result.error();
  }

  /**
   * TODO 更新设置.
   *
   * @param params 更新信息
   * @return 更新结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public Result updUserGroupSetting(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error();
    }
    long groupId = ((Number) params.get("groupId")).longValue();
    long userId = ((Number) params.get("userId")).longValue();
    if (groupId <= 0 || userId <= 0) {
      return Result.error();
    }
    GroupMember groupMember = new GroupMember();
    groupMember.setGroupId(groupId);
    groupMember.setUserId(userId);
    if (params.containsKey(USER_FLAG_TOP)) {
      groupMember.setFlagTop((Integer) params.get(USER_FLAG_TOP));
    }
    if (params.containsKey(USER_FLAG_DELETE)) {
      groupMember.setFlagDelete((Integer) params.get(USER_FLAG_DELETE));
    }
    if (params.containsKey(USER_NOT_DISTURB)) {
      groupMember.setNotDisturb((Integer) params.get(USER_NOT_DISTURB));
    }
    this.groupMemberMapper.updateGroupUserInfo(groupMember);
    return Result.success();
  }

  /**
   * TODO 管理列表.
   *
   * @param params 群聊ID
   * @return 成员列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public Result getGroupAdminList(Map<String, Object> params) {
    Map<String, Object> result = Maps.newHashMap();
    if (params == null || params.isEmpty()) {
      return Result.error();
    }
    long groupId = ((Number) params.get("groupId")).longValue();
    if (groupId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    Group group = this.groupService.getGroupInfo(groupId);
    if (group != null) {
      List<Long> adminList = this.groupMemberMapper.getGroupUserAdminList(groupId);
      if (group.getGroupGrade() == 0) {
        result.put("adminNumLimit", 3);
      } else {
        result.put("adminNumLimit", 6);
      }
      List<Map<String, Object>> groupUserList = this.userRemote.getUserList(adminList);
      if (groupUserList != null) {
        for (Map<String, Object> objectMap : groupUserList) {
          objectMap.put("thumbIconUrl", objectMap.get("iconUrl"));
          objectMap.remove("iconUrl");
        }
        result.put("adminList", groupUserList);
      }
    }
    return Result.success(result);
  }

  /**
   * TODO 成员列表.
   *
   * @param params 群聊ID
   * @return 成员列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public Result getGroupUserList(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error();
    }
    long groupId = ((Number) params.get("groupId")).longValue();
    if (groupId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    List<Map<String, Object>> userList = this.groupMemberMapper.getGroupUserList(groupId);
    List<Long> userIdList = Lists.newLinkedList();
    Map<Long, Object> powerMap = Maps.newHashMap();
    if (userList != null) {
      for (Map<String, Object> map : userList) {
        Long userId = ((Number) map.get("userId")).longValue();
        powerMap.put(userId, map.get("flagAuthority"));
        userIdList.add(userId);
      }
    }
    List<Map<String, Object>> groupUserList = this.userRemote.getUserList(userIdList);
    if (groupUserList != null) {
      for (Map<String, Object> stringObjectMap : groupUserList) {
        stringObjectMap.put("thumbIconUrl", stringObjectMap.get("iconUrl"));
        stringObjectMap.remove("iconUrl");
        Long userId = ((Number) stringObjectMap.get("userId")).longValue();
        stringObjectMap.put("flagAuthority", powerMap.get(userId));
      }
      groupUserList.sort((o1, o2) -> {
        Integer flag1 = (Integer) o1.get("flagAuthority");
        Integer flag2 = (Integer) o2.get("flagAuthority");
        return flag2.compareTo(flag1);
      });
    }
    return Result.success(groupUserList);
  }

  /**
   * TODO 成员列表.
   *
   * @param groupId 群聊ID
   * @return ID列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public Result getGroupMemberIdList(Long groupId) {
    return Result.success(this.groupMemberMapper.getGroupUserAllList(groupId));
  }

  /**
   * TODO 成员列表.
   *
   * @param groupId 群聊ID
   * @return ID列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public List<Long> getGroupUserIdList(Long groupId) {
    return this.groupMemberMapper.getGroupUserAllList(groupId);
  }


  /**
   * TODO 设置管理.
   *
   * @param params 设置信息
   * @return 设置结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/7/28 10:29
   * @update 2020/8/11 18:00
   */
  @Override
  public Result updateGroupAdmin(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    Long groupId = ((Number) params.get("groupId")).longValue();
    Long userId = ((Number) params.get("userId")).longValue();
    Long targetId = ((Number) params.get("targetId")).longValue();
    if (groupId <= 0 || userId <= 0 || targetId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    Group group = this.groupService.getGroupInfo(groupId);
    if (!group.getGroupAdmin().equals(userId)) {
      return Result.error(ErrorCode.GROUP_CHAT_AUTHORITY_SET);
    }
    GroupMember groupMember = new GroupMember();
    groupMember.setGroupId(groupId);
    groupMember.setUserId(targetId);
    //  type 1 设置管理员 2 撤销管理员
    Integer type = (Integer) params.get("type");
    if (type == 1) {
      Integer adminNum = this.groupMemberMapper.getGroupChatAdminNum(groupId);
      // 50人 最多3个管理员
      if (group.getGroupGrade() == 0) {
        if (adminNum == GROUP_ADMIN_MAX_1) {
          return Result.error(ErrorCode.GROUP_CHAT_AUTHORITY_NUM_1);
        }
      }
      // 100人 最多6个管理员
      if (group.getGroupGrade() == 1) {
        if (adminNum == GROUP_ADMIN_MAX_2) {
          return Result.error(ErrorCode.GROUP_CHAT_AUTHORITY_NUM_2);
        }
      }
      //  power 0 普通成员 1 管理员 2 群主
      groupMember.setFlagAuthority(1);
      this.groupMemberMapper.updateGroupUserInfo(groupMember);
    } else if (type == FLAG_INDEX) {
      groupMember.setFlagAuthority(0);
      this.groupMemberMapper.updateGroupUserInfo(groupMember);
    }
    return Result.success();
  }

}
