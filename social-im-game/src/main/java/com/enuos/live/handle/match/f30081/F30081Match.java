package com.enuos.live.handle.match.f30081;

import com.enuos.live.codec.Packet;
import com.enuos.live.utils.annotation.AbstractAction;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import com.enuos.live.pojo.MatchRoom;
import com.enuos.live.proto.f20001msg.F20001;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.manager.LoggerManager;
import io.netty.channel.Channel;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * TODO 狼人杀.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V3.0.0
 * @since 2020/8/27 18:20
 */

@Component
@AbstractAction(cmd = 100081)
public class F30081Match extends AbstractActionHandler {

  /**
   * TODO 匹配处理.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 15:43
   * @update 2020/10/28 15:43
   */
  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      switch (packet.child) {
        // 房间匹配
        case 4:
          roomMatch(channel, packet);
          break;
        // 快速开始
        case 6:
          quickStart(channel, packet);
          break;
        default:
          LoggerManager.warn("[MATCH 30081 HANDLE] CHILD ERROR: [{}]", packet.child);
          break;
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 房间匹配.
   *
   * @param channel 快速通道
   * @param packet 数据包
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 15:54
   * @update 2020/10/28 15:54
   */
  private void roomMatch(Channel channel, Packet packet) {
    try {
      F20001.F200014C2S request = F20001.F200014C2S.parseFrom(packet.bytes);
      // 房间场次 1-普通场 2-进阶场
      if (request.getScreening() == 1) {
        // 匹配人数 0-{6人} 1-{9人} 2-{12人}
        switch (request.getNumberMatch()) {
          case 0:

            break;
          case 1:
            break;
          default:
            break;
        }
      } else {
        // 匹配人数 0-{6人} 1-{9人} 2-{12人}
        switch (request.getNumberMatch()) {
          case 1:
            break;
          case 2:
            break;
          default:
            break;
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 检查残余.
   *
   * @param session [1-普通场 2-进阶场]
   * @param matchNumber [0-{6人} 1-{9人} 2-{12人}]
   * @return [0-获取 1-创建]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 16:46
   * @update 2020/10/28 16:46
   */
  private int checkResidualAuthenticity(Integer session, Integer matchNumber) {
    int index;
    if (session == 1) {
      index = werewolfNormalSession(matchNumber);
    } else {
      index = werewolfAdvancedSession(matchNumber);
    }
    return index;
  }

  private int werewolfNormalSession(Integer matchNumber) {
    MatchRoom matchRoom;
    boolean isExists;
    int index = 0;


    return 0;
  }

  private int werewolfAdvancedSession(Integer matchNumber) {


    return 0;
  }

  /**
   * TODO 快速开始.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 15:54
   * @update 2020/10/28 15:54
   */
  private void quickStart(Channel channel, Packet packet) {
    try {


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
   * @create 2020/10/28 15:42
   * @update 2020/10/28 15:42
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
   * @create 2020/10/28 15:42
   * @update 2020/10/28 15:42
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
   * @create 2020/10/28 15:42
   * @update 2020/10/28 15:42
   */
  @Override
  public void joinRobot(Long roomId, List<Long> playerIds) {
    // MATCH-EMPTY-METHOD
  }

}
