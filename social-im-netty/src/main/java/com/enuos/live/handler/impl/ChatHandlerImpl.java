package com.enuos.live.handler.impl;

import com.enuos.live.action.ChannelSet;
import com.enuos.live.action.ChatActionSet;
import com.enuos.live.component.FeignMultipartFile;
import com.enuos.live.constants.ChatRedisKey;
import com.enuos.live.constants.GameRedisKey;
import com.enuos.live.core.Packet;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.handler.ChatHandler;
import com.enuos.live.handler.TaskHandler;
import com.enuos.live.handler.WarnHandler;
import com.enuos.live.manager.ChannelManager;
import com.enuos.live.pojo.PicInfo;
import com.enuos.live.proto.c10002msg.C10002;
import com.enuos.live.proto.d10001msg.D10001;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.rest.ChatRemote;
import com.enuos.live.rest.GameRemote;
import com.enuos.live.rest.UploadRemote;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.util.GameUtil;
import com.enuos.live.util.JsonUtils;
import com.enuos.live.util.RedisUtils;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * TODO 聊天实现.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V1.0.0
 * @since 2020/4/9 9:15
 */

@Component
public class ChatHandlerImpl implements ChatHandler {

  private static final Logger logger = LoggerFactory.getLogger(ChatHandlerImpl.class);

  private static final int ENTER_CHAT = 1;
  private static final int LEAVE_CHAT = 0;

  @Resource
  private ChatRemote chatRemote;
  @Resource
  private UserRemote userRemote;
  @Resource
  private GameRemote gameRemote;
  @Resource
  private UploadRemote uploadRemote;
  @Resource
  private WarnHandler warnHandler;
  @Resource
  private TaskHandler taskHandler;

  /** Redis工具类. */
  @Resource
  private RedisUtils redisUtils;

  /**
   * TODO 聊天分发.
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/4/14 18:27
   * @update 2020/5/13 21:14
   */
  @Override
  public void enterChatRoom(Channel channel, Packet packet) {
    try {
      C10002.C100020c2s request = C10002.C100020c2s.parseFrom(packet.bytes);
      // 1 单聊 2 群聊
      if (request.getSort() == 1) {
        enterSingleChat(channel, packet);
      } else {
        enterGroupChat(channel, packet);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 进入单聊. 
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 21:14
   * @update 2020/8/5 15:38
   */
  private void enterSingleChat(Channel channel, Packet packet) {
    try {
      C10002.C100020c2s request = C10002.C100020c2s.parseFrom(packet.bytes);
      C10002.C100020s2c.Builder builder = C10002.C100020s2c.newBuilder();
      builder.setSort(request.getSort());
      builder.setTargetId(request.getTargetId());
      // 进入房间
      singleChatStatus(packet.userId, request.getTargetId(), ENTER_CHAT);
      // 是否拉黑
      Integer relation = this.userRemote.getRelation(request.getTargetId(), packet.userId, 1);
      if (relation != null) {
        builder.setIsBlacklist(relation);
      } else {
        channel.writeAndFlush(
            new Packet(ChannelSet.CMD_CHAT, ChatActionSet.ENTER_CHAT_ROOM,
                builder.setResult(1).build().toByteArray()));
        return;
      }
      // 获取用户信息
      List<Long> userIds = Lists.newLinkedList();
      userIds.add(packet.userId);
      userIds.add(request.getTargetId());
      C10002.ESUserInfo.Builder userInfo;
      for (Long userId : userIds) {
        userInfo = C10002.ESUserInfo.newBuilder();
        Map<String, Object> dressInfo = this.userRemote.getUserFrame(userId);
        if (dressInfo != null) {
          userInfo.setUserId(userId);
          if (!userId.equals(packet.userId)) {
            Map<String, Object> newResult = this.userRemote.getUserBase(packet.userId, userId);
            if (newResult != null) {
              String remark = String.valueOf(newResult.get("remark"));
              if (remark.length() > 0) {
                userInfo.setAlias(remark);
              } else {
                userInfo.setAlias(StringUtils.nvl(newResult.get("nickName")));
              }
            }
          } else {
            userInfo.setAlias(StringUtils.nvl(dressInfo.get("alias")));
          }
          userInfo.setIconURL(StringUtils.nvl(dressInfo.get("iconUrl")));
          userInfo.setIconFrame(StringUtils.nvl(dressInfo.get("iconFrame")));
          userInfo.setChatFrame(StringUtils.nvl(dressInfo.get("chatFrame")));
          userInfo.setChatFrameAttribute(StringUtils.nvl(dressInfo.get("chatFrameAttribute")));
          builder.addUserInfo(userInfo);
        }
      }
      // 信息完整校验
      if (builder.getUserInfoList().size() == 0) {
        channel.writeAndFlush(
            new Packet(ChannelSet.CMD_CHAT, ChatActionSet.ENTER_CHAT_ROOM,
                builder.setResult(1).build().toByteArray()));
      } else {
        channel.writeAndFlush(
            new Packet(ChannelSet.CMD_CHAT, ChatActionSet.ENTER_CHAT_ROOM,
                builder.setResult(0).build().toByteArray()));
        // 添加Redis
        this.redisUtils.set(ChatRedisKey.KEY_CHAT_LOGIN + packet.userId, request.getTargetId());
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 进入群聊. 
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 21:14
   * @update 2020/8/10 5:08
   */
  private void enterGroupChat(Channel channel, Packet packet) {
    try {
      C10002.C100020c2s request = C10002.C100020c2s.parseFrom(packet.bytes);
      C10002.C100020s2c.Builder builder = C10002.C100020s2c.newBuilder();
      builder.setSort(request.getSort());
      builder.setGroupId(request.getGroupId());
      // 刷新状态
      groupChatStatus(request.getGroupId(), packet.userId, ENTER_CHAT);
      // 刷新成员列表 存放Redis
      if (!this.redisUtils.hasKey(ChatRedisKey.KEY_CHAT_GROUP_MEMBER + request.getGroupId())) {
        Result result1 = this.chatRemote.getGroupUserId(request.getGroupId());
        Result result2 = this.chatRemote.getGroupInfoResult(request.getGroupId());
        List<Long> groupMember = result1.getCode().equals(0) ? JsonUtils.toListType(result1.getData(), Long.class) : null;
        Map<String, Object> groupInfo = result2.getCode().equals(0) ? JsonUtils.toObjectMap(result2.getData()) : null;
        if (CollectionUtils.isNotEmpty(groupMember) && groupInfo != null) {
          D10001.GroupMember.Builder groupBuilder = D10001.GroupMember.newBuilder();
          groupBuilder.setGroupIcon(StringUtils.nvl(groupInfo.get("groupIcon")));
          groupBuilder.setGroupName(StringUtils.nvl(groupInfo.get("groupName")));
          groupBuilder.addAllMemberId(groupMember);
          this.redisUtils.setByte(ChatRedisKey.KEY_CHAT_GROUP_MEMBER + request.getGroupId(), groupBuilder.build().toByteArray());
        } else {
          // 推送结果
          channel.writeAndFlush(
              new Packet(ChannelSet.CMD_CHAT, ChatActionSet.ENTER_CHAT_ROOM,
                  builder.setResult(1).build().toByteArray()));
        }
      }
      Map<String, Object> userDress = this.userRemote.getUserFrame(packet.userId);
      D10001.LoginGroup.Builder loginGroup =  D10001.LoginGroup.newBuilder();
      loginGroup.setGroupId(request.getGroupId());
      C10002.ESUserInfo.Builder userInfo = C10002.ESUserInfo.newBuilder();
      D10001.ESUserInfo.Builder dressInfo = D10001.ESUserInfo.newBuilder();
      if (userDress != null) {
        // 装饰信息
        userInfo.setUserId(packet.userId);
        userInfo.setAlias(StringUtils.nvl(userDress.get("alias")));
        userInfo.setIconURL(StringUtils.nvl(userDress.get("iconUrl")));
        userInfo.setIconFrame(StringUtils.nvl(userDress.get("iconFrame")));
        userInfo.setChatFrame(StringUtils.nvl(userDress.get("chatFrame")));
        userInfo.setChatFrameAttribute(StringUtils.nvl(userDress.get("chatFrameAttribute")));
        builder.addUserInfo(userInfo);
        // 缓存信息
        dressInfo.setUserId(userInfo.getUserId());
        dressInfo.setAlias(userInfo.getAlias());
        dressInfo.setIconURL(userInfo.getIconURL());
        dressInfo.setIconFrame(userInfo.getIconFrame());
        dressInfo.setChatFrame(userInfo.getChatFrame());
        dressInfo.setChatFrameAttribute(userInfo.getChatFrameAttribute());
        loginGroup.setUserInfo(dressInfo);
      }
      // 进入聊天室
      if (!this.redisUtils.hasKey(ChatRedisKey.KEY_CHAT_GROUP_LOGIN + packet.userId)) {
        this.redisUtils.setByte(ChatRedisKey.KEY_CHAT_GROUP_LOGIN + packet.userId, loginGroup.build().toByteArray());
      }
      ChannelManager.refreshChatGroup(request.getGroupId(), channel);
      channel.writeAndFlush(
          new Packet(ChannelSet.CMD_CHAT, ChatActionSet.ENTER_CHAT_ROOM,
              builder.setResult(0).build().toByteArray()));
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 信息分发. 
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 21:14
   * @update 2020/7/24 18:41
   */
  @Override
  public void sendMessage(Channel channel, Packet packet) {
    try {
      C10002.C100021c2s request = C10002.C100021c2s.parseFrom(packet.bytes);
      // 1 单聊 2 群聊
      if (request.getSort() == 1) {
        singleChatMessage(channel, packet);
      } else {
        groupChatMessage(channel, packet);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 发送消息. 单聊
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 21:14
   * @update 2020/7/28 21:08
   */
  private void singleChatMessage(Channel channel, Packet packet) {
    try {
      C10002.C100021c2s request = C10002.C100021c2s.parseFrom(packet.bytes);
      // 验证关系
      Result result = this.chatRemote.userRelationIsExist(request.getTargetId(), packet.userId);
      Integer isExist = result.getCode().equals(0) ? (Integer) result.getData() : null;
      if (isExist != null) {
        // 是否拉黑
        Integer relation = this.userRemote.getRelation(request.getTargetId(), packet.userId, 1);
        if (relation != null) {
          if (relation == 0) {
            Channel targetChannel = ChannelManager.getChannel(request.getTargetId());
            // 目标 在线/离线 && 目标连接服务 -> 判断网络连接（活跃状态）
            if (targetChannel != null && targetChannel.isActive()) {
              // 在线消息
              singleChatIsOnline(channel, targetChannel, packet);
            } else {
              // 离线消息
              singleChatIsOffline(channel, packet);
            }
          } else {
            sendFailed(channel, request);
          }
        } else {
          sendFailed(channel, request);
        }
      } else {
        sendFailed(channel, request);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 单聊消息. 在线
   *
   * @param userChannel 快速通道
   * @param targetChannel 目标通道
   * @param packet 客户端数据
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 21:14
   * @update 2020/7/28 7:10
   */
  private void singleChatIsOnline(Channel userChannel, Channel targetChannel, Packet packet) {
    try {
      C10002.C100021c2s request = C10002.C100021c2s.parseFrom(packet.bytes);
      switch (request.getMessageType()) {
        // Emoji
        case 1:
          sendEmojiToSingleChat(userChannel, targetChannel, packet);
          break;
        // 图片
        case 2:
          sendImageOrVideoToSingleChat(userChannel, targetChannel, packet);
          break;
        // 语音
        case 3:
          sendVoiceToSingleChat(userChannel, targetChannel, packet);
          break;
        // 视频
        case 4:
          sendImageOrVideoToSingleChat(userChannel, targetChannel, packet);
          break;
        // 文本
        default:
          sendTextToSingleChat(userChannel, targetChannel, packet);
          break;
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 发送表情. 在线(单聊)
   *
   * @param channel [快速管道]
   * @param targetChannel [目标管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 12:40
   * @update 2020/11/10 12:40
   */
  private void sendEmojiToSingleChat(Channel channel, Channel targetChannel, Packet packet) {
    try {
      C10002.C100021c2s request = C10002.C100021c2s.parseFrom(packet.bytes);
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      Map<String, Object> message = Maps.newHashMap();
      message.put("userId", packet.userId);
      message.put("targetId", request.getTargetId());
      message.put("messageId", request.getMessageId());
      message.put("messageType", request.getMessageType());
      message.put("sendTime", dtf.format(LocalDateTime.now()));
      message.putAll(messageTypeSorting(request, null));
      // 聊天状态
      Integer chatStatus = targetUserChatStatus(request.getTargetId(), packet.userId);
      // 当前目标用户在聊天室内
      if (chatStatus != 0) {
        Long recordId = resultRecordId(message, 1);
        if (recordId > 0) {
          // 发送成功
          sendSuccess(channel, request, recordId);
          // 发送消息
          message.put("recordId", recordId);
          message.put("isAction", 1);
          forwardMessage(targetChannel, message);
          // 更新列表 0 未读 1 已读
          updateNoticeList(message, 1);
        } else {
          sendFailed(channel, request);
        }
      } else {
        Long recordId = resultRecordId(message, 0);
        if (recordId > 0) {
          // 发送成功
          sendSuccess(channel, request, recordId);
          // 发送通知
          this.warnHandler.sendAloneNoticeMessage(message);
          message.put("recordId", recordId);
          // 更新列表  0 未读 1 已读
          updateNoticeList(message, 0);
        } else {
          sendFailed(channel, request);
        }
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 图片视频. 在线(单聊)
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 21:14
   * @update 2020/7/24 18:41
   */
  private void sendImageOrVideoToSingleChat(Channel channel, Channel targetChannel, Packet packet) {
    try {
      C10002.C100021c2s request = C10002.C100021c2s.parseFrom(packet.bytes);
      // 文件上传
      Map<String, Object> fileResult = fileUpload(request);
      if (fileResult != null) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> message =  Maps.newHashMap();
        message.put("userId", packet.userId);
        message.put("targetId", request.getTargetId());
        message.put("messageId", request.getMessageId());
        message.put("messageType", request.getMessageType());
        message.put("sendTime", dtf.format(LocalDateTime.now()));
        message.putAll(messageTypeSorting(request, fileResult));
        // 聊天状态 [0闲置中 1聊天中] 判断目标用户是否和当前用户在同一个聊天室
        Integer chatStatus = targetUserChatStatus(request.getTargetId(), packet.userId);
        // 当前目标用户在聊天室内
        if (chatStatus != 0) {
          Long recordId = resultRecordId(message, 1);
          if (recordId > 0) {
            // 发送成功
            sendSuccess(channel, request, recordId);
            // 发送消息
            message.put("recordId", recordId);
            message.put("isAction", 1);
            forwardMessage(targetChannel, message);
            // 更新列表 0 未读 1 已读
            updateNoticeList(message, 1);
          } else {
            sendFailed(channel, request);
          }
        } else {
          Long recordId = resultRecordId(message, 0);
          if (recordId > 0) {
            // 发送成功
            sendSuccess(channel, request, recordId);
            // 发送通知
            this.warnHandler.sendAloneNoticeMessage(message);
            // 更新列表  0 未读 1 已读
            message.put("recordId", recordId);
            updateNoticeList(message, 0);
          } else {
            sendFailed(channel, request);
          }
        }
      } else {
        // 上传失败
        sendFailed(channel, request);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 发送语音. 在线(单聊)
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 21:14
   * @update 2020/7/28 7:10
   */
  private void sendVoiceToSingleChat(Channel channel, Channel targetChannel, Packet packet) {
    try {
      C10002.C100021c2s request = C10002.C100021c2s.parseFrom(packet.bytes);
      // 文件上传
      Map<String, Object> fileResult = fileUpload(request);
      if (fileResult != null) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> message =  Maps.newHashMap();
        message.put("userId", packet.userId);
        message.put("targetId", request.getTargetId());
        message.put("messageId", request.getMessageId());
        message.put("messageType", request.getMessageType());
        message.put("sendTime", dtf.format(LocalDateTime.now()));
        message.putAll(messageTypeSorting(request, fileResult));
        // 聊天状态 [0闲置中 1聊天中] 判断目标用户是否和当前用户在同一个聊天室
        Integer chatStatus = targetUserChatStatus(request.getTargetId(), packet.userId);
        // 当前目标用户在聊天室内
        if (chatStatus != 0) {
          Long recordId = resultRecordId(message, 1);
          if (recordId > 0) {
            // 发送成功
            sendSuccess(channel, request, recordId);
            // 发送消息
            message.put("recordId", recordId);
            message.put("isAction", 0);
            forwardMessage(targetChannel, message);
            // 更新列表 0 未读 1 已读
            updateNoticeList(message, 1);
          } else {
            sendFailed(channel, request);
          }
        } else {
          Long recordId = resultRecordId(message, 0);
          if (recordId > 0) {
            // 发送成功
            sendSuccess(channel, request, recordId);
            // 发送通知
            this.warnHandler.sendAloneNoticeMessage(message);
            message.put("recordId", recordId);
            // 更新列表  0 未读 1 已读
            updateNoticeList(message, 0);
          } else {
            sendFailed(channel, request);
          }
        }
      } else {
        // 上传失败
        sendFailed(channel, request);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 发送文本. 在线(单聊)
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 21:14
   * @update 2020/7/24 18:41
   */
  private void sendTextToSingleChat(Channel channel, Channel targetChannel, Packet packet) {
    try {
      C10002.C100021c2s request = C10002.C100021c2s.parseFrom(packet.bytes);
      String content = request.getMessage();
      int contentLength = content.length();
      if (contentLength <= 200) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> message = Maps.newHashMap();
        message.put("userId", packet.userId);
        message.put("targetId", request.getTargetId());
        message.put("messageId", request.getMessageId());
        message.put("messageType", request.getMessageType());
        message.put("sendTime", dtf.format(LocalDateTime.now()));
        message.putAll(messageTypeSorting(request, null));
        // 聊天状态 [0闲置中 1聊天中] 判断目标用户是否和当前用户在同一个聊天室
        Integer chatStatus = targetUserChatStatus(request.getTargetId(), packet.userId);
        // 当前目标用户在聊天室内
        if (chatStatus != 0) {
          Long recordId = resultRecordId(message, 1);
          if (recordId > 0) {
            // 发送成功
            sendSuccess(channel, request, recordId);
            // 发送消息
            message.put("recordId", recordId);
            message.put("isAction", 1);
            forwardMessage(targetChannel, message);
            // 更新列表 0 未读 1 已读
            updateNoticeList(message, 1);
          } else {
            sendFailed(channel, request);
          }
        } else {
          Long recordId = resultRecordId(message, 0);
          if (recordId > 0) {
            // 发送成功
            sendSuccess(channel, request, recordId);
            // 发送通知
            this.warnHandler.sendAloneNoticeMessage(message);
            message.put("recordId", recordId);
            // 更新列表  0 未读 1 已读
            updateNoticeList(message, 0);
          } else {
            sendFailed(channel, request);
          }
        }
      } else {
        C10002.C100021s2c.Builder builder = C10002.C100021s2c.newBuilder();
        builder.setResult(2).setRecordId(0).setMessageId(request.getMessageId());
        channel.writeAndFlush(
            new Packet(ChannelSet.CMD_CHAT, ChatActionSet.SEND_MESSAGE,
                builder.build().toByteArray()));
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 单聊消息. 离线
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 21:14
   * @update 2020/7/28 7:10
   */
  private void singleChatIsOffline(Channel channel, Packet packet) {
    try {
      C10002.C100021c2s request = C10002.C100021c2s.parseFrom(packet.bytes);
      if (request.getMessageType() > 1) {
        // 图片  语音 视频
        offlineFileToSingleChat(channel, packet);
      } else if (request.getMessageType() == 1) {
        // Emoji
        offlineEmojiToSingleChat(channel, packet);
      } else {
        // 文本
        offlineTextToSingleChat(channel, packet);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 发送文件. 离线(单聊)
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 21:14
   * @update 2020/7/28 7:10
   */
  private void offlineFileToSingleChat(Channel channel, Packet packet) {
    try {
      C10002.C100021c2s request = C10002.C100021c2s.parseFrom(packet.bytes);
      // 文件上传
      Map<String, Object> fileResult = fileUpload(request);
      if (fileResult != null) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> message =  Maps.newHashMap();
        message.put("userId", packet.userId);
        message.put("targetId", request.getTargetId());
        message.put("messageId", request.getMessageId());
        message.put("messageType", request.getMessageType());
        message.put("sendTime", dtf.format(LocalDateTime.now()));
        message.putAll(messageTypeSorting(request, fileResult));
        Long recordId = resultRecordId(message, 0);
        if (recordId > 0) {
          // 发送成功
          sendSuccess(channel, request, recordId);
          message.put("recordId", recordId);
          // 更新列表  0 未读 1 已读
          updateNoticeList(message, 0);
        } else {
          sendFailed(channel, request);
        }
      } else {
        // 上传失败
        sendFailed(channel, request);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 发送表情. 离线(单聊)
   *
   * @param channel [快速通道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 13:57
   * @update 2020/11/10 13:57
   */
  private void offlineEmojiToSingleChat(Channel channel, Packet packet) {
    try {
      C10002.C100021c2s request = C10002.C100021c2s.parseFrom(packet.bytes);
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      Map<String, Object> message = Maps.newHashMap();
      message.put("userId", packet.userId);
      message.put("targetId", request.getTargetId());
      message.put("messageId", request.getMessageId());
      message.put("messageType", request.getMessageType());
      message.put("sendTime", dtf.format(LocalDateTime.now()));
      message.putAll(messageTypeSorting(request, null));
      Long recordId = resultRecordId(message, 0);
      if (recordId > 0) {
        // 发送成功
        sendSuccess(channel, request, recordId);
        message.put("recordId", recordId);
        // 更新列表  0 未读 1 已读
        updateNoticeList(message, 0);
      } else {
        sendFailed(channel, request);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 发送文本. 离线(单聊)
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 21:14
   * @update 2020/7/28 7:10
   */
  private void offlineTextToSingleChat(Channel channel, Packet packet) {
    try {
      C10002.C100021c2s request = C10002.C100021c2s.parseFrom(packet.bytes);
      String content = request.getMessage();
      int contentLength = content.length();
      if (contentLength <= 200) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> message = Maps.newHashMap();
        message.put("userId", packet.userId);
        message.put("targetId", request.getTargetId());
        message.put("messageId", request.getMessageId());
        message.put("messageType", request.getMessageType());
        message.put("sendTime", dtf.format(LocalDateTime.now()));
        message.putAll(messageTypeSorting(request, null));
        Long recordId = resultRecordId(message, 0);
        if (recordId > 0) {
          // 发送成功
          sendSuccess(channel, request, recordId);
          message.put("recordId", recordId);
          // 更新列表  0 未读 1 已读
          updateNoticeList(message, 0);
        } else {
          sendFailed(channel, request);
        }
      } else {
        C10002.C100021s2c.Builder builder = C10002.C100021s2c.newBuilder();
        builder.setResult(2).setRecordId(0).setMessageId(request.getMessageId());
        channel.writeAndFlush(
            new Packet(ChannelSet.CMD_CHAT, ChatActionSet.SEND_MESSAGE,
                builder.build().toByteArray()));
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 发送消息. 群聊
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/5/13 21:14
   * @update 2020/7/28 7:10
   */
  private void groupChatMessage(Channel channel, Packet packet) {
    try {
      C10002.C100021c2s request = C10002.C100021c2s.parseFrom(packet.bytes);
      if (this.redisUtils.hasKey(ChatRedisKey.KEY_CHAT_GROUP_MEMBER + request.getGroupId())) {
        byte[] bytes = this.redisUtils.getByte(ChatRedisKey.KEY_CHAT_GROUP_MEMBER + request.getGroupId());
        D10001.GroupMember groupMember = D10001.GroupMember.parseFrom(bytes);
        if (CollectionUtils.isNotEmpty(groupMember.getMemberIdList())) {
          List<Long> memberList = Lists.newArrayList();
          memberList.addAll(groupMember.getMemberIdList());
          if (memberList.contains(packet.userId)) {
            // 移除自己
            memberList.removeIf(longs -> longs.equals(packet.userId));
            // 在线成员
            List<Long> onlineList = Lists.newLinkedList();
            // 离线成员
            List<Long> offlineList = Lists.newLinkedList();
            for (Long memberId : memberList) {
              Channel isExist = ChannelManager.getChannel(memberId);
              // 目标 在线/离线 && 目标连接服务 -> 判断网络连接（活跃状态）
              if (isExist != null && isExist.isActive()) {
                onlineList.add(memberId);
              } else {
                offlineList.add(memberId);
              }
            }
            // 发送消息
            sendGroupMessage(channel, packet, onlineList, offlineList);
          } else {
            dataMissing(channel, request);
          }
        } else {
          sendFailed(channel, request);
        }
      } else {
        dataMissing(channel, request);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 消息分发. 群聊
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @param onlineList 在线成员
   * @param offlineList 离线成员
   * @author wangcaiwen|1443710411@qq.com
   * @since 2020/8/11 13:45
   * @date 2020/8/11 13:45
   */
  private void sendGroupMessage(Channel channel, Packet packet, List<Long> onlineList, List<Long> offlineList) {
    try {
      C10002.C100021c2s request = C10002.C100021c2s.parseFrom(packet.bytes);
      switch (request.getMessageType()) {
        // Emoji
        case 1:
          sendGroupMessageByEmoji(channel, packet, onlineList, offlineList);
          break;
        // 图片
        case 2:
          sendGroupMessageByFile(channel, packet, onlineList, offlineList);
          break;
        // 语音
        case 3:
          sendGroupMessageByVoice(channel, packet, onlineList, offlineList);
          break;
        // 视频
        case 4:
          sendGroupMessageByFile(channel, packet, onlineList, offlineList);
          break;
        // 文本
        default:
          sendGroupMessageByText(channel, packet, onlineList, offlineList);
          break;
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 发送表情. 群聊
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @param onlineList 在线成员
   * @param offlineList 离线成员
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 14:03
   * @update 2020/11/10 14:03
   */
  private void sendGroupMessageByEmoji(Channel channel, Packet packet, List<Long> onlineList, List<Long> offlineList) {
    try {
      C10002.C100021c2s request = C10002.C100021c2s.parseFrom(packet.bytes);
      byte[] groupByte = this.redisUtils.getByte(ChatRedisKey.KEY_CHAT_GROUP_MEMBER + request.getGroupId());
      byte[] userByte = this.redisUtils.getByte(ChatRedisKey.KEY_CHAT_GROUP_LOGIN + packet.userId);
      D10001.GroupMember groupInfo = D10001.GroupMember.parseFrom(groupByte);
      D10001.LoginGroup loginInfo = D10001.LoginGroup.parseFrom(userByte);
      D10001.ESUserInfo userInfo = loginInfo.getUserInfo();
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      Map<String, Object> message = Maps.newHashMap();
      message.put("groupId", request.getGroupId());
      message.put("sendId", userInfo.getUserId());
      message.put("sendName", userInfo.getAlias());
      message.put("messageId", request.getMessageId());
      message.put("messageType", request.getMessageType());
      message.put("sendTime", dtf.format(LocalDateTime.now()));
      message.putAll(messageTypeSorting(request, null));
      Long recordId = resultRecordId(message);
      if (recordId > 0) {
        // 发送成功
        sendSuccess(channel, request, recordId);
        message.put("recordId", recordId);
        message.put("isAction", 1);
        Map<String, Object> notice = Maps.newHashMap();
        notice.put("groupName", groupInfo.getGroupName());
        notice.put("groupIcon", groupInfo.getGroupName());
        notice.putAll(message);
        C10002.C100022s2c.Builder builder = aggregateMessage(message, userInfo);
        if (onlineList.size() > 0) {
          for (Long userId : onlineList) {
            Integer chatStatus = resultChatStatusGroup(request.getGroupId(), userId);
            // 当前目标用户在聊天室内
            if (chatStatus != 0) {
              ChannelManager.sendPacketToUserId(
                  new Packet(ChannelSet.CMD_CHAT, ChatActionSet.RECEIVE_MESSAGE,
                      builder.build().toByteArray()), userId);
            } else {
              offlineList.add(userId);
              // 发送提醒
              notice.put("userId", userId);
              this.warnHandler.sendGroupNoticeMessage(notice);
            }
          }
        }
        if (onlineList.size() > 0 && offlineList.size() > 0) {
          message.put("userList", offlineList);
          this.chatRemote.updateUserGroupUnreadNum(message);
        }
        if (onlineList.size() == 0 && offlineList.size() > 0) {
          message.put("userList", offlineList);
          this.chatRemote.updateUserGroupUnreadNum(message);
        }
        // 更新群聊末尾消息
        message.put("messageUser", packet.userId);
        this.chatRemote.updateGroupLastMessageMap(message);
      } else {
        sendFailed(channel, request);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 发送文本. 群聊
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @param onlineList 在线成员
   * @param offlineList 离线成员
   * @author wangcaiwen|1443710411@qq.com
   * @since 2020/8/11 13:46
   * @date 2020/8/11 13:46
   */
  private void sendGroupMessageByText(Channel channel, Packet packet, List<Long> onlineList, List<Long> offlineList) {
    try {
      C10002.C100021c2s request = C10002.C100021c2s.parseFrom(packet.bytes);
      String content = request.getMessage();
      int contentLength = content.length();
      if (contentLength <= 200) {
        byte[] groupByte = this.redisUtils.getByte(ChatRedisKey.KEY_CHAT_GROUP_MEMBER + request.getGroupId());
        byte[] userByte = this.redisUtils.getByte(ChatRedisKey.KEY_CHAT_GROUP_LOGIN + packet.userId);
        D10001.GroupMember groupInfo = D10001.GroupMember.parseFrom(groupByte);
        D10001.LoginGroup loginInfo = D10001.LoginGroup.parseFrom(userByte);
        D10001.ESUserInfo userInfo = loginInfo.getUserInfo();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> message = Maps.newHashMap();
        message.put("groupId", request.getGroupId());
        message.put("sendId", userInfo.getUserId());
        message.put("sendName", userInfo.getAlias());
        message.put("messageId", request.getMessageId());
        message.put("messageType", request.getMessageType());
        message.put("sendTime", dtf.format(LocalDateTime.now()));
        message.putAll(messageTypeSorting(request, null));
        Long recordId = resultRecordId(message);
        if (recordId > 0) {
          // 发送成功
          sendSuccess(channel, request, recordId);
          message.put("recordId", recordId);
          message.put("isAction", 1);
          Map<String, Object> notice = Maps.newHashMap();
          notice.put("groupName", groupInfo.getGroupName());
          notice.put("groupIcon", groupInfo.getGroupName());
          notice.putAll(message);
          C10002.C100022s2c.Builder builder = aggregateMessage(message, userInfo);
          if (onlineList.size() > 0) {
            for (Long userId : onlineList) {
              Integer chatStatus = resultChatStatusGroup(request.getGroupId(), userId);
              // 当前目标用户在聊天室内
              if (chatStatus != 0) {
                ChannelManager.sendPacketToUserId(
                    new Packet(ChannelSet.CMD_CHAT, ChatActionSet.RECEIVE_MESSAGE,
                        builder.build().toByteArray()), userId);
              } else {
                offlineList.add(userId);
                //  发送提醒
                notice.put("userId", userId);
                this.warnHandler.sendGroupNoticeMessage(notice);
              }
            }
          }
          if (onlineList.size() > 0 && offlineList.size() > 0) {
            message.put("userList", offlineList);
            this.chatRemote.updateUserGroupUnreadNum(message);
          }
          if (onlineList.size() == 0 && offlineList.size() > 0) {
            message.put("userList", offlineList);
            this.chatRemote.updateUserGroupUnreadNum(message);
          }
          // 更新群聊末尾消息
          message.put("messageUser", packet.userId);
          this.chatRemote.updateGroupLastMessageMap(message);
        } else {
          sendFailed(channel, request);
        }
      } else {
        C10002.C100021s2c.Builder builder = C10002.C100021s2c.newBuilder();
        builder.setResult(2).setRecordId(0).setMessageId(request.getMessageId());
        channel.writeAndFlush(
            new Packet(ChannelSet.CMD_CHAT, ChatActionSet.SEND_MESSAGE,
                builder.build().toByteArray()));
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 图片视频. 群聊
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @param onlineList 在线成员
   * @param offlineList 离线成员
   * @author wangcaiwen|1443710411@qq.com
   * @since 2020/8/11 13:52
   * @date 2020/8/11 13:52
   */
  private void sendGroupMessageByFile(Channel channel, Packet packet, List<Long> onlineList, List<Long> offlineList) {
    try {
      C10002.C100021c2s request = C10002.C100021c2s.parseFrom(packet.bytes);
      // 文件上传
      Map<String, Object> fileResult = fileUpload(request);
      if (fileResult != null) {
        byte[] groupByte = this.redisUtils.getByte(ChatRedisKey.KEY_CHAT_GROUP_MEMBER + request.getGroupId());
        byte[] userByte = this.redisUtils.getByte(ChatRedisKey.KEY_CHAT_GROUP_LOGIN + packet.userId);
        D10001.GroupMember groupInfo = D10001.GroupMember.parseFrom(groupByte);
        D10001.LoginGroup loginInfo = D10001.LoginGroup.parseFrom(userByte);
        D10001.ESUserInfo userInfo = loginInfo.getUserInfo();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> message = Maps.newHashMap();
        message.put("groupId", request.getGroupId());
        message.put("sendId", userInfo.getUserId());
        message.put("sendName", userInfo.getAlias());
        message.put("messageId", request.getMessageId());
        message.put("messageType", request.getMessageType());
        message.put("sendTime", dtf.format(LocalDateTime.now()));
        message.putAll(messageTypeSorting(request, fileResult));
        Long recordId = resultRecordId(message);
        if (recordId > 0) {
          // 发送成功
          sendSuccess(channel, request, recordId);
          message.put("recordId", recordId);
          message.put("isAction", 1);
          Map<String, Object> notice = Maps.newHashMap();
          notice.put("groupName", groupInfo.getGroupName());
          notice.put("groupIcon", groupInfo.getGroupName());
          notice.putAll(message);
          C10002.C100022s2c.Builder builder = aggregateMessage(message, userInfo);
          if (onlineList.size() > 0) {
            for (Long userId : onlineList) {
              Integer chatStatus = resultChatStatusGroup(request.getGroupId(), userId);
              // 当前目标用户在聊天室内
              if (chatStatus != 0) {
                ChannelManager.sendPacketToUserId(
                    new Packet(ChannelSet.CMD_CHAT, ChatActionSet.RECEIVE_MESSAGE,
                        builder.build().toByteArray()), userId);
              } else {
                offlineList.add(userId);
                //  发送提醒
                notice.put("userId", userId);
                this.warnHandler.sendGroupNoticeMessage(notice);
              }
            }
          }
          if (onlineList.size() > 0 && offlineList.size() > 0) {
            message.put("userList", offlineList);
            this.chatRemote.updateUserGroupUnreadNum(message);
          }
          if (onlineList.size() == 0 && offlineList.size() > 0) {
            message.put("userList", offlineList);
            this.chatRemote.updateUserGroupUnreadNum(message);
          }
          // 更新群聊末尾消息
          message.put("messageUser", packet.userId);
          this.chatRemote.updateGroupLastMessageMap(message);
        } else {
          sendFailed(channel, request);
        }
      } else {
        sendFailed(channel, request);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 发送语音. 群聊
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @param onlineList 在线成员
   * @param offlineList 离线成员
   * @author wangcaiwen|1443710411@qq.com
   * @since 2020/8/11 13:53
   * @date 2020/8/11 13:53
   */
  private void sendGroupMessageByVoice(Channel channel, Packet packet, List<Long> onlineList, List<Long> offlineList) {
    try {
      C10002.C100021c2s request = C10002.C100021c2s.parseFrom(packet.bytes);
      // 文件上传
      Map<String, Object> fileResult = fileUpload(request);
      if (fileResult != null) {
        byte[] groupByte = this.redisUtils.getByte(ChatRedisKey.KEY_CHAT_GROUP_MEMBER + request.getGroupId());
        byte[] userByte = this.redisUtils.getByte(ChatRedisKey.KEY_CHAT_GROUP_LOGIN + packet.userId);
        D10001.GroupMember groupInfo = D10001.GroupMember.parseFrom(groupByte);
        D10001.LoginGroup loginInfo = D10001.LoginGroup.parseFrom(userByte);
        D10001.ESUserInfo userInfo = loginInfo.getUserInfo();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> message = Maps.newHashMap();
        message.put("groupId", request.getGroupId());
        message.put("sendId", userInfo.getUserId());
        message.put("sendName", userInfo.getAlias());
        message.put("messageId", request.getMessageId());
        message.put("messageType", request.getMessageType());
        message.put("sendTime", dtf.format(LocalDateTime.now()));
        message.putAll(messageTypeSorting(request, fileResult));
        Long recordId = resultRecordId(message);
        if (recordId > 0) {
          // 发送成功
          sendSuccess(channel, request, recordId);
          message.put("recordId", recordId);
          message.put("isAction", 0);
          Map<String, Object> notice = Maps.newHashMap();
          notice.put("groupName", groupInfo.getGroupName());
          notice.put("groupIcon", groupInfo.getGroupName());
          notice.putAll(message);
          C10002.C100022s2c.Builder builder = aggregateMessage(message, userInfo);
          List<Long> tempList = Lists.newLinkedList();
          if (onlineList.size() > 0) {
            for (long userId : onlineList) {
              Integer chatStatus = resultChatStatusGroup(request.getGroupId(), userId);
              // 当前目标用户在聊天室内
              if (chatStatus != 0) {
                ChannelManager.sendPacketToUserId(
                    new Packet(ChannelSet.CMD_CHAT, ChatActionSet.RECEIVE_MESSAGE,
                        builder.build().toByteArray()), userId);
              } else {
                tempList.add(userId);
                offlineList.add(userId);
                //  发送提醒
                notice.put("userId", userId);
                this.warnHandler.sendGroupNoticeMessage(notice);
              }
            }
          }
          if (CollectionUtils.isNotEmpty(tempList)) {
            for (Long aLong : tempList) {
              onlineList.removeIf(userId -> userId.equals(aLong));
            }
          }
          if (onlineList.size() > 0 && offlineList.size() > 0) {
            message.put("userList", offlineList);
            this.chatRemote.updateUserGroupUnreadNum(message);
          }
          if (onlineList.size() == 0 && offlineList.size() > 0) {
            message.put("userList", offlineList);
            this.chatRemote.updateUserGroupUnreadNum(message);
          }
          // 更新群聊末尾消息
          message.put("messageUser", packet.userId);
          this.chatRemote.updateGroupLastMessageMap(message);
          // 生成语音未操作记录
          Map<String, Object> voiceMap = Maps.newHashMap();
          List<Long> userIds = Lists.newLinkedList();
          userIds.addAll(onlineList);
          userIds.addAll(offlineList);
          voiceMap.put("recordId", recordId);
          voiceMap.put("groupId", request.getGroupId());
          voiceMap.put("userList", userIds);
          this.chatRemote.insertVoiceUnreadInfo(voiceMap);
        } else {
          sendFailed(channel, request);
        }
      } else {
        sendFailed(channel, request);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 发送邀请. 
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author WangCaiWen
   * @since 2020/7/21 - 2020/7/21
   */
  @Override
  public void sendGameInvite(Channel channel, Packet packet) {
    try {
      C10002.C100023c2s request = C10002.C100023c2s.parseFrom(packet.bytes);
      C10002.C100023s2c.Builder builder = C10002.C100023s2c.newBuilder();
      // 验证关系
      Result result0 = this.chatRemote.userRelationIsExist(request.getTargetId(), packet.userId);
      Integer isExist = result0.getCode().equals(0) ? (Integer) result0.getData() : null;
      if (isExist != null) {
        // 是否拉黑
        Integer relation = this.userRemote.getRelation(request.getTargetId(), packet.userId, 1);
        if (relation != null) {
          if (relation == 0) {
            Channel targetChannel = ChannelManager.getChannel(request.getTargetId());
            // 目标 在线/离线 && 目标连接服务 -> 判断网络连接（活跃状态）
            if (targetChannel != null && targetChannel.isActive()) {
              DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
              Map<String, Object> message = Maps.newHashMap();
              message.put("userId", packet.userId);
              message.put("targetId", request.getTargetId());
              message.put("messageId", request.getMessageId());
              message.put("messageType", 5);
              message.put("sort", 0);
              message.put("inviteId", request.getInviteId());
              message.put("inviteTitle", request.getInviteTitle());
              message.put("inviteImageUrl", request.getInviteImageURL());
              message.put("sendTime", dtf.format(LocalDateTime.now()));
              // 聊天状态 [0闲置中 1聊天中] 判断目标用户是否和当前用户在同一个聊天室
              Integer chatStatus = targetUserChatStatus(request.getTargetId(), packet.userId);
              // 当前目标用户在聊天室内
              if (chatStatus != 0) {
                Long recordId = resultRecordId(message, 1);
                if (recordId > 0) {
                  // 发送成功
                  builder.setResult(0).setRecordId(recordId).setMessageId(request.getMessageId());
                  channel.writeAndFlush(
                      new Packet(ChannelSet.CMD_CHAT, ChatActionSet.SEND_GAME_INVITE,
                          builder.build().toByteArray()));
                  // 发送消息
                  message.put("recordId", recordId);
                  message.put("isAction", 1);
                  forwardMessage(targetChannel, message);
                  // 更新列表 0 未读 1 已读
                  updateNoticeList(message, 1);
                  // 添加RedisKey
                  addInviteList(packet.userId, recordId);
                } else {
                  // 发送失败
                  builder.setResult(1).setRecordId(0).setMessageId(request.getMessageId());
                  channel.writeAndFlush(
                      new Packet(ChannelSet.CMD_CHAT, ChatActionSet.SEND_GAME_INVITE,
                          builder.build().toByteArray()));
                }
              } else {
                Long recordId = resultRecordId(message, 0);
                if (recordId > 0) {
                  // 发送成功
                  builder.setResult(0).setRecordId(recordId).setMessageId(request.getMessageId());
                  channel.writeAndFlush(
                      new Packet(ChannelSet.CMD_CHAT, ChatActionSet.SEND_GAME_INVITE,
                          builder.build().toByteArray()));
                  // 发送通知
                  this.warnHandler.sendAloneNoticeMessage(message);
                  message.put("recordId", recordId);
                  // 更新列表  0 未读 1 已读
                  updateNoticeList(message, 0);
                  // 添加RedisKey
                  addInviteList(packet.userId, recordId);
                } else {
                  // 发送失败
                  builder.setResult(1).setRecordId(0).setMessageId(request.getMessageId());
                  channel.writeAndFlush(
                      new Packet(ChannelSet.CMD_CHAT, ChatActionSet.SEND_GAME_INVITE,
                          builder.build().toByteArray()));
                }
              }
            } else {
              // 离线消息
              offlineInvited(channel, packet);
            }
          } else {
            // 发送失败
            builder.setResult(1).setRecordId(0).setMessageId(request.getMessageId());
            channel.writeAndFlush(
                new Packet(ChannelSet.CMD_CHAT, ChatActionSet.SEND_GAME_INVITE,
                    builder.build().toByteArray()));
          }
        } else {
          // 发送失败
          builder.setResult(1).setRecordId(0).setMessageId(request.getMessageId());
          channel.writeAndFlush(
              new Packet(ChannelSet.CMD_CHAT, ChatActionSet.SEND_GAME_INVITE,
                  builder.build().toByteArray()));
        }
      } else {
        // 发送失败
        builder.setResult(1).setRecordId(0).setMessageId(request.getMessageId());
        channel.writeAndFlush(
            new Packet(ChannelSet.CMD_CHAT, ChatActionSet.SEND_GAME_INVITE,
                builder.build().toByteArray()));
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 离线邀请. 
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  private void offlineInvited(Channel channel, Packet packet) {
    try {
      C10002.C100023c2s request = C10002.C100023c2s.parseFrom(packet.bytes);
      C10002.C100023s2c.Builder builder = C10002.C100023s2c.newBuilder();
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      Map<String, Object> message = Maps.newHashMap();
      message.put("userId", packet.userId);
      message.put("targetId", request.getTargetId());
      message.put("messageId", request.getMessageId());
      message.put("messageType", 5);
      message.put("inviteId", request.getInviteId());
      message.put("inviteTitle", request.getInviteTitle());
      message.put("inviteImageUrl", request.getInviteImageURL());
      message.put("sendTime", dtf.format(LocalDateTime.now()));
      Long recordId = resultRecordId(message, 0);
      if (recordId > 0) {
        // 发送成功
        builder.setResult(0).setRecordId(recordId).setMessageId(request.getMessageId());
        channel.writeAndFlush(
            new Packet(ChannelSet.CMD_CHAT, ChatActionSet.SEND_GAME_INVITE,
                builder.build().toByteArray()));
        message.put("recordId", recordId);
        // 更新列表  0 未读 1 已读
        updateNoticeList(message, 0);
        // 添加RedisKey
        addInviteList(packet.userId, recordId);
      } else {
        builder.setResult(1).setRecordId(0).setMessageId(request.getMessageId());
        channel.writeAndFlush(
            new Packet(ChannelSet.CMD_CHAT, ChatActionSet.SEND_GAME_INVITE,
                builder.build().toByteArray()));
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 聆听语音. 
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author WangCaiWen
   * @since 2020/7/21 - 2020/7/21
   */
  @Override
  public void listenVoice(Channel channel, Packet packet) {
    try {
      C10002.C100024c2s request = C10002.C100024c2s.parseFrom(packet.bytes);
      Map<String, Object> result = Maps.newHashMap();
      result.put("recordId", request.getRecordId());
      // 1 单聊 2 群聊
      if (request.getSort() == 1) {
        this.chatRemote.listenToVoice(result);
      } else {
        result.put("groupId", request.getGroupId());
        result.put("userId", packet.userId);
        this.chatRemote.listenToVoiceGroup(result);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 接受邀请. 
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author WangCaiWen
   * @since 2020/7/21 - 2020/7/21
   */
  @Override
  public void acceptInvite(Channel channel, Packet packet) {
    try {
      C10002.C100025c2s request = C10002.C100025c2s.parseFrom(packet.bytes);
      C10002.C100025s2c.Builder builder = C10002.C100025s2c.newBuilder();
      C10002.C100026s2c.Builder sendBuilder = C10002.C100026s2c.newBuilder();
      Map<String, Object> objectMap = Maps.newHashMap();
      // 获取当前邀请列表 移除当前的记录 其他记录均为取消约战
      if (this.redisUtils.hasKey(ChatRedisKey.KEY_CHAT_GAME_INVITE + request.getUserId())) {
        byte[] bytes = this.redisUtils.getByte(ChatRedisKey.KEY_CHAT_GAME_INVITE + request.getUserId());
        this.redisUtils.del(ChatRedisKey.KEY_CHAT_GAME_INVITE + request.getUserId());
        D10001.InviteRecord inviteRecord = D10001.InviteRecord.parseFrom(bytes);
        if (CollectionUtils.isNotEmpty(inviteRecord.getRecordIdList())) {
          List<Long> recordIdList = Lists.newArrayList();
          recordIdList.addAll(inviteRecord.getRecordIdList());
          // 移除目标记录
          recordIdList.removeIf(record -> record.equals(request.getRecordId()));
          objectMap.put("recordIdList", recordIdList);
          // 取消约战
          this.chatRemote.updateInviteStatus(objectMap);
          objectMap = Maps.newHashMap();
        }
        // 通知对方
        Channel targetChannel = ChannelManager.getChannel(request.getUserId());
        // 目标 在线/离线 && 目标连接服务 -> 判断网络连接（活跃状态）
        if (targetChannel != null && targetChannel.isActive()) {
          // 是否好友
          Integer isFriends1 = this.userRemote.getRelation(packet.userId, request.getUserId(), 0);
          if (isFriends1 != null) {
            if (isFriends1 == 1) {
              // 每日任务之好友PK
              this.taskHandler.handle(packet.userId, request.getGameCode());
            }
          }
          // 是否好友
          Integer isFriends2 = this.userRemote.getRelation(request.getUserId(), packet.userId, 0);
          if (isFriends2 != null) {
            if (isFriends2 == 1) {
              // 每日任务之好友PK
              this.taskHandler.handle(request.getUserId(), request.getGameCode());
            }
          }
          // 聊天状态 [0闲置中 1聊天中] 判断目标用户是否和当前用户在同一个聊天室
          Integer chatStatus = targetUserChatStatus(request.getUserId(), packet.userId);
          // 当前目标用户在聊天室内
          if (chatStatus != 0) {
            // 更新状态
            objectMap.put("recordId", request.getRecordId());
            objectMap.put("acceptStatus", 1);
            this.chatRemote.updateChatMessageInvite(objectMap);
            // 获得房间ID
            long roomId = GameUtil.getRandomRoomNo();
            boolean isExists = !this.redisUtils.hasKey(GameRedisKey.KEY_GAME_ROOM_RECORD + roomId);
            while (!isExists) {
              roomId = GameUtil.getRandomRoomNo();
              isExists = !this.redisUtils.hasKey(GameRedisKey.KEY_GAME_ROOM_RECORD + roomId);
            }
            I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
            joinInfo.setRoomId(roomId).setGameId(request.getGameCode());
            byte[] joinByte = joinInfo.build().toByteArray();
            // 当前玩家
            this.redisUtils.setByte(GameRedisKey.KEY_GAME_JOIN_RECORD + packet.userId, joinByte);
            // 目标玩家
            this.redisUtils.setByte(GameRedisKey.KEY_GAME_JOIN_RECORD + request.getUserId(), joinByte);
            I10001.RoomRecord.Builder roomRecord = I10001.RoomRecord.newBuilder();
            roomRecord.setGameId(request.getGameCode())
                .setRoomId(roomId)
                .setOpenWay(0)
                .setRoomType(0)
                .setGameMode(0)
                .setSpeakMode(1)
                .setGameNumber(0)
                .setGameSession(0);
            this.redisUtils.setByte(GameRedisKey.KEY_GAME_ROOM_RECORD + roomId, roomRecord.build().toByteArray());
            // 转发数据
            sendBuilder.setRoomId(roomId)
                .setGameCode(request.getGameCode())
                .setMessageId(request.getMessageId());
            targetChannel.writeAndFlush(
                new Packet(ChannelSet.CMD_CHAT, ChatActionSet.RECEIVE_GAME_DATA,
                    sendBuilder.build().toByteArray()));
            builder.setResult(0)
                .setRoomId(roomId)
                .setMessageId(request.getMessageId());
            channel.writeAndFlush(
                new Packet(ChannelSet.CMD_CHAT, ChatActionSet.ACCEPT_INVITE,
                    builder.build().toByteArray()));
          } else {
            cancelBattle(channel, packet);
          }
        } else {
          cancelBattle(channel, packet);
        }
      } else {
        builder.setResult(1)
            .setRoomId(0)
            .setMessageId(request.getMessageId());
        channel.writeAndFlush(
            new Packet(ChannelSet.CMD_CHAT, ChatActionSet.ACCEPT_INVITE,
                builder.build().toByteArray()));
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 取消约战. 
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  private void cancelBattle(Channel channel, Packet packet) {
    try {
      C10002.C100025c2s request = C10002.C100025c2s.parseFrom(packet.bytes);
      C10002.C100025s2c.Builder builder = C10002.C100025s2c.newBuilder();
      Map<String, Object> objectMap = Maps.newHashMap();
      builder.setResult(1).setRoomId(0).setMessageId(request.getMessageId());
      channel.writeAndFlush(
          new Packet(ChannelSet.CMD_CHAT, ChatActionSet.ACCEPT_INVITE,
              builder.build().toByteArray()));
      objectMap.put("recordId", request.getRecordId());
      objectMap.put("inviteStatus", 1);
      // 取消约战
      this.chatRemote.updateChatMessageInvite(objectMap);
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 离开聊天. 
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author WangCaiWen
   * @since 2020/7/21 - 2020/7/21
   */
  @Override
  public void leaveChatRoom(Channel channel, Packet packet) {
    try {
      C10002.C100027c2s request = C10002.C100027c2s.parseFrom(packet.bytes);
      Map<String, Object> newResult = Maps.newHashMap();
      if (request.getSort() == 1) {
        // 刷新状态
        singleChatStatus(packet.userId, request.getTargetId(), LEAVE_CHAT);
        // 移除redis缓存
        this.redisUtils.del(ChatRedisKey.KEY_CHAT_LOGIN + packet.userId);
        // 判断邀请Key
        if (this.redisUtils.hasKey(ChatRedisKey.KEY_CHAT_GAME_INVITE + packet.userId)) {
          byte[] bytes = this.redisUtils.getByte(ChatRedisKey.KEY_CHAT_GAME_INVITE + packet.userId);
          this.redisUtils.del(ChatRedisKey.KEY_CHAT_GAME_INVITE + packet.userId);
          D10001.InviteRecord inviteRecord = D10001.InviteRecord.parseFrom(bytes);
          if (CollectionUtils.isNotEmpty(inviteRecord.getRecordIdList())) {
            // 转发消息
            C10002.C100028s2c.Builder builder = C10002.C100028s2c.newBuilder();
            builder.setTargetId(request.getTargetId());
            builder.addAllRecordIdList(inviteRecord.getRecordIdList());
            ChannelManager.sendPacketToUserId(
                new Packet(ChannelSet.CMD_CHAT, ChatActionSet.CANCEL_INVITE,
                    builder.build().toByteArray()), request.getTargetId());
            newResult.put("recordIdList", inviteRecord.getRecordIdList());
            // 取消约战
            this.chatRemote.updateInviteStatus(newResult);
          }
        }
      } else {
        ChannelManager.removeChatChannel(request.getGroupId(), channel);
        // 刷新状态
        groupChatStatus(request.getGroupId(), packet.userId, LEAVE_CHAT);
        this.redisUtils.del(ChatRedisKey.KEY_CHAT_GROUP_LOGIN + packet.userId);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 点击进入. 
   *
   * @param channel 快速通道
   * @param packet 客户端数据
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  @Override
  public void clickToEnter(Channel channel, Packet packet) {
    try {
      C10002.C100029c2s request = C10002.C100029c2s.parseFrom(packet.bytes);
      C10002.C100029s2c.Builder builder = C10002.C100029s2c.newBuilder();
      Result result = this.chatRemote.clickToEnter(request.getRecordId());
      Integer isOk = result.getCode().equals(0) ? (Integer) result.getData() : null;
      if (isOk != null && isOk > 0) {
        if (isOk == 1) {
          builder.setResult(0).setRecordId(request.getRecordId());
          channel.writeAndFlush(
              new Packet(ChannelSet.CMD_CHAT, ChatActionSet.CLICK_ENTER,
                  builder.build().toByteArray()));
        } else {
          builder.setResult(2).setRecordId(request.getRecordId());
          channel.writeAndFlush(
              new Packet(ChannelSet.CMD_CHAT, ChatActionSet.CLICK_ENTER,
                  builder.build().toByteArray()));
        }
      } else {
        builder.setResult(1).setRecordId(request.getRecordId());
        channel.writeAndFlush(
            new Packet(ChannelSet.CMD_CHAT, ChatActionSet.CLICK_ENTER,
                builder.build().toByteArray()));
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 发送邀请.
   *
   * @param params 邀请信息
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  @Override
  public Result sendInvite(Map<String, Object> params) {
    long targetId = ((Number) params.get("targetId")).longValue();
    long userId = ((Number) params.get("userId")).longValue();
    Result result0 = this.chatRemote.userRelationIsExist(targetId, userId);
    Integer isExist = result0.getCode().equals(0) ? (Integer) result0.getData() : null;
    if (isExist != null) {
      // 是否拉黑
      Integer relation = this.userRemote.getRelation(targetId, userId, 1);
      if (relation != null) {
        if (relation == 0) {
          DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
          Map<String, Object> message = Maps.newHashMap();
          message.put("userId", userId);
          message.put("targetId", targetId);
          String messageId = userId + "_to_" + targetId + "_" + System.currentTimeMillis();
          message.put("messageId", messageId);
          message.put("messageType", 5);
          message.put("sort", 1);
          message.put("roomId", params.get("roomId"));
          message.put("inviteId", params.get("gameCode"));
          message.put("inviteTitle", params.get("gameName"));
          message.put("inviteImageUrl", params.get("gameCover"));
          message.put("sendTime", dtf.format(LocalDateTime.now()));
          Channel targetChannel = ChannelManager.getChannel(targetId);
          // 目标 在线/离线 && 目标连接服务 -> 判断网络连接（活跃状态）
          if (targetChannel != null && targetChannel.isActive()) {
            // 聊天状态 [0闲置中 1聊天中] 判断目标用户是否和当前用户在同一个聊天室
            Integer chatStatus = targetUserChatStatus(targetId, userId);
            // 当前目标用户在聊天室内
            if (chatStatus != 0) {
              Long recordId = resultRecordId(message, 1);
              if (recordId > 0) {
                // 发送消息
                message.put("recordId", recordId);
                message.put("isAction", 1);
                forwardMessage(targetChannel, message);
                // 更新列表 0 未读 1 已读
                updateNoticeList(message, 1);
                return Result.success();
              } else {
                return Result.error(ErrorCode.NETWORK_ERROR);
              }
            } else {
              Long recordId = resultRecordId(message, 0);
              if (recordId > 0) {
                // 发送通知
                this.warnHandler.sendAloneNoticeMessage(message);
                message.put("recordId", recordId);
                // 更新列表  0 未读 1 已读
                updateNoticeList(message, 0);
                return Result.success();
              } else {
                return Result.error(ErrorCode.NETWORK_ERROR);
              }
            }
          }
          // 离线消息
          Long recordId = resultRecordId(message, 0);
          if (recordId > 0) {
            message.put("recordId", recordId);
            // 更新列表  0 未读 1 已读
            updateNoticeList(message, 0);
          } else {
            return Result.error(ErrorCode.NETWORK_ERROR);
          }
          return Result.success();
        } else {
          return Result.error(-1, "邀请失败！对方拒收消息！");
        }
      }
    }
    return Result.error(-1, "发送失败！");
  }

  /**
   * TODO 刷新状态. 单聊
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @param status 聊天状态
   * @author WangCaiWen
   * @since 2020/7/24 - 2020/7/24
   */
  private void singleChatStatus(Long userId, Long targetId, Integer status) {
    try {
      Map<String, Object> refreshObject = Maps.newHashMap();
      refreshObject.put("userId", userId);
      refreshObject.put("targetId", targetId);
      refreshObject.put("chatStatus", status);
      this.chatRemote.updateChatStatus(refreshObject);
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 刷新状态. 群聊
   *
   * @param groupId 群聊ID
   * @param userId 用户ID
   * @param status 聊天状态
   * @author WangCaiWen
   * @since 2020/7/24 - 2020/7/24
   */
  private void groupChatStatus(Long groupId, Long userId, Integer status) {
    try {
      Map<String, Object> refreshObject = Maps.newHashMap();
      refreshObject.put("groupId", groupId);
      refreshObject.put("userId", userId);
      refreshObject.put("chatStatus", status);
      this.chatRemote.updateGroupChat(refreshObject);
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 聊天状态. 单聊
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @return 聊天状态
   * @author WangCaiWen
   * @date 2020/7/24
   */
  private Integer targetUserChatStatus(Long userId, Long targetId) {
    Result result = this.chatRemote.getUserChatStatus(userId, targetId);
    // 服务调不通  -> 默认为离线
    return result.getCode().equals(0) ? (Integer) result.getData() : 0;
  }

  /**
   * TODO 聊天状态. 群聊
   *
   * @param groupId 群聊ID
   * @param userId 用户ID
   * @return java.lang.Integer
   * @author wangcaiwen|1443710411@qq.com
   * @since 2020/8/11 15:11
   * @date 2020/8/11 15:11
   */
  private Integer resultChatStatusGroup(Long groupId, Long userId) {
    Result result = this.chatRemote.getUserGroupStatus(groupId, userId);
    // 服务调不通  -> 默认为离线
    return result.getCode().equals(0) ? (Integer) result.getData() : 0;
  }

  /**
   * TODO 记录编码. 单聊
   *
   * @param message 发送信息
   * @param readStatus 阅读状态
   * @return 记录ID
   * @author WangCaiWen
   * @date 2020/7/24
   */
  private Long resultRecordId(Map<String, Object> message, Integer readStatus) {
    message.put("messageRead", readStatus);
    Result result = this.chatRemote.insChatMessage(message);
    return result.getCode().equals(0) ? ((Number) result.getData()).longValue() : 0;
  }

  /**
   * TODO 记录编码. 群聊
   *
   * @param message 发送信息
   * @return 记录ID
   * @author wangcaiwen|1443710411@qq.com
   * @since 2020/8/11 14:10
   * @date 2020/8/11 14:10
   */
  private Long resultRecordId(Map<String, Object> message) {
    Result result = this.chatRemote.newGroupMessage(message);
    return result.getCode().equals(0) ? ((Number) result.getData()).longValue() : 0;
  }

  /**
   * TODO 更新通知. 单聊
   *
   * @param message 发送信息
   * @param readType 阅读标记
   * @author WangCaiWen
   * @date 2020/7/24
   */
  private void updateNoticeList(Map<String, Object> message, Integer readType) {
    Map<String, Object> result = Maps.newHashMap();
    result.put("userId", message.get("userId"));
    result.put("targetId", message.get("targetId"));
    result.put("recordId", message.get("recordId"));
    result.put("readType", readType);
    result.put("sendTime", message.get("sendTime"));
    this.chatRemote.updateChatListMessage(result);
  }

  /**
   * TODO 文件上传.
   *
   * @param request 文件信息
   * @return 上传回调
   * @author WangCaiWen
   * @date 2020/7/24
   */
  private Map<String, Object> fileUpload(C10002.C100021c2s request) {
    Map<String, Object> fileResult = null;
    try {
      String uuid = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
      switch (request.getMessageType()) {
        case 2:
          C10002.ESImageInfo imageInfo = request.getImageInfo();
          String imageExt = "." + imageInfo.getImageExt();
          byte[] imageByte = imageInfo.getImageFile().toByteArray();
          InputStream inputStreamImage = new ByteArrayInputStream(imageByte);
          MultipartFile imageFile = new FeignMultipartFile("file", uuid + imageExt,
              ContentType.APPLICATION_OCTET_STREAM.toString(), inputStreamImage);
          Result imageResult = this.uploadRemote.uploadChatFile(imageFile, StringUtils.nvl(imageInfo.getHeight()),
              StringUtils.nvl(imageInfo.getWidth()), "chat/");
          PicInfo imagePicInfo = imageResult.getCode().equals(0) ? JsonUtils.toObjectPojo(imageResult.getData(), PicInfo.class) : null;
          if (imagePicInfo != null) {
            fileResult = Maps.newHashMap();
            fileResult.put("fileUrl", imagePicInfo.getPicUrl());
          }
          break;
        case 3:
          C10002.ESVoiceInfo voiceInfo = request.getVoiceInfo();
          String voiceExt = "." + voiceInfo.getVoiceExt();
          byte[] voiceByte = voiceInfo.getVoiceFile().toByteArray();
          InputStream inputStreamVoice = new ByteArrayInputStream(voiceByte);
          MultipartFile voiceFile = new FeignMultipartFile("file", uuid + voiceExt,
              ContentType.APPLICATION_OCTET_STREAM.toString(), inputStreamVoice);
          Result voiceResult = this.uploadRemote.uploadChatFile(voiceFile, "", "", "chat/");
          PicInfo voicePicInfo = voiceResult.getCode().equals(0) ? JsonUtils.toObjectPojo(voiceResult.getData(), PicInfo.class) : null;
          if (voicePicInfo != null) {
            fileResult = Maps.newHashMap();
            fileResult.put("fileUrl", voicePicInfo.getPicUrl());
          }
          break;
        default:
          C10002.ESVideoInfo videoInfo = request.getVideoInfo();
          String videoExt = "." + videoInfo.getVideoExt();
          byte[] videoByte = videoInfo.getVideoFile().toByteArray();
          String coverExt = "." + videoInfo.getCoverExt();
          byte[] coverByte = videoInfo.getCoverFile().toByteArray();
          InputStream inputStreamVideo = new ByteArrayInputStream(videoByte);
          InputStream inputStreamCover = new ByteArrayInputStream(coverByte);
          MultipartFile videoFile = new FeignMultipartFile("file", uuid + videoExt,
              ContentType.APPLICATION_OCTET_STREAM.toString(), inputStreamVideo);
          uuid = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
          MultipartFile coverFile = new FeignMultipartFile("file", uuid + coverExt,
              ContentType.APPLICATION_OCTET_STREAM.toString(), inputStreamCover);
          Result videoResult = this.uploadRemote.uploadChatFile(videoFile, StringUtils.nvl(videoInfo.getHeight()),
              StringUtils.nvl(videoInfo.getWidth()), "chat/");
          Result coverResult = this.uploadRemote.uploadChatFile(coverFile, "", "", "chat/");
          PicInfo videoPicInfo = videoResult.getCode().equals(0) ? JsonUtils.toObjectPojo(videoResult.getData(), PicInfo.class) : null;
          PicInfo coverPicInfo = coverResult.getCode().equals(0) ? JsonUtils.toObjectPojo(coverResult.getData(), PicInfo.class) : null;
          if (videoPicInfo != null && coverPicInfo != null) {
            fileResult = Maps.newHashMap();
            fileResult.put("fileUrl", videoPicInfo.getPicUrl());
            fileResult.put("coverUrl", coverPicInfo.getPicUrl());
          }
          break;
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
    return fileResult;
  }

  /**
   * TODO 表情信息.
   *
   * @param result [聊天信息]
   * @return [表情信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 12:57
   * @update 2020/11/10 12:57
   */
  private C10002.ESEmojiInfo.Builder emojiBuilder(Map<String, Object> result) {
    C10002.ESEmojiInfo.Builder emojiInfo = C10002.ESEmojiInfo.newBuilder();
    if (Objects.nonNull(result)) {
      emojiInfo.setEmName(StringUtils.nvl(result.get("emoji")));
      emojiInfo.setAnimURL(StringUtils.nvl(result.get("emojiUrl")));
    }
    return emojiInfo;
  }

  /**
   * TODO 图片信息.
   *
   * @param result 图片信息
   * @return ESImageInfo
   * @author WangCaiWen
   * @since 2020/7/21
   */
  private C10002.ESImageInfo.Builder imageBuilder(Map<String, Object> result) {
    C10002.ESImageInfo.Builder imageInfo = C10002.ESImageInfo.newBuilder();
    if (result != null) {
      imageInfo.setWidth((Integer) result.get("fileWidth"));
      imageInfo.setHeight((Integer) result.get("fileHeight"));
      imageInfo.setImageURL(StringUtils.nvl(result.get("fileUrl")) + "?p=0");
      imageInfo.setLitimg(StringUtils.nvl(result.get("fileUrl")) + "?w=400");
    }
    return imageInfo;
  }

  /**
   * TODO 语音信息. 
   *
   * @param result 语音信息
   * @return ESVoiceInfo
   * @author WangCaiWen
   * @since 2020/7/21
   */
  private C10002.ESVoiceInfo.Builder voiceBuilder(Map<String, Object> result) {
    C10002.ESVoiceInfo.Builder voiceInfo = C10002.ESVoiceInfo.newBuilder();
    voiceInfo.setDuration((int) result.get("fileDuration"));
    voiceInfo.setVoiceURL(StringUtils.nvl(result.get("fileUrl")));
    return voiceInfo;
  }

  /**
   * TODO 视频信息. 
   *
   * @param result 视频信息
   * @return ESVideoInfo
   * @author WangCaiWen
   * @date 2020/7/24
   */
  private C10002.ESVideoInfo.Builder videoBuilder(Map<String, Object> result) {
    C10002.ESVideoInfo.Builder videoInfo = C10002.ESVideoInfo.newBuilder();
    videoInfo.setWidth((int) result.get("fileWidth"));
    videoInfo.setHeight((int) result.get("fileHeight"));
    videoInfo.setDuration((int) result.get("fileDuration"));
    videoInfo.setVideoURL(StringUtils.nvl(result.get("fileUrl")));
    videoInfo.setCoverURL(StringUtils.nvl(result.get("coverUrl")));
    return videoInfo;
  }

  /**
   * TODO 邀请信息. 
   *
   * @param result 邀请信息
   * @return ESInviteInfo
   * @author WangCaiWen
   * @date 2020/7/24
   */
  private C10002.ESInviteInfo.Builder inviteBuilder(Map<String, Object> result) {
    C10002.ESInviteInfo.Builder inviteInfo = C10002.ESInviteInfo.newBuilder();
    inviteInfo.setSort((Integer) result.get("sort"));
    // 房间ID
    if (result.containsKey("roomId")) {
      inviteInfo.setRoomId(((Number) result.get("roomId")).longValue());
    }
    inviteInfo.setGameCode(((Number) result.get("inviteId")).longValue());
    inviteInfo.setInviteImageURL(StringUtils.nvl(result.get("inviteImageUrl")));
    inviteInfo.setInviteTitle(StringUtils.nvl(result.get("inviteTitle")));
    inviteInfo.setInviteStatus(0);
    inviteInfo.setAcceptStatus(0);
    return inviteInfo;
  }

  /**
   * TODO 邀请缓存. 
   *
   * @param userId 用户ID
   * @param recordId 消息的记录ID
   * @author WangCaiWen
   * @since 2020/7/27 - 2020/7/27
   */
  private void addInviteList(Long userId, Long recordId) {
    try {
      D10001.InviteRecord.Builder builder = D10001.InviteRecord.newBuilder();
      if (this.redisUtils.hasKey(ChatRedisKey.KEY_CHAT_GAME_INVITE + userId)) {
        // 更新
        byte[] bytes = this.redisUtils.getByte(ChatRedisKey.KEY_CHAT_GAME_INVITE + userId);
        this.redisUtils.del(ChatRedisKey.KEY_CHAT_GAME_INVITE + userId);
        D10001.InviteRecord inviteRecord = D10001.InviteRecord.parseFrom(bytes);
        builder.addAllRecordId(inviteRecord.getRecordIdList());
        builder.addRecordId(recordId);
        this.redisUtils.setByte(ChatRedisKey.KEY_CHAT_GAME_INVITE + userId, builder.build().toByteArray());
      } else {
        // 添加
        builder.addRecordId(recordId);
        String redisKey = ChatRedisKey.KEY_CHAT_GAME_INVITE + userId;
        this.redisUtils.setByte(redisKey, builder.build().toByteArray());
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 文件信息. 
   *
   * @param request 客户端数据
   * @param fileResult 文件上传返回信息
   * @return 文件信息
   * @author WangCaiWen
   * @date 2020/7/24
   */
  private Map<String, Object> messageTypeSorting(C10002.C100021c2s request, Map<String, Object> fileResult) {
    Map<String, Object> message = Maps.newHashMap();
    // 消息类型 「0-文本 1-Emoji 2-图片 3-语音 4-视频 5-游戏邀请·单聊」
    int messageType = request.getMessageType();
    switch (messageType) {
      case 1:
        C10002.ESEmojiInfo emojiInfo = request.getEmojiInfo();
        message.put("emoji", emojiInfo.getEmName());
        message.put("emojiUrl", emojiInfo.getAnimURL());
        break;
      case 2:
        C10002.ESImageInfo imageInfo = request.getImageInfo();
        message.put("fileWidth", imageInfo.getWidth());
        message.put("fileHeight", imageInfo.getHeight());
        message.put("fileUrl", fileResult.get("fileUrl"));
        break;
      case 3:
        C10002.ESVoiceInfo voiceInfo = request.getVoiceInfo();
        message.put("fileUrl", fileResult.get("fileUrl"));
        message.put("fileDuration", voiceInfo.getDuration());
        break;
      case 4:
        C10002.ESVideoInfo videoInfo = request.getVideoInfo();
        message.put("fileWidth", videoInfo.getWidth());
        message.put("fileHeight", videoInfo.getHeight());
        message.put("fileUrl", fileResult.get("fileUrl"));
        message.put("coverUrl", fileResult.get("coverUrl"));
        message.put("fileDuration", videoInfo.getDuration());
        break;
      default:
        message.put("message", request.getMessage());
        break;
    }
    return message;
  }

  /**
   * TODO 聚合消息. 
   *
   * @param message 发送信息
   * @param userInfo 用户信息
   * @return 聚合结果
   * @author wangcaiwen|1443710411@qq.com
   * @since 2020/8/11 14:50
   * @date 2020/8/11 14:50
   */
  private C10002.C100022s2c.Builder aggregateMessage(Map<String, Object> message, D10001.ESUserInfo userInfo) {
    C10002.ESUserInfo.Builder sendUser = C10002.ESUserInfo.newBuilder();
    sendUser.setUserId(userInfo.getUserId());
    sendUser.setAlias(userInfo.getAlias());
    sendUser.setIconURL(userInfo.getIconURL());
    sendUser.setIconFrame(userInfo.getIconFrame());
    sendUser.setChatFrame(userInfo.getChatFrame());
    sendUser.setChatFrameAttribute(userInfo.getChatFrameAttribute());
    C10002.C100022s2c.Builder sendBuilder = C10002.C100022s2c.newBuilder();
    sendBuilder.setMessageOwner(sendUser);
    sendBuilder.setRecordId(((Number) message.get("recordId")).longValue());
    sendBuilder.setGroupId(((Number) message.get("groupId")).longValue());
    sendBuilder.setUserId(((Number) message.get("sendId")).longValue());
    sendBuilder.setMessageType((Integer) message.get("messageType"));
    sendBuilder.setIsAction((int) message.get("isAction"));
    switch (sendBuilder.getMessageType()) {
      case 1:
        sendBuilder.setEmojiInfo(emojiBuilder(message));
        break;
      case 2:
        sendBuilder.setImageInfo(imageBuilder(message));
        break;
      case 3:
        sendBuilder.setVoiceInfo(voiceBuilder(message));
        break;
      case 4:
        sendBuilder.setVideoInfo(videoBuilder(message));
        break;
      default:
        sendBuilder.setMessage(StringUtils.nvl(message.get("message")));
        break;
    }
    // 聊天分类 【1 单聊 2 群聊】
    sendBuilder.setSort(2);
    sendBuilder.setMessageId(StringUtils.nvl(message.get("messageId")));
    sendBuilder.setMessageSort(0);
    sendBuilder.setCreateTime(StringUtils.nvl(message.get("sendTime")));
    return sendBuilder;
  }

  /**
   * TODO 转发消息. 单聊
   *
   * @param channel 快速通道
   * @param message 发送信息
   * @author WangCaiWen
   * @date 2020/7/24
   */
  private void forwardMessage(Channel channel, Map<String, Object> message) {
    if (channel.isActive()) {
      logger.info("<== 10002 [发送消息.目标状态-活跃] Target: [{}], Channel：{}", message.get("targetId"), channel);
      C10002.C100022s2c.Builder sendBuilder = C10002.C100022s2c.newBuilder();
      sendBuilder.setRecordId(((Number) message.get("recordId")).longValue());
      sendBuilder.setUserId(((Number) message.get("userId")).longValue());
      sendBuilder.setMessageId(StringUtils.nvl(message.get("messageId")));
      sendBuilder.setMessageType((int) message.get("messageType"));
      switch (sendBuilder.getMessageType()) {
        // Emoji
        case 1:
          sendBuilder.setEmojiInfo(emojiBuilder(message));
          break;
        // 图片
        case 2:
          sendBuilder.setImageInfo(imageBuilder(message));
          break;
        // 语音
        case 3:
          sendBuilder.setVoiceInfo(voiceBuilder(message));
          break;
        // 视频
        case 4:
          sendBuilder.setVideoInfo(videoBuilder(message));
          break;
        // 游戏邀请
        case 5:
          sendBuilder.setInviteInfo(inviteBuilder(message));
          break;
        // 文本
        default:
          sendBuilder.setMessage(StringUtils.nvl(message.get("message")));
          break;
      }
      // 聊天分类 1-单聊 2-群聊
      sendBuilder.setSort(1);
      sendBuilder.setIsAction((int) message.get("isAction"));
      sendBuilder.setCreateTime(StringUtils.nvl(message.get("sendTime")));
      channel.writeAndFlush(
          new Packet(ChannelSet.CMD_CHAT, ChatActionSet.RECEIVE_MESSAGE,
              sendBuilder.build().toByteArray()));
      logger.info("<== 10002 [发送消息.发送成功] Target: [{}]", message.get("targetId"));
    } else {
      logger.info("<== 10002 [发送消息.目标状态-不活跃] Target: [{}], Channel：{}", message.get("targetId"), channel);
      logger.info("<== 10002 [发送消息.发送失败] Target: [{}]", message.get("targetId"));
    }
  }

  /**
   * TODO 发送成功.
   *
   * @param channel 快速通道
   * @param request 客户端信息
   * @param recordId 记录ID
   * @author wangcaiwen|1443710411@qq.com
   * @since 2020/8/11 14:16
   * @date 2020/8/11 14:16
   */
  private void sendSuccess(Channel channel, C10002.C100021c2s request, Long recordId) {
    C10002.C100021s2c.Builder builder = C10002.C100021s2c.newBuilder();
    builder.setResult(0).setRecordId(recordId).setMessageId(request.getMessageId());
    channel.writeAndFlush(
        new Packet(ChannelSet.CMD_CHAT, ChatActionSet.SEND_MESSAGE,
            builder.build().toByteArray()));
  }

  /**
   * TODO 发送失败.
   *
   * @param channel 快速通道
   * @param request 客户端信息
   * @author wangcaiwen|1443710411@qq.com
   * @since 2020/8/11 14:16
   * @date 2020/8/11 14:16
   */
  private void sendFailed(Channel channel, C10002.C100021c2s request) {
    C10002.C100021s2c.Builder builder = C10002.C100021s2c.newBuilder();
    builder.setResult(1).setRecordId(0).setMessageId(request.getMessageId());
    channel.writeAndFlush(
        new Packet(ChannelSet.CMD_CHAT, ChatActionSet.SEND_MESSAGE,
            builder.build().toByteArray()));
  }

  /**
   * TODO 数据缺失.
   *
   * @param channel [通讯管道]
   * @param request [客户端数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/19 9:35
   * @update 2020/10/19 9:35
   */
  private void dataMissing(Channel channel, C10002.C100021c2s request) {
    C10002.C100021s2c.Builder builder = C10002.C100021s2c.newBuilder();
    builder.setResult(2).setRecordId(0).setMessageId(request.getMessageId());
    channel.writeAndFlush(
        new Packet(ChannelSet.CMD_CHAT, ChatActionSet.SEND_MESSAGE,
            builder.build().toByteArray()));
  }

}
