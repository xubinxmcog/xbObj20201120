package com.enuos.live.handle.match.f30291;

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
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.utils.RoomUtils;
import com.enuos.live.utils.JsonUtils;
import com.enuos.live.manager.LoggerManager;
import com.enuos.live.utils.RedisUtils;
import io.netty.channel.Channel;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * TODO 炸弹猫咪.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V2.0.0
 * @since 2020/8/27 18:28
 */

@Component
@AbstractAction(cmd = 100291)
public class F30291Match extends AbstractActionHandler {

  @Resource
  private UserRemote userRemote;
  @Resource
  private RedisUtils redisUtils;

  @Override
  public void handle(Channel channel, Packet packet) {
    try {
      F20001.F200017S2C.Builder builder = F20001.F200017S2C.newBuilder();
      // 获得金币&钻石
      Result result = this.userRemote.getCurrency(packet.userId);
      if (Objects.nonNull(result)) {
        Map<String, Object> currencyInfo = JsonUtils.toObjectMap(result.getData());
        if (Objects.nonNull(currencyInfo)) {
          Integer gold = (Integer) currencyInfo.get("gold");
          if (gold >= 20) {
            if (MatchManager.MATCH_EXPLODING_KITTENS.size() > 0) {
              MatchRoom matchRoom = MatchManager.MATCH_EXPLODING_KITTENS.get(0);
              matchRoom.setPeopleNum(matchRoom.getPeopleNum() - 1);
              if (matchRoom.getPeopleNum() == 0) {
                MatchManager.delExplodingKittensMatch(matchRoom.getRoomId());
              }
              builder.setRoomId(matchRoom.getRoomId());
              channel.writeAndFlush(new Packet(ActionCmd.GAME_MATCH, MatchCmd.POKER_MATCH,
                  builder.build().toByteArray()));
              I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
              joinInfo.setRoomId(matchRoom.getRoomId());
              joinInfo.setGameId(30291L);
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
              matchRoom.setPeopleNum(4);
              builder.setRoomId(matchRoom.getRoomId());
              channel.writeAndFlush(new Packet(ActionCmd.GAME_MATCH, MatchCmd.POKER_MATCH,
                  builder.build().toByteArray()));
              MatchManager.refreshExplodingKittensMatch(matchRoom);
              I10001.JoinGame.Builder joinInfo = I10001.JoinGame.newBuilder();
              joinInfo.setRoomId(matchRoom.getRoomId());
              joinInfo.setGameId(30291L);
              byte[] joinByte = joinInfo.build().toByteArray();
              this.redisUtils.setByte(GameKey.KEY_GAME_JOIN_RECORD.getName() + packet.userId, joinByte);
              I10001.RoomRecord.Builder roomRecord = I10001.RoomRecord.newBuilder();
              roomRecord.setGameId(30291L).setRoomId(matchRoom.getRoomId()).setOpenWay(0).setRoomType(0)
                  .setGameMode(0).setSpeakMode(0).setGameNumber(0).setGameSession(0);
              byte[] roomByte = roomRecord.build().toByteArray();
              this.redisUtils.setByte(GameKey.KEY_GAME_ROOM_RECORD.getName() + roomId, roomByte);
            }
          } else {
            builder.setError("无法加入游戏.您的游戏金币不足20！").setRoomId(0);
            channel.writeAndFlush(new Packet(ActionCmd.GAME_MATCH, MatchCmd.POKER_MATCH,
                builder.build().toByteArray()));
          }
        } else {
          builder.setError("当前游戏服务正在维护.请稍后尝试！").setRoomId(0);
          channel.writeAndFlush(new Packet(ActionCmd.GAME_MATCH, MatchCmd.POKER_MATCH,
              builder.build().toByteArray()));
        }
      } else {
        builder.setError("当前游戏服务正在维护.请稍后尝试！").setRoomId(0);
        channel.writeAndFlush(new Packet(ActionCmd.GAME_MATCH, MatchCmd.POKER_MATCH,
            builder.build().toByteArray()));
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
