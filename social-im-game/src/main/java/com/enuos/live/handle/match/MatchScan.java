package com.enuos.live.handle.match;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO 匹配字典.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/20 19:50
 */

public class MatchScan {

  private static Map map = new HashMap<Long, Integer>() {
    private static final long serialVersionUID = -4741498876821028806L;
    {
      // 五子棋
      put(30001L, 100001);
      // 斗兽棋
      put(30011L, 100011);
      // 飞行棋
      put(30021L, 100021);
      // 扫雷
      put(30031L, 100031);
      // 你画我猜
      put(30041L, 100041);
      // 谁是卧底
      put(30051L, 100051);
      // 你说我猜
      put(30061L, 100061);
      // 一站到底
      put(30071L, 100071);
      // 狼人杀
      put(30081L, 100081);
      // 连连看
      put(30091L, 100091);
      // 斗地主
      put(30101L, 100101);
      // 七乐麻将
      put(30111L, 100111);
      // 七乐捕鱼
      put(30121L, 100121);
      // 奔跑吧！小熊
      put(30131L, 100131);
      // 黄金矿工
      put(30141L, 100141);
      // 草原争霸
      put(30151L, 100151);
      // 魔法与龙
      put(30161L, 100161);
      // 小熊快跑
      put(30171L, 100171);
      // 熊熊大作战
      put(30181L, 100181);
      // 最强小熊
      put(30191L, 100191);
      // 消糖果
      put(30201L, 100201);
      // 台球
      put(30211L, 100211);
      // 飞刀达人
      put(30221L, 100221);
      // 泡泡龙
      put(30231L, 100231);
      // 蛇梯棋
      put(30241L, 100241);
      // 一起优诺
      put(30251L, 100251);
      // 跳一跳
      put(30261L, 100261);
      // 一路向前
      put(30271L, 100271);
      // 火力全开
      put(30281L, 100281);
      // 炸弹猫
      put(30291L, 100291);
    }
  };

  /**
   * TODO 获得匹配编码.
   *
   * @param gameId [游戏ID]
   * @return [匹配编码]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/20 21:07
   * @update 2020/8/26 12:48
   */
  public static int getMatchCode(Long gameId) {
    return (int) map.get(gameId);
  }

}
