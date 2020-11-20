package com.enuos.live.handle.game.f30041;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.codec.Packet;
import com.enuos.live.utils.annotation.AbstractAction;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import io.netty.channel.Channel;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 【30041】你画我猜.
 *
 * @author WangCaiWen Created on 2020/5/15 14:42
 */
@Component
@AbstractAction(cmd = ActionCmd.GAME_DRAW_SOMETHING)
public class GuessedDraw extends AbstractActionHandler {

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
