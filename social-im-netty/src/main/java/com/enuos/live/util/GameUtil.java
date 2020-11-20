package com.enuos.live.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * TODO 游戏工具.
 *
 * @author wangcaiwen|1443710411@qq.com
 * @version V1.0.0
 * @since 2020/8/13 10:43
 */

public class GameUtil {

  /**
   * TODO 生成房间编号.
   *
   * @return long 房间编号
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/13 10:50
   * @update 2020/8/13 10:50
   */
  public static long getRandomRoomNumber() {
    String randomOne = String.valueOf(ThreadLocalRandom.current().nextInt(9990) + 10);
    String randomTwo = String.valueOf(ThreadLocalRandom.current().nextInt(9990) + 10);
    String timestamp = String.valueOf(System.currentTimeMillis()/1000);
    String roomNumber = timestamp + randomOne + randomTwo;
    if (roomNumber.length() < 18) {
      int diff = 18 - roomNumber.length();
      switch (diff){
        case 1:
          roomNumber = roomNumber + "0";
          break;
        case 2:
          roomNumber = roomNumber + "00";
          break;
        case 3:
          roomNumber = roomNumber + "000";
          break;
        default:
          roomNumber = roomNumber + "0000";
          break;
      }
    }
    return Long.parseLong(roomNumber);
  }

  public static int getRandomRoomNo() {
    String randomOne = String.valueOf(ThreadLocalRandom.current().nextInt(9990) + 10);
    String randomThree = String.valueOf(ThreadLocalRandom.current().nextInt(8) + 1);
    String randomTwo = String.valueOf(ThreadLocalRandom.current().nextInt(9990) + 10);
    String roomNumber = randomOne + randomThree + randomTwo;
    if (roomNumber.length() < 9) {
      int diff = 9 - roomNumber.length();
      switch (diff){
        case 1:
          roomNumber = roomNumber + "0";
          break;
        case 2:
          roomNumber = roomNumber + "00";
          break;
        case 3:
          roomNumber = roomNumber + "000";
          break;
        default:
          roomNumber = roomNumber + "0000";
          break;
      }
    }
    return Integer.parseInt(roomNumber);
  }

}
