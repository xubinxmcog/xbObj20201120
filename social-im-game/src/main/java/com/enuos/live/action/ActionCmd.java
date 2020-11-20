package com.enuos.live.action;

/**
 * TODO 操作命令.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.0.0
 * @since 2020/5/15 15:00
 */

public class ActionCmd {

  /** 游戏心跳. */
  public static final int GAME_HEART = 10000;
  /** 聊天信息. */
  public static final int GAME_CHAT = 10001;

  //---------------------------------------------游戏------------------------------------------------

  /** 五子棋. */
  public static final int GAME_GO_BANG = 30001;
  /** 斗兽棋. */
  public static final int GAME_ANIMAL = 30011;
  /** 飞行棋. */
  public static final int GAME_AEROPLANE = 30021;
  /** 扫雷. */
  public static final int GAME_MINESWEEPER = 30031;
  /** 你画我猜. */
  public static final int GAME_DRAW_SOMETHING = 30041;
  /** 谁是卧底. */
  public static final int GAME_WHO_IS_SPY = 30051;
  public static final int GAME_FIND_UNDERCOVER = 30051;
  /** 你说我猜. */
  public static final int GAME_GUESS_SAID = 30061;
  /** 一站到底. */
  public static final int GAME_MUST_STAND = 30071;
  /** 狼人杀. */
  public static final int GAME_WEREWOLF = 30081;
  /** 连连看. */
  public static final int GAME_LINK_LINK = 30091;
  /** 斗地主. */
  public static final int GAME_LANDLORDS = 30101;
  /** 麻将. */
  public static final int GAME_SPARROW = 30111;
  /** 捕鱼. */
  public static final int GAME_FISHING = 30121;
  /** 奔跑吧！小熊. */
  public static final int GAME_RUN_BEAR = 30131;
  /** 黄金矿工. */
  public static final int GAME_GOLD_MINER = 30141;
  /** 草原争霸. */
  public static final int GAME_GRASSLAND_CONTEST = 30151;
  /** 魔法与龙. */
  public static final int GAME_MAGIC_DRAGON = 30161;
  /** 小熊快跑. */
  public static final int GAME_BEAR_RUNNING = 30171;
  /** 熊熊大作战. */
  public static final int GAME_BEAR_BATTLE = 30181;
  /** 最强小熊. */
  public static final int GAME_STRONGEST_BEAR = 30191;
  /** 消糖果. */
  public static final int GAME_DESTROY_CANDY = 30201;
  /** 台球. */
  public static final int GAME_BILLIARDS = 30211;
  /** 飞刀达人. */
  public static final int GAME_KNIFE_MASTER = 30221;
  /** 泡泡龙. */
  public static final int GAME_BUBBLE_DRAGON = 30231;
  /** 蛇梯棋 */
  public static final int GAME_SNAKES_CHESS = 30241;
  /** 优诺. */
  public static final int GAME_UNO = 30251;
  /** 跳一跳. */
  public static final int GAME_JUMP_JUMP = 30261;
  /** 一路向前. */
  public static final int GAME_ALWAYS_FORWARD = 30271;
  /** 火力全开. */
  public static final int GAME_FULL_POWER = 30281;
  /** 炸弹猫. */
  public static final int GAME_EXPLODING_KITTENS = 30291;

  //-------------------------------------------软件--------------------------------------------------

  /** App心跳. */
  public static final int APP_HEART = 20000;
  /** 游戏匹配. */
  public static final int GAME_MATCH = 20001;
  /** 创建房间. */
  public static final int CREATE_ROOM = 20011;
}
