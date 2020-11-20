package com.enuos.live.handle.heart;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.utils.annotation.AbstractAction;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import com.enuos.live.channel.GameChannel;
import com.enuos.live.codec.Packet;
import com.enuos.live.constants.GameKey;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.rest.IconRemote;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.manager.MemManager;
import com.enuos.live.manager.NoticeManager;
import com.enuos.live.utils.StringUtils;
import com.enuos.live.utils.JsonUtil;
import com.enuos.live.utils.JsonUtils;
import com.enuos.live.manager.LoggerManager;
import com.enuos.live.utils.NameUtil;
import com.enuos.live.utils.RedisUtils;
import io.netty.channel.Channel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * TODO 游戏服务.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.0.0
 * @since 2020/5/15 16:33
 */

@Component
@AbstractAction(cmd = ActionCmd.GAME_HEART)
public class GameHeart extends AbstractActionHandler {

  /** 心跳消息. */
  private static final Packet HEART_PACKET = new Packet(ActionCmd.GAME_HEART, (short) 0, null);
  /** 登录消息. */
  private static final Packet LOGIN_PACKET = new Packet(ActionCmd.GAME_HEART, (short) 1, null);
  /** 心跳交换. */
  private static final short HEART = 0;
  /** 用户信息. */
  private static final short USER_INFO = 1;
  /** 测试房间. */
  private static final long TEST_ID = 2020L;

  @Resource
  private IconRemote iconRemote;
  @Resource
  private UserRemote userRemote;
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
          LoggerManager.info("[PLAYER LOGIN] ID: [{}] TIME: [{}]", packet.userId, LocalDateTime.now());
          Channel oldChannel = GameChannel.getChannel(packet.userId);
          if (Objects.nonNull(oldChannel)) {
            GameChannel.clearOldChannel(oldChannel);
          }
          if (packet.roomId == TEST_ID) {
            I10001.PlayerInfo.Builder playerInfo = I10001.PlayerInfo.newBuilder();
            playerInfo.setUserId(packet.userId);
//            String randomIcon = this.iconRemote.getRandomIcon("mobile", "json");
//            Map<String, Object> randomMap = JsonUtil.toStringMap(randomIcon);
            playerInfo.setIconUrl("https://7lestore.oss-cn-hangzhou.aliyuncs.com/header/e9b068f4aab848a385ae80af01719a16.jpeg");
            playerInfo.setNickName(NameUtil.getRandomEnglishName());
            playerInfo.setSex(ThreadLocalRandom.current().nextInt(2) + 1);
            playerInfo.setLevel(ThreadLocalRandom.current().nextInt(6) + 1);
            if (this.redisUtils.hasKey(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId)) {
              this.redisUtils.del(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId);
            }
            this.redisUtils.setByte(GameKey.KEY_GAME_TEST_LOGIN.getName() + packet.userId, playerInfo.build().toByteArray());
            MemManager.addMemberRec(packet.userId);
          } else {
            // 会员信息
            Result result = this.userRemote.getBase(packet.userId);
            if (Objects.nonNull(result)) {
              Map<String, Object> adminInfo = result.getCode().equals(0) ? JsonUtils.toObjectMap(result.getData()) : null;
              if (Objects.nonNull(adminInfo)) {
                //是否会员[-1 过期会员 0 否 1 是]
                Integer isMember = (Integer) adminInfo.get("isMember");
                if (isMember == 1) {
                  MemManager.addMemberRec(packet.userId);
                }
              }
            }
          }
          channel.writeAndFlush(LOGIN_PACKET);
          NoticeManager.GAME_GROUP.add(channel);
          GameChannel.addChannel(packet.userId, channel);
          break;
        default:
          LoggerManager.warn("[PLAYER LOGIN] CHILD ERROR: [{}]", packet.child);
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
   * @create 2020/5/19 21:09
   * @update 2020/9/15 14:56
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
