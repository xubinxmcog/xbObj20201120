package com.enuos.live.handle.game.f30031;

import com.enuos.live.pojo.GameRobot;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.proto.f30031msg.F30031;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.util.Timeout;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;

/**
 * TODO 房间数据.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.2.0
 * @since 2020/6/1 15:47
 */

@Data
@SuppressWarnings("WeakerAccess")
public class MinesweeperRoom {
  /** 房间ID. */
  private Long roomId;
  /** 炸弹数量 [默认：18]. */
  private Integer bombNum = 18;
  /** 房间回合 [默认：1]. */
  private Integer roomRound = 1;
  /** 房间状态 [0-未开始 1-已开始]. */
  private Integer roomStatus = 0;
  /** 开始时间. */
  private LocalDateTime startTime;
  /** 操作时间. */
  private LocalDateTime latestTime;
  /** 操作顺序. */
  private List<Long> actionOrder = Lists.newLinkedList();
  /** 玩家操作. */
  private Map<Long, MinesweeperCoords> playerAction = Maps.newHashMap();
  /** 玩家列表. */
  private List<MinesweeperPlayer> playerList = Lists.newCopyOnWriteArrayList();
  /** 坐标数据. */
  private MinesweeperCoords[][] board = new MinesweeperCoords[DESKTOP][DESKTOP];
  /** 空白数据. */
  private Map<String, MinesweeperCoords> blankData = Maps.newHashMap();
  /** 桌面数据. */
  private Map<String, MinesweeperCoords> desktopData = Maps.newHashMap();
  /** 操作记录. */
  private List<MinesweeperCoords> actionRecord = Lists.newCopyOnWriteArrayList();
  /** 定时数据. */
  private HashMap<Integer, Timeout> timeOutMap = Maps.newHashMap();

  /** 桌面大小. */
  private static final int DESKTOP = 8;
  /** 炸弹数量. */
  private static final int BOMB_NUM = 18;

  // 初始化数据.
  {
    for (int i = 0; i < DESKTOP; i++) {
      for (int j = 0; j < DESKTOP; j++) {
        MinesweeperCoords minesweeperCoords = new MinesweeperCoords(i, j);
        board[i][j] = minesweeperCoords;
        desktopData.put(pointId(i, j), minesweeperCoords);
      }
    }
    // 放置炸弹
    List<MinesweeperCoords> minesweeperCoordsList = placeBomb();
    // 放置数值
    setPeripheralParameters(minesweeperCoordsList);
    // 提取空白数据
    extractBlankData();
  }

  /**
   * TODO 初始房间.
   *
   * @param roomId [房间ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/2 18:22
   * @update 2020/9/17 21:07
   */
  MinesweeperRoom(Long roomId) {
    this.roomId = roomId;
  }

  /**
   * TODO 放置炸弹.
   *
   * @return [炸弹数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/2 18:22
   * @update 2020/7/22 18:19
   */
  private List<MinesweeperCoords> placeBomb() {
    List<MinesweeperCoords> minesweeperCoordsList = new LinkedList<>();
    int x = 0;
    while (x <= BOMB_NUM - 1) {
      SecureRandom random = new SecureRandom();
      MinesweeperCoords coords = desktopData.get(pointId(random.nextInt(8), random.nextInt(8)));
      if (coords.getIsBomb() != 1) {
        coords.setIsBomb(1);
        board[coords.getPositionOne()][coords.getPositionTwo()] = coords;
        desktopData.put(pointId(coords.getPositionOne(), coords.getPositionTwo()), coords);
        minesweeperCoordsList.add(coords);
        x++;
      }
    }
    return minesweeperCoordsList;
  }

  /**
   * TODO 放置数值.
   *
   * @param coordsList [炸弹位置]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/2 18:22
   * @update 2020/7/22 18:19
   */
  private void setPeripheralParameters(List<MinesweeperCoords> coordsList) {
    for (MinesweeperCoords minesweeperCoords : coordsList) {
      int x = minesweeperCoords.getPositionOne();
      int y = minesweeperCoords.getPositionTwo();
      // 左上 (x-1),(y+1)
      if (desktopData.get(pointId(x - 1, y + 1)) != null) {
        MinesweeperCoords coords = desktopData.get(pointId(x - 1, y + 1));
        if (coords.getIsBomb() == 0) {
          coords.setCodeValue(coords.getCodeValue() + 1);
          desktopData.put(pointId(coords.getPositionOne(), coords.getPositionTwo()), coords);
        }
      }
      // 上   (x),(y+1)
      if (desktopData.get(pointId(x, y + 1)) != null) {
        MinesweeperCoords coords = desktopData.get(pointId(x, y + 1));
        if (coords.getIsBomb() == 0) {
          coords.setCodeValue(coords.getCodeValue() + 1);
          desktopData.put(pointId(coords.getPositionOne(), coords.getPositionTwo()), coords);
        }
      }
      // 右上 (x+1),(y+1)
      if (desktopData.get(pointId(x + 1, y + 1)) != null) {
        MinesweeperCoords coords = desktopData.get(pointId(x + 1, y + 1));
        if (coords.getIsBomb() == 0) {
          coords.setCodeValue(coords.getCodeValue() + 1);
          desktopData.put(pointId(coords.getPositionOne(), coords.getPositionTwo()), coords);
        }
      }
      // 左   (x-1),(y)
      if (desktopData.get(pointId(x - 1, y)) != null) {
        MinesweeperCoords coords = desktopData.get(pointId(x - 1, y));
        if (coords.getIsBomb() == 0) {
          coords.setCodeValue(coords.getCodeValue() + 1);
          desktopData.put(pointId(coords.getPositionOne(), coords.getPositionTwo()), coords);
        }
      }
      // 右   (x+1),(y)
      if (desktopData.get(pointId(x + 1, y)) != null) {
        MinesweeperCoords coords = desktopData.get(pointId(x + 1, y));
        if (coords.getIsBomb() == 0) {
          coords.setCodeValue(coords.getCodeValue() + 1);
          desktopData.put(pointId(coords.getPositionOne(), coords.getPositionTwo()), coords);
        }
      }
      // 左下 (x-1),(y-1)
      if (desktopData.get(pointId(x - 1, y - 1)) != null) {
        MinesweeperCoords coords = desktopData.get(pointId(x - 1, y - 1));
        if (coords.getIsBomb() == 0) {
          coords.setCodeValue(coords.getCodeValue() + 1);
          desktopData.put(pointId(coords.getPositionOne(), coords.getPositionTwo()), coords);
        }
      }
      // 下   (x),(y-1)
      if (desktopData.get(pointId(x, y - 1)) != null) {
        MinesweeperCoords coords = desktopData.get(pointId(x, y - 1));
        if (coords.getIsBomb() == 0) {
          coords.setCodeValue(coords.getCodeValue() + 1);
          desktopData.put(pointId(coords.getPositionOne(), coords.getPositionTwo()), coords);
        }
      }
      // 右下 (x+1),(y-1)
      if (desktopData.get(pointId(x + 1, y - 1)) != null) {
        MinesweeperCoords coords = desktopData.get(pointId(x + 1, y - 1));
        if (coords.getIsBomb() == 0) {
          coords.setCodeValue(coords.getCodeValue() + 1);
          desktopData.put(pointId(coords.getPositionOne(), coords.getPositionTwo()), coords);
        }
      }
    }
  }

  /**
   * TODO 提取空白.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/2 18:22
   * @update 2020/7/22 18:19
   */
  private void extractBlankData() {
    for (MinesweeperCoords coords : desktopData.values()) {
      if (coords.getIsBomb() == 0 && coords.getCodeValue() == 0) {
        blankData.put(pointId(coords.getPositionOne(), coords.getPositionTwo()), coords);
      }
    }
  }

  /**
   * TODO 组合坐标.
   *
   * @param x [x 索引 0开始]
   * @param y [y 索引 0开始]
   * @return [坐标Key]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/2 18:22
   * @update 2020/6/2 18:22
   */
  private String pointId(int x, int y) {
    return String.format("x%s:y%s", x, y);
  }

  /**
   * TODO 关联坐标.
   *
   * @param x [x坐标]
   * @param y [y坐标]
   * @return [坐标数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/2 18:22
   * @update 2020/7/22 18:19
   */
  public List<MinesweeperCoords> getLinkCoords(int x, int y) {
    List<MinesweeperCoords> minesweeperCoordsList = new LinkedList<>();
    // 左上 (x-1),(y+1)
    // 获得空白数据
    MinesweeperCoords coordsUpperLeft = blankData.get(pointId(x - 1, y + 1));
    if (coordsUpperLeft != null) {
      // 存在空白数据
      minesweeperCoordsList.add(coordsUpperLeft);
      blankData.remove(pointId(x - 1, y + 1));
      List<MinesweeperCoords> minesweeperCoordsList1 = getLinkCoords(coordsUpperLeft.getPositionOne(),
          coordsUpperLeft.getPositionTwo());
      if (minesweeperCoordsList1 != null && minesweeperCoordsList1.size() > 0) {
        minesweeperCoordsList.addAll(minesweeperCoordsList1);
      }
    } else {
      coordsUpperLeft = desktopData.get(pointId(x - 1, y + 1));
      if (coordsUpperLeft != null) {
        if (coordsUpperLeft.getStatus() == 1) {
          minesweeperCoordsList.add(coordsUpperLeft);
        }
      }
    }
    // 上   (x),(y+1)
    MinesweeperCoords coordsUpper = blankData.get(pointId(x, y + 1));
    if (coordsUpper != null) {
      // 存在空白数据
      minesweeperCoordsList.add(coordsUpper);
      blankData.remove(pointId(x, y + 1));
      List<MinesweeperCoords> minesweeperCoordsList1 = getLinkCoords(coordsUpper.getPositionOne(),
          coordsUpper.getPositionTwo());
      if (minesweeperCoordsList1 != null && minesweeperCoordsList1.size() > 0) {
        minesweeperCoordsList.addAll(minesweeperCoordsList1);
      }
    } else {
      coordsUpper = desktopData.get(pointId(x, y + 1));
      if (coordsUpper != null) {
        if (coordsUpper.getStatus() == 1) {
          minesweeperCoordsList.add(coordsUpper);
        }
      }
    }
    // 右上 (x+1),(y+1)
    MinesweeperCoords coordsUpperRight = blankData.get(pointId(x + 1, y + 1));
    if (coordsUpperRight != null) {
      // 存在空白数据
      minesweeperCoordsList.add(coordsUpperRight);
      blankData.remove(pointId(x + 1, y + 1));
      List<MinesweeperCoords> minesweeperCoordsList1 = getLinkCoords(coordsUpperRight.getPositionOne(),
          coordsUpperRight.getPositionTwo());
      if (minesweeperCoordsList1 != null && minesweeperCoordsList1.size() > 0) {
        minesweeperCoordsList.addAll(minesweeperCoordsList1);
      }
    } else {
      coordsUpperRight = desktopData.get(pointId(x + 1, y + 1));
      if (coordsUpperRight != null) {
        if (coordsUpperRight.getStatus() == 1) {
          minesweeperCoordsList.add(coordsUpperRight);
        }
      }
    }
    // 左   (x-1),(y)
    MinesweeperCoords coordsLeft = blankData.get(pointId(x - 1, y));
    if (coordsLeft != null) {
      // 存在空白数据
      minesweeperCoordsList.add(coordsLeft);
      blankData.remove(pointId(x - 1, y));
      List<MinesweeperCoords> minesweeperCoordsList1 = getLinkCoords(coordsLeft.getPositionOne(),
          coordsLeft.getPositionTwo());
      if (minesweeperCoordsList1 != null && minesweeperCoordsList1.size() > 0) {
        minesweeperCoordsList.addAll(minesweeperCoordsList1);
      }
    } else {
      coordsLeft = desktopData.get(pointId(x - 1, y));
      if (coordsLeft != null) {
        if (coordsLeft.getStatus() == 1) {
          minesweeperCoordsList.add(coordsLeft);
        }
      }
    }
    // 右   (x+1),(y)
    MinesweeperCoords coordsRight = blankData.get(pointId(x + 1, y));
    if (coordsRight != null) {
      // 存在空白数据
      minesweeperCoordsList.add(coordsRight);
      blankData.remove(pointId(x + 1, y));
      List<MinesweeperCoords> minesweeperCoordsList1 = getLinkCoords(coordsRight.getPositionOne(),
          coordsRight.getPositionTwo());
      if (minesweeperCoordsList1 != null && minesweeperCoordsList1.size() > 0) {
        minesweeperCoordsList.addAll(minesweeperCoordsList1);
      }
    } else {
      coordsRight = desktopData.get(pointId(x + 1, y));
      if (coordsRight != null) {
        if (coordsRight.getStatus() == 1) {
          minesweeperCoordsList.add(coordsRight);
        }
      }
    }
    // 左下 (x-1),(y-1)
    MinesweeperCoords coordsBottomLeft = blankData.get(pointId(x - 1, y - 1));
    if (coordsBottomLeft != null) {
      // 存在空白数据
      minesweeperCoordsList.add(coordsBottomLeft);
      blankData.remove(pointId(x - 1, y - 1));
      List<MinesweeperCoords> minesweeperCoordsList1 = getLinkCoords(coordsBottomLeft.getPositionOne(),
          coordsBottomLeft.getPositionTwo());
      if (minesweeperCoordsList1 != null && minesweeperCoordsList1.size() > 0) {
        minesweeperCoordsList.addAll(minesweeperCoordsList1);
      }
    } else {
      coordsBottomLeft = desktopData.get(pointId(x - 1, y - 1));
      if (coordsBottomLeft != null) {
        if (coordsBottomLeft.getStatus() == 1) {
          minesweeperCoordsList.add(coordsBottomLeft);
        }
      }
    }
    // 下   (x),(y-1)
    MinesweeperCoords coordsLower = blankData.get(pointId(x, y - 1));
    if (coordsLower != null) {
      // 存在空白数据
      minesweeperCoordsList.add(coordsLower);
      blankData.remove(pointId(x, y - 1));
      List<MinesweeperCoords> minesweeperCoordsList1 = getLinkCoords(coordsLower.getPositionOne(),
          coordsLower.getPositionTwo());
      if (minesweeperCoordsList1 != null && minesweeperCoordsList1.size() > 0) {
        minesweeperCoordsList.addAll(minesweeperCoordsList1);
      }
    } else {
      coordsLower = desktopData.get(pointId(x, y - 1));
      if (coordsLower != null) {
        if (coordsLower.getStatus() == 1) {
          minesweeperCoordsList.add(coordsLower);
        }
      }
    }
    // 右下 (x+1),(y-1)
    MinesweeperCoords coordsBottomRight = blankData.get(pointId(x + 1, y - 1));
    if (coordsBottomRight != null) {
      // 存在空白数据
      minesweeperCoordsList.add(coordsBottomRight);
      blankData.remove(pointId(x + 1, y - 1));
      List<MinesweeperCoords> minesweeperCoordsList1 = getLinkCoords(coordsBottomRight.getPositionOne(),
          coordsBottomRight.getPositionTwo());
      if (minesweeperCoordsList1 != null && minesweeperCoordsList1.size() > 0) {
        minesweeperCoordsList.addAll(minesweeperCoordsList1);
      }
    } else {
      coordsBottomRight = desktopData.get(pointId(x + 1, y - 1));
      if (coordsBottomRight != null) {
        if (coordsBottomRight.getStatus() == 1) {
          minesweeperCoordsList.add(coordsBottomRight);
        }
      }
    }
    List<MinesweeperCoords> minesweeperCoords = minesweeperCoordsList.stream().distinct()
        .collect(Collectors.toList());
    if (minesweeperCoords.size() > 0) {
      for (MinesweeperCoords coords : minesweeperCoords) {
        String pointId = pointId(coords.getPositionOne(), coords.getPositionTwo());
        coords.setStatus(2);
        desktopData.put(pointId, coords);
      }
    }
    return minesweeperCoords;
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
      MinesweeperPlayer player = new MinesweeperPlayer();
      player.setUserId(playerInfo.getUserId());
      player.setUserName(playerInfo.getNickName());
      player.setUserIcon(playerInfo.getIconUrl());
      player.setUserSex(playerInfo.getSex());
      player.setChannel(channel);
      player.setIsRobot(1);
      if (playerList.size() == 0) {
        player.setIdentity(1);
        this.playerList.add(player);
      } else if (playerList.size() == 1) {
        player.setIdentity(2);
        this.playerList.add(player);
      }
    }
  }

  /**
   * TODO 开启陪玩.
   *
   * @param gameRobot [机器人信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/14 11:21
   * @update 2020/9/14 11:21
   */
  public void joinRobot(GameRobot gameRobot) {
    if (Objects.nonNull(gameRobot)) {
      MinesweeperPlayer player = new MinesweeperPlayer();
      player.setUserId(gameRobot.getRobotId());
      player.setUserName(gameRobot.getRobotName());
      player.setUserIcon(gameRobot.getRobotAvatar());
      player.setUserSex(gameRobot.getRobotSex());
      player.setChannel(null);
      player.setIsRobot(0);
      if (playerList.size() == 0) {
        player.setIdentity(1);
        this.playerList.add(player);
      }
    }
  }

  /**
   * TODO 玩家信息.
   *
   * @param userId [玩家ID]
   * @return [玩家信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/9/17 21:07
   */
  public MinesweeperPlayer getPlayerInfo(Long userId) {
    return playerList.stream()
        .filter(partaker -> partaker.isBoolean(userId))
        .findFirst().orElse(null);
  }

  /**
   * TODO 设置时间.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/7/22 18:19
   */
  public void setUpStartTime() {
    this.startTime = LocalDateTime.now();
  }

  /**
   * TODO 可以选择.
   *
   * @param position [坐标数据]
   * @return [选择结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/17 21:07
   * @update 2020/9/17 21:07
   */
  public boolean canChoose(F30031.ChoicePosition position) {
    int x = position.getChoicePositionX();
    int y = position.getChoicePositionY();
    MinesweeperCoords coords = getCoordinateData(x, y);
    return coords.getStatus() <= 1;
  }

  /**
   * TODO 玩家选择.
   *
   * @param request [客户端数据]
   * @param userId [玩家ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/7/22 18:19
   */
  public void playerSelect(F30031.F300312C2S request, Long userId) {
    F30031.ChoicePosition position = request.getChoicePosition();
    MinesweeperCoords coords = new MinesweeperCoords(position.getChoicePositionX(), position.getChoicePositionY());
    coords.setIndex(request.getIsMine());
    this.actionOrder.add(userId);
    this.playerAction.put(userId, coords);
  }

  /**
   * TODO 玩家操作.
   *
   * @param minesweeperCoords [坐标数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/7/22 18:19
   */
  public void playerMoves(MinesweeperCoords minesweeperCoords) {
    int x = minesweeperCoords.getPositionOne();
    int y = minesweeperCoords.getPositionTwo();
    String pointId = pointId(x, y);
    MinesweeperCoords coords = getCoordinateData(x, y);
    if (coords.getStatus() == 1) {
      // 翻开
      coords.setStatus(2);
      coords.setIndex(0);
      board[x][y] = coords;
      desktopData.put(pointId, coords);
      this.actionRecord.add(coords);
      //  减少雷数
      if (coords.getIsBomb() == 1) {
        this.bombNum = bombNum - 1;
      }
    }
  }

  /**
   * TODO 剩余数量.
   *
   * @return [剩余数量].
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/7/22 18:19
   */
  public Integer numberOfRemaining() {
    Integer number = 0;
    for (MinesweeperCoords coords : desktopData.values()) {
      if (coords.getStatus() == 1) {
        number++;
      }
    }
    return number;
  }

  /**
   * TODO 刷新雷数.
   *
   * @param coordsOne 坐标数据1
   * @param coordsTwo 坐标数据2
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/7/22 18:19
   */
  public void refreshThunder(MinesweeperCoords coordsOne, MinesweeperCoords coordsTwo) {
    if (coordsTwo == null) {
      MinesweeperCoords coords = getCoordinateData(coordsOne.getPositionOne(), coordsOne.getPositionTwo());
      // 减雷数
      if (coords.getIsBomb() == 1) {
        if (bombNum > 0) {
          this.bombNum = bombNum - 1;
        }
      }
    } else {
      MinesweeperCoords coords1 = getCoordinateData(coordsOne.getPositionOne(), coordsOne.getPositionTwo());
      // 减雷数
      if (coords1.getIsBomb() == 1) {
        if (bombNum > 0) {
          this.bombNum = bombNum - 1;
        }
      }
      MinesweeperCoords coords2 = getCoordinateData(coordsTwo.getPositionOne(), coordsTwo.getPositionTwo());
      // 减雷数
      if (coords2.getIsBomb() == 1) {
        if (bombNum > 0) {
          this.bombNum = bombNum - 1;
        }
      }
    }
  }

  /**
   * TODO 坐标数据.
   *
   * @param x [x索引 0开始]
   * @param y [y索引 0开始]
   * @return [坐标数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/7/22 18:19
   */
  public MinesweeperCoords getCoordinateData(int x, int y) {
    String pointId = pointId(x, y);
    return desktopData.get(pointId);
  }

  /**
   * TODO 刷新回合.
   *
   * @return [回合数]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/7/22 18:19
   */
  public int refreshRound() {
    this.roomRound = roomRound + 1;
    return this.roomRound;
  }

  /**
   * TODO 初始房间.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/7/22 18:19
   */
  public void initRoomInfo() {
    this.actionOrder = Lists.newLinkedList();
    this.playerAction = Maps.newHashMap();
  }

  /**
   * TODO 更新状态.
   *
   * @param status [状态值]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/7/22 18:19
   */
  public void updateRoomStatus(Integer status) {
    this.roomStatus = status;
  }

  /**
   * TODO 移除数据.
   *
   * @param x [x索引 0开始]
   * @param y [y索引 0开始]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/7/22 18:19
   */
  public void removeCoords(int x, int y) {
    MinesweeperCoords coords = getCoordinateData(x, y);
    String pointId = pointId(x, y);
    coords.setStatus(2);
    desktopData.put(pointId, coords);
    blankData.remove(pointId);
  }

  /**
   * TODO 操作记录.
   *
   * @param minesweeperCoords [操作记录]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/7/22 18:19
   */
  public void addRecord(List<MinesweeperCoords> minesweeperCoords) {
    this.actionRecord.addAll(minesweeperCoords);
  }

  /**
   * TODO 离开游戏.
   *
   * @param userId [用户ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/22 18:19
   * @update 2020/7/22 18:19
   */
  public void leaveGame(Long userId) {
    playerList.removeIf(s -> s.getUserId().equals(userId));
  }

  /**
   * TODO 棋桌数据.
   *
   * @return [棋桌数据]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/8 6:44
   * @update 2020/6/24 18:06
   */
  public List<MinesweeperCoords> getDesktopData() {
    return this.actionRecord;
  }

  /**
   * TODO 添加定时.
   *
   * @param taskId [任务ID].
   * @param timeout [定时任务].
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/6/8 6:44
   * @update 2020/6/15 18:19
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
   * @create 2020/6/8 6:44
   * @update 2020/6/8 6:44
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
   * @create 2020/6/8 6:44
   * @update 2020/7/22 18:19
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
   * @create 2020/6/8 6:44
   * @update 2020/9/17 21:07
   */
  public void destroy() {
    for (Timeout out : timeOutMap.values()) {
      out.cancel();
    }
    timeOutMap.clear();
  }

  /**
   * TODO 随机选择.
   *
   * @return [选择位置]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/23 16:16
   * @update 2020/9/23 16:16
   */
  public MinesweeperCoords randomSelectPosition() {
    List<MinesweeperCoords> coordsList = Lists.newLinkedList();
    for (MinesweeperCoords coords : desktopData.values()) {
      if (coords.getStatus() == 1) {
        coordsList.add(coords);
      }
    }
    return coordsList.get((int) (Math.random() * coordsList.size()));
  }
}
