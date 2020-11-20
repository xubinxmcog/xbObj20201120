package com.enuos.live.service.impl;

import com.enuos.live.component.FeignMultipartFile;
import com.enuos.live.constants.ChatRedisKey;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.GroupMapper;
import com.enuos.live.pojo.Group;
import com.enuos.live.pojo.GroupMember;
import com.enuos.live.pojo.PicInfo;
import com.enuos.live.proto.d10001msg.D10001;
import com.enuos.live.rest.UploadRemote;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.service.*;
import com.enuos.live.utils.IDUtils;
import com.enuos.live.utils.ImageUtil;
import com.enuos.live.utils.JsonUtils;
import com.enuos.live.utils.RedisUtils;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * @author WangCaiWen Created on 2020/4/15 18:08
 */
@Slf4j
@Service("groupService")
public class GroupServiceImpl implements GroupService {

  @Resource
  private GroupMapper groupMapper;
  @Resource
  private GroupMemberService groupMemberService;
  @Resource
  private GroupMessageService groupMessageService;
  @Resource
  private GroupMessageDeleteService groupMessageDeleteService;
  @Resource
  private NoticeMemberService noticeMemberService;
  @Resource
  private UserRemote userRemote;
  @Resource
  private UploadRemote uploadRemote;

  /**
   * Redis工具类.
   */
  @Resource
  private RedisUtils redisUtils;

  private static final String GROUP_NAME = "groupName";
  private static final String GROUP_NOTICE = "groupNotice";
  private static final String GROUP_INTRO = "groupIntro";
  private static final String GROUP_ICON = "groupIcon";
  private static final int FLAG_GROUP_NUM_1 = 50;
  private static final int FLAG_GROUP_NUM_2 = 100;

  @Override
  public Result getUserGroupSetting(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    long groupId = ((Number) params.get("groupId")).longValue();
    long userId = ((Number) params.get("userId")).longValue();
    if (groupId <= 0 || userId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    Map<String, Object> tempResult = new HashMap<>(16);
    tempResult.put("groupId", groupId);
    tempResult.put("userId", userId);
    // 获取群设置信息
    Map<String, Object> newResult = this.groupMapper.getOpenGroupInfo(tempResult);
    if (newResult != null) {
      // 获取9名群内用户ID
      List<Long> showUserIdList = this.groupMemberService.getShowUserIdList(groupId);
      if (showUserIdList != null) {
        //  远程调用用户数据
        List<Map<String, Object>> groupUserList = this.userRemote.getUserList(showUserIdList);
        if (groupUserList != null) {
          for (Map<String, Object> objectMap : groupUserList) {
            objectMap.put("thumbIconUrl", StringUtils.nvl(objectMap.get("iconUrl")));
            objectMap.remove("iconUrl");
          }
          groupUserList.sort((o1, o2) -> {
            long flag1 = ((Number) o1.get("userId")).longValue();
            long flag2 = ((Number) o2.get("userId")).longValue();
            int io1 = showUserIdList.indexOf(flag1);
            int io2 = showUserIdList.indexOf(flag2);
            if(io1 != -1){
              io1 = groupUserList.size() - io1;
            }
            if(io2 != -1){
              io2 = groupUserList.size() - io2;
            }
            return io2- io1;
          });
          newResult.put("groupUser", groupUserList);
        } else {
          return Result.error(ErrorCode.NETWORK_ERROR);
        }
      }
      // 更新群头像
      if (StringUtils.isEmpty(StringUtils.nvl(newResult.get(GROUP_ICON)))) {
        insGroupIcon(groupId);
      }
      newResult.remove(GROUP_ICON);
      Result adminResult = this.userRemote.getBase(userId);
      Map<String, Object> adminInfo = adminResult.getCode().equals(0) ? JsonUtils.toObjectMap(adminResult.getData()) : null;
      if (adminInfo != null) {
        //是否会员[-1 过期会员 0 否 1 是]
        Integer isMember = (Integer) adminInfo.get("isMember");
        if (isMember < 1) {
          newResult.put("isMember", 0);
        } else {
          newResult.put("isMember", 1);
        }
      } else {
        return Result.error(ErrorCode.NETWORK_ERROR);
      }
    }
    return Result.success(newResult);
  }

  @Override
  public Group getGroupInfo(Long groupId) {
    return this.groupMapper.getGroupInfo(groupId);
  }

  @Override
  public Result newGroupChat(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    List<Long> userIdList = JsonUtils.toListType(params.get("userIds"), Long.class);
    if (CollectionUtils.isNotEmpty(userIdList)) {
      int userIdSize = userIdList.size();
      if (userIdSize + 1 > FLAG_GROUP_NUM_1) {
        return Result.error(ErrorCode.GROUP_CHAT_INVITE_NUM_MAX);
      }
      long groupId = IDUtils.randomEightId();
      Group group = this.groupMapper.getGroupInfo(groupId);
      while (Objects.nonNull(group)) {
        groupId = IDUtils.randomEightId();
        group = this.groupMapper.getGroupInfo(groupId);
      }
      long groupAdmin = ((Number) params.get("sponsorId")).longValue();
      if (groupId <= 0 || groupAdmin <= 0) {
        return Result.error(ErrorCode.CHAT_PARAM_ERROR);
      }
      List<Long> groupMemberList = Lists.newLinkedList();
      if (userIdSize < 3) {
        groupMemberList.add(groupAdmin);
        groupMemberList.addAll(userIdList);
      } else {
        groupMemberList.add(groupAdmin);
        groupMemberList.add(userIdList.get(0));
        groupMemberList.add(userIdList.get(1));
      }
      String groupName = this.userRemote.getNickName(groupMemberList);
      Group groups = new Group();
      groups.setGroupId(groupId);
      groups.setGroupAdmin(groupAdmin);
      groupName = interceptGroupName(groupName);
      if (Objects.nonNull(groupName)) {
        groups.setGroupName(groupName);
      }
      groups.setGroupGrade(0);
      this.groupMapper.newGroupChat(groups);
      Map<String, Object> result = Maps.newHashMap();
      // admin用户 flagAuthority 标记权限 [0普通 1管理 2房主]
      List<Map<String, Object>> groupUserList = Lists.newLinkedList();
      result.put("groupId", groupId);
      result.put("userId", groupAdmin);
      result.put("flagAuthority", 2);
      result.put("chatStatus", 1);
      result.put("unreadNum", 0);
      groupUserList.add(result);
      // 邀请用户
      for (Long aLong : userIdList) {
        result = Maps.newHashMap();
        result.put("groupId", groupId);
        result.put("userId", aLong);
        result.put("flagAuthority", 0);
        result.put("chatStatus", 0);
        result.put("unreadNum", 1);
        groupUserList.add(result);
      }
      this.groupMemberService.saveGroupUserList(groupUserList);
      this.groupMessageDeleteService.insertMessage(groupUserList);
      // 生成加入记录
      // 往列表中加入adminId
      userIdList.add(groupAdmin);
      String joinMessage = this.userRemote.getNickName(userIdList);
      if (StringUtils.isNotEmpty(joinMessage)) {
        result = Maps.newHashMap();
        result.put("groupId", groupId);
        result.put("message", joinMessage);
        result.put("sort", 1);
        this.groupMessageService.newGroupMessageBySort(result);
      }
      // 生成群聊头像
      insGroupIcon(groupId);
      // 添加缓存
      Group groupInfo = this.groupMapper.getGroupInfo(groupId);
      List<Long> groupMember = this.groupMemberService.getGroupUserIdList(groupId);
      D10001.GroupMember.Builder groupBuilder = D10001.GroupMember.newBuilder();
      groupBuilder.setGroupIcon(groupInfo.getGroupIcon());
      groupBuilder.setGroupName(groupInfo.getGroupName());
      groupBuilder.addAllMemberId(groupMember);
      this.redisUtils.setByte(ChatRedisKey.KEY_CHAT_GROUP_MEMBER + groupId, groupBuilder.build().toByteArray());
      return Result.success(groupId);
    }
    return Result.error(ErrorCode. GROUP_CHAT_INVITE_NUM_NULL);
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

  private void insGroupIcon(Long groupId) {
    try {
      List<String> iconList = this.groupMemberService.getGroupIcon(groupId);
      // 生成群聊头像
      if (iconList != null) {
        byte[] imageFile = ImageUtil.getCombinationOfHead(iconList);
        InputStream inputStream = new ByteArrayInputStream(imageFile);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        MultipartFile file = new FeignMultipartFile("file", uuid + ".jpg", ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
        Result result = this.uploadRemote.uploadChatFile(file);
        PicInfo picInfo1 = result.getCode().equals(0) ? JsonUtils.toObjectPojo(result.getData(), PicInfo.class) : null;
        if (picInfo1 != null) {
          Group group = new Group();
          group.setGroupId(groupId);
          group.setGroupIcon(picInfo1.getPicUrl());
          this.groupMapper.updateGroupInfo(group);
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      log.error(ExceptionUtil.getStackTrace(e));
    }
  }

  @Override
  public Result inviteJoinGroup(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    long groupId = ((Number) params.get("groupId")).longValue();
    long userId = ((Number) params.get("userId")).longValue();
    if (groupId <= 0 || userId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    List<Long> inviteList = JsonUtils.toListType(params.get("userIds"), Long.class);
    if (CollectionUtils.isNotEmpty(inviteList)) {
      List<Long> groupMemberList = this.groupMemberService.getGroupUserIdList(groupId);
      // 筛选重复人员
      inviteList = inviteList.stream()
          .filter(aLong -> !groupMemberList.contains(aLong))
          .collect(Collectors.toCollection(Lists::newLinkedList));
      int inviteSize = inviteList.size();
      if (inviteSize > 0) {
        int groupSize = groupMemberList.size();
        // 检查群内人数、群人数限制
        Group group = this.groupMapper.getGroupInfo(groupId);
        // 群聊等级 [0. 50人 1. 100人]
        if (group.getGroupGrade() == 0) {
          if (groupSize < FLAG_GROUP_NUM_1) {
            int residue = FLAG_GROUP_NUM_1 - groupSize;
            if (inviteSize > residue) {
              return Result.error(-1, "邀请失败!当前群剩余" + residue + "空位，可前往群管理申请升级群人数");
            }
          } else if (groupSize == FLAG_GROUP_NUM_1){
            return Result.error(ErrorCode.GROUP_CHAT_NUM_MAX_2);
          } else {
            return Result.error(ErrorCode.GROUP_CHAT_NUM_MAX_2);
          }
        } else {
          if (group.getGroupNum() < FLAG_GROUP_NUM_2) {
            int residue = FLAG_GROUP_NUM_2 - groupSize;
            if (inviteSize > residue) {
              return Result.error(-1, "邀请失败!当前群剩余" + residue + "空位，可酌情清理群人员");
            }
          } else {
            return Result.error(ErrorCode.GROUP_CHAT_NUM_MAX_1);
          }
        }
        // 1）加入邀请用户
        List<Map<String, Object>> groupUserList = Lists.newLinkedList();
        Map<String, Object> result;
        for (Long aLong : inviteList) {
          result = Maps.newHashMap();
          result.put("groupId", groupId);
          result.put("userId", aLong);
          result.put("flagAuthority", 0);
          result.put("chatStatus", 0);
          result.put("unreadNum", 1);
          groupUserList.add(result);
        }
        this.groupMemberService.saveGroupUserList(groupUserList);
        this.groupMessageDeleteService.insertMessage(groupUserList);
        // 3）生成加入记录
        String joinMessage = this.userRemote.getNickName(inviteList);
        if (StringUtils.isNotEmpty(joinMessage)) {
          result = Maps.newHashMap();
          result.put("groupId", groupId);
          result.put("message", joinMessage);
          result.put("sort", 1);
          this.groupMessageService.newGroupMessageBySort(result);
        }
        // 刷新图标
        this.groupMemberService.updateGroupNameOrIcon(groupId);
        // 刷新缓存
        this.redisUtils.del(ChatRedisKey.KEY_CHAT_GROUP_MEMBER + groupId);
        Group groupInfo = this.groupMapper.getGroupInfo(groupId);
        List<Long> groupMember = this.groupMemberService.getGroupUserIdList(groupId);
        D10001.GroupMember.Builder groupBuilder = D10001.GroupMember.newBuilder();
        groupBuilder.setGroupIcon(groupInfo.getGroupIcon());
        groupBuilder.setGroupName(groupInfo.getGroupName());
        groupBuilder.addAllMemberId(groupMember);
        this.redisUtils.setByte(ChatRedisKey.KEY_CHAT_GROUP_MEMBER + groupId, groupBuilder.build().toByteArray());
        return Result.success();
      }
    }
    return Result.error(ErrorCode.GROUP_CHAT_INVITE_NUM_NULL);
  }

  @Override
  public void updateGroupInfo(Group group) {
    this.groupMapper.updateGroupInfo(group);
  }

  @Override
  public Result getGroupInfoMap(Long groupId) {
    Map<String, Object> result = new HashMap<>(16);
    if (groupId == null || groupId <= 0) {
      return Result.error();
    }
    Group group = this.groupMapper.getGroupInfo(groupId);
    if (group != null) {
      Result adminResult = this.userRemote.getBase(group.getGroupAdmin());
      Map<String, Object> adminInfo = adminResult.getCode().equals(0) ? JsonUtils.toObjectMap(adminResult.getData()) : null;
      if (adminInfo != null) {
        //是否会员[-1 过期会员 0 否 1 是]
        Integer isMember = (Integer) adminInfo.get("isMember");
        // 群聊等级 [0. 50人 1. 100人]
        if (group.getGroupGrade() == 1 && isMember < 1) {
          this.groupMapper.lowerGroupGraded(group.getGroupAdmin());
        }
      }
      result.put("groupName", group.getGroupName());
      result.put("groupIcon", group.getGroupIcon());
      return Result.success(result);
    }
    return Result.error();
  }

  @Override
  public Result getUserTheGroupListByUserId(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    long userId = ((Number) params.get("userId")).longValue();
    if (userId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    List<Map<String, Object>> groupList = this.groupMapper.getGroupListByUserId(userId);
    return Result.success(groupList);
  }

  @Override
  public Result searchGroupByName(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    List<Map<String, Object>> groupList = this.groupMapper.searchGroupByName(params);
    return Result.success(groupList);
  }

  @Override
  public Result updGroupNameOrNoticeAndIntro(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    long groupId = ((Number) params.get("groupId")).longValue();
    long userId = ((Number) params.get("userId")).longValue();
    if (groupId <= 0 || userId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    GroupMember groupMember = this.groupMemberService.getGroupUserInfo(groupId, userId);
    if (groupMember != null) {
      if (groupMember.getFlagAuthority() == 0) {
        return Result.error(ErrorCode.GROUP_CHAT_AUTHORITY);
      }
      Group obj = new Group();
      obj.setGroupId(groupId);
      if (params.containsKey(GROUP_NAME)) {
        String groupName = StringUtils.nvl(params.get(GROUP_NAME));
        if (StringUtils.isNotEmpty(groupName)) {
          int nameLength = groupName.length();
          if (nameLength <= 20) {
            obj.setGroupName(StringUtils.nvl(params.get(GROUP_NAME)));
            obj.setGroupAuto(1);
          } else {
            return Result.error(ErrorCode.GROUP_INFO_NAME_LENGTH);
          }
        } else {
          return Result.error(ErrorCode.GROUP_INFO_NAME);
        }
      }
      if (params.containsKey(GROUP_NOTICE)) {
        String groupNotice = StringUtils.nvl(params.get(GROUP_NOTICE));
        int noticeLength = groupNotice.length();
        if (noticeLength <= 100) {
          obj.setGroupNotice(groupNotice);
        } else {
          return Result.error(ErrorCode.GROUP_INFO_NOTICE_LENGTH);
        }
      }
      if (params.containsKey(GROUP_INTRO)) {
        String groupIntro = StringUtils.nvl(params.get(GROUP_INTRO));
        int introLength = groupIntro.length();
        if (introLength <= 100) {
          obj.setGroupIntro(groupIntro);
        } else {
          return Result.error(ErrorCode.GROUP_INFO_INTRO_LENGTH);
        }
      }
      this.groupMapper.updateGroupInfo(obj);
      return Result.success();
    } else {
      return Result.error(ErrorCode.NETWORK_ERROR);
    }
  }

  /**
   * TODO 提升等级.
   *
   * @param params 用户信息
   * @return 提升结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/14 9:49
   * @update 2020/8/14 9:49
   */
  @Override
  public Result upgradeGroupGrade(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.CHAT_PARAM_NULL);
    }
    long groupId = ((Number) params.get("groupId")).longValue();
    long userId = ((Number) params.get("userId")).longValue();
    if (groupId <= 0 || userId <= 0) {
      return Result.error(ErrorCode.CHAT_PARAM_ERROR);
    }
    Result adminResult = this.userRemote.getBase(userId);
    Map<String, Object> adminInfo = adminResult.getCode().equals(0) ? JsonUtils.toObjectMap(adminResult.getData()) : null;
    Integer adminMember;
    if (adminInfo != null) {
      adminMember = (Integer) adminInfo.get("isMember");
    } else {
      return Result.error(ErrorCode.NETWORK_ERROR);
    }
    //是否会员[-1 过期会员 0 否 1 是]
    if (adminMember == 1) {
      Group group = new Group();
      // 群聊等级 [0. 50人 1. 100人]
      group.setGroupGrade(1);
      group.setGroupId(groupId);
      this.groupMapper.updateGroupInfo(group);
    } else {
      return Result.error(ErrorCode.GROUP_UPGRADE_FAILED);
    }
    return Result.success();
  }

  @Override
  public void deleteGroupAdmin(Long groupId) {
    this.groupMapper.deleteGroupAdmin(groupId);
  }

  @Override
  public void dissolveGroup(Long userId) {
    // 获取用户自己创建的群聊列表
    List<Long> groupList = this.groupMapper.getUserCreateGroupIdList(userId);
    if (groupList != null && groupList.size() > 0) {
      Map<String, Object> tempResult;
      for (Long groupId : groupList) {
        tempResult = new HashMap<>(16);
        Group group = this.groupMapper.getGroupInfo(groupId);
        List<Long> groupUserList = this.groupMemberService.getGroupUserIdList(groupId);
        for (Long memberId : groupUserList) {
          this.redisUtils.del(ChatRedisKey.KEY_CHAT_GROUP_LOGIN + memberId);
        }
        this.redisUtils.del(ChatRedisKey.KEY_CHAT_GROUP_MEMBER + groupId);
        // 开始解散
        this.groupMemberService.dissolveGroupChat(userId);
        groupUserList.removeIf(along -> along.equals(userId));
        if (groupUserList.size() > 0) {
          tempResult.put("groupName", group.getGroupName());
          tempResult.put("userIds", groupUserList);
          this.noticeMemberService.sendGroupDissolveNotice(tempResult);
        }
      }
    }
    // 离开加入的所有群
    this.groupMemberService.leaveJoinGroupChat(userId);
  }

}
