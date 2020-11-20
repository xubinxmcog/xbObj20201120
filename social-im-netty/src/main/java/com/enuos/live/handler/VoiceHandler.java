package com.enuos.live.handler;

import com.enuos.live.core.Packet;
import com.enuos.live.result.Result;
import io.netty.channel.Channel;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author WangCaiWen
 * Created on 2020/4/9 11:06
 */
public interface VoiceHandler {

    /**
     * 开播
     */
    void startBroadcast(Channel channel, Packet packet);

    /**
     * 下播
     */
    void endBroadcast(Channel channel, Packet packet);

    /**
     * 异常下播
     */
    Result exceptionEndBroadcast(Long roomId, Long userId);

    /**
     * 观众进入房间
     *
     * @param channel 通道
     * @param packet  数据包
     */
    void enterRoom(Channel channel, Packet packet);

    /**
     * 发送消息
     *
     * @param channel 通道
     * @param packet  数据包
     */
    void sendMessage(Channel channel, Packet packet);

    /**
     * 房间信息改变
     *
     * @param channel 通道
     * @param packet  数据包
     */
    void updateRoom(Channel channel, Packet packet);


    /**
     * 正在说话状态
     *
     * @param channel 通道
     * @param packet  数据包
     */
    void speakStatus(Channel channel, Packet packet);

    /**
     * 排麦列表改变
     *
     * @param channel 通道
     * @param packet  数据包
     */
    void upMicList(Channel channel, Packet packet);

    /**
     * 座位信息改变
     *
     * @param channel 通道
     * @param packet  数据包
     */
    void upRoomSeatList(Channel channel, Packet packet);

    /**
     * 退出房间
     *
     * @param channel 通道
     * @param packet  数据包
     */
    void exitRoom(Channel channel, Packet packet);


    /**
     * 异常退出房间
     *
     * @param channel 通道
     * @param roomId  房间号
     * @param userId  用户ID
     */
    void exceptionExitRoom(Channel channel, Long roomId, Long userId);

    /**
     * 伴奏播放状态
     *
     * @param channel 通道
     * @param packet  数据包
     */
    void accompanyPlayStatus(Channel channel, Packet packet);

    /**
     * 红包
     *
     * @param channel 通道
     * @param packet  数据包
     */
    void redPackets(Channel channel, Packet packet);

    /**
     * 送礼物
     *
     * @param channel 通道
     * @param packet  数据包
     */
    void giveGift(Channel channel, Packet packet);

    /**
     * 表情
     *
     * @param channel 通道
     * @param packet  数据包
     */
    void emoji(Channel channel, Packet packet);

    /**
     * 互动表情
     *
     * @param channel 通道
     * @param packet  数据包
     */
    void interactEmoji(Channel channel, Packet packet);

    /**
     * 抽奖结果
     *
     * @param channel 通道
     * @param packet  数据包
     */
    void luckDraw(Channel channel, Packet packet);

    /**
     * 重回房间
     *
     * @param channel 通道
     * @param packet  数据包
     */
    void reEnterRoom(Channel channel, Packet packet);

    /**
     * 发起PK
     *
     * @param channel 通道
     * @param packet  数据包
     */
    void startPk(Channel channel, Packet packet);

    /**
     * 投票
     *
     * @param channel 通道
     * @param packet  数据包
     */
    void pkPoll(Channel channel, Packet packet);

    /**
     * 设置管理员
     *
     * @param channel 通道
     * @param packet  数据包
     */
    void setAdmin(Channel channel, Packet packet);

    /**
     * 多处登录通知
     * @param deviceId
     */
    void singleSignOn(Long userId, String deviceId, Channel channel);

    /**
     * 赠送礼物通知
     * @param params
     * @return
     */
    Result giveGiftNotice(Map<String, Object> params);

}
