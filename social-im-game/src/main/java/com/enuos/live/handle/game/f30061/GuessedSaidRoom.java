package com.enuos.live.handle.game.f30061;

import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.util.Timeout;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

/**
 * TODO 你说我猜.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/8/4 9:48
 */

@Data
@SuppressWarnings("WeakerAccess")
public class GuessedSaidRoom {
  /** 房间ID. */
  private Long roomId;
  /** 当前玩家. */
  private Long actionPlayer;
  /** 房间回合. */
  private Integer roomRound = 0;
  /** 房间状态 [0-未开始 1-已开始]. */
  private Integer roomStatus = 0;
  /** 最大人数. */
  private Integer maxPlayerNum = 6;
  /** 当前人数. */
  private Integer nowPlayerNum = 0;
  /** 开始时间. */
  private LocalDateTime startTime;
  /** 回合时间. */
  private LocalDateTime roundTime;
  /** 选择时间. */
  private LocalDateTime selectTime;
  /** 当前词汇. */
  private GuessedSaidWord currentWord;
  /** 得分排名. */
  private List<Long> scoreRanking = Lists.newArrayList();
  /** 词汇列表. */
  private List<String> tempWordsList = Lists.newArrayList();
  /** 描述玩家. */
  private List<Long> describePlayerList = Lists.newArrayList();
  /** 临时数据. */
  private Map<String, GuessedSaidWord> tempWordMap = Maps.newHashMap();
  /** 观战列表. */
  private List<GuessedSaidPlayer> watchList = Lists.newCopyOnWriteArrayList();
  /** 玩家列表. */
  private List<GuessedSaidPlayer> playerList = Lists.newCopyOnWriteArrayList();
  /** 定时数据. */
  private HashMap<Integer, Timeout> timeOutMap = Maps.newHashMap();

  /**
   * TODO 初始房间.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 15:51
   * @update 2020/8/5 9:09
   */
  GuessedSaidRoom(Long roomId) {
    this.roomId = roomId;
    for (int i = 1; i < maxPlayerNum + 1; i++) {
      GuessedSaidPlayer player = new GuessedSaidPlayer(i);
      this.playerList.add(player);
    }
  }

  /**
   * TODO 进入房间.
   *
   * @param channel [通信管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/5 9:09
   * @update 2020/8/5 9:09
   */
  public void enterSeat(Channel channel, I10001.PlayerInfo playerInfo) {
    if (Objects.nonNull(playerInfo)) {
      for (GuessedSaidPlayer player : playerList) {
        if (player.getPlayerId() == 0) {
          player.setPlayerId(playerInfo.getUserId());
          player.setPlayerName(playerInfo.getNickName());
          player.setPlayerAvatar(playerInfo.getIconUrl());
          player.setPlayerSex(playerInfo.getSex());
          player.setPlayerChannel(channel);
          break;
        }
      }
    }
    this.playerList.sort(Comparator.comparing(GuessedSaidPlayer::getSeatNumber));
  }

  /**
   * TODO 加入观战.
   *
   * @param channel [通信管道]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/5 9:33
   * @update 2020/8/5 9:33
   */
  public void enterWatch(Channel channel, I10001.PlayerInfo playerInfo) {
    if (Objects.nonNull(playerInfo)) {
      GuessedSaidPlayer player = new GuessedSaidPlayer(0);
      player.setPlayerId(playerInfo.getUserId());
      player.setPlayerName(playerInfo.getNickName());
      player.setPlayerAvatar(playerInfo.getIconUrl());
      player.setPlayerSex(playerInfo.getSex());
      player.setPlayerChannel(channel);
      player.setIdentity(1);
      this.watchList.add(player);
    }
  }

  /**
   * TODO 玩家信息.
   *
   * @param playerId [玩家ID]
   * @return [玩家信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/5 9:07
   * @update 2020/8/5 9:07
   */
  public GuessedSaidPlayer getPlayerInfo(Long playerId) {
    GuessedSaidPlayer player = playerList.stream()
        .filter(saidPlayer -> saidPlayer.isBoolean(playerId))
        .findFirst().orElse(null);
    if (Objects.isNull(player)) {
      player = watchList.stream()
          .filter(watchPlayer -> watchPlayer.isBoolean(playerId))
          .findFirst().orElse(null);
    }
    return player;
  }

  /**
   * TODO 入座数量.
   *
   * @return [入座数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/5 18:03
   * @update 2020/8/5 18:03
   */
  public int numberOfSeats() {
    return (int) playerList.stream()
        .filter(s -> s.getPlayerId() > 0).count();
  }

  /**
   * TODO 观战玩家.
   *
   * @return [头像列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/6 14:22
   * @update 2020/8/6 14:22
   */
  public List<String> getWatchPlayerIcon() {
    List<String> iconList = Lists.newLinkedList();
    watchList.forEach(audience -> iconList.add(audience.getPlayerAvatar()));
    return iconList;
  }

  /**
   * TODO 玩家选择.
   *
   * @param words [词汇]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 10:51
   * @update 2020/8/7 10:51
   */
  public void playerSelect(String words) {
    this.currentWord = tempWordMap.get(words);
    this.tempWordMap = Maps.newHashMap();
    // 找出描述玩家
    List<GuessedSaidPlayer> filterPlayerList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && !s.getPlayerId().equals(actionPlayer))
        .collect(Collectors.toList());
    List<Long> decsList = Lists.newLinkedList();
    filterPlayerList.forEach(player -> decsList.add(player.getPlayerId()));
    this.describePlayerList = decsList;
  }

  /**
   * TODO 初始词汇.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 14:05
   * @update 2020/8/7 14:05
   */
  public void initWordsInfo() {
    this.tempWordsList = Lists.newArrayList();
    this.tempWordMap = Maps.newHashMap();
  }

  /**
   * TODO 词汇处理.
   *
   * @param wordsList [词汇列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 11:02
   * @update 2020/8/7 11:02
   */
  public void wordsHandler(List<Map<String, Object>> wordsList) {
    List<String> stringList = Lists.newLinkedList();
    wordsList.forEach(wordInfo -> {
      GuessedSaidWord word = new GuessedSaidWord();
      word.setLexicon(StringUtils.nvl(wordInfo.get("lexicon")));
      word.setLexiconHint(StringUtils.nvl(wordInfo.get("lexiconHint")));
      word.setLexiconWords((Integer) wordInfo.get("lexiconWords"));
      stringList.add(word.getLexicon());
      this.tempWordMap.put(word.getLexicon(), word);
    });
    this.tempWordsList = stringList;
  }

  /**
   * TODO 设置玩家.
   *
   * @param oldPlayer [旧的玩家]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 12:59
   * @update 2020/8/7 12:59
   */
  public void setUpActionPlayer(Long oldPlayer) {
    List<GuessedSaidPlayer> playList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getPlayerStatus() == 2 || s.getPlayerStatus() == 3)
        .sorted(Comparator.comparing(GuessedSaidPlayer::getSeatNumber))
        .collect(Collectors.toList());
    if (oldPlayer == 0L) {
      this.actionPlayer = playList.get(0).getPlayerId();
    } else {
      int index = 0;
      for (GuessedSaidPlayer player : playList) {
        if (player.isBoolean(oldPlayer)) {
          break;
        }
        index++;
      }
      if (index == playList.size() - 1) {
        this.actionPlayer = playList.get(0).getPlayerId();
      } else {
        this.actionPlayer = playList.get(index + 1).getPlayerId();
      }
    }
  }

  /**
   * TODO 已准备数.
   *
   * @return [准备数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 15:04
   * @update 2020/8/7 15:04
   */
  public int preparations() {
    return (int) playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getPlayerStatus() == 1).count();
  }

  /**
   * TODO 未准备数.
   *
   * @return [未准备数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 13:32
   * @update 2020/8/7 13:32
   */
  public int unprepared() {
    return (int) playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getPlayerStatus() == 0).count();
  }

  /**
   * TODO 开始游戏.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/10 3:42
   * @update 2020/8/10 3:42
   */
  public void startGame() {
    this.roomStatus = 1;
    this.roomRound = roomRound + 1;
    this.nowPlayerNum = (int) playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getPlayerStatus() == 1).count();
    this.playerList.stream()
        .filter(player -> player.getPlayerId() > 0)
        .forEach(player -> player.setPlayerStatus(2));
  }

  /**
   * TODO 剩余座位.
   *
   * @return [剩余数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 14:32
   * @update 2020/8/7 14:32
   */
  public int remainingSeat() {
    return (int) playerList.stream()
        .filter(s -> s.getPlayerId() == 0).count();
  }

  /**
   * TODO 目标座位.
   *
   * @param seat [座位编号]
   * @return [座位信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 14:32
   * @update 2020/8/7 14:32
   */
  public GuessedSaidPlayer getTargetSeat(Integer seat) {
    return playerList.stream()
        .filter(s -> s.getSeatNumber().equals(seat))
        .findFirst().orElse(null);
  }

  /**
   * TODO 加入座位.
   *
   * @param playerId [玩家ID]
   * @return Integer [座位号]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 14:43
   * @update 2020/8/7 14:43
   */
  public int joinSeat(Long playerId) {
    int seat = 0;
    GuessedSaidPlayer player = getPlayerInfo(playerId);
    for (GuessedSaidPlayer saidPlayer : playerList) {
      if (saidPlayer.getPlayerId() == 0) {
        saidPlayer.setPlayerId(player.getPlayerId());
        saidPlayer.setPlayerName(player.getPlayerName());
        saidPlayer.setPlayerAvatar(player.getPlayerAvatar());
        saidPlayer.setPlayerSex(player.getPlayerSex());
        saidPlayer.setPlayerChannel(player.getPlayerChannel());
        saidPlayer.setPlayerAvatarFrame(player.getPlayerAvatarFrame());
        saidPlayer.setPlayerStatus(0);
        saidPlayer.setIdentity(0);
        seat = saidPlayer.getSeatNumber();
        break;
      }
    }
    this.watchList.removeIf(s -> s.getPlayerId().equals(playerId));
    return seat;
  }

  /**
   * TODO 加入座位.
   *
   * @param playerId [玩家ID]
   * @param seat [座位编号]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 14:43
   * @update 2020/8/7 14:43
   */
  public void joinSeat(Long playerId, Integer seat) {
    GuessedSaidPlayer player = getPlayerInfo(playerId);
    if (player.getIdentity() == 0) {
      int oldSeat = player.getSeatNumber();
      this.playerList.removeIf(s -> s.getSeatNumber().equals(seat));
      player.setSeatNumber(seat);
      this.playerList.removeIf(s -> s.getSeatNumber().equals(oldSeat));
      GuessedSaidPlayer newPlayer = new GuessedSaidPlayer(oldSeat);
      this.playerList.add(newPlayer);
      this.playerList.sort(Comparator.comparing(GuessedSaidPlayer::getSeatNumber));
    } else {
      for (GuessedSaidPlayer saidPlayer : playerList) {
        if (saidPlayer.getSeatNumber().equals(seat)) {
          saidPlayer.setPlayerId(player.getPlayerId());
          saidPlayer.setPlayerName(player.getPlayerName());
          saidPlayer.setPlayerAvatar(player.getPlayerAvatar());
          saidPlayer.setPlayerSex(player.getPlayerSex());
          saidPlayer.setPlayerChannel(player.getPlayerChannel());
          saidPlayer.setPlayerAvatarFrame(player.getPlayerAvatarFrame());
          saidPlayer.setPlayerStatus(0);
          saidPlayer.setIdentity(0);
          break;
        }
      }
      this.watchList.removeIf(s -> s.getPlayerId().equals(playerId));
    }
  }

  /**
   * TODO 离开座位.
   *
   * @param playerId [玩家ID]
   * @return [座位编号]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 14:55
   * @update 2020/8/7 14:55
   */
  public int leaveSeat(Long playerId) {
    GuessedSaidPlayer player = getPlayerInfo(playerId);
    int seat = player.getSeatNumber();
    this.playerList.removeIf(s -> s.getPlayerId().equals(playerId));
    GuessedSaidPlayer newPlayer = new GuessedSaidPlayer(seat);
    this.playerList.add(newPlayer);
    this.playerList.sort(Comparator.comparing(GuessedSaidPlayer::getSeatNumber));
    GuessedSaidPlayer watch = new GuessedSaidPlayer(0);
    watch.setPlayerId(player.getPlayerId());
    watch.setPlayerName(player.getPlayerName());
    watch.setPlayerSex(player.getPlayerSex());
    watch.setPlayerAvatar(player.getPlayerAvatar());
    watch.setPlayerAvatarFrame(player.getPlayerAvatarFrame());
    watch.setPlayerChannel(player.getPlayerChannel());
    watch.setLinkStatus(player.getLinkStatus());
    watch.setIdentity(1);
    this.watchList.add(watch);
    return newPlayer.getSeatNumber();
  }

  /**
   * TODO 正确人数.
   *
   * @return [正确数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/7 15:57
   * @update 2020/8/7 15:57
   */
  public int describeIsTrue() {
    return (int) playerList.stream()
        .filter(s -> s.getPlayerId() > 0
            && s.getPlayerStatus() == 2 && s.getWordIsTrue() == 0).count();
  }

  /**
   * TODO 初始回合.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/10 3:13
   * @update 2020/8/10 3:13
   */
  public void initRound() {
    this.currentWord = null;
    initWordsInfo();
    this.describePlayerList = Lists.newArrayList();
    this.scoreRanking = Lists.newArrayList();
    this.roomRound = roomRound + 1;
    this.playerList.stream()
        .filter(player -> player.getPlayerId() > 0)
        .forEach(player -> {
          player.setPlayerWords(null);
          player.setWordIsTrue(1);
        });
  }

  /**
   * TODO 初始游戏.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/10 3:13
   * @update 2020/8/10 3:13
   */
  public void initGame() {
    this.actionPlayer = 0L;
    this.roomRound = 0;
    this.roomStatus = 0;
    this.nowPlayerNum = 0;
    this.startTime = null;
    this.roundTime = null;
    this.selectTime = null;
    this.scoreRanking = Lists.newArrayList();
    this.describePlayerList = Lists.newArrayList();
    initWordsInfo();
    // 初始玩家消息
    this.playerList.stream()
        .filter(player -> player.getPlayerId() > 0)
        .forEach(GuessedSaidPlayer::init);
  }

  /**
   * TODO 离线玩家.
   *
   * @return [离线玩家]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/10 10:00
   * @update 2020/8/10 10:00
   */
  public List<Long> offlinePlayers() {
    List<GuessedSaidPlayer> playList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getLinkStatus() == 1)
        .collect(Collectors.toList());
    List<Long> clearList = Lists.newArrayList();
    List<Integer> seatList = Lists.newArrayList();
    if (CollectionUtils.isNotEmpty(playList)) {
      playList.forEach(player -> {
        clearList.add(player.getPlayerId());
        seatList.add(player.getSeatNumber());
      });
    }
    clearList.forEach(playerId -> playerList.removeIf(s ->s.isBoolean(playerId)));
    if (CollectionUtils.isNotEmpty(seatList)) {
      seatList.forEach(s -> {
        GuessedSaidPlayer newPlayer = new GuessedSaidPlayer(s);
        playerList.add(newPlayer);
      });
      // 排序
      this.playerList.sort(Comparator.comparing(GuessedSaidPlayer::getSeatNumber));
    }
    return clearList;
  }

  /**
   * TODO 入座玩家.
   *
   * @return [入座玩家]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/10 10:24
   * @update 2020/8/10 10:24
   */
  public List<Long> seatedPlayers() {
    List<GuessedSaidPlayer> playList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getLinkStatus() == 0)
        .collect(Collectors.toList());
    List<Long> readyList = Lists.newArrayList();
    if (CollectionUtils.isNotEmpty(playList)) {
      playList.forEach(s -> readyList.add(s.getPlayerId()));
    }
    return readyList;
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
   * TODO 得分排名.
   *
   * @return [得分排名]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/10 3:10
   * @update 2020/8/10 3:10
   */
  public List<GuessedSaidPlayer> getRanking() {
    return playerList.stream()
        .filter(s -> s.getPlayerId() > 0)
        // 分数排序
        .sorted(Comparator.comparing(GuessedSaidPlayer::getPlayerScore).reversed())
        .collect(Collectors.toList());
  }

  /**
   * TODO 玩家列表.
   *
   * @return [玩家列表]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/10 16:47
   * @update 2020/8/10 16:47
   */
  public List<GuessedSaidPlayer> playGameList() {
    return playerList.stream()
        .filter(s -> s.getPlayerId() > 0)
        .collect(Collectors.toList());
  }

  /**
   * TODO 离开游戏.
   *
   * @param playerId [玩家ID]
   * @param identity [玩家身份]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/10 13:28
   * @update 2020/8/10 13:28
   */
  public void leaveGame(Long playerId, Integer identity) {
    GuessedSaidPlayer player = getPlayerInfo(playerId);
    if (identity == 0) {
      int seat = player.getSeatNumber();
      this.playerList.removeIf(s -> s.getPlayerId().equals(playerId));
      GuessedSaidPlayer newPlayer = new GuessedSaidPlayer(seat);
      this.playerList.add(newPlayer);
      // 排序
      this.playerList.sort(Comparator.comparing(GuessedSaidPlayer::getSeatNumber));
    } else {
      watchList.removeIf(s -> s.getPlayerId().equals(playerId));
    }
  }

  /**
   * TODO 添加定时.
   *
   * @param taskId [任务ID].
   * @param timeout [定时任务].
   * @author WangCaiWen·1443710411@qq.com
   * @create 2020/8/4 17:28
   * @update 2020/8/4 17:28
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
   * @author WangCaiWen·1443710411@qq.com
   * @create 2020/8/4 17:29
   * @update 2020/8/4 17:29
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
   * @author WangCaiWen·1443710411@qq.com
   * @create 2020/8/4 17:30
   * @update 2020/8/4 17:30
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
   * @author wangcaiwen·1443710411@qq.com
   * @create 2020/8/4 17:30
   * @update 2020/8/4 17:30
   */
  public void destroy() {
    for (Timeout out : timeOutMap.values()) {
      out.cancel();
    }
    timeOutMap.clear();
  }

}
