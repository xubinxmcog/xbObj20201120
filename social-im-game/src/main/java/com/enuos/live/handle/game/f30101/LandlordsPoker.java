package com.enuos.live.handle.game.f30101;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 斗地主扑克.
 *
 * @author WangCaiWen Created on 2020/5/15 15:08
 */
@Data
@NoArgsConstructor
public class LandlordsPoker implements Serializable {

  private static final long serialVersionUID = -6092168738728532367L;

  /**
   * 牌值
   */
  private static String[] numbers = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13",
      "14"};
  /**
   * 花色
   */
  private static String[] colors = {"♠", "♥", "♣", "♦"};
  /**
   * 花色 类型1♠ 2♥ 3♣ 4♦
   */
  private static String[] colorsType = {"1.", "2.", "3.", "4."};

  public static HashMap<Integer, String> poker = new HashMap<>(100);
  public static ArrayList<Integer> pokerNumber = new ArrayList<>();

  public static LandlordsPoker getInstance() {
    return Landlords.INSTANCE;
  }

  private static class Landlords {

    static final LandlordsPoker INSTANCE = new LandlordsPoker();
  }

  public void newPoker() {
    init();
    //创建牌（52张）
    String[] pokers = new String[52];
    int index = 0;
    for (String number : numbers) {
      for (String color : colorsType) {
        pokers[index++] = color + number;
      }
    }
    //创建Map集合，键：编号  值：牌
    int indexs = 0;
    //遍历数组，用花色+点数的组合,存储到Map集合中
    for (String obj : pokers) {
      poker.put(indexs, obj);
      // 牌ID
      pokerNumber.add(indexs);
      indexs++;
    }
  }

  private void init() {
    poker = new HashMap<>(100);
    pokerNumber = new ArrayList<>();
  }
}
