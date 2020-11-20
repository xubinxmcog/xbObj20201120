package com.enuos.live.server;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.enuos.live.feign.OrderFeign;
import com.enuos.live.feign.UserFeign;
import com.enuos.live.mapper.GiftExtraRewardMapper;
import com.enuos.live.pojo.GiftExtraReward;
import com.enuos.live.pojo.UserTitle;
import com.enuos.live.service.RoomTaskAsync;
import com.enuos.live.utils.method.AliasMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @ClassName RoomLogServer
 * @Description: TODO 送礼物额外奖励处理
 * @Author xubin
 * @Date 2020/11/5
 * @Version V2.0
 **/
@Slf4j
@Component
public class GiftExtraRewardServer {

    @Autowired
    private GiftExtraRewardMapper giftExtraRewardMapper;

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private RoomTaskAsync roomTaskAsync;

    private static final long second = 3162240000L; // 一年(秒)

    private static final long day_second = 86400L; // 一天(秒)

    /**
     * @MethodName: luckDraw
     * @Description: TODO 送礼抽奖励
     * @Param: [giftId:礼物ID, userId:用户ID]
     * @Return: void
     * @Author: xubin
     * @Date: 17:41 2020/11/10
     **/
    public void luckDraw(Long giftId, Long userId) {

        // 查询礼物额外奖励列表
        List<GiftExtraReward> giftExtraRewards = giftExtraRewardMapper.getGiftExtraRewards(giftId);

        if (ObjectUtil.isNotEmpty(giftExtraRewards)) {

            List<Double> probabilityList = new LinkedList<>();

            for (GiftExtraReward giftExtraReward : giftExtraRewards) {
                probabilityList.add(giftExtraReward.getProbability());
            }

            // 根据概率获取奖励 总概率总和要等于1 否则算法不准确
            AliasMethod aliasMethod = new AliasMethod(probabilityList);
            int next = aliasMethod.next(); // 返回奖励列表下标

            // 得到的奖励
            GiftExtraReward giftExtraReward = giftExtraRewards.get(next);

            // 送物额外奖励数量
            Map<String, Object> giftExtraRewardNum = giftExtraRewardMapper.getGiftExtraRewardNum(giftExtraReward.getAwardNumId());
            if (ObjectUtil.isNotEmpty(giftExtraRewardNum)) {
                Integer numMin = MapUtil.getInt(giftExtraRewardNum, "numMin");// 奖励数量的最小值
                Integer numMax = MapUtil.getInt(giftExtraRewardNum, "numMax");// 奖励数量的最大值
                int giftNum = RandomUtil.randomInt(numMin, numMax + 1); // 随机获取奖品数量
                log.info("发放奖励用户ID=[{}], 奖品code=[{}], 数量=[{}]", userId, giftExtraReward.getAwardCode(), giftNum);

                provideReward(userId, giftExtraReward.getAwardCode(), giftExtraReward.getTimeLimit(), giftNum);
            }
        }

    }

    /**
     * @MethodName: provideReward
     * @Description: TODO 发放奖励
     * @Param: [userId: 用户ID, awardCode: 奖品code, timeLimit: 奖品有效期限, giftNum:奖品数量]
     * @Return: void
     * @Author: xubin
     * @Date: 11:18 2020/11/11
     **/
    private void provideReward(Long userId, String awardCode, Integer timeLimit, Integer giftNum) {
        log.info("送礼额外奖励发放, userId=[{}], awardCode=[{}], timeLimit=[{}], giftNum=[{}],", userId, awardCode, timeLimit, giftNum);
        Map<String, Object> param = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>(giftNum);

        long life = timeLimit == 1 ? second : timeLimit * day_second;
        param.put("userId", userId);
        if ("GOLD".equals(awardCode)) {
            log.info("送礼额外奖励发放金币, userId=[{}]", userId);
            // 金币
            Map<String, Object> map = new HashMap<>();
            map.put("rewardCode", awardCode);
            map.put("number", giftNum);
            list.add(map);
            param.put("list", list);
            userFeign.rewardHandler(param);
            Map<String, Object> billMap = new HashMap(); // 入账
            billMap.put("price", giftNum);
            billMap.put("priceType", 3);
            billMap.put("userId", userId);
            billMap.put("productName", "送礼额外奖励");
            billMap.put("status", 1);
            roomTaskAsync.entryBill(billMap);
        } else if (awardCode.startsWith("CH")) {
            log.info("送礼额外奖励发放称号 userId=[{}]", userId);
            // 称号
            upUserTitle(userId, awardCode, life, giftNum);
        } else if (awardCode.startsWith("T")) {
            // 礼物券
            log.info("送礼额外奖励发放礼物券 userId=[{}]", userId);
            for (int i = 0; i < giftNum; i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("rewardCode", awardCode);
                map.put("life", life);
                list.add(map);
            }
            param.put("list", list);
            orderFeign.addBackpack(param);
        } else {
            //  其他礼物或进场特效
            log.info("送礼额外奖励发放其他东西 userId=[{}]", userId);
            Map<String, Object> map = new HashMap<>();
            map.put("rewardCode", awardCode);
            map.put("life", life);
            map.put("number", giftNum);
            list.add(map);
            param.put("list", list);
            orderFeign.addBackpack(param);
        }

    }

    /**
     * @MethodName: upUserTitle
     * @Description: TODO 处理发放称号
     * @Param: [userId:用户ID, awardCode: 奖品code, life:有效期限 [秒], number:数量]
     * @Return: void
     * @Author: xubin
     * @Date: 13:37 2020/11/11
     **/
    public void upUserTitle(Long userId, String awardCode, Long life, Integer number) {
        UserTitle userTitle = giftExtraRewardMapper.getUserTitle(userId, awardCode);
        if (ObjectUtil.isEmpty(userTitle)) {
            Date date = new Date();
            long milliSecond = System.currentTimeMillis() + (life * 1000 * number);
            date.setTime(milliSecond);
            UserTitle title = new UserTitle();
            title.setUserId(userId);
            title.setTitleCode(awardCode);
            title.setExpireTime(date);
            int i = giftExtraRewardMapper.insertUserTitle(title);
            if (i != 1) {
                log.error("发放称号失败, userId=[{}]", userId);
            }
        } else {
            Date expireTime = userTitle.getExpireTime();
            Date date = new Date();
            int compareTo = expireTime.compareTo(date);
            if (compareTo == -1) {
                long milliSecond = System.currentTimeMillis() + (life * 1000 * number);
                date.setTime(milliSecond);
            } else {
                long milliSecond = expireTime.getTime() + (life * 1000 * number);
                date.setTime(milliSecond);
            }
            userTitle.setExpireTime(date);
            int i = giftExtraRewardMapper.updateUserTitle(userTitle);
            if (i != 1) {
                log.error("发放称号更新失败, userId=[{}]", userId);
            }
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            int randomNum = RandomUtil.randomInt(5, 5 + 1);
            System.out.print(randomNum + " ");
        }

    }
}
