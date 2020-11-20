package com.enuos.live.schedule;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.enuos.live.constants.RedisKey;
import com.enuos.live.dto.RoomHeatDTO;
import com.enuos.live.feign.NettyFeign;
import com.enuos.live.mapper.RedPacketsSendMapper;
import com.enuos.live.mapper.TbVoiceRoomHeatMapper;
import com.enuos.live.mapper.UserAccountAttachPOMapper;
import com.enuos.live.pojo.RedPacketsSend;
import com.enuos.live.pojo.RoomPO;
import com.enuos.live.pojo.TbVoiceRoomHeat;
import com.enuos.live.service.RoomService;
import com.enuos.live.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName RedPacketsSchdule
 * @Description: TODO 语音房定时处理任务
 * @Author xubin
 * @Date 2020/6/12
 * @Version V1.0
 **/
@Slf4j
@Component
public class RoomJobSchdule {

    @Autowired
    private RedPacketsSendMapper redPacketsSendMapper;

    @Autowired
    private UserAccountAttachPOMapper accountAttachPOMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RoomService roomService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private NettyFeign nettyFeign;

    @Autowired
    private TbVoiceRoomHeatMapper voiceRoomHeatMapper;

    @Value("${rpBack.cycle}")
    private String cycle;

    @Value("${rpBack.type}")
    private String type;

    /**
     * @MethodName: overdueRpBack
     * @Description: TODO 过期红包退回处理
     * @Param: []
     * @Return: void
     * @Author: xubin
     * @Date: 15:41 2020/7/27
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Scheduled(cron = "${scheduledCron.rpBack}") // 每 * 分钟执行一次
    public void overdueRpBack() {

        String uuid = IdUtil.simpleUUID();
        String lockKey = "KEY_LOCK:RPBACK";

        log.info("开始执行过期红包退回处理任务...");

        try {
            // 使用Redis加锁
            Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, 60, TimeUnit.SECONDS);
            if (!ifAbsent) {
                log.info("红包退回任务加锁状态");
                return;
            }
            List<RedPacketsSend> redPacketsSends = redPacketsSendMapper.selectOverdueRp(cycle, type);

            if (ObjectUtil.isEmpty(redPacketsSends)) {
                log.info("selectOverdueRp is null");
                return;
            }
            for (RedPacketsSend redPacketsSend : redPacketsSends) {

                Integer surplusAmount = redPacketsSend.getSurplusAmount();

                redPacketsSend.setIsClose(1);
                if (surplusAmount > 0) {
                    Integer integer = accountAttachPOMapper.upUserAccountAttachAmount(Long.valueOf(redPacketsSend.getSurplusAmount()), redPacketsSend.getUserId());
                    if (integer > 0) {
                        redPacketsSend.setSurplusAmount(0);
                        String rpKey = RedisKey.KEY_RP + redPacketsSend.getId();
                        String robRpKey = rpKey + "_" + redPacketsSend.getRoomId();
                        redisUtils.del(rpKey, robRpKey); // 删除缓存
                    }
                }
            }
            redPacketsSendMapper.updateBatch(redPacketsSends);

        } catch (Exception e) {
            log.error("执行过期红包退回处理任务异常!!! ");
            e.printStackTrace();
        } finally {
            if (uuid.equals(redisTemplate.opsForValue().get(lockKey))) {
                log.info("红包退回处理释放锁");
                redisTemplate.delete(lockKey);
            }
        }
    }


    /**
     * 主播异常退出房间处理下播
     */
    @Scheduled(cron = "${scheduledCron.roomOwner}") // 每 * 分钟执行一次
    public void roomOwnerExceptionEndBroadcast() {

        String uuid = IdUtil.simpleUUID();
        String lockKey = "KEY_LOCK:ROOMOWNER";

        try {
            // 使用Redis加锁
            Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, 30, TimeUnit.SECONDS);
            if (!ifAbsent) {
                log.info("主播异常退出房间处理下播任务加锁状态");
                return;
            }
            Set<ZSetOperations.TypedTuple> set = redisUtils.zRangeWithScores(RedisKey.KEY_ROOM_STATE, 0, 1);
            if (ObjectUtil.isNotEmpty(set)) {
                ZSetOperations.TypedTuple value = (ZSetOperations.TypedTuple) set.toArray()[0];
                String[] idValues = value.getValue().toString().split("_");

                Long roomId = Long.valueOf(idValues[0]);
                Long userId = Long.valueOf(idValues[1]);
                log.info("主播异常退出,房间号=[{}],userId=[{}]", roomId, userId);
                long expTime = new Double(value.getScore()).longValue();
                long currentTimeMills = System.currentTimeMillis() / 1000;
                if (currentTimeMills >= expTime) {
                    log.info("执行主播异常退出房间下播处理, roomId:[{}]", roomId);
                    RoomPO roomPO = new RoomPO();
                    roomPO.setRoomId(roomId);
                    // 调用socket发送下播通知
                    nettyFeign.exceptionEndBroadcast(roomId, userId);
                    redisUtils.zRemove(RedisKey.KEY_ROOM_STATE, value.getValue());
                }
            }
        } catch (Exception e) {
            log.error("执行主播异常退出房间处理下播任务异常!!! ");
            e.printStackTrace();
        } finally {
            if (uuid.equals(redisTemplate.opsForValue().get(lockKey))) {
                redisTemplate.delete(lockKey);
            }
        }


    }

    /**
     * 更新语音房热度
     */
    @Scheduled(cron = "${scheduledCron.roomHead}") // 每 10 分钟执行一次
    public void upRoomHeat() {
        log.info("更新语音房热度");
        List<RoomHeatDTO> roomHeats = voiceRoomHeatMapper.getRoomHeat();
        if (ObjectUtil.isNotEmpty(roomHeats)) {
            List<TbVoiceRoomHeat> list = new ArrayList<>();

            for (RoomHeatDTO roomHeat : roomHeats) {
                TbVoiceRoomHeat records = new TbVoiceRoomHeat();
                Long roomId = roomHeat.getRoomId();
                Long totalCharmValue = roomHeat.getTotalCharmValue();// 房间贡献榜总贡献
                Long weekCharmValue = roomHeat.getWeekCharmValue() * 2;// 房间贡献榜本周贡献
                Long unitCharmValue = roomHeat.getUnitCharmValue() * 50;// 单位时间用户累计赠送礼物贡献
                Long roomUserNum = roomHeat.getRoomUserNum() * 1000;// 当前房间内用户数
                Long vipUserNum = roomHeat.getVipUserNum() * 4000;// 当前房间内会员用户数
                Long concernNum = roomHeat.getConcernNum() * 200;// 房间关注用户数
                Long weekConcernNum = roomHeat.getWeekConcernNum() * 500;// 本周新增关注用户数
                Long activityNum = roomHeat.getActivityNum() * 100;// 用户累计发送消息数
                Double totalBroadcastTime = roomHeat.getTotalBroadcastTime() * 5;// 语音房累计播放时长 分钟
                Double weekBroadcastTime = roomHeat.getWeekBroadcastTime() * 20;// 语音房本周播放时长 分钟
                Double concernBroadcastTime = roomHeat.getConcernBroadcastTime() * 50;// 语音房本次开播时长 分钟
                Long headValue = roomHeat.getHeadValue();// 后台推荐值

                Double totalRoomHeat = totalCharmValue + weekCharmValue + unitCharmValue + roomUserNum + vipUserNum + concernNum + weekConcernNum + activityNum + totalBroadcastTime + weekBroadcastTime + concernBroadcastTime + headValue;
                records.setRoomId(roomId);
                records.setHeat(totalRoomHeat.longValue());
                list.add(records);
            }
            voiceRoomHeatMapper.insertBatchRoomHeat(list);
        }
    }
}
