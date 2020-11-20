package com.enuos.live.handle.game.f30051.dddd;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.codec.Packet;
import com.enuos.live.handle.game.f30051.FindSpyWord;
import com.enuos.live.manager.GroupManager;
import com.enuos.live.proto.f30051msg.F30051;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.Timeout;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

/**
 * TODO 谁是卧底.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/11/4 10:17
 */

@Data
@SuppressWarnings("WeakerAccess")
public class FindUndercoverRoom {

  /** 房间ID. */
  private Long roomId;
  /** 房间类型 0-{4-6人>1卧底} 1-{7-8人>2卧底}. */
  private Integer roomType;
  /** 房间匹配 0-开启 1-关闭. */
  private Integer roomMatch;
  /** 交流方式 0-文字 1-语音. */
  private Integer speakMode = 0;

  /** 房间回合 +1. */
  private Integer roomRound = 0;
  /** 爆词回合 y+1. */
  private Integer openRound = 0;
  /** 最大回合 x-y. */
  private Integer maxRoomRound = 0;

  /** 房间状态 0-未开始 1-已开始. */
  private Integer roomStatus = 0;
  /** 换词标记 0-未开始 1-已开始 2-已结束. */
  private Integer changeIndex = 0;
  /** 战斗标记 0-未开始 1-进行中. */
  private Integer battleIndex = 0;
  /** 处理标记 [全体成员连续无投出票]. */
  private Integer handleIndex = 0;

  /** 说话玩家. */
  private Long speakPlayer = 0L;
  /** 换词玩家. */
  private Long changePlayer = 0L;
  /** 爆词玩家. */
  private Long openWordsPlayer = 0L;

  /** 房间词汇. */
  private FindUndercoverWord roomLexicon;

  /** 描述时间. 20s */
  private LocalDateTime voteTime;
  /** 描述时间. 20s */
  private LocalDateTime speakTime;
  /** 回合时间. 40s */
  private LocalDateTime depictTime;
  /** 换词时间. 15s */
  private LocalDateTime changeTime;
  /** 爆词时间. 20s */
  private LocalDateTime openWordTime;
  /** 词汇时间. 05s */
  private LocalDateTime showWordTime;
  /** 平票时间. 20s */
  private LocalDateTime voteBattleTime;
  /** 描述时间. 20s */
  private LocalDateTime depictBattleTime;

  /** 平票玩家. */
  private List<Long> battleList = Lists.newArrayList();
  /** 卧底玩家. */
  private List<Long> undercoverList = Lists.newArrayList();
  /** 投票信息. */
  private List<FindUndercoverVote> voteList = Lists.newCopyOnWriteArrayList();
  /** 玩家列表. */
  private List<FindUndercoverPlayer> playerList = Lists.newCopyOnWriteArrayList();
  /** 观战列表. */
  private List<FindUndercoverPlayer> spectatorList = Lists.newCopyOnWriteArrayList();
  /** 观看群组. */
  private ChannelGroup spectatorGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

  /** 定时数据. */
  private HashMap<Integer, Timeout> timeOutMap = Maps.newHashMap();

  /**
   * TODO 初始房间.
   *
   * @param roomId [房间ID]
   * @param roomType [房间类型]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 13:25
   * @update 2020/11/6 13:25
   */
  FindUndercoverRoom(Long roomId, Integer roomType) {
    this.roomId = roomId;
    this.roomType = roomType;
    if (roomType == 0) {
      IntStream.range(1, UndercoverStatic.getInt(UndercoverStatic.PEOPLE_MAX_1) + 1)
          .mapToObj(FindUndercoverPlayer::new)
          .forEach(undercoverPlayer -> playerList.add(undercoverPlayer));
    } else {
      IntStream.range(1, UndercoverStatic.getInt(UndercoverStatic.PEOPLE_MAX_2) + 1)
          .mapToObj(FindUndercoverPlayer::new)
          .forEach(undercoverPlayer -> playerList.add(undercoverPlayer));
    }
  }

  /**
   * TODO 玩家信息.
   *
   * @param playerId [玩家ID]
   * @return [玩家信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 13:24
   * @update 2020/11/6 13:24
   */
  public FindUndercoverPlayer getPlayerInfo(Long playerId) {
    FindUndercoverPlayer targetplayer = playerList.stream()
        .filter(player -> player.isEquals(playerId))
        .findFirst().orElse(null);
    if (Objects.nonNull(targetplayer)) {
      targetplayer = spectatorList.stream()
          .filter(player -> player.isEquals(playerId))
          .findFirst().orElse(null);
    }
    return targetplayer;
  }

  /**
   * TODO 进入座位.
   *
   * @param channel [通信管道]
   * @param playerInfo [玩家信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 13:41
   * @update 2020/11/6 13:41
   */
  public void enterSeat(Channel channel, I10001.PlayerInfo playerInfo) {
    if (Objects.nonNull(playerInfo)) {
      for (FindUndercoverPlayer undercoverPlayer : playerList) {
        if (undercoverPlayer.getPlayerId() == 0) {
          undercoverPlayer.setPlayerId(playerInfo.getUserId());
          undercoverPlayer.setPlayerName(playerInfo.getNickName());
          undercoverPlayer.setAvatarIcon(playerInfo.getIconUrl());
          undercoverPlayer.setPlayerSex(playerInfo.getSex());
          undercoverPlayer.setChannel(channel);
          break;
        }
      }
      this.playerList.sort(Comparator.comparing(FindUndercoverPlayer::getSeatNumber));
    }
  }

  /**
   * TODO 进入观战.
   *
   * @param channel [通信管道]
   * @param playerInfo [玩家信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 13:47
   * @update 2020/11/6 13:47
   */
  public void enterSpectator(Channel channel, I10001.PlayerInfo playerInfo) {
    if (Objects.nonNull(playerInfo)) {
      FindUndercoverPlayer undercoverPlayer = new FindUndercoverPlayer(0);
      undercoverPlayer.setPlayerId(playerInfo.getUserId());
      undercoverPlayer.setPlayerName(playerInfo.getNickName());
      undercoverPlayer.setAvatarIcon(playerInfo.getIconUrl());
      undercoverPlayer.setPlayerSex(playerInfo.getSex());
      undercoverPlayer.setChannel(channel);
      undercoverPlayer.setIdentity(1);
      this.spectatorList.add(undercoverPlayer);
      this.spectatorGroup.add(channel);
    }
  }

  /**
   * TODO 加入座位.
   *
   * @param playerId [玩家ID]
   * @return [座位编号]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 13:52
   * @update 2020/11/6 13:52
   */
  public int joinSeat(Long playerId) {
    int seatNumber = 0;
    FindUndercoverPlayer playerInfo = getPlayerInfo(playerId);
    for (FindUndercoverPlayer undercoverPlayer : playerList) {
      if (undercoverPlayer.getPlayerId() == 0) {
        undercoverPlayer.setPlayerId(playerInfo.getPlayerId());
        undercoverPlayer.setPlayerName(playerInfo.getPlayerName());
        undercoverPlayer.setAvatarIcon(playerInfo.getAvatarIcon());
        undercoverPlayer.setAvatarFrame(playerInfo.getAvatarFrame());
        undercoverPlayer.setPlayerSex(playerInfo.getPlayerSex());
        undercoverPlayer.setChannel(playerInfo.getChannel());
        seatNumber = undercoverPlayer.getSeatNumber();
        break;
      }
    }
    this.spectatorList.removeIf(player -> player.isEquals(playerId));
    return seatNumber;
  }

  /**
   * TODO 加入座位.
   *
   * @param playerId [玩家ID]
   * @param seatNumber [座位编号]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 13:58
   * @update 2020/11/6 13:58
   */
  public void joinSeat(Long playerId, Integer seatNumber) {
    FindUndercoverPlayer playerInfo = getPlayerInfo(playerId);
    for (FindUndercoverPlayer undercoverPlayer : playerList) {
      if (Objects.equals(undercoverPlayer.getSeatNumber(), seatNumber)) {
        undercoverPlayer.setPlayerId(playerInfo.getPlayerId());
        undercoverPlayer.setPlayerName(playerInfo.getPlayerName());
        undercoverPlayer.setAvatarIcon(playerInfo.getAvatarIcon());
        undercoverPlayer.setAvatarFrame(playerInfo.getAvatarFrame());
        undercoverPlayer.setPlayerSex(playerInfo.getPlayerSex());
        undercoverPlayer.setChannel(playerInfo.getChannel());
        break;
      }
    }
    this.spectatorList.removeIf(player -> player.isEquals(playerId));
    this.spectatorGroup.remove(playerInfo.getChannel());
  }

  /**
   * TODO 离开座位.
   *
   * @param playerId [玩家ID]
   * @return [座位编号]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 13:58
   * @update 2020/11/6 13:58
   */
  public int leaveSeat(Long playerId) {
    FindUndercoverPlayer playerInfo = getPlayerInfo(playerId);
    int seatNumber = playerInfo.getSeatNumber();
    FindUndercoverPlayer newPlayer = new FindUndercoverPlayer(seatNumber);
    this.playerList.removeIf(player -> player.isEquals(playerId));
    this.playerList.add(newPlayer);
    this.playerList.sort(Comparator.comparing(FindUndercoverPlayer::getSeatNumber));
    FindUndercoverPlayer spectator = new FindUndercoverPlayer(0);
    spectator.setChannel(playerInfo.getChannel());
    spectator.setPlayerId(playerInfo.getPlayerId());
    spectator.setPlayerSex(playerInfo.getPlayerSex());
    spectator.setPlayerName(playerInfo.getPlayerName());
    spectator.setAvatarIcon(playerInfo.getAvatarIcon());
    spectator.setAvatarFrame(playerInfo.getAvatarFrame());
    spectator.setIdentity(1);
    this.spectatorList.add(spectator);
    this.spectatorGroup.add(playerInfo.getChannel());
    return seatNumber;
  }

  /**
   * TODO 剩余座位.
   *
   * @return [座位数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 14:29
   * @update 2020/11/6 14:29
   */
  public int remainingSeat() {
    return (int) playerList.stream()
        .filter(s -> s.getPlayerId() == 0).count();
  }

  /**
   * TODO 入座数量.
   *
   * @return [座位数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/6 14:29
   * @update 2020/11/6 14:29
   */
  public int seatedPlayersNum() {
    return (int) playerList.stream()
        .filter(s -> s.getPlayerId() > 0).count();
  }

  /**
   * TODO 入座离开.
   *
   * @return [座位数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 12:40
   * @update 2020/11/11 12:40
   */
  public int seatedLeavePlayerNum() {
    return (int) playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && (s.getChannel() == null)).count();
  }

  /**
   * TODO 未准备数.
   *
   * @return [准备数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/31 20:13
   * @update 2020/8/31 20:13
   */
  public int unprepared() {
    return (int) playerList.stream()
        .filter(player -> player.getPlayerId() > 0 && player.getPlayerStatus() == 0).count();
  }

  /**
   * TODO 已准备数.
   *
   * @return [准备数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/31 20:09
   * @update 2020/8/31 20:09
   */
  public int preparations() {
    return (int) playerList.stream()
        .filter(player -> player.getPlayerId() > 0 && player.getPlayerStatus() == 1).count();
  }

  /**
   * TODO 同意数量.
   *
   * @return [同意数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 10:29
   * @update 2020/11/9 10:29
   */
  public int agreeChangeWordsNum() {
    return (int) playerList.stream()
        .filter(player -> player.getPlayerId() > 0 && player.getChangeIndex() == 0).count();
  }

  /**
   * TODO 拒绝数量.
   *
   * @return [拒绝数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 10:30
   * @update 2020/11/9 10:30
   */
  public int disagreeChangeWordsNum() {
    return (int) playerList.stream()
        .filter(player -> player.getPlayerId() > 0 && player.getChangeIndex() == 1).count();
  }

  /**
   * TODO 刷新回合.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 15:54
   * @update 2020/11/9 15:54
   */
  public void refreshRounds() {
    this.roomRound = roomRound + 1;
  }

  /**
   * TODO 玩家状态.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/2 19:15
   */
  public void refreshPlayerStatus() {
    this.playerList.stream()
        .filter(player -> player.getPlayerId() > 0 && player.getPlayerStatus() == 1)
        .forEach(player -> player.setPlayerStatus(2));
  }

  /**
   * TODO 设置词汇.
   *
   * @param lexiconMap [词汇信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 13:17
   * @update 2020/9/1 13:17
   */
  public void setRoomLexicon(Map<String, Object> lexiconMap) {
    FindUndercoverWord undercoverWord = new FindUndercoverWord();
    undercoverWord.setLexiconNo((Integer) lexiconMap.get("lexiconNo"));
    undercoverWord.setLexiconMass(StringUtils.nvl(lexiconMap.get("lexiconMass")));
    undercoverWord.setLexiconSpy(StringUtils.nvl(lexiconMap.get("lexiconSpy")));
    this.roomLexicon = undercoverWord;
  }

  /**
   * TODO 最大回合.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 16:50
   * @update 2020/11/9 16:50
   */
  public void setMaxGameRound() {
    //0-[4-6人>1卧底] 1-[7-8人>2卧底]
    if (roomType == 0) {
      this.openRound = 2;
      this.maxRoomRound = seatedPlayersNum() - 1;
    } else {
      this.openRound = 3;
      this.maxRoomRound = seatedPlayersNum() - 1;
    }
  }

  /**
   * TODO 下一玩家.
   *
   * @param nowPlayer [当前玩家]
   * @return [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 17:24
   * @update 2020/11/9 17:24
   */
  public long getNextPlayer(Long nowPlayer) {
    List<FindUndercoverPlayer> undercoverPlayerList = playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
        ).sorted(Comparator.comparing(FindUndercoverPlayer::getSeatNumber))
        .collect(Collectors.toList());
    int playerSize = undercoverPlayerList.size();
    int playerIndex = 0;
    for (FindUndercoverPlayer player : undercoverPlayerList) {
      if (player.isEquals(nowPlayer)) {
        break;
      }
      playerIndex++;
    }
    if (playerIndex == playerSize - 1) {
      return 0L;
    } else {
      return undercoverPlayerList.get(playerIndex + 1).getPlayerId();
    }
  }

  /**
   * TODO 设置玩家.
   *
   * @param oldPlayer [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 17:28
   * @update 2020/11/9 17:28
   */
  public void setUpSpeakPlayer(Long oldPlayer) {
    List<FindUndercoverPlayer> undercoverPlayerList = playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
        ).sorted(Comparator.comparing(FindUndercoverPlayer::getSeatNumber))
        .collect(Collectors.toList());
    if (oldPlayer == 0) {
      this.speakPlayer = undercoverPlayerList.get(0).getPlayerId();
    } else {
      int playerIndex = 0;
      for (FindUndercoverPlayer player : undercoverPlayerList) {
        if (player.isEquals(oldPlayer)) {
          break;
        }
        playerIndex++;
      }
      this.speakPlayer = undercoverPlayerList.get(playerIndex + 1).getPlayerId();
    }
  }

  /**
   * TODO 补全描述.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/9 17:55
   * @update 2020/11/9 17:55
   */
  public void completionDepict() {
    List<FindUndercoverPlayer> undercoverPlayerList = playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
            && StringUtils.isEmpty(player.getSpeakWords())
        ).collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(undercoverPlayerList)) {
      F30051.F300517S2C.Builder builder;
      for (FindUndercoverPlayer player : undercoverPlayerList) {
        builder = F30051.F300517S2C.newBuilder();
        builder.setWord("......");
        builder.setUserID(player.getPlayerId());
        builder.setSpeakPlayer(player.getSeatNumber());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_FIND_UNDERCOVER, FindUndercoverCmd.WORDS_DESCRIPTION,
                builder.build().toByteArray()), roomId);
      }
    }
  }

  /**
   * TODO 玩家投票.
   *
   * @param targetId [目标ID]
   * @param userId [玩家ID]
   * @param iconUrl [玩家头像]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 11:19
   * @update 2020/11/10 11:19
   */
  public void playerVote(Long targetId, Long userId, String iconUrl) {
    FindUndercoverVote voteInfo = voteList.stream()
        .filter(vote -> vote.isEquals(targetId))
        .findFirst().orElse(null);
    if (Objects.nonNull(voteInfo)) {
      voteInfo.wasVoted(userId, iconUrl);
    }
  }

  /**
   * TODO 剩余间谍.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 16:47
   * @update 2020/11/10 16:47
   */
  public int spiesRemaining() {
    return (int) playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            // 用户状态 0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开
            && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
            // 游戏身份 0-未分配 1-平民 2-卧底
            && player.getGameIdentity() == 2).count();
  }

  /**
   * TODO 剩余平民.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 16:47
   * @update 2020/11/10 16:47
   */
  public int civiliansRemaining() {
    return (int) playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            // 用户状态 0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开
            && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
            // 游戏身份 0-未分配 1-平民 2-卧底
            && player.getGameIdentity() == 1).count();
  }

  /**
   * TODO 回合初始.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/10 16:54
   * @update 2020/11/10 16:54
   */
  public void roundInit() {
    this.playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            // 用户状态 0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开
            && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
        ).forEach(player -> {
          player.setOpenWords(null);
          player.setSpeakWords(null);
          player.setVoteIndex(0);
          player.setBattleIndex(0);
          player.setSurvivalTimes(player.getSurvivalTimes() + 1);
    });
    this.openWordsPlayer = 0L;
    this.speakPlayer = 0L;
    this.voteList = Lists.newCopyOnWriteArrayList();
  }

  /**
   * TODO 分配身份.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 13:17
   * @update 2020/9/1 13:17
   */
  public void assignIdentity() {
    List<FindUndercoverPlayer> undercoverPlayers = playerList.stream()
        .filter(player -> player.getPlayerId() > 0)
        .sorted(Comparator.comparing(FindUndercoverPlayer::getSeatNumber))
        .collect(Collectors.toList());
    List<FindUndercoverPlayer> nullSeat = playerList.stream()
        .filter(player -> player.getPlayerId() == 0)
        .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(nullSeat)) {
      this.playerList = nullSeat;
    }
    // 房间类型 0-{4-6人>1卧底} 1-{7-8人>2卧底}
    if (roomType == 0) {
      int random = ThreadLocalRandom.current().nextInt(undercoverPlayers.size());
      FindUndercoverPlayer undercoverPlayer = undercoverPlayers.get(random);
      // 游戏身份 0-未分配 1-平民 2-卧底
      undercoverPlayer.setGameIdentity(2);
      // 加入间谍
      this.undercoverList.add(undercoverPlayer.getPlayerId());
      this.playerList.add(undercoverPlayer);
      undercoverPlayers.removeIf(player -> player.isEquals(undercoverPlayer.getPlayerId()));
      undercoverPlayers.forEach(player -> player.setGameIdentity(1));
      this.playerList.addAll(undercoverPlayers);
      this.playerList.sort(Comparator.comparing(FindUndercoverPlayer::getSeatNumber));
    } else {
      int random = ThreadLocalRandom.current().nextInt(undercoverPlayers.size());
      FindUndercoverPlayer undercoverPlayer1 = undercoverPlayers.get(random);
      // 游戏身份 0-未分配 1-平民 2-卧底
      undercoverPlayer1.setGameIdentity(2);
      // 加入间谍 1
      this.undercoverList.add(undercoverPlayer1.getPlayerId());
      this.playerList.add(undercoverPlayer1);
      undercoverPlayers.removeIf(player -> player.isEquals(undercoverPlayer1.getPlayerId()));
      random = ThreadLocalRandom.current().nextInt(undercoverPlayers.size());
      FindUndercoverPlayer undercoverPlayer2 = undercoverPlayers.get(random);
      // 游戏身份 0-未分配 1-平民 2-卧底
      undercoverPlayer2.setGameIdentity(2);
      // 加入间谍 2
      this.undercoverList.add(undercoverPlayer2.getPlayerId());
      this.playerList.add(undercoverPlayer2);
      undercoverPlayers.removeIf(player -> player.isEquals(undercoverPlayer2.getPlayerId()));
      undercoverPlayers.forEach(player -> player.setGameIdentity(1));
      this.playerList.addAll(undercoverPlayers);
      this.playerList.sort(Comparator.comparing(FindUndercoverPlayer::getSeatNumber));
    }
  }


  /**
   * TODO 玩家离开.
   *
   * @param playerId [玩家ID]
   * @param identity [玩家身份]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 12:49
   * @update 2020/11/11 12:49
   */
  public void leaveGame(Long playerId, Integer identity, Integer seatNo) {
    FindUndercoverPlayer undercoverPlayer = getPlayerInfo(playerId);
    // 玩家身份 0-玩家 1-观众
    if (identity == 0) {
      this.playerList.removeIf(player -> player.isEquals(playerId));
      FindUndercoverPlayer newPlayer = new FindUndercoverPlayer(seatNo);
      this.playerList.add(newPlayer);
      this.playerList.sort(Comparator.comparing(FindUndercoverPlayer::getSeatNumber));
    } else {
      this.spectatorGroup.remove(undercoverPlayer.getChannel());
      this.spectatorList.removeIf(player -> player.isEquals(playerId));
    }
  }

  /**
   * TODO 头像列表.
   *
   * @return [头像列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 13:40
   * @update 2020/11/11 13:40
   */
  public List<String> getSpectatorIcon() {
    return spectatorList.stream().map(FindUndercoverPlayer::getAvatarIcon)
        .limit(50).collect(Collectors.toList());
  }

  /**
   * TODO 目标座位.
   *
   * @param seat [座位编号]
   * @return [座位信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 14:16
   * @update 2020/11/11 14:16
   */
  public FindUndercoverPlayer getTargetSeat(Integer seat) {
    return playerList.stream()
        .filter(player -> player.getSeatNumber().equals(seat))
        .findFirst().orElse(null);
  }

  /**
   * TODO 可以投票.
   *
   * @return [玩家列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 16:28
   * @update 2020/11/11 16:28
   */
  public List<Long> whoCanVote() {
    this.voteList = Lists.newCopyOnWriteArrayList();
    List<FindUndercoverPlayer> undercoverPlayers = playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            // 用户状态 0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开
            && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
        ).collect(Collectors.toList());
    List<Long> userIds = Lists.newLinkedList();
    undercoverPlayers.forEach(player -> {
      userIds.add(player.getPlayerId());
      FindUndercoverVote undercoverVote = new FindUndercoverVote(player.getPlayerId());
      this.voteList.add(undercoverVote);
    });
    return userIds;
  }

  /**
   * TODO 可以投票.
   *
   * @param userIds [描述玩家]
   * @return [玩家列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 16:37
   * @update 2020/11/11 16:37
   */
  public List<Long> whoCanVote(List<Long> userIds) {
    this.voteList = Lists.newCopyOnWriteArrayList();
    userIds.forEach(playerId -> {
      FindUndercoverVote undercoverVote = new FindUndercoverVote(playerId);
      this.voteList.add(undercoverVote);
    });
    return playerList.stream()
        .filter(player ->player.getPlayerId() > 0
            && !userIds.contains(player.getPlayerId())
            // 用户状态 0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开
            && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
        ).map(FindUndercoverPlayer::getPlayerId).collect(Collectors.toList());
  }

  /**
   * TODO 投票计算.
   *
   * @return [计算数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 17:26
   * @update 2020/11/11 17:26
   */
  public List<Long> voteCalculation() {
    // 按票数排序 高 -> 低
    this.voteList.sort(Comparator.comparing(FindUndercoverVote::getVoteSize).reversed());
    if (CollectionUtils.isNotEmpty(voteList)) {
      List<Long> userIds = Lists.newLinkedList();
      FindUndercoverVote undercoverVote = voteList.get(0);
      if (undercoverVote.getVoteSize() == 0) {
        // 没有玩家投票 添加处理标记
        this.handleIndex  = handleIndex + 1;
      }
      voteList.stream()
          // 筛选票数相同的玩家
          .filter(vote -> vote.getVoteSize().equals(undercoverVote.getVoteSize()))
          .forEach(vote -> userIds.add(vote.getUserId()));
      this.battleList = userIds;
      return userIds;
    }
    return Collections.emptyList();
  }


  /**
   * TODO 初始描述.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 17:42
   * @update 2020/11/11 17:42
   */
  public void initSpeakWords() {
    this.playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            // 用户状态 0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开
            && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
        ).forEach(player -> {
          player.setOpenWords(null);
          player.setSpeakWords(null);
    });
  }











































































  /**
   * TODO 添加定时.
   *
   * @param taskId [任务ID].
   * @param timeout [定时任务].
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public void addTimeOut(int taskId, Timeout timeout) {
    if (timeOutMap.containsKey(taskId)) {
      return;
    }
    timeOutMap.put(taskId, timeout);
  }

  /**
   * TODO 取消定时.
   *
   * @param taskId [任务ID].
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public void cancelTimeOut(int taskId) {
    if (timeOutMap.containsKey(taskId)) {
      timeOutMap.get(taskId).cancel();
      timeOutMap.remove(taskId);
    }
  }

  /**
   * TODO 移除定时.
   *
   * @param taskId [任务ID].
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public void removeTimeOut(int taskId) {
    if (!timeOutMap.containsKey(taskId)) {
      return;
    }
    timeOutMap.remove(taskId);
  }

  /**
   * TODO 销毁定时.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public void destroy() {
    timeOutMap.values().forEach(Timeout::cancel);
    timeOutMap.clear();
  }

}
