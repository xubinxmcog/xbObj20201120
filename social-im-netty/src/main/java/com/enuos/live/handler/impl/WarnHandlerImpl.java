package com.enuos.live.handler.impl;

import cn.hutool.core.map.MapUtil;
import com.enuos.live.action.ChannelSet;
import com.enuos.live.action.ChatActionSet;
import com.enuos.live.action.WarnActionSet;
import com.enuos.live.core.NettyCtxGroup;
import com.enuos.live.core.Packet;
import com.enuos.live.handler.WarnHandler;
import com.enuos.live.manager.ChannelManager;
import com.enuos.live.proto.c10001msg.C10001;
import com.enuos.live.proto.c10002msg.C10002;
import com.enuos.live.proto.c10004msg.C10004;
import com.enuos.live.rest.ChatRemote;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.util.JsonUtils;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * TODO 软件通知.
 *
 * @author WangCaiWen
 * @version 1.0
 * @since 2020-04-09 11:01:55
 */
@Slf4j
@Component
public class WarnHandlerImpl implements WarnHandler {

  private static final int INTERACT_FLAG_2 = 2;
  private static final int INTERACT_FLAG_5 = 5;
  private static final int INTERACT_FLAG_6 = 6;
  private static final String REMARK = "remark";

  /**
   * FEIGN
   */
  @Resource
  private UserRemote userRemote;
  @Resource
  private ChatRemote chatRemote;

  /**
   * TODO 聊天提醒.
   *
   * @param params 发送参数
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public void sendAloneNoticeMessage(Map<String, Object> params) {
    try {
      C10001.C100012s2c.Builder builder = C10001.C100012s2c.newBuilder();
      long userId = ((Number) params.get("userId")).longValue();
      long targetId = ((Number) params.get("targetId")).longValue();
      Integer messageType = (Integer) params.get("messageType");
      // 是否开启免打扰
      Result result = this.chatRemote.getUserNoticeStatus(targetId, userId);
      Integer noticeStatus = result.getCode().equals(0) ? (Integer) result.getData() : null;
      if (noticeStatus != null) {
        if (noticeStatus == 0) {
          Map<String, Object> newResult = this.userRemote.getUserBase(targetId, userId);
          if (newResult != null) {
            builder.addThumbIconURL(StringUtils.nvl(newResult.get("iconUrl")));
            builder.setAttachId(userId);
            builder.setSort(1);
            builder.setMessage(StringUtils.nvl(params.get("message")));
            if (messageType == 0) {
              builder.setMessage(StringUtils.nvl(params.get("message")));
            } else {
              String message;
              switch (messageType) {
                case 1:
                  message = "[表情]";
                  break;
                case 2:
                  message = "[图片]";
                  break;
                case 3:
                  message = "[语音]";
                  break;
                case 4:
                  message = "[视频]";
                  break;
                default:
                  message = "[游戏邀请]";
                  break;
              }
              builder.setMessage(message);
            }
            builder.setMessageType(messageType);
            String remark = StringUtils.nvl(newResult.get("remark"));
            if (remark.length() > 0) {
              builder.setAlias(remark);
            } else {
              builder.setAlias(StringUtils.nvl(newResult.get("nickName")));
            }
            ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_NOTICE,
                WarnActionSet.CHAT_NOTICE, builder.build().toByteArray()), targetId);
          }
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      log.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 聊天提醒.
   *
   * @param params 发送参数
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public void sendGroupNoticeMessage(Map<String, Object> params) {
    try {
      C10001.C100012s2c.Builder builder = C10001.C100012s2c.newBuilder();
      long groupId = ((Number) params.get("groupId")).longValue();
      long userId = ((Number) params.get("userId")).longValue();
      Integer messageType = (Integer) params.get("messageType");
      Result result = this.chatRemote.getUserGroupNoticeStatus(groupId, userId);
      Integer noticeStatus = result.getCode().equals(0) ? (Integer) result.getData() : null;
      if (noticeStatus != null) {
        // 是否开启免打扰
        if (noticeStatus == 0) {
          builder.setAlias(StringUtils.nvl(params.get("groupName")));
          builder.addThumbIconURL(StringUtils.nvl(params.get("groupIcon")));
          builder.setAttachId(groupId);
          builder.setSort(2);
          if (messageType == 0) {
            builder.setMessage(StringUtils.nvl(params.get("sendName")) + ": " + StringUtils.nvl(params.get("message")));
          } else {
            String message;
            switch (messageType) {
              case 1:
                message = "[表情]";
                break;
              case 2:
                message = "[图片]";
                break;
              case 3:
                message = "[语音]";
                break;
              default:
                message = "[视频]";
                break;
            }
            builder.setMessage(StringUtils.nvl(params.get("sendName")) + ": " + message);
          }
          builder.setMessageType(messageType);
          ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_NOTICE,
              WarnActionSet.CHAT_NOTICE, builder.build().toByteArray()), userId);
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      log.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 互动通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result interactNotice(Map<String, Object> params) {
    C10001.C100011s2c.Builder builder = C10001.C100011s2c.newBuilder();
    String sponsorName = StringUtils.nvl(params.get("sponsorName"));
    String sponsorIcon = StringUtils.nvl(params.get("sponsorIcon"));
    // 通知来源 [0 关注用户 1 评论动态 2 回复评论 3 转发动态 4 点赞动态 5 点赞评论 6 @xxx用户 ]
    Long sponsorId = ((Number) params.get("sponsorId")).longValue();
    Integer source = (Integer) params.get("source");
    builder.setThumbIconURL(sponsorIcon);
    // 0 关注用户
    if (source == 0) {
      builder.setNoticeSource(source);
    } else {
      // 2 回复评论 || 5 点赞评论
      if (source == INTERACT_FLAG_2 || source == INTERACT_FLAG_5) {
        builder.setNoticeSource(source);
        builder.setPostId((Integer) params.get("storyId"));
        builder.setAttachId((Integer) params.get("attachId"));
      } else {
        builder.setNoticeSource(source);
        builder.setPostId((Integer) params.get("storyId"));
      }
    }
    // 6 @xxx用户
    if (source == INTERACT_FLAG_6) {
      List<Long> userIdList = JsonUtils.toListType(params.get("userIds"), Long.class);
      userIdList.forEach(userId -> {
        Map<String, Object> userInfo = this.userRemote.getUserBase(userId, sponsorId);
        if (userInfo != null) {
          if (userInfo.containsKey(REMARK)) {
            String remark = StringUtils.nvl(userInfo.get(REMARK));
            if (remark.length() > 0) {
              builder.setAlias(remark);
            } else {
              builder.setAlias((sponsorName == null) ? "" : sponsorName);
            }
          } else {
            builder.setAlias((sponsorName == null) ? "" : sponsorName);
          }
        } else {
          builder.setAlias((sponsorName == null) ? "" : sponsorName);
        }
        ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_NOTICE,
            WarnActionSet.INTERACT_NOTICE, builder.build().toByteArray()), userId);
      });
    } else {
      Long receiverId = ((Number) params.get("receiverId")).longValue();
      Map<String, Object> userInfo = this.userRemote.getUserBase(receiverId, sponsorId);
      if (userInfo != null) {
        if (userInfo.containsKey(REMARK)) {
          String remark = StringUtils.nvl(userInfo.get(REMARK));
          if (remark.length() > 0) {
            builder.setAlias(remark);
          } else {
            builder.setAlias((sponsorName == null) ? "" : sponsorName);
          }
        } else {
          builder.setAlias((sponsorName == null) ? "" : sponsorName);
        }
      } else {
        builder.setAlias((sponsorName == null) ? "" : sponsorName);
      }
      ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_NOTICE,
          WarnActionSet.INTERACT_NOTICE, builder.build().toByteArray()), receiverId);
    }
    return Result.success();
  }

  /**
   * TODO 软件通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result softwareNotice(Map<String, Object> params) {
    C10001.C100013s2c.Builder builder = C10001.C100013s2c.newBuilder();
    // 通知类型 [0全局推送 1会员推送 2个人推送]
    Integer noticeType = (Integer) params.get("noticeType");
    builder.setNoticeType(noticeType);
    switch (noticeType) {
      // 当前在线的所有用户
      case 0:
        builder.setTitle(StringUtils.nvl(params.get("contentTitle")));
        builder.setContent(StringUtils.nvl(params.get("contentIntro")));
        NettyCtxGroup.group.writeAndFlush(new Packet(ChannelSet.CMD_NOTICE,
            WarnActionSet.SOFTWARE_NOTICE, builder.build().toByteArray()));
        break;
      // 当前在线的会员用户
      case 1:
        builder.setTitle(StringUtils.nvl(params.get("contentTitle")));
        builder.setContent(StringUtils.nvl(params.get("contentIntro")));
        NettyCtxGroup.vipGroup.writeAndFlush(new Packet(ChannelSet.CMD_NOTICE,
            WarnActionSet.SOFTWARE_NOTICE, builder.build().toByteArray()));
        break;
      // 个人用户推送
      default:
        Long userId = ((Number) params.get("userId")).longValue();
        builder.setTitle(StringUtils.nvl(params.get("contentTitle")));
        builder.setContent(StringUtils.nvl(params.get("content")));
        ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_NOTICE,
            WarnActionSet.SOFTWARE_NOTICE, builder.build().toByteArray()), userId);
        break;
    }
    return Result.success();
  }

  /**
   * TODO 解散通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result dissolveChatNotice(Map<String, Object> params) {
    C10001.C100013s2c.Builder builder = C10001.C100013s2c.newBuilder();
    builder.setNoticeType(2);
    builder.setTitle("群聊通知");
    builder.setContent("你加入的群聊 " + StringUtils.nvl(params.get("groupName")) + " 已被群主解散.");
    List<Long> userIdList = JsonUtils.toListType(params.get("userIds"), Long.class);
    if (userIdList != null) {
      for (Long aLong : userIdList) {
        // 软件通知
        ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_NOTICE,
            WarnActionSet.SOFTWARE_NOTICE, builder.build().toByteArray()), aLong);
        // 退出聊天
        ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_CHAT,
            ChatActionSet.DISSOLVE_CHAT, null), aLong);
      }
    }
    return Result.success();
  }

  /**
   * TODO 添加通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  @Override
  public Result newAddFriendNotice(Map<String, Object> params) {
    long targetId = ((Number) params.get("targetId")).longValue();
    Map<String, Object> dressInfo = this.userRemote.getUserFrame(targetId);
    if (dressInfo != null) {
      C10001.C100010s2c.Builder builder = C10001.C100010s2c.newBuilder();
      builder.setNickName(StringUtils.nvl(dressInfo.get("alias")));
      builder.setUserId(targetId);
      ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_NOTICE,
          WarnActionSet.APPLY_NOTICE, builder.build().toByteArray()), targetId);
    }
    return Result.success();
  }

  /**
   * TODO 群聊通知.
   *
   * @param params [groupId, message]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 15:44
   * @update 2020/11/11 15:44
   */
  @Override
  public Result groupNoticeMessage(Map<String, Object> params) {
    long groupId = ((Number) params.get("groupId")).longValue();
    String message = StringUtils.nvl(params.get("message"));
    C10002.C1000210s2c.Builder response = C10002.C1000210s2c.newBuilder();
    ChannelManager.sendPacketToChatGroup(
        new Packet(ChannelSet.CMD_CHAT, (short) 10,
            response.setNoticeMessage(message).build().toByteArray()), groupId);
    return Result.success();
  }

  /**
   * @MethodName: vipGradeNotice
   * @Description: TODO 会员升级通知
   * @Param: [params]
   * @Return: com.enuos.live.result.Result
   * @Author: xubin
   * @Date: 15:07 2020/8/25
   **/
  @Override
  public Result vipGradeNotice(Map<String, Object> params) {
    log.info("会员升级全服通知,params=[{}]", params);
    Long userId = MapUtil.getLong(params, "userId");
    C10004.C100040s2c.Builder builder = C10004.C100040s2c.newBuilder();
    builder.setUserId(userId);// 用户id
    builder.setNickName(String.valueOf(params.get("nickName"))); // 昵称
    builder.setThumbIconURL(String.valueOf(params.get("thumbIconURL"))); // 头像
    builder.setVip(MapUtil.getLong(params, "vip")); // vip等级

    // 当前在线的所有用户发送消息
    NettyCtxGroup.group.writeAndFlush(
        new Packet(ChannelSet.MONEY_NOTICE, (short) 0, builder.build().toByteArray()));

    return Result.success();
  }

}
