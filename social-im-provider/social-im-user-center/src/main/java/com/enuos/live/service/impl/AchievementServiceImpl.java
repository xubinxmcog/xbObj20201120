package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.manager.AchievementEnum;
import com.enuos.live.mapper.AchievementMapper;
import com.enuos.live.mapper.RewardMapper;
import com.enuos.live.mapper.TaskFollowMapper;
import com.enuos.live.mapper.TaskRewardRecordMapper;
import com.enuos.live.pojo.Achievement;
import com.enuos.live.pojo.Task;
import com.enuos.live.result.Result;
import com.enuos.live.service.AchievementService;
import com.enuos.live.service.RewardService;
import com.enuos.live.utils.page.PageInfo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 成就
 * @Author wangyingjie
 * @Date 2020/6/16
 * @Modified
 */
@Slf4j
@Service
public class AchievementServiceImpl implements AchievementService {

    @Autowired
    private RewardService rewardService;

    @Autowired
    private RewardMapper rewardMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private TaskFollowMapper taskFollowMapper;

    @Autowired
    private TaskRewardRecordMapper taskRewardRecordMapper;

    /**
     * @Description: 达成的成就数
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/17
     */
    @Override
    public Result num(Task task) {
        if (task == null) {
            return Result.empty();
        }

        Map<String, Integer> numMap = taskRewardRecordMapper.getRecordNumByPrefix(task.getUserId(), AchievementEnum.AMT.getCode());

        return Result.success(numMap);
    }

    /**
     * @Description: 成就列表
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/16
     */
    @Override
    public Result list(Task task) {
        if (task == null) {
            return Result.empty();
        }

        Long userId = task.getUserId();

        PageHelper.startPage(task.pageNum, task.pageSize);

        List<Integer> typeList = achievementMapper.getType();
        PageInfo pageInfo = new PageInfo<>(typeList);

        List<Achievement> achievementList = achievementMapper.getAchievementList(userId, typeList);
        if (CollectionUtils.isEmpty(achievementList)) {
            return Result.error(ErrorCode.NO_DATA);
        }

        // 获取进度
        List<Map<String, Object>> progressList = taskFollowMapper.getProgress(userId, AchievementEnum.AMT.getCode());
        Map<String, Integer> progressMap = progressList.stream().collect(Collectors.toMap(k -> MapUtils.getString(k, "code"), v -> MapUtils.getInteger(v, "progress"), (k1, k2) -> k1));

        // 获取领奖记录
        List<String> recordList = taskRewardRecordMapper.getRecordByPrefix(userId, AchievementEnum.AMT.getCode());

        achievementList.forEach(a -> {
            String code = a.getCode();
            int line = a.getLine();

            a.setProgress(progressMap.containsKey(code) ? progressMap.get(code) : 0);
            // 是否获取[-1 未获得 0 可获得 1 已获得]
            a.setIsGot(recordList.contains(code) ? 1 : progressMap.containsKey(code) && progressMap.get(code) >= line ? 0 : -1);
        });

        TreeMap<Integer, List<Achievement>> achievementMap = achievementList.stream().collect(Collectors.groupingBy(Achievement::getType, TreeMap::new, Collectors.toList()));
        List<List<Achievement>> resultList = new ArrayList<>();
        resultList.addAll(achievementMap.values());
        pageInfo.setList(resultList);
        return Result.success(pageInfo);
    }

    /**
     * @Description: 获取成就奖励
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/17
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result toGet(Task task) {
        Long userId = task.getUserId();
        String code = task.getCode();
        String[] codes = code.split("_");

        if (taskRewardRecordMapper.isExists(userId, code) != null) {
            return Result.error(ErrorCode.REWARD_IS_GOT);
        }

        List<Map<String, Object>> list = rewardMapper.getRewardByCode(codes[0], Integer.valueOf(codes[1]));
        if (CollectionUtils.isEmpty(list)) {
            log.error("Can not get reward param[userId:{}, taskCode:{}, suffix:{}]", userId, codes[0], codes[1]);
            return Result.error(ErrorCode.REWARD_NOT_EXISTS);
        }

        Result result = rewardService.handler(userId, list);
        if (result.getCode() == 0) {
            taskRewardRecordMapper.save(userId, code);
            return Result.success();
        } else {
            return Result.error(ErrorCode.REWARD_FAIL_GOT);
        }
    }

    /**
     * @Description: 成就进度统一处理
     * @Param: [params] 
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/7/13 
     */ 
    @Override
    @Transactional
    public void handler(Map<String, Object> params) {
        log.info("==========[Achievement handler start]==========");
        if (MapUtils.isEmpty(params)) {
            log.error("Params is empty");
            return;
        }

        Long userId = MapUtils.getLong(params, "userId");
        String code = MapUtils.getString(params, "code");
        int progress = MapUtils.getIntValue(params, "progress");
        // 是否重置成就进度[0 否 1 是]
        int isReset = MapUtils.getIntValue(params, "isReset");

        if (userId == null || code == null) {
            log.error("Params is null");
            return;
        }

        int line = Integer.parseInt(StringUtils.substringAfterLast(code, "_"));

        // 查询该成就是否已获得
        Map<String, Object> followMap = taskFollowMapper.getTaskFollow(params);
        if (MapUtils.isEmpty(followMap)) {
            // 记录成就
            taskFollowMapper.save(params);
        } else {
            // 已经达成的成就不再记录
            int currentProgress = MapUtils.getIntValue(followMap, "progress");
            if (currentProgress < line) {
                if (isReset == 0) {
                    progress += currentProgress;
                }
                params.put("progress", progress > line ? line : progress);

                taskFollowMapper.updateProgress(params);
            }
        }

        log.info("==========[Achievement handler end]==========");
    }

    /**
     * @Description: 成就进度统一处理
     * @Param: [params]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/13
     */
    @Override
    @Transactional
    public void handlers(Map<String, Object> params) {
        log.info("==========[Achievement handlers start]==========");

        if (MapUtils.isEmpty(params)) {
            log.error("Params is empty");
            return;
        }

        long userId = MapUtils.getLong(params, "userId");
        List<Map<String, Object>> list = (List<Map<String, Object>>) params.get("list");
        if (CollectionUtils.isEmpty(list)) {
            log.error("List is empty");
            return;
        }

        int line, progress, currentProgress;
        List<Map<String, Object>> followList = taskFollowMapper.getAchievementFollowList(userId, list);
        if (CollectionUtils.isEmpty(followList)) {
            for (Map map : list) {
                line = Integer.parseInt(StringUtils.substringAfterLast(MapUtils.getString(map, "code"), "_"));
                progress = MapUtils.getIntValue(map, "progress");
                map.put("progress", progress > line ? line : progress);
            }

            taskFollowMapper.batchSave(userId, list);
        } else {
            Map<String, Integer> followMap = followList.stream().collect(Collectors.toMap(k -> MapUtils.getString(k, "code"), v -> MapUtils.getInteger(v, "progress"), (k1, k2) -> k1));
            // 移除已经达成的成就
            list.removeIf(map -> Optional.ofNullable(followMap.get(MapUtils.getString(map, "code"))).orElse(0) >= Integer.parseInt(StringUtils.substringAfterLast(MapUtils.getString(map, "code"), "_")));

            if (CollectionUtils.isEmpty(list)) {
                log.info("Achievements reached");
                return;
            }

            for (Map map : list) {
                line = Integer.parseInt(StringUtils.substringAfterLast(MapUtils.getString(map, "code"), "_"));
                progress = MapUtils.getIntValue(map, "progress");
                currentProgress = followMap.get(MapUtils.getString(map, "code"));
                if (MapUtils.getIntValue(map, "isReset") == 0) {
                    progress += currentProgress;
                }
                map.put("progress", progress > line ? line : progress);
            }

            taskFollowMapper.batchUpdateProgress(userId, list);
        }

        log.info("==========[Achievement handlers end]==========");
    }

}