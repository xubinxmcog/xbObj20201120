package com.enuos.live.handle.game.f30071;

import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.util.Timeout;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Data;

/**
 * TODO 一站到底房间数据.
 *
 * @author WangCaiWen - missiw@163.com
 * @since 2020/7/8 14:35
 */

@Data
@SuppressWarnings("WeakerAccess")
public class MustStandRoom {

  /**
   * 房间ID「例如：2020」.
   */
  private Long roomId;
  /**
   * 房间回合「默认：1」.
   */
  private Integer roomRound = 1;
  /**
   * 房间状态 0「未开始」 1「已开始」 2「已结束」.
   */
  private Integer roomStatus = 0;
  /**
   * 开始时间「例如：2020-07-08T16:58:53.978」.
   */
  private LocalDateTime startTime;
  /**
   * 回合时间「例如：2020-07-08T16:58:53.978」.
   */
  private LocalDateTime roundTime;
  /**
   * 游戏题目〈回合数, 题目〉.
   */
  private Map<Integer, MustStandProblem> problemMap = Maps.newConcurrentMap();
  /**
   * 参与者列表.
   */
  private List<MustStandPlayer> partakerList = Lists.newCopyOnWriteArrayList();
  /**
   * 定时数据.
   */
  private HashMap<Integer, Timeout> timeOutMap = Maps.newHashMap();

  MustStandRoom(Long roomId) {
    this.roomId = roomId;
  }

  /**
   * TODO 进入游戏.
   *
   * @param channel 快速通道
   * @author WangCaiWen
   * @since 2020/7/8 - 2020/7/8
   */
  public void enterRoom(Channel channel, I10001.PlayerInfo playerInfo) {
    if (Objects.nonNull(playerInfo)) {
      MustStandPlayer partaker = new MustStandPlayer();
      partaker.setUserId(playerInfo.getUserId());
      partaker.setUserName(playerInfo.getNickName());
      partaker.setUserIcon(playerInfo.getIconUrl());
      partaker.setUserSex(playerInfo.getSex());
      partaker.setChannel(channel);
      if (partakerList.size() == 0) {
        partaker.setIdentity(1);
        partakerList.add(partaker);
      } else if (partakerList.size() == 1) {
        partaker.setIdentity(2);
        partakerList.add(partaker);
      }
    }
  }

  /**
   * TODO 添加游戏题目.
   *
   * @param problemList 题目列表
   * @author WangCaiWen
   * @since 2020/7/8 - 2020/7/8
   */
  public void addGameProblem(List<Map<String, Object>> problemList) {
    int index = 1;
    for (Map<String, Object> objectMap : problemList) {
      MustStandProblem problem = new MustStandProblem();
      problem.setLabel(StringUtils.nvl(objectMap.get("label")));
      problem.setTitle(StringUtils.nvl(objectMap.get("title")));
      problem.setAnswer(StringUtils.nvl(objectMap.get("answer")));
      if (objectMap.containsKey("wrongA")) {
        String a = StringUtils.nvl(objectMap.get("wrongA"));
        if (StringUtils.isNotEmpty(a)) {
          problem.setWrongA(a);
        }
      }
      if (objectMap.containsKey("wrongB")) {
        String b = StringUtils.nvl(objectMap.get("wrongB"));
        if (StringUtils.isNotEmpty(b)) {
          problem.setWrongB(b);
        }
      }
      if (objectMap.containsKey("wrongC")) {
        String c = StringUtils.nvl(objectMap.get("wrongC"));
        if (StringUtils.isNotEmpty(c)) {
          problem.setWrongC(c);
        }
      }
      problem.disruptChoose();
      problemMap.put(index, problem);
      index++;
    }
  }

  /**
   * TODO 刷新状态.
   *
   * @param status 房间状态
   * @author WangCaiWen
   * @since 2020/7/9 - 2020/7/9
   */
  public void refreshStatus(Integer status) {
    this.roomStatus = status;
  }

  /**
   * TODO 设置时间.
   *
   * @author WangCaiWen
   * @since 2020/7/9 - 2020/7/9
   */
  public void setUpStartTime() {
    this.startTime = LocalDateTime.now();
  }

  /**
   * TODO 刷新回合.
   *
   * @author WangCaiWen
   * @since 2020/7/8 - 2020/7/8
   */
  public void refreshRound() {
    this.roomRound = roomRound + 1;
  }

  /**
   * TODO 初始化用户.
   *
   * @author WangCaiWen
   * @since 2020/7/9 - 2020/7/9
   */
  public void initUserInfo() {
    for (MustStandPlayer partaker : partakerList) {
      partaker.initUserInfo();
    }
  }

  /**
   * TODO 刷新时间.
   *
   * @author WangCaiWen
   * @since 2020/7/9 - 2020/7/9
   */
  public void refreshRoundTime() {
    this.roundTime = LocalDateTime.now();
  }

  /**
   * TODO 获得玩家信息.
   *
   * @param userId 玩家ID
   * @return 玩家信息
   * @author WangCaiWen
   * @since 2020/7/8 - 2020/7/8
   */
  public MustStandPlayer getPlayerInfo(Long userId) {
    return partakerList.stream()
        .filter(partaker -> partaker.isBoolean(userId)).findFirst().orElse(null);
  }

  /**
   * TODO 获得胜利玩家.
   *
   * @return 玩家ID
   * @author WangCaiWen
   * @since 2020/7/9 - 2020/7/9
   */
  public Long getWinUserId() {
    MustStandPlayer partaker1 = partakerList.get(0);
    MustStandPlayer partaker2 = partakerList.get(1);
    if (partaker1.getUserScore().equals(partaker2.getUserScore())) {
      return 0L;
    } else if (partaker1.getUserScore() > partaker2.getUserScore()) {
      return partaker1.getUserId();
    } else {
      return partaker2.getUserId();
    }
  }

  /**
   * TODO 离开游戏.
   *
   * @param userId 用户ID
   * @author WangCaiWen
   * @since 2020/7/9 - 2020/7/9
   */
  public void leaveGame(Long userId) {
    partakerList.removeIf(s -> s.getUserId().equals(userId));
  }

  /**
   * TODO 检查操作.
   *
   * @return 检查结果
   * @author WangCaiWen
   * @since 2020/7/9 - 2020/7/9
   */
  public boolean actionExamine() {
    int index = 0;
    for (MustStandPlayer partaker : partakerList) {
      if (partaker.getSelectIndex() > 0) {
        index++;
      }
    }
    return index == 2;
  }

  /**
   * TODO 添加定时.
   *
   * @param taskId 任务ID.
   * @param timeout 定时任务.
   * @author WangCaiWen
   * @since 2020/7/9 - 2020/7/9
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
   * @author WangCaiWen
   * @since 2020/7/9 - 2020/7/9
   */
  public void cancelTimeOut(int taskId) {
    if (timeOutMap.containsKey(taskId)) {
      timeOutMap.get(taskId).cancel();
      timeOutMap.remove(taskId);
    }
  }

  /**
   * TODO 移除定时数据.
   *
   * @param taskId 定时ID.
   * @author WangCaiWen
   * @since 2020/7/9 - 2020/7/9
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
   * @author WangCaiWen
   * @since 2020/7/9 - 2020/7/9
   */
  public void destroy() {
    for (Timeout out : timeOutMap.values()) {
      out.cancel();
    }
    timeOutMap.clear();
  }

}
