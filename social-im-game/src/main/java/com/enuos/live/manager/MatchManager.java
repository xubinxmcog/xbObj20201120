package com.enuos.live.manager;

import com.enuos.live.pojo.MatchRoom;
import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * TODO 匹配管理.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/6/24 14:09
 */

@Component
public class MatchManager {

  /** 谁是卧底 4-6 文字/语音. */
  public static CopyOnWriteArrayList<MatchRoom> MATCH_FIND_SPY_ONE_WORDS = Lists.newCopyOnWriteArrayList();
  public static CopyOnWriteArrayList<MatchRoom> MATCH_FIND_SPY_ONE_SPECK = Lists.newCopyOnWriteArrayList();
  /** 谁是卧底 7-8 文字/语音. */
  public static CopyOnWriteArrayList<MatchRoom> MATCH_FIND_SPY_TWO_WORDS = Lists.newCopyOnWriteArrayList();
  public static CopyOnWriteArrayList<MatchRoom> MATCH_FIND_SPY_TWO_SPECK = Lists.newCopyOnWriteArrayList();

  /**
   * TODO 刷新谁是卧底匹配.
   *
   * @param matchRoom [匹配数据]
   * @param roomType [0{4-6人} 1{7-8人}]
   * @param speakMode [0{文字} 1{语音}]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/18 14:05
   * @update 2020/9/18 14:05
   */
  public static void updFindSpyMatch(MatchRoom matchRoom, Integer roomType, Integer speakMode) {
    if (roomType == 0) {
      findSpyRoomMatchTypeOne(matchRoom, speakMode);
    } else {
      findSpyRoomMatchTypeTwo(matchRoom, speakMode);
    }
  }

  /**
   * TODO 间谍匹配类型一.
   *
   * @param matchRoom [匹配数据]
   * @param speakMode [0{文字} 1{语音}]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/18 14:06
   * @update 2020/9/18 14:06
   */
  private static void findSpyRoomMatchTypeOne(MatchRoom matchRoom, Integer speakMode) {
    if (speakMode == 0) {
      if (CollectionUtils.isNotEmpty(MATCH_FIND_SPY_ONE_WORDS)) {
        MatchRoom matchInfo = MATCH_FIND_SPY_ONE_WORDS.stream()
            .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
            .findFirst().orElse(null);
        if (Objects.nonNull(matchInfo)) {
          // 谁是卧底.刷新人数.4-6人.文字场
          matchInfo.setPeopleNum(matchRoom.getPeopleNum());
          MATCH_FIND_SPY_ONE_WORDS.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        } else {
          // 谁是卧底.开放匹配.4-6人.文字场
          MATCH_FIND_SPY_ONE_WORDS.add(matchRoom);
          MATCH_FIND_SPY_ONE_WORDS.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
      } else {
        // 谁是卧底.开放匹配.4-6人.文字场
        MATCH_FIND_SPY_ONE_WORDS.add(matchRoom);
        MATCH_FIND_SPY_ONE_WORDS.sort(Comparator.comparing(MatchRoom::getPeopleNum));
      }
    } else {
      if (CollectionUtils.isNotEmpty(MATCH_FIND_SPY_ONE_SPECK)) {
        MatchRoom matchInfo = MATCH_FIND_SPY_ONE_SPECK.stream()
            .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
            .findFirst().orElse(null);
        if (Objects.nonNull(matchInfo)) {
          // 谁是卧底.刷新人数.4-6人.文字场
          matchInfo.setPeopleNum(matchRoom.getPeopleNum());
          MATCH_FIND_SPY_ONE_SPECK.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        } else {
          // 谁是卧底.开放匹配.4-6人.文字场
          MATCH_FIND_SPY_ONE_SPECK.add(matchRoom);
          MATCH_FIND_SPY_ONE_SPECK.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
      } else {
        // 谁是卧底.开放匹配.4-6人.文字场
        MATCH_FIND_SPY_ONE_SPECK.add(matchRoom);
        MATCH_FIND_SPY_ONE_SPECK.sort(Comparator.comparing(MatchRoom::getPeopleNum));
      }
    }
  }

  /**
   * TODO 间谍匹配类型二.
   *
   * @param matchRoom [匹配数据]
   * @param speakMode [0{文字} 1{语音}]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/18 14:07
   * @update 2020/9/18 14:07
   */
  private static void findSpyRoomMatchTypeTwo(MatchRoom matchRoom, Integer speakMode) {
    if (speakMode == 0) {
      if (CollectionUtils.isNotEmpty(MATCH_FIND_SPY_TWO_WORDS)) {
        MatchRoom matchInfo = MATCH_FIND_SPY_TWO_WORDS.stream()
            .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
            .findFirst().orElse(null);
        if (Objects.nonNull(matchInfo)) {
          // 谁是卧底.刷新人数.4-6人.文字场
          matchInfo.setPeopleNum(matchRoom.getPeopleNum());
          MATCH_FIND_SPY_TWO_WORDS.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        } else {
          // 谁是卧底.开放匹配.4-6人.文字场
          MATCH_FIND_SPY_TWO_WORDS.add(matchRoom);
          MATCH_FIND_SPY_TWO_WORDS.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
      } else {
        // 谁是卧底.开放匹配.4-6人.文字场
        MATCH_FIND_SPY_TWO_WORDS.add(matchRoom);
        MATCH_FIND_SPY_TWO_WORDS.sort(Comparator.comparing(MatchRoom::getPeopleNum));
      }
    } else {
      if (CollectionUtils.isNotEmpty(MATCH_FIND_SPY_TWO_SPECK)) {
        MatchRoom matchInfo = MATCH_FIND_SPY_TWO_SPECK.stream()
            .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
            .findFirst().orElse(null);
        if (Objects.nonNull(matchInfo)) {
          // 谁是卧底.刷新人数.4-6人.文字场
          matchInfo.setPeopleNum(matchRoom.getPeopleNum());
          MATCH_FIND_SPY_TWO_SPECK.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        } else {
          // 谁是卧底.开放匹配.4-6人.文字场
          MATCH_FIND_SPY_TWO_SPECK.add(matchRoom);
          MATCH_FIND_SPY_TWO_SPECK.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
      } else {
        // 谁是卧底.开放匹配.4-6人.文字场
        MATCH_FIND_SPY_TWO_SPECK.add(matchRoom);
        MATCH_FIND_SPY_TWO_SPECK.sort(Comparator.comparing(MatchRoom::getPeopleNum));
      }
    }
  }

  /**
   * TODO 移除谁是卧底匹配.
   *
   * @param roomId [房间ID]
   * @param matchNumber [0{4-6人} 1{7-8人}」]
   * @param meetMode [0-文字 1-语音]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/18 14:08
   * @update 2020/9/18 14:08
   */
  public static void delFindSpyMatch(Long roomId, Integer matchNumber, Integer meetMode) {
    if (matchNumber == 0) {
      if (meetMode == 0) {
        MATCH_FIND_SPY_ONE_WORDS.removeIf(matchRoom -> Objects.equals(matchRoom.getRoomId(), roomId));
        if (CollectionUtils.isNotEmpty(MATCH_FIND_SPY_ONE_WORDS)) {
          MATCH_FIND_SPY_ONE_WORDS.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
      } else {
        MATCH_FIND_SPY_ONE_SPECK.removeIf(matchRoom -> Objects.equals(matchRoom.getRoomId(), roomId));
        if (CollectionUtils.isNotEmpty(MATCH_FIND_SPY_ONE_SPECK)) {
          MATCH_FIND_SPY_ONE_SPECK.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
      }
    } else {
      if (meetMode == 0) {
        MATCH_FIND_SPY_TWO_WORDS.removeIf(matchRoom -> Objects.equals(matchRoom.getRoomId(), roomId));
        if (CollectionUtils.isNotEmpty(MATCH_FIND_SPY_TWO_WORDS)) {
          MATCH_FIND_SPY_TWO_WORDS.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
      } else {
        MATCH_FIND_SPY_TWO_SPECK.removeIf(matchRoom -> Objects.equals(matchRoom.getRoomId(), roomId));
        if (CollectionUtils.isNotEmpty(MATCH_FIND_SPY_TWO_SPECK)) {
          MATCH_FIND_SPY_TWO_SPECK.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
      }
    }
  }

  /** 你说我猜. */
  public static CopyOnWriteArrayList<MatchRoom> MATCH_GUESSED_SAID = Lists.newCopyOnWriteArrayList();

  /**
   * TODO 刷新你说我猜匹配.
   *
   * @param matchRoom [匹配数据].
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/18 14:15
   * @update 2020/9/18 14:15
   */
  public static void refreshGuessedSaidMatch(MatchRoom matchRoom) {
    if (CollectionUtils.isNotEmpty(MATCH_GUESSED_SAID)) {
      MatchRoom matchInfo = MATCH_GUESSED_SAID.stream()
          .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
          .findFirst().orElse(null);
      if (Objects.nonNull(matchInfo)) {
        // 你说我猜.刷新人数
        matchInfo.setPeopleNum(matchRoom.getPeopleNum());
        MATCH_GUESSED_SAID.sort(Comparator.comparing(MatchRoom::getPeopleNum));
      } else {
        // 你说我猜.开放匹配
        MATCH_GUESSED_SAID.add(matchRoom);
        MATCH_GUESSED_SAID.sort(Comparator.comparing(MatchRoom::getPeopleNum));
      }
    } else {
      // 你说我猜.开放匹配
      MATCH_GUESSED_SAID.add(matchRoom);
      MATCH_GUESSED_SAID.sort(Comparator.comparing(MatchRoom::getPeopleNum));
    }
  }

  /**
   * TODO 移除你说我猜匹配.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/18 14:08
   * @update 2020/9/18 14:08
   */
  public static void delGuessedSaidMatch(Long roomId) {
    // 你说我猜.移除匹配
    MATCH_GUESSED_SAID.removeIf(matchRoom -> Objects.equals(matchRoom.getRoomId(), roomId));
    if (CollectionUtils.isNotEmpty(MATCH_GUESSED_SAID)) {
      MATCH_GUESSED_SAID.sort(Comparator.comparing(MatchRoom::getPeopleNum));
    }
  }

  /** 优诺. */
  public static CopyOnWriteArrayList<MatchRoom> MATCH_UNO_POKER = Lists.newCopyOnWriteArrayList();

  /**
   * TODO 刷新优诺扑克匹配.
   *
   * @param matchRoom [匹配数据].
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/18 14:15
   * @update 2020/9/18 14:15
   */
  public static void refreshUnoPokerMatch(MatchRoom matchRoom) {
    if (CollectionUtils.isNotEmpty(MATCH_UNO_POKER)) {
      MatchRoom matchInfo = MATCH_UNO_POKER.stream()
          .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
          .findFirst().orElse(null);
      if (Objects.nonNull(matchInfo)) {
        // 优诺扑克.刷新人数
        matchInfo.setPeopleNum(matchRoom.getPeopleNum());
        MATCH_UNO_POKER.sort(Comparator.comparing(MatchRoom::getPeopleNum));
      } else {
        // 优诺扑克.开放匹配
        MATCH_UNO_POKER.add(matchRoom);
        MATCH_UNO_POKER.sort(Comparator.comparing(MatchRoom::getPeopleNum));
      }
    } else {
      // 优诺扑克.开放匹配
      MATCH_UNO_POKER.add(matchRoom);
      MATCH_UNO_POKER.sort(Comparator.comparing(MatchRoom::getPeopleNum));
    }
  }

  /**
   * TODO 移除优诺扑克匹配.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/18 14:08
   * @update 2020/9/18 14:08
   */
  public static void delUnoPokerMatch(Long roomId) {
    // 优诺扑克.移除匹配
    MATCH_UNO_POKER.removeIf(matchRoom -> Objects.equals(matchRoom.getRoomId(), roomId));
    if (CollectionUtils.isNotEmpty(MATCH_UNO_POKER)) {
      MATCH_UNO_POKER.sort(Comparator.comparing(MatchRoom::getPeopleNum));
    }
  }

  /** 炸弹猫. */
  public static CopyOnWriteArrayList<MatchRoom> MATCH_EXPLODING_KITTENS = Lists.newCopyOnWriteArrayList();

  /**
   * TODO 刷新炸弹猫咪匹配.
   *
   * @param matchRoom [匹配数据].
   * @author wangcaiwen|1443710411@qq.com
   * @create 2020/9/18 14:15
   * @update 2020/9/18 14:15
   */
  public static void refreshExplodingKittensMatch(MatchRoom matchRoom) {
    if (CollectionUtils.isNotEmpty(MATCH_EXPLODING_KITTENS)) {
      MatchRoom matchInfo = MATCH_EXPLODING_KITTENS.stream()
          .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
          .findFirst().orElse(null);
      if (Objects.nonNull(matchInfo)) {
        // 炸弹猫咪.刷新人数
        matchInfo.setPeopleNum(matchRoom.getPeopleNum());
        MATCH_EXPLODING_KITTENS.sort(Comparator.comparing(MatchRoom::getPeopleNum));
      } else {
        // 炸弹猫咪.开放匹配
        MATCH_EXPLODING_KITTENS.add(matchRoom);
        MATCH_EXPLODING_KITTENS.sort(Comparator.comparing(MatchRoom::getPeopleNum));
      }
    } else {
      // 炸弹猫咪.开放匹配
      MATCH_EXPLODING_KITTENS.add(matchRoom);
      MATCH_EXPLODING_KITTENS.sort(Comparator.comparing(MatchRoom::getPeopleNum));
    }
  }

  /**
   * TODO 移除炸弹猫咪匹配.
   *
   * @param roomId 房间ID
   * @author wangcaiwen|1443710411@qq.com
   * @create 2020/9/18 14:08
   * @update 2020/9/18 14:08
   */
  public static void delExplodingKittensMatch(Long roomId) {
    // 炸弹猫咪.移除匹配
    MATCH_EXPLODING_KITTENS.removeIf(matchRoom -> Objects.equals(matchRoom.getRoomId(), roomId));
    if (CollectionUtils.isNotEmpty(MATCH_EXPLODING_KITTENS)) {
      MATCH_EXPLODING_KITTENS.sort(Comparator.comparing(MatchRoom::getPeopleNum));
    }
  }

  /** 七乐斗王 经典 [1-初级场 2-中级场 3-高级场 4-王者场]. */
  public static CopyOnWriteArrayList<MatchRoom> LANDLORDS_SESSION_CLASSIC_1 = Lists.newCopyOnWriteArrayList();
  public static CopyOnWriteArrayList<MatchRoom> LANDLORDS_SESSION_CLASSIC_2 = Lists.newCopyOnWriteArrayList();
  public static CopyOnWriteArrayList<MatchRoom> LANDLORDS_SESSION_CLASSIC_3 = Lists.newCopyOnWriteArrayList();
  public static CopyOnWriteArrayList<MatchRoom> LANDLORDS_SESSION_CLASSIC_4 = Lists.newCopyOnWriteArrayList();
  /** 七乐斗王 癞子 [1-初级场 2-中级场 3-高级场 4-王者场]. */
  public static CopyOnWriteArrayList<MatchRoom> LANDLORDS_SESSION_SHAMELESSLY_1 = Lists.newCopyOnWriteArrayList();
  public static CopyOnWriteArrayList<MatchRoom> LANDLORDS_SESSION_SHAMELESSLY_2 = Lists.newCopyOnWriteArrayList();
  public static CopyOnWriteArrayList<MatchRoom> LANDLORDS_SESSION_SHAMELESSLY_3 = Lists.newCopyOnWriteArrayList();
  public static CopyOnWriteArrayList<MatchRoom> LANDLORDS_SESSION_SHAMELESSLY_4 = Lists.newCopyOnWriteArrayList();

  /**
   * TODO 刷新地主.
   *
   * @param matchRoom [匹配数据]
   * @param gameMode [0-经典 1-癞子]
   * @param session [1-初级场 2-中级场 3-高级场 4-王者场]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/27 17:00
   * @update 2020/10/27 17:00
   */
  public static void refreshLandlords(MatchRoom matchRoom, Integer gameMode, Integer session) {
    if (gameMode == 0) {
      classicGamePlay(matchRoom, session);
    } else {
      shamelesslyGamePlay(matchRoom, session);
    }
  }

  /**
   * TODO 经典玩法.
   *
   * @param matchRoom [匹配数据]
   * @param session [1-初级场 2-中级场 3-高级场 4-王者场]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/27 17:20
   * @update 2020/10/27 17:20
   */
  private static void classicGamePlay(MatchRoom matchRoom, Integer session) {
    switch (session) {
      case 1:
        if (CollectionUtils.isNotEmpty(LANDLORDS_SESSION_CLASSIC_1)) {
          MatchRoom matchInfo = LANDLORDS_SESSION_CLASSIC_1.stream()
              .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
              .findFirst().orElse(null);
          if (Objects.nonNull(matchInfo)) {
            matchInfo.setPeopleNum(matchRoom.getPeopleNum());
            LANDLORDS_SESSION_CLASSIC_1.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          } else {
            LANDLORDS_SESSION_CLASSIC_1.add(matchRoom);
            LANDLORDS_SESSION_CLASSIC_1.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
        } else {
          LANDLORDS_SESSION_CLASSIC_1.add(matchRoom);
          LANDLORDS_SESSION_CLASSIC_1.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
        break;
      case 2:
        if (CollectionUtils.isNotEmpty(LANDLORDS_SESSION_CLASSIC_2)) {
          MatchRoom matchInfo = LANDLORDS_SESSION_CLASSIC_2.stream()
              .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
              .findFirst().orElse(null);
          if (Objects.nonNull(matchInfo)) {
            matchInfo.setPeopleNum(matchRoom.getPeopleNum());
            LANDLORDS_SESSION_CLASSIC_2.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          } else {
            LANDLORDS_SESSION_CLASSIC_2.add(matchRoom);
            LANDLORDS_SESSION_CLASSIC_2.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
        } else {
          LANDLORDS_SESSION_CLASSIC_2.add(matchRoom);
          LANDLORDS_SESSION_CLASSIC_2.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
        break;
      case 3:
        if (CollectionUtils.isNotEmpty(LANDLORDS_SESSION_CLASSIC_3)) {
          MatchRoom matchInfo = LANDLORDS_SESSION_CLASSIC_3.stream()
              .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
              .findFirst().orElse(null);
          if (Objects.nonNull(matchInfo)) {
            matchInfo.setPeopleNum(matchRoom.getPeopleNum());
            LANDLORDS_SESSION_CLASSIC_3.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          } else {
            LANDLORDS_SESSION_CLASSIC_3.add(matchRoom);
            LANDLORDS_SESSION_CLASSIC_3.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
        } else {
          LANDLORDS_SESSION_CLASSIC_3.add(matchRoom);
          LANDLORDS_SESSION_CLASSIC_3.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
        break;
      default:
        if (CollectionUtils.isNotEmpty(LANDLORDS_SESSION_CLASSIC_4)) {
          MatchRoom matchInfo = LANDLORDS_SESSION_CLASSIC_4.stream()
              .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
              .findFirst().orElse(null);
          if (Objects.nonNull(matchInfo)) {
            matchInfo.setPeopleNum(matchRoom.getPeopleNum());
            LANDLORDS_SESSION_CLASSIC_4.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          } else {
            LANDLORDS_SESSION_CLASSIC_4.add(matchRoom);
            LANDLORDS_SESSION_CLASSIC_4.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
        } else {
          LANDLORDS_SESSION_CLASSIC_4.add(matchRoom);
          LANDLORDS_SESSION_CLASSIC_4.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
        break;
    }
  }

  /**
   * TODO 癞子玩法.
   *
   * @param matchRoom [匹配数据]
   * @param session [1-初级场 2-中级场 3-高级场 4-王者场]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/27 17:21
   * @update 2020/10/27 17:21
   */
  private static void shamelesslyGamePlay(MatchRoom matchRoom, Integer session) {
    switch (session) {
      case 1:
        if (CollectionUtils.isNotEmpty(LANDLORDS_SESSION_SHAMELESSLY_1)) {
          MatchRoom matchInfo = LANDLORDS_SESSION_SHAMELESSLY_1.stream()
              .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
              .findFirst().orElse(null);
          if (Objects.nonNull(matchInfo)) {
            matchInfo.setPeopleNum(matchRoom.getPeopleNum());
            LANDLORDS_SESSION_SHAMELESSLY_1.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          } else {
            LANDLORDS_SESSION_SHAMELESSLY_1.add(matchRoom);
            LANDLORDS_SESSION_SHAMELESSLY_1.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
        } else {
          LANDLORDS_SESSION_SHAMELESSLY_1.add(matchRoom);
          LANDLORDS_SESSION_SHAMELESSLY_1.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
        break;
      case 2:
        if (CollectionUtils.isNotEmpty(LANDLORDS_SESSION_SHAMELESSLY_2)) {
          MatchRoom matchInfo = LANDLORDS_SESSION_SHAMELESSLY_2.stream()
              .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
              .findFirst().orElse(null);
          if (Objects.nonNull(matchInfo)) {
            matchInfo.setPeopleNum(matchRoom.getPeopleNum());
            LANDLORDS_SESSION_SHAMELESSLY_2.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          } else {
            LANDLORDS_SESSION_SHAMELESSLY_2.add(matchRoom);
            LANDLORDS_SESSION_SHAMELESSLY_2.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
        } else {
          LANDLORDS_SESSION_SHAMELESSLY_2.add(matchRoom);
          LANDLORDS_SESSION_SHAMELESSLY_2.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
        break;
      case 3:
        if (CollectionUtils.isNotEmpty(LANDLORDS_SESSION_SHAMELESSLY_3)) {
          MatchRoom matchInfo = LANDLORDS_SESSION_SHAMELESSLY_3.stream()
              .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
              .findFirst().orElse(null);
          if (Objects.nonNull(matchInfo)) {
            matchInfo.setPeopleNum(matchRoom.getPeopleNum());
            LANDLORDS_SESSION_SHAMELESSLY_3.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          } else {
            LANDLORDS_SESSION_SHAMELESSLY_3.add(matchRoom);
            LANDLORDS_SESSION_SHAMELESSLY_3.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
        } else {
          LANDLORDS_SESSION_SHAMELESSLY_3.add(matchRoom);
          LANDLORDS_SESSION_SHAMELESSLY_3.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
        break;
      default:
        if (CollectionUtils.isNotEmpty(LANDLORDS_SESSION_SHAMELESSLY_4)) {
          MatchRoom matchInfo = LANDLORDS_SESSION_SHAMELESSLY_4.stream()
              .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
              .findFirst().orElse(null);
          if (Objects.nonNull(matchInfo)) {
            matchInfo.setPeopleNum(matchRoom.getPeopleNum());
            LANDLORDS_SESSION_SHAMELESSLY_4.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          } else {
            LANDLORDS_SESSION_SHAMELESSLY_4.add(matchRoom);
            LANDLORDS_SESSION_SHAMELESSLY_4.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
        } else {
          LANDLORDS_SESSION_SHAMELESSLY_4.add(matchRoom);
          LANDLORDS_SESSION_SHAMELESSLY_4.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
        break;
    }
  }

  /**
   * TODO 移除地主.
   *
   * @param roomId 房间ID
   * @param gameMode [0-经典 1-癞子]
   * @param session [1-初级场 2-中级场 3-高级场 4-王者场]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/27 17:39
   * @update 2020/10/27 17:39
   */
  public static void delLandlordsMatch(Long roomId, Integer gameMode, Integer session) {
    if (gameMode == 0) {
      switch (session) {
        case 1:
          LANDLORDS_SESSION_CLASSIC_1.removeIf(s -> Objects.equals(s.getRoomId(), roomId));
          if (CollectionUtils.isNotEmpty(LANDLORDS_SESSION_CLASSIC_1)) {
            LANDLORDS_SESSION_CLASSIC_1.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
          break;
        case 2:
          LANDLORDS_SESSION_CLASSIC_2.removeIf(s -> Objects.equals(s.getRoomId(), roomId));
          if (CollectionUtils.isNotEmpty(LANDLORDS_SESSION_CLASSIC_2)) {
            LANDLORDS_SESSION_CLASSIC_2.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
          break;
        case 3:
          LANDLORDS_SESSION_CLASSIC_3.removeIf(s -> Objects.equals(s.getRoomId(), roomId));
          if (CollectionUtils.isNotEmpty(LANDLORDS_SESSION_CLASSIC_3)) {
            LANDLORDS_SESSION_CLASSIC_3.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
          break;
        default:
          LANDLORDS_SESSION_CLASSIC_4.removeIf(s -> Objects.equals(s.getRoomId(), roomId));
          if (CollectionUtils.isNotEmpty(LANDLORDS_SESSION_CLASSIC_4)) {
            LANDLORDS_SESSION_CLASSIC_4.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
          break;
      }
    } else {
      switch (session) {
        case 1:
          LANDLORDS_SESSION_SHAMELESSLY_1.removeIf(s -> Objects.equals(s.getRoomId(), roomId));
          if (CollectionUtils.isNotEmpty(LANDLORDS_SESSION_SHAMELESSLY_1)) {
            LANDLORDS_SESSION_SHAMELESSLY_1.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
          break;
        case 2:
          LANDLORDS_SESSION_SHAMELESSLY_2.removeIf(s -> Objects.equals(s.getRoomId(), roomId));
          if (CollectionUtils.isNotEmpty(LANDLORDS_SESSION_SHAMELESSLY_2)) {
            LANDLORDS_SESSION_SHAMELESSLY_2.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
          break;
        case 3:
          LANDLORDS_SESSION_SHAMELESSLY_3.removeIf(s -> Objects.equals(s.getRoomId(), roomId));
          if (CollectionUtils.isNotEmpty(LANDLORDS_SESSION_SHAMELESSLY_3)) {
            LANDLORDS_SESSION_SHAMELESSLY_3.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
          break;
        default:
          LANDLORDS_SESSION_SHAMELESSLY_4.removeIf(s -> Objects.equals(s.getRoomId(), roomId));
          if (CollectionUtils.isNotEmpty(LANDLORDS_SESSION_SHAMELESSLY_4)) {
            LANDLORDS_SESSION_SHAMELESSLY_4.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
          break;
      }
    }
  }

  /** 狼人杀 普通 [0-{6人} 1-{9人} 2-{12人}]. */
  public static CopyOnWriteArrayList<MatchRoom> WEREWOLF_SESSION_NORMAL_ZERO = Lists.newCopyOnWriteArrayList();
  public static CopyOnWriteArrayList<MatchRoom> WEREWOLF_SESSION_NORMAL_ONE = Lists.newCopyOnWriteArrayList();
  public static CopyOnWriteArrayList<MatchRoom> WEREWOLF_SESSION_NORMAL_TWO = Lists.newCopyOnWriteArrayList();
  /** 狼人杀 进阶 [1-{9人} 2-{12人}]. */
  public static CopyOnWriteArrayList<MatchRoom> WEREWOLF_SESSION_ADVANCED_ONE = Lists.newCopyOnWriteArrayList();
  public static CopyOnWriteArrayList<MatchRoom> WEREWOLF_SESSION_ADVANCED_TWO = Lists.newCopyOnWriteArrayList();

  /**
   * TODO 刷新狼人.
   *
   * @param matchRoom [匹配数据]
   * @param session [1-普通场 2-进阶场]
   * @param number [0-{6人} 1-{9人} 2-{12人}]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 16:12
   * @update 2020/10/28 16:12
   */
  public static void refreshWerewolf(MatchRoom matchRoom, Integer session, Integer number) {
    if (session == 1) {
      normalGamePlay(matchRoom, number);
    } else {
      advancedGamePlay(matchRoom, number);
    }
  }

  /**
   * TODO 狼人. 普通玩法
   *
   * @param matchRoom [匹配数据]
   * @param number [0-{6人} 1-{9人} 2-{12人}]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 16:17
   * @update 2020/10/28 16:17
   */
  private static void normalGamePlay(MatchRoom matchRoom, Integer number) {
    switch (number) {
      case 0:
        if (CollectionUtils.isNotEmpty(WEREWOLF_SESSION_NORMAL_ZERO)) {
          MatchRoom matchInfo = WEREWOLF_SESSION_NORMAL_ZERO.stream()
              .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
              .findFirst().orElse(null);
          if (Objects.nonNull(matchInfo)) {
            matchInfo.setPeopleNum(matchRoom.getPeopleNum());
            WEREWOLF_SESSION_NORMAL_ZERO.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          } else {
            WEREWOLF_SESSION_NORMAL_ZERO.add(matchRoom);
            WEREWOLF_SESSION_NORMAL_ZERO.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
        } else {
          WEREWOLF_SESSION_NORMAL_ZERO.add(matchRoom);
          WEREWOLF_SESSION_NORMAL_ZERO.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
        break;
      case 1:
        if (CollectionUtils.isNotEmpty(WEREWOLF_SESSION_NORMAL_ONE)) {
          MatchRoom matchInfo = WEREWOLF_SESSION_NORMAL_ONE.stream()
              .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
              .findFirst().orElse(null);
          if (Objects.nonNull(matchInfo)) {
            matchInfo.setPeopleNum(matchRoom.getPeopleNum());
            WEREWOLF_SESSION_NORMAL_ONE.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          } else {
            WEREWOLF_SESSION_NORMAL_ONE.add(matchRoom);
            WEREWOLF_SESSION_NORMAL_ONE.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
        } else {
          WEREWOLF_SESSION_NORMAL_ONE.add(matchRoom);
          WEREWOLF_SESSION_NORMAL_ONE.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
        break;
      default:
        if (CollectionUtils.isNotEmpty(WEREWOLF_SESSION_NORMAL_TWO)) {
          MatchRoom matchInfo = WEREWOLF_SESSION_NORMAL_TWO.stream()
              .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
              .findFirst().orElse(null);
          if (Objects.nonNull(matchInfo)) {
            matchInfo.setPeopleNum(matchRoom.getPeopleNum());
            WEREWOLF_SESSION_NORMAL_TWO.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          } else {
            WEREWOLF_SESSION_NORMAL_TWO.add(matchRoom);
            WEREWOLF_SESSION_NORMAL_TWO.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
        } else {
          WEREWOLF_SESSION_NORMAL_TWO.add(matchRoom);
          WEREWOLF_SESSION_NORMAL_TWO.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
        break;
    }
  }

  /**
   * TODO 狼人. 进阶玩法
   *
   * @param matchRoom [匹配数据]
   * @param number [0-{6人} 1-{9人} 2-{12人}]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 16:18
   * @update 2020/10/28 16:18
   */
  private static void advancedGamePlay(MatchRoom matchRoom, Integer number) {
    switch (number) {
      case 1:
        if (CollectionUtils.isNotEmpty(WEREWOLF_SESSION_ADVANCED_ONE)) {
          MatchRoom matchInfo = WEREWOLF_SESSION_ADVANCED_ONE.stream()
              .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
              .findFirst().orElse(null);
          if (Objects.nonNull(matchInfo)) {
            matchInfo.setPeopleNum(matchRoom.getPeopleNum());
            WEREWOLF_SESSION_ADVANCED_ONE.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          } else {
            WEREWOLF_SESSION_ADVANCED_ONE.add(matchRoom);
            WEREWOLF_SESSION_ADVANCED_ONE.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
        } else {
          WEREWOLF_SESSION_ADVANCED_ONE.add(matchRoom);
          WEREWOLF_SESSION_ADVANCED_ONE.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
        break;
      case 2:
        if (CollectionUtils.isNotEmpty(WEREWOLF_SESSION_ADVANCED_TWO)) {
          MatchRoom matchInfo = WEREWOLF_SESSION_ADVANCED_TWO.stream()
              .filter(s -> Objects.equals(s.getRoomId(), matchRoom.getRoomId()))
              .findFirst().orElse(null);
          if (Objects.nonNull(matchInfo)) {
            matchInfo.setPeopleNum(matchRoom.getPeopleNum());
            WEREWOLF_SESSION_ADVANCED_TWO.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          } else {
            WEREWOLF_SESSION_ADVANCED_TWO.add(matchRoom);
            WEREWOLF_SESSION_ADVANCED_TWO.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
        } else {
          WEREWOLF_SESSION_ADVANCED_TWO.add(matchRoom);
          WEREWOLF_SESSION_ADVANCED_TWO.sort(Comparator.comparing(MatchRoom::getPeopleNum));
        }
        break;
      default:
        break;
    }
  }

  /**
   * TODO 移除狼人.
   *
   * @param roomId [房间ID]
   * @param session [1-普通场 2-进阶场]
   * @param number [0-{6人} 1-{9人} 2-{12人}]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/28 16:32
   * @update 2020/10/28 16:32
   */
  public static void delWerewolfMatch(Long roomId, Integer session, Integer number) {
    if (session == 1) {
      switch (number) {
        case 0:
          WEREWOLF_SESSION_NORMAL_ZERO.removeIf(s -> Objects.equals(s.getRoomId(), roomId));
          if (CollectionUtils.isNotEmpty(WEREWOLF_SESSION_NORMAL_ZERO)) {
            WEREWOLF_SESSION_NORMAL_ZERO.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
          break;
        case 1:
          WEREWOLF_SESSION_NORMAL_ONE.removeIf(s -> Objects.equals(s.getRoomId(), roomId));
          if (CollectionUtils.isNotEmpty(WEREWOLF_SESSION_NORMAL_ONE)) {
            WEREWOLF_SESSION_NORMAL_ONE.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
          break;
        default:
          WEREWOLF_SESSION_NORMAL_TWO.removeIf(s -> Objects.equals(s.getRoomId(), roomId));
          if (CollectionUtils.isNotEmpty(WEREWOLF_SESSION_NORMAL_TWO)) {
            WEREWOLF_SESSION_NORMAL_TWO.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
          break;
      }
    } else {
      switch (number) {
        case 1:
          WEREWOLF_SESSION_ADVANCED_ONE.removeIf(s -> Objects.equals(s.getRoomId(), roomId));
          if (CollectionUtils.isNotEmpty(WEREWOLF_SESSION_ADVANCED_ONE)) {
            WEREWOLF_SESSION_ADVANCED_ONE.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
          break;
        case 2:
          WEREWOLF_SESSION_ADVANCED_TWO.removeIf(s -> Objects.equals(s.getRoomId(), roomId));
          if (CollectionUtils.isNotEmpty(WEREWOLF_SESSION_ADVANCED_TWO)) {
            WEREWOLF_SESSION_ADVANCED_TWO.sort(Comparator.comparing(MatchRoom::getPeopleNum));
          }
          break;
        default:
          break;
      }
    }
  }

}
