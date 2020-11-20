package com.enuos.live.service.impl;

import com.enuos.live.constants.RedisKey;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.UserFeign;
import com.enuos.live.manager.ActivityEnum;
import com.enuos.live.manager.CurrencyEnum;
import com.enuos.live.mapper.ActivityMapper;
import com.enuos.live.mapper.QiuRiMapper;
import com.enuos.live.mapper.TaskFollowMapper;
import com.enuos.live.mapper.TaskRewardRecordMapper;
import com.enuos.live.pojo.Jackpot;
import com.enuos.live.pojo.JackpotReward;
import com.enuos.live.pojo.QiuRi;
import com.enuos.live.pojo.QiuRiTask;
import com.enuos.live.result.Result;
import com.enuos.live.service.QiuRiService;
import com.enuos.live.utils.ActivityUtils;
import com.enuos.live.utils.DateUtils;
import com.enuos.live.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 丹枫迎秋[ACT0001]
 * @Author wangyingjie
 * @Date 2020/9/17
 * @Modified
 */
@Slf4j
@Service
public class QiuRiServiceImpl implements QiuRiService {

    private static final int[] ARRAY_1 = {1, 2, 3};
    private static final int[] ARRAY_2 = {4, 5, 6};
    private static final int[] ARRAY_3 = {7, 8, 9};

    @Autowired
    private ActivityUtils activityUtils;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private QiuRiMapper qiuRiMapper;

    @Autowired
    private TaskRewardRecordMapper taskRewardRecordMapper;

    @Autowired
    private TaskFollowMapper taskFollowMapper;

    /**
     * @Description: 详情
     * @Param: [qiuRi]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    @Override
    public Result detail(QiuRi qiuRi) {
        QiuRi qiuriDetail = qiuRiMapper.getQiuRiByCode(ActivityEnum.ACT0001.getCode());
        Result result = activityUtils.isBetween(qiuriDetail.getStartTime(), qiuriDetail.getEndTime());
        if (result != null) {
            return result;
        }

        Long userId = qiuRi.userId;
        String key = String.valueOf(userId);
        // 初始化redis数据[奖池，计数，奖章]
        initRedis(key);

        qiuriDetail.setJackpot((Jackpot) redisUtils.getHash(RedisKey.KEY_ACT0001_USER_JACKPOT, key));
        qiuriDetail.setCount((Integer) redisUtils.getHash(RedisKey.KEY_ACT0001_USER_COUNT, key));
        qiuriDetail.setCurrent((Integer) redisUtils.getHash(RedisKey.KEY_ACT0001_USER_CURRENCY, key));

        String prefix = DateUtils.getPrefixOfCurrentDate();
        List<String> codeList = qiuriDetail.getTaskList().stream().map(a -> prefix.concat(a.getCode())).collect(Collectors.toList());

        List<String> recordList = activityMapper.getRecordList(userId, codeList);
        List<Map<String, Integer>> progressList = activityMapper.getProgressList(userId, codeList);

        Map<String, Integer> progressMap = CollectionUtils.isNotEmpty(progressList) ? progressList.stream().collect(Collectors.toMap(k -> MapUtils.getString(k, "code"), v -> MapUtils.getInteger(v, "progress"), (k1, k2) -> k1)) : Collections.EMPTY_MAP;

        String code;
        int limit;
        for (QiuRiTask qiuRiTask : qiuriDetail.getTaskList()) {
            code = prefix.concat(qiuRiTask.getCode());
            limit = Integer.valueOf(StringUtils.substringAfterLast(qiuRiTask.getCode(), "_"));
            if (CollectionUtils.isNotEmpty(recordList) && recordList.contains(code)) {
                qiuRiTask.setIsGot(1);
            } else {
                if (ActivityEnum.ACT000106.getCode().equals(qiuRiTask.getCode())) {

                    Integer isMember = userFeign.isMember(userId);
                    if (isMember == null) {
                        return Result.error(ErrorCode.NETWORK_ERROR);
                    }

                    qiuRiTask.setIsGot(isMember > 0 ? 0 : -1);
                } else {
                    qiuRiTask.setIsGot(progressMap.containsKey(code) && progressMap.get(code) >= limit ? 0 : -1);
                }
            }
        }

        return Result.success(qiuriDetail);
    }

    /**
     * @Description: 领奖
     * @Param: [qiuRi]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/14
     */
    @Override
    public Result toGet(QiuRi qiuRi) {
        Long userId = qiuRi.userId;
        String key = String.valueOf(userId);
        String code = DateUtils.getPrefixOfCurrentDate().concat(qiuRi.getCode());

        if (!ActivityEnum.ACT000108.getCode().equals(qiuRi.getCode())) {
            if (taskRewardRecordMapper.isExists(userId, code) != null) {
                return Result.error(ErrorCode.REWARD_IS_GOT);
            }

            taskRewardRecordMapper.save(userId, code);
        }

        Map<String, Object> reward = activityMapper.getRewardByCode(StringUtils.substringBeforeLast(qiuRi.getCode(), "_"));
        if (MapUtils.isEmpty(reward)) {
            return Result.error(ErrorCode.NO_DATA);
        }

        int currency = (Integer) redisUtils.getHash(RedisKey.KEY_ACT0001_USER_CURRENCY, key);
        if (ActivityEnum.ACT000108.getCode().equals(qiuRi.getCode())) {
            currency += qiuRi.getProgress() * MapUtils.getIntValue(reward, "number");
        } else {
            currency += MapUtils.getIntValue(reward, "number");
        }

        redisUtils.setHash(RedisKey.KEY_ACT0001_USER_CURRENCY, key, currency);

        return Result.success();
    }

    /**
     * @Description: 任务处理
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/14
     */
    @Override
    public Result handler(Map<String, Object> params) {
        if (MapUtils.isEmpty(params) || StringUtils.isEmpty(MapUtils.getString(params, "code"))) {
            return Result.empty();
        }

        QiuRi qiuriDetail = qiuRiMapper.getQiuRiByCode(ActivityEnum.ACT0001.getCode());
        Result result = activityUtils.isBetween(qiuriDetail.getStartTime(), qiuriDetail.getEndTime());
        if (result != null) {
            return result;
        }

        Long userId = MapUtils.getLong(params, "userId");
        String code = MapUtils.getString(params, "code");
        int progress = MapUtils.getIntValue(params, "progress");
        int line = Integer.parseInt(StringUtils.substringAfterLast(code, "_"));

        // 钻石可无限兑换
        if (ActivityEnum.ACT000108.getCode().equals(code)) {
            long diamond = -15L * progress;
            result = userFeign.countDiamond(new HashMap<String, Long>() {
                {
                    put("userId", userId);
                    put("diamond", diamond);
                }
            });

            if (result.getCode() != 0) {
                return result;
            }

            result = toGet(new QiuRi(userId, code, progress));

            if (result.getCode() != 0) {
                throw new RuntimeException(result.getMsg());
            } else {
                return result;
            }
        }

        // 金币兑换
        if (ActivityEnum.ACT000107.getCode().equals(code)) {
            result = userFeign.countGold(new HashMap<String, Long>() {
                {
                    put("userId", userId);
                    put("gold", -2000L);
                }
            });

            if (result.getCode() != 0) {
                return result;
            }

            result = toGet(new QiuRi(userId, code));

            if (result.getCode() != 0) {
                throw new RuntimeException(result.getMsg());
            }
        }

        // 是否存在该进度
        params.put("code", DateUtils.getPrefixOfCurrentDate().concat(code));
        Map<String, Object> followMap = taskFollowMapper.getTaskFollow(params);
        if (MapUtils.isEmpty(followMap)) {
            // 记录成就
            taskFollowMapper.save(params);
        } else {
            // 已经达成的任务不再记录
            int currentProgress = MapUtils.getIntValue(followMap, "progress");
            if (currentProgress < line) {
                params.put("progress", progress > line ? line : progress);
                taskFollowMapper.updateProgress(params);
            }
        }

        return Result.success();
    }

    /**
     * @Description: 丹枫迎秋选牌
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/13
     */
    @Override
    @Transactional
    public Result choose(Map<String, Object> params) {
        if (MapUtils.isEmpty(params)) {
            return Result.empty();
        }

        Long userId = MapUtils.getLong(params, "userId");
        String key = String.valueOf(userId);
        Integer index = MapUtils.getInteger(params, "index");

        int currency = (Integer) redisUtils.getHash(RedisKey.KEY_ACT0001_USER_CURRENCY, key);
        if (--currency < 0) {
            return Result.error(ErrorCode.NOT_ENOUGH_CURRENCY_ACT0001);
        }

        Jackpot userJackpot = (Jackpot) redisUtils.getHash(RedisKey.KEY_ACT0001_USER_JACKPOT, key);
        if (index > userJackpot.getList().size() - 1) {
            return Result.error(ErrorCode.DATA_ERROR);
        }
        if (userJackpot.getList().get(index).getIsGot() == 1) {
            return Result.error(ErrorCode.REWARD_IS_GOT);
        }

        // 轮数
        int count = (Integer) redisUtils.getHash(RedisKey.KEY_ACT0001_USER_COUNT, key);

        // 该轮奖池
        List<JackpotReward> list = ((Jackpot) redisUtils.getHash(RedisKey.KEY_ACT0001_JACKPOT, getJackpotId(count))).getList();

        // 用户奖池，已获得的奖励序号
        List<JackpotReward> jackpotRewardList = userJackpot.getList();
        List<Integer> sequenceList = jackpotRewardList.stream().filter(a -> a.getSequence() != null).map(JackpotReward::getSequence).collect(Collectors.toList());

        // 本轮抽奖次数
        int time = sequenceList.size();
        time++;

        // 9个奖品，前3次从前3个奖励中抽取，依次类推
        // 需求如此请@产品
        int[] random;
        if (time >= 1 && time <= 3) {
            random = Arrays.stream(ARRAY_1).filter(a -> !sequenceList.contains(a)).toArray();
        } else if (time >= 4 && time <= 6) {
            random = Arrays.stream(ARRAY_2).filter(a -> !sequenceList.contains(a)).toArray();
        } else {
            random = Arrays.stream(ARRAY_3).filter(a -> !sequenceList.contains(a)).toArray();
        }

        // 获取奖励的序号
        int sequence = random[RandomUtils.nextInt(random.length)];
        // 序号拿奖励
        JackpotReward jackpotReward = list.stream().filter(a -> sequence == a.getSequence()).findFirst().get();

        // 若该轮奖励抽完则刷新下一轮奖励
        if (time == 9) {
            redisUtils.setHash(RedisKey.KEY_ACT0001_USER_COUNT, key, ++count);
            userJackpot = (Jackpot) redisUtils.getHash(RedisKey.KEY_ACT0001_JACKPOT, getJackpotId(count));
            userJackpot.getList().forEach(a -> {
                a.setSequence(null);
                a.getReward().clear();
            });
            redisUtils.setHash(RedisKey.KEY_ACT0001_USER_JACKPOT, key, userJackpot);
        } else {
            jackpotReward.setIsGot(1);
            userJackpot.getList().set(index, jackpotReward);
            redisUtils.setHash(RedisKey.KEY_ACT0001_USER_JACKPOT, key, userJackpot);
        }

        // 奖励处理
        Map<String, Object> rewardMap = jackpotReward.getReward();
        if (StringUtils.equals(MapUtils.getString(rewardMap, "rewardCode"), CurrencyEnum.PROP001.CODE)) {
            currency += MapUtils.getInteger(rewardMap, "number");
        } else {
            userFeign.rewardHandler(new HashMap<String, Object>() {
                {
                    put("userId", userId);
                    put("list", new ArrayList<Map<String, Object>>() {
                        {
                            add(rewardMap);
                        }
                    });
                }
            });
        }

        redisUtils.setHash(RedisKey.KEY_ACT0001_USER_CURRENCY, key, currency);

        // 选牌次数
        rewardMap.put("time", time);

        return Result.success(rewardMap);
    }

    /**
     * @Description: 初始化丹枫迎秋奖池
     * @Param: [jackpot]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/13
     */
    @Override
    public Result initJackpot(Jackpot jackpot) {
        if (jackpot == null) {
            return Result.empty();
        }

        redisUtils.setHash(RedisKey.KEY_ACT0001_JACKPOT, String.valueOf(jackpot.getId()), jackpot);

        return Result.success();
    }

    /**
     * @Description: 获取奖池配置
     * @Param: [jackpot]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/20
     */
    @Override
    public Result getJackpot(Jackpot jackpot) {
        if (Objects.isNull(jackpot) || Objects.isNull(jackpot.getId())) {
            Map<Object, Object> map = redisUtils.getMHash(RedisKey.KEY_ACT0001_JACKPOT);
            List<Jackpot> jackpotList = new ArrayList<>();

            if (MapUtils.isEmpty(map)) {
                return Result.success(jackpotList);
            }

            map.entrySet().forEach(m -> jackpotList.add((Jackpot) m.getValue()));
            return Result.success(jackpotList.stream().sorted(Comparator.comparing(Jackpot::getId)).collect(Collectors.toList()));
        }

        if (!redisUtils.hasHashKey(RedisKey.KEY_ACT0001_JACKPOT, String.valueOf(jackpot.getId()))) {
            return Result.error(ErrorCode.NO_DATA);
        }

        return Result.success(redisUtils.getHash(RedisKey.KEY_ACT0001_JACKPOT, String.valueOf(jackpot.getId())));
    }

    /**
     * @Description: 获取奖池id
     * @Param: [count]
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/8/13
     */
    private String getJackpotId(int count) {
        String id;
        if (count <= 6) {
            switch (count) {
                case 1:
                    id = "1";
                    break;
                case 2:
                    id = "2";
                    break;
                case 3:
                    id = "1";
                    break;
                case 4:
                    id = "2";
                    break;
                case 5:
                    id = "3";
                    break;
                case 6:
                    id = "4";
                    break;
                default:
                    id = "";
                    break;
            }
        } else {
            id = count % 2 == 0 ? "6" : "5";
        }

        return id;
    }

    /**
     * @Description: 初始化redis
     * @Param: [key]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/8/13
     */
    private void initRedis(String key) {
        // 初始化奖池
        if (!redisUtils.hasHashKey(RedisKey.KEY_ACT0001_USER_JACKPOT, key)) {
            Jackpot jackpot = (Jackpot) redisUtils.getHash(RedisKey.KEY_ACT0001_JACKPOT, "1");
            if (Objects.isNull(jackpot) || CollectionUtils.isEmpty(jackpot.getList())) {
                log.error("==========[No jackpot]");
                return;
            }
            jackpot.getList().forEach(jackpotReward -> {
                jackpotReward.setSequence(null);
                jackpotReward.getReward().clear();
            });
            redisUtils.setHash(RedisKey.KEY_ACT0001_USER_JACKPOT, key, jackpot);
        }

        // 初始化奖池计数
        if (!redisUtils.hasHashKey(RedisKey.KEY_ACT0001_USER_COUNT, key)) {
            redisUtils.setHash(RedisKey.KEY_ACT0001_USER_COUNT, key, 1);
        }

        // 初始化奖章
        if (!redisUtils.hasHashKey(RedisKey.KEY_ACT0001_USER_CURRENCY, key)) {
            redisUtils.setHash(RedisKey.KEY_ACT0001_USER_CURRENCY, key, 0);
        }
    }

}
