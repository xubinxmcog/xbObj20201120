package com.enuos.live.task;

import com.enuos.live.utils.annotation.AbstractActionHandler;
import com.enuos.live.channel.GameChannel;
import com.enuos.live.channel.SoftChannel;
import com.enuos.live.constants.GameKey;
import com.enuos.live.proto.i10001msg.I10001;
import com.enuos.live.server.HandlerContext;
import com.enuos.live.utils.ExceptionUtil;
import com.enuos.live.manager.LoggerManager;
import com.enuos.live.utils.RedisUtils;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.stereotype.Component;

/**
 * TODO 数据检测.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.0.0
 * @since 2020/8/3 18:36
 */

@Component
public class GameMonitorTask {

  @Resource
  private RedisUtils redisUtils;
  @Resource
  private HandlerContext handlerContext;

  /** 创建监测线程. */
  private static ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1,
      new BasicThreadFactory.Builder().namingPattern("monitor-09199-%d").daemon(true).build());
  // 初始化计时器 延时5分钟启动，每5分钟扫描一次

  {
    executor.scheduleWithFixedDelay(this::dataMonitor, 5, 5, TimeUnit.MINUTES);
  }

  /**
   * TODO 数据监测.
   *
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/10/9 10:51
   * @update 2020/10/9 10:51
   */
  private void dataMonitor() {
    try {
      LoggerManager.info("[MONITOR SERVER] PLAYER ONLINE: [{}], MEMBER ONLINE: [{}]", GameChannel.onlinePlayers(), SoftChannel.onlinePlayers());
      if (GameChannel.onlinePlayers() == 0) {
        Set<String> roomKeyList = this.redisUtils.keySearch("*" + GameKey.KEY_GAME_ROOM_RECORD.getName() + "*");
        if (CollectionUtils.isNotEmpty(roomKeyList)) {
          LoggerManager.info("[MONITOR SERVER] RESIDUE ROOM, RIGHT NOW DELETE!!!!!");
          for (String roomKey : roomKeyList) {
            byte[] roomByte = this.redisUtils.getByte(roomKey);
            I10001.RoomRecord roomRecord = I10001.RoomRecord.parseFrom(roomByte);
            AbstractActionHandler instance = this.handlerContext.getInstance((int) roomRecord.getGameId());
            instance.cleaning(roomRecord.getRoomId());
          }
          this.redisUtils.delKeys(roomKeyList);
        }
        Set<String> joinKeyList = this.redisUtils.keySearch("*" + GameKey.KEY_GAME_JOIN_RECORD.getName() + "*");
        if (CollectionUtils.isNotEmpty(joinKeyList)) {
          this.redisUtils.delKeys(joinKeyList);
        }
      }
    } catch (Exception e) {
      LoggerManager.error(e.getMessage());
      LoggerManager.error(ExceptionUtil.getStackTrace(e));
    }
  }
}
