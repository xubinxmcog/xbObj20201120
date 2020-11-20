package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.OrderFeign;
import com.enuos.live.mapper.AccountAttachMapper;
import com.enuos.live.pojo.AccountAttach;
import com.enuos.live.result.Result;
import com.enuos.live.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/12
 * @Modified
 */
@Slf4j
@Service
public class RewardServiceImpl implements RewardService {

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private ExpService expService;

    @Autowired
    private ActiveService activeService;

    @Autowired
    private BadgeService badgeService;

    @Autowired
    private AccountAttachMapper accountAttachMapper;

    /**
     * @Description: 奖励处理
     * @Param: [userId, list]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result handler(Long userId, List<Map<String, Object>> list) {
        log.info("==========[Reward handler begin : [userId = {}, list = {}]]==========", userId, list);

        if (CollectionUtils.isEmpty(list)) {
            log.error("List is empty");
            throw new RuntimeException("no reward");
        }

        AccountAttach attach = accountAttachMapper.getByUserId(userId);

        int active = 0;
        long experience = 0;

        int gashapon = attach.getGashapon();
        long diamond = attach.getDiamond();
        long gold = attach.getGold();

        List<Map<String, Object>> rewardList = new ArrayList<>();

        for (Map rMap : list) {
            String rewardCode = MapUtils.getString(rMap, "rewardCode");
            long number = MapUtils.getLongValue(rMap, "number");
            switch (rewardCode) {
                case "DIAMOND":
                    diamond += number;
                    break;
                case "GOLD":
                    gold += number;
                    break;
                case "GASHAPON":
                    gashapon += number;
                    break;
                case "EXP":
                    experience += number;
                    break;
                case "ACTIVE":
                    active += number;
                    break;
                default:
                    rewardList.add(rMap);
                    break;
            }
        }

        attach.setDiamond(diamond);
        attach.setGold(gold);
        attach.setGashapon(gashapon);

        if (experience != 0) {
            Map<String, Object> map = expService.level(attach.getLevel(), attach.getExperience(), experience);
            if (MapUtils.isEmpty(map)) {
                return Result.error(ErrorCode.DATA_ERROR);
            }

            attach.setLevel(MapUtils.getInteger(map, "level"));
            attach.setExperience(MapUtils.getLong(map, "experience"));
        }

        // 1更新钻石，金币，等级，经验
        accountAttachMapper.update(attach);

        // 2更新活跃度
        if (active != 0) {
            activeService.countActive(userId, active);
        }

        if (CollectionUtils.isNotEmpty(rewardList)) {
            // 3更新徽章
            List<Map<String, Object>> badgeList = rewardList.stream().filter(reward -> MapUtils.getString(reward, "rewardCode").startsWith("B")).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(badgeList)) {
                badgeService.batchSave(userId, badgeList);
            }

            // 4更新券&装饰
            List<Map<String, Object>> otherList = rewardList.stream().filter(reward -> !MapUtils.getString(reward, "rewardCode").startsWith("B")).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(otherList)) {
                orderFeign.addBackpack(new HashMap<String, Object>() {
                    {
                        put("userId", userId);
                        put("list", otherList);
                    }
                });
            }
        }

        log.info("==========[Reward handler end]==========");

        return Result.success();
    }

}
