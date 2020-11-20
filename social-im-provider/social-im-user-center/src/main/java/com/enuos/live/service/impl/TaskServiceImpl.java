package com.enuos.live.service.impl;

import com.enuos.live.constant.CodeEnum;
import com.enuos.live.constants.RedisKey;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.TaskMapper;
import com.enuos.live.pojo.Task;
import com.enuos.live.pojo.TaskParam;
import com.enuos.live.result.Result;
import com.enuos.live.service.MemberService;
import com.enuos.live.service.RewardService;
import com.enuos.live.service.TaskService;
import com.enuos.live.task.Key;
import com.enuos.live.task.TemplateEnum;
import com.enuos.live.utils.DateUtils;
import com.enuos.live.utils.RedisUtils;
import com.enuos.live.utils.page.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description 任务中心
 * @Author wangyingjie
 * @Date 2020/6/10
 * @Modified
 */
@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    @Value("${task.daily.on-off}")
    private boolean onOff;

    @Autowired
    private RewardService rewardService;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private MemberService memberService;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * @Description: 活跃详情
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/10
     */
    @Override
    public Result active(Long userId) {
        if (Objects.isNull(userId)) {
            return Result.empty();
        }

        // 获取活跃任务
        List<Map<String, Object>> result = taskMapper.getTaskActive("003", TemplateEnum.H01.CODE, TemplateEnum.H02.CODE);
        if (CollectionUtils.isEmpty(result)) {
            return Result.error(ErrorCode.NO_DATA);
        }
        // 任务分组[每日活跃&每周活跃]
        Map<String, List<Map<String, Object>>> map = result.stream().collect(Collectors.groupingBy(m -> MapUtils.getString(m, "code")));

        // redis活跃记录 大小key(redisKey & itemKey)
        int da = 0, wa = 0;
        String rKey = Key.getTaskActive(userId);
        String iKey = DateUtils.getLocalDateOfPattern();
        Map<Object, Object> actMap = redisUtils.getMHash(rKey);
        if (MapUtils.isNotEmpty(actMap)) {
            da = Optional.ofNullable((Integer) actMap.get(iKey)).orElse(0);
            wa = actMap.entrySet().stream().mapToInt(e -> (int) e.getValue()).sum();
        }

        // 领取记录
        String[] templateCodes = {TemplateEnum.H01.CODE, TemplateEnum.H02.CODE};
        List<String> recordList = taskMapper.getRecord(new TaskParam(userId, "003", templateCodes));
        String code, item; int a, line;
        for (Map.Entry<String, List<Map<String, Object>>> entry : map.entrySet()) {
            if (entry.getKey().equals(TemplateEnum.H01.CODE)) {
                a = da;
                code = iKey.concat(".003.");
            } else {
                a = wa;
                code = DateUtils.getLocalDateOfPattern(DateUtils.getThisWeekBegin()).concat(".003.");
            }

            code = code.concat(entry.getKey()).concat(".");

            for (Map<String, Object> m : entry.getValue()) {
                line = MapUtils.getIntValue(m, "line");
                item = code.concat(String.valueOf(line));
                m.put("code", entry.getKey().concat(".").concat(String.valueOf(line)));
                m.put("isGot", recordList.contains(item) ? 1 : a >= line ? 0 : -1);
            }
        }

        result.clear();
        Map dMap = new HashMap();
        dMap.put("code", TemplateEnum.H01.CODE);
        dMap.put("active", da);
        dMap.put("list", map.get(TemplateEnum.H01.CODE));

        Map wMap = new HashMap();
        wMap.put("code", TemplateEnum.H02.CODE);
        wMap.put("active", wa);
        wMap.put("list", map.get(TemplateEnum.H02.CODE));

        result.add(dMap);
        result.add(wMap);

        return Result.success(result);
    }

    /**
     * @Description: 任务列表
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/11
     */
    @Override
    @Transactional
    public Result list(Task task) {
        if (Objects.isNull(task)) {
            return Result.empty();
        }

        Long userId = task.getUserId();

        List<Task> taskList = getDayTask();
        if (CollectionUtils.isEmpty(taskList)) {
            return Result.error(ErrorCode.NO_DATA);
        }

        String rKey = Key.getTaskDay(userId);

        // 是否会员
        Integer isMember = memberService.isMember(userId);
        if (isMember == 1 && !redisUtils.hasHashKey(rKey, TemplateEnum.H03.CODE)) {
            redisUtils.setHash(rKey, TemplateEnum.H03.CODE, 1, 7, TimeUnit.DAYS);
        }

        Map<Object, Object> map = Optional.ofNullable(redisUtils.getMHash(rKey)).orElse(new HashMap<>());
        if (map.containsKey(TemplateEnum.V06.CODE)) {
            map.put(TemplateEnum.V06.CODE, MapUtils.getIntValue(map, TemplateEnum.V06.CODE) / 60);
        }

        // 领取记录
        String prefix = DateUtils.getLocalDateOfPattern();
        String[] codes = new String[taskList.size()];
        codes = taskList.stream().map(Task::getCode).collect(Collectors.toList()).toArray(codes);
        List<String> recordList = taskMapper.getRecord(new TaskParam(userId, prefix,"003", codes));

        String code;
        for (Task t : taskList) {
            t.setUserId(userId);
            code = prefix.concat(".").concat(t.getTaskCode()).concat(".").concat(t.getCode());
            // -1 未达成不可领取 0 可以领取 1 已获得
            t.setIsGot(recordList.contains(code) ?
                    1 : map.containsKey(t.getCode()) && MapUtils.getIntValue(map, t.getCode()) >= t.getSuffix() ?
                    0 : -1);
        }

        return Result.success(new PageInfo<>(taskList));
    }

    /**
     * @Description: 活跃奖励
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/15
     */
    @Override
    public Result activeReward(Task task) {
        if (task == null || StringUtils.isBlank(task.getCode())) {
            return Result.empty();
        }

        // templateCode.suffix
        String[] codes = task.getCode().split("\\.");
        if (codes.length < 2) {
            return Result.error(201, "无效的CODE");
        }
        List<Map<String, Object>> rewardList = taskMapper.getReward(new TaskParam("003", codes[0], Integer.valueOf(codes[1])));

        return Result.success(rewardList);
    }

    /**
     * @Description: 领奖
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result toGet(Task task) {
        if (task == null || StringUtils.isBlank(task.getCode())) {
            return Result.empty();
        }

        Long userId = task.getUserId();
        String code = task.getCode();
        String[] codes = code.split("\\.");

        // 活跃任务
        String prefix = TemplateEnum.H02.CODE.equals(codes[0]) ? DateUtils.getLocalDateOfPattern(DateUtils.getThisWeekBegin()) : DateUtils.getLocalDateOfPattern();
        TaskParam taskParam = new TaskParam(userId, prefix,"003", codes[0]);
        int line, value = 0;
        if (TemplateEnum.H01.CODE.equals(codes[0]) || TemplateEnum.H02.CODE.equals(codes[0])) {
            line = Integer.valueOf(codes[1]);

            String iKey = DateUtils.getLocalDateOfPattern();
            Map<Object, Object> actMap = redisUtils.getMHash(Key.getTaskActive(userId));
            if (MapUtils.isNotEmpty(actMap)) {
                if (TemplateEnum.H01.CODE.equals(codes[0])) {
                    value = Optional.ofNullable((Integer) actMap.get(iKey)).orElse(0);
                } else {
                    value = actMap.entrySet().stream().mapToInt(e -> (int) e.getValue()).sum();
                }
            }

            taskParam.setSuffix(line);
        } else {
            line = taskMapper.getSuffix(taskParam);
            value = Optional.ofNullable((Integer) redisUtils.getHash(Key.getTaskDay(userId), code)).orElse(0);
            if (TemplateEnum.V06.CODE.equals(code)) {
                value = value / 60;
            }
        }

        if (taskMapper.isExistsRecord(taskParam) != null) {
            return Result.error(ErrorCode.REWARD_IS_GOT);
        }

        if (value < line) {
            return Result.error(ErrorCode.REWARD_NO_PERMISSION);
        }

        List<Map<String, Object>> rewardList = taskMapper.getReward(taskParam);
        if (CollectionUtils.isEmpty(rewardList)) {
            return Result.error(ErrorCode.REWARD_NOT_EXISTS);
        }

        rewardService.handler(userId, rewardList);

        taskMapper.saveRecord(taskParam);

        return Result.success();
    }

    /**
     * @Description: 获取每日任务
     * @Param: []
     * @Return: java.util.List<com.enuos.live.pojo.Task>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    private List<Task> getDayTask() {
        String key = RedisKey.KEY_TASK_CENTER.concat(DateUtils.getLocalDateOfPattern());
        if (!redisUtils.hasKey(key)) {
            List<Task> taskList = taskMapper.getList();
            if(onOff) {
                redisUtils.set(key, taskList, CodeEnum.CODE7.getCode(), TimeUnit.DAYS);
                return taskList;
            }

            Map<Integer, List<Task>> groupMap = taskList.stream().collect(Collectors.groupingBy(Task::getGroupId));

            taskList.clear();
            int size;
            for (Map.Entry<Integer, List<Task>> entry : groupMap.entrySet()) {
                size = entry.getValue().size();
                taskList.add(entry.getValue().get(size == 1 ? 0 : RandomUtils.nextInt(size)));
            }

            redisUtils.set(key, taskList, CodeEnum.CODE7.getCode(), TimeUnit.DAYS);
        }

        return (List<Task>) redisUtils.get(key);
    }
}