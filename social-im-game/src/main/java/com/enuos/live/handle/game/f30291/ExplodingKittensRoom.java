package com.enuos.live.handle.game.f30291;

import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.manager.GroupManager;
import com.enuos.live.manager.ChatManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.Timeout;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

/**
 * TODO 炸弹猫.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V1.0.0
 * @since 2020/8/31 12:33
 */

@Data
@SuppressWarnings("WeakerAccess")
public class ExplodingKittensRoom {
  /** 房间ID. */
  private Long roomId;
  /** 开放方式 [0-公开 1-创建]. */
  private Integer openWay;
  /** 房间状态 [0-未开始 1-已开始]. */
  private Integer roomStatus = 0;
  /** 炸弹数量. */
  private Integer explodingNum = 4;
  /** 炸弹几率. */
  private Integer probability = 0;
  /** 旋转方向 1-顺时针 2-逆时针. */
  private Integer direction = 1;
  /** 诅咒次数. */
  private Integer cursesNum = 0;
  /** 被诅咒玩家. */
  private Long cursedPlayer = 0L;
  /** 被祈求玩家. */
  private Long prayForPlayers = 0L;
  /** 当前玩家. */
  private Long nowActionPlayer = 0L;
  /** 开始时间. */
  private LocalDateTime startTime;
  /** 操作时间. */
  private LocalDateTime actionTime;
  /** 当前卡牌. */
  private ExplodingKittensCard desktopCardInfo;
  /** 炸弹卡牌. */
  private ExplodingKittensCard explodingCardInfo;
  /** 名次记录. */
  private List<Long> rankingList = Lists.newCopyOnWriteArrayList();
  /** 剩余卡牌. */
  private List<ExplodingKittensCard> remainingCard = Lists.newCopyOnWriteArrayList();
  /** 玩家列表. */
  private List<ExplodingKittensPlayer> playerList = Lists.newCopyOnWriteArrayList();
  /** 观战列表. */
  private List<ExplodingKittensPlayer> audienceList = Lists.newCopyOnWriteArrayList();
  /** 观看群组. */
  private ChannelGroup audienceGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
  /** 定时数据. */
  private HashMap<Integer, Timeout> timeOutMap = Maps.newHashMap();

  private static final int PLAYER_NUM = 5;
  private static final int VIEW_THREE_CARDS = 3;
  private static final int EXTRACT_NUM = 20;
  private static final int APPORTION_NUM = 4;
  private static final int EXPLODING_NUM_FUNC_01 = 4;
  private static final int DISARM_BOMB_NUM_FUNC_02 = 5;
  private static final int PROPHET_NUM_FUNC_03 = 5;
  private static final int DIVINATION_NUM_FUNC_04 = 5;
  private static final int SHUFFLE_NUM_FUNC_05 = 5;
  private static final int DRAW_BOTTOM_NUM_FUNC_06 = 5;
  private static final int SKIP_NUM_FUNC_07 = 6;
  private static final int TURN_TO_NUM_FUNC_08 = 5;
  private static final int CURSE_NUM_FUNC_09 = 3;
  private static final int DOUBLE_CURSE_NUM_FUNC_10 = 2;
  private static final int PRAY_NUM_FUNC_11 = 5;
  private static final int MANACLE_NUM_FUNC_12 = 2;
  private static final int EXCHANGE_NUM_FUNC_13 = 4;

  /**
   * TODO 初始数据.
   *
   * @param roomId 房间ID
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 3:36
   * @since 2020/9/7 3:36
   */
  ExplodingKittensRoom(Long roomId) {
    this.roomId = roomId;
    for (int i = 1; i < PLAYER_NUM + 1; i++) {
      ExplodingKittensPlayer player = new ExplodingKittensPlayer(i);
      this.playerList.add(player);
    }
  }

  /**
   * TODO 开始游戏.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 3:32
   * @since 2020/9/7 3:32
   */
  public void startGame() {
    this.roomStatus = 1;
    this.direction = 1;
    this.createPoker();
    // 开始洗牌
    Collections.shuffle(remainingCard);
    // 提取20张卡牌
    List<ExplodingKittensCard> tempCardList = Lists.newLinkedList();
    while (tempCardList.size() < EXTRACT_NUM) {
      tempCardList.add(remainingCard.remove(0));
    }
    int index = 1;
    // 炸弹卡 4
    List<ExplodingKittensCard> explodingList = Lists.newLinkedList();
    while (explodingList.size() < EXPLODING_NUM_FUNC_01) {
      ExplodingKittensCard kittensCard = new ExplodingKittensCard();
      kittensCard.setCardId(index);
      kittensCard.setCardFunction(1);
      explodingList.add(kittensCard);
      index++;
    }
    // 拆弹卡 5
    List<ExplodingKittensCard> bombDisposalList = Lists.newLinkedList();
    while (bombDisposalList.size() < DISARM_BOMB_NUM_FUNC_02) {
      ExplodingKittensCard kittensCard = new ExplodingKittensCard();
      kittensCard.setCardId(index);
      kittensCard.setCardFunction(2);
      bombDisposalList.add(kittensCard);
      index++;
    }
    // 开始发牌
    playerList.forEach(player -> {
      // 分发拆弹卡
      player.getPlayerCard().add(bombDisposalList.remove(0));
      List<ExplodingKittensCard> playerCard = Lists.newLinkedList();
      while (playerCard.size() < APPORTION_NUM) {
        int cardNo = ThreadLocalRandom.current().nextInt(tempCardList.size());
        playerCard.add(tempCardList.remove(cardNo));
      }
      player.setPlayerStatus(2);
      player.getPlayerCard().addAll(playerCard);
    });
    // 放入炸弹
    this.remainingCard.addAll(explodingList);
    // 重新洗牌
    Collections.shuffle(remainingCard);
  }

  /**
   * TODO 生成卡牌.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 3:35
   * @since 2020/9/7 3:35
   */
  private void createPoker() {
    prophetCard();
    divinationCard();
    shuffleCards();
    drawBottomCard();
    jumpOverCard();
    turnToCard();
    curseCard();
    doubleCurseCard();
    prayCard();
    manacleCard();
    exchangeCard();
  }

  /**
   * TODO 先知卡牌.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 3:59
   * @since 2020/9/7 3:59
   */
  private void prophetCard() {
    int index = 10;
    // 先知卡 5
    List<ExplodingKittensCard> prophetList = Lists.newLinkedList();
    while (prophetList.size() < PROPHET_NUM_FUNC_03) {
      ExplodingKittensCard kittensCard = new ExplodingKittensCard();
      kittensCard.setCardId(index);
      kittensCard.setCardFunction(3);
      prophetList.add(kittensCard);
      index++;
    }
    this.remainingCard.addAll(prophetList);
  }

  /**
   * TODO 占星卡牌.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 4:00
   * @since 2020/9/7 4:00
   */
  private void divinationCard() {
    int index = 15;
    // 占星卡 5
    List<ExplodingKittensCard> horoscopeList = Lists.newLinkedList();
    while (horoscopeList.size() < DIVINATION_NUM_FUNC_04) {
      ExplodingKittensCard kittensCard = new ExplodingKittensCard();
      kittensCard.setCardId(index);
      kittensCard.setCardFunction(4);
      horoscopeList.add(kittensCard);
      index++;
    }
    this.remainingCard.addAll(horoscopeList);
  }

  /**
   * TODO 洗牌卡牌.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 4:00
   * @since 2020/9/7 4:00
   */
  private void shuffleCards() {
    int index = 20;
    // 洗牌卡 5
    List<ExplodingKittensCard> shuffleList = Lists.newLinkedList();
    while (shuffleList.size() < SHUFFLE_NUM_FUNC_05) {
      ExplodingKittensCard kittensCard = new ExplodingKittensCard();
      kittensCard.setCardId(index);
      kittensCard.setCardFunction(5);
      shuffleList.add(kittensCard);
      index++;
    }
    this.remainingCard.addAll(shuffleList);
  }

  /**
   * TODO 抽底卡牌.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 4:01
   * @since 2020/9/7 4:01
   */
  private void drawBottomCard() {
    int index = 25;
    // 抽底卡 5
    List<ExplodingKittensCard> bottomOutList = Lists.newLinkedList();
    while (bottomOutList.size() < DRAW_BOTTOM_NUM_FUNC_06) {
      ExplodingKittensCard kittensCard = new ExplodingKittensCard();
      kittensCard.setCardId(index);
      kittensCard.setCardFunction(6);
      bottomOutList.add(kittensCard);
      index++;
    }
    this.remainingCard.addAll(bottomOutList);
  }

  /**
   * TODO 跳过卡牌.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 4:01
   * @since 2020/9/7 4:01
   */
  private void jumpOverCard() {
    int index = 30;
    // 跳过卡 6
    List<ExplodingKittensCard> jumpOverList = Lists.newLinkedList();
    while (jumpOverList.size() < SKIP_NUM_FUNC_07) {
      ExplodingKittensCard kittensCard =  new ExplodingKittensCard();
      kittensCard.setCardId(index);
      kittensCard.setCardFunction(7);
      jumpOverList.add(kittensCard);
      index++;
    }
    this.remainingCard.addAll(jumpOverList);
  }

  /**
   * TODO 转向卡牌.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 4:01
   * @since 2020/9/7 4:01
   */
  private void turnToCard() {
    int index = 36;
    // 转向卡 5
    List<ExplodingKittensCard> turnToList = Lists.newLinkedList();
    while (turnToList.size() < TURN_TO_NUM_FUNC_08) {
      ExplodingKittensCard kittensCard = new ExplodingKittensCard();
      kittensCard.setCardId(index);
      kittensCard.setCardFunction(8);
      turnToList.add(kittensCard);
      index++;
    }
    this.remainingCard.addAll(turnToList);
  }

  /**
   * TODO 诅咒卡牌.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 4:02
   * @since 2020/9/7 4:02
   */
  private void curseCard() {
    int index = 41;
    // 诅咒卡 3
    List<ExplodingKittensCard> curseList = Lists.newLinkedList();
    while (curseList.size() < CURSE_NUM_FUNC_09) {
      ExplodingKittensCard kittensCard = new ExplodingKittensCard();
      kittensCard.setCardId(index);
      kittensCard.setCardFunction(9);
      curseList.add(kittensCard);
      index++;
    }
    this.remainingCard.addAll(curseList);
  }

  /**
   * TODO 诅咒卡牌*2.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 4:02
   * @since 2020/9/7 4:02
   */
  private void doubleCurseCard() {
    int index = 44;
    // 诅咒*2卡 2
    List<ExplodingKittensCard> doubleCurseList = Lists.newLinkedList();
    while (doubleCurseList.size() < DOUBLE_CURSE_NUM_FUNC_10) {
      ExplodingKittensCard kittensCard = new ExplodingKittensCard();
      kittensCard.setCardId(index);
      kittensCard.setCardFunction(10);
      doubleCurseList.add(kittensCard);
      index++;
    }
    this.remainingCard.addAll(doubleCurseList);
  }

  /**
   * TODO 祈求卡牌.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 4:02
   * @since 2020/9/7 4:02
   */
  private void prayCard() {
    int index = 46;
    // 祈求卡 5
    List<ExplodingKittensCard> prayCardList = Lists.newLinkedList();
    while (prayCardList.size() < PRAY_NUM_FUNC_11) {
      ExplodingKittensCard kittensCard = new ExplodingKittensCard();
      kittensCard.setCardId(index);
      kittensCard.setCardFunction(11);
      prayCardList.add(kittensCard);
      index++;
    }
    this.remainingCard.addAll(prayCardList);
  }

  /**
   * TODO 束缚卡牌.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 4:03
   * @since 2020/9/7 4:03
   */
  private void manacleCard() {
    int index = 51;
    // 束缚卡 2
    List<ExplodingKittensCard> manacleList = Lists.newLinkedList();
    while (manacleList.size() < MANACLE_NUM_FUNC_12) {
      ExplodingKittensCard kittensCard = new ExplodingKittensCard();
      kittensCard.setCardId(index);
      kittensCard.setCardFunction(12);
      manacleList.add(kittensCard);
      index++;
    }
    this.remainingCard.addAll(manacleList);
  }

  /**
   * TODO 交换卡牌.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 4:03
   * @since 2020/9/7 4:03
   */
  private void exchangeCard() {
    int index = 53;
    // 交换卡 4
    List<ExplodingKittensCard> exchangeList = Lists.newLinkedList();
    while (exchangeList.size() < EXCHANGE_NUM_FUNC_13) {
      ExplodingKittensCard kittensCard = new ExplodingKittensCard();
      kittensCard.setCardId(index);
      kittensCard.setCardFunction(13);
      exchangeList.add(kittensCard);
      index++;
    }
    this.remainingCard.addAll(exchangeList);
  }

  /**
   * TODO 设置玩家.
   *
   * @param old 当前玩家
   * @param dir 当前方向
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 18:41
   * @update 2020/8/31 18:41
   */
  public void setActionPlayer(Long old, Integer dir) {
    List<ExplodingKittensPlayer> kittensPlayers = playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            && (player.getPlayerStatus() == 2 || player.getPlayerStatus() == 4))
        .sorted(Comparator.comparing(ExplodingKittensPlayer::getSeatNumber))
        .collect(Collectors.toList());
    if (dir > 1) {
      // 反转
      Collections.reverse(kittensPlayers);
    }
    if (old == 0) {
      this.nowActionPlayer = kittensPlayers.get(0).getPlayerId();
    } else {
      int playerIndex = 0;
      for (ExplodingKittensPlayer kittensPlayer : kittensPlayers) {
        if (kittensPlayer.isBoolean(old)) {
          break;
        }
        playerIndex++;
      }
      if (playerIndex == kittensPlayers.size() - 1) {
        this.nowActionPlayer = kittensPlayers.get(0).getPlayerId();
      } else {
        this.nowActionPlayer = kittensPlayers.get(playerIndex + 1).getPlayerId();
      }
    }
  }

  /**
   * TODO 下一玩家.
   *
   * @param playerId [玩家ID]
   * @return [下一玩家]
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 19:07
   * @update 2020/8/31 19:07
   */
  public long getNextActionPlayer(Long playerId) {
    List<ExplodingKittensPlayer> kittensPlayers = playerList.stream()
        .filter(player -> player.getPlayerId() > 0
            // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
            && (player.getPlayerStatus() == 2|| player.getPlayerStatus() == 4))
        .sorted(Comparator.comparing(ExplodingKittensPlayer::getSeatNumber))
        .collect(Collectors.toList());
    if (direction > 1) {
      Collections.reverse(kittensPlayers);
    }
    int playerIndex = 0;
    for (ExplodingKittensPlayer kittensPlayer : kittensPlayers) {
      if (kittensPlayer.isBoolean(playerId)) {
        break;
      }
      playerIndex++;
    }
    List<Long> players = Lists.newLinkedList();
    kittensPlayers.forEach(player -> players.add(player.getPlayerId()));
    if (playerIndex == players.size() - 1) {
      return players.get(0);
    } else {
      return players.get(playerIndex + 1);
    }
  }

  /**
   * TODO 头像列表.
   *
   * @return list 头像列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 19:14
   * @update 2020/8/31 19:14
   */
  public List<String> getAudienceIcon() {
    return audienceList.stream().map(ExplodingKittensPlayer::getPlayerAvatar)
        .collect(Collectors.toCollection(Lists::newLinkedList));
  }

  /**
   * TODO 玩家信息.
   *
   * @param playerId 玩家ID
   * @return 玩家信息
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 19:19
   * @update 2020/8/31 19:19
   */
  public ExplodingKittensPlayer getPlayerInfo(Long playerId) {
    ExplodingKittensPlayer kittensPlayer = playerList.stream()
        .filter(player -> player.isBoolean(playerId))
        .findFirst().orElse(null);
    if (Objects.isNull(kittensPlayer)) {
      kittensPlayer = audienceList.stream()
          .filter(player -> player.isBoolean(playerId))
          .findFirst().orElse(null);
    }
    return kittensPlayer;
  }

  /**
   * TODO 进入座位.
   *
   * @param channel 快速通道
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 19:21
   * @update 2020/8/31 19:21
   */
  public void enterSeat(Channel channel, I10001.PlayerInfo playerInfo) {
    if (Objects.nonNull(playerInfo)) {
      for (ExplodingKittensPlayer kittensPlayer : playerList) {
        if (kittensPlayer.getPlayerId() == 0) {
          kittensPlayer.setPlayerId(playerInfo.getUserId());
          kittensPlayer.setPlayerName(playerInfo.getNickName());
          kittensPlayer.setPlayerAvatar(playerInfo.getIconUrl());
          kittensPlayer.setPlayerSex(playerInfo.getSex());
          kittensPlayer.setChannel(channel);
          break;
        }
      }
    }
    this.playerList.sort(Comparator.comparing(ExplodingKittensPlayer::getSeatNumber));
  }

  /**
   * TODO 进入观战.
   *
   * @param channel 快速通道
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 19:34
   * @update 2020/8/31 19:34
   */
  public void enterAudience(Channel channel, I10001.PlayerInfo playerInfo) {
    if (Objects.nonNull(playerInfo)) {
      ExplodingKittensPlayer kittensPlayer = new ExplodingKittensPlayer(0);
      kittensPlayer.setPlayerId(playerInfo.getUserId());
      kittensPlayer.setPlayerName(playerInfo.getNickName());
      kittensPlayer.setPlayerAvatar(playerInfo.getIconUrl());
      kittensPlayer.setPlayerSex(playerInfo.getSex());
      kittensPlayer.setChannel(channel);
      kittensPlayer.setIdentity(1);
      this.audienceList.add(kittensPlayer);
      this.audienceGroup.add(channel);
    }
  }

  /**
   * TODO 加入座位.
   *
   * @param playerId 玩家ID
   * @return int 座位号
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 19:46
   * @update 2020/8/31 19:46
   */
  public int joinSeat(Long playerId) {
    int seat = 0;
    ExplodingKittensPlayer kittensPlayer = getPlayerInfo(playerId);
    for (ExplodingKittensPlayer player : playerList) {
      if (player.getPlayerId() == 0) {
        player.setPlayerId(kittensPlayer.getPlayerId());
        player.setPlayerName(kittensPlayer.getPlayerName());
        player.setPlayerAvatar(kittensPlayer.getPlayerAvatar());
        player.setPlayerSex(kittensPlayer.getPlayerSex());
        player.setChannel(kittensPlayer.getChannel());
        player.setAvatarFrame(kittensPlayer.getAvatarFrame());
        player.setCardBackSkin(kittensPlayer.getCardBackSkin());
        player.setPlayerGold(kittensPlayer.getPlayerGold());
        player.setPlayerStatus(0);
        player.setIdentity(0);
        seat = player.getSeatNumber();
        break;
      }
    }
    this.audienceGroup.remove(kittensPlayer.getChannel());
    this.audienceList.removeIf(s -> s.getPlayerId().equals(playerId));
    return seat;
  }

  /**
   * TODO 离开座位.
   *
   * @param playerId 玩家ID
   * @return int 座位号
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 20:05
   * @update 2020/8/31 20:05
   */
  public int leaveSeat(Long playerId) {
    ExplodingKittensPlayer kittensPlayer = getPlayerInfo(playerId);
    int seat = kittensPlayer.getSeatNumber();
    this.playerList.removeIf(s -> s.getPlayerId().equals(playerId));
    ExplodingKittensPlayer newKittensPlayer = new ExplodingKittensPlayer(seat);
    this.playerList.add(newKittensPlayer);
    this.playerList.sort(Comparator.comparing(ExplodingKittensPlayer::getSeatNumber));
    // 新的信息
    ExplodingKittensPlayer audience = new ExplodingKittensPlayer(0);
    audience.setPlayerId(kittensPlayer.getPlayerId());
    audience.setPlayerName(kittensPlayer.getPlayerName());
    audience.setPlayerSex(kittensPlayer.getPlayerSex());
    audience.setPlayerAvatar(kittensPlayer.getPlayerAvatar());
    audience.setAvatarFrame(kittensPlayer.getAvatarFrame());
    audience.setChannel(kittensPlayer.getChannel());
    audience.setCardBackSkin(kittensPlayer.getCardBackSkin());
    audience.setPlayerGold(kittensPlayer.getPlayerGold());
    audience.setIdentity(1);
    this.audienceList.add(audience);
    this.audienceGroup.add(audience.getChannel());
    return newKittensPlayer.getSeatNumber();
  }

  /**
   * TODO 已准备数.
   *
   * @return int
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 20:09
   * @update 2020/8/31 20:09
   */
  public int preparations() {
    return (int) playerList.stream()
        .filter(player -> player.getPlayerId() > 0 && player.getPlayerStatus() == 1).count();
  }

  /**
   * TODO 未准备数.
   *
   * @return int 准备数量
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 20:13
   * @update 2020/8/31 20:13
   */
  public int unprepared() {
    return (int) playerList.stream()
        .filter(player -> player.getPlayerId() > 0 && player.getPlayerStatus() == 0).count();
  }

  /**
   * TODO 剩余座位.
   *
   * @return int 座位数
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 20:17
   * @update 2020/8/31 20:17
   */
  public int remainingSeat() {
    return (int) playerList.stream()
        .filter(s -> s.getPlayerId() == 0).count();
  }

  /**
   * TODO 入座玩家.
   *
   * @return list 玩家列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 20:19
   * @update 2020/8/31 20:19
   */
  public List<Long> seatedPlayers() {
    List<ExplodingKittensPlayer> playList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getLinkStatus() == 0)
        .collect(Collectors.toList());
    List<Long> readyList = Lists.newArrayList();
    if (CollectionUtils.isNotEmpty(playList)) {
      playList.forEach(s -> readyList.add(s.getPlayerId()));
    }
    return readyList;
  }

  /**
   * TODO 入座玩家.
   *
   * @return int 入座人数
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/3 15:06
   * @update 2020/9/3 15:06
   */
  public int seatedPlayersNum() {
    return (int) playerList.stream()
        .filter(s -> s.getPlayerId() > 0).count();
  }

  /**
   * TODO 断线玩家.
   *
   * @return int 玩家人数
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/3 15:07
   * @update 2020/9/3 15:07
   */
  public int seatedPlayersIsDisconnected() {
    return (int) playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getLinkStatus() == 1).count();
  }

  /**
   * TODO 放置炸弹.
   *
   * @param seatIndex [1-第1张 2-第2张 3-第3张 4-第4张 5-第5张 6-底牌 7-随机]
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 20:23
   * @update 2020/8/31 20:23
   */
  public void placeExploding(Integer seatIndex) {
    if (explodingCardInfo != null) {
      switch (seatIndex) {
        case 1:
          remainingCard.add(0, explodingCardInfo);
          break;
        case 2:
          remainingCard.add(1, explodingCardInfo);
          break;
        case 3:
          remainingCard.add(2, explodingCardInfo);
          break;
        case 4:
          remainingCard.add(3, explodingCardInfo);
          break;
        case 5:
          remainingCard.add(4, explodingCardInfo);
          break;
        case 6:
          remainingCard.add(explodingCardInfo);
          break;
        default:
          if (remainingCard.size() > 0) {
            int randomNo = ThreadLocalRandom.current().nextInt(remainingCard.size());
            remainingCard.add(randomNo, explodingCardInfo);
          } else {
            remainingCard.add(0, explodingCardInfo);
          }
          break;
      }
      explodingProbability();
      this.explodingCardInfo = null;
    }
  }

  /**
   * TODO 清除离线.
   *
   * @return [离线玩家]
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 15:59
   * @update 2020/8/17 15:59
   */
  public List<Long> offlinePlayers() {
    List<ExplodingKittensPlayer> kittensPlayers = playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getLinkStatus() == 1)
        .collect(Collectors.toList());
    List<Long> clearList = Lists.newArrayList();
    List<Integer> seatList = Lists.newArrayList();
    if (CollectionUtils.isNotEmpty(kittensPlayers)) {
      kittensPlayers.forEach(player -> {
        clearList.add(player.getPlayerId());
        seatList.add(player.getSeatNumber());
        GroupManager.removeChannel(roomId, player.getChannel());
        ChatManager.removeChatChannel(roomId, player.getChannel());
      });
    }
    clearList.forEach(playerId -> this.playerList.removeIf(s -> s.isBoolean(playerId)));
    if (CollectionUtils.isNotEmpty(seatList)) {
      seatList.forEach(seat -> {
        ExplodingKittensPlayer newPlayer = new ExplodingKittensPlayer(seat);
        playerList.add(newPlayer);
      });
      // 排序
      this.playerList.sort(Comparator.comparing(ExplodingKittensPlayer::getSeatNumber));
    }
    return clearList;
  }

  /**
   * TODO 金币不足.
   *
   * @return [不足玩家]
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/20 10:01
   * @update 2020/8/20 10:01
   */
  public List<Long> goldShortage() {
    List<ExplodingKittensPlayer> kittensPlayers = playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getPlayerGold() < 20)
        .collect(Collectors.toList());
    List<Long> clearList = Lists.newArrayList();
    if (CollectionUtils.isNotEmpty(kittensPlayers)) {
      kittensPlayers.forEach(player -> {
        leaveSeat(player.getPlayerId());
        clearList.add(player.getPlayerId());
      });
    }
    // 排序
    this.playerList.sort(Comparator.comparing(ExplodingKittensPlayer::getSeatNumber));
    return clearList;
  }

  /**
   * TODO 使用先知.
   *
   * @return List  [前三张]
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/31 20:43
   * @update 2020/8/31 20:43
   */
  public List<ExplodingKittensCard> performProphet() {
    List<ExplodingKittensCard> prophet = Lists.newLinkedList();
    if (CollectionUtils.isNotEmpty(remainingCard)) {
      prophet.addAll(remainingCard);
      if (prophet.size() > VIEW_THREE_CARDS) {
        return prophet.subList(0, 3);
      } else {
        return prophet;
      }
    }
    return Collections.emptyList();
  }

  /**
   * TODO 最近炸弹.
   *
   * @return int 第?张
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/1 8:45
   * @update 2020/9/1 8:45
   */
  public int explodingSeat() {
    int index = 1;
    for (ExplodingKittensCard kittensCard : remainingCard) {
      if (kittensCard.getCardFunction() == 1) {
        break;
      }
      index++;
    }
    return index;
  }

  /**
   * TODO 使用抽底.
   *
   * @return 新的卡牌
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 5:14
   * @since 2020/9/7 5:14
   */
  public ExplodingKittensCard useBottomingCard() {
    if (CollectionUtils.isNotEmpty(remainingCard)) {
      if (remainingCard.size() > 1) {
        return remainingCard.remove(remainingCard.size() - 1);
      } else {
        return remainingCard.remove(remainingCard.size() - 1);
      }
    }
    return null;
  }

  /**
   * TODO 使用转向.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 5:22
   * @since 2020/9/7 5:22
   */
  public void useSteering() {
    if (direction == 1) {
      this.direction = 2;
    } else {
      this.direction = 1;
    }
  }

  /**
   * TODO 可选玩家一.
   *
   * @return list 玩家列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 5:31
   * @since 2020/9/7 5:31
   */
  public List<Long> optionalPlayersOne() {
    List<ExplodingKittensPlayer> playList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0
            // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
            && (s.getPlayerStatus() == 2 || s.getPlayerStatus() == 4))
        .collect(Collectors.toList());
    List<Long> selectList = Lists.newArrayList();
    if (CollectionUtils.isNotEmpty(playList)) {
      playList.forEach(s -> selectList.add(s.getPlayerId()));
    }
    return selectList;
  }

  /**
   * TODO 可选玩家二.
   *
   * @param playerId 玩家ID
   * @return list 玩家列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 5:31
   * @since 2020/9/7 5:31
   */
  public List<Long> optionalPlayersTwo(Long playerId) {
    List<ExplodingKittensPlayer> playList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0
            && !s.isBoolean(playerId)
            // 束缚标记(已束缚玩家不可选)
            && s.getBondageIndex() == 0
            // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
            && (s.getPlayerStatus() == 2 || s.getPlayerStatus() == 4))
        .collect(Collectors.toList());
    List<Long> selectList = Lists.newArrayList();
    if (CollectionUtils.isNotEmpty(playList)) {
      playList.forEach(s -> selectList.add(s.getPlayerId()));
    }
    return selectList;
  }

  /**
   * TODO 可选玩家三.
   *
   * @param playerId 玩家ID
   * @return list 玩家列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 9:32
   * @update 2020/9/7 9:32
   */
  public List<Long> optionalPlayersThree(Long playerId) {
    List<ExplodingKittensPlayer> playList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0
            && !s.isBoolean(playerId)
            // 玩家手牌
            && s.getPlayerCard().size() > 0
            // 用户状态 [0-未准备 1-已准备 2-游戏中 3-已出局 4-已离开]
            && (s.getPlayerStatus() == 2 || s.getPlayerStatus() == 4))
        .collect(Collectors.toList());
    List<Long> selectList = Lists.newArrayList();
    if (CollectionUtils.isNotEmpty(playList)) {
      playList.forEach(s -> selectList.add(s.getPlayerId()));
    }
    return selectList;
  }

  /**
   * TODO 玩家出牌.
   *
   * @param cardId 卡牌ID
   * @param playerId 玩家ID
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/18 13:50
   * @update 2020/8/18 13:50
   */
  public void playCards(Integer cardId, Long playerId) {
    for (ExplodingKittensPlayer kittensPlayer : playerList) {
      if (kittensPlayer.isBoolean(playerId)) {
        kittensPlayer.removeCard(cardId);
        break;
      }
    }
  }

  /**
   * TODO 验证卡牌.
   *
   * @param cardId 卡牌ID
   * @param playerId 玩家ID
   * @return boolean 存在结果
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/4 9:50
   * @update 2020/9/4 9:50
   */
  public boolean verifyCardIsExists(Integer cardId, Long playerId) {
    boolean isExists = false;
    for (ExplodingKittensPlayer kittensPlayer : playerList) {
      if (kittensPlayer.isBoolean(playerId)) {
        List<ExplodingKittensCard> kittensCards = kittensPlayer.getPlayerCard().stream()
            .filter(card -> Objects.equals(card.getCardId(), cardId))
            .collect(Collectors.toList());
        isExists = CollectionUtils.isNotEmpty(kittensCards);
        break;
      }
    }
    return isExists;
  }

  /**
   * TODO 炸弹概率.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 10:58
   * @update 2020/9/7 10:58
   */
  public void explodingProbability() {
    if (explodingNum == 0 && remainingCard.size() == 0) {
      this.probability = 0;
    } else {
      BigDecimal exploding = new BigDecimal(explodingNum);
      BigDecimal cardSize = new BigDecimal(remainingCard.size());
      BigDecimal probability = exploding.divide(cardSize, 2, BigDecimal.ROUND_HALF_UP);
      NumberFormat numberFormat = NumberFormat.getPercentInstance();
      numberFormat.setMaximumFractionDigits(2);
      int bombPro = Integer.valueOf(numberFormat.format(probability.doubleValue()).replace("%", ""));
      this.setProbability(bombPro);

    }
  }

  /**
   * TODO 消除诅咒.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/3 17:30
   * @update 2020/9/3 17:30
   */
  public void eliminateCurse() {
    if (cursesNum == 1) {
      this.cursesNum = cursesNum - 1;
      this.cursedPlayer = 0L;
    } else if (cursesNum > 1) {
      this.cursesNum = cursesNum - 1;
    }
  }

  /**
   * TODO 开始洗牌.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 5:00
   * @since 2020/9/7 5:00
   */
  public void startShuffling() {
    Collections.shuffle(remainingCard);
  }

  /**
   * TODO 玩家出局.
   *
   * @param playerId 玩家ID
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 11:30
   * @update 2020/9/7 11:30
   */
  public void playerOut(Long playerId) {
    if (explodingNum > 0) {
      this.explodingNum = explodingNum - 1;
    }
    this.explodingCardInfo = null;
    explodingProbability();
    ExplodingKittensPlayer outPlayer = getPlayerInfo(playerId);
    outPlayer.setPlayerStatus(3);
    this.rankingList.add(playerId);
  }

  /**
   * TODO 更新金币.
   *
   * @param playerId 玩家ID
   * @param gold 获得/失去金币
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/9/7 20:00
   * @update 2020/9/7 20:00
   */
  public void updateGold(Long playerId, Integer gold) {
    ExplodingKittensPlayer kittensPlayer = getPlayerInfo(playerId);
    kittensPlayer.setPlayerGold(kittensPlayer.getPlayerGold() + gold);
  }

  /**
   * TODO 初始游戏.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/20 9:33
   * @update 2020/8/20 9:33
   */
  public void initGame() {
    this.roomStatus = 0;
    this.explodingNum = 4;
    this.probability = 0;
    this.direction = 1;
    this.cursesNum = 0;
    this.cursedPlayer = 0L;
    this.prayForPlayers = 0L;
    this.nowActionPlayer = 0L;
    this.startTime = null;
    this.actionTime = null;
    this.desktopCardInfo = null;
    this.explodingCardInfo = null;
    this.rankingList = Lists.newCopyOnWriteArrayList();
    this.remainingCard = Lists.newCopyOnWriteArrayList();
    // 初始玩家
    playerList.stream().filter(player -> player.getPlayerId() > 0)
        .forEach(ExplodingKittensPlayer::init);
  }

  /**
   * TODO 玩家离开.
   *
   * @param playerId 玩家ID
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/20 13:53
   * @update 2020/8/20 13:53
   */
  public void leaveGame(Long playerId, Integer identity) {
    ExplodingKittensPlayer player = getPlayerInfo(playerId);
    if (identity == 0) {
      int seat = player.getSeatNumber();
      this.playerList.removeIf(s -> s.getPlayerId().equals(playerId));
      ExplodingKittensPlayer newPlayer = new ExplodingKittensPlayer(seat);
      this.playerList.add(newPlayer);
      // 排序
      this.playerList.sort(Comparator.comparing(ExplodingKittensPlayer::getSeatNumber));
    } else {
      this.audienceGroup.remove(player.getChannel());
      this.audienceList.removeIf(s -> s.getPlayerId().equals(playerId));
    }
  }

  /**
   * TODO 添加定时.
   *
   * @param taskId 任务ID.
   * @param timeout 定时任务.
   * @author WangCaiWen·1443710411@qq.com
   * @date 2020/8/4 17:28
   * @since 2020/8/4 17:28
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
   * @param taskId 定时ID.
   * @author WangCaiWen·1443710411@qq.com
   * @date 2020/8/4 17:29
   * @since 2020/8/4 17:29
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
   * @param taskId 定时ID.
   * @author WangCaiWen·1443710411@qq.com
   * @date 2020/8/4 17:30
   * @since 2020/8/4 17:30
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
   * @date 2020/8/4 17:30
   * @since 2020/8/4 17:30
   */
  public void destroy() {
    timeOutMap.values().forEach(Timeout::cancel);
    timeOutMap.clear();
  }
}
