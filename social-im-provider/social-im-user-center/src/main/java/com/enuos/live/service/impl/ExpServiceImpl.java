package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.CurrencyMapper;
import com.enuos.live.mapper.DayExpMapper;
import com.enuos.live.mapper.ExpMapper;
import com.enuos.live.pojo.AccountAttach;
import com.enuos.live.pojo.Threshold;
import com.enuos.live.result.Result;
import com.enuos.live.service.ExpService;
import com.enuos.live.utils.BigDecimalUtil;
import com.enuos.live.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description 等级经验
 * @Author wangyingjie
 * @Date 2020/5/18
 * @Modified
 */
@Slf4j
@Service
public class ExpServiceImpl implements ExpService {

    /** 普通最大经验值 */
    private static long NORMAL;

    /** 会员最大经验值 */
    private static long MEMBER;

    /** 最大等级 */
    private static int LEVEL_MAX;

    /** 等级经验映射 */
    private static Map<Integer, Long> LEVEL_EXP;

    @Autowired
    private ExpMapper expMapper;

    @Autowired
    private DayExpMapper dayExpMapper;

    @Autowired
    private CurrencyMapper currencyMapper;

    /**
     * @Description: 等级经验
     * @Param: [accountAttach]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/5/21
     */
    @Override
    @Transactional
    public Result gameHandler(AccountAttach accountAttach) {
        Long userId = accountAttach.getUserId();
        Long gold = accountAttach.getGold();
        // 获取阈值
        if (Objects.isNull(NORMAL) || Objects.isNull(MEMBER) || Objects.isNull(LEVEL_MAX) || MapUtils.isEmpty(LEVEL_EXP)) {
            init();
        }

        // 获取当前用户等级，经验值
        AccountAttach current = expMapper.getUserLevelExp(userId);

        // 更新金币
        if (!Objects.isNull(gold) && gold != 0) {
            currencyMapper.updateGold(userId, BigDecimalUtil.nAdd(current.getGold(), gold));
        }

        // 用户经验阈值
        long limit = current.getIsMember() == 1 ? MEMBER : NORMAL;
        // 可获得的最大经验不可超过阈值
        long exp = accountAttach.getExperience() > limit ? limit : accountAttach.getExperience();
        // 当前经验值
        long dayExp = current.getDayExp();

        LocalDateTime today = DateUtils.getCurrentDateTime();

        if (current.getUpdateTime().toLocalDate().isEqual(today.toLocalDate())) {
            if (dayExp >= limit) {
                return Result.error(ErrorCode.EXP_GAME_TODAY_MAX);
            } else {
                dayExp += exp;
            }
        } else {
            dayExp = exp;
        }

        // 获取实际获得的经验
        if (dayExp > limit) {
            exp = limit + exp - dayExp;
            dayExp = limit;
        }

        dayExpMapper.update(userId, dayExp, today);

        Map<String, Object> map = level(current.getLevel(), current.getExperience(), exp);

        current.setExperience(MapUtils.getLong(map, "experience"));
        current.setLevel(MapUtils.getInteger(map, "level"));

        expMapper.update(current);

        AccountAttach result = expMapper.getUserLevelExp(userId);
        result.setRemainderExp(limit - result.getDayExp());

        return Result.success(result);
    }

    /**
     * @Description: 游戏今日已得经验
     * @Param: [accountAttach]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/5/21
     */
    @Override
    @Transactional
    public Result gameToday(AccountAttach accountAttach) {
        if (Objects.isNull(accountAttach) || Objects.isNull(accountAttach.userId)) {
            return Result.empty();
        }

        Map<String, Object> result = new HashMap() {
            {
                put("tgExp", 0);
            }
        };

        Long userId = accountAttach.userId;

        AccountAttach current = expMapper.getTodayGameExp(userId);
        if (Objects.isNull(current)) {
            dayExpMapper.initDayExp(userId);
            return Result.success(result);
        } else {
            LocalDateTime today = DateUtils.getCurrentDateTime();

            if (current.getUpdateTime().toLocalDate().isEqual(today.toLocalDate())) {
                result.put("tgExp", current.getDayExp());
            } else {
                dayExpMapper.update(userId, 0L, today);
            }
        }

        return Result.success(result);
    }

    /**
     * @Description: 计算经验
     * @Param: [userId, experience]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/12
     */
    @Override
    @Transactional
    public Result countExp(Long userId, Long experience) {
        if (experience == null || experience == 0) {
            return Result.success();
        }

        AccountAttach current = expMapper.getLevelExp(userId);
        if (Objects.isNull(current)) {
            return Result.error(ErrorCode.DATA_ERROR);
        }

        Integer cLevel = current.getLevel();

        Map<String, Object> map = level(cLevel, current.getExperience(), experience);

        current.setExperience(MapUtils.getLong(map, "experience"));
        current.setLevel(MapUtils.getInteger(map, "level"));

        int result = expMapper.update(current);

        return result > 0 ? Result.success() : Result.error();
    }

    /**
     * ==========[内部调用]==========
     */

    /**
     * @Description: 计算经验
     * @Param: [level, experience, addExperience]
     * @Return: java.util.Map<java.lang.String , java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/9/28
     */
    @Override
    public Map<String, Object> level(Integer level, Long experience, Long addExperience) {
        if (level == null || experience == null || addExperience == null) {
            return null;
        }

        long sumExp = experience + addExperience;
        while (sumExp >= LEVEL_EXP.get(level)) {
            // 升级以后的经验
            sumExp = sumExp - LEVEL_EXP.get(level);
            level++;
            if (level > LEVEL_MAX) {
                sumExp = LEVEL_EXP.get(LEVEL_MAX);
                level = LEVEL_MAX;
                break;
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("level", level);
        map.put("experience", sumExp);

        return map;
    }

    /** [PRIVATE] */

    /**
     * @Description: 初始化设置
     * @Param: []
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @PostConstruct
    private void init() {
        List<Threshold> allList = expMapper.getThreshold();
        LEVEL_EXP = new HashMap<>();
        allList.forEach(all -> {
            switch (all.getCodeType()) {
                case 0:
                    LEVEL_MAX = all.getThreshold().intValue();
                    break;
                case 1:
                    LEVEL_EXP.put(all.getCode(), all.getThreshold());
                    break;
                case 2:
                    if (all.getCode() == 0) {
                        NORMAL = all.getThreshold();
                    }
                    if (all.getCode() == 1) {
                        MEMBER = all.getThreshold();
                    }
                    break;
                default:
                    break;
            }
        });
    }
}
