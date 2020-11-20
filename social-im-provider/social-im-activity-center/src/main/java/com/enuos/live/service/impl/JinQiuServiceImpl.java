package com.enuos.live.service.impl;

import com.enuos.live.constants.RedisKey;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.UserFeign;
import com.enuos.live.manager.ActivityEnum;
import com.enuos.live.manager.CurrencyEnum;
import com.enuos.live.mapper.ActivityMapper;
import com.enuos.live.mapper.JinQiuMapper;
import com.enuos.live.mapper.TaskRewardRecordMapper;
import com.enuos.live.pojo.JackpotReward;
import com.enuos.live.pojo.JinQiu;
import com.enuos.live.result.Result;
import com.enuos.live.service.JinQiuService;
import com.enuos.live.utils.ActivityUtils;
import com.enuos.live.utils.DateUtils;
import com.enuos.live.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 金秋送福[ACT0005]
 * @Author wangyingjie
 * @Date 2020/9/23
 * @Modified
 */
@Slf4j
@Service
public class JinQiuServiceImpl implements JinQiuService {

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private JinQiuMapper jinQiuMapper;

    @Autowired
    private TaskRewardRecordMapper taskRewardRecordMapper;

    @Autowired
    private ActivityUtils activityUtils;

    @Autowired
    private RedisUtils redisUtils;
    
    /** 
     * @Description: 详情
     * @Param: [jinQiu] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/9/24 
     */ 
    @Override
    public Result detail(JinQiu jinQiu) {
        if (Objects.isNull(jinQiu)) {
            return Result.empty();
        }

        JinQiu jinqiuDetail = jinQiuMapper.getDetail(ActivityEnum.ACT0005.getCode());
        if (Objects.isNull(jinqiuDetail)) {
            return Result.error(ErrorCode.NO_DATA);
        }

        Result result = activityUtils.isBetween(jinqiuDetail.getStartTime(), jinqiuDetail.getEndTime());
        if (result != null) {
            return result;
        }

        // 获取领奖记录
        List<JackpotReward> list = jinqiuDetail.getList();
        if (CollectionUtils.isEmpty(list)) {
            return Result.empty();
        }

        LocalDate currentDate = DateUtils.getCurrentDate();
        String prefix = String.valueOf(currentDate.getYear()).concat("_");

        List<String> codeList = list.stream().map(jr -> prefix + ActivityEnum.ACT0005.getCode() + "_" + jr.getSequence()).collect(Collectors.toList());
        List<Map<String, Object>> recordList = jinQiuMapper.getRewardRecord(jinQiu.userId, codeList);

        List<Integer> suffixList = new ArrayList<>();
        List<String> timeList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(recordList)) {
            suffixList = recordList.stream().map(record -> Integer.parseInt(StringUtils.substringAfterLast(MapUtils.getString(record, "code"), "_"))).collect(Collectors.toList());
            timeList = recordList.stream().map(record -> MapUtils.getString(record, "createTime")).collect(Collectors.toList());
        }

        boolean noToday = !timeList.contains(currentDate.toString());

        // 定义计数器
        // isGot -1 未完成, 0 可完成, 1 已完成
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            if (suffixList.contains(list.get(i).getSequence())) {
                list.get(i).setIsGot(1);
            } else {
                count++;
                // 在未完成和已完成中间有只有一个可完成, 且今日签到不存在
                list.get(i).setIsGot(count == 1 && noToday ? 0 : -1);
            }
        }

        return Result.success(jinqiuDetail);
    }

    /** 
     * @Description: 领奖
     * @Param: [params]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/9/25 
     */ 
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result toGet(Map<String, Object> params) {
        if (MapUtils.isEmpty(params)) {
            return Result.empty();
        }

        Long userId = MapUtils.getLong(params, "userId");
        Integer sequence = MapUtils.getInteger(params, "sequence");

        LocalDate current = DateUtils.getCurrentDate();
        String code = new StringBuilder(String.valueOf(current.getYear())).append("_").append(ActivityEnum.ACT0005.getCode()).append("_").append(sequence).toString();

        if (taskRewardRecordMapper.isExists(userId, code) != null) {
            return Result.error(ErrorCode.REWARD_IS_GOT);
        }

        taskRewardRecordMapper.save(userId, code);

        List<Map<String, Object>> rewardList = activityMapper.getRewardListByCodeAndSuffix(ActivityEnum.ACT0005.getCode(), sequence);

        int propNum = rewardList.stream().filter(m -> StringUtils.equals(MapUtils.getString(m, "rewardCode"), CurrencyEnum.PROP001.CODE)).mapToInt(m -> MapUtils.getIntValue(m, "number")).sum();
        if (propNum != 0) {
            String key = String.valueOf(userId);
            int currency = (int) Optional.ofNullable(redisUtils.getHash(RedisKey.KEY_ACT0001_USER_CURRENCY, key)).orElse(0);
            currency += propNum;
            redisUtils.setHash(RedisKey.KEY_ACT0001_USER_CURRENCY, key, currency);
        }

        rewardList.removeIf(m -> StringUtils.equals(MapUtils.getString(m, "rewardCode"), CurrencyEnum.PROP001.CODE));
        if (CollectionUtils.isEmpty(rewardList)) {
            return Result.success();
        }

        Result result = userFeign.rewardHandler(new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("list", rewardList);
            }
        });

        if (result.getCode() != 0) {
            throw new RuntimeException(result.getMsg());
        }

        return Result.success();
    }
}
