package com.enuos.live.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.enuos.live.constants.RedisKey;
import com.enuos.live.dto.EmoticonDTO;
import com.enuos.live.dto.GiftGiveDTO;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.NettyFeign;
import com.enuos.live.feign.UserFeign;
import com.enuos.live.manager.AchievementEnum;
import com.enuos.live.manager.ActivityEnum;
import com.enuos.live.manager.TaskEnum;
import com.enuos.live.mapper.*;
import com.enuos.live.pojo.Emoticon;
import com.enuos.live.pojo.Gift;
import com.enuos.live.pojo.UserAccountAttachPO;
import com.enuos.live.pojo.UserCharm;
import com.enuos.live.result.Result;
import com.enuos.live.server.GiftExtraRewardServer;
import com.enuos.live.service.GiftService;
import com.enuos.live.service.RoomTaskAsync;
import com.enuos.live.server.RoomTaskServer;
import com.enuos.live.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @ClassName GiftServiceImpl
 * @Description: TODO 礼物
 * @Author xubin
 * @Date 2020/6/17
 * @Version V2.0
 **/
@Slf4j
@Service
public class GiftServiceImpl implements GiftService {

    @Autowired
    private GiftMapper giftMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserAccountAttachPOMapper accountAttachPOMapper;

    @Autowired
    private UserCharmMapper userCharmMapper;

    @Autowired
    private RoomTaskAsync roomTaskAsync;

    @Autowired
    private GiftCouponMapper giftCouponMapper;

    @Autowired
    private EmoticonMapper emoticonMapper;

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private RoomTaskServer roomTaskServer;

    @Autowired
    private NettyFeign nettyFeign;

    @Autowired
    private GiveNumMapper giveNumMapper;

    @Resource(name = "taskFxbDrawExecutor")
    ExecutorService executorService;

    @Autowired
    private GiftExtraRewardServer giftExtraRewardServer;

    /**
     * @MethodName: getList
     * @Description: TODO 查询礼物列表
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:07 2020/6/17
     **/
    @Override
    public Result getList() {
        return Result.success(giftMapper.getList());
    }

    /**
     * @MethodName: getUserCouponList
     * @Description: TODO 用户礼物券列表
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:14 2020/6/18
     **/
    @Override
    public Result getUserCouponList(Long userId) {
        giftCouponMapper.deleteOverdue();
        return Result.success(giftCouponMapper.selectUserGiftCouponList(userId));
    }

    /**
     * @MethodName: give
     * @Description: TODO 赠送礼物
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 12:56 2020/6/18
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result give(GiftGiveDTO dto) {

        Long giftId = dto.getGiftId(); // 礼物ID
        String giftKey = RedisKey.KEY_GIFT + giftId;
        Gift gift = (Gift) redisUtils.get(giftKey);
        if (ObjectUtil.isEmpty(gift)) {
            gift = giftMapper.selectByPrimaryKey(giftId); // 礼物信息
            if (ObjectUtil.isEmpty(gift))
                return Result.error(ErrorCode.DATA_ERROR);
            redisUtils.set(giftKey, gift, 86400); // 暂定24小时有效期
        }
        Long userId = dto.getUserId(); // 送礼人ID
        Long receiveUserId = dto.getReceiveUserId(); // 收礼人ID
        Integer giftNum = dto.getGiftNum(); // 礼物数量
        Long roomId = dto.getRoomId(); // 房间ID
        log.info("赠送礼物userId=[{}],收礼人ID=[{}],礼物ID=[{}],礼物数量=[{}],giftCouponId=[{}],房间id=[{}]", userId, receiveUserId, giftId, giftNum, dto.getGiftCouponId(), roomId);

        UserAccountAttachPO accountAttachPO = null;
        Map<String, Object> billMap = null;
        long totalAmount = 0L;
        if (StrUtil.isEmpty(dto.getGiftCouponId())) { // 判断送礼物

            Integer priceType = gift.getPriceType(); // 支付类型 2:钻石 3:金币
            Long giftPrice = gift.getGiftPrice(); // 价格

            //查询用户金币和钻石
            Map<String, Object> map = accountAttachPOMapper.getBalance(null, userId);
            if (ObjectUtil.isEmpty(map)) {
                log.info("未查询到用户金币和钻石数据userId={}", userId);
                return Result.error(ErrorCode.CONTENT_EMPTY);
            }
            final int id = (Integer) map.get("id"); // 用户账户Id
            final long gold = (Long) map.get("gold"); // 用户金币
            final long diamond = (Long) map.get("diamond"); // 用户钻石

            accountAttachPO = new UserAccountAttachPO();
            accountAttachPO.setId(id);
            billMap = new HashMap(); // 入账
            if (priceType == 2) { // 钻石
                totalAmount = giftPrice * giftNum;
                final long surplusDiamond = diamond - totalAmount;
                if (surplusDiamond < 0) {
                    return Result.error(ErrorCode.NOT_ENOUGH_DIAMOND);
                }
                accountAttachPO.setDiamond(surplusDiamond);
                billMap.put("price", -totalAmount);
                billMap.put("priceType", 2);

//                Gift finalGift = gift;
//                new Thread(() -> {
//                    userFeign.addGrowth(userId, Integer.parseInt(String.valueOf(totalAmount))); // 添加成长值
//                    giveGiftNotice(dto, finalGift);
//                }).start();

            } else if (priceType == 3) { // 金币
                totalAmount = giftPrice * giftNum;
                final long surplusGold = gold - totalAmount;
                if (surplusGold < 0) {
                    return Result.error(ErrorCode.NOT_ENOUGH_GOLD);
                }
                accountAttachPO.setGold(surplusGold);
                billMap.put("price", -totalAmount);
                billMap.put("priceType", 3);
            }

//            Integer update = accountAttachPOMapper.update(accountAttachPO);// 扣除用户钻石或者金币
//            if (update > 0) { // 入账
//                billMap.put("userId", userId);
//                billMap.put("productName", "礼物" + receiveUserId + gift.getGiftName());
//                billMap.put("status", 1);
//                roomTaskAsync.entryBill(billMap);
//            }

        } else if (1 == gift.getGiftType()) { // 送礼物券
            int giftCount = giftCouponMapper.giftCount(userId, dto.getGiftCouponId());
            if (giftCount < giftNum) {
                return Result.error(201, "礼物券不足");
            }
            if (giftCouponMapper.delUserGiftCoupon(userId, dto.getGiftCouponId(), giftNum) < 1) {
                log.error("扣减券失败, userId=[{}], 券code=[{}]", userId, dto.getGiftCouponId());
                return Result.error();
            }
        } else {
            log.error("无效的礼物或礼物券, dto=[{}]", dto);
            return Result.error();
        }

        Long charmValue = gift.getCharmValue() * giftNum;
        UserCharm userCharm = new UserCharm();
        userCharm.setCharmValue(charmValue);
        userCharm.setGiftId(giftId);
        userCharm.setGiftNum(giftNum);
        userCharm.setGiveUserId(userId);
        userCharm.setUserId(receiveUserId);
        userCharm.setRoomId(roomId);
        Integer priceType = gift.getPriceType() == null ? 0 : gift.getPriceType();
        Long giftPrice = gift.getGiftPrice() == null ? 0 : gift.getGiftPrice() * giftNum;
        userCharm.setPriceType(priceType);
        userCharm.setGiftPrice(giftPrice);

        // 增加魅力表
        userCharmMapper.insert(userCharm); // 新增

        dto.setGiftName(gift.getGiftName());
        dto.setCharmValue(gift.getCharmValue());

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("experience", gift.getExpValue());

        UserAccountAttachPO finalAccountAttachPO = accountAttachPO;
        Map<String, Object> finalBillMap = billMap;
        Gift finalGift = gift;
        long finalTotalAmount = totalAmount;

        executorService.submit(() -> {

            if (null != finalGift.getPriceType() && finalGift.getPriceType() == 2 && finalTotalAmount != 0) {
                userFeign.addGrowth(userId, Integer.parseInt(String.valueOf(finalTotalAmount))); // 添加成长值
                giveGiftNotice(dto, finalGift); // 送礼全服通告
            }

            if (ObjectUtil.isNotEmpty(finalAccountAttachPO)) {
                Integer update = accountAttachPOMapper.update(finalAccountAttachPO);// 扣除用户钻石或者金币
                if (update > 0 && ObjectUtil.isNotEmpty(finalBillMap)) { // 入账
                    finalBillMap.put("userId", userId);
                    finalBillMap.put("productName", "礼物" + receiveUserId + finalGift.getGiftName());
                    finalBillMap.put("status", 1);
                    roomTaskAsync.entryBill(finalBillMap);
                }
            }

            int i = userCharmMapper.upUserCountCharm(receiveUserId, charmValue);// 更新用户总魅力值
            log.info("更新用户总魅力值状态=[{}]", i);
            achievementHandler(receiveUserId, charmValue); // 魅力值成就进度处理
            roomTaskServer.taskHandler(TaskEnum.PGT0034.getCode(), userId, roomId); // 语音房任务达成.
            userFeign.countExp(map); // 计算经验值
            giftExtraRewardServer.luckDraw(giftId, userId); // 送礼物额外奖励处理
            roomTaskServer.roomActivity(ActivityEnum.ACT000105.getCode(), userId); // 丹枫秋日活动, 送任意礼物
        });

        return Result.success(dto);
    }

    private void giveGiftNotice(GiftGiveDTO dto, Gift gift) {
        if (gift.getGiftPrice() >= 2000 && gift.getPriceType() == 2) {
            Map<String, Object> params = new HashMap<>();
            Map<String, Object> userMap = userFeign.getUserBase(dto.getUserId(), "nickName", "thumbIconUrl");
            Map<String, Object> toUserMap = userFeign.getUserBase(dto.getReceiveUserId(), "nickName", "thumbIconUrl");
            params.put("roomId", dto.getRoomId()); // 房间ID
            params.put("fromUserId", dto.getUserId()); // 送礼人ID
            params.put("fromIconURL", MapUtils.getString(userMap, "thumbIconUrl")); // 送礼人头像
            params.put("fromNickName", MapUtils.getString(userMap, "nickName")); // 送礼人昵称
            params.put("toUserId", dto.getReceiveUserId()); // 收礼人ID
            params.put("toIconURL", MapUtils.getString(toUserMap, "thumbIconUrl")); // 收礼人头像
            params.put("toNickName", MapUtils.getString(toUserMap, "nickName")); // /收礼人昵称
            params.put("giftURL", gift.getGiftUrl()); // 礼物图片
            params.put("giftName", gift.getGiftName()); // 礼物名称
            params.put("giftNum", dto.getGiftNum()); // 礼物数量
            Result result = nettyFeign.giveGiftNotice(params);
            log.info("赠送礼物通知结果, [{}]", result.getCode());
        }
    }

    /**
     * @MethodName: giveEmoticon
     * @Description: TODO 赠送表情包
     * @Param: [dto]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 10:43 2020/6/23
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result giveEmoticon(EmoticonDTO dto) {

        Long emId = dto.getEmId(); // 礼物ID
        String giftKey = RedisKey.KEY_EM + emId;
        Emoticon emoticon = (Emoticon) redisUtils.get(giftKey);
        if (ObjectUtil.isEmpty(emoticon)) {
            emoticon = emoticonMapper.selectByPrimaryKey(emId); // 礼物信息
            if (ObjectUtil.isEmpty(emoticon))
                return Result.error(ErrorCode.DATA_ERROR);
            redisUtils.set(giftKey, emoticon, 86400); // 暂定24小时有效期
        }
        Long userId = dto.getUserId(); // 送礼人ID
        Long receiveUserId = dto.getReceiveUserId(); // 收礼人ID
        Integer emNum = dto.getEmNum(); // 礼物数量

        Integer priceType = emoticon.getPriceType(); // 支付类型 2:钻石 3:金币
        Long giftPrice = emoticon.getEmPrice(); // 价格

        //查询用户金币和钻石
        Map<String, Object> map = accountAttachPOMapper.getBalance(null, userId);
        if (ObjectUtil.isEmpty(map)) {
            log.info("未查询到用户金币和钻石数据userId={}", userId);
            return Result.error(ErrorCode.CONTENT_EMPTY);
        }
        final int id = (Integer) map.get("id"); // 用户账户Id
        final long gold = (Long) map.get("gold"); // 用户金币
        final long diamond = (Long) map.get("diamond"); // 用户钻石
        long totalAmount = giftPrice * emNum; // 总价

        UserAccountAttachPO accountAttachPO = new UserAccountAttachPO();
        Map<String, Object> billMap = new HashMap(); // 入账
        accountAttachPO.setId(id);
        if (3 == priceType) {
            long surplusGold = gold - totalAmount;
            if (surplusGold < 0) {
                return Result.error(ErrorCode.NOT_ENOUGH_GOLD);
            }
            accountAttachPO.setGold(surplusGold);
            billMap.put("price", -totalAmount);
            billMap.put("priceType", 3);
        } else if (2 == priceType) {
            long surplusDiamond = diamond - totalAmount;
            if (surplusDiamond < 0) {
                return Result.error(ErrorCode.NOT_ENOUGH_DIAMOND);
            }
            accountAttachPO.setDiamond(surplusDiamond);
            billMap.put("price", -totalAmount);
            billMap.put("priceType", 2);
        } else {
            log.error("GiftServiceImpl.giveEmoticon无效的支付类型, [{}]", priceType);
            return Result.error(ErrorCode.DATA_ERROR);
        }
        accountAttachPO.setId(id);
        Integer update = accountAttachPOMapper.update(accountAttachPO);// 扣除用户钻石或者金币
        if (update > 0) { // 入账
            billMap.put("userId", userId);
            billMap.put("productName", "表情" + receiveUserId + emoticon.getEmName());
            billMap.put("status", 1);
            roomTaskAsync.entryBill(billMap);
        }

        dto.setEmName(emoticon.getEmName());
        dto.setEmUrl(emoticon.getEmUrl());
        return Result.success(dto);
    }

    @Override
    public Result getGiveNumList(Long giftId) {
        if (!Objects.isNull(giftId)) {
            List<Map<String, Object>> map = (List<Map<String, Object>>) redisUtils.get(RedisKey.KEY_GIFT_GIVE_NUM + giftId);
            if (ObjectUtil.isNotEmpty(map)) {
                return Result.success(map);
            }
            List<Map<String, Object>> maps = giveNumMapper.selectGiftGiveNumList(giftId);
            if (ObjectUtil.isNotEmpty(maps)) {
                redisUtils.set(RedisKey.KEY_GIFT_GIVE_NUM + giftId, maps, 2700);
                return Result.success(maps);
            }
        }
        return Result.success(giveNumMapper.selectGiveNumList());
    }

    /**
     * @MethodName: achievementHandler
     * @Description: TODO 魅力值成就进度处理
     * @Param: [roomId]
     * @Return: void
     * @Author: xubin
     * @Date: 17:00 2020/7/14
     **/
    private void achievementHandler(Long userId, Long charmValue) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>() {
            {
                add(new HashMap<String, Object>() {
                    {
                        put("code", AchievementEnum.AMT0049.getCode());
                        put("progress", charmValue);
                        put("isReset", 0);
                    }
                });
                add(new HashMap<String, Object>() {
                    {
                        put("code", AchievementEnum.AMT0050.getCode());
                        put("progress", charmValue);
                        put("isReset", 0);
                    }
                });
            }
        };

        userFeign.achievementHandlers(new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("list", list);
            }
        });
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}
