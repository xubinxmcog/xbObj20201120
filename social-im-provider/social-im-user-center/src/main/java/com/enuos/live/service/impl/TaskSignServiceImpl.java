package com.enuos.live.service.impl;

import com.enuos.live.constant.Constellation;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.manager.AchievementEnum;
import com.enuos.live.mapper.RewardMapper;
import com.enuos.live.mapper.TaskRewardRecordMapper;
import com.enuos.live.mapper.TaskSignMapper;
import com.enuos.live.mapper.TaskSignRecordMapper;
import com.enuos.live.pojo.TaskSign;
import com.enuos.live.result.Result;
import com.enuos.live.service.AchievementService;
import com.enuos.live.service.CurrencyService;
import com.enuos.live.service.RewardService;
import com.enuos.live.service.TaskSignService;
import com.enuos.live.utils.BeanUtils;
import com.enuos.live.utils.BigDecimalUtil;
import com.enuos.live.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 签到任务
 * @Author wangyingjie
 * @Date 9:33 2020/4/9
 * @Modified
 */
@Slf4j
@Service
public class TaskSignServiceImpl implements TaskSignService {

    /**
     * 补签金币
     */
    private static final long BACK_SIGN_GOLD = 100;

    /**
     * 签到基础奖励
     */
    private static final long BASE_SIGN_GOLD = 100;

    /**
     * 签到奖励
     */
    private static Map<String, Map<String, Object>> SIGN_REWARD;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private RewardService rewardService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private RewardMapper rewardMapper;

    @Autowired
    private TaskSignMapper taskSignMapper;

    @Autowired
    private TaskSignRecordMapper taskSignRecordMapper;

    @Autowired
    private TaskRewardRecordMapper taskRewardRecordMapper;

    /**
     * @Description: 签到详情
     * @Param: [taskSign]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/5/21
     */
    @Override
    public Result detail(TaskSign taskSign) {
        if (Objects.isNull(taskSign) || Objects.isNull(taskSign.userId)) {
            return Result.empty();
        }

        Long userId = taskSign.userId;
        LocalDate current = DateUtils.getCurrentDate();

        // 获取星座及该星座的所有日期
        Map<String, Object> map = Constellation.getConstellation();
        List<String> calendar = (List<String>) map.get("calendar");
        String taskCode = MapUtils.getString(map, "taskCode");

        // 获取任务及任务奖励[深拷贝]
        Map<String, Object> trMap = BeanUtils.deepCopyByJson(SIGN_REWARD.get(taskCode), HashMap.class);

        // 任务code规则[taskcode] 签到任务[prefix(year)_taskcode] 领奖code[签到任务[prefix(year)_taskcode_suffix]
        String year = String.valueOf(current.getYear());
        String prefix = year.concat("_");
        String signCode = prefix.concat(taskCode);

        // 获取领奖记录
        List<String> gotCode = taskRewardRecordMapper.getRecordByCode(userId, signCode);

        // 获取签到记录
        LocalDate startDate = DateUtils.getLocalDate(calendar.get(0));
        LocalDate endDate = DateUtils.getLocalDate(calendar.get(calendar.size() - 1));
        List<Map<String, Object>> srList = taskSignRecordMapper.getSignRecordList(userId, startDate, endDate);

        trMap.put("calendar", initSignCalendar(current, calendar, srList));

        // 签到code规则 年_任务_奖励
        Map<String, Object> sMap = taskSignMapper.getSignCount(userId, signCode);

        int sumSignCount = 0;
        if (MapUtils.isNotEmpty(sMap)) {
            // 累计签到次数
            int signCount = MapUtils.getIntValue(sMap, "signCount");
            sumSignCount = signCount;
            // 当前用户签到领奖 isGot 是否获取奖励 [-1 未获得 0 可获得 1 已获得]
            ((List<Map<String, Object>>) trMap.get("signTaskList")).forEach(signTask -> {
                if (gotCode.contains(prefix.concat(MapUtils.getString(signTask, "code")))) {
                    signTask.put("isGot", 1);
                } else {
                    if (signCount >= MapUtils.getIntValue(signTask, "count")) {
                        signTask.put("isGot", 0);
                    }
                }
            });

        }

        trMap.put("signCount", "已累计签到" + sumSignCount + "天");

        return Result.success(trMap);
    }

    /**
     * @Description: 每日签到/补签
     * @Param: [taskSign]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result daySign(TaskSign taskSign) {
        Integer signType = taskSign.getSignType();
        String code = taskSign.getCode();

        taskSign.setCode(String.valueOf(DateUtils.getCurrentDate().getYear()).concat("_").concat(code));

        if (signType == 0) {
            return currentSign(taskSign);
        } else {
            return backSign(taskSign);
        }

    }

    /**
     * @Description: 去补签
     * @Param: [taskSign]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    @Override
    public Result toBackSign(TaskSign taskSign) {
        if (Objects.isNull(taskSign) || Objects.isNull(taskSign.userId) || StringUtils.isBlank(taskSign.getCode())) {
            return Result.empty();
        }

        String code = String.valueOf(DateUtils.getCurrentDate().getYear()).concat("_").concat(taskSign.getCode());

        Map<String, Object> map = taskSignMapper.getSignCount(taskSign.userId, code);
        if (MapUtils.isNotEmpty(map)) {
            map.remove("signCount");
            map.put("gold", (MapUtils.getIntValue(map, "backSignCount") + 1) * BACK_SIGN_GOLD);
        } else {
            map = new HashMap<String, Object>() {
                {
                    put("gold", BACK_SIGN_GOLD);
                    put("continueSignCount", 0);
                    put("backSignCount", 0);
                }
            };
        }

        return Result.success(map);
    }

    /**
     * @Description: 领取奖励
     * @Param: [taskSign]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    @Override
    @Transactional
    public Result toGet(TaskSign taskSign) {
        Long userId = taskSign.userId;
        String code = String.valueOf(LocalDate.now().getYear()).concat("_").concat(taskSign.getCode());

        String[] codes = taskSign.getCode().split("_");

        if (taskRewardRecordMapper.isExists(userId, code) != null) {
            return Result.error(ErrorCode.REWARD_IS_GOT);
        }

        List<Map<String, Object>> list = rewardMapper.getRewardByCode(codes[0], Integer.valueOf(codes[1]));
        if (CollectionUtils.isEmpty(list)) {
            log.error("Can not get reward param[userId:{}, taskCode:{}, suffix:{}]", userId, codes[0], codes[1]);
            return Result.error(ErrorCode.REWARD_NOT_EXISTS);
        }

        rewardService.handler(userId, list);
        taskRewardRecordMapper.save(userId, code);

        return Result.success();
    }

    /**
     * @Description: 当日签到
     * @Param: [taskSign]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    private Result currentSign(TaskSign taskSign) {
        LocalDate currentDate = DateUtils.getCurrentDate();
        Long userId = taskSign.userId;

        TaskSign ts = taskSignMapper.getTaskSign(taskSign);

        // 成就是否需要重置[0 否 1 是]
        int isReset = 0;

        // 初始化签到
        initTaskSign(taskSign);
        taskSign.setSignTime(currentDate);

        // 首签
        if (Objects.isNull(ts)) {
            taskSignMapper.saveTaskSign(taskSign);
        } else {
            LocalDate signTime = ts.getSignTime();
            Integer signCount = ts.getSignCount();
            Integer continueSignCount = ts.getContinueSignCount();
            Integer signType = ts.getSignType();

            // 当天是否签到，当天已经签到不允许签
            if (signTime.isEqual(currentDate)) {
                return Result.error(ErrorCode.SIGN_YET);
            }

            // 计算累计签与连续签
            taskSign.setSignCount(++signCount);
            if (signType == 0 && signTime.until(currentDate).getDays() == 1) {
                taskSign.setContinueSignCount(++continueSignCount);
            } else {
                isReset = 1;
            }

            taskSignMapper.updateTaskSign(taskSign);
        }

        taskSignRecordMapper.saveSignRecord(taskSign);

        // 此处直接领取金币
        Map<String, Object> map = signSuccess(userId, taskSign.getCode());

        // 成就处理
        achievementHandlers(taskSign, isReset);

        return Result.success(map);
    }

    /**
     * @Description: 初始化签到类
     * @Param: [taskSign]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/8/20
     */
    private void initTaskSign(TaskSign taskSign) {
        taskSign.setSignCount(1);
        if (taskSign.getSignType() == 0) {
            taskSign.setContinueSignCount(1);
            taskSign.setBackSignCount(null);
        } else {
            taskSign.setContinueSignCount(null);
            taskSign.setBackSignCount(1);
        }
    }

    /** 
     * @Description: 成就处理
     * @Param: [taskSign, isReset]
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/7/13 
     */
    @Async
    public void achievementHandlers(TaskSign taskSign, int isReset) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>() {
            {
                add(new HashMap<String, Object>() {
                    {
                        put("code", AchievementEnum.AMT0043.getCode());
                        put("progress", 1);
                        put("isReset", isReset);
                    }
                });
                add(new HashMap<String, Object>() {
                    {
                        put("code", AchievementEnum.AMT0044.getCode());
                        put("progress", 1);
                        put("isReset", isReset);
                    }
                });
            }
        };

        achievementService.handlers(new HashMap<String, Object>() {
            {
                put("userId", taskSign.userId);
                put("list", list);
            }
        });
    }

    /**
     * @Description: 补签
     * @Param: [taskSign]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    private Result backSign(TaskSign taskSign) {
        Long userId = taskSign.userId;
        // 时间是否大于等于当前时间
        LocalDate signTime = taskSign.getSignTime();
        if (!signTime.isBefore(DateUtils.getCurrentDate())) {
            return Result.error(ErrorCode.SIGN_BACK_ERROR);
        }

        // 是否有签到
        TaskSign ts = taskSignMapper.getTaskSign(taskSign);

        // 消费金币
        int backSignCount = Objects.isNull(ts) ? 1 : ts.getBackSignCount() + 1;
        long needGold = Math.negateExact(backSignCount * BACK_SIGN_GOLD);
        Result cResult = currencyService.countGold(taskSign.userId, needGold);
        if (cResult.getCode() != 0) {
            throw new RuntimeException(cResult.getMsg());
        }

        // 初始化补签
        initTaskSign(taskSign);

        if (Objects.isNull(ts)) {
            taskSignMapper.saveTaskSign(taskSign);
        } else {
            // 是否有签到或者补签
            if (taskSignRecordMapper.isExists(taskSign) != null) {
                return Result.error(ErrorCode.SIGN_RECORD_EXISTS);
            }

            // 请求补签时间小于签到表的签到时间时，不做时间修改
            if (signTime.isBefore(ts.getSignTime())) {
                taskSign.setSignTime(null);
            }

            // 计算累计签与补签
            Integer signCount = ts.getSignCount();
            taskSign.setSignCount(++signCount);
            taskSign.setBackSignCount(backSignCount);

            taskSignMapper.updateTaskSign(taskSign);
        }

        if (taskSign.getSignTime() == null) {
            taskSign.setSignTime(signTime);
        }

        taskSignRecordMapper.saveSignRecord(taskSign);

        // 此处直接领取金币
        Map<String, Object> map = signSuccess(userId, taskSign.getCode());

        return Result.success(map);
    }

    /**
     * @Description: 签到成功的结果
     * @Param: [userId, code]
     * @Return: java.util.Map<java.lang.String   ,   java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/6/8
     */
    private Map<String, Object> signSuccess(Long userId, String code) {
        Map<String, Object> map = taskSignMapper.getSignCount(userId, code);
        int vip = MapUtils.getIntValue(map, "vip");
        String expirationTime = MapUtils.getString(map, "expirationTime");

        if (vip > 0 && DateUtils.getLocalDateTime(expirationTime).isAfter(DateUtils.getCurrentDateTime())) {
            map.put("isMember", 1);
        } else {
            map.put("isMember", 0);
            map.replace("vipGold", 0L);
        }

        map.remove("signCount");
        map.remove("backSignCount");
        map.put("gold", SIGN_REWARD.get("DST0001").get("number"));

        Result result = currencyService.countGold(userId, BigDecimalUtil.nAdd(BASE_SIGN_GOLD, MapUtils.getLong(map, "vipGold")));
        if (result.getCode() != 0) {
            throw new RuntimeException(result.getMsg());
        }

        return map;
    }

    /**
     * @Description: 获取签到日历
     * @Param: [current, dateList, signRecordList]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/7/22
     */
    private List<Map<String, Object>> initSignCalendar(LocalDate current, List<String> dateList, List<Map<String, Object>> signRecordList) {
        Map srm = signRecordList.stream().collect(Collectors.toMap(k -> k.get("signTime"), v -> v.get("signType"), (key1, key2) -> key1));

        List<String> thisWeekDays = DateUtils.getThisWeekDays();

        List<Map<String, Object>> calendar = new ArrayList<>();

        // signType 签到类型 [-1 不可签 0 正常 1 补签]
        for (String date : dateList) {
            calendar.add(new HashMap<String, Object>() {
                {
                    put("date", date);
                    put("isSign", srm.containsKey(date) ? 1 : 0);
                    // 当前时间以前 补签1 当前时间以后 不可签-1 当前 可签0
                    put("signType", LocalDate.parse(date).isAfter(current) ? -1 : Objects.isNull(srm.get(date)) ? date.equals(current.toString()) ? 0 : 1 : srm.get(date));
                    put("isThisWeek", thisWeekDays.contains(date) ? 1 : 0);
                }
            });
        }

        return calendar;
    }

    /**
     * @Description: 签到奖励初始化
     * @Param: []
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/6/3
     */
    @PostConstruct
    private void initSignReward() {
        log.info("==========[Sign reward init start]");
        SIGN_REWARD = new HashMap<>();

        List<Map<String, Object>> srList = rewardMapper.getSignReward();
        if (CollectionUtils.isEmpty(srList)) {
            throw new RuntimeException("Can not get sign reward");
        }

        List<Map<String, Object>> signDescription = taskSignMapper.getSignDescription();

        srList.forEach(sr -> {
            String key = MapUtils.getString(sr, "taskCode");
            int category = MapUtils.getIntValue(sr, "category");

            if (category == 1) {
                SIGN_REWARD.put(key, sr);
                return;
            }

            if (category == 2 && !SIGN_REWARD.containsKey(key)) {
                String constellation = MapUtils.getString(sr, "title");
                SIGN_REWARD.put(key, new HashMap<String, Object>() {
                    {
                        put("constellation", constellation);
                        put("code", key);
                        put("iconUrl", MapUtils.getString(sr, "iconUrl"));
                        put("backgroundUrl", MapUtils.getString(sr, "backgroundUrl"));

                        List<Map<String, Object>> list = new ArrayList<>();

                        signDescription.forEach(sd -> list.add(new HashMap<String, Object>() {
                            {
                                put("signContinueType", MessageFormat.format(MapUtils.getString(sd, "description"), sd.get("suffix")));
                                put("count", sd.get("suffix"));
                                put("code", key.concat("_").concat(MapUtils.getString(sd, "suffix")));
                                put("isGot", -1);
                                put("signRewardList", new ArrayList<Map<String, Object>>());
                            }
                        }));

                        put("signTaskList", list);
                    }
                });
            }

            Integer count = MapUtils.getInteger(sr, "suffix");
            List<Map<String, Object>> rewardList = (List<Map<String, Object>>) SIGN_REWARD.get(key).get("signTaskList");

            int index;

            switch (count) {
                case 3:
                    index = 0;
                    break;
                case 7:
                    index = 1;
                    break;
                case 12:
                    index = 2;
                    break;
                case 18:
                    index = 3;
                    break;
                case 25:
                    index = 4;
                    break;
                default:
                    index = 0;
                    break;
            }

            ((List<Map<String, Object>>) rewardList.get(index).get("signRewardList")).add(new HashMap<String, Object>() {
                {
                    put("rewardCode", MapUtils.getString(sr, "rewardCode"));
                    put("rewardName", MapUtils.getString(sr, "rewardName"));
                    put("description", MapUtils.getString(sr, "description"));
                    put("categoryId", MapUtils.getInteger(sr, "categoryId"));
                    put("url", MapUtils.getString(sr, "url"));
                    put("life", MapUtils.getLong(sr, "life"));
                    put("number", MapUtils.getString(sr, "number"));
                }
            });
        });

        log.info("==========[Sign reward init end]");
    }

}