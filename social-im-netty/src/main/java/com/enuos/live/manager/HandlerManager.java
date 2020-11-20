package com.enuos.live.manager;

import com.enuos.live.action.*;
import com.enuos.live.constants.ChatRedisKey;
import com.enuos.live.constants.RedisKey;
import com.enuos.live.core.NettyCtxGroup;
import com.enuos.live.core.Packet;
import com.enuos.live.handler.ChatHandler;
import com.enuos.live.handler.PetsHandler;
import com.enuos.live.handler.VoiceHandler;
import com.enuos.live.proto.c10000msg.C10000;
import com.enuos.live.proto.c10002msg.C10002;
import com.enuos.live.proto.d10001msg.D10001;
import com.enuos.live.rest.ChatRemote;
import com.enuos.live.rest.RoomRemote;
import com.enuos.live.util.RedisUtils;
import com.enuos.live.utils.ExceptionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.Channel;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * TODO 操作分发.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020/3/11 - 2020/7/29
 */

@Slf4j
@Component
public class HandlerManager {

    @Resource
    private VoiceHandler voiceHandler;
    @Resource
    private ChatHandler chatHandler;
    @Resource
    private ChatRemote chatRemote;
    @Resource
    private RoomRemote roomRemote;
    @Resource
    private PetsHandler petsHandler;

    @Value("${room.expTime:1500}")
    private long expTime;
    /**
     * Redis工具类
     */
    @Resource
    private RedisUtils redisUtils;

    /**
     * TODO 心跳/登录.
     *
     * @param channel 快速通道
     * @param packet  客户端数据
     * @author WangCaiWen
     * @date 2020/7/29
     */
    public void doHeart(Channel channel, Packet packet) {
        switch (packet.child) {
            // 心跳处理
            case HeartActionSet.HEART:
                channel.writeAndFlush(new Packet(ChannelSet.CMD_HEART, HeartActionSet.HEART, null));
                break;
            case HeartActionSet.USER:
                log.info("LOGIN SERVER -> MEMBER: [{}] -> TIME: [{}]", packet.userId, LocalDateTime.now());
                if (!validateLogon(channel, packet)) {
                    return;
                }
                Channel oldChannel = ChannelManager.getChannel(packet.userId);
                if (oldChannel != null) {
                    ChannelManager.removeChannel(oldChannel);
                }
                ChannelManager.addChannel(packet.userId, channel);
                channel.writeAndFlush(new Packet(ChannelSet.CMD_HEART, HeartActionSet.USER, null));
                try {
                    C10000.C100001c2s request = C10000.C100001c2s.parseFrom(packet.bytes);
                    int identity = request.getIdentity();
                    // 身份 0 普通用户 1 会员用户
                    if (identity == 1) {
                        // 存入会员数据
                        NettyCtxGroup.vipGroup.add(channel);
                    }
                } catch (Exception e) {
                    log.error("HandlerManager.doHeart [错误信息: {}]", e.getMessage());
                    log.error(ExceptionUtil.getStackTrace(e));
                }
                break;
            default:
                log.warn("Unsupported CMD [{}]", packet.child);
                break;
        }
    }

    /**
     * 登录验证
     *
     * @param channel
     * @param packet
     * @return
     */
    public boolean validateLogon(Channel channel, Packet packet) {
        try {
            C10000.C100001c2s request = C10000.C100001c2s.parseFrom(packet.bytes);
            String deviceId = request.getDeviceId();
            String token = request.getToken();
            long userId = packet.getUserId();
            log.info("登录校验参数, userId=[{}], deviceId=[{}], token=[{}]", userId, deviceId, token);
            if (token.equals(redisUtils.get(RedisKey.KEY_TOKEN + userId))) {
                log.info("登录成功, userId=[{}]", userId);
                voiceHandler.singleSignOn(userId, deviceId, channel);
                return true;
            }
            log.info("登录失败, userId=[{}]", userId);
            C10000.C100002s2c.Builder newBuilder = C10000.C100002s2c.newBuilder();
            channel.writeAndFlush(new Packet(ChannelSet.CMD_HEART, VoiceActionSet.SEND_MESSAGE, newBuilder.build().toByteArray()));

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * TODO 单聊&群聊.
     *
     * @param channel 快速通道
     * @param packet  客户端数据
     * @author WangCaiWen
     * @date 2020/7/29
     */
    public void doChat(Channel channel, Packet packet) {
        // 子消息号
        switch (packet.child) {
            // 进入聊天
            case ChatActionSet.ENTER_CHAT_ROOM:
                this.chatHandler.enterChatRoom(channel, packet);
                break;
            // 发送消息
            case ChatActionSet.SEND_MESSAGE:
                this.chatHandler.sendMessage(channel, packet);
                break;
            // 游戏邀请
            case ChatActionSet.SEND_GAME_INVITE:
                this.chatHandler.sendGameInvite(channel, packet);
                break;
            // 聆听语音
            case ChatActionSet.LISTEN_VOICE:
                this.chatHandler.listenVoice(channel, packet);
                break;
            // 接受邀请
            case ChatActionSet.ACCEPT_INVITE:
                this.chatHandler.acceptInvite(channel, packet);
                break;
            // 离开聊天
            case ChatActionSet.LEAVE_CHAT_ROOM:
                this.chatHandler.leaveChatRoom(channel, packet);
                break;
            // 点击进入
            case ChatActionSet.CLICK_ENTER:
                this.chatHandler.clickToEnter(channel, packet);
                break;
            default:
                log.warn("Unsupported CMD [{}]", packet.child);
                break;
        }
    }

    /**
     * @MethodName: doVoice
     * @Description: TODO 语音房操作
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 15:46 2020/7/1
     **/
    public void handleVoice(Channel channel, Packet packet) {
        switch (packet.getChild()) {
            case VoiceHandleSet.ACCOMPANY_PLAY_STATUS: // 伴奏播放状态
                this.voiceHandler.accompanyPlayStatus(channel, packet);
                break;
            case VoiceHandleSet.RED_PACKETS: // 红包
                this.voiceHandler.redPackets(channel, packet);
                break;
            case VoiceHandleSet.GIVE_GIFT: // 送礼物
                this.voiceHandler.giveGift(channel, packet);
                break;
            case VoiceHandleSet.EMOJI: // 表情
                this.voiceHandler.emoji(channel, packet);
                break;
            case VoiceHandleSet.INTERACT_EMOJI: // 互动表情
                this.voiceHandler.interactEmoji(channel, packet);
                break;
            case VoiceHandleSet.LUCK_DRAW: // 抽奖
                this.voiceHandler.luckDraw(channel, packet);
                break;
            case VoiceHandleSet.RE_ENTER_ROOM: // 重回房间
                this.voiceHandler.reEnterRoom(channel, packet);
                break;
            case VoiceHandleSet.START_PK: // 发起PK
                this.voiceHandler.startPk(channel, packet);
                break;
            case VoiceHandleSet.PK_POLL: // 投票
                this.voiceHandler.pkPoll(channel, packet);
                break;
            case VoiceHandleSet.SET_ADMIN: // 设置管理员
                this.voiceHandler.setAdmin(channel, packet);
                break;
            default:
                log.warn("暂不支持的服务指令{}, handleVoice", packet.child);
                break;
        }

    }

    /**
     * @MethodName: doVoice
     * @Description: TODO 语音房
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 2020/5/14
     **/
    public void doVoice(Channel channel, Packet packet) {
        switch (packet.child) {
            case VoiceActionSet.START_BROADCAST: // 开播
                this.voiceHandler.startBroadcast(channel, packet);
                break;
            case VoiceActionSet.ENTER_ROOM: // 进入房间
                this.voiceHandler.enterRoom(channel, packet);
                break;
            case VoiceActionSet.SEND_MESSAGE: // 发送消息
                this.voiceHandler.sendMessage(channel, packet);
                break;
            case VoiceActionSet.UPDATE_ROOM: // 房间信息改变
                this.voiceHandler.updateRoom(channel, packet);
                break;
            case VoiceActionSet.SPEAK_STATUS: // 正在说话状态
                this.voiceHandler.speakStatus(channel, packet);
                break;
            case VoiceActionSet.END_BROADCAST: // 下播
                this.voiceHandler.endBroadcast(channel, packet);
                break;
            case VoiceActionSet.UP_MIC_LIST: // 排麦列表改变
                this.voiceHandler.upMicList(channel, packet);
                break;
            case VoiceActionSet.UP_ROOM_SEAT_LIST: // 座位信息改变
                this.voiceHandler.upRoomSeatList(channel, packet);
                break;
            case VoiceActionSet.EXIT_ROOM: // 退出房间
                this.voiceHandler.exitRoom(channel, packet);
                break;
            default:
                log.warn("暂不支持的服务指令{}", packet.child);
                break;
        }
    }

    /**
     * TODO 离开聊天.
     *
     * @param userId 用户ID
     * @author wangcaiwen|1443710411@qq.com
     * @date 2020/8/11 12:51
     * @since 2020/4/09 10:47
     */
    public void doLeaveChat(Long userId, Channel channel) {
      try {
        // 离开单聊
        if (redisUtils.hasKey(ChatRedisKey.KEY_CHAT_LOGIN + userId)) {
          // 游戏邀请
          if (redisUtils.hasKey(ChatRedisKey.KEY_CHAT_GAME_INVITE + userId)) {
            Map<String, Object> newResult = Maps.newHashMap();
            long targetId = (long) redisUtils.get(ChatRedisKey.KEY_CHAT_LOGIN + userId);
            byte[] bytes = redisUtils.getByte(ChatRedisKey.KEY_CHAT_GAME_INVITE + userId);
            // 移除缓存
            redisUtils.del(ChatRedisKey.KEY_CHAT_GAME_INVITE + userId);
            D10001.InviteRecord inviteRecord = D10001.InviteRecord.parseFrom(bytes);
            if (CollectionUtils.isNotEmpty(inviteRecord.getRecordIdList())) {
              List<Long> recordIdList = Lists.newArrayList();
              recordIdList.addAll(inviteRecord.getRecordIdList());
              // 转发消息
              C10002.C100028s2c.Builder builder = C10002.C100028s2c.newBuilder();
              builder.setTargetId(targetId);
              builder.addAllRecordIdList(recordIdList);
              ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_CHAT,
                  ChatActionSet.CANCEL_INVITE, builder.build().toByteArray()), targetId);
              newResult.put("recordIdList", recordIdList);
              // 取消约战
              this.chatRemote.updateInviteStatus(newResult);
            }
          }
          redisUtils.del(ChatRedisKey.KEY_CHAT_LOGIN + userId);
        }
        // 离开群聊
        if (redisUtils.hasKey(ChatRedisKey.KEY_CHAT_GROUP_LOGIN + userId)) {
          byte[] userByte = this.redisUtils.getByte(ChatRedisKey.KEY_CHAT_GROUP_LOGIN + userId);
          D10001.LoginGroup loginInfo = D10001.LoginGroup.parseFrom(userByte);
          ChannelManager.removeChatChannel(loginInfo.getGroupId(), channel);
          redisUtils.del(ChatRedisKey.KEY_CHAT_GROUP_LOGIN + userId);
        }
        // 离开服务
        this.chatRemote.leaveService(userId);
      } catch (Exception e) {
        log.error(e.getMessage());
        log.error(ExceptionUtil.getStackTrace(e));
      }
    }

//    public void roomLeave(Long userId) {
//        Integer roomId = (Integer) redisUtils.get(RedisKey.KEY_ROOM_STATE + userId); // 房间号
//        if (!Objects.isNull(roomId)) {
////            long expTime = 1500;// 秒
//            long time = (System.currentTimeMillis() / 1000) + expTime;
//            // 指定语音房缓存分值
//            redisUtils.zSet(RedisKey.KEY_ROOM_STATE, roomId, (double) time);
//            redisUtils.expire(RedisKey.KEY_ROOM_STATE + userId, expTime);
//            log.warn("房主 " + userId + " 离开房间, roomId=[{}]", roomId);
//        }
//    }

    /**
     * @MethodName: exceptionExitRoom
     * @Description: TODO 异常退出房间
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 15:50 2020/7/17
     **/
    public void exceptionExitRoom(Channel channel, Long userId) {

        Integer roomId = (Integer) redisUtils.get(RedisKey.KEY_ROOM_STATE + userId); // 根据userId房间号
        if (!Objects.isNull(roomId)) { // 房主
//            long expTime = 1500;// 秒
            long time = (System.currentTimeMillis() / 1000) + expTime;
            // 指定语音房缓存分值
            redisUtils.zSet(RedisKey.KEY_ROOM_STATE, roomId + "_" + userId, (double) time);
            redisUtils.expire(RedisKey.KEY_ROOM_STATE + userId, expTime);
            ChannelManager.removeVoiceChannel(Long.valueOf(roomId), channel);// 移除群组中的channel
            log.warn("房主离开房间, roomId=[{}], userId=[{}]", roomId, userId);
        } else { // 用户
            if (redisUtils.hasKey(RedisKey.KEY_ROOM_USER + userId)) {
                Long roomId1 = Long.valueOf(redisUtils.get(RedisKey.KEY_ROOM_USER + userId).toString());
                log.info("用户异常退出房间,roomId=[{}],userId=[{}]", roomId1, userId);
                voiceHandler.exceptionExitRoom(channel, roomId1, userId);
            }
        }
        redisUtils.del(RedisKey.KEY_ROOM_LOGIN + userId); // 删除客户端设备号
    }

    /**
     * @MethodName: doVoice
     * @Description: TODO 宠物
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 13:08 2020/8/24
     **/
    public void doPets(Channel channel, Packet packet) {
        switch (packet.child) {
            case PetsActionSet.P_PLAY: // 基础信息
                this.petsHandler.play(channel, packet);
                break;
            case PetsActionSet.P_OPERATION: // 操作
                this.petsHandler.operation(channel, packet);
                break;
            case PetsActionSet.P_FOOD: // 宠物喂食
                this.petsHandler.food(channel, packet);
                break;
            case PetsActionSet.P_TOYS: // 宠物玩具
                this.petsHandler.toys(channel, packet);
                break;
            default:
                log.warn("暂不支持的服务指令{}", packet.child);
                break;
        }
    }
}
