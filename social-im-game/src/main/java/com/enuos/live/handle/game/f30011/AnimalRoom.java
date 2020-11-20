package com.enuos.live.handle.game.f30011;

import com.enuos.live.pojo.GameRobot;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.proto.f30011msg.F30011;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.util.Timeout;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

/**
 * TODO 房间数据.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/5/21 19:58
 */

@Data
@SuppressWarnings("WeakerAccess")
public class AnimalRoom {
  /** 房间ID. */
  private Long roomId;
  /** 下一玩家. */
  private Long nextActionId;
  /** 房间状态 [0-未开始 1-已开始 2-已经结束]. */
  private Integer gameStatus = 0;
  /** 开始时间. */
  private LocalDateTime startTime;
  /** 操作时间. */
  private LocalDateTime actionTime;
  /** 玩家列表. */
  private List<AnimalPlayer> playerList = Lists.newCopyOnWriteArrayList();
  /** 棋盘数据. */
  private Map<String, AnimalCoords> checkPoints = Maps.newHashMap();
  /** 定时数据. */
  private HashMap<Integer, Timeout> timeOutMap = new HashMap<>();
  /** 棋盘数据. */
  private AnimalCoords[][] board = new AnimalCoords[CHECKERBOARD][CHECKERBOARD];

  /** 棋盘大小. */
  private static final int CHECKERBOARD = 4;
  /** 大象角色ID. */
  private static final int ELEPHANT_ROLES = 8;

  // 初始化棋盘.
  {
    List<Integer> animalList = IntStream.range(1, CHECKERBOARD * CHECKERBOARD + 1).boxed()
        .collect(Collectors.toCollection(LinkedList::new));
    for (int i = 0; i < CHECKERBOARD; i++) {
      for (int j = 0; j < CHECKERBOARD; j++) {
        AnimalCoords pointInfo = new AnimalCoords(i, j);
        int index = (int) (Math.random() * animalList.size());
        pointInfo.setAnimal(animalList.get(index));
        pointInfo.setColor(colorsMap.get(pointInfo.getAnimal()));
        pointInfo.setRoles(rolesMap.get(pointInfo.getAnimal()));
        pointInfo.setStatus(1);
        board[i][j] = pointInfo;
        animalList.remove(index);
        checkPoints.put(pointId(i, j), pointInfo);
      }
    }
  }

  /** 颜色数据. */
  private static HashMap<Integer, Integer> colorsMap = Maps.newHashMap();
  static {
    colorsMap.put(1, 1);
    colorsMap.put(2, 1);
    colorsMap.put(3, 1);
    colorsMap.put(4, 1);
    colorsMap.put(5, 1);
    colorsMap.put(6, 1);
    colorsMap.put(7, 1);
    colorsMap.put(8, 1);
    colorsMap.put(9, 2);
    colorsMap.put(10, 2);
    colorsMap.put(11, 2);
    colorsMap.put(12, 2);
    colorsMap.put(13, 2);
    colorsMap.put(14, 2);
    colorsMap.put(15, 2);
    colorsMap.put(16, 2);
  }

  /** 动物角色. */
  private static HashMap<Integer, Integer> rolesMap = Maps.newHashMap();
  static {
    rolesMap.put(1, 1);
    rolesMap.put(2, 2);
    rolesMap.put(3, 3);
    rolesMap.put(4, 4);
    rolesMap.put(5, 5);
    rolesMap.put(6, 6);
    rolesMap.put(7, 7);
    rolesMap.put(8, 8);
    rolesMap.put(9, 1);
    rolesMap.put(10, 2);
    rolesMap.put(11, 3);
    rolesMap.put(12, 4);
    rolesMap.put(13, 5);
    rolesMap.put(14, 6);
    rolesMap.put(15, 7);
    rolesMap.put(16, 8);
  }

  /** 蓝色皮肤. */
  private static HashMap<Integer, Integer> skinMap = Maps.newHashMap();
  static {
    skinMap.put(1, 9);
    skinMap.put(2, 10);
    skinMap.put(3, 11);
    skinMap.put(4, 12);
    skinMap.put(5, 13);
    skinMap.put(6, 14);
    skinMap.put(7, 15);
    skinMap.put(8, 16);
  }

  /**
   * TODO 初始数据.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/27 21:47
   * @update 2020/5/27 21:47
   */
  AnimalRoom(Long roomId) {
    this.roomId = roomId;
  }

  /**
   * TODO 组合坐标.
   *
   * @param x [x 索引 0开始]
   * @param y [y 索引 0开始]
   * @return [坐标Key]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/25 6:26
   * @update 2020/7/30 21:28
   */
  private String pointId(int x, int y) {
    return String.format("x%s:y%s", x, y);
  }

  /**
   * TODO 蓝色动物.
   *
   * @param index [标记值]
   * @return [动物ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/31 7:08
   * @update 2020/7/31 7:08
   */
  public Integer getAnimalId(Integer index) {
    return skinMap.get(index);
  }

  /**
   * TODO 加入房间.
   *
   * @param channel [通信管道]
   * @param playerInfo [玩家信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/14 12:48
   * @update 2020/9/14 12:48
   */
  public void joinRoom(Channel channel, I10001.PlayerInfo playerInfo) {
    if (Objects.nonNull(playerInfo)) {
      AnimalPlayer player = new AnimalPlayer();
      player.setUserId(playerInfo.getUserId());
      player.setUserName(playerInfo.getNickName());
      player.setUserIcon(playerInfo.getIconUrl());
      player.setUserSex(playerInfo.getSex());
      player.setChannel(channel);
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
    }
  }

  /**
   * TODO 开启陪玩.
   *
   * @param gameRobot [机器人信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/22 21:08
   * @update 2020/9/22 21:08
   */
  public void joinRobot(GameRobot gameRobot) {
    if (Objects.nonNull(gameRobot)) {
      AnimalPlayer player = new AnimalPlayer();
      player.setUserId(gameRobot.getRobotId());
      player.setUserName(gameRobot.getRobotName());
      player.setUserIcon(gameRobot.getRobotAvatar());
      player.setUserSex(gameRobot.getRobotSex());
      player.setChannel(null);
      player.setIdentity(1);
      player.setIsRobot(0);
      this.playerList.add(player);
      if (playerList.size() == 1) {
        this.nextActionId = gameRobot.getRobotId();
      }
    }
  }

  /**
   * TODO 玩家数据.
   *
   * @param userId [userId]
   * @return [玩家数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/27 21:47
   * @update 2020/7/30 21:28
   */
  public AnimalPlayer getGamePlayer(Long userId) {
    return playerList.stream()
        .filter(partaker -> partaker.isBoolean(userId))
        .findFirst().orElse(null);
  }

  /**
   * TODO 翻转位置.
   *
   * @param x [x轴]
   * @param y [y轴]
   * @param userId [用户ID]
   * @return [坐标数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/25 21:10
   * @update 2020/7/30 21:28
   */
  public AnimalCoords flipPosition(int x, int y, Long userId) {
    String pointId = pointId(x, y);
    AnimalCoords animalCoords = getCheckedPoint(x, y);
    animalCoords.setStatus(2);
    board[x][y] = animalCoords;
    checkPoints.put(pointId, animalCoords);
    AnimalPlayer player = getGamePlayer(userId);
    if (player.getIdentity() == 1) {
      this.nextActionId = playerList.get(1).getUserId();
      player.setFlipAnimalCoords(animalCoords);
    } else {
      this.nextActionId = playerList.get(0).getUserId();
      player.setFlipAnimalCoords(animalCoords);
    }
    emptyActionRound();
    this.actionTime = LocalDateTime.now();
    return animalCoords;
  }

  /**
   * TODO 坐标数据.
   *
   * @param x [x索引 0开始]
   * @param y [y索引 0开始]
   * @return [坐标数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/25 21:10
   * @update 2020/7/30 21:28
   */
  public AnimalCoords getCheckedPoint(int x, int y) {
    String pointId = pointId(x, y);
    return checkPoints.get(pointId);
  }

  /**
   * TODO 开始时间.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/25 21:10
   * @update 2020/7/30 21:28
   */
  public void setUpGameStartTime() {
    this.startTime = LocalDateTime.now();
    this.actionTime = this.startTime;
  }

  /**
   * TODO 操作判断.
   *
   * @param request [客户端数据]
   * @return [是否操作]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/27 21:47
   * @update 2020/9/22 21:08
   */
  public boolean isAction(F30011.F300112C2S request) {
    F30011.MPosition initPosition = request.getInitPosition();
    F30011.MPosition movePosition = request.getMovePosition();
    AnimalCoords coordsUser = getCheckedPoint(initPosition.getPositionX(), initPosition.getPositionY());
    AnimalCoords animalCoords = getCheckedPoint(movePosition.getPositionX(), movePosition.getPositionY());
    int x1 = initPosition.getPositionX();
    int y2 = initPosition.getPositionY();
    int x3 = movePosition.getPositionX();
    int y4 = movePosition.getPositionY();
    boolean one = (y2 == y4) && (x1 + 1 == x3 || x1 - 1 == x3);
    boolean two = (x1 == x3) && (y2 - 1 == y4 || y2 + 1 == y4);
    // 移动位置
    if (one || two) {
      // 当前位置 为 null
      if (animalCoords.getStatus() == 0) {
        return true;
      }
      // 当前位置 未翻开
      if (animalCoords.getStatus() == 1) {
        return false;
      }
      // 当前位置 已翻开
      Integer beastId = request.getInitBeastId();
      Integer animal = coordsUser.getAnimal();
      // 校验兽ID
      if (!beastId.equals(animal)) {
        return false;
      }
      // 检查目标ID
      Integer targetAnimal = request.getMoveBeastId();
      if (!targetAnimal.equals(animalCoords.getAnimal())) {
        return false;
      }
      // 检查颜色 角色
      Integer userColor = coordsUser.getColor();
      Integer targetColor = animalCoords.getColor();
      if (userColor.equals(targetColor)) {
        // 颜色相同 不可操作
        return false;
      }
      int userRoles = coordsUser.getRoles();
      int targetRoles = animalCoords.getRoles();
      // 1 鼠 8 象 ---------- 鼠吃象/象打鼠被灭
      if (userRoles == 1 && targetRoles == ELEPHANT_ROLES) {
        return true;
      }
      if (userRoles == ELEPHANT_ROLES && targetRoles == 1) {
        return true;
      }
      // 角色相同 同归于尽
      if (userRoles == targetRoles) {
        return true;
      }
      // 击杀/被击杀
      return true;
    } else {
      return false;
    }
  }

  /**
   * TODO 玩家操作.
   *
   * @param request [客户端数据]
   * @param userId [用户ID]
   * @return [结果返回]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/27 21:47
   * @update 2020/9/22 21:08
   */
  public int playerMoves(F30011.F300112C2S request, Long userId) {
    F30011.MPosition initPosition = request.getInitPosition();
    F30011.MPosition movePosition = request.getMovePosition();
    AnimalCoords userCoords = getCheckedPoint(initPosition.getPositionX(), initPosition.getPositionY());
    AnimalCoords targetCoords = getCheckedPoint(movePosition.getPositionX(), movePosition.getPositionY());
    AnimalPlayer player = getGamePlayer(userId);
    if (player.getIdentity() == 1) {
      this.nextActionId = playerList.get(1).getUserId();
    } else {
      this.nextActionId = playerList.get(0).getUserId();
    }
    this.actionTime = LocalDateTime.now();
    // 当前用户信息
    AnimalPlayer animalPlayer = getGamePlayer(userId);
    // 移动
    if (targetCoords.getStatus() == 0) {
      updateCoordsInfo(targetCoords, userCoords);
      // 更改用户信息
      animalPlayer.setActionNum(animalPlayer.getActionNum() + 1);
      animalPlayer.setNewAnimalCoords(targetCoords);
      return 1;
    }
    Integer userRoles = userCoords.getRoles();
    Integer targetRoles = targetCoords.getRoles();
    // 鼠吃象
    if (userRoles == 1 && targetRoles == ELEPHANT_ROLES) {
      updateCoordsInfo(targetCoords, userCoords);
      // 更改用户信息
      animalPlayer.setNewAnimalCoords(targetCoords);
      emptyActionRound();
      return 2;
    }
    // 象打鼠被灭
    if (userRoles == ELEPHANT_ROLES && targetRoles == 1) {
      userCoords.setColor(0);
      userCoords.setAnimal(0);
      userCoords.setRoles(0);
      userCoords.setStatus(0);
      board[userCoords.getPositionX()][userCoords.getPositionY()] = userCoords;
      checkPoints.put(pointId(userCoords.getPositionX(), userCoords.getPositionY()), userCoords);
      // 更改用户信息
      animalPlayer.setNewAnimalCoords(userCoords);
      emptyActionRound();
      return 3;
    }
    // 角色相同 同归于尽
    if (userRoles.equals(targetRoles)) {
      targetCoords.setColor(0);
      targetCoords.setAnimal(0);
      targetCoords.setRoles(0);
      targetCoords.setStatus(0);
      board[targetCoords.getPositionX()][targetCoords.getPositionY()] = targetCoords;
      checkPoints.put(pointId(targetCoords.getPositionX(), targetCoords.getPositionY()), targetCoords);
      userCoords.setColor(0);
      userCoords.setAnimal(0);
      userCoords.setRoles(0);
      userCoords.setStatus(0);
      board[userCoords.getPositionX()][userCoords.getPositionY()] = userCoords;
      checkPoints.put(pointId(userCoords.getPositionX(), userCoords.getPositionY()), userCoords);
      // 更改用户信息
      animalPlayer.setNewAnimalCoords(userCoords);
      emptyActionRound();
      return 4;
    }
    if (userRoles > targetRoles) {
      // 击杀对方
      updateCoordsInfo(targetCoords, userCoords);
      // 更改用户信息
      animalPlayer.setNewAnimalCoords(targetCoords);
      emptyActionRound();
      return 2;
    } else {
      // 被击杀
      userCoords.setColor(0);
      userCoords.setAnimal(0);
      userCoords.setRoles(0);
      userCoords.setStatus(0);
      board[userCoords.getPositionX()][userCoords.getPositionY()] = userCoords;
      checkPoints.put(pointId(userCoords.getPositionX(), userCoords.getPositionY()), userCoords);
      // 更改用户信息
      animalPlayer.setNewAnimalCoords(userCoords);
      emptyActionRound();
      return 3;
    }
  }

  /**
   * TODO 更新坐标.
   *
   * @param animalCoords [目标坐标]
   * @param coordsUser [起始坐标]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/25 21:10
   * @update 2020/9/22 21:08
   */
  private void updateCoordsInfo(AnimalCoords animalCoords, AnimalCoords coordsUser) {
    animalCoords.setColor(coordsUser.getColor());
    animalCoords.setAnimal(coordsUser.getAnimal());
    animalCoords.setRoles(coordsUser.getRoles());
    animalCoords.setStatus(2);
    board[animalCoords.getPositionX()][animalCoords.getPositionY()] = animalCoords;
    checkPoints.put(pointId(animalCoords.getPositionX(), animalCoords.getPositionY()), animalCoords);
    coordsUser.setColor(0);
    coordsUser.setAnimal(0);
    coordsUser.setRoles(0);
    coordsUser.setStatus(0);
    board[coordsUser.getPositionY()][coordsUser.getPositionY()] = coordsUser;
    checkPoints.put(pointId(coordsUser.getPositionX(), coordsUser.getPositionY()), coordsUser);
  }

  /**
   * TODO 清空回合.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/27 21:47
   * @update 2020/9/17 21:07
   */
  private void emptyActionRound() {
    playerList.forEach(player -> player.setActionNum(0));
  }

  /**
   * TODO 输赢判断.
   *
   * @param userId [用户ID]
   * @return [结果码]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/27 21:47
   * @update 2020/9/17 21:07
   */
  public int isWinOrDraw(Long userId) {
    // 0 未胜利 1 胜利 2 平局 3 输了
    int red = 0;
    int blue = 0;
    for (AnimalCoords animalCoords : checkPoints.values()) {
      if (animalCoords.getStatus() >= 1) {
        if (animalCoords.getColor() == 1) {
          red = red + 1;
        } else {
          blue = blue + 1;
        }
      }
    }
    AnimalPlayer animalPlayer = getGamePlayer(userId);
    if (animalPlayer.getIdentity() == 1) {
      // 当前玩家为红色
      // 查看蓝色数据
      if (blue > 0 && red == 0) {
        return 3;
      }
      if (blue > 0) {
        return 0;
      }
      if (blue == 0 && red > 0) {
        return 1;
      }
      if (blue == 0 && red == 0) {
        return 2;
      }
    } else {
      // 蓝色
      // 查看红色数据
      if (red > 0 && blue == 0) {
        return 3;
      }
      if (red > 0) {
        return 0;
      }
      if (red == 0 && blue > 0) {
        return 1;
      }
      if (red == 0 && blue == 0) {
        return 2;
      }
    }
    return 0;
  }

  /**
   * TODO 离开游戏.
   *
   * @param userId [用户ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/27 21:47
   * @update 2020/7/30 21:28
   */
  public void leaveGame(Long userId) {
    playerList.removeIf(player -> player.isBoolean(userId));
  }

  /**
   * TODO 棋桌数据.
   *
   * @return [棋桌数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/5/27 21:47
   * @update 2020/6/5 7:03
   */
  public List<AnimalCoords> getDesktopData() {
    return new LinkedList<>(checkPoints.values());
  }

  /**
   * TODO 添加定时.
   *
   * @param taskId [任务ID].
   * @param timeout [定时任务].
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/5 7:03
   * @update 2020/6/5 7:03
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
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/5 7:03
   * @update 2020/6/5 7:03
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
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/5 7:03
   * @update 2020/7/30 21:28
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
   * @create 2020/6/5 7:03
   * @update 2020/9/17 21:07
   */
  public void destroy() {
    timeOutMap.values().forEach(Timeout::cancel);
    timeOutMap.clear();
  }

  /**
   * TODO 分析棋牌.
   *
   * @return [棋牌数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/22 17:47
   * @update 2020/9/22 17:47
   */
  public List<AnimalCoords> explore() {
    // 机器人颜色为1 红色
    // 搜索已翻开的属于自己的颜色 没有属于自己的牌 翻牌
    List<AnimalCoords> animalCoords = canOperateAnimal();
    if (CollectionUtils.isEmpty(animalCoords)) {
      return Collections.emptyList();
    }
    List<AnimalCoords> attackList = Lists.newLinkedList();
    List<AnimalCoords> advanceList = Lists.newLinkedList();
    Map<String, AnimalCoords> attackMap = Maps.newHashMap();
    Map<String, AnimalCoords> advanceMap = Maps.newHashMap();
    for (AnimalCoords coords : animalCoords) {
      // [0-不可出击 1-可以出击 2-可以前进 3-忽略]
      int action1 = getTargetLocationLeft(coords);
      if (action1 == 1 || action1 == 2) {
        AnimalCoords coords1 = getCheckedPoint(coords.getPositionX() - 1, coords.getPositionY());
        if (action1 == 1) {
          if (!attackList.contains(coords1)) {
            String pointId = pointId(coords1.getPositionX(), coords1.getPositionY());
            if (!attackMap.containsKey(pointId)) {
              attackMap.put(pointId, coords);
              attackList.add(coords1);
            }
          }
        } else {
          if (!advanceList.contains(coords1)) {
            String pointId = pointId(coords1.getPositionX(), coords1.getPositionY());
            if (!advanceMap.containsKey(pointId)) {
              advanceMap.put(pointId, coords);
              advanceList.add(coords1);
            }
          }
        }
      }
      // [0-不可出击 1-可以出击 2-可以前进 3-忽略]
      int action2 = getTargetLocationRight(coords);
      if (action2 == 1 || action2 == 2) {
        AnimalCoords coords2 = getCheckedPoint(coords.getPositionX() + 1, coords.getPositionY());
        if (action2 == 1) {
          if (!attackList.contains(coords2)) {
            String pointId = pointId(coords2.getPositionX(), coords2.getPositionY());
            if (!attackMap.containsKey(pointId)) {
              attackMap.put(pointId, coords);
              attackList.add(coords2);
            }
          }
        } else {
          if (!advanceList.contains(coords2)) {
            String pointId = pointId(coords2.getPositionX(), coords2.getPositionY());
            if (!advanceMap.containsKey(pointId)) {
              advanceMap.put(pointId, coords);
              advanceList.add(coords2);
            }
          }
        }
      }
      // [0-不可出击 1-可以出击 2-可以前进 3-忽略]
      int action3 = getTargetLocationUpon(coords);
      if (action3 == 1 || action3 == 2) {
        AnimalCoords coords3 = getCheckedPoint(coords.getPositionX(), coords.getPositionY() + 1);
        if (action3 == 1) {
          if (!attackList.contains(coords3)) {
            String pointId = pointId(coords3.getPositionX(), coords3.getPositionY());
            if (!attackMap.containsKey(pointId)) {
              attackMap.put(pointId, coords);
              attackList.add(coords3);
            }
          }
        } else {
          if (!advanceList.contains(coords3)) {
            String pointId = pointId(coords3.getPositionX(), coords3.getPositionY());
            if (!advanceMap.containsKey(pointId)) {
              advanceMap.put(pointId, coords);
              advanceList.add(coords3);
            }
          }
        }
      }
      // [0-不可出击 1-可以出击 2-可以前进 3-忽略]
      int action4 = getTargetLocationUnder(coords);
      if (action4 == 1 || action4 == 2) {
        AnimalCoords coords4 = getCheckedPoint(coords.getPositionX(), coords.getPositionY() - 1);
        if (action4 == 1) {
          if (!attackList.contains(coords4)) {
            String pointId = pointId(coords4.getPositionX(), coords4.getPositionY());
            if (!attackMap.containsKey(pointId)) {
              attackMap.put(pointId, coords);
              attackList.add(coords4);
            }
          }
        } else {
          if (!advanceList.contains(coords4)) {
            String pointId = pointId(coords4.getPositionX(), coords4.getPositionY());
            if (!advanceMap.containsKey(pointId)) {
              advanceMap.put(pointId, coords);
              advanceList.add(coords4);
            }
          }
        }
      }
    }
    if (CollectionUtils.isNotEmpty(attackList)) {
      AnimalCoords animalCoords1 = attackList.get((int) (Math.random() * attackList.size()));
      AnimalCoords animalCoords2 = attackMap.get(pointId(animalCoords1.getPositionX(), animalCoords1.getPositionY()));
      List<AnimalCoords> actionList = Lists.newLinkedList();
      actionList.add(animalCoords2);
      actionList.add(animalCoords1);
      return actionList;
    } else if (CollectionUtils.isNotEmpty(advanceList)) {
      AnimalCoords animalCoords1 = advanceList.get((int) (Math.random() * advanceList.size()));
      AnimalCoords animalCoords2 = advanceMap.get(pointId(animalCoords1.getPositionX(), animalCoords1.getPositionY()));
      List<AnimalCoords> actionList = Lists.newLinkedList();
      actionList.add(animalCoords2);
      actionList.add(animalCoords1);
      return actionList;
    }
    return Collections.emptyList();
  }

  /**
   * TODO 可以操作.
   *
   * @return [操作位置]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/22 16:01
   * @update 2020/9/22 16:01
   */
  private List<AnimalCoords> canOperateAnimal() {
    List<AnimalCoords> animalCoords = checkPoints.values().stream()
        .filter(coords -> coords.getColor() == 1 && coords.getStatus() == 2)
        .collect(Collectors.toCollection(Lists::newLinkedList));
    //  棋颜色 1-红 2-蓝               状态 0-空 1-关闭 2-翻开
    return animalCoords.size() > 0 ? animalCoords : Collections.emptyList();
  }

  /**
   * TODO 随机翻牌.
   *
   * @return [翻牌位置]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/22 17:26
   * @update 2020/9/22 17:26
   */
  public AnimalCoords randomFlopPosition() {
    List<AnimalCoords> coordsList = checkPoints.values().stream()
        .filter(coords -> coords.getStatus() == 1)
        .collect(Collectors.toCollection(Lists::newLinkedList));
    return coordsList.get((int) (Math.random() * coordsList.size()));
  }

  /**
   * TODO 能否翻牌.
   *
   * @return [翻牌结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/22 16:00
   * @update 2020/9/22 16:00
   */
  public boolean canFlop() {
    return checkPoints.values().stream().anyMatch(coords -> coords.getStatus() == 1);
  }

  /**
   * TODO 左边位置.
   *
   * @param coords [初始位置]
   * @return [0-不可出击 1-可以出击 2-可以前进 3-忽略]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/22 15:29
   * @update 2020/9/22 15:29
   */
  private int getTargetLocationLeft(AnimalCoords coords) {
    int x = coords.getPositionX();
    int y = coords.getPositionY();
    if (x > 0 && x < 4) {
      //  棋颜色 1-红 2-蓝          状态 0-空 1-关闭 2-翻开
      AnimalCoords animalCoords = getCheckedPoint(x - 1, y);
      if (animalCoords.getStatus() == 1) {
        // 未翻开
        return 0;
      } else if (animalCoords.getStatus() == 0 && animalCoords.getColor() == 0) {
        // 空位置
        return 2;
      } else if (animalCoords.getStatus() == 2 && animalCoords.getColor() == 2) {
        int nowAnimal = coords.getRoles();
        int targetAnimal = animalCoords.getRoles();
        if (nowAnimal == 1 && targetAnimal == 8) {
          // 鼠吃象.可以出击
          return 1;
        }
        if (nowAnimal == 8 && targetAnimal == 1) {
          // 自杀.不可出击
          return 0;
        }
        if (nowAnimal > targetAnimal) {
          // 大于目标.可以出击
          return 1;
        }
        if (nowAnimal < targetAnimal) {
          // 自杀.不可出击
          return 0;
        }
      } else if (animalCoords.getStatus() == 2 && animalCoords.getColor() == 1) {
        // 自己棋子 不可出击
        return 0;
      }
    }
    // 边界.忽略
    return 3;
  }

  /**
   * TODO 右边位置.
   *
   * @param coords [初始位置]
   * @return [0-不可出击 1-可以出击 2-可以前进 3-忽略]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/22 15:34
   * @update 2020/9/22 15:34
   */
  private int getTargetLocationRight(AnimalCoords coords) {
    int x = coords.getPositionX();
    int y = coords.getPositionY();
    if (x >= 0 && x < 3) {
      //  棋颜色 1-红 2-蓝          状态 0-空 1-关闭 2-翻开
      AnimalCoords animalCoords = getCheckedPoint(x + 1, y);
      if (animalCoords.getStatus() == 1) {
        // 未翻开
        return 0;
      } else if (animalCoords.getStatus() == 0 && animalCoords.getColor() == 0) {
        // 空位置
        return 2;
      } else if (animalCoords.getStatus() == 2 && animalCoords.getColor() == 2) {
        int nowAnimal = coords.getRoles();
        int targetAnimal = animalCoords.getRoles();
        if (nowAnimal == 1 && targetAnimal == 8) {
          // 鼠吃象.可以出击
          return 1;
        }
        if (nowAnimal == 8 && targetAnimal == 1) {
          // 自杀.不可出击
          return 0;
        }
        if (nowAnimal > targetAnimal) {
          // 大于目标.可以出击
          return 1;
        }
        if (nowAnimal < targetAnimal) {
          // 自杀.不可出击
          return 0;
        }
      } else if (animalCoords.getStatus() == 2 && animalCoords.getColor() == 1) {
        // 自己棋子 不可出击
        return 0;
      }
    }
    // 边界.忽略
    return 3;
  }

  /**
   * TODO 上方位置.
   *
   * @param coords [初始位置]
   * @return [0-不可出击 1-可以出击 2-可以前进 3-忽略]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/22 15:38
   * @update 2020/9/22 15:38
   */
  private int getTargetLocationUpon(AnimalCoords coords) {
    int x = coords.getPositionX();
    int y = coords.getPositionY();
    if (y >= 0 && y < 3) {
      AnimalCoords animalCoords = getCheckedPoint(x, y + 1);
      if (animalCoords.getStatus() == 1) {
        // 未翻开
        return 0;
      } else if (animalCoords.getStatus() == 0 && animalCoords.getColor() == 0) {
        // 空位置
        return 2;
      } else if (animalCoords.getStatus() == 2 && animalCoords.getColor() == 2) {
        int nowAnimal = coords.getRoles();
        int targetAnimal = animalCoords.getRoles();
        if (nowAnimal == 1 && targetAnimal == 8) {
          // 鼠吃象.可以出击
          return 1;
        }
        if (nowAnimal == 8 && targetAnimal == 1) {
          // 自杀.不可出击
          return 0;
        }
        if (nowAnimal > targetAnimal) {
          // 大于目标.可以出击
          return 1;
        }
        if (nowAnimal < targetAnimal) {
          // 自杀.不可出击
          return 0;
        }
      } else if (animalCoords.getStatus() == 2 && animalCoords.getColor() == 1) {
        // 自己棋子 不可出击
        return 0;
      }
    }
    // 边界.忽略
    return 3;
  }

  /**
   * TODO 下方位置.
   *
   * @param coords [初始位置]
   * @return [0-不可出击 1-可以出击 2-可以前进 3-忽略]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/22 15:40
   * @update 2020/9/22 15:40
   */
  private int getTargetLocationUnder(AnimalCoords coords) {
    int x = coords.getPositionX();
    int y = coords.getPositionY();
    if (y > 0 && y < 4) {
      AnimalCoords animalCoords = getCheckedPoint(x, y - 1);
      if (animalCoords.getStatus() == 1) {
        // 未翻开
        return 0;
      } else if (animalCoords.getStatus() == 0 && animalCoords.getColor() == 0) {
        // 空位置
        return 2;
      } else if (animalCoords.getStatus() == 2 && animalCoords.getColor() == 2) {
        int nowAnimal = coords.getRoles();
        int targetAnimal = animalCoords.getRoles();
        if (nowAnimal == 1 && targetAnimal == 8) {
          // 鼠吃象.可以出击
          return 1;
        }
        if (nowAnimal == 8 && targetAnimal == 1) {
          // 自杀.不可出击
          return 0;
        }
        if (nowAnimal > targetAnimal) {
          // 大于目标.可以出击
          return 1;
        }
        if (nowAnimal < targetAnimal) {
          // 自杀.不可出击
          return 0;
        }
      } else if (animalCoords.getStatus() == 2 && animalCoords.getColor() == 1) {
        // 自己棋子 不可出击
        return 0;
      }
    }
    // 边界.忽略
    return 3;
  }

}
