package com.enuos.live.handle.game.f30001;

import com.enuos.live.pojo.GameRobot;
import com.enuos.live.proto.f30001msg.F30001;
import com.enuos.live.proto.i10001msg.I10001;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.util.Timeout;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;

/**
 * TODO 房间数据.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/5/16 13:39
 */

@Data
@SuppressWarnings("WeakerAccess")
public class GoBangRoom {
  /** 房间ID. */
  private Long roomId;
  /** 悔棋玩家. */
  private Long regretUser;
  /** 下一玩家. */
  private Long nextActionId;
  /** 房间状态 [0-未开始 1-已开始 2-已结束]. */
  private Integer roomStatus = 0;
  /** 开启机器人 [0-关闭 1-开启]. */
  private Integer startRobot = 0;
  /** 悔棋标记. */
  private Integer indexRegret = 0;
  /** 成就标记. */
  private Integer successIndex = 0;
  /** 开始时间. */
  private LocalDateTime startTime;
  /** 操作时间. */
  private LocalDateTime actionTime;
  /** 玩家列表. */
  private List<GoBangPlayer> playerList = Lists.newCopyOnWriteArrayList();
  /** 4子连接. */
  private List<GoBangCoords> is4LinkRecord = Lists.newCopyOnWriteArrayList();
  /** 棋盘数据. */
  private Map<String, GoBangCoords> checkPoints = Maps.newHashMap();
  /** 定时数据. */
  private HashMap<Integer, Timeout> timeOutMap = Maps.newHashMap();
  /** 棋盘数据. */
  private GoBangCoords[][] board = new GoBangCoords[CHECKERBOARD][CHECKERBOARD];

  /** 棋盘大小.  */
  private static final int CHECKERBOARD = 15;
  /** 悔棋数. */
  private static final int INDEX_CHESS = 2;
  /** 验证大小.  */
  private static final int INDEX_THREE = 3;
  private static final int INDEX_FOUR = 4;

  // 初始化棋盘.
  {
    for (int i = 0; i < CHECKERBOARD; i++) {
      for (int j = 0; j < CHECKERBOARD; j++) {
        GoBangCoords pointInfo = new GoBangCoords(i, j);
        board[i][j] = pointInfo;
        checkPoints.put(pointId(i, j), pointInfo);
      }
    }
  }

  /**
   * TODO 初始数据.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  GoBangRoom(Long roomId) {
    this.roomId = roomId;
  }

  /**
   * TODO 组合坐标.
   *
   * @param x [x 索引 0开始]
   * @param y [y 索引 0开始]
   * @return [坐标Key]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  private String pointId(int x, int y) {
    return String.format("x%s:y%s", x, y);
  }

  /**
   * TODO 开始时间.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  public void setUpGameStartTime() {
    this.startTime = LocalDateTime.now();
    this.actionTime = this.startTime;
  }

  /**
   * TODO 加入房间.
   *
   * @param channel [通信管道]
   * @param playerInfo [玩家信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  public void joinRoom(Channel channel, I10001.PlayerInfo playerInfo) {
    if (Objects.nonNull(playerInfo)) {
      GoBangPlayer player = new GoBangPlayer();
      player.setUserId(playerInfo.getUserId());
      player.setUserName(playerInfo.getNickName());
      player.setUserIcon(playerInfo.getIconUrl());
      player.setUserSex(playerInfo.getSex());
      player.setChannel(channel);
      if (startRobot == 0) {
        if (playerList.size() == 0) {
          player.setIdentity(1);
          this.playerList.add(player);
        } else if (playerList.size() == 1) {
          player.setIdentity(2);
          this.playerList.add(player);
        }
        if (playerList.size() == 1) {
          this.nextActionId = playerList.get(0).getUserId();
        }
      } else {
        GoBangPlayer robot = playerList.get(0);
        // 用户身份 [1-黑 2-白]
        if (robot.getIdentity() == 1) {
          player.setIdentity(2);
          this.playerList.add(player);
        } else {
          player.setIdentity(1);
          this.playerList.add(player);
        }
        this.playerList.sort(Comparator.comparing(GoBangPlayer::getIdentity));
        this.nextActionId = playerList.get(0).getUserId();
      }
    }
  }

  /**
   * TODO 开启陪玩.
   *
   * @param gameRobot [机器人信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  public void joinRobot(GameRobot gameRobot) {
    if (Objects.nonNull(gameRobot)) {
      boolean flag = RandomUtils.nextBoolean();
      GoBangPlayer player = new GoBangPlayer();
      player.setUserId(gameRobot.getRobotId());
      player.setUserName(gameRobot.getRobotName());
      player.setUserIcon(gameRobot.getRobotAvatar());
      player.setUserSex(gameRobot.getRobotSex());
      player.setChannel(null);
      player.setIdentity(flag ? 1 : 2);
      player.setSuccessIndex(0);
      player.setIsRobot(0);
      this.playerList.add(player);
      if (playerList.size() == 1) {
        this.nextActionId = gameRobot.getRobotId();
      }
    }
  }

  /**
   * TODO 玩家信息.
   *
   * @param userId [玩家ID]
   * @return [玩家信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  public GoBangPlayer getPlayerInfo(Long userId) {
    return playerList.stream()
        .filter(partaker -> partaker.isBoolean(userId))
        .findFirst().orElse(null);
  }

  /**
   * TODO 玩家操作.
   *
   * @param position [坐标信息]
   * @param userId [玩家ID]
   * @return [胜利结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  public boolean actionPlacement(F30001.DPosition position, Long userId) {
    GoBangPlayer player = getPlayerInfo(userId);
    int x = position.getPositionX();
    int y = position.getPositionY();
    GoBangCoords coords = getCheckedPoint(x, y);
    coords.setColor(player.getIdentity());
    board[x][y] = coords;
    checkPoints.put(pointId(x, y), coords);
    player.playerAction(coords);
    this.nextActionId = getNextGamePlayer(userId);
    return isWinner(player.getUserId(), player.getIdentity(), x, y);
  }

  /**
   * TODO 初始位置.
   *
   * @param targetCoords [目标位置]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  public void initCoords(GoBangCoords targetCoords) {
    int x = targetCoords.getPositionX();
    int y = targetCoords.getPositionY();
    GoBangCoords coords = getCheckedPoint(x, y);
    coords.setColor(0);
    board[x][y] = coords;
    checkPoints.put(pointId(x, y), coords);
  }

  /**
   * TODO 下一玩家.
   *
   * @param userId [玩家ID]
   * @return [下一玩家]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  public long getNextGamePlayer(Long userId) {
    int index = 0;
    for (GoBangPlayer player : playerList) {
      if (player.isBoolean(userId)) {
        break;
      }
      index++;
    }
    if (index == 0) {
      return playerList.get(1).getUserId();
    }
    return playerList.get(0).getUserId();
  }

  /**
   * TODO 输赢判断.
   *
   * @param color [1-黑 2-白]
   * @param x [x 索引 0开始]
   * @param y [y 索引 0开始]
   * @return [输赢结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  public boolean isWinner(Long userId, int color, int x, int y) {
    GoBangPlayer nowPlayer = getPlayerInfo(userId);
    if (Objects.nonNull(nowPlayer)) {
      //横
      if (is5PointLine(color, d -> x - d, d -> y, d -> x + d, d -> y)) {
        if (successIndex == 1) {
          nowPlayer.is4LinkRecord();
        }
        this.successIndex = 0;
        return true;
      }
      //竖
      if (is5PointLine(color, d -> x, d -> y - d, d -> x, d -> y + d)) {
        if (successIndex == 1) {
          nowPlayer.is4LinkRecord();
        }
        this.successIndex = 0;
        return true;
      }
      //左斜
      if (is5PointLine(color, d -> x - d, d -> y - d, d -> x + d, d -> y + d)) {
        if (successIndex == 1) {
          nowPlayer.is4LinkRecord();
        }
        this.successIndex = 0;
        return true;
      }
      //右斜
      if (is5PointLine(color, d -> x - d, d -> y + d, d -> x + d, d -> y - d)) {
        if (successIndex == 1) {
          nowPlayer.is4LinkRecord();
        }
        this.successIndex = 0;
        return true;
      }
      if (successIndex == 1) {
        nowPlayer.is4LinkRecord();
      }
      this.successIndex = 0;
    }
    return false;
  }

  /**
   * TODO 是否五连.
   *
   * @param color [身份颜色]
   * @param leftX [左边x]
   * @param leftY [左边y]
   * @param rightX [右边x]
   * @param rightY [右边y]
   * @return [五连结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  private boolean is5PointLine(int color, IntFunction<Integer> leftX, IntFunction<Integer> leftY,
      IntFunction<Integer> rightX, IntFunction<Integer> rightY) {
    int leftLineCount = lineCount(color, leftX, leftY);
    if (leftLineCount == INDEX_THREE || leftLineCount == INDEX_FOUR) {
      this.successIndex = 1;
    }
    if (leftLineCount == INDEX_FOUR) {
      return true;
    }
    int rightLineCount = lineCount(color, rightX, rightY);
    if (rightLineCount == INDEX_THREE || rightLineCount == INDEX_FOUR) {
      this.successIndex = 1;
    }
    if (rightLineCount == INDEX_FOUR) {
      return true;
    }
    if (leftLineCount + rightLineCount >= 5) {
      return true;
    }
    return leftLineCount + rightLineCount + 1 == 5;
  }

  /**
   * TODO 五连计数.
   *
   * @param targetColor [目标颜色]
   * @param x [x轴数据]
   * @param y [y轴数据
   * @return [计数结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  private int lineCount(int targetColor, IntFunction<Integer> x, IntFunction<Integer> y) {
    int maxDistance = 4;
    for (int i = 1; i <= maxDistance; i++) {
      Integer thisX = x.apply(i);
      Integer thisY = y.apply(i);
      Preconditions.checkNotNull(thisX);
      Preconditions.checkNotNull(thisY);
      GoBangCoords checkedPoint = getCheckedPoint(thisX, thisY);
      if (checkedPoint == null || checkedPoint.getColor() != targetColor) {
        this.is4LinkRecord.clear();
        return i - 1;
      }
      is4LinkRecord.add(checkedPoint);
    }
    return 4;
  }

  /**
   * TODO 剩余数量.
   *
   * @return [剩余数量]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  public Integer remainingPiece() {
    Integer index = 0;
    for (GoBangCoords value : checkPoints.values()) {
      if (value.getColor() == 0) {
        index++;
      }
    }
    return index;
  }

  /**
   * TODO 坐标数据.
   *
   * @param x [x索引 0开始]
   * @param y [y索引 0开始]
   * @return [坐标数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  public GoBangCoords getCheckedPoint(int x, int y) {
    String pointId = pointId(x, y);
    return checkPoints.get(pointId);
  }

  /**
   * TODO 离开游戏.
   *
   * @param userId [用户ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  public void leaveGame(Long userId) {
    playerList.removeIf(s -> s.getUserId().equals(userId));
  }

  /**
   * TODO 添加定时.
   *
   * @param taskId [任务ID]
   * @param timeout [定时任务]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
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
   * @param taskId [任务ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
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
   * @param taskId [任务ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
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
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  public void destroy() {
    for (Timeout out : timeOutMap.values()) {
      out.cancel();
    }
    timeOutMap.clear();
  }

  /**
   * TODO 分析棋盘.
   *
   * @return [分析结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 14:08
   * @update 2020/9/15 14:08
   */
  public GoBangCoords explore() {
    List<GoBangCoords> operablePosition = getAnOperablePosition();
    List<GoBangCoords> weightMaxPosition = Lists.newLinkedList();
    int max = 0;
    if (CollectionUtils.isNotEmpty(operablePosition)) {
      for (GoBangCoords coords : operablePosition) {
        int weight = getWeight(coords.getPositionX(), coords.getPositionY());
        coords.setWeight(weight);
        if (weight > max) {
          max = weight;
        }
        if (max != 0 && weight == max) {
          if (weightMaxPosition.size() > 0) {
            if (weightMaxPosition.get(0).getWeight() < max) {
              weightMaxPosition.clear();
            }
          }
          weightMaxPosition.add(coords);
        }
      }
      //随机抽取一个位置
      GoBangCoords coords = weightMaxPosition.get((int) (Math.random() * weightMaxPosition.size()));
      //返回分析的位置
      return new GoBangCoords(coords.getPositionX(), coords.getPositionY());
    }
    return null;
  }

  /**
   * TODO 操作坐标.
   *
   * @return [操作坐标]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 14:10
   * @update 2020/9/15 14:10
   */
  private List<GoBangCoords> getAnOperablePosition() {
    List<GoBangCoords> goBangCoordsList = Lists.newLinkedList();
    for (int i = 0; i < CHECKERBOARD; i++) {
      for (int j = 0; j < CHECKERBOARD; j++) {
        if (board[i][j].getColor() > 0) {
          if (j != 0 && board[i][j - 1].getColor() == 0) {
            addToList(goBangCoordsList, i, j - 1);
          }
          if (j != (board.length - 1) && board[i][j + 1].getColor() == 0) {
            addToList(goBangCoordsList, i, j + 1);
          }
          if (i != 0 && j != 0 && board[i - 1][j - 1].getColor() == 0) {
            addToList(goBangCoordsList, i - 1, j - 1);
          }
          if (i != 0 && board[i - 1][j].getColor() == 0) {
            addToList(goBangCoordsList, i - 1, j);
          }
          if (i != 0 && j != (board.length - 1) && board[i - 1][j + 1].getColor() == 0) {
            addToList(goBangCoordsList, i - 1, j + 1);
          }
          if (i != (board.length - 1) && j != 0 && board[i + 1][j - 1].getColor() == 0) {
            addToList(goBangCoordsList, i + 1, j - 1);
          }
          if (i != (board.length - 1) && board[i + 1][j].getColor() == 0) {
            addToList(goBangCoordsList, i + 1, j);
          }
          if (i != (board.length - 1) && j != (board.length - 1) && board[i + 1][j + 1].getColor() == 0) {
            addToList(goBangCoordsList, i + 1, j + 1);
          }
        }
      }
    }
    return goBangCoordsList.size() > 0 ? goBangCoordsList : Collections.emptyList();
  }

  /**
   * TODO 添加坐标.
   *
   * @param coordsList [可选坐标]
   * @param x [x轴]
   * @param y [y轴]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 14:12
   * @update 2020/9/15 14:12
   */
  private void addToList(List<GoBangCoords> coordsList, int x, int y) {
    int sign = 0;
    for (GoBangCoords coords : coordsList) {
      if (coords.getPositionX() == x && coords.getPositionY() == y) {
        sign = 1;
        break;
      }
    }
    if (sign == 0) {
      coordsList.add(new GoBangCoords(x, y));
    }
  }

  /**
   * TODO 获得权值.
   *
   * @param x [x轴]
   * @param y [y轴]
   * @return [权值]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 14:14
   * @update 2020/9/15 14:14
   */
  private int getWeight(int x, int y) {
    //以己方棋子和对方棋子模拟落子计算权值和
    int weightOne = getWeightOne(x, y, 1) + getWeightOne(x, y, 2);
    int weightTwo = getWeightTwo(x, y, 1) + getWeightTwo(x, y, 2);
    int skewWeightOne = getSkewWeightOne(x, y, 1) + getSkewWeightOne(x, y, 2);
    int skewWeightTwo = getSkewWeightTwo(x, y, 1) + getSkewWeightTwo(x, y, 2);
    return weightOne + weightTwo + skewWeightOne + skewWeightTwo;
  }

  /**
   * TODO X轴权值.
   *
   * @param x [x轴]
   * @param y [y轴]
   * @param index [玩家标记]
   * @return [权值]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 14:16
   * @update 2020/9/15 14:16
   */
  private int getWeightOne(int x, int y, int index) {
    int flag = index == 1 ? 2 : 1;
    // 模拟落子
    board[x][y].setColor(index);
    //左侧、右侧的状态，用来记录棋型
    int leftStatus = 0;
    int rightStatus = 0;
    //count是相连棋子个数
    int j, count = 0;
    //扫描记录棋型
    for (j = y; j < board.length; j++) {
      if (board[x][j].getColor() == index) {
        count++;
      } else {
        if (board[x][j].getColor() == 0) {
          // 右侧为空
          rightStatus = 1;
        }
        if (board[x][j].getColor() == flag) {
          // 右侧被对方堵住
          rightStatus = 2;
        }
        break;
      }
    }
    for (j = y - 1; j >= 0; j--) {
      if (board[x][j].getColor() == index) {
        count++;
      } else {
        if (board[x][j].getColor() == 0) {
          // 左侧为空
          leftStatus = 1;
        }
        if (board[x][j].getColor() == flag) {
          // 左侧被对方堵住
          leftStatus = 2;
        }
        break;
      }
    }
    board[x][y].setColor(0);
    //根据棋型计算空位分数
    return getWeightBySituation(count, leftStatus, rightStatus);
  }

  /**
   * TODO Y轴权值.
   *
   * @param x [x轴]
   * @param y [y轴]
   * @param index [玩家标记]
   * @return [权值]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 14:20
   * @update 2020/9/15 14:20
   */
  private int getWeightTwo(int x, int y, int index) {
    int flag = index == 1 ? 2 : 1;
    // 模拟落子
    board[x][y].setColor(index);
    //左侧、右侧的状态，用来记录棋型
    int topStatus = 0;
    int bottomStatus = 0;
    int i, count = 0;
    //扫描记录棋型
    for (i = x; i < board.length; i++) {
      if (board[i][y].getColor() == index) {
        count++;
      } else {
        if (board[i][y].getColor() == 0) {
          // 下侧为空
          bottomStatus = 1;
        }
        if (board[i][y].getColor() == flag) {
          // 下侧被对方堵住
          bottomStatus = 2;
        }
        break;
      }
    }
    for (i = x - 1; i >= 0; i--) {
      if (board[i][y].getColor() == index) {
        count++;
      } else {
        if (board[i][y].getColor() == 0) {
          // 上侧为空
          topStatus = 1;
        }
        if (board[i][y].getColor() == flag) {
          // 上侧被对方堵住
          topStatus = 2;
        }
        break;
      }
    }
    board[x][y].setColor(0);
    return getWeightBySituation(count, topStatus, bottomStatus);
  }

  /**
   * TODO 偏斜权值. 从左上到右下
   *
   * @param x [x轴]
   * @param y [y轴]
   * @param index [玩家标记]
   * @return [权值]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 14:22
   * @update 2020/9/15 14:22
   */
  private int getSkewWeightOne(int x, int y, int index) {
    int flag = index == 1 ? 2 : 1;
    // 模拟落子
    board[x][y].setColor(index);
    int topStatus = 0;
    int bottomStatus = 0;
    int i, j, count = 0;
    for (i = x, j = y; i < board.length && j < board.length; i++, j++) {
      if (board[i][j].getColor() == index) {
        count++;
      } else {
        if (board[i][j].getColor() == 0) {
          // 下侧为空
          bottomStatus = 1;
        }
        if (board[i][j].getColor() == flag) {
          // 下侧被对方堵住
          bottomStatus = 2;
        }
        break;
      }
    }
    for (i = x - 1, j = y - 1; i >= 0 && j >= 0; i--, j--) {
      if (board[i][j].getColor() == index) {
        count++;
      } else {
        if (board[i][j].getColor() == 0) {
          // 上侧为空
          topStatus = 1;
        }
        if (board[i][j].getColor() == flag) {
          // 上侧被对方堵住
          topStatus = 2;
        }
        break;
      }
    }
    board[x][y].setColor(0);
    return getWeightBySituation(count, topStatus, bottomStatus);
  }

  /**
   * TODO 偏斜权值. 从右上到左下
   *
   * @param x [x轴]
   * @param y [y轴]
   * @param index [玩家标记]
   * @return [权值]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/15 14:22
   * @update 2020/9/15 14:22
   */
  private int getSkewWeightTwo(int x, int y, int index) {
    int flag = index == 1 ? 2 : 1;
    // 模拟落子
    board[x][y].setColor(index);
    int topStatus = 0;
    int bottomStatus = 0;
    int i, j, count = 0;
    for (i = x, j = y; i < board.length && j >= 0; i++, j--) {
      if (board[i][j].getColor() == index) {
        count++;
      } else {
        if (board[i][j].getColor() == 0) {
          // 下侧为空
          bottomStatus = 1;
        }
        if (board[i][j].getColor() == flag) {
          // 下侧被对方堵住
          bottomStatus = 2;
        }
        break;
      }
    }
    for (i = x - 1, j = y + 1; i >= 0 && j < board.length; i--, j++) {
      if (board[i][j].getColor() == index) {
        count++;
      } else {
        if (board[i][j].getColor() == 0) {
          // 上侧为空
          topStatus = 1;
        }
        if (board[i][j].getColor() == flag) {
          // 上侧被对方堵住
          topStatus = 2;
        }
        break;
      }
    }
    board[x][y].setColor(0);
    return getWeightBySituation(count, topStatus, bottomStatus);
  }

  /**
   * TODO 计算权值.
   *
   * @param count [连子个数]
   * @param leftStatus [左侧封堵情况 1-空位，2-对方或墙]
   * @param rightStatus [右侧封堵情况 1-空位，2-对方或墙]
   * @return [权值]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  private int getWeightBySituation(int count, int leftStatus, int rightStatus) {
    int weight = 0;
    // 五子情况
    if (count >= INDEX_FOUR + 1) {
      // 赢了
      weight += 200000;
    } else {
      boolean indexOne = leftStatus == 2 && rightStatus == 1;
      boolean indexTwo = leftStatus == 1 && rightStatus == 2;
      switch (count) {
        case 4:
          // 四子情况
          if (leftStatus == 1 && rightStatus == 1) {
            weight += 50000;
          }
          if (indexOne || indexTwo) {
            weight += 3000;
          }
          if (leftStatus == 2 && rightStatus == 2) {
            weight += 1000;
          }
          break;
        case 3:
          //三子情况
          if (leftStatus == 1 && rightStatus == 1) {
            weight += 3000;
          }
          if (indexOne || indexTwo) {
            weight += 1000;
          }
          if (leftStatus == 2 && rightStatus == 2) {
            weight += 500;
          }
          break;
        case 2:
          //二子情况
          if (leftStatus == 1 && rightStatus == 1) {
            weight += 500;
          }
          if (indexOne || indexTwo) {
            weight += 200;
          }
          if (leftStatus == 2 && rightStatus == 2) {
            weight += 100;
          }
          break;
        default:
          if (leftStatus == 1 && rightStatus == 1) {
            weight += 100;
          }
          if (indexOne || indexTwo) {
            weight += 50;
          }
          if (leftStatus == 2 && rightStatus == 2) {
            weight += 30;
          }
          break;

      }
    }
    return weight;
  }
}
