package com.enuos.live.handle.match.f30191;

import com.enuos.live.codec.Packet;
import com.enuos.live.utils.annotation.AbstractAction;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import io.netty.channel.Channel;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * TODO 最强小熊.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V1.0.0
 * @since 2020/8/27 18:26
 */

@Component
@AbstractAction(cmd = 100191)
public class F30191Match extends AbstractActionHandler {

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
