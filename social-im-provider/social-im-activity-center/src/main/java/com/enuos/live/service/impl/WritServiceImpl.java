package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.OrderFeign;
import com.enuos.live.manager.WritEnum;
import com.enuos.live.mapper.WritMapper;
import com.enuos.live.pojo.*;
import com.enuos.live.result.Result;
import com.enuos.live.service.CurrencyService;
import com.enuos.live.service.WritService;
import com.enuos.live.task.Key;
import com.enuos.live.task.TemplateEnum;
import com.enuos.live.utils.BeanUtils;
import com.enuos.live.utils.DateUtils;
import com.enuos.live.utils.RedisUtils;
import com.enuos.live.utils.method.DiscreteMethod;
import com.enuos.live.utils.page.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 乐享令状
 * @Author wangyingjie
 * @Date 2020/10/10
 * @Modified
 */
@Slf4j
@Service
public class WritServiceImpl implements WritService {

    private static final Map<String, Object> LEVEL_PRICE;

    private static final List<Map<String, Object>> STEP_PRICE;

    static {
        LEVEL_PRICE = new HashMap<String, Object>() {
            {
                put("levelPrice", WritEnum.LP.VALUE);
            }
        };

        STEP_PRICE = new ArrayList<Map<String, Object>>() {
            {
                add(new HashMap<String, Object>() {
                    {
                        put("step", 1);
                        put("stepPrice", WritEnum.SA.VALUE);
                    }
                });
                add(new HashMap<String, Object>() {
                    {
                        put("step", 2);
                        put("stepPrice", WritEnum.SB.VALUE);
                    }
                });
            }
        };
    }

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private WritMapper writMapper;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * @Description: 等级
     * @Param: [writ]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/26
     */
    @Override
    @Transactional
    public Result level(Writ writ) {
        if (Objects.isNull(writ) || Objects.isNull(writ.userId)) {
            return Result.empty();
        }

        Long userId = writ.userId;
        String taskCode = getTaskCode();
        if (StringUtils.isBlank(taskCode)) {
            return Result.error(ErrorCode.ACTIVITY_NOT_EXISTS);
        }

        // 获取本期令状活动用户信息
        WritUser writUser = writMapper.getWritUser(taskCode, TemplateEnum.A04.CODE, userId);
        if (Objects.isNull(writUser)) {
            writMapper.initWritUser(taskCode, userId);
            writUser = writMapper.getWritUser(taskCode, TemplateEnum.A04.CODE, userId);

            // 1级奖励领取
            List<Map<String, Object>> list = writMapper.getLevelRewardOfList(taskCode, TemplateEnum.D01.CODE, 1, 1);
            // 奖励入用户背包
            addBackpack(userId, list);
            // 保存领取记录，记录编码[令状编码_类别_类型_等级]
            writMapper.saveRecord(new WritRecord(userId, taskCode, TemplateEnum.D01.CODE, 1, 1));
        }

        // 设置等级积分
        Integer integralLine = WritEnum.LI.VALUE;
        if (writUser.getIntegral() >= integralLine) {
            writUser.setIntegral(writUser.getIntegral() % integralLine);
        }

        writUser.setLine(integralLine);

        return Result.success(writUser);
    }

    /**
     * @Description: 列表
     * @Param: [writ:[type 1 奖励 2 任务 3 兑换 4 排行]]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/26
     */
    @Override
    public Result list(Writ writ) {
        if (Objects.isNull(writ) || Objects.isNull(writ.userId) || Objects.isNull(writ.getType())) {
            return Result.empty();
        }

        String taskCode = getTaskCode();
        if (StringUtils.isBlank(taskCode)) {
            return Result.error(ErrorCode.ACTIVITY_NOT_EXISTS);
        }

        writ.setTaskCode(taskCode);

        switch (writ.getType()) {
            case 1:
                return rewardList(writ);
            case 2:
                return taskList(writ);
            case 3:
                return exchangeList(writ);
            case 4:
                return rankList(writ);
            default:
                return Result.empty();
        }
    }

    /**
     * @Description: 奖励列表
     * @Param: [writ]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/10
     */
    Result rewardList(Writ writ) {
        if (Objects.isNull(writ.pageNum) || Objects.isNull(writ.pageSize)) {
            return Result.empty();
        }

        Long userId = writ.userId;
        int pageNum = writ.pageNum;
        int pageSize = writ.pageSize;
        String taskCode = writ.getTaskCode();

        // 获取当前等级
        WritUser writUser = writMapper.getWritUser(taskCode, TemplateEnum.A04.CODE, userId);
        Integer level = writUser.getLevel();
        Integer step = writUser.getStep();

        List<Integer> levelList = getLevelOfPage(writ.pageNum, writ.pageSize);
        if (CollectionUtils.isEmpty(levelList)) {
            return Result.success(ErrorCode.NO_DATA);
        }
        
        // 获取所有奖励
        List<Reward> rewardList = writMapper.getRewardByLevel(taskCode, TemplateEnum.D01.CODE, levelList);
        if (CollectionUtils.isEmpty(rewardList)) {
            rewardList = writMapper.getRewardByLevel(taskCode, TemplateEnum.D01.CODE, null);
        }
        TreeMap<Integer, List<Reward>> rewardMap = rewardList.stream().collect(Collectors.groupingBy(Reward::getLevel, TreeMap::new, Collectors.toList()));

        // 获取领奖记录
        List<String> recordList = writMapper.getRecord(taskCode, TemplateEnum.D01.CODE, userId);

        List<WritReward> writRewardList = new ArrayList<>();
        Reward tr; String tc;
        for (Integer i : levelList) {
            WritReward writReward = new WritReward();
            writReward.setLevel(i);
            rewardList = Optional.ofNullable(rewardMap.get(i)).orElse(rewardMap.get(81));
            for (Reward reward : rewardList) {
                tr = BeanUtils.deepCopyByJson(reward, Reward.class);
                tc = tr.getCode().concat("." + i);
                tr.setCode(tc);
                tr.setLevel(i);
                // isGot[-1 不可获得(未解锁||等级未达标) 0 可获得 1 已获得]
                tr.setIsGot(recordList.contains(tc) ? 1 : level >= tr.getLevel() ? step == 0 && tr.getCategory() == 1 ? -1 : 0 : -1);
                if (tr.getCategory() == 0) {
                    writReward.setBaseReward(tr);
                } else {
                    writReward.setStepReward(tr);
                }
            }
            writRewardList.add(writReward);
        }
        
        // 重新封装page[因为等级无上限所以分页无上限]
        PageInfo pageInfo = new PageInfo(writRewardList);
        pageInfo.setTotal((pageNum + 1) * pageSize);
        pageInfo.setPageSize(pageSize);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPages(pageNum + 1);

        return Result.success(pageInfo);
    }
    
    /** 
     * @Description: 获取该页显示的等级
     * @Param: [pageNum, pageSize] 
     * @Return: java.util.List<java.lang.Integer> 
     * @Author: wangyingjie
     * @Date: 2020/11/4 
     */ 
    private List<Integer> getLevelOfPage(Integer pageNum, Integer pageSize) {
        int start = (pageNum - 1) * pageSize + 1;
        int end = start + pageSize;
        List<Integer> levelList = new ArrayList<>();
        for (int i = start; i < end; i++) {
            levelList.add(i);
        }
        return levelList;
    }

    /**
     * @Description: 任务列表
     * @Param: [writ]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/26
     */
    @Transactional
    Result taskList(Writ writ) {
        Long userId = writ.userId;
        String taskCode = writ.getTaskCode();

        // 获取任务列表
        List<Task> taskList = writMapper.getTask(taskCode, 2);

        // 获取任务今日领取记录
        List<String> drList = writMapper.getRecordTemplateCodeList(userId, taskCode, 2, DateUtils.getCurrentDate());
        Map<String, Long> drMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(drList)) {
            drMap = drList.stream().collect(Collectors.groupingBy(k -> k, Collectors.counting()));
        }

        // 获取任务本周领取记录
        List<String> wrList = writMapper.getRecordTemplateCodeList(userId, taskCode, 2, DateUtils.getThisWeekBegin());
        Map<String, Long> wrMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(wrList)) {
            wrMap = wrList.stream().collect(Collectors.groupingBy(k -> k, Collectors.counting()));
        }

        String code;
        int isGot, progress, wr, dr, wc, dc;
        for (Task task : taskList) {
            code = task.getTemplateCode();

            // 本周和本日领奖上限
            wc = task.getWeekCount();
            dc = task.getDayCount();

            // 本周和本日领奖次数
            wr = MapUtils.getIntValue(wrMap, code);
            dr = MapUtils.getIntValue(drMap, code);

            isGot = wr < wc && dr < dc ? -1 : 1;
            progress = wr;

            task.setIsGot(isGot);
            task.setWeekProgress(progress);
            task.setWeekIntegral(progress * task.getIntegral());
        }

        WritTask writTask = writMapper.getWritTask(taskCode, userId);
        writTask.setLine(writTask.getStep() == 1 ? WritEnum.SAIL.VALUE : WritEnum.SBIL.VALUE);
        writTask.setIntegral(taskList.stream().mapToInt(Task::getWeekIntegral).sum());
        writTask.setTaskList(taskList);

        return Result.success(writTask);
    }

    /**
     * @Description: 兑换列表
     * @Param: [writ]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/26
     */
    Result exchangeList(Writ writ) {
        WritExchange writExchange = Optional.ofNullable(writMapper.getWritExchange(writ.getTaskCode(), 3, writ.userId)).orElse(new WritExchange());
        if (CollectionUtils.isNotEmpty(writExchange.getExchangeList())) {
            List<String> boxList = writExchange.getExchangeList().stream().filter(reward -> reward.getCategory() == 1).map(Reward::getRewardCode).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(boxList)) {
                List<Reward> boxRewardList = writMapper.getRewardOfBoxByBoxCodeList(boxList);
                if (CollectionUtils.isNotEmpty(boxRewardList)) {
                    Map<String, List<Reward>> boxRewardMap = boxRewardList.stream().collect(Collectors.groupingBy(Reward::getBoxCode));
                    writExchange.getExchangeList().forEach(reward -> {
                        if(boxRewardMap.containsKey(reward.getRewardCode())) {
                            reward.setBoxRewardList(boxRewardMap.get(reward.getRewardCode()));
                        }
                    });
                }
            }
        }

        return Result.success(writExchange);
    }

    /** 
     * @Description: 排行榜列表
     * @Param: [writ] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/10/26 
     */ 
    Result rankList(Writ writ) {
        Task task = writMapper.getTaskInfo(writ.getTaskCode(), TemplateEnum.RANK.CODE);
        List<WritUser> rankList = writMapper.getRankList(writ.getTaskCode(), 20);

        return Result.success(new HashMap<String, Object>() {
            {
                put("backgroundUrl", task != null ? task.getBackgroundUrl() : "");
                put("rankList", rankList);
            }
        });
    }

    /**
     * @Description: 价格
     * @Param: [writ:[type 1 等级价格 2 进阶价格]]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/26
     */
    @Override
    public Result price(Writ writ) {
        if (Objects.isNull(writ) || Objects.isNull(writ.getType())) {
            return Result.empty();
        }

        String taskCode = getTaskCode();
        if (StringUtils.isBlank(taskCode)) {
            return Result.error(ErrorCode.ACTIVITY_NOT_EXISTS);
        }

        switch (writ.getType()) {
            case 1:
                return Result.success(LEVEL_PRICE);
            case 2:
                List<Map<String, Object>> stepPrice = BeanUtils.deepCopyByJson(STEP_PRICE, ArrayList.class);
                List<Map<String, Object>> stepReward = writMapper.getRewardSimple(taskCode, TemplateEnum.STEP.CODE);
                if (CollectionUtils.isNotEmpty(stepReward)) {
                    stepPrice.get(0).put("stepReward", stepReward.stream().filter(m -> MapUtils.getIntValue(m, "suffix") == 1).findFirst().orElse(null));
                    stepPrice.get(1).put("stepReward", stepReward.stream().filter(m -> MapUtils.getIntValue(m, "suffix") == 2).findFirst().orElse(null));
                }

                return Result.success(stepPrice);
            default:
                return Result.empty();
        }
    }

    /**
     * @Description: 购买
     * @Param: [writ:[type 1 购买等级 2 解锁进阶]]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/26
     */
    @Override
    public Result buy(Writ writ) {
        if (Objects.isNull(writ) || Objects.isNull(writ.userId) || Objects.isNull(writ.getType())) {
            return Result.empty();
        }

        String taskCode = getTaskCode();
        if (StringUtils.isBlank(taskCode)) {
            return Result.error(ErrorCode.ACTIVITY_NOT_EXISTS);
        }

        writ.setTaskCode(taskCode);

        switch (writ.getType()) {
            case 1:
                return buyLevel(writ);
            case 2:
                return buyStep(writ);
            default:
                return Result.empty();
        }
    }

    /**
     * @Description: 购买等级
     * @Param: [writ]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/13
     */
    @Transactional
    public Result buyLevel(Writ writ) {
        if (Objects.isNull(writ.getLevel())) {
            return Result.empty();
        }

        Long userId = writ.userId;
        String taskCode = writ.getTaskCode();
        Integer level = writ.getLevel();
        if (level > 100) {
            return Result.error(201, "一次至多购买100级");
        }

        Integer integral = WritEnum.LI.VALUE;
        Long diamond = (long) -WritEnum.LP.VALUE;

        // 级数*每级钻石
        diamond = level * diamond;

        // 1.扣除钻石
        Result result = currencyService.countDiamond(userId, diamond, WritEnum.LP.NAME);
        if (result.getCode() != 0) {
            return result;
        }

        // 2.加积分等级
        WritUser writUser = writMapper.getWritUser(taskCode, TemplateEnum.A04.CODE, userId);
        writMapper.updateWritUser(new WritUser(userId, taskCode, level + writUser.getLevel(), level * integral + writUser.getIntegral()));

        return Result.success();
    }

    /**
     * @Description: 解锁进阶
     * @Param: [writ]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/14
     */
    public Result buyStep(Writ writ) {
        if (Objects.isNull(writ.getStep()) || (writ.getStep() != 1 && writ.getStep() != 2)) {
            return Result.empty();
        }

        int step = writ.getStep();
        Long userId = writ.userId;
        String taskCode = writ.getTaskCode();

        WritUser writUser = writMapper.getWritUser(taskCode, TemplateEnum.A04.CODE, userId);

        if (step == writUser.getStep()) {
            return Result.error(ErrorCode.IS_UNLOCK);
        }

        WritEnum diamondEnum = step == 1 ? WritEnum.SA : WritEnum.SB;
        WritEnum integralEnum = step == 1 ? WritEnum.SAI : WritEnum.SBI;

        long diamond = (long) -diamondEnum.VALUE;

        // 1.消耗钻石
        Result result = currencyService.countDiamond(userId, diamond, diamondEnum.NAME);
        if (result.getCode() != 0) {
            return result;
        }

        Integer integral = writUser.getIntegral() + integralEnum.VALUE;
        Integer level = writUser.getLevel() + integralEnum.VALUE / WritEnum.LI.VALUE;

        // 2.更新
        writMapper.updateWritUser(new WritUser(userId, taskCode, step, level, integral));

        // 3.专属奖励
        List<Map<String, Object>> stepReward = writMapper.getRewardSimple(taskCode, TemplateEnum.STEP.CODE);
        if (CollectionUtils.isNotEmpty(stepReward)) {
            stepReward = stepReward.stream().filter(m -> MapUtils.getIntValue(m, "suffix") == step).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(stepReward)) {
                addBackpack(userId, stepReward);
            }
        }

        return Result.success();
    }

    /**
     * @Description: 兑换
     * @Param: [writ]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/14
     */
    @Override
    public Result exchange(Writ writ) {
        if (Objects.isNull(writ)
                || Objects.isNull(writ.userId)
                || Objects.isNull(writ.getTemplateCode())
                || Objects.isNull(writ.getNumber())) {
            return Result.empty();
        }

        String taskCode = getTaskCode();
        if (StringUtils.isBlank(taskCode)) {
            return Result.error(ErrorCode.ACTIVITY_NOT_EXISTS);
        }

        Long userId = writ.userId;
        String templateCode = writ.getTemplateCode();
        Integer number = writ.getNumber();
        if (number > 5) {
            return Result.error(201, "一次至多兑换5个");
        }

        // 获取奖励消耗
        Map<String, Object> rewardMap = writMapper.getReward(taskCode, templateCode);
        if (MapUtils.isEmpty(rewardMap)) {
            return Result.error(ErrorCode.REWARD_NOT_EXISTS);
        }

        String rewardCode = MapUtils.getString(rewardMap, "rewardCode");
        int expendTicket = MapUtils.getIntValue(rewardMap, "expendTicket");
        int sumExpendTicket = expendTicket * number;
        int category = MapUtils.getIntValue(rewardMap, "category");

        // 获取用户
        WritUser writUser = writMapper.getWritUser(taskCode, TemplateEnum.A04.CODE, userId);
        if (sumExpendTicket > writUser.getTicket()) {
            return Result.error(ErrorCode.NOT_ENOUGH_TICKET);
        }

        // 更新券
        writMapper.updateWritUser(new WritUser(userId, taskCode, writUser.getTicket() - sumExpendTicket));

        List<Map<String, Object>> resultList = new ArrayList<>();
        if (category == 1) {
            // 获取礼盒内的奖励
            List<Map<String, Object>> rewardList = writMapper.getRewardOfBoxByBoxCode(rewardCode);
            List<Integer> weightList = rewardList.stream().map(m -> MapUtils.getInteger(m, "weight")).collect(Collectors.toList());

            DiscreteMethod method = new DiscreteMethod(weightList);
            // 批量抽奖
            for (int i = 1; i <= number ; i++) {
                resultList.add(rewardList.get(method.next()));
            }
        } else {
            for (int i = 1; i <= number ; i++) {
                resultList.add(rewardMap);
            }
        }

        // 放入背包
        addBackpack(userId, resultList);

        return Result.success(resultList);
    }

    /**
     * @Description: 领取等级奖励
     * @Param: [writ]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/10/27
     */
    @Override
    public Result toGet(Writ writ) {
        if (Objects.isNull(writ) || Objects.isNull(writ.userId) || Objects.isNull(writ.getLevel()) || Objects.isNull(writ.getStep())) {
            return Result.empty();
        }

        String taskCode = getTaskCode();
        if (StringUtils.isBlank(taskCode)) {
            return Result.error(ErrorCode.ACTIVITY_NOT_EXISTS);
        }

        Long userId = writ.userId;
        Integer level = writ.getLevel();
        Integer step = writ.getStep();

        // 是否达到领取奖励的条件
        WritUser writUser = writMapper.getWritUser(taskCode, TemplateEnum.A04.CODE, userId);
        if (level.intValue() > writUser.getLevel().intValue() || step.intValue() > writUser.getStep().intValue()) {
            return Result.error(ErrorCode.REWARD_NO_PERMISSION);
        }

        // 不可重复领取
        WritRecord record = new WritRecord(userId, taskCode, TemplateEnum.D01.CODE, step, level);
        if (writMapper.isExistsRecord(record) != null) {
            return Result.error(ErrorCode.REWARD_IS_GOT);
        }

        Map<String, Object> rewardMap = writMapper.getLevelRewardOfMap(taskCode, TemplateEnum.D01.CODE, step, level > 81 ? 81 : level);
        if (MapUtils.isEmpty(rewardMap) || Objects.isNull(rewardMap.get("rewardCode"))) {
            return Result.error(ErrorCode.NO_DATA);
        }

        String rewardCode = MapUtils.getString(rewardMap, "rewardCode");
        if ("GOLD".equals(rewardCode)) {
            currencyService.countGold(userId, MapUtils.getLong(rewardMap, "number"), "乐享令状领取等级奖励");
        } else if ("PASSCHECK001".equals(rewardCode)) {
            writMapper.updateWritUser(new WritUser(userId, taskCode, writUser.getTicket() + MapUtils.getInteger(rewardMap, "number")));
        } else {
            addBackpack(writ.userId, new ArrayList<Map<String, Object>>() {
                {
                    add(rewardMap);
                }
            });
        }

        writMapper.saveRecord(record);

        return Result.success();
    }


    /**
     * ==========[内部调用]==========
     */


    /**
     * @Description: 日常任务领奖
     * @Param: [userId, templateCode]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/10/29
     */
    @Override
    @Transactional
    public void dailyTask(Long userId, String templateCode) {
        log.info("TASK[TD] Writ dailyTask begin, params[userId:{}, templateCode:{}]", userId, templateCode);
        String taskCode = getTaskCode();
        if (StringUtils.isBlank(taskCode)) {
            log.info("TASK[TD] Writ dailyTask lose");
            return;
        }

        Task task = writMapper.getTaskInfo(taskCode, templateCode);
        if (Objects.isNull(task)) {
            log.info("TASK[TD] Writ dailyTask no task");
            return;
        }

        int dc = task.getDayCount();
        int wc = task.getWeekCount();

        int dr = writMapper.getRecordCount(userId, taskCode, templateCode, DateUtils.getCurrentDate());
        int wr = writMapper.getRecordCount(userId, taskCode, templateCode, DateUtils.getThisWeekBegin());

        if (wr >= wc || dr >= dc) {
            log.info("TASK[TD] Writ dailyTask line");
            return;
        }

        String dayKey = Key.getTaskDay(userId);

        int num = 1;
        if ("V06".equals(templateCode)) {
            // 语音房存储的为分钟数，num为一次完成数
            int minute = (int) redisUtils.getHash(dayKey, templateCode);
            num = dc - (minute / 10 - dr);
            if (num <= 0) {
                log.info("TASK[TD] Writ dailyTask integral is null");
                return;
            }
        }

        Integer integral = writMapper.getIntegralOfDayTask(taskCode, templateCode);
        if (integral == null) {
            log.info("TASK[TD] Writ dailyTask integral is null");
            return;
        }

        WritUser writUser = writMapper.getWritUser(taskCode, TemplateEnum.A04.CODE, userId);
        if (Objects.isNull(writUser)) {
            writMapper.initWritUser(taskCode, userId);
            writUser = writMapper.getWritUser(taskCode, TemplateEnum.A04.CODE, userId);
        }

        integral = num * integral + writUser.getIntegral();

        writMapper.updateWritUser(new WritUser(userId, taskCode, integral / WritEnum.LI.VALUE + 1, integral));

        for (int i = 1; i <= num; i++) {
            writMapper.saveRecord(new WritRecord(userId, taskCode, templateCode));
        }

        log.info("TASK[TD] Writ end");
    }

    /**
     * @Description: 入用户背包
     * @Param: [userId, list]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/10/10
     */
    @Async
    void addBackpack(Long userId, List<Map<String, Object>> list) {
        orderFeign.addBackpack(new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("list", list);
            }
        });
    }

    /**
     * @Description: 获取令状期号
     * @Param: []
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/10/26
     */
    private String getTaskCode() {
        return writMapper.getTaskCode(TemplateEnum.A04.CODE, DateUtils.getCurrentDateTime());
    }
}