package com.enuos.live.service.impl;

import com.enuos.live.constants.RedisKey;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.manager.GashaponEnum;
import com.enuos.live.manager.PatternEnum;
import com.enuos.live.mapper.GashaponMapper;
import com.enuos.live.pojo.Task;
import com.enuos.live.result.Result;
import com.enuos.live.service.CurrencyService;
import com.enuos.live.service.GashaponService;
import com.enuos.live.service.RewardService;
import com.enuos.live.utils.DateUtils;
import com.enuos.live.utils.RedisUtils;
import com.enuos.live.utils.page.PageInfo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/19
 * @Modified
 */
@Slf4j
@Service
public class GashaponServiceImpl implements GashaponService {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RewardService rewardService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private GashaponMapper gashaponMapper;

    /**
     * @Description: 扭蛋数
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/22
     */
    @Override
    public Result num(Task task) {
        if (task == null) {
            return Result.empty();
        }

        Map<String, Object> result = gashaponMapper.getNum(task.getUserId());

        return Result.success(result);
    }

    /**
     * @Description: 列表
     * @Param: [task]
     * @Return: Result
     * @Author: wangyingjie
     * @Date: 2020/6/19
     */
    @Override
    public Result lotteryList(Task task) {
        if (task == null) {
            return Result.empty();
        }

        Long userId = task.getUserId();
        String prefix = getDayPrefix();
        int currtime = LocalTime.now().toSecondOfDay();

        List<Map<String, Object>> list = gashaponMapper.getLotteryList(userId, prefix, currtime);

        if (CollectionUtils.isEmpty(list)) {
            log.info("gashapon not started");
            return Result.success(201, "活动暂未开始敬请期待");
        }

        // 过滤掉开奖任务倒计时小于5s
        list = list.stream().filter(m -> MapUtils.getIntValue(m, "type") == 0 || (MapUtils.getIntValue(m, "type") == 1 && MapUtils.getIntValue(m, "countdown") >= 5)).collect(Collectors.toList());
        // 模拟参与人数+实际参与人数
        addJoinNum(list);

        return Result.success(list);
    }

    /**
     * @Description: 参与
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/22
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result join(Task task) {
        String prefix = getDayPrefix();

        Long userId = task.getUserId();
        Integer joinCount = task.getJoinCount();
        String code = task.getCode();
        String[] codes = code.split("_");

        if (codes.length != 2) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "error code");
        }

        // 获取参与阈值
        Map<String, Object> map = gashaponMapper.getSettingsByCode(userId, codes[0], Integer.valueOf(codes[1]));
        if (MapUtils.isEmpty(map)) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "can not get settings");
        }

        // 参与次数阈值
        if (joinCount <= 0 || joinCount > MapUtils.getIntValue(map, "joinCountLine")) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "gashapon join error");
        }

        // 活动时间左开右闭
        int currentTime = LocalTime.now().toSecondOfDay();
        if (currentTime < MapUtils.getIntValue(map, "startTime") || currentTime >= MapUtils.getIntValue(map, "endTime")) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "gashapon not started");
        }

        String finalCode = prefix.concat(code);

        // 参与次数验证
        Map<String, Object> joinInfo = gashaponMapper.isJoin(userId, finalCode);
        int result;
        if (MapUtils.isEmpty(joinInfo)) {
            result = gashaponMapper.saveGashaponRecord(userId, finalCode, joinCount, -1, LocalDateTime.of(LocalDate.now(), LocalTime.ofSecondOfDay(Long.valueOf(codes[1]))));
        } else {
            int lastJoinCount = MapUtils.getIntValue(joinInfo, "joinCount");
            int finalJoinCount = lastJoinCount + joinCount;
            if (finalJoinCount > MapUtils.getIntValue(map, "joinCountLine")) {
                return Result.error(ErrorCode.EXCEPTION_CODE, "参与次数已用完");
            }

            result = gashaponMapper.updateGashaponRecord(userId, finalCode, finalJoinCount);
        }

        // 减扭蛋
        if (result > 0) {
            Result cResult = currencyService.countGashapon(userId, Math.negateExact(MapUtils.getIntValue(map, "expend") * joinCount));
            if (cResult.getCode() != 0) {
                throw new RuntimeException(cResult.getMsg());
            } else {
                return Result.success();
            }
        } else {
            return Result.error();
        }

    }

    /**
     * @Description: 获取开奖结果
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/24
     */
    @Override
    public Result result(Task task) {
        String finalCode = getDayPrefix().concat(task.getCode());
        Integer result = gashaponMapper.getResult(task.getUserId(), finalCode);

        return Result.success(new HashMap() {
            {
                put("result", result == null ? -1 : result);
            }
        });
    }

    /**
     * @Description: 兑换列表
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/24
     */
    @Override
    public Result exchangeList(Task task) {
        if (task == null || task.getUserId() == null) {
            return Result.empty();
        }

        // 获取前缀 本周一
        String prefix = getWeekPrefix();
        List<Map<String, Object>> resultList = gashaponMapper.getExchangeList(task.getUserId(), prefix);

        return Result.success(resultList);
    }

    /**
     * @Description: 兑换
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/24
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result exchange(Task task) {
        Long userId = task.getUserId();
        String code = task.getCode();

        String prefix = getWeekPrefix();
        String finalCode = prefix.concat(code);

        // 查询阈值
        Map<String, Object> map = gashaponMapper.getSettingsForExchange(code);
        // 查询本周参与次数
        Map<String, Object> joinInfo = gashaponMapper.isJoin(userId, finalCode);
        int result;
        if (MapUtils.isEmpty(joinInfo)) {
            result = gashaponMapper.saveGashaponRecord(userId, finalCode, 1, 1, LocalDateTime.now());
        } else {
            int joinCount = MapUtils.getIntValue(joinInfo, "joinCount");
            if (joinCount + 1 > MapUtils.getIntValue(map, "joinCountLine")) {
                return Result.error(201, "参与次数已用完");
            }
            result = gashaponMapper.updateGashaponRecord(userId, finalCode, ++joinCount);
        }

        // 减扭蛋
        if (result > 0) {
            Result cResult = currencyService.countGashapon(userId, Math.negateExact(MapUtils.getIntValue(map, "expend")));
            if (cResult.getCode() != 0) {
                throw new RuntimeException(cResult.getMsg());
            }
        } else {
            return Result.error();
        }

        // 加奖励
        List<Map<String, Object>> list = gashaponMapper.getReward(code);
        if (CollectionUtils.isEmpty(list)) {
            throw new RuntimeException("can not get reward");
        }

        rewardService.handler(userId, list);

        return Result.success();
    }

    /**
     * @Description: 中奖记录
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/28
     */
    @Override
    public Result lotteryRecordPage(Task task) {
        if (task == null || task.getUserId() == null) {
            return Result.empty();
        }

        PageHelper.startPage(task.pageNum, task.pageSize);

        List<Map<String, Object>> list = gashaponMapper.lotteryRecordPage(task.getUserId());

        if (CollectionUtils.isNotEmpty(list)) {
            int current = LocalTime.now().toSecondOfDay();
            list.forEach(m -> m.put("countdown", MapUtils.getInteger(m, "result") == -1 && MapUtils.getLong(m,"endTime") - current >= 5 ? MapUtils.getLong(m,"endTime") - current : 0));
        }

        return Result.success(new PageInfo<>(list));
    }

    /**
     * @Description: 兑换记录
     * @Param: [task]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/28
     */
    @Override
    public Result exchangeRecordPage(Task task) {
        if (task == null || task.getUserId() == null) {
            return Result.empty();
        }

        PageHelper.startPage(task.pageNum, task.pageSize);

        List<Map<String, Object>> list = gashaponMapper.exchangeRecordPage(task.getUserId());

        return Result.success(new PageInfo<>(list));
    }

    /**
     * @Description: 获取日期前缀yyyyMMdd_
     * @Param: []
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/6/12
     */
    private String getDayPrefix() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(PatternEnum.YYYYMMDD.getPattern())).concat("_");
    }

    /**
     * @Description: 获取日期前缀yyyyMMdd_
     * @Param: []
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/6/12
     */
    private String getWeekPrefix() {
        return DateUtils.getThisWeekBegin().format(DateTimeFormatter.ofPattern(PatternEnum.YYYYMMDD.getPattern())).concat("_");
    }

    /**
     * @Description: 加人数
     * @Param: [list]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/27
     */
    private void addJoinNum(List<Map<String, Object>> list) {
        list.forEach(m -> m.put("joinNum", MapUtils.getIntValue(m, "joinNum") + getJoinNumByRobot(MapUtils.getString(m, "code"))));
    }

    /**
     * @Description: 获取参与人数[机器人]
     * @Param: [code]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/7/27
     */
    private Integer getJoinNumByRobot(String code) {
        String key = RedisKey.KEY_GASHAPON_ROBOT.concat(LocalDate.now().format(DateTimeFormatter.ofPattern(PatternEnum.YYYYMMDD.getPattern()))).concat(":").concat(code);

        if (!redisUtils.hasKey(key)) {
            String k = code.split("_")[0];
            GashaponEnum gashaponEnum = GashaponEnum.valueOf(k);
            redisUtils.set(key, RandomUtils.nextInt(gashaponEnum.getMaxJoinNum()) + gashaponEnum.getMinJoinNum(), 1, TimeUnit.DAYS);
        }

        return (Integer) redisUtils.get(key);
    }
}