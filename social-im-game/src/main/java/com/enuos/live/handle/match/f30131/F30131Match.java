package com.enuos.live.handle.match.f30131;

import com.enuos.live.codec.Packet;
import com.enuos.live.utils.annotation.AbstractAction;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import io.netty.channel.Channel;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * TODO 奔跑吧！小熊.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V3.0.0
 * @since 2020/8/27 18:24
 */

@Component
@AbstractAction(cmd = 100131)
public class F30131Match extends AbstractActionHandler {

  @Override
  public void handle(Channel channel, Packet packet) {

  }

  @Override
  public void shutOff(Long userId, Long attachId) {

  }

  @Override
  public void cleaning(Long roomId) {

  }

  @Override
  public void joinRobot(Long roomId, List<Long> playerIds) {

  }

}
