package com.enuos.live.handle.game.f30051;

import com.enuos.live.action.ActionCmd;
import com.enuos.live.codec.Packet;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.manager.GroupManager;
import com.enuos.live.manager.ChatManager;
import com.enuos.live.proto.f30051msg.F30051;
import com.enuos.live.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.Timeout;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.time.LocalDateTime;
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
 * @version v2.2.0
 * @since 2020/7/1 9:01
 */

@Data
@SuppressWarnings("WeakerAccess")
public class FindSpyRoom {
  /** 房间ID. */
  private Long roomId;
  /** 开放方式 [0-公开 1-创建]. */
  private Integer openWay;
  /** 房间类型 [0-{4-6人>1卧底} 1-{7-8人>2卧底}]. */
  private Integer roomType;
  /** 交流方式 [0-文字 1-语音]. */
  private Integer speakMode = 0;
  /** 房间回合 +1. */
  private Integer roomRound = 0;
  /** 爆词回合 y+1. */
  private Integer openRound = 0;
  /** 最大回合 x-y. */
  private Integer maxRoomRound = 0;
  /** 房间状态 [0-未开始 1-已开始]. */
  private Integer roomStatus = 0;
  /** 换词玩家. */
  private Long changeUser = 0L;
  /** 换词标记 [0-未开始 1-已开始 2-已结束]. */
  private Integer changeIndex = 0;
  /** 战斗标记 [0-未开始 1-进行中]. */
  private Integer battleIndex = 0;
  /** 爆词玩家. */
  private Long openWordsUser = 0L;
  /** 当前玩家[speakMode = 1]. */
  private Long lastActionId = 0L;
  /** 房间词汇. */
  private FindSpyWord roomLexicon;
  /** 回合时间. */
  private LocalDateTime roundTime;
  /** 特殊时间. */
  private LocalDateTime specialTime;
  /** 处理标记 [全体成员连续无投出票]. */
  private Integer handleIndex = 0;
  /** 临时数据(可以描述玩家-> 平票). */
  private List<Long> tempList = Lists.newArrayList();
  /** 卧底玩家. */
  private List<Long> spyPlayerList = Lists.newArrayList();
  /** 投票信息. */
  private List<FindSpyVote> voteList = Lists.newCopyOnWriteArrayList();
  /** 玩家列表. */
  private List<FindSpyPlayer> playerList = Lists.newCopyOnWriteArrayList();
  /** 观看列表. */
  private List<FindSpyPlayer> audienceList = Lists.newCopyOnWriteArrayList();
  /** 观看群组. */
  private ChannelGroup audienceGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
  /** 定时数据. */
  private HashMap<Integer, Timeout> timeOutMap = Maps.newHashMap();

  /**
   * TODO 初始房间.
   *
   * @param roomId [房间ID]
   * @param roomType [房间类型]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  FindSpyRoom(Long roomId, Integer roomType) {
    this.roomId = roomId;
    this.roomType = roomType;
    if (roomType == 0) {
      IntStream.range(1, FindSpyAssets.getInt(FindSpyAssets.PEOPLE_MAX_1) + 1)
          .mapToObj(FindSpyPlayer::new).forEach(findSpyPlayer -> playerList.add(findSpyPlayer));
    } else {
      IntStream.range(1, FindSpyAssets.getInt(FindSpyAssets.PEOPLE_MAX_2) + 1)
          .mapToObj(FindSpyPlayer::new).forEach(findSpyPlayer -> playerList.add(findSpyPlayer));
    }
  }

  /**
   * TODO 玩家信息.
   *
   * @param playerId [玩家ID]
   * @return [玩家信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public FindSpyPlayer getPlayerInfo(Long playerId) {
    FindSpyPlayer partaker = playerList.stream()
        .filter(player -> player.isBoolean(playerId))
        .findFirst().orElse(null);
    if (Objects.isNull(partaker)) {
      partaker = audienceList.stream()
          .filter(audience -> audience.isBoolean(playerId))
          .findFirst().orElse(null);
    }
    return partaker;
  }

  /**
   * TODO 进入座位.
   *
   * @param channel [通信管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/24 13:34
   * @update 2020/8/24 13:34
   */
  public void enterSeat(Channel channel, I10001.PlayerInfo playerInfo) {
    if (Objects.nonNull(playerInfo)) {
      for (FindSpyPlayer findSpyPlayer : playerList) {
        if (findSpyPlayer.getPlayerId() == 0) {
          findSpyPlayer.setPlayerId(playerInfo.getUserId());
          findSpyPlayer.setPlayerName(playerInfo.getNickName());
          findSpyPlayer.setPlayerAvatar(playerInfo.getIconUrl());
          findSpyPlayer.setPlayerSex(playerInfo.getSex());
          findSpyPlayer.setChannel(channel);
          break;
        }
      }
      this.playerList.sort(Comparator.comparing(FindSpyPlayer::getSeatNumber));
    }
  }

  /**
   * TODO 进入观战.
   *
   * @param channel [通信管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/17 14:22
   * @update 2020/8/17 14:22
   */
  public void enterAudience(Channel channel, I10001.PlayerInfo playerInfo) {
    if (Objects.nonNull(playerInfo)) {
      FindSpyPlayer player = new FindSpyPlayer(0);
      player.setPlayerId(playerInfo.getUserId());
      player.setPlayerName(playerInfo.getNickName());
      player.setPlayerAvatar(playerInfo.getIconUrl());
      player.setPlayerSex(playerInfo.getSex());
      player.setChannel(channel);
      player.setIdentity(1);
      this.audienceList.add(player);
      this.audienceGroup.add(channel);
    }
  }

  /**
   * TODO 加入座位.
   *
   * @param playerId [玩家ID]
   * @return [座位编号]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public int joinSeat(Long playerId) {
    int seat = 0;
    FindSpyPlayer audienceInfo = getPlayerInfo(playerId);
    for (FindSpyPlayer playerInfo : playerList) {
      if (playerInfo.getPlayerId() == 0) {
        playerInfo.setPlayerId(audienceInfo.getPlayerId());
        playerInfo.setPlayerName(audienceInfo.getPlayerName());
        playerInfo.setPlayerAvatar(audienceInfo.getPlayerAvatar());
        playerInfo.setPlayerSex(audienceInfo.getPlayerSex());
        playerInfo.setAvatarFrame(audienceInfo.getAvatarFrame());
        playerInfo.setIdentity(0);
        playerInfo.setChannel(audienceInfo.getChannel());
        playerInfo.setPlayerStatus(0);
        playerInfo.setChangeAction(2);
        seat = playerInfo.getSeatNumber();
        break;
      }
    }
    this.audienceGroup.remove(audienceInfo.getChannel());
    this.audienceList.removeIf(player -> player.getPlayerId().equals(playerId));
    return seat;
  }

  /**
   * TODO 加入座位.
   *
   * @param playerId [玩家ID]
   * @param seat [座位编号]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public void joinSeat(Long playerId, Integer seat) {
    FindSpyPlayer playerInfo = getPlayerInfo(playerId);
    if (playerInfo.getIdentity() == 0) {
      int oldSeat = playerInfo.getSeatNumber();
      this.playerList.removeIf(s -> s.getSeatNumber().equals(seat));
      playerInfo.setSeatNumber(seat);
      this.playerList.removeIf(s -> s.getSeatNumber().equals(oldSeat));
      FindSpyPlayer newPlayer = new FindSpyPlayer(oldSeat);
      this.playerList.add(newPlayer);
      this.playerList.sort(Comparator.comparing(FindSpyPlayer::getSeatNumber));
    } else {
      for (FindSpyPlayer findSpyPlayer : playerList) {
        if (findSpyPlayer.getSeatNumber().equals(seat)) {
          findSpyPlayer.setPlayerId(playerInfo.getPlayerId());
          findSpyPlayer.setPlayerName(playerInfo.getPlayerName());
          findSpyPlayer.setPlayerAvatar(playerInfo.getPlayerAvatar());
          findSpyPlayer.setPlayerSex(playerInfo.getPlayerSex());
          findSpyPlayer.setChannel(playerInfo.getChannel());
          findSpyPlayer.setAvatarFrame(playerInfo.getAvatarFrame());
          findSpyPlayer.setPlayerStatus(0);
          findSpyPlayer.setIdentity(0);
          break;
        }
      }
      this.audienceGroup.remove(playerInfo.getChannel());
      this.audienceList.removeIf(player -> player.getPlayerId().equals(playerId));
    }
  }

  /**
   * TODO 离开座位.
   *
   * @param playerId [玩家ID]
   * @return [座位编号]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 12:44
   * @update 2020/9/1 12:44
   */
  public int leaveSeat(Long playerId) {
    FindSpyPlayer findSpyPlayer = getPlayerInfo(playerId);
    int seat = findSpyPlayer.getSeatNumber();
    this.playerList.removeIf(player -> player.getPlayerId().equals(playerId));
    FindSpyPlayer newFindSpyPlayer = new FindSpyPlayer(seat);
    this.playerList.add(newFindSpyPlayer);
    this.playerList.sort(Comparator.comparing(FindSpyPlayer::getSeatNumber));
    FindSpyPlayer audience = new FindSpyPlayer(0);
    audience.setPlayerId(findSpyPlayer.getPlayerId());
    audience.setPlayerName(findSpyPlayer.getPlayerName());
    audience.setPlayerAvatar(findSpyPlayer.getPlayerAvatar());
    audience.setPlayerSex(findSpyPlayer.getPlayerSex());
    audience.setAvatarFrame(findSpyPlayer.getAvatarFrame());
    audience.setChannel(findSpyPlayer.getChannel());
    audience.setIdentity(1);
    this.audienceList.add(audience);
    this.audienceGroup.add(findSpyPlayer.getChannel());
    return newFindSpyPlayer.getSeatNumber();
  }

  /**
   * TODO 设置玩家.
   *
   * @param old [当前玩家]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 12:54
   * @update 2020/9/1 12:54
   */
  public void setActionPlayer(Long old) {
    List<FindSpyPlayer> findSpyPlayers = playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            // 玩家标记 [0-游戏中 1-已出局]
            && player.getPlayerIndex() == 0
            // 玩家身份 [0-玩家 1-观众]
            && player.getIdentity() == 0
            // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
            && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
            // 连接状态 [0-连接中 1-已断开]
            && (player.getLinkStatus() == 0 || player.getLinkStatus() == 1)
        ).sorted(Comparator.comparing(FindSpyPlayer::getSeatNumber))
        .collect(Collectors.toList());
    if (old == 0) {
      this.lastActionId = findSpyPlayers.get(0).getPlayerId();
    } else {
      int playerIndex = 0;
      for (FindSpyPlayer findSpyPlayer : findSpyPlayers) {
        if (findSpyPlayer.isBoolean(old)) {
          break;
        }
        playerIndex++;
      }
      if (playerIndex == findSpyPlayers.size() - 1) {
        this.lastActionId = findSpyPlayers.get(0).getPlayerId();
      } else {
        this.lastActionId = findSpyPlayers.get(playerIndex + 1).getPlayerId();
      }
    }
  }

  /**
   * TODO 下一玩家.
   *
   * @param old [上一玩家]
   * @return [下一玩家]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/31 19:07
   * @update 2020/8/31 19:07
   */
  public long getNextActionPlayer(Long old) {
    List<FindSpyPlayer> findSpyPlayers = playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            // 玩家标记 [0-游戏中 1-已出局]
            && player.getPlayerIndex() == 0
            // 玩家身份 [0-玩家 1-观众]
            && player.getIdentity() == 0
            // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
            && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
            // 连接状态 [0-连接中 1-已断开]
            && (player.getLinkStatus() == 0 || player.getLinkStatus() == 1)
        ).sorted(Comparator.comparing(FindSpyPlayer::getSeatNumber))
        .collect(Collectors.toList());
    int playerIndex = 0;
    for (FindSpyPlayer findSpyPlayer : findSpyPlayers) {
      if (findSpyPlayer.isBoolean(old)) {
        break;
      }
      playerIndex++;
    }
    List<Long> players = Lists.newLinkedList();
    findSpyPlayers.forEach(player -> players.add(player.getPlayerId()));
    if (playerIndex == players.size() - 1) {
      return players.get(0);
    } else {
      return players.get(playerIndex + 1);
    }
  }

  /**
   * TODO 头像列表.
   *
   * @return [头像列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/31 19:14
   * @update 2020/8/31 19:14
   */
  public List<String> getAudienceIcon() {
    return audienceList.stream().map(FindSpyPlayer::getPlayerAvatar)
        .collect(Collectors.toCollection(Lists::newLinkedList));
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
   * TODO 剩余座位.
   *
   * @return [座位数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/31 20:17
   * @update 2020/8/31 20:17
   */
  public int remainingSeat() {
    return (int) playerList.stream()
        .filter(s -> s.getPlayerId() == 0).count();
  }

  /**
   * TODO 入座玩家.
   *
   * @return [玩家列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/31 20:19
   * @update 2020/8/31 20:19
   */
  public List<Long> seatedPlayers() {
    List<FindSpyPlayer> findSpyPlayers = playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getLinkStatus() == 0)
        .collect(Collectors.toList());
    List<Long> userIds = Lists.newArrayList();
    if (CollectionUtils.isNotEmpty(findSpyPlayers)) {
      findSpyPlayers.forEach(s -> userIds.add(s.getPlayerId()));
    }
    return userIds;
  }

  /**
   * TODO 入座数量.
   *
   * @return [玩家数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/31 20:19
   * @update 2020/8/31 20:19
   */
  public int seatedPlayersNum() {
    return (int) playerList.stream()
        .filter(s -> s.getPlayerId() > 0).count();
  }

  /**
   * TODO 入座断开.
   *
   * @return [玩家数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/14 14:58
   * @update 2020/10/14 14:58
   */
  public int seatedPlayersIsDisconnected() {
    return (int) playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getLinkStatus() == 1).count();
  }

  /**
   * TODO 刷新回合.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 12:54
   * @update 2020/9/1 12:54
   */
  public void refreshRounds() {
    this.roomRound = roomRound + 1;
  }

  /**
   * TODO 最大回合.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/2 19:15
   * @update 2020/9/2 19:15
   */
  public void setMaxGameRound() {
    //0-[4-6人>1卧底] 1-[7-8人>2卧底]
    if (roomType == 0) {
      this.openRound = 2;
      this.maxRoomRound = seatedPlayers().size() - 1;
    } else {
      this.openRound = 3;
      this.maxRoomRound = seatedPlayers().size() - 1;
    }
  }

  /**
   * TODO 回合初始.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public void roundInit() {
    this.playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            // 玩家标记 [0-游戏中 1-已出局]
            && player.getPlayerIndex() == 0
            // 玩家身份 [0-玩家 1-观众]
            && player.getIdentity() == 0
            // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
            && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
            // 连接状态 [0-连接中 1-已断开]
            && (player.getLinkStatus() == 0 || player.getLinkStatus() == 1)
        ).forEach(player -> {
          player.setOpenWords(null);
          player.setSpeakWords(null);
          player.setPlayerVoteIndex(0);
          player.setPlayerLiveOn(player.getPlayerLiveOn() + 1); });
    this.openWordsUser = 0L;
    this.lastActionId = 0L;
    this.spyPlayerList = Lists.newArrayList();
    this.voteList = Lists.newCopyOnWriteArrayList();
  }

  /**
   * TODO 初始描述.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public void initSpeakWords() {
    this.playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            // 玩家标记 [0-游戏中 1-已出局]
            && player.getPlayerIndex() == 0
            // 玩家身份 [0-玩家 1-观众]
            && player.getIdentity() == 0
            // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
            && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
            // 连接状态 [0-连接中 1-已断开]
            && (player.getLinkStatus() == 0 || player.getLinkStatus() == 1)
        ).forEach(player -> {
          player.setOpenWords(null);
          player.setSpeakWords(null);
        });
  }

  /**
   * TODO 初始房间.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public void initRoomInfo() {
    destroy();
    // 房间回合
    this.roomRound = 0;
    // 最大回合
    this.maxRoomRound = 0;
    // 房间状态
    this.roomStatus = 0;
    // 换词玩家
    this.changeUser = 0L;
    // 换词标记
    this.changeIndex = 0;
    // 当前玩家
    this.lastActionId = 0L;
    // 房间词汇
    this.roomLexicon = null;
    this.tempList = Lists.newArrayList();
    // 投票玩家
    this.spyPlayerList = Lists.newArrayList();
    // 投票信息
    this.voteList = Lists.newCopyOnWriteArrayList();
    // 玩家列表
    this.playerList.stream()
        .filter(player -> player.getPlayerId() > 0)
        .forEach(FindSpyPlayer::finishInit);
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
    FindSpyWord findSpyWord = new FindSpyWord();
    findSpyWord.setLexiconNo((Integer) lexiconMap.get("lexiconNo"));
    findSpyWord.setLexiconMass(StringUtils.nvl(lexiconMap.get("lexiconMass")));
    findSpyWord.setLexiconSpy(StringUtils.nvl(lexiconMap.get("lexiconSpy")));
    this.roomLexicon = findSpyWord;
  }

  /**
   * TODO 分配身份.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 13:17
   * @update 2020/9/1 13:17
   */
  public void assignIdentity() {
    //房间类型 0「4-6人»1卧底」 1「7-8人»2卧底」.
    if (roomType == 0) {
      // 一个卧底
      List<FindSpyPlayer> findSpyPlayers = playerList.stream()
          .filter(s -> s.getPlayerId() > 0)
          .sorted(Comparator.comparing(FindSpyPlayer::getSeatNumber))
          .collect(Collectors.toList());
      findSpyPlayers.forEach(player -> playerList.removeIf(s -> s.isBoolean(player.getPlayerId())));
      int random = ThreadLocalRandom.current().nextInt(findSpyPlayers.size());
      FindSpyPlayer spyPlayer = findSpyPlayers.get(random);
      spyPlayer.setGameIdentity(2);
      // 加入间谍名单
      this.spyPlayerList.add(spyPlayer.getPlayerId());
      findSpyPlayers.removeIf(s -> s.isBoolean(spyPlayer.getPlayerId()));
      findSpyPlayers.forEach(s -> s.setGameIdentity(1));
      findSpyPlayers.add(spyPlayer);
      this.playerList.addAll(findSpyPlayers);
      this.playerList.sort(Comparator.comparing(FindSpyPlayer::getSeatNumber));
    } else {
      // 两个卧底
      List<FindSpyPlayer> findSpyPlayers = playerList.stream()
          .filter(s -> s.getPlayerId() > 0)
          .sorted(Comparator.comparing(FindSpyPlayer::getSeatNumber))
          .collect(Collectors.toList());
      findSpyPlayers.forEach(player -> playerList.removeIf(s -> s.isBoolean(player.getPlayerId())));
      int random = ThreadLocalRandom.current().nextInt(findSpyPlayers.size());
      FindSpyPlayer spyPlayerOne = findSpyPlayers.get(random);
      spyPlayerOne.setGameIdentity(2);
      // 间谍1号
      this.spyPlayerList.add(spyPlayerOne.getPlayerId());
      findSpyPlayers.removeIf(s -> s.isBoolean(spyPlayerOne.getPlayerId()));
      random = ThreadLocalRandom.current().nextInt(findSpyPlayers.size());
      FindSpyPlayer spyPlayerTwo = findSpyPlayers.get(random);
      spyPlayerTwo.setGameIdentity(2);
      // 间谍2号
      this.spyPlayerList.add(spyPlayerTwo.getPlayerId());
      findSpyPlayers.removeIf(s -> s.isBoolean(spyPlayerTwo.getPlayerId()));
      findSpyPlayers.forEach(s -> s.setGameIdentity(1));
      findSpyPlayers.add(spyPlayerOne);
      findSpyPlayers.add(spyPlayerTwo);
      this.playerList.addAll(findSpyPlayers);
      this.playerList.sort(Comparator.comparing(FindSpyPlayer::getSeatNumber));
    }
  }

  /**
   * TODO 清除离线.
   *
   * @return [离线玩家]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/17 15:59
   * @update 2020/8/17 15:59
   */
  public List<Long> offlinePlayers() {
    List<FindSpyPlayer> unoPlayerList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getLinkStatus() == 1)
        .collect(Collectors.toList());
    List<Long> clearList = Lists.newArrayList();
    List<Integer> seatList = Lists.newArrayList();
    if (CollectionUtils.isNotEmpty(unoPlayerList)) {
      unoPlayerList.forEach(player -> {
        clearList.add(player.getPlayerId());
        seatList.add(player.getSeatNumber());
        GroupManager.removeChannel(roomId, player.getChannel());
        ChatManager.removeChatChannel(roomId, player.getChannel());
      });
    }
    clearList.forEach(playerId -> this.playerList.removeIf(s -> s.isBoolean(playerId)));
    if (CollectionUtils.isNotEmpty(seatList)) {
      seatList.forEach(integer -> {
        FindSpyPlayer newPlayer = new FindSpyPlayer(integer);
        this.playerList.add(newPlayer);
      });
      // 排序
      this.playerList.sort(Comparator.comparing(FindSpyPlayer::getSeatNumber));
    }
    return clearList;
  }

  /**
   * TODO 刷新状态.
   *
   * @param status [状态值]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public void refreshRoomStatus(Integer status) {
    this.roomStatus = status;
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
   * TODO 玩家离开.
   *
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/20 13:53
   * @update 2020/8/20 13:53
   */
  public void leaveGame(Long playerId, Integer identity) {
    FindSpyPlayer player = getPlayerInfo(playerId);
    if (identity == 0) {
      int seat = player.getSeatNumber();
      this.playerList.removeIf(s -> s.getPlayerId().equals(playerId));
      FindSpyPlayer newPlayer = new FindSpyPlayer(seat);
      this.playerList.add(newPlayer);
      // 排序
      this.playerList.sort(Comparator.comparing(FindSpyPlayer::getSeatNumber));
    } else {
      this.audienceGroup.remove(player.getChannel());
      this.audienceList.removeIf(s -> s.getPlayerId().equals(playerId));

    }
  }

  /**
   * TODO 同意数量.
   *
   * @return [同意数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public int agreeChangeWordsNum() {
    return (int) playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getChangeAction() == 0).count();
  }

  /**
   * TODO 拒绝数量.
   *
   * @return [不同意数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public int disagreeChangeWordsNum() {
    return (int) playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getChangeAction() == 1).count();
  }

  /**
   * TODO 最后一个.
   *
   * @return [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public long getLastUserId() {
    List<FindSpyPlayer> findSpyPlayers = playerList.stream()
        .filter(s -> s.getPlayerId() > 0
            // 玩家标记 [0-游戏中 1-已出局]
            && s.getPlayerIndex() == 0
            // 玩家身份 [0-玩家 1-观众]
            && s.getIdentity() == 0
            // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
            && (s.getPlayerStatus() == 2 || s.getPlayerStatus() == 4)
            // 连接状态 [0-连接中 1-已断开]
            && (s.getLinkStatus() == 0 || s.getLinkStatus() == 1)
        ).collect(Collectors.toList());
    return findSpyPlayers.get(findSpyPlayers.size() - 1).getPlayerId();
  }

  /**
   * TODO 补全描述.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public void completionDesc() {
    List<FindSpyPlayer> findSpyPlayers = playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            // 玩家标记 [0-游戏中 1-已出局]
            && player.getPlayerIndex() == 0
            // 玩家身份 [0-玩家 1-观众]
            && player.getIdentity() == 0
            // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
            && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
            // 连接状态 [0-连接中 1-已断开]
            && (player.getLinkStatus() == 0 || player.getLinkStatus() == 1)
            // 当前描述
            && StringUtils.isEmpty((player.getSpeakWords())))
        .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(findSpyPlayers)) {
      F30051.F300517S2C.Builder builder;
      for (FindSpyPlayer findSpyPlayer : findSpyPlayers) {
        builder = F30051.F300517S2C.newBuilder();
        builder.setWord("...");
        builder.setUserID(findSpyPlayer.getPlayerId());
        builder.setSpeakPlayer(findSpyPlayer.getSeatNumber());
        GroupManager.sendPacketToGroup(
            new Packet(ActionCmd.GAME_WHO_IS_SPY, FindSpyCmd.SPEAK_WORDS,
                builder.build().toByteArray()), roomId);
      }
    }
  }

  /**
   * TODO 可以投票.
   *
   * @return [玩家列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public List<Long> whoCanVote() {
    this.voteList = Lists.newCopyOnWriteArrayList();
    List<FindSpyPlayer> findSpyPlayers = playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            // 玩家标记 [0-游戏中 1-已出局]
            && player.getPlayerIndex() == 0
            // 玩家身份 [0-玩家 1-观众]
            && player.getIdentity() == 0
            // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
            && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
            // 连接状态 [0-连接中 1-已断开]
            && (player.getLinkStatus() == 0 || player.getLinkStatus() == 1)
        ).collect(Collectors.toList());
    List<Long> userList = Lists.newLinkedList();
    findSpyPlayers.forEach(player -> {
      userList.add(player.getPlayerId());
      FindSpyVote findSpyVote = new FindSpyVote(player.getPlayerId());
      this.voteList.add(findSpyVote);
    });
    return userList;
  }

  /**
   * TODO 可以投票.
   *
   * @return [玩家列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public List<Long> whoCanVote(List<Long> userIds) {
    this.voteList = Lists.newCopyOnWriteArrayList();
    userIds.forEach(playerId -> {
      FindSpyVote findSpyVote = new FindSpyVote(playerId);
      this.voteList.add(findSpyVote);
    });
    return playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            && !userIds.contains(player.getPlayerId())
            // 玩家标记 [0-游戏中 1-已出局]
            && player.getPlayerIndex() == 0
            // 玩家身份 [0-玩家 1-观众]
            && player.getIdentity() == 0
            // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
            && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4)
            // 连接状态 [0-连接中 1-已断开]
            && (player.getLinkStatus() == 0 || player.getLinkStatus() == 1)
        ).map(FindSpyPlayer::getPlayerId).collect(Collectors.toCollection(Lists::newLinkedList));
  }

  /**
   * TODO 玩家投票.
   *
   * @param targetId [目标ID]
   * @param userId 用户ID
   * @param iconUrl 用户头像
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public void playerVote(Long targetId, Long userId, String iconUrl) {
    voteList.stream()
        .filter(spyVote -> spyVote.isBoolean(targetId))
        .forEach(spyVote -> spyVote.wasVoted(userId, iconUrl));
  }

  /**
   * TODO 投票信息.
   *
   * @param targetId 目标ID
   * @return 投票信息
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public List<String> getVoteInfo(Long targetId) {
    List<String> voteInfos = Lists.newLinkedList();
    voteList.stream()
        .filter(findSpyVote -> findSpyVote.isBoolean(targetId)).findFirst()
        .ifPresent(findSpyVote -> voteInfos.addAll(findSpyVote.getVoteList()));
    return voteInfos;
  }

  /**
   * TODO 投票计算.
   *
   * @return [计算数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public List<Long> voteCalculation() {
    this.voteList.sort(Comparator.comparing(FindSpyVote::getVoteSize).reversed());
    List<Long> partakerList = Lists.newLinkedList();
    if (CollectionUtils.isNotEmpty(voteList)) {
      FindSpyVote findSpyVote = voteList.get(0);
      if (findSpyVote.getVoteSize() == 0) {
        // 没有玩家投票 添加处理标记
        this.handleIndex  = handleIndex + 1;
      }
      voteList.stream()
          // 筛选出和最后一个票数相同的玩家
          .filter(vote -> vote.getVoteSize().equals(findSpyVote.getVoteSize()))
          .forEach(vote -> partakerList.add(vote.getUserId()));
    }
    this.tempList = partakerList;
    return partakerList;
  }

  /**
   * TODO 找出间谍.
   *
   * @param playerId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public void findOutSpy(Long playerId) {
    this.spyPlayerList.removeIf(s -> s.equals(playerId));
  }

  /**
   * TODO 平民人数.
   *
   * @return [平民人数]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public int civiliansNum() {
    return (int) playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            && player.getPlayerStatus() == 2
            && player.getGameIdentity() == 1).count();
  }

  /**
   * TODO 间谍数量.
   *
   * @return [间谍数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/1 21:09
   * @update 2020/9/1 21:09
   */
  public int spyNum() {
    return (int) playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            && player.getPlayerStatus() == 2
            && player.getGameIdentity() == 2).count();
  }

  /**
   * TODO 目标座位.
   *
   * @param seat [座位编号]
   * @return [座位信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/2 19:15
   * @update 2020/9/2 19:15
   */
  public FindSpyPlayer getTargetSeat(Integer seat) {
    return playerList.stream()
        .filter(s -> s.getSeatNumber().equals(seat))
        .findFirst().orElse(null);
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
