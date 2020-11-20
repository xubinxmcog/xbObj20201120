package com.enuos.live.handle.heart;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.utils.annotation.AbstractAction;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import com.enuos.live.channel.SoftChannel;
import com.enuos.live.codec.Packet;
import com.enuos.live.constants.GameKey;
import com.enuos.live.proto.f20000msg.F20000;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.manager.NoticeManager;
import com.enuos.live.manager.LoggerManager;
import com.enuos.live.utils.RedisUtils;
import io.netty.channel.Channel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * TODO 软件服务.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.0.0
 * @since 2020/5/19 15:02
 */

@Component
@AbstractAction(cmd = ActionCmd.APP_HEART)
public class SocialHeart extends AbstractActionHandler {

  /** 心跳消息. */
  private static final Packet HEART_PACKET = new Packet(ActionCmd.APP_HEART, (short) 0, null);
  /** 心跳交换. */
  private static final short HEART = 0;
  /** 用户信息. */
  private static final short USER_INFO = 1;

  @Resource
  private RedisUtils redisUtils;

  /**
   * TODO 处理分发.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/19 21:09
   * @update 2020/9/15 15:53
   */
  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      switch (packet.child) {
        case HEART:
          channel.writeAndFlush(HEART_PACKET);
          break;
        case USER_INFO:
          LoggerManager.info("[MEMBER LOGIN] ID: [{}] TIME: [{}]", packet.userId, LocalDateTime.now());
          Channel oldChannel =  SoftChannel.getChannel(packet.userId);
          if (Objects.nonNull(oldChannel)) {
            SoftChannel.clearOldChannel(oldChannel);
          }
          F20000.F200001C2S request = F20000.F200001C2S.parseFrom(packet.bytes);
          I10001.PlayerInfo.Builder playerInfo = I10001.PlayerInfo.newBuilder();
          playerInfo.setUserId(packet.userId);
          playerInfo.setIconUrl(request.getThumbIconURL());
          playerInfo.setNickName(request.getNickName());
          playerInfo.setSex(request.getSex());
          playerInfo.setLevel(request.getLevel());
          if (this.redisUtils.hasKey(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId)) {
            this.redisUtils.del(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId);
          }
          this.redisUtils.setByte(GameKey.KEY_GAME_USER_LOGIN.getName() + packet.userId, playerInfo.build().toByteArray());
          F20000.F200001S2C.Builder builder = F20000.F200001S2C.newBuilder();
          channel.writeAndFlush(new Packet(ActionCmd.APP_HEART, USER_INFO, builder.setResult(0).build().toByteArray()));
          NoticeManager.SOFT_GROUP.add(channel);
          SoftChannel.addChannel(packet.userId, channel);
          break;
        default:
          LoggerManager.warn("[MEMBER LOGIN] CHILD ERROR: [{}]", packet.child);
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
   * @create 2020/9/15 16:01
   * @update 2020/9/15 16:01
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
   * @create 2020/9/15 15:54
   * @update 2020/9/15 15:54
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
