package com.enuos.live.handler.impl;

import cn.hutool.core.lang.ObjectId;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.enuos.live.action.ChannelSet;
import com.enuos.live.action.VoiceActionSet;
import com.enuos.live.action.VoiceHandleSet;
import com.enuos.live.component.FeignMultipartFile;
import com.enuos.live.constants.Constants;
import com.enuos.live.constants.RedisKey;
import com.enuos.live.core.NettyCtxGroup;
import com.enuos.live.core.Packet;
import com.enuos.live.handler.VoiceHandler;
import com.enuos.live.manager.ChannelManager;
import com.enuos.live.manager.HandlerManager;
import com.enuos.live.manager.TaskEnum;
import com.enuos.live.pojo.PicInfo;
import com.enuos.live.proto.c10000msg.C10000;
import com.enuos.live.proto.c10004msg.C10004;
import com.enuos.live.proto.c20001msg.C20001;
import com.enuos.live.proto.c20002msg.C20002;
import com.enuos.live.rest.RoomRemote;
import com.enuos.live.rest.UploadRemote;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.util.JsonUtils;
import com.enuos.live.util.RedisUtils;
import com.enuos.live.utils.StringUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @author WangCaiWen
 * Created on 2020/4/9 11:06
 */
@Slf4j
@Component
public class VoiceHandlerImpl implements VoiceHandler {

    @Resource
    private UserRemote userRemote;

    @Resource
    private RoomRemote roomRemote;

    @Resource
    private UploadRemote uploadRemote;

    @Resource
    private RedisUtils redisUtils;

    @Resource(name = "taskFxbDrawExecutor")
    ExecutorService executorService;

    private final HandlerManager handlerManager;

    // 文件大小
    @Value("${room.fileSize}")
    private double fileSize;
    private static final double mult = 1048576; //1MB

    // 消息最大长度
    @Value("${room.messageSize}")
    private int messageSize;


    public VoiceHandlerImpl(HandlerManager handlerManager) {
        super();
        this.handlerManager = handlerManager;
    }

    /**
     * @MethodName: startBroadcast
     * @Description: TODO 开播
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 2020/5/18
     **/
    @Override
    public void startBroadcast(Channel channel, Packet packet) {
        try {
            C20001.C200010c2s request = C20001.C200010c2s.parseFrom(packet.bytes);
            Long roomId = request.getRoomId();
            Long userId = packet.getUserId();
            log.info("开播room=[{}]", roomId);
            ChannelManager.addVoiceGroup(roomId, channel); // 更新创建群组
            if (roomId.toString().length() < 8) { // 电台房间号8位, 电台不做下线处理
                // 保存普通房间开播状态
                redisUtils.set(RedisKey.KEY_ROOM_STATE + userId, roomId);
            }
            redisUtils.sSet(RedisKey.KEY_ROOM_USER + roomId, userId); // 添加房间-用户缓存
            redisUtils.set(RedisKey.KEY_ROOM_USER + userId, roomId); // 添加用户-房间缓存
            int voiceGroupSize = ChannelManager.getVoiceGroupSize(roomId);
            log.info("房间人数=[{}],roomId=[{}]", voiceGroupSize, roomId);
            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE, VoiceActionSet.START_BROADCAST, request.toByteArray()), packet, roomId, channel);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

    /**
     * @MethodName: endBroadcast
     * @Description: TODO APP正常下播
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 2020/5/18
     **/
    @Override
    public void endBroadcast(Channel channel, Packet packet) {
        try {
            C20001.C200017c2s request = C20001.C200017c2s.parseFrom(packet.bytes);
            Long roomId = request.getRoomId();
            Long userId = packet.getUserId();

            log.info("APP下播roomId=[{}],userId=[{}]", roomId, userId);
            endRoom(roomId, userId);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * @MethodName: endBroadcast
     * @Description: TODO 异常下播
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 2020/9/24
     **/
    @Override
    public Result exceptionEndBroadcast(Long roomId, Long userId) {
        log.info("异常下播roomId=[{}],userId=[{}]", roomId, userId);
        endRoom(roomId, userId);
        return Result.success();
    }

    /**
     * @MethodName: endRoom
     * @Description: TODO 房间下播方法
     * @Param: [roomId:房间号]
     * @Return: void
     * @Author: xubin
     * @Date: 14:56 2020/9/24
     **/
    private void endRoom(Long roomId, Long userId) {

        C20001.C200017s2c.Builder endBroadcast = C20001.C200017s2c.newBuilder();
        log.info("下播roomId=[{}]", roomId);

        endBroadcast.setRoomId(roomId);

        ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE, VoiceActionSet.END_BROADCAST, endBroadcast.build().toByteArray()), roomId); // 发送消息给房间内所有人
        ChannelManager.removeVoiceGroup(roomId);
        roomRemote.endBroadcast(roomId); // 调下播接口
        redisUtils.del(RedisKey.KEY_ROOM_STATE + userId); //  下播删除普通房间开播状态
        redisUtils.del(RedisKey.KEY_ROOM_USER + roomId); // 删除房间-用户缓存

        Set<Object> objects = redisUtils.sGet(RedisKey.KEY_ROOM_USER + roomId);
        if (ObjectUtil.isNotEmpty(objects)) {
            for (Object object : objects) {
                redisUtils.del(RedisKey.KEY_ROOM_USER + object); // 删除用户-房间缓存
            }
        }
    }

    /**
     * @MethodName: enterRoom
     * @Description: TODO 观众进入房间
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 2020/5/18
     **/
    @Override
    public void enterRoom(Channel channel, Packet packet) {
        try {
            C20001.C200011c2s request = C20001.C200011c2s.parseFrom(packet.bytes);
            C20001.C200011s2c.Builder userInfo = C20001.C200011s2c.newBuilder();
            long roomId = request.getRoomId();
            long userId = packet.getUserId();
            Map<String, Object> userMsg = this.userRemote.getUserMsg(userId);
            log.info("用户进入房间roomId=[{}], userId=[{}]", roomId, userId);
            if (ObjectUtil.isNotEmpty(userMsg)) {
                Integer role = this.roomRemote.getVoiceRoomUserRole(userId, roomId);
                userInfo.setRoomId(roomId); // 房间Id
                userInfo.setUserId(userId);//用户Id
                userInfo.setAlias(StringUtils.nvl(userMsg.get("nickName"))); // 昵称
                userInfo.setThumbIconURL(StringUtils.nvl(userMsg.get("iconUrl"))); // 头像
                userInfo.setSex((Integer) userMsg.get("sex")); // 性别
                userInfo.setIsMember((Integer) userMsg.get("isMember")); // 是否会员 0：否； 1：是
                userInfo.setVip((Integer) userMsg.get("vip")); // 会员等级
                userInfo.setRole(role); // 角色 [0观众 1房主 2管理员 3官方]
                String enterEffects = roomRemote.enterEffects(userId);
                userInfo.setEnterEffects(enterEffects == null ? "" : enterEffects); // 进场特效
                log.info("用户角色role=[{}]", role);

                ChannelManager.addVoiceGroup(roomId, channel); // 更新创建群组
//                int voiceGroupSize = roomRemote.getOnlineNum(roomId);
                int voiceGroupSize = (int) redisUtils.sGetSetSize(RedisKey.KEY_ROOM_USER + roomId) + 1;
                log.info("房间人数=[{}],roomId=[{}]", voiceGroupSize, roomId);
                userInfo.setOnlineNum(voiceGroupSize);
                ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE, VoiceActionSet.ENTER_ROOM, userInfo.build().toByteArray()), roomId); // 发送消息给房间内所有人
                redisUtils.sSet(RedisKey.KEY_ROOM_USER + roomId, userId);
                redisUtils.set(RedisKey.KEY_ROOM_USER + userId, roomId);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * @MethodName: sendMessage
     * @Description: TODO 发送消息
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 2020/5/18
     **/
    @Override
    public void sendMessage(Channel channel, Packet packet) {
        String messageId = "";
        long roomId = 0;
        long userId = packet.getUserId();
        try {
            C20001.C200012c2s request = C20001.C200012c2s.parseFrom(packet.bytes);
            C20001.C200013s2c.Builder message = C20001.C200013s2c.newBuilder(); // 接收消息
            C20001.C200012s2c.Builder messageReturn = C20001.C200012s2c.newBuilder(); // 发送消息反馈
            roomId = request.getRoomId();

            // 判断用户是否禁言 禁言直接返回
            Map<String, Object> bannedMap = roomRemote.isBanned(roomId, userId);
            if (1 == (int) bannedMap.get("isBanned")) {
                log.info("[{}]用户禁言中...", userId);
                messageReturn.setResult(1);
                messageReturn.setErrorMsg("你在禁言中，剩余时间：" + bannedMap.get("msg"));
                messageReturn.setMessageId(request.getMessageId());
                ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_VOICE, VoiceActionSet.SEND_MESSAGE, messageReturn.build().toByteArray()), userId);
                return;
            }
            int messageType = request.getMessageType(); // 消息类型 【0 文本 1 Emoji 2 图片 ...】

            if (messageType == 0 && request.getMessage().length() > messageSize) {
                log.info("[{}]发送消息内容超长", userId);
                messageReturn.setResult(1);
                messageReturn.setErrorMsg("你发送消息内容超长，最长不得超过" + messageSize + "字");
                messageReturn.setMessageId(request.getMessageId());
                ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_VOICE, VoiceActionSet.SEND_MESSAGE, messageReturn.build().toByteArray()), userId);
                return;
            }

            messageId = request.getMessageId(); // 消息的唯一标识符

            List<Long> userIdList = new LinkedList<>();
            userIdList.add(userId);
            Map<String, Object> userInfo = this.userRemote.getUserMsg(userId);
            log.info("发送消息roomId=[{}], userId=[{}], 查询结果=[{}]", roomId, userId, userInfo);
//            List<Long> roomUserIdList = roomRemote.getRoomUserIdList(request.getRoomId());// 根据群聊Id获取群聊用户ID
            if (ObjectUtil.isNotEmpty(userInfo)) {
                message.setRoomId(roomId); // 房间Id
                message.setUserId(userId);//用户Id
                message.setAlias(StringUtils.nvl(userInfo.get("nickName"))); // 昵称
                message.setThumbIconURL(StringUtils.nvl(userInfo.get("iconUrl"))); // 头像
                message.setSex((Integer) userInfo.get("sex")); // 性别
                message.setMessage(request.getMessage()); // 消息
                message.setMessageType(messageType); // 消息类型 【0 文本 1 Emoji 2 图片 ...】
                message.setMessageId(request.getMessageId()); // 消息的唯一标识符
                message.setRole(request.getRole()); // 角色

                if (messageType == 2) {
                    int i = chatFileUpload(message, request);
                    if (i == 1) {
                        log.info("[{}]发送文件内容超长", userId);
                        messageReturn.setResult(1);
                        messageReturn.setErrorMsg("发送文件最大不能超过:" + fileSize + "MB");
                        messageReturn.setMessageId(request.getMessageId());
                        ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_VOICE, VoiceActionSet.SEND_MESSAGE, messageReturn.build().toByteArray()), userId);
                        return;
                    }

                }
                ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE, VoiceActionSet.RECEIVE_MESSAGE, message.build().toByteArray()), roomId); // 发送消息给房间内所有人
                messageReturn.setResult(0);
                messageReturn.setErrorMsg("success");
                messageReturn.setMessageId(request.getMessageId());
                ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_VOICE, VoiceActionSet.SEND_MESSAGE, messageReturn.build().toByteArray()), userId); // 发送给自己反馈信息
                roomTaskHandler(TaskEnum.PGT0032.getCode(), userId, roomId);
            }
        } catch (
                InvalidProtocolBufferException e) {
            C20001.C200012s2c.Builder messageReturn = C20001.C200012s2c.newBuilder(); // 发送消息反馈
            messageReturn.setResult(1);
            messageReturn.setErrorMsg("error");
            messageReturn.setErrorMsg(messageId);
            ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_VOICE, VoiceActionSet.SEND_MESSAGE, messageReturn.build().toByteArray()), userId); // 发送给自己反馈信息
            e.printStackTrace();
        }

    }

    /**
     * @MethodName: updateRoom
     * @Description: TODO 房间信息改变
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 2020/5/18
     **/
    @Override
    public void updateRoom(Channel channel, Packet packet) {

        C20001.C200014c2s request = null;
        try {
            request = C20001.C200014c2s.parseFrom(packet.bytes);
            C20001.C200014s2c.Builder upRoom = C20001.C200014s2c.newBuilder();
            long roomId = request.getRoomId();
            log.info("房间信息改变roomId=[{}], userId = [{}]", roomId, packet.getUserId());
            upRoom.setRoomId(request.getRoomId());
            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE, VoiceActionSet.UPDATE_ROOM, upRoom.build().toByteArray()), roomId); // 发送消息给房间内所有人
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }


    }

    /**
     * @MethodName: speakStatus
     * @Description: TODO 正在说话状态
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 2020/5/18
     **/
    @Override
    public void speakStatus(Channel channel, Packet packet) {
        try {
            C20001.C200016c2s request = C20001.C200016c2s.parseFrom(packet.bytes);
            long roomId = request.getRoomId();
//            log.info("正在说话状态roomId=[{}], userId = [{}]", roomId, packet.getUserId());
            C20001.C200016s2c.Builder speakStatus = C20001.C200016s2c.newBuilder();
            speakStatus.setRoomId(roomId);
            speakStatus.setUserId(request.getUserId());
            speakStatus.setType(request.getType());
            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE, VoiceActionSet.SPEAK_STATUS, speakStatus.build().toByteArray()), roomId); // 发送消息给房间内所有人
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * @MethodName: upMicList
     * @Description: TODO 排麦列表改变
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 18:08 2020/7/2
     **/
    @Override
    public void upMicList(Channel channel, Packet packet) {
        try {
            C20001.C200019c2s request = C20001.C200019c2s.parseFrom(packet.bytes);
            long roomId = request.getRoomId();
            log.info("排麦列表改变roomId=[{}], userId = [{}]", roomId, packet.getUserId());
            C20001.C200019s2c.Builder response = C20001.C200019s2c.newBuilder();
            response.setRoomId(roomId);
            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE, VoiceActionSet.UP_MIC_LIST, response.build().toByteArray()), roomId); // 发送消息给房间内所有人
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * @MethodName: upRoomSeatList
     * @Description: TODO 座位信息改变
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 18:08 2020/7/2
     **/
    @Override
    public void upRoomSeatList(Channel channel, Packet packet) {
        try {
            C20001.C200015c2s request = C20001.C200015c2s.parseFrom(packet.bytes);
            long roomId = request.getRoomId();
            log.info("座位信息改变roomId=[{}], userId = [{}]", roomId, packet.getUserId());
            C20001.C200015s2c.Builder response = C20001.C200015s2c.newBuilder();
            response.setRoomId(roomId);
            response.setTargetUserId(request.getTargetUserId());
            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE, VoiceActionSet.UP_ROOM_SEAT_LIST, response.build().toByteArray()), roomId); // 发送消息给房间内所有人
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * @MethodName: exitRoom
     * @Description: TODO 退出房间
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 2020/5/18
     **/
    @Override
    public void exitRoom(Channel channel, Packet packet) {

        try {
            C20001.C200018c2s request = C20001.C200018c2s.parseFrom(packet.bytes);
            long roomId = request.getRoomId();
            long userId = packet.getUserId();

            C20001.C200018s2c.Builder exitRoom = C20001.C200018s2c.newBuilder();
            exitRoom.setRoomId(roomId);
            exitRoom.setUserId(userId);
            roomRemote.out(roomId, userId); // 退出房间接口
            redisUtils.setRemove(RedisKey.KEY_ROOM_USER + roomId, userId);
            redisUtils.del(RedisKey.KEY_ROOM_USER + userId);

            int voiceGroupSize = (int) redisUtils.sGetSetSize(RedisKey.KEY_ROOM_USER + roomId);
//            int voiceGroupSize = roomRemote.getOnlineNum(roomId);
            log.info("房间人数=[{}],roomId=[{}]", voiceGroupSize, roomId);
            exitRoom.setOnlineNum(voiceGroupSize);

            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE, VoiceActionSet.EXIT_ROOM, exitRoom.build().toByteArray()), roomId); // 发送消息给房间内所有人

            Long roomID = (Long) redisUtils.get(RedisKey.KEY_ROOM_STATE + userId); // 根据userId房间号
            if (null != roomID) { // 判断是否房主
                log.info("房主退出房间,roomID=[{}],userId=[{}]", roomID, userId);
                exceptionEndBroadcast(roomID, userId);
            } else {
                log.info("退出房间roomId=[{}], userId = [{}]", roomId, userId);
                ChannelManager.removeVoiceChannel(roomId, channel);// 移除群组中的channel
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @MethodName: exceptionExitRoom
     * @Description: TODO 异常退出房间
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 15:41 2020/7/17
     **/
    @Override
    public void exceptionExitRoom(Channel channel, Long roomId, Long userId) {
        try {
            log.info("异常退出房间roomId=[{}], userId = [{}]", roomId, userId);
            C20001.C200018s2c.Builder exitRoom = C20001.C200018s2c.newBuilder();
            exitRoom.setRoomId(roomId);
            exitRoom.setUserId(userId);
            Channel channel1 = ChannelManager.getChannel(userId);

            roomRemote.out(roomId, userId); // 退出房间接口
            redisUtils.setRemove(RedisKey.KEY_ROOM_USER + roomId, userId);
            redisUtils.del(RedisKey.KEY_ROOM_USER + userId);

            int voiceGroupSize = (int) redisUtils.sGetSetSize(RedisKey.KEY_ROOM_USER + roomId);
            log.info("房间人数=[{}],roomId=[{}]", voiceGroupSize, roomId);
            exitRoom.setOnlineNum(voiceGroupSize);
            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE, VoiceActionSet.EXIT_ROOM, exitRoom.build().toByteArray()),
                    new Packet(), roomId, channel1);
            ChannelManager.removeVoiceChannel(roomId, channel);// 移除群组中的channel
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @MethodName: accompanyPlayStatus
     * @Description: TODO 伴奏播放状态
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 16:03 2020/7/1
     **/
    @Override
    public void accompanyPlayStatus(Channel channel, Packet packet) {
        try {
            C20002.C200020c2s request = C20002.C200020c2s.parseFrom(packet.bytes);
            C20002.C200020s2c.Builder acPlayStatus = C20002.C200020s2c.newBuilder();
            long roomId = request.getRoomId();
            log.info("伴奏播放状态roomId=[{}], userId = [{}]", roomId, packet.getUserId());
            acPlayStatus.setRoomId(roomId);
            acPlayStatus.setStatus(request.getStatus());
            acPlayStatus.setMusicName(request.getMusicName());
            acPlayStatus.setUserIconURL(request.getUserIconURL());
//            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE_HANDLE, VoiceHandleSet.ACCOMPANY_PLAY_STATUS, acPlayStatus.build().toByteArray()),
//                    new Packet(), roomId, channel);
//            ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_VOICE_HANDLE, VoiceHandleSet.ACCOMPANY_PLAY_STATUS, acPlayStatus.build().toByteArray()),
//                    packet.getUserId()); // 发送给指定用户信息
            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE_HANDLE, VoiceHandleSet.ACCOMPANY_PLAY_STATUS, acPlayStatus.build().toByteArray()), roomId); // 发送消息给房间内所有人
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

    /**
     * @MethodName: redPackets
     * @Description: TODO 红包
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 16:03 2020/7/1
     **/
    @Override
    public void redPackets(Channel channel, Packet packet) {

        try {
            C20002.C200021c2s request = C20002.C200021c2s.parseFrom(packet.bytes);
            C20002.C200021s2c.Builder redPackets = C20002.C200021s2c.newBuilder();

            long roomId = request.getRoomId();
            log.info("红包roomId=[{}], userId = [{}]", roomId, packet.getUserId());

            redPackets.setRoomId(roomId);
            redPackets.setRpId(request.getRpId());
            redPackets.setSendUserId(request.getSendUserId());
            redPackets.setSendNickName(request.getSendNickName());
            redPackets.setSendIconURL(request.getSendIconURL());
            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE_HANDLE, VoiceHandleSet.RED_PACKETS, redPackets.build().toByteArray()), roomId); // 发送消息给房间内所有人
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }


    }

    /**
     * @MethodName: giveGift
     * @Description: TODO 送礼物
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 16:04 2020/7/1
     **/
    @Override
    public void giveGift(Channel channel, Packet packet) {
        try {
            C20002.C200022c2s request = C20002.C200022c2s.parseFrom(packet.bytes);
            C20002.C200022s2c.Builder giveGift = C20002.C200022s2c.newBuilder();
            long roomId = request.getRoomId();
            log.info("送礼物roomId=[{}], userId = [{}]", roomId, packet.getUserId());
            giveGift.setRoomId(roomId);
            giveGift.setFromUserId(request.getFromUserId());
            giveGift.setFromIconURL(request.getFromIconURL());
            giveGift.setFromNickName(request.getFromNickName());
            giveGift.setToUserId(request.getToUserId());
            giveGift.setToNickName(request.getToNickName());
            giveGift.setToSex(request.getToSex());
            giveGift.setGiftURL(request.getGiftURL());
            giveGift.setGiftNum(request.getGiftNum());
            giveGift.setDynamicPicture(request.getDynamicPicture());
            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE_HANDLE, VoiceHandleSet.GIVE_GIFT, giveGift.build().toByteArray()), roomId); // 发送消息给房间内所有人
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

    /**
     * @MethodName: emoji
     * @Description: TODO 表情
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 16:04 2020/7/1
     **/
    @Override
    public void emoji(Channel channel, Packet packet) {
        try {
            C20002.C200023c2s request = C20002.C200023c2s.parseFrom(packet.bytes);
            C20002.C200023s2c.Builder emoji = C20002.C200023s2c.newBuilder();
            long roomId = request.getRoomId();
            log.info("表情roomId=[{}], userId = [{}]", roomId, packet.getUserId());
            emoji.setRoomId(roomId);
            emoji.setUserId(request.getUserId());
            emoji.setFaceType(request.getFaceType());
            emoji.setResultIndex(request.getResultIndex());
            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE_HANDLE, VoiceHandleSet.EMOJI, emoji.build().toByteArray()), roomId); // 发送消息给房间内所有人
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * @MethodName: interactEmoji
     * @Description: TODO 互动表情
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 16:04 2020/7/1
     **/
    @Override
    public void interactEmoji(Channel channel, Packet packet) {
        try {
            C20002.C200024c2s request = C20002.C200024c2s.parseFrom(packet.bytes);
            C20002.C200024s2c.Builder iEmoji = C20002.C200024s2c.newBuilder();
            long roomId = request.getRoomId();
            log.info("互动表情roomId=[{}], userId = [{}]", roomId, packet.getUserId());
            iEmoji.setRoomId(roomId);
            iEmoji.setFromUserId(request.getFromUserId());
            iEmoji.setToUserId(request.getToUserId());
            iEmoji.setFaceName(request.getFaceName());
            iEmoji.setMoveAnimToLeft(request.getMoveAnimToLeft());
            iEmoji.setEndAnimToLeft(request.getEndAnimToLeft());
            iEmoji.setMoveAnimToRight(request.getMoveAnimToRight());
            iEmoji.setEndAnimToRight(request.getEndAnimToRight());
            iEmoji.setStaticToLeft(request.getStaticToLeft());
            iEmoji.setStaticToRight(request.getStaticToRight());
            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE_HANDLE, VoiceHandleSet.INTERACT_EMOJI, iEmoji.build().toByteArray()), roomId); // 发送消息给房间内所有人
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * @MethodName: luckDraw
     * @Description: TODO 抽奖结果
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 9:50 2020/7/3
     **/
    @Override
    public void luckDraw(Channel channel, Packet packet) {
        try {
            C20002.C200025c2s request = C20002.C200025c2s.parseFrom(packet.bytes);
            C20002.C200025s2c.Builder response = C20002.C200025s2c.newBuilder();
            long roomId = request.getRoomId();
            log.info("抽奖结果roomId=[{}], userId = [{}]", roomId, packet.getUserId());

            List<C20002.ESRoomUserInfo> targetList = request.getTargetList();
            List<C20002.ESRoomUserInfo> winningList = request.getWinningList();

            response.setRoomId(roomId);
            response.addAllTarget(targetList);
            response.addAllWinning(winningList);

            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE_HANDLE, VoiceHandleSet.LUCK_DRAW, response.build().toByteArray()), roomId); // 发送消息给房间内所有人
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * @MethodName: reEnterRoom
     * @Description: TODO 房主重回房间
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 13:05 2020/7/6
     **/
    @Override
    public void reEnterRoom(Channel channel, Packet packet) {
        try {
            C20001.C200011c2s request = C20001.C200011c2s.parseFrom(packet.bytes);
            long roomId = request.getRoomId();
            long userId = packet.getUserId();
            log.info("房主重回房间，房间号：[{}]，用户ID：[{}]", roomId, packet.getUserId());
            ChannelManager.addVoiceGroup(roomId, channel); // 更新创建群组
            if (redisUtils.hasKey(RedisKey.KEY_ROOM_STATE + packet.getUserId())) {
                redisUtils.set(RedisKey.KEY_ROOM_STATE + packet.getUserId(), roomId);
                redisUtils.zRemove(RedisKey.KEY_ROOM_STATE, roomId);
            }
            redisUtils.sSet(RedisKey.KEY_ROOM_USER + roomId, packet.getUserId()); // 添加房间-用户缓存
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * @MethodName: startPk
     * @Description: TODO 发起PK
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 13:26 2020/7/7
     **/
    @Override
    public void startPk(Channel channel, Packet packet) {
        try {
            C20002.C200027c2s request = C20002.C200027c2s.parseFrom(packet.bytes);
            C20002.C200027s2c.Builder response = C20002.C200027s2c.newBuilder();
            long roomId = request.getRoomId();
            log.info("发起PK roomId=[{}], userId = [{}]", roomId, packet.getUserId());

            List<C20002.ESRoomUserInfo> targetList = request.getTargetList();

            response.setRoomId(roomId);
            response.setType(request.getType());
            response.setTime(request.getTime());
            response.addAllTarget(targetList);

            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE_HANDLE, VoiceHandleSet.START_PK, response.build().toByteArray()), roomId); // 发送消息给房间内所有人
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * @MethodName: pkPoll
     * @Description: TODO 投票
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 13:26 2020/7/7
     **/
    @Override
    public void pkPoll(Channel channel, Packet packet) {
        try {
            C20002.C200028c2s request = C20002.C200028c2s.parseFrom(packet.bytes);
            C20002.C200028s2c.Builder response = C20002.C200028s2c.newBuilder();
            long roomId = request.getRoomId();
            log.info("投票roomId=[{}], userId = [{}]", roomId, packet.getUserId());

            response.setRoomId(roomId);
            response.setTargetUserId(request.getTargetUserId());
            response.setType(request.getType());
            response.setPoll(request.getPoll());

//            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE_HANDLE, VoiceHandleSet.PK_POLL, response.build().toByteArray()),
//                    new Packet(), roomId, channel);
//            ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_VOICE_HANDLE, VoiceHandleSet.PK_POLL, response.build().toByteArray()),
//                    packet.getUserId()); // 发送给指定用户信息
            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE_HANDLE, VoiceHandleSet.PK_POLL, response.build().toByteArray()), roomId); // 发送消息给房间内所有人
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * @MethodName: setAdmin
     * @Description: TODO 设置管理员
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 11:02 2020/7/24
     **/
    @Override
    public void setAdmin(Channel channel, Packet packet) {

        try {
            C20002.C200029c2s request = C20002.C200029c2s.parseFrom(packet.bytes);
            C20002.C200029s2c.Builder response = C20002.C200029s2c.newBuilder();
            long roomId = request.getRoomId();
            log.info("设置管理员roomId=[{}], userId = [{}]", roomId, packet.getUserId());

            response.setRoomId(roomId);
            response.setTargetUserId(request.getTargetUserId());
            response.setType(request.getType());
            response.setTargetNickName(request.getTargetNickName());

            ChannelManager.sendPacketToVoiceGroup(new Packet(ChannelSet.CMD_VOICE_HANDLE, VoiceHandleSet.SET_ADMIN, response.build().toByteArray()), roomId); // 发送消息给房间内所有人
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

    /**
     * 异常登录通知
     *
     * @param userId
     */
    @Override
    public void singleSignOn(Long userId, String deviceId, Channel channel) {
        log.info("客户端设备号=[{}]", deviceId);
        String key = RedisKey.KEY_ROOM_LOGIN + userId;
        String value = (String) redisUtils.get(key);
        log.info("redis客户端设备号=[{}]", value);
        if (StrUtil.isEmpty(value)) {
            redisUtils.set(key, deviceId);
        } else {
            if (!value.equals(deviceId)) {
                log.info("用户多处登录, userId=[{}]", userId);
                if (userId != null && userId > 0) {
                    Channel channel1 = ChannelManager.getChannel(userId);
                    if (channel1 != null) {
                        if (channel1.isActive()) {
                            log.info("多处登录通知, userId=[{}]", userId);
                            C10000.C100002s2c.Builder newBuilder = C10000.C100002s2c.newBuilder();
                            channel1.writeAndFlush(new Packet(ChannelSet.CMD_HEART, VoiceActionSet.SEND_MESSAGE, newBuilder.build().toByteArray()));
                        }
                        ChannelManager.removeChannelScheme2(channel1);
//                        handlerManager.doLeave(userId);
                        handlerManager.exceptionExitRoom(channel1, userId);
                    }
                }
                redisUtils.set(key, deviceId); // 更新设备号
            } else {
                log.info("用户重新登录, userId=[{}]", userId);
                // 判断是否在语音房内
                if (redisUtils.hasKey(RedisKey.KEY_ROOM_USER + userId)) {
                    // 在语音房内 更新语音房用户通道
                    Long roomId1 = Long.valueOf(redisUtils.get(RedisKey.KEY_ROOM_USER + userId).toString());
                    if (!Objects.isNull(roomId1) && roomId1 > 0) {
                        log.info("更新语音房群组,roomId=[{}]", roomId1);
                        Channel oldChannel = ChannelManager.getChannel(userId);
                        if (oldChannel != null) {
                            ChannelManager.removeVoiceChannel(roomId1, oldChannel);// 移除群组中的channel
                            redisUtils.setRemove(RedisKey.KEY_ROOM_USER + roomId1, userId);
                        }
                    }
                }
            }
        }
    }

    /**
     * @MethodName: giveGiftNotice
     * @Description: TODO 赠送礼物通知
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 10:38 2020/8/25
     **/
    @Override
    public Result giveGiftNotice(Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("fromUserId").toString());
        log.info("赠送礼物全服通知,userId=[{}]", userId);
        C10004.C100041s2c.Builder builder = C10004.C100041s2c.newBuilder();
        builder.setRoomId(Long.valueOf(params.get("roomId").toString()));// 房间号
        builder.setFromUserId(userId); // 送礼人ID
        builder.setFromIconURL(String.valueOf(params.get("fromIconURL"))); // 送礼人头像
        builder.setFromNickName(String.valueOf(params.get("fromNickName"))); // 送礼人昵称
        builder.setToUserId(Long.valueOf(params.get("toUserId").toString())); // 收礼人ID
        builder.setToIconURL(String.valueOf(params.get("toIconURL"))); // 收礼人头像
        builder.setToNickName(String.valueOf(params.get("toNickName"))); // 收礼人昵称
        builder.setGiftURL(String.valueOf(params.get("giftURL"))); // 礼物图片
        builder.setGiftName(String.valueOf(params.get("giftName"))); // 礼物名称
        builder.setGiftNum(Long.valueOf(params.get("giftNum").toString())); // 数量
        // 当前在线的所有用户发送消息
        NettyCtxGroup.group.writeAndFlush(new Packet(ChannelSet.MONEY_NOTICE, (short) 1, builder.build().toByteArray()));

        return Result.success();
    }

    // 文件上传
    public int chatFileUpload(C20001.C200013s2c.Builder message,
                              C20001.C200012c2s request) {
        try {
            C20001.ESRImageInfo imageInfo = request.getImageInfo(0);
            C20001.ESRImageInfo.Builder responseImgInfo = C20001.ESRImageInfo.newBuilder();

            String newName = "room" + ObjectId.next();

            String imageExt = "." + imageInfo.getImageExt(); // 文件格式

            responseImgInfo.setHeight(imageInfo.getHeight());
            responseImgInfo.setWidth(imageInfo.getWidth());
            responseImgInfo.setImageExt(imageInfo.getImageExt());

            byte[] bytes = imageInfo.getImageFile().toByteArray();
            if (bytes.length > fileSize * mult) {
                return 1;
            }
            InputStream inputStream = new ByteArrayInputStream(bytes);
            MultipartFile file = new FeignMultipartFile("file", newName + imageExt,
                    ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
            int height = imageInfo.getHeight();
            int width = imageInfo.getWidth();
            Result result = null;
            if (height > width) {
                result = uploadRemote.uploadChatFile(file, String.valueOf(400), "", Constants.ROOM_CHAT + request.getRoomId() + "/");
            } else {
                result = uploadRemote.uploadChatFile(file, "", String.valueOf(400), Constants.ROOM_CHAT + request.getRoomId() + "/");
            }
            PicInfo picInfo = result.getCode().equals(0) ? JsonUtils.toObjectPojo(result.getData(), PicInfo.class) : null;
            if (ObjectUtil.isNotEmpty(picInfo)) {
                responseImgInfo.setImageURL(picInfo.getPicUrl());
                responseImgInfo.setLitimg(picInfo.getLittlePicUrl());
            }
            message.addImageInfo(responseImgInfo);
        } catch (Exception e) {
            log.error("语音房聊天文件上传异常");
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * 语音房任务达成
     *
     * @param code
     * @param userId
     * @param roomId
     */
    private void roomTaskHandler(String code, Long userId, Long roomId) {
        executorService.submit(() -> {
            roomRemote.roomTaskHandler(code, userId, roomId);
        });
    }

}
