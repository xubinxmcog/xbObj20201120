package com.enuos.live.handle.match.f30061;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.codec.Packet;
import com.enuos.live.constants.GameKey;
import com.enuos.live.handle.match.MatchCmd;
import com.enuos.live.pojo.MatchRoom;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.manager.MatchManager;
import com.enuos.live.utils.annotation.AbstractAction;
import com.enuos.live.utils.annotation.AbstractActionHandler;
import com.enuos.live.proto.f20001msg.F20001;
import com.enuos.live.rest.GameRemote;
import com.enuos.live.result.Result;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.utils.RoomUtils;
import com.enuos.live.utils.JsonUtils;
import com.enuos.live.utils.StringUtils;
import com.enuos.live.manager.LoggerManager;
import com.enuos.live.utils.RedisUtils;
import io.netty.channel.Channel;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * TODO 你说我猜.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V1.0.0
 * @since 2020/7/8 9:57
 */

@Component
@AbstractAction(cmd = 100061)
public class F30061Match extends AbstractActionHandler {

  @Resource
  private GameRemote gameRemote;
  @Resource
  private RedisUtils redisUtils;

  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      F20001.F200015S2C.Builder builder = F20001.F200015S2C.newBuilder();
      Result result = this.gameRemote.getGameInfo(30061L);
      Map<String, Object> gameInfoMap = result.getCode().equals(0) ? JsonUtils.toObjectMap(result.getData()) : null;
      if (gameInfoMap != null) {
        String gameLink = StringUtils.nvl(gameInfoMap.get("gameLink"));
        if (MatchManager.MATCH_GUESSED_SAID.size() > 0) {
          MatchRoom matchRoom = MatchManager.MATCH_GUESSED_SAID.get(0);
          matchRoom.setPeopleNum(matchRoom.getPeopleNum() - 1);
          if (matchRoom.getPeopleNum() == 0) {
            MatchManager.delGuessedSaidMatch(matchRoom.getRoomId());
          }
          builder.setGameLink(gameLink);
          builder.setRoomId(matchRoom.getRoomId());
          channel.writeAndFlush(new Packet(ActionCmd.GAME_MATCH, MatchCmd.GUESS_MATCH,
              builder.build().toByteArray()));
          I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
          joinInfo.setRoomId(matchRoom.getRoomId());
          joinInfo.setGameId(30061L);
          byte[] joinByte = joinInfo.build().toByteArray();
          this.redisUtils.setByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId, joinByte);
        } else {
          long roomId = RoomUtils.getRandomRoomNo();
          // 监测房间ID是否存在
          boolean isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
          while (!isExists) {
            roomId = RoomUtils.getRandomRoomNo();
            isExists = !this.redisUtils.hasKey(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId);
          }
          MatchRoom matchRoom = new MatchRoom();
          matchRoom.setRoomId(roomId);
          matchRoom.setPeopleNum(7);
          builder.setGameLink(gameLink);
          builder.setRoomId(matchRoom.getRoomId());
          channel.writeAndFlush(new Packet(ActionCmd.GAME_MATCH, MatchCmd.GUESS_MATCH,
              builder.build().toByteArray()));
          MatchManager.refreshGuessedSaidMatch(matchRoom);
          I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
          joinInfo.setRoomId(matchRoom.getRoomId());
          joinInfo.setGameId(30061L);
          byte[] joinByte = joinInfo.build().toByteArray();
          this.redisUtils.setByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId, joinByte);
          I10001.RoomRecord.Builder roomRecord = I10001.RoomRecord.newBuilder();
          roomRecord.setGameId(30061L).setRoomId(matchRoom.getRoomId()).setOpenWay(0).setRoomType(0).setGameMode(0)
              .setSpeakMode(0).setGameNumber(0).setGameSession(0);
          byte[] roomByte = roomRecord.build().toByteArray();
          this.redisUtils.setByte(GameKey.KEY_GAME_ROOM_RECORD.getName() + matchRoom.getRoomId(), roomByte);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }

  @Override
  public void shutOff(Long userId, Long attachId) {
    // MATCH-EMPTY-METHOD
  }

  @Override
  public void cleaning(Long roomId) {
    // MATCH-EMPTY-METHOD
  }

  @Override
  public void joinRobot(Long roomId, List<Long> playerIds) {
    // MATCH-EMPTY-METHOD
  }

}
