package com.enuos.live.handle.game.f30251;

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
 * TODO 优诺房间.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V2.0.0
 * @since 2020/8/13 11:10
 */

@Data
@SuppressWarnings("WeakerAccess")
public class UnoRoom {

  /**
   * 房间ID [例如：2020].
   */
  private Long roomId;
  /**
   * 开放方式 0「公开」 1「创建」.
   */
  private Integer openWay;
  /**
   * 当前玩家 [例如：128379773].
   */
  private Long nowActionId = 0L;
  /**
   * 房间状态 0-未开始 1-已开始.
   */
  private Integer roomStatus = 0;
  /**
   * 当前颜色 红-1 黄-2 蓝-3 绿-4.
   */
  private Integer nowColors;
  /**
   * 旋转方向 1-顺时针 2-逆时针.
   */
  private Integer direction;
  /**
   * 开始时间「例如：2020-07-08T16:58:53.978」.
   */
  private LocalDateTime startTime;
  /**
   * 操作时间「例如：2020-07-08T16:58:53.978」.
   */
  private LocalDateTime actionTime;
  /**
   * 开局扑克.
   */
  private List<UnoPoker> grantUnoPoker = Lists.newCopyOnWriteArrayList();
  /**
   * 扑克仓库.
   */
  private List<UnoPoker> unoPokerDepot = Lists.newCopyOnWriteArrayList();
  /**
   * 回收扑克.
   */
  private List<UnoPoker> recyclePoker = Lists.newCopyOnWriteArrayList();
  /**
   * 玩家列表.
   */
  private List<UnoPlayer> playerList = Lists.newCopyOnWriteArrayList();
  /**
   * 观战列表.
   */
  private List<UnoPlayer> watchList = Lists.newCopyOnWriteArrayList();
  /**
   * 观看群组.
   */
  private ChannelGroup watchGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
  /**
   * 定时数据.
   */
  private HashMap<Integer, Timeout> timeOutMap = Maps.newHashMap();
  /**
   * 出牌数据.
   */
  private byte[] playCard;
  /**
   * 选色数据.
   */
  private byte[] selectColor;
  /**
   * 摸牌数据.
   */
  private byte[] touchCard;
  /**
   * 质疑数据.
   */
  private byte[] questionCards;
  /**
   * 观战数据.
   */
  private byte[] watchInfo;
  /**
   * 发牌数据.
   */
  private byte[] dealCard;

  /**
   * 静态数据.
   */
  private static final int GRANT_POKER = 28;
  private static final int ZERO_CARD = 4;
  private static final int DIGITAL_CARD = 10;
  private static final int CARD_COLORS = 5;
  private static final int CARD_FLAG = 5;
  private static final int FUNCTION_CARD_MIN = 10;
  private static final int FUNCTION_CARD_MAX = 13;
  private static final int UNIVERSAL_CARD_MIN = 13;
  private static final int UNIVERSAL_CARD_MAX = 15;
  private static final int PLAYER_NUM_MAX = 5;
  private static final int INIT_CARD_NUM = 7;

  UnoRoom(Long roomId) {
    this.roomId = roomId;
    for (int i = 1; i < PLAYER_NUM_MAX; i++) {
      UnoPlayer player = new UnoPlayer(i);
      this.playerList.add(player);
    }
  }

  /**
   * TODO 创建扑克.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 13:19
   * @update 2020/8/17 13:19
   */
  private void createPoker() {
    zeroCard();
    digitalCard();
    functionCard();
    universalCard();
    // 洗牌
    Collections.shuffle(unoPokerDepot);
    // 提取28张初始牌
    List<UnoPoker> tempList = Lists.newLinkedList();
    while (tempList.size() < GRANT_POKER) {
      tempList.add(unoPokerDepot.remove(0));
    }
    this.grantUnoPoker.addAll(tempList);
  }

  /**
   * TODO 创建零字牌.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 13:19
   * @update 2020/8/17 13:19
   */
  private void zeroCard() {
    // 零字牌
    List<UnoPoker> zeroCard = Lists.newLinkedList();
    int colors = 1;
    while (zeroCard.size() < ZERO_CARD) {
      UnoPoker unoPoker = new UnoPoker();
      unoPoker.setPokerId(0);
      unoPoker.setPokerColors(colors);
      unoPoker.setPokerFunction(0);
      zeroCard.add(unoPoker);
      colors++;
    }
    this.unoPokerDepot.addAll(zeroCard);
  }

  /**
   * TODO 创建数字牌.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 13:20
   * @update 2020/8/17 13:20
   */
  private void digitalCard() {
    // 数字牌
    List<UnoPoker> digitalCard = Lists.newLinkedList();
    int colors = 1;
    for (int i = 1; i < DIGITAL_CARD; i++) {
      while (colors < CARD_COLORS) {
        UnoPoker unoPoker = new UnoPoker();
        unoPoker.setPokerId(i);
        unoPoker.setPokerColors(colors);
        unoPoker.setPokerFunction(0);
        digitalCard.add(unoPoker);
        colors++;
      }
      if (colors == 5) {
        colors = 1;
      }
    }
    // 开局扑克
    this.unoPokerDepot.addAll(digitalCard);
    this.unoPokerDepot.addAll(digitalCard);
    int pokerNo = ThreadLocalRandom.current().nextInt(unoPokerDepot.size());
    this.recyclePoker.add(unoPokerDepot.remove(pokerNo));
  }

  /**
   * TODO 创建功能牌.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 13:21
   * @update 2020/8/17 13:21
   */
  private void functionCard() {
    List<UnoPoker> functionCard = Lists.newLinkedList();
    // 功能牌
    int colors = 1;
    for (int i = FUNCTION_CARD_MIN; i < FUNCTION_CARD_MAX; i++) {
      while (colors < CARD_COLORS) {
        UnoPoker unoPoker = new UnoPoker();
        unoPoker.setPokerId(i);
        unoPoker.setPokerColors(colors);
        if (i == 10) {
          unoPoker.setPokerFunction(1);
        } else if (i == 11) {
          unoPoker.setPokerFunction(2);
        } else {
          unoPoker.setPokerFunction(3);
        }
        functionCard.add(unoPoker);
        colors++;
      }
      if (colors == 5) {
        colors = 1;
      }
    }
    this.unoPokerDepot.addAll(functionCard);
    this.unoPokerDepot.addAll(functionCard);
  }

  /**
   * TODO 创建万能牌.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 13:21
   * @update 2020/8/17 13:21
   */
  private void universalCard() {
    // 万能牌
    List<UnoPoker> universalCard = Lists.newLinkedList();
    int flag = 1;
    for (int i = UNIVERSAL_CARD_MIN; i < UNIVERSAL_CARD_MAX; i++) {
      while (flag < CARD_FLAG) {
        UnoPoker unoPoker = new UnoPoker();
        unoPoker.setPokerId(i);
        unoPoker.setPokerColors(0);
        if (i == 13) {
          unoPoker.setPokerFunction(4);
        } else {
          unoPoker.setPokerFunction(5);
        }
        universalCard.add(unoPoker);
        flag++;
      }
      if (flag == 5) {
        flag = 1;
      }
    }
    this.unoPokerDepot.addAll(universalCard);
  }

  /**
   * TODO 进入座位.
   *
   * @param channel 快速通道
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 14:12
   * @update 2020/8/17 14:12
   */
  public void enterSeat(Channel channel, I10001.PlayerInfo playerInfo) {
    if (Objects.nonNull(playerInfo)) {
      for (UnoPlayer player : playerList) {
        if (player.getPlayerId() == 0) {
          player.setPlayerId(playerInfo.getUserId());
          player.setPlayerName(playerInfo.getNickName());
          player.setPlayerAvatar(playerInfo.getIconUrl());
          player.setPlayerSex(playerInfo.getSex());
          player.setChannel(channel);
          break;
        }
      }
    }
    this.playerList.sort(Comparator.comparing(UnoPlayer::getSeatNumber));
  }


  /**
   * TODO 进入观战.
   *
   * @param channel 快速通道
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 14:22
   * @update 2020/8/17 14:22
   */
  public void enterWatch(Channel channel, I10001.PlayerInfo playerInfo) {
    if (Objects.nonNull(playerInfo)) {
      UnoPlayer player = new UnoPlayer(0);
      player.setPlayerId(playerInfo.getUserId());
      player.setPlayerName(playerInfo.getNickName());
      player.setPlayerAvatar(playerInfo.getIconUrl());
      player.setPlayerSex(playerInfo.getSex());
      player.setChannel(channel);
      // 身份观众
      player.setIdentity(1);
      this.watchList.add(player);
      this.watchGroup.add(channel);
    }
  }


  /**
   * TODO 玩家信息.
   *
   * @param playerId 玩家ID
   * @return UnoPlayer 玩家信息
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 14:17
   * @update 2020/8/17 14:17
   */
  public UnoPlayer getPlayerInfo(Long playerId) {
    UnoPlayer player = playerList.stream().filter(unoPlayer -> unoPlayer.isBoolean(playerId))
        .findFirst().orElse(null);
    if (Objects.isNull(player)) {
      player = watchList.stream().filter(watchPlayer -> watchPlayer.isBoolean(playerId)).findFirst()
          .orElse(null);
    }
    return player;
  }

  /**
   * TODO 头像列表.
   *
   * @return List 头像列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 14:25
   * @update 2020/8/17 14:25
   */
  public List<String> getWatchPlayerIcon() {
    List<String> iconList = Lists.newLinkedList();
    watchList.forEach(audience -> iconList.add(audience.getPlayerAvatar()));
    return iconList;
  }

  /**
   * TODO 设置玩家.
   *
   * @param oldPlayer 上一玩家
   * @param direction 当前方向 [ 0-顺时针 1-逆时针]
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 14:32
   * @update 2020/8/17 14:32
   */
  public void setUpActionPlayer(Long oldPlayer, Integer direction) {
    List<UnoPlayer> unoPlayerList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0)
        .sorted(Comparator.comparing(UnoPlayer::getSeatNumber))
        .collect(Collectors.toList());
    if (direction == 2) {
      // 反转列表
      Collections.reverse(unoPlayerList);
    }
    if (oldPlayer == 0L) {
      int playerNo = ThreadLocalRandom.current().nextInt(unoPlayerList.size());
      this.nowActionId = unoPlayerList.get(playerNo).getPlayerId();
    } else {
      int index = 0;
      for (UnoPlayer player : unoPlayerList) {
        if (player.isBoolean(oldPlayer)) {
          break;
        }
        index++;
      }
      if (index == unoPlayerList.size() - 1) {
        this.nowActionId = unoPlayerList.get(0).getPlayerId();
      } else {
        this.nowActionId = unoPlayerList.get(index + 1).getPlayerId();
      }
    }
  }

  /**
   * TODO 下一玩家.
   *
   * @param oldPlayer 上一玩家
   * @return long 下一玩家
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/18 12:48
   * @update 2020/8/18 12:48
   */
  public long getNextActionUser(Long oldPlayer) {
    List<UnoPlayer> unoPlayerList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0)
        .sorted(Comparator.comparing(UnoPlayer::getSeatNumber))
        .collect(Collectors.toList());
    if (direction == 2) {
      // 反转列表
      Collections.reverse(unoPlayerList);
    }
    int index = 0;
    for (UnoPlayer player : unoPlayerList) {
      if (player.isBoolean(oldPlayer)) {
        break;
      }
      index++;
    }
    List<Long> players = Lists.newLinkedList();
    unoPlayerList.forEach(player -> players.add(player.getPlayerId()));
    if (index == players.size() - 1) {
      return players.get(0);
    } else {
      return players.get(index + 1);
    }
  }

  /**
   * TODO 上一玩家.
   *
   * @param nowPlayer 当前玩家
   * @return long 上一玩家
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/19 9:52
   * @update 2020/8/19 9:52
   */
  public long getPreviousActionUser(Long nowPlayer) {
    List<UnoPlayer> unoPlayerList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0)
        .sorted(Comparator.comparing(UnoPlayer::getSeatNumber))
        .collect(Collectors.toList());
    if (direction == 2) {
      // 反转列表
      Collections.reverse(unoPlayerList);
    }
    int index = 0;
    for (UnoPlayer player : unoPlayerList) {
      if (player.isBoolean(nowPlayer)) {
        break;
      }
      index++;
    }
    List<Long> players = Lists.newLinkedList();
    unoPlayerList.forEach(player -> players.add(player.getPlayerId()));
    if (index == 0) {
      return players.get(unoPlayerList.size() - 1);
    } else {
      return players.get(index - 1);
    }
  }

  /**
   * TODO 初始缓存.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/26 20:24
   * @update 2020/8/26 20:24
   */
  public void initByte() {
    this.playCard = null;
    this.selectColor = null;
    this.touchCard = null;
    this.questionCards = null;
    this.watchInfo = null;
    this.dealCard = null;
  }

  /**
   * TODO 已准备数.
   *
   * @return int 准备数量
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 14:53
   * @update 2020/8/17 14:53
   */
  public int preparations() {
    List<UnoPlayer> unoPlayerList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getPlayerStatus() == 1)
        .collect(Collectors.toList());
    return unoPlayerList.size();
  }

  /**
   * TODO 未准备数.
   *
   * @return int 准备数量
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 14:55
   * @update 2020/8/17 14:55
   */
  public int unprepared() {
    List<UnoPlayer> unoPlayerList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getPlayerStatus() == 0)
        .collect(Collectors.toList());
    return unoPlayerList.size();
  }

  /**
   * TODO 剩余座位.
   *
   * @return int 剩余座位
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 14:57
   * @update 2020/8/17 14:57
   */
  public int remainingSeat() {
    List<UnoPlayer> unoPlayerList = playerList.stream()
        .filter(s -> s.getPlayerId() == 0)
        .collect(Collectors.toList());
    return unoPlayerList.size();
  }

  /**
   * TODO 入座玩家.
   *
   * @return list 入座玩家
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/20 10:42
   * @update 2020/8/20 10:42
   */
  public List<Long> seatedPlayers() {
    List<UnoPlayer> playList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getLinkStatus() == 0)
        .collect(Collectors.toList());
    List<Long> readyList = Lists.newArrayList();
    if (CollectionUtils.isNotEmpty(playList)) {
      playList.forEach(s -> readyList.add(s.getPlayerId()));
    }
    return readyList;
  }

  public int seatedPlayersNum() {
    List<UnoPlayer> playList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0)
        .collect(Collectors.toList());
    return playList.size();
  }

  public int seatedPlayersIsDisconnected() {
    List<UnoPlayer> playList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getLinkStatus() == 1)
        .collect(Collectors.toList());
    return playList.size();
  }

  /**
   * TODO 开始游戏.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 15:39
   * @update 2020/8/17 15:39
   */
  public void startGame() {
    this.roomStatus = 1;
    // 当前方向
    this.direction = ThreadLocalRandom.current().nextInt(2) + 1;
    // 创建扑克
    createPoker();
    // 开始发牌
    licensing();
    // 当前颜色
    this.nowColors = recyclePoker.get(recyclePoker.size() - 1).getPokerColors();
    // 更新状态
    playerList.forEach(player -> player.setPlayerStatus(2));
    initByte();
  }

  /**
   * TODO 初始游戏.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/20 9:33
   * @update 2020/8/20 9:33
   */
  public void initGame() {
    this.nowActionId = 0L;
    this.roomStatus = 0;
    this.nowColors = null;
    this.direction = null;
    this.startTime = null;
    this.actionTime = null;
    this.grantUnoPoker = Lists.newCopyOnWriteArrayList();
    this.unoPokerDepot = Lists.newCopyOnWriteArrayList();
    this.recyclePoker = Lists.newCopyOnWriteArrayList();
    // 初始玩家
    playerList.forEach(player -> {
      if (player.getPlayerId() > 0) {
        player.init();
      }
    });
    initByte();
  }

  /**
   * TODO 开始发牌.
   *
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 15:38
   * @update 2020/8/17 15:38
   */
  private void licensing() {
    playerList.forEach(player -> {
      List<UnoPoker> playerPoker = Lists.newLinkedList();
      while (playerPoker.size() < INIT_CARD_NUM) {
        int pokerNo = ThreadLocalRandom.current().nextInt(grantUnoPoker.size());
        playerPoker.add(grantUnoPoker.remove(pokerNo));
      }
      // 排序
      playerPoker = pokerSort(playerPoker);
      player.setPlayerPoker(playerPoker);
    });
    this.grantUnoPoker = Lists.newCopyOnWriteArrayList();
  }

  /**
   * TODO 扑克排序.
   *
   * @param playerPoker 乱的扑克
   * @return list 排序过后
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 15:26
   * @update 2020/8/17 15:26
   */
  public List<UnoPoker> pokerSort(List<UnoPoker> playerPoker) {
    List<UnoPoker> newPokerList = Lists.newLinkedList();
    if (playerPoker.size() > 1) {
      // 1.找出万能牌
      List<UnoPoker> universalCard = playerPoker.stream()
          .filter(s -> s.getPokerId() == 13 || s.getPokerId() == 14)
          .sorted(Comparator.comparing(UnoPoker::getPokerId))
          .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(universalCard)) {
        newPokerList.addAll(universalCard);
      }
      // 2.红色牌组
      List<UnoPoker> redCardList = playerPoker.stream()
          .filter(s -> s.getPokerColors() == 1)
          .sorted(Comparator.comparing(UnoPoker::getPokerId))
          .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(redCardList)) {
        newPokerList.addAll(redCardList);
      }
      // 3.黄色牌组
      List<UnoPoker> yellowCardList = playerPoker.stream()
          .filter(s -> s.getPokerColors() == 2)
          .sorted(Comparator.comparing(UnoPoker::getPokerId))
          .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(yellowCardList)) {
        newPokerList.addAll(yellowCardList);
      }
      // 4.蓝色牌组
      List<UnoPoker> blueCardList = playerPoker.stream()
          .filter(s -> s.getPokerColors() == 3)
          .sorted(Comparator.comparing(UnoPoker::getPokerId))
          .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(blueCardList)) {
        newPokerList.addAll(blueCardList);
      }
      // 5.绿色牌组
      List<UnoPoker> greenCardList = playerPoker.stream()
          .filter(s -> s.getPokerColors() == 4)
          .sorted(Comparator.comparing(UnoPoker::getPokerId))
          .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(greenCardList)) {
        newPokerList.addAll(greenCardList);
      }
    } else {
      newPokerList.addAll(playerPoker);
    }
    return newPokerList;
  }

  /**
   * TODO 提示扑克.
   *
   * @param pokerList 当前扑克
   * @return list 提示扑克
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/18 10:24
   * @update 2020/8/18 10:24
   */
  public List<UnoPoker> hintPoker(List<UnoPoker> pokerList) {
    List<UnoPoker> hintPokerList = Lists.newLinkedList();
    UnoPoker lastPoker = getLastPoker();
    if (lastPoker.getPokerId() < 13) {
      // 1.找出万能牌
      List<UnoPoker> universalCard = pokerList.stream()
          .filter(s -> s.getPokerId() == 13 || s.getPokerId() == 14)
          .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(universalCard)) {
        hintPokerList.addAll(universalCard);
      }
      // 2.对应颜色
      List<UnoPoker> colorCard = pokerList.stream()
          .filter(s -> s.getPokerColors().equals(nowColors))
          .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(colorCard)) {
        hintPokerList.addAll(colorCard);
      }
      // 只有对方打出转向牌
      if (lastPoker.getPokerId() == 11) {
        List<UnoPoker> turnCard = pokerList.stream()
            .filter(s -> s.getPokerId().equals(lastPoker.getPokerId()))
            .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(turnCard)) {
          hintPokerList.addAll(turnCard);
        }
      }
      if (lastPoker.getPokerId() == 10) {
        List<UnoPoker> linkCard = pokerList.stream()
            .filter(s -> (!s.getPokerColors().equals(nowColors) || Objects.equals(s.getPokerColors(), nowColors))
                && s.getPokerId().equals(lastPoker.getPokerId()))
            .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(linkCard)) {
          hintPokerList.addAll(linkCard);
        }
      }
      if (lastPoker.getPokerId() < 10) {
        // 3.相关联的牌ID
        List<UnoPoker> linkCard = pokerList.stream()
            .filter(s -> !s.getPokerColors().equals(nowColors) &&
                s.getPokerId().equals(lastPoker.getPokerId()))
            .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(linkCard)) {
          hintPokerList.addAll(linkCard);
        }
      }
    } else {
      // 当前是万能牌 取当前房间颜色
      // 1.找出万能牌
      List<UnoPoker> universalCard = pokerList.stream()
          .filter(s -> s.getPokerId() == 13 || s.getPokerId() == 14)
          .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(universalCard)) {
        hintPokerList.addAll(universalCard);
      }
      // 2.对应颜色
      List<UnoPoker> colorCard = pokerList.stream()
          .filter(s -> s.getPokerColors().equals(nowColors))
          .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(colorCard)) {
        hintPokerList.addAll(colorCard);
      }
    }
    return hintPokerList;
  }

  /**
   * TODO 最后扑克.
   *
   * @return 扑克信息
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/18 10:03
   * @update 2020/8/18 10:03
   */
  public UnoPoker getLastPoker() {
    return recyclePoker.get(recyclePoker.size() - 1);
  }

  /**
   * TODO 玩家摸牌.
   *
   * @return 扑克信息
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/18 10:06
   * @update 2020/8/18 10:06
   */
  public UnoPoker touchCards() {
    return unoPokerDepot.remove(0);
  }

  /**
   * TODO 玩家出牌.
   *
   * @param poker 扑克信息
   * @param playerId 玩家ID
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/18 13:50
   * @update 2020/8/18 13:50
   */
  public void playCards(UnoPoker poker, Long playerId) {
    for (UnoPlayer player : playerList) {
     if (player.isBoolean(playerId)) {
       player.removePoker(poker);
       this.recyclePoker.add(poker);
       break;
     }
    }
  }
  /**
   * TODO 加入座位.
   *
   * @param playerId 玩家ID
   * @return int 座位号
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 15:45
   * @update 2020/8/17 15:45
   */
  public int joinSeat(Long playerId) {
    int seat = 0;
    UnoPlayer player = getPlayerInfo(playerId);
    for (UnoPlayer unoPlayer : playerList) {
      if (unoPlayer.getPlayerId() == 0) {
        unoPlayer.setPlayerId(player.getPlayerId());
        unoPlayer.setPlayerName(player.getPlayerName());
        unoPlayer.setPlayerAvatar(player.getPlayerAvatar());
        unoPlayer.setPlayerSex(player.getPlayerSex());
        unoPlayer.setChannel(player.getChannel());
        unoPlayer.setAvatarFrame(player.getAvatarFrame());
        unoPlayer.setPlayerGold(player.getPlayerGold());
        unoPlayer.setCardBackSkin(player.getCardBackSkin());
        unoPlayer.setPlayerStatus(0);
        unoPlayer.setIdentity(0);
        seat = unoPlayer.getSeatNumber();
        break;
      }
    }
    this.watchGroup.remove(player.getChannel());
    this.watchList.removeIf(s -> s.getPlayerId().equals(playerId));
    return seat;
  }

  /**
   * TODO 离开座位.
   *
   * @param playerId 玩家ID
   * @return int 座位号
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 15:56
   * @update 2020/8/17 15:56
   */
  public int leaveSeat(Long playerId) {
    UnoPlayer player = getPlayerInfo(playerId);
    int seat = player.getSeatNumber();
    UnoPlayer watch = new UnoPlayer(0);
    watch.setPlayerId(player.getPlayerId());
    watch.setPlayerName(player.getPlayerName());
    watch.setPlayerSex(player.getPlayerSex());
    watch.setPlayerAvatar(player.getPlayerAvatar());
    watch.setAvatarFrame(player.getAvatarFrame());
    watch.setChannel(player.getChannel());
    watch.setLinkStatus(player.getLinkStatus());
    watch.setPlayerGold(player.getPlayerGold());
    watch.setCardBackSkin(player.getCardBackSkin());
    watch.setIdentity(1);
    this.watchList.add(watch);
    this.watchGroup.add(watch.getChannel());
    // 离开
    this.playerList.removeIf(s -> s.getPlayerId().equals(playerId));
    UnoPlayer newPlayer = new UnoPlayer(seat);
    this.playerList.add(newPlayer);
    // 排序
    this.playerList.sort(Comparator.comparing(UnoPlayer::getSeatNumber));
    return newPlayer.getSeatNumber();
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
    UnoPlayer player = getPlayerInfo(playerId);
    if (identity == 0) {
      int seat = player.getSeatNumber();
      this.playerList.removeIf(s -> s.getPlayerId().equals(playerId));
      UnoPlayer newPlayer = new UnoPlayer(seat);
      this.playerList.add(newPlayer);
      // 排序
      this.playerList.sort(Comparator.comparing(UnoPlayer::getSeatNumber));
    } else {
      this.watchGroup.remove(player.getChannel());
      this.watchList.removeIf(s -> s.getPlayerId().equals(playerId));

    }
  }

  /**
   * TODO 清除离线.
   *
   * @return list 离线玩家
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 15:59
   * @update 2020/8/17 15:59
   */
  public List<Long> offlinePlayers() {
    List<UnoPlayer> unoPlayerList = playerList.stream()
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
    clearList.forEach(playerId -> playerList.removeIf(s ->s.isBoolean(playerId)));
    if (CollectionUtils.isNotEmpty(seatList)) {
      seatList.forEach(integer -> {
        UnoPlayer newPlayer = new UnoPlayer(integer);
        playerList.add(newPlayer);
      });
      // 排序
      this.playerList.sort(Comparator.comparing(UnoPlayer::getSeatNumber));
    }
    return clearList;
  }

  /**
   * TODO 金币不足.
   *
   * @return list 不足玩家
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/20 10:01
   * @update 2020/8/20 10:01
   */
  public List<Long> goldShortage() {
    List<UnoPlayer> unoPlayerList = playerList.stream()
        .filter(s -> s.getPlayerId() > 0 && s.getPlayerGold() < 120)
        .collect(Collectors.toList());
    List<Long> clearList = Lists.newArrayList();
    if (CollectionUtils.isNotEmpty(unoPlayerList)) {
      unoPlayerList.forEach(player -> {
        leaveSeat(player.getPlayerId());
        clearList.add(player.getPlayerId());
      });
    }
    // 排序
    this.playerList.sort(Comparator.comparing(UnoPlayer::getSeatNumber));
    return clearList;
  }


  /**
   * TODO 计算得分.
   *
   * @return list 玩家列表
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 16:09
   * @update 2020/8/17 16:09
   */
  public List<UnoPlayer> getScoreRanking() {
    playerList.forEach(player -> {
      List<UnoPoker> playerPoker = player.getPlayerPoker();
      if (CollectionUtils.isNotEmpty(playerPoker)) {
        for (UnoPoker unoPoker : playerPoker) {
          if (unoPoker.getPokerId() > 0) {
            Integer pokerId = unoPoker.getPokerId();
            if (pokerId > 9 && pokerId < 13) {
              // -20
              player.setGameScore(player.getGameScore() + (-20));
            } else if (pokerId > 12) {
              // -50
              player.setGameScore(player.getGameScore() + (-50));
            } else {
              player.setGameScore(player.getGameScore() + pokerId * -1);
            }
          }
        }
      } else {
        player.setGameScore(0);
      }
    });
    // 得分排序
    List<UnoPlayer> scoreRanking = playerList.stream()
        .sorted(Comparator.comparing(UnoPlayer::getGameScore).reversed())
        .collect(Collectors.toList());
    UnoPlayer playerOne = scoreRanking.get(0);
    UnoPlayer playerTwo = scoreRanking.get(1);
    UnoPlayer playerThree = scoreRanking.get(2);
    UnoPlayer playerFour = scoreRanking.get(3);
    int difference = Math.abs(playerTwo.getGameScore()) - Math.abs(playerOne.getGameScore());
    BigDecimal probability = getProbability(difference);
    int threeScore = Math.abs(playerThree.getGameScore());
    // 第三名默认最低 40
    int threeGold = 0;
    if (threeScore > 0 && threeScore <= 40) {
      threeGold = 40;
    } else if (threeScore > 40 && threeScore < 80) {
      threeGold = threeScore;
    } else if (threeScore > 80) {
      threeGold = 80;
    }
    int fourScore = Math.abs(playerFour.getGameScore());
    // 第四名默认最低 60
    int fourGold = 0;
    if (fourScore > 0 && fourScore <= 60) {
      fourGold = 60;
    } else if (fourScore > 60 && fourScore <= 120) {
      fourGold = fourScore;
    } else if (fourScore > 120) {
      fourGold = 120;
    }
    BigDecimal goldSum = new BigDecimal(threeGold + fourGold);
    BigDecimal gameFees = new BigDecimal("0.8");
    BigDecimal probValue = new BigDecimal(1).subtract(probability);
    int oneGold = goldSum.multiply(gameFees).multiply(probability).intValue();
    int twoGold = goldSum.multiply(gameFees).multiply(probValue).intValue();
    int index = 0;
    for (UnoPlayer player : scoreRanking) {
      if (index == 0) {
        player.setGameGold(oneGold);
      } else if (index == 1) {
        player.setGameGold(twoGold);
      } else if (index == 2) {
        player.setGameGold(threeGold * -1);
      } else {
        player.setGameGold(fourGold * -1);
        break;
      }
      player.setPlayerGold(player.getPlayerGold() + player.getGameGold());
      for (UnoPlayer unoPlayer : playerList) {
        if (unoPlayer.isBoolean(player.getPlayerId())) {
          unoPlayer.setPlayerGold(player.getPlayerGold());
          unoPlayer.setGameGold(player.getGameGold());
          break;
        }
      }
      index++;
    }
    return scoreRanking;
  }

  /**
   * TODO 获得概率.
   *
   * @param difference 差值
   * @return 概率
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/17 16:55
   * @update 2020/8/17 16:55
   */
  private BigDecimal getProbability(int difference) {
    if (difference == 0) {
      return new BigDecimal("0.5");
    } else if (difference > 0 && difference <= 10) {
      return new BigDecimal("0.6");
    } else if (difference > 10 && difference <= 30) {
      return new BigDecimal("0.65");
    } else if (difference > 30 && difference <= 50) {
      return new BigDecimal("0.7");
    } else if (difference > 50 && difference <= 70) {
      return new BigDecimal("0.75");
    } else {
      return new BigDecimal("0.8");
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
    for (Timeout out : timeOutMap.values()) {
      out.cancel();
    }
  }
}
