package com.enuos.live.service.impl;

import com.enuos.live.constants.RedisKey;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.UserFeign;
import com.enuos.live.mapper.RedPacketsSendMapper;
import com.enuos.live.mapper.UserAccountAttachPOMapper;
import com.enuos.live.pojo.RedPacketsSend;
import com.enuos.live.pojo.RobVO;
import com.enuos.live.pojo.UserAccountAttachPO;
import com.enuos.live.result.Result;
import com.enuos.live.service.RedPacketsService;
import com.enuos.live.service.RoomTaskAsync;
import com.enuos.live.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @ClassName RedPacketsServiceImpl
 * @Description: TODO 红包处理类
 * @Author xubin
 * @Date 2020/6/10
 * @Version V1.0
 **/
@Service
@Slf4j
public class RedPacketsServiceImpl implements RedPacketsService {

    @Autowired
    private RedPacketsSendMapper redPacketsSendMapper;

    @Autowired
    private UserAccountAttachPOMapper accountAttachPOMapper;

    @Autowired
    private RoomTaskAsync taskAsync;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RoomTaskAsync roomTaskAsync;

    @Autowired
    private UserFeign userFeign;

    /**
     * 用户
     */
    private static final String[] USER_COLUMN = {"nickName", "thumbIconUrl", "sex"};


    /**
     * @MethodName: send
     * @Description: TODO  发红包
     * @Param: [redPacketsSend]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/6/10
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result send(RedPacketsSend redPacketsSend) {
        long userId = redPacketsSend.getUserId();
        int rpNum = redPacketsSend.getRpNum(); // 红包总个数
        int totalAmount = redPacketsSend.getTotalAmount();// 总金额
        int reAmount = rpNum * 10;
        if (reAmount > totalAmount) {
            return Result.error(80011, "平均单个红包最少10金币");
        }

        //查询用户金币
        Map<String, Object> map = accountAttachPOMapper.getBalance(null, userId);
        if (map.size() <= 0) {
            log.info("未查询到用户金币和钻石数据userId={}", userId);
            return Result.error(ErrorCode.CONTENT_EMPTY);
        }
        final int id = (Integer) map.get("id"); // 用户账户Id
        final long gold = (Long) map.get("gold"); // 用户金币
        final long diamond = (Long) map.get("diamond"); // 用户钻石
        final long surplusGold = gold - totalAmount;
        if (surplusGold < 0) {
            log.info("用户金币不足userId={}, 金币剩余=[{}]", userId, gold);
            Map map1 = exchangeGold(Math.abs(surplusGold));
            long diamond1 = Long.valueOf(map1.get("diamond").toString());
            Map msg = new HashMap();
            if (diamond1 > diamond) {
                Map map2 = new HashMap();
                msg.put("code", 8001);
                msg.put("msg", "金币不足");
                map2.put("tips", "立即前往商城充值");
                msg.put("data", map2);
            } else {
                map1.put("tips", "是否消耗" + map1.get("diamond") + "钻石兑换" + map1.get("gold") + "金币");
                msg.put("code", 8002);
                msg.put("msg", "金币不足");
                msg.put("data", map1);
            }
            return Result.success(msg);
        }

        // 生成红包
        int max = (totalAmount / rpNum) * 4;
        if (max > totalAmount) {
            max = totalAmount;
        }
        List<Integer> resultList = new ArrayList<>(); // 数组转list存Redis
        if (rpNum > 1) {
            int total = 0;
            int[] result = RedPacketsServiceImpl.generate(totalAmount, rpNum, max, 1);
            for (int i = 0; i < result.length; i++) {
                total += result[i];
                resultList.add(result[i]);
                log.info(result[i] + " "); // 打印生成的红包
            }
            log.info(userId + "红包总金额: " + total + "==" + totalAmount);
        } else {
            resultList.add(totalAmount);
        }

        UserAccountAttachPO accountAttachPO = new UserAccountAttachPO();
        accountAttachPO.setId(id);
        accountAttachPO.setGold(surplusGold);

        redPacketsSend.setRpSurplus(rpNum);
        redPacketsSend.setSurplusAmount(totalAmount);
        redPacketsSendMapper.insert(redPacketsSend);
        accountAttachPOMapper.update(accountAttachPO);
        long time = 24 * 3600;
        String rpKey = RedisKey.KEY_RP + redPacketsSend.getId();
        redisUtils.lSet(rpKey, resultList, time); // 24小时

        Map<String, Object> userMap = userFeign.getUserBase(userId, USER_COLUMN);

        Map<String, Object> billMap = new HashMap(); // 入账参数
        billMap.put("price", -totalAmount);
        billMap.put("priceType", 3);
        billMap.put("userId", userId);
        billMap.put("productName", "发红包");
        billMap.put("status", 1);
        roomTaskAsync.entryBill(billMap);

        String thumbIconUrl = (String) userMap.get("thumbIconUrl");
        String nickName = (String) userMap.get("nickName");
        redPacketsSend.setThumbIconUrl(thumbIconUrl);
        redPacketsSend.setNickName(nickName);
        return Result.success(redPacketsSend);
    }

    /**
     * @param rpId       红包ID
     * @param sendUserId 发红包人ID
     * @param roomId     房间ID
     * @param userId     抢红包人ID
     * @MethodName: rob
     * @Description: TODO 抢红包
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/6/11
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result rob(RobVO robVO) {
        Long sendUserId = robVO.getSendUserId();
        Long roomId = robVO.getRoomId();
        Long userId = robVO.getUserId();
        Long rpId = robVO.getRpId();
        String rpKey = RedisKey.KEY_RP + rpId; // 红包key
        log.info("抢红包: userId=[{}], rpId=[{}], roomId=[{}]", userId, rpId, roomId);
        long expire = redisUtils.getExpire(rpKey); // 红包key过期时间

        String robRpKey = rpKey + "_" + roomId;
        boolean b1 = redisUtils.sHasKey(robRpKey, userId);
        if (b1) {
            return Result.error(2023, "您已经领过该红包");
        }
        redisUtils.sSetAndTime(robRpKey, expire, userId);

        long lSize = redisUtils.lGetListSize(rpKey);
        if (lSize < 1) {
            redisUtils.del(rpKey);
            redisUtils.del(robRpKey);
            return Result.error(200, "很遗憾没有抢到");
        }

        int rp = 0;// 抢到的红包金额
        if (lSize == 1) {
            rp = (int) redisUtils.lGetIndex(rpKey, 0);
            redisUtils.del(rpKey);
            redisUtils.del(robRpKey);
        } else {
            int random = (int) (Math.random() * lSize);//随机数
            rp = (int) redisUtils.lGetIndex(rpKey, random);
            redisUtils.lRemove(rpKey, 1, rp);
        }
        Map<String, Object> userMap = userFeign.getUserBase(sendUserId, USER_COLUMN);
        if (0 < rp) {
            taskAsync.upUserAttach(userId, rp); // 红包金额存入抢红包人的账户
            accountAttachPOMapper.upSurplusAmount(rp, rpId); // 更新剩余红包
            Map<String, Object> billMap = new HashMap(); // 入账
            billMap.put("price", rp);
            billMap.put("priceType", 3);
            billMap.put("userId", userId);
            billMap.put("productName", "红包-" + userMap.get("nickName") + sendUserId + "的");
            billMap.put("status", 1);
            roomTaskAsync.entryBill(billMap);
        }

        userMap.put("rp", rp);
        userMap.put("sendUserId", sendUserId);
        userMap.remove("userId");
        return Result.success(userMap);
    }

    // 红包生成算法测试
    public static void main(String[] args) {
        int totalAmount = 99999;
        int rpNum = 2;
        int max = (totalAmount / rpNum) * 4;
        if (max > totalAmount)
            max = totalAmount;
        int total = 0;
        int[] generate = RedPacketsServiceImpl.generate(totalAmount, rpNum, max, 1);
        for (int i = 0; i < generate.length; i++) {
            total += generate[i];
            System.out.print(generate[i] + " "); // 打印生成的红包
        }
        System.out.println();
        System.out.println("红包总金额: " + total + "==" + totalAmount);
    }

    /**
     * @MethodName: generate
     * @Description: TODO 生成红包
     * @Param: [totalAmount: 红包总金额, rpNum: 红包个数, max: 每个红包最大金额, min:每个红包最小金额]
     * @Return: int[]
     * @Author: xubin
     * @Date: 2020/6/10
     **/
    public static int[] generate(int totalAmount, int rpNum, int max, int min) {
        int[] result = new int[rpNum];

        int average = totalAmount / rpNum;

        int a = average - min;
        int b = max - min;

        int range1 = sqr(average - min);
        int range2 = sqr(max - average);

        for (int i = 0; i < result.length; i++) {
            if (nextInt(min, max) > average) {
                int temp = min + xRandom(min, average);
                result[i] = temp;
                totalAmount -= temp;
            } else {
                int temp = max - xRandom(average, max);
                result[i] = temp;
                totalAmount -= temp;
            }
        }
        // 如果还有余钱，则尝试加到小红包里，如果加不进去，则尝试下一个。
        while (totalAmount > 0) {
            for (int i = 0; i < result.length; i++) {
                if (totalAmount > 0 && result[i] < max) {
                    result[i]++;
                    totalAmount--;
                }
            }
        }
        // 如果钱是负数了，还得从已生成的小红包中抽取回来
        while (totalAmount < 0) {
            for (int i = 0; i < result.length; i++) {
                if (totalAmount < 0 && result[i] > min) {
                    result[i]--;
                    totalAmount++;
                }
            }
        }

        return result;
    }

    static Random random = new Random();

    static {
        random.setSeed(System.currentTimeMillis());
    }

    static int sqrt(int n) {
        return (int) Math.sqrt(n);
    }

    static int sqr(int n) {
        return n * n;
    }

    static int nextInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    /**
     * 生产min和max之间的随机数，但是概率不是平均的，从min到max方向概率逐渐加大。
     * 先平方，然后产生一个平方值范围内的随机数，再开方，这样就产生了一种“膨胀”再“收缩”的效果。
     *
     * @param min
     * @param max
     * @return
     */
    static int xRandom(int min, int max) {
        int sqr = sqr(max - min);
        if (sqr <= 0) {
            sqr = Integer.MAX_VALUE;
        }
        return sqrt(random.nextInt(sqr));
    }

    /**
     * @MethodName: exchangeGold
     * @Description: TODO 计算钻石兑换金币数据
     * @Param: [gold]
     * @Return: java.util.Map
     * @Author: xubin
     * @Date: 13:08 2020/6/22
     **/
    public Map exchangeGold(long gold) {
        double goldNum = gold; // 金币类型转换
        int ratio = accountAttachPOMapper.selectByPrimaryKeyGoldRatio(gold);
        double flag = 0;
        long totalGold = 0;
        int result1 = 0; // 需要的钻石
        flag = Math.ceil(goldNum / ratio); // 向上取整计算
        result1 = (int) flag;
        totalGold = result1 * ratio;

        log.info(result1 + "");
        log.info(totalGold + "");
        Map map = new HashMap();
        map.put("diamond", result1);
        map.put("gold", totalGold);
        return map;
    }

}
