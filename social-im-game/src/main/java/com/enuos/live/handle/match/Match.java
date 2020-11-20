package com.enuos.live.handle.match;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.utils.annotation.AbstractAction;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import com.enuos.live.codec.Packet;
import com.enuos.live.proto.f20001msg.F20001;
import com.enuos.live.server.HandlerContext;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.manager.LoggerManager;
import io.netty.channel.Channel;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * TODO 匹配处理.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version v1.0.0
 * @since 2020/5/19 15:17
 */

@Component
@AbstractAction(cmd = ActionCmd.GAME_MATCH)
public class Match extends AbstractActionHandler {

  @Resource
  private HandlerContext handlerContext;

  /** 最大编码长度. */
  private static final long MAX_CODE_LENGTH = 30000L;

  /**
   * TODO 处理分发.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/19 21:09
   * @update 2020/9/30 15:23
   */
  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      switch (packet.child) {
        case MatchCmd.UNIVERSAL_MATCH:
          universalMatch(channel, packet);
          break;
        case MatchCmd.CANCEL_MATCH:
          cancelMatch(channel, packet);
          break;
        case MatchCmd.AEROPLANE_MATCH:
          aeroplaneMatch(channel, packet);
          break;
        case MatchCmd.WHO_IS_SPY_MATCH:
          whoIsSpyMatch(channel, packet);
          break;
        case MatchCmd.WEREWOLF_MATCH:
          werewolfMatch(channel, packet);
          break;
        case MatchCmd.GUESS_MATCH:
          guessMatch(channel, packet);
          break;
        case MatchCmd.QUICK_START:
          quickStart(channel, packet);
          break;
        case MatchCmd.POKER_MATCH:
          pokerMatch(channel, packet);
          break;
        case MatchCmd.SPARROW_MATCH:
          sparrowMatch(channel, packet);
          break;
        case MatchCmd.LANDLORDS_MATCH:
          landlordsMatch(channel, packet);
          break;
        default:
          LoggerManager.warn("[MATCH 20001 HANDLE] CHILD ERROR: [{}]", packet.child);
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
   * @create 2020/7/8 21:08
   * @update 2020/9/30 18:11
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
   * @create 2020/8/3 18:36
   * @update 2020/9/30 18:11
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
   * @create 2020/8/21 18:11
   * @update 2020/9/30 18:11
   */
  @Override
  public void joinRobot(Long roomId, List<Long> playerIds) {
    // MATCH-EMPTY-METHOD
  }

  /**
   * TODO 通用匹配.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/19 18:16
   * @update 2020/9/30 18:11
   */
  private void universalMatch(Channel channel, Packet packet) {
    try {
      F20001.F200010C2S request = F20001.F200010C2S.parseFrom(packet.bytes);
      if (request.getGameCode() > MAX_CODE_LENGTH) {
        int matchCode = MatchScan.getMatchCode(request.getGameCode());
        if (matchCode > 0) {
          AbstractActionHandler instance = this.handlerContext.getInstance(matchCode);
          instance.handle(channel, packet);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 取消匹配.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/19 18:16
   * @update 2020/9/17 21:07
   */
  private void cancelMatch(Channel channel, Packet packet) {
    try {
      F20001.F200011C2S request = F20001.F200011C2S.parseFrom(packet.bytes);
      if (channel.isActive()) {
        if (request.getGameCode() > MAX_CODE_LENGTH) {
          int matchCode = MatchScan.getMatchCode(request.getGameCode());
          if (matchCode > 0) {
            AbstractActionHandler instance = this.handlerContext.getInstance(matchCode);
            instance.shutOff(packet.userId, request.getGameCode());
          }
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 飞行棋.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/24 18:06
   * @update 2020/9/17 21:07
   */
  private void aeroplaneMatch(Channel channel, Packet packet) {
    try {
      F20001.F200012C2S request = F20001.F200012C2S.parseFrom(packet.bytes);
      if (request.getGameCode() > MAX_CODE_LENGTH) {
        int matchCode = MatchScan.getMatchCode(request.getGameCode());
        if (matchCode > 0) {
          AbstractActionHandler instance = this.handlerContext.getInstance(matchCode);
          instance.handle(channel, packet);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 谁是卧底.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/5 18:12
   * @update 2020/9/17 21:07
   */
  private void whoIsSpyMatch(Channel channel, Packet packet) {
    try {
      F20001.F200013C2S request = F20001.F200013C2S.parseFrom(packet.bytes);
      if (request.getGameCode() > MAX_CODE_LENGTH) {
        int matchCode = MatchScan.getMatchCode(request.getGameCode());
        if (matchCode > 0) {
          AbstractActionHandler instance = this.handlerContext.getInstance(matchCode);
          instance.handle(channel, packet);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 狼人杀.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/8 21:08
   * @update 2020/9/17 21:07
   */
  private void werewolfMatch(Channel channel, Packet packet) {
    try {
      F20001.F200014C2S request = F20001.F200014C2S.parseFrom(packet.bytes);
      if (request.getGameCode() > MAX_CODE_LENGTH) {
        int matchCode = MatchScan.getMatchCode(request.getGameCode());
        if (matchCode > 0) {
          AbstractActionHandler instance = this.handlerContext.getInstance(matchCode);
          instance.handle(channel, packet);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 社交游戏.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/8 21:08
   * @update 2020/9/17 21:07
   */
  private void guessMatch(Channel channel, Packet packet) {
    try {
      F20001.F200015C2S request = F20001.F200015C2S.parseFrom(packet.bytes);
      if (request.getGameCode() > MAX_CODE_LENGTH) {
        int matchCode = MatchScan.getMatchCode(request.getGameCode());
        if (matchCode > 0) {
          AbstractActionHandler instance = this.handlerContext.getInstance(matchCode);
          instance.handle(channel, packet);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 快速开始.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/8 21:08
   * @update 2020/9/17 21:07
   */
  private void quickStart(Channel channel, Packet packet) {
    try {
      F20001.F200016C2S request = F20001.F200016C2S.parseFrom(packet.bytes);
      if (request.getGameCode() > MAX_CODE_LENGTH) {
        int matchCode = MatchScan.getMatchCode(request.getGameCode());
        if (matchCode > 0) {
          AbstractActionHandler instance = this.handlerContext.getInstance(matchCode);
          instance.handle(channel, packet);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 扑克匹配.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/27 13:26
   * @update 2020/8/27 13:26
   */
  private void pokerMatch(Channel channel, Packet packet) {
    try {
      F20001.F200017C2S request = F20001.F200017C2S.parseFrom(packet.bytes);
      if (request.getGameCode() > MAX_CODE_LENGTH) {
        int matchCode = MatchScan.getMatchCode(request.getGameCode());
        if (matchCode > 0) {
          AbstractActionHandler instance = this.handlerContext.getInstance(matchCode);
          instance.handle(channel, packet);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 七乐麻将.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/27 13:29
   * @update 2020/8/27 13:29
   */
  private void sparrowMatch(Channel channel, Packet packet) {
    try {
      F20001.F200017C2S request = F20001.F200017C2S.parseFrom(packet.bytes);
      if (request.getGameCode() > MAX_CODE_LENGTH) {
        int matchCode = MatchScan.getMatchCode(request.getGameCode());
        if (matchCode > 0) {
          AbstractActionHandler instance = this.handlerContext.getInstance(matchCode);
          instance.handle(channel, packet);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  /**
   * TODO 七乐地主.
   *
   * @param channel [通信管道]
   * @param packet [数据包]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/27 14:52
   * @update 2020/8/27 14:52
   */
  private void landlordsMatch(Channel channel, Packet packet) {
    try {
      F20001.F200017C2S request = F20001.F200017C2S.parseFrom(packet.bytes);
      if (request.getGameCode() > MAX_CODE_LENGTH) {
        int matchCode = MatchScan.getMatchCode(request.getGameCode());
        if (matchCode > 0) {
          AbstractActionHandler instance = this.handlerContext.getInstance(matchCode);
          instance.handle(channel, packet);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

}
