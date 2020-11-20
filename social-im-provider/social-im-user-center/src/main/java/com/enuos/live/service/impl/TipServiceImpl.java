package com.enuos.live.service.impl;

import com.enuos.live.constant.Constellation;
import com.enuos.live.constants.RedisKey;
import com.enuos.live.manager.PatternEnum;
import com.enuos.live.manager.TaskEnum;
import com.enuos.live.mapper.TipMapper;
import com.enuos.live.pojo.Task;
import com.enuos.live.pojo.Tip;
import com.enuos.live.result.Result;
import com.enuos.live.service.MemberService;
import com.enuos.live.service.TipService;
import com.enuos.live.utils.DateUtils;
import com.enuos.live.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 红点提示
 * @Author wangyingjie
 * @Date 2020/8/18
 * @Modified
 */
@Slf4j
@Service
public class TipServiceImpl implements TipService {

    @Autowired
    private MemberService memberService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private TipMapper tipMapper;

    /**
     * @Description: 是否提示
     * @Param: [tip]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/18
     */
    @Override
    public Result isTip(Tip tip) {
        Long userId = tip.userId;
        // 提示类别[1：每日签到；2：星座签到；3：活跃；4：日常；5：成就；6：扭蛋抽奖；7：扭蛋兑换；8：等级；9：邀请；10：活动]
        List<String> categoryList = tip.getCategoryList();

        LocalDate localDate = DateUtils.getCurrentDate();

        Map<String, Boolean> map = new HashMap<>();

        for (String category : categoryList) {
            switch (category) {
                case "1":
                    map.put(category, tip1(userId, localDate));
                    break;
                case "2":
                    map.put(category, tip2(userId, localDate));
                    break;
                case "3":
                    map.put(category, tip3(userId, localDate));
                    break;
                case "4":
                    map.put(category, tip4(userId, localDate));
                    break;
                case "5":
                    map.put(category, tip5(userId));
                    break;
                case "8":
                    map.put(category, tip8(userId));
                    break;
                default:
                    break;
            }
        }

        return Result.success(map);
    }

    /**
     * @Description: 1.今日是否签到
     * @Param: [userId, localDate]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    private boolean tip1(Long userId, LocalDate localDate) {
        // 今日是否签到
        return tipMapper.isSign(userId, localDate) == null ? true : false;
    }

    /**
     * @Description: 2.星座签到奖励未领取提示
     * @Param: [userId, localDate]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    private boolean tip2(Long userId, LocalDate localDate) {
        String code = Constellation.getCode(localDate);
        String prefix = String.valueOf(localDate.getYear()).concat("_");
        Integer signCount = tipMapper.getSignCount(userId, prefix.concat(code));
        if (signCount == null) {
            return true;
        }

        return isGot(userId, signCount, prefix, code);
    }

    /**
     * @Description: 3.活跃奖励未领取提示
     * @Param: [userId, localDate]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/8/18
     */
    private boolean tip3(Long userId, LocalDate localDate) {
        boolean d, w;
        // 每日
        Integer dayActive = Optional.ofNullable(tipMapper.getActiveOfDay(userId, localDate)).orElse(0);
        d = isGot(userId, dayActive, DateUtils.getPrefixOfCurrentDate(localDate), TaskEnum.DAT0001.getCode());

        // 每周
        Integer weekActive = Optional.ofNullable(tipMapper.getActiveOfWeek(userId, DateUtils.getThisWeekBegin(localDate), DateUtils.getThisWeekEnd(localDate))).orElse(0);
        w = isGot(userId, weekActive, DateUtils.getPrefixOfCurrentDate(DateUtils.getThisWeekBegin(localDate)), TaskEnum.WAT0001.getCode());

        return d || w ? true : false;
    }

    /** 
     * @Description: 4.每日任务是否领奖
     * @Param: [userId, localDate] 
     * @Return: boolean 
     * @Author: wangyingjie
     * @Date: 2020/8/18 
     */ 
    private boolean tip4(Long userId, LocalDate localDate) {
        // 获取每日任务
        String key = RedisKey.KEY_TASK_CENTER.concat(localDate.format(DateTimeFormatter.ofPattern(PatternEnum.YYYYMMDD.getPattern())));
        if (!redisUtils.hasKey(key)) {
            return true;
        }

        List<Task> taskList = (List<Task>) redisUtils.get(key);
        String prefix = DateUtils.getPrefixOfCurrentDate(localDate);
        List<String> codeList = taskList.stream().map(a -> prefix.concat(a.getCode())).collect(Collectors.toList());

        // 筛选出未领奖的任务
        List<String> gotCodeList = tipMapper.getTaskRecord(userId, codeList);
        if (CollectionUtils.isNotEmpty(gotCodeList)) {
            codeList = codeList.stream().filter(a -> !gotCodeList.contains(a)).collect(Collectors.toList());
        }

        // 每日任务未领奖
        if (codeList.contains(prefix.concat(TaskEnum.DLT0001.getCode()))) {
            return true;
        }

        // 会员每日未领奖
        Integer isMember = memberService.isMember(userId);
        if (isMember == 1 && codeList.contains(prefix.concat(TaskEnum.PGT0035.getCode()))) {
            return true;
        }

        // 获取今日进度筛选未领奖的
        return isGot(userId, codeList);

    }

    /** 
     * @Description: 5.成就奖励是否领取
     * @Param: [userId] 
     * @Return: boolean 
     * @Author: wangyingjie
     * @Date: 2020/8/19 
     */ 
    private boolean tip5(Long userId) {
        List<String> codeList = tipMapper.getTaskCode(5);
        return isGot(userId, codeList);
    }

    /**
     * @Description: 8.等级奖励是否领取
     * @Param: [userId]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/8/19
     */
    private boolean tip8(Long userId) {
        // 获取当前等级
        Integer level = tipMapper.getLevel(userId);
        if (level == 1) {
            return false;
        }

        List<String> codeList = new ArrayList<>();
        while (level > 1) {
            codeList.add(TaskEnum.LVT0001.getCode().concat("_").concat(String.valueOf(level)));
            level--;
        }

        return tipMapper.isGotReward(userId, codeList) < codeList.size() ? true : false;
    }

    /**
     * @Description: 任务达成是否领取
     * @Param: [userId, codeList]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/8/19
     */
    private boolean isGot(Long userId, List<String> codeList) {
        List<Map<String, Object>> list = tipMapper.getTaskFollow(userId, codeList);
        if (CollectionUtils.isEmpty(list)) {
            return false;
        }

        list = list.stream().filter(a -> MapUtils.getIntValue(a, "progress") >= Integer.parseInt(StringUtils.substringAfterLast(MapUtils.getString(a, "code"), "_"))).collect(Collectors.toList());

        return CollectionUtils.isEmpty(list) ? false : true;
    }

    /**
     * @Description: 任务达成是否领取
     * @Param: [userId, value, prefix, code]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/8/18
     */
    private boolean isGot(Long userId, Integer value, String prefix, String code) {
        List<Integer> suffixList = tipMapper.getSuffix(code);
        List<String> codeList = new ArrayList<>();
        for (Integer suffix : suffixList) {
            if (value >= suffix) {
                codeList.add(prefix.concat(code).concat("_").concat(String.valueOf(suffix)));
            }
        }

        if (CollectionUtils.isEmpty(codeList)) {
            return false;
        }

        return tipMapper.isGotReward(userId, codeList) < codeList.size() ? true : false;
    }

}
