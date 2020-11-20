package com.enuos.live.utils;

import com.enuos.live.utils.StringUtils;

import java.util.UUID;

/**
 * @author WangCaiWen Created on 2019/10/31 9:36
 */
public class IDUtils {

  /**
   * 随机ID（9位）
   *
   * @return userId
   */
  public static int randomNineId() {
    String randomId = UUID.randomUUID()
        .toString()
        .replace("-", "");
    randomId = randomId.replaceAll("[a-zA-Z]", "");
    String userId = randomId.substring(0, 9);
    String oneData = randomId.substring(0, 1);
    String start = StringUtils.nvl((int) (Math.random() * 9 + 1));
    String index = "0";
    if (oneData.equals(index)) {
      String end = userId.substring(1);
      userId = start + end;
    }
    return Integer.parseInt(userId);
  }

  /**
   * 随机ID（6位）
   *
   * @return userId
   */
  public static int randomSixId() {
    String randomId = UUID.randomUUID()
        .toString()
        .replace("-", "");
    randomId = randomId.replaceAll("[a-zA-Z]", "");
    String userId = randomId.substring(0, 6);
    String oneData = randomId.substring(0, 1);
    String start = StringUtils.nvl((int) (Math.random() * 9 + 1));
    String index = "0";
    if (oneData.equals(index)) {
      String end = userId.substring(1);
      userId = start + end;
    }
    return Integer.parseInt(userId);
  }

  /**
   * 随机ID（8位）
   *
   * @return userId
   */
  public static Long randomEightId() {
    String randomId = UUID.randomUUID().toString().replace("-", "");
    randomId = randomId.replaceAll("[a-zA-Z]", "");
    String userId = randomId.substring(0, 8);
    String oneData = randomId.substring(0, 1);
    String start = StringUtils.nvl((int) (Math.random() * 9 + 1));
    String index = "0";
    if (oneData.equals(index)) {
      String end = userId.substring(1);
      userId = start + end;
    }
    return Long.valueOf(userId);
  }

}
