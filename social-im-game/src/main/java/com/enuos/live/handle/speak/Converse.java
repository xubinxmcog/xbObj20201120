package com.enuos.live.handle.speak;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.codec.Packet;
import com.enuos.live.utils.annotation.AbstractAction;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import com.enuos.live.manager.ChatManager;
import com.enuos.live.channel.SoftChannel;
import com.enuos.live.proto.f10001msg.F10001;
import com.enuos.live.proto.f20000msg.F20000;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.manager.LoggerManager;
import io.netty.channel.Channel;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * TODO 交流服务.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.0.0
 * @since 2020/5/16 8:27
 */

@Component
@AbstractAction(cmd = ActionCmd.GAME_CHAT)
public class Converse extends AbstractActionHandler {

  /**
   * TODO 处理分发.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/16 17:09
   * @update 2020/8/21 10:49
   */
  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      switch (packet.child) {
        // 文字交流
        case ConverseCmd.CONVERSE_TEXT:
          F10001.F10001C2S converseInfo = F10001.F10001C2S.parseFrom(packet.bytes);
          F10001.F10001S2C.Builder converseBuilder = F10001.F10001S2C.newBuilder();
          converseBuilder.setUserID(packet.userId);
          converseBuilder.setMessage(converseInfo.getMessage());
          ChatManager.sendPacketToGroup(new Packet(ActionCmd.GAME_CHAT, ConverseCmd.CONVERSE_TEXT,
              converseBuilder.build().toByteArray()), packet.roomId);
          break;
        // 语音交流
        case ConverseCmd.CONVERSE_VOICE:
          F10001.F100011C2S voiceInfo = F10001.F100011C2S.parseFrom(packet.bytes);
          F20000.F200003S2C.Builder voiceBuilder = F20000.F200003S2C.newBuilder();
          voiceBuilder.setType(voiceInfo.getIsOpenVoice() == 0 ? 1 : 0);
          SoftChannel.sendPacketToUserId(new Packet(ActionCmd.APP_HEART, (short) 3,
              voiceBuilder.build().toByteArray()), packet.userId);
          break;
        // 聊天表情
        case ConverseCmd.CONVERSE_ROOM:
          F10001.F100012C2S chatInfo = F10001.F100012C2S.parseFrom(packet.bytes);
          F10001.F100012S2C.Builder chatBuilder = F10001.F100012S2C.newBuilder();
          chatBuilder.setType(1);
          chatBuilder.setMessage(chatInfo.getMessage());
          chatBuilder.setUserID(packet.userId);
          ChatManager.sendPacketToGroup(new Packet(ActionCmd.GAME_CHAT, ConverseCmd.CONVERSE_ROOM,
              chatBuilder.build().toByteArray()), packet.roomId);
          break;
        // 游戏邀请
        case ConverseCmd.GAME_INVITE:
          F10001.F100013C2S inviteInfo = F10001.F100013C2S.parseFrom(packet.bytes);
          F20000.F200004S2C.Builder inviteBuilder = F20000.F200004S2C.newBuilder();
          inviteBuilder.setGameCode(inviteInfo.getGameCode()).setRoomId(packet.roomId);
          SoftChannel.sendPacketToUserId(new Packet(ActionCmd.APP_HEART, (short) 4,
              inviteBuilder.build().toByteArray()), packet.userId);
          break;
        // 切换扬声器
        case ConverseCmd.CONVERSE_LOUDSPEAKER:
          F10001.F100014C2S loudspeakerInfo = F10001.F100014C2S.parseFrom(packet.bytes);
          F20000.F200006S2C.Builder loudspeakerBuilder = F20000.F200006S2C.newBuilder();
          loudspeakerBuilder.setType(loudspeakerInfo.getIsOpenLoudspeaker());
          SoftChannel.sendPacketToUserId(new Packet(ActionCmd.APP_HEART, (short) 6,
              loudspeakerBuilder.build().toByteArray()), packet.userId);
          break;
        default:
          LoggerManager.warn("[SOFT 10001 HANDLE] CHILD ERROR: [{}]", packet.child);
          break;
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 关闭处理.
   *
   * @param userId [用户ID]
   * @param attachId [附属ID(或房间Id)]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/21 10:47
   * @update 2020/8/21 10:47
   */
  @Override
  public void shutOff(Long userId, Long attachId) {
    // MATCH-EMPTY-METHOD
  }

  /**
   * TODO 清除处理.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/21 10:47
   * @update 2020/8/21 10:47
   */
  @Override
  public void cleaning(Long roomId) {
    // MATCH-EMPTY-METHOD
  }

  /**
   * TODO 陪玩处理.
   *
   * @param roomId [房间ID]
   * @param playerIds [机器人信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 15:55
   * @update 2020/9/15 15:55
   */
  @Override
  public void joinRobot(Long roomId, List<Long> playerIds) {
    // MATCH-EMPTY-METHOD
  }


}
